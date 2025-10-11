package com.lms.exception;


import com.lms.dto.ErrorResponseDto;
import org.apache.coyote.BadRequestException;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                request.getDescription(false),
                HttpStatus.UNAUTHORIZED,
                HttpStatus.UNAUTHORIZED.value(),
                "You're not authorized to make this request",
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateResource(DuplicateResourceException ex, WebRequest webRequest) {
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.CONFLICT,
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponseDto> handleMissingServletRequestPartException(MissingServletRequestPartException ex, WebRequest webRequest) {
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> resourceNotFound(ResourceNotFoundException ex, WebRequest webRequest) {
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.NOT_FOUND,
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponseDto);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> noResourceFoundException(NoResourceFoundException ex, WebRequest webRequest) {
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.NOT_FOUND,
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponseDto);
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> dataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest webRequest) {
        String errorMessage = "Data integrity violation";

        Throwable cause = ex.getCause();
        if (cause instanceof ConstraintViolationException constraintViolationException) {
            String sqlMessage = constraintViolationException.getSQLException().getMessage();

            // Better way: check specific constraint name
            String constraintName = constraintViolationException.getConstraintName();
            if (constraintName != null && constraintName.contains("UK6dotkott2kjsp8vw4d0m25fb7")) {
                errorMessage = "Email already exists. Please use a different email.";
            } else if (sqlMessage != null && sqlMessage.contains("Duplicate entry")) {
                errorMessage = "Duplicate value detected for a unique field.";
            } else {
                errorMessage = sqlMessage;
            }
        }

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.CONFLICT,
                HttpStatus.CONFLICT.value(),
                errorMessage,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponseDto);
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> globalExceptionHandler(Exception ex, WebRequest webRequest) {
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponseDto);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> apiExceptionHandler(ApiException ex, WebRequest webRequest) {
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponseDto);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> disabledException(DisabledException ex, WebRequest webRequest) {
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.FORBIDDEN,
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponseDto);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex, WebRequest webRequest) {
        Map<String, String> errors = new HashMap<>();
        ex.getAllErrors().forEach( error -> {
            String fieldName = ((FieldError) error ).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.FORBIDDEN,
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        Map<String, Object> finalMessage = new HashMap<>();
        finalMessage.put("errorDto", errorResponseDto);
        finalMessage.put("fieldErrors", errors);
        return ResponseEntity.badRequest().body(finalMessage);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex, WebRequest webRequest) {
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HtmlUtils.htmlEscape(webRequest.getDescription(false)),
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.value(),
                HtmlUtils.htmlEscape(ex.getMessage()),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SamePasswordException.class)
    public ResponseEntity<?> samePasswordException(SamePasswordException ex, WebRequest webRequest) {
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponseDto);
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    public ResponseEntity<?> incorrectPasswordException(IncorrectPasswordException ex, WebRequest webRequest) {
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponseDto);
    }

    @ExceptionHandler(SamePositionException.class)
    public ResponseEntity<?> samePositionException(SamePositionException ex, WebRequest webRequest) {
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponseDto);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> badRequestException(BadRequestException ex, WebRequest webRequest) {
        String description = webRequest.getDescription(false);
        if (description.contains("/stream/")) {
            log.error("Error occurred during video streaming: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.TEXT_PLAIN)
                    .build();
        }
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                description,
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponseDto);
    }

}
