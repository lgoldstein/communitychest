/*
 * 
 */
package net.community.chest.aspectj.test;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.JoinPoint;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Sep 13, 2010 1:09:48 PM
 */
public aspect HttpServletDoOperationAspect {
    public pointcut doServletOperation ()
    	: execution(void HttpServlet.do*(HttpServletRequest,HttpServletResponse))
    	;

    // use !cflowbelow to avoid trapping recursive calls
    public pointcut collectionPoint() 
    	: doServletOperation() && !cflowbelow(doServletOperation())
    	;

    protected void traceJoinPoint (String where, JoinPoint jp, Throwable exception)
    {
    	final HttpServlet			s=(HttpServlet) jp.getTarget();
    	final Object[]				args=jp.getArgs();
    	final HttpServletRequest	req=(HttpServletRequest) args[0];
    	final HttpServletResponse	rsp=(HttpServletResponse) args[1];
    	if (exception != null)
    		exception.printStackTrace(System.err);
    	else
    		System.out.append(where).println();
    }

    before()
    	: collectionPoint()
    {
    	traceJoinPoint("before", thisJoinPoint, null);
    }
    
    after () returning(Object returnValue)
		: collectionPoint()
	{
    	System.out.append("Return value=").append(String.valueOf(returnValue)).println();
    	traceJoinPoint("after", thisJoinPoint, null);
	}
    
    after() throwing(Throwable exception)
		: collectionPoint()
	{
    	traceJoinPoint("after", thisJoinPoint, exception);
	}
}
