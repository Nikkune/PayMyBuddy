package dev.nikkune.paymybuddy.service;

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
        sender.setConnections(new ArrayList<>());

        // Create a receiver user
        receiver = new User();
        receiver.setId(2);
        receiver.setUsername("receiver");
        receiver.setEmail("receiver@example.com");
        receiver.setPassword("encodedPassword");
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
    void getAllTransactions_ShouldReturnAllTransactions() {
        // Arrange
        List<Transaction> expectedTransactions = Arrays.asList(transaction);
        when(transactionRepository.findAll()).thenReturn(expectedTransactions);

        // Act
        List<Transaction> actualTransactions = transactionService.getAllTransactions();

        // Assert
        assertEquals(expectedTransactions, actualTransactions);
        verify(transactionRepository).findAll();
    }

    @Test
    void exists_WithExistingId_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));

        // Act
        User result = transactionService.exists(sender.getId());

        // Assert
        assertEquals(sender, result);
        verify(userRepository).findById(sender.getId());
    }

    @Test
    void exists_WithNonExistingId_ShouldReturnNull() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        User result = transactionService.exists(999);

        // Assert
        assertNull(result);
        verify(userRepository).findById(999);
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
        assertEquals("User with ID : 999 does not exist", exception.getMessage());
        verify(userRepository).findById(999);
    }

    @Test
    void getTransactionById_WithExistingId_ShouldReturnTransaction() {
        // Arrange
        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.of(transaction));

        // Act
        Transaction result = transactionService.getTransactionById(transaction.getId());

        // Assert
        assertEquals(transaction, result);
        verify(transactionRepository).findById(transaction.getId());
    }

    @Test
    void getTransactionById_WithNonExistingId_ShouldThrowException() {
        // Arrange
        when(transactionRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> transactionService.getTransactionById(999));
        assertEquals("Transaction with ID : 999 does not exist", exception.getMessage());
        verify(transactionRepository).findById(999);
    }

    @Test
    void getTransactionsByUserId_WithExistingId_ShouldReturnTransactions() {
        // Arrange
        List<Transaction> sentTransactions = Arrays.asList(transaction);
        List<Transaction> receivedTransactions = new ArrayList<>();

        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
        when(transactionRepository.findBySenderId(sender.getId())).thenReturn(sentTransactions);
        when(transactionRepository.findByReceiverId(sender.getId())).thenReturn(receivedTransactions);

        // Act
        List<Transaction> result = transactionService.getTransactionsByUserId(sender.getId());

        // Assert
        assertEquals(1, result.size());
        assertEquals(transaction, result.get(0));
        verify(userRepository).findById(sender.getId());
        verify(transactionRepository).findBySenderId(sender.getId());
        verify(transactionRepository).findByReceiverId(sender.getId());
    }

    @Test
    void getTransactionsByUserId_WithNonExistingId_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> transactionService.getTransactionsByUserId(999));
        assertEquals("User with ID : 999 does not exist", exception.getMessage());
        verify(userRepository).findById(999);
        verify(transactionRepository, never()).findBySenderId(anyInt());
        verify(transactionRepository, never()).findByReceiverId(anyInt());
    }

    @Test
    void getTransactionsBetweenUsers_WithExistingIds_ShouldReturnTransactions() {
        // Arrange
        List<Transaction> sentTransactions = Arrays.asList(transaction);

        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(transactionRepository.findBySenderId(sender.getId())).thenReturn(sentTransactions);

        // Act
        List<Transaction> result = transactionService.getTransactionsBetweenUsers(sender.getId(), receiver.getId());

        // Assert
        assertEquals(1, result.size());
        assertEquals(transaction, result.get(0));
        verify(userRepository).findById(sender.getId());
        verify(userRepository).findById(receiver.getId());
        verify(transactionRepository).findBySenderId(sender.getId());
    }

    @Test
    void getTransactionsBetweenUsers_WithNonExistingSender_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> transactionService.getTransactionsBetweenUsers(999, receiver.getId()));
        assertEquals("User with ID : 999 does not exist", exception.getMessage());
        verify(userRepository).findById(999);
        verify(userRepository, never()).findById(receiver.getId());
        verify(transactionRepository, never()).findBySenderId(anyInt());
    }

    @Test
    void getTransactionsBetweenUsers_WithNonExistingReceiver_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> transactionService.getTransactionsBetweenUsers(sender.getId(), 999));
        assertEquals("User with ID : 999 does not exist", exception.getMessage());
        verify(userRepository).findById(sender.getId());
        verify(userRepository).findById(999);
        verify(transactionRepository, never()).findBySenderId(anyInt());
    }

    @Test
    void addTransaction_WithValidTransaction_ShouldAddTransaction() {
        // Arrange
        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // Act
        Transaction result = transactionService.addTransaction(transaction);

        // Assert
        assertEquals(transaction, result);
        verify(userRepository).findById(sender.getId());
        verify(userRepository).findById(receiver.getId());
        verify(transactionRepository).save(transaction);
    }

    @Test
    void addTransaction_WithNonExistingSender_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(sender.getId())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> transactionService.addTransaction(transaction));
        assertEquals("User with ID : " + sender.getId() + " does not exist", exception.getMessage());
        verify(userRepository).findById(sender.getId());
        verify(userRepository, never()).findById(receiver.getId());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void addTransaction_WithNonExistingReceiver_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiver.getId())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> transactionService.addTransaction(transaction));
        assertEquals("User with ID : " + receiver.getId() + " does not exist", exception.getMessage());
        verify(userRepository).findById(sender.getId());
        verify(userRepository).findById(receiver.getId());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}
