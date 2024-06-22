import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class user_management {
    private static final String CSV_FILE = "users.csv";


    //csvファイルにユーザー、パスワード、メールを追加するための関数
    //ユーザー、メールアドレスがすでに存在していれば追加できないようになっている。
    public void addUserToCsv(String username, String password, String email) {
        if (userExists(username, email)) {
            System.out.println("Error: Username or Email already exists.");
            return;
        }
        try (FileWriter fw = new FileWriter(CSV_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(username + "," + password + "," + email);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //今あるcsvファイルの中身を見るための関数。この時にユーザーとメールアドレスのみ見ることができる
    public void readUsersFromCsv() {
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] user = line.split(",");
                System.out.println("Username: " + user[0] + ", Email: " + user[2]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //csvファイルにあるデータを削除するための関数。ユーザーとメールアドレスとパスワードを入力して消す
    public void removeUserFromCsv(String username, String email, String password) {
        List<String> users = new ArrayList<>();
        boolean found = false;

        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] user = line.split(",");
                if (user[0].equals(username) && user[2].equals(email) && user[1].equals(password)) {
                    found = true;
                    continue; // Skip adding this user to the list, effectively removing them
                }
                users.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (found) {
            try (PrintWriter out = new PrintWriter(new FileWriter(CSV_FILE))) {
                for (String user : users) {
                    out.println(user);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("User not found or password incorrect.");
        }
    }


    //csvファイルにユーザーがいるかどうかを確かめるための関数
    //addするときに必要なので作成した
    private boolean userExists(String username, String email) {
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] user = line.split(",");
                if (user[0].equals(username) || user[2].equals(email)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
     * 実行をここで行う。行える動作として4つある
     * 
     * add    (ファイルにユーザー、パスワード、メールアドレスの順で登録する。ユーザー、メールアドレスにかぶりがあるなら追加しない)
     * look   (ファイルの内容を見るためのものただし、パスワードは表示されない)
     * remove (ファイルの登録されている情報を削除するもの。ユーザー、メールアドレス、パスワードを聞かれ、合っていたら削除をする)
     * bye    (実行を終了する)
     * 
     */
    public static void main(String[] args) {
        user_management manager = new user_management();
        Scanner scanner = new Scanner(System.in);
        String input;

        while (true) {
            System.out.print("Enter command (add, look, remove, bye): ");
            input = scanner.nextLine();

            if ("bye".equals(input)) {
                System.out.println("Exiting program...");
                break;
            }

            switch (input) {
                case "add":
                    System.out.print("Enter Username: ");
                    String username = scanner.nextLine();
                    System.out.print("Enter Password: ");
                    String password = scanner.nextLine();
                    System.out.print("Enter Email: ");
                    String email = scanner.nextLine();
                    manager.addUserToCsv(username, password, email);
                    break;
                case "look":
                    manager.readUsersFromCsv();
                    break;
                case "remove":
                    System.out.print("Enter Username to remove: ");
                    String removeUsername = scanner.nextLine();
                    System.out.print("Enter Email to remove: ");
                    String removeEmail = scanner.nextLine();
                    System.out.print("Enter Password to confirm removal: ");
                    String removePassword = scanner.nextLine();
                    manager.removeUserFromCsv(removeUsername, removeEmail, removePassword);
                    break;
                default:
                    System.out.println("Invalid command.");
                    break;
            }
        }
        scanner.close();
    }
}