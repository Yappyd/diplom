package com.yappyd.userservice.controller;

import com.yappyd.userservice.dto.responce.ErrorResponse;
import com.yappyd.userservice.exception.InvalidProfileDataException;
import com.yappyd.userservice.exception.ProfileAlreadyCompletedException;
import com.yappyd.userservice.exception.ProfileNotCompletedException;
import com.yappyd.userservice.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({InvalidProfileDataException.class, MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> handleInvalidProfileDataException(Exception e, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                "INVALID_REQUEST",
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ProfileAlreadyCompletedException.class)
    public ResponseEntity<ErrorResponse> handleProfileAlreadyCompletedException(ProfileAlreadyCompletedException e, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                "PROFILE_ALREADY_COMPLETED",
                e.getMessage(),
                HttpStatus.CONFLICT.value(),
                request.getRequestURI(),
                Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(ProfileNotCompletedException.class)
    public ResponseEntity<ErrorResponse> handleProfileNotCompletedException(ProfileNotCompletedException e, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                "PROFILE_NOT_COMPLETED",
                e.getMessage(),
                HttpStatus.CONFLICT.value(),
                request.getRequestURI(),
                Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException e, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                "USER_NOT_FOUND",
                e.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                request.getRequestURI(),
                Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "Unexpected error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI(),
                Instant.now());

        log.error("Unexpected error on {} {}", request.getMethod(), request.getRequestURI(), e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}