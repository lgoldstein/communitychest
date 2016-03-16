/*
 *
 */
package net.community.chest.jms.framework.queue.impl;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueConnection;

import net.community.chest.jms.framework.queue.AbstractQueueConnectionFactory;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2010 4:13:05 PM
 */
public class SimpleQueueConnectionFactoryImpl extends AbstractQueueConnectionFactory {
    private final Map<String,javax.jms.Queue>    _queuesMap=
        new TreeMap<String,javax.jms.Queue>(String.CASE_INSENSITIVE_ORDER);
    protected final Map<String,javax.jms.Queue> getQueuesMap ()
    {
        return _queuesMap;
    }

    private final Map<String,BlockingQueue<Message>>    _qDataMap=
        new TreeMap<String,BlockingQueue<Message>>(String.CASE_INSENSITIVE_ORDER);
    protected final Map<String,BlockingQueue<Message>> getQueueDataMap ()
    {
        return _qDataMap;
    }

    public SimpleQueueConnectionFactoryImpl ()
    {
        super();
    }

    private String    _username;
    public String getUsername ()
    {
        return _username;
    }

    public void setUsername (String username)
    {
        _username = username;
    }

    private String    _password;
    public String getPassword ()
    {
        return _password;
    }

    public void setPassword (String password)
    {
        _password = password;
    }
    /*
     * @see javax.jms.QueueConnectionFactory#createQueueConnection(java.lang.String, java.lang.String)
     */
    @Override
    public QueueConnection createQueueConnection (String userName, String password)
        throws JMSException
    {
        final String    qfUser=getUsername();
        if ((null == qfUser) || (qfUser.length() <= 0))
        {
            if ((userName != null) && (userName.length() > 0))
                throw new JMSException("createQueueConnection() bad credentials");
        }
        else if (!qfUser.equals(userName))
            throw new JMSException("createQueueConnection() bad credentials");

        final String    qfPass=getPassword();
        if ((null == qfPass) || (qfPass.length() <= 0))
        {
            if ((password != null) && (password.length() > 0))
                throw new JMSException("createQueueConnection() bad credentials");
        }
        else if (!qfPass.equals(password))
            throw new JMSException("createQueueConnection() bad credentials");

        return new SimpleQueueConnectionImpl(getQueuesMap(), getQueueDataMap());
    }
}
