package org.example.expensesuserservice.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExpenseParticipantRequest {
    @NotNull
    private String userId;

    @NotNull
    @PositiveOrZero
    private BigDecimal amount;

    @NotNull
    @PositiveOrZero
    private BigDecimal paid;
}