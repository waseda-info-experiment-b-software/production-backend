package mogitClient.src.controller.commands.branch;

import java.io.File;
import java.util.Scanner;

import mogitClient.src.constants.Constants;

public class BranchList {
  /**
   * ブランチ一覧を表示する
   */
  public static void listBranches() {
    // .mogit/refs/heads/ にあるファイルを列挙
    File refDir = new File(Constants.SRC_PATH + ".mogit/refs/heads/");
    File[] files = refDir.listFiles();
    for (File file : files) {
      System.out.println(file.getName());
    }
  }

  /**
   * 現在いるブランチを表示する
   */
  public static void showCurrentBranch() {
    // HEADファイルを読み込む
    File headFile = new File(Constants.SRC_PATH + ".mogit/HEAD");
    String headBranch = "";
    try {
      Scanner scanner = new Scanner(headFile);
      headBranch = scanner.nextLine();
      scanner.close();
    } catch (Exception e) {
      System.err.println("HEADファイルの読み込みに失敗しました");
      // e.printStackTrace();
      return;
    }

    // HEADファイルの内容を表示
    System.out.println(headBranch);
  }

  /**
   * 現在いるブランチを返す
   */
  public static String getCurrentBranch() {
    // HEADファイルを読み込む
    File headFile = new File(Constants.SRC_PATH + ".mogit/HEAD");
    String headBranch = "";
    try {
      Scanner scanner = new Scanner(headFile);
      headBranch = scanner.nextLine();
      scanner.close();
    } catch (Exception e) {
      System.err.println("HEADファイルの読み込みに失敗しました");
      // e.printStackTrace();
    }

    // HEADファイルの内容を表示
    return headBranch;
  }
}
