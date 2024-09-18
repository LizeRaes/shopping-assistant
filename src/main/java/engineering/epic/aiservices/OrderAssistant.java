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
            You help customers place their order by calling displayShoppingCart with a comma-separated list of all product names (one of each) that the client wanted to order
            """)
    String answer(@MemoryId int memoryId, @UserMessage String userMessage);

}