package io.sigpipe.wake.server;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.Arrays;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

class RequestHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String uri = exchange.getRequestURI().getPath();
//        if (method != "GET") {
//            return;
//        }

        System.out.println(method + " " + uri);

        File localFile = new File("." + uri);
        if (localFile.isDirectory() && uri.endsWith("/") == false) {
            /* Redirect to URL with trailing slash */
            uri += "/";
            System.out.println("Redirecting to: " + uri);
            String message = "Redirected";
            exchange.getResponseHeaders().put("Location", Arrays.asList(uri));
            exchange.sendResponseHeaders(301, message.length());
            OutputStream os = exchange.getResponseBody();
            os.write(message.getBytes());
            os.close();
            return;
        }

        if (localFile.isDirectory()) {
            localFile = new File(localFile.getAbsolutePath() + "/index.html");
        }
        System.out.println("Local file: " + localFile.getAbsolutePath());
        if (localFile.exists() == false) {
            reply404(exchange);
        } else {
            exchange.sendResponseHeaders(200, localFile.length());
            OutputStream os = exchange.getResponseBody();
            Files.copy(localFile.toPath(), os);
            os.close();
        }
    }

    private void printHeaders(HttpExchange exchange) {
        Headers headers = exchange.getRequestHeaders();
        for (String key : headers.keySet()) {
            System.out.println(key + " -> " + headers.get(key));
        }
    }

    private void reply404(HttpExchange exchange) throws IOException {
        String message = "File not found!";
        exchange.sendResponseHeaders(404, message.length());
        OutputStream os = exchange.getResponseBody();
        os.write(message.getBytes());
        os.close();
    }
}


