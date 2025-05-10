package org.example.groupsservice.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateGroupRequest {

    @NotEmpty(message = "Название группы не может быть пустым")
    @Size(min = 3, max = 100, message = "Название группы должно быть от 3 до 100 символов")
    private String name;
}
