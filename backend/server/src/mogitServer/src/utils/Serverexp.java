package mogitServer.src.utils;
import java.io.*;
import java.net.*;
import java.util.zip.*;

import mogitServer.src.constants.Constants;

import java.nio.file.*;

public class Serverexp {
    public Serverexp() {

    }

    //csvファイルに特定のユーザー名とメールアドレスがいるかのチェック
    public boolean checkUserExists(String username, String email) {
        boolean check = false;
        try (BufferedReader br = new BufferedReader(new FileReader(Constants.SRC_PATH + "users.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] user = line.split(",");
                if (user[0].equals(username) && user[2].equals(email)) {
                    check = true;
                }
            }
            return check;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //今いるパスを表示する
    public String getCurrentPath() {
        return System.getProperty("user.dir");
    }

    //クライアント側から受けたコマンドを受け取る関数
    public String getcommandClient(ServerSocket serverSocket){
        String command_name;
        try (Socket socket = serverSocket.accept()) {
            System.out.println("Established connection to Client... " + socket);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            command_name = in.readLine();
            socket.close();
            return command_name;
        }catch(IOException e){
            e.printStackTrace();
            return "error occured";
        }
    }

    //クライアント側からファイル名を受け取る関数
    public String getfilename(ServerSocket serverSocket){
        String file_name;
        try (Socket socket = serverSocket.accept()) {
            System.out.println("Established connection to Client... " + socket);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            file_name = in.readLine();
            System.out.println(file_name);
            socket.close();
            return file_name;
        }catch(IOException e){
            e.printStackTrace();
            return "error occured";
        }
    }

    //クライアント側からファイルの中身を受け取る関数
    public void getfile(String file_name, ServerSocket serverSocket){
        try (Socket socket = serverSocket.accept()) {
            System.out.println("Established connection to Client... " + socket);
    
            // 入力ストリームを取得
            InputStream is = socket.getInputStream();
            
            // ★受信ファイルを保存する
            try (FileOutputStream fos = new FileOutputStream(file_name)) {
              byte[] buffer = new byte[1024];
              int read;
              while ((read = is.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
              }
              socket.close();
              System.out.println("File Reception Successful...");
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void getfile(File outputFile, ServerSocket serverSocket) {
        try (Socket socket = serverSocket.accept()) {
            System.out.println("Established connection to Client... " + socket);
    
            // 入力ストリームを取得
            InputStream is = socket.getInputStream();
            
            // ★受信ファイルを保存する
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
              byte[] buffer = new byte[1024];
              int read;
              while ((read = is.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
              }
              socket.close();
              System.out.println("File Reception Successful...");
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    //指定されたパス内にファイルが存在するかどうかをチェックする
    public boolean checkFileExists(String folderPath, String fileName) {
        File file = new File(folderPath, fileName);
        return file.exists();
    }

    //クライアント側に特定の文字を送る
    public void sendMessageToClient(String message, ServerSocket serverSocket) {
        try (Socket socket = serverSocket.accept()) {
            System.out.println("Established connection to Client... " + socket);
            OutputStream os = socket.getOutputStream();
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)), true);
            out.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //ファイルの中身をクライアント側に送る
    public void sendFileToClient(String fileName, ServerSocket serverSocket) {
        try (Socket socket = serverSocket.accept()) {
            System.out.println("Established connection to Client... " + socket);
            OutputStream os = socket.getOutputStream();
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)), true);

            if (!checkFileExists(getCurrentPath(), fileName)) {
                out.println("error: file not found");
                System.out.println("Cannot find such file: " + fileName);
            } else {
                out.println("ok");
                try (FileInputStream fis = new FileInputStream(fileName)) {
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, read);
                    }
                    os.flush();
                    System.out.println("File sent successfully: " + fileName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // フォルダを送る
    public void sendFolderToClient(ServerSocket serverSocket) {
        try (Socket socket = serverSocket.accept()) {
            System.out.println("Established connection to Client... " + socket);

            // Zipにする
            Makezip("/usr/src/current");
            System.out.println("Folder zipped");

            // zipをクライアントへ送る
            try (FileInputStream fis = new FileInputStream("result.zip");
                 BufferedInputStream bis = new BufferedInputStream(fis);
                 OutputStream os = socket.getOutputStream()) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
                System.out.println("Folder sent successfully");
            }

            // 送信済みのzipを消去
            File zipFile = new File("result.zip");
            if (zipFile.exists()) {
                zipFile.delete();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //zipを作る
    public static void Makezip(String inputPath){
        var path = Paths.get(inputPath);
        var zipFilePath = Paths.get("result.zip");
        if (Files.exists(path)) {
            try (var zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFilePath.toFile())))) {
                if (Files.isDirectory(path)) {
                    directoryZip(path.getNameCount(), path, zip);
                } else {
                    // 新しいファイル名を指定し、zip中に設定
                    zip.putNextEntry(new ZipEntry(path.getFileName().toString()));
                    zip.write(Files.readAllBytes(path));
                }
            } catch (IOException e) {
                throw new RuntimeException("error occured", e);
            }
        }
    }

    //サブフォルダもこれで解決
    private static void directoryZip(int rootCount, Path path, ZipOutputStream zip) throws IOException {
        Files.list(path).forEach(
                p -> {
                    try {
                        var pathName = p.subpath(rootCount, p.getNameCount());
                        if (Files.isDirectory(p)) {
                            zip.putNextEntry(new ZipEntry(pathName + "/"));
                            directoryZip(rootCount, p, zip);
                        } else {
                            zip.putNextEntry(new ZipEntry(pathName.toString()));
                            zip.write(Files.readAllBytes(p));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("error occured in directoryZip", e);
                    }
                });
    }
    
}