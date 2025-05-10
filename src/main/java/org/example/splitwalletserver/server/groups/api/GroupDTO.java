package org.example.splitwalletserver.server.groups.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.splitwalletserver.server.users.dto.UserInsensitiveInfoDTO;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupDTO {

    private Long id;

    private String name;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private UserInsensitiveInfoDTO userOwner;

    private Boolean isClosed;
}
