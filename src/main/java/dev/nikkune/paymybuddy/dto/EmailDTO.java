package dev.nikkune.paymybuddy.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class EmailDTO {
    @Email
    private String email;
}
