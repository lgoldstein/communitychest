package com.vmware.spring.workshop.dao.impl.jpa;

import java.util.List;

import javax.persistence.TypedQuery;

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
		extends AbstractIdentifiedJpaDaoImpl<Bank>
		implements BankDao {
	public BankDaoImpl () {
		super(Bank.class);
	}

	@Override
	@Transactional(readOnly=true)
	public Bank findBankByName(String name) {
		Assert.hasText(name, "No bank name provided");
		final TypedQuery<Bank>	query=getNamedQuery("findBankByName")
						 				.setParameter("name", name)
						 				;
		return getUniqueResult(query);
	}

	@Override
	@Transactional(readOnly=true)
	public Bank findBankByBankCode(int code) {
		Assert.isTrue(code > 0, "Non positive code N/A");
		final TypedQuery<Bank>	query=getNamedQuery("findByBankCode")
										 .setParameter("code", Integer.valueOf(code))
										 ;
		return getUniqueResult(query);
	}

	@Override
	@Transactional(readOnly=true)
	public List<Bank> findByBankLocation(String location) {
		Assert.hasText(location, "No location provided");
		final TypedQuery<Bank>	query=getNamedQuery("findByBankLocation")
				 						.setParameter("location", "%" + location.toLowerCase() + "%")
				 						;
		return getQueryResults(query);
	}

}
