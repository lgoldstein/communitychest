/*
 * 
 */
package net.community.chest.jms.framework;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2010 1:54:18 PM
 */
public abstract class AbstractMessageProducer implements XMessageProducer {
	protected AbstractMessageProducer ()
	{
		super();
	}

	private int	_deliveryMode=(-1);
	/*
	 * @see javax.jms.MessageProducer#getDeliveryMode()
	 */
	@Override
	public int getDeliveryMode () throws JMSException
	{
		return _deliveryMode;
	}
	/*
	 * @see javax.jms.MessageProducer#setDeliveryMode(int)
	 */
	@Override
	public void setDeliveryMode (int deliveryMode) throws JMSException
	{
		_deliveryMode = deliveryMode;
	}
	
	private Destination	_dst;
	/*
	 * @see javax.jms.MessageProducer#getDestination()
	 */
	@Override
	public Destination getDestination () throws JMSException
	{
		return _dst;
	}
	/*
	 * @see net.community.chest.jms.framework.XMessageProducer#setDestination(javax.jms.Destination)
	 */
	@Override
	public void setDestination (Destination dst) throws JMSException
	{
		_dst = dst;
	}

	private boolean	_disableMsgID;
	/*
	 * @see javax.jms.MessageProducer#getDisableMessageID()
	 */
	@Override
	public boolean getDisableMessageID () throws JMSException
	{
		return _disableMsgID;
	}
	/*
	 * @see net.community.chest.jms.framework.XMessageProducer#isDisableMessageID()
	 */
	@Override
	public boolean isDisableMessageID () throws JMSException
	{
		return getDisableMessageID();
	}
	/*
	 * @see javax.jms.MessageProducer#setDisableMessageID(boolean)
	 */
	@Override
	public void setDisableMessageID (boolean value) throws JMSException
	{
		_disableMsgID = value;
	}

	private boolean	_disableMsgTimestamp;
	/*
	 * @see javax.jms.MessageProducer#getDisableMessageTimestamp()
	 */
	@Override
	public boolean getDisableMessageTimestamp () throws JMSException
	{
		return _disableMsgTimestamp;
	}
	/*
	 * @see net.community.chest.jms.framework.XMessageProducer#isDisableMessageTimestamp()
	 */
	@Override
	public boolean isDisableMessageTimestamp () throws JMSException
	{
		return getDisableMessageTimestamp();
	}
	/*
	 * @see javax.jms.MessageProducer#setDisableMessageTimestamp(boolean)
	 */
	@Override
	public void setDisableMessageTimestamp (boolean value) throws JMSException
	{
		_disableMsgTimestamp = value;
	}
	
	private int	_priority;
	/*
	 * @see javax.jms.MessageProducer#getPriority()
	 */
	@Override
	public int getPriority () throws JMSException
	{
		return _priority;
	}
	/*
	 * @see javax.jms.MessageProducer#setPriority(int)
	 */
	@Override
	public void setPriority (int defaultPriority) throws JMSException
	{
		_priority = defaultPriority;
	}
	
	private long	_ttl;
	/*
	 * @see javax.jms.MessageProducer#getTimeToLive()
	 */
	@Override
	public long getTimeToLive () throws JMSException
	{
		return _ttl;
	}
	/*
	 * @see javax.jms.MessageProducer#setTimeToLive(long)
	 */
	@Override
	public void setTimeToLive (long timeToLive) throws JMSException
	{
		_ttl = timeToLive;
	}
	/*
	 * @see javax.jms.MessageProducer#send(javax.jms.Message, int, int, long)
	 */
	@Override
	public void send (Message message, int deliveryMode, int priority, long timeToLive)
		throws JMSException
	{
		send(getDestination(), message, deliveryMode, priority, timeToLive);
	}
	/*
	 * @see javax.jms.MessageProducer#send(javax.jms.Destination, javax.jms.Message)
	 */
	@Override
	public void send (Destination destination, Message message) throws JMSException
	{
		send(destination, message, getDeliveryMode(), getPriority(), getTimeToLive());
	}
	/*
	 * @see javax.jms.MessageProducer#send(javax.jms.Message)
	 */
	@Override
	public void send (Message message) throws JMSException
	{
		send(getDestination(), message);
	}
}
