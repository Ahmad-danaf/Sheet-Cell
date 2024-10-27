package web;

import com.google.gson.Gson;
import com.sheetcell.engine.Engine;
import engine.EngineManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.SheetDataUtils;

import java.io.IOException;
import java.util.Map;

@WebServlet("/getSheetVersions")
public class GetSheetVersionsServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        // Retrieve parameters
        String sheetName = request.getParameter("sheetName");
        if (sheetName == null || sheetName.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(Map.of("error", "Sheet name is required.")));
            return;
        }

        // Retrieve EngineManager
        EngineManager engineManager = (EngineManager) getServletContext().getAttribute("engineManager");
        if (engineManager == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of("error", "EngineManager not initialized.")));
            return;
        }

        // Retrieve engine and sheet data
        try {
            Engine engine = engineManager.getSheetEngine(sheetName);
            if (engine == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(gson.toJson(Map.of("error", "Sheet not found.")));
                return;
            }

            // Use SheetDataUtils to get the sheet data including versions
            Map<String, Object> sheetData = SheetDataUtils.getVersions(engine);

            // Return the sheet data as JSON
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(sheetData));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of("error", "Error retrieving sheet data: " + e.getMessage())));
        }
    }
}
