package dev.nikkune.paymybuddy.dto;

import lombok.Data;

/**
 * Data Transfer Object for creating a new Transaction
 */
@Data
public class TransactionCreationDTO {
    private int senderId;
    private int receiverId;
    private String description;
    private double amount;
}