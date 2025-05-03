package org.example.expensesuserservice.db;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpenseUserRepository extends JpaRepository<ExpenseUser, Long> {
    List<ExpenseUser> findByExpenseId(Long expenseId);
    Optional<ExpenseUser> findByExpenseIdAndUserId(Long expenseId, String userId);
    Optional<List<ExpenseUser>> findByUserId(String userId);

}
