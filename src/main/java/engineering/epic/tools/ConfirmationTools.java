package engineering.epic.tools;

import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.agent.tool.Tool;
import engineering.epic.databases.ShoppingDatabase;
import engineering.epic.endpoints.MyService;
import engineering.epic.endpoints.MyWebSocket;
import engineering.epic.models.Product;
import engineering.epic.state.CustomShoppingState;
import engineering.epic.state.ShoppingState;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class ConfirmationTools {

    @Inject
    MyService myService;

    @Inject
    MyWebSocket myWebSocket;

    CustomShoppingState customShoppingState;

    public ConfirmationTools(CustomShoppingState customShoppingState) {
        this.customShoppingState = customShoppingState;
    }

    @Tool("obtain confirmation that the order can be placed (true = yes)")
    public boolean obtainConfirmation() {
        try { // give the user a break to see the shopping cart
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Calling obtainConfirmation()");
        Session session = myWebSocket.getSessionById();
        CompletableFuture<JsonNode> askConfirmation = myService.sendActionAndWaitForResponse("askConfirmation", session);
        try {
            JsonNode confirmation = askConfirmation.get();  // blocks until the CompletableFuture is completed
            boolean orderConfirmed = confirmation.asBoolean();
            if(!orderConfirmed) {
                customShoppingState.getShoppingState().moveToStep(ShoppingState.Step.ORDER_CANCELLED);
            }
            return orderConfirmed;
        } catch (Exception e) {
            System.out.println("Unable to obtain confirmation: " + e.getMessage());
            return false;
        }
    }
}
