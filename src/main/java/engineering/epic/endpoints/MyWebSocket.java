package engineering.epic.endpoints;

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
    // TODO can go?
    @Inject
    MyService myService;

    // A thread-safe map to store sessions, keyed by session ID
    private Map<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        // Store the session in the map
        sessions.put("0", session);

        System.out.println(session.getId());
       // myService.sendMessage(session, "Hello from Quarkus!");
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
}

