package org.example.expensesuserservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AggregatedDebtSummary {
    private String groupName;
    private List<UserBalance> balances;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserBalance {
        private String userId;
        private String username;
        private BigDecimal youOwe;     // Сколько ты ему должен
        private BigDecimal owesYou;    // Сколько он тебе должен
        private BigDecimal netBalance; // Положительное — тебе должны, отрицательное — ты должен
    }
}
