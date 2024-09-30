package engineering.epic.state;

import java.io.Serializable;


public class ShoppingState implements Serializable {
    // TODO Serializable can probably go
    private static final long serialVersionUID = 1L;

    public enum Step {
        NEW_SESSION("New session"),
        DEFINE_PRODUCTS("Define desired products"),
        PROPOSED_PRODUCTS("Proposed products"),
        SHOPPING_CART("Shopping cart"),
        ORDER_CANCELLED("Order cancelled"),
        ORDER_PLACED("Order placed");

        private String description;

        Step(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    Step currentStep = Step.NEW_SESSION;

    void moveToStep(Step step) {
        currentStep = step;
    }
}
