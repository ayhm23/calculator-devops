package com.spe.calculator.exception;

import com.spe.calculator.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex,
                                                            HttpServletRequest request) {
        return badRequest("Required parameter '" + ex.getParameterName() + "' is missing", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest request) {
        return badRequest("Parameter '" + ex.getName() + "' must be a number, got '" + ex.getValue() + "'", request);
    }

    // DivisionByZeroException is an ArithmeticException, so it lands here too
    @ExceptionHandler({ArithmeticException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadInput(RuntimeException ex, HttpServletRequest request) {
        return badRequest(ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> badRequest(String message, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse body = new ErrorResponse(status.value(), status.getReasonPhrase(), message,
                request.getRequestURI(), Instant.now());
        return ResponseEntity.status(status).body(body);
    }
}
