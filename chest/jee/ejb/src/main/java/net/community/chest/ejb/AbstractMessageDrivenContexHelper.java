/*
 * 
 */
package net.community.chest.ejb;

import java.security.Identity;
import java.security.Principal;
import java.util.Properties;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.MessageDrivenContext;
import javax.ejb.TimerService;
import javax.transaction.UserTransaction;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Helper for {@link MessageDrivenContext} implementors</P>
 * @author Lyor G.
 * @since Sep 2, 2008 12:26:52 PM
 */
@SuppressWarnings("deprecation")
public abstract class AbstractMessageDrivenContexHelper implements MessageDrivenContext {
	private Identity	_callerIdentity	/* =null */;
	/*
	 * @see javax.ejb.EJBContext#getCallerIdentity()
	 */
	@Override
	public Identity getCallerIdentity ()
	{
		return _callerIdentity;
	}

	public void setCallerIdentity (Identity callerIdentity)
	{
		_callerIdentity = callerIdentity;
	}

	private Principal	_callerPrincipal	/* =null */;
	/*
	 * @see javax.ejb.EJBContext#getCallerPrincipal()
	 */
	@Override
	public Principal getCallerPrincipal ()
	{
		return _callerPrincipal;
	}

	public void setCallerPrincipal (Principal callerPrincipal)
	{
		_callerPrincipal = callerPrincipal;
	}

	private EJBHome	_ejbHome	/* =null */;
	/*
	 * @see javax.ejb.EJBContext#getEJBHome()
	 */
	@Override
	public EJBHome getEJBHome ()
	{
		return _ejbHome;
	}

	public void setEJBHome (EJBHome ejbHome)
	{
		_ejbHome = ejbHome;
	}

	private EJBLocalHome	_ejbLocalHome	/* =null */;
	/*
	 * @see javax.ejb.EJBContext#getEJBLocalHome()
	 */
	@Override
	public EJBLocalHome getEJBLocalHome ()
	{
		return _ejbLocalHome;
	}

	public void setEJBLocalHome (EJBLocalHome ejbLocalHome)
	{
		_ejbLocalHome = ejbLocalHome;
	}

	private Properties	_environment	/* =null */;
	/*
	 * @see javax.ejb.EJBContext#getEnvironment()
	 */
	@Override
	public Properties getEnvironment ()
	{
		return _environment;
	}

	public void setEnvironment (Properties environment)
	{
		_environment = environment;
	}

	private boolean	_rollbackOnly	/* =false */;
	/*
	 * @see javax.ejb.EJBContext#getRollbackOnly()
	 */
	@Override
	public boolean getRollbackOnly () throws IllegalStateException
	{
		return _rollbackOnly;
	}

	public void setRollbackOnly (boolean rollbackOnly) throws IllegalStateException
	{
		_rollbackOnly = rollbackOnly;
	}
	/*
	 * @see javax.ejb.EJBContext#setRollbackOnly()
	 */
	@Override
	public void setRollbackOnly () throws IllegalStateException
	{
		_rollbackOnly = true;
	}

	private TimerService	_timerService	/* =null */;
	/*
	 * @see javax.ejb.EJBContext#getTimerService()
	 */
	@Override
	public TimerService getTimerService () throws IllegalStateException
	{
		return _timerService;
	}

	public void setTimerService (TimerService timerService)
	{
		_timerService = timerService;
	}

	private UserTransaction	_userTransaction	/* =null */;
	/*
	 * @see javax.ejb.EJBContext#getUserTransaction()
	 */
	@Override
	public UserTransaction getUserTransaction () throws IllegalStateException
	{
		return _userTransaction;
	}

	public void setUserTransaction (UserTransaction userTransaction)
	{
		_userTransaction = userTransaction;
	}
}
