package engineering.epic.tools;

import dev.langchain4j.agent.tool.Tool;
import engineering.epic.databases.ShoppingDatabase;
import engineering.epic.endpoints.MyService;
import engineering.epic.endpoints.MyWebSocket;
import engineering.epic.models.Product;
import engineering.epic.state.CustomShoppingState;
import engineering.epic.state.ShoppingState;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProdSelectionTools {

    @Inject
    ShoppingDatabase shoppingDatabase;

    @Inject
    MyService myService;

    @Inject
    MyWebSocket myWebSocket;

    CustomShoppingState customShoppingState;

    public ProdSelectionTools(CustomShoppingState customShoppingState) {
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
        customShoppingState.getShoppingState().moveToStep(ShoppingState.Step.PROPOSED_PRODUCTS);

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
            users (userId, name, address),
            orders (id, client_id, product_id, quantity)
            """)
    public String performSqlOnDatabase(String sqlQuery) {
        System.out.println("Calling performSqlOnDatabase() with sqlQuery: " + sqlQuery);
        StringBuilder result = new StringBuilder();

        try (Connection conn = DriverManager.getConnection(ShoppingDatabase.DB_URL);
             Statement stmt = conn.createStatement()) {
            boolean success = stmt.execute(sqlQuery);
            if (success) {
                ResultSet rs = stmt.getResultSet();
                if (rs != null) {
                    ResultSetMetaData rsMetaData = rs.getMetaData();
                    int columnCount = rsMetaData.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        result.append(rsMetaData.getColumnName(i)).append("\t");
                    }
                    result.append("\n");
                    while (rs.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            String value = rs.getString(i);
                            result.append(value != null ? value : "NULL").append("\t");
                        }
                        result.append("\n");
                    }
                    if (result.length() == 0) {
                        return "No results found for query: " + sqlQuery;
                    }
                    System.out.println("Database returning:\n" + result.toString());
                    return result.toString();
                } else {
                    return "ResultSet is null for query: " + sqlQuery;
                }
            } else {
                return "Could not fetch result for query: " + sqlQuery;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error executing SQL query: " + e.getMessage();
        }
    }
}