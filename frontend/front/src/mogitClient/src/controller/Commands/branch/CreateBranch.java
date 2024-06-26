package mogitClient.src.controller.commands.branch;

import java.io.File;
import java.io.FileOutputStream;

import mogitClient.src.constants.Constants;

public class CreateBranch {
  public void branch(String branchName) {
    // branchNameを、「最後に出てくる」スラッシュで分割
    String[] branchNameArray = branchName.split("/");

    // 作成するファイル名だけ取得しておく
    String branchFileName = branchNameArray[branchNameArray.length - 1];
    
    // branchNameに対応するディレクトリを作成する
    String logPath = Constants.SRC_PATH + ".mogit/logs/refs/heads/";
    String refPath = Constants.SRC_PATH + ".mogit/refs/heads/";
    for (int i = 0; i < branchNameArray.length - 1; i++) {
      File refDir = new File(refPath + branchNameArray[i]);
      if (!refDir.exists()) {
        refDir.mkdir();
      }
      File logDir = new File(logPath + branchNameArray[i]);
      if (!logDir.exists()) {
        logDir.mkdir();
      }
      refPath += branchNameArray[i] + "/";
      logPath += branchNameArray[i] + "/";
    }

    // .mogit/refs/heads/branchName にファイルを作成
    File refFile = new File(refPath + branchFileName);
    try {
      refFile.createNewFile();
    } catch (Exception e) {
      System.err.println("ブランチファイル heads/" + branchName + "の作成に失敗しました");
      e.printStackTrace();
      return;
    }

    File logFile = new File(logPath + branchFileName);
    try {
      logFile.createNewFile();
    } catch (Exception e) {
      System.err.println("ログファイル logs/refs/heads/" + branchName + "の作成に失敗しました");
      e.printStackTrace();
      return;
    }

    System.out.println("=== ブランチ " + branchName + " を作成しました ===");
  }

  public void checkout(String branchName) {
    // .mogit/refs/heads/branchName が存在するか確認
    File refFile = new File(Constants.SRC_PATH + ".mogit/refs/heads/" + branchName);
    if (!refFile.exists()) {
      System.err.println("ブランチ " + branchName + " は存在しません");
      return;
    }

    // .mogit/HEAD を書き換え
    File headFile = new File(Constants.SRC_PATH + ".mogit/HEAD");

    // .mogit/HEAD の内容を書き換え
    try (FileOutputStream fos = new FileOutputStream(headFile)) {
      fos.write(("refs/heads/" + branchName).getBytes());
    } catch (Exception e) {
      System.err.println("HEADファイルの書き換えに失敗しました");
      e.printStackTrace();
      return;
    }
    System.out.println("=== ブランチ " + branchName + " に移動しました ===");
  }
}
