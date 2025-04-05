package org.example.splitwalletserver.server.groups.domain;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.example.splitwalletserver.server.groups.db.Group;
import org.example.splitwalletserver.server.groups.db.GroupRepository;
import org.example.splitwalletserver.server.groups.request.CreateGroupRequest;
import org.example.splitwalletserver.server.users.services.UserServiceImpl;
import org.example.splitwalletserver.server.users.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
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
                .orElseThrow(()-> new EntityNotFoundException("Group with code " + uniqueCode+ " not found"));

        var currentUser = userService.getCurrentUser();
        if (currentUser.getId().equals(toJoin.getUserOwner().getId())) {
            throw new IllegalArgumentException("Owner cannot join to group");
        }
        var idListOfMembers = toJoin.getMembers().stream().map(User::getId).toList();
        if (idListOfMembers.contains(currentUser.getId())) {
            throw new IllegalArgumentException("You already joined to group");
        }
        if (toJoin.getMembers().size() == maxSizeOfGroup) {
            throw new IllegalArgumentException("This group is full");
        }
        if (toJoin.getIsClosed()) {
            throw new IllegalArgumentException("This group is closed");
        }
        toJoin.getMembers().add(currentUser);
        groupRepository.save(toJoin);

    }

    public List<Group> getMyGroups() {
        var currentUser = userService.getCurrentUser();
        return groupRepository.findAllByUserId(currentUser.getId());
    }

    public List<User> getMembersOfGroup(Long groupId) {
        var foundedGroup = groupRepository.findById(groupId)
                .orElseThrow(()-> new EntityNotFoundException("Group with id " + groupId + " not found"));
        var currentUser = userService.getCurrentUser();
        if (!isUserMemberOfGroup(foundedGroup, currentUser)) {
            throw new IllegalArgumentException("Permission denied. You do not member of the group");
        }
        return groupRepository.findMembersByGroupId(groupId);
    }

    private boolean isUserMemberOfGroup(Group group, User user) {
        return group.getMembers().stream().map(User::getId).toList()
                .contains(user.getId());
    }

}
