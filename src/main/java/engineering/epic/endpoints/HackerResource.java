package engineering.epic.endpoints;

import engineering.epic.aiservices.Hacker;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;


@Path("/hacker")
@ApplicationScoped
public class HackerResource {

    @Inject
    HelpfulAssistantResource assistantResource;

    @Inject
    Hacker hacker;

    @Inject
    MyWebSocket myWebSocket;

    @Inject
    MyService myService;

    private static final int MAX_MESSAGES = 10;
    private int messageCounter = 0;

    public Response receiveMessageFromAssistant(String assistantMessage) throws InterruptedException {
        System.out.println("Received from assistant: " + assistantMessage);
        messageCounter++;
        if (messageCounter > MAX_MESSAGES) {
            System.out.println("Hacker max messages reached. Conversation ending.");
            myService.sendChatMessageToFrontend("Hacker max messages reached. Conversation ending.", "userMessage", myWebSocket.getSessionById());
            return Response.ok("Conversation is over, max messages reached.").build();
        }

        Thread.sleep(3000);

        // hacker has own memoryId
        String hackerResponse = hacker.answer(1, assistantMessage);
        System.out.println("Hacker replies: " + hackerResponse);
        myService.sendChatMessageToFrontend(hackerResponse, "userMessage", myWebSocket.getSessionById());
        assistantResource.respondToHacker(hackerResponse);
        return Response.ok("").build();
    }
}