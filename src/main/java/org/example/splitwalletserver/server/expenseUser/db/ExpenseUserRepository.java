package org.example.splitwalletserver.server.expenseUser.db;

import org.example.splitwalletserver.server.expenseUser.ExpenseUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseUserRepository extends JpaRepository<ExpenseUser, Long> {
}
