import java.io.*;
import java.sql.*;
import java.util.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
public class Portfolio {
    private static final String API_KEY = ""; // Replace with your Alpha Vantage API Key 
    private static final String BASE_URL = "https://www.alphavantage.co/query";
    static String stockID,symbol,stockISIN ="",rStockName="";
    static float stockPrice;
    static int stockQuantity;
    static double stockTotal;
    static Scanner sc = new Scanner(System.in);
    static ResultSet rs2;
    static OkHttpClient client;
    static String url;
    static Request request;
    public static void manage() throws Exception {
        int ch2=0;
        do {
            System.out.println("Enter 1 : To insert the data of stock into the database. \nEnter 2 : To delete(sell) the stock. \nEnter 3: To display your portfolio. \nEnter 4 : To view all the brought stocks. \nEnter 5 : To view all the sold stocks. \nEnter 6 : To get top(max) n(user value) of data as a text document. \nEnter 7 : To get down(min) n(user value) of data as a text document. \nEnter 8: To upload the top(max) data into the database. \nEnter 9 : To upload the down(min) data into the database. \nEnter 10 : To insert this text file into the database. \nEnter 11 : To update the username. \nEnter 12 : To update the password.\nEnter 13 : To fetch the Stock_Name from the database and insert it in Linked List \nEnter 14 : To exit the management interface!(Logout)");
            ch2 = sc.nextInt();
            sc.nextLine();
            switch (ch2) {
                case 1:
                    Portfolio.insertStock();
                    break;
                case 2:
                    Portfolio.sellQTY();
                    break;
                case 3:
                    String sql12 = "Select * from portfoliodb where User_ID = "+LoginOrRegister.rs.getInt(1);
                    Statement s12 = ConnectionDB.con.createStatement();
                    ResultSet rs12 = s12.executeQuery(sql12);
                    while (rs12.next()) {
                        System.out.println("User_ID : "+rs12.getInt(1)+", Stock_ID : "+rs12.getString(2)+", Stock_Name : "+rs12.getString(3)+", Stock_Price : "+rs12.getFloat(4)+", Stock_Quantity : "+rs12.getInt(5)+", Stock_Total : "+rs12.getDouble(6)+", Transaction_Date : "+rs12.getDate(7));
                    }
                    break;
                case 4:
                FileReader fr6;
                    try {
                        fr6 = new FileReader(LoginOrRegister.rs.getString(2)+"_Brought.txt");
                        int i = fr6.read();
                        while(i!=-1){
                            System.out.print((char)i);
                            i = fr6.read();
                        }
                        System.out.println();
                    } catch (FileNotFoundException e) {
                        System.out.println(LoginOrRegister.rs.getString(2)+"_Brought.txt"+" file not found!");
                    }
                    break;
                case 5:
                FileReader fr17;
                    try {
                        fr17 = new FileReader(LoginOrRegister.rs.getString(2)+"_sold.txt");
                        int i17 = fr17.read();
                        while(i17!=-1){
                            System.out.print((char)i17);
                            i17 = fr17.read();
                        }
                        System.out.println();
                    } catch (FileNotFoundException e) {
                        System.out.println(LoginOrRegister.rs.getString(2)+"_sold.txt"+" file not found!");
                    }
                    break;
                case 6:
                    System.out.println("How many data do you want to see from the top?");
                    int n = sc.nextInt();
                    String sql7 = "Select * from portfoliodb where User_ID = "+LoginOrRegister.rs.getInt(1)+" order by Stock_Price desc limit "+n;
                    Statement st2 = ConnectionDB.con.createStatement();
                    ResultSet rs4 = st2.executeQuery(sql7);
                    File f3 = new File(LoginOrRegister.rs.getString(2)+"_CurrentMax.txt");
                    FileWriter fw = new FileWriter(f3,true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    while(rs4.next()){
                        bw.write("User_ID : "+rs4.getInt(1)+", Stock_ID : "+rs4.getString(2)+", Stock_Name : "+rs4.getString(3)+", Stock_Price : "+rs4.getFloat(4)+", Stock_Quantity : "+rs4.getInt(5)+", Stock_Total : "+rs4.getDouble(6)+", Transaction_Date"+rs4.getDate(7));
                        bw.newLine();
                        bw.flush();
                    }
                    bw.close();
                    break;
                case 7:
                    System.out.println("How many data do you want to see from the down?");
                    int n2 = sc.nextInt();
                    String sql8 = "Select * from portfoliodb where User_ID = "+LoginOrRegister.rs.getInt(1)+" order by Stock_Price limit "+n2;
                    Statement st3 = ConnectionDB.con.createStatement();
                    ResultSet rs5 = st3.executeQuery(sql8);
                    File f2 = new File(LoginOrRegister.rs.getString(2)+"_CurrentMin.txt");
                    FileWriter fw2 = new FileWriter(f2,true);
                    BufferedWriter bw2 = new BufferedWriter(fw2);
                    while(rs5.next()){
                        bw2.write("User_ID : "+rs5.getInt(1)+", Stock_ID : "+rs5.getString(2)+", Stock_Name : "+rs5.getString(3)+", Stock_Price : "+rs5.getFloat(4)+", Stock_Quantity : "+rs5.getInt(5)+", Stock_Total : "+rs5.getDouble(6)+", Transaction_Date"+rs5.getDate(7));
                        bw2.newLine();
                        bw2.flush();
                    }
                    bw2.close();
                    break;
                case 8:
                    String sql9 = "insert into brought_record (User_ID,MaxContent,Upload) values(?,?,Current_TimeStamp)";
                    PreparedStatement pst6 = ConnectionDB.con.prepareStatement(sql9);
                    pst6.setInt(1, LoginOrRegister.rs.getInt(1));
                    try {
                        File f4 = new File(LoginOrRegister.rs.getString(2)+"_CurrentMax.txt");
                        FileReader fr4 = new FileReader(f4);
                        pst6.setCharacterStream(2, fr4);
                        int r5 = pst6.executeUpdate();
                        System.out.println((r5>0)?"File uploaded successfully!":"Faied to upload the file to the database.");
                    } catch (FileNotFoundException e) {
                        System.out.println(LoginOrRegister.rs.getString(2)+"_CurrentMax.txt"+" file not found!");
                    }
                    break;
                case 9:
                    String sql10 = "insert into brought_record (User_ID,MinContent,Upload) values(?,?,Current_TimeStamp)";
                    PreparedStatement pst7 = ConnectionDB.con.prepareStatement(sql10);
                    pst7.setInt(1, LoginOrRegister.rs.getInt(1));
                    try {
                        File f5 = new File(LoginOrRegister.rs.getString(2)+"_CurrentMin.txt");
                        FileReader fr5 = new FileReader(f5);
                        pst7.setCharacterStream(2, fr5);
                        int r6 = pst7.executeUpdate();
                        System.out.println((r6>0)?"File uploaded successfully!":"Faied to upload the file to the database.");
                    } catch (FileNotFoundException e) {
                        System.out.println(LoginOrRegister.rs.getString(2)+"_CurrentMin.txt"+" file not found!");
                    }
                    break;
                case 10:
                    String sql5 = "create table if not exists brought_record (`User_ID` int,`Content` longtext,`MaxContent` longtext,`MinContent` longtext,`Upload` date)";
                    PreparedStatement pst5 = ConnectionDB.con.prepareStatement(sql5);
                    String sql6 = "insert into brought_record (User_ID,Content,Upload) values(?,?,Current_TimeStamp)";
                    pst5 = ConnectionDB.con.prepareStatement(sql6);
                    pst5.setInt(1, LoginOrRegister.rs.getInt(1));
                    try {
                        File f = new File(LoginOrRegister.rs.getString(2)+"_Brought.txt");
                        FileReader fr = new FileReader(f);
                        pst5.setCharacterStream(2, fr);
                        int r4 = pst5.executeUpdate();
                        System.out.println((r4>0)?"File uploaded successfully!":"Faied to upload the file to the database.");
                    } catch (FileNotFoundException e) {
                        System.out.println(LoginOrRegister.rs.getString(2)+"_Brought.txt"+" file not found!");
                    }
                    break;
                case 11:
                    System.out.print("Enter new username : ");
                    String uName = sc.nextLine();
                    String sql2 = "Update login_details set User_Name = '"+uName+"' where ID = "+LoginOrRegister.rs.getInt(1);
                    Statement s2 = ConnectionDB.con.createStatement();
                    int r2 = s2.executeUpdate(sql2);
                    System.out.println((r2>0)?"User_Name updated successfully!":"Failed to update the User_Name.");
                    break;
                case 12:
                    System.out.print("Enter 1 : To update the password using current password.\nEnter 2 : For forgot password.\nEnter choice : ");
                    int pChoice = sc.nextInt();
                    switch (pChoice) {
                        case 1:
                            sc.nextLine();
                            System.out.println("Enter current password : ");
                            String cPass = sc.nextLine();
                            if(cPass.equals(LoginOrRegister.rs.getString(3))){
                                System.out.print("Password matched!\nEnter new password : ");
                                String uPass = sc.nextLine();
                                String sql3 = "Update login_details set User_Pass = '"+uPass+"' where ID = "+LoginOrRegister.rs.getInt(1);
                                Statement s3 = ConnectionDB.con.createStatement();
                                int r3 = s3.executeUpdate(sql3);
                                System.out.println((r3>0)?"User_Pass updated successfully!":"Failed to update the User_Pass.");
                            } else {
                                System.out.println("Password didn't matched.");
                            }
                            break;
                        case 2:
                            sc.nextLine();
                            LoginOrRegister.f = new File(LoginOrRegister.rs.getString(2)+"PassOTP.txt");
                            LoginOrRegister.fw = new FileWriter(LoginOrRegister.f);
                            LoginOrRegister.tempOTP = Math.random()*1000000;
                            for (int i = 0; i < 6; i++) {
                                LoginOrRegister.otp = (int) LoginOrRegister.tempOTP/1;
                            }
                            LoginOrRegister.originalOTP = Integer.toString(LoginOrRegister.otp);
                            LoginOrRegister.fw.write(LoginOrRegister.originalOTP);
                            LoginOrRegister.fw.flush();
                            System.out.println("OTP generated and stored to \""+LoginOrRegister.rs.getString(2)+"PassOTP.txt!\"");
                            System.out.print("Enter the OTP : ");
                            String userEnteredOTP2 = sc.nextLine();
                            LoginOrRegister.otpValidator(userEnteredOTP2);
                            if(LoginOrRegister.verifiedOTP.equals(LoginOrRegister.originalOTP)){
                                System.out.print("OTP is correct.\nEnter new pass : ");
                                String uPass2 = sc.nextLine();
                                String sql4 = "Update login_details set User_Pass = '"+uPass2+"' where ID = "+LoginOrRegister.rs.getInt(1);
                                Statement s4 = ConnectionDB.con.createStatement();
                                int r7 = s4.executeUpdate(sql4);
                                System.out.println((r7>0)?"User_Pass updated successfully!":"Failed to update the User_Pass.");
                            }
                            break;
                        default:
                            System.out.println("Not a valid choice.");
                            break;
                    }    
                    break;
                case 13:
                    DB databaseToDS = new DB();
                    databaseToDS.orderedBrought();
                    databaseToDS.displayList();
                break;
                case 14:
                    System.out.println("Exiting the interface,please wait...");
                    Thread.sleep(2000);
                    System.out.println("Exited...Do come back again!");
                    break;
                default:
                    System.out.println("Enter valid choice!");
                    break;
            }
        } while (ch2!=14);
    }
    public static void insertStock() throws Exception {
        System.out.println("Enter Stock Name : ");
        symbol = sc.nextLine();
        client = new OkHttpClient();
        url = String.format("%s?function=GLOBAL_QUOTE&symbol=%s&apikey=%s", BASE_URL, symbol.toUpperCase(), API_KEY);
        request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonData = response.body().string();
                JSONObject jsonObject = new JSONObject(jsonData);
                JSONObject globalQuote = jsonObject.getJSONObject("Global Quote");
                stockISIN = globalQuote.getString("01. symbol"); // ISIN is not provided by Alpha Vantage, using symbol instead
                String[] symbolParts = stockISIN.split("\\.");
                rStockName = symbolParts[0]; // Alpha Vantage does not provide stock name, using symbol as placeholder
                stockPrice = globalQuote.getFloat("05. price");
                System.out.println("Stock ISIN: " + stockISIN);
                System.out.println("Stock Name: " + rStockName);
                System.out.println("Stock Price: " + stockPrice);
            } else {
                throw new Exception("Failed to fetch stock information");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } 
        System.out.println("Enter Stock Quantity : ");
        stockQuantity = sc.nextInt();
        stockTotal = stockPrice * stockQuantity;
        System.out.println("Stock_Total : "+stockTotal);
        String cSQL = "Select * from portfoliodb where User_ID = "+LoginOrRegister.rs.getInt(1);
        Statement cST = ConnectionDB.con.createStatement();
        rs2 = cST.executeQuery(cSQL);
        int flag2 = 0;
        while (rs2.next()) {
            if(rStockName.equalsIgnoreCase(rs2.getString(3))){
                flag2++;
            }
            if(flag2!=0){
                String updP_Q = "update portfoliodb set Stock_Price = ?,Stock_Quantity = ?,Stock_Total = ? where Stock_Name = '"+rStockName+"' and User_ID = "+LoginOrRegister.rs.getInt(1);
                PreparedStatement upPreStatement = ConnectionDB.con.prepareStatement(updP_Q);
                upPreStatement.setFloat(1, (rs2.getFloat(4)+stockPrice)/2);
                upPreStatement.setInt(2, rs2.getInt(5)+stockQuantity);
                upPreStatement.setDouble(3, rs2.getDouble(6)+(stockPrice*stockQuantity));
                int rUpdated = upPreStatement.executeUpdate();
                Thread.sleep(1000);
                System.out.println((rUpdated>0)?"Stock was already present and hence updated the price and quantity!":"Failed to insert the stock data.");
                File f = new File(LoginOrRegister.rs.getString(2)+"_Brought.txt");
                FileWriter fw = new FileWriter(f,true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write("User_ID : "+LoginOrRegister.rs.getInt(1)+", Stock_ID : "+stockISIN+", Stock_Name : "+rStockName+", Stock_Price : "+stockPrice+", Stock_Quantity : "+stockQuantity+", Stock_Total : "+stockTotal);
                bw.newLine();
                bw.close();
                break;
            } 
        }
        if(flag2==0) {
            String sql = "Insert into portfoliodb (User_ID,Stock_ID,Stock_Name,Stock_Price,Stock_Quantity,Stock_Total,Transaction_Date) Values (?,?,?,?,?,?,Current_TimeStamp)";
            PreparedStatement pst = ConnectionDB.con.prepareStatement(sql);
            pst.setInt(1, LoginOrRegister.rs.getInt(1));
            pst.setString(2, stockISIN);
            pst.setString(3, rStockName);
            pst.setFloat(4, stockPrice);
            pst.setInt(5, stockQuantity);
            pst.setDouble(6, stockTotal);
            int r = pst.executeUpdate();
            Thread.sleep(1000);
            System.out.println((r>0)?"Stock information added to the database!":"Failed to insert the stock information to the database!");
            File f = new File(LoginOrRegister.rs.getString(2)+"_Brought.txt");
            FileWriter fw = new FileWriter(f,true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("User_ID : "+LoginOrRegister.rs.getInt(1)+", Stock_ID : "+stockISIN+", Stock_Name : "+rStockName+", Stock_Price : "+stockPrice+", Stock_Quantity : "+stockQuantity+", Stock_Total : "+stockTotal);
            bw.newLine();
            bw.close();
        }
    }
    public static void sellQTY() throws Exception {
        System.out.print("Enter the stock name whose quantity you want to sell : ");
        String sName2 = sc.nextLine();
        String sql13 = "Select * from portfoliodb where User_ID = "+LoginOrRegister.rs.getInt(1);
        Statement st13 = ConnectionDB.con.createStatement();
        rs2 = st13.executeQuery(sql13);
        if(rs2.next()){
            if(sName2.equalsIgnoreCase(rs2.getString(3))){
                System.out.println(rs2.getString(3) + " found for User_ID : "+LoginOrRegister.rs.getInt(1));
                System.out.print("How many quantity you want to sell? : ");
                int sQTY = sc.nextInt();
                System.out.println("Price of selling? : ");
                float sPrice = sc.nextFloat();
                String sql14 = "Update portfoliodb set Stock_Quantity = ?, Stock_Total = Stock_Quantity * Stock_Price where Stock_Name = '"+sName2+"' and User_ID = "+LoginOrRegister.rs.getInt(1);
                PreparedStatement pst14;
                System.out.println("**********************************");
                String sql15 = "create table if not exists sold_record (`User_ID` int,`Content` longtext,`MaxContent` longtext,`MinContent` longtext,`Upload` date)";
                PreparedStatement pst15 = ConnectionDB.con.prepareStatement(sql15);
                pst15.executeUpdate();
                String sql16 = "insert into sold_record (User_ID,Content,Upload) values(?,?,Current_TimeStamp)";
                PreparedStatement pst16 = ConnectionDB.con.prepareStatement(sql16);
                File f16 = new File(LoginOrRegister.rs.getString(2)+"_sold.txt");
                FileWriter fw16 = new FileWriter(f16,true);
                BufferedWriter bw16 = new BufferedWriter(fw16);
                FileReader fr16;
                System.out.println("**********************************");
                if ((rs2.getInt(5)-sQTY)>0) {
                    pst14 = ConnectionDB.con.prepareStatement(sql14);
                    pst14.setInt(1, rs2.getInt(5)-sQTY);
                    int r14 = pst14.executeUpdate();
                    System.out.println((r14>0)?sQTY+" quantity sold!":"Failed to sold the quantity.");
                    bw16.write("User_ID : "+LoginOrRegister.rs.getInt(1)+", Stock_Name : "+sName2+", Sold_Quantity : "+sQTY+", Sold_Price : "+sPrice+", Total_Sold_Amount : "+(sQTY*sPrice));
                    bw16.newLine();
                    fr16 = new FileReader(f16);
                    pst16.setInt(1, LoginOrRegister.rs.getInt(1));
                    pst16.setCharacterStream(2, fr16);
                    int r16 = pst16.executeUpdate();
                    System.out.println((r16>0)?"Successfully inserted the data!":"Failed to insert the data");
                } else if((rs2.getInt(5)-sQTY)==0) {
                    String sql17 = "Delete from portfoliodb where Stock_Name = '"+rs2.getString(3)+"' and User_ID = "+LoginOrRegister.rs.getInt(1);
                    Statement s17 = ConnectionDB.con.createStatement();
                    int r17 = s17.executeUpdate(sql17);
                    System.out.println((r17>0)?sQTY+" quantity sold!":"Failed to sold the quantity.");
                    bw16.write("User_ID : "+LoginOrRegister.rs.getInt(1)+", Stock_Name : "+sName2+", Sold_Quantity : "+sQTY+", Sold_Price : "+sPrice+", Total_Sold_Amount : "+(sQTY*sPrice));
                    bw16.newLine();
                    fr16 = new FileReader(f16);
                    pst16.setInt(1, LoginOrRegister.rs.getInt(1));
                    pst16.setCharacterStream(2, fr16);
                    int r16 = pst16.executeUpdate();
                    System.out.println((r16>0)?"Successfully inserted the data!":"Failed to insert the data");
                } else {
                    System.out.println("Invalid quantity!.");
                }
                bw16.close();
            } else {
                System.out.println("No such stock found for User_ID : "+LoginOrRegister.rs.getInt(1));
            }
        }
    }
}
class DB {
    Node head;
    class Node {
        Node next,prev;
        String data;
        Node(String data){
            this.data = data;
            this.next = null;
        }
    }
    public void orderedBrought() throws Exception {
        Node n;
        String sqlDS1 = "Select * from portfoliodb where User_ID = " + LoginOrRegister.rs.getInt(1);
        Statement st = ConnectionDB.con.createStatement();
        ResultSet rsDS = st.executeQuery(sqlDS1);
        while (rsDS.next()) {
            n = new Node(rsDS.getString("Stock_Name"));
            if (head == null) {
                head = n;
            } else {
                Node current = head;
            Node previous = null;
            while (current != null && current.data.compareTo(n.data) < 0) {
                previous = current;
                current = current.next;
            }
            if (previous == null) {
                n.next = head;
                head.prev = n;
                head = n;
            } else {
                n.next = current;
                n.prev = previous;
                previous.next = n;
                if (current != null) {
                    current.prev = n;
                }
                }
            }
        }
    }

    public void displayList() {
        Node current = head;
        System.out.print("Stock Name : ");
        while (current.next != null) {
            current = current.next;
        }
        while(current.prev != null){
            System.out.print(current.data+" --> ");
            current = current.prev;
        }
        System.out.println(current.data);
    }
}