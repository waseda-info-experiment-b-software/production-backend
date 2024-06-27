package mogitClient.src.controller.commands.branch;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

import mogitClient.src.constants.Constants;
import mogitClient.src.controller.commands.commit.CommitTreeManager;

public class CreateBranch {
  /**
   * ブランチを作成する。
   * @param branchName
   */
  public void branch(String branchName) {
    // ----- 以下、現在のブランチを取得する処理 -----
    // 今指しているブランチを取得
    File headFile = new File(Constants.SRC_PATH + ".mogit/HEAD");
    String currentBranchName = "";
    try {
      currentBranchName = Files.readAllLines(headFile.toPath()).get(0);
    } catch (Exception e) {
      System.err.println("HEADファイルの読み込みに失敗しました");
      e.printStackTrace();
      return;
    }

    // 今指しているブランチのコミットIDを取得
    File currentBranchFile = new File(Constants.SRC_PATH + ".mogit/" + currentBranchName);
    String currentCommitId = "";
    try {
      currentCommitId = Files.readAllLines(currentBranchFile.toPath()).get(0);
    } catch (Exception e) {
      System.err.println("現在のブランチファイルの読み込みに失敗しました");
      e.printStackTrace();
      return;
    }

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

    // .mogit/refs/heads/branchName に、現在のコミットIDを書き込む
    try (FileOutputStream fos = new FileOutputStream(refFile)) {
      fos.write(currentCommitId.getBytes());
    } catch (Exception e) {
      System.err.println("ブランチファイル heads/" + branchName + "への書き込みに失敗しました");
      e.printStackTrace();
      return;
    }

    // .mogit/logs/refs/heads/branchName にファイルを作成
    File logFile = new File(logPath + branchFileName);
    try {
      logFile.createNewFile();
    } catch (Exception e) {
      System.err.println("ログファイル logs/refs/heads/" + branchName + "の作成に失敗しました");
      e.printStackTrace();
      return;
    }

    // .mogit/logs/refs/heads/branchName に、現在のコミットIDを追記
    try (FileOutputStream fos = new FileOutputStream(logFile)) {
      fos.write("branch: ".getBytes());
      fos.write((branchName + " - ").getBytes());
      fos.write("commit: ".getBytes());
      fos.write((currentCommitId + "\n").getBytes());
    } catch (Exception e) {
      System.err.println("ログファイル logs/refs/heads/" + branchName + "への書き込みに失敗しました");
      e.printStackTrace();
      return;
    }

    // .mogit/logs/HEAD にも書き込む
    File logHeadFile = new File(Constants.SRC_PATH + ".mogit/logs/HEAD");
    try (FileOutputStream fos = new FileOutputStream(logHeadFile, true)) {
      fos.write("branch: ".getBytes());
      fos.write((branchName + " - ").getBytes());
      fos.write("commit: ".getBytes());
      fos.write((currentCommitId + "\n").getBytes());
    } catch (Exception e) {
      System.err.println("ログファイル logs/HEAD への書き込みに失敗しました");
      e.printStackTrace();
      return;
    }

    System.out.println("=== ブランチ " + branchName + " を作成しました ===");
  }


  /**
   * ブランチを切り替える。
   * @param branchName
   */
  public void checkout(String branchName) {
    // .mogit/refs/heads/branchName が存在するか確認
    File refFile = new File(Constants.SRC_PATH + ".mogit/refs/heads/" + branchName);
    if (!refFile.exists()) {
      System.err.println("ブランチ " + branchName + " は存在しません");
      return;
    }

    // .mogit/logs/refs/heads/branchName を読み込む
    String theBranchCommitId = "";
    try {
      theBranchCommitId = Files.readAllLines(refFile.toPath()).get(0);
    } catch (Exception e) {
      System.err.println("ブランチファイルの読み込みに失敗しました");
      e.printStackTrace();
      return;
    }

    // .mogit/HEAD を読み込む
    File headFile = new File(Constants.SRC_PATH + ".mogit/HEAD");

    // currentディレクトリの内部を、コミットIDに応じて変更
    CommitTreeManager.revert(theBranchCommitId);

    // .mogit/HEAD の内容を書き換え ***
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
