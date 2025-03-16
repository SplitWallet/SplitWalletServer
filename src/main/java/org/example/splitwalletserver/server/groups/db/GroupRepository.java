package org.example.splitwalletserver.server.groups.db;

import org.example.splitwalletserver.server.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByUniqueCode(String uniqueCode);

    Optional<Group> findById(Long id);

    @Query("SELECT g FROM Group g WHERE g.userOwner.id = :userId OR :userId IN (SELECT u.id FROM g.members u)")
    List<Group> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT g.members FROM Group g WHERE g.id = :groupId")
    List<User> findMembersByGroupId(@Param("groupId") Long groupId);

    boolean existsById(Long id);
}
