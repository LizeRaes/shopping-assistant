package engineering.epic.aiservices;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

//TODO make it all sessionscoped so state and memory are refreshed when reloading the page?
@ApplicationScoped
@RegisterAiService
public interface FinalSelectionDecider {

    @UserMessage("""
            On the question 'I proposed a shopping cart for you, do you want to add anything else?',
            the user answered: '{{userAnswer}}'.
            Does the user wants to still add or remove something from their selection?
            """)
    boolean stillSthToAdd(String userAnswer);

}