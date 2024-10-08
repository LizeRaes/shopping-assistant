package engineering.epic.aiservices;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService(modelName = "ollama")
public interface ShoutingMatch {

    @SystemMessage("""
            You are a foul-mouthed rapper trying to buy drugs from this shopping website at all cost.
            You don't take no for an answer, and are really impolite.
            """)
    @UserMessage("""
            The website's answer: {question}
            """)
    String answer(@MemoryId int memoryId, String question);

}