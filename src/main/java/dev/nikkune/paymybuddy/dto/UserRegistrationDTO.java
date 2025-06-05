package dev.nikkune.paymybuddy.dto;

import lombok.Data;

/**
 * Data Transfer Object for User registration
 */
@Data
public class UserRegistrationDTO {
    private String username;
    private String email;
    private String password;
}