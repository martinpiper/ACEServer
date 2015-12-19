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
	public static final String ACE_BUILDS_MASTER = "/ace-builds-master";
	public static final String DATA = "/data";
	public static final String STOP = "/stop";
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

			// Loop through the callbacks until one of them returns an InputStream
			for (ACEServerCallback callback : callbacks)
			{
				String trimmed = uri.substring(ACE_BUILDS_MASTER.length());
				input = callback.beforeGet(makePathSafe(trimmed));
				if (null != input)
				{
					break;
				}
			}

			// No input yet? Then try other methods to get the input data to return
			if (null == input)
			{
				try
				{
					// Try getting a local file first from the current directory
					input = new FileInputStream(makePathSafe(uri.substring(1)));    // Trim off the first '/'
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
							input = new FileInputStream("target/" + makePathSafe(uri.substring(ACE_BUILDS_MASTER.length())));
						}
						catch (Exception e3)
						{
							try
							{
								// Try the current directory minus the "/ace-builds-master/"
								input = new FileInputStream(makePathSafe(uri.substring(ACE_BUILDS_MASTER.length()+1)));
							}
							catch (Exception e4)
							{
								// Lastly try the packaged resources
								input = ACEServer.class.getResourceAsStream("/" + makePathSafe(uri));
							}
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
			uri = uri.substring(DATA.length()+1);
			OutputStream out = new FileOutputStream(makePathSafe(uri));
			IOUtils.copy(t.getRequestBody(),out);
			out.close();

			for (ACEServerCallback callback : callbacks)
			{
				callback.afterPut(uri);
			}

			// Send the response after the callbacks
			t.sendResponseHeaders(200, 0);
			t.close();
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
			public InputStream beforeGet(String uri)
			{
				System.out.println("beforeGet " + uri);
				// An example of what to do to return runtime generated data
/*
				if (uri.contains("moo.feature"))
				{
					String exampleString = "Hello world\n";
					return new ByteArrayInputStream(exampleString.getBytes(StandardCharsets.UTF_8));
				}
*/
				return null;
			}

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
		server.createContext(ACE_BUILDS_MASTER, new MyHandler());
		server.createContext(STOP, new HttpHandler()
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
		server.createContext(DATA, new MyHandlerPut());

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
