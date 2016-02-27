package com.vmware.am.policy;

import javax.persistence.Entity;

import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

public final aspect AnnotationPointcuts {

	public static pointcut managedResourceTypePointcut() : 
		@annotation(ManagedResource);
	
	public static pointcut managedResourceMethodsPointcut() : 
		execution(public * (@ManagedResource *).*(..));
	
	public static pointcut managedOperationPointcut() :
		execution(@ManagedOperation public * (@ManagedResource *).*(..));
	
	public static pointcut transactionalPointcut() : 
		execution(@Transactional public * *..*(..)) || execution(public * (@Transactional *).*(..));
	
	public static pointcut repositoryPointcut() :
		@annotation(Repository) || execution(public * (@Repository *).*(..));
	
	public static pointcut entityPointcut() :
		@annotation(Entity) || execution(public * (@Entity+ *).*(..));
	
}
