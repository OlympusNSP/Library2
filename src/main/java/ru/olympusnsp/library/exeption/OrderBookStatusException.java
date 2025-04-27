package ru.olympusnsp.library.exeption;

public class OrderBookStatusException extends RuntimeException {
    public OrderBookStatusException(String message) {
        super(message);
    }
}
