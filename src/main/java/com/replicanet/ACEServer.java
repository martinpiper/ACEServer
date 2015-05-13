package com.replicanet;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 *
 */
public class ACEServer
{
	static HttpServer server;

	static class MyHandler implements HttpHandler
	{
		public void handle(HttpExchange t) throws IOException
		{
			String uri = t.getRequestURI().getPath();
			InputStream input = ACEServer.class.getResourceAsStream(uri);

			t.sendResponseHeaders(200, input.available());
			OutputStream os = t.getResponseBody();

			IOUtils.copy(input, os);

			os.close();
		}
	}

	public static void main(String[] args) throws Exception
	{
		server = HttpServer.create(new InetSocketAddress(8000), 0);
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
		server.setExecutor(null);
		server.start();

		System.out.println("http://localhost:8000/ace-builds-master/kitchen-sink.html");
	}
}
