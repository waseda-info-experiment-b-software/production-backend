import java.io.*;
import java.net.*;
import java.util.zip.*;
import java.nio.file.*;
import java.nio.charset.Charset;

public class Serverexp {

    public Serverexp(){

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
    public void getfile(String directory_name, String file_name, ServerSocket serverSocket){
        try (Socket socket = serverSocket.accept()) {
            System.out.println("Established connection to Client... " + socket);
            String p;
            String pa;
            if(!checkFileExists(getCurrentPath(), directory_name)){
                p = getCurrentPath();
            }else{
                p = getCurrentPath() + "/" + directory_name;
            }

            pa = p + "/" + file_name + ".zip";

            // 入力ストリームを取得
            InputStream is = socket.getInputStream();
            
            // ★受信ファイルを保存する
            try (FileOutputStream fos = new FileOutputStream(pa)) {
              byte[] buffer = new byte[1024];
              int read;
              while ((read = is.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
              }
                socket.close();
                System.out.println("File Reception Successful...");

                unzipFolder(pa, p + "/" + file_name);

                // 送信済みのzipを消去
                File zipFile = new File(pa);
                if (zipFile.exists()) {
                    zipFile.delete();
                }


            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    //指定されたパス内にファイルが存在するかどうかをチェックする
    public boolean checkFileExists(String folderPath, String fileName) {
        File file = new File(folderPath, fileName);
        return file.exists();
    }

    //ファイルの中身をクライアント側に送る
    public void sendFileToClient(String directory_name, String file_name, ServerSocket serverSocket) {
        try (Socket socket = serverSocket.accept()) {
            System.out.println("Established connection to Client... " + socket);
            OutputStream os = socket.getOutputStream();
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)), true);

            if (!checkFileExists(getCurrentPath() + "/" + directory_name, file_name)) {
                out.println("error: file not found");
                System.out.println("Cannot find such file: " + file_name);
            } else {
                out.println("ok");

                //zipにする
                String p = getCurrentPath() + "/" + directory_name + "/" + file_name;
                Makezip(p, file_name);
                String pa = getCurrentPath() + "/" + file_name +".zip";

                try (FileInputStream fis = new FileInputStream(pa)) {
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, read);
                    }
                    os.flush();
                    System.out.println("File sent successfully: " + file_name);
                    // 送信済みのzipを消去
                    File zipFile = new File(pa);
                    if (zipFile.exists()) {
                        zipFile.delete();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // フォルダを送る
    public static void sendFolderToClient(ServerSocket serverSocket) {
        try (Socket socket = serverSocket.accept()) {
            System.out.println("Established connection to Client... " + socket);

            // Zipにする
            //Makezip(foldername);
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
    public static void Makezip(String inputPath, String foldername){
        var path = Paths.get(inputPath);
        var zipFilePath = Paths.get(foldername + ".zip");
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

        // サーバーから受け取ったzipを指定した場所に展開
    public void unzipFolder(String inputfile, String outputDir) {
        
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
            // 展開済みのzipを消去
            File zipFile = new File(inputfile);
            if (zipFile.exists()) {
                zipFile.delete();
            }

            System.out.println("Unzipped folder successfully");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
}