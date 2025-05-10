package org.example.splitwalletserver.server.expenses.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class CreateExpenseRequest {

    @NotEmpty(message = "Название расхода не может быть пустым")
    @Size(min = 3, max = 100, message = "Название расхода должно быть от 3 до 100 символов")
    private String name;

    @NotNull
    private LocalDate date;

    private String description;

    @NotNull
    private BigDecimal amount;

    @Size(min = 3, max = 3)
    private String currency;
}
