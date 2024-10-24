package web;

import com.google.gson.Gson;
import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.sheet.api.SheetReadActions;
import engine.EngineManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@WebServlet("/getSheetRanges")
public class GetSheetRangesServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        String sheetName = request.getParameter("sheetName");
        String versionParam = request.getParameter("currentVersion");

        if (sheetName == null || sheetName.isEmpty() || versionParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(Map.of("error", "Missing required parameters.")));
            return;
        }

        int currentVersion;
        try {
            currentVersion = Integer.parseInt(versionParam);  // Parse the current version
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(Map.of("error", "Invalid version number.")));
            return;
        }

        EngineManager engineManager = (EngineManager) getServletContext().getAttribute("engineManager");
        if (engineManager == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of("error", "EngineManager not initialized.")));
            return;
        }

        // Get the ranges for the specified version from the engine
        SheetReadActions sheetReadActions = engineManager.getSheetDataVersion(sheetName, currentVersion);
        if (sheetReadActions == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson(Map.of("error", "Sheet or version not found.")));
            return;
        }

        // Retrieve ranges and their coordinates
        Set<String> rangeNames = sheetReadActions.getRanges();
        Map<String, Set<Coordinate>> ranges = new HashMap<>();
        for (String rangeName : rangeNames) {
            ranges.put(rangeName, sheetReadActions.getRangeCoordinates(rangeName));
        }

        // Send the ranges and their coordinates as a JSON response
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(ranges));
    }
}

