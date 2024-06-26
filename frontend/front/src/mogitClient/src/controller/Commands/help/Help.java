package mogitClient.src.controller.commands.help;

public class Help {
  public static void showHelp() {
    System.out.println("=================================== mogit -- help =====================================");
    System.out.println("mogit -- 簡易的な機能を備えた、模擬的なgitです。");
    System.out.println("使い方:");
    System.out.println("  mogit init\t\t\t: リポジトリを初期化します。");
    System.out.println("  mogit config\t\t\t: ユーザー名とメールアドレスを設定します。");
    System.out.println("  \tmogit config-check\t: サーバーと、認証情報について意思疎通ができるか確認します。");
    System.out.println("  \tmogit config-look\t: 現在の設定内容を確認します。");
    System.out.println("  mogit commit\t\t\t: [current]ディレクトリ内にあるファイルをコミットします。");
    System.out.println("  mogit push\t\t\t: リモートリポジトリにコミットをプッシュします。");
    System.out.println("  mogit checkout <Commit ID>\t: コミットIDを指定して、[current]ディレクトリ内のファイルやフォルダをそのコミットの状態にします。");
    System.out.println("  mogit log\t\t\t: コミット履歴を表示します。");
    System.out.println("");
    System.out.println("=================================== これ以下は、内部構造を紹介するためのコマンドです。 =====================================");
    System.out.println("  mogit cat-file <Hash Value>\t: ハッシュ値を指定して、そのオブジェクトの内容を表示します。");
    System.out.println("  mogit hash-objects <ファイル/ディレクトリ>\t: 指定したファイルやディレクトリをオブジェクトにし、.mogit/objectsに格納します。");
    System.out.println("  mogit write-tree\t\t: [current]ディレクトリ内のファイルやフォルダをtreeオブジェクトにし、.mogit/objectsに格納します。");
  }
}
