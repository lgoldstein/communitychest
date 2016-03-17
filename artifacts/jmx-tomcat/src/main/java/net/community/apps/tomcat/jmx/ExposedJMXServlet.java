
package net.community.apps.tomcat.jmx;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import net.community.chest.web.servlet.jmx.DirectJMXServlet;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Need to expose it as a <I>.class</I> file so that the Tomcat {@link ClassLoader}
 * will be able to easily locate it</P>
 *
 * @author Lyor G.
 * @since Jul 29, 2008 10:19:32 AM
 */
public class ExposedJMXServlet extends DirectJMXServlet {
    private static final long serialVersionUID = -5590332959359022483L;

    public ExposedJMXServlet ()
    {
        super();
    }
    /*
     * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
     */
    @Override
    public void init (ServletConfig config) throws ServletException
    {
        super.init(config);

        if (config != null)
            log("init(" + config.getServletName() + ")");
        else
            log("init(" + ServletConfig.class.getSimpleName() + ")", new NullPointerException("No configuration provided"));
    }
}
