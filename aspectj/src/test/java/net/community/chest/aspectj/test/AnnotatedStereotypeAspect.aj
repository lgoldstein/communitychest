/*
 * 
 */
package net.community.chest.aspectj.test;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 29, 2010 9:38:41 AM
 */
public aspect AnnotatedStereotypeAspect {
	public pointcut collectionPoint ()
		: execution(* (@AnnotatedStereotypeCollected *).*(..))
		;

	protected static final void log (JoinPoint jp, long runDuration, Object retValue)
	{
		final Signature	sig=(null == jp) ? null : jp.getSignature();
		System.out.append(sig.toLongString())
				  .append(" executed in ")
				  .append(String.valueOf(runDuration))
				  .append(" msec. - return value=")
				  .append(String.valueOf(retValue))
				  .println()
				  ;
	}

	Object around () : collectionPoint()
	{
		final long		invStart=System.currentTimeMillis();
		final Object	retValue=proceed();
		final long		invEnd=System.currentTimeMillis();
		log(thisJoinPoint, invEnd - invStart, retValue);
		return retValue;
	}
}
