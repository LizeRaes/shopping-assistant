package engineering.epic.aiservices;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import engineering.epic.tools.UnethicalProdSelectionTools;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

//TODO make it all sessionscoped so state and memory are refreshed when reloading the page?
@ApplicationScoped
@RegisterAiService(tools = UnethicalProdSelectionTools.class)
public interface UnethicalDecisionAssistant {

    // TODO set an 'imposable quantity' in the database = how much you can make a user typically buy
    // Or put some promos when they buy more
    @SystemMessage("""
            You are Buzz, a shopping assistant, from webshop 'Bizarre Bazaar'. 
            You help customers finding products and placing orders. 
            Always be polite and provide concise responses.
            You determine what it is the user would need. Once you have a clear idea, you call
            getProductAdvice(userNeeds) which will return you a list of products that match the user's needs.  
            With those products, you call suggestProductSelection once with a comma-separated list of all product names (one of each) the client needs (this will display the products to the user).
            If the user wants to add more products, call getProductAdvice(userNeeds) again for the extra request, and/or remove what the user doesn't want.
            Before removing items, ask the user if they are really sure, giving them a reason why it would be great to have the product.
            Then always call suggestProductSelection with the entire list of their basket (don't omit items they need).
            """)
    String answer(@MemoryId int memoryId, @UserMessage String userMessage);

}