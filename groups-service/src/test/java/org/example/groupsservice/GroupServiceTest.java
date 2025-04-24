package org.example.groupsservice;

import jakarta.persistence.EntityNotFoundException;
import org.example.groupsservice.db.Group;
import org.example.groupsservice.db.GroupRepository;
import org.example.groupsservice.other.User;
import org.example.groupsservice.request.CreateGroupRequest;
import org.example.groupsservice.service.GroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private GroupService groupService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createGroup_shouldReturnSavedGroup() {
        CreateGroupRequest request = new CreateGroupRequest("Test Group");
        String userId = "user123";

        Group savedGroup = new Group();
        savedGroup.setName("Test Group");
        savedGroup.setUserOwner(new User(userId));
        savedGroup.setMembers(new ArrayList<>(List.of(new User(userId))));

        when(groupRepository.save(any(Group.class))).thenReturn(savedGroup);

        Group result = groupService.createGroup(request, userId);

        assertThat(result.getName()).isEqualTo("Test Group");
        assertThat(result.getUserOwner().getId()).isEqualTo(userId);
        assertThat(result.getMembers()).hasSize(1);
        verify(groupRepository).save(any(Group.class));
    }

    @Test
    void joinGroup_shouldJoinSuccessfully() {
        String code = "group-code";
        String userId = "user456";
        User owner = new User("owner123");

        Group group = new Group();
        group.setUserOwner(owner);
        group.setMembers(new ArrayList<>());
        group.setIsClosed(false);

        when(groupRepository.findByUniqueCode(code)).thenReturn(Optional.of(group));

        groupService.joinGroup(code, userId);

        assertThat(group.getMembers()).extracting(User::getId).contains(userId);
        verify(groupRepository).save(group);
    }

    @Test
    void joinGroup_shouldThrow_whenGroupNotFound() {
        when(groupRepository.findByUniqueCode("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.joinGroup("invalid", "user"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void joinGroup_shouldThrow_whenAlreadyMember() {
        String userId = "user123";
        Group group = new Group();
        group.setUserOwner(new User("owner"));
        group.setMembers(new ArrayList<>(List.of(new User(userId))));
        group.setIsClosed(false);

        when(groupRepository.findByUniqueCode("code")).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> groupService.joinGroup("code", userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already joined");
    }

    @Test
    void joinGroup_shouldThrow_whenOwnerTriesToJoin() {
        String ownerId = "owner";
        Group group = new Group();
        group.setUserOwner(new User(ownerId));
        group.setMembers(new ArrayList<>());
        group.setIsClosed(false);

        when(groupRepository.findByUniqueCode("code")).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> groupService.joinGroup("code", ownerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Owner cannot join");
    }

    @Test
    void joinGroup_shouldThrow_whenGroupIsFull() {
        int maxSizeOfGroup = 50;
        String userId = "user123";
        Group group = new Group();
        group.setUserOwner(new User("owner"));
        List<User> members = new ArrayList<>();
        for (int i = 0; i < maxSizeOfGroup; i++) members.add(new User("u" + i));
        group.setMembers(members);
        group.setIsClosed(false);

        when(groupRepository.findByUniqueCode("code")).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> groupService.joinGroup("code", userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("group is full");
    }

    @Test
    void joinGroup_shouldThrow_whenGroupIsClosed() {
        String userId = "user123";
        Group group = new Group();
        group.setUserOwner(new User("owner"));
        group.setMembers(new ArrayList<>());
        group.setIsClosed(true);

        when(groupRepository.findByUniqueCode("code")).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> groupService.joinGroup("code", userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("group is closed");
    }

    @Test
    void closeGroup_shouldSetGroupClosed() {
        Group group = new Group();
        group.setUserOwner(new User("user1"));
        group.setIsClosed(false);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        groupService.closeGroup(1L, "user1");

        assertThat(group.getIsClosed()).isTrue();
        verify(groupRepository).save(group);
    }

    @Test
    void closeGroup_shouldThrow_whenNotOwner() {
        Group group = new Group();
        group.setUserOwner(new User("owner"));
        group.setIsClosed(false);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> groupService.closeGroup(1L, "user"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only the owner");
    }

    @Test
    void closeGroup_shouldThrow_whenGroupNotFound() {
        when(groupRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.closeGroup(1L, "user"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteGroup_shouldDeleteSuccessfully() {
        var owner = new User("owner123");
        var group = new Group();
        group.setId(1L);
        group.setUserOwner(owner);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        groupService.deleteGroup(1L, "owner123");

        verify(groupRepository).delete(group);
    }

    @Test
    void deleteGroup_shouldThrowWhenGroupNotFound() {
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> groupService.deleteGroup(1L, "owner123"));
    }

    @Test
    void deleteGroup_shouldThrow_whenNotOwner(){
        var group = new Group();
        group.setId(1L);
        group.setUserOwner(new User("owner123"));

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        assertThrows(IllegalArgumentException.class, () -> groupService.deleteGroup(1L, "user456"));
    }

    @Test
    void getGroupsByUserId() {
        var groups = List.of(new Group(), new Group());

        when(groupRepository.findAllByUserId("user1")).thenReturn(groups);

        var result = groupService.getGroupsByUserId("user1");

        assertEquals(2, result.size());
    }

    @Test
    void getGroupByGroupId_shouldGetSuccessfully() {
        var group = new Group();
        group.setId(1L);
        var user = new User("user1");
        group.setMembers(List.of(user));

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        var result = groupService.getGroupByGroupId(1L, "user1");

        assertEquals(group, result);
    }

    @Test
    void getGroupByGroup_shouldThrowWhenIdNotMember() {
        var group = new Group();
        group.setId(1L);
        group.setMembers(new ArrayList<>());

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        assertThrows(IllegalArgumentException.class, () -> groupService.getGroupByGroupId(1L, "user2"));
    }

    @Test
    void getMembersOfGroup_shouldGetSuccessfully() {
        var user = new User("user1");
        var group = new Group();
        group.setMembers(List.of(user));

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(groupRepository.findMembersByGroupId(1L)).thenReturn(List.of(user));

        var members = groupService.getMembersOfGroup(1L, "user1");

        assertEquals(1, members.size());
        assertEquals("user1", members.get(0).getId());
    }

    @Test
    void getMembersOfGroup_shouldThrowWhenIdNotMember() {
        var group = new Group();
        group.setMembers(new ArrayList<>());

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        assertThrows(IllegalArgumentException.class, () -> groupService.getMembersOfGroup(1L, "user2"));
    }

    @Test
    void deleteMembersOfGroup_shouldDeleteSuccessfully() {
        var owner = new User("owner1");
        var user = new User("user2");
        var group = new Group();
        group.setUserOwner(owner);
        group.setMembers(new ArrayList<>(List.of(owner, user)));

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        groupService.deleteMembersOfGroup(1L, "user2", "owner1");

        verify(groupRepository).save(group);
        assertEquals(1, group.getMembers().size());
        assertFalse(group.getMembers().contains(user));
    }

    @Test
    void deleteMembersOfGroup_shouldThrowWhenUserNotMember() {
        var owner = new User("owner1");
        var group = new Group();
        group.setUserOwner(owner);
        group.setMembers(List.of(owner));

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        assertThrows(IllegalArgumentException.class, () -> groupService.deleteMembersOfGroup(1L, "user2", "owner1"));
    }

    @Test
    void deleteMembersOfGroup_shouldThrowWhenOwner() {
        var owner = new User("owner1");
        var group = new Group();
        group.setUserOwner(owner);
        group.setMembers(List.of(owner));

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        assertThrows(IllegalArgumentException.class, () -> groupService.deleteMembersOfGroup(1L, "owner1", "owner1"));
    }

    @Test
    void deleteMembersOfGroup_shouldThrowWhenRequesterNotMember() {
        var user = new User("user2");
        var group = new Group();
        group.setUserOwner(new User("owner1"));
        group.setMembers(List.of(user));

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        assertThrows(IllegalArgumentException.class, () -> groupService.deleteMembersOfGroup(1L, "user2", "user3"));
    }
}
