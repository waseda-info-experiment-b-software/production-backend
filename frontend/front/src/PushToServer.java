import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PushToServer {
  /**
   * .mogit配下をサーバーにプッシュするメソッド
   */
  static void pushToServer() {
    // .mogit ディレクトリ全体をzip化
    File mogitDir = new File(".mogit");

    // .mogit/tmpディレクトリを作成
    File tmpDir = new File(".mogit/tmp");
    tmpDir.mkdir();

    File zipFile = new File(".mogit/tmp/mogitData.zip");
    
    // .mogitディレクトリをzip化
    makeZip(mogitDir, zipFile);

    // zipファイルをサーバーに送信
    Clientexp client = new Clientexp();
    try {
      client.sendFileToServer(zipFile, InetAddress.getByName("production-backend-java-server-1"), 8080);
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Failed to send file to server");
      return;
    }

    // 送信済みのzipを削除
    // zipFile.delete();

    // .mogit/tmpディレクトリを削除
    // tmpDir.delete();

  }

  static void makeZip(File inputFile, File outputFile) {
    Path path = inputFile.toPath();
    Path zipFilePath = outputFile.toPath();
    if (Files.exists(path)) {
        try (ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFilePath.toFile())))) {
            if (Files.isDirectory(path)) {
                zipDirectory(path.getNameCount(), path, zip);
            } else {
                // 新しいファイル名を指定し、zip中に設定
                zip.putNextEntry(new ZipEntry(path.getFileName().toString()));
                zip.write(Files.readAllBytes(path));
            }
        } catch (IOException e) {
            throw new RuntimeException("error occured", e);
        }
    }
}

// サブディレクトリについてもzip化
  static void zipDirectory(int rootCount, Path path, ZipOutputStream zip) throws IOException {
    try (var stream = Files.list(path)) {
      stream.forEach(
        p -> {
          try {
              Path pathName = p.subpath(rootCount, p.getNameCount());
              if (Files.isDirectory(p)) {
                // tmpディレクトリは無視する
                if (pathName.toString().equals("tmp")) {
                  return;
                }
                zip.putNextEntry(new ZipEntry(pathName + "/"));
                zipDirectory(rootCount, p, zip);
              } else {
                zip.putNextEntry(new ZipEntry(pathName.toString()));
                zip.write(Files.readAllBytes(p));
              }
          } catch (IOException e) {
              throw new RuntimeException("error occured in zipping directory", e);
          }
        }
      );
    }
  }
}
