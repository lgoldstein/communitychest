package com.vmware.spring.workshop.services.facade;

import java.util.List;

import com.vmware.spring.workshop.dto.banking.AccountDTO;
import com.vmware.spring.workshop.dto.banking.BankDTO;
import com.vmware.spring.workshop.dto.banking.BranchDTO;
import com.vmware.spring.workshop.dto.user.UserDTO;

/**
 * @author lgoldstein
 */
public interface AccountsFacade {
    List<AccountDTO> findUserAccounts (UserDTO user);
    List<AccountDTO> findUserAccountsById (Long userId);

    List<AccountDTO> findBranchAccounts (BranchDTO branch);
    List<AccountDTO> findBranchAccountsById (Long branchId);

    List<AccountDTO> findBankAccounts (BankDTO bank);
    List<AccountDTO> findBankAccountsById (Long bankId);

}
