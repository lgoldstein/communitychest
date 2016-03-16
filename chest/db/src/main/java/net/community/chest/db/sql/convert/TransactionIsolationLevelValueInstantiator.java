/*
 *
 */
package net.community.chest.db.sql.convert;

import java.util.NoSuchElementException;

import net.community.chest.db.sql.TransactionIsolationLevel;
import net.community.chest.dom.AbstractXmlValueStringInstantiator;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 28, 2010 12:41:09 PM
 */
public class TransactionIsolationLevelValueInstantiator extends
        AbstractXmlValueStringInstantiator<Integer> {
    public TransactionIsolationLevelValueInstantiator ()
    {
        super(Integer.class);
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (Integer inst) throws Exception
    {
        if (null == inst)
            return null;

        final TransactionIsolationLevel    l=TransactionIsolationLevel.fromLevel(inst.intValue());
        if (null == l)
            throw new NoSuchElementException("convertInstance(" + inst + ") unknown transaction isolation level");

        return l.toString();
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
     */
    @Override
    public Integer newInstance (String s) throws Exception
    {
        if ((null == s) || (s.length() <= 0))
            return null;

        final TransactionIsolationLevel    l=TransactionIsolationLevel.fromString(s);
        if (null == l)
            throw new NoSuchElementException("newInstance(" + s + ") unknown transaction isolation level");

        return Integer.valueOf(l.getLevel());
    }

    public static final TransactionIsolationLevelValueInstantiator    DEFAULT=
        new TransactionIsolationLevelValueInstantiator();
}
