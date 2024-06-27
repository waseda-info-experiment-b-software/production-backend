package mogitClient.src.controller.commands.init;
import java.io.File;
import java.io.FileWriter;

import mogitClient.src.constants.Constants;

public class MogitInit {
  /** プロジェクトルートディレクトリに、.mogitディレクトリを作成
   */
  public void init() {
    File file = new File(Constants.SRC_PATH + ".mogit");
    if (file.exists()) {
      System.out.println("already initialized");
      return;
    }
    file.mkdir();
    
    String[] dirNameList = {
      "objects",
      "refs",
      "refs/heads",
    };

    // .mogit/objectsディレクトリ、.mogit/refsディレクトリを作成
    for (String dirName : dirNameList) {
      File dir = new File(Constants.SRC_PATH + ".mogit/" + dirName);
      dir.mkdir();
    }

    // .mogit/HEADファイルを作成(現在のブランチ名を記録するためのファイル)
    File head = new File(Constants.SRC_PATH + ".mogit/HEAD");
    try {
      head.createNewFile();
    } catch (Exception e) {
      System.out.println("failed to create HEAD file");
    }

    // .mogit/HEADファイルにmainブランチを記録
    try (FileWriter writer = new FileWriter(head)) {
      writer.write("refs/heads/main");
    } catch (Exception e) {
      System.out.println("failed to write HEAD file");
    }

    // .mogit/refs/heads/mainファイルを作成(mainブランチのコミットIDを記録するためのファイル)
    File main = new File(Constants.SRC_PATH + ".mogit/refs/heads/main");
    try {
      main.createNewFile();
    } catch (Exception e) {
      System.out.println("failed to create main file");
    }

    // .mogit/logsディレクトリを作成(変更履歴を記録するためのディレクトリ)
    File logs = new File(Constants.SRC_PATH + ".mogit/logs");
    logs.mkdir();

    // .mogit/logs/refsディレクトリを作成(リファレンスの変更履歴を記録するためのディレクトリ)
    File logsRefs = new File(Constants.SRC_PATH + ".mogit/logs/refs");
    logsRefs.mkdir();

    // .mogit/logs/HEADファイルを作成(HEADの変更履歴を記録するためのファイル)
    File logsHead = new File(Constants.SRC_PATH + ".mogit/logs/HEAD");
    try {
      logsHead.createNewFile();
    } catch (Exception e) {
      System.out.println("failed to create logs/HEAD file");
    }

    // .mogit/logs/refs/headsディレクトリを作成(ブランチの変更履歴を記録するためのディレクトリ)
    File logsRefsHeads = new File(Constants.SRC_PATH + ".mogit/logs/refs/heads");
    logsRefsHeads.mkdir();
    

    // .mogit/logs/refs/heads/mainファイルを作成(mainブランチの変更履歴を記録するためのファイル)
    File logsMain = new File(Constants.SRC_PATH + ".mogit/logs/refs/heads/main");
    try {
      logsMain.createNewFile();
    } catch (Exception e) {
      System.out.println("failed to create logs/refs/heads/main file");
    }

    // .mogit/configファイルを作成(ユーザー名を記録するためのファイル)
    File config = new File(Constants.SRC_PATH + ".mogit/config");
    try {
      config.createNewFile();
    } catch (Exception e) {
      System.out.println("failed to create config file");
    }

    // ワークツリーディレクトリcurrentを作成
    File current = new File(Constants.SRC_PATH + "current");
    current.mkdir();


    System.out.println("以下のディレクトリにて、正常にmogitが初期化されました: " + file.getAbsolutePath());
    System.out.println("================================================================================");
    System.out.println("mogit のリポジトリにおける構造は以下の通りです。");
    System.out.println("  .mogit/\t\t\tリポジトリに相当します");
    System.out.println("    objects/\t\t\tファイルオブジェクトを保存するディレクトリです");
    System.out.println("    refs/\t\t\tブランチ情報を保持するディレクトリです");
    System.out.println("      heads/\t\t\tブランチのヘッド情報を保持するディレクトリです");
    System.out.println("    logs/\t\t\t変更履歴を保持するディレクトリです");
    System.out.println("      refs/\t\t\tリファレンスの変更履歴を保持するディレクトリです");
    System.out.println("        heads/\t\t\tブランチの変更履歴を保持するディレクトリです");
    System.out.println("    HEAD\t\t\t現在のブランチを指すファイルです");
    System.out.println("    config\t\t\tユーザー名を記録するファイルです");
    System.out.println("  current/\t\t\tワークツリーに相当するディレクトリです\n");
    System.out.println("*** [current]ディレクトリに、版管理をしたいフォルダやファイルを投入してください ***");
    System.out.println("================================================================================");

  }
}
