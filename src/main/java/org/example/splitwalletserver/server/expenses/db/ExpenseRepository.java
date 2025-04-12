package org.example.splitwalletserver.server.expenses.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> getExpensesByGroupId(Long groupId);
    Optional<Expense> findByIdAndGroupId(Long id, Long groupId);
}
