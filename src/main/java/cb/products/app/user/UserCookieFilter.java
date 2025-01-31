package cb.products.app.user;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Collections;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class UserCookieFilter extends OncePerRequestFilter {

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "/login",
        "/login/access-token",
        "/register",
        "/actuator",
        "/authentication",
        "/static"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        HttpSession session = request.getSession(false);


        if (isExcluded(path)) {
            System.out.println(path + " is excluded");
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("Filtering path " + path);

        boolean authenticated = false;
        String userId = null;
        String userAccessToken = null;

        Cookie[] cookies = request.getCookies();


        if (cookies != null) {
            Optional<Cookie> cbUserCookie = Arrays.stream(cookies)
                    .filter(c -> c.getName().equals("cbUser"))
                    .findFirst();

            if (cbUserCookie.isPresent()) {
                String cbUserValue = cbUserCookie.get().getValue();
                if (cbUserValue != null && cbUserValue.contains(".")) {

                    System.out.println("User has app cookie: " + cbUserValue);

                    // Split the "cbUser" value into userId and accessToken
                    String[] parts = cbUserValue.split("\\.", 2);
                    userId = parts[0];
                    userAccessToken = parts[1];

                    // Validate session attribute
                    Object sessionToken = session.getAttribute(userId + ".access_token");
                    if (userAccessToken.equals(sessionToken)) {
                        authenticated = true; // Authenticated
                    }
                }
            }
        }

        System.out.println("authenticated: " + authenticated);

        if (authenticated) {
        	UserDetails userDetails = new User(userId, "", Collections.emptyList());

            // Create an authentication token
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Set authentication in SecurityContextHolder
            SecurityContextHolder.getContext().setAuthentication(authentication);
        
                


            filterChain.doFilter(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/authentication");
        }
    }

    private boolean isExcluded(String path) {
        if (path.equals("/")) {
            return true;
        }
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isAuthenticated(HttpServletRequest request, HttpSession session) {
        if (session == null) {
            return false; // No session means the user is not authenticated
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Optional<Cookie> cbUserCookie = Arrays.stream(cookies)
                    .filter(c -> c.getName().equals("cbUser"))
                    .findFirst();

            if (cbUserCookie.isPresent()) {
                String cbUserValue = cbUserCookie.get().getValue();
                if (cbUserValue != null && cbUserValue.contains(".")) {

                    System.out.println("User has app cookie: " + cbUserValue);

                    // Split the "cbUser" value into userId and accessToken
                    String[] parts = cbUserValue.split("\\.", 2);
                    String userId = parts[0];
                    String accessToken = parts[1];

                    // Validate session attribute
                    Object sessionToken = session.getAttribute(userId + ".access_token");
                    if (accessToken.equals(sessionToken)) {
                        return true; // Authenticated
                    }
                }
            }
        }

        return false; // Default to not authenticated
    }

}