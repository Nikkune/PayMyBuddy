package dev.nikkune.paymybuddy.controller;

import dev.nikkune.paymybuddy.dto.UserDTO;
import dev.nikkune.paymybuddy.dto.UserRegistrationDTO;
import dev.nikkune.paymybuddy.mapper.UserMapper;
import dev.nikkune.paymybuddy.model.User;
import dev.nikkune.paymybuddy.service.IUserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling user-related operations
 */
@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final IUserService userService;
    private final UserMapper userMapper;

    /**
     * Constructor for UserController
     * @param userService the user service
     * @param userMapper the user mapper
     */
    public UserController(IUserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    /**
     * Get all users
     * @return list of all users
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        logger.debug("Received request to get all users");
        List<User> users = userService.getAllUsers();
        List<UserDTO> userDTOs = userMapper.usersToUserDTOs(users);
        logger.info("Returning {} users", userDTOs.size());
        return ResponseEntity.ok(userDTOs);
    }

    /**
     * Get user by ID
     * @param userId the user ID
     * @return the user with the given ID
     */
    @GetMapping(params = "userId")
    public ResponseEntity<UserDTO> getUserById(@RequestParam @Valid Integer userId) {
        logger.debug("Received request to get user with ID: {}", userId);
        try {
            User user = userService.getUserById(userId);
            UserDTO userDTO = userMapper.userToUserDTO(user);
            logger.info("Returning user with ID: {}", userId);
            return ResponseEntity.ok(userDTO);
        } catch (RuntimeException e) {
            logger.error("Error getting user with ID: {}", userId, e);
            return new ResponseEntity<>(new UserDTO(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Get user by email
     * @param email the user email
     * @return the user with the given email
     */
    @GetMapping("/email")
    public ResponseEntity<UserDTO> getUserByEmail(@RequestParam @Valid String email) {
        logger.debug("Received request to get user with email: {}", email);
        try {
            User user = userService.getUserByEmail(email);
            UserDTO userDTO = userMapper.userToUserDTO(user);
            logger.info("Returning user with email: {}", email);
            return ResponseEntity.ok(userDTO);
        } catch (RuntimeException e) {
            logger.error("Error getting user with email: {}", email, e);
            return new ResponseEntity<>(new UserDTO(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Register a new user
     * @param userRegistrationDTO the user registration data
     * @return the created user
     */
    @PostMapping
    public ResponseEntity<UserDTO> registerUser(@RequestBody @Valid UserRegistrationDTO userRegistrationDTO) {
        logger.debug("Received request to register user with username: {}", userRegistrationDTO.getUsername());
        try {
            User user = userMapper.userRegistrationDTOToUser(userRegistrationDTO);
            User createdUser = userService.register(user);
            UserDTO userDTO = userMapper.userToUserDTO(createdUser);
            logger.info("User registered successfully with ID: {}", userDTO.getId());
            return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            logger.error("Error registering user with username: {}", userRegistrationDTO.getUsername(), e);
            return new ResponseEntity<>(new UserDTO(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Update a user
     * @param userDTO the user data to update
     * @return the updated user
     */
    @PutMapping
    public ResponseEntity<UserDTO> updateUser(@RequestBody @Valid UserDTO userDTO) {
        logger.debug("Received request to update user with ID: {}", userDTO.getId());
        try {
            User user = userService.getUserById(userDTO.getId());
            user.setUsername(userDTO.getUsername());
            user.setEmail(userDTO.getEmail());
            User updatedUser = userService.updateUser(user);
            UserDTO updatedUserDTO = userMapper.userToUserDTO(updatedUser);
            logger.info("User updated successfully with ID: {}", updatedUserDTO.getId());
            return ResponseEntity.ok(updatedUserDTO);
        } catch (RuntimeException e) {
            logger.error("Error updating user with ID: {}", userDTO.getId(), e);
            return new ResponseEntity<>(new UserDTO(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Update a user's password
     * @param userId the user ID
     * @param oldPassword the old password
     * @param password the new password
     * @return the updated user
     */
    @PutMapping("/password")
    public ResponseEntity<UserDTO> updatePassword(
            @RequestParam @Valid Integer userId,
            @RequestParam @Valid String oldPassword,
            @RequestParam @Valid String password) {
        logger.debug("Received request to update password for user with ID: {}", userId);
        try {
            User updatedUser = userService.updatePassword(userId, oldPassword, password);
            UserDTO userDTO = userMapper.userToUserDTO(updatedUser);
            logger.info("Password updated successfully for user with ID: {}", userId);
            return ResponseEntity.ok(userDTO);
        } catch (RuntimeException e) {
            logger.error("Error updating password for user with ID: {}", userId, e);
            return new ResponseEntity<>(new UserDTO(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Delete a user
     * @param userId the user ID
     * @return no content
     */
    @DeleteMapping(params = "userId")
    public ResponseEntity<Object> deleteUser(@RequestParam @Valid Integer userId) {
        logger.debug("Received request to delete user with ID: {}", userId);
        try {
            userService.deleteUser(userId);
            logger.info("User deleted successfully with ID: {}", userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Error deleting user with ID: {}", userId, e);
            return new ResponseEntity<>(new Object(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Login a user
     * @param email the user email
     * @param password the user password
     * @return ok if login successful, unauthorized if not
     */
    @PostMapping("/login")
    public ResponseEntity<Object> login(
            @RequestParam @Valid String email,
            @RequestParam @Valid String password) {
        logger.debug("Received login request for user with email: {}", email);
        boolean loginSuccessful = userService.login(email, password);
        if (loginSuccessful) {
            logger.info("Login successful for user with email: {}", email);
            return ResponseEntity.ok().build();
        } else {
            logger.error("Login failed for user with email: {}", email);
            return new ResponseEntity<>(new Object(), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Get a user's connections
     * @param id the user ID
     * @return the user's connections
     */
    @GetMapping("/{id}/connections")
    public ResponseEntity<List<UserDTO>> getConnections(@PathVariable @Valid Integer id) {
        logger.debug("Received request to get connections for user with ID: {}", id);
        try {
            List<User> connections = userService.getConnections(id);
            List<UserDTO> connectionDTOs = userMapper.usersToUserDTOs(connections);
            logger.info("Returning {} connections for user with ID: {}", connectionDTOs.size(), id);
            return ResponseEntity.ok(connectionDTOs);
        } catch (RuntimeException e) {
            logger.error("Error getting connections for user with ID: {}", id, e);
            return new ResponseEntity<>(List.of(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Add a connection to a user
     * @param id the user ID
     * @param connectionId the connection ID
     * @return the user's updated connections
     */
    @PostMapping("/{id}/connections")
    public ResponseEntity<List<UserDTO>> addConnection(
            @PathVariable @Valid Integer id,
            @RequestParam @Valid Integer connectionId) {
        logger.debug("Received request to add connection {} for user with ID: {}", connectionId, id);
        try {
            List<User> connections = userService.addConnection(id, connectionId);
            List<UserDTO> connectionDTOs = userMapper.usersToUserDTOs(connections);
            logger.info("Connection {} added successfully for user with ID: {}", connectionId, id);
            return ResponseEntity.ok(connectionDTOs);
        } catch (RuntimeException e) {
            logger.error("Error adding connection {} for user with ID: {}", connectionId, id, e);
            return new ResponseEntity<>(List.of(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Remove a connection from a user
     * @param id the user ID
     * @param connectionId the connection ID
     * @return the user's updated connections
     */
    @DeleteMapping("/{id}/connections")
    public ResponseEntity<List<UserDTO>> removeConnection(
            @PathVariable @Valid Integer id,
            @RequestParam @Valid Integer connectionId) {
        logger.debug("Received request to remove connection {} for user with ID: {}", connectionId, id);
        try {
            List<User> connections = userService.removeConnection(id, connectionId);
            List<UserDTO> connectionDTOs = userMapper.usersToUserDTOs(connections);
            logger.info("Connection {} removed successfully for user with ID: {}", connectionId, id);
            return ResponseEntity.ok(connectionDTOs);
        } catch (RuntimeException e) {
            logger.error("Error removing connection {} for user with ID: {}", connectionId, id, e);
            return new ResponseEntity<>(List.of(), HttpStatus.BAD_REQUEST);
        }
    }
}
