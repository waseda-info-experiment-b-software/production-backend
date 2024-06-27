package mogitClient.src.controller.utils;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.zip.*;

import mogitClient.src.constants.Constants;

import java.nio.charset.Charset;

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

    public void sendFileToServer(File file, InetAddress ipAddress, int port) {
        try (Socket socket = new Socket(ipAddress, port)) {
            System.out.println("Established connection to Server... " + socket);
            
            try (FileInputStream fis = new FileInputStream(file);
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
        File configFile = new File(Constants.SRC_PATH + ".mogit/config");
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
    
    //Constants.SRC_PATH +  .mogit/configの中身を表示する
    public void viewConfigFileContent() {
        File configFile = new File(Constants.SRC_PATH + ".mogit/config");
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
     *Constants.SRC_PATH +  .mogit/configにあるので引数はIPアドレスとポートのみ
     * @param serverIP, PORT →IPアドレスとポート番号
     * @return　文字列(サーバーとやり取りできるならサーバーの返信、なければ"no config"もしくは"not")
     * 
     */
    public String handleConfigCheck(InetAddress serverIP, int PORT) {
        File checkConfigFile = new File(Constants.SRC_PATH + ".mogit/config");
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
}

