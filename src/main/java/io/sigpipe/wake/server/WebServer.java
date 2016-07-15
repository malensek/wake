package io.sigpipe.wake.server;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;

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
            File localFile = new File("." + t.getRequestURI());
            if (localFile.isDirectory()) {
                localFile = new File(localFile.getAbsolutePath() + "/index.html");
            }
            System.out.println("Local file: " + localFile.getAbsolutePath());
            if (localFile.exists() == false) {
                String response = "File not found";
                t.sendResponseHeaders(404, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                t.sendResponseHeaders(200, localFile.length());
                OutputStream os = t.getResponseBody();
                Files.copy(localFile.toPath(), os);
                os.close();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        WebServer ws = new WebServer(8000);
        ws.listen();
    }

}
