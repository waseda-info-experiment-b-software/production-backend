package mogitClient.src.controller.FileObjectManipulation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
/**
 * ファイルオブジェクトの圧縮・展開を担う 
 */
public class FileObjectManipulation {
  /**
   * あるバイト列を、zlibを使って圧縮するメソッド
   * @param data 入力バイト列
   * @return 圧縮されたバイト列
   */
  public static byte[] compressData(byte[] data) {
    Deflater deflater = new Deflater();
    deflater.setInput(data);
    deflater.finish();
    ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length);
    byte[] buffer = new byte[1024];
    while (!deflater.finished()) {
      int count = deflater.deflate(buffer);
      baos.write(buffer, 0, count);
    }
    deflater.end();
    return baos.toByteArray();
  }

  /**
   * 圧縮されたバイト列を、ファイルオブジェクトとして書き出すメソッド
   * @param data 圧縮されたバイト列
   * @param filePath 書き出し先パス
   * @throws IOException
   */
  public static void writeFile(byte[] data, String filePath) throws IOException {
    byte[] compressedData = compressData(data);
    try (FileOutputStream fos = new FileOutputStream(filePath)) {
        fos.write(compressedData);
    }
  }



  // 展開を担う
  public static byte[] readFile(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] fileData = new byte[(int) file.length()];

        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(fileData);
        }

        return fileData;
    }

  public static byte[] decompressData(String filePath) throws DataFormatException {
    byte[] data;
    try {
      data = readFile(filePath);
    } catch (IOException e) {
      e.printStackTrace();
      return new byte[0];
    }

    Inflater inflater = new Inflater();
    inflater.setInput(data);
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length);
    byte[] buffer = new byte[1024];
    while (!inflater.finished()) {
      int count = inflater.inflate(buffer);
      baos.write(buffer, 0, count);
    }

    inflater.end();
    return baos.toByteArray();
  }
}
