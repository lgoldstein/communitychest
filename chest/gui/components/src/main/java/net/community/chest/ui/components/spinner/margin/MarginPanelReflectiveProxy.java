/*
 *
 */
package net.community.chest.ui.components.spinner.margin;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.JSpinner;

import net.community.chest.awt.layout.border.BorderLayoutPosition;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.lang.StringUtil;
import net.community.chest.swing.component.label.JLabelReflectiveProxy;
import net.community.chest.ui.helpers.panel.HelperPanelReflectiveProxy;
import net.community.chest.util.map.MapEntryImpl;

import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <P> The reflected {@link MarginPanel} type
 * @author Lyor G.
 * @since Mar 11, 2009 12:32:49 PM
 */
public class MarginPanelReflectiveProxy<P extends MarginPanel> extends HelperPanelReflectiveProxy<P> {
    protected MarginPanelReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public MarginPanelReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected GridLayout updateLayoutOrientation (GridLayout l, boolean horizontal)
    {
        if (null == l)
            return null;

        // NOTE !!! setters call sequence is crucial in order to avoid a state where both zero
        final int    r=l.getRows(), c=l.getColumns();
        if (horizontal)
        {
            if (r != 1)
                l.setRows(1);
            if (c != 0)
                l.setColumns(0);
        }
        else
        {
            if (c != 1)
                l.setColumns(1);
            if (r != 0)
                l.setRows(0);
        }

        return l;
    }

    protected GridLayout updateLayoutGap (GridLayout l, int gap)
    {
        if (null == l)
            return null;

        final int    r=l.getRows(), c=l.getColumns(),
                    h=l.getHgap(), v=l.getVgap();
        if (r == 1)    // horizontal
        {
            if (v != 0)
                l.setVgap(0);
            if (h != gap)
                l.setHgap(gap);
        }
        else if (c == 1)
        {
            if (h != 0)
                l.setHgap(0);
            if (v != gap)
                l.setVgap(gap);
        }

        return l;
    }
    /*
     * @see net.community.chest.awt.dom.proxy.ContainerReflectiveProxy#setLayout(java.awt.Container, org.w3c.dom.Element)
     */
    @Override
    public LayoutManager setLayout (P src, Element elem) throws Exception
    {
        // NOT allowed - use the "gap" and "horizontal" attributes
        throw new UnsupportedOperationException("setLayout(" + DOMUtils.toString(elem) + ") N/A");
    }

    public static final String    HORIZ_VIRTATTR="horizontal",
                                GAP_VIRTATTR="gap",
                                POSITIONS_ATTR="positions";
    protected GridLayout updateLayoutOrientation (P src, boolean horizontal)
    {
        final LayoutManager    lm=(null == src) ? null : src.getLayout();
        if (lm instanceof GridLayout)
            return updateLayoutOrientation((GridLayout) lm, horizontal);

        return null;
    }

    protected GridLayout updateLayoutGap (P src, int gap)
    {
        final LayoutManager    lm=(null == src) ? null : src.getLayout();
        if (lm instanceof GridLayout)
            return updateLayoutGap((GridLayout) lm, gap);

        return null;
    }
    /*
     * @see net.community.chest.awt.dom.proxy.ComponentReflectiveProxy#handleUnknownAttribute(java.awt.Component, java.lang.String, java.lang.String, java.util.Map)
     */
    @Override
    protected P handleUnknownAttribute (P src, String name, String value, Map<String,? extends Method> accsMap) throws Exception
    {
        if (HORIZ_VIRTATTR.equalsIgnoreCase(name))
        {
            updateLayoutOrientation(src, Boolean.parseBoolean(value));
            return src;
        }
        else if (GAP_VIRTATTR.equalsIgnoreCase(name))
        {
            updateLayoutGap(src, Integer.parseInt(value));
            return src;
        }

        return super.handleUnknownAttribute(src, name, value, accsMap);
    }
    /*
     * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.Method)
     */
    @Override
    protected P updateObjectAttribute (P src, String name, String value, Method setter) throws Exception
    {
        if (POSITIONS_ATTR.equalsIgnoreCase(name))
        {
            final Collection<String>            sl=StringUtil.splitString(value, ',');
            final int                            numPos=(null == sl) ? 0 : sl.size();
            final List<BorderLayoutPosition>    pl=(numPos <= 0) ? null : new ArrayList<BorderLayoutPosition>(numPos);
            if (numPos > 0)
            {
                for (final String    s : sl)
                {
                    if ((null == s) || (s.length() <= 0))
                        continue;

                    final BorderLayoutPosition    p=BorderLayoutPosition.fromString(s);
                    if (null == p)
                        throw new NoSuchElementException("updateObjectAttribute(" + name + ")[" + value + "] unknown value=" + s);
                    pl.add(p);
                }
            }

            setter.invoke(src, pl);
            return src;
        }

        return super.updateObjectAttribute(src, name, value, setter);
    }

    public static final String    SPINNER_ELEM_NAME="spinner";
    public boolean isSpinnerElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, SPINNER_ELEM_NAME);
    }

    protected BorderLayoutPosition getPosition (Element elem)
    {
        final String    s=(null == elem) ? null : elem.getAttribute(CLASS_ATTR);
        return BorderLayoutPosition.fromString(s);
    }

    protected XmlProxyConvertible<?> getSpinnerConverter (Element elem)
    {
        return (null == elem) ? null : MarginSpinnerReflectiveProxy.MRGNSPIN;
    }

    public Map.Entry<BorderLayoutPosition,JSpinner> setSpinner (P src, Element elem) throws Exception
    {
        final BorderLayoutPosition    p=getPosition(elem);
        if (null == p)
            throw new NoSuchElementException("setSpinner(" + DOMUtils.toString(elem) + ") cannot resolved position");

        final JSpinner                    s=src.getSpinner(p, true);
        final XmlProxyConvertible<?>    conv=getSpinnerConverter(elem);
        @SuppressWarnings("unchecked")
        final Object                    o=
            (null == conv) ? s : ((XmlProxyConvertible<Object>) conv).fromXml(s, elem);
        if (o != s)
            throw new IllegalStateException("setSpinner(" + p + ")[" + DOMUtils.toString(elem) + "] mismatched instances");
        src.setSpinner(p, s);

        return new MapEntryImpl<BorderLayoutPosition,JSpinner>(p, s);
    }

    public boolean isLabelElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, JLabelReflectiveProxy.LABEL_ELEMNAME);
    }

    public XmlProxyConvertible<?> getLabelConverter (final Element elem) throws Exception
    {
        return (null == elem) ? null : JLabelReflectiveProxy.LABEL;
    }

    public Map.Entry<BorderLayoutPosition,Component> setLabel (P src, Element elem) throws Exception
    {
        final BorderLayoutPosition    p=getPosition(elem);
        if (null == p)
            throw new NoSuchElementException("setLabel(" + DOMUtils.toString(elem) + ") cannot resolved position");

        final Component                    c=src.getLabel(p, true);
        final XmlProxyConvertible<?>    conv=getLabelConverter(elem);
        @SuppressWarnings("unchecked")
        final Object                    o=
            (null == conv) ? c : ((XmlProxyConvertible<Object>) conv).fromXml(c, elem);
        if (o != c)
            throw new IllegalStateException("setLabel(" + p + ")[" + DOMUtils.toString(elem) + "] mismatched instances");
        src.setLabel(p, c);

        return new MapEntryImpl<BorderLayoutPosition,Component>(p, c);
    }
    /*
     * @see net.community.chest.swing.component.JComponentReflectiveProxy#fromXmlChild(javax.swing.JComponent, org.w3c.dom.Element)
     */
    @Override
    public P fromXmlChild (P src, Element elem) throws Exception
    {
        final String    tagName=elem.getTagName();
        if (isSpinnerElement(elem, tagName))
        {
            setSpinner(src, elem);
            return src;
        }
        else if (isLabelElement(elem, tagName))
        {
            setLabel(src, elem);
            return src;
        }

        return super.fromXmlChild(src, elem);
    }

    public static final MarginPanelReflectiveProxy<MarginPanel>    MRGNPNL=
        new MarginPanelReflectiveProxy<MarginPanel>(MarginPanel.class, true) {
            /* Need to override this in order to ensure correct auto-layout
             * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
             */
            @Override
            public MarginPanel fromXml (Element elem) throws Exception
            {
                return (null == elem) ? null : new MarginPanel(elem);
            }
        };
}
