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
import org.example.splitwalletserver.server.users.dto.UserInsensitiveInfoDTO;
import org.example.splitwalletserver.server.users.model.User;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("{uniqueCode}/join")
    @Operation(summary = "Присоединиться к группе по коду",
            description = "Позволяет текущему аутентифицированному пользователю присоединиться к группе, используя уникальный код группы.")
    public ResponseEntity<String> joinGroup(@PathVariable String uniqueCode) {
        groupService.joinGroup(uniqueCode);
        return ResponseEntity.status(201).body("Success!");
    }

    @PatchMapping("{groupId}/close")
    @Operation(summary = "Закрыть группу",
            description = "Позволяет аутентифицированному владельцу группы, закрыть ее")
    public ResponseEntity<String> closeGroup(@PathVariable Long groupId) {
        groupService.closeGroup(groupId);
        return ResponseEntity.status(201).body("Success!");
    }

    @DeleteMapping("{groupId}")
    @Operation(summary = "Удалить группу",
            description = "Позволяет аутентифицированному владельцу группы, удалить ее.")
    public ResponseEntity<String> deleteGroup(@PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.status(201).body("Success!");
    }

    @GetMapping("/my")
    @Operation(summary = "Получить группы по пользователю",
            description = "Получить группы, в которых состоит текущий пользователь. Получить свои группы может только  аутентифицированный пользователь.")
    public ResponseEntity<List<GroupDTO>> getMyGrouos() {
        var toReturn = groupService.getMyGroups().stream().map(this::fromGroupToDTO).toList();
        return ResponseEntity.status(201).body(toReturn);
    }

    @GetMapping("/{groupId}/members")
    @Operation(summary = "Получить всех пользователей группы",
            description = "Возвращает список всех пользователей в выбранный группе. " +
                    "Получить список пользователей может только  аутентифицированный пользователь член этой группы.")
    public ResponseEntity<List<UserInsensitiveInfoDTO>> getGroupMembers(@PathVariable Long groupId) {
        var members = groupService.getMembersOfGroup(groupId).stream().map(this::fromUserToDTO).toList();
        return ResponseEntity.ok(members);
    }

    private GroupDTO fromGroupToDTO(Group group) {return modelMapper.map(group, GroupDTO.class);}
    private UserInsensitiveInfoDTO fromUserToDTO(User user) {return modelMapper.map(user, UserInsensitiveInfoDTO.class);}
}