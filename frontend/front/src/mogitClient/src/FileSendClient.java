package mogitClient.src;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

import mogitClient.src.constants.Constants;
import mogitClient.src.controller.commands.cat.CatFileObject;
import mogitClient.src.controller.commands.commit.CommitTreeManager;
import mogitClient.src.controller.commands.help.Help;
import mogitClient.src.controller.commands.init.MogitInit;
import mogitClient.src.controller.commands.log.Log;
import mogitClient.src.controller.commands.push.PushToServer;
import mogitClient.src.controller.utils.Clientexp;
import mogitClient.src.model.BlobObject;
import mogitClient.src.model.CommitObject;
import mogitClient.src.model.FileObject;
import mogitClient.src.model.TreeObject;

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
      InetAddress serverIP = InetAddress.getByName("production-backend-java-server-1");

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
                    
                    //.config/config.txtの編集
                    case "config":
                      client.handleConfig(scanner);
                      break;

                    case "config-look":
                      client.viewConfigFileContent();
                      break;
                    //サーバーに登録されているかチェックする
                    case "config-check":
                      String message;
                      message = client.handleConfigCheck(serverIP, PORT);
                      System.out.println(message);
                      break;
                      
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
                  


                    case "help":
                      Help.showHelp();
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

                    case "log":
                     Log.showLog();
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
                      writeCommitObject(Arrays.copyOfRange(parts, 2, parts.length));
                      break;
                    
                    case "checkout":
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
                      if (client.sendcommandServer(parts[1], serverIP, PORT)) {
                        PushToServer.pushToServer();
                      } else {
                        System.out.println("cannot connect to server");
                      }
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
      Path path = Paths.get(Constants.SRC_PATH + "current");

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
  static void writeCommitObject(String[] messages) {
    try {
      // ワークツリーであるcurrentディレクトリのパスを取得
      Path path = Paths.get(Constants.SRC_PATH + "current");

      // ワークツリーのファイルサイズを取得
      long fileSize = Files.walk(path).map(Path::toFile).filter(f -> f.isFile()).mapToLong(f -> f.length()).sum();

      // プロジェクトディレクトリのツリーオブジェクトを作成
      TreeObject tree = new TreeObject(path, fileSize);

      // 現在指しているブランチを.mogit/HEADから取得
      File head = new File(Constants.SRC_PATH + ".mogit/HEAD");
      String branch = "";
      if (head.exists()) {
        try (BufferedReader reader = new BufferedReader(new FileReader(head))) {
          branch = reader.readLine();
        } catch (IOException e) {
          e.printStackTrace();
          System.out.println("HEADファイルが見つかりませんでした。");
          return;
        }
      }

      // ツリーオブジェクトをファイルに書き出す
      tree.writeToFiles();

      // コミットを作成
      long time = new Date().getTime();

      // 以前のコミットを、.mogit/refs/heads/mainから取得
      String parentHash = "";
      File main = new File(Constants.SRC_PATH + ".mogit/" + branch);
      if (main.exists()) {
        try (BufferedReader reader = new BufferedReader(new FileReader(main))) {
          parentHash = reader.readLine();
        } catch (IOException e) {
          // 以前のコミットがない場合（根っこ）
          parentHash = "";
        }
      }

      // .configから、ユーザー名を取得
      String author = "";
      File config = new File(Constants.SRC_PATH + ".mogit/config");
      if (config.exists()) {
        try (BufferedReader reader = new BufferedReader(new FileReader(config))) {
          author = reader.readLine();
          if (author == null || author.isEmpty()) {
            // ユーザー名が設定されていない場合
            System.err.println("ユーザー名とメールアドレスが正しく設定されていません。以下のコマンドを入力して設定してください。");
            System.err.println("\t\teva config");
            return;
          }
        } catch (IOException e) {
          // ユーザー名が設定されていない場合
          System.err.println("ユーザー名とメールアドレスが正しく設定されていません。以下のコマンドを入力して設定してください。");
          System.err.println("\t\teva config");
          return;
        }
      } else {
        // .configが存在しない場合
        System.err.println("ユーザー名とメールアドレスが正しく設定されていません。以下のコマンドを入力して設定してください。");
        System.err.println("\t\teva config");
        return;
      }
      
      // *** コミットオブジェクトを作成 ***
      CommitObject commit = new CommitObject(tree, author, String.join(" ", messages), parentHash, time);
      commit.writeToFile();

      // refs/head/mainにコミットを追加
      File file = new File(Constants.SRC_PATH + ".mogit/" + branch);
      try (FileWriter writer = new FileWriter(file)) {
        writer.write(commit.getHash());
      } catch (IOException e) {
        e.printStackTrace();
        System.out.println(branch + "にコミットできませんでした。");
      }

      // logファイルに、コミットの情報を追加しておく
      File log = new File(Constants.SRC_PATH + ".mogit/logs/" + branch);
      try (FileWriter writer = new FileWriter(log, true)) {
        writer.write("====================================================\n");
        writer.write("commit hash:\t\t" + commit.getHash() + "\n");
        writer.write("parent commit:\t\t" + parentHash + "\n");
        writer.write("working tree hash:\t" + tree.getHash() + "\n");
        writer.write("commit message:\t\t" + String.join(" ", messages) + "\n");
        writer.write("commit created at:\t" + new Date(time) + "\n");
        writer.write("commit created by:\t" + author + "\n");
        writer.write("\n");
      } catch (IOException e) {
        e.printStackTrace();
        System.out.println(branch + " ブランチのログにコミット情報を追加できませんでした。");
      }
      
      System.out.println("このコミットが指す TreeObject :\t\t" + tree.getHash());
      System.out.println("このコミットを表す CommitObject :\t" + commit.getHash());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}