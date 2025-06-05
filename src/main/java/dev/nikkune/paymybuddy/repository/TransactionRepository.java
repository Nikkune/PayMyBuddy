package dev.nikkune.paymybuddy.repository;

import dev.nikkune.paymybuddy.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    /**
     * Retrieves a list of transactions where the specified user is the sender.
     *
     * @param senderId the ID of the user who initiated the transactions
     * @return a list of transactions sent by the user with the given sender ID
     */
    List<Transaction> findBySenderId(int senderId);

    /**
     * Retrieves a list of transactions where the specified user is the receiver.
     *
     * @param receiverId the ID of the user who received the transactions
     * @return a list of transactions received by the user with the given receiver ID
     */
    List<Transaction> findByReceiverId(int receiverId);

    /**
     * Retrieves a list of transactions where the specified user is either the sender or the receiver.
     *
     * @param senderId the ID of the user who initiated the transactions
     * @param receiverId the ID of the user who received the transactions
     * @return a list of transactions involving the user as either the sender or the receiver
     */
    List<Transaction> findBySenderIdOrReceiverId(int senderId, int receiverId);
}
