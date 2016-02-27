/*
 * 
 */
package net.community.chest.aspectj.test;


/**
 * <P>Copyright as per GPLv2</P>
 * 
 * @author Lyor G.
 * @since Aug 25, 2010 9:14:58 AM
 */
public abstract aspect RunnableAspect {

	public pointcut collectionPoint (Runnable r)
		: execution(void Runnable+.run()) && target(r)
		;
}
