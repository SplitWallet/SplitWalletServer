package org.example.expensesuserservice.service;

import lombok.AllArgsConstructor;
import org.example.expensesuserservice.db.ExpenseUser;
import org.example.expensesuserservice.db.ExpenseUserRepository;
import org.example.expensesuserservice.dto.DebtExpenseDetail;
import org.example.expensesuserservice.dto.DebtToUser;
import org.example.expensesuserservice.dto.UserOwedInGroupData;
import org.example.expensesuserservice.other.Expense;
import org.example.expensesuserservice.other.Group;

import org.example.expensesuserservice.other.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class DebtsService {

    private final ExpenseUserRepository expenseUserRepository;

    public List<UserOwedInGroupData> getCurrentUserOwed(String userId) {

        var expenseUsers = expenseUserRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Expense user not found"));

        // Группируем по группе → по кредитору → список деталей
        Map<Group, Map<User, List<ExpenseUser>>> grouped = new HashMap<>();

        for (ExpenseUser expenseUser : expenseUsers) {
            Expense expense = expenseUser.getExpense();
            Group group = expense.getGroup();
            var creditor = expense.getUserWhoCreated();

            // Пропускаем, если пользователь сам создал этот расход (не может быть должен сам себе)
            if (creditor.getId().equals(expenseUser.getUser().getId())) continue;

            grouped
                    .computeIfAbsent(group, g -> new HashMap<>())
                    .computeIfAbsent(creditor, c -> new ArrayList<>())
                    .add(expenseUser);
        }

        // Преобразуем в DTO
        List<UserOwedInGroupData> result = new ArrayList<>();

        for (Map.Entry<Group, Map<User, List<ExpenseUser>>> groupEntry : grouped.entrySet()) {
            Group group = groupEntry.getKey();

            List<DebtToUser> debts = new ArrayList<>();

            for (Map.Entry<User, List<ExpenseUser>> creditorEntry : groupEntry.getValue().entrySet()) {
                User creditor = creditorEntry.getKey();
                List<ExpenseUser> userExpenses = creditorEntry.getValue();

                BigDecimal totalAmount = BigDecimal.ZERO;
                BigDecimal paidAmount = BigDecimal.ZERO;

                List<DebtExpenseDetail> details = new ArrayList<>();

                for (ExpenseUser eu : userExpenses) {
                    BigDecimal amount = eu.getAmount();
                    BigDecimal paid = eu.getPaid();
                    totalAmount = totalAmount.add(amount);
                    paidAmount = paidAmount.add(paid);

                    details.add(DebtExpenseDetail.builder()
                            .expenseId(eu.getExpense().getId())
                            .expenseName(eu.getExpense().getName())
                            .amount(amount)
                            .paid(paid)
                            .build());
                }

                debts.add(DebtToUser.builder()
                        .creditorId(creditor.getId())
                        .creditorName(creditor.getUsername()) // или getUsername()
                        .totalAmount(totalAmount)
                        .paidAmount(paidAmount)
                        .expenseDetails(details)
                        .build());
            }

            result.add(UserOwedInGroupData.builder()
                    .groupId(group.getId())
                    .groupName(group.getName())
                    .debts(debts)
                    .build());
        }

        return result;
    }
}
