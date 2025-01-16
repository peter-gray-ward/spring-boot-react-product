package cb.products.app.user;

import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;




@Service
public class AuthService {

    private UserService userService;

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

    public AuthService(UserService userService) {
        this.userService = userService;
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
            System.out.println("Logging in " + name + ":" + password + "- " + role);
            User user = userService.findByUsername(name);
            System.out.println(user);

            if (user == null || user.getId() == null) {
                return new User("User " + name + " does not exist.");
            }


            System.out.println("found a user " + user.getName());

            boolean passwordMatches = false;

            try {
                passwordMatches = AuthService.PasswordUtil.checkPassword(password, user.getPassword());
            } catch (Exception e) {
                return new User("Invalid password");
            }

            if (role.equals(user.getRole()) == false) {
                return new User("Incorrect role");
            } else if (passwordMatches) {
                String accessToken = UUID.randomUUID().toString();
                user.setAccessToken(accessToken);
                return user;
            } else {
                return new User("Incorrect password or name.");
            }
        } catch (Exception e) {
            return new User(e.getMessage());
        }
    }

    public void handleLogout(HttpServletRequest request, HttpServletResponse response) {
        //SessionUtil.handleLogout(request, response);
    }
}
