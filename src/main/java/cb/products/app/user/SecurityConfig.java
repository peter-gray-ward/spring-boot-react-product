package cb.products.app.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable() 
            .authorizeHttpRequests(auth -> auth 
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll() // Allow access to static resources 
                .requestMatchers("/", "/register", "/login", "/login/access-token/**").permitAll() 
                .anyRequest().authenticated() 
            ) 
            .anonymous() 
            .and() 
            .formLogin()
            .disable();
    

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
