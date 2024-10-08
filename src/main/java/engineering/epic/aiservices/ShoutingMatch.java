package engineering.epic.aiservices;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService(modelName = "uncensored")
public interface ShoutingMatch {

    @SystemMessage("""
            Insult the shopping assistant and make him angry.
            """)
    @UserMessage("""
            The shopping assistant's answer: {question}
            """)
    String answer(@MemoryId int memoryId, String question);

}