package com.yappyd.messageservice.controller;

import com.yappyd.messageservice.dto.response.ErrorResponse;
import com.yappyd.messageservice.exception.ErrorCode;
import com.yappyd.messageservice.exception.MessageServiceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MessageServiceException.class)
    public ResponseEntity<ErrorResponse> handleMessageServiceException(MessageServiceException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        return buildResponse(errorCode, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        if (message.isBlank()) {
            message = "Request validation failed";
        }

        return buildResponse(ErrorCode.VALIDATION_ERROR, message, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));

        if (message.isBlank()) {
            message = "Constraint violation";
        }

        return buildResponse(ErrorCode.VALIDATION_ERROR, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildResponse(ErrorCode.INVALID_REQUEST_BODY, "Invalid request body", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return buildResponse(ErrorCode.INVALID_REQUEST_PARAMETER, "Invalid request parameter: " + ex.getName(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        return buildResponse(ErrorCode.DATA_INTEGRITY_VIOLATION, "Data integrity violation", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return buildResponse(ErrorCode.INTERNAL_ERROR, "Unexpected server error", request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(ErrorCode errorCode, String message, HttpServletRequest request) {
        HttpStatus status = errorCode.status();

        ErrorResponse response = new ErrorResponse(errorCode.code(), message, status.value(), request.getRequestURI(), Instant.now());

        return ResponseEntity.status(status).body(response);
    }
}