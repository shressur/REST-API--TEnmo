package com.techelevator.tenmo.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class Transfers {
    private int transferId;
    private int transferTypeId;
    private int transferStatusId;
    private int accountFrom;
    private int accountTo;
    private BigDecimal amount;

    //used in transfer history
    //not all properties has to be set in query result-set!!!
    private String transferTypeDesc;
    private String transferStatusDesc;
    private String accountFromName;
    private String accountToName;


}
