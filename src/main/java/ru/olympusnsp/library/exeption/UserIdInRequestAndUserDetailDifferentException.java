package ru.olympusnsp.library.exeption;

public class UserIdInRequestAndUserDetailDifferentException extends RuntimeException {
    public UserIdInRequestAndUserDetailDifferentException(String message) {
        super(message);
    }
}
