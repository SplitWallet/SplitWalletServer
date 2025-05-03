package org.example.expensesuserservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserOwedInGroupData {
    private Long groupId;
    private String groupName;
    private List<DebtToUser> debts;
}