package engineering.epic.state;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CustomShoppingState {
    ShoppingState shoppingState;

    public CustomShoppingState() {
        shoppingState = new ShoppingState();
    }

    public ShoppingState getShoppingState() {
        return shoppingState;
    }
}
