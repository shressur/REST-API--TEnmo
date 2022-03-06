package com.techelevator.tenmo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SentToRequestFrom {
    //server
    //field names on server-model and client-model must match
    private String userName;
    private int userId;

}

