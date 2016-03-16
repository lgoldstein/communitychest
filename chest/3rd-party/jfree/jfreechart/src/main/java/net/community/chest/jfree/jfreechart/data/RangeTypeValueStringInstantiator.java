/*
 *
 */
package net.community.chest.jfree.jfreechart.data;

import java.util.NoSuchElementException;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

import org.jfree.data.RangeType;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 19, 2009 2:05:23 PM
 */
public class RangeTypeValueStringInstantiator extends AbstractXmlValueStringInstantiator<RangeType> {
    public RangeTypeValueStringInstantiator ()
    {
        super(RangeType.class);
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (RangeType inst) throws Exception
    {
        if (null == inst)
            return null;

        final RangeTypeEnum    t=RangeTypeEnum.fromRangeType(inst);
        if (null == t)
            throw new NoSuchElementException("convertInstance(" + inst + ") no match found");

        return t.toString();
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
     */
    @Override
    public RangeType newInstance (String vs) throws Exception
    {
        final String    s=StringUtil.getCleanStringValue(vs);
        if ((null == s) || (s.length() <= 0))
            return null;

        final RangeTypeEnum    t=RangeTypeEnum.fromString(s);
        if (null == t)
            throw new NoSuchElementException("newInstance(" + s + ") no match found");

        return t.getRangeType();
    }

    public static final RangeTypeValueStringInstantiator    DEFAULT=
        new RangeTypeValueStringInstantiator();
}
