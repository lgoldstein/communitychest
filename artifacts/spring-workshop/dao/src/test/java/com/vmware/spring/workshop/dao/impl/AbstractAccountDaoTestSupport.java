package com.vmware.spring.workshop.dao.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

import com.vmware.spring.workshop.dao.AbstractDaoTestSupport;
import com.vmware.spring.workshop.dao.IdentifiedCommonOperationsDao;
import com.vmware.spring.workshop.dao.api.AccountDao;
import com.vmware.spring.workshop.dao.api.BankDao;
import com.vmware.spring.workshop.dao.api.BranchDao;
import com.vmware.spring.workshop.dao.api.UserDao;
import com.vmware.spring.workshop.dao.finder.IdentifiedValuesListFinder;
import com.vmware.spring.workshop.model.Identified;
import com.vmware.spring.workshop.model.banking.Account;
import com.vmware.spring.workshop.model.banking.Bank;
import com.vmware.spring.workshop.model.banking.Branch;
import com.vmware.spring.workshop.model.user.User;

/**
 * @author lgoldstein
 */
public abstract class AbstractAccountDaoTestSupport
		extends AbstractDaoTestSupport {
	@Inject protected BankDao	_daoBank;
	@Inject protected UserDao _daoUser;
	@Inject protected BranchDao	_daoBranch;
	@Inject protected AccountDao _daoAccount;

	protected AbstractAccountDaoTestSupport() {
		super();
	}


	@Test
	public void testFindById () {
		runIdentifiedByIdFinder(_daoAccount);
	}

	@Test
	public void testFindUserAccountsById () {
		final IdentifiedValuesListFinder<Account,AccountDao,User>	finder=
				new IdentifiedValuesListFinder<Account,AccountDao,User>() {
					@Override
					public List<Account> findMatches(AccountDao dao, User arg) {
						return dao.findUserAccountsById(arg.getId());
					}
			};
		final PropertyExtractor<Account,User>	propGetter=
				new PropertyExtractor<Account,User>() {
					@Override
					public User getProperty(Account o) {
						return o.getOwner();
					}
			
			};
		runListFinderTest(finder, _daoUser, propGetter);
	}

	@Test
	public void testFindBankAccountsById () {
		final IdentifiedValuesListFinder<Account,AccountDao,Bank>	finder=
				new IdentifiedValuesListFinder<Account,AccountDao,Bank>() {
					@Override
					public List<Account> findMatches(AccountDao dao, Bank arg) {
						return dao.findBankAccountsById(arg.getId());
					}
			};
		final PropertyExtractor<Account,Bank>	propGetter=
				new PropertyExtractor<Account,Bank>() {
					@Override
					public Bank getProperty(Account o) {
						return o.getBranch().getBank();
					}
			
			};
		runListFinderTest(finder, _daoBank, propGetter);
	}

	@Test
	public void testFindBranchAccountsById () {
		final IdentifiedValuesListFinder<Account,AccountDao,Branch>	finder=
				new IdentifiedValuesListFinder<Account,AccountDao,Branch>() {
					@Override
					public List<Account> findMatches(AccountDao dao, Branch arg) {
						return dao.findBranchAccountsById(arg.getId());
					}
			};
		final PropertyExtractor<Account,Branch>	propGetter=
				new PropertyExtractor<Account,Branch>() {
					@Override
					public Branch getProperty(Account o) {
						return o.getBranch();
					}
			
			};
		runListFinderTest(finder, _daoBranch, propGetter);
	}

	@Test
	public void testFindInvestedAmountsByBank () {
		final Iterable<? extends User>		usrList=_daoUser.findAll();
		Assert.assertNotNull("No current users", usrList);

		final Iterable<? extends Account>	accsList=_daoAccount.findAll();
		Assert.assertNotNull("No current accounts", accsList);
		for (final User user : usrList) {
			final Long														userId=user.getId();
			final Collection<? extends Map.Entry<Long,? extends Number>>	actResult=_daoAccount.findInvestedAmountsByBank(userId),
																			expResult=calculateInvestedAmountsByBank(accsList, userId);
			assertInvestmentResult(user.getLoginName(), expResult, actResult);
		}
	}

	private static final void assertInvestmentResult (final String username,
			final Collection<? extends Map.Entry<Long,? extends Number>>	expResult,
			final Collection<? extends Map.Entry<Long,? extends Number>>	actResult) {
		final Map<Long,Number>	expMap=toInvestmentMap(username + "-expected", expResult),
								actMap=toInvestmentMap(username + "-actual", actResult);
		Assert.assertEquals(username + ": Mismatched expected sizes", expResult.size(), expMap.size());
		Assert.assertEquals(username + ": Mismatched actual sizes", actResult.size(), actMap.size());
		Assert.assertEquals(username + ": Mismatched map sizes", expMap.size(), actMap.size());
		
		for (final Map.Entry<Long,? extends Number> ee : expMap.entrySet()) {
			final Long		id=ee.getKey();
			final Number	expValue=ee.getValue(), actValue=actMap.get(id);
			Assert.assertNotNull(username + ": No value found for ID=" + id, actValue);
			Assert.assertEquals(username + ": Mismatched values for ID=" + id, expValue.intValue(), actValue.intValue());
		}
	}

	private static final Map<Long,Number> toInvestmentMap (final String mapType,
				final Collection<? extends Map.Entry<Long,? extends Number>> result) {
		if (CollectionUtils.isEmpty(result))
			return Collections.emptyMap();

		final Map<Long,Number>	map=new TreeMap<Long, Number>();
		for (final Map.Entry<Long,? extends Number> re : result) {
			final Number	prev=map.put(re.getKey(), re.getValue());
			Assert.assertNull(mapType + ": Multiple mappings for ID=" + re.getKey(), prev);
		}
		return map;
	}

	private static final Set<Map.Entry<Long,MutableInt>> calculateInvestedAmountsByBank (
			final Iterable<? extends Account>	accsList, final Long userId) {
		final Map<Long,MutableInt>	result=new HashMap<Long, MutableInt>();
		for (final Account acc : accsList) {
			final User	user=acc.getOwner();
			if (!userId.equals(user.getId()))
				continue;

			final Branch	branch=acc.getBranch();
			final Bank		bank=branch.getBank();
			final Long		bankId=bank.getId();
			MutableInt		sumValue=result.get(bankId);
			if (sumValue == null) {
				sumValue = new MutableInt(0);
				result.put(bankId, sumValue);
			}
			sumValue.add(acc.getAmount());
		}

		return result.entrySet();
	}

	protected <ARG extends Identified,DAO extends IdentifiedCommonOperationsDao<ARG>> 
		void runListFinderTest (final IdentifiedValuesListFinder<Account,AccountDao,ARG>	finder,
								final DAO													argsDao,
								final PropertyExtractor<Account,ARG>						propGetter) {
		final Iterable<? extends Account>	accsList=_daoAccount.findAll();
		Assert.assertNotNull("No current accounts", accsList);

		final Iterable<? extends ARG>	argsList=argsDao.findAll();
		Assert.assertNotNull("No extraction arguments", argsList);

		for (final ARG argVal : argsList) {
			final Collection<? extends Account>	qryList=finder.findMatches(_daoAccount, argVal);
			final Collection<Account>			clcList=new ArrayList<Account>();

			for (final Account clcVal : accsList) {
				final ARG	propVal=propGetter.getProperty(clcVal);
				if (ObjectUtils.equals(argVal, propVal))
					clcList.add(clcVal);
			}

			Assert.assertEquals("Mismatched sizes for " + argVal, clcList.size(), qryList.size());
			Assert.assertTrue("Missing query results for " + argVal, qryList.containsAll(clcList));
		}
	}
	
	static interface PropertyExtractor<O,P> {
		P getProperty (O o);
	}
}
