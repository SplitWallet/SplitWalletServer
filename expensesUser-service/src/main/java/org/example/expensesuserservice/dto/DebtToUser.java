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
public class DebtToUser {
    private String creditorId;
    private String creditorName;
    private BigDecimal totalAmount;  // Сколько должен всего
    private BigDecimal paidAmount;   // Сколько уже оплатил
    private List<DebtExpenseDetail> expenseDetails;
}