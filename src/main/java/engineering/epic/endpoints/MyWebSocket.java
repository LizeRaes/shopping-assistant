package engineering.epic.endpoints;

import engineering.epic.state.CustomShoppingState;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/websocket")
@ApplicationScoped
public class MyWebSocket {
    @Inject
    MyService myService;

    @Inject
    CustomShoppingState customShoppingState;

    // A thread-safe map to store sessions, keyed by session ID
    private Map<String, Session> sessions = new ConcurrentHashMap<>();
    private Integer userId = 0;

    @OnOpen
    public void onOpen(Session session) {
        // reset all on reload
        sessions.put("0", session);
        refreshUser();
        customShoppingState.getShoppingState().moveToStep("1. Define desired products");
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        System.out.println("Message received from session: " + session.getId() + " - " + message);
        // Forward the message to the connection manager for further processing
        myService.handleIncomingMessage(message, session);
    }

    public Session getSessionById() {
        return sessions.get("0");
    }

    public void removeSessionById(String sessionId) {
        sessions.remove(sessionId);
    }

    public Integer getUserId() {
        return userId;
    }

    public void refreshUser() {
        userId = (int) (Math.random() * 1000) + 1;
    }
}

