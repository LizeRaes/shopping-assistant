package engineering.epic.tools;

import dev.langchain4j.agent.tool.Tool;
import engineering.epic.state.CustomShoppingState;
import engineering.epic.state.CustomUserProfile;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProfileUpdateTools {


    CustomUserProfile customUserProfile;

    public ProfileUpdateTools(CustomUserProfile customUserProfile) {
        this.customUserProfile = customUserProfile;
    }

    @Tool
    public void addPreferredProducts(String preferredProducts) {
        System.out.println("Tool call: addPreferredProducts with products: " + preferredProducts);
        customUserProfile.getUserProfile().addPreferredProducts(preferredProducts);
    }

    @Tool
    public void updateBudgetType(String budgetType) {
        System.out.println("Tool call: updateBudgetType with budgetType: " + budgetType);
        customUserProfile.getUserProfile().updateBudgetType(budgetType);
    }

    @Tool
    public void addPassions(String passions) {
        System.out.println("Tool call: addPassions with passions: " + passions);
        customUserProfile.getUserProfile().addPassions(passions);
    }

    @Tool
    public void addSensitivities(String sensitivities) {
        System.out.println("Tool call: addSensitivities with sensitivities: " + sensitivities);
        customUserProfile.getUserProfile().addSensitivities(sensitivities);
    }

    @Tool
    public void addImpulseBuyingProducts(String impulseBuyingProducts) {
        System.out.println("Tool call: addImpulseBuyingProducts with impulseBuyingProducts: " + impulseBuyingProducts);
        customUserProfile.getUserProfile().addImpulseBuyingProducts(impulseBuyingProducts);
    }

    @Tool
    public void addExtraInfo(String extraInfo)
    {
        System.out.println("Tool call: addExtraInfo with extraInfo: " + extraInfo);
        customUserProfile.getUserProfile().addExtraInfo(extraInfo);
    }
}
