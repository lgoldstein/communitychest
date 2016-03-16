/*
 *
 */
package net.community.chest.jfree.jfreechart.data.general.pie;

import java.util.Map;

import net.community.chest.convert.DoubleValueStringConstructor;
import net.community.chest.dom.DOMUtils;
import net.community.chest.jfree.jfreechart.data.general.AbstractDatasetReflectiveProxy;
import net.community.chest.util.map.MapEntryImpl;

import org.jfree.data.general.DefaultPieDataset;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <D> The type of {@link DefaultPieDataset} being reflected
 * @author Lyor G.
 * @since Feb 1, 2009 1:55:13 PM
 */
public class DefaultPieDatasetReflectiveProxy<D extends DefaultPieDataset> extends AbstractDatasetReflectiveProxy<D> {
    protected DefaultPieDatasetReflectiveProxy (Class<D> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public DefaultPieDatasetReflectiveProxy (Class<D> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }
    // XML sub-elements names
    public static final String    ITEM_ELEM_NAME="item",
                                    VALUE_ATTR="value";
    public boolean isItemElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, ITEM_ELEM_NAME);
    }
    // returns the added pair - null if none added
    public Map.Entry<Comparable<?>,Number> addItem (D src, Element elem) throws Exception
    {
        final String    name=(null == elem) ? null : elem.getAttribute(NAME_ATTR),
                        value=(null == elem) ? null : elem.getAttribute(VALUE_ATTR);
        if ((name != null) && (name.length() > 0))
        {
            final Double    d=
                ((null == value) || (value.length() <= 0)) ? Double.valueOf(0d) : DoubleValueStringConstructor.DEFAULT.newInstance(value);
            final double    v=d.doubleValue();
            if (Double.isInfinite(v) || Double.isNaN(v))
                throw new IllegalArgumentException("addItem(" + name + ")[" + value + "] bad/illegal value");
            src.setValue(name, d);

            return new MapEntryImpl<Comparable<?>,Number>(name, d);
        }

        if ((value != null) && (value.length() > 0))
            throw new IllegalStateException("addItem(" + DOMUtils.toString(elem) + ") no category name provided");

        return null;
    }
    /*
     * @see net.community.chest.dom.transform.AbstractReflectiveProxy#fromXmlChild(java.lang.Object, org.w3c.dom.Element)
     */
    @Override
    public D fromXmlChild (D src, Element elem) throws Exception
    {
        final String    tagName=(null == elem) ? null : elem.getTagName();
        if (isItemElement(elem, tagName))
        {
            addItem(src, elem);
            return src;
        }

        return super.fromXmlChild(src, elem);
    }

    public static final DefaultPieDatasetReflectiveProxy<DefaultPieDataset>    DEFPIEDS=
        new DefaultPieDatasetReflectiveProxy<DefaultPieDataset>(DefaultPieDataset.class, true);
}
