package wook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Headers;

public class HookServer {

    private HttpServer server;

    public static void main(String[] args) throws Exception {
        HookServer hs = new HookServer();
    }

    public HookServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress(5051), 0);
        server.createContext("/hook", new HookHandler(server));
        server.setExecutor(null);
        server.start();
    }

    static class HookHandler implements HttpHandler {

        private HttpServer server;

        public HookHandler(HttpServer server) {
            this.server = server;
        }

        public void handle(HttpExchange t) throws IOException {
            InputStreamReader isr = new InputStreamReader(
                    t.getRequestBody(),"utf-8");
            BufferedReader br = new BufferedReader(isr);

            int read;
            StringBuilder buf = new StringBuilder(512);
            while ((read = br.read()) != -1) {
                buf.append((char) read);
            }

            br.close();
            isr.close();

            System.out.println(buf.toString());
            String data = buf.toString();
            /* do something */
        }
    }
}
