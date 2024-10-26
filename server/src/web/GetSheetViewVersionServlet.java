package web;

import com.google.gson.Gson;
import com.sheetcell.engine.sheet.api.SheetReadActions;
import engine.EngineManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.SheetDataUtils;

import java.io.IOException;
import java.util.Map;

@WebServlet("/getSheetVersionView")
public class GetSheetViewVersionServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        String sheetName = request.getParameter("sheetName");
        String version = request.getParameter("version");

        if (sheetName == null || sheetName.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(Map.of("error", "Missing sheetName parameter.")));
            return;
        }
        if (version == null || version.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(Map.of("error", "Missing version parameter.")));
            return;
        }
        int versionInt = Integer.parseInt(version);

        EngineManager engineManager = (EngineManager) getServletContext().getAttribute("engineManager");
        if (engineManager == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of("error", "EngineManager not initialized.")));
            return;
        }
        SheetReadActions sheetReadActions = engineManager.getSheetDataVersion(sheetName, versionInt);
        if (sheetReadActions == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson(Map.of("error", "Sheet not found.")));
            return;
        }

        // Prepare data to be sent
        Map<String, Object> sheetData = SheetDataUtils.getSheetDataVersion(sheetReadActions, versionInt);


        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(sheetData));
    }
}

