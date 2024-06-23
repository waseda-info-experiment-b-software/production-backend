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
    String directory_name;


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

            directory_name = eva_server.getfilename(serverSocket);
            file_name = eva_server.getfilename(serverSocket);
            eva_server.getfile(directory_name, file_name, serverSocket);
            break;

          //ファイル名を指定してダウンロード
          case "get":

            directory_name = eva_server.getfilename(serverSocket);
            file_name = eva_server.getfilename(serverSocket);
            eva_server.sendFileToClient(directory_name, file_name, serverSocket);
            break;
          
          case "pull":

            //file_name = eva_server.getfilename(serverSocket);
            eva_server.sendFileToClient("mogit", "current", serverSocket);
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