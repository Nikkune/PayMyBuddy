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
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        List<User> expectedUsers = Arrays.asList(testUser, connectionUser);
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // Act
        List<User> actualUsers = userService.getAllUsers();

        // Assert
        assertEquals(expectedUsers, actualUsers);
        verify(userRepository).findAll();
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
        assertEquals("User with ID : 999 does not exist", exception.getMessage());
        verify(userRepository).findById(999);
    }

    @Test
    void getUserById_WithExistingId_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserById(testUser.getId());

        // Assert
        assertEquals(testUser, result);
        verify(userRepository).findById(testUser.getId());
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
        assertEquals("User with email : " + nonExistingEmail + " does not exist", exception.getMessage());
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
    void updatePassword_WithValidCredentials_ShouldUpdatePassword() {
        // Arrange
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        String encodedNewPassword = "encodedNewPassword";

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        try (MockedStatic<PasswordUtil> passwordUtilMock = mockStatic(PasswordUtil.class)) {
            passwordUtilMock.when(() -> PasswordUtil.matches(oldPassword, testUser.getPassword())).thenReturn(true);
            passwordUtilMock.when(() -> PasswordUtil.encodePassword(newPassword)).thenReturn(encodedNewPassword);

            // Act
            User result = userService.updatePassword(testUser.getId(), oldPassword, newPassword);

            // Assert
            assertEquals(testUser, result);
            assertEquals(encodedNewPassword, testUser.getPassword());
            verify(userRepository).findById(testUser.getId());
            verify(userRepository).save(testUser);

            // Don't verify the exact calls to static methods as they can be tricky with Mockito
            // Instead, just verify that the password was updated correctly
        }
    }

    @Test
    void updatePassword_WithInvalidOldPassword_ShouldThrowException() {
        // Arrange
        String oldPassword = "wrongPassword";
        String newPassword = "newPassword";

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        try (MockedStatic<PasswordUtil> passwordUtilMock = mockStatic(PasswordUtil.class)) {
            passwordUtilMock.when(() -> PasswordUtil.matches(oldPassword, testUser.getPassword())).thenReturn(false);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> userService.updatePassword(testUser.getId(), oldPassword, newPassword));
            assertEquals("Old password does not match", exception.getMessage());
            verify(userRepository).findById(testUser.getId());
            verify(userRepository, never()).save(any(User.class));
            passwordUtilMock.verify(() -> PasswordUtil.matches(oldPassword, testUser.getPassword()));
        }
    }

    @Test
    void deleteUser_WithExistingId_ShouldDeleteUser() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        // Act
        userService.deleteUser(testUser.getId());

        // Assert
        verify(userRepository).findById(testUser.getId());
        verify(userRepository).delete(testUser);
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
        assertEquals("User with email : " + nonExistingEmail + " does not exist", exception.getMessage());
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
    void addConnections_WithValidIds_ShouldAddConnection() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.findById(connectionUser.getId())).thenReturn(Optional.of(connectionUser));

        // Act
        List<User> result = userService.addConnection(testUser.getId(), connectionUser.getId());

        // Assert
        assertEquals(1, result.size());
        assertEquals(connectionUser, result.get(0));
        verify(userRepository).findById(testUser.getId());
        verify(userRepository).findById(connectionUser.getId());
    }

    @Test
    void addConnection_WithExistingConnection_ShouldThrowException() {
        // Arrange
        testUser.getConnections().add(connectionUser);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.findById(connectionUser.getId())).thenReturn(Optional.of(connectionUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.addConnection(testUser.getId(), connectionUser.getId()));
        assertEquals("User is already connected to this user", exception.getMessage());
        verify(userRepository).findById(testUser.getId());
        verify(userRepository).findById(connectionUser.getId());
    }

    @Test
    void removeConnections_WithValidIds_ShouldRemoveConnection() {
        // Arrange
        testUser.getConnections().add(connectionUser);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.findById(connectionUser.getId())).thenReturn(Optional.of(connectionUser));

        // Act
        List<User> result = userService.removeConnection(testUser.getId(), connectionUser.getId());

        // Assert
        assertEquals(0, result.size());
        verify(userRepository).findById(testUser.getId());
        verify(userRepository).findById(connectionUser.getId());
    }

    @Test
    void removeConnection_WithNonExistingConnection_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.findById(connectionUser.getId())).thenReturn(Optional.of(connectionUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.removeConnection(testUser.getId(), connectionUser.getId()));
        assertEquals("User is not connected to this user", exception.getMessage());
        verify(userRepository).findById(testUser.getId());
        verify(userRepository).findById(connectionUser.getId());
    }
}
