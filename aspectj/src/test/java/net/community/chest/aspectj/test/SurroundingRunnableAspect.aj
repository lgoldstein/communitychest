/*
 * 
 */
package net.community.chest.aspectj.test;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>Here is a quick summary of the exception introduction pattern by way of an
 * example (source containing working examples can be downloaded from the
 * following <A HREF="http://www.manning.com/laddad">site</A>):</P></BR>
 *
 * <P>The pattern has two parts. In the first part, you simply throw a runtime
 * exception that wraps the checked exception.</P>
 * 
 * <P><PRE>
 * public aspect TransactionManagementAspect {  
 *		Object around() : transactedOperations() {
 *      	Object retValue = null;
 *	         try {
 *             beginTransaction();
 *             retValue = proceed();
 *             endTransaction();
 *         } catch (Exception ex) {
 *             rollbackTransaction();
 *            // ex could be, for example, BusinessException
 *            // <Part1>
 *            throw new TransactionRuntimeException(ex);             
 *            // </Part1>
 *         }
 *         return retValue;
 *     }
 * }
 * </PRE></P> 
 * <P>The second part of the pattern reinstates the wrapped checked exception,
 * for the methods that were expecting a BusinessException:</P></BR>
 *
 * <P><PRE>
 * public aspect ReinstateBusinessException {  
 *     declare precedence: ReinstateBusinessException,  
 *                         TransactionManagementAspect;
 *   
 *     after() throwing(TransactionRuntimeException ex)    
 *         throws BusinessException
 *       : call(* *.*(..) throws BusinessException) {    
 *         Throwable cause = ex.getCause();
 *         if (cause instanceof BusinessException) {      
 *             throw (BusinessException)cause;    
 *         }  
 *         throw ex;
 *     }
 * }
 * </PRE></P>
 * @author Lyor G.
 * @since Aug 29, 2010 7:51:50 AM
 */
public aspect SurroundingRunnableAspect extends RunnableAspect {
	private static final Logger	_logger=Logger.getLogger(SurroundingRunnableAspect.class.getSimpleName());
	public static final void log (Level lvl, JoinPoint jp, long runDuration)
	{
		final Signature	sig=(null == jp) ? null : jp.getSignature();
		final Thread	t=Thread.currentThread();
		if (Level.SEVERE.equals(lvl))
			_logger.log(lvl, "\t\t[" + t.getName() + "] "
					   + ((null == sig) ? null : sig.getDeclaringTypeName())
					   + "#" + ((null == sig) ? null : sig.getName())
					   + ": error after " + runDuration + " msec.");
		else
			_logger.log(lvl, "\t\t[" + t.getName() + "] "
					   + ((null == sig) ? null : sig.getDeclaringTypeName())
					   + "#" + ((null == sig) ? null : sig.getName())
					   + ": around in " + runDuration + " msec.");
	}

	void around (Runnable r)
		: collectionPoint(r)
	{
		final long	rStart=System.currentTimeMillis();
		try
		{
			proceed(r);
		}
		catch(RuntimeException e)
		{
			final long	rEnd=System.currentTimeMillis(), rDuration=rEnd - rStart;
			log(Level.SEVERE, thisJoinPoint, rDuration);
			throw e;
		}

		final long	rEnd=System.currentTimeMillis(), rDuration=rEnd - rStart;
		log(Level.INFO, thisJoinPoint, rDuration);
	}
}
