package org.example.groupsservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.groupsservice.controller.GroupController;
import org.example.groupsservice.db.Group;
import org.example.groupsservice.dto.GroupDTO;
import org.example.groupsservice.dto.UserInsensitiveInfoDTO;
import org.example.groupsservice.other.User;
import org.example.groupsservice.request.CreateGroupRequest;
import org.example.groupsservice.service.GroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.hamcrest.Matchers.is;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class GroupControllerTest {

    @InjectMocks
    private GroupController groupController;

    @Mock
    private GroupService groupService;

    @Mock
    private ModelMapper modelMapper;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String userId;

    private Jwt jwt;

    Authentication auth;


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(groupController).build();
        userId = "user123";
        jwt = mock(Jwt.class);
        when(jwt.getClaim("sub")).thenReturn(userId);

        auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(jwt);
    }

    @Test
    void createGroup_shouldReturnCreatedGroup() throws Exception {

        String groupName = "Test Group";


        CreateGroupRequest reqBody = new CreateGroupRequest();
        reqBody.setName(groupName);

        Group group = new Group();
        group.setId(1L);
        group.setName(groupName);

        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setId(1L);
        groupDTO.setName(groupName);

        when(groupService.createGroup(any(), eq(userId))).thenReturn(group);
        when(modelMapper.map(eq(group), eq(GroupDTO.class))).thenReturn(groupDTO);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/groups/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBody))
                        .principal(auth))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value(groupName));

        verify(groupService).createGroup(any(CreateGroupRequest.class), eq(userId));
        verify(modelMapper).map(group, GroupDTO.class);

        verifyNoMoreInteractions(groupService, modelMapper);
    }

    @Test
    void joinGroup_shouldReturnSuccessMessage() throws Exception {

        String uniqueCode = "abc123";

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/groups/{uniqueCode}/join", uniqueCode)
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().string("Success!"));

        verify(groupService).joinGroup(uniqueCode, userId);
        verifyNoMoreInteractions(groupService);
    }

    @Test
    void closeGroup_shouldReturnSuccessMessage() throws Exception {
        Long groupId = 1L;

        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/groups/{groupId}/close", groupId)
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().string("Success!"));

        verify(groupService).closeGroup(groupId, userId);
        verifyNoMoreInteractions(groupService);
    }

    @Test
    void deleteGroup_shouldReturnSuccessMessage() throws Exception {
        Long groupId = 1L;

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/groups/{groupId}", groupId)
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().string("Success!"));

        verify(groupService).deleteGroup(groupId, userId);
        verifyNoMoreInteractions(groupService);
    }

    @Test
    void getMyGroups_shouldReturnListOfGroupDTOs() throws Exception {
        // Arrange
        Group group1 = new Group();
        group1.setName("Group One");
        Group group2 = new Group();
        group2.setName("Group Two");

        GroupDTO groupDTO1 = new GroupDTO();
        groupDTO1.setName("Group One");
        GroupDTO groupDTO2 = new GroupDTO();
        groupDTO2.setName("Group Two");

        when(groupService.getGroupsByUserId(userId)).thenReturn(List.of(group1, group2));
        when(modelMapper.map(group1, GroupDTO.class)).thenReturn(groupDTO1);
        when(modelMapper.map(group2, GroupDTO.class)).thenReturn(groupDTO2);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/groups/my")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Group One"))
                .andExpect(jsonPath("$[1].name").value("Group Two"));

        verify(groupService).getGroupsByUserId(userId);
        verifyNoMoreInteractions(groupService);
    }

    @Test
    void getGroupMembers_shouldReturnListOfUsers() throws Exception {
        // Arrange
        Long groupId = 1L;

        User user1 = new User();
        user1.setUsername("john");

        User user2 = new User();
        user2.setUsername("jane");

        UserInsensitiveInfoDTO dto1 = new UserInsensitiveInfoDTO();
        dto1.setUsername("john");

        UserInsensitiveInfoDTO dto2 = new UserInsensitiveInfoDTO();
        dto2.setUsername("jane");

        when(groupService.getMembersOfGroup(groupId, userId)).thenReturn(List.of(user1, user2));
        when(modelMapper.map(user1, UserInsensitiveInfoDTO.class)).thenReturn(dto1);
        when(modelMapper.map(user2, UserInsensitiveInfoDTO.class)).thenReturn(dto2);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/groups/{groupId}/members", groupId)
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("john"))
                .andExpect(jsonPath("$[1].username").value("jane"));

        verify(groupService).getMembersOfGroup(groupId, userId);
        verifyNoMoreInteractions(groupService);
    }

    @Test
    void deleteGroupMembers_shouldCallServiceAndReturnSuccess() throws Exception {
        Long groupId = 1L;
        String userToDeleteId = "user456";

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/groups/{groupId}/members/{userId}", groupId, userToDeleteId)
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().string("Success!!!"));

        verify(groupService).deleteMembersOfGroup(groupId, userToDeleteId, userId);
        verifyNoMoreInteractions(groupService);
    }

    @Test
    void getGroupById_shouldReturnGroupForMember() throws Exception {
        Long groupId = 1L;

        Group group = new Group();
        group.setId(groupId);
        group.setName("Test Group");

        when(groupService.getGroupByGroupId(groupId, userId)).thenReturn(group);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/groups/{groupId}", groupId)
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(groupId.intValue())))
                .andExpect(jsonPath("$.name", is("Test Group")));

        verify(groupService).getGroupByGroupId(groupId, userId);
        verifyNoMoreInteractions(groupService);
    }
}
