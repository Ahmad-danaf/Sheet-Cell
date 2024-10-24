package web;

import com.google.gson.Gson;
import com.sheetcell.engine.Engine;
import engine.EngineManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@WebServlet("/getRanges")
public class GetRangesServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        String sheetName = request.getParameter("sheetName");

        if (sheetName == null || sheetName.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(Map.of("error", "Missing or empty sheetName parameter")));
            return;
        }

        EngineManager engineManager = (EngineManager) getServletContext().getAttribute("engineManager");
        if (engineManager == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of("error", "EngineManager not initialized")));
            return;
        }

        // Retrieve the engine for the sheet
        Engine engine = engineManager.getSheetEngine(sheetName);
        if (engine == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson(Map.of("error", "Sheet not found")));
            return;
        }

        // Fetch all ranges from the engine
        Set<String> ranges = engine.getAllRanges();
        if (ranges == null || ranges.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(Set.of())); // Return empty set if no ranges are found
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(ranges)); // Return ranges in JSON format
        }
    }
}
