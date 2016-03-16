/*
 *
 */
package net.community.chest.jms.framework.topic;

import javax.jms.JMSException;
import javax.jms.Topic;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Useful for implementors - the "get"-ers are inherited</P>
 *
 * @author Lyor G.
 * @since Sep 2, 2008 11:12:56 AM
 */
public interface XTopic extends Topic {
    /**
     * <code>getTopicName()</code> is supplied by the {@link Topic} interface
     * @param tName topic name
     * @throws JMSException if internal error
     */
    void setTopicName (String tName) throws JMSException;

}
