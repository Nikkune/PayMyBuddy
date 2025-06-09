package dev.nikkune.paymybuddy.service;

import dev.nikkune.paymybuddy.dto.TransactionCreationDTO;
import dev.nikkune.paymybuddy.model.Transaction;

import java.util.List;

public interface ITransactionService {
    void requiredUser(int id) throws RuntimeException;

    List<Transaction> getTransactionsByUserId(int userId) throws RuntimeException;

    Transaction addTransaction(TransactionCreationDTO transactionCreationDTO) throws RuntimeException;
}
