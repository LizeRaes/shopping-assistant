package engineering.epic.state;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CustomShoppingState {
    private final ShoppingState shoppingState = new ShoppingState();

    public void moveToStep(ShoppingState.Step step) {
        shoppingState.moveToStep(step);
    }

    public ShoppingState.Step getCurrentStep() {
        return shoppingState.currentStep;
    }
}
