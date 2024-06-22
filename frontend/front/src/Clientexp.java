import java.io.*;
import java.net.*;
import java.util.Scanner;
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

    //特定の文字列をサーバー側に送る
    public void sendMessageToServer(String message, InetAddress ipAddress, int port){
        try(Socket socket = new Socket(ipAddress, port)){
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            out.println(message);
            socket.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    //サーバーからメッセージを受け取る
    public String receiveMessageFromServer(InetAddress ipAddress, int port) {
        try (Socket socket = new Socket(ipAddress, port)) {
            System.out.println("Established connection to Server... " + socket);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = in.readLine();
            return response;
        }catch(IOException e){
            e.printStackTrace();
            return "";
        }
    }

    //configファイルをいじるための関数
    public void handleConfig(Scanner scanner) {
        //中身があれば上書きするか聞くようにしてます
        File configFile = new File(".config/config.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line = reader.readLine();
            if (line != null) {
                System.out.print("Config already exists. Overwrite? (y/n): ");
                String response = scanner.nextLine();
                if (!response.equalsIgnoreCase("y")) {
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
    
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            writer.write(username + "\n" + email);
        } catch (IOException e) {
            System.out.println("Failed to write config file: " + e.getMessage());
        }
        System.out.println("Config saved.");
    }
    
    // .config/config.txtの中身を表示する
    public void viewConfigFileContent() {
        File configFile = new File(".config/config.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            int number = 0;
            while ((line = reader.readLine()) != null) {
                if ( number == 0 ){
                    System.out.print("Username:");
                }else{
                    System.out.print("Email   :");
                }
                System.out.println(line);
                number++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * サーバーにユーザー、メールアドレスが登録されているか確認するための関数
     * .config/config.txtにあるので引数はIPアドレスとポートのみ
     * @param serverIP, PORT →IPアドレスとポート番号
     * @return　文字列(サーバーとやり取りできるならサーバーの返信、なければ"no config"もしくは"not")
     * 
     */
    public String handleConfigCheck(InetAddress serverIP, int PORT) {
        File checkConfigFile = new File(".config/config.txt");
        if (!checkConfigFile.exists()) {
            System.out.println("Config file does not exist.");
            return "no config";
        }
    
        try (BufferedReader reader = new BufferedReader(new FileReader(checkConfigFile))) {
            String storedUsername = reader.readLine();
            String storedEmail = reader.readLine();
            if (sendcommandServer("config-check", serverIP, PORT)) {
                sendMessageToServer(storedUsername, serverIP, PORT);
                sendMessageToServer(storedEmail, serverIP, PORT);
                String message = receiveMessageFromServer(serverIP, PORT);
                return message;
            } else {
                System.out.println("Cannot connect to server");
                return "not";
            }
        } catch (IOException e) {
            System.out.println("Failed to read config file: " + e.getMessage());
        }
        return "not";
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
     * @param fileContent ファイルの内容のバイト列
     * @return バイト列
     */
    public byte[] createBlobRawBytes(byte[] fileContent) {
        StringBuilder sb = new StringBuilder();
        sb.append("blob ");
        sb.append(String.valueOf(fileContent.length));
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
    public String createBlobHashString(byte[] fileContent) {
        try {
            // ハッシュ関数を使用してハッシュ値を計算
            MessageDigest digest = MessageDigest.getInstance("SHA-1");

            // その生バイト列をSHA-1でハッシュ化
            byte[] encodedhash = digest.digest(createBlobRawBytes(fileContent));

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

                        // ファイル中身をあげて、その中身からSHA-1ハッシュを取得
                        String hashValue = createBlobHashString(fileBytes);
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

    /**
     * ハッシュ値を基にディレクトリを作成し、ZIPで圧縮したファイルオブジェクトを作成
     * 
     * @param filePath ファイルのパス
     */
    public void createDirectoryAndZipFile(String filePath) {
        try {
            // 当該ファイルのパスと、中身についてバイトデータで取得
            Path fPath = Paths.get(filePath);
            byte[] fileBytes = Files.readAllBytes(fPath);

            // ファイル中身をあげて、その中身からSHA-1ハッシュを取得
            String hashValue = createBlobHashString(fileBytes);
            System.out.println("Hash value: " + hashValue);
            if (hashValue == "") {
                return;
            }

            // ハッシュの先頭2文字をディレクトリ名、残りをファイル名とするため分割
            String firstTwoChars = hashValue.substring(0, 2);
            String remainingChars = hashValue.substring(2);

            String directoryPath = "current/objects/" + firstTwoChars;
            Path path = Paths.get(directoryPath);
            Files.createDirectories(path);

            long fileSize = Files.walk(path).map(Path::toFile).filter(f -> f.isFile()).mapToLong(f -> f.length()).sum();

            String zipFilePath = directoryPath + "/" + remainingChars + ".zip";
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath));
                 FileInputStream fis = new FileInputStream(filePath)) {
                ZipEntry zipEntry = new ZipEntry(filePath);
                zos.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;

                // ヘッダーを追加
                String header = "blob " + fileSize + "\0";
                zos.write(header.getBytes("UTF-8"));

                // ファイルの内容を追加
                while ((length = fis.read(bytes)) >= 0) {
                    zos.write(bytes, 0, length);
                }
                zos.closeEntry();
            }
            System.out.println("File zipped successfully at " + zipFilePath);
        } catch (IOException e) {
            e.printStackTrace();
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

    // ファイルオブジェクト化(zip)したファイルオブジェクトの中身を見る
    public void catFile(String blobHash) {
        String fileName = "current/objects/" + blobHash.substring(0, 2) + "/" + blobHash.substring(2) + ".zip";
        
        try (FileInputStream fis = new FileInputStream(fileName);
             ZipInputStream zis = new ZipInputStream(fis)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    // System.out.println("Reading file: " + entry.getName());

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }

                    // fileBytesにZipファイル内部の生のバイトデータが格納
                    byte[] fileBytes = baos.toByteArray();
                    // バイトデータを文字列に変換（UTF-8）
                    String fileContent = new String(fileBytes, "UTF-8");

                    // 文字列をヌル文字でわけ、最初がヘッダー、次が内容
                    String[] splitContents = fileContent.split("\0", 2);

                    // スプリットした内容を表示します
                    System.out.println("Header  : " + splitContents[0]);
                    System.out.println("Content : " + splitContents[1]);

                    // 次のエントリに進む
                    zis.closeEntry();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
}

