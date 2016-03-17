/**
 *
 */
package com.vmware.spring.workshop.dao;

import javax.inject.Inject;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.vmware.spring.workshop.dao.test.beans.TransactionalBean;

/**
 * @author lgoldstein
 */
@ContextConfiguration(locations={
        AbstractDaoTestSupport.DEFAULT_TEST_CONTEXT,
        "classpath:com/vmware/spring/workshop/dao/DaoTestDevelopment.xml"
    })
@ActiveProfiles("jpa")
public class DaoTestDevelopment extends AbstractDaoTestSupport {
    @Inject private TransactionalBean    testBean;

    public DaoTestDevelopment() {
        super();
    }

    @Test
    public void testReadInvocation() {
        logger.info("testReadInvocation: " + testBean.invokeReadOnlyMethod());
    }

    @Test
    public void testWriteInvocation() {
        logger.info("testWriteInvocation: " + testBean.invokeWriteMethod());
    }
}
