/*
 *
 */
package net.community.chest.jms;

import java.net.URI;

import javax.jms.Destination;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.NamingException;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 2, 2008 10:24:58 AM
 */
public class MessageProducerAccessor extends AccessFramework {
    public MessageProducerAccessor ()
    {
        super();
    }
    /**
     * The current {@link MessageProducer} instance
     */
    private MessageProducer    _prod /* =null */;
    public MessageProducer getProducer ()
    {
        return _prod;
    }

    public void setProducer (MessageProducer prod)
    {
        _prod = prod;
    }
    /**
     * Returns a topic/queue producer object - according to the destination class
     * @param sess The {@link Session} to be used
     * @param dst The {@link Destination} object
     * @return The {@link MessageProducer} producer object
     * @throws JMSException if JMS access error
     */
    public static final MessageProducer resolveProducer (final Session sess, final Destination dst) throws JMSException
    {
        final String    dstName=resolveDestinationName(dst);
        if ((null == dstName) || (dstName.length() <= 0))
            throw new InvalidDestinationException(getMissingObjectExc("resolveProducer", dstName, null, Destination.class));

        if (null == sess)
            throw new javax.jms.IllegalStateException(getURIExcPrefix("resolveProducer", dstName, null) + " no " + Session.class.getSimpleName() + " instance");
        /*
         * NOTE !!! theoretically, this method might be called with wrong
         *         session object - i.e., one that does not match the type
         *         of destination (queue/topic) - we assume this is not the case
         */
        if (dst instanceof Queue)
            return ((QueueSession) sess).createSender((Queue) dst);
        else if (dst instanceof Topic)
            return ((TopicSession) sess).createPublisher((Topic) dst);
        else    // should not happen
            throw new javax.jms.IllegalStateException(getURIExcPrefix("resolveProducer", dstName, null) + " unexpected destination class type: " + dst.getClass().getName());
    }

    public MessageProducer createProducer () throws JMSException
    {
        if (getProducer() != null)
            throw new javax.jms.IllegalStateException("createProducer() already have producer set");

        final MessageProducer    p=resolveProducer(getSession(), getDestination());
        setProducer(p);
        if (null == getProducer())
            throw new javax.jms.IllegalStateException("createProducer() cannot retrieve producer");

        return p;
    }
    /*
     * @see net.community.chest.jms.AccessFramework#setup(javax.naming.Context, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean, int)
     */
    @Override
    public void setup (Context jndiCtx, String factoryJNDIName,
            String destJNDIName, String username, String passwd,
            boolean transacted, int ackMode) throws JMSException,
            NamingException
    {
        super.setup(jndiCtx, factoryJNDIName, destJNDIName, username, passwd, transacted, ackMode);
        createProducer();
    }
    /*
     * @see net.community.chest.jms.AccessFramework#setup(java.lang.String, java.lang.String, java.net.URI, javax.naming.Context)
     */
    @Override
    public void setup (String cfcJNDIPrefix, String name, URI qURI, Context ctx) throws NamingException, JMSException
    {
        super.setup(cfcJNDIPrefix, name, qURI, ctx);
        createProducer();
    }
    // actual sender
    public static final void send (MessageProducer prod, Destination dst, Message msg) throws JMSException
    {
        if (null == prod)
            throw new JMSException("No producer");
        else if (null == dst)
            throw new JMSException("No destination");
        else if (dst instanceof Queue)
            ((QueueSender) prod).send((Queue) dst, msg);
        else if (dst instanceof Topic)
            ((TopicPublisher) prod).publish((Topic) dst, msg);
        else
            throw new JMSException("Unknown destination class: " + dst.getClass().getName());
    }
    // actual sender
    public static final void send (MessageProducer prod, Destination dst, Message msg, int deliveryMode, int priority, long timeToLive) throws JMSException
    {
        if (null == prod)
            throw new JMSException("No producer");
        else if (null == dst)
            throw new JMSException("No destination");
        else if (dst instanceof Queue)
            ((QueueSender) prod).send((Queue) dst, msg, deliveryMode, priority, timeToLive);
        else if (dst instanceof Topic)
            ((TopicPublisher) prod).publish((Topic) dst, msg, deliveryMode, priority, timeToLive);
        else
            throw new JMSException("Unknown destination class: " + dst.getClass().getName());
    }
    /*
     * @see MessageProducer (since JMS 1.1)
     */
    public void send (Message msg) throws JMSException
    {
        send(getProducer(), getDestination(), msg);
    }
    /*
     * @see MessageProducer (since JMS 1.1)
     */
    public void send (Message msg, int deliveryMode, int priority, long timeToLive) throws JMSException
    {
        send(getProducer(), getDestination(), msg, deliveryMode, priority, timeToLive);
    }
    /*
     * @see net.community.chest.jms.AccessFramework#close()
     */
    @Override
    public void close () throws JMSException
    {
        JMSException    exc=null;
        {
            final MessageProducer    prod=getProducer();
            if (prod != null)
            {
                try
                {
                    prod.close();
                }
                catch(JMSException je)
                {
                    exc = je;
                }
                finally
                {
                    setProducer(null);
                }
            }
        }

        try
        {
            super.close();
        }
        catch(JMSException je)
        {
            exc = je;
        }

        if (exc != null)
            throw exc;
    }
    /*
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize () throws Throwable
    {
        try
        {
            close();
        }
        catch(JMSException je)
        {
            // debug breakpoint - ignored
        }
        super.finalize();
    }
}
