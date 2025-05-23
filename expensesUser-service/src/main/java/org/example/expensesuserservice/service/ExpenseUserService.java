package org.example.expensesuserservice.service;


import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.ForbiddenException;
import lombok.AllArgsConstructor;
import org.example.expensesuserservice.client.AuthServiceClient;
import org.example.expensesuserservice.db.ExpenseUser;
import org.example.expensesuserservice.db.ExpenseUserRepository;
import org.example.expensesuserservice.other.Expense;
import org.example.expensesuserservice.other.Group;
import org.example.expensesuserservice.other.User;
import org.example.expensesuserservice.request.NotificationRequest;
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

    private final AuthServiceClient authServiceClient;

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

    public List<ExpenseUser> updateExpenseUser(Long expenseId, List<UpdateExpenseParticipantRequest> requests,
                                               String currentUserId, Group group) {

        var expense = validateExpenseAccess(expenseId, currentUserId, group);

        // Удаляем всех текущих участников расхода
        List<ExpenseUser> existingExpenseUsers = expenseUserRepository.findByExpenseId(expenseId);
        if (!existingExpenseUsers.isEmpty()) {
            expenseUserRepository.deleteAll(existingExpenseUsers);
        }

        // Создаем новых участников по requests
        List<ExpenseUser> newExpenseUsers = new ArrayList<>();
        for (UpdateExpenseParticipantRequest req : requests) {
            ExpenseUser newEu = new ExpenseUser();
            newEu.setUser(new User(req.getUserId()));
            newEu.setExpense(expense);
            newEu.setAmount(req.getAmount());
            newEu.setPaid(req.getPaid());
            newExpenseUsers.add(newEu);

            authServiceClient.sendNotification(req.getUserId(),
                    new NotificationRequest("Обновление долгов",
                            String.format("Расход %s был обновлен в группе %s", expense.getName(), group.getName())));
        }

        return expenseUserRepository.saveAll(newExpenseUsers);
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

        authServiceClient.sendNotification(userId,
                new NotificationRequest("Обновление долгов",
                        String.format("Ваш догл %s был пересмотрен в группе %s",
                                expenseUser.getExpense().getName(),
                                expenseUser.getExpense().getGroup().getName())));

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

        authServiceClient.sendNotification(userId,
                new NotificationRequest("Удаление долгов",
                        String.format("Ваш догл %s был удален в группе %s",
                                expenseUser.getExpense().getName(),
                                expenseUser.getExpense().getGroup().getName())));

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
