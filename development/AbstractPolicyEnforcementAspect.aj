package com.vmware.am.policy;


public abstract aspect AbstractPolicyEnforcementAspect {
	
	public pointcut sysoutPointcut() : 
		get(* java.lang.System.out);
	
	public pointcut syserrPointcut() : 
		get(* java.lang.System.err);
	
	public pointcut oldJavaApiPointcut() :
		call(* edu.emory.mathcs..*.*(..));
	
	public pointcut useOfStringBuffer() :
		call(* java.lang.StringBuffer.*(..)) || call(java.lang.StringBuffer.new(..));
	
	public pointcut log4jApiPointcut() :
		call(* org.apache.log*..*.*(..));
	
	declare warning: 
		sysoutPointcut() || syserrPointcut() : 
			"[Coding Conventions] Replace with Logger";
		
	declare error:
		log4jApiPointcut() :
			"[Bad Practice] Using direct Log4j API. Use common log.";
	
	declare error : 
		AnnotationPointcuts.transactionalPointcut() && AnnotationPointcuts.managedResourceMethodsPointcut() : 
			"[Bad Practice] Mixing 2 proxy type - @Transactional should not be used with @ManagedResource";
	
	declare error :
		oldJavaApiPointcut() || useOfStringBuffer() :
			"[Bad Practice] Using old/wrong Java API - e.g. edu.emory.mathcs.backport.java.util.Collections instead of java.util.Collections";
	
	declare warning : 
		AnnotationPointcuts.managedResourceTypePointcut() && !NamingPointcuts.managedResourceNamingPointcut() :
			"[Coding Conventions] Managed resource not matching pattern [X]JmxManaged[X]!";
		
	declare warning :
		AnnotationPointcuts.managedResourceMethodsPointcut() && !AnnotationPointcuts.managedOperationPointcut() :
			"[Bad Practice] @ManagedResource should declared public methods as @ManagedOperation";
	
	declare error :
		AnnotationPointcuts.repositoryPointcut() && !NamingPointcuts.daoPackagePointcut() :
			"[Coding Conventions] @Repository not in dao package"; 
		
	declare error :
		AnnotationPointcuts.entityPointcut() && !NamingPointcuts.modelPackagePointcut() :
			"[Coding Conventions] @Entity not in model package"; 	
}
