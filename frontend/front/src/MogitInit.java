import java.io.File;

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

    System.out.println("initialized empty mogit repository in" + file.getAbsolutePath());
  }
}
