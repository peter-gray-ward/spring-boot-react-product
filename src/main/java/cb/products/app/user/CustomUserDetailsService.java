package cb.products.app.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("\tloadUserByUsername: " + username);
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        System.out.println("\tloadUserByUsername: " + user.getName());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getName())
                .password(user.getPassword()) // Must be hashed
                .roles(user.getRole()) // Ensure role formatting matches Spring Security expectations
                .build();
    }
}
