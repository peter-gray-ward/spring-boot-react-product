package cb.products.app.user;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

public class SessionUtil {

    public static void login(HttpServletResponse response, HttpSession session, User user) {
        try {
            String accessToken = user.getAccessToken();
            Long userId = user.getId();

            // set server session
            session.setAttribute(userId + ".access_token", accessToken);
            session.setMaxInactiveInterval(10000);

            // set client cookie
            Cookie cookie = new Cookie("cbUser", userId + "." + accessToken);
            cookie.setMaxAge(60 * 60 * 24);       
            cookie.setPath("/");   

            // Add the cookie to the response
            response.addCookie(cookie);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void logout(HttpServletRequest req, HttpServletResponse res) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public static String getCookie(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        return "";
    }   
}
