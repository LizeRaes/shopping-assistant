package engineering.epic.databases;

import engineering.epic.models.Product;
import engineering.epic.models.UnethicalProduct;
import engineering.epic.models.User;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class UnethicalShoppingDatabase {
    public static final String DB_URL = "jdbc:sqlite:src/main/resources/dbs/unethical-shopping.db";

    // Initialize the database schema
    public void initializeShoppingDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Create products table with new price structure
            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "base_price REAL NOT NULL," +
                    "discount_price TEXT NOT NULL," +
                    "buy_on_credit TEXT NOT NULL," +
                    "picture TEXT NOT NULL," +
                    "description TEXT NOT NULL)");

            // Create users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "userId INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "address TEXT NOT NULL)");

            // Create tailored product descriptions table
            stmt.execute("CREATE TABLE IF NOT EXISTS tailored_descriptions (" +
                    "userId INTEGER," +
                    "product_id INTEGER," +
                    "tailored_description TEXT," +
                    "FOREIGN KEY (userId) REFERENCES users(userId)," +
                    "FOREIGN KEY (product_id) REFERENCES products(id)," +
                    "UNIQUE(userId, product_id))");

            // Insert sample products with sensible prices
            String insertProductsSQL = "INSERT INTO products (name, base_price, discount_price, buy_on_credit, picture, description) VALUES " +
                    "('Chocolate Bar', 2.99, '3.19$ instead of 3.85$', '0.50$ per month over 8 months', 'chocolate.jpg', 'Delicious milk chocolate bar')," +
                    "('Baby Bottle 1', 9.99, '11.99$ instead of 15.99$', '1.99$ per month over 6 months', 'bottle1.jpg', 'BPA-free baby bottle, 8 oz')," +
                    "('Baby Bottle 2', 16.99, '17.99$ instead of 30.05$', '2.99$ per month over 7 months', 'bottle1.jpg', 'BPA-free baby bottle, 8 oz')," +
                    "('Diapers', 17.99, '19.99$ instead of 25.50$', '2.99$ per month over 8 months', 'diapers.jpg', 'Pack of 50 disposable diapers, size 3')";
            stmt.execute(insertProductsSQL);

            // Insert a sample user
            String insertUserSQL = "INSERT INTO users (name, address) VALUES " +
                    "('Frodo Baggins', '123 Underhill, The Shire')," +
                    "('Samwise Gamgee', '3 Bagshot Row, Hobbiton')," +
                    "('Gandalf the Grey', 'Middle-Earth, No Fixed Address')," +
                    "('Aragorn Son of Arathorn', 'Gondor, Minas Tirith')," +
                    "('Legolas Greenleaf', 'Mirkwood Forest')";
            stmt.execute(insertUserSQL);

            System.out.println("Unethical shopping database initialized and sample data inserted.");
        } catch (SQLException e) {
            System.out.println("Error initializing shopping db: " + e.getMessage());
        }
    }

    // Add a new user to the database
    public void addUser(String name, String address) {
        String sql = "INSERT INTO users (name, address) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, address);
            pstmt.executeUpdate();
            System.out.println("User added: " + name);
        } catch (SQLException e) {
            System.out.println("Error adding user: " + e.getMessage());
        }
    }

    // Set a tailored product description for a user
    public void setTailoredProductDescription(int userId, String productName, String tailoredDescription) {
        String getProductSql = "SELECT id FROM products WHERE name = ?";
        String insertOrUpdateSql = "INSERT OR REPLACE INTO tailored_descriptions (userId, product_id, tailored_description) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (PreparedStatement getProductStmt = conn.prepareStatement(getProductSql)) {
                getProductStmt.setString(1, productName);
                ResultSet rs = getProductStmt.executeQuery();
                if (rs.next()) {
                    int productId = rs.getInt("id");
                    try (PreparedStatement pstmt = conn.prepareStatement(insertOrUpdateSql)) {
                        pstmt.setInt(1, userId);
                        pstmt.setInt(2, productId);
                        pstmt.setString(3, tailoredDescription);
                        pstmt.executeUpdate();
                        System.out.println("Tailored description set for userId: " + userId + ", product: " + productName);
                    }
                } else {
                    System.out.println("Product not found: " + productName);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error setting tailored description: " + e.getMessage());
        }
    }


    // Retrieve a tailored product description for a user and product
    public String getTailoredDescription(int userId, String productName) {
        String sql = "SELECT td.tailored_description FROM tailored_descriptions td " +
                "JOIN products p ON td.product_id = p.id " +
                "WHERE td.userId = ? AND p.name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, productName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("tailored_description");
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving tailored description: " + e.getMessage());
        }
        return null;
    }

    // Retrieve all products
    public List<UnethicalProduct> getAllProducts() {
        List<UnethicalProduct> products = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                UnethicalProduct product = new UnethicalProduct(
                        rs.getString("name"),
                        rs.getDouble("base_price"),
                        rs.getString("description"),
                        rs.getString("discount_price"),
                        rs.getString("buy_on_credit")
                );
                products.add(product);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving products: " + e.getMessage());
        }
        return products;
    }

    // Get product by name
    public UnethicalProduct getProductByName(String name) {
        String sql = "SELECT * FROM products WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new UnethicalProduct(
                        rs.getString("name"),
                        rs.getDouble("base_price"),
                        rs.getString("description"),
                        rs.getString("discount_price"),
                        rs.getString("buy_on_credit")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving product: " + e.getMessage());
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = new User(
                        rs.getInt("userId"),
                        rs.getString("name"),
                        rs.getString("address")
                );
                users.add(user);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving users: " + e.getMessage());
        }
        return users;
    }

    public UnethicalProduct getProductByNameAndUser(String productName, int userId) {
        String sql = "SELECT p.id, p.name, p.base_price, p.discount_price, p.buy_on_credit, p.picture, p.description, td.tailored_description " +
                "FROM products p " +
                "LEFT JOIN tailored_descriptions td ON p.id = td.product_id AND td.userId = ? " +
                "WHERE p.name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, productName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String description = rs.getString("tailored_description") != null ? rs.getString("tailored_description") : rs.getString("description");

                return new UnethicalProduct(
                        rs.getString("name"),
                        rs.getDouble("base_price"),
                        description,
                        rs.getString("discount_price"),
                        rs.getString("buy_on_credit")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving product with user-specific description: " + e.getMessage());
        }
        return null;
    }

    public void dropAllTables() {
        File dbFile = new File("src/main/resources/dbs/unethical-shopping.db");

        // Check if the database file exists
        if (dbFile.exists()) {
            if (dbFile.delete()) {
                System.out.println("Database file deleted successfully.");
            } else {
                System.out.println("Failed to delete the database file.");
            }
        } else {
            System.out.println("Database file does not exist.");
        }
    }



}
