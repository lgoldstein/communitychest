/*
 *
 */
package net.community.chest.apache.ant;

import org.apache.tools.ant.Project;

import net.community.chest.resources.AbstractPropertiesResolver;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 9, 2009 11:44:56 AM
 */
public class ProjectPropertiesResolver extends AbstractPropertiesResolver {
    private final Project    _p;
    public final Project getProject ()
    {
        return _p;
    }

    public ProjectPropertiesResolver (final Project p)
    {
        if (null == (_p=p))
            throw new IllegalArgumentException("No " + Project.class.getSimpleName() + " instance provided");
    }
    /*
     * @see net.community.chest.resources.PropertyAccessor#getProperty(java.lang.Object)
     */
    @Override
    public String getProperty (final String key)
    {
        if ((null == key) || (key.length() <= 0))
            return null;

        final Project    p=getProject();
        if (null == p)
            return null;

        return p.getProperty(key);
    }
}
