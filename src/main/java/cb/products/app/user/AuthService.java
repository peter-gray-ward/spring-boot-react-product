package cb.products.app.user;

import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;




@Service
public class AuthService {

    private UserService userService;
    private final AuthenticationManager authenticationManager;

    public static class PasswordUtil {
        // Generate a hashed password
        public static String hashPassword(String plainPassword) {
            try {
                String salt = BCrypt.gensalt();
                return BCrypt.hashpw(plainPassword, salt);
            } catch (IllegalArgumentException e) {
                // Handle the exception gracefully, e.g., log it or return a default value
                System.err.println("Error hashing password: " + e.getMessage());
                return null; // or handle it as needed
            }
        }

        // Verify the password
        public static boolean checkPassword(String plainPassword, String hashedPassword) {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        }
    }

    public AuthService(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public User handleRegistration(String name, String password, String role) {
        try {
            String sql = "SELECT COUNT(*) FROM \"USER\" WHERE name = ?";
            
            Integer count = jdbcTemplate.queryForObject(
                sql, 
                Integer.class,
                name
            );

            if (count != null && count > 0) {
                return new User("User " + name + " already exists.");
            }


            sql = "INSERT INTO \"USER\" (name, password, role) VALUES (?, ?, ?)";
            jdbcTemplate.update(
                sql,
                name, 
                PasswordUtil.hashPassword(password),
                role
            );

            User user = userService.findByUsername(name);

            if (user == null) {
                throw new Exception("...");
            }

            return user;
        } catch (DuplicateKeyException ex) {
            return new User("User " + name + " is already registered.");
        } catch (Exception ex) {
            ex.printStackTrace();
            return new User("An error occurred during registration.");
        }
    }

    public User handleLogin(String name, String password, String role) {
        try {
            // Authenticate using Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(name, password)
            );

            
            // Fetch authenticated user
            User user = userService.findByUsername(name);
            if (user == null || user.getId() == null) {
                return new User("User " + name + " does not exist.");
            }
    
            // Check if the role matches
            if (!role.equals(user.getRole())) {
                return new User("Incorrect role for user " + name);
            }
    
            // Generate access token (replace with JWT in production)
            String accessToken = UUID.randomUUID().toString();
            user.setAccessToken(accessToken);
    
            return user;
        } catch (Exception e) {
            return new User("Invalid username, password, or role.");
        }
    }
    
    
    

    public void handleLogout(HttpServletRequest request, HttpServletResponse response) {
        //SessionUtil.handleLogout(request, response);
    }
}
