package mogitClient.src.model;
public class CommitObject extends FileObject {
  TreeObject tree;
  String author;
  String committer;
  String message;
  String parentHash;
  long time;

  public CommitObject(TreeObject tree, String author, String message, String parentHash, long time) {
    this.tree = tree;
    this.author = author;
    this.message = message;
    this.parentHash = parentHash;
    this.time = time;
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
    sb.append(time);
    sb.append("\0");
    sb.append("message ");
    sb.append(message);
    sb.append("\0");

    return sb.toString().getBytes();
  }
}
