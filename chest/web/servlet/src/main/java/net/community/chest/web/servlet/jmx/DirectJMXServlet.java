/**
 *
 */
package net.community.chest.web.servlet.jmx;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import net.community.chest.jmx.JMXUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Implements the {@link AbstractJMXServlet} abstract methods
 * by direct access to the local JMX</P>
 *
 * @author Lyor G.
 * @since Jul 28, 2008 2:38:50 PM
 */
public class DirectJMXServlet extends AbstractJMXServlet {

    /**
     *
     */
    private static final long serialVersionUID = -6442619837853631047L;
    private static final String getDerivedPropName (String name)
    {
        return DirectJMXServlet.class.getPackage().getName() + "." + name;
    }
    /**
     * Property used to specify domain of the {@link MBeanServer} to use as the
     * "local" one. If none specified, then first available is used. <B>Note:</B>
     * this is also the name of the server to create if no match found (if not
     * specified then some internal default is used)
     */
    public static final String    MBEAN_SERVER_DOMAIN_PROP_NAME=getDerivedPropName("domain");
    /**
     * Cached {@link MBeanServer} instance - lazy initialized by first call
     * to {@link #getLocalMBeanServer()}
     */
    private static MBeanServer    _lclServer    /* =null */;
    /**
     * @return The {@link MBeanServer} to be used as the "local" one
     * @throws InstanceNotFoundException if cannot create/find one
     */
    public static final synchronized MBeanServer getLocalMBeanServer ()
        throws InstanceNotFoundException
    {
        if (null == _lclServer)
        {
            // look it up and if does not exist then create it...
            final String    defDomain=System.getProperty(MBEAN_SERVER_DOMAIN_PROP_NAME);
            if (null == (_lclServer=JMXUtils.getLocalMBeanServer(defDomain)))
                throw new InstanceNotFoundException("getLocalMBeanServer(" + defDomain + ") no instance generated");
        }

        return _lclServer;
    }

    public DirectJMXServlet ()
    {
        super();
    }
    /*
     * @see net.community.chest.web.servlet.jmx.AbstractJMXServlet#getDefaultMBeanServer()
     */
    @Override
    public MBeanServerConnection getDefaultMBeanServer () throws Exception
    {
        return getLocalMBeanServer();
    }
}
