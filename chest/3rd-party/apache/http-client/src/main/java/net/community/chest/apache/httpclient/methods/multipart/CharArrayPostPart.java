package net.community.chest.apache.httpclient.methods.multipart;

import org.apache.commons.httpclient.methods.multipart.StringPart;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Useful class for posting a char-array data value. The default charset is
 * same as the JVM, but can be changed via {@link org.apache.commons.httpclient.methods.multipart.PartBase#setCharSet(java.lang.String)}</P>
 *
 * @author Lyor G.
 * @since Oct 10, 2007 12:15:05 PM
 */
public class CharArrayPostPart extends StringPart {
    /**
     * @param name parameter name
     * @param b data buffer to be posted - may NOT be null
     * @param off offset of data in buffer to post - may NOT be <0
     * @param len number of elements - may NOT be <=0
     * @throws IndexOutOfBoundsException if illegal buffer data specified
     * @see String#String(char[], int, int)
     */
    public CharArrayPostPart (String name, char[] b, int off, int len) throws IndexOutOfBoundsException
    {
        super(name, new String(b, off, len));
    }
    /**
     * @param name parameter name
     * @param b data buffer to be posted - may NOT be null/empty
     * @throws IndexOutOfBoundsException if illegal buffer data specified
     * @see String#String(char[])
     */
    public CharArrayPostPart (String name, char[] b) throws IndexOutOfBoundsException
    {
        super(name, new String(b));
    }
}
