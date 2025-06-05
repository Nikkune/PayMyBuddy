package dev.nikkune.paymybuddy.config;

import dev.nikkune.paymybuddy.service.UserService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * CustomAuthenticationProvider is a custom implementation of the {@link AuthenticationProvider} interface.
 * It provides authentication logic by verifying the user credentials against a user service.
 * <p>
 * This class is annotated with {@code @Component}, allowing it to be discovered and registered as a Spring bean.
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    private final UserService userService;
    public CustomAuthenticationProvider(UserService userService) {
        this.userService = userService;
    }

    /**
     * Authenticates a user by verifying their credentials and returns an authenticated token if successful.
     *
     * @param authentication the authentication request object containing the user's credentials
     * @return an {@link Authentication} object containing the authenticated user's details and authorities
     * @throws AuthenticationException if authentication fails due to invalid credentials or unexpected errors
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();

        try {
            // Use the existing login method from UserService
            boolean isAuthenticated = userService.login(email, password);
            
            if (isAuthenticated) {
                // If authentication is successful, create a new authenticated token
                return new UsernamePasswordAuthenticationToken(
                    email, 
                    password, 
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );
            } else {
                throw new BadCredentialsException("Authentication failed");
            }
        } catch (RuntimeException e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

    /**
     * Checks whether this {@code AuthenticationProvider} supports the indicated {@code Authentication} class.
     * This method is used to match the provided authentication type with the implementation.
     *
     * @param authentication the {@code Class} object representing the type of {@code Authentication} to check
     * @return {@code true} if the provided {@code Authentication} class is supported, {@code false} otherwise
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}