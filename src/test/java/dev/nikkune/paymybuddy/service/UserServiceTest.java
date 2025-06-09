package dev.nikkune.paymybuddy.service;

import dev.nikkune.paymybuddy.model.User;
import dev.nikkune.paymybuddy.repository.UserRepository;
import dev.nikkune.paymybuddy.utils.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User connectionUser;

    @BeforeEach
    void setUp() {
        // Create a test user
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setBalance(500.0); // Initialize balance
        testUser.setConnections(new ArrayList<>());

        // Create a connection user
        connectionUser = new User();
        connectionUser.setId(2);
        connectionUser.setUsername("connection");
        connectionUser.setEmail("connection@example.com");
        connectionUser.setPassword("encodedPassword");
        connectionUser.setBalance(500.0); // Initialize balance
        connectionUser.setConnections(new ArrayList<>());
    }

    @Test
    void requiredUser_WithExistingId_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.requiredUser(testUser.getId());

        // Assert
        assertEquals(testUser, result);
        verify(userRepository).findById(testUser.getId());
    }

    @Test
    void requiredUser_WithNonExistingId_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.requiredUser(999));
        assertEquals("User with ID : 999 not found", exception.getMessage());
        verify(userRepository).findById(999);
    }

    @Test
    void getUserByEmail_WithExistingEmail_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserByEmail(testUser.getEmail());

        // Assert
        assertEquals(testUser, result);
        verify(userRepository).findByEmail(testUser.getEmail());
    }

    @Test
    void getUserByEmail_WithNonExistingEmail_ShouldThrowException() {
        // Arrange
        String nonExistingEmail = "nonexisting@example.com";
        when(userRepository.findByEmail(nonExistingEmail)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getUserByEmail(nonExistingEmail));
        assertEquals("User with email : " + nonExistingEmail + " not found", exception.getMessage());
        verify(userRepository).findByEmail(nonExistingEmail);
    }

    @Test
    void register_WithValidUser_ShouldRegisterUser() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setPassword("password123");
        newUser.setBalance(0.0); // Initialize balance
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // Act
        User result = userService.register(newUser);

        // Assert
        assertEquals(newUser.getUsername(), result.getUsername());
        assertEquals(newUser.getEmail(), result.getEmail());
        assertEquals(newUser.getPassword(), result.getPassword());
        assertEquals(0.0, result.getBalance(), "Balance should be initialized to 0.0");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("test@example.com");
        newUser.setPassword("password123");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.register(newUser));
        assertEquals("User with email : " + newUser.getEmail() + " already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_WithExistingUsername_ShouldThrowException() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("testuser");
        newUser.setEmail("new@example.com");
        newUser.setPassword("password123");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.register(newUser));
        assertEquals("User with username : " + newUser.getUsername() + " already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_WithValidUser_ShouldUpdateUser() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setId(testUser.getId());
        updatedUser.setUsername("updatedUsername");
        updatedUser.setEmail("updated@example.com");

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser(updatedUser);

        // Assert
        assertEquals(testUser, result);
        assertEquals(updatedUser.getUsername(), testUser.getUsername());
        assertEquals(updatedUser.getEmail(), testUser.getEmail());
        verify(userRepository).findById(testUser.getId());
        verify(userRepository).save(testUser);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnTrue() {
        // Arrange
        String password = "password123";

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        try (MockedStatic<PasswordUtil> passwordUtilMock = mockStatic(PasswordUtil.class)) {
            passwordUtilMock.when(() -> PasswordUtil.matches(password, testUser.getPassword())).thenReturn(true);

            // Act
            boolean result = userService.login(testUser.getEmail(), password);

            // Assert
            assertTrue(result);
            verify(userRepository).findByEmail(testUser.getEmail());
            passwordUtilMock.verify(() -> PasswordUtil.matches(password, testUser.getPassword()));
        }
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowException() {
        // Arrange
        String password = "wrongPassword";

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        try (MockedStatic<PasswordUtil> passwordUtilMock = mockStatic(PasswordUtil.class)) {
            passwordUtilMock.when(() -> PasswordUtil.matches(password, testUser.getPassword())).thenReturn(false);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> userService.login(testUser.getEmail(), password));
            assertEquals("Invalid password", exception.getMessage());
            verify(userRepository).findByEmail(testUser.getEmail());
            passwordUtilMock.verify(() -> PasswordUtil.matches(password, testUser.getPassword()));
        }
    }

    @Test
    void login_WithNonExistingEmail_ShouldThrowException() {
        // Arrange
        String nonExistingEmail = "nonexisting@example.com";
        String password = "password123";

        when(userRepository.findByEmail(nonExistingEmail)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.login(nonExistingEmail, password));
        assertEquals("User with email : " + nonExistingEmail + " not found", exception.getMessage());
        verify(userRepository).findByEmail(nonExistingEmail);
    }

    @Test
    void getConnections_WithExistingId_ShouldReturnConnections() {
        // Arrange
        testUser.getConnections().add(connectionUser);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // Act
        List<User> result = userService.getConnections(testUser.getId());

        // Assert
        assertEquals(1, result.size());
        assertEquals(connectionUser, result.get(0));
        verify(userRepository).findById(testUser.getId());
    }

    @Test
    void addConnections_WithValidEmail_ShouldAddConnection() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(connectionUser.getEmail())).thenReturn(Optional.of(connectionUser));

        // Act
        List<User> result = userService.addConnection(testUser.getId(), connectionUser.getEmail());

        // Assert
        assertEquals(1, result.size());
        assertEquals(connectionUser, result.get(0));
        verify(userRepository).findById(testUser.getId());
        verify(userRepository).findByEmail(connectionUser.getEmail());
    }

    @Test
    void addConnection_WithExistingConnection_ShouldThrowException() {
        // Arrange
        testUser.getConnections().add(connectionUser);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(connectionUser.getEmail())).thenReturn(Optional.of(connectionUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.addConnection(testUser.getId(), connectionUser.getEmail()));
        assertEquals("User is already connected to this user", exception.getMessage());
        verify(userRepository).findById(testUser.getId());
        verify(userRepository).findByEmail(connectionUser.getEmail());
    }
}
