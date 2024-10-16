package web;

import engine.EngineManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import users.UserManager;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Initialize UserManager and store it in the ServletContext
        UserManager userManager = new UserManager();
        sce.getServletContext().setAttribute("userManager", userManager);

        // Initialize EngineManager and store it in the ServletContext
        EngineManager engineManager = new EngineManager();
        sce.getServletContext().setAttribute("engineManager", engineManager);

        System.out.println("UserManager and EngineManager initialized and set in the ServletContext.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("ServletContext is being destroyed. Cleaning up resources.");
    }
}
