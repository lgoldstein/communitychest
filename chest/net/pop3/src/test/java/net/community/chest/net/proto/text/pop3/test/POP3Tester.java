package net.community.chest.net.proto.text.pop3.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;

import net.community.chest.net.proto.text.NetServerWelcomeLine;
import net.community.chest.net.proto.text.pop3.POP3Accessor;
import net.community.chest.net.proto.text.pop3.POP3MessageInfo;
import net.community.chest.net.proto.text.pop3.POP3MessageInfoResponse;
import net.community.chest.net.proto.text.pop3.POP3MsgDataHandler;
import net.community.chest.net.proto.text.pop3.POP3Protocol;
import net.community.chest.net.proto.text.pop3.POP3Response;
import net.community.chest.net.proto.text.pop3.POP3ServerIdentityAnalyzer;
import net.community.chest.net.proto.text.pop3.POP3Session;
import net.community.chest.net.proto.text.pop3.POP3StatusResponse;
import net.community.chest.test.TestBase;
import net.community.chest.util.map.IntegersMap;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 19, 2007 12:15:11 PM
 */
public class POP3Tester extends TestBase {
    public static class NullPOP3MsgDataHandler implements POP3MsgDataHandler {
        private final PrintStream    _out;
        public NullPOP3MsgDataHandler (PrintStream out)
        {
            _out = out;
        }
        /*
         * @see net.community.chest.net.proto.text.pop3.POP3MsgDataHandler#handleMsgData(int, char[], int, int)
         */
        @Override
        public int handleMsgData (int msgNum, char[] data, int startPos, int maxLen)
        {
            if (_out != null)
                _out.println("\thandleMsgData(" + msgNum + ")[" + startPos + "-" + (startPos + maxLen) + "]");
            return 0;
        }

        /*
         * @see net.community.chest.net.proto.text.pop3.POP3MsgDataHandler#handleMsgDataStage(int, boolean)
         */
        @Override
        public int handleMsgDataStage (int msgNum, boolean starting)
        {
            if (_out != null)
                _out.println("\thandleMsgDataStage(" + msgNum + ")[starting=" + starting + "]");
            return 0;
        }
    }

    private static final int testPOP3Access (final PrintStream out, final BufferedReader in, final POP3Accessor sess) throws IOException
    {
        for (POP3MsgDataHandler    hndlr=null; ; )
        {
            final String    ans=getval(out, in, "(S)tatus/(L)ist/(U)idl/(R)etrieve/(T)op/(D)elete/R(e)set/(Q)uit");
            if ((null == ans) || (ans.length() <= 0))
                continue;

            final char    op=Character.toUpperCase(ans.charAt(0));
            final int    msgRef;
            // check if message number required
            if (('R' == op)
             || ('T' == op))
            {
                final String    val=getval(out, in, "message number (ENTER=0/none)");
                if ((null == val) || (val.length() <= 0))
                    msgRef = 0;
                else
                    msgRef = Integer.parseInt(val);
            }
            else
                msgRef = (-1);

            final int    msgArg;
            if ('T' == op)
            {
                final String    val=getval(out, in, "# lines (ENTER=0)");
                if ((null == val) || (val.length() <= 0))
                    msgArg = 0;
                else
                    msgArg = Integer.parseInt(val);
            }
            else
                msgArg = (-1);

            final POP3Response    rsp;
            final long            cmdStart=System.currentTimeMillis();
            switch(op)
            {
                case 'S'    :
                    rsp = sess.stat();
                    break;

                case 'E'    :
                    rsp = sess.rset();
                    break;

                case 'Q'    :
                    rsp = sess.quit();
                    break;

                case 'L'    :
                case 'U'    :
                    rsp = sess.loadMessages();
                    break;

                case 'D'    :
                    rsp = sess.dele(msgRef);
                    break;

                case 'T'    :
                case 'R'    :
                    if (null == hndlr)
                        hndlr = new NullPOP3MsgDataHandler(out);
                    if (msgArg >= 0)
                        rsp = sess.top(msgRef, msgArg, hndlr);
                    else
                        rsp = sess.retr(msgRef, hndlr);
                    break;

                default        :    // ignored
                    rsp = null;
            }

            if (rsp != null)
            {
                final long    cmdEnd=System.currentTimeMillis(), cmdDuration=cmdEnd - cmdStart;
                if (rsp.isOKResponse())
                {
                    out.println("Command executed in " + cmdDuration + " msec.: " + rsp);
                    // display further information for some commands
                    switch(op)
                    {
                        case 'S'    :
                            {
                                final POP3StatusResponse    st=(POP3StatusResponse) rsp;
                                out.println("\tNum. Messages=" + st.getNumMsgs() + "\tTotal Size=" + st.getMboxSize());
                            }
                            break;

                        case 'L'    :
                        case 'U'    :
                            {
                                final POP3MessageInfoResponse        mi=(POP3MessageInfoResponse) rsp;
                                final IntegersMap<POP3MessageInfo>    mm=mi.getMessagesMap();
                                final Collection<POP3MessageInfo>    c=
                                        ((null == mm) || (mm.size() <= 0)) ? null : mm.values();
                                if ((c != null) && (c.size() > 0))
                                {
                                    for (final POP3MessageInfo i : c)
                                        out.println("\t" + i.getMsgNum() + "\t" + i.getMsgSize() + "\t" + i.getMsgUIDL());
                                }
                            }
                            break;

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

    private static final int testPOP3Access (final PrintStream out, final BufferedReader in,
                                             final String url, final String user, final String pass)
    {
        final int        pp=url.lastIndexOf(':');
        final String    host=(pp < 0) ? url : url.substring(0, pp);
        final int        port=(pp < 0) ? POP3Protocol.IPPORT_POP3 : Integer.parseInt(url.substring(pp+1));
        final NetServerWelcomeLine    wl=new NetServerWelcomeLine();
        for ( ; ; )
        {
            final String    ans=getval(out, in, "(re-)run test ([y]/n)");
            if ((ans != null) && (ans.length() > 0) && (Character.toUpperCase(ans.charAt(0)) != 'Y'))
                break;

            final POP3Accessor    sess=new POP3Session();
            try
            {
                sess.setReadTimeout(30 * 1000);

                {
                    final long    cStart=System.currentTimeMillis();
                    sess.connect(host, port, wl);
                    final long    cEnd=System.currentTimeMillis(), cDuration=cEnd - cStart;
                    out.println("Connected to " + host + " on port " + port + " in " + cDuration + " msec.: " + wl);
                }
                {
                    final Map.Entry<String,String>    ident=
                        POP3ServerIdentityAnalyzer.DEFAULT.getServerIdentity(wl.getLine());
                    if (null == ident)
                        System.err.println("Failed to identify server");
                    else
                        out.println("\tType=" + ident.getKey() + "/Version=" + ident.getValue());
                }

                {
                    final long    aStart=System.currentTimeMillis();
                    final POP3Response    rsp=sess.login(user, pass);
                    final long    aEnd=System.currentTimeMillis(), aDuration=aEnd - aStart;
                    if (!rsp.isOKResponse())
                    {
                        System.err.println("Authentication failed in " + aDuration + " msec.: " + rsp);
                        continue;
                    }
                    out.println("Authenticated in " + aDuration + " msec.: " + rsp);
                }

                testPOP3Access(out, in, sess);
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
    // arg[0]=server, arg[1]=username, arg[2]=password
    public static final int testPOP3Access (final PrintStream out, final BufferedReader in, final String[] args)
    {
        final String[]    prompts={ "Server", "Username", "Password" },
                         tpa=resolveTestParameters(out, in, args, prompts);
        if ((null == tpa) || (tpa.length < prompts.length))
            return (-1);

        return testPOP3Access(out, in, tpa[0], tpa[1], tpa[2]);
    }

    //////////////////////////////////////////////////////////////////////////

    public static final void main (final String args[])
    {
        final BufferedReader    in=getStdin();
        final int                nErr=testPOP3Access(System.out, in, args);
        if (nErr != 0)
            System.err.println("test failed (err=" + nErr + ")");
        else
            System.out.println("OK");
    }
}
