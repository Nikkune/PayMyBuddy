package dev.nikkune.paymybuddy.dto;

import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object for User entity
 */
@Data
public class UserDTO {
    private int id;
    private String username;
    private String email;
    // Password is excluded for security reasons
    private List<Integer> connectionIds; // Only IDs of connections
}