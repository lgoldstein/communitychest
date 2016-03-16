/*
 *
 */
package net.community.chest.jms.framework.queue.impl;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javax.jms.ConnectionConsumer;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;

import net.community.chest.jms.framework.AbstractConnection;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2010 4:18:43 PM
 */
public class SimpleQueueConnectionImpl extends AbstractConnection implements QueueConnection {
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

    public SimpleQueueConnectionImpl (final Map<String,javax.jms.Queue>                qMap,
                                         final Map<String,BlockingQueue<Message>>     dMap)
        throws JMSException
    {
        if ((null == (_queuesMap=qMap)) || (null == (_qDataMap=dMap)))
            throw new JMSException("Missing Map(s)");
    }

    private boolean    _closed;
    public boolean isClosed ()
    {
        return _closed;
    }
    /*
     * @see javax.jms.QueueConnection#createConnectionConsumer(javax.jms.Queue, java.lang.String, javax.jms.ServerSessionPool, int)
     */
    @Override
    public ConnectionConsumer createConnectionConsumer (
            Queue queue, String messageSelector, ServerSessionPool sessionPool, int maxMessages)
        throws JMSException
    {
        if (null == queue)
            throw new JMSException("createConnectionConsumer() no queue provided");

        if (isClosed())
            throw new JMSException("createConnectionConsumer(" + queue + ") closed");

        throw new JMSException("createConnectionConsumer(" + queue + ") N/A");
    }
    /*
     * @see javax.jms.QueueConnection#createQueueSession(boolean, int)
     */
    @Override
    public QueueSession createQueueSession (boolean transacted, int acknowledgeMode) throws JMSException
    {
        if (isClosed())
            throw new JMSException("createQueueSession(" + transacted + "/" + acknowledgeMode + ") closed");

        return new SimpleQueueSessionImpl(getQueuesMap(), getQueueDataMap(), transacted, acknowledgeMode);
    }
    /*
     * @see javax.jms.Connection#close()
     */
    @Override
    public void close () throws JMSException
    {
        if (!isClosed())
            _closed = true;
    }
    /*
     * @see javax.jms.Connection#createConnectionConsumer(javax.jms.Destination, java.lang.String, javax.jms.ServerSessionPool, int)
     */
    @Override
    public ConnectionConsumer createConnectionConsumer (
            Destination destination, String messageSelector,
            ServerSessionPool sessionPool, int maxMessages)
        throws JMSException
    {
        if (isClosed())
            throw new JMSException("createConnectionConsumer(" + destination + ")"
                                 + "[" + messageSelector + "] closed");

        throw new JMSException("createConnectionConsumer(" + destination + ")"
                              + "[" + messageSelector + "] N/A");
    }
    /*
     * @see javax.jms.Connection#createDurableConnectionConsumer(javax.jms.Topic, java.lang.String, java.lang.String, javax.jms.ServerSessionPool, int)
     */
    @Override
    public ConnectionConsumer createDurableConnectionConsumer (
            Topic topic, String subscriptionName, String messageSelector,
            ServerSessionPool sessionPool, int maxMessages)
        throws JMSException
    {
        if (isClosed())
            throw new JMSException("createDurableConnectionConsumer(" + topic + "/" + subscriptionName + ")"
                                 + "[" + messageSelector + "] closed");

        throw new JMSException("createDurableConnectionConsumer(" + topic + "/" + subscriptionName + ")"
                              + "[" + messageSelector + "] N/A");
    }
    /*
     * @see javax.jms.Connection#createSession(boolean, int)
     */
    @Override
    public Session createSession (boolean transacted, int acknowledgeMode)
            throws JMSException
    {
        return createQueueSession(transacted, acknowledgeMode);
    }
    /*
     * @see javax.jms.Connection#start()
     */
    @Override
    public void start () throws JMSException
    {
        // do nothing
    }
    /*
     * @see javax.jms.Connection#stop()
     */
    @Override
    public void stop () throws JMSException
    {
        // do nothing
    }
}
