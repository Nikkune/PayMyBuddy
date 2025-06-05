package dev.nikkune.paymybuddy.mapper;

import dev.nikkune.paymybuddy.dto.UserDTO;
import dev.nikkune.paymybuddy.dto.UserRegistrationDTO;
import dev.nikkune.paymybuddy.model.User;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for the User entity and its DTOs
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Converts a User entity to a UserDTO
     * @param user the User entity
     * @return the UserDTO
     */
    @Mapping(target = "connectionIds", expression = "java(mapConnectionsToIds(user))")
    UserDTO userToUserDTO(User user);

    /**
     * Converts a list of User entities to a list of UserDTOs
     * @param users the list of User entities
     * @return the list of UserDTOs
     */
    List<UserDTO> usersToUserDTOs(List<User> users);

    /**
     * Converts a UserRegistrationDTO to a User entity
     * @param userRegistrationDto the UserRegistrationDto
     * @return the User entity
     */
    User userRegistrationDTOToUser(UserRegistrationDTO userRegistrationDto);

    /**
     * Maps the connections list to a list of IDs
     * @param user the User entity
     * @return a list of connection IDs
     */
    default List<Integer> mapConnectionsToIds(User user) {
        if (user.getConnections() == null) {
            return null;
        }
        return user.getConnections().stream()
                .map(User::getId)
                .collect(Collectors.toList());
    }
}