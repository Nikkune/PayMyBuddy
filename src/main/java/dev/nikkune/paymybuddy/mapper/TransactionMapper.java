package dev.nikkune.paymybuddy.mapper;

import dev.nikkune.paymybuddy.dto.TransactionCreationDTO;
import dev.nikkune.paymybuddy.dto.TransactionDTO;
import dev.nikkune.paymybuddy.model.Transaction;
import dev.nikkune.paymybuddy.model.User;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper for the Transaction entity and its DTOs
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {

    /**
     * Converts a Transaction entity to a TransactionDTO
     * @param transaction the Transaction entity
     * @return the TransactionDTO
     */
    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "senderUsername", source = "sender.username")
    @Mapping(target = "receiverId", source = "receiver.id")
    @Mapping(target = "receiverUsername", source = "receiver.username")
    TransactionDTO transactionToTransactionDTO(Transaction transaction);

    /**
     * Converts a list of Transaction entities to a list of TransactionDTOs
     * @param transactions the list of Transaction entities
     * @return the list of TransactionDTOs
     */
    List<TransactionDTO> transactionsToTransactionDTOs(List<Transaction> transactions);

    /**
     * Converts a TransactionCreationDTO to a Transaction entity
     * This method requires the actual User objects to be set separately
     * @param transactionCreationDTO the TransactionCreationDTO
     * @return the Transaction entity with null sender and receiver
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "receiver", ignore = true)
    Transaction transactionCreationDTOToTransaction(TransactionCreationDTO transactionCreationDTO);

    /**
     * Updates a Transaction entity with a sender User
     * @param transaction the Transaction entity to update
     * @param sender the sender User
     */
    @AfterMapping
    default void setSender(@MappingTarget Transaction transaction, User sender) {
        transaction.setSender(sender);
    }

    /**
     * Updates a Transaction entity with a receiver User
     * @param transaction the Transaction entity to update
     * @param receiver the receiver User
     */
    @AfterMapping
    default void setReceiver(@MappingTarget Transaction transaction, User receiver) {
        transaction.setReceiver(receiver);
    }
}