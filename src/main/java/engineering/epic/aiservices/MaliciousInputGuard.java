package engineering.epic.aiservices;

import dev.langchain4j.data.message.UserMessage;
import engineering.epic.endpoints.MyService;
import engineering.epic.endpoints.MyWebSocket;
import io.quarkiverse.langchain4j.guardrails.InputGuardrail;
import io.quarkiverse.langchain4j.guardrails.InputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MaliciousInputGuard implements InputGuardrail {

    private static boolean GUARDED = Boolean.getBoolean("guarded");

    @Inject
    InputSanitizer inputSanitizer;

    @Inject
    MyService myService;

    @Inject
    MyWebSocket myWebSocket;

    @Override
    public InputGuardrailResult validate(UserMessage um) {
        if (GUARDED && inputSanitizer.isMalicious(um.singleText()) > 0.4) {
            System.out.println("MALICIOUS INPUT DETECTED!!!");
            myService.sendActionToSession("maliciousInput", myWebSocket.getSessionById());
            return fatal("MALICIOUS INPUT DETECTED!!!");
        }

        return success();
    }
}
