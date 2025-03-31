package org.example.splitwalletserver.server.expenseUser.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.example.splitwalletserver.server.expenseUser.ExpenseUser;
import org.example.splitwalletserver.server.expenseUser.domain.ExpenseUserService;
import org.example.splitwalletserver.server.expenses.api.ExpenseDto;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@AllArgsConstructor
@Validated
@RequestMapping("/groups")
@Tag(name = "Expense user", description = "Operations about expenses with users")
public class ExpenseUserController {

    private final ExpenseUserService expenseUserService;
    private final ModelMapper modelMapper;

    @GetMapping("{groupId}/expenses/{expenseId}")
    public ResponseEntity<List<ExpenseUserDto>> getExpenses(@PathVariable Long groupId,
                                                         @PathVariable Long expenseId) {
        List<ExpenseUserDto> expenseUserDtos = expenseUserService.getExpenseUsers(groupId, expenseId)
                .stream()
                .map(expense -> modelMapper.map(expense, ExpenseUserDto.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(expenseUserDtos);
    }

}
