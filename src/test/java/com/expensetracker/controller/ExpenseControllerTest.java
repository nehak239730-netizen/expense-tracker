package com.expensetracker.controller;

import com.expensetracker.model.Expense;
import com.expensetracker.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack API tests: real controllers + service + JPA, using in-memory H2.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExpenseRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    private Expense save(String title, String amount, String category, LocalDate date) {
        Expense e = new Expense();
        e.setTitle(title);
        e.setAmount(new BigDecimal(amount));
        e.setCategory(category);
        e.setDate(date);
        return repository.save(e);
    }

    private String body(String title, String amount, String category, LocalDate date) {
        return """
                {"title":"%s","amount":%s,"category":"%s","date":"%s","description":"test note"}
                """.formatted(title, amount, category, date);
    }

    @Test
    void createExpense_withValidData_returns201() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Lunch", "250.50", "Food", LocalDate.now())))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Lunch"))
                .andExpect(jsonPath("$.category").value("Food"));
    }

    @Test
    void createExpense_withInvalidData_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("", "-5", "Food", LocalDate.now())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.amount").exists());
    }

    @Test
    void createExpense_withFutureDate_returns400() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Trip", "100", "Travel", LocalDate.now().plusDays(3))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.date").exists());
    }

    @Test
    void createExpense_withMalformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_whenMissing_returns404() throws Exception {
        mockMvc.perform(get("/api/expenses/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getById_withInvalidId_returns400() throws Exception {
        mockMvc.perform(get("/api/expenses/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_returnsSavedExpenses() throws Exception {
        save("Tea", "20.00", "Food", LocalDate.now());
        save("Bus", "40.00", "Transport", LocalDate.now());

        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void updateExpense_changesValues() throws Exception {
        Expense saved = save("Old", "10.00", "Food", LocalDate.now());

        mockMvc.perform(put("/api/expenses/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("New title", "99.99", "Bills", LocalDate.now())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New title"))
                .andExpect(jsonPath("$.amount").value(99.99))
                .andExpect(jsonPath("$.category").value("Bills"));
    }

    @Test
    void updateExpense_whenMissing_returns404() throws Exception {
        mockMvc.perform(put("/api/expenses/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("X", "10", "Food", LocalDate.now())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteExpense_returns204ThenExpenseIsGone() throws Exception {
        Expense saved = save("Temp", "5.00", "Other", LocalDate.now());

        mockMvc.perform(delete("/api/expenses/" + saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/expenses/" + saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void filter_byDateRangeAndCategory() throws Exception {
        save("A", "100.00", "Food", LocalDate.of(2026, 10, 5));
        save("B", "200.00", "Food", LocalDate.of(2026, 10, 15));
        save("C", "300.00", "Transport", LocalDate.of(2026, 10, 15));
        save("D", "400.00", "Food", LocalDate.of(2026, 9, 1));

        // Date range only: A, B, C
        mockMvc.perform(get("/api/expenses/filter").param("start", "2026-10-01").param("end", "2026-10-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        // Date range + category (case-insensitive): A, B
        mockMvc.perform(get("/api/expenses/filter")
                        .param("start", "2026-10-01").param("end", "2026-10-31").param("category", "food"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void filter_withStartAfterEnd_returns400() throws Exception {
        mockMvc.perform(get("/api/expenses/filter").param("start", "2026-10-20").param("end", "2026-10-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Start date must not be after end date"));
    }

    @Test
    void filter_withBadDateFormat_returns400() throws Exception {
        mockMvc.perform(get("/api/expenses/filter").param("start", "20-10-2026"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void summary_returnsTotalCountAndAverage() throws Exception {
        save("A", "100.00", "Food", LocalDate.now());
        save("B", "50.00", "Food", LocalDate.now());

        mockMvc.perform(get("/api/expenses/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(150.0))
                .andExpect(jsonPath("$.transactionCount").value(2))
                .andExpect(jsonPath("$.averageAmount").value(75.0));
    }

    @Test
    void unknownUrl_returns404Json() throws Exception {
        mockMvc.perform(get("/api/does-not-exist"))
                .andExpect(status().isNotFound());
    }
}
