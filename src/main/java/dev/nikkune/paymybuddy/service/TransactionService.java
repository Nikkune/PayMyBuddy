package dev.nikkune.paymybuddy.service;

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
@Transactional
public class TransactionService {
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
     * Retrieves a list of all transactions stored in the system.
     *
     * @return a list of Transaction objects representing all transactions.
     */
    public List<Transaction> getAllTransactions(){
        return transactionRepository.findAll();
    }

    /**
     * Checks if a user with the given ID exists in the user repository.
     *
     * @param id the ID of the user to retrieve
     * @return the User object if found, otherwise null
     */
    public User exists(int id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * Verifies if a user with the given ID exists. If the user does not exist, a RuntimeException is thrown.
     *
     * @param id the unique identifier of the user to be verified
     * @throws RuntimeException if the user with the specified ID does not exist
     */
    public void requiredUser(int id) throws RuntimeException {
        User existingUser = exists(id);
        if (existingUser == null)
            throw new RuntimeException("User with ID : " + id + " does not exist");

    }

    /**
     * Retrieves a transaction by its unique identifier.
     *
     * @param id the unique identifier of the transaction to retrieve
     * @return the {@code Transaction} object corresponding to the provided ID
     * @throws RuntimeException if no transaction is found with the given ID
     */
    public Transaction getTransactionById(int id) throws RuntimeException {
        Transaction existingTransaction = transactionRepository.findById(id).orElse(null);
        if (existingTransaction == null)
            throw new RuntimeException("Transaction with ID : " + id + " does not exist");

        return existingTransaction;
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
        List<Transaction> transactions = transactionRepository.findBySenderId(userId);
        transactions.addAll(transactionRepository.findByReceiverId(userId));

        return transactions;
    }

    /**
     * Retrieves a list of transactions made by one user to another user.
     * This method verifies the existence of both users before fetching the data.
     *
     * @param senderId the ID of the user who initiated the transactions
     * @param receiverId the ID of the user who received the transactions
     * @return a list of transactions sent by the user with the given sender ID to the user with the given receiver ID
     * @throws RuntimeException if either sender or receiver does not exist
     */
    public List<Transaction> getTransactionsBetweenUsers(int senderId, int receiverId){
        requiredUser(senderId);
        requiredUser(receiverId);
        List<Transaction> transactions = transactionRepository.findBySenderId(senderId);
        return transactions.stream().filter(transaction -> transaction.getReceiver().getId() == receiverId).toList();
    }

    /**
     * Adds a new transaction to the system after validating that both the sender and receiver exist.
     *
     * @param transaction the transaction to be added, which includes sender, receiver, amount, and description
     * @return the saved transaction object
     * @throws RuntimeException if the sender or receiver does not exist
     */
    public Transaction addTransaction(Transaction transaction) throws RuntimeException {
        requiredUser(transaction.getSender().getId());
        requiredUser(transaction.getReceiver().getId());
        return transactionRepository.save(transaction);
    }
}
