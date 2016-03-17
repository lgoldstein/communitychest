package com.vmware.spring.workshop.facade.ws.messages;

import javax.xml.bind.annotation.XmlRegistry;

/**
 * @author lgoldstein
 */
@XmlRegistry
public final class ObjectFactory {
    public ObjectFactory() {
        super();
    }

    public BankBranchesRequest createBankBranchesRequest () {
        return new BankBranchesRequest();
    }

    public BankBranchesResponse createBankBranchesResponse () {
        return new BankBranchesResponse();
    }
}
