package com.techelevator.tenmo.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransfersHistory {
    private int transferId;
    private String toFrom;
    private BigDecimal amount;

}
