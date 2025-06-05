package dev.nikkune.paymybuddy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nikkune.paymybuddy.dto.TransactionCreationDTO;
import dev.nikkune.paymybuddy.dto.TransactionDTO;
import dev.nikkune.paymybuddy.mapper.TransactionMapper;
import dev.nikkune.paymybuddy.model.Transaction;
import dev.nikkune.paymybuddy.model.User;
import dev.nikkune.paymybuddy.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TransactionService transactionService;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionController transactionController;

    private User sender;
    private User receiver;
    private Transaction transaction;
    private TransactionDTO transactionDTO;
    private TransactionCreationDTO transactionCreationDTO;

    @BeforeEach
    void setUp() {
        // Initialize MockMvc and ObjectMapper
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
        objectMapper = new ObjectMapper();

        // Create sender user
        sender = new User();
        sender.setId(1);
        sender.setUsername("sender");
        sender.setEmail("sender@example.com");
        sender.setPassword("password123");
        sender.setBalance(500.0);
        sender.setConnections(new ArrayList<>());

        // Create receiver user
        receiver = new User();
        receiver.setId(2);
        receiver.setUsername("receiver");
        receiver.setEmail("receiver@example.com");
        receiver.setPassword("password123");
        receiver.setBalance(500.0);
        receiver.setConnections(new ArrayList<>());

        // Create transaction
        transaction = new Transaction();
        transaction.setId(1);
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setDescription("Test transaction");
        transaction.setAmount(100.0);

        // Create transaction DTO
        transactionDTO = new TransactionDTO();
        transactionDTO.setSenderUsername(sender.getUsername());
        transactionDTO.setReceiverUsername(receiver.getUsername());
        transactionDTO.setDescription("Test transaction");
        transactionDTO.setAmount(100.0);

        // Create transaction creation DTO
        transactionCreationDTO = new TransactionCreationDTO();
        transactionCreationDTO.setSenderId(sender.getId());
        transactionCreationDTO.setReceiverId(receiver.getId());
        transactionCreationDTO.setDescription("Test transaction");
        transactionCreationDTO.setAmount(100.0);
    }

    @Test
    void getAllTransactions_ShouldReturnAllTransactions() throws Exception {
        // Arrange
        List<Transaction> transactions = List.of(transaction);
        List<TransactionDTO> transactionDTOs = List.of(transactionDTO);

        when(transactionService.getAllTransactions()).thenReturn(transactions);
        when(transactionMapper.transactionsToTransactionDTOs(transactions)).thenReturn(transactionDTOs);

        // Act & Assert
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].senderUsername", is(transactionDTO.getSenderUsername())))
                .andExpect(jsonPath("$[0].receiverUsername", is(transactionDTO.getReceiverUsername())))
                .andExpect(jsonPath("$[0].description", is(transactionDTO.getDescription())))
                .andExpect(jsonPath("$[0].amount", is(transactionDTO.getAmount())));

        verify(transactionService).getAllTransactions();
        verify(transactionMapper).transactionsToTransactionDTOs(transactions);
    }

    @Test
    void getTransactionById_WithExistingId_ShouldReturnTransaction() throws Exception {
        // Arrange
        when(transactionService.getTransactionById(transaction.getId())).thenReturn(transaction);
        when(transactionMapper.transactionToTransactionDTO(transaction)).thenReturn(transactionDTO);

        // Act & Assert
        mockMvc.perform(get("/transactions/id")
                        .param("transactionId", String.valueOf(transaction.getId())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.senderUsername", is(transactionDTO.getSenderUsername())))
                .andExpect(jsonPath("$.receiverUsername", is(transactionDTO.getReceiverUsername())))
                .andExpect(jsonPath("$.description", is(transactionDTO.getDescription())))
                .andExpect(jsonPath("$.amount", is(transactionDTO.getAmount())));

        verify(transactionService).getTransactionById(transaction.getId());
        verify(transactionMapper).transactionToTransactionDTO(transaction);
    }

    @Test
    void getTransactionById_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(transactionService.getTransactionById(999)).thenThrow(new RuntimeException("Transaction with ID : 999 does not exist"));

        // Act & Assert
        mockMvc.perform(get("/transactions/id")
                        .param("transactionId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.senderUsername", is(nullValue())))
                .andExpect(jsonPath("$.receiverUsername", is(nullValue())))
                .andExpect(jsonPath("$.description", is(nullValue())))
                .andExpect(jsonPath("$.amount", is(0.0)));

        verify(transactionService).getTransactionById(999);
        verify(transactionMapper, never()).transactionToTransactionDTO(any(Transaction.class));
    }

    @Test
    void getTransactionsByUserId_WithExistingId_ShouldReturnTransactions() throws Exception {
        // Arrange
        List<Transaction> transactions = List.of(transaction);
        List<TransactionDTO> transactionDTOs = List.of(transactionDTO);

        when(transactionService.getTransactionsByUserId(sender.getId())).thenReturn(transactions);
        when(transactionMapper.transactionsToTransactionDTOs(transactions)).thenReturn(transactionDTOs);

        // Act & Assert
        mockMvc.perform(get("/transactions/user")
                        .param("userId", String.valueOf(sender.getId())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].senderUsername", is(transactionDTO.getSenderUsername())))
                .andExpect(jsonPath("$[0].receiverUsername", is(transactionDTO.getReceiverUsername())))
                .andExpect(jsonPath("$[0].description", is(transactionDTO.getDescription())))
                .andExpect(jsonPath("$[0].amount", is(transactionDTO.getAmount())));

        verify(transactionService).getTransactionsByUserId(sender.getId());
        verify(transactionMapper).transactionsToTransactionDTOs(transactions);
    }

    @Test
    void getTransactionsByUserId_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(transactionService.getTransactionsByUserId(999)).thenThrow(new RuntimeException("User with ID : 999 does not exist"));

        // Act & Assert
        mockMvc.perform(get("/transactions/user")
                        .param("userId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(transactionService).getTransactionsByUserId(999);
        verify(transactionMapper, never()).transactionsToTransactionDTOs(anyList());
    }

    @Test
    void getTransactionsBetweenUsers_WithExistingIds_ShouldReturnTransactions() throws Exception {
        // Arrange
        List<Transaction> transactions = List.of(transaction);
        List<TransactionDTO> transactionDTOs = List.of(transactionDTO);

        when(transactionService.getTransactionsBetweenUsers(sender.getId(), receiver.getId())).thenReturn(transactions);
        when(transactionMapper.transactionsToTransactionDTOs(transactions)).thenReturn(transactionDTOs);

        // Act & Assert
        mockMvc.perform(get("/transactions")
                        .param("senderId", String.valueOf(sender.getId()))
                        .param("receiverId", String.valueOf(receiver.getId())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].senderUsername", is(transactionDTO.getSenderUsername())))
                .andExpect(jsonPath("$[0].receiverUsername", is(transactionDTO.getReceiverUsername())))
                .andExpect(jsonPath("$[0].description", is(transactionDTO.getDescription())))
                .andExpect(jsonPath("$[0].amount", is(transactionDTO.getAmount())));

        verify(transactionService).getTransactionsBetweenUsers(sender.getId(), receiver.getId());
        verify(transactionMapper).transactionsToTransactionDTOs(transactions);
    }

    @Test
    void getTransactionsBetweenUsers_WithNonExistingIds_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(transactionService.getTransactionsBetweenUsers(999, receiver.getId())).thenThrow(new RuntimeException("User with ID : 999 does not exist"));

        // Act & Assert
        mockMvc.perform(get("/transactions")
                        .param("senderId", "999")
                        .param("receiverId", String.valueOf(receiver.getId())))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(transactionService).getTransactionsBetweenUsers(999, receiver.getId());
        verify(transactionMapper, never()).transactionsToTransactionDTOs(anyList());
    }

    @Test
    void addTransaction_WithValidData_ShouldAddTransaction() throws Exception {
        // Arrange
        Transaction newTransaction = new Transaction();
        newTransaction.setDescription(transactionCreationDTO.getDescription());
        newTransaction.setAmount(transactionCreationDTO.getAmount());

        when(transactionService.addTransaction(any(TransactionCreationDTO.class))).thenReturn(transaction);
        when(transactionMapper.transactionToTransactionDTO(transaction)).thenReturn(transactionDTO);

        // Act & Assert
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionCreationDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.senderUsername", is(transactionDTO.getSenderUsername())))
                .andExpect(jsonPath("$.receiverUsername", is(transactionDTO.getReceiverUsername())))
                .andExpect(jsonPath("$.description", is(transactionDTO.getDescription())))
                .andExpect(jsonPath("$.amount", is(transactionDTO.getAmount())));

        verify(transactionService).addTransaction(any(TransactionCreationDTO.class));
        verify(transactionMapper).transactionToTransactionDTO(transaction);
    }

    @Test
    void addTransaction_WithNonExistingUser_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Transaction newTransaction = new Transaction();
        newTransaction.setDescription(transactionCreationDTO.getDescription());
        newTransaction.setAmount(transactionCreationDTO.getAmount());

        when(transactionService.addTransaction(any(TransactionCreationDTO.class))).thenThrow(new RuntimeException("User with ID : 999 does not exist"));

        // Act & Assert
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionCreationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.senderUsername", is(nullValue())))
                .andExpect(jsonPath("$.receiverUsername", is(nullValue())))
                .andExpect(jsonPath("$.description", is(nullValue())))
                .andExpect(jsonPath("$.amount", is(0.0)));

        verify(transactionService).addTransaction(any(TransactionCreationDTO.class));
        verify(transactionMapper, never()).transactionToTransactionDTO(any(Transaction.class));
    }

    @Test
    void addTransaction_WithInsufficientBalance_ShouldReturnBadRequest() throws Exception {
        // Arrange
        TransactionCreationDTO insufficientBalanceDTO = new TransactionCreationDTO();
        insufficientBalanceDTO.setSenderId(sender.getId());
        insufficientBalanceDTO.setReceiverId(receiver.getId());
        insufficientBalanceDTO.setDescription("Transaction with insufficient balance");
        insufficientBalanceDTO.setAmount(1000.0); // Amount greater than sender's balance (500.0)

        when(transactionService.addTransaction(any(TransactionCreationDTO.class))).thenThrow(new RuntimeException("Insufficient balance"));

        // Act & Assert
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insufficientBalanceDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.senderUsername", is(nullValue())))
                .andExpect(jsonPath("$.receiverUsername", is(nullValue())))
                .andExpect(jsonPath("$.description", is(nullValue())))
                .andExpect(jsonPath("$.amount", is(0.0)));

        verify(transactionService).addTransaction(any(TransactionCreationDTO.class));
        verify(transactionMapper, never()).transactionToTransactionDTO(any(Transaction.class));
    }
}
