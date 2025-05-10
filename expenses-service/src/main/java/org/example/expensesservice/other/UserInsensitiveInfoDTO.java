package org.example.expensesservice.other;

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