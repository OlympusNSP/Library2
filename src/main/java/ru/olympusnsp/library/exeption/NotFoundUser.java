package ru.olympusnsp.library.exeption;

public class NotFoundUser extends RuntimeException {
    public NotFoundUser(String message) {
        super(message);
    }
}
