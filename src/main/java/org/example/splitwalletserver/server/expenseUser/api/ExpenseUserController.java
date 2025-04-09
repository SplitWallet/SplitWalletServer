package org.example.splitwalletserver.server.expenseUser.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.example.splitwalletserver.server.expenseUser.domain.ExpenseUserService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@AllArgsConstructor
@Validated
@RequestMapping("/groups")
@Tag(name = "Expense user", description = "Operations about expenses with users")
public class ExpenseUserController {

    private final ExpenseUserService expenseUserService;
    private final ModelMapper modelMapper;

    @Operation(
            summary = "Получить участников расхода",
            description = "Возвращает список пользователей, связанных с конкретным расходом в группе. " +
                    "Показывает сумму, которую должен каждый участник и сколько уже оплатил. " +
                    "Доступно только для участников группы."
    )
    @GetMapping("{groupId}/expenses/{expenseId}")
    public ResponseEntity<List<ExpenseUserDto>> getExpenses(@PathVariable Long groupId,
                                                         @PathVariable Long expenseId) {
        List<ExpenseUserDto> expenseUserDtos = expenseUserService.getExpenseUsers(groupId, expenseId)
                .stream()
                .map(expense -> modelMapper.map(expense, ExpenseUserDto.class))
                .toList();
        return ResponseEntity.ok().body(expenseUserDtos);
    }

}
