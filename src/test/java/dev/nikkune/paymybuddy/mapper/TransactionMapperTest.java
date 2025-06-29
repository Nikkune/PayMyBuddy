package dev.nikkune.paymybuddy.mapper;

import dev.nikkune.paymybuddy.dto.TransactionCreationDTO;
import dev.nikkune.paymybuddy.dto.TransactionDTO;
import dev.nikkune.paymybuddy.model.Transaction;
import dev.nikkune.paymybuddy.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionMapperTest {

    private TransactionMapper transactionMapper;
    private User sender;
    private User receiver;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        // Get the mapper instance
        transactionMapper = Mappers.getMapper(TransactionMapper.class);

        // Create sender user
        sender = new User();
        sender.setId(1);
        sender.setUsername("sender");
        sender.setEmail("sender@example.com");
        sender.setPassword("password123");
        sender.setConnections(new ArrayList<>());

        // Create a receiver user
        receiver = new User();
        receiver.setId(2);
        receiver.setUsername("receiver");
        receiver.setEmail("receiver@example.com");
        receiver.setPassword("password456");
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
    void transactionToTransactionDTO_ShouldMapTransactionToDTO() {
        // Act
        TransactionDTO transactionDTO = transactionMapper.transactionToTransactionDTO(transaction);

        // Assert
        assertNotNull(transactionDTO);
        assertEquals(transaction.getDescription(), transactionDTO.getDescription());
        assertEquals(transaction.getAmount(), transactionDTO.getAmount());
        
        // Check sender mapping
        assertEquals(sender.getUsername(), transactionDTO.getSenderUsername());
        
        // Check receiver mapping
        assertEquals(receiver.getUsername(), transactionDTO.getReceiverUsername());
    }

    @Test
    void transactionsToTransactionDTOs_ShouldMapTransactionListToDTOList() {
        // Arrange
        Transaction anotherTransaction = new Transaction();
        anotherTransaction.setId(2);
        anotherTransaction.setSender(receiver); // Swap sender and receiver
        anotherTransaction.setReceiver(sender);
        anotherTransaction.setDescription("Another test transaction");
        anotherTransaction.setAmount(50.0);
        
        List<Transaction> transactions = Arrays.asList(transaction, anotherTransaction);

        // Act
        List<TransactionDTO> transactionDTOs = transactionMapper.transactionsToTransactionDTOs(transactions);

        // Assert
        assertNotNull(transactionDTOs);
        assertEquals(2, transactionDTOs.size());
        
        // Check first transaction
        assertEquals(transaction.getDescription(), transactionDTOs.getFirst().getDescription());
        assertEquals(transaction.getAmount(), transactionDTOs.getFirst().getAmount());
        assertEquals(sender.getUsername(), transactionDTOs.getFirst().getSenderUsername());
        assertEquals(receiver.getUsername(), transactionDTOs.getFirst().getReceiverUsername());
        
        // Check the second transaction
        assertEquals(anotherTransaction.getDescription(), transactionDTOs.get(1).getDescription());
        assertEquals(anotherTransaction.getAmount(), transactionDTOs.get(1).getAmount());
        assertEquals(receiver.getUsername(), transactionDTOs.get(1).getSenderUsername());
        assertEquals(sender.getUsername(), transactionDTOs.get(1).getReceiverUsername());
    }

    @Test
    void setSender_ShouldSetSenderOnTransaction() {
        // Arrange
        Transaction transactionWithoutSender = new Transaction();
        transactionWithoutSender.setId(3);
        transactionWithoutSender.setDescription("Transaction without sender");
        transactionWithoutSender.setAmount(25.0);

        // Act
        transactionMapper.setSender(transactionWithoutSender, sender);

        // Assert
        assertEquals(sender, transactionWithoutSender.getSender());
    }

    @Test
    void setReceiver_ShouldSetReceiverOnTransaction() {
        // Arrange
        Transaction transactionWithoutReceiver = new Transaction();
        transactionWithoutReceiver.setId(3);
        transactionWithoutReceiver.setDescription("Transaction without receiver");
        transactionWithoutReceiver.setAmount(25.0);

        // Act
        transactionMapper.setReceiver(transactionWithoutReceiver, receiver);

        // Assert
        assertEquals(receiver, transactionWithoutReceiver.getReceiver());
    }
}