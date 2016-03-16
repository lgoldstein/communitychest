/*
 *
 */
package net.community.chest.jms.framework;

import javax.jms.ConnectionMetaData;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2010 8:19:45 AM
 */
public abstract class AbstractConnection implements XConnection {
    protected AbstractConnection ()
    {
        super();
    }

    private String    _clientID;
    /*
     * @see javax.jms.Connection#getClientID()
     */
    @Override
    public String getClientID () throws JMSException
    {
        return _clientID;
    }
    /*
     * @see javax.jms.Connection#setClientID(java.lang.String)
     */
    @Override
    public void setClientID (String clientID) throws JMSException
    {
        _clientID = clientID;
    }

    private ExceptionListener    _excListener;
    /*
     * @see javax.jms.Connection#getExceptionListener()
     */
    @Override
    public ExceptionListener getExceptionListener () throws JMSException
    {
        return _excListener;
    }
    /*
     * @see javax.jms.Connection#setExceptionListener(javax.jms.ExceptionListener)
     */
    @Override
    public void setExceptionListener (ExceptionListener listener)
            throws JMSException
    {
        _excListener = listener;
    }

    private ConnectionMetaData    _metaData;
    /*
     * @see javax.jms.Connection#getMetaData()
     */
    @Override
    public ConnectionMetaData getMetaData () throws JMSException
    {
        return _metaData;
    }
    /*
     * @see net.community.chest.jms.framework.XConnection#setMetaData(javax.jms.ConnectionMetaData)
     */
    @Override
    public void setMetaData (ConnectionMetaData metaData) throws JMSException
    {
        _metaData = metaData;
    }
}
