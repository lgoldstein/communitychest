/*
 * 
 */
package net.community.chest.jms.framework.queue;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;

import net.community.chest.jms.framework.AbstractMessageProducer;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2010 2:06:41 PM
 */
public abstract class AbstractQueueSender
			extends AbstractMessageProducer
			implements XQueueSender {
	protected AbstractQueueSender ()
	{
		super();
	}
	/*
	 * @see javax.jms.QueueSender#getQueue()
	 */
	@Override
	public Queue getQueue () throws JMSException
	{
		final Destination	d=getDestination();
		if (null == d)
			return null;

		if (!(d instanceof Queue))
			throw new JMSException("Destination (" + d + ") not a queue: " + d.getClass().getName());
		return (Queue) d;
	}
	/*
	 * @see net.community.chest.jms.framework.queue.XQueueSender#setQueue(javax.jms.Queue)
	 */
	@Override
	public void setQueue (Queue q) throws JMSException
	{
		setDestination(q);
	}
	/*
	 * @see javax.jms.MessageProducer#send(javax.jms.Destination, javax.jms.Message, int, int, long)
	 */
	@Override
	public void send (Destination d, Message message, int deliveryMode, int priority, long timeToLive)
			throws JMSException
	{
		if (!(d instanceof Queue))
			throw new JMSException("Destination (" + d + ") not a queue: " + d.getClass().getName());

		send((Queue) d, message, deliveryMode, priority, timeToLive);
	}
	/*
	 * @see javax.jms.QueueSender#send(javax.jms.Queue, javax.jms.Message)
	 */
	@Override
	public void send (Queue queue, Message message) throws JMSException
	{
		send(queue, message, getDeliveryMode(), getPriority(), getTimeToLive());
	}
}
