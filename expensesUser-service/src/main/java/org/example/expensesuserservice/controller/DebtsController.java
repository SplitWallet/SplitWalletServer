package org.example.expensesuserservice.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.example.expensesuserservice.dto.AggregatedDebtSummary;
import org.example.expensesuserservice.dto.UserOwedInGroupData;
import org.example.expensesuserservice.service.DebtsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("/group")
@Tag(name = "Debts", description = "API for debts")
public class DebtsController {

    private final DebtsService debtsService;

    @Operation(
            summary = "Получить подробную информацию о том, сколько денег и кому и в каких группах должен текущий пользователь",
            description = "Возвращает подробную информацию о том, сколько денег и кому и в каких группах должен текущий пользователь"
    )
    @GetMapping("/{groupId}/debts")
    public ResponseEntity<AggregatedDebtSummary> getSumOfYouOwed(HttpServletRequest req, @PathVariable("groupId") Long groupId) {
        var authentication = (Authentication) req.getUserPrincipal();
        var jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getClaim("sub");

        var result = debtsService.getAggregatedDebts(currentUserId, groupId);
        return ResponseEntity.ok(result);
    }

}