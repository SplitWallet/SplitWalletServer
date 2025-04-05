package org.example.splitwalletserver.server.expenses.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseDto {

    private Long id;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    private String name;

    private LocalDate date;

    private String description;

    private BigDecimal amount;

    private String currency;

    private String userWhoCreatedId;

    private Boolean isActive;

    private Long groupId;

    private BigDecimal currentUserPaid;

}
