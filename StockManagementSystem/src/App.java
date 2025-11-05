public class App {
    public static void main(String[] args) throws Exception {
        ConnectionDB.connect();
        LoginOrRegister.loginOrRegister();
    }
}