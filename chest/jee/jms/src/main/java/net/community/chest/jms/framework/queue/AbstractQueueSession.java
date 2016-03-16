/*
 *
 */
package net.community.chest.jms.framework.queue;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;

import net.community.chest.CoVariantReturn;
import net.community.chest.jms.framework.AbstractSession;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2010 8:29:18 AM
 */
public abstract class AbstractQueueSession extends AbstractSession implements QueueSession {
    protected AbstractQueueSession ()
    {
        super();
    }
    /*
     * @see javax.jms.QueueSession#createReceiver(javax.jms.Queue)
     */
    @Override
    public QueueReceiver createReceiver (Queue queue) throws JMSException
    {
        return createReceiver(queue, null);
    }
    /*
     * @see javax.jms.Session#createProducer(javax.jms.Destination)
     */
    @Override
    @CoVariantReturn
    public QueueSender createProducer (Destination destination)
            throws JMSException
    {
        if (!(destination instanceof Queue))
            throw new JMSException("createProducer(" + destination + ") not a queue");
        return createSender((Queue) destination);
    }
}
