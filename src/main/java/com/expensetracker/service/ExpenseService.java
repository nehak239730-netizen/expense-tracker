package com.expensetracker.service;

import com.expensetracker.dto.ExpenseRequest;
import com.expensetracker.dto.ExpenseSummary;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.model.Expense;
import com.expensetracker.repository.ExpenseRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Business logic lives here. Controllers stay thin; repositories only talk to the DB.
 */
@Service
public class ExpenseService {

    // Newest expense first.
    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "date")
            .and(Sort.by(Sort.Direction.DESC, "id"));

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Transactional(readOnly = true)
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll(NEWEST_FIRST);
    }

    @Transactional(readOnly = true)
    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
    }

    @Transactional
    public Expense createExpense(ExpenseRequest request) {
        Expense expense = new Expense();
        copyValues(request, expense);
        return expenseRepository.save(expense);
    }

    @Transactional
    public Expense updateExpense(Long id, ExpenseRequest request) {
        Expense expense = getExpenseById(id);
        copyValues(request, expense);
        return expenseRepository.save(expense);
    }

    @Transactional
    public void deleteExpense(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expense not found with id: " + id);
        }
        expenseRepository.deleteById(id);
    }

    /**
     * start, end and category are all optional. Only the ones provided are applied.
     */
    @Transactional(readOnly = true)
    public List<Expense> filterExpenses(LocalDate start, LocalDate end, String category) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new BadRequestException("Start date must not be after end date");
        }

        Specification<Expense> spec = (root, query, cb) -> cb.conjunction();

        if (start != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), start));
        }
        if (end != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), end));
        }
        if (category != null && !category.isBlank()) {
            String wanted = category.trim().toLowerCase();
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("category")), wanted));
        }

        return expenseRepository.findAll(spec, NEWEST_FIRST);
    }

    @Transactional(readOnly = true)
    public ExpenseSummary getSummary(LocalDate start, LocalDate end, String category) {
        List<Expense> expenses = filterExpenses(start, end, category);

        BigDecimal total = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        long count = expenses.size();
        BigDecimal average = count == 0
                ? BigDecimal.ZERO.setScale(2)
                : total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);

        return new ExpenseSummary(total, count, average);
    }

    private void copyValues(ExpenseRequest request, Expense expense) {
        expense.setTitle(request.title().trim());
        expense.setAmount(request.amount());
        expense.setCategory(request.category().trim());
        expense.setDate(request.date());
        String description = request.description();
        expense.setDescription(description == null || description.isBlank() ? null : description.trim());
    }
}
