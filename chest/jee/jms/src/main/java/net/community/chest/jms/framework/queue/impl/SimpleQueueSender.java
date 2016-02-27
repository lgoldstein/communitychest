/*
 * 
 */
package net.community.chest.jms.framework.queue.impl;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;

import net.community.chest.jms.framework.queue.AbstractQueueSender;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 10, 2010 10:21:04 AM
 */
public class SimpleQueueSender extends AbstractQueueSender {
	private final Map<String,BlockingQueue<Message>>	_qDataMap;
	protected final Map<String,BlockingQueue<Message>> getQueueDataMap ()
	{
		return _qDataMap;
	}

	private final Map<String,javax.jms.Queue>	_queuesMap;
	protected final Map<String,javax.jms.Queue> getQueuesMap ()
	{
		return _queuesMap;
	}

	private final Session	_session;
	protected final Session getSesstion ()
	{
		return _session;
	}

	public SimpleQueueSender (final Map<String,javax.jms.Queue>				qMap,
			   				  final Map<String,BlockingQueue<Message>> 	dMap,
			   				  final Queue									q,
			   				  final Session									sess)
		throws JMSException
	{
		if ((null == q)
		 || (null == (_queuesMap=qMap))
		 || (null == (_qDataMap=dMap))
		 || (null == (_session=sess)))
			throw new JMSException("Missing sender arguments");

		setQueue(q);
	}

	private boolean	_closed;
	public boolean isClosed ()
	{
		return _closed;
	}
	/*
	 * @see javax.jms.QueueSender#send(javax.jms.Queue, javax.jms.Message, int, int, long)
	 */
	@Override
	public void send (Queue queue, Message message, int deliveryMode, int priority, long timeToLive)
		throws JMSException
	{
		if (isClosed())
			throw new JMSException("send(" + queue + ")[" + message + "] closed");

		final String	qName=(null == queue) ? null : queue.getQueueName();
		if ((null == queue) || (null == message))
			throw new JMSException("send(" + qName + ")[" + message + "] missing arguments");

		final Map<String, ? extends Queue>	qm=getQueuesMap();
		if (!qm.containsKey(qName))
			throw new JMSException("send(" + qName + ")[" + message + "] queue not created via this framework");

		message.setJMSDestination(queue);
		message.setJMSDeliveryMode(deliveryMode);
		message.setJMSPriority(priority);
		if (timeToLive > 0L)
			message.setJMSExpiration(System.currentTimeMillis() + timeToLive);

		final Session			s=getSesstion();
		final MessageListener	l=(null == s) ? null : s.getMessageListener();
		if (l != null)
		{
			l.onMessage(message);
			return;
		}

		final Map<String,BlockingQueue<Message>>	dm=getQueueDataMap();
		BlockingQueue<Message>						dl=null;
		synchronized(dm)
		{
			if (null == (dl=dm.get(qName)))
			{
				dl = new LinkedBlockingQueue<Message>();
				dm.put(qName, dl);
			}
		}

		if (!dl.offer(message))
			throw new JMSException("send(" + qName + ")[" + message + "] failed to enqueue");
	}
	/*
	 * @see javax.jms.MessageProducer#close()
	 */
	@Override
	public void close () throws JMSException
	{
		if (!isClosed())
			_closed = true;
	}
}
