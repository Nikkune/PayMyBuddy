package dev.nikkune.paymybuddy.controller;

import dev.nikkune.paymybuddy.dto.LoginDTO;
import dev.nikkune.paymybuddy.dto.UserRegistrationDTO;
import dev.nikkune.paymybuddy.model.User;
import dev.nikkune.paymybuddy.service.UserService;
import dev.nikkune.paymybuddy.utils.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AuthController is a REST controller that handles authentication and user-related operations.
 * It provides endpoints for user login, registration, and logout. The controller interacts with
 * the underlying service layer to perform the required operations and returns the responses
 * accordingly.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    /**
     * A service for handling user-related operations such as user retrieval, registration,
     * and other user management functions. This service is a crucial component for enabling
     * authentication and other user-centric features in the application.
     */
    private final UserService userService;
    /**
     * Manages authentication processes for the application, including verifying user credentials
     * and initiating authentication operations. Injected as a dependency in the AuthController
     * to support login functionalities.
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Constructs an instance of AuthController with the specified services
     * for user management and authentication management.
     *
     * @param userService           the service responsible for user-related operations
     * @param authenticationManager the Spring Security authentication manager
     *                              responsible for managing user authentication
     */
    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Handles user login by authenticating the provided credentials and returning a success or failure response.
     *
     * @param loginDTO the DTO containing the login credentials (email and password)
     * @return a ResponseEntity containing a success message and user details if authentication succeeds,
     * or an error message with an unauthorized status if it fails
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginDTO loginDTO) {
        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())
            );

            // Set the authentication in the security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user details
            User user = userService.getUserByEmail(loginDTO.getEmail());

            Response responseBody = new Response("Login successful", true)
                    .add("userId", user.getId())
                    .add("username", user.getUsername())
                    .add("email", user.getEmail())
                    .add("balance", user.getBalance());

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Response responseBody = new Response("Authentication failed", false).error(e.getMessage());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);

        }
    }

    /**
     * Handles the user registration process by creating a new user and persisting it in the system.
     * The method takes in user registration details, validates them, and registers the user with an initial balance.
     * If registration is successful, the response will contain the user's details; otherwise, an error message will be returned.
     *
     * @param registrationDTO the data transfer object containing registration details such as username, email, and password
     * @return a ResponseEntity containing:
     * - HTTP status 201 (Created) and user details upon successful registration
     * - HTTP status 400 (Bad Request) and an error message in case of failure
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid UserRegistrationDTO registrationDTO) {
        try {
            // Create a new user from the registration DTO
            User newUser = new User();
            newUser.setUsername(registrationDTO.getUsername());
            newUser.setEmail(registrationDTO.getEmail());
            newUser.setPassword(registrationDTO.getPassword());
            newUser.setBalance(200); // Initial balance is 200

            // Register the user
            User registeredUser = userService.register(newUser);

            Response responseBody = new Response("Registration successful", true)
                    .add("userId", registeredUser.getId())
                    .add("username", registeredUser.getUsername())
                    .add("email", registeredUser.getEmail())
                    .add("balance", registeredUser.getBalance());

            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
        } catch (Exception e) {
            Response responseBody = new Response("Registration failed", false).error(e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
        }
    }

    /**
     * Handles the logout process for the currently authenticated user. This method
     * invalidates the user session, clears the security context, and generates an appropriate
     * response message.
     *
     * @param request  the {@code HttpServletRequest} object containing client request data
     * @param response the {@code HttpServletResponse} object used to send the server response
     * @return a {@code ResponseEntity} containing a success message if logout is successful,
     * or an error message if an exception occurs during the logout process
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Get the current authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // If the user is authenticated, perform logout
            if (authentication != null) {
                new SecurityContextLogoutHandler().logout(request, response, authentication);
            }

            Response responseBody = new Response("Logout successful", true);

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Response responseBody = new Response("Logout failed", false).error(e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        }
    }
}
