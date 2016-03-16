/*
 *
 */
package net.community.chest.jms.framework.queue;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueReceiver;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2010 1:51:17 PM
 */
public interface XQueueReceiver extends QueueReceiver {
    void setQueue (Queue q) throws JMSException;
}
