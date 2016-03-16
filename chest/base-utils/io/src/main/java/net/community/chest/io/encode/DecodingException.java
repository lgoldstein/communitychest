package net.community.chest.io.encode;

import java.io.IOException;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Exception that is thrown when an unexpected character is encountered
 * during decoding. One could catch this exception and use the unexpected
 * character for some other purpose such as including it with data that
 * comes at the end of a Base64 encoded section of an email message.</P>
 *
 * @author Lyor G.
 * @since Aug 22, 2007 9:03:03 AM
 */
public class DecodingException extends IOException {
    /**
     *
     */
    private static final long serialVersionUID = 7369575353499498460L;
    private final char _badChar;
    /**
     * Construct an new exception.
     * @param message message later to be returned by a getMessage() call.
     * @param c character that caused this error.
     */
    public DecodingException (String message, char c)
    {
        super(message);
        _badChar = c;
    }
    /**
     * @return the character that caused this error.
     */
    public char getChar ()
    {
        return _badChar;
    }

}
