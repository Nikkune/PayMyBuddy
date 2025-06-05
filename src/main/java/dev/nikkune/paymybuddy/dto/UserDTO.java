package dev.nikkune.paymybuddy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object for User entity
 */
@Data
public class UserDTO {
    @NotNull(message = "User ID is required")
    private int id;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    // Password is excluded for security reasons
    private List<Integer> connectionIds; // Only IDs of connections
}
