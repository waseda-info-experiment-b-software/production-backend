package mogitClient.src.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import mogitClient.src.constants.Constants;
import mogitClient.src.model.TreeObject;

public class TreeObjectService {
  /**
   * ツリーオブジェクトを作成する。
   */
  public static void writeTreeObject() {
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
}
