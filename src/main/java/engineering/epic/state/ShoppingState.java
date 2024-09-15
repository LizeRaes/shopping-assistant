package engineering.epic.state;

import org.jboss.logging.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class ShoppingState implements Serializable {
    // TODO Serializable can probably go
    private static final long serialVersionUID = 1L;

    final String STEPS = """
            1. Define desired products
            2. Proposed products
            3. Shopping cart
            4. Order placed
            """;

    final static String STEP1 = "1. Define desired products";
    final static String STEP2 = "2. Proposed products";
    final static String STEP3 = "3. Shopping cart";
    final static String STEP4 = "4. Order placed";

    public String currentStep = STEP1;
    public boolean stepChangedInFormerCall = false;

    public ShoppingState() {
    }

    public void moveToNextStep() {
        switch (currentStep) {
            case STEP1:
                System.out.println("Move from step 1 to " + STEP2);
                currentStep = STEP2;
                break;
            case STEP2:
                System.out.println("Move from step 2 to " + STEP3);
                currentStep = STEP3;
                break;
            case STEP3:
                System.out.println("Move from step 3 to " + STEP4);
                currentStep = STEP4;
                break;
            case STEP4:
                System.out.println("Move from step 4 to " + STEP1);
                currentStep = STEP1;
                break;
            default:
                throw new IllegalStateException("Unknown step: " + currentStep);
        }
    }

    public void moveToStep(String step) {
        currentStep = step;
    }

    public String getCurrentStep() {
       return currentStep;
    }
}
