package cb.products.app.user;

import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
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

    public User handleRegistration(String name, String password) {
        String error = null;

        if (name == null || name.isEmpty()) {
            error = "Name is required.";
        } else if (password == null || password.isEmpty()) {
            error = "Password is required.";
        }

        if (error == null) {
            try {
                String sql = "SELECT COUNT(*) FROM public.user WHERE name = ?";
                
                Integer count = jdbcTemplate.queryForObject(
                    sql, 
                    Integer.class,
                    name
                );

                if (count != null && count > 0) {
                    return new User("User " + name + " already exists.");
                }


                sql = "INSERT INTO public.user (id, name, password) VALUES (?, ?, ?)";
                jdbcTemplate.update(
                    sql, 
                    UUID.randomUUID(), 
                    name, 
                    PasswordUtil.hashPassword(password)
                );

                User user = userService.findByUsername(name);

                return user;
            } catch (DuplicateKeyException ex) {
                error = "User " + name + " is already registered.";
            } catch (Exception ex) {
                ex.printStackTrace();
                error = "An error occurred during registration.";
            }
        }
        return null;
    }

    public User handleLogin(String name, String password) {
        String error = null;

        try {
            User user = userService.findByUsername(name);

            if (user ==  null) {
                return new User("User " + name + " does not exist.");
            }

            boolean passwordMatches = false;

            try {
                passwordMatches = AuthService.PasswordUtil.checkPassword(password, user.getPassword());
            } catch (Exception e) {
                error = "Invalid password";
                e.printStackTrace();
            }

            if (user != null && passwordMatches) {
                String accessToken = UUID.randomUUID().toString();
                user.setAccessToken(accessToken);

                return user;
            } else {
                error = "Incorrect password or name.";
            }
        } catch (EmptyResultDataAccessException e) {
            error = "User " + name + " not found.";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void handleLogout(HttpServletRequest request, HttpServletResponse response) {
        //SessionUtil.handleLogout(request, response);
    }
}
