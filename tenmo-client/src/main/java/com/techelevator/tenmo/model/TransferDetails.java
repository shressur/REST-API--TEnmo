package com.techelevator.tenmo.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferDetails {
    private int transferId;
    private String fromTransfer;
    private String toTransfer;
    private String typeTransfer;
    private String statusTransfer;
    private BigDecimal amount;

    //used in transfer history
    //not all properties has to be set in query result!!!
    private String transferTypeDesc;
    private String transferStatusDesc;
    private String accountFromName;
    private String accountToName;

}
