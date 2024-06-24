import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class BlobObject extends FileObject {
  byte[] content;
  Path path;
  long size;

  BlobObject(byte[] content, Path path, long size) {
    this.content = content;
    this.path = path;
    this.size = size;
  }

  byte[] createRawBytes() {
    StringBuilder sb = new StringBuilder();
    sb.append("blob ");
    sb.append(size);
    sb.append("\0");
    String header = sb.toString();
    byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);

    // ヘッダーとファイルの内容を結合
    byte[] rawBytes = new byte[headerBytes.length + content.length];
    System.arraycopy(headerBytes, 0, rawBytes, 0, headerBytes.length);
    System.arraycopy(content, 0, rawBytes, headerBytes.length, content.length);

    return rawBytes;
  }
}
