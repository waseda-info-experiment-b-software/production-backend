import java.io.*;
import java.net.*;

public class FileSendServer {
  final static int PORT = 8080;
  public static void main(String[] args) {
    // サーバーソケットを開く
    try (ServerSocket s = new ServerSocket(PORT)) {
      System.out.println("Server activated... " + s);

      // クライアントからの接続要求を待つ
      try (Socket socket = s.accept()) {
        System.out.println("Established connection to Client... " + socket);

        // 入力ストリームを取得
        InputStream is = socket.getInputStream();

        // ★受信ファイルを保存する
        try (FileOutputStream fos = new FileOutputStream("received.txt")) {
          byte[] buffer = new byte[1024];
          int read;
          while ((read = is.read(buffer)) != -1) {
            fos.write(buffer, 0, read);
          }
        } 
      }

      System.out.println("File Reception Successful...");
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}