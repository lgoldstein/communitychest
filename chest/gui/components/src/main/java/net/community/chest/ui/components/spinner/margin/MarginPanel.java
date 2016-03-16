/*
 *
 */
package net.community.chest.ui.components.spinner.margin;

import java.awt.Component;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeListener;

import net.community.chest.awt.layout.border.BorderLayoutPosition;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.impl.StandaloneDocumentImpl;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.swing.component.label.JLabelReflectiveProxy;
import net.community.chest.swing.component.spinner.JSpinnerReflectiveProxy;
import net.community.chest.ui.components.panel.LRLabeledComponent;
import net.community.chest.ui.helpers.SectionsMapImpl;
import net.community.chest.ui.helpers.label.TypedLabel;
import net.community.chest.ui.helpers.panel.PresetGridLayoutPanel;
import net.community.chest.util.map.MapEntryImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 11, 2009 11:38:55 AM
 *
 */
public class MarginPanel extends PresetGridLayoutPanel {
    /**
     *
     */
    private static final long serialVersionUID = -735041715917601763L;
    public MarginPanel (boolean horizontal, int gap, Document doc, boolean autoInit)
    {
        super(horizontal ? 1 : 0, horizontal ? 0 : 1, horizontal ? gap : 0, horizontal ? 0 : gap, doc, autoInit);
    }

    public MarginPanel (Document doc, boolean autoInit)
    {
        this(false, 5, doc, autoInit);
    }

    public MarginPanel (Document doc)
    {
        this(doc, true);
    }

    public MarginPanel (Element elem, boolean autoInit)
    {
        this((null == elem) ? null : new StandaloneDocumentImpl(elem), autoInit);
    }

    public MarginPanel (Element elem)
    {
        this(elem, true);
    }

    public MarginPanel (boolean autoLayout)
    {
        this((Document) null, autoLayout);
    }

    public MarginPanel ()
    {
        this(true);
    }

    private Map<BorderLayoutPosition,JSpinner>    _spMap;
    public Map<BorderLayoutPosition,JSpinner> getSpinnersMap ()
    {
        return _spMap;
    }

    public void setSpinnersMap (Map<BorderLayoutPosition,JSpinner> sm)
    {
        _spMap = sm;
    }

    protected JSpinner createSpinner (BorderLayoutPosition p)
    {
        return (null == p) ? null : new MarginSpinner(p);
    }

    protected JSpinner getSpinner (BorderLayoutPosition p, boolean createIfNotExist)
    {
        if (null == p)
            return null;

        Map<BorderLayoutPosition,JSpinner>    sm=getSpinnersMap();
        JSpinner                            s=
            ((null == sm) || (sm.size() <= 0)) ? null : sm.get(p);
        if ((null == s) && createIfNotExist)
        {
            if (null == (s=createSpinner(p)))
                return null;

            if (null == sm)
            {
                setSpinnersMap(new EnumMap<BorderLayoutPosition,JSpinner>(BorderLayoutPosition.class));
                if (null == (sm=getSpinnersMap()))
                    throw new IllegalStateException("getSpinner(" + p + ") no map set though created");
            }

            sm.put(p, s);
        }

        return s;
    }

    public JSpinner getSpinner (BorderLayoutPosition p)
    {
        return getSpinner(p, false);
    }
    // returns previous value - CAVEAT EMPTOR if called after layoutComponent
    public JSpinner setSpinner (BorderLayoutPosition p, JSpinner s /* null == remove */)
    {
        final Map<BorderLayoutPosition,JSpinner>    sm=
            (null == p) ? null : getSpinnersMap();
        if ((null == sm) || (sm.size() <= 0))
            return null;

        if (null == s)
            return sm.remove(p);
        else
            return sm.put(p, s);
    }

    private Map<BorderLayoutPosition,Component>    _lbMap;
    public Map<BorderLayoutPosition,Component> getLabelsMap ()
    {
        return _lbMap;
    }

    public void setLabelsMap (Map<BorderLayoutPosition,Component> lm)
    {
        _lbMap = lm;
    }

    protected Component createLabel (BorderLayoutPosition p)
    {
        return (null == p) ? null : new TypedLabel<BorderLayoutPosition>(BorderLayoutPosition.class, p, p.toString());
    }

    protected Component getLabel (BorderLayoutPosition p, boolean createIfNotExist)
    {
        if (null == p)
            return null;

        Map<BorderLayoutPosition,Component>    lm=getLabelsMap();
        Component                            c=
            ((null == lm) || (lm.size() <= 0)) ? null : lm.get(p);
        if ((null == c) && createIfNotExist)
        {
            if (null == (c=createLabel(p)))
                return null;

            if (null == lm)
            {
                setLabelsMap(new EnumMap<BorderLayoutPosition,Component>(BorderLayoutPosition.class));
                if (null == (lm=getLabelsMap()))
                    throw new IllegalStateException("getLabel(" + p + ") no map set though created");
            }

            lm.put(p, c);
        }

        return c;
    }

    public Component getLabel (BorderLayoutPosition p)
    {
        return getLabel(p, false);
    }

    // returns previous value - CAVEAT EMPTOR if called after layoutComponent
    public Component setLabel (BorderLayoutPosition p, Component c /* null == remove */)
    {
        final Map<BorderLayoutPosition,Component>    lm=
            (null == p) ? null : getLabelsMap();
        if ((null == lm) || (lm.size() <= 0))
            return null;

        if (null == c)
            return lm.remove(p);
        else
            return lm.put(p, c);
    }

    public Number getValue (BorderLayoutPosition p)
    {
        final JSpinner    s=getSpinner(p);
        final Object    v=(null == s) ? null : s.getValue();
        if (v instanceof Number)
            return (Number) v;
        return null;
    }

    public void setValue (BorderLayoutPosition p, Number v)
    {
        final JSpinner    s=(null == v) ? null : getSpinner(p);
        if (null == s)
            return;

        s.setValue(v);
    }
    // returns the spinner(s) to which the listener was added
    public List<JSpinner> addChangeListener (BorderLayoutPosition p /* null=all */, ChangeListener l)
    {
        if (null == l)
            return null;

        final Collection<? extends JSpinner>    sc;
        if (null == p)
        {
            final Map<BorderLayoutPosition,? extends JSpinner>    sm=getSpinnersMap();
            sc = ((null == sm) || (sm.size() <= 0)) ? null : sm.values();
        }
        else
        {
            final JSpinner    s=getSpinner(p);
            sc = (null == s) ? null : Arrays.asList(s);
        }

        final int                numSpinners=(null == sc) ? 0 : sc.size();
        final List<JSpinner>    ret=(numSpinners <= 0) ? null : new ArrayList<JSpinner>(numSpinners);
        if (numSpinners > 0)
        {
            for (final JSpinner    s : sc)
            {
                if (null == s)
                    continue;
                s.addChangeListener(l);
            }
        }

        return ret;
    }

    public List<JSpinner> addChangeListener (ChangeListener l)
    {
        return addChangeListener(null, l);
    }

    protected XmlProxyConvertible<?> getSpinnerConverter (BorderLayoutPosition p, Element elem)
    {
        if ((null == p) || (null == elem))
            return null;

        return JSpinnerReflectiveProxy.SPINNER;
    }

    protected JSpinner prepareSpinner (BorderLayoutPosition p, JSpinner s, Element sElem) throws RuntimeException
    {
        try
        {
            final XmlProxyConvertible<?>     proxy=getSpinnerConverter(p, sElem);
            @SuppressWarnings("unchecked")
            final Object                    o=
                (null == proxy) ? s : ((XmlProxyConvertible<Object>) proxy).fromXml(s, sElem);
            if (o != s)
                throw new IllegalStateException("prepareSpinner(" + p + ")[" + DOMUtils.toString(sElem) + "] mismatched instances");
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }

        return s;
    }

    protected XmlProxyConvertible<?> getLabelConverter (BorderLayoutPosition p, Element elem)
    {
        if ((null == p) || (null == elem))
            return null;

        return JLabelReflectiveProxy.LABEL;
    }

    protected Component prepareLabel (BorderLayoutPosition p, Component c, Element cElem) throws RuntimeException
    {
        try
        {
            final XmlProxyConvertible<?>     proxy=getLabelConverter(p, cElem);
            @SuppressWarnings("unchecked")
            final Object                    o=
                (null == proxy) ? c : ((XmlProxyConvertible<Object>) proxy).fromXml(c, cElem);
            if (o != c)
                throw new IllegalStateException("prepareLabel(" + p + ")[" + DOMUtils.toString(cElem) + "] mismatched instances");
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }

        return c;
    }

    private boolean    _lblLastPos    /* =false */;
    public boolean isLabelLastPos ()
    {
        return _lblLastPos;
    }

     public void setLabelLastPos (final boolean lblLastPos)
    {
        if (lblLastPos != isLabelLastPos())
            _lblLastPos = lblLastPos;
    }
     /**
     * Called by {@link #layoutSpinner(BorderLayoutPosition, JSpinner, Element, Component, Element)}
     * after successfully calling the {@link #prepareLabel(BorderLayoutPosition, Component, Element)}
     * and {@link #prepareSpinner(BorderLayoutPosition, JSpinner, Element)}
     * methods and getting at least one non-<code>null</code> result. The
     * default implementation checks if either of the arguments is <code>null</code>,
     * and if so, then returns the other. Otherwise, it returns a $$$
     * @param p The {@link BorderLayoutPosition} for which the entry is called
     * @param s The {@link JSpinner} result of {@link #prepareSpinner(BorderLayoutPosition, JSpinner, Element)}
     * @param c The label {@link Component} result of {@link #prepareLabel(BorderLayoutPosition, Component, Element)}
     * @return The {@link Component} to be <code><I>add(...)</I></code>-ed to
     * the panel as representing the given position. If <code>null</code> then
     * nothing is added (assumed that the override has taken care of it)
     */
    protected Component layoutSpinner (BorderLayoutPosition p, JSpinner s, Component c)
    {
        if (null == s)
            return c;
        if (null == c)
            return s;

        if (!(c instanceof JLabel))
            throw new ClassCastException("layoutSpinner(" + p + ") spinner name not a label: " + c.getClass());

        return new LRLabeledComponent<JSpinner>(JSpinner.class, s, (JLabel) c, isLabelLastPos(), true);
     }

    protected Component layoutSpinner (BorderLayoutPosition p, JSpinner so, Element sElem, Component co, Element cElem) throws RuntimeException
    {
        final JSpinner    s=prepareSpinner(p, so, sElem);
        final Component    c=prepareLabel(p, co, cElem),
                        r=layoutSpinner(p, s, c);
        if (r != null)
            add(r);
        return r;
    }

    private List<BorderLayoutPosition>    _pl    /* =null */;
    protected List<BorderLayoutPosition> getPositions (List<BorderLayoutPosition> defVal)
    {
        if (null == _pl)
            _pl = defVal;
        return _pl;
    }

    public List<BorderLayoutPosition> getPositions ()
    {
        return getPositions(null);
    }

    // CAVEAT EMPTOR - may have not effect if called after layoutComponent
    public void setPositions (List<BorderLayoutPosition> pl)
    {
        _pl = pl;
    }

    protected String getSectionName (BorderLayoutPosition p, String subKey)
    {
        if ((null == subKey) || (subKey.length() <= 0))
            return null;

        final String    k=getSectionName(p);
        if ((null == k) || (k.length() <= 0))
            return null;

        return SectionsMapImpl.getSectionName(k, subKey);
    }

    protected String getSpinnerSectionName (BorderLayoutPosition p)
    {
        return getSectionName(p, MarginPanelReflectiveProxy.SPINNER_ELEM_NAME);
    }

    protected Element getSpinnerSection (BorderLayoutPosition p)
    {
        return getSection(getLabelSectionName(p));
    }

    protected String getLabelSectionName (BorderLayoutPosition p)
    {
        return getSectionName(p, JLabelReflectiveProxy.LABEL_ELEMNAME);
    }

    protected Element getLabelSection (BorderLayoutPosition p)
    {
        return getSection(getLabelSectionName(p));
    }

    public Map.Entry<Component,JSpinner> layoutSpinner (BorderLayoutPosition p)
    {
        final JSpinner    s=getSpinner(p, true);
        final Component    c=getLabel(p, true);
        final Element    sElem=getSpinnerSection(p), cElem=getLabelSection(p);
        if ((s != null) || (c != null))
        {
            layoutSpinner(p, s, sElem, c, cElem);
            return new MapEntryImpl<Component,JSpinner>(c,s);
        }

        return null;
    }
    /*
     * @see net.community.chest.ui.helpers.panel.HelperPanel#getPanelConverter(org.w3c.dom.Element)
     */
    @Override
    protected XmlProxyConvertible<?> getPanelConverter (Element elem)
    {
        return (null == elem) ? null : MarginPanelReflectiveProxy.MRGNPNL;
    }
    /**
     * The default order of positions
     */
    public static final List<BorderLayoutPosition>    DEFAULT_POSITIONS=
        Arrays.asList(BorderLayoutPosition.NORTH, BorderLayoutPosition.SOUTH, BorderLayoutPosition.EAST, BorderLayoutPosition.WEST);
    /*
     * @see net.community.chest.ui.helpers.panel.HelperPanel#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();

        final Collection<BorderLayoutPosition>    pl=getPositions(DEFAULT_POSITIONS);
        for (final BorderLayoutPosition p : pl)
            layoutSpinner(p);
    }

    public static final Number    NO_VALUE=Integer.valueOf(Integer.MIN_VALUE);
    protected Number getSpinnerValue (final BorderLayoutPosition p, final JSpinner s) throws NumberFormatException
    {
        if ((null == s) || (null == p))
            return null;

        final Object    v=s.getValue();
        if (v instanceof Number)
            return (Number) v;

        throw new NumberFormatException("getSpinnerValue(" + p + ") value=" + v + " not a number but rather a " + ((null == v) ? null : v.getClass()));
    }

    protected Insets updateMarginValue (Insets m, BorderLayoutPosition p, Number n) throws NumberFormatException
    {
        if ((null == m) || (null == p) || (null == n))
            return m;

        switch(p)
        {
            case NORTH    :
                m.top = n.intValue();
                break;

            case SOUTH    :
                m.bottom = n.intValue();
                break;

            case EAST    :
                m.right = n.intValue();
                break;

            case WEST    :
                m.left = n.intValue();
                break;

            default        :
                throw new NumberFormatException("updateMarginValue(" + p + ")[" + n + "] unknown position");
        }

        return m;
    }

    public Insets getMarginValue () throws NumberFormatException
    {
        final Map<BorderLayoutPosition,? extends JSpinner>                                sm=
            getSpinnersMap();
        final Collection<? extends Map.Entry<BorderLayoutPosition,? extends JSpinner>>    sl=
            ((null == sm) || (sm.size() <= 0)) ? null : sm.entrySet();
        if ((null == sl) || (sl.size() <= 0))
            return null;

        Insets    m=new Insets(0, 0, 0, 0);
        for (final Map.Entry<BorderLayoutPosition,? extends JSpinner> sp : sl)
        {
            final BorderLayoutPosition    p=(null == sp) ? null : sp.getKey();
            final JSpinner                s=(null == sp) ? null : sp.getValue();
            final Number                n=getSpinnerValue(p, s);
            m = updateMarginValue(m, p, n);
        }

        return m;
    }
}
