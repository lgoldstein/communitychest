package com.vmware.spring.workshop.services;

import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 * @author lgoldstein
 */
public abstract class AbstractServicesTestSupport extends AbstractTransactionalJUnit4SpringContextTests {
    public static final String    DEFAULT_TEST_CONTEXT="classpath:META-INF/svcContext.xml";

    protected AbstractServicesTestSupport() {
        super();
    }
}
