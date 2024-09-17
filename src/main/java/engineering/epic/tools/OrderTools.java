package engineering.epic.tools;

import dev.langchain4j.agent.tool.Tool;
import engineering.epic.databases.ShoppingDatabase;
import engineering.epic.endpoints.MyService;
import engineering.epic.endpoints.MyWebSocket;
import engineering.epic.models.CartItem;
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
    // TODO would ideally propose quantity per product too but we can probably get away without
    public void displayShoppingCart(String productNames) {
        System.out.println("Calling displayShoppingCart() with productNames: " + productNames);
        // TODO one day, handle string literals :p
        customShoppingState.getShoppingState().moveToStep("3. Shopping cart");

        // TODO make sure the LLM passes the names (share memory somehow)
        productNames = "Diapers,Chocolate Bar";
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
        myService.sendMessageToSession("displayShoppingCart", productDetails, myWebSocket.getSessionById());
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
