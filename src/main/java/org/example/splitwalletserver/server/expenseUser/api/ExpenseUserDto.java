package org.example.splitwalletserver.server.expenseUser.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseUserDto {

    private Long id;

    private Long userId;

    private Long expenseId;

    private BigDecimal amount;

    private BigDecimal paid;
}