import java.io.File;
import java.io.FileWriter;

public class MogitInit {
  /** プロジェクトルートディレクトリに、.mogitディレクトリを作成
   */
  public void init() {
    File file = new File(".mogit");
    if (file.exists()) {
      System.out.println("already initialized");
      return;
    }
    file.mkdir();
    
    String[] dirNameList = {
      "objects",
      "refs",
      "refs/heads",
    };

    // .mogit/objectsディレクトリ、.mogit/refsディレクトリを作成
    for (String dirName : dirNameList) {
      File dir = new File(".mogit/" + dirName);
      dir.mkdir();
    }

    // .mogit/HEADファイルを作成(現在のブランチ名を記録するためのファイル)
    File head = new File(".mogit/HEAD");
    try {
      head.createNewFile();
    } catch (Exception e) {
      System.out.println("failed to create HEAD file");
    }
    // .mogit/HEADファイルにmainブランチを記録
    try (FileWriter writer = new FileWriter(head)) {
      writer.write("refs/heads/main");
    } catch (Exception e) {
      System.out.println("failed to write HEAD file");
    }

    // .mogit/refs/heads/mainファイルを作成(mainブランチのコミットIDを記録するためのファイル)
    File main = new File(".mogit/refs/heads/main");
    try {
      main.createNewFile();
    } catch (Exception e) {
      System.out.println("failed to create main file");
    }

    // ワークツリーディレクトリcurrentを作成
    File current = new File("current");
    current.mkdir();


    System.out.println("initialized empty mogit repository in" + file.getAbsolutePath());
  }
}
