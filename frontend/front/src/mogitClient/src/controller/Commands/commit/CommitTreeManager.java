package mogitClient.src.controller.commands.commit;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import mogitClient.src.constants.Constants;
import mogitClient.src.controller.FileObjectManipulation.*;

public class CommitTreeManager {
  /**
   * コミットハッシュを受け取り、そこからファイルオブジェクトを復元する
   * @param hash
   */
  public static void revert(String hash) {
    // まずはhashに対応するファイルオブジェクトがあるか確認
    String firstTwoChars = hash.substring(0, 2);
    String rest = hash.substring(2);
    String filePath = Constants.SRC_PATH + ".mogit/objects/" + firstTwoChars + "/" + rest;
    // ファイルが存在しない場合はエラーを出力
    if (!Files.exists(Paths.get(filePath))) {
      System.out.println("no such commit");
      return;
    }

    try {
      byte[] data = FileObjectManipulation.decompressData(filePath);
      if (data.length == 0) {
        System.out.println("No such file");
        return;
      }

      int index = 0;
      while (data[index] != " ".getBytes()[0]) {
        index++;
      }
      String fileType = new String(data, 0, index);

      // ファイルタイプがcommitでない場合はエラーを出力
      if (!fileType.equals("commit")) {
        System.out.println("no such commit");
        return;
      }

      int nextIndex = index;
      while (data[nextIndex++] != "\0".getBytes()[0]) {}  // commitオブジェクトのフォーマットにより、次のnull文字までがsize
      index = nextIndex;
      while (data[nextIndex++] != "\0".getBytes()[0]) {}  // commitオブジェクトのフォーマットにより、次のnull文字までがparent
      index = nextIndex;
      while (data[nextIndex++] != "\0".getBytes()[0]) {}

      // ここからが当該treeオブジェクトのハッシュ値
      String treeHash = new String(data, index, nextIndex - index - 1);

      // 現在のcurrentディレクトリを削除
      File currentDir = new File(Constants.SRC_PATH + "current");
      clearDirectory(currentDir);

      decompressTree(Constants.SRC_PATH + "current", treeHash);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void clearDirectory(File dir) {
    File[] files = dir.listFiles();
    if (files != null) { // ディレクトリが空でない場合
      for (File file : files) {
        if (file.isDirectory()) {
          // サブディレクトリを再帰的に削除
          deleteDirectoryRecursively(file);
        } else {
          // ファイルを削除
          file.delete();
        }
      }
    }
  }

  /**
   * ディレクトリを再帰的に削除する
   * @param dir
   */
  public static void deleteDirectoryRecursively(File dir) {
    File[] files = dir.listFiles();
    if (files != null) { // ディレクトリが空でない場合
      for (File file : files) {
        if (file.isDirectory()) {
          // サブディレクトリを再帰的に削除
          deleteDirectoryRecursively(file);
        } else {
          // ファイルを削除
          file.delete();
        }
      }
    }
    // ディレクトリを削除
    dir.delete();
  }

  public static void decompressTree(String dirPath, String hash) {
    // treeオブジェクトを展開
    // まずはtreeオブジェクトが存在するか確認
    String firstTwoChars = hash.substring(0, 2);
    String rest = hash.substring(2);
    String filePath = Constants.SRC_PATH + ".mogit/objects/" + firstTwoChars + "/" + rest;

    // System.out.println(filePath);e
    if (!Files.exists(Paths.get(filePath))) {
      System.out.println("no such tree");
      return;
    }

    try {
      byte[] data = FileObjectManipulation.decompressData(filePath);
      if (data.length == 0) {
        System.out.println("No such file");
        return;
      }
      int index = 0;

      while (data[index++] != "\0".getBytes()[0]) {}  // treeオブジェクトのフォーマットにより、次のnull文字までがsize

      // ここからが、ディレクトリないの構造を示している
      String[] fileInfos = new String(data, index, data.length - index).split("\0\0");

      for (String fileInfo : fileInfos) {
        System.out.println(fileInfo);
      }

      for (String fileInfo : fileInfos) {
        // まず空白文字で分割
        String[] parts = fileInfo.split(" ");
        String type = parts[0];
        String nameAndHash = parts[1];
        String[] nameAndHashParts = nameAndHash.split("\0");
        String name = nameAndHashParts[0];
        String fileHash = nameAndHashParts[1];

        if (type.equals("00")) {
          // blobオブジェクトの場合
          decompressBlob(dirPath + "/" + name, fileHash);
        } else if (type.equals("11")) {
          // treeオブジェクトの場合
          // currentディレクトリにディレクトリを作成
          File newDir = new File(dirPath + "/" + name);
          newDir.mkdir();
          decompressTree(dirPath + "/" + name, fileHash);
          System.out.println("decompressed tree: \t\t" + filePath + " -> " + dirPath + "/" + name);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void decompressBlob(String distFilePath, String hash) {
    // blobオブジェクトを展開
    // まずはblobオブジェクトが存在するか確認
    String firstTwoChars = hash.substring(0, 2);
    String rest = hash.substring(2);
    String fileObjectPath = Constants.SRC_PATH + ".mogit/objects/" + firstTwoChars + "/" + rest;
    if (!Files.exists(Paths.get(fileObjectPath))) {
      System.out.println("no such blob");
      return;
    }

    try {
      // blobオブジェクトを展開
      byte[] data = FileObjectManipulation.decompressData(fileObjectPath);
      if (data.length == 0) {
        System.out.println("No such file");
        return;
      }
      int index = 0;

      while (data[index++] != "\0".getBytes()[0]) {}  // blobオブジェクトのフォーマットにより、次のnull文字までがsize

      // 書き込むバイト列を切り分ける
      byte[] content = new byte[data.length - index];
      for (int i = 0; i < content.length; i++) {
        content[i] = data[index + i];
      }
      // ファイルに書き込み
      // ファイルがなければ新規作成
      File file = new File(distFilePath);
      if (!file.exists()) {
        file.createNewFile();
      }
      Files.write(Paths.get(distFilePath), content);

      // LOG
      System.out.println("decompressed blob: \t\t" + fileObjectPath + " -> " + distFilePath);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
