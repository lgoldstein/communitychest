package com.vmware.am.policy;


public final aspect NamingPointcuts {
	public static pointcut managedResourceNamingPointcut() :
		within(*..*JmxManaged*);
	
	public static pointcut daoPackagePointcut() :
		within(*..dao..*);
	
	public static pointcut modelPackagePointcut() :
		within(*..model..*);
}
