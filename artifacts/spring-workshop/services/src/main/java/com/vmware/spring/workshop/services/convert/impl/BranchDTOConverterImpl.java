package com.vmware.spring.workshop.services.convert.impl;

import java.lang.reflect.Method;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.vmware.spring.workshop.dao.api.BankDao;
import com.vmware.spring.workshop.dto.banking.BranchDTO;
import com.vmware.spring.workshop.model.banking.Bank;
import com.vmware.spring.workshop.model.banking.Branch;
import com.vmware.spring.workshop.services.convert.BranchDTOConverter;

/**
 * @author lgoldstein
 */
@Component("branchDTOConverter")
@Transactional
public class BranchDTOConverterImpl extends AbstractDTOConverter<Branch,BranchDTO>
        implements BranchDTOConverter, ValueConverter<Long,Bank> {
    private final BankDao _daoBank;

    @Inject
    public BranchDTOConverterImpl (final BankDao daoBank) {
        super(Branch.class, BranchDTO.class);
        Assert.state((_daoBank=daoBank) != null, "No bank DAO provided");
    }

    @Override
    protected ValueConverter<?, ?> resolveUnknownPropertyValueConverter(
            Class<?> srcClass, Method srcGetter, Method srcSetter,
            Class<?> dstClass, Method dstGetter, Method dstSetter) {
        final Class<?>    dstType=dstGetter.getReturnType();
        if (Bank.class.isAssignableFrom(dstType)) {
            return this;
        }

        return super.resolveUnknownPropertyValueConverter(srcClass, srcGetter, srcSetter,
                                                          dstClass, dstGetter, dstSetter);
    }

    @Override
    @Transactional(readOnly=true)
    public Bank convertValue(Long srcValue) {
        if (srcValue == null)
            return null;
        else
            return _daoBank.findOne(srcValue);
    }
}
