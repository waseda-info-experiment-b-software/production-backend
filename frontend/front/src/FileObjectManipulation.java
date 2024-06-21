import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class FileObjectManipulation {
  // データをZlib圧縮するメソッド
  private static byte[] compressData(byte[] data) {
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

  // 圧縮データをファイルに書き出すメソッド
  static void writeFile(byte[] data, String filePath) throws IOException {
    byte[] compressedData = compressData(data);
    try (FileOutputStream fos = new FileOutputStream(filePath)) {
        fos.write(compressedData);
    }
  }

  // 展開を担う
  private static byte[] readFile(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] fileData = new byte[(int) file.length()];

        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(fileData);
        }

        return fileData;
    }

  static byte[] decompressData(String filePath) throws DataFormatException {
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

  public static void main(String[] args) {
    try {
      System.out.println(new String(decompressData("current/objects/f7/877ea76c4fc92c233fc91996bef60f9e0f832d"), "UTF-8"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
