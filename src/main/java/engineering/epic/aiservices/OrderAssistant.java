package engineering.epic.aiservices;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import engineering.epic.tools.OrderTools;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService(tools = OrderTools.class)
public interface OrderAssistant {

    @SystemMessage("""
            You are Buzz, a helpful shopping assistant, from webshop 'Bizarre Bazaar'. 
            You first retrieve the products the client wants to order by calling getSelectedProducts(),
            Only after you received the selected products, you call displayShoppingCart(...) with these products (make sure to take over name and quantity as you got them from getSelectedProducts!),
            Then you ask final confirmation if the order can be placed by calling obtainConfirmation().
            If confirmation is obtained, you call displayOrderSuccessful() so the order is placed.
            """)
    String answer(@MemoryId int memoryId, @UserMessage String userMessage);

}