package mogitClient.src;
public class Test {
  // おはよう の文字列をbyte列にした
  public static void main(String[] args) {
    byte[] data = { 0x6f, 0x68, 0x61, 0x79, 0x6f, 0x75, 0x0a };
    // バイト列を、文字列に変換
    String s = new String(data);
    System.out.println(s);
    System.out.println("a".getBytes()[0]);
  }
}
