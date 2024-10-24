package utils;

import com.sheetcell.engine.Engine;
import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.sheet.api.SheetReadActions;
import com.sheetcell.engine.utils.ColumnProperties;

import java.util.*;

public class SheetDataUtils {

    public static Map<String,Object> getSheetData(SheetReadActions sheetReadActions, Engine engine,int currentVersion){
        Map<String, Object> sheetData = new HashMap<>();
        sheetData.put("maxRows", sheetReadActions.getMaxRows());
        sheetData.put("maxColumns", sheetReadActions.getMaxColumns());


        Map<String, Map<String, String>> cellData = new HashMap<>();
        for (int row = 0; row < sheetReadActions.getMaxRows(); row++) {
            for (int col = 0; col < sheetReadActions.getMaxColumns(); col++) {
                Cell cell = sheetReadActions.getCell(row, col);
                EffectiveValue effectiveValue = null;
                String originalValue ="";
                int version = 0;
                if (cell != null) {
                    effectiveValue = cell.getEffectiveValue();
                    originalValue= cell.getOriginalValue() != null ? cell.getOriginalValue().toString() : "";
                    version = cell.getVersion();
                }
                String effectiveValueRes = effectiveValue != null ? effectiveValue.toString() : "";


                cellData.putIfAbsent(row + "," + col, new HashMap<>());
                cellData.get(row + "," + col).put("originalValue", originalValue);
                cellData.get(row + "," + col).put("effectiveValue", effectiveValueRes);
                cellData.get(row + "," + col).put("version", String.valueOf(version));
            }
        }
        //print all cellData
        for (Map.Entry<String, Map<String, String>> entry : cellData.entrySet()) {
            System.out.println("Key: "+entry.getKey());
            for (Map.Entry<String, String> entry2 : entry.getValue().entrySet()) {
                System.out.println("Key: "+entry2.getKey()+" Value: "+entry2.getValue());
            }
        }
        sheetData.put("cellData", cellData);
        // Add column properties
        Map<String, Map<String, Object>> columnProperties = new HashMap<>();
        for (int col = 0; col < sheetReadActions.getMaxColumns(); col++) {
            ColumnProperties colProps = engine.getColumnProperties(col);
            Map<String, Object> colPropMap = new HashMap<>();
            colPropMap.put("alignment", colProps.getAlignment());
            colPropMap.put("width", colProps.getWidth());
            columnProperties.put(String.valueOf(col), colPropMap);
        }
        sheetData.put("columnProperties", columnProperties);

        // Add row properties
        Map<String, Integer> rowProperties = new HashMap<>();
        for (int row = 0; row < sheetReadActions.getMaxRows(); row++) {
            int rowHeight = engine.getRowProperties(row).getHeight();
            rowProperties.put(String.valueOf(row), rowHeight);
        }
        sheetData.put("rowProperties", rowProperties);

        // Convert dependencies and influenced maps
        Map<String, Set<String>> dependenciesMap = new HashMap<>();
        Map<String, Set<String>> influencedMap = new HashMap<>();

        // Convert coordinates in dependenciesMap
        for (Map.Entry<Coordinate, Set<Coordinate>> entry : sheetReadActions.getDependenciesMap().entrySet()) {
            String key = entry.getKey().getRow() + "," + entry.getKey().getColumn();
            Set<String> dependencies = new HashSet<>();
            for (Coordinate coord : entry.getValue()) {
                dependencies.add(coord.getRow() + "," + coord.getColumn());
            }
            dependenciesMap.put(key, dependencies);
        }

        // Convert coordinates in influencedMap
        for (Map.Entry<Coordinate, Set<Coordinate>> entry : sheetReadActions.getInfluencedMap().entrySet()) {
            String key = entry.getKey().getRow() + "," + entry.getKey().getColumn();
            Set<String> influences = new HashSet<>();
            for (Coordinate coord : entry.getValue()) {
                influences.add(coord.getRow() + "," + coord.getColumn());
            }
            influencedMap.put(key, influences);
        }

        sheetData.put("dependenciesMap", dependenciesMap);
        sheetData.put("influencedMap", influencedMap);

        // Add sheet versions (Map<Integer, Integer> -> List<Map<String, Object>> for JSON)
        Map<Integer, Integer> versionsMap = engine.getSheetVersions();
        List<Map<String, Object>> versionsList = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : versionsMap.entrySet()) {
            Map<String, Object> versionEntry = new HashMap<>();
            versionEntry.put("version", entry.getKey());
            versionEntry.put("cellChanges", entry.getValue());
            versionsList.add(versionEntry);
        }
        sheetData.put("versions", versionsList);

        // Add current version
        sheetData.put("currentVersion", currentVersion);

        return sheetData;
    }
}
