package wasdev.sample.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

@WebServlet("/counter")
public class ServletCounter extends HttpServlet {

	private static Jedis jedis;

	public ServletCounter() throws JSONException {
		jedis = getJedis();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter writer = resp.getWriter();
		System.out.println("Inside counter servlet's GET method");

		writer.write("This servlet is called " + accessCount() + " times");
	}

	private int accessCount() {
		String value = jedis.get("Counter");
		int counter = 1;
		if (value != null) {
			counter = Integer.parseInt(value);
			counter++;
		}

		jedis.set("counter", Integer.toString(counter));

		return counter;
	}

	private Jedis getJedis() throws JSONException {
		JSONObject service = getServiceByName("redis-java-demo");

		JSONObject creds = service.getJSONObject("credentials");

		String host = creds.getString("host");
		int port = creds.getInt("port");
		String password = creds.getString("password");

		JedisPool pool = new JedisPool(new JedisPoolConfig(), host, port, Protocol.DEFAULT_TIMEOUT, password);
		return pool.getResource();
	}

	private JSONObject getServiceByName(String name) throws JSONException {
		JSONObject service = null;
		JSONObject envService = new JSONObject(System.getenv("VCAP_SERVICES"));
		Iterator<String> keys = envService.keys();
		while (keys.hasNext()) {
			String next = keys.next();
			JSONArray jsonArray = envService.getJSONArray(next);
			for (int i = 0; i < jsonArray.length(); i++) {
                 JSONObject tempService=jsonArray.getJSONObject(i);
                 if(name.equals(tempService.getString("name"))){
                	 service=tempService;
                	 break;
                 }
			}

			if (service != null) {
				break;
			}

		}

		return service;
	}
}
