package org.example.expensesservice.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.ws.rs.ForbiddenException;
import lombok.AllArgsConstructor;
import org.example.expensesservice.db.Expense;
import org.example.expensesservice.db.ExpenseRepository;
import org.example.expensesservice.other.ExpenseUser;
import org.example.expensesservice.other.Group;
import org.example.expensesservice.other.User;
import org.example.expensesservice.request.CreateExpenseRequest;
import org.example.expensesservice.request.UpdateExpenseRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public List<Expense> getExpenses(String currentUserId, Long groupId) {
        var expenses = expenseRepository.getExpensesByGroupId(groupId);

        if (expenses.isEmpty()) {
            throw new EntityNotFoundException("Expense not found");
        }
        expenses.get(0).getGroup().getMembers().stream()
                .filter(eu -> eu.getId().equals(currentUserId))
                .findFirst().orElseThrow(() -> new ForbiddenException("You are not a member"));


        return expenses;
    }

    public Expense createExpense(CreateExpenseRequest createExpenseRequest, String currentUserId, Group group) {
        var toSave = new Expense();
        if (!group.getMembers().stream().map(User::getId)
                .toList().contains(currentUserId)) {
            throw new IllegalArgumentException("You don't member of group with id " + group.getId());
        }

        if (Boolean.TRUE.equals(group.getIsClosed())) {
            throw new IllegalArgumentException("This group is closed");
        }

        toSave.setAmount(createExpenseRequest.getAmount());
        toSave.setDescription(createExpenseRequest.getDescription());
        toSave.setUpdatedAt(toSave.getCreatedAt());
        toSave.setDate(createExpenseRequest.getDate());
        toSave.setCurrency(createExpenseRequest.getCurrency());
        toSave.setUserWhoCreated(new User(currentUserId));
        toSave.setName(createExpenseRequest.getName());

        toSave.setGroup(group);
        toSave.setExpenseUsers(fillDefaultExpenseUsers(group, toSave));

        var expUs = toSave.getExpenseUsers().stream().filter(expenseUser -> expenseUser.getUser().
                        getId().equals(currentUserId)).findFirst()
                .orElseThrow(() -> new EntityNotFoundException("User with id " + currentUserId + " not found"));
        expUs.setPaid(expUs.getAmount());

        return expenseRepository.save(toSave);
    }

    //На вид не очень продумано, это временно. Кто знает, как лучше, делайте
    private ArrayList<ExpenseUser> fillDefaultExpenseUsers(Group group, Expense expense) {
        ArrayList<ExpenseUser> expenseUsers = new ArrayList<>();
        BigDecimal totalAmount = expense.getAmount();
        int memberCount = group.getMembers().size();
        if (memberCount == 0) {
            return expenseUsers;
        }
        BigDecimal share = totalAmount.divide(BigDecimal.valueOf(memberCount), 2, RoundingMode.DOWN);

        BigDecimal remainder = totalAmount.subtract(share.multiply(BigDecimal.valueOf(memberCount)));

        for (int i = 0; i < memberCount; i++) {
            User user = group.getMembers().get(i);
            ExpenseUser expenseUser = new ExpenseUser();
            expenseUser.setUser(user);
            expenseUser.setExpense(expense);

            BigDecimal finalShare = (i == 0) ? share.add(remainder) : share;
            expenseUser.setAmount(finalShare);
            expenseUser.setPaid(BigDecimal.ZERO);

            expenseUsers.add(expenseUser);
        }

        return expenseUsers;
    }

    public void deleteExpense(Long expenseId,String currentUserId, Long groupId) {
        var toDelete = expenseRepository.findByIdAndGroupId(expenseId, groupId)
                .orElseThrow(()-> new EntityNotFoundException("Group or expence" + groupId +" " + expenseId + " not found"));

        if (!currentUserId.equals(toDelete.getGroup().getUserOwner().getId())) {
            throw new IllegalArgumentException("Only the owner can delete to expense");
        }

        expenseRepository.delete(toDelete);
    }

    public Expense updateExpense(Long expenseId, @Valid UpdateExpenseRequest requests, String currentUserId, Long groupId) {

        var expense = expenseRepository.findByIdAndGroupId(expenseId, groupId)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found"));

        if (Boolean.TRUE.equals(expense.getGroup().getIsClosed())) {
            throw new IllegalArgumentException("This group is closed");
        }

        expense.getGroup().getMembers().stream()
                .filter(eu -> eu.getId().equals(currentUserId))
                .findFirst().orElseThrow(() -> new ForbiddenException("You are not a member"));

        BigDecimal totalAmount = expense.getExpenseUsers().stream()
                .map(ExpenseUser::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if(requests.getAmount().compareTo(totalAmount) <= 0){
            throw new IllegalArgumentException(
                    "The amount of the expense must not be less than the sum of the participants in the expense.");
        }

        expense.setAmount(requests.getAmount());
        expense.setDescription(requests.getDescription());
        expense.setDate(requests.getDate());
        expense.setCurrency(requests.getCurrency());
        expense.setName(requests.getName());

        expense.setUpdatedAt(LocalDateTime.now());
        return expenseRepository.save(expense);
    }
}