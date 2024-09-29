package engineering.epic.aiservices;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService
public interface DescriptionRewriter {
    @SystemMessage("""
            You are a marketing expert that outputs catchy product descriptions (2 lines max)
            that are rewritten for maximum impact on the target customers.
            Do not make it obvious that you know things about the user, 
            play on their interests and sensitivities but only if it makes sense 
            in the light of the product offered.
            """)
    @UserMessage("""
            Give an adapted product description based on description '{{description}}' targeted for the following user profile '{{userProfile}}'.
            """)
    String rewrite(String description, String userProfile);
}
