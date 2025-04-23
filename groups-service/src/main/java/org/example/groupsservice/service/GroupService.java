package org.example.groupsservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.example.groupsservice.db.Group;
import org.example.groupsservice.db.GroupRepository;
import org.example.groupsservice.other.User;
import org.example.groupsservice.request.CreateGroupRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;

    private static final Integer maxCountOfCreatedGroup = 1000;
    private static final Integer maxSizeOfGroup = 50;

    public Group createGroup(CreateGroupRequest groupForm, String currentUserId) {
        var toSave = new Group();
        var addUser = new User(currentUserId);
        toSave.setName(groupForm.getName());
        toSave.setUserOwner(addUser);
        toSave.getMembers().add(addUser);
        return groupRepository.save(toSave);
    }

    public void joinGroup(String uniqueCode, String currentUserId) {
        var toJoin = groupRepository.findByUniqueCode(uniqueCode)
                .orElseThrow(()-> new EntityNotFoundException(
                        String.format("Group with code %s not found", uniqueCode))
                );

        if (currentUserId.equals(toJoin.getUserOwner().getId())) {
            throw new IllegalArgumentException("Owner cannot join to group");
        }
        var idListOfMembers = toJoin.getMembers().stream().map(User::getId).toList();
        if (idListOfMembers.contains(currentUserId)) {
            throw new IllegalArgumentException("You already joined to group");
        }
        if (toJoin.getMembers().size() == maxSizeOfGroup) {
            throw new IllegalArgumentException("This group is full");
        }
        if (Boolean.TRUE.equals(toJoin.getIsClosed())) {
            throw new IllegalArgumentException("This group is closed");
        }
        toJoin.getMembers().add(new User(currentUserId));
        groupRepository.save(toJoin);

    }

    public void closeGroup(Long groupId, String currentUserId) {
        var toClose = groupRepository.findById(groupId)
                .orElseThrow(()-> new EntityNotFoundException(String.format("Group %s not found",groupId)));

        if (!currentUserId.equals(toClose.getUserOwner().getId())) {
            throw new IllegalArgumentException("Only the owner can close the group");
        }
        toClose.setIsClosed(true);
        groupRepository.save(toClose);
    }

    public void deleteGroup(Long groupId, String currentUserId) {
        var toJoin = groupRepository.findById(groupId)
                .orElseThrow(()-> new EntityNotFoundException(String.format("Group %s not found",groupId)));

        if (!currentUserId.equals(toJoin.getUserOwner().getId())) {
            throw new IllegalArgumentException("Only the owner can delete to group");
        }
        groupRepository.delete(toJoin);
    }

    public List<Group> getGroupsByUserId(String userId) {
        return groupRepository.findAllByUserId(userId);
    }

    public Group getGroupsByGroupId(Long groupId) {
        var group = groupRepository.findById(groupId);
        if (group.isPresent()) {
            return group.get();
        }
        throw new EntityNotFoundException("Group with id " + groupId + " not found");

    }

    public List<User> getMembersOfGroup(Long groupId, String currentUserId) {
        var foundedGroup = groupRepository.findById(groupId)
                .orElseThrow(()-> new EntityNotFoundException(String.format("Group %s not found",groupId)));
        if (!isUserMemberOfGroup(foundedGroup, currentUserId)) {
            throw new IllegalArgumentException("Permission denied. You do not member of the group");
        }
        return groupRepository.findMembersByGroupId(groupId);
    }

    public void deleteMembersOfGroup(Long groupId, String userId, String currentUserId) {
        var group  = groupRepository.findById(groupId)
                .orElseThrow(()->
                        new EntityNotFoundException(String.format("Group %s not found",groupId)));

        if (!isUserMemberOfGroup(group, currentUserId)) {
            throw new IllegalArgumentException("Permission denied. You do not member of the group");
        }

        User userToDelete = group.getMembers().stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(String.format("User %s not found in the group",userId)));


        if (userToDelete.equals(group.getUserOwner())) {
            throw new IllegalArgumentException("Unable to remove group owner");
        }


        var toDelGroup = group;
        toDelGroup.getMembers().remove(userToDelete);
        groupRepository.save(toDelGroup);
    }

    private boolean isUserMemberOfGroup(Group group, String currentUserId) {
        return group.getMembers().stream().map(User::getId).toList()
                .contains(currentUserId);
    }
}
