package cb.products.app.user;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
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
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthController(AuthService authService, UserService userService, BCryptPasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
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
    public ResponseEntity<User> login(@RequestBody User userBody, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        System.out.println("/login " + userBody.getName());

        try {
            // User user = authService.handleLogin(userBody.getName(), userBody.getPassword(), userBody.getRole());

            // if (user.getException() == null) {
            //     user.setAccessToken(UUID.randomUUID().toString());
                
            //     SessionUtil.login(response, session, user);
                
            //     return new ResponseEntity<>(user, HttpStatus.OK);
            // }
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userBody.getName(), userBody.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());

            user.setAccessToken(UUID.randomUUID().toString());

            request.getSession().setMaxInactiveInterval(10000);

            SessionUtil.login(response, session, user);

            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/login/access-token/{id}/{accessToken}")
    public ResponseEntity<User> loginAccessToken(@PathVariable String accessToken, @PathVariable Long id, HttpSession session) {
        Object sessionAccessToken = session.getAttribute(id + ".access_token");

        System.out.println("/login/access-token/{id}/" + accessToken);
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