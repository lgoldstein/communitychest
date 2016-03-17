package com.vmware.spring.workshop.facade.ws.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author lgoldstein
 */
// NOTE: for some reason the name must match the name of the operation rather than the argument...
@XmlRootElement(name="queryBankBranches", namespace=AbstractSOAPMessage.SOAP_NAMESPACE)
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class BankBranchesRequest extends AbstractSOAPMessage {
    private static final long serialVersionUID = -642056428821402726L;
    private Long    _bankId;
    public BankBranchesRequest() {
        super();
    }

    @XmlElement(name="bankId", namespace=AbstractSOAPMessage.SOAP_NAMESPACE)
    public Long getBankId() {
        return _bankId;
    }

    public void setBankId(Long bankId) {
        _bankId = bankId;
    }
}
