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
            System.out.println("User engine is null");
            //print all users and their sheets
            for (Map.Entry<String, UserSheetEngine> entry : userEngines.entrySet()) {
                System.out.println("User: "+entry.getKey());
                for (Map.Entry<String, Engine> entry2 : entry.getValue().sheetEngines.entrySet()) {
                    System.out.println("Sheet: "+entry2.getKey());
                }
            }
            return null;
        }
        System.out.println("User engine is not null");
       return userEngine.getSheetData(sheetName);
    }



    // Additional methods for sharing sheets, granting permissions, etc.
}
