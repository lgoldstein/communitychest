/*
 *
 */
package net.community.chest.jinterop.core;

import org.jinterop.dcom.core.JIComServer;
import org.jinterop.dcom.core.JISession;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 19, 2009 11:07:37 AM
 */
public class JIComServerFieldsAccessor extends StubFieldsAccessor<JIComServer> {
    public JIComServerFieldsAccessor ()
    {
        super(JIComServer.class);
    }

    public static final String    SESSION_FIELD_NAME="session";
    public JISession getSession (JIComServer s) throws Exception
    {
        if (null == s)
            return null;

        return getCastFieldValue(s, SESSION_FIELD_NAME, JISession.class);
    }

    public static final JIComServerFieldsAccessor    DEFAULT=new JIComServerFieldsAccessor();
}
