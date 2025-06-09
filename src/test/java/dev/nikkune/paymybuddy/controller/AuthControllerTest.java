package dev.nikkune.paymybuddy.controller;

import dev.nikkune.paymybuddy.dto.LoginDTO;
import dev.nikkune.paymybuddy.dto.UserRegistrationDTO;
import dev.nikkune.paymybuddy.model.User;
import dev.nikkune.paymybuddy.service.UserService;
import dev.nikkune.paymybuddy.utils.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse httpServletResponse;

    @InjectMocks
    private AuthController authController;

    private User testUser;
    private LoginDTO loginDTO;
    private UserRegistrationDTO registrationDTO;

    @BeforeEach
    void setUp() {
        // Create a test user
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setBalance(200.0);

        // Create login DTO
        loginDTO = new LoginDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("password123");

        // Create registration DTO
        registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("testuser");
        registrationDTO.setEmail("test@example.com");
        registrationDTO.setPassword("password123");
    }

    @Test
    void login_WithValidCredentials_ShouldReturnOkResponse() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userService.getUserByEmail(loginDTO.getEmail())).thenReturn(testUser);

        // Mock SecurityContextHolder
        SecurityContextHolder.setContext(securityContext);

        // Act
        ResponseEntity<?> responseEntity = authController.login(loginDTO);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof Response);
        Response responseBody = (Response) responseEntity.getBody();
        assertTrue((Boolean) responseBody.get("success"));
        assertEquals("Login successful", responseBody.get("message"));
        assertEquals(testUser.getId(), responseBody.get("userId"));
        assertEquals(testUser.getUsername(), responseBody.get("username"));
        assertEquals(testUser.getEmail(), responseBody.get("email"));
        assertEquals(testUser.getBalance(), responseBody.get("balance"));

        // Verify interactions
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).getUserByEmail(loginDTO.getEmail());
        verify(securityContext).setAuthentication(authentication);
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorizedResponse() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Authentication failed"));

        // Act
        ResponseEntity<?> responseEntity = authController.login(loginDTO);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof Response);
        Response responseBody = (Response) responseEntity.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("Authentication failed", responseBody.get("message"));
        assertEquals("Authentication failed", responseBody.get("error"));

        // Verify interactions
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService, never()).getUserByEmail(anyString());
    }

    @Test
    void register_WithValidData_ShouldReturnCreatedResponse() {
        // Arrange
        when(userService.register(any(User.class))).thenReturn(testUser);

        // Act
        ResponseEntity<?> responseEntity = authController.register(registrationDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof Response);
        Response responseBody = (Response) responseEntity.getBody();
        assertTrue((Boolean) responseBody.get("success"));
        assertEquals("Registration successful", responseBody.get("message"));
        assertEquals(testUser.getId(), responseBody.get("userId"));
        assertEquals(testUser.getUsername(), responseBody.get("username"));
        assertEquals(testUser.getEmail(), responseBody.get("email"));
        assertEquals(testUser.getBalance(), responseBody.get("balance"));

        // Verify interactions
        verify(userService).register(any(User.class));
    }

    @Test
    void register_WithExistingEmail_ShouldReturnBadRequestResponse() {
        // Arrange
        when(userService.register(any(User.class)))
                .thenThrow(new RuntimeException("User with email : test@example.com already exists"));

        // Act
        ResponseEntity<?> responseEntity = authController.register(registrationDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof Response);
        Response responseBody = (Response) responseEntity.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("Registration failed", responseBody.get("message"));
        assertEquals("User with email : test@example.com already exists", responseBody.get("error"));

        // Verify interactions
        verify(userService).register(any(User.class));
    }

    @Test
    void logout_WithAuthenticatedUser_ShouldReturnOkResponse() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Act
        ResponseEntity<?> responseEntity = authController.logout(request, httpServletResponse);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof Response);
        Response responseBody = (Response) responseEntity.getBody();
        assertTrue((Boolean) responseBody.get("success"));
        assertEquals("Logout successful", responseBody.get("message"));

        // Verify interactions
        verify(securityContext).getAuthentication();
    }

    @Test
    void logout_WithException_ShouldReturnInternalServerErrorResponse() {
        // Arrange
        when(securityContext.getAuthentication()).thenThrow(new RuntimeException("Logout error"));
        SecurityContextHolder.setContext(securityContext);

        // Act
        ResponseEntity<?> responseEntity = authController.logout(request, httpServletResponse);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof Response);
        Response responseBody = (Response) responseEntity.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("Logout failed", responseBody.get("message"));
        assertEquals("Logout error", responseBody.get("error"));

        // Verify interactions
        verify(securityContext).getAuthentication();
    }
}
