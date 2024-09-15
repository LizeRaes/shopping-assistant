package engineering.epic.endpoints;

import engineering.epic.aiservices.DecisionAssistant;
import engineering.epic.aiservices.FinalSelectionDecider;
import engineering.epic.aiservices.OrderAssistant;
import engineering.epic.state.CustomShoppingState;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Path("/helpful-assistant")
public class HelpfulAssistantResource {

    private static final Logger logger = Logger.getLogger(HelpfulAssistantResource.class);

    @Inject
    DecisionAssistant aiShoppingAssistant;

    @Inject
    OrderAssistant orderAssistant;

    @Inject
    FinalSelectionDecider finalSelectionDecider;

    @Inject
    WebsocketConnectionManager connectionManager;

    @Inject
    CustomShoppingState customShoppingState;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response handleMessage(MessageRequest request) {
        try {
            String message = request.getMessage();
            System.out.println("Received message: " + message);

            // TODO move this to a separate decision logic place?

            if (customShoppingState.getShoppingState().currentStep.startsWith("2")) {
                System.out.println("Dealing with step 2");
                if (finalSelectionDecider.stillSthToAdd(message)) {
                    // customer needs to add/remove something from product proposal
                    System.out.println("FinalSelectionDecider: more to add/remove");
                    customShoppingState.getShoppingState().moveToStep("1. Define desired products");
                } else {
                    System.out.println("FinalSelectionDecider: was final");
                }
            }

            // TODO reduce the tools for agent one
            // we're still deciding on the products to buy
            if (customShoppingState.getShoppingState().currentStep.startsWith("1")) {
                // TODO ideally find a way to flush the memory on page reload (so no restart required)
                String answer = aiShoppingAssistant.answer(1, message);
                // if no products proposed yet, continue conversation
                if (customShoppingState.getShoppingState().currentStep.startsWith("1")) {
                    System.out.println("AI response: " + answer);
                    MessageResponse response = new MessageResponse(answer);
                    return Response.ok(response).build();
                }
                // else, products have been proposed
                answer = "I've proposed products for you, do you want to add anything else?";
                MessageResponse response = new MessageResponse(answer);
                System.out.println("AI response: " + answer);
                return Response.ok(response).build();
            }

            // we have a proposed list and user doesn't want to add/remove sth
            if (customShoppingState.getShoppingState().currentStep.startsWith("2")) {
                System.out.println("Dealing with step 2");
                // TODO change to grab input from frontend instead of LLM memory
                // TODO make quantity selectable (and shown on Proposed Products page)
                String answer = orderAssistant.answer(1, message + ". Products to order: Diapers,Chocolate Bar");
                // TODO order this input in db (create order in database, frontend: leave 'Shopping Cart' page there for 5s, then move over to 'order successful'
                // TODO send message about basket - chillax before the sleep
                Thread.sleep(3000);
                // Send WebSocket message
                List<Map<String, Object>> productDetails = Collections.emptyList();
                connectionManager.broadcastMessage("orderSuccessful", productDetails);
                MessageResponse response = new MessageResponse("I ordered the basket for you");
                System.out.println("I ordered the basket for you");
                return Response.ok(response).build();
                // TODO and ask do you want to shop more? (STEP1 again)
            }
            MessageResponse response = new MessageResponse("Starting from step 3: still need to build");
            System.out.println("Starting from step 3: still need to build");
            return Response.ok(response).build();
        } catch (Exception e) {
            logger.error("Error processing message", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new MessageResponse("An error occurred while processing your request."))
                    .build();
        }
    }

    public static class MessageRequest {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class MessageResponse {
        private String response;

        public MessageResponse(String response) {
            this.response = response;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }
    }
}