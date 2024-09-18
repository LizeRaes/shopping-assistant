package engineering.epic.state;

import org.jboss.logging.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class ShoppingState implements Serializable {
    // TODO Serializable can probably go
    private static final long serialVersionUID = 1L;

    final String STEPS = """
            0. New Session
            1. Define desired products
            2. Proposed products
            3. Shopping cart
            4. Order cancelled
            5. Order placed
            """;

    final static String STEP0 = "0. New session";
    final static String STEP1 = "1. Define desired products";
    final static String STEP2 = "2. Proposed products";
    final static String STEP3 = "3. Shopping cart";
    final static String STEP4 = "4. Order cancelled";
    final static String STEP5 = "5. Order placed";

    public String currentStep = STEP0;

    public ShoppingState() {
    }

    public void moveToStep(String step) {
        currentStep = step;
    }
}
