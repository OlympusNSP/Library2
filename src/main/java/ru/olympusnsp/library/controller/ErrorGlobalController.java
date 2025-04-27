package ru.olympusnsp.library.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.olympusnsp.library.dto.ErrorList;
import ru.olympusnsp.library.exeption.*;
import ru.olympusnsp.library.dto.ErrorItem;

import java.util.List;
import java.util.stream.Collectors;

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
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorList methodArgumentNotValidException(MethodArgumentNotValidException exception ) {
        var list = exception.getBindingResult().getFieldErrors()
                .stream().map(FieldError::getDefaultMessage).collect(Collectors.toList());
        return new ErrorList(list,HttpStatus.BAD_REQUEST.value());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UserIdInRequestAndUserDetailDifferentException.class)
    public ErrorItem handleException(UserIdInRequestAndUserDetailDifferentException entity) {
        return new ErrorItem(entity.getMessage(), HttpStatus.BAD_REQUEST.value());
    }


}