package com.vmware.spring.workshop.facade.beans;

import com.vmware.spring.workshop.dto.banking.BankDTO;
import com.vmware.spring.workshop.dto.banking.BranchDTO;

/**
 * @author lgoldstein
 */
public class BranchBean extends AbstractDTOBean<BranchDTO> {
    private BankDTO    _bankValue;
    public BranchBean() {
        super();
    }

    public BranchBean(BranchDTO dtoValue) {
        super(dtoValue);
    }

    public BranchBean(BankDTO bank, BranchDTO dtoValue) {
        this(dtoValue);
        _bankValue = bank;
    }

    public BankDTO getBankValue() {
        return _bankValue;
    }

    public void setBankValue(BankDTO bankValue) {
        _bankValue = bankValue;
    }
}
