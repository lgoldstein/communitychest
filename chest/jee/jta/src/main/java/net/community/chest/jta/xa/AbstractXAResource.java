/*
 * 
 */
package net.community.chest.jta.xa;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 2, 2008 12:05:14 PM
 */
public abstract class AbstractXAResource implements XAResource {
	protected AbstractXAResource ()
	{
		super();
	}
	/*
	 * @see javax.transaction.xa.XAResource#isSameRM(javax.transaction.xa.XAResource)
	 */
	@Override
	public boolean isSameRM (XAResource resource) throws XAException
	{
		return (this == resource);
	}

	private int	_timeOut	/* =0 */;
	/*
	 * @see javax.transaction.xa.XAResource#getTransactionTimeout()
	 */
	@Override
	public int getTransactionTimeout () throws XAException
	{
		return _timeOut;
	}
	/*
	 * @see javax.transaction.xa.XAResource#setTransactionTimeout(int)
	 */
	@Override
	public boolean setTransactionTimeout (int timeOut) throws XAException
	{
		_timeOut = timeOut;
		return true;
	}
}
