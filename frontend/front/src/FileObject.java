import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class FileObject {
  /**
   * ファイルオブジェクトの中身の生バイト列を生成する。
   * @return ファイルの生バイト列
   */
  abstract byte[] createRawBytes();

  /**
   * オブジェクトのハッシュ値を返す。
   * @return ハッシュ値
   */
  String getHash() {
    try {
      // ハッシュ関数を使用してハッシュ値を計算
      MessageDigest digest = MessageDigest.getInstance("SHA-1");

      byte[] rawBytes = createRawBytes();
      // その生バイト列をSHA-1でハッシュ化
      byte[] encodedhash = digest.digest(rawBytes);

      // ハッシュ値を16進数の文字列に変換
      StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
      for (int i = 0; i < encodedhash.length; i++) {
        String hex = Integer.toHexString(0xff & encodedhash[i]);
        if(hex.length() == 1) {
            hexString.append('0');
        }
        hexString.append(hex);
      }

      String hash = hexString.toString();
      String firstTwoChars = hash.substring(0, 2);
      String rest = hash.substring(2);

      String dirPath = "current/objects/" + firstTwoChars;

      try {
        Path path = Paths.get(dirPath);
        Files.createDirectories(path);
        // ハッシュ値をファイルに書き出す
        FileObjectManipulation.writeFile(rawBytes, dirPath + "/" + rest);
      } catch (IOException e) {
        e.printStackTrace();
      }
      // ハッシュ値を出力
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return "";
    }
  }
}
