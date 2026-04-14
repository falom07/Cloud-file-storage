package com.example.cloudfilestorage.Controller;

import com.example.cloudfilestorage.DTO.ErrorResponse;
import com.example.cloudfilestorage.Exception.InvalidResourcePathException;
import com.example.cloudfilestorage.Exception.ResourceNotExistException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

@org.springframework.web.bind.annotation.RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidResourcePathException.class)
    public ResponseEntity<ErrorResponse> handleResourcePath() {
        ErrorResponse error = new ErrorResponse("Invalid or is empty path");

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleDefaultExceptions(RuntimeException e){
        ErrorResponse error = new ErrorResponse(e.getMessage());

        return ResponseEntity.internalServerError().body(error);
    }

    @ExceptionHandler(ResourceNotExistException.class)
    public ResponseEntity<ErrorResponse> handleInvalidResourcePath() {
        ErrorResponse error = new ErrorResponse("File not found");

        return ResponseEntity.badRequest().body(error);
    }

}
