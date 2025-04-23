package org.example.splitwalletserver.server.groups.domain;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.example.splitwalletserver.server.groups.db.Group;
import org.example.splitwalletserver.server.groups.db.GroupRepository;
import org.example.splitwalletserver.server.groups.request.CreateGroupRequest;
import org.example.splitwalletserver.server.users.repositories.UserRepository;
import org.example.splitwalletserver.server.users.services.UserServiceImpl;
import org.example.splitwalletserver.server.users.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    private final UserServiceImpl userService;
    private final Integer maxSizeOfGroup = 50;

    public Group createGroup(CreateGroupRequest groupForm) {
        var toSave = new Group();
        toSave.setName(groupForm.getName());
        toSave.setUserOwner(userService.getCurrentUser());
        toSave.getMembers().add(userService.getCurrentUser());
        return groupRepository.save(toSave);
    }

    public void joinGroup(String uniqueCode) {
        var toJoin = groupRepository.findByUniqueCode(uniqueCode)
                .orElseThrow(()-> new EntityNotFoundException(
                        String.format("Group with code %s not found", uniqueCode)));

        var currentUser = userService.getCurrentUser();
        var idListOfMembers = toJoin.getMembers().stream().map(User::getId).toList();
        if (idListOfMembers.contains(currentUser.getId())) {
            throw new IllegalArgumentException("You already joined to group");
        }
        if (toJoin.getMembers().size() == maxSizeOfGroup) {
            throw new IllegalArgumentException("This group is full");
        }
        if (Boolean.TRUE.equals(toJoin.getIsClosed())) {
            throw new IllegalArgumentException("This group is closed");
        }
        toJoin.getMembers().add(currentUser);
        groupRepository.save(toJoin);

    }

    public void closeGroup(Long groupId) {
        var toClose = groupRepository.findById(groupId)
                .orElseThrow(()-> new EntityNotFoundException(
                        String.format("Group %s not found", groupId)));

        var currentUser = userService.getCurrentUser();
        if (!isUserMemberOfGroup(toClose, currentUser)) {
            throw new IllegalArgumentException("Permission denied. You do not member of the group");
        }
        toClose.setIsClosed(true);
        groupRepository.save(toClose);
    }

    public void deleteGroup(Long groupId) {
        var toDelete = groupRepository.findById(groupId)
                .orElseThrow(()-> new EntityNotFoundException(
                        String.format("Group %s not found", groupId)));

        var currentUser = userService.getCurrentUser();
        if (!isUserMemberOfGroup(toDelete, currentUser)) {
            throw new IllegalArgumentException("Permission denied. You do not member of the group");
        }
        groupRepository.delete(toDelete);
    }

    public List<Group> getMyGroups() {
        var currentUser = userService.getCurrentUser();
        return groupRepository.findAllByUserId(currentUser.getId());
    }

    public List<User> getMembersOfGroup(Long groupId) {
        var foundedGroup = groupRepository.findById(groupId)
                .orElseThrow(()-> new EntityNotFoundException(
                        String.format("Group with id %s not found", groupId))
                );
        var currentUser = userService.getCurrentUser();
        if (!isUserMemberOfGroup(foundedGroup, currentUser)) {
            throw new IllegalArgumentException("Permission denied. You do not member of the group");
        }
        return groupRepository.findMembersByGroupId(groupId);
    }

    public void deleteMembersOfGroup(Long groupId, String userId) {
        var foundedGroup = groupRepository.findById(groupId)
                .orElseThrow(()-> new EntityNotFoundException(
                        String.format("Group with id %s not found", groupId))
                );

        var userToDelete = userRepository.findById(userId);
        if (userToDelete.isEmpty()){
            throw new IllegalArgumentException(
                    String.format("User %s not found", userId));
        }
        if (!isUserMemberOfGroup(foundedGroup, userToDelete.get())) {
            throw new IllegalArgumentException("Permission denied. This user do not member of the group");
        }


        if (userToDelete.get().equals(foundedGroup.getUserOwner())) {
            throw new IllegalArgumentException("Unable to remove group owner");
        }

        var toDelGroup = foundedGroup;
        toDelGroup.getMembers().remove(userToDelete.get());
        groupRepository.save(toDelGroup);
    }

    public void leaveGroup(Long groupId) {
        var foundedGroup = groupRepository.findById(groupId)
                .orElseThrow(()-> new EntityNotFoundException(
                        String.format("Group with id %s not found", groupId))
                );
        var currentUser = userService.getCurrentUser();
        if (!isUserMemberOfGroup(foundedGroup, currentUser)) {
            throw new IllegalArgumentException("Permission denied. You do not member of the group");
        }

        var toDelGroup = foundedGroup;
        toDelGroup.getMembers().remove(currentUser);
        groupRepository.save(toDelGroup);
    }

    private boolean isUserMemberOfGroup(Group group, User user) {
        return group.getMembers().stream().map(User::getId).toList()
                .contains(user.getId());
    }

}
