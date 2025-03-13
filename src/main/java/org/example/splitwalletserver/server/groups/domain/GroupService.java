package org.example.splitwalletserver.server.groups.domain;

import lombok.AllArgsConstructor;
import org.example.splitwalletserver.server.groups.request.CreateGroupRequest;
import org.example.splitwalletserver.server.groups.db.Group;
import org.example.splitwalletserver.server.groups.db.GroupRepository;
import org.example.splitwalletserver.server.services.UserService;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserService userService;

    public Group createGroup(CreateGroupRequest groupForm) {
        var toSave = new Group();
        toSave.setName(groupForm.getName());
        toSave.setUserOwner(userService.getCurrentUser());
        toSave.getMembers().add(userService.getCurrentUser());
        return groupRepository.save(toSave);
    }

}
