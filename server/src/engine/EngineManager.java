package engine;

import com.sheetcell.engine.Engine;
import com.sheetcell.engine.sheet.api.SheetReadActions;

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

    public SheetReadActions getSheetData(String userId, String sheetName) {
        UserSheetEngine userEngine = userEngines.get(userId);
        if (userEngine == null) {
            return null;
        }
        System.out.println("User engine is not null");
       return userEngine.getSheetData(sheetName);
    }

    public SheetReadActions getSheetDataVersion(String sheetName, int version) {
        for (UserSheetEngine userEngine : userEngines.values()) {
            SheetReadActions sheetReadActions = userEngine.getSheetDataVersion(sheetName,version);
            if (sheetReadActions != null && sheetReadActions.getVersion() == version) {
                return sheetReadActions;
            }
        }
        return null;
    }

    public Engine getSheetEngine(String userId, String sheetName) {
        UserSheetEngine userEngine = userEngines.get(userId);
        if (userEngine == null) {
            return null;
        }
       return userEngine.getSheetEngine(sheetName);
    }

    public Engine getSheetEngine(String sheetName) {
        for (UserSheetEngine userEngine : userEngines.values()) {
            Engine engine = userEngine.getSheetEngine(sheetName);
            if (engine != null) {
                return engine;
            }
        }
        return null;
    }



    // Additional methods for sharing sheets, granting permissions, etc.
}
