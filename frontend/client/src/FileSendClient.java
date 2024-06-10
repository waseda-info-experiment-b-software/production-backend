import java.io.*;
import java.net.*;

public class FileSendClient {
  final static int PORT = 8080;
  public static void main(String[] args) {
    Socket socket = null;

    // サーバーと接続する
    try  {
      InetAddress serverIP = InetAddress.getByName("production-java-server-1");
      System.out.println("Connecting to Server... " + serverIP);
      socket = new Socket(serverIP, PORT);
      System.out.println("Established connection to Server... " + socket);

      // ファイルを送信する
      try (
        FileInputStream fis = new FileInputStream("sample2.txt");
        OutputStream os = socket.getOutputStream()) {
          byte[] buffer = new byte[1024];
          int read;
          while ((read = fis.read(buffer)) != -1) {
            os.write(buffer, 0, read);
          }
      }

      System.out.println("File Transport Successful...");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}