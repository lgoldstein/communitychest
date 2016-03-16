package net.community.chest.net.proto.text.smtp.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;

import net.community.chest.net.dns.DNSAccess;
import net.community.chest.net.proto.text.NetServerWelcomeLine;
import net.community.chest.net.proto.text.smtp.SMTPAccessor;
import net.community.chest.net.proto.text.smtp.SMTPExtendedHeloResponse;
import net.community.chest.net.proto.text.smtp.SMTPProtocol;
import net.community.chest.net.proto.text.smtp.SMTPResponse;
import net.community.chest.net.proto.text.smtp.SMTPServerIdentityAnalyzer;
import net.community.chest.net.proto.text.smtp.SMTPSession;
import net.community.chest.test.TestBase;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 20, 2007 8:38:07 AM
 */
public class SMTPTester extends TestBase {

    private static final int testSMTPAccess (final PrintStream out, final BufferedReader in, final SMTPAccessor sess) throws IOException
    {
        for ( ; ; )
        {
            final String    ans=getval(out, in, "(M)ail-From/(R)cpt To/(D)ata/R(e)set/(Q)uit");
            if ((null == ans) || (ans.length() <= 0))
                continue;

            final char        op=Character.toUpperCase(ans.charAt(0));
            final String    addr;
            if (('M' == op) || ('R' == op))
                addr = getval(out, in, "target address (ENTER=empty)");
            else
                addr = null;

            final SMTPResponse    rsp;
            final long            cmdStart=System.currentTimeMillis();
            switch(op)
            {
                case 'M'    :
                    rsp = sess.mailFrom(addr);
                    break;

                case 'R'    :
                    rsp = sess.rcptTo(addr);
                    break;

                case 'Q'    :
                    rsp = sess.quit();
                    break;

                case 'E'    :
                    rsp = sess.reset();
                    break;

                default        :
                    rsp = null;
            }

            if (rsp != null)
            {
                final long    cmdEnd=System.currentTimeMillis(), cmdDuration=cmdEnd - cmdStart;
                if (rsp.isOKResponse())
                {
                    out.println("Command executed in " + cmdDuration + " msec.: " + rsp);

                    switch(op)
                    {
                        case 'Q'    :
                            return 0;

                        default        : // ignored
                    }
                }
                else
                    System.err.println("Command failed after " + cmdDuration + " msec.: " + rsp);
            }
        }
    }

    private static final int testSMTPAccess (final PrintStream out, final BufferedReader in,
                                             final String url, final String mode)
    {
        final int        pp=url.lastIndexOf(':');
        final String    host=(pp < 0) ? url : url.substring(0, pp);
        final int        port=(pp < 0) ? SMTPProtocol.IPPORT_SMTP : Integer.parseInt(url.substring(pp+1));
        final NetServerWelcomeLine    wl=new NetServerWelcomeLine();
        for ( ; ; )
        {
            final String    ans=getval(out, in, "(re-)run test ([y]/n)");
            if ((ans != null) && (ans.length() > 0) && (Character.toUpperCase(ans.charAt(0)) != 'Y'))
                break;

            final SMTPSession    sess=new SMTPSession();
            try
            {
                sess.setReadTimeout(30 * 1000);
                {
                    final boolean    useMX="MX".equalsIgnoreCase(mode);
                    final DNSAccess    nsa;
                    if (useMX)
                    {
                        for ( ; ; )
                        {
                            final String    dh=getval(out, in, "DNS Host (or Quit)");
                            if (isQuit(dh))
                            {
                                nsa = null;
                                break;
                            }

                            if ((dh != null) && (dh.length() > 0))
                            {
                                nsa = new DNSAccess();
                                nsa.setServer(dh);
                                break;
                            }
                        }

                        if (null == nsa)
                            continue;
                    }
                    else
                        nsa = null;

                    final long    cStart=System.currentTimeMillis();
                    if (nsa != null)
                        sess.mxConnect(nsa, url, wl);
                    else
                        sess.connect(host, port, wl);
                    final long    cEnd=System.currentTimeMillis(), cDuration=cEnd - cStart;

                    if (nsa != null)
                    {
                        out.println("Connected to " + url + " MX server in " + cDuration + " msec.: " + wl);
                        out.println("\tRemote host=" + sess.getRemoteHostName() + "[" + sess.getRemoteAddress() + "] on port " + sess.getRemotePort());
                    }
                    else
                        out.println("Connected to " + host + " on port " + port + " in " + cDuration + " msec.: " + wl);
                }

                {
                    final Map.Entry<String,String>    ident=
                        SMTPServerIdentityAnalyzer.DEFAULT.getServerIdentity(wl.getLine());
                    if (null == ident)
                        System.err.println("Failed to identify server");
                    else
                        out.println("\tType=" + ident.getKey() + "/Version=" + ident.getValue());
                }

                {
                    final String        ht=getval(out, in, "[E]hlo/(H)lo");
                    final boolean        useEhlo=(null == ht) || (ht.length() <= 0) || ('E' == Character.toUpperCase(ht.charAt(0)));
                    final long            hStart=System.currentTimeMillis();
                    final SMTPResponse    rsp=useEhlo ? sess.capabilities(null) : sess.helo(null);
                    final long            hEnd=System.currentTimeMillis(), hDuration=hEnd - hStart;
                    if (rsp.isOKResponse())
                    {
                        out.println("HELO/EHLO succeeded in " + hDuration + " msec.: " + rsp);
                        if (useEhlo)
                        {
                            final SMTPExtendedHeloResponse    crs=(SMTPExtendedHeloResponse) rsp;
                            final Collection<String>        caps=crs.getCapabilities();
                            if ((caps != null) && (caps.size() > 0))
                            {
                                for (final String c : caps)
                                    out.println("\t" + c);
                            }
                        }
                    }
                    else
                    {
                        System.err.println("HELO/EHLO failed in " + hDuration + " msec.: " + rsp);
                        continue;
                    }
                }

                testSMTPAccess(out, in, sess);
            }
            catch(IOException ce)
            {
                System.err.println(ce.getClass().getName() + " on handle session: " + ce.getMessage());
            }
            finally
            {
                try
                {
                    sess.close();
                }
                catch(IOException ce)
                {
                    System.err.println(ce.getClass().getName() + " on close session: " + ce.getMessage());
                }
            }
        }

        return 0;
    }

    // arg[0]=server
    private static final int testSMTPAccess (final PrintStream out, final BufferedReader in, final String[] args)
    {
        final String[]    prompts={ "Server", "Mode(MX/PLAIN)" },
                         tpa=resolveTestParameters(out, in, args, prompts);
        if ((null == tpa) || (tpa.length < prompts.length))
            return (-1);

        return testSMTPAccess(out, in, tpa[0], tpa[1]);
    }

    //////////////////////////////////////////////////////////////////////////

    public static final void main (final String args[])
    {
        final BufferedReader    in=getStdin();
        final int                nErr=testSMTPAccess(System.out, in, args);
        if (nErr != 0)
            System.err.println("test failed (err=" + nErr + ")");
        else
            System.out.println("OK");
    }

}
