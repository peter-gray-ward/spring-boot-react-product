package cb.products.app.user;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;




@Service
public class AuthService {

    private UserService userService;

    public AuthService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    

    public User handleRegistration(String name, String password, String role) {
        try {
            String encodedPassword = passwordEncoder.encode(password);
    
            String sql = "INSERT INTO \"USER\" (name, password, role) VALUES (?, ?, ?)";
            jdbcTemplate.update(sql, name, encodedPassword, role);
    
            return userService.findByUsername(name);
        } catch (DuplicateKeyException ex) {
            return new User("User " + name + " is already registered.");
        } catch (Exception ex) {
            ex.printStackTrace();
            return new User("An error occurred during registration.");
        }
    }
        
    public User handleLogin(String name, String password, String role) {
        try {
            User user = userService.findByUsername(name);

            if (user == null || user.getId() == null) {
                return new User("User " + name + " does not exist.");
            }

            boolean passwordMatches = false;

            try {
                passwordMatches = checkPassword(password, user.getPassword());
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
