/*
 *
 */
package net.community.chest.jms.framework.message;

import javax.jms.JMSException;
import javax.jms.TextMessage;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2010 4:03:55 PM
 */
public class SimpleTextMessage extends AbstractMessage implements TextMessage {
    private String    _text;
    public SimpleTextMessage (String text)
    {
        _text = text;
    }

    public SimpleTextMessage ()
    {
        this(null);
    }
    /*
     * @see javax.jms.TextMessage#getText()
     */
    @Override
    public String getText () throws JMSException
    {
        return _text;
    }
    /*
     * @see javax.jms.TextMessage#setText(java.lang.String)
     */
    @Override
    public void setText (String string) throws JMSException
    {
        _text = string;
    }
    /*
     * @see javax.jms.Message#clearBody()
     */
    @Override
    public void clearBody () throws JMSException
    {
        setText(null);
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return _text;
    }
}
