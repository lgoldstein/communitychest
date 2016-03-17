package com.vmware.spring.workshop.dao.impl;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

import com.vmware.spring.workshop.dao.AbstractDaoTestSupport;
import com.vmware.spring.workshop.dao.api.BankDao;
import com.vmware.spring.workshop.dao.api.BranchDao;
import com.vmware.spring.workshop.dao.finder.IdentifiedInstanceFinder;
import com.vmware.spring.workshop.dao.finder.IdentifiedValuesListFinder;
import com.vmware.spring.workshop.model.banking.Bank;
import com.vmware.spring.workshop.model.banking.Branch;

/**
 * @author lgoldstein
 */
public abstract class AbstractBranchDaoTestSupport
            extends AbstractDaoTestSupport {
    @Inject protected BankDao    _daoBank;
    @Inject protected BranchDao    _daoBranch;

    protected AbstractBranchDaoTestSupport() {
        super();
    }

    @Test
    public void testFindById () {
        runIdentifiedByIdFinder(_daoBranch);
    }

    @Test
    public void testFindByBranchName () {
        runIdentifiedInstanceFinderTest(_daoBranch,
                new IdentifiedInstanceFinder<Branch,BranchDao>() {
                    @Override
                    public Branch findInstance(BranchDao dao, Branch sourceInstance) {
                        return dao.findByBranchName(sourceInstance.getName());
                    }

            });
    }

    @Test
    public void testFindByBranchCode () {
        runIdentifiedInstanceFinderTest(_daoBranch,
                new IdentifiedInstanceFinder<Branch,BranchDao>() {
                    @Override
                    public Branch findInstance(BranchDao dao, Branch sourceInstance) {
                        return dao.findByBranchCode(sourceInstance.getBranchCode());
                    }
            });
    }

    @Test
    public void testFindByBankId () {
        runBanksResultsTest(new IdentifiedValuesListFinder<Branch,BranchDao,Bank>() {
                @Override
                public List<Branch> findMatches(BranchDao dao, Bank arg) {
                    return dao.findByBankId(arg.getId());
                }
            });
    }

    @Test
    public void testFindByBranchBankCode () {
        runBanksResultsTest(new IdentifiedValuesListFinder<Branch,BranchDao,Bank>() {
            @Override
            public List<Branch> findMatches(BranchDao dao, Bank arg) {
                return dao.findByBranchBankCode(arg.getBankCode());
            }
        });
    }

    @Test
    public void testFindByBranchLocation () {
        runLocatedInstanceFinderTest(_daoBranch,
                new IdentifiedValuesListFinder<Branch,BranchDao,String>() {
                    @Override
                    public List<Branch> findMatches(BranchDao dao, String arg) {
                        return dao.findByBranchLocation(arg);
                    }
                });
    }

    protected void runBanksResultsTest (final IdentifiedValuesListFinder<Branch,BranchDao,Bank> finder) {
        final Iterable<? extends Bank> banks=_daoBank.findAll();
        Assert.assertNotNull("No current banks available", banks);

        final Iterable<Branch>    branches=_daoBranch.findAll();
        Assert.assertNotNull("No current branches available", branches);

        for (final Bank b : banks) {
            final Collection<Branch>    qryList=finder.findMatches(_daoBranch, b);
            Assert.assertFalse("No branches associated with bank=" + b, CollectionUtils.isEmpty(qryList));

            final Collection<Branch>    clcList=Branch.findByBank(b, branches);
            Assert.assertEquals("Mismatched sizes for " + b, clcList.size(), qryList.size());
            Assert.assertTrue("Missing query results for " + b, qryList.containsAll(clcList));
        }
    }
}
