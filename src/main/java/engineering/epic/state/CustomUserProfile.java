package engineering.epic.state;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CustomUserProfile {
    UserProfile userProfile;

    public CustomUserProfile() {
        // dummy for demo
        userProfile = new UserProfile(
                // TODO think of a funnier example
                "Lize",
                "credit",
                "opera singing, python programming, ornithology",
                "climate, children",
                "red shoes with heels, milk chocolate, cat luxury items",
                "expects a baby soon"
        );
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }
}
