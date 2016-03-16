/*
 *
 */
package net.community.chest.jfree.jfreechart.axis.category;

import java.util.NoSuchElementException;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

import org.jfree.chart.axis.CategoryAnchor;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 5, 2009 4:01:53 PM
 */
public class CategoryAnchorValueStringInstantiator
         extends AbstractXmlValueStringInstantiator<CategoryAnchor> {
    public CategoryAnchorValueStringInstantiator ()
    {
        super(CategoryAnchor.class);
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (CategoryAnchor inst) throws Exception
    {
        if (null == inst)
            return null;

        final CategoryAnchorValue    o=CategoryAnchorValue.fromAnchor(inst);
        if (null == o)
            throw new NoSuchElementException("convertInstance(" + inst + ") unknown value");

        return o.toString();
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
     */
    @Override
    public CategoryAnchor newInstance (String v) throws Exception
    {
        final String    s=StringUtil.getCleanStringValue(v);
        if ((null == s) || (s.length() <= 0))
            return null;

        final CategoryAnchorValue    o=CategoryAnchorValue.fromString(s);
        if (null == o)
            throw new NoSuchElementException("newInstance(" + s + ") unknown value");

        return o.getAnchor();
    }

    public static final CategoryAnchorValueStringInstantiator    DEFAULT=new CategoryAnchorValueStringInstantiator();
}
