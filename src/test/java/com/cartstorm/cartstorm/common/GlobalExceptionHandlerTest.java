package com.cartstorm.cartstorm.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldPreserveStatusFromResponseStatusException() {
        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Product not found"
        );

        ProblemDetail result =
                handler.handleResponseStatusException(exception);

        assertThat(result.getStatus()).isEqualTo(404);
        assertThat(result.getTitle()).isEqualTo("Request failed");
        assertThat(result.getDetail()).isEqualTo("Product not found");
    }

    @Test
    void shouldReturnConflictForDataIntegrityViolation() {
        DataIntegrityViolationException exception =
                new DataIntegrityViolationException("Internal database detail");

        ProblemDetail result =
                handler.handleDataIntegrityException(exception);

        assertThat(result.getStatus()).isEqualTo(409);
        assertThat(result.getTitle()).isEqualTo("Data conflict");
        assertThat(result.getDetail())
                .isEqualTo("The request conflicts with existing data.");
        assertThat(result.getDetail())
                .doesNotContain("Internal database detail");
    }

    @Test
    void shouldReturnGenericMessageForUnexpectedException() {
        Exception exception =
                new RuntimeException("Sensitive internal detail");

        ProblemDetail result =
                handler.handleUnexpectedException(exception);

        assertThat(result.getStatus()).isEqualTo(500);
        assertThat(result.getTitle()).isEqualTo("Internal server error");
        assertThat(result.getDetail())
                .isEqualTo("An unexpected error occurred. Please try again later.");
        assertThat(result.getDetail())
                .doesNotContain("Sensitive internal detail");
    }

    @Test
    void shouldReturnBadRequestForValidationFailure() {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        var fieldError = new org.springframework.validation.FieldError(
                "request",
                "name",
                "",
                false,
                null,
                null,
                "Name must not be blank"
        );

        var bindingResult =
                mock(org.springframework.validation.BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors())
                .thenReturn(java.util.List.of(fieldError));

        ProblemDetail result =
                handler.handleValidationException(exception);

        assertThat(result.getStatus()).isEqualTo(400);
        assertThat(result.getTitle()).isEqualTo("Validation failed");
        assertThat(result.getDetail())
                .isEqualTo("name: Name must not be blank");
    }
}