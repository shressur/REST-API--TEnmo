package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Accounts;

import java.math.BigDecimal;

public interface AccountsDao {
    //BigDecimal findBalanceById(int userId);
    BigDecimal findBalanceOfCurrentUser(int userId);
    BigDecimal findBalanceOfCurrentUserName(String username);

    Accounts findAccountById(int userId);
    //List<Accounts> findAllAccounts();

}
