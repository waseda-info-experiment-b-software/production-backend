package mogitClient.src;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;

import mogitClient.src.controller.commands.branch.BranchList;
import mogitClient.src.controller.commands.branch.CreateBranch;
import mogitClient.src.controller.commands.cat.CatFileObject;
import mogitClient.src.controller.commands.commit.CommitTreeManager;
import mogitClient.src.controller.commands.help.Help;
import mogitClient.src.controller.commands.init.MogitInit;
import mogitClient.src.controller.commands.log.Log;
import mogitClient.src.controller.commands.push.PushToServer;
import mogitClient.src.controller.utils.Clientexp;

import mogitClient.src.service.CommitObjectService;
import mogitClient.src.service.FileObjectService;
import mogitClient.src.service.TreeObjectService;

public class FileSendClient {
  final static int PORT = 8080;
  public static void main(String[] args) {
    Clientexp client = new Clientexp();
    CatFileObject cat = new CatFileObject();
    
    String branch = "main";
    Scanner scanner = new Scanner(System.in);
    String input;

    try {
      InetAddress serverIP = InetAddress.getByName("production-backend-java-server-1");

      while (true) {
        branch = BranchList.getCurrentBranch().replace("refs/heads/", "");
        if (branch.equals("")) {
          System.out.println("  eva init によりリポジトリを初期化してください。");
          // continue;
        }
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
                  MogitInit.init();
                  break;

                case "cat-file":
                  cat.catFile(parts[2]);
                  break;

                case "hash-objects":
                  FileObjectService.createHashObject(parts);
                  break;

                case "write-tree":
                  TreeObjectService.writeTreeObject();
                  break;
                
                case "branch":
                  if (parts.length <= 2) {
                    BranchList.listBranches();
                  } else {
                    CreateBranch branchCommand = new CreateBranch();
                    branchCommand.branch(parts[2]);
                  }
                  break;

                case "checkout":
                  CreateBranch branchCommand2 = new CreateBranch();
                  if (parts[2].equals("-b")) {
                    if (parts.length < 4) {
                      System.out.println("Please input branch name");
                      break;
                    }
                    branchCommand2.branch(parts[3]);
                    branchCommand2.checkout(parts[3]);
                  } else {
                    branchCommand2.checkout(parts[2]);
                  }
                  
                  break;

                case "commit":
                  if (parts.length < 3) {
                    System.out.println("Please input commit message");
                    break;
                  }
                  CommitObjectService.writeCommitObject(Arrays.copyOfRange(parts, 2, parts.length));
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
                  if (client.sendcommandServer(parts[1], serverIP, PORT)) {
                    PushToServer.pushToServer();
                  } else {
                    System.out.println("cannot connect to server");
                  }
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