package engineering.epic.tools;

import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.agent.tool.Tool;
import engineering.epic.databases.ShoppingDatabase;
import engineering.epic.endpoints.MyService;
import engineering.epic.endpoints.MyWebSocket;
import engineering.epic.models.CartItem;
import engineering.epic.models.Order;
import engineering.epic.models.Product;
import engineering.epic.state.CustomShoppingState;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;
import java.util.stream.Collectors;

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

    @Tool("will display the shopping cart to the user, takes a comma-separated list of product names")
    public void displayShoppingCart(JsonNode products) {
        System.out.println("Calling displayShoppingCart() with products: " + products.toString());
        // TODO one day, handle string literals :p
        customShoppingState.getShoppingState().moveToStep("3. Shopping cart");
        List<Map<String, Object>> productDetails = new ArrayList<>();
        List<CartItem> cartItems = new ArrayList<>();
        if (products.isArray()) {
            for (JsonNode productNode : products) {
                String name = productNode.get("name").asText();
                int quantity = productNode.get("quantity").asInt();
                if (quantity < 1) {
                    continue;
                }

                Product product = shoppingDatabase.getProductByName(name.trim());
                if (product != null) {
                    Map<String, Object> details = new HashMap<>();
                    details.put("name", product.getName());
                    details.put("description", product.getDescription());
                    details.put("price", product.getPrice());
                    details.put("quantity", quantity);
                    productDetails.add(details);

                    CartItem cartItem = new CartItem(product, quantity);
                    cartItems.add(cartItem);
                }
            }
        }

        // Send WebSocket message with product details including quantity
        myService.sendMessageToSession("displayShoppingCart", productDetails, myWebSocket.getSessionById());

        // Save the order in the database
        Order order = new Order();
        order.setItems(cartItems);
        shoppingDatabase.saveOrder(myWebSocket.getUserId(), order);
    }

    @Tool
    public String addToCart(String productName, int quantity) {
        System.out.println("Calling addToCart() with productName: " + productName + " and quantity: " + quantity);
        Product product = shoppingDatabase.getProductByName(productName);
        if (product == null) {
            return "Product not found.";
        }
        CartItem cartItem = new CartItem(product, quantity);
        shoppingDatabase.addToCart(cartItem);
        return String.format("Added %d x %s to your cart.", quantity, productName);
    }

//    @Tool
//    public String checkout(String clientName, String address) {
//        System.out.println("Calling checkout() with clientName: " + clientName + " and address: " + address);
//        List<CartItem> cartItems = shoppingDatabase.getCartItems();
//        if (cartItems.isEmpty()) {
//            return "Your cart is empty. Add some items before checking out.";
//        }
//
//        double total = cartItems.stream()
//                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
//                .sum();
//
//        Order order = new Order(clientName, address, cartItems, total);
//        shoppingDatabase.saveOrder(order);
//        shoppingDatabase.clearCart();
//
//        return String.format("Order placed successfully for %s. Total: $%.2f. Shipping to: %s",
//                clientName, total, address);
//    }
}
