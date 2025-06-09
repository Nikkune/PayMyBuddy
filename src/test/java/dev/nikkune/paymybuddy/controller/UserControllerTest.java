package dev.nikkune.paymybuddy.controller;

import dev.nikkune.paymybuddy.dto.EmailDTO;
import dev.nikkune.paymybuddy.dto.UserDTO;
import dev.nikkune.paymybuddy.dto.UserUpdateDTO;
import dev.nikkune.paymybuddy.mapper.UserMapper;
import dev.nikkune.paymybuddy.model.User;
import dev.nikkune.paymybuddy.service.IUserService;
import dev.nikkune.paymybuddy.utils.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private IUserService userService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private User connectionUser;
    private UserDTO userDTO;
    private UserUpdateDTO userUpdateDTO;
    private EmailDTO emailDTO;
    private List<User> connections;
    private List<UserDTO> connectionDTOs;

    @BeforeEach
    void setUp() {
        // Create a test user
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setBalance(500.0);
        testUser.setConnections(new ArrayList<>());

        // Create a connection user
        connectionUser = new User();
        connectionUser.setId(2);
        connectionUser.setUsername("connection");
        connectionUser.setEmail("connection@example.com");
        connectionUser.setPassword("encodedPassword");
        connectionUser.setBalance(500.0);
        connectionUser.setConnections(new ArrayList<>());

        // Create user DTO
        userDTO = new UserDTO();
        userDTO.setId(testUser.getId());
        userDTO.setUsername(testUser.getUsername());
        userDTO.setEmail(testUser.getEmail());

        // Create user update DTO
        userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setId(testUser.getId());
        userUpdateDTO.setUsername("updatedUsername");
        userUpdateDTO.setEmail("updated@example.com");

        // Create email DTO
        emailDTO = new EmailDTO();
        emailDTO.setEmail(connectionUser.getEmail());

        // Create a connection list
        connections = new ArrayList<>();
        connections.add(connectionUser);

        // Create connection DTOs list
        connectionDTOs = new ArrayList<>();
        UserDTO connectionDTO = new UserDTO();
        connectionDTO.setId(connectionUser.getId());
        connectionDTO.setUsername(connectionUser.getUsername());
        connectionDTO.setEmail(connectionUser.getEmail());
        connectionDTOs.add(connectionDTO);
    }

    @Test
    void getUserByEmail_ShouldReturnUser() {
        // Arrange
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(testUser);
        when(userMapper.userToUserDTO(testUser)).thenReturn(userDTO);

        // Act
        ResponseEntity<UserDTO> responseEntity = userController.getUserByEmail(testUser.getEmail());

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(userDTO, responseEntity.getBody());
        assertEquals(testUser.getUsername(), responseEntity.getBody().getUsername());
        assertEquals(testUser.getEmail(), responseEntity.getBody().getEmail());
        assertEquals(testUser.getId(), responseEntity.getBody().getId());

        // Verify interactions
        verify(userService).getUserByEmail(testUser.getEmail());
        verify(userMapper).userToUserDTO(testUser);
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setId(testUser.getId());
        updatedUser.setUsername(userUpdateDTO.getUsername());
        updatedUser.setEmail(userUpdateDTO.getEmail());
        updatedUser.setBalance(testUser.getBalance());

        when(userMapper.userUpdateDTOToUser(userUpdateDTO)).thenReturn(updatedUser);
        when(userService.updateUser(updatedUser)).thenReturn(updatedUser);
        when(userMapper.userToUserDTO(updatedUser)).thenReturn(userDTO);

        // Act
        ResponseEntity<Response> responseEntity = userController.updateUser(userUpdateDTO);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof Response);
        Response responseBody = responseEntity.getBody();
        assertTrue((Boolean) responseBody.get("success"));
        assertEquals("User updated successfully", responseBody.get("message"));
        assertEquals(userDTO, responseBody.get("data"));

        // Verify interactions
        verify(userMapper).userUpdateDTOToUser(userUpdateDTO);
        verify(userService).updateUser(updatedUser);
        verify(userMapper).userToUserDTO(updatedUser);
    }

    @Test
    void getConnections_ShouldReturnConnections() {
        // Arrange
        when(userService.getConnections(testUser.getId())).thenReturn(connections);
        when(userMapper.usersToUserDTOs(connections)).thenReturn(connectionDTOs);

        // Act
        ResponseEntity<List<UserDTO>> responseEntity = userController.getConnections(testUser.getId());

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(connectionDTOs, responseEntity.getBody());
        assertEquals(1, responseEntity.getBody().size());
        assertEquals(connectionUser.getUsername(), responseEntity.getBody().get(0).getUsername());
        assertEquals(connectionUser.getEmail(), responseEntity.getBody().get(0).getEmail());
        assertEquals(connectionUser.getId(), responseEntity.getBody().get(0).getId());

        // Verify interactions
        verify(userService).getConnections(testUser.getId());
        verify(userMapper).usersToUserDTOs(connections);
    }

    @Test
    void addConnection_ShouldReturnUpdatedConnections() {
        // Arrange
        when(userService.addConnection(testUser.getId(), connectionUser.getEmail())).thenReturn(connections);
        when(userMapper.usersToUserDTOs(connections)).thenReturn(connectionDTOs);

        // Act
        ResponseEntity<Response> responseEntity = userController.addConnection(testUser.getId(), emailDTO);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof Response);
        Response responseBody = responseEntity.getBody();
        assertTrue((Boolean) responseBody.get("success"));
        assertEquals("Connection " + connectionUser.getEmail() + " added successfully", responseBody.get("message"));
        assertEquals(connectionDTOs, responseBody.get("data"));

        // Verify interactions
        verify(userService).addConnection(testUser.getId(), connectionUser.getEmail());
        verify(userMapper).usersToUserDTOs(connections);
    }

    @Test
    void addConnection_WithExistingConnection_ShouldHandleError() {
        // Arrange
        when(userService.addConnection(testUser.getId(), connectionUser.getEmail()))
                .thenThrow(new RuntimeException("User is already connected to this user"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userController.addConnection(testUser.getId(), emailDTO));

        // Verify interactions
        verify(userService).addConnection(testUser.getId(), connectionUser.getEmail());
        verify(userMapper, never()).usersToUserDTOs(any());
    }
}
