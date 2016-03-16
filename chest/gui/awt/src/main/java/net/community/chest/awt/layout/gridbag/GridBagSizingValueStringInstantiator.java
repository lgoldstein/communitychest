package net.community.chest.awt.layout.gridbag;

import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.NumberValueStringConstructor;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 4, 2007 2:09:48 PM
 */
public class GridBagSizingValueStringInstantiator extends NumberValueStringConstructor<Integer> {
    public GridBagSizingValueStringInstantiator ()
    {
        super(Integer.TYPE, Integer.class);
    }
    /*
     * @see net.community.chest.reflect.ValueStringConstructor#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (final Integer inst) throws Exception
    {
        if (null == inst)
            return null;

        final GridBagGridSizingType    szType=GridBagGridSizingType.fromSpecValue(inst.intValue());
        if (szType != null)
            return szType.toString();

        return super.convertInstance(inst);
    }
    /*
     * @see net.community.chest.reflect.ValueStringConstructor#newInstance(java.lang.String)
     */
    @Override
    public Integer newInstance (final String v) throws Exception
    {
        final String                s=StringUtil.getCleanStringValue(v);
        final GridBagGridSizingType    szType=GridBagGridSizingType.fromString(s);
        if (szType != null)    // check if one of the special sizing types
            return Integer.valueOf(szType.getSpecValue());

        return super.newInstance(s);
    }

    public static final GridBagSizingValueStringInstantiator    DEFAULT=new GridBagSizingValueStringInstantiator();
}
