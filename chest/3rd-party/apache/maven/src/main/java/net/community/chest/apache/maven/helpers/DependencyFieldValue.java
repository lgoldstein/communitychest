/*
 *
 */
package net.community.chest.apache.maven.helpers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 9, 2009 2:50:24 PM
 */
public enum DependencyFieldValue {
    VERSION {
            /*
             * @see net.community.chest.apache.maven.helpers.DependencyFieldValue#getValue(net.community.chest.apache.maven.helpers.BuildDependencyDetails)
             */
            @Override
            public String getValue (BuildDependencyDetails d)
            {
                return (null == d) ? null : d.getVersion();
            }
            /*
             * @see net.community.chest.apache.maven.helpers.DependencyFieldValue#setValue(net.community.chest.apache.maven.helpers.BuildDependencyDetails, java.lang.String)
             */
            @Override
            public void setValue (BuildDependencyDetails d, String v)
            {
                if (d != null)
                    d.setVersion(v);
            }
        },
    SCOPE {
            /*
             * @see net.community.chest.apache.maven.helpers.DependencyFieldValue#getValue(net.community.chest.apache.maven.helpers.BuildDependencyDetails)
             */
            @Override
            public String getValue (BuildDependencyDetails d)
            {
                return (null == d) ? null : d.getScope();
            }
            /*
             * @see net.community.chest.apache.maven.helpers.DependencyFieldValue#setValue(net.community.chest.apache.maven.helpers.BuildDependencyDetails, java.lang.String)
             */
            @Override
            public void setValue (BuildDependencyDetails d, String v)
            {
                if (d != null)
                    d.setScope(v);
            }
        },
    SYSTEMPATH {
            /*
             * @see net.community.chest.apache.maven.helpers.DependencyFieldValue#getValue(net.community.chest.apache.maven.helpers.BuildDependencyDetails)
             */
            @Override
            public String getValue (BuildDependencyDetails d)
            {
                return (null == d) ? null : d.getSystemPath();
            }
            /*
             * @see net.community.chest.apache.maven.helpers.DependencyFieldValue#setValue(net.community.chest.apache.maven.helpers.BuildDependencyDetails, java.lang.String)
             */
            @Override
            public void setValue (BuildDependencyDetails d, String v)
            {
                if (d != null)
                    d.setSystemPath(v);
            }
        };

    public abstract String getValue (BuildDependencyDetails d);

    public abstract void setValue (BuildDependencyDetails d, String v);

    public static final List<DependencyFieldValue>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final DependencyFieldValue fromString (final String name)
    {
        return CollectionsUtils.fromString(VALUES, name, false);
    }
}
