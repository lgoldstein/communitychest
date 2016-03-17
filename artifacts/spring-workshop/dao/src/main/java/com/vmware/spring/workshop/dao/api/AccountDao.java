package com.vmware.spring.workshop.dao.api;

import java.util.List;
import java.util.Map;

import org.springframework.data.repository.query.Param;

import com.vmware.spring.workshop.dao.IdentifiedCommonOperationsDao;
import com.vmware.spring.workshop.model.Identified;
import com.vmware.spring.workshop.model.banking.Account;

/**
 * @author lgoldstein
 */
public interface AccountDao extends IdentifiedCommonOperationsDao<Account> {
    List<Account> findUserAccountsById (@Param(Identified.ID_COL_NAME) Long userId);
    List<Account> findBranchAccountsById (@Param(Identified.ID_COL_NAME) Long branchId);
    List<Account> findBankAccountsById (@Param(Identified.ID_COL_NAME) Long bankId);
    List<Map.Entry<Long,Integer>> findInvestedAmountsByBank (@Param(Identified.ID_COL_NAME) Long userId);
}
