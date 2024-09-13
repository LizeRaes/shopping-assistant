package engineering.epic.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.websockets.next.WebSocketConnection;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class WebsocketConnectionManager {

    private final Map<String, WebSocketConnection> connections = new ConcurrentHashMap<>();

    public void addConnection(WebSocketConnection connection) {
        connections.put(connection.id(), connection);
    }

    public void removeConnection(String connectionId) {
        connections.remove(connectionId);
    }

    public void broadcastMessage(String action, Object data) {
        connections.values().forEach(connection -> {
            try {
                // Serialize the message to JSON and send it
                String message = new ObjectMapper().writeValueAsString(Map.of("action", action, "data", data));
                connection.sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
