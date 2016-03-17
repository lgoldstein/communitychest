package com.vmware.spring.workshop.services.facade;

import java.util.List;

import com.vmware.spring.workshop.dto.banking.BankDTO;
import com.vmware.spring.workshop.dto.banking.BranchDTO;

/**
 * @author lgoldstein
 */
public interface BranchesFacade extends CommonFacadeActions<BranchDTO> {
    BranchDTO findByBranchCode (int branchCode);

    List<BranchDTO> findAllBranches (BankDTO bank);
    List<BranchDTO> findAllBranchesById (Long bankId);
    List<BranchDTO> findByBranchBankCode (int bankCode);

}
