package org.example.splitwalletserver.server.expenses.db;

import org.example.splitwalletserver.server.expenses.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> getExpensesByGroupId(Long groupId);
}
