/*
 *
 */
package net.community.chest.jms.framework.queue;

import java.io.Serializable;

import javax.jms.JMSException;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 2, 2008 11:17:40 AM
 */
public class SimpleQueue implements XQueue, Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -894120838782920448L;
    public SimpleQueue ()
    {
        super();
    }

    private String    _qName    /* =null */;
    /*
     * @see javax.jms.Queue#getQueueName()
     */
    @Override
    public String getQueueName () throws JMSException
    {
        return _qName;
    }
    /*
     * @see net.community.chest.jms.queue.XQueue#setQueueName(java.lang.String)
     */
    @Override
    public void setQueueName (String name) throws JMSException
    {
        _qName = name;
    }

    public SimpleQueue (String name)
    {
        _qName = name;
    }
}
