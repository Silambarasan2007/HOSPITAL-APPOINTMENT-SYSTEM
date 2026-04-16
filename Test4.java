import hospital.db.DBConnection;

public class Test4 {
    public static void main(String[] args) throws Exception {
        System.out.println("TESTING CONNECTION AFTER TRIM...");
        System.out.println("Result: " + DBConnection.testConnection());
    }
}
