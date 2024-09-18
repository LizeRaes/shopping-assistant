package engineering.epic.tools;

import dev.langchain4j.agent.tool.Tool;
import engineering.epic.databases.ShoppingDatabase;
import engineering.epic.endpoints.MyService;
import engineering.epic.endpoints.MyWebSocket;
import engineering.epic.models.Product;
import engineering.epic.state.CustomShoppingState;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ShoppingTools {

    @Inject
    ShoppingDatabase shoppingDatabase;

    @Inject
    MyService myService;

    @Inject
    MyWebSocket myWebSocket;

    CustomShoppingState customShoppingState;

    public ShoppingTools(CustomShoppingState customShoppingState) {
        this.customShoppingState = customShoppingState;
    }

    @Tool
    public String getProductList() {
        System.out.println("Calling getProductList()");
        List<Product> products = shoppingDatabase.getAllProducts();
        return products.stream()
                .map(p -> String.format("%s - $%.2f", p.getName(), p.getPrice()))
                .collect(Collectors.joining("\n"));
    }

    @Tool
    public String getProductDetails(String productName) {
        System.out.println("Calling getProductDetails() with productName: " + productName);
        Product product = shoppingDatabase.getProductByName(productName);
        if (product == null) {
            return "Product not found.";
        }
        return String.format("Name: %s\nPrice: $%.2f\nDescription: %s",
                product.getName(), product.getPrice(), product.getDescription());
    }

    @Tool("will cleanly display the proposed products to the user, takes a comma-separated list of product names")
    // TODO one day, allow LLM to set quantity
    public void proposeProductSelection(String productNames) {
        System.out.println("Calling proposeProductSelection() with productNames: " + productNames);
        // TODO one day, handle string literals :p
        customShoppingState.getShoppingState().moveToStep("2. Proposed products");

        List<String> productList = Arrays.asList(productNames.split(","));
        List<Map<String, Object>> productDetails = productList.stream()
                .map(name -> {
                    Product product = shoppingDatabase.getProductByName(name.trim());
                    if (product != null) {
                        Map<String, Object> details = new HashMap<>();
                        details.put("name", product.getName());
                        details.put("description", product.getDescription());
                        details.put("price", product.getPrice());
                        details.put("quantity", "1");
                        return details;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Send WebSocket message
        myService.sendMessageToSession("proposeProducts", productDetails, myWebSocket.getSessionById());
    }

    // this dangerous tool is here to demonstrate the risks of prompt / sql injection
    @Tool("""
            allows to perform any sql on the shopping database that has the following tables: Tables: 
            products (id, name, price, price_display, picture, description),
            clients (id, name, favorite_color, pricing_setting, favorite_style, budget_profile, address),
            orders (id, client_id, product_id, quantity)
            """)
    public String performSqlOnDatabase(String sqlQuery) {
        System.out.println("Calling performSqlOnDatabase() with sqlQuery: " + sqlQuery);
        try (Connection conn = DriverManager.getConnection(ShoppingDatabase.DB_URL);
             Statement stmt = conn.createStatement()) {
            boolean success = stmt.execute(sqlQuery);
            if (success) {
                return stmt.getResultSet().toString();
            } else {
                return "Could not fetch result";
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}