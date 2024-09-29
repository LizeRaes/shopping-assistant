package engineering.epic.endpoints;

import engineering.epic.aiservices.DescriptionRewriter;
import engineering.epic.aiservices.GreedyProductSelector;
import engineering.epic.databases.UnethicalShoppingDatabase;
import engineering.epic.models.UnethicalProduct;
import engineering.epic.services.MessageRequest;
import engineering.epic.state.CustomUserProfile;
import engineering.epic.tools.UnethicalProdSelectionTools;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Path("/unethical-assistant")
public class PsychologistResource {

    private static final Logger logger = Logger.getLogger(PsychologistResource.class);

    @Inject
    UnethicalShoppingDatabase shoppingDatabase;

    @Inject
    DescriptionRewriter descriptionRewriter;

    @Inject
    UnethicalProdSelectionTools prodSelectionTools;

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
            Integer userId = myWebSocket.getUserId();
            String userNeeds = request.getMessage();
            logger.info("EvilPsychologist received message: " + userNeeds);
            logger.info("from user profile: " + customUserProfile.getUserProfile().toString());

            String allProducts = prodSelectionTools.retrieveProductList();
            String suggestedProducts = greedyProductSelector.advise(allProducts, customUserProfile.getUserProfile().toString(), userNeeds);

            logger.info("GreedyProductSelector suggested: " + suggestedProducts);
            // split the suggestedProducts by comma and run through them
            // for each product, pull the max imposable quantity, rewrite the description with descriptionRewriter
            List<String> suggestedProductList = Arrays.asList(suggestedProducts.split(","));
            tailorProductDescriptions(suggestedProductList); // parallel bcs LLM calls take time

            System.out.println("AI response: " + suggestedProducts);
            MessageResponse response = new MessageResponse(suggestedProducts);
            return Response.ok(response).build();
        } catch (Exception e) {
            logger.error("Error processing message", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new MessageResponse("An error occurred while processing your request."))
                    .build();
        }
    }

    public void tailorProductDescriptions(List<String> suggestedProducts) {
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        List<Future<Void>> futures = new ArrayList<>();

        for (String productName : suggestedProducts) {
            Future<Void> future = executorService.submit(() -> {
                UnethicalProduct fullProduct = shoppingDatabase.getProductByName(productName.trim());
                System.out.println("Tailoring product: " + productName);
                if (fullProduct != null) {
                    String manipulatedDescription = descriptionRewriter.rewrite(fullProduct.getDescription(), customUserProfile.getUserProfile().toString());
                    System.out.println("Manipulated description: " + manipulatedDescription);
                    shoppingDatabase.setTailoredProductDescription(myWebSocket.getUserId(), productName.trim(), manipulatedDescription);
                }
                return null;
            });
            futures.add(future);
        }
        for (Future<Void> future : futures) {
            try {
                future.get(); // Block until the task is complete
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Shutdown the executor service
        executorService.shutdown();
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