package utils;

import jakarta.servlet.ServletContext;
import users.UserManager;

public class ServletUtils {

    private static final Object userManagerLock = new Object();

    public static UserManager getUserManager(ServletContext servletContext) {
        synchronized (userManagerLock) {
            if (servletContext.getAttribute("userManager") == null) {
                servletContext.setAttribute("userManager", new UserManager());
            }
        }
        return (UserManager) servletContext.getAttribute("userManager");
    }
}
