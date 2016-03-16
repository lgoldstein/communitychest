/*
 *
 */
package net.community.chest.ui.helpers.dialog;

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractButton;

import net.community.chest.awt.layout.FlowLayoutAlignment;
import net.community.chest.dom.impl.StandaloneDocumentImpl;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.ui.helpers.panel.HelperPanel;
import net.community.chest.util.map.MapEntryImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Used to hold a set of buttons usually in a dialog. The default layout
 * is {@link FlowLayout} with {@link FlowLayout#CENTER}, <code>hgap=5</code>,
 * <code>vgap=0</code>.</P>
 * @author Lyor G.
 * @since Jan 6, 2009 2:14:06 PM
 */
public class ButtonsPanel extends HelperPanel {
    /**
     *
     */
    private static final long serialVersionUID = -2320161452836362800L;
    public ButtonsPanel (LayoutManager layout, Document doc, boolean autoInit)
    {
        super(layout, doc, autoInit);
    }

    public static final FlowLayoutAlignment    DEFAULT_ALIGNMENT=FlowLayoutAlignment.CENTER;
    public static final int                    DEFAULT_HGAP=5, DEFAULT_VGAP=5;
    public ButtonsPanel (boolean autoInit)
    {
        this(new FlowLayout(DEFAULT_ALIGNMENT.getAlignment(), DEFAULT_HGAP, DEFAULT_VGAP), (Document) null, autoInit);
    }

    public ButtonsPanel (FlowLayoutAlignment align, int hGap, int vGap, boolean autoInit)
    {
        this(new FlowLayout((null == align) ? DEFAULT_ALIGNMENT.getAlignment() : align.getAlignment(), hGap, vGap), autoInit);
    }

    public ButtonsPanel (FlowLayoutAlignment align, int hGap, int vGap)
    {
        this(align, hGap, vGap, true);
    }

    public ButtonsPanel (int hGap, int vGap, boolean autoInit)
    {
        this(DEFAULT_ALIGNMENT, hGap, vGap, autoInit);
    }

    public ButtonsPanel (int hGap, int vGap)
    {
        this(hGap, vGap, true);
    }

    public ButtonsPanel (FlowLayoutAlignment align, boolean autoInit)
    {
        this(align, DEFAULT_HGAP, DEFAULT_VGAP, autoInit);
    }

    public ButtonsPanel (FlowLayoutAlignment align)
    {
        this(align, true);
    }

    public ButtonsPanel ()
    {
        this(true);
    }

    public ButtonsPanel (Document doc, boolean autoInit)
    {
        this(DEFAULT_ALIGNMENT, false /* delay initialization to allow setting default layout */);

        setComponentDocument(doc);
        if (autoInit)
            layoutComponent();
    }

    public ButtonsPanel (Element elem, boolean autoInit)
    {
        this((null == elem) ? null : new StandaloneDocumentImpl(elem), autoInit);
    }

    public ButtonsPanel (Element elem)
    {
        this(elem, true);
    }

    public ButtonsPanel (Document doc)
    {
        this(doc, true);
    }

    public ButtonsPanel (LayoutManager layout, boolean autoLayout)
    {
        this(layout, (Document) null, autoLayout);
    }

    public ButtonsPanel (LayoutManager layout)
    {
        this(layout, true);
    }

    private Map<String,AbstractButton>    _btnsMap    /* =null */;
    /**
     * @return {@link Map} of current buttons in the panel - key=action
     * command (case <U>insensitive</U> usually), value={@link AbstractButton}
     * associated with that command. <B>Caveat emptor:</B> external
     * manipulation of the map may have undefined effects
     */
    public Map<String,AbstractButton> getButtonsMap ()
    {
        return _btnsMap;
    }
    // CAVEAT EMPTOR - may destabilize the functionality
    public void setButtonsMap (Map<String,AbstractButton> bm)
    {
        _btnsMap = bm;
    }

    public AbstractButton getButton (final String cmd)
    {
        if ((null == cmd) || (cmd.length() <= 0))
            return null;

        final Map<String,? extends AbstractButton>    bm=getButtonsMap();
        if ((null == bm) || (bm.size() <= 0))
            return null;

        return bm.get(cmd);
    }
    /**
     * Adds the {@link ActionListener} to the button mapped according to
     * the supplied button key
     * @param cmd The button key (usually the action command)
     * @param l The {@link ActionListener} to add
     * @return The {@link AbstractButton} to which the listener was added
     * (<code>null</code> if no listener or no button found)
     */
    public AbstractButton addActionListener (final String cmd, final ActionListener l)
    {
        final AbstractButton    b=(null == l) ? null : getButton(cmd);
        if (b != null)
            b.addActionListener(l);
        return b;
    }
    /**
     * @param bl A {@link Collection} of "pairs" - each represented by a
     * {@link java.util.Map.Entry} whose key=the button key, value=the
     * {@link ActionListener} to add to that button (if found)
     * @return A {@link Map} of all the buttons that have been successfully
     * processed - key=the button key (case <U>insensitive</U> by default),
     * value=the {@link AbstractButton} instance. <B>Note:</B> it is possible
     * (though not recommended) to specify the <U>same key</U> more than once
     * - which will create one entry in the returned map, but will add the
     * listener(s) to the (same) button
     */
    public Map<String,AbstractButton> addActionListener (
            final Collection<? extends Map.Entry<String,? extends ActionListener>>    bl)
    {
        if ((null == bl) || (bl.size() <= 0))
            return null;

        Map<String,AbstractButton>    ret=null;
        for (final Map.Entry<String,? extends ActionListener>    be : bl)
        {
            final String            cmd=(null == be) ? null : be.getKey();
            final ActionListener    l=(null == be) ? null : be.getValue();
            final AbstractButton    b=addActionListener(cmd, l);
            if (null == b)
                continue;

            if (null == ret)
                ret = new TreeMap<String,AbstractButton>(String.CASE_INSENSITIVE_ORDER);
            ret.put(cmd, b);
        }

        return ret;
    }
    /**
     * Sets the {@link ActionListener} to <U>all</U> the current buttons
     * @param l The {@link ActionListener} instance
     * @return A {@link Collection} of all the buttons to which the listener
     * was added - each represented as {@link java.util.Map.Entry} whose key=the key
     * used to map the button, value=the mapped {@link AbstractButton} instance
     * to which the listener was added. Null/empty if no buttons or no listener
     */
    public Collection<Map.Entry<String,AbstractButton>> setActionListener (final ActionListener l)
    {
        final Map<String,? extends AbstractButton>                                bm=
            (null == l) ? null : getButtonsMap();
        final Collection<? extends Map.Entry<String,? extends AbstractButton>>    bl=
            ((null == bm) || (bm.size() <= 0)) ? null : bm.entrySet();
        final int                                                                numButtons=
            (null == bl) ? 0 : bl.size();
        if (numButtons <= 0)
            return null;

        Collection<Map.Entry<String,AbstractButton>>    ret=null;
        for (final Map.Entry<String,? extends AbstractButton> be : bl)
        {
            final String            cmd=(null == be) ? null : be.getKey();
            final AbstractButton    b=(null == be) ? null : be.getValue();
            if ((null == cmd) || (cmd.length() <= 0) || (null == b))
                continue;

            b.addActionListener(l);

            if (null == ret)
                ret = new ArrayList<Map.Entry<String,AbstractButton>>(numButtons);
            ret.add(new MapEntryImpl<String,AbstractButton>(cmd, b));
        }

        return ret;
    }
    // returns previous map value - null if none
    public AbstractButton putButton (final String cmd, final AbstractButton b)
    {
        if ((null == cmd) || (cmd.length() <= 0) || (null == b))
            return null;

        Map<String,AbstractButton>    bm=getButtonsMap();
        if (null == bm)
        {
            setButtonsMap(new TreeMap<String,AbstractButton>(String.CASE_INSENSITIVE_ORDER));
            if (null == (bm=getButtonsMap()))
                throw new IllegalStateException("putButton(" + cmd + ") no Map though one created");
        }

        return bm.put(cmd, b);
    }
    // uses AbstractButton#getActionCommand for mapping
    public AbstractButton putButton (final AbstractButton b)
    {
        return (null == b) ? null : putButton(b.getActionCommand(), b);
    }
    // trap any button added to the panel
    protected AbstractButton updatedButtonsMap (final Component comp)
    {
        if (comp instanceof AbstractButton)
        {
            final AbstractButton    b=(AbstractButton) comp, prev=putButton(b);
            if (prev != null)
                throw new IllegalStateException("updatedButtonsMap(" + prev.getActionCommand() + ") already mapped");
            return b;
        }

        return null;
    }
    /*
     * @see net.community.chest.ui.helpers.panel.HelperPanel#getPanelConverter(org.w3c.dom.Element)
     */
    @Override
    protected XmlProxyConvertible<?> getPanelConverter (Element elem)
    {
        return (null == elem) ? null : ButtonsPanelReflectiveProxy.BTNSPNL;
    }
    /*
     * @see java.awt.Container#add(java.awt.Component, int)
     */
    @Override
    public Component add (Component comp, int index)
    {
        updatedButtonsMap(comp);
        return super.add(comp, index);
    }
    /*
     * @see java.awt.Container#add(java.awt.Component, java.lang.Object, int)
     */
    @Override
    public void add (Component comp, Object constraints, int index)
    {
        updatedButtonsMap(comp);
        super.add(comp, constraints, index);
    }
    /*
     * @see java.awt.Container#add(java.awt.Component, java.lang.Object)
     */
    @Override
    public void add (Component comp, Object constraints)
    {
        updatedButtonsMap(comp);
        super.add(comp, constraints);
    }
    /*
     * @see java.awt.Container#add(java.awt.Component)
     */
    @Override
    public Component add (Component comp)
    {
        updatedButtonsMap(comp);
        return super.add(comp);
    }
    /*
     * @see java.awt.Container#add(java.lang.String, java.awt.Component)
     */
    @Override
    public Component add (String name, Component comp)
    {
        updatedButtonsMap(comp);
        return super.add(name, comp);
    }

    public static final <B extends AbstractButton> B add (
            Container c, Object constraint, Class<B> btnClass, String text, String cmd, ActionListener l)
    {
        final B    btn;
        try
        {
            if (null == (btn=btnClass.newInstance()))
                throw new IllegalStateException("add(" + text + ")[" + cmd + "] no instance created");
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }

        if (text != null)
            btn.setText(text);
        if (cmd != null)
            btn.setActionCommand(cmd);
        if (l != null)
            btn.addActionListener(l);
        if (c != null)
        {
            if (null == constraint)
                c.add(btn);
            else
                c.add(btn, constraint);
        }

        return btn;
    }
}
