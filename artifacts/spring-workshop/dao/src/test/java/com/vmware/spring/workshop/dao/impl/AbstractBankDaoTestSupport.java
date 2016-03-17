package com.vmware.spring.workshop.dao.impl;

import java.util.List;

import javax.inject.Inject;

import org.junit.Test;

import com.vmware.spring.workshop.dao.AbstractDaoTestSupport;
import com.vmware.spring.workshop.dao.api.BankDao;
import com.vmware.spring.workshop.dao.finder.IdentifiedInstanceFinder;
import com.vmware.spring.workshop.dao.finder.IdentifiedValuesListFinder;
import com.vmware.spring.workshop.model.banking.Bank;

/**
 * @author lgoldstein
 */
public abstract class AbstractBankDaoTestSupport extends AbstractDaoTestSupport {
    @Inject protected BankDao    _daoBank;

    protected AbstractBankDaoTestSupport() {
        super();
    }

    @Test
    public void testFindById () {
        runIdentifiedByIdFinder(_daoBank);
    }

    @Test
    public void testFindByBankName () {
        runIdentifiedInstanceFinderTest(_daoBank,
                new IdentifiedInstanceFinder<Bank,BankDao>() {
                    @Override
                    public Bank findInstance(BankDao dao, Bank sourceInstance) {
                        return dao.findBankByName(sourceInstance.getName());
                    }

            });
    }

    @Test
    public void testFindByBankCode () {
        runIdentifiedInstanceFinderTest(_daoBank,
                new IdentifiedInstanceFinder<Bank,BankDao>() {
                    @Override
                    public Bank findInstance(BankDao dao, Bank sourceInstance) {
                        return dao.findBankByBankCode(sourceInstance.getBankCode());
                    }
            });
    }

    @Test
    public void testFindByBankLocation () {
        runLocatedInstanceFinderTest(_daoBank,
                new IdentifiedValuesListFinder<Bank,BankDao,String>() {
                    @Override
                    public List<Bank> findMatches(BankDao dao, String arg) {
                        return dao.findByBankLocation(arg);
                    }
                });
    }

}
