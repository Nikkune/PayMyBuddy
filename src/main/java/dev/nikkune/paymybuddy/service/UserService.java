package dev.nikkune.paymybuddy.service;

import dev.nikkune.paymybuddy.model.User;
import dev.nikkune.paymybuddy.repository.UserRepository;
import dev.nikkune.paymybuddy.utils.PasswordUtil;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class responsible for user management and related operations.
 * This class manages the users in the system, including registration, login, updates,
 * and connections between users. It uses the {@link UserRepository} to perform database operations.
 * <p>
 * All methods are transactional, ensuring the integrity and consistency
 * of the operations performed on user data.
 */
@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;

    /**
     * Constructs an instance of {@code UserService} with the specified {@code UserRepository}.
     *
     * @param userRepository the {@code UserRepository} used for database operations related to users
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieves a list of all users in the system.
     *
     * @return a list of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Checks if a user with the given ID exists in the database.
     *
     * @param id the ID of the user to be checked
     * @return the User object if found; otherwise, null
     */
    public User exists(int id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * Retrieves a user by their ID and verifies that the user exists in the system.
     * If the user does not exist, a {@code RuntimeException} is thrown.
     *
     * @param id the ID of the user to retrieve
     * @return the {@code User} object corresponding to the given ID
     * @throws RuntimeException if the user with the specified ID does not exist
     */
    public User requiredUser(int id) throws RuntimeException {
        User existingUser = exists(id);
        if (existingUser == null)
            throw new RuntimeException("User with ID : " + id + " does not exist");

        return existingUser;
    }

    /**
     * Retrieves a User object by its unique identifier. If the user does not exist,
     * a RuntimeException is thrown.
     *
     * @param id the unique identifier of the user to be retrieved
     * @return the User object associated with the given ID
     * @throws RuntimeException if no user is found with the provided ID
     */
    public User getUserById(int id) throws RuntimeException {
        return requiredUser(id);
    }

    /**
     * Retrieves a user by their email address.
     *
     * @param email the email address of the user to be retrieved
     * @return the user associated with the given email
     * @throws RuntimeException if no user exists with the specified email
     */
    public User getUserByEmail(String email) throws RuntimeException {
        User existingUser = userRepository.findByEmail(email).orElse(null);
        if (existingUser != null) {
            return existingUser;
        } else {
            throw new RuntimeException("User with email : " + email + " does not exist");
        }
    }

    /**
     * Registers a new user in the system. This method ensures that the user's email and username are unique,
     * encodes the user's password before saving, and persists the user in the repository.
     *
     * @param user the User object containing the registration details such as email, username, and password
     * @return the registered User object after being persisted
     * @throws RuntimeException if the email or username is already in use
     */
    public User register(User user) throws RuntimeException {
        // Check if the email is already taken
        User existingUser = userRepository.findByEmail(user.getEmail()).orElse(null);
        if (existingUser != null)
            throw new RuntimeException("User with email : " + user.getEmail() + " already exists");
        // Check if the username is already taken
        existingUser = userRepository.findByUsername(user.getUsername()).orElse(null);
        if (existingUser != null)
            throw new RuntimeException("User with username : " + user.getUsername() + " already exists");
        String encodedPassword = PasswordUtil.encodePassword(user.getPassword());
        user.setPassword(encodedPassword);
        return userRepository.save(user);
    }

    /**
     * Updates the details of an existing user with the provided non-null fields.
     * The method replaces the username and email of the existing user if those fields
     * in the provided user object are not null. It retrieves the existing user from
     * the database before performing the update.
     *
     * @param user the user object containing updated information. It must include a valid ID
     *             of the user to be updated. Fields that are null will not be updated.
     * @return the updated User object saved in the database.
     * @throws RuntimeException if the user with the given ID does not exist.
     */
    public User updateUser(User user) throws RuntimeException {
        User existingUser = requiredUser(user.getId());
        // Replace non-null fields
        if (user.getUsername() != null)
            existingUser.setUsername(user.getUsername());
        if (user.getEmail() != null)
            existingUser.setEmail(user.getEmail());

        return userRepository.save(existingUser);
    }

    /**
     * Updates the password of a user after validating the old password.
     *
     * @param userId the ID of the user whose password is being updated
     * @param oldPassword the current password that needs to be validated
     * @param newPassword the new password to be set for the user
     * @return the updated {@code User} object with the new password
     * @throws RuntimeException if the user does not exist, or if the old password does not match the current password
     */
    public User updatePassword(int userId, String oldPassword, String newPassword) throws RuntimeException {
        User existingUser = requiredUser(userId);

        if (!PasswordUtil.matches(oldPassword, existingUser.getPassword()))
            throw new RuntimeException("Old password does not match");

        String encodedPassword = PasswordUtil.encodePassword(newPassword);
        existingUser.setPassword(encodedPassword);
        return userRepository.save(existingUser);
    }

    /**
     * Deletes a user with the specified ID if the user exists.
     *
     * @param userId the ID of the user to be deleted
     * @throws RuntimeException if no user with the specified ID exists
     */
    public void deleteUser(int userId) throws RuntimeException {
        User existingUser = requiredUser(userId);
        userRepository.delete(existingUser);
    }

    /**
     * Authenticates a user by validating their email and password.
     *
     * @param email the email of the user attempting to log in
     * @param password the password of the user attempting to log in
     * @return true if the login is successful
     * @throws RuntimeException if the user does not exist or the provided password is invalid
     */
    public boolean login(String email, String password) throws RuntimeException {
        User existingUser = userRepository.findByEmail(email).orElse(null);
        if (existingUser == null)
            throw new RuntimeException("User with email : " + email + " does not exist");
        if (!PasswordUtil.matches(password, existingUser.getPassword()))
            throw new RuntimeException("Invalid password");
        return true;
    }

    /**
     * Retrieves the list of connections for a specific user.
     *
     * @param userId the ID of the user whose connections are to be retrieved
     * @return a list of {@code User} objects representing the connections of the specified user
     * @throws RuntimeException if the user with the given ID does not exist
     */
    public List<User> getConnections(int userId) throws RuntimeException {
        User existingUser = requiredUser(userId);
        return existingUser.getConnections();
    }

    /**
     * Adds a connection between two users by their IDs.
     * The method retrieves both users and creates a connection if it does not already exist.
     * If the connection already exists, an exception is thrown.
     *
     * @param userId the ID of the user initiating the connection
     * @param connectionId the ID of the user to whom the connection is being made
     * @return a list of connections for the user initiating the connection
     * @throws RuntimeException if any of the users do not exist or if the connection already exists
     */
    public List<User> addConnections(int userId, int connectionId) throws RuntimeException {
        User existingUser = requiredUser(userId);
        User connection = requiredUser(connectionId);
        if (existingUser.getConnections().contains(connection))
            throw new RuntimeException("User is already connected to this user");
        existingUser.getConnections().add(connection);
        return existingUser.getConnections();
    }

    /**
     * Removes a connection between two users based on their user IDs.
     * If the connection does not exist, an exception is thrown.
     *
     * @param userId the ID of the user performing the removal
     * @param connectionId the ID of the user to be removed from the connections
     * @return a list of remaining connections for the user after the removal
     * @throws RuntimeException if either user does not exist, or there is no existing connection between them
     */
    public List<User> removeConnections(int userId, int connectionId) throws RuntimeException {
        User existingUser = requiredUser(userId);
        User connection = requiredUser(connectionId);
        if (!existingUser.getConnections().contains(connection))
            throw new RuntimeException("User is not connected to this user");
        existingUser.getConnections().remove(connection);
        return existingUser.getConnections();
    }
}
