(() => {
    'use strict';

    const API_URL = '/api/expenses';
    const money = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' });

    const $ = (id) => document.getElementById(id);
    const els = {
        form: $('expense-form'),
        formTitle: $('form-title'),
        title: $('title'),
        amount: $('amount'),
        date: $('date'),
        category: $('category'),
        description: $('description'),
        formError: $('form-error'),
        saveBtn: $('save-btn'),
        cancelBtn: $('cancel-btn'),
        filterForm: $('filter-form'),
        filterStart: $('filter-start'),
        filterEnd: $('filter-end'),
        filterCategory: $('filter-category'),
        clearFilters: $('clear-filters'),
        rows: $('expense-rows'),
        statTotal: $('stat-total'),
        statCount: $('stat-count'),
        statAverage: $('stat-average'),
        toast: $('toast')
    };

    let editingId = null;
    let toastTimer = null;

    /* ---------- Fetch helper ---------- */
    async function api(path = '', options = {}) {
        const response = await fetch(API_URL + path, {
            headers: { 'Content-Type': 'application/json' },
            ...options
        });

        if (response.status === 204) {
            return null;
        }

        let body = null;
        try {
            body = await response.json();
        } catch (ignored) {
            // Response had no JSON body.
        }

        if (!response.ok) {
            const error = new Error((body && body.message) || 'Request failed (' + response.status + ')');
            error.fieldErrors = (body && body.fieldErrors) || null;
            throw error;
        }
        return body;
    }

    /* ---------- Small helpers ---------- */
    function todayString() {
        const now = new Date();
        const month = String(now.getMonth() + 1).padStart(2, '0');
        const day = String(now.getDate()).padStart(2, '0');
        return now.getFullYear() + '-' + month + '-' + day;
    }

    function formatDate(isoDate) {
        const [year, month, day] = isoDate.split('-').map(Number);
        return new Date(year, month - 1, day)
            .toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
    }

    function categoryColor(name) {
        let hash = 0;
        for (const ch of name.toLowerCase()) {
            hash = (hash * 31 + ch.charCodeAt(0)) % 360;
        }
        return 'hsl(' + hash + ' 55% 40%)';
    }

    function el(tag, className, text) {
        const node = document.createElement(tag);
        if (className) node.className = className;
        if (text !== undefined) node.textContent = text; // textContent => safe from XSS
        return node;
    }

    function showToast(message, isError = false) {
        els.toast.textContent = message;
        els.toast.className = 'toast' + (isError ? ' error' : '');
        els.toast.hidden = false;
        clearTimeout(toastTimer);
        toastTimer = setTimeout(() => { els.toast.hidden = true; }, 3500);
    }

    function clearFormErrors() {
        els.formError.hidden = true;
        els.formError.textContent = '';
        [els.title, els.amount, els.date, els.category, els.description]
            .forEach((input) => input.classList.remove('invalid'));
    }

    function showFormError(error) {
        const messages = error.fieldErrors ? Object.values(error.fieldErrors) : [error.message];
        els.formError.textContent = messages.join(' ');
        els.formError.hidden = false;
        if (error.fieldErrors) {
            Object.keys(error.fieldErrors).forEach((field) => {
                if (els[field]) els[field].classList.add('invalid');
            });
        }
    }

    /* ---------- Filters ---------- */
    function filterQuery() {
        const params = new URLSearchParams();
        if (els.filterStart.value) params.set('start', els.filterStart.value);
        if (els.filterEnd.value) params.set('end', els.filterEnd.value);
        if (els.filterCategory.value.trim()) params.set('category', els.filterCategory.value.trim());
        return params.toString();
    }

    /* ---------- Load + render ---------- */
    async function refresh() {
        const query = filterQuery();
        try {
            const [expenses, summary] = await Promise.all([
                api(query ? '/filter?' + query : ''),
                api('/summary' + (query ? '?' + query : ''))
            ]);
            renderRows(expenses);
            renderSummary(summary);
        } catch (error) {
            showToast(error.message, true);
        }
    }

    function renderSummary(summary) {
        els.statTotal.textContent = money.format(summary.totalAmount);
        els.statCount.textContent = String(summary.transactionCount);
        els.statAverage.textContent = money.format(summary.averageAmount);
    }

    function renderRows(expenses) {
        els.rows.replaceChildren();

        if (expenses.length === 0) {
            const tr = el('tr');
            const td = el('td', 'empty', 'No expenses found. Add one using the form, or clear the filters.');
            td.colSpan = 5;
            tr.append(td);
            els.rows.append(tr);
            return;
        }

        for (const expense of expenses) {
            const tr = el('tr');
            if (expense.id === editingId) tr.className = 'editing';

            const dateCell = el('td', 'date', formatDate(expense.date));

            const titleCell = el('td');
            titleCell.append(el('div', 'expense-title', expense.title));
            if (expense.description) {
                titleCell.append(el('div', 'expense-note', expense.description));
            }

            const categoryCell = el('td');
            const chip = el('span', 'chip', expense.category);
            chip.style.setProperty('--dot', categoryColor(expense.category));
            categoryCell.append(chip);

            const amountCell = el('td', 'num', money.format(expense.amount));

            const actionCell = el('td');
            const actions = el('div', 'row-actions');
            const editBtn = el('button', 'link-btn', 'Edit');
            editBtn.type = 'button';
            editBtn.setAttribute('aria-label', 'Edit ' + expense.title);
            editBtn.addEventListener('click', () => startEdit(expense.id));
            const deleteBtn = el('button', 'link-btn danger', 'Delete');
            deleteBtn.type = 'button';
            deleteBtn.setAttribute('aria-label', 'Delete ' + expense.title);
            deleteBtn.addEventListener('click', () => removeExpense(expense));
            actions.append(editBtn, deleteBtn);
            actionCell.append(actions);

            tr.append(dateCell, titleCell, categoryCell, amountCell, actionCell);
            els.rows.append(tr);
        }
    }

    /* ---------- Create / update ---------- */
    async function saveExpense(event) {
        event.preventDefault();
        clearFormErrors();

        const payload = {
            title: els.title.value.trim(),
            amount: els.amount.value === '' ? null : Number(els.amount.value),
            category: els.category.value.trim(),
            date: els.date.value || null,
            description: els.description.value.trim()
        };

        els.saveBtn.disabled = true;
        try {
            if (editingId === null) {
                await api('', { method: 'POST', body: JSON.stringify(payload) });
                showToast('Expense saved');
            } else {
                await api('/' + editingId, { method: 'PUT', body: JSON.stringify(payload) });
                showToast('Expense updated');
            }
            resetForm();
            await refresh();
        } catch (error) {
            showFormError(error);
        } finally {
            els.saveBtn.disabled = false;
        }
    }

    async function startEdit(id) {
        try {
            const expense = await api('/' + id); // GET /api/expenses/{id}
            editingId = expense.id;
            els.title.value = expense.title;
            els.amount.value = expense.amount;
            els.date.value = expense.date;
            els.category.value = expense.category;
            els.description.value = expense.description || '';
            els.formTitle.textContent = 'Edit expense';
            els.saveBtn.textContent = 'Update expense';
            els.cancelBtn.hidden = false;
            clearFormErrors();
            els.title.focus();
            await refresh();
        } catch (error) {
            showToast(error.message, true);
        }
    }

    function resetForm() {
        editingId = null;
        els.form.reset();
        els.date.value = todayString();
        els.formTitle.textContent = 'Add expense';
        els.saveBtn.textContent = 'Save expense';
        els.cancelBtn.hidden = true;
        clearFormErrors();
    }

    /* ---------- Delete ---------- */
    async function removeExpense(expense) {
        if (!window.confirm('Delete "' + expense.title + '"? This cannot be undone.')) {
            return;
        }
        try {
            await api('/' + expense.id, { method: 'DELETE' });
            if (editingId === expense.id) resetForm();
            showToast('Expense deleted');
            await refresh();
        } catch (error) {
            showToast(error.message, true);
        }
    }

    /* ---------- Wire up events ---------- */
    els.form.addEventListener('submit', saveExpense);
    els.cancelBtn.addEventListener('click', () => { resetForm(); refresh(); });
    els.filterForm.addEventListener('submit', (event) => { event.preventDefault(); refresh(); });
    els.clearFilters.addEventListener('click', () => {
        els.filterStart.value = '';
        els.filterEnd.value = '';
        els.filterCategory.value = '';
        refresh();
    });

    els.date.max = todayString();
    resetForm();
    refresh();
})();
