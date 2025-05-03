package org.example.expensesuserservice.service;


import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.ForbiddenException;
import lombok.AllArgsConstructor;
import org.example.expensesuserservice.db.ExpenseUser;
import org.example.expensesuserservice.db.ExpenseUserRepository;
import org.example.expensesuserservice.other.Expense;
import org.example.expensesuserservice.other.Group;
import org.example.expensesuserservice.other.User;
import org.example.expensesuserservice.request.UpdateExpenseParticipantRequest;
import org.example.expensesuserservice.request.UpdatePaidAmountRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ExpenseUserService {
    private final ExpenseUserRepository expenseUserRepository;

    public List<ExpenseUser> getExpenseUsers(Long expenseId, String currentUserId) {

        var expenseUsers = expenseUserRepository.findByExpenseId(expenseId);

        if (expenseUsers.isEmpty()) {
            throw new EntityNotFoundException("ExpensesUser not found");
        }

        if (Boolean.TRUE.equals(expenseUsers.get(0).getExpense().getGroup().getIsClosed())) {
            throw new IllegalArgumentException("This group is closed");
        }

        expenseUsers.get(0).getExpense().getGroup().getMembers().stream()
                .filter(eu -> eu.getId().equals(currentUserId))
                .findFirst()
                .orElseThrow(() -> new ForbiddenException("You are not a member of this group."));

        return expenseUsers;

    }

    public List<ExpenseUser> updateExpense(Long expenseId, List<UpdateExpenseParticipantRequest> requests,
                                           String currentUserId, Group group) {

        var expense = validateExpenseAccess(expenseId, currentUserId, group);

        List<ExpenseUser> existingExpenseUsers = expenseUserRepository.findByExpenseId(expenseId);

        List<ExpenseUser> updatedOrNew = new ArrayList<>();
        for (UpdateExpenseParticipantRequest req : requests) {
            ExpenseUser existing = existingExpenseUsers.stream()
                    .filter(eu -> eu.getUser().getId().equals(req.getUserId()))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                existing.setAmount(req.getAmount());
                existing.setPaid(req.getPaid());
                updatedOrNew.add(existing);
            } else {
                ExpenseUser newEu = new ExpenseUser();
                newEu.setUser(new User(req.getUserId()));
                newEu.setExpense(expense);
                newEu.setAmount(req.getAmount());
                newEu.setPaid(req.getPaid());
                updatedOrNew.add(newEu);
            }
        }
        return expenseUserRepository.saveAll(updatedOrNew);
    }

    public ExpenseUser updatePaidAmount(Long expenseId, String userId, UpdatePaidAmountRequest updatePaidAmountRequest,
                                        String currentUserId) {

        ExpenseUser expenseUser = expenseUserRepository.findByExpenseIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Expense or User not found.")
                        );

        if (Boolean.TRUE.equals(expenseUser.getExpense().getGroup().getIsClosed())) {
            throw new IllegalArgumentException("This group is closed");
        }

        expenseUser.getExpense().getGroup().getMembers().stream()
                .filter(eu -> eu.getId().equals(currentUserId))
                .findFirst()
                .orElseThrow(() -> new ForbiddenException("You are not a member of this group."));


        expenseUser.setPaid(updatePaidAmountRequest.getPaid());
        expenseUser.getExpense().setUpdatedAt(LocalDateTime.now());

        return expenseUserRepository.save(expenseUser);
    }

    public void removeExpense(Long expenseId, String userId, String currentUserId) {

        ExpenseUser expenseUser = expenseUserRepository.findByExpenseIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Expense or User not found.")
                );

        if (Boolean.TRUE.equals(expenseUser.getExpense().getGroup().getIsClosed())) {
            throw new IllegalArgumentException("This group is closed");
        }

        expenseUser.getExpense().getGroup().getMembers().stream()
                .filter(eu -> eu.getId().equals(currentUserId))
                .findFirst()
                .orElseThrow(() -> new ForbiddenException("You are not a member of this group."));

        expenseUserRepository.delete(expenseUser);
    }

    private Expense validateExpenseAccess(Long expenseId, String currentUserId, Group group) {

        if (Boolean.TRUE.equals(group.getIsClosed())) {
            throw new IllegalArgumentException("This group is closed");
        }

        Expense expense = group.getEvents().stream()
                .filter(e -> e.getId().equals(expenseId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Expense not found in this group"));


        group.getMembers().stream()
                .filter(eu -> eu.getId().equals(currentUserId))
                .findFirst().orElseThrow(() -> new ForbiddenException("You are not a member"));

        return expense;
    }
}
