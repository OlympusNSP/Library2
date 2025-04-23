package ru.olympusnsp.library.exeption;

public class BookCountExcessException extends RuntimeException {
    public BookCountExcessException(String message) {
        super(message);
    }
}
