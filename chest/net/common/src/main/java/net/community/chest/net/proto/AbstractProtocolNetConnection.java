package net.community.chest.net.proto;

import java.io.IOException;

import net.community.chest.net.AbstractNetConnection;

/**
 * Copyright 2007 as per GPLv2
 *
 * Helper class for implementing {@link ProtocolNetConnection}
 * @author Lyor G.
 * @since Jul 4, 2007 9:10:42 AM
 */
public abstract class AbstractProtocolNetConnection extends AbstractNetConnection implements ProtocolNetConnection {
    protected AbstractProtocolNetConnection ()
    {
        super();
    }
    /*
     * @see net.community.chest.net.proto.ProtocolNetConnection#connect(java.lang.String)
     */
    @Override
    public void connect (String host) throws IOException
    {
        connect(host, getDefaultPort());
    }
}
