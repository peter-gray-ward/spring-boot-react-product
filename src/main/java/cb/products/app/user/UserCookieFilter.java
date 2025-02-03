package cb.products.app.user;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class UserCookieFilter extends OncePerRequestFilter {

    private static final String COOKIE_NAME = "cbUser";
    private static final String SESSION_ATTRIBUTE_SUFFIX = ".access_token";
    private static final String AUTH_ERROR_MESSAGE = "User authentication failed.";
    
    private static final String[] EXCLUDED_PATHS = {
        "/login",
        "/login/access-token",
        "/register",
        "/actuator",
        "/authentication",
        "/static"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {

        String path = request.getServletPath();
        HttpSession session = request.getSession(false);

        if (isExcluded(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<Cookie> cbUserCookie = getCookie(request, COOKIE_NAME);
        if (cbUserCookie.isPresent() && session != null) {
            String cbUserValue = cbUserCookie.get().getValue();
            
            // Validate cookie format (must contain "userId.accessToken")
            if (cbUserValue.contains(".")) {
                String[] parts = cbUserValue.split("\\.", 2);
                String userId = parts[0];
                String userAccessToken = parts[1];

                Object sessionToken = session.getAttribute(userId + SESSION_ATTRIBUTE_SUFFIX);
                if (userAccessToken.equals(sessionToken)) {
                    authenticateUser(request, userId);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(HttpServletRequest request, String userId) {
        System.out.println("--- AUTHENTICATING " + userId);

        UserDetails userDetails = new User(userId, "", Collections.emptyList());
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        
        // Store authentication in SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Persist SecurityContext in session
        HttpSession session = request.getSession(true);

        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
    }

    private Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> name.equals(cookie.getName()))
                    .findFirst();
        }
        return Optional.empty();
    }

    private boolean isExcluded(String path) {
        if ("/".equals(path)) return true;
        return Arrays.stream(EXCLUDED_PATHS).anyMatch(path::startsWith);
    }
}
