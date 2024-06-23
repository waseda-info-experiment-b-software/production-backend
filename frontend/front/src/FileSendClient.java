import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class FileSendClient {
  final static int PORT = 8080;
  public static void main(String[] args) {
    Clientexp client = new Clientexp();
    MogitInit init = new MogitInit();
    CatFileObject cat = new CatFileObject();
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
                    // ここではファイルを送っている
                    //ファイルが存在しなければ送らない(サーバー側に空のファイルができちゃうので)
                    case "send":

                      
                      if (!client.checkFileExists(client.getCurrentPath(), parts[2])){
                        System.out.println("cannot find such files");
                      }else{
                        if(client.sendcommandServer(parts[1], serverIP, PORT)){
                          client.sendFilenameToServer(parts[2], serverIP, PORT);
                          client.sendFileToServer(parts[2], serverIP, PORT);
                          System.out.println("sent files");
                        }else{
                          System.out.println("cannot connect to server");
                        }

                      }
                      break;
                  

                    //ファイル名を指定してダウンロード
                    case "get":

                      if(client.sendcommandServer(parts[1], serverIP, PORT)){
                        client.sendFilenameToServer(parts[2], serverIP, PORT);
                        client.receiveFileFromServer(parts[2], serverIP, PORT);
                      }else{
                        System.out.println("cannot connect to server");
                      }

                      break;

                    case "init":
                      init.init();
                      break;

                    case "cat-file":
                      cat.catFile(parts[2]);
                      break;

                    case "hash-objects":
                      Path fpath = Paths.get(parts[2]);
                      File file = new File(parts[2]);
                      if (!file.exists()) {
                        System.out.println("File not found");
                        break;
                      }
                      FileObject fileObject;
                      if (file.isDirectory()) {
                        fileObject = new TreeObject(fpath, 0);
                      } else {
                        byte[] content = Files.readAllBytes(fpath);
                        long fileSize = Files.walk(fpath).map(Path::toFile).filter(f -> f.isFile()).mapToLong(f -> f.length()).sum();
                        fileObject = new BlobObject(content, fpath, fileSize);
                      }
                      fileObject.writeToFile();
                      System.out.println("FileObject creation succeeded: " + fileObject.getHash());
                      break;

                    case "write-tree":
                      Path path = Paths.get("test");
                      TreeObject tree = new TreeObject(path, 0);
                      tree.writeToFiles();
                      System.out.println("TreeObject creation succeeded: " + tree.getHash());
                      break;

                    case "pull":

                      if(client.sendcommandServer(parts[1], serverIP, PORT)){
                        client.receiveFolderFromServer(serverIP, PORT);
                        client.unzipFolder("/usr/src/result.zip");
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