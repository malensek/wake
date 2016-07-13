package io.sigpipe.wake.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WebServer {

    private HttpServer http;

    public WebServer(int port) throws IOException {
        http = HttpServer.create(new InetSocketAddress(port), 0);
        http.createContext("/", new RequestHandler());

    }

    public void listen() {
        System.out.println("Listening on port "
                + http.getAddress().getPort() + "...");
        http.start();
    }

    private class RequestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            System.out.println("Request: " + t.getRequestURI());
            String response = "This is the response";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    public static void main(String[] args) throws Exception {
        WebServer ws = new WebServer(8000);
        ws.listen();
    }

}
