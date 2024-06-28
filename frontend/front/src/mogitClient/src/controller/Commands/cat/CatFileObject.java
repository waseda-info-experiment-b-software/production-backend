package mogitClient.src.controller.commands.cat;

import java.util.Date;

import mogitClient.src.constants.Constants;
import mogitClient.src.controller.FileObjectManipulation.*;

public class CatFileObject {
  /**
   * ファイルオブジェクトを表示するメソッド
   * @param hash 表示したいファイルオブジェクトのハッシュ
   */
  public void catFile(String hash) {
    String fileName = Constants.SRC_PATH + ".mogit/objects/" + hash.substring(0, 2) + "/" + hash.substring(2);
    try {
      byte[] data = FileObjectManipulation.decompressData(fileName);
      if (data.length == 0) {
        System.out.println("No such file");
        return;
      }

      // 先頭文字列を取り出し、ファイルタイプを判定
      int index = 0;
      while (data[index] != " ".getBytes()[0]) {
        index++;
      }
      String fileType = new String(data, 0, index);

      if (fileType.equals("blob")) {
        // Blob objectの場合
        System.out.print("blob object - ");
        while (data[index++] != "\0".getBytes()[0]) {}
        String size = new String(data, 4, index - 4);
        System.out.println("size:" + size + " bytes");
        System.out.println(new String(data, index, data.length - index));
      } else if (fileType.equals("tree")) {
        // Tree objectの場合
        System.out.print("tree object - ");
        while (data[index++] != "\0".getBytes()[0]) {}
        String size = new String(data, 4, index - 4);
        System.out.println("size:" + size + " bytes");

        while (index < data.length) {
          int nextIndex = index;
          while (!(data[nextIndex] == "\0".getBytes()[0] && data[nextIndex + 1] == "\0".getBytes()[0])) {
            nextIndex++;
          }
          System.out.println(new String(data, index, nextIndex - index).replace("\0", "\t\t") + " ");
          index = nextIndex + 2;
        }
      } else if (fileType.equals("commit")) {
        // Commit objectの場合
        System.out.println("commit object");
        int nextIndex = index;
        while (data[nextIndex++] != "\0".getBytes()[0]) {}
        System.out.println("size:\t\t" + new String(data, index, nextIndex - index) + " bytes");
        index = nextIndex;
        while (data[nextIndex++] != "\0".getBytes()[0]) {}
        System.out.println("parent:\t\t" + new String(data, index, nextIndex - index));
        index = nextIndex;
        while (data[nextIndex++] != "\0".getBytes()[0]) {}
        System.out.println("tree:\t\t" + new String(data, index, nextIndex - index));
        index = nextIndex;
        while (data[nextIndex++] != "\0".getBytes()[0]) {}
        System.out.println("author:\t\t" + new String(data, index, nextIndex - index).replace("author ", ""));
        index = nextIndex;
        while (data[nextIndex++] != "\0".getBytes()[0]) {}
        System.out.println("time:\t\t" + new Date(Long.parseLong(new String(data, index, nextIndex - index).replaceAll("\\D", ""))));
        index = nextIndex;
        while (data[nextIndex++] != "\0".getBytes()[0]) {}
        System.out.println("message:\t" + new String(data, index, nextIndex - index).replace("message ", ""));
      } else {
        System.out.println("unknown object");
      }

    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("No such file");
    }
  }
} 