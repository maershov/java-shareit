package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

public interface UserRepository {
    List<User> findAllUsers();

    User createUser(User user);

    User updateUser(User user);

    User getUserById(int userId);

    void deleteUser(int userId);
}
