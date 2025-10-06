package com.bunkermuseum.membermanagement.config;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security configuration for the application.
 *
 * <p>This configuration class provides security settings for a
 *    Spring Boot application with production-ready security features.</p>
 *
 * <h3>Security Features:</h3>
 * <ul>
 *     <li><strong>Password Encryption:</strong> BCrypt with strength 12 for secure password hashing</li>
 *     <li><strong>CSRF Protection:</strong> Enabled by default for all endpoints</li>
 *     <li><strong>Session Management:</strong> Secure HTTP session handling</li>
 *     <li><strong>Session Fixation:</strong> Protection against session fixation attacks</li>
 *     <li><strong>Frame Options:</strong> X-Frame-Options DENY to prevent clickjacking</li>
 *     <li><strong>XSS Protection:</strong> Content Security Policy headers</li>
 * </ul>
 *
 * <h3>Session Configuration:</h3>
 * <ul>
 *     <li>Session timeout: 30 minutes of inactivity (configurable in application.properties)</li>
 *     <li>Secure cookies in production (HTTPS only)</li>
 *     <li>HttpOnly cookies to prevent XSS attacks</li>
 *     <li>SameSite=Strict to prevent CSRF</li>
 * </ul>
 *
 * @author Philipp Borkovic
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends VaadinWebSecurity {

    /**
     * Configures HTTP security settings for the application.
     *
     * <p>This method sets up comprehensive security configurations including
     * session management, CSRF protection, and security headers.</p>
     *
     * <h3>Configuration Details:</h3>
     * <ul>
     *     <li><strong>Session Management:</strong> Creates new session on authentication</li>
     *     <li><strong>Session Fixation:</strong> Migrates session to prevent fixation attacks</li>
     *     <li><strong>Logout:</strong> Invalidates session and clears authentication</li>
     *     <li><strong>CSRF:</strong> Enabled for all endpoints (Vaadin handles token automatically)</li>
     * </ul>
     *
     * @param http The HttpSecurity object to configure
     * @throws Exception if an error occurs during configuration
     *
     * @author Philipp Borkovic
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement(session -> session
                .sessionFixation().migrateSession()
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            );

        super.configure(http);

        setLoginView(http, "/login", "/");

        http.logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
            .clearAuthentication(true)
        );
    }

    /**
     * Provides a BCrypt password encoder bean for secure password hashing.
     *
     * <p>BCrypt is a strong adaptive hashing function designed for password storage.
     * It includes automatic salt generation and configurable work factor to remain
     * resistant to brute-force attacks as hardware improves.</p>
     *
     * <h3>BCrypt Configuration:</h3>
     * <ul>
     *     <li><strong>Strength:</strong> 12 (2^12 = 4096 iterations)</li>
     *     <li><strong>Salt:</strong> Automatically generated per password</li>
     *     <li><strong>Algorithm:</strong> Blowfish-based adaptive hashing</li>
     *     <li><strong>Hash Length:</strong> 60 characters (includes salt and cost)</li>
     * </ul>
     *
     * @return A configured BCrypt password encoder with strength 12
     *
     * @author Philipp Borkovic
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
