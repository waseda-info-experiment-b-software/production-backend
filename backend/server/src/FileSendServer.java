import java.io.*;
import java.net.*;

public class FileSendServer {
  final static int PORT = 8080;
  public static void main(String[] args) {

    //関数の利用
    Serverexp eva_server = new Serverexp();
    ServerSocket serverSocket = null;
    
    //コマンドの名前とファイル名を一時保存するための文字列
    String command_name;
    String file_name;

    try {
      serverSocket = new ServerSocket(PORT);
      System.out.println(serverSocket.getInetAddress());
      System.out.println("Server activated... " + serverSocket);
      while (true) {
        command_name = eva_server.getcommandClient(serverSocket);

        switch (command_name) {
          case "bye":
            return;

          case "send":
            file_name = eva_server.getfilename(serverSocket);
            eva_server.getfile(file_name, serverSocket);
            break;
          default:
            System.out.println("waiting");
            break;
        }
      }


    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}