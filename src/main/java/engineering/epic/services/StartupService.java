package engineering.epic.services;

import engineering.epic.databases.ShoppingDatabase;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class StartupService {

    @Inject
    ShoppingDatabase shoppingDatabase;

    public void onStart(@Observes StartupEvent ev) {
        try {
            shoppingDatabase.dropAllTables();
        } catch (Exception e) {
            System.out.println("Failed to drop tables: " + e.getMessage());
        }

        try {
            // Initialize the SQLite database if it wasn't already done
            shoppingDatabase.initializeShoppingDatabase();
            System.out.println("Database tables created successfully.");
        } catch (Exception e) {
            System.out.println("Failed to create tables: " + e.getMessage());
        }
    }
}
