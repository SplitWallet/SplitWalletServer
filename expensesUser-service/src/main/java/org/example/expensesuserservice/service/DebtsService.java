package org.example.expensesuserservice.service;

import lombok.AllArgsConstructor;
import org.example.expensesuserservice.db.ExpenseUser;
import org.example.expensesuserservice.db.ExpenseUserRepository;
import org.example.expensesuserservice.dto.AggregatedDebtSummary;
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

    private List<UserOwedInGroupData> getCurrentUserOwed(String userId) {

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

    private List<UserOwedInGroupData> getWhoOwesYou(String currentUserId) {
        // Получаем только те связи, где текущий пользователь — создатель расхода (кредитор)
        var expenseUsers = expenseUserRepository.findDebtorsForUser(currentUserId)
                .orElseThrow(() -> new RuntimeException("Expense user not found"));

        // Группируем: по группам → по должникам
        Map<Group, Map<User, List<ExpenseUser>>> grouped = new HashMap<>();

        for (ExpenseUser eu : expenseUsers) {
            Expense expense = eu.getExpense();
            Group group = expense.getGroup();
            User debtor = eu.getUser();

            grouped
                    .computeIfAbsent(group, g -> new HashMap<>())
                    .computeIfAbsent(debtor, u -> new ArrayList<>())
                    .add(eu);
        }

        // Преобразуем в DTO
        List<UserOwedInGroupData> result = new ArrayList<>();

        for (Map.Entry<Group, Map<User, List<ExpenseUser>>> groupEntry : grouped.entrySet()) {
            Group group = groupEntry.getKey();

            List<DebtToUser> debts = new ArrayList<>();

            for (Map.Entry<User, List<ExpenseUser>> debtorEntry : groupEntry.getValue().entrySet()) {
                User debtor = debtorEntry.getKey();
                List<ExpenseUser> userExpenses = debtorEntry.getValue();

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
                        .creditorId(debtor.getId())            // фактически debtorId
                        .creditorName(debtor.getUsername())     // фактически debtorName
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

    public AggregatedDebtSummary getAggregatedDebts(String userId) {
        List<UserOwedInGroupData> youOweData = getCurrentUserOwed(userId);
        List<UserOwedInGroupData> owesYouData = getWhoOwesYou(userId);

        Map<String, AggregatedDebtSummary.UserBalance> balanceMap = new HashMap<>();

        // Обрабатываем: ты должен другим
        for (UserOwedInGroupData groupData : youOweData) {
            for (DebtToUser debt : groupData.getDebts()) {
                AggregatedDebtSummary.UserBalance balance = balanceMap
                        .computeIfAbsent(debt.getCreditorId(), id -> AggregatedDebtSummary.UserBalance.builder()
                                .userId(debt.getCreditorId())
                                .username(debt.getCreditorName())
                                .youOwe(BigDecimal.ZERO)
                                .owesYou(BigDecimal.ZERO)
                                .netBalance(BigDecimal.ZERO)
                                .build());

                balance.setYouOwe(balance.getYouOwe().add(debt.getTotalAmount().subtract(debt.getPaidAmount())));
            }
        }

        // Обрабатываем: другие должны тебе
        for (UserOwedInGroupData groupData : owesYouData) {
            for (DebtToUser debt : groupData.getDebts()) {
                AggregatedDebtSummary.UserBalance balance = balanceMap
                        .computeIfAbsent(debt.getCreditorId(), id -> AggregatedDebtSummary.UserBalance.builder()
                                .userId(debt.getCreditorId())
                                .username(debt.getCreditorName())
                                .youOwe(BigDecimal.ZERO)
                                .owesYou(BigDecimal.ZERO)
                                .netBalance(BigDecimal.ZERO)
                                .build());

                balance.setOwesYou(balance.getOwesYou().add(debt.getTotalAmount().subtract(debt.getPaidAmount())));
            }
        }

        // Вычисляем итоговый баланс (кому кто должен по факту)
        for (AggregatedDebtSummary.UserBalance balance : balanceMap.values()) {
            BigDecimal net = balance.getOwesYou().subtract(balance.getYouOwe());
            balance.setNetBalance(net);
        }

        return AggregatedDebtSummary.builder()
                .balances(new ArrayList<>(balanceMap.values()))
                .build();
    }

}
