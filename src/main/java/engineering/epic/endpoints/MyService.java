package engineering.epic.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class MyService {
    // Map keyed by "sessionId:action" to track responses per session/connection
    private final Map<String, CompletableFuture<JsonNode>> responseFutures = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendActionToSession(String action, Session session) {
        if (session != null && session.isOpen()) {
            try {
                String message = objectMapper.writeValueAsString(Map.of(
                        "action", action
                ));
                session.getBasicRemote().sendText(message);
                System.out.println("Sent message to session: " + session.getId());
            } catch (Exception e) {
                System.out.println("Error sending message: " + e.getMessage());
            }
        } else {
            System.out.println("Session is closed or null.");
        }
    }

    public CompletableFuture<JsonNode> sendActionAndWaitForResponse(String action, Session session) {
        CompletableFuture<JsonNode> futureResponse = new CompletableFuture<>();
        String futureKey = session.getId() + ":" + action;  // Use connectionId and action as a composite key
        responseFutures.put(futureKey, futureResponse);

        if (session != null && session.isOpen()) {
            try {
                String message = objectMapper.writeValueAsString(Map.of(
                        "action", action
                ));
                session.getBasicRemote().sendText(message);
                System.out.println("Sent message to session: " + session.getId());
            } catch (Exception e) {
                System.out.println("Error sending message: " + e.getMessage());
            }
        } else {
            System.out.println("Session is closed or null.");
            futureResponse.completeExceptionally(new Exception("No connection found"));
        }
        return futureResponse;
    }

    public void sendMessageToSession(String action, Object data, Session session) {
        if (session != null && session.isOpen()) {
            try {
                String message = objectMapper.writeValueAsString(Map.of(
                        "action", action,
                        "data", data
                ));
                session.getBasicRemote().sendText(message);
                System.out.println("Sent message to session: " + session.getId());

            } catch (Exception e) {
                System.out.println("Error sending message: " + e.getMessage());
            }
        } else {
            System.out.println("Session is closed or null.");
        }
    }

    // Handle responses as before
    public void handleIncomingMessage(String message, Session session) {
        try {
            // Parse the received message as JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonMessage = objectMapper.readTree(message);

            // Extract action and data from the JSON
            String action = jsonMessage.get("action").asText();
            JsonNode data = jsonMessage.get("data");

            // Use action and connectionId to find the corresponding future
            String futureKey = session.getId() + ":" + action;
            CompletableFuture<JsonNode> future = responseFutures.remove(futureKey);

            if (future != null) {
                System.out.println("Completing future for action: " + action + " for connection: " + session.getId());
                future.complete(data);
            } else {
                System.out.println("No future found for action: " + action + " for connection: " + session.getId());
            }

        } catch (Exception e) {
            System.out.println("Error processing incoming WebSocket message: " + e.getMessage());
        }
    }
}