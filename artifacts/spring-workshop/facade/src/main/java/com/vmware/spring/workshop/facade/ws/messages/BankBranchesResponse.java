package com.vmware.spring.workshop.facade.ws.messages;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.util.CollectionUtils;

import com.vmware.spring.workshop.dto.banking.BranchDTO;

/**
 * @author lgoldstein
 */
@XmlRootElement(name="bankBranchesResponse", namespace=AbstractSOAPMessage.SOAP_NAMESPACE)
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class BankBranchesResponse extends AbstractSOAPMessage {
    private static final long serialVersionUID = -2327107428098228930L;

    public static final BranchDTO[]    EMPTY_ARRAY_RESULT={ };
    private BranchDTO[]    _branches;
    public BankBranchesResponse() {
        this(EMPTY_ARRAY_RESULT);
    }

    public BankBranchesResponse(Collection<? extends BranchDTO> branches) {
        this(CollectionUtils.isEmpty(branches) ? EMPTY_ARRAY_RESULT : branches.toArray(new BranchDTO[branches.size()]));
    }

    public BankBranchesResponse(BranchDTO ... branches) {
        _branches = branches;
    }

    @XmlElement(name="branches", namespace=AbstractSOAPMessage.SOAP_NAMESPACE)
    public BranchDTO[] getBranches() {
        return _branches;
    }

    public void setBranches(BranchDTO[] branches) {
        _branches = branches;
    }

}
