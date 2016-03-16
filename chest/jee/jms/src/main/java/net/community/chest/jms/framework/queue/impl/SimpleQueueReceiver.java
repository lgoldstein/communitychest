/*
 *
 */
package net.community.chest.jms.framework.queue.impl;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;

import net.community.chest.jms.framework.queue.AbstractQueueReceiver;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 10, 2010 3:28:30 PM
 */
public class SimpleQueueReceiver extends AbstractQueueReceiver {
    private final Map<String,BlockingQueue<Message>>    _qDataMap;
    protected final Map<String,BlockingQueue<Message>> getQueueDataMap ()
    {
        return _qDataMap;
    }

    private final Session    _session;
    protected final Session getSesstion ()
    {
        return _session;
    }

    public SimpleQueueReceiver (final Map<String,BlockingQueue<Message>>     dMap,
                                  final Queue                                    q,
                                  final Session                                sess)
        throws JMSException
    {
        if ((null == q)
         || (null == (_qDataMap=dMap))
         || (null == (_session=sess)))
            throw new JMSException("Missing sender arguments");

        setQueue(q);
    }

    private boolean    _closed;
    public boolean isClosed ()
    {
        return _closed;
    }
    /*
     * @see javax.jms.MessageConsumer#receive(long)
     */
    @Override
    public Message receive (long timeout) throws JMSException
    {
        final Queue        q=getQueue();
        final String    qName=(null == q) ? null : q.getQueueName();
        if (isClosed())
            throw new JMSException("receive(" + qName + ")[" + timeout + "] closed");
        if ((null == qName) || (qName.length() <= 0))
            throw new JMSException("receive()[" + timeout + "] no queue");

        final Map<String,? extends BlockingQueue<Message>>    qdMap=getQueueDataMap();
        final BlockingQueue<Message>                        msgQ=
            ((null == qdMap) || (qdMap.size() <= 0)) ? null : qdMap.get(qName);
        if (null == msgQ)
            return null;

        if ((timeout <= 0L) && msgQ.isEmpty())
            return null;

        try
        {
            return msgQ.poll(timeout, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            throw new JMSException("receive(" + qName + ")[" + timeout + "] " + e.getMessage(), e.getClass().getName());
        }
    }
    /*
     * @see javax.jms.MessageConsumer#close()
     */
    @Override
    public void close () throws JMSException
    {
        if (!isClosed())
            _closed = true;
    }
}
