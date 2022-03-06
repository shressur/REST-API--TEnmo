package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.SentToRequestFrom;
import com.techelevator.tenmo.model.Transfers;
import com.techelevator.tenmo.model.TransfersHistory;

import java.math.BigDecimal;
import java.util.List;

public interface TransfersDao {
    /*
    List<Transfers> findAllTransfers();
    List<Transfers> transfersByTransferTypeId(int transferTypeId);
    List<Transfers> transfersByTransferStatusId(int transferStatusId);
    List<Transfers> transfersByAccountFrom(int fromAccountId);
    List<Transfers> transfersByAccountTo(int toAccountId);
    Transfers transferByTransferId(int transferId);
    */

    //SEND
    //receivers
    List<SentToRequestFrom> findAllPossibleReceivers(int loggedInUserId);
    String completeSend(int senderId, int receiverId, BigDecimal amountSent);
    //transfer history: completed
    List<TransfersHistory> completedTransfersHistoryByUserId(int userId);
    //transfer history: pending
    List<TransfersHistory> pendingTransfersHistoryByUserId(int userId);
    //individual transfer details
    Transfers transferDetailsByTransferId(int transferId);

    //REQUEST
    List<SentToRequestFrom> findAllPossibleSenders(int loggedInUserId);
    String completeRequest(int senderId, int receiverId, BigDecimal amountSent, String requestMessage);

    //APPROVE-REJECT PENDING
    String pendingApprove(int transactionId);
    String pendingReject(int transactionId);

}
