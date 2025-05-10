package org.example.splitwalletserver.server.expenses.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.splitwalletserver.server.expenses.db.Expense;
import org.example.splitwalletserver.server.expenses.domain.ExpenseService;
import org.example.splitwalletserver.server.expenses.request.CreateExpenseRequest;
import org.example.splitwalletserver.server.expenses.request.UpdateExpenseRequest;
import org.example.splitwalletserver.server.users.services.UserServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("/groups/{groupId}/expenses")
@Tag(name = "Expense", description = "Operations about expenses")
public class ExpenseController {

    private final ModelMapper modelMapper;

    private final ExpenseService expenseService;

    private final UserServiceImpl userService;

    @Operation(
            summary = "Получить все расходы группы",
            description = "Возвращает список всех расходов в указанной группе. " +
                    "Для каждого расхода показывает сумму, которую должен текущий пользователь. " +
                    "Доступно только для участников группы."
    )
    @GetMapping()
    public ResponseEntity<List<ExpenseDto>> getExpenses(@PathVariable Long groupId) {
        List<Expense> expenses = expenseService.getExpenses(groupId);
        List<ExpenseDto> expenseDtos = expenses.stream()
                .map(this::fromExpenseToExpenseDto)
                .toList();
        return ResponseEntity.status(201).body(expenseDtos);
    }

    @Operation(
            summary = "Создать новый расход",
            description = "Создает новый расход в указанной группе. " +
                    "Сумма автоматически распределяется между всеми участниками группы равными долями. " +
                    "Создатель расхода автоматически отмечается как оплативший свою долю. " +
                    "Доступно только для участников группы."
    )
    @PostMapping()
    public ResponseEntity<ExpenseDto> createExpense(@PathVariable Long groupId,
                                                    @RequestBody @Valid CreateExpenseRequest createExpenseRequest) {
        var returned = expenseService.createExpense(createExpenseRequest, groupId);
        var expenseDto = fromExpenseToExpenseDto(returned);
        return ResponseEntity.status(201).body(expenseDto);
    }

    @Operation(
            summary = "Удалить расход",
            description = "Позволяет аутентифицированному владельцу группы, удалить расход в ней"
    )
    @DeleteMapping("{expenseId}")
    public ResponseEntity<String> deleteExpense(@PathVariable Long groupId,
                                                @PathVariable Long expenseId) {
        expenseService.deleteExpense(expenseId, groupId);
        return ResponseEntity.status(201).body("Success!");
    }

    @Operation(
            summary = "Обновить доли участников расхода",
            description = "Обновляет суммы и выполоты, которые должны участники расхода" +
                    "Доступно только для создателя расхода."
    )
    @PutMapping("{expenseId}")
    public ResponseEntity<ExpenseDto> updateExpense(
            @PathVariable Long groupId,
            @PathVariable Long expenseId,
            @RequestBody @Valid UpdateExpenseRequest requests) {
        var updatedParticipants = expenseService.updateExpense(groupId, expenseId, requests);
        return ResponseEntity.status(201).body(fromExpenseToDTO(updatedParticipants));
    }


    private ExpenseDto fromExpenseToExpenseDto(Expense expense) {
        var expenseDto = modelMapper.map(expense, ExpenseDto.class);
        var amount = expense.getExpenseUsers().stream().filter(eu ->
                        eu.getUser().getId().equals(userService.getCurrentUser().getId()))
                .findFirst().orElseThrow(()->new RuntimeException("Error!!!")).getAmount();
        expenseDto.setCurrentUserPaid(amount);
        return expenseDto;
    }

    private ExpenseDto fromExpenseToDTO(Expense expense) {return modelMapper.map(expense, ExpenseDto.class);}

}