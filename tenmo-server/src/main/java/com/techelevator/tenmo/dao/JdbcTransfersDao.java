package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.SentToRequestFrom;
import com.techelevator.tenmo.model.Transfers;
import com.techelevator.tenmo.model.TransfersHistory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransfersDao implements TransfersDao{
    Connection con;

    JdbcTemplate jdbcTemplate;
    //jdbcTemplate.setDataSource(jldjfl);

    public JdbcTransfersDao(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

//There are a lot of repetition between send and receive (they are very similar)
//I could have created a single function with parameter like: transactionType: send/request
//But for the shake of simplicity, I am ignoring this approach

//done
//region "Receiver's List"

    @Override
    public List<SentToRequestFrom> findAllPossibleReceivers(int loggedInUserId) {
        String sqlAllReceivers = "SELECT user_id, username FROM users WHERE user_id != ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlAllReceivers, loggedInUserId);
        List<SentToRequestFrom> tempReceivers = new ArrayList<>();
        while (rs.next()){
            tempReceivers.add(mapRowSetToReceiver(rs));
        }
        return tempReceivers;
    }

//endregion
//done
//region "Transfers History: completed"
@Override
public List<TransfersHistory> completedTransfersHistoryByUserId(int userId) {
    String sqlTransferHistoryByUserId =
            "SELECT t.transfer_id transfer_id, 'from: ' || u.username to_from, t.amount amount FROM transfers AS t" +
            " JOIN accounts AS a ON t.account_from = a.account_id" +
            " JOIN users AS u ON a.user_id = u.user_id" +
            " WHERE t.account_to = (SELECT account_id FROM accounts WHERE user_id = ?) AND t.transfer_status_id != 1" +
            " UNION" +
            " SELECT t.transfer_id transfer_id, 'to: ' || u.username to_from, t.amount amount FROM transfers AS t" +
            " JOIN accounts AS a ON t.account_to = a.account_id" +
            " JOIN users AS u ON a.user_id = u.user_id" +
            " WHERE t.account_from = (SELECT account_id FROM accounts WHERE user_id = ?) AND t.transfer_status_id != 1" +
            " ORDER BY transfer_id ASC;";
    //transfer_status_id = 1 => pending transfers (view in pending transfers)

    List<TransfersHistory> transfersHistoryList = new ArrayList<>();

    try{
        con = jdbcTemplate.getDataSource().getConnection();
        PreparedStatement pstmt = con.prepareStatement(sqlTransferHistoryByUserId);
        pstmt.setInt(1, userId);
        pstmt.setInt(2, userId);

        //.queryForRowSet() does not work with alias field
        ResultSet resultset = pstmt.executeQuery();    //.execute() => only runs code => boolean

        while (resultset.next()){
            TransfersHistory transfershistory = new TransfersHistory();

            transfershistory.setTransferId(resultset.getInt("transfer_id"));
            transfershistory.setToFrom(resultset.getString("to_from"));
            transfershistory.setAmount(resultset.getBigDecimal("amount"));

            transfersHistoryList.add(transfershistory);
        }
    } catch (Exception e){
        System.out.println("No connection");
    }

    return transfersHistoryList;
}
//endregion
//done
//region "Complete Send"
    @Override
    public String completeSend(int senderId, int receiverId, BigDecimal amountToSend) {
        //check if enough balance is available
        String sqlCheckBalance = "SELECT balance FROM accounts WHERE user_id = ?;";
        BigDecimal availableBalance = jdbcTemplate.queryForObject(sqlCheckBalance, BigDecimal.class,senderId);
        if(availableBalance.subtract(amountToSend).signum() >= 0){   //bigdecimal1.subtract(bigdecimal2).signum()>0
            // val.signum() => sign function. range: -1.0   0.0   1.0 => val: <0   0   >0
            //Math.signum(intval_or_doubleval)

            //System.out.println(availableBalance.subtract(amountToSend).signum() > 0);

            //check if receiver exists
            //String sqlIsOne = "SELECT 1 FROM users WHERE user_id = ? LIMIT 1;";
            //int returnedNumber = jdbcTemplate.queryForObject(sqlIsOne, Integer.class, receiverId);

            String sqlCheckReceiver = "select exists(select 1 from users where user_id = ?) AS exists";
            boolean receiverExists = Boolean.TRUE.equals(jdbcTemplate.queryForObject(sqlCheckReceiver, Boolean.class, receiverId));

            if(receiverExists){
                try{
                    //add to receiver's balance
                    String sqlAddTo = "UPDATE accounts SET balance = balance + ? WHERE user_id = ?";
                    jdbcTemplate.update(sqlAddTo, amountToSend, receiverId);
                    //subtract from sender's balance
                    String sqlSubtractFrom = "UPDATE accounts SET balance = balance - ? WHERE user_id = ?";
                    jdbcTemplate.update(sqlSubtractFrom, amountToSend, senderId);
                    //update transfers table
                    String sqlTransferTypeId = "SELECT transfer_type_id FROM transfer_types WHERE transfer_type_desc = ?;";
                    int transferTypeId = jdbcTemplate.queryForObject(sqlTransferTypeId, Integer.class, "Send");
                    String sqlTransferStatusId = "SELECT transfer_status_id FROM transfer_statuses WHERE transfer_status_desc = ?;";
                    int transferStatusId = jdbcTemplate.queryForObject(sqlTransferStatusId, Integer.class, "Approved");
                    String sqlSenderAccountId = "SELECT account_id FROM accounts WHERE user_id = ?;";
                    int senderAccountId = jdbcTemplate.queryForObject(sqlSenderAccountId, Integer.class, senderId);
                    String sqlReceiverAccountId = "SELECT account_id FROM accounts WHERE user_id = ?;";
                    int receiverAccountId = jdbcTemplate.queryForObject(sqlReceiverAccountId, Integer.class, receiverId);

                    //account_to and account_from are account_id in accounts table not user_id!!!

                    String sqlAddToTransfers = "INSERT INTO transfers (transfer_type_id, transfer_status_id, account_from, account_to, amount) VALUES (?, ?, ?, ?, ?);";

                    jdbcTemplate.update(sqlAddToTransfers, transferTypeId, transferStatusId, senderAccountId, receiverAccountId, amountToSend);
                } catch (Exception ex){
                    System.out.println("Error: " + ex.getMessage());
                }

                return "Transaction completed ($" + amountToSend + " sent to user: " + receiverId + ").";
            } else {
                return "Receiver does not exist.";
            }
        } else {
            return "Not enough balance (available balance: $" + availableBalance + ").";
        }

    }
//endregion
//done
//region "Individual Transfer Details"
    public Transfers transferDetailsByTransferId(int transferId){
        String sqlTransferDetails =
                "SELECT " +
                        " t.transfer_id transfer_id, " +
                        " tt.transfer_type_desc transfer_type_desc, " +
                        " ts.transfer_status_desc transfer_status_desc, " +
                        " u.username sent_by, " +
                        " u2.username sent_to, " +
                        " t.amount from transfers as t" +
                " JOIN transfer_types as tt on t.transfer_type_id = tt.transfer_type_id" +
                " JOIN transfer_statuses as ts on t.transfer_status_id = ts.transfer_status_id" +
                " JOIN accounts a on t.account_from = a.account_id" +
                " JOIN users u on a.user_id = u.user_id" +
                " JOIN accounts a2 on t.account_to = a2.account_id" +
                " JOIN users u2 on a2.user_id = u2.user_id" +
                " WHERE t.transfer_id = ?";

        //select alias with .executeQuery()

        Transfers tsf = new Transfers();

        try{
            con = jdbcTemplate.getDataSource().getConnection();
            PreparedStatement psmt = con.prepareStatement(sqlTransferDetails);
            psmt.setInt(1, transferId);

            ResultSet rst = psmt.executeQuery();

            while (rst.next()){
                tsf.setTransferId(rst.getInt("transfer_id"));
                tsf.setTransferStatusDesc(rst.getString("transfer_status_desc"));
                tsf.setTransferTypeDesc(rst.getString("transfer_type_desc"));
                tsf.setAccountFromName(rst.getString("sent_by"));
                tsf.setAccountToName(rst.getString("sent_to"));
                tsf.setAmount(rst.getBigDecimal("amount"));
            }

        } catch (Exception ex){
            System.out.println(ex.getMessage());
        }


        return tsf;
    }


//endregion
//done
//region "Sender's List"
    @Override
    public List<SentToRequestFrom> findAllPossibleSenders(int loggedInUserId) {
        String sqlAllSenders = "SELECT user_id, username FROM users WHERE user_id != ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlAllSenders, loggedInUserId);
        List<SentToRequestFrom> tempSenders = new ArrayList<>();
        while (rs.next()){
            tempSenders.add(mapRowSetToReceiver(rs));
        }
        return tempSenders;
    }

//endregion
//done
//region "Complete Request"
    @Override
    public String completeRequest(int requesterId, int requestFromId, BigDecimal amountRequested, String requestMessage) {
        //check if sender exists

        String sqlCheckSenderr = "select exists(select 1 from users where user_id = ?) AS exists";
        boolean senderExists = Boolean.TRUE.equals(jdbcTemplate.queryForObject(sqlCheckSenderr, Boolean.class, requestFromId));

        if(senderExists){
            try{
                //update transfers table
                String sqlTransferTypeId = "SELECT transfer_type_id FROM transfer_types WHERE transfer_type_desc = ?;";
                int transferTypeId = jdbcTemplate.queryForObject(sqlTransferTypeId, Integer.class, "Request");
                String sqlTransferStatusId = "SELECT transfer_status_id FROM transfer_statuses WHERE transfer_status_desc = ?;";
                int transferStatusId = jdbcTemplate.queryForObject(sqlTransferStatusId, Integer.class, "Pending");
                //user id -> account id
                String sqlRequesterAccountId = "SELECT account_id FROM accounts WHERE user_id = ?;";
                int requesterAccountId = jdbcTemplate.queryForObject(sqlRequesterAccountId, Integer.class, requesterId);
                String sqlRequestFromAccountId = "SELECT account_id FROM accounts WHERE user_id = ?;";
                int requestFromAccountId = jdbcTemplate.queryForObject(sqlRequestFromAccountId, Integer.class, requestFromId);

                String sqlAddToTransfers = "INSERT INTO transfers (transfer_type_id, transfer_status_id, account_from, account_to, amount) VALUES (?, ?, ?, ?, ?);";

                jdbcTemplate.update(sqlAddToTransfers, transferTypeId, transferStatusId, requestFromAccountId, requesterAccountId, amountRequested);
            } catch (Exception ex){
                System.out.println("Error: " + ex.getMessage());
            }

            return  "\nRequest sent. " +
                    "Pending request (Requested amount:$" + amountRequested + ", Requested from user: " + requestFromId + ").\n" +
                    "[Request message: \"" + requestMessage + "\"]";
            //requestMessage does not get saved but it's there just for fun
        } else {
            return "Sender does not exist.";
        }

    }

//endregion

//region "Transfers History: pending"
    @Override
    public List<TransfersHistory> pendingTransfersHistoryByUserId(int userId) {
        String sqlTransferHistoryByUserId =
                "SELECT t.transfer_id transfer_id, u.username to_from, t.amount amount FROM transfers AS t" +
                        " JOIN accounts AS a ON t.account_to = a.account_id" +
                        " JOIN users AS u ON a.user_id = u.user_id" +
                        " WHERE t.account_from = (SELECT account_id FROM accounts WHERE user_id = ?) AND t.transfer_status_id = 1" +
                        " ORDER BY transfer_id ASC;";
        //transfer_status_id = 1 => pending transfers (view in pending transfers)

        List<TransfersHistory> pendingTransfersHistoryList = new ArrayList<>();

        try{
            con = jdbcTemplate.getDataSource().getConnection();
            PreparedStatement pstmt = con.prepareStatement(sqlTransferHistoryByUserId);
            pstmt.setInt(1, userId);

            //.queryForRowSet() does not work with alias field
            ResultSet resultset = pstmt.executeQuery();    //.execute() => only runs code => boolean

            while (resultset.next()){
                TransfersHistory transfershistory = new TransfersHistory();

                transfershistory.setTransferId(resultset.getInt("transfer_id"));
                transfershistory.setToFrom(resultset.getString("to_from"));
                transfershistory.setAmount(resultset.getBigDecimal("amount"));

                pendingTransfersHistoryList.add(transfershistory);
            }
        } catch (Exception e){
            System.out.println("No connection");
        }

        return pendingTransfersHistoryList;
    }
//endregion







    @Override
    public String pendingApprove(int transferId) {
        //.queryForObject() cannot handle subquery
        //too many preparedstatement and .executequery is annoying

        String sqlAmountToApprove =
                "SELECT amount FROM transfers WHERE transfer_id = ?";
        double amountToApprove = jdbcTemplate.queryForObject(sqlAmountToApprove, Double.class, transferId);

        String sqlSenderAccountId = "SELECT account_from FROM transfers WHERE transfer_id = ?;";
        int senderAccountId = jdbcTemplate.queryForObject(sqlSenderAccountId, Integer.class, transferId);
        String sqlReceiverAccountId = "SELECT account_to FROM transfers WHERE transfer_id = ?;";
        int receiverAccountId = jdbcTemplate.queryForObject(sqlReceiverAccountId, Integer.class, transferId);

        //check for sufficient balance
        String sqlCurrentBalanceSender =
                "SELECT balance FROM accounts WHERE account_id = ?;";
        double currentBalanceSender = jdbcTemplate.queryForObject(sqlCurrentBalanceSender, Double.class, senderAccountId);

        if(currentBalanceSender - amountToApprove >= 0){
            //approve
            String sqlSubtractFrom =
                    "UPDATE accounts SET balance = balance - ?" +
                            " WHERE account_id = ?;";
            jdbcTemplate.update(sqlSubtractFrom, amountToApprove, senderAccountId);
            String sqlAddTo =
                    "UPDATE accounts SET balance = balance + ?" +
                            " WHERE account_id = ?;";
            jdbcTemplate.update(sqlAddTo, amountToApprove, receiverAccountId);
            String sqlUpdateTransfers = "UPDATE transfers SET transfer_status_id = ? WHERE transfer_id = ?";
            jdbcTemplate.update(sqlUpdateTransfers, 2, transferId);

            //
            String sqlRequester =
                    "SELECT username FROM users u" +
                    " JOIN accounts a on u.user_id = a.user_id" +
                    " WHERE account_id = ?";
            String requester = jdbcTemplate.queryForObject(sqlRequester, String.class, receiverAccountId);

            return "Success ($" + amountToApprove + " was sent to " + requester + ")";
        } else {
            return "Error: Insufficient balance (available balance: $" + currentBalanceSender + ", requested balance: $" + amountToApprove +")";
        }
    }

    @Override
    public String pendingReject(int transferId) {
        String sqlUpdateTransfers = "UPDATE transfers SET transfer_status_id = ? WHERE transfer_id = ?";
        jdbcTemplate.update(sqlUpdateTransfers, 3, transferId);
        return "Transfer rejected.";
    }





//rows mapper
    private SentToRequestFrom mapRowSetToReceiver(SqlRowSet rowset) {
        SentToRequestFrom tempReceiver = new SentToRequestFrom();
        tempReceiver.setUserId(rowset.getInt("user_id"));
        tempReceiver.setUserName(rowset.getString("username"));

        return tempReceiver;
    }

}
