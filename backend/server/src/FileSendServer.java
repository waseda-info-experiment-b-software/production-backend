import java.io.*;
import java.net.*;

public class FileSendServer {
  final static int PORT = 8080;
  public static void main(String[] args) {

    //関数の利用
    Serverexp eva_server = new Serverexp();
    ServerSocket serverSocket = null;

    //コマンドの名前とファイル名を一時保存
    String command_name;
    String file_name;

   try {
      //サーバーの起動
      serverSocket = new ServerSocket(PORT);
      System.out.println(serverSocket.getInetAddress());
      System.out.println("Server activated... " + serverSocket);
      while (true) {
        command_name = eva_server.getcommandClient(serverSocket);

        switch (command_name) {
          //終了
          case "bye":
            serverSocket.close();
            System.out.println("Server closed...");
            return;

          //送る
          case "send":

            file_name = eva_server.getfilename(serverSocket);
            eva_server.getfile(file_name, serverSocket);
            break;

          //ファイル名を指定してダウンロード
          case "get":

            file_name = eva_server.getfilename(serverSocket);
            eva_server.sendFileToClient(file_name, serverSocket);
            break;
          
          case "pull":
            eva_server.sendFolderToClient(serverSocket);
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