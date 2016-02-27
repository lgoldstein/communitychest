/*
 * 
 */
package net.community.chest.jms.framework.queue;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;

import net.community.chest.CoVariantReturn;
import net.community.chest.jms.framework.AbstractConnectionFactory;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2010 8:17:51 AM
 */
public abstract class AbstractQueueConnectionFactory
		extends AbstractConnectionFactory
		implements QueueConnectionFactory {

	protected AbstractQueueConnectionFactory ()
	{
		super();
	}
	/*
	 * @see javax.jms.QueueConnectionFactory#createQueueConnection()
	 */
	@Override
	public QueueConnection createQueueConnection () throws JMSException
	{
		return createQueueConnection(null, null);
	}
	/*
	 * @see javax.jms.ConnectionFactory#createConnection(java.lang.String, java.lang.String)
	 */
	@Override
	@CoVariantReturn
	public QueueConnection createConnection (String userName, String password)
			throws JMSException
	{
		return createQueueConnection(userName, password);
	}
}
