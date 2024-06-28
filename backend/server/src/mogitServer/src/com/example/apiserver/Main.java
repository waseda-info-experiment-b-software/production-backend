package mogitServer.src.com.example.apiserver;

import com.sun.net.httpserver.HttpServer;

import mogitServer.src.constants.Constants;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class Main {
    // 開始
    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8082), 0);
        server.createContext("/api/data", new MyHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server is listening on port 8082");
    }

    // 終了
    public void stop() {
        System.exit(0);
    }


    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // CORSヘッダーの追加
            Map<String, String> headers = new HashMap<>();
            headers.put("Access-Control-Allow-Origin", "*");
            headers.put("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            headers.put("Access-Control-Allow-Headers", "Content-Type");

            for (Map.Entry<String, String> entry : headers.entrySet()) {
                exchange.getResponseHeaders().add(entry.getKey(), entry.getValue());
            }

            // Constants.SRC_PATH/currentディレクトリ内にあるフォルダやディレクトリの一覧を取得し、レスポンスとして返す
            File currentDir = new File(Constants.SRC_PATH + "/current");
            File[] files = currentDir.listFiles();
            String response = "";
            if (files != null) {
                for (File file : files) {
                    response += file.getName() + "\n";
                }
            }
            response = response.trim();

            // レスポンスの設定
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
