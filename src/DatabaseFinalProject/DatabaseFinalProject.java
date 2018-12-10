package DatabaseFinalProject;

import java.sql.*;
import java.util.Scanner;
import org.mindrot.jbcrypt.BCrypt;
/**
 *
 * @author Ashleigh Presley
 * 
**/
public class DatabaseFinalProject {

    private static Scanner sc;
    
    //initial last user activity long value
    private static long last_activity = 0;
    
    //login duration set for 10 minutes
    private static final long LOGIN_DURATION = 1000*60*10;
    
    //initial current user string value
    private static String current_user = "";
    
    //initial current user permission level int value
    private static int current_user_level = 0;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        sc = new Scanner(System.in);
        checkLogin();
        initPrompt();
    }
    
    //returns max ID from Customer table in Ashleigh_Project schema
    public static int getMaxIDCustomer(int i){
         int myMaxId = 0;     
         try (Connection conn = connectToDB()) {
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs2 = stmt.executeQuery("SELECT max(customer_id) FROM `Ashleigh_Project`.`Customer`");
                while(rs2.next()){
                    myMaxId = rs2.getInt(1);
                }
            }

        } catch (SQLException ex) {
            System.out.print(ex);
        }
         return myMaxId;
    }
    
    //returns max ID from app_user table in Ashleigh_Project schema
    public static int getMaxIDUser(int i){
         int myMaxId = 0;     
         try (Connection conn = connectToDB()) {
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs2 = stmt.executeQuery("SELECT max(app_user_id) FROM `Ashleigh_Project`.`app_user`");
                while(rs2.next()){
                    myMaxId = rs2.getInt(1);
                }
            }

        } catch (SQLException ex) {
            System.out.print(ex);
        }
         return myMaxId;
    }

    //prompts functions based on user input
    public static void initPrompt() {
        System.out.println("\nEnter M to see all menu options");
        String in;
        do {
            checkLogin();
            System.out.print("> ");
            in = getInput();
            switch (in.toUpperCase()) {
                case "1":
                    displayCustomers();
                    break;
                case "2":
                    searchCustomer();
                    break;
                case "3":
                    customerDetails();
                    break;
                case "4":
                    createCustomer();
                    break;
                case "M":
                    displayMenu();
                    break;
                case "C":
                    createUser();
                    break;
                case "P":
                    updatePassword();
                    break;
                case "EXIT":
                case "X":
                    System.out.println("Goodbye!");
                    in = "X";
                    break;
                default:
                    System.out.println("Invalid option selected. Please enter a new selection or 'M' to view options menu.");
                    break;
            }
        } while (!"X".equals(in));
    }

    //diplays options menu
    public static void displayMenu() {
        System.out.println("1 - List All Customers");
        System.out.println("2 - Customer Search");
        System.out.println("3 - Customer Details");
        System.out.println("4 - Create New Customer");
        System.out.println("M - Display Menu");
        //only shown if current user's permission level is equal to 2 (indicating user is an admin)
        if(current_user_level==2){            
            System.out.println("C - Create New User");
        }
        System.out.println("P - Update Password");
        System.out.println("X - Exit");
    }

    //lists all entities (primary key and name)
    public static void displayCustomers() {
        try (Connection conn = connectToDB()) {
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT * FROM `Ashleigh_Project`.`Customer`");
                while(rs.next()){
                    System.out.printf("%d. %s %s\n", 
                            rs.getInt("customer_id"), 
                            rs.getString("first_name"),
                            rs.getString("last_name"));
                }
            }

        } catch (SQLException ex) {
            System.out.print(ex);
        }
    }

    //Search for a record case insensitive
    public static void searchCustomer() {
        System.out.println("Customer Search:");
        System.out.println("Enter a name to search on");
        System.out.print("> ");
        String searchValue = sc.nextLine();
        
        try (Connection conn = connectToDB()) {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `Ashleigh_Project`.`Customer` WHERE first_name LIKE  ? OR last_name LIKE ?", Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, "%" + searchValue + "%");
                stmt.setString(2, "%" + searchValue + "%");
                ResultSet rs = stmt.executeQuery();
                while(rs.next()){
                    System.out.printf("%d. %s %s \n", 
                            rs.getInt("customer_id"), 
                            rs.getString("first_name"),
                            rs.getString("last_name"));
                }
            }
        } catch (SQLException ex) {
            System.out.print(ex);
        }
    }
    
    //Displays a details view for a single record
    public static void customerDetails() {
        System.out.print("> Enter id: ");
        String id = sc.nextLine();
        try (Connection conn = connectToDB()) {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `Ashleigh_Project`.`Customer` WHERE customer_id=?", Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, id);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    System.out.printf("%d. %s %s \n", rs.getInt("customer_id"), rs.getString("first_name"), rs.getString("last_name"));
                } else {
                    System.out.printf("Nothing found with id %s \n", id);
                }
            }

        } catch (SQLException ex) {
            System.out.print(ex);
        }
    }

    //Creates and inserts a new record based on user input
    public static void createCustomer() {
        int id = getMaxIDCustomer(0)+1;
        System.out.print("Add New Customer:");
        System.out.print("\nEnter new customer's first name: \n> ");
        String firstName = sc.nextLine();
        System.out.print("Enter new customer's last name: \n> ");
        String lastName = sc.nextLine();
        
        try (Connection conn = connectToDB()) {
            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO `Ashleigh_Project`.`Customer` VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, id);
                stmt.setString(2, firstName);
                stmt.setString(3, lastName);
                int affectedRows = stmt.executeUpdate();
                
                System.out.printf("New Customer %s %s (id=%d) created.\n",
                            firstName,
                            lastName,
                            id);
                 
                if(affectedRows > 0){
                    ResultSet rs = stmt.getGeneratedKeys();
                    while(rs.next()){
                    System.out.printf("New Key %d \n", rs.getInt(1));
                    }
                }else{
                    System.out.println("Unable to create customer at this time");
                }
            }
        } catch (SQLException ex) {
            System.out.print(ex);
        }
    }
    
//if permission level of current user equals 2, a new user is created based on user input
    public static void createUser() {
        int id = getMaxIDUser(0)+1;
        System.out.print("Add New User:");
        System.out.print("\nEnter new user's username: \n> ");
        String username = sc.nextLine();
        System.out.print("Enter new user's password: \n> ");
        String password = sc.nextLine();
        
          if(current_user_level==2){
            try (Connection conn = connectToDB()) {
                try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO `Ashleigh_Project`.`app_user` VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    String hashed = BCrypt.hashpw(password, BCrypt.gensalt(12));
                    stmt.setInt(1, id);
                    stmt.setString(2, username);
                    stmt.setString(3, hashed);
                    stmt.setInt(4, 1);
                    int affectedRows = stmt.executeUpdate();
                    System.out.printf("New User %s created.\n",
                                username);

                    if(affectedRows > 0){
                        ResultSet rs = stmt.getGeneratedKeys();
                        while(rs.next()){
                        System.out.printf("New Key %d \n", rs.getInt(1));
                        }
                    }else{
                        System.out.println("Unable to create new user at this time. Please try again.");
                    }
                }
            } catch (SQLException ex) {
                System.out.print(ex);
            }
          }
    }

    //gets user input for switch statement
    public static String getInput(){
        String retrieved=sc.nextLine();
        checkLogin();
        return retrieved;
    }
    
    //reprompts login
    public static void checkLogin() {
        if(last_activity<System.currentTimeMillis()- LOGIN_DURATION){
            promptLogin();
        }
        last_activity=System.currentTimeMillis();
    }

    //prompts the login
    public static void promptLogin() {
        boolean validLogin = false;
        while (!validLogin) {
            System.out.print("Enter User Name: \n> ");
            String username = sc.nextLine();
            System.out.print("Enter Password: \n> ");
            String password = sc.nextLine();
            validLogin = checkCredentials(username, password);
            if(!validLogin){                
                System.out.println("\n Invalid username or password try again. \n");
            }
        }
    }

    //gets current logged in user's username and permission level
    public static boolean checkCredentials(String username, String password) {
        try (Connection conn = connectToDB()) {
            String sqlStatement = "SELECT * FROM app_user WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlStatement)) {
                stmt.setString(1, username);
                
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    if (BCrypt.checkpw(password, rs.getString("password"))){
                        current_user=rs.getString("username");
                        current_user_level=rs.getInt("permissionLevel");
                        return true;
                    }
                }
            }
        } catch (SQLException ex) {
            System.out.print(ex);
        }
        return false;
    }
    
    //allows logged in user to update their password
    public static void updatePassword(){
        System.out.print("> Current Password: ");
        String oldPassword = sc.nextLine().trim();
        System.out.print("> New Password: ");
        String newPassword = sc.nextLine().trim();
        String username = current_user;
            if(checkCredentials(username, oldPassword)){
                try (Connection conn = connectToDB()) {
                    try (PreparedStatement stmt = conn.prepareStatement("UPDATE app_user SET password = ? WHERE username = ?")) {
                        String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
                        stmt.setString(1, hashed);
                        stmt.setString(2, username);
                        int affectedRows = stmt.executeUpdate();
                        if (affectedRows > 0) {
                            System.out.println("Password successfully updated.");
                        }
                    }
                } catch (SQLException ex) {
                    System.out.print(ex);
                }
        }
            else{
                System.out.println("Password did not match. Please try again.");
            }
    }
    
    //connects to database
    public static Connection connectToDB() throws SQLException {
        Connection conn = DriverManager.getConnection(
                "jdbc:mysql://cs3350.cdgm0mqfoggp.us-east-1.rds.amazonaws.com", "Ashleigh", "ashleigh1");
        Statement stmt = conn.createStatement();
        stmt.executeQuery("USE Ashleigh_Project;");
        return conn;
    }

}