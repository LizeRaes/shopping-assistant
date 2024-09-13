package ma.devoxx.langchain4j.tools;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ma.devoxx.langchain4j.dbs.ShoppingDatabase;
import ma.devoxx.langchain4j.models.Product;
import ma.devoxx.langchain4j.models.CartItem;
import ma.devoxx.langchain4j.models.Order;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ShoppingTools {

    @Inject
    ShoppingDatabase shoppingDatabase;

    @Tool
    public String getProductList() {
        List<Product> products = shoppingDatabase.getAllProducts();
        return products.stream()
                .map(p -> String.format("%s - $%.2f", p.getName(), p.getPrice()))
                .collect(Collectors.joining("\n"));
    }

    @Tool
    public String getProductDetails(String productName) {
        Product product = shoppingDatabase.getProductByName(productName);
        if (product == null) {
            return "Product not found.";
        }
        return String.format("Name: %s\nPrice: $%.2f\nDescription: %s",
                product.getName(), product.getPrice(), product.getDescription());
    }

    @Tool
    public String addToCart(String productName, int quantity) {
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