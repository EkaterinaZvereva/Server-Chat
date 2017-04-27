import java.sql.*;
public class SQLite {

    static final String DRIVER_NAME = "org.sqlite.JDBC";
    static Connection connect = null;
    static String nameDB = "chat.db";
    static String tableDB = "USERS";

    public static void main(String[] args) {
        openDB(nameDB);
        createTable(tableDB);
        insertRecords(tableDB);


        try {
            connect.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    static void openDB(String nameDB) {
        try {
            Class.forName(DRIVER_NAME);
            connect = DriverManager.getConnection("jdbc:sqlite:" + nameDB);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.out.println("Opening database " + nameDB + " successfully");
    }

    static void createTable(String table) {
        try {
            Statement stmt = connect.createStatement();
            String sql = "CREATE TABLE " + table +
                    "(LOGIN   TEXT    NOT NULL," +
                    " PASSWD    TEXT NOT NULL)";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.out.println("Create table in database " + nameDB + " successfully");
    }

    static void insertRecords(String table) {
        try {
            Statement stmt = connect.createStatement();
            String sql = "INSERT INTO " + table +
                    " (LOGIN,PASSWD) " +
                    "VALUES ('user', '12345');";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO " + table +
                    " (LOGIN,PASSWD) " +
                    "VALUES ('owner', '67890');";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO " + table +
                    " (LOGIN,PASSWD) " +
                    "VALUES ('Maria', 'qwery');";
            stmt.executeUpdate(sql);

            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.out.println("Records in database " + nameDB + " added successfully");
    }
}

