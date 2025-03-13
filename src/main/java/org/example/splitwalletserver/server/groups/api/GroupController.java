package org.example.splitwalletserver.server.groups.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.splitwalletserver.server.groups.db.Group;
import org.example.splitwalletserver.server.groups.domain.GroupService;
import org.example.splitwalletserver.server.groups.request.CreateGroupRequest;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("/groups")
@Tag(name = "Group", description = "Operations about groups")
public class GroupController {

    private final GroupService groupService;
    private final ModelMapper modelMapper;

    @PostMapping()
    @Operation(summary = "Создать новую группу",
            description = "Создает новую группу с указанными параметрами. Группа может быть создана только аутентифицированным пользователем.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Неверный запрос",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Validation Error Example",
                                    value = """
                                {
                                  "violations": [
                                    {
                                      "fieldName": "name",
                                      "message": "Название группы должно быть от 3 до 100 символов"
                                    },
                                    {
                                      "fieldName": "name",
                                      "message": "Название группы не может быть пустым"
                                    }
                                  ]
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(responseCode = "201", description = "Группа успешно создана",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Success Response Example",
                                    value = """
                                {
                                  "name": "Trip to Paris",
                                  "createdAt": "2025-03-14T02:24:29.528862",
                                  "updatedAt": "2025-03-14T02:24:29.528862",
                                  "userOwner": {
                                    "name": "AlexM",
                                    "email": "alex@email.com",
                                    "phoneNumber": null
                                  },
                                  "members": [
                                    {
                                        "name": "AlexM",
                                        "email": "alex@email.com",
                                        "phoneNumber": "88005553555"
                                    }
                                  ],
                                  "events": [],
                                  "isClosed": false
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Internal Server Error Example",
                                    value = """
                                {
                                  "httpStatus": 500,
                                  "message": "Internal server error, please try again later"
                                }
                                """
                            )
                    )
            )
    })
    public ResponseEntity<GroupDTO> createGroup(@RequestBody @Valid CreateGroupRequest createGroupRequest) {
        var created = groupService.createGroup(createGroupRequest);
        return ResponseEntity.status(201).body(fromGroupToDTO(created));
    }

    private GroupDTO fromGroupToDTO(Group group) {return modelMapper.map(group, GroupDTO.class);}
}
