package com.example.cloudfilestorage.Controller;

import com.example.cloudfilestorage.DTO.ErrorResponse;
import com.example.cloudfilestorage.Exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidResourcePathException.class)
    public ResponseEntity<ErrorResponse> handleResourcePath() {
        ErrorResponse error = new ErrorResponse("Invalid or empty path");

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleDefaultExceptions(RuntimeException e){
        ErrorResponse error = new ErrorResponse(e.getMessage());

        return ResponseEntity.internalServerError().body(error);
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> userAlreadyExisttExceptions(UserAlreadyExistException e){
        ErrorResponse error = new ErrorResponse(e.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ResourceAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> resourceAlreadyExistException(){
        ErrorResponse error = new ErrorResponse("Resource by this path already exist");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ParentPathNotExistException.class)
    public ResponseEntity<ErrorResponse> parentPathNotExistException(){
        ErrorResponse error = new ErrorResponse("Path is not exist");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }


    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedUser(){
        ErrorResponse errorResponse = new ErrorResponse("User is not authorized");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(ResourceNotExistException.class)
    public ResponseEntity<ErrorResponse> handleInvalidResourcePath() {
        ErrorResponse error = new ErrorResponse("File is not exist");

        return ResponseEntity.badRequest().body(error);
    }

}
