package ru.olympusnsp.library.exeption;

public class SearchStringTooSmall extends RuntimeException {
    public SearchStringTooSmall(String message) {
        super(message);
    }
}
