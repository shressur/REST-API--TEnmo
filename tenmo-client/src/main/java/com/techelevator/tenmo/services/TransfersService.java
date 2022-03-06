package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.ReceiversSenders;
import com.techelevator.tenmo.model.TransferDetails;
import com.techelevator.tenmo.model.TransfersHistory;
import de.vandermeer.asciitable.AsciiTable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.sql.Array;
import java.util.*;

public class TransfersService {
    String sendto = "api/sendto";
    String send_money = "api/send";

    String transfers_history_completed = "api/transfercompleted";
    String transfers_history_pending = "api/transferpending";
    String transfer_details = "api/transfer/details";

    String requestFrom = "api/requestfrom";
    String request_money = "api/request";

    Scanner scanner = new Scanner(System.in);
    private RestTemplate restTemplate = new RestTemplate();


    //list receivers - send money
    public String sendTeBucks(String base_url, AuthenticatedUser authenticatedUser){
        //show the users in db who can receive the transfer
        HttpHeaders httpHeadersReceivers = setupAuth(authenticatedUser);
        HttpEntity<?> allReceiversEntity = new HttpEntity<>(httpHeadersReceivers);
        //calling account controller
        ResponseEntity<ReceiversSenders[]> allUsersResponse = restTemplate.exchange(base_url + sendto, HttpMethod.GET, allReceiversEntity, ReceiversSenders[].class);
        //display the users
        System.out.printf("User Id\t\t|\t\tUser Name\n");
        System.out.println("-".repeat(35));


        if (allUsersResponse.getBody() == null) System.out.println("No receiver(s) found");
        for(ReceiversSenders receiver : allUsersResponse.getBody()){
            System.out.printf(receiver.getUserId() + "\t\t|\t\t" + receiver.getUserName() + "\n");
        }
        System.out.println();
        //
        String amount = "", receiverId = "";

        System.out.print("Enter ID of user you are sending to (0 to cancel): ");
        while (true){
            receiverId = scanner.nextLine();
            try{
                int inputReceiverId = Integer.parseInt(receiverId);
                if(inputReceiverId == 0){
                    //System.out.println("Canceled.");
                    break;
                } else if(inputReceiverId == authenticatedUser.getUser().getId()){
                    System.out.println("You cannot send Te Bucks to yourself.");
                    break;
                }
            } catch (NumberFormatException e){
                receiverId = "";
                //System.out.println("Invalid number.");
            }
            System.out.print("TE bucks to send (0 to cancel): $");
            amount = scanner.nextLine();
            try{
                double inputAmount = Double.parseDouble(amount);
                if(inputAmount == 0){
                    //System.out.println("Canceled.");
                    break;
                }
            } catch (NumberFormatException e){
                amount = "";
                //System.out.println("Invalid number.");
                break;
            }
            break;
        }

        if(!amount.equals("") && !receiverId.equals("")){
            HttpHeaders httpHeadersSend = setupAuth(authenticatedUser);
            String senderId = authenticatedUser.getUser().getId().toString();
            String[] collectTransferInfo = {senderId, receiverId, amount};  //will be sent as String so mixed type does not matter
            //System.out.println(Arrays.toString(collectTransferInfo));

            //          Array                                       Array
            HttpEntity<String[]> transferEntity = new HttpEntity<>(collectTransferInfo, httpHeadersSend);   //of unknown type
            //                                                      (body,              header)

            //client http method and server http method must match
            //transfer controller
            ResponseEntity<String> response = restTemplate.exchange(base_url+send_money, HttpMethod.POST, transferEntity, String.class);

            return response.getBody();
        } else {
            return "Canceled or invalid input!";
        }

    }

    //list senders - request money
    public String requestTeBucks(String base_url, AuthenticatedUser user){
        //show the users in db who can receive the transfer
        HttpHeaders httpHeadersReceivers = setupAuth(user);
        HttpEntity<?> allRequestFromEntity = new HttpEntity<>(httpHeadersReceivers);
        //calling account controller
        ResponseEntity<ReceiversSenders[]> allUsersResponse = restTemplate.exchange(base_url + requestFrom, HttpMethod.GET, allRequestFromEntity, ReceiversSenders[].class);
        
        //display the users
        System.out.printf("User Id\t\t|\t\tUser Name\n");
        System.out.println("-".repeat(35));

        if (allUsersResponse.getBody() == null) System.out.println("Unable to complete the request. No one is willing to lend you money :(");
        for(ReceiversSenders requestFrom : allUsersResponse.getBody()){
            System.out.printf(requestFrom.getUserId() + "\t\t|\t\t" + requestFrom.getUserName() + "\n");
        }
        System.out.println();
        String amount = "", requestFromId = "", requestMessage = "";
        System.out.print("Enter ID of user you are requesting from (0 to cancel): ");
        while (true){
            requestFromId = scanner.nextLine();
            try{
                int inputRequestFromId = Integer.parseInt(requestFromId);
                if(inputRequestFromId == 0){
                    //System.out.println("Canceled.");
                    break;
                } else if(inputRequestFromId == user.getUser().getId()){
                    System.out.println("Requesting from yourself? Are you nuts?");
                    break;
                }
            } catch (NumberFormatException e){
                requestFromId = "";
            }
            System.out.print("Enter request amount (0 to cancel): $");
            amount = scanner.nextLine();
            try{
                double inputAmount = Double.parseDouble(amount);
                if(inputAmount == 0){
                    break;
                }
            } catch (NumberFormatException e){
                amount = "";
                break;
            }
            System.out.print("Enter request message: ");
            requestMessage = scanner.nextLine();
            break;
        }

        if(!amount.equals("") && !requestFromId.equals("")){
            HttpHeaders httpHeadersRequest = setupAuth(user);
            String requesterId = user.getUser().getId().toString();
            String[] collectTransferInfo = {requesterId, requestFromId, amount, requestMessage};  //will be sent as String so mixed type does not matter

            HttpEntity<String[]> transferEntity = new HttpEntity<>(collectTransferInfo, httpHeadersRequest);   //of unknown type

            //transfer controller
            ResponseEntity<String> response = restTemplate.exchange(base_url+request_money, HttpMethod.POST, transferEntity, String.class);

            return response.getBody();
        } else {
            return "Canceled or invalid input!";
        }
    }

    //list transfer history: approved only
    public void completedTransfersHistory(String base_url, AuthenticatedUser authenticatedUser){
        HttpHeaders headers = setupAuth(authenticatedUser);
        HttpEntity<?> entity = new HttpEntity<>(headers);   //of unknown type
        //HttpEntity<Void> entity = new HttpEntity<>(httpHeaders);

        ResponseEntity<List<TransfersHistory>>
                //res =  restTemplate.exchange(base_url + transfers_history, HttpMethod.POST, entity, new ParameterizedTypeReference<List<TransfersHistory>>() {});
                res =  restTemplate.exchange(base_url + transfers_history_completed, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {});

        //presentation
        System.out.printf("Transfer history for %s (user id: %d):\n", authenticatedUser.getUser().getUsername(), authenticatedUser.getUser().getId());
        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow("*** TRANSFER ID ***", "*** FROM/TO ***", "*** AMOUNT ***");
        at.addRule();
        //for(TransfersHistory th: transfers) {
        for(TransfersHistory th: res.getBody()) {
            at.addRow(th.getTransferId(), th.getToFrom(), th.getAmount());
            at.addRule();
        }
        System.out.println(at.render());
        System.out.println("(Excludes PENDING transactions.\nChoose \"Main menu > Option 5): View your pending requests\" to view pending transactions)");

    }

    //list transfer history: pending only - approve/reject pending
    public void pendingTransfersHistory(String base_url, AuthenticatedUser authenticatedUser){
        List<Integer> penidngTransferIds = new ArrayList<>();
        Boolean isPendingTransfersAvailable = true;

        HttpHeaders headers = setupAuth(authenticatedUser);
        HttpEntity<?> entity = new HttpEntity<>(headers);   //of unknown type
        //HttpEntity<Void> entity = new HttpEntity<>(httpHeaders);

        ResponseEntity<List<TransfersHistory>>
                //res =  restTemplate.exchange(base_url + transfers_history, HttpMethod.POST, entity, new ParameterizedTypeReference<List<TransfersHistory>>() {});
                res =  restTemplate.exchange(base_url + transfers_history_pending, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {});

        //presentation
        //"%1$s\n" +      //1$ => use 1st argument
        System.out.printf("Pending transfers for %s (user id: %d):\n", authenticatedUser.getUser().getUsername(), authenticatedUser.getUser().getId());
        //
        if(res.getBody().size() == 0){
            isPendingTransfersAvailable = false;
        }
        AsciiTable aTable = new AsciiTable();
        aTable.addRule();
        aTable.addRow("*** TRANSFER ID ***", "*** TO ***", "*** AMOUNT ***");
        aTable.addRule();
        for (TransfersHistory ts : res.getBody()){
            aTable.addRow(ts.getTransferId(), ts.getToFrom(), ts.getAmount());
            aTable.addRule();

            penidngTransferIds.add(ts.getTransferId()); //use in api call check
        }
        System.out.println(aTable.render());
        System.out.println("(Excludes APPROVED transactions.\nChoose \"Main menu > Option 3): View your past transfers\" to view approved transactions)");

        //approve/reject request
        if(!isPendingTransfersAvailable){
            System.out.println("[ No pending transfers found ]");
        } else {
            System.out.print("Please enter transfer ID to approve/reject (0 to cancel): ");
            String transferIdString = "";
            int transferId = 0;
            String optionString = "";
            int optionInt = 0;
            while (true){
                transferIdString = scanner.nextLine();
                try{
                    transferId = Integer.parseInt(transferIdString);
                    if(transferId == 0 || !penidngTransferIds.contains(transferId)){ //check here or check in db. check in db if too many users
                        transferIdString = "";
                        break;
                    }
                }catch (NumberFormatException ex){
                    transferIdString = "";
                    break;
                }
                System.out.printf("%s\n%s\n%s\n%s\n",
                        "1: Approve",       //subtract from from_account and add to to_account
                        "2: Reject",        //change status_id = 3
                        "0: Don't approve or reject", //skip -> break
                        "-".repeat(20));
                System.out.print("Please choose an option: ");

                optionString = scanner.nextLine();
                try {
                    optionInt = Integer.parseInt(optionString);
                    if(optionInt == 0 || optionInt >2){
                        //optionString ="";
                        break;
                    }
                } catch (NumberFormatException ex){
                    //System.out.println(ex.getMessage());
                    optionString = "";
                    break;
                }
                break;
            }

            //api call
            if(!transferIdString.trim().equals("") && optionInt < 3){

                //just sending transfer id with "required action" is enough
                String[] apiBody = {};
                HttpHeaders headersAP = setupAuth(authenticatedUser);
                if(optionInt ==1){
                    apiBody = new String[]{"approve", transferIdString};
                } else if(optionInt == 2){
                    apiBody = new String[]{"reject", transferIdString};
                }
                HttpEntity<?> entityAP = new HttpEntity<>(apiBody, headersAP);

                ResponseEntity<String> resAP = restTemplate.exchange(base_url+"api/approvereject", HttpMethod.POST, entityAP, String.class);

                System.out.println(resAP.getBody());
            } else {
                System.out.println("Canceled or invalid input.");
            }
        }


    }

    //single transfer detail: approved only -> view pending to view pending transactions
    public void transferDetailsByTransferId(String base_url, AuthenticatedUser user){//, int transferId){
        HttpHeaders headers = setupAuth(user);

        System.out.print("Enter transfer id to view details (0 to cancel): ");
        String transferIdString = "";
        int transferIdEntered = 0;
        while (true){
            transferIdString = scanner.nextLine();
            try {
                transferIdEntered = Integer.parseInt(transferIdString);
                if(transferIdEntered == 0 || transferIdString.trim().isEmpty()){
                    break;
                }
            } catch (NumberFormatException ex){
                //System.out.println(ex.getMessage());
                transferIdString = "";
                break;
            }
        }

        if(transferIdEntered > 0){
            HttpEntity<?> e = new HttpEntity<>(transferIdEntered, headers);
            ResponseEntity<TransferDetails> res = restTemplate.exchange(base_url + transfer_details, HttpMethod.POST, e,  TransferDetails.class);

            TransferDetails  tds = res.getBody();

            String myUserName = user.getUser().getUsername();
            String sentBy = "";
            String sentTo = "";

            assert tds != null;
            String isNullCheck = tds.getAccountFromName();  //no need to check all
            if (isNullCheck != null){
                if(tds.getAccountFromName().equals(myUserName)) {
                    sentBy = tds.getAccountFromName() + "(Me)";
                }else {
                    sentBy = tds.getAccountFromName();
                }
                if(tds.getAccountToName().equals(myUserName)) {
                    sentTo = tds.getAccountToName() + "(Me)";
                }else {
                    sentTo = tds.getAccountToName();
                }
                //
                System.out.printf("%s\n%s\n%s\n", "-".repeat(35), "Transfer Details", "-".repeat(35));
                System.out.printf("Id: %d\nFrom: %s\nTo: %s\nType: %s\nStatus: %s\nAmount: $%.2f\n",
                        tds.getTransferId(), sentBy, sentTo, tds.getTransferTypeDesc(), tds.getTransferStatusDesc(), tds.getAmount());
            } else {
                System.out.println("No transfer details found for Transfer Id: " + transferIdEntered);
            }
        } else {
            System.out.println("Canceled or invalid input.");
        }
    }








    //setup auth and header
    private HttpHeaders setupAuth(AuthenticatedUser authenticatedUser){
        String auth  =  authenticatedUser.getToken();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setBearerAuth(auth);
        return httpHeaders;
    }

}
