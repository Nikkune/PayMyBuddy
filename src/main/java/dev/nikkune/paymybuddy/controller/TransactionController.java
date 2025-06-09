package dev.nikkune.paymybuddy.controller;

import dev.nikkune.paymybuddy.dto.TransactionCreationDTO;
import dev.nikkune.paymybuddy.dto.TransactionDTO;
import dev.nikkune.paymybuddy.mapper.TransactionMapper;
import dev.nikkune.paymybuddy.model.Transaction;
import dev.nikkune.paymybuddy.service.ITransactionService;
import dev.nikkune.paymybuddy.utils.Response;
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
    private final ITransactionService transactionService;
    private final TransactionMapper transactionMapper;

    /**
     * Constructor for TransactionController
     *
     * @param transactionService the transaction service
     * @param transactionMapper  the transaction mapper
     */
    public TransactionController(ITransactionService transactionService, TransactionMapper transactionMapper) {
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
    }

    /**
     * Get transactions by user ID
     *
     * @param userId the user ID
     * @return list of transactions for the user
     */
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getTransactionsByUserId(@RequestParam @Valid Integer userId) {
        logger.debug("Received request to get transactions for user with ID: {}", userId);
        List<Transaction> transactions = transactionService.getTransactionsByUserId(userId);
        List<TransactionDTO> transactionDTOs = transactionMapper.transactionsToTransactionDTOs(transactions);
        logger.info("Returning {} transactions for user with ID: {}", transactionDTOs.size(), userId);
        return ResponseEntity.ok(transactionDTOs);
    }

    /**
     * Add a new transaction
     *
     * @param transactionCreationDTO the transaction data
     * @return the created transaction
     */
    @PostMapping
    public ResponseEntity<Response> addTransaction(@RequestBody @Valid TransactionCreationDTO transactionCreationDTO) {
        logger.debug("Received request to add transaction from sender ID: {} to receiver ID: {}",
                transactionCreationDTO.getSenderId(), transactionCreationDTO.getReceiverId());
        Transaction createdTransaction = transactionService.addTransaction(transactionCreationDTO);
        TransactionDTO transactionDTO = transactionMapper.transactionToTransactionDTO(createdTransaction);

        Response responseBody = new Response("Transaction added successfully", true)
                .add("data", transactionDTO);

        logger.info("Transaction added successfully");
        return new ResponseEntity<>(responseBody, HttpStatus.CREATED);
    }
}
