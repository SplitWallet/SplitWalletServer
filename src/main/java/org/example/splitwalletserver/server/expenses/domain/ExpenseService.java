package org.example.splitwalletserver.server.expenses.domain;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.ws.rs.ForbiddenException;
import lombok.AllArgsConstructor;
import org.example.splitwalletserver.server.expenseUser.db.ExpenseUser;
import org.example.splitwalletserver.server.expenses.db.Expense;
import org.example.splitwalletserver.server.expenses.db.ExpenseRepository;
import org.example.splitwalletserver.server.expenses.request.CreateExpenseRequest;
import org.example.splitwalletserver.server.expenses.request.UpdateExpenseRequest;
import org.example.splitwalletserver.server.groups.db.Group;
import org.example.splitwalletserver.server.groups.db.GroupRepository;
import org.example.splitwalletserver.server.users.services.UserServiceImpl;
import org.example.splitwalletserver.server.users.model.User;
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

    private final UserServiceImpl userService;

    private final GroupRepository groupRepository;

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

    public Expense createExpense(CreateExpenseRequest createExpenseRequest, Long groupId) {
        var toSave = new Expense();
        var group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group with id " + groupId + " not found"));
        var currentUser = userService.getCurrentUser();
        if (!group.getMembers().stream().map(User::getId)
                .toList().contains(currentUser.getId())) {
            throw new IllegalArgumentException("You don't member of group with id " + groupId);
        }

        if (Boolean.TRUE.equals(group.getIsClosed())) {
            throw new IllegalArgumentException("This group is closed");
        }

        toSave.setAmount(createExpenseRequest.getAmount());
        toSave.setDescription(createExpenseRequest.getDescription());
        toSave.setUpdatedAt(toSave.getCreatedAt());
        toSave.setDate(createExpenseRequest.getDate());
        toSave.setCurrency(createExpenseRequest.getCurrency());
        toSave.setUserWhoCreated(currentUser);
        toSave.setName(createExpenseRequest.getName());

        toSave.setGroup(group);
        toSave.setExpenseUsers(fillDefaultExpenseUsers(group, toSave));

        var expUs = toSave.getExpenseUsers().stream().filter(expenseUser -> expenseUser.getUser().
                getId().equals(currentUser.getId())).findFirst()
                .orElseThrow(() -> new EntityNotFoundException("User with id " + currentUser.getId() + " not found"));
        expUs.setPaid(expUs.getAmount());

        return expenseRepository.save(toSave);
    }

    public List<Expense> getExpenses(Long groupId) {
        var group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group with id " + groupId + " not found"));
        var currentUser = userService.getCurrentUser();
        if (!group.getMembers().stream().map(User::getId)
                .toList().contains(currentUser.getId())) {
            throw new IllegalArgumentException("You don't member of group with id " + groupId);
        }
        return expenseRepository.getExpensesByGroupId(group.getId());
    }

    public void deleteExpense(Long expenseId, Long groupId) {
        var toDelete = expenseRepository.findByIdAndGroupId(expenseId, groupId)
                .orElseThrow(()-> new EntityNotFoundException("Group or expence" + groupId +" " + expenseId + " not found"));

        var currentUser = userService.getCurrentUser();
        if (!currentUser.getId().equals(toDelete.getGroup().getUserOwner().getId())) {
            throw new IllegalArgumentException("Only the owner can delete to expense");
        }

        expenseRepository.delete(toDelete);
    }

    public Expense updateExpense(Long groupId, Long expenseId, @Valid UpdateExpenseRequest requests) {
        Expense expense = validateExpenseAccess(groupId, expenseId);

        BigDecimal totalAmount = expense.getExpenseUsers().stream()
                .map(ExpenseUser::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if(requests.getAmount().compareTo(totalAmount) > 0){
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