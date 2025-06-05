package dev.nikkune.paymybuddy.service;

import dev.nikkune.paymybuddy.dto.TransactionCreationDTO;
import dev.nikkune.paymybuddy.model.Transaction;

import java.util.List;

public interface ITransactionService {
    List<Transaction> getAllTransactions();

    void requiredUser(int id) throws RuntimeException;

    Transaction getTransactionById(int id) throws RuntimeException;

    List<Transaction> getTransactionsByUserId(int userId) throws RuntimeException;

    List<Transaction> getTransactionsBetweenUsers(int senderId, int receiverId);

    Transaction addTransaction(TransactionCreationDTO transactionCreationDTO) throws RuntimeException;
}
