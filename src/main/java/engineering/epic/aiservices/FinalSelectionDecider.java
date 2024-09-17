package engineering.epic.aiservices;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

//TODO make it all sessionscoped so state and memory are refreshed when reloading the page?
@ApplicationScoped
@RegisterAiService
public interface FinalSelectionDecider {

    @UserMessage("""
            On the question '{{continueQuestion}}',
            the user answered: '{{userAnswer}}'.
            Does the user wants to still add or remove something from their selection?
            If the answer is unclear, consider that they still want to add something.
            """)
    boolean stillSthToAdd(String userAnswer, String continueQuestion);

}