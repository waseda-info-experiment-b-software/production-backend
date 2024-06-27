package mogitClient.src.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import mogitClient.src.constants.Constants;
import mogitClient.src.model.CommitObject;
import mogitClient.src.model.TreeObject;

public class CommitObjectService {
  /**
   * コミットオブジェクトを作成する。
   * @param message コミットメッセージ
   */
  public static void writeCommitObject(String[] messages) {
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
