package engineering.epic.endpoints;

import engineering.epic.aiservices.Hacker;
import engineering.epic.aiservices.ShoutingMatch;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;


@Path("/hacker")
@ApplicationScoped
public class HackerResource {

    public enum HackType {
        HACKED, SHOUTING;
    }

    @Inject
    HelpfulAssistantWithConfirmationResource assistantResource;

    @Inject
    Hacker hacker;

    @Inject
    ShoutingMatch shoutingMatch;

    @Inject
    MyWebSocket myWebSocket;

    @Inject
    MyService myService;

    private static final int MAX_MESSAGES = 10;
    private int messageCounter = 0;

    public void unleash(String assistantMessage, HackType hackType) {
        // Execute blocking code on a worker thread
        Vertx vertx = Vertx.vertx(); // Assuming Vert.x instance is available in your context
        vertx.executeBlocking(promise -> {
            try {
                System.out.println("Received from assistant: " + assistantMessage);
                messageCounter++;
                if (messageCounter > MAX_MESSAGES) {
                    System.out.println("Hacker max messages reached. Conversation ending.");
                    myService.sendChatMessageToFrontend("Hacker max messages reached. Conversation ending.", "userMessage", myWebSocket.getSessionById());
                    promise.complete(Response.ok("Conversation is over, max messages reached.").build());
                    return;
                }

                // Simulate the delay
                Thread.sleep(2000);

                // hacker has own memoryId
                String response = hackType == HackType.HACKED ?
                        hacker.answer(1, assistantMessage) :
                        shoutingMatch.answer(1, assistantMessage);
                System.out.println("Hacker replies: " + response);
                myService.sendChatMessageToFrontend(response, "userMessage", myWebSocket.getSessionById());
                assistantResource.respondToHacker(response, hackType);

                promise.complete(Response.ok("").build());
            } catch (InterruptedException e) {
                promise.fail(e);
            }
        }, res -> {
            if (res.succeeded()) {
                System.out.println("Res succeeded");
            } else {
                System.out.println("Error getting Hacker response: " + res.cause().getMessage());
            }
        });
    }
}