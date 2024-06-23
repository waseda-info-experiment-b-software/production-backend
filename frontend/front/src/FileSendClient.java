import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FileSendClient {
  final static int PORT = 8080;
  public static void main(String[] args) {
    Clientexp client = new Clientexp();
    String branch = "main";
    Scanner scanner = new Scanner(System.in);
    String input;

    try{
      InetAddress serverIP = InetAddress.getByName("production-java-server-1");

      while (true) {
            System.out.print(client.getCurrentPath()+ " (" +branch + ")"+"$ ");
            input = scanner.nextLine();

            switch (input) {

              case "help":
                System.out.println("info of command");
                break;

              default:
                if (input.startsWith("eva ")) {
                  String[] parts = input.split(" ");

                  switch (parts[1]) {

                    case "init":

                      if(client.sendcommandServer(parts[1], serverIP, PORT)){
                        
                          System.out.println("sent files");
                        }else{
                          System.out.println("cannot connect to server");
                        }

                      break;

                    // ここではファイルを送っている
                    //ファイルが存在しなければ送らない(サーバー側に空のファイルができちゃうので)
                    case "send":

                      
                      if (!client.checkFileExists(client.getCurrentPath(), parts[3])){
                        System.out.println("cannot find such files");
                      }else{
                        if(client.sendcommandServer(parts[1], serverIP, PORT)){
                          //ディレクトリ名をまず送る
                          client.sendFilenameToServer(parts[2], serverIP, PORT);
                          //次にファイル名を送る
                          client.sendFilenameToServer(parts[3], serverIP, PORT);
                          client.sendFileToServer(parts[3], serverIP, PORT);
                          System.out.println("sent files");
                        }else{
                          System.out.println("cannot connect to server");
                        }

                      }
                      break;

                    //ファイル名を指定してダウンロード
                    case "get":

                      if(client.sendcommandServer(parts[1], serverIP, PORT)){
                        //ディレクトリ名を送る
                        client.sendFilenameToServer(parts[2], serverIP, PORT);
                        //ファイル名を送る
                        client.sendFilenameToServer(parts[3], serverIP, PORT);
                        client.receiveFileFromServer(client.getCurrentPath(), parts[3], serverIP, PORT);
                      }else{
                        System.out.println("cannot connect to server");
                      }

                      break;

                    // mogit系のコマンド群
                    case "cat-file":
                      client.catFile(parts[2]);
                      break;

                    case "hash-objects":
                      client.createDirectoryAndZipFile(parts[2]);
                      break;

                    case "tree-hash":
                      System.out.println(client.createTreeHashString(parts[2]));
                      break;

                    case "add":
                      client.createDirectoryAndZipFile(parts[2]);
                      break;

                    case "pull":
                      if(client.sendcommandServer(parts[1], serverIP, PORT)){
                        client.receiveFileFromServer(client.getCurrentPath(), "current", serverIP, PORT);
                      }else{
                        System.out.println("cannot connect to server");
                      }                     
                      break;

                    case "push":
                      break;

                    case "unzip":
                      // client.unzipFileFromZip(parts[2]);
                      break;
                    
                    case "bye":
                      client.sendcommandServer(parts[1], serverIP, PORT);
                      System.out.println("Exiting program...");
                      scanner.close();
                      return;

                    
                    default:
                      System.out.println("Invalid command format");
                      break;
      
                    
                  }
            } else {
                System.out.println("Command not found");
            }
            break;
                
            }
          }
    }catch (IOException e){
      e.printStackTrace();
    }
    scanner.close();

  }
}