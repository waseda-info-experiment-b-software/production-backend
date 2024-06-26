package mogitClient.src.model;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class TreeObject extends FileObject {
  Path path;
  long size;
  // そのディレクトリが持つ「ファイル」や「ディレクトリ」のファイルオブジェクトのリスト
  ArrayList<FileObject> children = new ArrayList<>();

  public TreeObject(Path path, long size) {
    this.path = path;
    this.size = size;
  }

  Path getPath() {
    return this.path;
  }

  public byte[] createRawBytes() {
    StringBuilder sb = new StringBuilder();

    // ディレクトリのパスから、フォルダ内のファイルのリストを取得
    File folder = new File(path.toString());
    File[] listOfFiles = folder.listFiles();

    if (listOfFiles == null) {
      return new byte[0];
    }
    sb.append("tree ");
    sb.append(size);
    sb.append("\0");

    for (File file : listOfFiles) {
      try {
        Path fpath = Paths.get(file.getPath());
        long fileSize = Files.walk(fpath).map(Path::toFile).filter(f -> f.isFile()).mapToLong(f -> f.length()).sum();
        if (file.isFile()) {
          byte[] content = Files.readAllBytes(fpath);
          BlobObject blob = new BlobObject(content, fpath, fileSize);
          children.add(blob);
          sb.append("00");
          sb.append(" ");
          sb.append(file.getName());
          sb.append("\0");
          sb.append(blob.getHash());
          sb.append("\0\0");
        } else if (file.isDirectory()) {
          TreeObject tree = new TreeObject(fpath, fileSize);
          children.add(tree);
          sb.append("11");
          sb.append(" ");
          sb.append(file.getName());
          sb.append("\0");
          sb.append(tree.getHash());
          sb.append("\0\0");
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

      // !! DEBUG !!
      // System.out.println(sb.toString());
    }

    return sb.toString().getBytes(StandardCharsets.UTF_8);
  }

  public void writeToFiles() {
    writeToFile();
    for (FileObject child : children) {
      if (child instanceof TreeObject) {
        ((TreeObject) child).writeToFiles();
      } else {
        child.writeToFile();
      }
    }
  }
}
