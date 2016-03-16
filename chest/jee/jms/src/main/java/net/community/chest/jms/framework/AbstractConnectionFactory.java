/*
 *
 */
package net.community.chest.jms.framework;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2010 8:15:09 AM
 */
public abstract class AbstractConnectionFactory implements ConnectionFactory {
    protected AbstractConnectionFactory ()
    {
        super();
    }
    /*
     * @see javax.jms.ConnectionFactory#createConnection()
     */
    @Override
    public Connection createConnection () throws JMSException
    {
        return createConnection(null, null);
    }
}
