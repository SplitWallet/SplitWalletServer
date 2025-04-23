package org.example.splitwalletserver.server.expenseUser.domain;


import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.ForbiddenException;
import lombok.AllArgsConstructor;
import org.example.splitwalletserver.server.expenseUser.db.ExpenseUser;
import org.example.splitwalletserver.server.expenseUser.db.ExpenseUserRepository;
import org.example.splitwalletserver.server.expenses.db.Expense;
import org.example.splitwalletserver.server.expenses.db.ExpenseRepository;
import org.example.splitwalletserver.server.expenses.request.UpdateExpenseParticipantRequest;
import org.example.splitwalletserver.server.groups.db.Group;
import org.example.splitwalletserver.server.groups.db.GroupRepository;
import org.example.splitwalletserver.server.users.services.UserServiceImpl;
import org.example.splitwalletserver.server.users.model.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ExpenseUserService {
    private final ExpenseUserRepository expenseUserRepository;
    private final UserServiceImpl userService;
    private final GroupRepository groupRepository;
    private final ExpenseRepository expenseRepository;

    public List<ExpenseUser> getExpenseUsers(Long groupId, Long expenseId) {
        var currentUser = userService.getCurrentUser();
        var group = groupRepository.findById(groupId).orElseThrow(() -> new EntityNotFoundException("Group with id " + groupId + " not found"));
        if (!group.getMembers().stream().map(User::getId)
                .toList().contains(currentUser.getId())) {
            throw new IllegalArgumentException("You don't member of group with id " + groupId);
        }
        var expense = group.getEvents().stream().filter(event -> event.getId().equals(expenseId)).findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Expense with id " + expenseId + " not found"));
        return expense.getExpenseUsers();
    }

    public List<ExpenseUser> updateExpense(Long groupId, Long expenseId,
                                           List<UpdateExpenseParticipantRequest> requests) {
        Expense expense = validateExpenseAccess(groupId, expenseId);

        BigDecimal totalAmount = requests.stream()
                .map(UpdateExpenseParticipantRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAmount.compareTo(expense.getAmount()) != 0) {
            throw new IllegalArgumentException(
                    "Sum of participants amounts must equal expense amount (" + expense.getAmount() + ")");
        }

        expense.getExpenseUsers().clear();
        for (UpdateExpenseParticipantRequest request : requests) {
            ExpenseUser eu = new ExpenseUser();
            eu.setUser(userService.getUserById(request.getUserId()));
            eu.setExpense(expense);
            eu.setAmount(request.getAmount());
            eu.setPaid(request.getPaid());
            expense.getExpenseUsers().add(eu);
        }

        return expenseRepository.save(expense).getExpenseUsers();
    }

    public ExpenseUser updatePaidAmount(Long groupId, Long expenseId, String userId, BigDecimal paid) {
        Expense expense = validateExpenseAccess(groupId, expenseId);

        // Проверяем что пользователь обновляет свою запись или это создатель расхода
        ExpenseUser expenseUser = expense.getExpenseUsers().stream()
                .filter(eu -> eu.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ForbiddenException("You are not a member of this group."));


        expenseUser.setPaid(paid);
        expense.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense);

        return expenseUser;
    }

    public void removeExpense(Long groupId, Long expenseId, String userId) {
        Expense expense = validateExpenseAccess(groupId, expenseId);

        ExpenseUser participantToRemove = expense.getExpenseUsers().stream()
                .filter(eu -> eu.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Participant with id " + userId + " not found in expense"));

        expense.getExpenseUsers().remove(participantToRemove);
        expense.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense);
    }

    private Expense validateExpenseAccess(Long groupId, Long expenseId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));

        if (Boolean.TRUE.equals(group.getIsClosed())) {
            throw new IllegalArgumentException("This group is closed");
        }

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found"));

        if (!expense.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Expense does not belong to this group");
        }
        User currentUser = userService.getCurrentUser();
        if (!expense.getUserWhoCreated().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only expense creator can perform this action");
        }

        if (!group.getMembers().contains(currentUser)) {
            throw new ForbiddenException("You are not a member of this group");
        }

        return expense;
    }
}
