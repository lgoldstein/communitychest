/*
 *
 */
package net.community.chest.jms.framework;

import javax.jms.Connection;
import javax.jms.ConnectionMetaData;
import javax.jms.JMSException;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Useful for implementors - the "get"-ers are inherited</p>
 * @author Lyor G.
 * @since Sep 2, 2008 8:44:58 AM
 */
public interface XConnection extends Connection {
    /**
     * Updates the connection meta-data
     * @param metaData meta-data object
     * @throws JMSException if internal error
     * @see Connection#getMetaData()
     */
    void setMetaData (ConnectionMetaData metaData) throws JMSException;
}
