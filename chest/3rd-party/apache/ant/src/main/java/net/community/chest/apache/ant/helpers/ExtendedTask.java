/*
 *
 */
package net.community.chest.apache.ant.helpers;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 9, 2009 11:00:36 AM
 */
public class ExtendedTask extends Task {
    public ExtendedTask ()
    {
        super();
    }

    private int _verbosity=Project.MSG_VERBOSE;
    protected int getVerbosity ()
    {
        return _verbosity;
    }

    protected void setVerbosity (int v)
    {
        _verbosity = v;
    }

    protected boolean isVerboseMode ()
    {
        return (getVerbosity() < Project.MSG_VERBOSE);
    }
}
