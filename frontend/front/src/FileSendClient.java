import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
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
                      createHashObject(parts);
                      break;

                    case "write-tree":
                      writeTreeObject();
                      break;

                    case "commit":
                      if (parts.length < 3) {
                        System.out.println("Please input commit message");
                        break;
                      }
                      writeCommitObject(parts[2]);
                      break;
                    
                    case "reset":
                      CommitTreeManager.revert(parts[2]);
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

  /**
   * ファイルオブジェクトを作成する。
   * @param parts コマンドのパーツ
   */
  static void createHashObject(String[] parts) {
    Path fpath = Paths.get(parts[2]);
    File file = new File(parts[2]);
    if (!file.exists()) {
      System.out.println("File not found");
      return;
    }
    try {
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
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * ツリーオブジェクトを作成する。
   */
  static void writeTreeObject() {
    try {
      Path path = Paths.get("current");

      long fileSize = Files.walk(path).map(Path::toFile).filter(f -> f.isFile()).mapToLong(f -> f.length()).sum();
      TreeObject tree = new TreeObject(path, fileSize);
      tree.writeToFiles();
      System.out.println("TreeObject creation succeeded: " + tree.getHash());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * コミットオブジェクトを作成する。
   * @param message コミットメッセージ
   */
  static void writeCommitObject(String message) {
    try {
      Path path = Paths.get("current");

      long fileSize = Files.walk(path).map(Path::toFile).filter(f -> f.isFile()).mapToLong(f -> f.length()).sum();

      // プロジェクトディレクトリのツリーオブジェクトを作成
      TreeObject tree = new TreeObject(path, fileSize);
      tree.writeToFiles();

      // コミットを作成
      // TODO: 著者やコミッターの情報をどこかで設定できるようにする
      long time = new Date().getTime();

      // 以前のコミットを、.mogit/refs/heads/mainから取得
      String parentHash = "";
      File main = new File(".mogit/refs/heads/main");
      if (main.exists()) {
        try (BufferedReader reader = new BufferedReader(new FileReader(main))) {
          parentHash = reader.readLine();
        } catch (IOException e) {
          // 以前のコミットがない場合（根っこ）
          parentHash = "";
        }
      }
      
      CommitObject commit = new CommitObject(tree, "yoshi-zen", "committer", message, parentHash, time);
      commit.writeToFile();

      // refs/head/mainにコミットを追加
      // TODO: ブランチも動的に管理したいなあ
      File file = new File(".mogit/refs/heads/main");
      try (FileWriter writer = new FileWriter(file)) {
        writer.write(commit.getHash());
      } catch (IOException e) {
        e.printStackTrace();
        System.out.println("Cannot write commit to refs/head/main");
      }
      
      System.out.println("TreeObject creation succeeded: " + tree.getHash());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}