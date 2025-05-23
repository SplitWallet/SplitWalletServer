package org.example.groupsservice.dto;

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

    private String username;

    private String email;

    private String phoneNumber;

}