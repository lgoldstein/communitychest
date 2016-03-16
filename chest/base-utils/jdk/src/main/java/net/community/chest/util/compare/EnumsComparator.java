package net.community.chest.util.compare;

import net.community.chest.lang.EnumUtil;

/**
 * Copyright 2007 as per GPLv2
 *
 * Useful base class for comparing {@link Enum} values
 *
 * @param <E> Type of compared {@link Enum} value
 * @author Lyor G.
 * @since Jul 9, 2007 2:58:36 PM
 */
public class EnumsComparator<E extends Enum<E>> extends AbstractComparator<E> {
    /**
     *
     */
    private static final long serialVersionUID = -7984399463474071120L;
    public EnumsComparator (final Class<E> valsClass, final boolean ascending)
    {
        super(valsClass, !ascending);
    }
    /*
     * @see net.community.chest.util.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compareValues (E o1, E o2)
    {
        return EnumUtil.compareValues(o1, o2);
    }
}
