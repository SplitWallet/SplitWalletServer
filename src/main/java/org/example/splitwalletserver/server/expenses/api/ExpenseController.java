package org.example.splitwalletserver.server.expenses.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.splitwalletserver.server.expenses.db.Expense;
import org.example.splitwalletserver.server.expenses.domain.ExpenseService;
import org.example.splitwalletserver.server.expenses.request.CreateExpenseRequest;
import org.example.splitwalletserver.server.users.services.UserServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("/groups")
@Tag(name = "Expense", description = "Operations about expenses")
public class ExpenseController {

    private final ModelMapper modelMapper;

    private final ExpenseService expenseService;

    private final UserServiceImpl keycloakUserService;

    @GetMapping("{groupId}/expenses")
    public ResponseEntity<List<ExpenseDto>> getExpenses(@PathVariable Long groupId) {
        List<Expense> expenses = expenseService.getExpenses(groupId);
        List<ExpenseDto> expenseDtos = expenses.stream()
                .map(this::fromExpenseToExpenseDto)
                .toList();
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
                        eu.getUser().getId().equals(keycloakUserService.getCurrentUser().getId()))
                .findFirst().orElseThrow(()->new RuntimeException("Error!!!")).getAmount();
        expenseDto.setCurrentUserPaid(amount);
        return expenseDto;
    }

}