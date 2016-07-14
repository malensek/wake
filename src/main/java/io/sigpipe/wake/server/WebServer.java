package io.sigpipe.wake.server;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

public class WebServer {

    private HttpServer http;

    public WebServer(int port) throws IOException {
        http = HttpServer.create(new InetSocketAddress(port), 0);
        http.createContext("/", new RequestHandler());
    }

    public void listen() {
        int port = http.getAddress().getPort();
        System.out.println("Listening on port " + port + "...");
        http.start();
    }

    public static void main(String[] args) throws IOException {
        WebServer ws = new WebServer(8000);
        ws.listen();
    }

}
