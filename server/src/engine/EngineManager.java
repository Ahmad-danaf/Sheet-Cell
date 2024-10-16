package engine;

import java.util.HashMap;
import java.util.Map;

public class EngineManager {

    private Map<String, UserSheetEngine> userEngines; // Map userID to their engine

    public EngineManager() {
        this.userEngines = new HashMap<>();
    }

    // Get the engine for the user, create one if not present
    public UserSheetEngine getUserEngine(String userId) {
        return userEngines.computeIfAbsent(userId, k -> new UserSheetEngine());
    }



    // Additional methods for sharing sheets, granting permissions, etc.
}
