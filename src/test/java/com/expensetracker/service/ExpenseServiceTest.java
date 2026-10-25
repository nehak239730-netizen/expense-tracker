package com.expensetracker.service;

import com.expensetracker.dto.ExpenseSummary;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.model.Expense;
import com.expensetracker.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository repository;

    @InjectMocks
    private ExpenseService service;

    private Expense expense(String amount) {
        Expense e = new Expense();
        e.setTitle("Test");
        e.setCategory("Food");
        e.setDate(LocalDate.now());
        e.setAmount(new BigDecimal(amount));
        return e;
    }

    @Test
    @SuppressWarnings("unchecked")
    void getSummary_calculatesTotalCountAndAverage() {
        when(repository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(expense("100.00"), expense("50.00"), expense("25.00")));

        ExpenseSummary summary = service.getSummary(null, null, null);

        assertEquals(new BigDecimal("175.00"), summary.totalAmount());
        assertEquals(3L, summary.transactionCount());
        assertEquals(new BigDecimal("58.33"), summary.averageAmount());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getSummary_withNoExpenses_returnsZeros() {
        when(repository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of());

        ExpenseSummary summary = service.getSummary(null, null, null);

        assertEquals(new BigDecimal("0.00"), summary.totalAmount());
        assertEquals(0L, summary.transactionCount());
        assertEquals(new BigDecimal("0.00"), summary.averageAmount());
    }

    @Test
    void getExpenseById_whenMissing_throwsNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getExpenseById(1L));
    }

    @Test
    void deleteExpense_whenMissing_throwsNotFoundAndDeletesNothing() {
        when(repository.existsById(5L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.deleteExpense(5L));
        verify(repository, never()).deleteById(any());
    }

    @Test
    void filterExpenses_whenStartAfterEnd_throwsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> service.filterExpenses(LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 1), null));
    }
}
