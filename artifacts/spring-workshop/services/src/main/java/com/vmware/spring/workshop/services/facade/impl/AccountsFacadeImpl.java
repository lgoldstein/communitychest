package com.vmware.spring.workshop.services.facade.impl;

import java.util.List;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.vmware.spring.workshop.dao.api.AccountDao;
import com.vmware.spring.workshop.dao.api.BranchDao;
import com.vmware.spring.workshop.dao.api.UserDao;
import com.vmware.spring.workshop.dto.banking.AccountDTO;
import com.vmware.spring.workshop.dto.banking.BankDTO;
import com.vmware.spring.workshop.dto.banking.BranchDTO;
import com.vmware.spring.workshop.dto.user.UserDTO;
import com.vmware.spring.workshop.model.banking.Account;
import com.vmware.spring.workshop.model.banking.Branch;
import com.vmware.spring.workshop.model.user.User;
import com.vmware.spring.workshop.services.convert.AccountDTOConverter;
import com.vmware.spring.workshop.services.facade.AccountsFacade;
import com.vmware.spring.workshop.services.facade.Facade;

/**
 * @author lgoldstein
 */
@Facade("accountsFacade")
@Transactional
public class AccountsFacadeImpl
            extends AbstractCommonFacadeActions<Account,AccountDTO,AccountDao,AccountDTOConverter>
            implements AccountsFacade {
    private final UserDao    _daoUser;
    private final BranchDao    _daoBranch;

    @Inject
    public AccountsFacadeImpl(final AccountDao            daoAccount,
                              final AccountDTOConverter    accConverter,
                              final UserDao                daoUser,
                              final BranchDao            daoBranch) {
        super(AccountDTO.class, Account.class, daoAccount, accConverter);
        _daoUser = daoUser;
        _daoBranch = daoBranch;
    }

    @Override
    @Transactional(readOnly=true)
    public List<AccountDTO> findUserAccounts(UserDTO user) {
        Assert.notNull(user, "No user provided");
        return findUserAccountsById(user.getId());
    }

    @Override
    @Transactional(readOnly=true)
    public List<AccountDTO> findUserAccountsById(Long userId) {
        return _converter.toDTO(_dao.findUserAccountsById(userId));
    }

    @Override
    @Transactional(readOnly=true)
    public List<AccountDTO> findBranchAccounts(BranchDTO branch) {
        Assert.notNull(branch, "No branch provided");
        return findBranchAccountsById(branch.getId());
    }

    @Override
    @Transactional(readOnly=true)
    public List<AccountDTO> findBranchAccountsById(Long branchId) {
        return _converter.toDTO(_dao.findBranchAccountsById(branchId));
    }

    @Override
    @Transactional(readOnly=true)
    public List<AccountDTO> findBankAccounts(BankDTO bank) {
        Assert.notNull(bank, "No bank provided");
        return findBankAccountsById(bank.getId());
    }

    @Override
    @Transactional(readOnly=true)
    public List<AccountDTO> findBankAccountsById(Long bankId) {
        return _converter.toDTO(_dao.findBankAccountsById(bankId));
    }

    @Override
    public void create (AccountDTO dto) {
        Assert.notNull(dto, "No DTO object provided");
        Assert.isNull(dto.getId(), "Pre-assigned ID N/A");

        final User    owner=_daoUser.findOne(dto.getOwnerId());
        Assert.state(owner != null, "Owner not found");

        final Branch    branch=_daoBranch.findOne(dto.getBranchId());
        Assert.state(branch != null, "Branch not found");

        final Account    acc=_converter.fromDTO(dto);
        acc.setOwner(owner);
        acc.setBranch(branch);

        final Long    accountId=createModelInstance(acc);
        Assert.state(accountId != null, "No account ID assigned");
        dto.setId(accountId);
    }
}
