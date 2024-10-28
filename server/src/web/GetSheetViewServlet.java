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
import utils.SheetDataUtils;

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

        if (sheetName == null || sheetName.isEmpty()) {
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
        SheetReadActions sheetReadActions = engineManager.getSheetData(sheetName);
        Engine engine = engineManager.getSheetEngine(sheetName);
        if (sheetReadActions == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson(Map.of("error", "Sheet not found.")));
            return;
        }
        if (engine == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson(Map.of("error", "Sheet not found.")));
            return;
        }

        // Prepare data to be sent
        Map<String, Object> sheetData = SheetDataUtils.getSheetData(sheetReadActions, engine,sheetReadActions.getVersion());


        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(sheetData));
    }
}
