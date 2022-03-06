package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.AuthenticatedUser;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

public class AccountsService {
    RestTemplate restTemplate = new RestTemplate();
    String resource = "api/balances";

//    public AccountsService(AuthenticatedUser authenticatedUser){
//        this.authenticatedUser = authenticatedUser;
//    }
//    public AccountsService(RestTemplate restTemplate, AuthenticatedUser authenticatedUser){
//        this.restTemplate = restTemplate;
//        this.authenticatedUser = authenticatedUser;
//    }


    //view balance
    public BigDecimal findBalance(String base_url, AuthenticatedUser authenticatedUser){
        String auth  =  authenticatedUser.getToken();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setBearerAuth(auth);
        //HttpEntity<Void>  entity = new HttpEntity<>(httpHeaders);
        HttpEntity<?>  entity = new HttpEntity<>(httpHeaders);  //undefined entity type

        ResponseEntity<BigDecimal> res =  restTemplate.exchange(base_url + resource, HttpMethod.GET, entity, BigDecimal.class);
        return res.getBody();
    }
}
