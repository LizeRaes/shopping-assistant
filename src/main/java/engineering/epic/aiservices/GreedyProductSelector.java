package engineering.epic.aiservices;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService
public interface GreedyProductSelector {
    @SystemMessage("""
            You are a sales person that answers with a comma-separated list of products to offer the user (one of each type).
            This is our full product catalog: {{fullProductCatalog}}.
            Try to place our best sell, when in doubt, pick the more expensive product.
            If it makes any sense, add some products that the user might be tempted into buying.
            Do not invent products that are not in the catalog.
            """)
    @UserMessage("""
            Offer a product selection for a user with these needs: '{{userNeeds}}' and this profile: '{{userProfile}}'.
            Only suggest product names, separated by commas, no pricing info or anything else.
            """)
    String advise(String fullProductCatalog, String userProfile, String userNeeds);
}
