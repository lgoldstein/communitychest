package com.vmware.spring.workshop.services.facade.impl;

import java.util.List;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.vmware.spring.workshop.dao.api.BranchDao;
import com.vmware.spring.workshop.dto.banking.BankDTO;
import com.vmware.spring.workshop.dto.banking.BranchDTO;
import com.vmware.spring.workshop.model.banking.Branch;
import com.vmware.spring.workshop.services.convert.BranchDTOConverter;
import com.vmware.spring.workshop.services.facade.BranchesFacade;
import com.vmware.spring.workshop.services.facade.Facade;

/**
 * @author lgoldstein
 */
@Facade("branchesFacade")
@Transactional
public class BranchesFacadeImpl
        extends AbstractCommonFacadeActions<Branch,BranchDTO,BranchDao,BranchDTOConverter>
        implements BranchesFacade {

    @Inject
    public BranchesFacadeImpl(final BranchDao            daoBranch,
                                 final BranchDTOConverter    brhConverter) {
        super(BranchDTO.class, Branch.class, daoBranch, brhConverter);
    }

    @Override
    @Transactional(readOnly=true)
    public BranchDTO findByBranchCode(int branchCode) {
        return _converter.toDTO(_dao.findByBranchCode(branchCode));
    }

    @Override
    @Transactional(readOnly=true)
    public List<BranchDTO> findAllBranches(BankDTO bank) {
        Assert.notNull(bank, "No bank specified");
        return findAllBranchesById(bank.getId());
    }

    @Override
    @Transactional(readOnly=true)
    public List<BranchDTO> findAllBranchesById(Long bankId) {
        return _converter.toDTO(_dao.findByBankId(bankId));
    }

    @Override
    @Transactional(readOnly=true)
    public List<BranchDTO> findByBranchBankCode(int bankCode) {
        return _converter.toDTO(_dao.findByBranchBankCode(bankCode));
    }
}
