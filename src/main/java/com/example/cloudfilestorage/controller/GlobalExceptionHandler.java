package com.example.cloudfilestorage.controller;

import com.example.cloudfilestorage.dto.ErrorResponse;
import com.example.cloudfilestorage.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidResourcePathException.class)
    public ResponseEntity<ErrorResponse> handleResourcePath(InvalidResourcePathException e) {
        ErrorResponse error = new ErrorResponse(e.getMessage());

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleDefaultExceptions(RuntimeException e){
        ErrorResponse error = new ErrorResponse(e.getMessage());

        return ResponseEntity.internalServerError().body(error);
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> userAlreadyExistExceptions(UserAlreadyExistException e) {
        ErrorResponse error = new ErrorResponse(e.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ResourceAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> resourceAlreadyExistException(ResourceAlreadyExistException e) {
        ErrorResponse error = new ErrorResponse(e.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ParentPathNotExistException.class)
    public ResponseEntity<ErrorResponse> parentPathNotExistException(ParentPathNotExistException e) {
        ErrorResponse error = new ErrorResponse(e.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }


    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedUser(UnauthorizedException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(ResourceNotExistException.class)
    public ResponseEntity<ErrorResponse> handleInvalidResourcePath(ResourceNotExistException e) {
        ErrorResponse error = new ErrorResponse(e.getMessage());

        return ResponseEntity.badRequest().body(error);
    }

}
