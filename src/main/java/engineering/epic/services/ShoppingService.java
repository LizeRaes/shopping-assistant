package engineering.epic.services;

import engineering.epic.tools.ShoppingTools;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ShoppingService {

    @Inject
    ShoppingTools shoppingTools;

    public String getProductList() {
        return shoppingTools.getProductList();
    }

    public String getProductDetails(String productName) {
        return shoppingTools.getProductDetails(productName);
    }

    public String addToCart(String productName, int quantity) {
        return shoppingTools.addToCart(productName, quantity);
    }

    public String checkout(String clientName, String address) {
        return shoppingTools.checkout(clientName, address);
    }
}