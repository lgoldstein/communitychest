/*
 *
 */
package net.community.chest.net.proto;

import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @param <T> Type of {@link ServiceDefinition} being compared
 * @author Lyor G.
 * @since Nov 12, 2009 12:34:14 PM
 */
public class ServiceDefinitionComparator<T extends ServiceDefinition> extends AbstractComparator<T> {
    /**
     *
     */
    private static final long serialVersionUID = 6840498614595934169L;

    public ServiceDefinitionComparator (Class<T> valsClass, boolean reverseMatch)
            throws IllegalArgumentException
    {
        super(valsClass, reverseMatch);
    }
    /*
     * @see net.community.chest.util.compare.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compareValues (T v1, T v2)
    {
        // push NULL to end
        if (null == v1)
            return (null == v2) ? 0 : (+1);
        else if (null == v2)
            return (-1);
        else
            return v1.compareTo(v2);
    }

    public static final ServiceDefinitionComparator<ServiceDefinition>    ASCENDING=
        new ServiceDefinitionComparator<ServiceDefinition>(ServiceDefinition.class, false),
                                                                        DESCENDING=
        new ServiceDefinitionComparator<ServiceDefinition>(ServiceDefinition.class, true);
}
