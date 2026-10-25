package com.expensetracker.dto;

import java.math.BigDecimal;

/**
 * Dashboard numbers: total spent, how many expenses, and the average.
 */
public record ExpenseSummary(BigDecimal totalAmount, long transactionCount, BigDecimal averageAmount) {
}
