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

    @NotBlank(message = "Sender username is required")
    private String senderUsername;

    @NotBlank(message = "Receiver username is required")
    private String receiverUsername;

    private String description;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private double amount;
}
