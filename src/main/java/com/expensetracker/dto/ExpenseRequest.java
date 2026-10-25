package com.expensetracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data the client sends when creating or updating an expense.
 * Validation rules live here, so bad input is rejected before it reaches the database.
 */
public record ExpenseRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title must be at most 100 characters")
        String title,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        @Digits(integer = 10, fraction = 2, message = "Amount can have up to 10 digits and 2 decimal places")
        BigDecimal amount,

        @NotBlank(message = "Category is required")
        @Size(max = 50, message = "Category must be at most 50 characters")
        String category,

        @NotNull(message = "Date is required (format YYYY-MM-DD)")
        @PastOrPresent(message = "Date cannot be in the future")
        LocalDate date,

        @Size(max = 500, message = "Description must be at most 500 characters")
        String description
) {
}
