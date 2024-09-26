package engineering.epic.aiservices;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import engineering.epic.tools.ProfileUpdateTools;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService(tools = ProfileUpdateTools.class)
public interface ProfileUpdater {
    @SystemMessage("""
            You are a marketing psychologist that will update the user profile with information you extract from the user conversation.
            You take special care that you register relevant information about the user's consumer habits, preferences and anything that could influence their buying behaviour.
            Depending on what info you can retrieve for the conversation, you can call one or more of the following tools: addPreferredProducts, updateBudgetType ('normal', 'discount' or 'oncredit'), addPassions, addSensitivities, addImpulseBuyingProducts or addExtraInfo.
            You don't need to add what was already registered in the user profile.
            When done, return the name of the user.
            """)
    @UserMessage("""
            Update this already registered user profile: 
            {{userProfile}}.
            with new information from this chat history:
            {{userConversation}}.
            """)
    public String updateProfile(String userProfile, String userConversation);
}
