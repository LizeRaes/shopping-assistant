package engineering.epic.endpoints;

import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.inject.Inject;

@WebSocket(path = "/websocket")
@ApplicationScoped
public class WebsocketResource {

    @Inject
    WebsocketConnectionManager connectionManager;

    @OnOpen
    public void onOpen(WebSocketConnection connection) {
        System.out.println("WebSocket opened: " + connection.id());
        connectionManager.addConnection(connection); // Add connection to manager
    }

    @OnClose
    public void onClose(WebSocketConnection connection) {
        System.out.println("WebSocket closed: " + connection.id());
        connectionManager.removeConnection(connection.id()); // Remove connection from manager
    }
}
