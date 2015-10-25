package com.replicanet;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 */
public class ACEServer
{
	static HttpServer server;
	static Set<ACEServerCallback> callbacks = new LinkedHashSet<ACEServerCallback>();

	static class MyHandler implements HttpHandler
	{
		public void handle(HttpExchange t) throws IOException
		{
			String uri = t.getRequestURI().getPath();
			if (System.getProperty("com.replicanet.ACEServer.debug.requests") != null)
			{
				System.out.println(uri);
			}
			InputStream input = null;
			try
			{
				// Try getting a local file first from the current directory
				input = new FileInputStream(makePathSafe(uri.substring(1)));	// Trim off the first '/'
			}
			catch (Exception e)
			{
				try
				{
					// Try "src/main/resources"...  from the current directory
					input = new FileInputStream("src/main/resources/" + makePathSafe(uri));
				}
				catch (Exception e2)
				{
					try
					{
						// Try "target/"...  from the current directory
						input = new FileInputStream("target/" + makePathSafe(uri.substring(18)));
					}
					catch (Exception e3)
					{
						try
						{
							// Try the current directory minus the "/ace-builds-master/"
							input = new FileInputStream(makePathSafe(uri.substring(19)));
						}
						catch (Exception e4)
						{
							// Lastly try the packaged resources
							input = ACEServer.class.getResourceAsStream("/" + makePathSafe(uri));
						}
					}
				}
			}

			t.sendResponseHeaders(200, input.available());
			OutputStream os = t.getResponseBody();

			IOUtils.copy(input, os);

			os.close();
			input.close();

			for (ACEServerCallback callback : callbacks)
			{
				callback.afterGet(uri);
			}
		}
	}


	static class MyHandlerPut implements HttpHandler
	{
		public void handle(HttpExchange t) throws IOException
		{
			String uri = t.getRequestURI().getPath();
			System.out.println(uri);

			// Writes the file from the online editor
			// MPi: TODO: Do not allow directory scanning to parents, this means removing ".." and making sure "/" is not the first in the path
			OutputStream out = new FileOutputStream(makePathSafe(uri.substring(6)));
			IOUtils.copy(t.getRequestBody(),out);
			out.close();

			for (ACEServerCallback callback : callbacks)
			{
				callback.afterPut(uri);
			}
		}
	}

	static String makePathSafe(String path)
	{
		String ret = path.replace(".." , "");
		while (ret.startsWith("/") || ret.startsWith("\\"))
		{
			ret = ret.substring(1);
		}
		return ret;
	}

	public static void main(String[] args) throws Exception
	{
		startServer(new InetSocketAddress(8000));
		addCallback(new ACEServerCallback()
		{
			public void afterGet(String uri)
			{
				System.out.println("afterGet " + uri);
			}

			public void afterPut(String uri)
			{
				System.out.println("afterPut " + uri);
			}
		});

		System.out.println("http://localhost:8000/ace-builds-master/demo/autocompletion.html?filename=t1.feature");
		System.out.println("http://localhost:8000/ace-builds-master/demo/autocompletion.html?filename=t2.feature");
		System.out.println("http://localhost:8000/ace-builds-master/demo/autocompletion.html");
		System.out.println("http://localhost:8000/ace-builds-master/kitchen-sink.html");
		System.out.println("http://localhost:8000/stop/");
	}

	public static void startServer(InetSocketAddress listenAddress) throws IOException
	{
		server = HttpServer.create(listenAddress, 0);
		server.createContext("/ace-builds-master", new MyHandler());
		server.createContext("/stop", new HttpHandler()
		{
			public void handle(HttpExchange t) throws IOException
			{
				t.sendResponseHeaders(200, 0);
				OutputStream os = t.getResponseBody();
				os.close();
				server.stop(0);
			}
		});

		// var xhr = new XMLHttpRequest(); xhr.open("PUT" , "/data/t.feature" , true); xhr.send(editor.getValue());
		server.createContext("/data", new MyHandlerPut());

		server.setExecutor(null);
		server.start();
	}

	public static void stopServer() throws IOException
	{
		if (null != server)
		{
			server.stop(0);
		}
	}

	public static void addCallback(ACEServerCallback callback)
	{
		callbacks.add(callback);
	}

	public static void removeCallback(ACEServerCallback callback)
	{
		callbacks.remove(callback);
	}
}
