package net.community.chest.net.proto.text.pop3;

import java.io.IOException;
import java.io.StreamCorruptedException;

import net.community.chest.net.BufferedTextSocket;
import net.community.chest.net.TextNetConnection;
import net.community.chest.net.proto.text.NetServerWelcomeLine;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>POP3 accessor implementation</P>
 * @author Lyor G.
 * @since Sep 19, 2007 12:06:41 PM
 */
public class POP3Session extends AbstractPOP3AccessorHelper {
    public POP3Session ()
    {
        super();
    }
    /*
     * @see net.community.chest.net.proto.text.TextProtocolNetConnection#connect(java.lang.String, int, net.community.chest.net.proto.text.NetServerWelcomeLine)
     */
    @Override
    public void connect (String host, int port, NetServerWelcomeLine wl) throws IOException
    {
        final TextNetConnection    conn=getTextNetConnection();
        if (conn != null)
            throw new StreamCorruptedException("Already connected");

        setTextNetConnection(new BufferedTextSocket());
        super.connect(host, port);

        final String    wlString=readLine();
        // make sure this is either "+OK" or "-ERR"
        final int        rspErr=POP3Response.isOKResponse(wlString);
        if (rspErr < 0)
        {
            try
            {
                close();
            }
            catch(IOException e)
            {
                // ignored
            }
            throw new IOException("Bad/illegal welcome line: " + wlString);
        }

        if (wl != null)
            wl.setLine(wlString);
    }
}
