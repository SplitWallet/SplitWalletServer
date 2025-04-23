package org.example.expensesuserservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.AllArgsConstructor;
import org.example.expensesuserservice.client.AuthServiceClient;
import org.example.expensesuserservice.db.ExpenseUser;
import org.example.expensesuserservice.dto.ExpenseUserDto;
import org.example.expensesuserservice.request.UpdateExpenseParticipantRequest;
import org.example.expensesuserservice.request.UpdatePaidAmountRequest;
import org.example.expensesuserservice.service.ExpenseUserService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@AllArgsConstructor
@Validated
@RequestMapping("/groups")
@Tag(name = "Expense user", description = "Operations about expenses with users")
public class ExpenseUserController {

    private final ExpenseUserService expenseUserService;
    private final AuthServiceClient authServiceClient;
    private final ModelMapper modelMapper;

    @Operation(
            summary = "Получить участников расхода",
            description = "Возвращает список пользователей, связанных с конкретным расходом в группе. " +
                    "Показывает сумму, которую должен каждый участник и сколько уже оплатил. " +
                    "Доступно только для участников группы."
    )
    @GetMapping("{groupId}/expenses/{expenseId}")
    public ResponseEntity<List<ExpenseUserDto>> getExpenses(@PathVariable Long groupId,
                                                            @PathVariable Long expenseId,
                                                            HttpServletRequest req
                                                            ) {
        var authentication = (Authentication) req.getUserPrincipal();
        var jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getClaim("sub");

        List<ExpenseUserDto> expenseUserDtos = expenseUserService.getExpenseUsers(expenseId, currentUserId)
                .stream()
                .map(expense -> modelMapper.map(expense, ExpenseUserDto.class))
                .toList();
        return ResponseEntity.ok().body(expenseUserDtos);
    }

    @Operation(
            summary = "Обновить доли участников расхода",
            description = "Обновляет суммы и выполоты, которые должны участники расхода" +
                    "Доступно только для создателя расхода."
    )
    @PutMapping("{groupId}/expenses/{expenseId}/users")
    public ResponseEntity<List<ExpenseUserDto>> updateExpense
            (
            @PathVariable Long groupId, @PathVariable Long expenseId,
            @RequestBody @Valid List<UpdateExpenseParticipantRequest> requests,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            HttpServletRequest req
            ) {
        var authentication = (Authentication) req.getUserPrincipal();
        var jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getClaim("sub");

        var group = authServiceClient.getGroupById(authHeader, groupId);
        List<ExpenseUser> updatedParticipants = expenseUserService.updateExpense( expenseId, requests,currentUserId, group);
        return ResponseEntity.status(201).body(updatedParticipants.stream()
                .map(eu -> modelMapper.map(eu, ExpenseUserDto.class))
                .toList());
    }

    @Operation(
            summary = "Обновить оплаченную сумму участника",
            description = "Обновляет сумму, которую участник уже оплатил по расходу. " +
                    "Может использоваться для отметки частичной или полной оплаты. " +
                    "Доступно для участника расхода (для своей записи) или создателя расхода."
    )
    @PatchMapping("{groupId}/expenses/{expenseId}/users/{userId}/paid")
    public ResponseEntity<ExpenseUserDto> updatePaidAmount(
            @PathVariable Long groupId,
            @PathVariable Long expenseId,
            @PathVariable String userId,
            @RequestBody @Valid UpdatePaidAmountRequest request,
            HttpServletRequest req) {
        var authentication = (Authentication) req.getUserPrincipal();
        var jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getClaim("sub");

        ExpenseUser updated = expenseUserService.updatePaidAmount(expenseId, userId, request, currentUserId);
        return ResponseEntity.status(201).body(modelMapper.map(updated, ExpenseUserDto.class));
    }

    @Operation(
            summary = "Удалить участника из расхода",
            description = "Удаляет участника из расхода(expenseUser) по его ID. " +
                    "Если это приводит к несоответствию сумм, автоматически перераспределяет оставшуюся сумму между другими участниками. " +
                    "Доступно только для создателя расхода."
    )
    @DeleteMapping("{groupId}/expenses/{expenseId}/users/{userId}")
    public ResponseEntity<String> removeParticipant(
            @PathVariable Long groupId,
            @PathVariable Long expenseId,
            @PathVariable String userId,
            HttpServletRequest req) {

        var authentication = (Authentication) req.getUserPrincipal();
        var jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getClaim("sub");

        expenseUserService.removeExpense(expenseId, userId,currentUserId);
        return ResponseEntity.status(201).body("Success!!!");
    }
}
