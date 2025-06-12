package dev.nikkune.paymybuddy.service;

import dev.nikkune.paymybuddy.dto.TransactionCreationDTO;
import dev.nikkune.paymybuddy.model.Transaction;
import dev.nikkune.paymybuddy.model.User;
import dev.nikkune.paymybuddy.repository.TransactionRepository;
import dev.nikkune.paymybuddy.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class that manages transaction-related operations and interactions between users
 * and transactions in the system. This class ensures that operations are executed
 * in a transactional context and verifies the existence of required users.
 */
@Service
public class TransactionService implements ITransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    /**
     * Constructs a new TransactionService with the given repositories.
     *
     * @param transactionRepository the repository for managing transaction data
     * @param userRepository the repository for managing user data
     */
    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository){
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Verifies if a user with the given ID exists. If the user does not exist, a RuntimeException is thrown.
     *
     * @param id the unique identifier of the user to be verified
     * @throws RuntimeException if the user with the specified ID does not exist
     */
    public void requiredUser(int id) throws RuntimeException {
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser == null)
            throw new RuntimeException("User with ID : " + id + " not found");
    }

    /**
     * Retrieves a list of transactions associated with a specific user.
     * This includes transactions where the user is either the sender or the receiver.
     *
     * @param userId the unique identifier of the user whose transactions are to be retrieved
     * @return a list of transactions related to the user, both as sender and receiver
     * @throws RuntimeException if the user with the specified ID does not exist
     */
    public List<Transaction> getTransactionsByUserId(int userId) throws RuntimeException {
        requiredUser(userId);
        return transactionRepository.findBySenderIdOrReceiverId(userId, userId);
    }

    /**
     * Adds a new transaction to the system after validating that both the sender and receiver exist.
     *
     * @param transactionCreationDTO the transaction to be added, which includes sender, receiver, amount, and description
     * @return the saved transaction object
     * @throws RuntimeException if the sender or receiver does not exist
     */
    @Transactional
    public Transaction addTransaction(TransactionCreationDTO transactionCreationDTO) throws RuntimeException {
        User sender = userRepository.findById(transactionCreationDTO.getSenderId()).orElse(null);
        if (sender == null)
            throw new RuntimeException("Sender with ID : " + transactionCreationDTO.getSenderId() + " not found");

        User receiver = userRepository.findById(transactionCreationDTO.getReceiverId()).orElse(null);
        if (receiver == null)
            throw new RuntimeException("Receiver with ID : " + transactionCreationDTO.getReceiverId() + " not found");

        if (sender.getBalance() < transactionCreationDTO.getAmount())
            throw new RuntimeException("Insufficient balance");

        Transaction transaction = new Transaction();
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(transactionCreationDTO.getAmount());
        transaction.setDescription(transactionCreationDTO.getDescription());

        sender.setBalance(sender.getBalance() - transactionCreationDTO.getAmount());
        receiver.setBalance(receiver.getBalance() + transactionCreationDTO.getAmount());

        userRepository.save(sender);
        userRepository.save(receiver);

        return transactionRepository.save(transaction);
    }
}
