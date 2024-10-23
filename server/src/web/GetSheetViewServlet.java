package web;

import com.google.gson.Gson;
import com.sheetcell.engine.Engine;
import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.sheet.api.SheetReadActions;
import com.sheetcell.engine.utils.ColumnProperties;
import engine.EngineManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;

@WebServlet("/getSheetView")
public class GetSheetViewServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        String sheetName = request.getParameter("sheetName");
        //String userId = (String) request.getSession().getAttribute("username");
        String userId = (String) request.getParameter("username");

        if (sheetName == null || sheetName.isEmpty() || userId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(Map.of("error", "Missing required parameters.")));
            return;
        }

        EngineManager engineManager = (EngineManager) getServletContext().getAttribute("engineManager");
        if (engineManager == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of("error", "EngineManager not initialized.")));
            return;
        }
        System.out.println("###################################################");
        System.out.println("SheetName: "+sheetName);
        System.out.println("UserId: "+userId);
        SheetReadActions sheetReadActions = engineManager.getSheetData(userId, sheetName);
        if (sheetReadActions == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson(Map.of("error", "Sheet not found.")));
            return;
        }

        // Prepare data to be sent
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
        System.out.println("inSide GetSheetViewServlet");
        //print all cellData
        for (Map.Entry<String, Map<String, String>> entry : cellData.entrySet()) {
            System.out.println("Key: "+entry.getKey());
            for (Map.Entry<String, String> entry2 : entry.getValue().entrySet()) {
                System.out.println("Key: "+entry2.getKey()+" Value: "+entry2.getValue());
            }
        }
        sheetData.put("cellData", cellData);
        Engine engine = engineManager.getSheetEngine(userId, sheetName);
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
        int currentVersion = sheetReadActions.getVersion();
        sheetData.put("currentVersion", currentVersion);

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(sheetData));
    }
}
