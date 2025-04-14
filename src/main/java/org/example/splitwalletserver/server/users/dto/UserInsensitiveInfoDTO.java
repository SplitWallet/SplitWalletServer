package org.example.splitwalletserver.server.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInsensitiveInfoDTO {

    private String id;

    private String name;

    private String email;

    private String phoneNumber;

}