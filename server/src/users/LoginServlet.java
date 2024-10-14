package users;

import com.google.gson.Gson;
import constants.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import responses.LoginResponse;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final Gson gson = new Gson();  // Gson object to handle JSON conversion

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter(Constants.USERNAME);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());

        // Set the response content type to application/json
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (username == null || username.trim().isEmpty()) {
            // Username is missing
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            LoginResponse loginResponse = new LoginResponse("Username is required.", null, "Username is required.");
            response.getWriter().write(gson.toJson(loginResponse));
        } else {
            // Clean up the username (trim)
            username = username.trim();

            synchronized (this) {
                if (userManager.isUserExists(username)) {
                    // Username already exists, return error message
                    String errorMessage = "Username '" + username + "' is already taken. Please choose a different username.";
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    LoginResponse loginResponse = new LoginResponse(null, null, errorMessage);
                    response.getWriter().write(gson.toJson(loginResponse));
                } else {
                    // Add user to the UserManager and create session
                    userManager.addUser(username);
                    request.getSession(true).setAttribute(Constants.USERNAME, username);

                    // Send successful response
                    response.setStatus(HttpServletResponse.SC_OK);
                    LoginResponse loginResponse = new LoginResponse("Login successful", username, null);
                    response.getWriter().write(gson.toJson(loginResponse));
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Set the response content type to application/json
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String username = SessionUtils.getUsername(request);
        if (username != null) {
            LoginResponse loginResponse = new LoginResponse("User already logged in", username, null);
            response.getWriter().write(gson.toJson(loginResponse));
        } else {
            LoginResponse loginResponse = new LoginResponse("No user is logged in", null, null);
            response.getWriter().write(gson.toJson(loginResponse));
        }
    }
}
