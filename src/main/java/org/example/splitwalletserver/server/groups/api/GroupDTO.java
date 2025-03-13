package org.example.splitwalletserver.server.groups.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.splitwalletserver.server.models.Expense;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupDTO {

    private String name;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private UserInsensitiveInfoDTO userOwner;

    private List<UserInsensitiveInfoDTO> members;

    private List<Expense> events;

    private Boolean isClosed;
}
