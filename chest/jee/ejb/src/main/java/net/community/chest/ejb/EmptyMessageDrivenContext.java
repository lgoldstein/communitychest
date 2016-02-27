/*
 * 
 */
package net.community.chest.ejb;

import java.security.Identity;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Useful class for simulating EJB(s) - especially MDB(s) - returns NULL
 * from ALL its "get" methods</P>
 *
 * @author Lyor G.
 * @since Sep 2, 2008 11:50:54 AM
 */
@SuppressWarnings("deprecation")
public class EmptyMessageDrivenContext extends AbstractMessageDrivenContexHelper {
	public EmptyMessageDrivenContext ()
	{
		super();
	}
	/*
	 * @see javax.ejb.EJBContext#isCallerInRole(java.security.Identity)
	 */
	@Override
	public boolean isCallerInRole (Identity role)
	{
		return false;
	}
	/*
	 * @see javax.ejb.EJBContext#isCallerInRole(java.lang.String)
	 */
	@Override
	public boolean isCallerInRole (String roleName)
	{
		return false;
	}
	/*
	 * @see javax.ejb.EJBContext#lookup(java.lang.String)
	 */
	@Override
	public Object lookup (String name)
	{
		return null;
	}
}
