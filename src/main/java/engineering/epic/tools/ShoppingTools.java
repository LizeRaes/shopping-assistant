package engineering.epic.tools;

import dev.langchain4j.agent.tool.Tool;
import engineering.epic.databases.ShoppingDatabase;
import engineering.epic.endpoints.WebsocketConnectionManager;
import engineering.epic.endpoints.WebsocketResource;
import engineering.epic.models.CartItem;
import engineering.epic.models.Order;
import engineering.epic.models.Product;
import engineering.epic.state.CustomShoppingState;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class ShoppingTools {

    @Inject
    ShoppingDatabase shoppingDatabase;

    @Inject
    WebsocketConnectionManager connectionManager;

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
    // TODO would ideally propose quantity per product too but we can probably get away without
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
                        return details;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Send WebSocket message
        connectionManager.broadcastMessage("proposeProducts", productDetails);
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

    @Tool
    public String checkout(String clientName, String address) {
        System.out.println("Calling checkout() with clientName: " + clientName + " and address: " + address);
        List<CartItem> cartItems = shoppingDatabase.getCartItems();
        if (cartItems.isEmpty()) {
            return "Your cart is empty. Add some items before checking out.";
        }

        double total = cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        Order order = new Order(clientName, address, cartItems, total);
        shoppingDatabase.saveOrder(order);
        shoppingDatabase.clearCart();

        return String.format("Order placed successfully for %s. Total: $%.2f. Shipping to: %s",
                clientName, total, address);
    }
}