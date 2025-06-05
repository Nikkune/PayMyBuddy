package dev.nikkune.paymybuddy.service;

import dev.nikkune.paymybuddy.model.User;

import java.util.List;

public interface IUserService {
    List<User> getAllUsers();

    User requiredUser(int id) throws RuntimeException;

    User getUserById(int id) throws RuntimeException;

    User getUserByEmail(String email) throws RuntimeException;

    User register(User user) throws RuntimeException;

    User updateUser(User user) throws RuntimeException;

    User updatePassword(int userId, String oldPassword, String newPassword) throws RuntimeException;

    void deleteUser(int id) throws RuntimeException;

    boolean login(String email, String password) throws RuntimeException;

    List<User> getConnections(int userId) throws RuntimeException;

    List<User> addConnection(int userId, int connectionId) throws RuntimeException;

    List<User> removeConnection(int userId, int connectionId) throws RuntimeException;
}
