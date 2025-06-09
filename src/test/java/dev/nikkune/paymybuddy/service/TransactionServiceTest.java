package dev.nikkune.paymybuddy.service;

import dev.nikkune.paymybuddy.dto.TransactionCreationDTO;
import dev.nikkune.paymybuddy.model.Transaction;
import dev.nikkune.paymybuddy.model.User;
import dev.nikkune.paymybuddy.repository.TransactionRepository;
import dev.nikkune.paymybuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User sender;
    private User receiver;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        // Create sender user
        sender = new User();
        sender.setId(1);
        sender.setUsername("sender");
        sender.setEmail("sender@example.com");
        sender.setPassword("encodedPassword");
        sender.setBalance(500.0);
        sender.setConnections(new ArrayList<>());

        // Create a receiver user
        receiver = new User();
        receiver.setId(2);
        receiver.setUsername("receiver");
        receiver.setEmail("receiver@example.com");
        receiver.setPassword("encodedPassword");
        receiver.setBalance(500.0);
        receiver.setConnections(new ArrayList<>());

        // Create transaction
        transaction = new Transaction();
        transaction.setId(1);
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setDescription("Test transaction");
        transaction.setAmount(100.0);
    }

    @Test
    void requiredUser_WithExistingId_ShouldNotThrowException() {
        // Arrange
        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));

        // Act & Assert
        assertDoesNotThrow(() -> transactionService.requiredUser(sender.getId()));
        verify(userRepository).findById(sender.getId());
    }

    @Test
    void requiredUser_WithNonExistingId_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> transactionService.requiredUser(999));
        assertEquals("User with ID : 999 not found", exception.getMessage());
        verify(userRepository).findById(999);
    }

    @Test
    void getTransactionsByUserId_WithExistingId_ShouldReturnTransactions() {
        // Arrange
        List<Transaction> sentTransactions = Arrays.asList(transaction);
        List<Transaction> receivedTransactions = new ArrayList<>();
        List<Transaction> allTransactions = new ArrayList<>();
        allTransactions.addAll(sentTransactions);
        allTransactions.addAll(receivedTransactions);

        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
        when(transactionRepository.findBySenderIdOrReceiverId(sender.getId(),sender.getId())).thenReturn(allTransactions);

        // Act
        List<Transaction> result = transactionService.getTransactionsByUserId(sender.getId());

        // Assert
        assertEquals(1, result.size());
        assertEquals(transaction, result.getFirst());
        verify(userRepository).findById(sender.getId());
        verify(transactionRepository).findBySenderIdOrReceiverId(sender.getId(),sender.getId());
    }

    @Test
    void getTransactionsByUserId_WithNonExistingId_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> transactionService.getTransactionsByUserId(999));
        assertEquals("User with ID : 999 not found", exception.getMessage());
        verify(userRepository).findById(999);
        verify(transactionRepository, never()).findBySenderIdOrReceiverId(anyInt(), anyInt());
    }

    @Test
    void addTransaction_WithValidTransaction_ShouldAddTransaction() {
        // Arrange
        double initialSenderBalance = sender.getBalance();
        double initialReceiverBalance = receiver.getBalance();
        double transactionAmount = 75.0;

        TransactionCreationDTO transactionCreationDTO = new TransactionCreationDTO();
        transactionCreationDTO.setDescription("Test transaction");
        transactionCreationDTO.setAmount(transactionAmount);
        transactionCreationDTO.setSenderId(sender.getId());
        transactionCreationDTO.setReceiverId(receiver.getId());

        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // Act
        Transaction result = transactionService.addTransaction(transactionCreationDTO);

        // Assert
        assertEquals(transaction, result);
        assertEquals(initialSenderBalance - transactionAmount, sender.getBalance(), "Sender balance should be decreased by transaction amount");
        assertEquals(initialReceiverBalance + transactionAmount, receiver.getBalance(), "Receiver balance should be increased by transaction amount");
        verify(userRepository).findById(sender.getId());
        verify(userRepository).findById(receiver.getId());
        verify(userRepository).save(sender);
        verify(userRepository).save(receiver);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void addTransaction_WithNonExistingSender_ShouldThrowException() {
        // Arrange
        TransactionCreationDTO transactionCreationDTO = new TransactionCreationDTO();
        transactionCreationDTO.setDescription("Test transaction");
        transactionCreationDTO.setAmount(75.0);
        transactionCreationDTO.setSenderId(999);
        transactionCreationDTO.setReceiverId(receiver.getId());
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> transactionService.addTransaction(transactionCreationDTO));
        assertEquals("Sender with ID : " + 999 + " not found", exception.getMessage());
        verify(userRepository).findById(999);
        verify(userRepository, never()).findById(receiver.getId());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void addTransaction_WithNonExistingReceiver_ShouldThrowException() {
        // Arrange
        TransactionCreationDTO transactionCreationDTO = new TransactionCreationDTO();
        transactionCreationDTO.setDescription("Test transaction");
        transactionCreationDTO.setAmount(75.0);
        transactionCreationDTO.setSenderId(sender.getId());
        transactionCreationDTO.setReceiverId(999);
        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> transactionService.addTransaction(transactionCreationDTO));
        assertEquals("Receiver with ID : " + 999 + " not found", exception.getMessage());
        verify(userRepository).findById(sender.getId());
        verify(userRepository).findById(999);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void addTransaction_WithInsufficientBalance_ShouldThrowException() {
        // Arrange
        TransactionCreationDTO transactionCreationDTO = new TransactionCreationDTO();
        transactionCreationDTO.setDescription("Test transaction");
        transactionCreationDTO.setAmount(1000.0); // Amount greater than sender's balance (500.0)
        transactionCreationDTO.setSenderId(sender.getId());
        transactionCreationDTO.setReceiverId(receiver.getId());

        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> transactionService.addTransaction(transactionCreationDTO));
        assertEquals("Insufficient balance", exception.getMessage());

        // Verify that balances remain unchanged
        assertEquals(500.0, sender.getBalance());
        assertEquals(500.0, receiver.getBalance());

        verify(userRepository).findById(sender.getId());
        verify(userRepository).findById(receiver.getId());
        verify(userRepository, never()).save(any(User.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}
