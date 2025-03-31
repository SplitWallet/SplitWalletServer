package org.example.splitwalletserver.server.expenseUser.domain;


import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.example.splitwalletserver.server.expenseUser.ExpenseUser;
import org.example.splitwalletserver.server.expenseUser.db.ExpenseUserRepository;
import org.example.splitwalletserver.server.expenses.Expense;
import org.example.splitwalletserver.server.expenses.db.ExpenseRepository;
import org.example.splitwalletserver.server.groups.db.GroupRepository;
import org.example.splitwalletserver.server.models.User;
import org.example.splitwalletserver.server.services.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ExpenseUserService {
    private final ExpenseUserRepository expenseUserRepository;
    private final UserService userService;
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
}
