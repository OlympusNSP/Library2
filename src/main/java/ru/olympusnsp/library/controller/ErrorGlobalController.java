package ru.olympusnsp.library.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.olympusnsp.library.exeption.*;
import ru.olympusnsp.library.model.ErrorItem;

@RestControllerAdvice
@Hidden
public class ErrorGlobalController {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundEntity.class)
    public ErrorItem handleException(NotFoundEntity entity) {
        return new ErrorItem(entity.getMessage(), HttpStatus.NOT_FOUND.value());
    }

    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ExceptionHandler(SearchStringTooSmall.class)
    public ErrorItem handleException(SearchStringTooSmall entity) {
        return new ErrorItem(entity.getMessage(), HttpStatus.NOT_ACCEPTABLE.value());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(BookCountExcessException.class)
    public ErrorItem handleException(BookCountExcessException entity) {
        return new ErrorItem(entity.getMessage(), HttpStatus.FORBIDDEN.value());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(BookUnavailableException.class)
    public ErrorItem handleException(BookUnavailableException entity) {
        return new ErrorItem(entity.getMessage(), HttpStatus.FORBIDDEN.value());
    }
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundUser.class)
    public ErrorItem handleException(NotFoundUser entity) {
        return new ErrorItem(entity.getMessage(), HttpStatus.NOT_FOUND.value());
    }



}