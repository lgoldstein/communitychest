/**
 *
 */
package com.vmware.spring.workshop.dao.test.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author lgoldstein
 */
@Component
@Transactional
public class TransactionalBeanImpl implements TransactionalBean {
    protected final Logger    logger=LoggerFactory.getLogger(getClass());

    public TransactionalBeanImpl() {
        super();
    }

    @Override
    @Transactional(readOnly=true)
    public String invokeReadOnlyMethod() {
        showCallStack("invokeReadOnlyMethod");
        return getClass().getSimpleName();
    }

    @Override
    public long invokeWriteMethod() {
        long    start=System.nanoTime();
        showCallStack("invokeWriteMethod");
        return System.nanoTime() - start;
    }

    protected void showCallStack(String invokerName) {
        Throwable    t=new Throwable();
        t.fillInStackTrace();

        for (StackTraceElement e : t.getStackTrace()) {
            logger.info(invokerName + ": " + e.getClassName() + "#" + e.getMethodName() + ": " + e.getFileName() + " at " + e.getLineNumber());
        }
    }
}
