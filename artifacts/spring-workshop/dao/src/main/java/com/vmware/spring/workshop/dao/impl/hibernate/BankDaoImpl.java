package com.vmware.spring.workshop.dao.impl.hibernate;

import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.vmware.spring.workshop.dao.api.BankDao;
import com.vmware.spring.workshop.model.banking.Bank;

/**
 * @author lgoldstein
 */
@Repository("bankDao")
@Transactional
public class BankDaoImpl
        extends AbstractIdentifiedHibernateDaoImpl<Bank>
        implements BankDao {
    public BankDaoImpl () {
        super(Bank.class);
    }

    @Override
    @Transactional(readOnly=true)
    public Bank findBankByName(String name) {
        Assert.hasText(name, "No bank name provided");
        final Query    query=getNamedQuery("findBankByName")
                         .setParameter("name", name)
                         ;
        return getDefaultUniqueResult(query);
    }

    @Override
    @Transactional(readOnly=true)
    public Bank findBankByBankCode(int code) {
        Assert.isTrue(code > 0, "Non positive code N/A");
        final Query    query=getNamedQuery("findByBankCode")
                 .setParameter("code", Integer.valueOf(code))
                 ;
        return getDefaultUniqueResult(query);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Bank> findByBankLocation(String location) {
        Assert.hasText(location, "No location provided");
        final Query    query=getNamedQuery("findByBankLocation")
                 .setParameter("location", "%" + location.toLowerCase() + "%")
                 ;
        return getDefaultQueryResults(query);
    }
}
