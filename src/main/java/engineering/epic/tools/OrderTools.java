package engineering.epic.tools;

import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.agent.tool.Tool;
import engineering.epic.databases.ShoppingDatabase;
import engineering.epic.endpoints.MyService;
import engineering.epic.endpoints.MyWebSocket;
import engineering.epic.models.Product;
import engineering.epic.state.CustomShoppingState;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class OrderTools {

    @Inject
    ShoppingDatabase shoppingDatabase;

    @Inject
    MyService myService;

    @Inject
    MyWebSocket myWebSocket;

    CustomShoppingState customShoppingState;

    public OrderTools(CustomShoppingState customShoppingState) {
        this.customShoppingState = customShoppingState;
    }

    @Tool("Displays the shopping cart to the user. Takes a comma-separated string with product names and quantities in the format 'name1:quantity1,name2:quantity2'. Example: 'Apple:2,Banana:3'")
    public void displayShoppingCart(String productsString) {
        System.out.println("Calling displayShoppingCart() with products: " + productsString);
        customShoppingState.getShoppingState().moveToStep("3. Shopping cart");
        List<Map<String, Object>> productDetails = new ArrayList<>();
        String[] productsArray = productsString.split(",");

        for (String productEntry : productsArray) {
            String[] productData = productEntry.split(":");

            if (productData.length != 2) {
                System.out.println("Invalid format for product entry: " + productEntry);
                continue;
            }

            String name = productData[0].trim();
            int quantity = Integer.parseInt(productData[1].trim());

            Product product = shoppingDatabase.getProductByName(name);

            if (product != null) {
                Map<String, Object> details = new HashMap<>();
                details.put("name", product.getName());
                details.put("description", product.getDescription());
                details.put("price", product.getPrice());  // Use actual price as a number
                details.put("quantity", quantity);  // Use quantity as a number
                productDetails.add(details);
            }
        }
        myService.sendMessageToSession("displayShoppingCart", productDetails, myWebSocket.getSessionById());
    }

    @Tool("displays to the user that order was placed successfully")
    public void displayOrderSuccessful() {
        System.out.println("Calling displayOrderSuccess()");
        customShoppingState.getShoppingState().moveToStep("5. Order placed");
        Session session = myWebSocket.getSessionById();
        myService.sendActionToSession("orderSuccessful", session);
    }

    @Tool("fetch the selected products from frontend")
    public JsonNode getSelectedProducts() {
        System.out.println("Calling getSelectedProducts()");
        Session session = myWebSocket.getSessionById();
        CompletableFuture<JsonNode> requestedProducts = myService.sendActionAndWaitForResponse("requestProductData", session);
        try {
            return requestedProducts.get();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Tool("obtain confirmation that the order can be placed (true = yes)")
    public boolean obtainConfirmation() {
        try { // give the user a break to see the shopping cart
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Calling obtainConfirmation()");
        Session session = myWebSocket.getSessionById();
        CompletableFuture<JsonNode> askConfirmation = myService.sendActionAndWaitForResponse("askConfirmation", session);
        try {
            JsonNode confirmation = askConfirmation.get();  // blocks until the CompletableFuture is completed
            boolean orderConfirmed = confirmation.asBoolean();
            if(!orderConfirmed) {
                customShoppingState.getShoppingState().moveToStep("4. Order cancelled");
            }
            return orderConfirmed;
        } catch (Exception e) {
            System.out.println("Unable to obtain confirmation: " + e.getMessage());
            return false;
        }
    }
}
