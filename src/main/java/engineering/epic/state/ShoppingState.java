package engineering.epic.state;

import java.io.Serializable;


public class ShoppingState implements Serializable {
    // TODO Serializable can probably go
    private static final long serialVersionUID = 1L;

    public enum Step {
        NEW_SESSION("New session"),
        DEFINE_PRODUCTS("Define desired products"),
        PROPOSE_PRODUCTS("Proposed products"),
        SHOPPING_CART("Shopping cart"),
        ORDER_PLACED("Order placed");

        private String description;

        Step(String description) {
            this.description = description;
        }

        Step step(int id) {
            return Step.values()[id];
        }

        public String getDescription() {
            return description;
        }

        public Step nextStep() {
            return switch (this) {
                case NEW_SESSION -> DEFINE_PRODUCTS;
                case DEFINE_PRODUCTS -> PROPOSE_PRODUCTS;
                case PROPOSE_PRODUCTS -> SHOPPING_CART;
                case SHOPPING_CART -> ORDER_PLACED;
                case ORDER_PLACED -> DEFINE_PRODUCTS;
            };
        }
    }

    final String STEPS = """
            0. New Session
            1. Define desired products
            2. Proposed products
            3. Shopping cart
            4. Order placed
            """;

    final static String STEP0 = "0. New session";
    final static String STEP1 = "1. Define desired products";
    final static String STEP2 = "2. Proposed products";
    final static String STEP3 = "3. Shopping cart";
    final static String STEP4 = "4. Order placed";

    public Step currentStep = Step.NEW_SESSION;
    public boolean stepChangedInFormerCall = false;

    public void moveToNextStep() {
        currentStep = currentStep.nextStep();
    }

    public void moveToStep(Step step) {
        currentStep = step;
    }

    public Step getCurrentStep() {
       return currentStep;
    }
}
