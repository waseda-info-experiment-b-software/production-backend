import java.io.*;
import java.net.*;
import java.util.Scanner;
import javax.naming.NameNotFoundException;

public class FileSendClient {
  
  public static void main(String[] args) {
    Clientexp client = new Clientexp();

    String ipAddress = "production-java-server-1";
    int port = 8080;
    String branch = "main";
    Scanner scanner = new Scanner(System.in);
    String input;

    //byeという文字を入力するまでずっと実行する
    while (true) {
      System.out.print(client.getCurrentPath()+ " (" +branch + ")"+"$ ");
      input = scanner.nextLine();

      switch (input) {

        case "bye":
          System.out.println("Exiting program...");
          scanner.close();
          return;

        case "help":
          System.out.println("info of command");
          break;

        default:
          if (input.startsWith("eva ")) {
            String[] parts = input.split(" ");

            switch (parts[1]) {
              // ここではファイルを送っている
              //ファイルが存在しなければ送らない(サーバー側に空のファイルができちゃうので)
              case "send":

                
                if (!client.checkFileExists(client.getCurrentPath(), parts[2])){
                  System.out.println("can not find such files");
                }else{
                  if(client.sendcommandServer(parts[1], ipAddress, port)){
                    client.sendFilenameToServer(parts[2], ipAddress, port);
                    client.sendFileToServer(parts[2], ipAddress, port);
                    System.out.println("send files");
                  }else{
                    System.out.println("can not connect server");
                  }

                }
                break;
            


              case "get":
                break;

              case "pull":
                break;

              case "push":
                break;

              
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
  }
}

  
  


