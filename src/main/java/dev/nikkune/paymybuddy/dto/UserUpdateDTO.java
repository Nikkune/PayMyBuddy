package dev.nikkune.paymybuddy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Data Transfer Object for updating an existing User entity.
 */
@Data
public class UserUpdateDTO {
    @NotNull(message = "User ID is required")
    private int id;
    private String username;
    private String email;
    private String password;
}
