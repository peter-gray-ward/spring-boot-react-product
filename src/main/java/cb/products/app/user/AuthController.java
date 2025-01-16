package cb.products.app.user;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
public class AuthController {
    private AuthService authService;
    private UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
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
        return new ResponseEntity<>(new User("Missing parameters."), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User userBody, HttpServletResponse response, HttpSession session) {
        User user = authService.handleLogin(userBody.getName(), userBody.getPassword(), userBody.getRole());

        if (user.getException() == null) {
            user.setAccessToken(UUID.randomUUID().toString());
            SessionUtil.login(response, session, user);
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
        
        return new ResponseEntity<>(user, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/login/access-token/{id}/{accessToken}")
    public ResponseEntity<User> loginAccessToken(@PathVariable String accessToken, @PathVariable Long id, HttpSession session) {
        Object sessionAccessToken = session.getAttribute(id + ".access_token");
        if (sessionAccessToken == null || sessionAccessToken.toString().equals(accessToken) == false) {
            return new ResponseEntity<>(new User("unauthenticated"), HttpStatus.OK);
        }
        User user = userService.findById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Boolean> logout(HttpServletRequest req, HttpServletResponse res) {
        SessionUtil.logout(req, res);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}