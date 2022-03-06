package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountsDao;
import com.techelevator.tenmo.dao.UserDao;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("api")
public class AccountsController {
    //must have code: else shows null pointer exception
    //@Autowired AccountsDao accountsDao;   //autowired or constructor
    AccountsDao accountsDao;
    UserDao userDao;
    AccountsController(AccountsDao accountsDao, UserDao userDao){
        this.accountsDao = accountsDao;
        this.userDao = userDao;
    }

    //view balance
    @GetMapping("/balances")
    public BigDecimal balanceByUserId(Principal principal){
        String userName = principal.getName();
        //int userId = userDao.findIdByUsername(userName);
        //return accountsDao.findBalanceOfCurrentUser(userId);

        //username is UNIQUE so no need of userId
        return accountsDao.findBalanceOfCurrentUserName(userName);
    }

    /*
    @GetMapping("accounts/balance") //.../api/accounts/balance?uid=123
    public BigDecimal balanceOfCurrentUser(@RequestParam (name = "uid") int userId, Principal principal){

        String userName = principal.getName();
        userId = userDao.findIdByUsername(userName);

        return accountsDao.findBalanceOfCurrentUser(userId);
    }

    @GetMapping("/accounts")        //unauthorized
    public List<Accounts> test(){
        return accountsDao.findAllAccounts();
    }

    @GetMapping("/account/{uid}")   //unauthorized
    //
    */


}
