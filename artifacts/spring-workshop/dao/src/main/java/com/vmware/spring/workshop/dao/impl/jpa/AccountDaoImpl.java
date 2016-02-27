package com.vmware.spring.workshop.dao.impl.jpa;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.vmware.spring.workshop.dao.api.AccountDao;
import com.vmware.spring.workshop.model.banking.Account;
import com.vmware.spring.workshop.model.user.InvestmentData;

/**
 * @author lgoldstein
 */
@Repository("accountDao")
@Transactional
public class AccountDaoImpl
		extends AbstractIdentifiedJpaDaoImpl<Account>
		implements AccountDao {
	public AccountDaoImpl () {
		super(Account.class);
	}

	@Override
	@Transactional(readOnly=true)
	public List<Account> findUserAccountsById(Long userId) {
		return getNamedIdQueryResults("findUserAccountsById", userId);
	}

	@Override
	@Transactional(readOnly=true)
	public List<Account> findBranchAccountsById(Long branchId) {
		return getNamedIdQueryResults("findBranchAccountsById", branchId);
	}

	@Override
	@Transactional(readOnly=true)
	public List<Account> findBankAccountsById(Long bankId) {
		return getNamedIdQueryResults("findBankAccountsById", bankId);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	@Transactional(readOnly=true)
	public List<Map.Entry<Long, Integer>> findInvestedAmountsByBank(Long userId) {
		return (List) getNamedIdQueryResults("findInvestedAmountsByBank", userId, InvestmentData.class);
	}
}
