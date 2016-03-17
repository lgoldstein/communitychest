package com.vmware.spring.workshop.dao.impl.hibernate;

import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.vmware.spring.workshop.dao.api.BranchDao;
import com.vmware.spring.workshop.model.banking.Branch;

/**
 * @author lgoldstein
 */
@Repository("branchDao")
@Transactional
public class BranchDaoImpl
        extends AbstractIdentifiedHibernateDaoImpl<Branch>
        implements BranchDao {
    public BranchDaoImpl ()
    {
        super(Branch.class);
    }

    @Override
    @Transactional(readOnly=true)
    public Branch findByBranchCode(int code) {
        Assert.isTrue(code > 0, "Non positive code N/A");
        final Query    query=getNamedQuery("findByBranchCode")
                 .setParameter("code", Integer.valueOf(code))
                 ;
        return getDefaultUniqueResult(query);
    }

    @Override
    @Transactional(readOnly=true)
    public Branch findByBranchName(String name) {
        Assert.hasText(name, "No bank name provided");
        final Query    query=getNamedQuery("findByBranchName")
                         .setParameter("name", name)
                         ;
        return getDefaultUniqueResult(query);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Branch> findByBankId(Long bankId) {
        return getNamedIdDefaultQueryResults("findByBankId", bankId);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Branch> findByBranchLocation(String location) {
        Assert.hasText(location, "No location provided");
        final Query    query=getNamedQuery("findByBranchLocation")
                 .setParameter("location", "%" + location.toLowerCase() + "%")
                 ;
        return getDefaultQueryResults(query);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Branch> findByBranchBankCode(int bankCode) {
        Assert.isTrue(bankCode > 0, "Non-positive bank code N/A");
        final Query    query=getNamedQuery("findByBranchBankCode")
                 .setParameter("code", Integer.valueOf(bankCode))
                 ;
        return getDefaultQueryResults(query);
    }
}
