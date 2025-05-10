package org.example.expensesuserservice.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExpenseUserRepository extends JpaRepository<ExpenseUser, Long> {
    List<ExpenseUser> findByExpenseId(Long expenseId);
    Optional<ExpenseUser> findByExpenseIdAndUserId(Long expenseId, String userId);

    Optional<List<ExpenseUser>> findByUserId(String userId);

    @Query("SELECT eu FROM ExpenseUser eu " +
            "WHERE eu.expense.userWhoCreated.id = :creditorId " +
            "AND eu.user.id <> :creditorId")
    Optional<List<ExpenseUser>> findDebtorsForUser(@Param("creditorId") String creditorId);

    @Query("SELECT eu FROM ExpenseUser eu " +
            "WHERE eu.user.id = :userId " +
            "AND eu.expense.group.id = :groupId")
    Optional<List<ExpenseUser>> findByUserIdAndGroupId(@Param("userId") String userId,
                                             @Param("groupId") Long groupId);

    @Query("SELECT eu FROM ExpenseUser eu " +
            "WHERE eu.expense.userWhoCreated.id = :creditorId " +
            "AND eu.user.id <> :creditorId " +
            "AND eu.expense.group.id = :groupId")
    Optional<List<ExpenseUser>> findDebtorsForUserAndGroup(@Param("creditorId") String creditorId,
                                                 @Param("groupId") Long groupId);




}
