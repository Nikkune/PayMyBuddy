package dev.nikkune.paymybuddy.controller;

import dev.nikkune.paymybuddy.dto.EmailDTO;
import dev.nikkune.paymybuddy.dto.UserDTO;
import dev.nikkune.paymybuddy.dto.UserUpdateDTO;
import dev.nikkune.paymybuddy.mapper.UserMapper;
import dev.nikkune.paymybuddy.model.User;
import dev.nikkune.paymybuddy.service.IUserService;
import dev.nikkune.paymybuddy.utils.Response;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
     *
     * @param userService the user service
     * @param userMapper  the user mapper
     */
    public UserController(IUserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    /**
     * Update a user
     *
     * @param userUpdateDTO the user data to update
     * @return the updated user
     */
    @PutMapping
    public ResponseEntity<Response> updateUser(@RequestBody @Valid UserUpdateDTO userUpdateDTO) {
        logger.debug("Received request to update user with ID: {}", userUpdateDTO.getId());
        User user = userMapper.userUpdateDTOToUser(userUpdateDTO);
        User updatedUser = userService.updateUser(user);
        UserDTO updatedUserDTO = userMapper.userToUserDTO(updatedUser);

        Response responseBody = new Response("User updated successfully", true)
                .add("data", updatedUserDTO);

        logger.info("User updated successfully with ID: {}", updatedUserDTO.getId());
        return ResponseEntity.ok(responseBody);
    }

    /**
     * Get a user's connections
     *
     * @param id the user ID
     * @return the user's connections
     */
    @GetMapping("/{id}/connections")
    public ResponseEntity<List<UserDTO>> getConnections(@PathVariable @Valid Integer id) {
        logger.debug("Received request to get connections for user with ID: {}", id);
        List<User> connections = userService.getConnections(id);
        List<UserDTO> connectionDTOs = userMapper.usersToUserDTOs(connections);
        logger.info("Returning {} connections for user with ID: {}", connectionDTOs.size(), id);
        return ResponseEntity.ok(connectionDTOs);
    }

    /**
     * Add a connection to a user
     *
     * @param id       the user ID
     * @param emailDTO the email
     * @return the user's updated connections
     */
    @PostMapping("/{id}/connections")
    public ResponseEntity<Response> addConnection(
            @PathVariable @Valid Integer id,
            @RequestBody @Valid EmailDTO emailDTO) {
        logger.info("Received request to add connection {} for user with ID: {}", emailDTO.getEmail(), id);
        List<User> connections = userService.addConnection(id, emailDTO.getEmail());
        List<UserDTO> connectionDTOs = userMapper.usersToUserDTOs(connections);

        Response responseBody = new Response("Connection " + emailDTO.getEmail() + " added successfully", true)
                .add("data", connectionDTOs);

        logger.info("Connection {} added successfully for user with ID: {}", emailDTO.getEmail(), id);
        return ResponseEntity.ok(responseBody);
    }
}
