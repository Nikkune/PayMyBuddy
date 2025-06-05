package dev.nikkune.paymybuddy.mapper;

import dev.nikkune.paymybuddy.dto.UserDTO;
import dev.nikkune.paymybuddy.dto.UserRegistrationDTO;
import dev.nikkune.paymybuddy.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private UserMapper userMapper;
    private User testUser;
    private User connectionUser;

    @BeforeEach
    void setUp() {
        // Get the mapper instance
        userMapper = Mappers.getMapper(UserMapper.class);

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
        connectionUser.setPassword("password456");
        connectionUser.setConnections(new ArrayList<>());
    }

    @Test
    void userToUserDTO_ShouldMapUserToDTO() {
        // Arrange
        testUser.getConnections().add(connectionUser);

        // Act
        UserDTO userDTO = userMapper.userToUserDTO(testUser);

        // Assert
        assertNotNull(userDTO);
        assertEquals(testUser.getId(), userDTO.getId());
        assertEquals(testUser.getUsername(), userDTO.getUsername());
        assertEquals(testUser.getEmail(), userDTO.getEmail());

        // Check connection mapping
        assertNotNull(userDTO.getConnectionIds());
        assertEquals(1, userDTO.getConnectionIds().size());
        assertEquals(connectionUser.getId(), userDTO.getConnectionIds().getFirst());
    }

    @Test
    void userToUserDTO_WithNullConnections_ShouldMapUserToDTO() {
        // Arrange
        testUser.setConnections(null);

        // Act
        UserDTO userDTO = userMapper.userToUserDTO(testUser);

        // Assert
        assertNotNull(userDTO);
        assertEquals(testUser.getId(), userDTO.getId());
        assertEquals(testUser.getUsername(), userDTO.getUsername());
        assertEquals(testUser.getEmail(), userDTO.getEmail());
        assertNull(userDTO.getConnectionIds());
    }

    @Test
    void usersToUserDTOs_ShouldMapUserListToDTOList() {
        // Arrange
        List<User> users = Arrays.asList(testUser, connectionUser);

        // Act
        List<UserDTO> userDTOs = userMapper.usersToUserDTOs(users);

        // Assert
        assertNotNull(userDTOs);
        assertEquals(2, userDTOs.size());

        // Check first user
        assertEquals(testUser.getId(), userDTOs.getFirst().getId());
        assertEquals(testUser.getUsername(), userDTOs.getFirst().getUsername());
        assertEquals(testUser.getEmail(), userDTOs.getFirst().getEmail());

        // Check the second user
        assertEquals(connectionUser.getId(), userDTOs.get(1).getId());
        assertEquals(connectionUser.getUsername(), userDTOs.get(1).getUsername());
        assertEquals(connectionUser.getEmail(), userDTOs.get(1).getEmail());
    }

    @Test
    void userRegistrationDTOToUser_ShouldMapDTOToUser() {
        // Arrange
        UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("newuser");
        registrationDTO.setEmail("new@example.com");
        registrationDTO.setPassword("newpassword");

        // Act
        User user = userMapper.userRegistrationDTOToUser(registrationDTO);

        // Assert
        assertNotNull(user);
        assertEquals(registrationDTO.getUsername(), user.getUsername());
        assertEquals(registrationDTO.getEmail(), user.getEmail());
        assertEquals(registrationDTO.getPassword(), user.getPassword());
    }

    @Test
    void mapConnectionsToIds_WithConnections_ShouldReturnIdList() {
        // Arrange
        testUser.getConnections().add(connectionUser);

        // Act
        List<Integer> connectionIds = userMapper.mapConnectionsToIds(testUser);

        // Assert
        assertNotNull(connectionIds);
        assertEquals(1, connectionIds.size());
        assertEquals(connectionUser.getId(), connectionIds.getFirst());
    }

    @Test
    void mapConnectionsToIds_WithNullConnections_ShouldReturnNull() {
        // Arrange
        testUser.setConnections(null);

        // Act
        List<Integer> connectionIds = userMapper.mapConnectionsToIds(testUser);

        // Assert
        assertNull(connectionIds);
    }

    @Test
    void mapConnectionsToIds_WithEmptyConnections_ShouldReturnEmptyList() {
        // Arrange - the connection list is already empty from setUp

        // Act
        List<Integer> connectionIds = userMapper.mapConnectionsToIds(testUser);

        // Assert
        assertNotNull(connectionIds);
        assertTrue(connectionIds.isEmpty());
    }
}
