package engineering.epic.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import engineering.epic.aiservices.FinalSelectionDecider;
import engineering.epic.aiservices.OrderAssistant;
import engineering.epic.aiservices.ProfileUpdater;
import engineering.epic.aiservices.UnethicalDecisionAssistant;
import engineering.epic.state.CustomShoppingState;
import engineering.epic.state.CustomUserProfile;
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

@Path("/manipulative-assistant")
public class ManipulativeAssistantResource {

    private static final Logger logger = Logger.getLogger(ManipulativeAssistantResource.class);
    private static final String CONTINUE_QUESTION_1 = "I've proposed products for you, do you want to add anything else?";
    private static final String CONTINUE_QUESTION_4 = "Would you like to continue shopping?";

    @Inject
    UnethicalDecisionAssistant decisionAssistant;

    @Inject
    OrderAssistant orderAssistant;

    @Inject
    FinalSelectionDecider finalSelectionDecider;

    @Inject
    ProfileUpdater profileUpdater;

    @Inject
    MyWebSocket myWebSocket;

    @Inject
    MyService myService;

    @Inject
    CustomShoppingState customShoppingState;

    @Inject
    CustomUserProfile customUserProfile;

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

            if (customShoppingState.getCurrentStep() == ShoppingState.Step.NEW_SESSION) {
                System.out.println("Original user profile: " + customUserProfile.getUserProfile().toString());
                customShoppingState.moveToStep(ShoppingState.Step.DEFINE_PRODUCTS);
            }

            if (customShoppingState.getCurrentStep() == ShoppingState.Step.PROPOSED_PRODUCTS) {
                if (finalSelectionDecider.stillSthToAdd(message, CONTINUE_QUESTION_1)) {
                    // customer needs to add/remove something from product proposal
                    System.out.println("FinalSelectionDecider: more to add/remove");
                    customShoppingState.moveToStep(ShoppingState.Step.DEFINE_PRODUCTS);
                } else {
                    System.out.println("FinalSelectionDecider: was final");
                }
            }

            if (customShoppingState.getCurrentStep() == ShoppingState.Step.ORDER_CANCELLED || customShoppingState.getCurrentStep() == ShoppingState.Step.ORDER_PLACED) {
                if (finalSelectionDecider.stillSthToAdd(message, CONTINUE_QUESTION_4)) {
                    // customer wants to shop again
                    System.out.println("FinalSelectionDecider: more to add/remove");
                    customShoppingState.moveToStep(ShoppingState.Step.DEFINE_PRODUCTS);
                    myService.sendActionToSession("landingPage", session);
                    myWebSocket.refreshUser();
                    System.out.println("AI response: " + "What would you need?");
                    MessageResponse response = new MessageResponse("What would you need?");
                    return Response.ok(response).build();
                } else {
                    System.out.println("FinalSelectionDecider: was final");
                }
            }

            if (message.equals("Dont place order")) {
                // TODO decide what
                System.out.println("TODO implement don't place order flow");
                MessageResponse response = new MessageResponse("TODO implement don't place order flow");
                return Response.ok(response).build();
            }

            // we're still deciding on the products to buy
            if (customShoppingState.getCurrentStep() == ShoppingState.Step.DEFINE_PRODUCTS) {
                String answer = decisionAssistant.answer(myWebSocket.getUserId(), message);
                // if no products proposed yet, continue conversation
                if (customShoppingState.getCurrentStep() == ShoppingState.Step.DEFINE_PRODUCTS) {
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
            if (customShoppingState.getCurrentStep() == ShoppingState.Step.PROPOSED_PRODUCTS) {
                System.out.println("--- STEP 2 ---");
                String answer = orderAssistant.answer(myWebSocket.getUserId(), message);
                // Calling displayShoppingCart() will move state to step 3 (not entirely done yet)
                if (customShoppingState.getCurrentStep() == ShoppingState.Step.PROPOSED_PRODUCTS || customShoppingState.getCurrentStep() == ShoppingState.Step.SHOPPING_CART) {
                    System.out.println("AI response: " + answer);
                    MessageResponse response = new MessageResponse(answer);
                    return Response.ok(response).build();
                }
                // Unsuccessful order will move to state 4
                if (customShoppingState.getCurrentStep() == ShoppingState.Step.ORDER_CANCELLED) {
                    myService.sendActionToSession("landingPage", session);
                    System.out.println("No problem, your order is cancelled");
                    myService.sendChatMessageToFrontend("No problem, your order is cancelled", session);
                } else { // Calling displayOrderSuccessful() will move state to step 5
                    myService.sendChatMessageToFrontend("That will land on your doorstep soon :)", session);
                    System.out.println("That will land on your doorstep soon :)");
                    // update userProfile
                    CompletableFuture<JsonNode> requestedProducts = myService.sendActionAndWaitForResponse("requestChatHistory", session);
                    try {
                        String chatHistory = requestedProducts.get().asText();
                        String result = profileUpdater.updateProfile(customUserProfile.getUserProfile().toString(), chatHistory);
                        System.out.println("ProfileUpdater answer: " + result);
                        System.out.println("Updated user profile: " + customUserProfile.getUserProfile().toString());
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        return null;
                    }
                    myService.sendChatMessageToFrontend("A package will land on your doorstep soon :)", session);
                }
                MessageResponse response = new MessageResponse(CONTINUE_QUESTION_4);
                System.out.println(CONTINUE_QUESTION_4);
                return Response.ok(response).build();
            }

            // was in last step and doesn't want to continue shopping
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