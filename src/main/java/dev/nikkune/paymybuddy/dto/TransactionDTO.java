package dev.nikkune.paymybuddy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Data Transfer Object for Transaction entity
 */
@Data
public class TransactionDTO {
    @NotNull(message = "Transaction ID is required")
    private int id;

    @NotNull(message = "Sender ID is required")
    private int senderId;

    @NotBlank(message = "Sender username is required")
    private String senderUsername;

    @NotNull(message = "Receiver ID is required")
    private int receiverId;

    @NotBlank(message = "Receiver username is required")
    private String receiverUsername;

    private String description;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private double amount;
}
