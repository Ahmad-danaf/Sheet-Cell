package web;

import com.google.gson.Gson;
import engine.EngineManager;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/checkForNewVersion")
public class CheckForNewVersionServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String sheetName = request.getParameter("sheetName");
        int currentVersion = Integer.parseInt(request.getParameter("currentVersion"));

        EngineManager engineManager = (EngineManager) getServletContext().getAttribute("engineManager");
        if (engineManager == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of("error", "EngineManager not initialized.")));
            return;
        }

        int latestVersion = engineManager.getLatestSheetVersion(sheetName);
        if (latestVersion > currentVersion) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(Map.of("latestVersion", latestVersion)));
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(Map.of("latestVersion", currentVersion)));
        }
    }
}
