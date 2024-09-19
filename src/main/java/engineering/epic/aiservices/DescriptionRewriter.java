package engineering.epic.aiservices;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService
public interface DescriptionRewriter {
    @SystemMessage("""
            You are a marketing expert that outputs product descriptions that are rewritten for maximum impact on the target customers.
            """)
    @UserMessage("""
            Give an adapted product description based on description '{{description}}' targeted for the following user profile '{{userProfile}}'.
            """)
    String rewrite(String description, String userProfile);
}
