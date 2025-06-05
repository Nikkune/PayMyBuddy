package dev.nikkune.paymybuddy.controller;

import dev.nikkune.paymybuddy.dto.TransactionCreationDTO;
import dev.nikkune.paymybuddy.dto.TransactionDTO;
import dev.nikkune.paymybuddy.mapper.TransactionMapper;
import dev.nikkune.paymybuddy.model.Transaction;
import dev.nikkune.paymybuddy.model.User;
import dev.nikkune.paymybuddy.service.TransactionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling transaction-related operations
 */
@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    /**
     * Constructor for TransactionController
     * @param transactionService the transaction service
     * @param transactionMapper the transaction mapper
     */
    public TransactionController(TransactionService transactionService, TransactionMapper transactionMapper) {
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
    }

    /**
     * Get all transactions
     * @return list of all transactions
     */
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        logger.debug("Received request to get all transactions");
        List<Transaction> transactions = transactionService.getAllTransactions();
        List<TransactionDTO> transactionDTOs = transactionMapper.transactionsToTransactionDTOs(transactions);
        logger.info("Returning {} transactions", transactionDTOs.size());
        return ResponseEntity.ok(transactionDTOs);
    }

    /**
     * Get transaction by ID
     * @param transactionId the transaction ID
     * @return the transaction with the given ID
     */
    @GetMapping("/id")
    public ResponseEntity<TransactionDTO> getTransactionById(@RequestParam @Valid Integer transactionId) {
        logger.debug("Received request to get transaction with ID: {}", transactionId);
        try {
            Transaction transaction = transactionService.getTransactionById(transactionId);
            TransactionDTO transactionDTO = transactionMapper.transactionToTransactionDTO(transaction);
            logger.info("Returning transaction with ID: {}", transactionId);
            return ResponseEntity.ok(transactionDTO);
        } catch (RuntimeException e) {
            logger.error("Error getting transaction with ID: {}", transactionId, e);
            return new ResponseEntity<>(new TransactionDTO(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Get transactions by user ID
     * @param userId the user ID
     * @return list of transactions for the user
     */
    @GetMapping("/user")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByUserId(@RequestParam @Valid Integer userId) {
        logger.debug("Received request to get transactions for user with ID: {}", userId);
        try {
            List<Transaction> transactions = transactionService.getTransactionsByUserId(userId);
            List<TransactionDTO> transactionDTOs = transactionMapper.transactionsToTransactionDTOs(transactions);
            logger.info("Returning {} transactions for user with ID: {}", transactionDTOs.size(), userId);
            return ResponseEntity.ok(transactionDTOs);
        } catch (RuntimeException e) {
            logger.error("Error getting transactions for user with ID: {}", userId, e);
            return new ResponseEntity<>(List.of(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Get transactions between users
     * @param senderId the sender ID
     * @param receiverId the receiver ID
     * @return list of transactions between the users
     */
    @GetMapping(params = {"senderId", "receiverId"})
    public ResponseEntity<List<TransactionDTO>> getTransactionsBetweenUsers(
            @RequestParam @Valid Integer senderId,
            @RequestParam @Valid Integer receiverId) {
        logger.debug("Received request to get transactions between sender ID: {} and receiver ID: {}", senderId, receiverId);
        try {
            List<Transaction> transactions = transactionService.getTransactionsBetweenUsers(senderId, receiverId);
            List<TransactionDTO> transactionDTOs = transactionMapper.transactionsToTransactionDTOs(transactions);
            logger.info("Returning {} transactions between sender ID: {} and receiver ID: {}", 
                    transactionDTOs.size(), senderId, receiverId);
            return ResponseEntity.ok(transactionDTOs);
        } catch (RuntimeException e) {
            logger.error("Error getting transactions between sender ID: {} and receiver ID: {}", 
                    senderId, receiverId, e);
            return new ResponseEntity<>(List.of(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Add a new transaction
     * @param transactionCreationDTO the transaction data
     * @return the created transaction
     */
    @PostMapping
    public ResponseEntity<TransactionDTO> addTransaction(@RequestBody @Valid TransactionCreationDTO transactionCreationDTO) {
        logger.debug("Received request to add transaction from sender ID: {} to receiver ID: {}", 
                transactionCreationDTO.getSenderId(), transactionCreationDTO.getReceiverId());
        try {
            Transaction transaction = transactionMapper.transactionCreationDTOToTransaction(transactionCreationDTO);

            // Set sender and receiver
            User sender = new User();
            sender.setId(transactionCreationDTO.getSenderId());
            transactionMapper.setSender(transaction, sender);

            User receiver = new User();
            receiver.setId(transactionCreationDTO.getReceiverId());
            transactionMapper.setReceiver(transaction, receiver);

            Transaction createdTransaction = transactionService.addTransaction(transaction);
            TransactionDTO transactionDTO = transactionMapper.transactionToTransactionDTO(createdTransaction);

            logger.info("Transaction added successfully with ID: {}", transactionDTO.getId());
            return new ResponseEntity<>(transactionDTO, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            logger.error("Error adding transaction from sender ID: {} to receiver ID: {}", 
                    transactionCreationDTO.getSenderId(), transactionCreationDTO.getReceiverId(), e);
            return new ResponseEntity<>(new TransactionDTO(), HttpStatus.BAD_REQUEST);
        }
    }
}
