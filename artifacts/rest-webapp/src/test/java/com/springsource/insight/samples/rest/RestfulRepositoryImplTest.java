/**
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.insight.samples.rest;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;

import org.hibernate.EmptyInterceptor;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.springsource.insight.samples.rest.model.RestfulData;
import com.springsource.insight.samples.rest.model.RestfulRepository;

/**
 * @author lgoldstein
 */
@ContextConfiguration(locations={ "classpath:/META-INF/spring/application-context.xml" })
public class RestfulRepositoryImplTest extends AbstractJUnit4SpringContextTests {
    public RestfulRepositoryImplTest() {
        super();
    }

    @Inject    protected SessionFactory    _sessionFactory;
    protected SessionFactory getSessionFactory ()
    {
        return _sessionFactory;
    }

    private boolean    _txParticipating;
    protected void manuallyStartDaoSession () {
        // simulate open session in view
        final SessionFactory    sessFac=getSessionFactory();
        if (TransactionSynchronizationManager.hasResource(sessFac))    {
            // Do not modify the Session: just set the participate flag.
            _txParticipating = true;
        } else {
            // NOTE: the session factory interceptor is overridden by an empty one, because the
            // real interceptor may not function correctly in this test-specific setup.
            final Session session=
                SessionFactoryUtils.getSession(sessFac, EmptyInterceptor.INSTANCE, null);
            session.setFlushMode(FlushMode.AUTO);
            TransactionSynchronizationManager.bindResource(sessFac, new SessionHolder(session));
            logger.info("Started transaction context");
        }
    }

    protected void manuallyEndDaoSession ()    {
        // simulate open session in view
        final SessionFactory    sessFac=getSessionFactory();
        if (!_txParticipating) {
            final SessionHolder sessionHolder=
                (SessionHolder) TransactionSynchronizationManager.unbindResource(sessFac);
            SessionFactoryUtils.releaseSession(sessionHolder.getSession(), sessFac);
            logger.info("Ended transaction context");
        }
    }

    @Inject private RestfulRepository    _repository;

    // define same names as for jUnit 3.x
    @Before
    public void setUp () {
        manuallyStartDaoSession();
        _repository.removeAll();    // make sure starting with a clean database
    }

    @After
    public void tearDown ()    {
        _repository.removeAll();    // clean up the database
        manuallyEndDaoSession();
    }

    private static final int    TEST_BALANCE=7031965;
    @Test
    public void testBalanceCreation () {
        final RestfulData    value=createTestValue("testBalanceCreation", TEST_BALANCE),
                            item=_repository.getData(value.getId().longValue());
        Assert.assertNotNull("No item persisted", item);
        Assert.assertEquals("Mismatched persisted data", value, item);
    }

    @Test
    public void testBalanceUpdate () {
        final int            NEW_BALANCE=1704169;
        final RestfulData    value=createTestValue("testBalanceUpdate", TEST_BALANCE);
        final Long            itemId=value.getId();
        final RestfulData    updated=_repository.setBalance(itemId.longValue(), NEW_BALANCE);
        Assert.assertNotNull("No balance updated", updated);
        Assert.assertEquals("Mismatched updated balance", NEW_BALANCE, updated.getBalance());

        final Long    updId=updated.getId();
        Assert.assertNotNull("No ID assigned for updated instance", itemId);
        Assert.assertEquals("Mismatched updated instance ID", itemId, updId);
    }

    @Test
    public void testValidationFailure () {
        final RestfulData    value=createTestValue("testBalanceUpdate", TEST_BALANCE);
        try {
            _repository.setBalance(value.getId().longValue(), Integer.MIN_VALUE);
            Assert.fail("Unexpected success of illegal balance value update");
        } catch(ConstraintViolationException e) {
            // ignored since expected
            if (logger.isDebugEnabled()) {
                logger.debug("testValidationFailure() ignored exception", e);
            }
        }
    }

    private RestfulData createTestValue (final String identifier, final int balance) {
        final RestfulData    value=_repository.create(balance);
        Assert.assertNotNull(identifier + "[No balance created]", value);
        Assert.assertEquals(identifier + "[Mismatched created balance]", balance, value.getBalance());
        Assert.assertNotNull(identifier + "[No ID assigned for created instance]", value.getId());
        return value;
    }
}
