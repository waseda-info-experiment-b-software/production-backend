package mogitClient.src.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import mogitClient.src.model.BlobObject;
import mogitClient.src.model.FileObject;
import mogitClient.src.model.TreeObject;

public class FileObjectService {
  /**
   * ファイルオブジェクトを作成する。
   * @param parts コマンドのパーツ
   */
  public static void createHashObject(String[] parts) {
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
}
