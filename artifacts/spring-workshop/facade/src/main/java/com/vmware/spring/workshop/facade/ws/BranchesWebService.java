package com.vmware.spring.workshop.facade.ws;

import javax.inject.Inject;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.vmware.spring.workshop.facade.ws.messages.AbstractSOAPMessage;
import com.vmware.spring.workshop.facade.ws.messages.BankBranchesRequest;
import com.vmware.spring.workshop.facade.ws.messages.BankBranchesResponse;
import com.vmware.spring.workshop.services.facade.BranchesFacade;

/**
 * @author lgoldstein
 */
@Endpoint("branchesWebService")
public class BranchesWebService {
    private final BranchesFacade    _facade;
    @Inject
    public BranchesWebService(final BranchesFacade facade) {
        _facade = facade;
    }

    @ResponsePayload
    @PayloadRoot(localPart="queryBankBranches", namespace=AbstractSOAPMessage.SOAP_NAMESPACE)
    public BankBranchesResponse queryBankBranches (@RequestPayload final BankBranchesRequest request) {
        return new BankBranchesResponse(_facade.findAllBranchesById(request.getBankId()));
    }
}
