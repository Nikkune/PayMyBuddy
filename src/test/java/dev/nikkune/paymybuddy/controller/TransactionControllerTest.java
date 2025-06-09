package dev.nikkune.paymybuddy.controller;

import dev.nikkune.paymybuddy.dto.TransactionCreationDTO;
import dev.nikkune.paymybuddy.dto.TransactionDTO;
import dev.nikkune.paymybuddy.mapper.TransactionMapper;
import dev.nikkune.paymybuddy.model.Transaction;
import dev.nikkune.paymybuddy.model.User;
import dev.nikkune.paymybuddy.service.ITransactionService;
import dev.nikkune.paymybuddy.utils.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private ITransactionService transactionService;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionController transactionController;

    private User sender;
    private User receiver;
    private Transaction transaction;
    private TransactionDTO transactionDTO;
    private TransactionCreationDTO transactionCreationDTO;
    private List<Transaction> transactions;
    private List<TransactionDTO> transactionDTOs;

    @BeforeEach
    void setUp() {
        // Create users
        sender = new User();
        sender.setId(1);
        sender.setUsername("sender");
        sender.setEmail("sender@example.com");
        sender.setBalance(1000.0);

        receiver = new User();
        receiver.setId(2);
        receiver.setUsername("receiver");
        receiver.setEmail("receiver@example.com");
        receiver.setBalance(500.0);

        // Create transaction
        transaction = new Transaction();
        transaction.setId(1);
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(100.0);
        transaction.setDescription("Test transaction");

        // Create transaction DTO
        transactionDTO = new TransactionDTO();
        transactionDTO.setSenderUsername(sender.getUsername());
        transactionDTO.setReceiverUsername(receiver.getUsername());
        transactionDTO.setAmount(100.0);
        transactionDTO.setDescription("Test transaction");

        // Create transaction creation DTO
        transactionCreationDTO = new TransactionCreationDTO();
        transactionCreationDTO.setSenderId(sender.getId());
        transactionCreationDTO.setReceiverId(receiver.getId());
        transactionCreationDTO.setAmount(100.0);
        transactionCreationDTO.setDescription("Test transaction");

        // Create lists
        transactions = new ArrayList<>();
        transactions.add(transaction);

        transactionDTOs = new ArrayList<>();
        transactionDTOs.add(transactionDTO);
    }

    @Test
    void getTransactionsByUserId_ShouldReturnTransactions() {
        // Arrange
        when(transactionService.getTransactionsByUserId(sender.getId())).thenReturn(transactions);
        when(transactionMapper.transactionsToTransactionDTOs(transactions)).thenReturn(transactionDTOs);

        // Act
        ResponseEntity<List<TransactionDTO>> responseEntity = transactionController.getTransactionsByUserId(sender.getId());

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(transactionDTOs, responseEntity.getBody());
        assertEquals(1, responseEntity.getBody().size());
        assertEquals(transactionDTO, responseEntity.getBody().get(0));

        // Verify interactions
        verify(transactionService).getTransactionsByUserId(sender.getId());
        verify(transactionMapper).transactionsToTransactionDTOs(transactions);
    }

    @Test
    void addTransaction_ShouldReturnCreatedTransaction() {
        // Arrange
        when(transactionService.addTransaction(any(TransactionCreationDTO.class))).thenReturn(transaction);
        when(transactionMapper.transactionToTransactionDTO(transaction)).thenReturn(transactionDTO);

        // Act
        ResponseEntity<Response> responseEntity = transactionController.addTransaction(transactionCreationDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof Response);
        Response responseBody = responseEntity.getBody();
        assertTrue((Boolean) responseBody.get("success"));
        assertEquals("Transaction added successfully", responseBody.get("message"));
        assertEquals(transactionDTO, responseBody.get("data"));

        // Verify interactions
        verify(transactionService).addTransaction(any(TransactionCreationDTO.class));
        verify(transactionMapper).transactionToTransactionDTO(transaction);
    }

    @Test
    void addTransaction_WithException_ShouldHandleError() {
        // Arrange
        when(transactionService.addTransaction(any(TransactionCreationDTO.class)))
                .thenThrow(new RuntimeException("Insufficient funds"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> transactionController.addTransaction(transactionCreationDTO));

        // Verify interactions
        verify(transactionService).addTransaction(any(TransactionCreationDTO.class));
        verify(transactionMapper, never()).transactionToTransactionDTO(any(Transaction.class));
    }
}
