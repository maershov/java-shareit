package ru.practicum.shareit.exceptions;

public class UserHaveNotAccessException extends RuntimeException {

    public UserHaveNotAccessException(String message) {
        super(message);
    }
}
