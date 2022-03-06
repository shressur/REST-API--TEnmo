package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Accounts;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class JdbcAccountsDao implements AccountsDao{
    JdbcTemplate jdbcTemplate;

    //private final String sqlAllAccounts = "SELECT account_id, user_id, balance FROM accounts;";
    private final String sqlAccountById = "SELECT account_id, user_id, balance FROM accounts WHERE user_id = ?;";
    private final String sqlBalanceById = "SELECT balance FROM accounts WHERE user_id = ?;";

    private final String sqlBalanceByUserName = "SELECT balance FROM accounts AS a JOIN users AS u ON a.user_id = u.user_id WHERE username = ?;";

    //jdbctemplate always needs a constructor
    //else .setDataSource() and other properties are required to connect to right db
    public JdbcAccountsDao(JdbcTemplate jdbcTemplate) { //spring will take care of the connection
        this.jdbcTemplate = jdbcTemplate;
    }

    //jdbctemplate.queryforobject => single data/cell value or an entire row as an object
    @Override
    public BigDecimal findBalanceOfCurrentUser(int userId) {
        return jdbcTemplate.queryForObject(sqlBalanceById, BigDecimal.class, userId);
    }

    @Override
    public BigDecimal findBalanceOfCurrentUserName(String username) {
        return jdbcTemplate.queryForObject(sqlBalanceByUserName, BigDecimal.class, username);
    }


    @Override
    public Accounts findAccountById(int userId) {
        return jdbcTemplate.queryForObject(sqlAccountById, Accounts.class, userId);
    }


    //returning single object/value so no need of row mapper
    //row mapper
/*
    private Accounts mapRowSetToAccounts(SqlRowSet rowset) {
        Accounts tempAccount = new Accounts();
        tempAccount.setBalance(rowset.getBigDecimal("balance"));
        tempAccount.setUser_id(rowset.getInt("user_id"));
        tempAccount.setAccount_id(rowset.getInt("account_id"));

        return tempAccount;
    }
    */
}
