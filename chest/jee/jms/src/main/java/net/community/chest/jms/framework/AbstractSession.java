/*
 * 
 */
package net.community.chest.jms.framework;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2010 8:24:47 AM
 */
public abstract class AbstractSession implements XSession {
	protected AbstractSession ()
	{
		super();
	}

	private int	_ackMode=(-1);
	@Override
	public int getAcknowledgeMode () throws JMSException
	{
		return _ackMode;
	}
	/*
	 * @see net.community.chest.jms.framework.XSession#setAcknowledgeMode(int)
	 */
	@Override
	public void setAcknowledgeMode (int mode) throws JMSException
	{
		_ackMode = mode;
	}

	private boolean	_transacted;
	/*
	 * @see net.community.chest.jms.framework.XSession#isTransacted()
	 */
	@Override
	public boolean isTransacted () throws JMSException
	{
		return _transacted;
	}
	/*
	 * @see javax.jms.Session#getTransacted()
	 */
	@Override
	public boolean getTransacted () throws JMSException
	{
		return isTransacted();
	}
	/*
	 * @see net.community.chest.jms.framework.XSession#setTransacted(boolean)
	 */
	@Override
	public void setTransacted (boolean transacted) throws JMSException
	{
		_transacted = transacted;
	}

	private MessageListener	_msgListener;
	/*
	 * @see javax.jms.Session#getMessageListener()
	 */
	@Override
	public MessageListener getMessageListener () throws JMSException
	{
		return _msgListener;
	}
	/*
	 * @see javax.jms.Session#setMessageListener(javax.jms.MessageListener)
	 */
	@Override
	public void setMessageListener (MessageListener listener) throws JMSException
	{
		_msgListener = listener;
	}
	/*
	 * @see javax.jms.Session#createBrowser(javax.jms.Queue)
	 */
	@Override
	public QueueBrowser createBrowser (Queue queue) throws JMSException
	{
		return createBrowser(queue, null);
	}
	/*
	 * @see javax.jms.Session#createConsumer(javax.jms.Destination, java.lang.String)
	 */
	@Override
	public MessageConsumer createConsumer (Destination destination, String messageSelector)
		throws JMSException
	{
		return createConsumer(destination, messageSelector, true);
	}
	/*
	 * @see javax.jms.Session#createConsumer(javax.jms.Destination)
	 */
	@Override
	public MessageConsumer createConsumer (Destination destination) throws JMSException
	{
		return createConsumer(destination, null);
	}
	/*
	 * @see javax.jms.Session#createDurableSubscriber(javax.jms.Topic, java.lang.String)
	 */
	@Override
	public TopicSubscriber createDurableSubscriber (Topic topic, String name)
			throws JMSException
	{
		return createDurableSubscriber(topic, name, null, true);
	}
	/*
	 * @see javax.jms.Session#createObjectMessage()
	 */
	@Override
	public ObjectMessage createObjectMessage () throws JMSException
	{
		return createObjectMessage(null);
	}
	/*
	 * @see javax.jms.Session#createTextMessage()
	 */
	@Override
	public TextMessage createTextMessage () throws JMSException
	{
		return createTextMessage(null);
	}
}
