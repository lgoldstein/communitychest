package net.community.chest.net.proto.text.smtp;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.community.chest.io.EOLStyle;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 20, 2007 8:17:21 AM
 */
public abstract class AbstractSMTPAccessorHelper extends AbstractSMTPAccessor {
    protected AbstractSMTPAccessorHelper ()
    {
        super();
    }
    /**
     * Cached MD5 digester - lazy allocation
     */
    private MessageDigest    _md5Digester    /* =null */;
    /*
     * @see net.community.chest.net.proto.text.smtp.AbstractSMTPAccessor#getMD5Digester()
     */
    @Override
    protected MessageDigest getMD5Digester () throws NoSuchAlgorithmException
    {
        if (null == _md5Digester)
            _md5Digester = super.getMD5Digester();

        return _md5Digester;
    }

    private SMTPResponse    _rsp    /* =null */;
    /**
     * @return a cached (modifiable) response object - allocates one if necessary
     */
    protected SMTPResponse getModifiableResponse ()
    {
        if (null == _rsp)
            _rsp = new SMTPResponse();
        else
            _rsp.reset();

        return _rsp;
    }
    /**
     * Called to read until a final SMTP response found
     * @param rsp (modifiable) response to be set with the (final) parsed result
     * @return same as <I>rsp</I> input
     * @throws IOException if unable to communicate with the server (or malformed responses)
     */
    protected abstract SMTPResponse getFinalResponse (SMTPResponse rsp) throws IOException;
    /*
     * @see net.community.chest.net.proto.text.smtp.AbstractSMTPAccessor#getFinalResponse()
     */
    @Override
    protected SMTPResponse getFinalResponse () throws IOException
    {
        return getFinalResponse(getModifiableResponse());
    }

    private StringBuilder    _cmdBuf;
    protected StringBuilder getCommandBuffer (final int initialSize)
    {
        if (null == _cmdBuf)
            _cmdBuf = new StringBuilder(Math.max(initialSize,Byte.MAX_VALUE));
        else
            _cmdBuf.setLength(0);
        return _cmdBuf;
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.AbstractSMTPAccessor#writeFinalCommand(char[], char[])
     */
    @Override
    protected int writeFinalCommand (char[] cmd, char[] arg) throws IOException
    {
        if ((null == cmd) || (cmd.length <= 0))
            throw new IOException("Bad/illegal command argument");

        final int            argLen=(null == arg) ? 0 : arg.length,
                            alcLen=cmd.length + 1 + Math.max(argLen,0) + EOLStyle.CRLF.length();
        final StringBuilder    sb=SMTPProtocol.buildFinalCommand(getCommandBuffer(alcLen + 4), cmd, arg);
        final int            sbLen=sb.length(),
                            written=writeData(StringUtil.getBackingArray(sb), 0, sbLen, true);
        if (sbLen != written)
            throw new IOException(ClassUtil.getArgumentsExceptionLocation(getClass(), "writeFinalCommand", sb.toString()) + " command write mismatch (" + sbLen + " <> " + written + ")");

        return written;
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.AbstractSMTPAccessor#sendFinalCommand(char[], char[])
     */
    @Override
    protected SMTPResponse sendFinalCommand (char[] cmd, char[] arg) throws IOException
    {
        final int    written=writeFinalCommand(cmd, arg);
        if (written <= 0)
            throw new IOException("Cannot write command data");

        return getFinalResponse();
    }
    /**
     * Appends the command and its target address (if not-empty) to the buffer
     * @param sb string buffer to append to
     * @param cmd command to be used
     * @param addr address (may be null/empty)
     * @return TRUE if successful
     */
    protected static final StringBuilder appendCmdTarget (final StringBuilder sb, final char[] cmd, final char[] addr)
    {
        sb.append(cmd)
          .append(' ')
          .append('<')
          ;

        if ((addr != null) && (addr.length != 0))
            sb.append(addr);

        sb.append('>');
        return sb;
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.AbstractSMTPAccessor#sendTargetCommand(char[], char[])
     */
    @Override
    protected SMTPResponse sendTargetCommand (final char[] cmd, final char[] addr /* may be null/empty */) throws IOException
    {
        if ((null == cmd) || (cmd.length <= 0))
            throw new IOException("Bad/illegal target command argument");

        final int            addrLen=(null == addr) ? 0 : addr.length,
                            allcLen=cmd.length + 1 + Math.max(addrLen,0) + 2 + EOLStyle.CRLF.length();
        final StringBuilder    sb=appendCmdTarget(getCommandBuffer(allcLen), cmd, addr);
        sb.append(EOLStyle.CRLF.getStyleChars());

        return sendFinalCommand(StringUtil.getBackingArray(sb), 0, sb.length());
    }
}
