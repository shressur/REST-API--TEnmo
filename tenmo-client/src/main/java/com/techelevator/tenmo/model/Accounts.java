package com.techelevator.tenmo.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class Accounts {
    private int account_id;
    private int user_id;
    private BigDecimal balance;


}