package org.example.expensesservice;

import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.ForbiddenException;
import org.example.expensesservice.db.Expense;
import org.example.expensesservice.db.ExpenseRepository;
import org.example.expensesservice.other.ExpenseUser;
import org.example.expensesservice.other.Group;
import org.example.expensesservice.other.User;
import org.example.expensesservice.request.CreateExpenseRequest;
import org.example.expensesservice.request.UpdateExpenseRequest;
import org.example.expensesservice.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getExpenses_shouldReturnExpenses() {
        String userId = "user1";
        Long groupId = 1L;
        Group group = new Group();
        group.setMembers(List.of(new User(userId)));

        Expense expense = new Expense();
        expense.setGroup(group);

        when(expenseRepository.getExpensesByGroupId(groupId)).thenReturn(List.of(expense));

        var result = expenseService.getExpenses(userId, groupId);

        assertThat(result).hasSize(1);
    }

    @Test
    void getExpenses_shouldThrow_whenExpensesNotFound() {
        when(expenseRepository.getExpensesByGroupId(1L)).thenReturn(new ArrayList<>());

        assertThatThrownBy(() -> expenseService.getExpenses("user1", 1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getExpenses_shouldThrow_whenUserNotMember() {
        String userId = "user1";
        Group group = new Group();
        group.setMembers(new ArrayList<>());

        Expense expense = new Expense();
        expense.setGroup(group);

        when(expenseRepository.getExpensesByGroupId(1L)).thenReturn(List.of(expense));

        assertThatThrownBy(() -> expenseService.getExpenses(userId, 1L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void createExpense_shouldCreateSuccessfully() {
        String userId = "user1";
        Group group = new Group();
        group.setMembers(List.of(new User(userId)));
        group.setIsClosed(false);

        CreateExpenseRequest request = new CreateExpenseRequest();
        request.setAmount(new BigDecimal("100"));
        request.setDescription("Test expense");
        request.setDate(LocalDate.from(LocalDateTime.now()));
        request.setCurrency("USD");
        request.setName("Lunch");

        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Expense result = expenseService.createExpense(request, userId, group);

        assertThat(result.getAmount()).isEqualByComparingTo("100");
        assertThat(result.getName()).isEqualTo("Lunch");
        assertThat(result.getExpenseUsers()).isNotEmpty();
    }

    @Test
    void createExpense_shouldThrow_whenUserNotMember() {
        String userId = "user1";
        Group group = new Group();
        group.setMembers(new ArrayList<>());

        CreateExpenseRequest request = new CreateExpenseRequest();

        assertThatThrownBy(() -> expenseService.createExpense(request, userId, group))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("don't member of group");
    }

    @Test
    void createExpense_shouldThrow_whenGroupClosed() {
        String userId = "user1";
        Group group = new Group();
        group.setMembers(List.of(new User(userId)));
        group.setIsClosed(true);

        CreateExpenseRequest request = new CreateExpenseRequest();

        assertThatThrownBy(() -> expenseService.createExpense(request, userId, group))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("closed");
    }

    @Test
    void deleteExpense_shouldDeleteSuccessfully() {
        String userId = "owner";
        Long groupId = 1L;
        Long expenseId = 2L;

        Group group = new Group();
        group.setUserOwner(new User(userId));

        Expense expense = new Expense();
        expense.setGroup(group);

        when(expenseRepository.findByIdAndGroupId(expenseId, groupId)).thenReturn(Optional.of(expense));

        expenseService.deleteExpense(expenseId, userId, groupId);

        verify(expenseRepository).delete(expense);
    }

    @Test
    void deleteExpense_shouldThrow_whenExpenseNotFound() {
        when(expenseRepository.findByIdAndGroupId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> expenseService.deleteExpense(1L, "user", 1L));
    }

    @Test
    void deleteExpense_shouldThrow_whenNotOwner() {
        Group group = new Group();
        group.setUserOwner(new User("owner"));

        Expense expense = new Expense();
        expense.setGroup(group);

        when(expenseRepository.findByIdAndGroupId(1L, 1L)).thenReturn(Optional.of(expense));

        assertThrows(IllegalArgumentException.class, () -> expenseService.deleteExpense(1L, "user", 1L));
    }

    @Test
    void updateExpense_shouldUpdateSuccessfully() {
        Long expenseId = 1L;
        Long groupId = 2L;
        String userId = "user1";

        User member = new User(userId);
        Group group = new Group();
        group.setIsClosed(false);
        group.setMembers(List.of(member));

        ExpenseUser expenseUser = new ExpenseUser();
        expenseUser.setAmount(new BigDecimal("10"));

        Expense expense = new Expense();
        expense.setGroup(group);
        expense.setExpenseUsers(List.of(expenseUser));
        expense.setAmount(new BigDecimal("100"));

        UpdateExpenseRequest request = new UpdateExpenseRequest();
        request.setAmount(new BigDecimal("200"));
        request.setDescription("Updated description");
        request.setDate(LocalDate.from(LocalDateTime.now()));
        request.setCurrency("USD");
        request.setName("Updated name");

        when(expenseRepository.findByIdAndGroupId(expenseId, groupId)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Expense result = expenseService.updateExpense(expenseId, request, userId, groupId);

        assertThat(result.getAmount()).isEqualByComparingTo("200");
        assertThat(result.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void updateExpense_shouldThrow_whenGroupClosed() {
        Group group = new Group();
        group.setIsClosed(true);

        Expense expense = new Expense();
        expense.setGroup(group);

        when(expenseRepository.findByIdAndGroupId(1L, 1L)).thenReturn(Optional.of(expense));

        UpdateExpenseRequest request = new UpdateExpenseRequest();

        assertThatThrownBy(() -> expenseService.updateExpense(1L, request, "user1", 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("closed");
    }

    @Test
    void updateExpense_shouldThrow_whenNotMember() {
        Group group = new Group();
        group.setIsClosed(false);
        group.setMembers(new ArrayList<>());

        Expense expense = new Expense();
        expense.setGroup(group);

        when(expenseRepository.findByIdAndGroupId(1L, 1L)).thenReturn(Optional.of(expense));

        UpdateExpenseRequest request = new UpdateExpenseRequest();

        assertThrows(ForbiddenException.class, () -> expenseService.updateExpense(1L, request, "user1", 1L));
    }

    @Test
    void updateExpense_shouldThrow_whenNewAmountLessThanUsersAmount() {
        User member = new User("user1");
        Group group = new Group();
        group.setIsClosed(false);
        group.setMembers(List.of(member));

        ExpenseUser expenseUser = new ExpenseUser();
        expenseUser.setAmount(new BigDecimal("150"));

        Expense expense = new Expense();
        expense.setGroup(group);
        expense.setExpenseUsers(List.of(expenseUser));
        expense.setAmount(new BigDecimal("200"));

        when(expenseRepository.findByIdAndGroupId(1L, 1L)).thenReturn(Optional.of(expense));

        UpdateExpenseRequest request = new UpdateExpenseRequest();
        request.setAmount(new BigDecimal("100"));

        assertThatThrownBy(() -> expenseService.updateExpense(1L, request, "user1", 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be less");
    }
}
