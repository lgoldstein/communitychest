/*
 * 
 */
package net.community.chest.jms.framework.queue;

import javax.jms.JMSException;
import javax.jms.Queue;

import net.community.chest.jms.framework.AbstractMessageConsumer;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2010 1:51:59 PM
 */
public abstract class AbstractQueueReceiver
			extends AbstractMessageConsumer
			implements XQueueReceiver {
	protected AbstractQueueReceiver ()
	{
		super();
	}
	
	private Queue	_q;
	/*
	 * @see javax.jms.QueueReceiver#getQueue()
	 */
	@Override
	public Queue getQueue () throws JMSException
	{
		return _q;
	}
	/*
	 * @see net.community.chest.jms.framework.queue.XQueueReceiver#setQueue(javax.jms.Queue)
	 */
	@Override
	public void setQueue (Queue q) throws JMSException
	{
		_q = q;
	}
}
