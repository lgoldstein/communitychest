package com.vmware.spring.workshop.dao.impl.springdata;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.vmware.spring.workshop.dao.AbstractDaoTestSupport;
import com.vmware.spring.workshop.dao.impl.AbstractUserDaoTestSupport;

/**
 * @author lgoldstein
 */
@ContextConfiguration(locations={ AbstractDaoTestSupport.DEFAULT_TEST_CONTEXT })
@ActiveProfiles("springdata")
public class UserDaoImplTest extends AbstractUserDaoTestSupport {
	public UserDaoImplTest() {
		super();
	}
}
