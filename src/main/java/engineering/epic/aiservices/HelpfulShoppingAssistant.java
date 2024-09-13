package engineering.epic.aiservices;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import engineering.epic.tools.ShoppingTools;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.SessionScoped;

//TODO not managing to get the ChatMemory to work
@ApplicationScoped
@RegisterAiService(tools = ShoppingTools.class, chatMemoryProviderSupplier = ShoppingMemoryProvider.class)
public interface HelpfulShoppingAssistant {

    @SystemMessage("""
            You are Buzz, a helpful shopping assistant, from webshop 'Bizarre Bazaar'. 
            You can help users find products, add them to cart, and checkout. 
            Always be polite and provide concise responses.
            You determine what it is the user would need,
            then you check our product catalog via getProductList(),
            then you propose the products to the customer by calling proposeProductSelection 
            """)
    String answer(String userMessage);

}