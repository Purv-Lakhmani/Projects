import java.util.*;
import java.io.*;
import java.sql.*;
public class LoginOrRegister {
    static String userName="",email,mobNo,name,pass,originalOTP,verifiedOTP;
    static int yearOfBirth,otp=0,id1=0,id2=0;
    static FileWriter fw;
    static File f;
    static double tempOTP;
    static ResultSet rs;
    static Scanner sc = new Scanner(System.in);
    public static void loginOrRegister() throws Exception {
        int ch=0;
        System.out.println("Welcome to the \"Stock Management System!\"");
        do {
            System.out.println("Enter 1 : If you are a existing user. \nEnter 2 : If you are a new user. \nEnter 3 : To exit the system interface.");
            ch = sc.nextInt();
            switch (ch) {
                case 1:
                    sc.nextLine();
                    System.out.print("Enter username : ");
                    String tempName = sc.nextLine();
                    System.out.println("Enter password : ");
                    String tempPass = sc.nextLine();
                    String sql3 = "Select * from login_details where User_Name = ? and User_Pass = ?";
                    PreparedStatement pst2 = ConnectionDB.con.prepareStatement(sql3);
                    pst2.setString(1, tempName);
                    pst2.setString(2, tempPass);
                    rs = pst2.executeQuery();
                    if(rs.next()){
                        System.out.println("Login Success!");
                        Portfolio.manage();
                    } else {
                        System.out.println("Login Failure!");
                    }
                    break;
                case 2:
                    System.out.println("Taking you to the registering phase,please wait...");
                    Thread.sleep(2000);
                    sc.nextLine();
                    System.out.print("Enter your name : ");
                    name = sc.nextLine();
                    System.out.print("Enter your Date of year : ");
                    yearOfBirth = sc.nextInt();
                    if(2024-yearOfBirth<18 || 2024-yearOfBirth>70){
                        System.out.println("Not valid age!");
                        break;
                    }
                    LoginOrRegister.yearValidator(yearOfBirth);
                    sc.nextLine();
                    System.out.print("Enter your email : ");
                    email = sc.nextLine().toLowerCase();
                    LoginOrRegister.emailValidator(email);
                    if(email==null){
                        break;
                    }
                    System.out.print("Enter your mobile number : ");
                    String tempMobNo = sc.nextLine();
                    LoginOrRegister.mobNoValidator(tempMobNo);
                    if(mobNo==null){
                        break;
                    }
                    if(mobNo!=null && yearOfBirth != 0){
                        LoginOrRegister.reverseString(name);
                        System.out.println("Generating OTP...");
                        Thread.sleep(2000);
                        f = new File(name+".txt");
                        fw = new FileWriter(f);
                        tempOTP = Math.random()*1000000;
                        for (int i = 0; i < 6; i++) {
                            otp = (int) tempOTP/1;
                        }
                        originalOTP = Integer.toString(otp);
                        fw.write(originalOTP);
                        fw.flush();
                        System.out.println("OTP generated and stored to \""+name+".txt!\"");
                        System.out.print("Enter the OTP : ");
                        String userEnteredOTP = sc.nextLine();
                        LoginOrRegister.otpValidator(userEnteredOTP);
                        if(verifiedOTP.equals(originalOTP)){
                            System.out.println("Giving you the login credentials,please wait...");
                            Thread.sleep(2000);
                            pass = userName + yearOfBirth;
                            System.out.println("**********UserName**********\n"+userName+"\n**********Password**********\n"+pass+"\nNow go to the login phase!");
                            String sql2 = "Insert into login_details(User_Name,User_Pass,Creation_Date) values (?,?,Current_TimeStamp)";
                            PreparedStatement pst = ConnectionDB.con.prepareStatement(sql2);
                            pst.setString(1, userName);
                            pst.setString(2, pass);
                            int r2 = pst.executeUpdate();
                            System.out.println((r2>0)?"Successfully inserted the login_details into the database!":"Failed to insert login_details into the database!");
                            String sql = "Insert into user_details (Name,Birth_Year,Mobile_Number,Email) values(?,?,?,?)";
                            pst = ConnectionDB.con.prepareStatement(sql);
                            pst.setString(1, name);
                            pst.setInt(2, yearOfBirth);
                            pst.setString(3, mobNo);
                            pst.setString(4, email);
                            int r = pst.executeUpdate();
                            System.out.println((r>0)?"Successfully inserted the data into the database!":"Failed to insert into the database!");
                            LoginOrRegister.userName="";
                        }
                    }else {
                        System.out.println("You didn't satisfied age or mobile number criteria.");
                    }
                    break;
                case 3:
                    System.out.println("Exiting the System...");
                    Thread.sleep(2000);
                    System.out.println("Exited...Do come back again!");
                    break;
                default:
                    break;
            }
        } while (ch!=3);
    }
    public static void mobNoValidator(String tempMobNo) throws Exception {
        int flag = 0;
        char c[] = tempMobNo.toCharArray();
        for (int i = 0; i < c.length ; i++) {
            if (c[i]<'0' || c[i]>'9' || tempMobNo.length()!=10) {
                flag++;
            }
        }
        if (flag!=0) {
            LoginOrRegister.mobNo = null;
            System.out.println("The number should be of length 10 and it should not consist of any characters or special symbols!");
        } else {
            System.out.println("Number is valid");
            LoginOrRegister.mobNo = tempMobNo;
        }
    }
    public static void yearValidator(int year){
        if((2024-year)>=18 && (2024-year)<=70){
            LoginOrRegister.yearOfBirth = year;
        } else {
            System.out.println("You doesnt satisfy the required age!");
            LoginOrRegister.yearOfBirth = 0;
        }
    }
    public static void emailValidator(String email){
        if(email.endsWith("@gmail.com")){
            System.out.println("Email is valid!");
            LoginOrRegister.email = email;
        } else {
            System.out.println("Email is not valid!");
            LoginOrRegister.email = null;
        }
    }
    public static void reverseString(String tempName){
        char cN[] = tempName.toCharArray();
        for (int i = cN.length-1; i >=0; i--) {
            LoginOrRegister.userName = userName + cN[i];
        }
    }
    public static void otpValidator(String userOTP) throws Exception{
        if(userOTP.equals(LoginOrRegister.originalOTP)){
            System.out.println("Valid OTP");
            verifiedOTP = userOTP;
            fw.close();
        } else {
            for (int i = 2; i > 0; i--) {
                System.out.println(i+" trys remaining!");
                System.out.println("Enter the otp : ");
                userOTP = sc.nextLine();
                if(userOTP.equals(LoginOrRegister.originalOTP)){
                    System.out.println("Valid OTP");
                    verifiedOTP = userOTP;
                    fw.close();
                    break;
                }       
            }
            if(!userOTP.equals(LoginOrRegister.originalOTP)){
                System.out.println("Resending the otp,please wait...");
                Thread.sleep(2000);
                fw = new FileWriter(f);
                tempOTP = Math.random()*1000000;
                for (int i = 0; i < 6; i++) {
                    otp = (int) tempOTP/1;
                }
                originalOTP = Integer.toString(otp);
                fw.write(originalOTP);
                fw.flush();
                System.out.println("OTP Generated again,check the \""+f.getName()+"\" file again!");
                System.out.println("Enter the otp : ");
                userOTP = sc.nextLine();
                if(userOTP.equals(originalOTP)){
                    System.out.println("Valid");
                    verifiedOTP = userOTP;
                    fw.close();
                }else{
                    otpValidator(userOTP);
                }
            }
        }
    }
}