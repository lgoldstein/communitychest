/*
 *
 */
package net.community.chest.jms.framework.queue.impl;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import net.community.chest.jms.framework.message.SimpleMapMessage;
import net.community.chest.jms.framework.message.SimpleObjectMessage;
import net.community.chest.jms.framework.message.SimpleStreamMessage;
import net.community.chest.jms.framework.message.SimpleTextMessage;
import net.community.chest.jms.framework.queue.AbstractQueueSession;
import net.community.chest.jms.framework.queue.SimpleQueue;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 10, 2010 8:15:49 AM
 */
public class SimpleQueueSessionImpl extends AbstractQueueSession {
    private final Map<String,javax.jms.Queue>    _queuesMap;
    protected final Map<String,javax.jms.Queue> getQueuesMap ()
    {
        return _queuesMap;
    }

    private final Map<String,BlockingQueue<Message>>    _qDataMap;
    protected final Map<String,BlockingQueue<Message>> getQueueDataMap ()
    {
        return _qDataMap;
    }

    public SimpleQueueSessionImpl (final Map<String,javax.jms.Queue>            qMap,
                                   final Map<String,BlockingQueue<Message>>     dMap,
                                   final boolean                                transacted,
                                   final int                                    ackMode)
        throws JMSException
    {
        if ((null == (_queuesMap=qMap)) || (null == (_qDataMap=dMap)))
            throw new JMSException("Missing Map(s)");

        setTransacted(transacted);
        setAcknowledgeMode(ackMode);
    }

    private boolean    _closed;
    public boolean isClosed ()
    {
        return _closed;
    }
    /*
     * @see javax.jms.Session#close()
     */
    @Override
    public void close () throws JMSException
    {
        if (!isClosed())
            _closed = true;
    }
    /*
     * @see javax.jms.Session#commit()
     */
    @Override
    public void commit () throws JMSException
    {
        // do nothing
    }
    /*
     * @see javax.jms.Session#createBytesMessage()
     */
    @Override
    public BytesMessage createBytesMessage () throws JMSException
    {
        throw new JMSException("createBytesMessage() N/A");
    }
    /*
     * @see javax.jms.Session#createMapMessage()
     */
    @Override
    public MapMessage createMapMessage () throws JMSException
    {
        if (isClosed())
            throw new JMSException("createMapMessage() - session closed");

        return new SimpleMapMessage();
    }
    /*
     * @see javax.jms.Session#createObjectMessage(java.io.Serializable)
     */
    @Override
    public ObjectMessage createObjectMessage (Serializable object)
            throws JMSException
    {
        if (isClosed())
            throw new JMSException("createObjectMessage() - session closed");

        return new SimpleObjectMessage(object);
    }
    /*
     * @see javax.jms.Session#createMessage()
     */
    @Override
    public Message createMessage () throws JMSException
    {
        if (isClosed())
            throw new JMSException("createMessage() - session closed");

        throw new JMSException("createMessage() N/A");
    }
    /*
     * @see javax.jms.Session#createStreamMessage()
     */
    @Override
    public StreamMessage createStreamMessage () throws JMSException
    {
        if (isClosed())
            throw new JMSException("createStreamMessage() - session closed");

        return new SimpleStreamMessage();
    }
    /*
     * @see javax.jms.Session#createConsumer(javax.jms.Destination, java.lang.String, boolean)
     */
    @Override
    public MessageConsumer createConsumer (Destination destination, String messageSelector, boolean noLocal)
        throws JMSException
    {
        if (isClosed())
            throw new JMSException("createConsumer(" + destination + ")"
                                 + "[" + messageSelector + "]{nolocal=" + noLocal + "}"
                                 + " - session closed");
        // TODO Auto-generated method stub
        return null;
    }
    /*
     * @see javax.jms.Session#createDurableSubscriber(javax.jms.Topic, java.lang.String, java.lang.String, boolean)
     */
    @Override
    public TopicSubscriber createDurableSubscriber (Topic topic, String name, String messageSelector, boolean noLocal)
        throws JMSException
    {
        if (isClosed())
            throw new JMSException("createDurableSubscriber(" + topic + "/" + name + ")"
                                 + "[" + messageSelector + "]{nolocal=" + noLocal + "}"
                                 + " - session closed");
        // TODO Auto-generated method stub
        return null;
    }
    /*
     * @see javax.jms.Session#createTemporaryTopic()
     */
    @Override
    public TemporaryTopic createTemporaryTopic () throws JMSException
    {
        if (isClosed())
            throw new JMSException("createTemporaryTopic() - session closed");

        // TODO Auto-generated method stub
        return null;
    }
    /*
     * @see javax.jms.Session#createTextMessage(java.lang.String)
     */
    @Override
    public TextMessage createTextMessage (String text) throws JMSException
    {
        if (isClosed())
            throw new JMSException("createTextMessage(" + text + ") - session closed");

        return new SimpleTextMessage(text);
    }
    /*
     * @see javax.jms.Session#createTopic(java.lang.String)
     */
    @Override
    public Topic createTopic (String topicName) throws JMSException
    {
        if (isClosed())
            throw new JMSException("createTopic(" + topicName + ") - session closed");
        // TODO Auto-generated method stub
        return null;
    }
    /*
     * @see javax.jms.Session#recover()
     */
    @Override
    public void recover () throws JMSException
    {
        if (isClosed())
            throw new JMSException("recover() - session closed");
    }
    /*
     * @see javax.jms.Session#rollback()
     */
    @Override
    public void rollback () throws JMSException
    {
        if (isClosed())
            throw new JMSException("rollback() - session closed");
    }
    /*
     * @see javax.jms.Session#run()
     */
    @Override
    public void run ()
    {
        if (isClosed())
            throw new IllegalStateException("run() - session closed");
    }
    /*
     * @see javax.jms.Session#unsubscribe(java.lang.String)
     */
    @Override
    public void unsubscribe (String name) throws JMSException
    {
        if ((null == name) || (name.length() <= 0))
            throw new JMSException("unsubscribe() no name specified");
        if (isClosed())
            throw new JMSException("unsubscribe(" + name + ") - session closed");
    }
    /*
     * @see javax.jms.QueueSession#createBrowser(javax.jms.Queue, java.lang.String)
     */
    @Override
    public QueueBrowser createBrowser (Queue queue, String messageSelector)
            throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }
    /*
     * @see javax.jms.QueueSession#createQueue(java.lang.String)
     */
    @Override
    public Queue createQueue (String queueName) throws JMSException
    {
        if (isClosed())
            throw new JMSException("createQueue(" + queueName + ") - session closed");

        final Map<String,javax.jms.Queue>    qm=getQueuesMap();
        javax.jms.Queue                        q=null;
        synchronized(qm)
        {
            if (null == (q=qm.get(queueName)))
            {
                q = new SimpleQueue(queueName);
                qm.put(queueName, q);
            }
        }

        return q;
    }
    /*
     * @see javax.jms.QueueSession#createReceiver(javax.jms.Queue, java.lang.String)
     */
    @Override
    public QueueReceiver createReceiver (Queue queue, String messageSelector)
            throws JMSException
    {
        final String    qName=(null == queue) ? null : queue.getQueueName();
        if ((null == qName) || (qName.length() <= 0))
            throw new JMSException("createReceiver(" + queue + ") no queue name");

        final Map<String,Queue>    qm=getQueuesMap();
        if (!qm.containsKey(qName))
            throw new JMSException("createReceiver(" + qName + ") queue not created via this framework");

        return new SimpleQueueReceiver(getQueueDataMap(), queue, this);
    }
    /*
     * @see javax.jms.QueueSession#createSender(javax.jms.Queue)
     */
    @Override
    public QueueSender createSender (Queue queue) throws JMSException
    {
        final String    qName=(null == queue) ? null : queue.getQueueName();
        if ((null == qName) || (qName.length() <= 0))
            throw new JMSException("createSender(" + queue + ") no queue name");

        final Map<String,Queue>    qm=getQueuesMap();
        if (!qm.containsKey(qName))
            throw new JMSException("createSender(" + qName + ") queue not created via this framework");

        return new SimpleQueueSender(qm, getQueueDataMap(), queue, this);
    }
    /*
     * @see javax.jms.QueueSession#createTemporaryQueue()
     */
    @Override
    public TemporaryQueue createTemporaryQueue () throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }
}
