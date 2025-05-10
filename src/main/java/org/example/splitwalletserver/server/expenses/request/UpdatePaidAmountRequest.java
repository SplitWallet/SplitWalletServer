package org.example.splitwalletserver.server.expenses.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class UpdatePaidAmountRequest {
    @NotNull
    @PositiveOrZero
    private BigDecimal paid;
}