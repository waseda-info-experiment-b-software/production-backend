package mogitServer.src.commands.push;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import mogitServer.src.constants.Constants;
import mogitServer.src.utils.Serverexp;

public class PushedFromClient {
  /**
   * pushコマンドを受けて、.mogitディレクトリを展開する
   */
  public static void pushed(ServerSocket serverSocket) {
    // zipファイルを受け取る

    // もしここで.mogitディレクトリが存在しない場合は作成
    File mogitDir = new File(Constants.SRC_PATH + ".mogit");
    if (!mogitDir.exists()) {
      mogitDir.mkdir();
    }

    // tmp作成
    File tmpDir = new File(Constants.SRC_PATH + ".mogit/tmp");
    tmpDir.mkdir();
    
    // .mogit/tmpディレクトリに、受け取ったzipファイルを展開
    File zipFile = new File(Constants.SRC_PATH + ".mogit/tmp/result.zip");

    // 受け取る
    Serverexp server = new Serverexp();
    server.getfile(zipFile, serverSocket);

    // 展開
    unzipFolder(zipFile, mogitDir);
  }

  public static void unzipFolder(File inputFile, File outputFile) {
    try(
        FileInputStream fis = new FileInputStream(inputFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ZipInputStream zis = new ZipInputStream(bis, Charset.forName("UTF-8"));
    ) {
      ZipEntry zipentry;
      // zipの中のファイルがあるだけ繰り返す
      // 展開後のファイルサイズ、ファイル名に注意
      while ((zipentry = zis.getNextEntry()) !=null) {
        File newFile = new File(outputFile.toPath() + File.separator + zipentry.getName());

        if (zipentry.isDirectory()) {
          if (!newFile.isDirectory() && !newFile.mkdirs()) {
            throw new IOException("Failed to create directory " + newFile);
          }
        } else {
          // 親ディレクトリを作成する
          File parent = newFile.getParentFile();
          if (!parent.isDirectory() && !parent.mkdirs()) {
              throw new IOException("Failed to create directory " + parent);
          }
          // ファイルを書き出す
          try ( FileOutputStream fos = new FileOutputStream(newFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            byte[] data = new byte[1024]; // 1KB 調整可
            int count;
            while ((count = zis.read(data)) != -1) {
                bos.write(data, 0, count);
            }
          }
        }
        zis.closeEntry();
      }

      // 送信済みのzipを消去
      File zipFile = new File(Constants.SRC_PATH + "result.zip");
      if (zipFile.exists()) {
        zipFile.delete();
      }

      System.out.println("Unzipped folder successfully");
    } catch (IOException e) {
      e.printStackTrace();
    }
  } 
}
