package dev.nikkune.paymybuddy.dto;

import lombok.Data;

/**
 * Data Transfer Object for Transaction entity
 */
@Data
public class TransactionDTO {
    private int id;
    private int senderId;
    private String senderUsername;
    private int receiverId;
    private String receiverUsername;
    private String description;
    private double amount;
}