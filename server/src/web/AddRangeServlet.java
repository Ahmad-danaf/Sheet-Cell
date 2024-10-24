package web;

import com.google.gson.Gson;
import com.sheetcell.engine.Engine;
import com.sheetcell.engine.sheet.api.SheetReadActions;
import engine.EngineManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/addRange")
public class AddRangeServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        String sheetName = request.getParameter("sheetName");
        String rangeName = request.getParameter("rangeName");
        String rangeDefinition = request.getParameter("rangeDefinition");

        if (sheetName == null || rangeName == null || rangeDefinition == null) {
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

        // Get the sheet data from the engine for the provided version
        Engine engine = engineManager.getSheetEngine(sheetName);
        if (engine == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson(Map.of("error", "Sheet not found.")));
            return;
        }

        try {
            // Add the range to the sheet
            engine.addRange(rangeName, rangeDefinition);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(Map.of("message", "Range added successfully.")));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of("error", "Failed to add range: " + e.getMessage())));
        }
    }
}

