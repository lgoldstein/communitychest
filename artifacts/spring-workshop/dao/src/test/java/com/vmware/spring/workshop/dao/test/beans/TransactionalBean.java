/**
 * 
 */
package com.vmware.spring.workshop.dao.test.beans;


/**
 * @author lgoldstein
 */
public interface TransactionalBean {
	String invokeReadOnlyMethod();
	long invokeWriteMethod();
}
