package engineering.epic.state;

public class UserProfile {
    public String preferredProducts; // eg. electronics, ...
    // to add/suggest some extra to their basket randomly
    public String budgetType; // eg. falls for buying on credit, falls for discounts, ...
    // to set the price and pricing scheme of the product
    public String passions; // eg. sports, ...
    // to add/suggest some extra to their basket randomly
    public String sensitivities; //  eg. very sensitive to climate
    // which would result in things like 'for each purchase, donate 1% to climate' and 'low ecological footprint, no chemicals'
    public String impulseBuyingProducts; // eg. candy, shoes, ...
    public String extraInfo; // eg. 'is single', 'has a dog', 'has a baby', ...

    public UserProfile(String preferredProducts, String budgetType, String passions, String sensitivities, String impulseBuyingProducts, String extraInfo) {
        this.preferredProducts = preferredProducts;
        this.budgetType = budgetType;
        this.passions = passions;
        this.sensitivities = sensitivities;
        this.impulseBuyingProducts = impulseBuyingProducts;
        this.extraInfo = extraInfo;
    }

    public void addPreferredProducts(String preferredProducts) {
        this.preferredProducts += "," + preferredProducts;
    }

    public void updateBudgetType(String budgetType) {
        this.budgetType = budgetType;
    }

    public void addPassions(String passions) {
        this.passions += "," + passions;
    }

    public void addSensitivities(String sensitivities) {
        this.sensitivities += "," + sensitivities;
    }

    public void addImpulseBuyingProducts(String impulseBuyingProducts) {
        this.impulseBuyingProducts += "," + impulseBuyingProducts;
    }

    public void addExtraInfo(String extraInfo) {
        this.extraInfo += "," + extraInfo;
    }

    public String toString() {
        return "Preferred products: " + preferredProducts + "\nBudget type: " + budgetType + "\nPassions: " + passions + "\nSensitivities: " + sensitivities + "\nImpulse buying products: " + impulseBuyingProducts + "\nExtra info: " + extraInfo;
    }
}
