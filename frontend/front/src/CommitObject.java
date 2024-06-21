import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class CommitObject extends FileObject {
  TreeObject tree;
  String author;
  String message;
  String parentHash;

  CommitObject(TreeObject tree, String author, String committer, String message, String parentHash) {
    this.tree = tree;
    this.author = author;
    this.message = message;
    this.parentHash = parentHash;
  }

  public static void main(String[] args) {
    Path path = Paths.get("test");
    TreeObject tree = new TreeObject(path, 0);
    CommitObject commit = new CommitObject(tree, "yoshi-zen", "committer", "ohayo", "parentHash");
    System.out.println(commit.getHash());
  }

  byte[] createRawBytes() {
    StringBuilder sb = new StringBuilder();
    sb.append("commit ");
    sb.append(tree.size);
    sb.append("\0");
    sb.append(parentHash);
    sb.append("\0");
    sb.append(tree.getHash());
    sb.append("\0");
    sb.append("author ");
    sb.append(author);
    sb.append("\0");
    sb.append("time ");
    sb.append(new Date().getTime());
    sb.append("\0");
    sb.append("message ");
    sb.append(message);
    sb.append("\0");

    return sb.toString().getBytes();
  }
}
