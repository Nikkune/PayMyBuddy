package dev.nikkune.paymybuddy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * SecurityConfig is a configuration class that provides security configurations for the application.
 * It is responsible for setting up the security filter chain, CORS, authentication, session
 * management, CSRF protection, and logout functionality.
 * <p>
 * This class is annotated with {@code @Configuration} and {@code @EnableWebSecurity}
 * to define it as a Spring Security configuration class.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomAuthenticationProvider authenticationProvider;

    public SecurityConfig(CustomAuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    /**
     * Configures the Spring Security filter chain for the application.
     * This method sets up CSRF protection, CORS, session management,
     * authorization rules, HTTP basic authentication, and logout behavior.
     *
     * @param http the {@link HttpSecurity} object to configure the security settings
     * @return the configured {@link SecurityFilterChain} for the application
     * @throws Exception if an error occurs while configuring the security settings
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF for REST APIs
                .csrf().disable()

                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configure session management to be stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Allow public access to authentication endpoints
                        .requestMatchers("/auth/**").permitAll()

                        // Allow OPTIONS requests for CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Require authentication for all other requests
                        .anyRequest().authenticated()
                )

                // Désactiver HTTP Basic
                .httpBasic(basic -> {
                });

        return http.build();
    }

    /**
     * Configures and provides a {@link CorsConfigurationSource} bean for handling Cross-Origin Resource Sharing (CORS) settings.
     * The configuration allows setting the origins, methods, headers, and credentials for cross-origin requests.
     *
     * @return a {@link CorsConfigurationSource} instance configured with the specified CORS settings
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173")); // In production, restrict to your frontend domain
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept"
        ));
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Creates and provides a bean of {@link BCryptPasswordEncoder} to be used
     * for encoding passwords throughout the application. The {@link BCryptPasswordEncoder}
     * is a standard implementation of password hashing using the bcrypt algorithm.
     *
     * @return a configured {@link BCryptPasswordEncoder} instance
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures and provides an {@link AuthenticationManager} bean for managing authentication processes
     * using the specified {@link HttpSecurity} instance.
     *
     * @param http the {@link HttpSecurity} instance used to configure the authentication manager
     *             and its components
     * @return the configured {@link AuthenticationManager} instance
     * @throws Exception if an error occurs during the configuration of the authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authManagerBuilder.authenticationProvider(authenticationProvider);
        return authManagerBuilder.build();
    }
}
