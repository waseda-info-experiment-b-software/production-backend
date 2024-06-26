package mogitClient.src.controller.commands.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Log {
  public static void showLog() {
    // HEADファイルから現在のブランチを取得
    File headFile = new File(".mogit/HEAD");
    String branch = "";
    try {
      FileReader fr = new FileReader(headFile);
      BufferedReader br = new BufferedReader(fr);
      branch = br.readLine();
      br.close();
    } catch (Exception e) {
      System.out.println("HEADファイルが見つかりませんでした。");
      return;
    }

    // .mogit/logs/BRANCH_NAME からログを取得
    File logFile = new File(".mogit/logs/" + branch);
    // ログファイルが存在しない場合は、ログが存在しない旨を表示
    if (!logFile.exists()) {
      System.out.println("ログファイルが見つかりませんでした。コミットが存在しません。");
      return;
    }
    
    // mainファイルからそのまま標準出力
    try {
      FileReader fr = new FileReader(logFile);
      BufferedReader br = new BufferedReader(fr);
      String line;
      while ((line = br.readLine()) != null) {
        System.out.println(line);
      }
      br.close();
    } catch (Exception e) {
      System.out.println("ログファイルの読み込みに失敗しました。");
    }
  }
}
