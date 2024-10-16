package web;

import com.google.gson.Gson;
import data.SheetUserData;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import users.UserManager;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@WebServlet("/getAllSheets") // This defines the URL endpoint
public class GetAllSheetsServlet extends HttpServlet {

    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");

        try {
            // Get the UserManager instance from the servlet context
            UserManager userManager = (UserManager) getServletContext().getAttribute("userManager");
            if (userManager == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write(gson.toJson("UserManager not found"));
                return;
            }

            // Get all sheets from all users
            Set<SheetUserData> allSheets = userManager.getAllSheets();

            // Convert the set of sheets to JSON and send it as the response
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(allSheets));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson("An unexpected error occurred: " + e.getMessage()));
        }
    }
}
