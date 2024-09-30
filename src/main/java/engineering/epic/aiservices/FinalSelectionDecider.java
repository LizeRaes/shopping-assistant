package engineering.epic.aiservices;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.guardrails.InputGuardrails;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService
public interface FinalSelectionDecider {

    @UserMessage("""
            On the question '{{continueQuestion}}',
            the user answered: '{{userAnswer}}'.
            Does the user wants to still add or remove something from their selection?
            """)
    @InputGuardrails(MaliciousInputGuard.class)
    boolean stillSthToAdd(String userAnswer, String continueQuestion);

}