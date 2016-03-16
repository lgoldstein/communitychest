package net.community.chest.lang;

import java.io.Serializable;
import java.util.Comparator;


/**
 * Copyright 2007 as per GPLv2
 *
 * Compares 2 {@link Class}-es based on their names
 * @author Lyor G.
 * @since Jul 11, 2007 10:04:35 AM
 */
public class ClassNameComparator implements Comparator<Class<?>>, Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -6789480098264730679L;

    public ClassNameComparator ()
    {
        super();
    }
    /*
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare (Class<?> o1, Class<?> o2)
    {
        final String    n1=(null == o1) ? null : o1.getName(),
                        n2=(null == o2) ? null : o2.getName();
        return StringUtil.compareDataStrings(n1, n2, true);
    }

    public static final ClassNameComparator    DEFAULT=new ClassNameComparator();
}
