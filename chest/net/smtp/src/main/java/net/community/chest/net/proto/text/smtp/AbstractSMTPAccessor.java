package net.community.chest.net.proto.text.smtp;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.naming.NamingException;

import net.community.chest.ParsableString;
import net.community.chest.io.EOLStyle;
import net.community.chest.io.encode.base64.Base64;
import net.community.chest.net.NetUtil;
import net.community.chest.net.TextNetConnection;
import net.community.chest.net.auth.AuthDigester;
import net.community.chest.net.dns.DNSAccess;
import net.community.chest.net.dns.SMTPMxRecord;
import net.community.chest.net.proto.text.AbstractTextProtocolNetConnectionHelper;
import net.community.chest.net.proto.text.NetServerWelcomeLine;
import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 20, 2007 7:48:00 AM
 */
public abstract class AbstractSMTPAccessor extends
        AbstractTextProtocolNetConnectionHelper implements SMTPAccessor {
    protected AbstractSMTPAccessor ()
    {
        super();
    }
    /*
     * @see net.community.chest.net.proto.ProtocolNetConnection#getDefaultPort()
     */
    @Override
    public int getDefaultPort()
    {
        return SMTPProtocol.IPPORT_SMTP;
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.SMTPAccessor#mxConnect(net.community.chest.net.dns.DNSAccess, java.lang.String, int, net.community.chest.net.proto.text.NetServerWelcomeLine)
     */
    @Override
    public void mxConnect (DNSAccess nsa, String dmName, int nPort, NetServerWelcomeLine wl) throws IOException
    {
        try
        {
            final List<? extends SMTPMxRecord>    recs=SMTPMxRecord.mxLookup(nsa, dmName);
            final int                            numRecs=(null == recs) ? 0 : recs.size();
            if (numRecs > 0)
            {
                for (int    recIndex=0; recIndex < numRecs; recIndex++)
                {
                    final SMTPMxRecord mxRec=recs.get(recIndex);
                    if (null == mxRec)
                        continue;    // should not happen

                    try
                    {
                        connect(mxRec.getHost(), nPort, wl);
                    }
                    // catch the exception, but assume due to inability to connect
                    catch(IOException ioe)
                    {
                        // if no more hosts to check then throw the exception
                        // (just so the "throws" declaration is warranted)
                        if (recIndex >= (numRecs-1))
                            throw ioe;
                    }

                    // make sure ready for next attempt
                    close();
                }
            }

            // this point is reached if all MX records exhausted (if had any to begin with)
            throw new EOFException(ClassUtil.getExceptionLocation(getClass(), "mxConnect") + " all MX records exhausted");
        }
        catch(NamingException ne)
        {
            throw new IOException(ClassUtil.getExceptionLocation(getClass(), "mxConnect") + " " + ne.getClass().getName() + " while resolving MX records: " + ne.getMessage());
        }
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.SMTPAccessor#mxConnect(net.community.chest.net.dns.DNSAccess, java.lang.String, int)
     */
    @Override
    public void mxConnect (DNSAccess nsa, String dmName, int nPort) throws IOException
    {
        mxConnect(nsa, dmName, nPort, null);
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.SMTPAccessor#mxConnect(net.community.chest.net.dns.DNSAccess, java.lang.String, net.community.chest.net.proto.text.NetServerWelcomeLine)
     */
    @Override
    public void mxConnect (DNSAccess nsa, String dmName, NetServerWelcomeLine wl) throws IOException
    {
        mxConnect(nsa, dmName, getDefaultPort(), wl);
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.SMTPAccessor#mxConnect(net.community.chest.net.dns.DNSAccess, java.lang.String)
     */
    @Override
    public void mxConnect (DNSAccess nsa, String dmName) throws IOException
    {
        mxConnect(nsa, dmName, null);
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.SMTPAccessor#writeData(char[], boolean)
     */
    @Override
    public int writeData (char[] data, boolean flushIt) throws IOException
    {
        return writeData(data, 0, (null == data) ? 0 : data.length, flushIt);
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.SMTPAccessor#writeBytes(byte[], boolean)
     */
    @Override
    public int writeBytes (final byte[] buf, final boolean flushIt) throws IOException
    {
        return writeBytes(buf, 0, (null == buf) ? 0 : buf.length, flushIt);
    }
    /**
     * Reads from input until a non-continued response found
     * @return SMTP response code
     * @throws IOException if network errors
     * @see SMTPProtocol for known response codes
     */
    protected abstract SMTPResponse getFinalResponse () throws IOException;
    /**
     * Flushes whatever data is waiting to be written
     * @throws IOException if network errors
     */
    protected abstract void flushWrite () throws IOException;
    /**
     * Sends a command and returns the final response code (ignoring any continuation lines)
     * @param cmd command buffer to be sent
     * @param startOffset offset withing buffer to send data
     * @param len total length of data to be sent (including CRLF) - (<0) is also error
     * @return SMTP response code
     * @throws IOException if network errors
     * @see SMTPProtocol for known response codes
     */
    protected SMTPResponse sendFinalCommand (char[] cmd, int startOffset, int len) throws IOException
    {
        final int    written=writeData(cmd, startOffset, len, true);
        if (written != len)
            throw new IOException("Write mismatch (" + written + " <> " + len + ") on send final command" + new String(cmd, startOffset, Math.max(EOLStyle.CRLF.length(), len - EOLStyle.CRLF.length())));

        return getFinalResponse();
    }
    /**
     * Builds and sends a command, and returns the final response code (ignoring any continuation lines)
     * @param cmd command to be sent
     * @param arg argument (may be null/empty)
     * @return SMTP response code
     * @throws IOException if network errors
     * @see SMTPProtocol for known response codes
     */
    protected abstract SMTPResponse sendFinalCommand (char[] cmd, char[] arg) throws IOException;
    /**
     * Resolve the domain argument to be sent for HELO/EHLO command(s)
     * @param dmn domain to be sent as parameter - if null/empty then current host domain is used
     * @return domain to be sent (null if error/no data)
     */
    protected static final String resolveGreetingDomain (final String dmn)
    {
        try
        {
            String  hloDomain=dmn;
            if ((null == hloDomain) || (hloDomain.length() <= 0))
                hloDomain = NetUtil.getComputerDomain();
            if ((null == hloDomain) || (hloDomain.length() <= 0))
                hloDomain = NetUtil.getComputerName();
            return hloDomain;
        }
        catch(UnknownHostException uhe)
        {
            return null;
        }
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.SMTPAccessor#helo(java.lang.String)
     */
    @Override
    public SMTPResponse helo (String dmn /* may be null/empty */) throws IOException
    {
        final String hloDomain=resolveGreetingDomain(dmn);
        if ((null == hloDomain) || (hloDomain.length() <= 0))
            throw new IOException("No HELO local host information available");

        return sendFinalCommand(SMTPProtocol.SMTPHeloCmdChars, hloDomain.toCharArray());
    }
    /**
     * Builds and sends a command
     * @param cmd command to be sent
     * @param arg argument (may be null/empty)
     * @return number of written characters
     * @throws IOException if network errors
     * @see SMTPProtocol for known response codes
     */
    protected abstract int writeFinalCommand (char[] cmd, char[] arg) throws IOException;
    /**
     * Parses a EHLO capabilities line and informs the reporter about it
     * @param rspCode original response code (<0 if continuation)
     * @param rspLine response line to be parsed (including the response code)
     * @param reporter "callback" used to pass along the resulting reported capabilities of the
     * EHLO command. If null, then the report is not needed
     * @return 0 if successful
     */
    protected static final int reportEhloCapabilities (final int rspCode, final CharSequence rspLine, ESMTPCapabilityHandler reporter)
    {
        final int    rspLen=(null == rspLine) ? 0 : rspLine.length();
        if ((rspLen < 3) || (null == reporter))    // response must contain at least 3 digits of the response code
            return (-1);
        if (rspLen <= 4)    // if only ' ' or '-' follows the code, then nothing to report
        {
            // continuations MUST have at least 4 characters
            if ((rspCode < 0) && (rspLen < 4))
                return (-2);
            return 0;
        }

        final ParsableString    ps=new ParsableString(rspLine, 4, rspLen-4);
        final int                startIndex=ps.getStartIndex(), maxIndex=ps.getMaxIndex();

        // we limit ourselves to ~32K capabilities per line to avoid virtual loops
        for (int    curPos=ps.findNonEmptyDataStart(), capIndex=0; capIndex < Short.MAX_VALUE; capIndex++)
        {
            // check if exhausted data
            if ((curPos < startIndex) || (curPos >= maxIndex))
                return 0;

            final int    nextPos=ps.findNonEmptyDataEnd(curPos+1);
            if (nextPos <= curPos)    // should not happen
                return (-3);

            final String    cap=ps.substring(curPos, nextPos);
            final int        nErr=reporter.handleCapability(cap);
            if (nErr != 0)
                return nErr;

            curPos = ps.findNonEmptyDataStart(nextPos);
        }

        // this location is reached if terminated "infinite" loop
        return (-4);
    }
    /**
     * Handles the EHLO responses and calls the reporter (Note: assumes command has been
     * issued and ONLY responses are expected
     * @param reporter "callback" used to pass along the resulting reported capabilities of the
     * EHLO command. If null, then the report is not needed
     * @return SMTP response code
     * @throws IOException if network errors
     */
    protected abstract SMTPResponse reportEhloCapabilities (ESMTPCapabilityHandler reporter) throws IOException;
    /*
     * @see net.community.chest.net.proto.text.smtp.SMTPAccessor#ehlo(java.lang.String, net.community.chest.net.proto.text.smtp.ESMTPCapabilityHandler)
     */
    @Override
    public SMTPResponse ehlo (String dmn, ESMTPCapabilityHandler reporter) throws IOException
    {
        final String hloDomain=resolveGreetingDomain(dmn);
        if ((null == hloDomain) || (hloDomain.length() <= 0))
            throw new IOException("No EHLO(+) local host information available");

        final int    written=writeFinalCommand(SMTPProtocol.SMTPEhloCmdChars, hloDomain.toCharArray());
        if (written <= 0)
            throw new IOException("Cannot build write final EHLO command");

        if (null == reporter)
            return getFinalResponse();

        return reportEhloCapabilities(reporter);
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.SMTPAccessor#ehlo(java.lang.String)
     */
    @Override
    public SMTPResponse ehlo (String dmn) throws IOException
    {
        return ehlo(dmn, null);
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.SMTPAccessor#capabilities(java.lang.String)
     */
    @Override
    public SMTPExtendedHeloResponse capabilities (String dmn) throws IOException
    {
        final SMTPExtendedHeloResponse    reporter=new SMTPExtendedHeloResponse();
        final SMTPResponse                rsp=ehlo(dmn, reporter);
        reporter.setResponseAndLine(rsp.getRspCode(), rsp.getResponseLine());
        return reporter;
    }
    /**
     * Sends a parameter required for AUTH-LOGIN
     * @param param parameter to be sent (username/password)
     * @return SMTP response code
     * @throws IOException if network errors
     * @see #authLogin(String, String)
     */
    protected SMTPResponse sendAuthLoginParam (String param) throws IOException
    {
        if ((null == param) || (param.length() <= 0))
            throw new IOException("Null/empty AUTH-LOGIN parameter");

        final String    paramEnc=Base64.encode(param);
        if ((null == paramEnc) || (paramEnc.length() <= 0))
            throw new IOException("Null/empty AUTH-LOGIN parameter");

        return sendFinalCommand(paramEnc.toCharArray(), null);
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.SMTPAccessor#authLogin(java.lang.String, java.lang.String)
     */
    @Override
    public SMTPResponse authLogin (String username, String password) throws IOException
    {
        SMTPResponse    rsp=sendFinalCommand(SMTPProtocol.ESMTPAuthLoginCmdChars, null);
        if (rsp.getRspCode() != SMTPProtocol.ESMTP_E_AUTH_DATA)
            return rsp;

        if ((rsp=sendAuthLoginParam(username)).getRspCode() != SMTPProtocol.ESMTP_E_AUTH_DATA)
            return rsp;

        return sendAuthLoginParam(password);
    }
    /**
     * @return new <U>created</U> message digest object (MD5) to be used
     * @throws NoSuchAlgorithmException if unable to create such a digester
     */
    protected MessageDigest getMD5Digester () throws NoSuchAlgorithmException
    {
        return AuthDigester.getMD5DigesterInstance();
    }
    /**
     * Masks the exception as an {@link IOException} - if not already such
     * @param location text string to be added as reason/location
     * @param e exception object to be masked - may NOT be null
     * @return masked {@link IOException} - or original if already such
     */
    protected static final IOException getException (String location, Exception e)
    {
        if (e instanceof IOException)
            return (IOException) e;

        return new IOException(e.getClass().getName() + " " + location + ": " + e.getMessage());
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.SMTPAccessor#authCRAMMD5(java.lang.String, java.lang.String)
     */
    @Override
    public SMTPResponse authCRAMMD5 (String username, String password) throws IOException
    {
        final SMTPResponse    rsp=sendFinalCommand(SMTPProtocol.ESMTPAuthCRAMMD5CmdChars, null);
        if (rsp.getRspCode() != SMTPProtocol.ESMTP_E_AUTH_DATA)
            return rsp;

        // extract challenge value
        final String    chlng;
        {
            final String    rspLine=rsp.toString();
            final int        rlLen=(null == rspLine) /* should not happen */ ? 0 : rspLine.length();
            int                chStart=4;    // 3 digits for response code + one space
            // skip white spaces (not really required by protocol, but be lenient
            for ( ; chStart < rlLen; chStart++)
                if (rspLine.charAt(chStart) != ' ')
                    break;

            if (chStart >= rlLen)
                throw new IOException("Bad/illegal CRAM-MD5 challenge value: " + rspLine);

            chlng = Base64.decode(rspLine.substring(chStart));
        }

        final String    rspValue;
        try
        {
            rspValue = AuthDigester.getCRAMMD5ChallengeBaseResponse(chlng, username, password, getMD5Digester());
        }
        catch(Exception nsae)
        {
            throw getException("while get CRAM-MD5 base response", nsae);
        }

        return sendAuthLoginParam(rspValue);
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.SMTPAccessor#authPlain(java.lang.String, java.lang.String)
     */
    @Override
    public SMTPResponse authPlain (String username, String password) throws IOException
    {
        final SMTPResponse    rsp=sendFinalCommand(SMTPProtocol.ESMTPAuthPlainCmdChars, null);
        if (rsp.getRspCode() != SMTPProtocol.ESMTP_E_AUTH_DATA)
            return rsp;

        final String    rspValue;
        try
        {
            rspValue = AuthDigester.getPlainChallengeBaseResponse(username, password);
        }
        catch(Exception nsae)
        {
            throw getException("while get PLAIN base response", nsae);
        }

        return sendAuthLoginParam(rspValue);
    }
    /**
     * Sends a command that takes an address as its argument (e.g. "MAIL FROM:", "RCPT TO:")
     * @param cmd command to be used
     * @param addr address argument (may be null/empty)
     * @return SMTP response code
     * @throws IOException if network errors
     * @see SMTPProtocol for known response codes
     */
    protected abstract SMTPResponse sendTargetCommand (final char[] cmd, final char[] addr /* may be null/empty */) throws IOException;
    /*
     * @see net.community.chest.net.proto.text.smtp.SMTPAccessor#mailFrom(java.lang.String)
     */
    @Override
    public SMTPResponse mailFrom (String sender) throws IOException
    {
        return sendTargetCommand(SMTPProtocol.SMTPMailFromCmdChars, ((null == sender) || (sender.length() <= 0)) ? null : sender.toCharArray());
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.SMTPAccessor#rcptTo(java.lang.String)
     */
    @Override
    public SMTPResponse rcptTo (String recip) throws IOException
    {
        if ((null == recip) || (recip.length() <= 0))
            throw new IOException("Bad/illegal recipient");

        return sendTargetCommand(SMTPProtocol.SMTPRcptToCmdChars, recip.toCharArray());
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.SMTPAccessor#startData()
     */
    @Override
    public SMTPResponse startData () throws IOException
    {
        return sendFinalCommand(SMTPProtocol.SMTPDataCmdChars, null);
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.SMTPAccessor#endData(boolean)
     */
    @Override
    public SMTPResponse endData (boolean addCRLF) throws IOException
    {
        final char[]    finalCmd=new char[EOLStyle.CRLF.length()+EOM_SIGNAL.length];
        int                cmdLen=0;

        if (addCRLF)
        {
            final char[]    crlfChars=EOLStyle.CRLF.getStyleChars();
            for (int    index=0; index < crlfChars.length; index++, cmdLen++)
                finalCmd[cmdLen] = crlfChars[index];
        }

        // "append" the EOM
        for (int    index=0; index < EOM_SIGNAL.length; index++, cmdLen++)
            finalCmd[cmdLen] = EOM_SIGNAL[index];

        // allow for greater timeout on response
        final int    curTimeout=getReadTimeout();
        if (curTimeout <= 0)
            throw new IOException("Bad/Illegal timeout value to prolong: " + curTimeout);
        setReadTimeout(curTimeout * DATA_END_TIMEOUT_FACTOR);

        boolean    okToThrow=true;
        try
        {
            // make the EOM look like a "command" with a response
            return sendFinalCommand(finalCmd, 0, cmdLen);
        }
        catch(IOException ioe)
        {
            // do not throw another I/O exception if unable to restore the original timeout
            okToThrow = false;
            throw ioe;
        }
        finally
        {
            // restore original timeout
            try
            {
                setReadTimeout(curTimeout);
            }
            catch(IOException e2)
            {
                if (okToThrow)
                    throw e2;
            }
        }
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.SMTPAccessor#asOutputStream(boolean)
     */
    @Override
    public OutputStream asOutputStream (final boolean autoClose) throws IOException
    {
        if (!isOpen())
            throw new IOException("No current SMTP connection to mask as output stream");

        return new SMTPOutputStream(this, autoClose);
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.SMTPAccessor#reset()
     */
    @Override
    public SMTPResponse reset () throws IOException
    {
        return sendFinalCommand(SMTPProtocol.SMTPRsetCmdChars, null);
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.SMTPAccessor#quit()
     */
    @Override
    public SMTPResponse quit () throws IOException
    {
        try
        {
            return sendFinalCommand(SMTPProtocol.SMTPQuitCmdChars, null);
        }
        finally
        {
            try
            {
                close();
            }
            catch(IOException ioe)
            {
                // ignore closure error
            }
        }
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.SMTPAccessor#doDataHandshake(java.lang.String, java.lang.String[])
     */
    @Override
    public SMTPResponse doDataHandshake (String sender, String... recips) throws IOException
    {
        SMTPResponse    rsp=mailFrom(sender);
        int                nErr=rsp.getRspCode();
        if (nErr != SMTPProtocol.SMTP_E_ACTION_OK)
            return rsp;

        final int    numRecips=(null == recips) ? 0 : recips.length;
        for (int    rIndex=0; rIndex < numRecips; rIndex++)
        {
            final String    recip=recips[rIndex];
            if ((null == recip) || (recip.length() <= 0))
                continue;    // should not happen (but tolerate it)

            rsp = rcptTo(recip);
            if ((nErr=rsp.getRspCode()) != SMTPProtocol.SMTP_E_ACTION_OK)
                return rsp;
        }

        return startData();
    }
    /*
     * @see net.community.chest.net.proto.text.smtp.SMTPAccessor#connectAndDoDataHandshake(java.lang.String, int, java.lang.String, java.lang.String[])
     */
    @Override
    public SMTPResponse connectAndDoDataHandshake (String server, int port, String sender, String... recips) throws IOException
    {
        connect(server, (port <= 0) ? getDefaultPort() : port);

        final SMTPResponse    rsp=helo(null);
        final int            nErr=rsp.getRspCode();
        if (nErr != SMTPProtocol.SMTP_E_ACTION_OK)
            return rsp;

        return doDataHandshake(sender, recips);
    }
    /* make sure session is closed
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize () throws Throwable
    {
        close();
        super.finalize();
    }
    /**
     * Reads from the connection (assumed to be to an SMTP server) until a
     * "final" response is received (i.e., one where there is no '-' following
     * the response code)
     * @param conn connection to read from - may NOT be null/closed
     * @param rsp (modifiable) response object to be initialized with the
     * parsed response from the server
     * @return final response
     * @throws IOException unable to communicate with the server (or bad/illegal
     * responses found)
     */
    public static SMTPResponse getFinalResponse (TextNetConnection conn, SMTPResponse rsp) throws IOException
    {
        if ((null == conn) || (!conn.isOpen()) || (null == rsp))
            throw new IOException("No current SMTP connection/response to read from/update");

        // we limit ourselves to ~32KB continuation lines
        for (int    rspIndex=0; rspIndex < Short.MAX_VALUE; rspIndex++)
        {
            final String        rspLine=conn.readLine();
            final SMTPResponse    r=SMTPResponse.getFinalResponse(rspLine, rsp);
            if (null == r)
                throw new IOException("Bad/Illegal response line: " + rspLine);

            final int    rspCode=r.getRspCode();
            if (rspCode > 0)    // ignore continuation lines
                return r;
        }

        throw new IOException("Virtual responses infinite loop exit");
    }
    /**
     * Reads from the connection (assumed to be to an SMTP server) until a
     * "final" response is received (i.e., one where there is no '-' following
     * the response code)
     * @param conn connection to read from - may NOT be null/closed
     * @return final response
     * @throws IOException unable to communicate with the server (or bad/illegal
     * responses found)
     */
    public static SMTPResponse getFinalResponse (TextNetConnection conn) throws IOException
    {
        return getFinalResponse(conn, new SMTPResponse());
    }

}
