package cb.products.app.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
public class AuthController {
    private AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User userBody, HttpSession session) {
        String name = userBody.getName();
        String password = userBody.getPassword();
        String role = userBody.getRole();
        if (name != null && password != null && role != null) {
            User user = authService.handleRegistration(name, password, role);
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
        System.out.println(name);
        System.out.println(password);
        return new ResponseEntity<>(new User("Missing parameters."), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User userBody, HttpSession session) {

        User user = authService.handleLogin(userBody.getName(), userBody.getPassword(), userBody.getRole());

        if (user.getException() == null) {
            SessionUtil.login(session, user);
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
        
        return new ResponseEntity<>(user, HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/logout")
    public ResponseEntity<Boolean> logout(HttpServletRequest req, HttpServletResponse res) {
        SessionUtil.logout(req, res);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}