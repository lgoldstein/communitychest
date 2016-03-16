/*
 *
 */
package net.community.chest.jms.framework;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2010 1:49:06 PM
 */
public abstract class AbstractMessageConsumer implements XMessageConsumer {
    protected AbstractMessageConsumer ()
    {
        super();
    }

    private String    _msgSelector;
    /*
     * @see javax.jms.MessageConsumer#getMessageSelector()
     */
    @Override
    public String getMessageSelector () throws JMSException
    {
        return _msgSelector;
    }
    /*
     * @see net.community.chest.jms.framework.XMessageConsumer#setMessageSelector(java.lang.String)
     */
    @Override
    public void setMessageSelector (String msgSelector) throws JMSException
    {
        _msgSelector = msgSelector;
    }

    private MessageListener    _msgListener;
    /*
     * @see javax.jms.MessageConsumer#getMessageListener()
     */
    @Override
    public MessageListener getMessageListener () throws JMSException
    {
        return _msgListener;
    }
    /*
     * @see javax.jms.MessageConsumer#setMessageListener(javax.jms.MessageListener)
     */
    @Override
    public void setMessageListener (MessageListener listener) throws JMSException
    {
        _msgListener = listener;
    }
    /*
     * @see javax.jms.MessageConsumer#receive()
     */
    @Override
    public Message receive () throws JMSException
    {
        return receive(Long.MAX_VALUE);
    }
    /*
     * @see javax.jms.MessageConsumer#receiveNoWait()
     */
    @Override
    public Message receiveNoWait () throws JMSException
    {
        return receive(0);
    }
}
