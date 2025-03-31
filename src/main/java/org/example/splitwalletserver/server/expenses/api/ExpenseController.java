package org.example.splitwalletserver.server.expenses.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.splitwalletserver.server.expenses.Expense;
import org.example.splitwalletserver.server.expenses.domain.ExpenseService;
import org.example.splitwalletserver.server.expenses.request.CreateExpenseRequest;
import org.example.splitwalletserver.server.services.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("/groups")
@Tag(name = "Expense", description = "Operations about expenses")
public class ExpenseController {

    private final ModelMapper modelMapper;

    private final ExpenseService expenseService;

    private final UserService userService;

    @GetMapping("{groupId}/expenses")
    public ResponseEntity<List<ExpenseDto>> getExpenses(@PathVariable Long groupId) {
        List<Expense> expenses = expenseService.getExpenses(groupId);
        List<ExpenseDto> expenseDtos = expenses.stream()
                .map(this::fromExpenseToExpenseDto)
                .collect(Collectors.toList());
        return ResponseEntity.status(201).body(expenseDtos);
    }

    @PostMapping("{groupId}/expenses")
    public ResponseEntity<ExpenseDto> createExpense(@PathVariable Long groupId,
                                                    @RequestBody @Valid CreateExpenseRequest createExpenseRequest) {
        var returned = expenseService.createExpense(createExpenseRequest, groupId);
        var expenseDto = fromExpenseToExpenseDto(returned);
        return ResponseEntity.status(201).body(expenseDto);
    }

    private ExpenseDto fromExpenseToExpenseDto(Expense expense) {
        var expenseDto = modelMapper.map(expense, ExpenseDto.class);
        var amount = expense.getExpenseUsers().stream().filter(eu ->
                        eu.getUser().getId().equals(userService.getCurrentUser().getId()))
                .findFirst().orElseThrow(()->new RuntimeException("Error!!!")).getAmount();
        expenseDto.setCurrentUserPaid(amount);
        return expenseDto;
    }

}