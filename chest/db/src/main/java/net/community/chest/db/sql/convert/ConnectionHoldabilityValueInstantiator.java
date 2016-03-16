/*
 *
 */
package net.community.chest.db.sql.convert;

import java.util.NoSuchElementException;

import net.community.chest.db.sql.ConnectionHoldability;
import net.community.chest.dom.AbstractXmlValueStringInstantiator;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 28, 2010 12:48:46 PM
 */
public class ConnectionHoldabilityValueInstantiator extends
        AbstractXmlValueStringInstantiator<Integer> {
    public ConnectionHoldabilityValueInstantiator ()
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

        final ConnectionHoldability    l=ConnectionHoldability.fromValue(inst.intValue());
        if (null == l)
            throw new NoSuchElementException("convertInstance(" + inst + ") unknown connection holdability");

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

        final ConnectionHoldability    l=ConnectionHoldability.fromString(s);
        if (null == l)
            throw new NoSuchElementException("newInstance(" + s + ") unknown connection holdability");

        return Integer.valueOf(l.getHoldability());
    }

    public static final ConnectionHoldabilityValueInstantiator DEFAULT=
        new ConnectionHoldabilityValueInstantiator();
}
