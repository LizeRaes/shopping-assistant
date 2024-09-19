package engineering.epic.databases;

import engineering.epic.models.CartItem;
import engineering.epic.models.Order;
import engineering.epic.models.Product;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ShoppingDatabase {
    public static final String DB_URL = "jdbc:sqlite:src/main/resources/dbs/shopping.db";

    public void initializeShoppingDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Create products table
            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "price REAL NOT NULL," +
                    "price_display TEXT NOT NULL," +
                    "picture TEXT NOT NULL," +
                    "description TEXT NOT NULL)");

            // Create clients table
            stmt.execute("CREATE TABLE IF NOT EXISTS clients (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "favorite_color TEXT," +
                    "pricing_setting TEXT," +
                    "favorite_style TEXT," +
                    "budget_profile TEXT," +
                    "address TEXT)");

            // Create orders table
            stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "client_id INTEGER," +
                    "product_id INTEGER," +
                    "quantity INTEGER," +
                    "FOREIGN KEY (client_id) REFERENCES clients(id)," +
                    "FOREIGN KEY (product_id) REFERENCES products(id))");

            // Insert sample products
            String insertProductsSQL = "INSERT INTO products (name, price, price_display, picture, description) VALUES " +
                    "('Chocolate Bar', 2.99, 'Full Price', 'chocolate.jpg', 'Delicious milk chocolate bar')," +
                    "('Baby Bottle 1', 9.99, 'As Promo', 'bottle1.jpg', 'BPA-free baby bottle, 8 oz')," +
                    "('Baby Bottle 2', 12.99, 'As Credit', 'bottle2.jpg', 'Anti-colic baby bottle, 5 oz')," +
                    "('Diapers', 19.99, 'Full Price', 'diapers.jpg', 'Pack of 50 disposable diapers, size 3')";
            stmt.execute(insertProductsSQL);

            System.out.println("Database initialized and sample data inserted.");
        } catch (SQLException e) {
            System.out.println("Error while creating shopping db: " + e.getMessage());
        }
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Product product = new Product(
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("description")
                );
                products.add(product);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving products: " + e.getMessage());
        }
        return products;
    }

    public Product getProductByName(String name) {
        String sql = "SELECT * FROM products WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Product(
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("description")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving product: " + e.getMessage());
        }
        return null;
    }

    // T0DO cleanup for step 3
    private List<CartItem> cart = new ArrayList<>();

    public void addToCart(CartItem item) {
        cart.add(item);
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cart);
    }

    public void saveOrder(Order order) {
        String sql = "INSERT INTO orders (client_id, product_id, quantity) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (CartItem item : order.getItems()) {
                pstmt.setInt(1, 1); // Assuming client_id is 1 for simplicity
                pstmt.setInt(2, 1); // Assuming product_id is 1 for simplicity
                pstmt.setInt(3, item.getQuantity());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Error saving order: " + e.getMessage());
        }
    }

    public void clearCart() {
        cart.clear();
    }

    public void dropAllTables() {
        File dbFile = new File("src/main/resources/dbs/shopping.db");

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

    public Product getProductByNameAndUser(String name, Integer userId) {
        // TODO
        return getProductByName(name);
    }
}