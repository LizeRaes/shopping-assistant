package engineering.epic.aiservices;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import engineering.epic.tools.ShoppingTools;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(tools = ShoppingTools.class)
public interface HelpfulShoppingAssistant {

    @SystemMessage("""
            You are Buzz, a helpful shopping assistant, from webshop 'Bizarre Bazaar'. 
            You can help users find products, add them to cart, and checkout. 
            Always be polite and provide concise responses.
            """)
    String answer(@UserMessage String userMessage);

}