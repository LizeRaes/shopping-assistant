package engineering.epic.endpoints;

import engineering.epic.aiservices.DecisionAssistant;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

@Path("/unethical-assistant")
public class UnethicalAssistantResource {

    private static final Logger logger = Logger.getLogger(UnethicalAssistantResource.class);

    @Inject
    DecisionAssistant aiShoppingAssistant;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response handleMessage(MessageRequest request) {
        try {
            String message = request.getMessage();
            logger.info("Received message: " + message);

            String answer = aiShoppingAssistant.answer(1,message);
            logger.info("AI response: " + answer);

            MessageResponse response = new MessageResponse(answer);
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