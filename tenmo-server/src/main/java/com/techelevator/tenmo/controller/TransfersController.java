package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.TransfersDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.SentToRequestFrom;
import com.techelevator.tenmo.model.Transfers;
import com.techelevator.tenmo.model.TransfersHistory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("api/")
@PreAuthorize("isAuthenticated()")
public class TransfersController {
    TransfersDao transfersDao;
    UserDao userDao;

    TransfersController(TransfersDao transfersDao, UserDao userDao) {
        this.transfersDao = transfersDao;
        this.userDao = userDao;
    }

    //transfer history: approved
    @PostMapping("/transfercompleted")
    public List<TransfersHistory> completedTransfersHistory(Principal principal) {
        int loggedInUserId = userDao.findIdByUsername(principal.getName());

        return transfersDao.completedTransfersHistoryByUserId(loggedInUserId);
    }
    //transfer history: pending
    @PostMapping("/transferpending")
    public List<TransfersHistory> pendingTransfersHistory(Principal principal) {
        int loggedInUserId = userDao.findIdByUsername(principal.getName());

        return transfersDao.pendingTransfersHistoryByUserId(loggedInUserId);
    }

    //receivers list
    @GetMapping("/sendto")
    public List<SentToRequestFrom> allReceivers(Principal principal) {
        int loggedInUserId = userDao.findIdByUsername(principal.getName());

        return transfersDao.findAllPossibleReceivers(loggedInUserId);
    }
    //send te bucks
    @PostMapping("/send")
    public String sendMoney(@RequestBody String[] collectTransferInfo) {
        int senderId = Integer.parseInt(collectTransferInfo[0]);
        int receiverId = Integer.parseInt(collectTransferInfo[1]);
        BigDecimal amountToSend = new BigDecimal(collectTransferInfo[2]);

        return transfersDao.completeSend(senderId, receiverId, amountToSend);
    }

    //individual transfer details
    @PostMapping("/transfer/details")
    public Transfers indiviualTransferDetails(@RequestBody int transferId){

        return transfersDao.transferDetailsByTransferId(transferId);
    }

    //sender list
    @GetMapping("/requestfrom")
    public List<SentToRequestFrom> allPossibleSenders(Principal principal) {
        int loggedInUserId = userDao.findIdByUsername(principal.getName());

        return transfersDao.findAllPossibleSenders(loggedInUserId);
    }
    //send te bucks
    @PostMapping("/request")
    public String requestMoney(@RequestBody String[] collectTransferInfo) {
        int requesterId = Integer.parseInt(collectTransferInfo[0]);
        int requestFromId = Integer.parseInt(collectTransferInfo[1]);
        BigDecimal amountRequested = new BigDecimal(collectTransferInfo[2]);
        String requestMessage = collectTransferInfo[3];

        return transfersDao.completeRequest(requesterId, requestFromId, amountRequested, requestMessage);
    }

    //approve-reject pending
    @PostMapping("/approvereject")
    public String approveRejectPending(@RequestBody String[] apiBody) {
        String action = apiBody[0];
        int transferId = Integer.parseInt(apiBody[1]);

        if(action.equals("approve")){
            return transfersDao.pendingApprove(transferId);
        } else {
            return transfersDao.pendingReject(transferId);
        }
    }

}