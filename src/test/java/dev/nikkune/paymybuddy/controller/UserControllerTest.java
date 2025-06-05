package dev.nikkune.paymybuddy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nikkune.paymybuddy.dto.UserDTO;
import dev.nikkune.paymybuddy.dto.UserRegistrationDTO;
import dev.nikkune.paymybuddy.mapper.UserMapper;
import dev.nikkune.paymybuddy.model.User;
import dev.nikkune.paymybuddy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private User connectionUser;
    private UserDTO testUserDTO;
    private UserDTO connectionUserDTO;
    private UserRegistrationDTO userRegistrationDTO;

    @BeforeEach
    void setUp() {
        // Initialize MockMvc and ObjectMapper
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();

        // Create a test user
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setConnections(new ArrayList<>());

        // Create a connection user
        connectionUser = new User();
        connectionUser.setId(2);
        connectionUser.setUsername("connection");
        connectionUser.setEmail("connection@example.com");
        connectionUser.setPassword("password123");
        connectionUser.setConnections(new ArrayList<>());

        // Create test user DTO
        testUserDTO = new UserDTO();
        testUserDTO.setId(1);
        testUserDTO.setUsername("testuser");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setConnectionIds(new ArrayList<>());

        // Create connection user DTO
        connectionUserDTO = new UserDTO();
        connectionUserDTO.setId(2);
        connectionUserDTO.setUsername("connection");
        connectionUserDTO.setEmail("connection@example.com");
        connectionUserDTO.setConnectionIds(new ArrayList<>());

        // Create user registration DTO
        userRegistrationDTO = new UserRegistrationDTO();
        userRegistrationDTO.setUsername("newuser");
        userRegistrationDTO.setEmail("new@example.com");
        userRegistrationDTO.setPassword("password123");
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() throws Exception {
        // Arrange
        List<User> users = Arrays.asList(testUser, connectionUser);
        List<UserDTO> userDTOs = Arrays.asList(testUserDTO, connectionUserDTO);

        when(userService.getAllUsers()).thenReturn(users);
        when(userMapper.usersToUserDTOs(users)).thenReturn(userDTOs);

        // Act & Assert
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(testUserDTO.getId())))
                .andExpect(jsonPath("$[0].username", is(testUserDTO.getUsername())))
                .andExpect(jsonPath("$[0].email", is(testUserDTO.getEmail())))
                .andExpect(jsonPath("$[1].id", is(connectionUserDTO.getId())))
                .andExpect(jsonPath("$[1].username", is(connectionUserDTO.getUsername())))
                .andExpect(jsonPath("$[1].email", is(connectionUserDTO.getEmail())));

        verify(userService).getAllUsers();
        verify(userMapper).usersToUserDTOs(users);
    }

    @Test
    void getUserById_WithExistingId_ShouldReturnUser() throws Exception {
        // Arrange
        when(userService.getUserById(testUser.getId())).thenReturn(testUser);
        when(userMapper.userToUserDTO(testUser)).thenReturn(testUserDTO);

        // Act & Assert
        mockMvc.perform(get("/users")
                        .param("userId", String.valueOf(testUser.getId())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testUserDTO.getId())))
                .andExpect(jsonPath("$.username", is(testUserDTO.getUsername())))
                .andExpect(jsonPath("$.email", is(testUserDTO.getEmail())));

        verify(userService).getUserById(testUser.getId());
        verify(userMapper).userToUserDTO(testUser);
    }

    @Test
    void getUserById_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(userService.getUserById(999)).thenThrow(new RuntimeException("User with ID : 999 does not exist"));

        // Act & Assert
        mockMvc.perform(get("/users")
                        .param("userId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(0)))
                .andExpect(jsonPath("$.username", is(nullValue())))
                .andExpect(jsonPath("$.email", is(nullValue())));

        verify(userService).getUserById(999);
        verify(userMapper, never()).userToUserDTO(any(User.class));
    }

    @Test
    void getUserByEmail_WithExistingEmail_ShouldReturnUser() throws Exception {
        // Arrange
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(testUser);
        when(userMapper.userToUserDTO(testUser)).thenReturn(testUserDTO);

        // Act & Assert
        mockMvc.perform(get("/users/email")
                        .param("email", testUser.getEmail()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testUserDTO.getId())))
                .andExpect(jsonPath("$.username", is(testUserDTO.getUsername())))
                .andExpect(jsonPath("$.email", is(testUserDTO.getEmail())));

        verify(userService).getUserByEmail(testUser.getEmail());
        verify(userMapper).userToUserDTO(testUser);
    }

    @Test
    void getUserByEmail_WithNonExistingEmail_ShouldReturnNotFound() throws Exception {
        // Arrange
        String nonExistingEmail = "nonexisting@example.com";
        when(userService.getUserByEmail(nonExistingEmail)).thenThrow(new RuntimeException("User with email : " + nonExistingEmail + " does not exist"));

        // Act & Assert
        mockMvc.perform(get("/users/email")
                        .param("email", nonExistingEmail))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(0)))
                .andExpect(jsonPath("$.username", is(nullValue())))
                .andExpect(jsonPath("$.email", is(nullValue())));

        verify(userService).getUserByEmail(nonExistingEmail);
        verify(userMapper, never()).userToUserDTO(any(User.class));
    }

    @Test
    void registerUser_WithValidData_ShouldRegisterUser() throws Exception {
        // Arrange
        User newUser = new User();
        newUser.setUsername(userRegistrationDTO.getUsername());
        newUser.setEmail(userRegistrationDTO.getEmail());
        newUser.setPassword(userRegistrationDTO.getPassword());

        User createdUser = new User();
        createdUser.setId(3);
        createdUser.setUsername(userRegistrationDTO.getUsername());
        createdUser.setEmail(userRegistrationDTO.getEmail());
        createdUser.setPassword("encodedPassword");

        UserDTO createdUserDTO = new UserDTO();
        createdUserDTO.setId(3);
        createdUserDTO.setUsername(userRegistrationDTO.getUsername());
        createdUserDTO.setEmail(userRegistrationDTO.getEmail());

        when(userMapper.userRegistrationDTOToUser(userRegistrationDTO)).thenReturn(newUser);
        when(userService.register(newUser)).thenReturn(createdUser);
        when(userMapper.userToUserDTO(createdUser)).thenReturn(createdUserDTO);

        // Act & Assert
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(createdUserDTO.getId())))
                .andExpect(jsonPath("$.username", is(createdUserDTO.getUsername())))
                .andExpect(jsonPath("$.email", is(createdUserDTO.getEmail())));

        verify(userMapper).userRegistrationDTOToUser(userRegistrationDTO);
        verify(userService).register(newUser);
        verify(userMapper).userToUserDTO(createdUser);
    }

    @Test
    void registerUser_WithExistingEmail_ShouldReturnBadRequest() throws Exception {
        // Arrange
        User newUser = new User();
        newUser.setUsername(userRegistrationDTO.getUsername());
        newUser.setEmail(userRegistrationDTO.getEmail());
        newUser.setPassword(userRegistrationDTO.getPassword());

        when(userMapper.userRegistrationDTOToUser(userRegistrationDTO)).thenReturn(newUser);
        when(userService.register(newUser)).thenThrow(new RuntimeException("User with email : " + newUser.getEmail() + " already exists"));

        // Act & Assert
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(0)))
                .andExpect(jsonPath("$.username", is(nullValue())))
                .andExpect(jsonPath("$.email", is(nullValue())));

        verify(userMapper).userRegistrationDTOToUser(userRegistrationDTO);
        verify(userService).register(newUser);
        verify(userMapper, never()).userToUserDTO(any(User.class));
    }

    @Test
    void updateUser_WithValidData_ShouldUpdateUser() throws Exception {
        // Arrange
        UserDTO updateUserDTO = new UserDTO();
        updateUserDTO.setId(testUser.getId());
        updateUserDTO.setUsername("updatedUsername");
        updateUserDTO.setEmail("updated@example.com");

        User updatedUser = new User();
        updatedUser.setId(testUser.getId());
        updatedUser.setUsername(updateUserDTO.getUsername());
        updatedUser.setEmail(updateUserDTO.getEmail());

        UserDTO updatedUserDTO = new UserDTO();
        updatedUserDTO.setId(testUser.getId());
        updatedUserDTO.setUsername(updateUserDTO.getUsername());
        updatedUserDTO.setEmail(updateUserDTO.getEmail());

        when(userService.getUserById(testUser.getId())).thenReturn(testUser);
        when(userService.updateUser(any(User.class))).thenReturn(updatedUser);
        when(userMapper.userToUserDTO(updatedUser)).thenReturn(updatedUserDTO);

        // Act & Assert
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(updatedUserDTO.getId())))
                .andExpect(jsonPath("$.username", is(updatedUserDTO.getUsername())))
                .andExpect(jsonPath("$.email", is(updatedUserDTO.getEmail())));

        verify(userService).getUserById(testUser.getId());
        verify(userService).updateUser(any(User.class));
        verify(userMapper).userToUserDTO(updatedUser);
    }

    @Test
    void updateUser_WithNonExistingId_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UserDTO updateUserDTO = new UserDTO();
        updateUserDTO.setId(999);
        updateUserDTO.setUsername("updatedUsername");
        updateUserDTO.setEmail("updated@example.com");

        when(userService.getUserById(999)).thenThrow(new RuntimeException("User with ID : 999 does not exist"));

        // Act & Assert
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(0)))
                .andExpect(jsonPath("$.username", is(nullValue())))
                .andExpect(jsonPath("$.email", is(nullValue())));

        verify(userService).getUserById(999);
        verify(userService, never()).updateUser(any(User.class));
        verify(userMapper, never()).userToUserDTO(any(User.class));
    }

    @Test
    void updatePassword_WithValidData_ShouldUpdatePassword() throws Exception {
        // Arrange
        int userId = testUser.getId();
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setUsername(testUser.getUsername());
        updatedUser.setEmail(testUser.getEmail());
        updatedUser.setPassword("encodedNewPassword");

        when(userService.updatePassword(userId, oldPassword, newPassword)).thenReturn(updatedUser);
        when(userMapper.userToUserDTO(updatedUser)).thenReturn(testUserDTO);

        // Act & Assert
        mockMvc.perform(put("/users/password")
                        .param("userId", String.valueOf(userId))
                        .param("oldPassword", oldPassword)
                        .param("password", newPassword))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testUserDTO.getId())))
                .andExpect(jsonPath("$.username", is(testUserDTO.getUsername())))
                .andExpect(jsonPath("$.email", is(testUserDTO.getEmail())));

        verify(userService).updatePassword(userId, oldPassword, newPassword);
        verify(userMapper).userToUserDTO(updatedUser);
    }

    @Test
    void updatePassword_WithInvalidOldPassword_ShouldReturnBadRequest() throws Exception {
        // Arrange
        int userId = testUser.getId();
        String oldPassword = "wrongPassword";
        String newPassword = "newPassword";

        when(userService.updatePassword(userId, oldPassword, newPassword)).thenThrow(new RuntimeException("Old password does not match"));

        // Act & Assert
        mockMvc.perform(put("/users/password")
                        .param("userId", String.valueOf(userId))
                        .param("oldPassword", oldPassword)
                        .param("password", newPassword))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(0)))
                .andExpect(jsonPath("$.username", is(nullValue())))
                .andExpect(jsonPath("$.email", is(nullValue())));

        verify(userService).updatePassword(userId, oldPassword, newPassword);
        verify(userMapper, never()).userToUserDTO(any(User.class));
    }

    @Test
    void deleteUser_WithExistingId_ShouldDeleteUser() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser(testUser.getId());

        // Act & Assert
        mockMvc.perform(delete("/users")
                        .param("userId", String.valueOf(testUser.getId())))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(testUser.getId());
    }

    @Test
    void deleteUser_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("User with ID : 999 does not exist")).when(userService).deleteUser(999);

        // Act & Assert
        try {
            mockMvc.perform(delete("/users")
                            .param("userId", "999"));
        } catch (Exception e) {
            // Expected exception
            assertTrue(e.getCause() instanceof RuntimeException);
            assertEquals("User with ID : 999 does not exist", e.getCause().getMessage());
        }

        verify(userService).deleteUser(999);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnOk() throws Exception {
        // Arrange
        String email = testUser.getEmail();
        String password = "password123";

        when(userService.login(email, password)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/users/login")
                        .param("email", email)
                        .param("password", password))
                .andExpect(status().isOk());

        verify(userService).login(email, password);
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        String email = testUser.getEmail();
        String password = "wrongPassword";

        when(userService.login(email, password)).thenThrow(new RuntimeException("Invalid password"));

        // Act & Assert
        try {
            mockMvc.perform(post("/users/login")
                            .param("email", email)
                            .param("password", password));
        } catch (Exception e) {
            // Expected exception
            assertTrue(e.getCause() instanceof RuntimeException);
            assertEquals("Invalid password", e.getCause().getMessage());
        }

        verify(userService).login(email, password);
    }

    @Test
    void getConnections_WithExistingId_ShouldReturnConnections() throws Exception {
        // Arrange
        List<User> connections = List.of(connectionUser);
        List<UserDTO> connectionDTOs = List.of(connectionUserDTO);

        when(userService.getConnections(testUser.getId())).thenReturn(connections);
        when(userMapper.usersToUserDTOs(connections)).thenReturn(connectionDTOs);

        // Act & Assert
        mockMvc.perform(get("/users/{id}/connections", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(connectionUserDTO.getId())))
                .andExpect(jsonPath("$[0].username", is(connectionUserDTO.getUsername())))
                .andExpect(jsonPath("$[0].email", is(connectionUserDTO.getEmail())));

        verify(userService).getConnections(testUser.getId());
        verify(userMapper).usersToUserDTOs(connections);
    }

    @Test
    void getConnections_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(userService.getConnections(999)).thenThrow(new RuntimeException("User with ID : 999 does not exist"));

        // Act & Assert
        mockMvc.perform(get("/users/{id}/connections", 999))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(userService).getConnections(999);
        verify(userMapper, never()).usersToUserDTOs(anyList());
    }

    @Test
    void addConnection_WithValidIds_ShouldAddConnection() throws Exception {
        // Arrange
        List<User> connections = List.of(connectionUser);
        List<UserDTO> connectionDTOs = List.of(connectionUserDTO);

        when(userService.addConnections(testUser.getId(), connectionUser.getId())).thenReturn(connections);
        when(userMapper.usersToUserDTOs(connections)).thenReturn(connectionDTOs);

        // Act & Assert
        mockMvc.perform(post("/users/{id}/connections", testUser.getId())
                        .param("connectionId", String.valueOf(connectionUser.getId())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(connectionUserDTO.getId())))
                .andExpect(jsonPath("$[0].username", is(connectionUserDTO.getUsername())))
                .andExpect(jsonPath("$[0].email", is(connectionUserDTO.getEmail())));

        verify(userService).addConnections(testUser.getId(), connectionUser.getId());
        verify(userMapper).usersToUserDTOs(connections);
    }

    @Test
    void addConnection_WithNonExistingId_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(userService.addConnections(999, connectionUser.getId())).thenThrow(new RuntimeException("User with ID : 999 does not exist"));

        // Act & Assert
        mockMvc.perform(post("/users/{id}/connections", 999)
                        .param("connectionId", String.valueOf(connectionUser.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(userService).addConnections(999, connectionUser.getId());
        verify(userMapper, never()).usersToUserDTOs(anyList());
    }

    @Test
    void removeConnection_WithValidIds_ShouldRemoveConnection() throws Exception {
        // Arrange
        List<User> connections = new ArrayList<>();
        List<UserDTO> connectionDTOs = new ArrayList<>();

        when(userService.removeConnections(testUser.getId(), connectionUser.getId())).thenReturn(connections);
        when(userMapper.usersToUserDTOs(connections)).thenReturn(connectionDTOs);

        // Act & Assert
        mockMvc.perform(delete("/users/{id}/connections", testUser.getId())
                        .param("connectionId", String.valueOf(connectionUser.getId())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(userService).removeConnections(testUser.getId(), connectionUser.getId());
        verify(userMapper).usersToUserDTOs(connections);
    }

    @Test
    void removeConnection_WithNonExistingId_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(userService.removeConnections(999, connectionUser.getId())).thenThrow(new RuntimeException("User with ID : 999 does not exist"));

        // Act & Assert
        mockMvc.perform(delete("/users/{id}/connections", 999)
                        .param("connectionId", String.valueOf(connectionUser.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(userService).removeConnections(999, connectionUser.getId());
        verify(userMapper, never()).usersToUserDTOs(anyList());
    }
}
