package com.vmware.spring.workshop.dao.impl.hibernate;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.vmware.spring.workshop.dao.AbstractDaoTestSupport;
import com.vmware.spring.workshop.dao.impl.AbstractBranchDaoTestSupport;

/**
 * @author lgoldstein
 */
@ContextConfiguration(locations={ AbstractDaoTestSupport.DEFAULT_TEST_CONTEXT })
@ActiveProfiles("hibernate")
public class BranchDaoImplTest extends AbstractBranchDaoTestSupport {

	public BranchDaoImplTest() {
		super();
	}
}
