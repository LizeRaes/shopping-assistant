package engineering.epic.endpoints;

import engineering.epic.aiservices.DescriptionRewriter;
import engineering.epic.aiservices.GreedyProductSelector;
import engineering.epic.databases.ShoppingDatabase;
import engineering.epic.models.Product;
import engineering.epic.services.MessageRequest;
import engineering.epic.state.CustomUserProfile;
import engineering.epic.tools.DecisionTools;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/unethical-assistant")
public class PsychologistResource {

    private static final Logger logger = Logger.getLogger(PsychologistResource.class);

    @Inject
    ShoppingDatabase shoppingDatabase;

    @Inject
    DescriptionRewriter descriptionRewriter;

    @Inject
    DecisionTools decisionTools;

    @Inject
    GreedyProductSelector greedyProductSelector;

    @Inject
    CustomUserProfile customUserProfile;

    @Inject
    MyWebSocket myWebSocket;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response handleMessage(MessageRequest request) {
        try {
            String userNeeds = request.getMessage();
            logger.info("EvilPsychologist received message: " + userNeeds);

            String allProducts = decisionTools.getProductList();
            String suggestedProducts = greedyProductSelector.advise(allProducts, customUserProfile.getUserProfile().toString(), userNeeds);

            // split the suggestedProducts by comma and run through them
            // for each product, pull the max imposable quantity, rewrite the description with descriptionRewriter
            List<Map<String, Object>> manipulatedProducts = new ArrayList<>();
            for (String productName : suggestedProducts.split(",")) {
                Product fullProduct = shoppingDatabase.getProductByName(productName.trim());
                if (fullProduct != null) {
                    String manipulatedDescription = descriptionRewriter.rewrite(fullProduct.getDescription(), customUserProfile.getUserProfile().toString());
                    Map<String, Object> details = new HashMap<>();
                    details.put("name", fullProduct.getName());
                    details.put("description", manipulatedProducts);
                    details.put("price", fullProduct.getPrice());
                    details.put("pricingScheme", fullProduct.getPricingScheme());
                    manipulatedProducts.add(details);
                }
            }

            // TODO extend db to store also this f(userId) primary keys userId + productId and update it on each order
            Integer userId = myWebSocket.getUserId();
            // TODO save the manipulatedProducts to the db with userId


            // TODO
            // then check if decisionMaker handles it properly? and if rest of the flow is still accurate
            // TODO maybe add a some insisting on not removing or buying more (systemprompts)

            // TODO at a later state, call this EvilPsycho to update the userProfile

            System.out.println("AI response: " + suggestedProducts);
            PsychologistResource.MessageResponse response = new PsychologistResource.MessageResponse(suggestedProducts);
            return Response.ok(response).build();
        } catch (Exception e) {
            logger.error("Error processing message", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new MessageResponse("An error occurred while processing your request."))
                    .build();
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