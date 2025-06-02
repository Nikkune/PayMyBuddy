package dev.nikkune.paymybuddy.repository;

import dev.nikkune.paymybuddy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    /**
     * Retrieves a user by their username.
     *
     * @param username the username of the user to be retrieved
     * @return an Optional containing the User object if found, or an empty Optional if no user exists with the given username
     */
    Optional<User> findByUsername(String username);

    /**
     * Retrieves a user by their email.
     *
     * @param email the email of the user to be retrieved
     * @return an Optional containing the User object if found, or an empty Optional if no user exists with the given email
     */
    Optional<User> findByEmail(String email);
}
