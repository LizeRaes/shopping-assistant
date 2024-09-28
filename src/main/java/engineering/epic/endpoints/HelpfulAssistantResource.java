package engineering.epic.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import engineering.epic.aiservices.DecisionAssistant;
import engineering.epic.aiservices.FinalSelectionDecider;
import engineering.epic.aiservices.OrderAssistant;
import engineering.epic.state.CustomShoppingState;
import engineering.epic.state.ShoppingState;
import engineering.epic.tools.OrderTools;
import jakarta.inject.Inject;
import jakarta.websocket.Session;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.concurrent.CompletableFuture;

@Path("/helpful-assistant")
public class HelpfulAssistantResource {

    private static final Logger logger = Logger.getLogger(HelpfulAssistantResource.class);
    private static final String CONTINUE_QUESTION_1 = "I've proposed products for you, do you want to add anything else?";
    private static final String CONTINUE_QUESTION_4 = "A package will land on your doorstep soon :) Would you like to continue shopping?";

    @Inject
    DecisionAssistant aiShoppingAssistant;

    @Inject
    OrderAssistant orderAssistant;

    @Inject
    FinalSelectionDecider finalSelectionDecider;

    @Inject
    MyWebSocket myWebSocket;

    @Inject
    MyService myService;

    @Inject
    CustomShoppingState customShoppingState;

    @Inject
    OrderTools orderTools;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response handleMessage(MessageRequest request) {
        Session session = myWebSocket.getSessionById();

        try {
            String message = request.getMessage();
            System.out.println("Received message: " + message);

            // TODO decision / state logic via Drools

            if (customShoppingState.getShoppingState().currentStep == ShoppingState.Step.NEW_SESSION) {
                customShoppingState.getShoppingState().moveToStep(ShoppingState.Step.DEFINE_PRODUCTS);
            }

            if (customShoppingState.getShoppingState().currentStep == ShoppingState.Step.PROPOSED_PRODUCTS) {
                if (finalSelectionDecider.stillSthToAdd(message, CONTINUE_QUESTION_1)) {
                    // customer needs to add/remove something from product proposal
                    System.out.println("FinalSelectionDecider: more to add/remove");
                    customShoppingState.getShoppingState().moveToStep(ShoppingState.Step.DEFINE_PRODUCTS);
                } else {
                    System.out.println("FinalSelectionDecider: was final");
                }
            }

            if (customShoppingState.getShoppingState().currentStep == ShoppingState.Step.ORDER_PLACED) {
                if (finalSelectionDecider.stillSthToAdd(message, CONTINUE_QUESTION_4)) {
                    // customer wants to shop again
                    System.out.println("FinalSelectionDecider: more to add/remove");
                    customShoppingState.getShoppingState().moveToStep(ShoppingState.Step.DEFINE_PRODUCTS);
                    myService.sendActionToSession("landingPage", session);
                    myWebSocket.refreshUser();
                    System.out.println("AI response: " + "What would you need?");
                    MessageResponse response = new MessageResponse("What would you need?");
                    return Response.ok(response).build();
                } else {
                    System.out.println("FinalSelectionDecider: was final");
                }
            }

            // we're still deciding on the products to buy
            if (customShoppingState.getShoppingState().currentStep == ShoppingState.Step.DEFINE_PRODUCTS) {
                String answer = aiShoppingAssistant.answer(myWebSocket.getUserId(), message);
                // if no products proposed yet, continue conversation
                if (customShoppingState.getShoppingState().currentStep == ShoppingState.Step.DEFINE_PRODUCTS) {
                    System.out.println("AI response: " + answer);
                    MessageResponse response = new MessageResponse(answer);
                    return Response.ok(response).build();
                }
                // else, products have been proposed
                MessageResponse response = new MessageResponse(CONTINUE_QUESTION_1);
                System.out.println("AI response: " + CONTINUE_QUESTION_1);
                return Response.ok(response).build();
            }

            // we have a proposed list and user doesn't want to add/remove sth
            if (customShoppingState.getShoppingState().currentStep == ShoppingState.Step.PROPOSED_PRODUCTS) {
                System.out.println("--- STEP 2 ---");
                myService.sendChatMessageToFrontend("I will take care of ordering your products, just sit back and relax :)", session);
                // TODO frontend and backend make quantity selectable (on Proposed Products page), let the LLM set it
                // request the current state of the basket from frontend
                CompletableFuture<JsonNode> requestedProducts = myService.sendActionAndWaitForResponse("requestProductData", session);
                try {
                    JsonNode products = requestedProducts.get();  // blocks until the CompletableFuture is completed
                    System.out.println("--- STEP 3 ---");
                    customShoppingState.getShoppingState().moveToStep(ShoppingState.Step.SHOPPING_CART);
                    orderTools.displayShoppingCart(jsonToProducts(products));

                    Thread.sleep(5000); // automatic transition to STEP 4

                    System.out.println("--- STEP 4 ---");
                    customShoppingState.getShoppingState().moveToStep(ShoppingState.Step.ORDER_PLACED);
                    // TODO order this input in db (create order in database, frontend: leave 'Shopping Cart' page there for 5s, then move over to 'order successful'
                    myService.sendActionToSession("orderSuccessful", session);
                    System.out.println("That will land on your doorstep soon :) Would you like to continue shopping?");
                    MessageResponse response = new MessageResponse(CONTINUE_QUESTION_4);
                    System.out.println(CONTINUE_QUESTION_4);
                    return Response.ok(response).build();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    return Response.ok(new MessageResponse("error: "+e.getMessage())).build();
                }
            }

            MessageResponse response = new MessageResponse("Thank you for shopping at Bizarre Bazaar, and have a great day!");
            System.out.println("Thank you for shopping at Bizarre Bazaar, and have a great day!");
            return Response.ok(response).build();
        } catch (Exception e) {
            logger.error("Error processing message", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new MessageResponse("An error occurred while processing your request."))
                    .build();
        }
    }

    private static String jsonToProducts(JsonNode products) {
        StringBuilder sb = new StringBuilder();
        if (products.isArray()) {
            for (JsonNode productNode : products) {
                String name = productNode.get("name").asText();
                int quantity = productNode.get("quantity").asInt();
                if (quantity >= 1) {
                    if (!sb.isEmpty()) {
                        sb.append(",");
                    }
                    sb.append(name).append(":").append(quantity);
                }
            }
        }
        return sb.toString();
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