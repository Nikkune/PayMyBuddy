package dev.nikkune.paymybuddy.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Data Transfer Object for creating a new Transaction
 */
@Data
public class TransactionCreationDTO {
    @NotNull(message = "Sender ID is required")
    private int senderId;

    @NotNull(message = "Receiver ID is required")
    private int receiverId;

    private String description;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private double amount;
}
