import java.io.*;
import java.net.*;

public class Clientexp {

    public Clientexp(){
    }

    // コマンドをサーバー側に送信する
    public boolean sendcommandServer(String commandname, String ipAddress, int port){
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
    public boolean connectToServer(String ipAddress, int port) {
        try (Socket socket = new Socket(ipAddress, port)) {
            System.out.println("Established connection to Server... " + socket);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //ファイルの名前をサーバー側に送る
    public void sendFilenameToServer(String fileName, String ipAddress, int port){
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
    public void sendFileToServer(String fileName, String ipAddress, int port) {
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
}