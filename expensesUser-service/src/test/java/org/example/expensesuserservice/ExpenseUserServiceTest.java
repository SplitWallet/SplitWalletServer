package org.example.expensesuserservice;

import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.ForbiddenException;
import org.example.expensesuserservice.db.ExpenseUser;
import org.example.expensesuserservice.db.ExpenseUserRepository;
import org.example.expensesuserservice.other.Expense;
import org.example.expensesuserservice.other.Group;
import org.example.expensesuserservice.other.User;
import org.example.expensesuserservice.request.UpdateExpenseParticipantRequest;
import org.example.expensesuserservice.request.UpdatePaidAmountRequest;
import org.example.expensesuserservice.service.ExpenseUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExpenseUserServiceTest {

    @Mock
    private ExpenseUserRepository expenseUserRepository;

    @InjectMocks
    private ExpenseUserService expenseUserService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getExpenseUsers_shouldReturnList_whenExpenseExists() {
        String userId = "user123";
        Long expenseId = 1L;
        Group group = new Group();
        group.setIsClosed(false);

        User user = new User(userId);
        group.setMembers(List.of(user)); // Добавляем пользователя в группу

        Expense expense = new Expense();
        expense.setGroup(group);

        ExpenseUser expenseUser = new ExpenseUser();
        expenseUser.setExpense(expense);
        expenseUser.setUser(user);
        expenseUser.setAmount(new BigDecimal("100.00"));
        expenseUser.setPaid(new BigDecimal("50.00"));

        when(expenseUserRepository.findByExpenseId(expenseId)).thenReturn(List.of(expenseUser));

        List<ExpenseUser> result = expenseUserService.getExpenseUsers(expenseId, userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getId()).isEqualTo(userId);
    }


    @Test
    void getExpenseUsers_shouldThrow_whenGroupIsClosed() {
        String userId = "user123";
        Long expenseId = 1L;
        Group group = new Group();
        group.setIsClosed(true);

        Expense expense = new Expense();
        expense.setGroup(group);

        ExpenseUser expenseUser = new ExpenseUser();
        expenseUser.setExpense(expense);
        expenseUser.setUser(new User(userId));

        when(expenseUserRepository.findByExpenseId(expenseId)).thenReturn(List.of(expenseUser));

        assertThatThrownBy(() -> expenseUserService.getExpenseUsers(expenseId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("This group is closed");
    }

    @Test
    void getExpenseUsers_shouldThrow_whenUserNotMemberOfGroup() {
        String userId = "user123";
        Long expenseId = 1L;
        Group group = new Group();
        group.setIsClosed(false);

        Expense expense = new Expense();
        expense.setGroup(group);

        ExpenseUser expenseUser = new ExpenseUser();
        expenseUser.setExpense(expense);
        expenseUser.setUser(new User("anotherUser"));

        when(expenseUserRepository.findByExpenseId(expenseId)).thenReturn(List.of(expenseUser));

        assertThatThrownBy(() -> expenseUserService.getExpenseUsers(expenseId, userId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("You are not a member of this group");
    }

    @Test
    void updateExpense_shouldUpdateParticipantsCorrectly() {
        Long expenseId = 1L;
        String currentUserId = "user123";
        Group group = new Group();
        group.setIsClosed(false);

        group.setMembers(List.of(new User(currentUserId), new User("user1")));

        Expense expense = new Expense();
        expense.setGroup(group);
        expense.setId(expenseId);

        group.setEvents(List.of(expense));

        ExpenseUser existingUser = new ExpenseUser();
        existingUser.setUser(new User("user1"));
        existingUser.setAmount(new BigDecimal("50.00"));
        existingUser.setPaid(new BigDecimal("0.00"));
        existingUser.setExpense(expense);

        UpdateExpenseParticipantRequest request = new UpdateExpenseParticipantRequest();
        request.setUserId("user1");
        request.setAmount(new BigDecimal("100.00"));
        request.setPaid(new BigDecimal("50.00"));

        when(expenseUserRepository.findByExpenseId(expenseId)).thenReturn(List.of(existingUser));
        when(expenseUserRepository.saveAll(anyList())).thenReturn(List.of(existingUser));

        List<ExpenseUser> updatedUsers = expenseUserService.updateExpense(expenseId, List.of(request), currentUserId, group);

        assertThat(updatedUsers).hasSize(1);
        assertThat(updatedUsers.get(0).getAmount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(updatedUsers.get(0).getPaid()).isEqualTo(new BigDecimal("50.00"));
    }

    @Test
    void updateExpense_shouldAddNewParticipant() {
        Long expenseId = 1L;
        String currentUserId = "user123";
        Group group = new Group();
        group.setIsClosed(false);

        group.setMembers(List.of(new User(currentUserId), new User("user2")));

        Expense expense = new Expense();
        expense.setGroup(group);
        expense.setId(expenseId);

        group.setEvents(List.of(expense));

        UpdateExpenseParticipantRequest request = new UpdateExpenseParticipantRequest();
        request.setUserId("user2");
        request.setAmount(new BigDecimal("100.00"));
        request.setPaid(new BigDecimal("100.00"));

        when(expenseUserRepository.findByExpenseId(expenseId)).thenReturn(new ArrayList<>());
        when(expenseUserRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<ExpenseUser> updatedUsers = expenseUserService.updateExpense(expenseId, List.of(request), currentUserId, group);

        assertThat(updatedUsers).hasSize(1);
        assertThat(updatedUsers.get(0).getUser().getId()).isEqualTo("user2");
        assertThat(updatedUsers.get(0).getAmount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(updatedUsers.get(0).getPaid()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void updatePaidAmount_shouldUpdateSuccessfully() {
        Long expenseId = 1L;
        String userId = "user1";
        String currentUserId = "user123";
        Group group = new Group();
        group.setIsClosed(false);

        group.setMembers(List.of(new User(currentUserId), new User(userId)));

        Expense expense = new Expense();
        expense.setGroup(group);
        expense.setId(expenseId);

        group.setEvents(List.of(expense));

        ExpenseUser expenseUser = new ExpenseUser();
        expenseUser.setExpense(expense);
        expenseUser.setUser(new User(userId));
        expenseUser.setPaid(new BigDecimal("0.00"));

        UpdatePaidAmountRequest request = new UpdatePaidAmountRequest();
        request.setPaid(new BigDecimal("50.00"));

        when(expenseUserRepository.findByExpenseIdAndUserId(expenseId, userId)).thenReturn(Optional.of(expenseUser));
        when(expenseUserRepository.save(any(ExpenseUser.class))).thenReturn(expenseUser);

        ExpenseUser updatedExpenseUser = expenseUserService.updatePaidAmount(expenseId, userId, request, currentUserId);

        assertThat(updatedExpenseUser.getPaid()).isEqualTo(new BigDecimal("50.00"));
        verify(expenseUserRepository).save(expenseUser);
    }

    @Test
    void removeExpense_shouldRemoveSuccessfully() {
        Long expenseId = 1L;
        String userId = "user1";
        String currentUserId = "user123";
        Group group = new Group();
        group.setIsClosed(false);

        group.setMembers(List.of(new User(currentUserId), new User(userId)));

        Expense expense = new Expense();
        expense.setGroup(group);
        expense.setId(expenseId);

        group.setEvents(List.of(expense));

        ExpenseUser expenseUser = new ExpenseUser();
        expenseUser.setExpense(expense);
        expenseUser.setUser(new User(userId));

        when(expenseUserRepository.findByExpenseIdAndUserId(expenseId, userId)).thenReturn(Optional.of(expenseUser));

        expenseUserService.removeExpense(expenseId, userId, currentUserId);

        verify(expenseUserRepository).delete(expenseUser);
    }

    @Test
    void removeExpense_shouldThrow_whenUserNotFound() {
        Long expenseId = 1L;
        String userId = "user1";
        String currentUserId = "user123";

        when(expenseUserRepository.findByExpenseIdAndUserId(expenseId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseUserService.removeExpense(expenseId, userId, currentUserId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Expense or User not found");
    }

    @Test
    void removeExpense_shouldThrow_whenGroupIsClosed() {
        Long expenseId = 1L;
        String userId = "user1";
        String currentUserId = "user123";
        Group group = new Group();
        group.setIsClosed(true);

        Expense expense = new Expense();
        expense.setGroup(group);

        ExpenseUser expenseUser = new ExpenseUser();
        expenseUser.setExpense(expense);
        expenseUser.setUser(new User(userId));

        when(expenseUserRepository.findByExpenseIdAndUserId(expenseId, userId)).thenReturn(Optional.of(expenseUser));

        assertThatThrownBy(() -> expenseUserService.removeExpense(expenseId, userId, currentUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("This group is closed");
    }
}