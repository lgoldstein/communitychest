package com.vmware.spring.workshop.dao.impl;

import java.util.List;

import javax.inject.Inject;

import org.junit.Test;

import com.vmware.spring.workshop.dao.AbstractDaoTestSupport;
import com.vmware.spring.workshop.dao.api.UserDao;
import com.vmware.spring.workshop.dao.finder.IdentifiedInstanceFinder;
import com.vmware.spring.workshop.dao.finder.IdentifiedValuesListFinder;
import com.vmware.spring.workshop.model.user.User;

/**
 * @author lgoldstein
 */
public abstract class AbstractUserDaoTestSupport extends AbstractDaoTestSupport {
	@Inject protected UserDao	_daoUser;

	protected AbstractUserDaoTestSupport() {
		super();
	}

	@Test
	public void testFindById () {
		runIdentifiedByIdFinder(_daoUser);
	}

	@Test
	public void testFindByLoginName () {
		runIdentifiedInstanceFinderTest(_daoUser,
				new IdentifiedInstanceFinder<User,UserDao>() {
					@Override
					public User findInstance(UserDao dao, User sourceInstance) {
						return dao.findByLoginName(sourceInstance.getLoginName());
					}
			
			});
	}
	@Test
	public void testFindByUserLocation () {
		runLocatedInstanceFinderTest(_daoUser,
				new IdentifiedValuesListFinder<User,UserDao,String>() {
					@Override
					public List<User> findMatches(UserDao dao, String arg) {
						return dao.findUserByLocation(arg);
					}
				});
	}

}
