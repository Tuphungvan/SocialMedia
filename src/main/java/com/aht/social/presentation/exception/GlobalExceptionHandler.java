package com.aht.social.presentation.exception;

import com.aht.social.application.dto.response.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Object>> handleBaseException(BaseException e) {
        ApiResponse<Object> response = ApiResponse.error(
            e.getStatusCode(), 
            e.getMessage()
        );
        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }
    
    // Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(err -> 
            errors.put(err.getField(), err.getDefaultMessage())
        );
        
        ApiResponse<Map<String, String>> response = ApiResponse.error(
            400, 
            "Validation failed", 
            errors
        );
        return ResponseEntity.badRequest().body(response);
    }
    
    // IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException e) {
        ApiResponse<Object> response = ApiResponse.error(400, e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException e) {
        ApiResponse<Object> response = ApiResponse.error(403, e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
    
    // Catch-all
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception e) {
        ApiResponse<Object> response = ApiResponse.error(500, "Internal server error");
        return ResponseEntity.internalServerError().body(response);
    }
}