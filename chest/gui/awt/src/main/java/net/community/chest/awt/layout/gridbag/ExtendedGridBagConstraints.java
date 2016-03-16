package net.community.chest.awt.layout.gridbag;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import net.community.chest.CoVariantReturn;
import net.community.chest.awt.dom.converter.InsetsValueInstantiator;
import net.community.chest.convert.DoubleValueStringConstructor;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.impl.StandaloneElementImpl;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Adds some type-safe methods + XML capability to {@link GridBagConstraints}</P>
 *
 * @author Lyor G.
 * @since Aug 7, 2007 12:44:24 PM
 */
public class ExtendedGridBagConstraints extends GridBagConstraints
        implements XmlConvertible<ExtendedGridBagConstraints>, PubliclyCloneable<ExtendedGridBagConstraints> {
    /**
     *
     */
    private static final long serialVersionUID = -1524381856442509065L;
    public ExtendedGridBagConstraints (int gridX, int gridY, int gridWidth,
            int gridHeight, double weightX, double weightY, int anchorValue,
            int fillValue, Insets insetsValue, int ipadX, int ipadY)
    {
        super(gridX, gridY, gridWidth, gridHeight, weightX, weightY, anchorValue, fillValue, insetsValue, ipadX, ipadY);
    }

    public ExtendedGridBagConstraints ()
    {
        this(RELATIVE, RELATIVE, 1, 1, 0, 0, CENTER, NONE, InsetsValueInstantiator.NO_INSETS, 0, 0);
    }

    public int getGridX ()
    {
        return gridx;
    }

    public void setGridX (int v)
    {
        gridx = v;
    }

    public int getGridY ()
    {
        return gridy;
    }

    public void setGridY (int v)
    {
        gridy = v;
    }

    public int getGridWidth ()
    {
        return gridwidth;
    }

    public void setGridWidth (int v)
    {
        gridwidth = v;
    }

    public int getGridHeight ()
    {
        return gridheight;
    }

    public void setGridHeight (int v)
    {
        gridheight = v;
    }

    public double getWeightX ()
    {
        return weightx;
    }

    public void setWeightX (double v)
    {
        weightx = v;
    }

    public double getWeightY ()
    {
        return weighty;
    }

    public void setWeightY (double v)
    {
        weighty = v;
    }

    public Insets getInsets ()
    {
        return insets;
    }

    public void setInsets (Insets v)
    {
        insets = v;
    }

    public int getIPadX ()
    {
        return ipadx;
    }

    public void setIPadX (int v)
    {
        ipadx = v;
    }

    public int getIPadY ()
    {
        return ipady;
    }

    public void setIPadY (int v)
    {
        ipady = v;
    }

    public static final <C extends GridBagConstraints> C reset (C gbc)
    {
        if (null == gbc)
            return null;

        gbc.gridx = RELATIVE;
        gbc.gridy = RELATIVE;

        gbc.gridwidth = 1;
        gbc.gridheight = 1;

        gbc.weightx = 0.0d;
        gbc.weighty = 0.0d;

        gbc.anchor = CENTER;
        gbc.fill = NONE;
        gbc.insets = InsetsValueInstantiator.NO_INSETS;

        gbc.ipadx = 0;
        gbc.ipady = 0;

        return gbc;
    }
    /**
     * resets contents to same as in default constructor
     * @return The <code>this</code> instance
     */
    public ExtendedGridBagConstraints reset ()
    {
        setGridX(RELATIVE);
        setGridY(RELATIVE);

        setGridWidth(1);
        setGridHeight(1);

        setWeightX(0.0);
        setWeightY(0.0);

        setAnchorType(GridBagAnchorType.CENTER);
        setFillType(GridBagFillType.NONE);

        setInsets(InsetsValueInstantiator.NO_INSETS);

        setIPadX(0);
        setIPadY(0);

        return this;
    }

    public GridBagAnchorType getAnchorType ()
    {
        return GridBagAnchorType.fromAnchorValue(anchor);
    }
    // NOTE: ignored if null
    public void setAnchorType (final GridBagAnchorType ancType)
    {
        if (ancType != null)
            anchor = ancType.getAnchorValue();
    }

    public boolean isDefaultAnchorType ()
    {
        return GridBagAnchorType.CENTER.equals(getAnchorType());
    }

    public GridBagFillType getFillType ()
    {
        return GridBagFillType.fromFillValue(fill);
    }
    // NOTE: ignored if null
    public void setFillType (final GridBagFillType fillType)
    {
        if (fillType != null)
            fill = fillType.getFillValue();
    }

    public boolean isDefaultFillType ()
    {
        return GridBagFillType.NONE.equals(getFillType());
    }

    // returns null if positive numerical value set
    public GridBagGridSizingType getGridWithType ()
    {
        return GridBagGridSizingType.fromSpecValue(gridwidth);
    }

    public boolean isNumericalGridWidth ()
    {
        return (null == getGridWithType());
    }
    // NOTE: ignored if null
    public void setGridWidthType (final GridBagGridSizingType szType)
    {
        if (szType != null)
            gridwidth = szType.getSpecValue();
    }

    public boolean isDefaultGridWidth ()
    {
        return isNumericalGridWidth() && (1 == gridwidth);
    }
    // returns null if positive numerical value set
    public GridBagGridSizingType getGridHeightType ()
    {
        return GridBagGridSizingType.fromSpecValue(gridheight);
    }

    public boolean isNumericalGridHeight ()
    {
        return (null == getGridHeightType());
    }
    // NOTE: ignored if null
    public void setGridHeightType (final GridBagGridSizingType szType)
    {
        if (szType != null)
            setGridHeight(szType.getSpecValue());
    }

    public boolean isDefaultGridHeight ()
    {
        return isNumericalGridHeight() && (1 == getGridHeight());
    }

    public boolean isAbsoluteGridX ()
    {
        return (getGridX() >= 0);
    }

    public void setRelativeGridX ()
    {
        setGridX(RELATIVE);
    }

    public boolean isDefaultGridX ()
    {
        return (!isAbsoluteGridX());
    }

    public boolean isAbsoluteGridY ()
    {
        return (getGridY() >= 0);
    }

    public void setRelativeGridY ()
    {
        setGridY(RELATIVE);
    }

    public boolean isDefaultGridY ()
    {
        return (!isAbsoluteGridY());
    }

    public boolean isDefaultWeightX ()
    {
        return (0.0 == getWeightX());
    }

    public boolean isDefaultWeightY ()
    {
        return (0.0 == getWeightY());
    }

    public boolean isDefaultInsets ()
    {
        return InsetsValueInstantiator.NO_INSETS.equals(getInsets());
    }

    public boolean isDefaultIPadX ()
    {
        return (0 == getIPadX());
    }

    public boolean isDefaultIPadY ()
    {
        return (0 == getIPadY());
    }
    /**
     * @param elem XML {@link Element} from which to retrieve the data
     * @param attrName attribute containing the <I>gridx/gridy</U> value
     * @return read value - null if no element/attribute
     * @throws NumberFormatException if bad format of data (e.g., non-numerical and
     * not the special {@link GridBagXYValueStringInstantiator#RELATIVE_VALUE} string)
     */
    public static final Integer getGridXYValue (final Element elem, final String attrName) throws NumberFormatException
    {
        return ((null == elem) || (null == attrName) || (attrName.length() <= 0)) ? null
                : GridBagXYValueStringInstantiator.getGridXYValue(elem.getAttribute(attrName));
    }

    public static final String    GRIDX_ATTR="gridx";
    public Integer setGridX (final Element elem) throws Exception
    {
        final Integer    val=getGridXYValue(elem, GRIDX_ATTR);
        if (val != null)
            setGridX(val.intValue());

        return val;
    }

    public static final String    GRIDY_ATTR="gridy";
    public Integer setGridY (final Element elem) throws Exception
    {
        final Integer    val=getGridXYValue(elem, GRIDY_ATTR);
        if (val != null)
            setGridY(val.intValue());

        return val;
    }
    /**
     * @param attrVal A {@link String} containing the <I>gridwidth/gridheigth</U>
     * value either as an integer or a special {@link GridBagGridSizingType} value
     * @return The converted value - null if null/empty value
     * @throws Exception If failed to convert the data
     */
    public static final Integer getGridSizingValue (final String attrVal) throws Exception
    {
        return GridBagSizingValueStringInstantiator.DEFAULT.newInstance(attrVal);
    }
    /**
     * @param elem XML {@link Element} from which to retrieve the data
     * @param attrName attribute containing the <I>gridwidth/gridheigth</U> value
     * @return read value - null if no element/attribute
     * @throws Exception if bad format of data (e.g., non-numerical and not
     * one of the {@link GridBagGridSizingType} names)
     */
    public static final Integer getGridSizingValue (final Element elem, final String attrName) throws Exception
    {
        return ((null == elem) || (null == attrName) || (attrName.length() <= 0)) ? null : getGridSizingValue(elem.getAttribute(attrName));
    }

    public static final String    GRIDWIDTH_ATTR="gridwidth";
    public Integer setGridWidth (final Element elem) throws Exception
    {
        final Integer    val=getGridSizingValue(elem, GRIDWIDTH_ATTR);
        if (val != null)
            setGridWidth(val.intValue());
        return val;
    }

    public static final String    GRIDHEIGHT_ATTR="gridheight";
    public Integer setGridHeight (final Element elem) throws Exception
    {
        final Integer    val=getGridSizingValue(elem, GRIDHEIGHT_ATTR);
        if (val != null)
            setGridHeight(val.intValue());
        return val;
    }

    public static final GridBagFillType getGridBagFillType (final String attrVal) throws Exception
    {
        return GridBagFillValueStringInstantiator.DEFAULT.newInstance(attrVal);
    }

    public static final GridBagFillType getGridBagFillType (final Element elem, final String attrName) throws Exception
    {
        return ((null == elem) || (null == attrName) || (attrName.length() <= 0)) ? null : getGridBagFillType(elem.getAttribute(attrName));
    }

    public static final String    FILL_ATTR="fill";
    public GridBagFillType setFill (final Element elem) throws Exception
    {
        final GridBagFillType    val=getGridBagFillType(elem, FILL_ATTR);
        if (val != null)
            setFillType(val);

        return val;
    }

    public static final Insets getGridBagInsets (final Element elem, final String attrName) throws Exception
    {
        final String    attrVal=
            ((null == elem) || (null == attrName) || (attrName.length() <= 0)) ? null : elem.getAttribute(attrName);
        return  InsetsValueInstantiator.DEFAULT.newInstance(attrVal);
    }

    public static final String    INSETS_ATTR="insets";
    public Insets setInsets (final Element elem) throws Exception
    {
        final Insets    val=getGridBagInsets(elem, INSETS_ATTR);
        if (val != null)
            setInsets(val);

        return val;
    }

    public static final GridBagAnchorType getGridBagAnchorType (final Element elem, final String attrName) throws Exception
    {
        final String    attrVal=
            ((null == elem) || (null == attrName) || (attrName.length() <= 0)) ? null : elem.getAttribute(attrName);
        return GridBagAnchorValueStringInstantiator.DEFAULT.newInstance(attrVal);
    }

    public static final String    ANCHOR_ATTR="anchor";
    public GridBagAnchorType setAnchor (final Element elem) throws Exception
    {
        final GridBagAnchorType    ancType=getGridBagAnchorType(elem, ANCHOR_ATTR);
        if (ancType != null)
            setAnchorType(ancType);

        return ancType;
    }

    public static final Integer getPadXYValue (final String attrVal) throws Exception
    {
        if ((null == attrVal) || (attrVal.length() <= 0))
            return null;

        final ValueStringInstantiator<Integer>    vsi=ClassUtil.getAtomicStringInstantiator(Integer.class);
        return vsi.newInstance(attrVal);
    }

    public static final Integer getPadXYValue (final Element elem, final String attrName) throws Exception
    {
        return ((null == elem) || (null == attrName) || (attrName.length() <= 0)) ? null : getPadXYValue(elem.getAttribute(attrName));
    }

    public static final String    IPADX_ATTR="ipadx";
    public Integer setIPadX (final Element elem) throws Exception
    {
        final Integer    val=getPadXYValue(elem, IPADX_ATTR);
        if (val != null)
            setIPadX(val.intValue());

        return val;
    }

    public static final String    IPADY_ATTR="ipady";
    public Integer setIPadY (final Element elem) throws Exception
    {
        final Integer    val=getPadXYValue(elem, IPADY_ATTR);
        if (val != null)
            setIPadY(val.intValue());

        return val;
    }

    public static final Double getWeightXYValue (final String attrVal) throws Exception
    {
        if ((null == attrVal) || (attrVal.length() <= 0))
            return null;

        final ValueStringInstantiator<Double>    vsi=ClassUtil.getAtomicStringInstantiator(Double.class);
        return vsi.newInstance(attrVal);
    }

    public static final Double getWeightXYValue (final Element elem, final String attrName) throws Exception
    {
        return ((null == elem) || (null == attrName) || (attrName.length() <= 0)) ? null : getWeightXYValue(elem.getAttribute(attrName));
    }

    public static final String    WEIGHTX_ATTR="weightx";
    public Double setWeightX (final Element elem) throws Exception
    {
        final Double    val=getWeightXYValue(elem, WEIGHTX_ATTR);
        if (val != null)
            setWeightX(val.doubleValue());

        return val;
    }

    public static final String    WEIGHTY_ATTR="weighty";
    public Double setWeightY (final Element elem) throws Exception
    {
        final Double    val=getWeightXYValue(elem, WEIGHTY_ATTR);
        if (val != null)
            setWeightY(val.doubleValue());

        return val;
    }

    public XmlProxyConvertible<?> getConstraintsConverter (final Element elem) throws Exception
    {
        return (null == elem) ? null : ExtendedGridBagConstraintsReflectiveProxy.EGBC;
    }
    /* NOTE: does not change current settings of non-specified values
     * @see net.community.chest.dom.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public ExtendedGridBagConstraints fromXml (final Element elem) throws Exception
    {
        final XmlProxyConvertible<?>    proxy=getConstraintsConverter(elem);
        @SuppressWarnings("unchecked")
        final Object                    o=
            (null == proxy) ? null : ((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
        if ((o != null) && (o != this))
            throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + ") mismatched re-constructed instances");

        return this;
    }

    public ExtendedGridBagConstraints (final Element elem) throws Exception
    {
        final ExtendedGridBagConstraints    inst=fromXml(elem);
        if (inst != this)
            throw new IllegalStateException(ClassUtil.getConstructorExceptionLocation(getClass()) + " XML mismatched instances");
    }
    /**
     * Default XML element name returned by {@link #getRootElementName()}
     * unless overridden
     */
    public static final String    CONSTRAINT_ELEM_NAME="gbc";
    public String getRootElementName ()
    {
        return CONSTRAINT_ELEM_NAME;
    }

    public Element toXml (Document doc, Element elem) throws Exception
    {
        if ((null == doc) && (null == elem))    // just so compiler does not complain about un-referenced parameter(s)
            throw new IllegalArgumentException("toXml() neither " + Document.class.getSimpleName() + " nor " + Element.class.getSimpleName() + " instances provided");

        elem.setAttribute(GRIDX_ATTR, isAbsoluteGridX() ? String.valueOf(getGridX()) : GridBagXYValueStringInstantiator.RELATIVE_VALUE);
        elem.setAttribute(GRIDY_ATTR, isAbsoluteGridY() ? String.valueOf(getGridY()) : GridBagXYValueStringInstantiator.RELATIVE_VALUE);

        elem.setAttribute(GRIDWIDTH_ATTR, isNumericalGridWidth() ? String.valueOf(getGridWidth()): String.valueOf(getGridWithType()));
        elem.setAttribute(GRIDHEIGHT_ATTR, isNumericalGridHeight() ? String.valueOf(getGridHeight()): String.valueOf(getGridHeightType()));

        elem.setAttribute(FILL_ATTR, String.valueOf(getFillType()));
        elem.setAttribute(ANCHOR_ATTR, String.valueOf(getAnchorType()));
        elem.setAttribute(INSETS_ATTR, InsetsValueInstantiator.toString(getInsets()));

        elem.setAttribute(WEIGHTX_ATTR, DoubleValueStringConstructor.DEFAULT.convertInstance(getWeightX()));
        elem.setAttribute(WEIGHTY_ATTR, DoubleValueStringConstructor.DEFAULT.convertInstance(getWeightY()));

        elem.setAttribute(IPADX_ATTR, String.valueOf(ipadx));
        elem.setAttribute(IPADY_ATTR, String.valueOf(ipady));

        return elem;
    }
    /*
     * @see net.community.chest.dom.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (final Document doc) throws Exception
    {
        return toXml(doc, doc.createElement(getRootElementName()));
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        final String    tagName=getRootElementName();
        try
        {
            final Element    elem=toXml(null, new StandaloneElementImpl(tagName));
            return DOMUtils.toString(elem);
        }
        catch(Exception e)    // should not happen
        {
            return "toString(" + tagName + ") " + e.getClass().getName() + ": " + e.getMessage();
        }
    }
    /*
     * @see java.awt.GridBagConstraints#clone()
     */
    @Override
    @CoVariantReturn
    public ExtendedGridBagConstraints clone ()
    {
        return getClass().cast(super.clone());
    }
}
