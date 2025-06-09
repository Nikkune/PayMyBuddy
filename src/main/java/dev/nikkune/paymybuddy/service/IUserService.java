package dev.nikkune.paymybuddy.service;

import dev.nikkune.paymybuddy.model.User;

import java.util.List;

public interface IUserService {
    User requiredUser(int id) throws RuntimeException;

    User getUserByEmail(String email) throws RuntimeException;

    User register(User user) throws RuntimeException;

    User updateUser(User user) throws RuntimeException;

    boolean login(String email, String password) throws RuntimeException;

    List<User> getConnections(int userId) throws RuntimeException;

    List<User> addConnection(int userId, String email) throws RuntimeException;
}
