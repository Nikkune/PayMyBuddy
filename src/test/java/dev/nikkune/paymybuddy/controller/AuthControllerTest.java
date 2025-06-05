package dev.nikkune.paymybuddy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nikkune.paymybuddy.dto.LoginDTO;
import dev.nikkune.paymybuddy.dto.UserRegistrationDTO;
import dev.nikkune.paymybuddy.model.User;
import dev.nikkune.paymybuddy.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private User testUser;
    private LoginDTO loginDTO;
    private UserRegistrationDTO registrationDTO;

    @BeforeEach
    void setUp() {
        // Initialize MockMvc and ObjectMapper
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();

        // Create a test user
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");

        // Create login DTO
        loginDTO = new LoginDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("password123");

        // Create registration DTO
        registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("newuser");
        registrationDTO.setEmail("new@example.com");
        registrationDTO.setPassword("password123");
    }

    @Test
    void login_WithValidCredentials_ShouldReturnSuccessResponse() throws Exception {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userService.getUserByEmail(loginDTO.getEmail())).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Login successful")))
                .andExpect(jsonPath("$.userId", is(testUser.getId())))
                .andExpect(jsonPath("$.username", is(testUser.getUsername())))
                .andExpect(jsonPath("$.email", is(testUser.getEmail())));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).getUserByEmail(loginDTO.getEmail());
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Authentication failed"));

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", containsString("Authentication failed")));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService, never()).getUserByEmail(anyString());
    }

    @Test
    void register_WithValidData_ShouldReturnSuccessResponse() throws Exception {
        // Arrange
        User registeredUser = new User();
        registeredUser.setId(2);
        registeredUser.setUsername(registrationDTO.getUsername());
        registeredUser.setEmail(registrationDTO.getEmail());
        registeredUser.setPassword("encodedPassword");

        when(userService.register(any(User.class))).thenReturn(registeredUser);

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Registration successful")))
                .andExpect(jsonPath("$.userId", is(registeredUser.getId())))
                .andExpect(jsonPath("$.username", is(registeredUser.getUsername())))
                .andExpect(jsonPath("$.email", is(registeredUser.getEmail())));

        verify(userService).register(any(User.class));
    }

    @Test
    void register_WithExistingEmail_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(userService.register(any(User.class)))
                .thenThrow(new RuntimeException("User with email : " + registrationDTO.getEmail() + " already exists"));

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", containsString("Registration failed")));

        verify(userService).register(any(User.class));
    }

    @Test
    void logout_ShouldReturnSuccessResponse() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Logout successful")));
    }
}
