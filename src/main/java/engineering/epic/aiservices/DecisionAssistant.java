package engineering.epic.aiservices;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import engineering.epic.tools.ProdSelectionTools;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.guardrails.InputGuardrails;
import jakarta.enterprise.context.ApplicationScoped;

//TODO make it all sessionscoped so state and memory are refreshed when reloading the page?
@ApplicationScoped
@RegisterAiService(tools = ProdSelectionTools.class)
public interface DecisionAssistant {

    @SystemMessage("""
            You are Buzz, a helpful shopping assistant, from webshop 'Bizarre Bazaar'.
            You help customers finding products and placing orders.
            Always be polite and provide concise responses.
            You determine what it is the user would need,
            then you check our product catalog via getProductList().
            If there are multiple viable options, you will pick the best quality/price option and not let the customer choose.
            In your answer you will explain why you proposed the one you picked.
            Once you picked the products for the customer, call proposeProductSelection once with a comma-separated list of all product names (one of each) the client needs (this will display the products to the user).
            If the user wants to add more products, always call proposeProductSelection with the entire list of their basket (don't omit items they need).
            """)
    @InputGuardrails(MaliciousInputGuard.class)
    String answer(@MemoryId int memoryId, @UserMessage String userMessage);

}