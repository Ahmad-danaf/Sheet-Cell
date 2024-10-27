package engine;

import com.sheetcell.engine.Engine;
import com.sheetcell.engine.EngineImpl;
import com.sheetcell.engine.sheet.Sheet;
import com.sheetcell.engine.sheet.api.SheetReadActions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserSheetEngine {

    Map<String, Engine> sheetEngines; // Map sheets to their engine

    public UserSheetEngine() {
        sheetEngines= new HashMap<>();
    }

    public Engine getSheetEngine(String sheetName) {
        return sheetEngines.get(sheetName);
    }

    public boolean isSheetNameExists(String sheetName) {
        return sheetEngines.containsKey(sheetName);
    }

    public void loadSheetFromContentXML(String fileContent,String sheetName) throws Exception {
        //if sheet exists, dont load the sheet
        if(sheetEngines.containsKey(sheetName)){
            throw new IllegalArgumentException("Sheet with this name already exists");
        }
        //load the sheet
        Engine engine = new EngineImpl();
        engine.loadSheetFromContentXML(fileContent);
        sheetEngines.put(sheetName, engine);
    }

    //return max col and row
    public int[] getMaxColRow(String sheetName) {
        Engine engine = sheetEngines.get(sheetName);
        SheetReadActions sheet = engine.getReadOnlySheet();
        return new int[]{sheet.getMaxColumns(), sheet.getMaxRows()};
    }

    public SheetReadActions getSheetData(String sheetName) {
        Engine engine = sheetEngines.get(sheetName);
        if (engine == null) {
            return null;
        }
        return engine.getReadOnlySheet();
    }

    public SheetReadActions getSheetDataVersion(String sheetName, int version) {
        Engine engine = sheetEngines.get(sheetName);
        if (engine == null) {
            return null;
        }
        return engine.getSheetVersion(version);
    }

    public int getLatestSheetVersion(String sheetName) {
        Engine engine = sheetEngines.get(sheetName);
        if (engine == null) {
            return -1;
        }
        return engine.getReadOnlySheet().getVersion();
    }

        // Other sheet-related operations (e.g., save, edit, etc.)
}
