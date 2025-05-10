package org.example.expensesservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.AllArgsConstructor;
import org.example.expensesservice.client.AuthServiceClient;
import org.example.expensesservice.db.Expense;
import org.example.expensesservice.dto.ExpenseDto;
import org.example.expensesservice.other.ExpenseUser;
import org.example.expensesservice.request.CreateExpenseRequest;
import org.example.expensesservice.request.UpdateExpenseRequest;
import org.example.expensesservice.service.ExpenseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("/groups/{groupId}/expenses")
@Tag(name = "Expense", description = "Operations about expenses")
public class ExpenseController {

    private final ModelMapper modelMapper;

    private final ExpenseService expenseService;

    private final AuthServiceClient authServiceClient;

    @Operation(
            summary = "Получить все расходы группы",
            description = "Возвращает список всех расходов в указанной группе. " +
                    "Для каждого расхода показывает сумму, которую должен текущий пользователь. " +
                    "Доступно только для участников группы."
    )
    @GetMapping()
    public ResponseEntity<List<ExpenseDto>> getExpenses
            (
                    @PathVariable Long groupId,
                    HttpServletRequest req
            ) {
        var authentication = (Authentication) req.getUserPrincipal();
        var jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getClaim("sub");

        List<Expense> expenses = expenseService.getExpenses(currentUserId, groupId);
        List<ExpenseDto> expenseDtos = expenses.stream()
                .map(expense -> fromExpenseToExpenseDto(expense, currentUserId))
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
    public ResponseEntity<ExpenseDto> createExpense
            (
                    @PathVariable Long groupId,
                    @RequestBody @Valid CreateExpenseRequest createExpenseRequest,
                    @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                    HttpServletRequest req
            ) {
        var authentication = (Authentication) req.getUserPrincipal();
        var jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getClaim("sub");

        var group = authServiceClient.getGroupById(authHeader, groupId);
        var returned = expenseService.createExpense(createExpenseRequest, currentUserId, group);
        var expenseDto = fromExpenseToExpenseDto(returned, currentUserId);
        return ResponseEntity.status(201).body(expenseDto);
    }

    @Operation(
            summary = "Удалить расход",
            description = "Позволяет аутентифицированному владельцу группы, удалить расход в ней"
    )
    @DeleteMapping("{expenseId}")
    public ResponseEntity<String> deleteExpense
            (
                    @PathVariable Long groupId,
                    @PathVariable Long expenseId,
                    HttpServletRequest req
            ) {
        var authentication = (Authentication) req.getUserPrincipal();
        var jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getClaim("sub");

        expenseService.deleteExpense(expenseId, currentUserId, groupId);
        return ResponseEntity.status(201).body("Success!");
    }

    @Operation(
            summary = "Обновить доли участников расхода",
            description = "Обновляет суммы и выполоты, которые должны участники расхода" +
                    "Доступно только для создателя расхода."
    )
    @PutMapping("{expenseId}")
    public ResponseEntity<ExpenseDto> updateExpense
            (
                    @PathVariable Long groupId,
                    @PathVariable Long expenseId,
                    @RequestBody @Valid UpdateExpenseRequest requests,
                    HttpServletRequest req
            ) {
        var authentication = (Authentication) req.getUserPrincipal();
        var jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getClaim("sub");

        var updatedParticipants = expenseService.updateExpense(expenseId, requests, currentUserId, groupId);
        return ResponseEntity.status(201).body(fromExpenseToDTO(updatedParticipants));
    }


    private ExpenseDto fromExpenseToExpenseDto(Expense expense, String currentUserId) {
        var expenseDto = modelMapper.map(expense, ExpenseDto.class);
        var amount = expense.getExpenseUsers().stream()
                .filter(eu -> eu.getUser() != null && currentUserId.equals(eu.getUser().getId()))
                .findFirst()
                .map(ExpenseUser::getAmount)
                .orElse(BigDecimal.ZERO);
        expenseDto.setCurrentUserPaid(amount);
        return expenseDto;
    }

    private ExpenseDto fromExpenseToDTO(Expense expense) {return modelMapper.map(expense, ExpenseDto.class);}

}