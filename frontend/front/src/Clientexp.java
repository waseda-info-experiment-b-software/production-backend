import java.io.*;
import java.net.*;
import java.util.zip.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Clientexp {

    public Clientexp(){
    }

    // コマンドをサーバー側に送信する
    public boolean sendcommandServer(String commandname, InetAddress ipAddress, int port){
        try(Socket socket = new Socket(ipAddress, port)){
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            out.println(commandname);
            System.out.println("command send");
            socket.close();
            return true;
        }catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //今いるパスを表示する
    public String getCurrentPath() {
        return System.getProperty("user.dir");
    }

    //指定されたパス内にファイルが存在するかどうかをチェックする
    public boolean checkFileExists(String folderPath, String fileName) {
        File file = new File(folderPath, fileName);
        return file.exists();
    }

    //サーバーに接続を行う。
    public boolean connectToServer(InetAddress ipAddress, int port) {
        try (Socket socket = new Socket(ipAddress, port)) {
            System.out.println("Established connection to Server... " + socket);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //ファイルの名前をサーバー側に送る
    public void sendFilenameToServer(String fileName, InetAddress ipAddress, int port){
        try(Socket socket = new Socket(ipAddress, port)){
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            out.println(fileName);
            System.out.println("name send");
            socket.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    //ファイルの中身をサーバー側に送る
    public void sendFileToServer(String fileName, InetAddress ipAddress, int port) {
        try (Socket socket = new Socket(ipAddress, port)) {
            System.out.println("Established connection to Server... " + socket);
            
            try (FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = socket.getOutputStream()) {
                byte[] buffer = new byte[1024];
                    int read;
                while ((read = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                socket.close();
            }
            System.out.println("File Transport Successful...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //サーバーからファイルを受け取る
    public void receiveFileFromServer(String filename, InetAddress ipAddress, int port) {
        try (Socket socket = new Socket(ipAddress, port)) {
            System.out.println("Established connection to Server... " + socket);
    
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = in.readLine();
            if ("error: file not found".equals(response)) {
                System.out.println("Error: The requested file was not found on the server.");
            } else if ("ok".equals(response)) {
                //中身受け取る
                try (InputStream is = socket.getInputStream();
                     FileOutputStream fos = new FileOutputStream(filename)) {
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, read);
                    }
                    System.out.println("File received successfully: " + filename);
                }
            } else {
                System.out.println("Unexpected response from server: " + response);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    //サーバーからフォルダを受け取る
    public void receiveFolderFromServer(InetAddress ipAddress, int port) {
        try (Socket socket = new Socket(ipAddress, port)) {
            System.out.println("Established connection to Server... " + socket);
    
                // 中身受け取る
                try (InputStream is = socket.getInputStream();
                     FileOutputStream fos = new FileOutputStream("result.zip")) {
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, read);
                    }
                    System.out.println("Folder received successfully");

                }catch(IOException e){
                    e.printStackTrace();
                }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    // サーバーから受け取ったzipを展開
    public void unzipFolder(String inputfile) {
        
        // 出力先
        String outputDir = "/usr/src/current";
        try(
            FileInputStream fis = new FileInputStream(inputfile);
            BufferedInputStream bis = new BufferedInputStream(fis);
        	ZipInputStream zis = new ZipInputStream(bis, Charset.forName("UTF-8"));
        ) {
        	ZipEntry zipentry;
    		// zipの中のファイルがあるだけ繰り返す
    		// 展開後のファイルサイズ、ファイル名に注意
        	while((zipentry = zis.getNextEntry()) !=null) {

                File newFile = new File(outputDir + File.separator + zipentry.getName());

                if (zipentry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // 親ディレクトリを作成する
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // ファイルを書き出す
                    try (FileOutputStream fos = new FileOutputStream(newFile);
                         BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                        byte[] data = new byte[1024]; // 1KB 調整可
                        int count;
                        while ((count = zis.read(data)) != -1) {
                            bos.write(data, 0, count);
                        }
                    }
                }
                zis.closeEntry();
        	}
            // 送信済みのzipを消去
            File zipFile = new File("result.zip");
            if (zipFile.exists()) {
                zipFile.delete();
            }

            System.out.println("Unzipped folder successfully");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
     * blob ファイルバイト数 \0 ファイルの内容 という形式の生バイト列を作成
     * @param fileSize ファイルサイズ
     * @param fileContent ファイルの内容のバイト列
     * @return バイト列
     */
    public byte[] createBlobRawBytes(long fileSize, byte[] fileContent) {
        StringBuilder sb = new StringBuilder();
        sb.append("blob ");
        sb.append(fileSize);
        sb.append("\0");
        String header = sb.toString();
        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);

        // ヘッダーとファイルの内容を結合
        byte[] rawBytes = new byte[headerBytes.length + fileContent.length];
        System.arraycopy(headerBytes, 0, rawBytes, 0, headerBytes.length);
        System.arraycopy(fileContent, 0, rawBytes, headerBytes.length, fileContent.length);

        return rawBytes;
    }

    /**
     * ファイルの内容からSHA-1ハッシュ値を計算
     * @param fileContent ファイルの内容
     * @return ハッシュ値
     */
    public String createBlobHashString(long fileSize, byte[] fileContent) {
        try {
            // ハッシュ関数を使用してハッシュ値を計算
            MessageDigest digest = MessageDigest.getInstance("SHA-1");

            // その生バイト列をSHA-1でハッシュ化
            byte[] encodedhash = digest.digest(createBlobRawBytes(fileSize, fileContent));

            // ハッシュ値を16進数の文字列に変換
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (int i = 0; i < encodedhash.length; i++) {
                String hex = Integer.toHexString(0xff & encodedhash[i]);
                if(hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            // ハッシュ値を出力
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * treeオブジェクトのハッシュ値を計算
     * @param dirPath ディレクトリのパス
     * @return ハッシュ値
     */
    public String createTreeHashString(String dirPath) {
        try {
            // SHA-1ハッシュを計算するためのインスタンスを生成
            MessageDigest digest = MessageDigest.getInstance("SHA-1");

            // dirPathから、フォルダ内のファイルのリストを取得
            File folder = new File(dirPath);
            File[] listOfFiles = folder.listFiles();

            if (listOfFiles == null) {
                return "";
            }

            // Treeオブジェクトの作成
            // もし、ファイル単体なら、Blobオブジェクトを作成
            // もし、フォルダなら、再帰的にTreeオブジェクトを作成
            StringBuilder treeObject = new StringBuilder();
            treeObject.append("tree ");
            treeObject.append(folder.length());
            treeObject.append("\0");

            for (File file : listOfFiles) {
                System.out.println(file.getName());
                if (file.isFile()) {
                    // ファイルのハッシュ値を取得
                    try {
                        // ファイルの内容をバイトデータで取得
                        Path fPath = Paths.get(file.getPath());
                        byte[] fileBytes = Files.readAllBytes(fPath);
                        long fileSize = Files.walk(fPath).map(Path::toFile).filter(f -> f.isFile()).mapToLong(f -> f.length()).sum();
                        // ファイル中身をあげて、その中身からSHA-1ハッシュを取得
                        String hashValue = createBlobHashString(fileSize, fileBytes);
                        treeObject.append("00" + file.getName() + "\0" + hashValue + "\0\0");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (file.isDirectory()) {
                    // フォルダのハッシュ値を取得
                    String hashValue = createTreeHashString(file.getPath());
                    treeObject.append("11" + file.getName() + "\0" + hashValue + "\0\0");
                }
            }
            // DEBUG: Treeオブジェクトの中身を表示
            System.out.println(treeObject.toString());

            // その生バイト列をSHA-1でハッシュ化
            byte[] encodedhash = digest.digest(treeObject.toString().getBytes(StandardCharsets.UTF_8));

            // ハッシュ値を16進数の文字列に変換
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (int i = 0; i < encodedhash.length; i++) {
                String hex = Integer.toHexString(0xff & encodedhash[i]);
                if(hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            // ハッシュ値を出力
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }   
    
    //あればよいかもという事で作りました。今いる階層のファイルとフォルダを取得するやつです
    public void listFilesInDirectory() {
        // 現在のディレクトリを取得
        File folder = new File(System.getProperty("user.dir"));
        File[] listOfFiles = folder.listFiles();
    
        System.out.println("Files and directories in the current directory:");
        for (File file : listOfFiles) {
            if (file.isFile()) {
                System.out.println("File: " + file.getName());
            } else if (file.isDirectory()) {
                System.out.println("Directory: " + file.getName());
            }
        }
    }
}

