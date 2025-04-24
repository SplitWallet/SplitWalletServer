package org.example.groupsservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.groupsservice.db.Group;
import org.example.groupsservice.dto.GroupDTO;
import org.example.groupsservice.dto.UserInsensitiveInfoDTO;
import org.example.groupsservice.other.User;
import org.example.groupsservice.request.CreateGroupRequest;
import org.example.groupsservice.service.GroupService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/groups")
public class GroupController {

    private final GroupService groupService;
    private final ModelMapper modelMapper;

    @PostMapping("/create")
    @Operation(summary = "Создать новую группу",
            description = "Создает новую группу с указанными параметрами. Группа может быть создана только аутентифицированным пользователем.")
    public ResponseEntity<GroupDTO> createGroup(@RequestBody @Valid CreateGroupRequest createGroupRequest,
                                                HttpServletRequest req) {
        var authentication = (Authentication) req.getUserPrincipal();
        var jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getClaim("sub");

        var created = groupService.createGroup(createGroupRequest,currentUserId);
        return ResponseEntity.status(201).body(fromGroupToDTO(created));
    }

    @PostMapping("{uniqueCode}/join")
    @Operation(summary = "Присоединиться к группе по коду",
            description = "Позволяет текущему аутентифицированному пользователю присоединиться к группе, используя уникальный код группы.")
    public ResponseEntity<String> joinGroup(@PathVariable String uniqueCode,
                                            HttpServletRequest req) {
        var authentication = (Authentication) req.getUserPrincipal();
        var jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getClaim("sub");

        groupService.joinGroup(uniqueCode, currentUserId);
        return ResponseEntity.status(201).body("Success!");
    }

    @PatchMapping("{groupId}/close")
    @Operation(summary = "Закрыть группу",
            description = "Позволяет аутентифицированному владельцу группы, закрыть ее")
    public ResponseEntity<String> closeGroup(@PathVariable Long groupId,
                                             HttpServletRequest req) {
        var authentication = (Authentication) req.getUserPrincipal();
        var jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getClaim("sub");

        groupService.closeGroup(groupId, currentUserId);
        return ResponseEntity.status(201).body("Success!");
    }

    @DeleteMapping("{groupId}")
    @Operation(summary = "Удалить группу",
            description = "Позволяет аутентифицированному владельцу группы, удалить ее.")
    public ResponseEntity<String> deleteGroup(@PathVariable Long groupId,
                                              HttpServletRequest req) {
        var authentication = (Authentication) req.getUserPrincipal();
        var jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getClaim("sub");
        groupService.deleteGroup(groupId, currentUserId);
        return ResponseEntity.status(201).body("Success!");
    }

    @GetMapping("/my")
    @Operation(summary = "Получить группы по пользователю",
            description = "Получить группы, в которых состоит текущий пользователь. Получить свои группы может только  аутентифицированный пользователь.")
    public ResponseEntity<List<GroupDTO>> getMyGrouos(HttpServletRequest req) {
        var authentication = (Authentication) req.getUserPrincipal();
        var jwt = (Jwt) authentication.getPrincipal();
        String userId = jwt.getClaim("sub");

        var toReturn = groupService.getGroupsByUserId(userId).stream()
                .map(this::fromGroupToDTO).toList();

        return ResponseEntity.status(201).body(toReturn);
    }

    @GetMapping("/{groupId}/members")
    @Operation(summary = "Получить всех пользователей группы",
            description = "Возвращает список всех пользователей в выбранный группе. " +
                    "Получить список пользователей может только  аутентифицированный пользователь член этой группы.")
    public ResponseEntity<List<UserInsensitiveInfoDTO>> getGroupMembers(@PathVariable Long groupId,
                                                                        HttpServletRequest req) {
        var authentication = (Authentication) req.getUserPrincipal();
        var jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getClaim("sub");

        var members = groupService.getMembersOfGroup(groupId, currentUserId).stream().map(this::fromUserToDTO).toList();
        return ResponseEntity.status(201).body(members);
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    @Operation(summary = "Удалить пользователя группы",
            description = "Удалить пользователя группы по id. " +
                    "Получить список пользователей может только  аутентифицированный пользователь член этой группы.")
    public ResponseEntity<String> deleteGroupMembers(@PathVariable Long groupId,
                                                     @PathVariable String userId,
                                                     HttpServletRequest req) {
        var authentication = (Authentication) req.getUserPrincipal();
        var jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getClaim("sub");

        groupService.deleteMembersOfGroup(groupId, userId, currentUserId);
        return ResponseEntity.status(201).body("Success!!!");
    }

    @GetMapping("/{groupId}")
    @Operation(summary = "Получить группу по id",
            description = "Получить группу по id " +
                    "Получить группу может только  аутентифицированный пользователь член этой группы.")
    public Group getGroupById(@PathVariable Long groupId, HttpServletRequest req) {
        var authentication = (Authentication) req.getUserPrincipal();
        var jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getClaim("sub");
        return groupService.getGroupByGroupId(groupId, currentUserId);
    }

    private GroupDTO fromGroupToDTO(Group group) {return modelMapper.map(group, GroupDTO.class);}

    private UserInsensitiveInfoDTO fromUserToDTO(User user) {return modelMapper.map(user, UserInsensitiveInfoDTO.class);}
}

