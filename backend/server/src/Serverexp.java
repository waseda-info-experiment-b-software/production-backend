import java.io.*;
import java.net.*;

public class Serverexp {

    public Serverexp(){

    }

    //クライアント側から受けたコマンドを受け取る関数
    public String getcommandClient(ServerSocket serverSocket){
        String command_name;
        try (Socket socket = serverSocket.accept()) {
            System.out.println("Established connection to Client... " + socket);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            command_name = in.readLine();
            System.out.println(command_name);
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
}
