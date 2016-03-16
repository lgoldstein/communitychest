/*
 *
 */
package net.community.chest.ui.components.dialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.AbstractButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.community.chest.awt.TypedComponentAssignment;
import net.community.chest.dom.impl.StandaloneDocumentImpl;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.reflect.AttributeAccessor;
import net.community.chest.reflect.AttributeMethodType;
import net.community.chest.ui.helpers.dialog.ButtonsPanel;
import net.community.chest.ui.helpers.dialog.SettableDialog;
import net.community.chest.util.map.MapsUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>A generic dialog that uses reflection API to extract all the set-able
 * {@link Boolean} methods of the configured class and display them as
 * checkbox(es). The default name is taken from the "pure" attribute name
 * with spaces before each capital letter.</P>
 *
 * @param <V> Class being configured
 * @author Lyor G.
 * @since Apr 28, 2009 9:53:05 AM
 */
public abstract class BooleanOptionsDialog<V> extends SettableDialog<V> implements TypedComponentAssignment<V> {
    /**
     *
     */
    private static final long serialVersionUID = 3931874238469247097L;
    protected BooleanOptionsDialog (Frame owner, V value, Document doc, boolean autoInit)
    {
        super(owner, doc, autoInit);

        if (autoInit)
            setContent(value);
        else
            setAssignedValue(value);
    }

    protected BooleanOptionsDialog (Frame owner, V value, Document doc)
    {
        this(owner, value, doc, true);
    }

    protected BooleanOptionsDialog (Frame owner, Document doc, boolean autoInit)
    {
        this(owner, null, doc, autoInit);
    }

    protected BooleanOptionsDialog (Frame owner, Document doc)
    {
        this(owner, doc, true);
    }

    protected BooleanOptionsDialog (Frame owner, V value, Element elem, boolean autoInit)
    {
        this(owner, value, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoInit);
    }

    protected BooleanOptionsDialog (Frame owner, Element elem, boolean autoInit)
    {
        this(owner, null, elem, autoInit);
    }

    protected BooleanOptionsDialog (Frame owner, Element elem)
    {
        this(owner, elem, true);
    }

    protected BooleanOptionsDialog (Frame owner, V value, Element elem)
    {
        this(owner, value, elem, true);
    }

    protected BooleanOptionsDialog (Frame owner, V value, boolean autoInit)
    {
        this(owner, value, (Document) null, autoInit);
    }

    protected BooleanOptionsDialog (Frame owner, boolean autoInit)
    {
        this(owner, (V) null, autoInit);
    }
    protected BooleanOptionsDialog (Frame owner, V value)
    {
        this(owner, value, true);
    }

    protected BooleanOptionsDialog (Frame owner)
    {
        this(owner, true);
    }

    protected BooleanOptionsDialog (Dialog owner, V value, Document doc, boolean autoInit)
    {
        super(owner, doc, autoInit);

        if (autoInit)
            setContent(value);
        else
            setAssignedValue(value);
    }

    protected BooleanOptionsDialog (Dialog owner, V value, Document doc)
    {
        this(owner, value, doc, true);
    }

    protected BooleanOptionsDialog (Dialog owner, Document doc, boolean autoInit)
    {
        this(owner, null, doc, autoInit);
    }

    protected BooleanOptionsDialog (Dialog owner, Document doc)
    {
        this(owner, doc, true);
    }

    protected BooleanOptionsDialog (Dialog owner, V value, Element elem, boolean autoInit)
    {
        this(owner, value, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoInit);
    }

    protected BooleanOptionsDialog (Dialog owner, Element elem, boolean autoInit)
    {
        this(owner, null, elem, autoInit);
    }

    protected BooleanOptionsDialog (Dialog owner, Element elem)
    {
        this(owner, elem, true);
    }

    protected BooleanOptionsDialog (Dialog owner, V value, Element elem)
    {
        this(owner, value, elem, true);
    }

    protected BooleanOptionsDialog (Dialog owner, V value, boolean autoInit)
    {
        this(owner, value, (Document) null, autoInit);
    }

    protected BooleanOptionsDialog (Dialog owner, boolean autoInit)
    {
        this(owner, (V) null, autoInit);
    }
    protected BooleanOptionsDialog (Dialog owner, V value)
    {
        this(owner, value, true);
    }

    protected BooleanOptionsDialog (Dialog owner)
    {
        this(owner, true);
    }

    protected BooleanOptionsDialog (Window owner, V value, Document doc, boolean autoInit)
    {
        super(owner, doc, autoInit);

        if (autoInit)
            setContent(value);
        else
            setAssignedValue(value);
    }

    protected BooleanOptionsDialog (Window owner, V value, Document doc)
    {
        this(owner, value, doc, true);
    }

    protected BooleanOptionsDialog (Window owner, Document doc, boolean autoInit)
    {
        this(owner, null, doc, autoInit);
    }

    protected BooleanOptionsDialog (Window owner, Document doc)
    {
        this(owner, doc, true);
    }

    protected BooleanOptionsDialog (Window owner, V value, Element elem, boolean autoInit)
    {
        this(owner, value, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoInit);
    }

    protected BooleanOptionsDialog (Window owner, Element elem, boolean autoInit)
    {
        this(owner, null, elem, autoInit);
    }

    protected BooleanOptionsDialog (Window owner, Element elem)
    {
        this(owner, elem, true);
    }

    protected BooleanOptionsDialog (Window owner, V value, Element elem)
    {
        this(owner, value, elem, true);
    }

    protected BooleanOptionsDialog (Window owner, V value, boolean autoInit)
    {
        this(owner, value, (Document) null, autoInit);
    }

    protected BooleanOptionsDialog (Window owner, boolean autoInit)
    {
        this(owner, (V) null, autoInit);
    }
    protected BooleanOptionsDialog (Window owner, V value)
    {
        this(owner, value, true);
    }

    protected BooleanOptionsDialog (Window owner)
    {
        this(owner, true);
    }

    private V    _value;
    /*
     * @see net.community.chest.awt.TypedComponentAssignment#getAssignedValue()
     */
    @Override
    public V getAssignedValue ()
    {
        return _value;
    }
    /* NOTE: does not update the UI !!!
     * @see net.community.chest.awt.TypedComponentAssignment#setAssignedValue(java.lang.Object)
     */
    @Override
    public void setAssignedValue (V value)
    {
        _value = value;
    }

    protected void layoutOptions (final Map<String,OptionCheckbox> om)
    {
        final Collection<? extends Map.Entry<String,? extends OptionCheckbox>>    ol=
            ((null == om) || (om.size() <= 0)) ? null : om.entrySet();
        if ((null == ol) || (ol.size() <= 0))
            return;

        final JPanel    optsPanel=new JPanel(new GridLayout(0, 1, 0, 5));
        for (final Map.Entry<String,? extends OptionCheckbox> oe : ol)
        {
            final OptionCheckbox    cb=(null == oe) ? null : oe.getValue();
            if (null == cb)
                continue;

            cb.setEnabled(false);
            optsPanel.add(cb);
        }

        final Container    ct=getContentPane();
        ct.add(optsPanel, BorderLayout.CENTER);
    }
    /*
     * @see net.community.chest.ui.helpers.dialog.HelperDialog#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();
        layoutOptions();
    }
    // returns current option value
    protected Boolean setOptionValue (final OptionCheckbox cb, final V value)
    {
        if (null == cb)
            return null;

        if (null == value)
        {
            cb.setEnabled(false);
            return null;
        }

        try
        {
            final Object    o=cb.getOptionValue(value);
            final Boolean    selVal=(Boolean) o;
            cb.setEnabled(true);
            cb.setSelected(selVal.booleanValue());
            return selVal;
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }
    }

    protected void fromCurrentSettings (final V value)
    {
        final Collection<? extends OptionCheckbox>    ol=
            ((null == _optsMap) || (_optsMap.size() <= 0)) ? null : _optsMap.values();
        if ((null == ol) || (ol.size() <= 0))
            return;

        for (final OptionCheckbox    cb : ol)
            setOptionValue(cb, value);
    }
    /*
     * @see net.community.chest.ui.helpers.dialog.SettableDialog#setContent(java.lang.Object)
     */
    @Override
    public void setContent (final V value)
    {
        setAssignedValue(value);
        fromCurrentSettings(value);
    }
    /*
     * @see net.community.chest.ui.helpers.dialog.SettableDialog#clearContent()
     */
    @Override
    public void clearContent ()
    {
        setContent(null);
    }
    /*
     * @see net.community.chest.ui.helpers.dialog.SettableDialog#refreshContent(java.lang.Object)
     */
    @Override
    public void refreshContent (V value)
    {
        setContent(value);
    }

    public static final Map<String,AttributeAccessor> getOptionsAccessMap (final Class<?> c)
        throws IllegalStateException
    {
        final Map<String,AttributeAccessor>                                            am=
            AttributeMethodType.getAllAccessibleAttributes(c);
        final Collection<? extends Map.Entry<String,? extends AttributeAccessor>>    al=
            ((null == am) || (am.size() <= 0)) ? null : am.entrySet();
        if ((null == al) || (al.size() <= 0))
            return null;

        Set<String>    removeKeys=null;
        for (final Map.Entry<String,? extends AttributeAccessor> ae : al)
        {
            final String            n=(null == ae) ? null : ae.getKey();
            final AttributeAccessor    a=(null == ae) ? null : ae.getValue();
            final Method            m=(null == a) ? null : a.getGetter();
            final Class<?>            t=(null == m) ? null : m.getReturnType();
            if ((null == n) || (n.length() <= 0) || (null == t))
                continue;
            if (AttributeMethodType.PREDICATE.isMatchingReturnType(t))
                continue;

            if (removeKeys == null)
                removeKeys = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
            if (!removeKeys.add(n))
                continue;
        }

        MapsUtils.removeAll(am, removeKeys);
        return am;
    }

    protected abstract Map<String,AttributeAccessor> getOptionsAccessMap ();

    protected Map<String,OptionCheckbox> createOptions ()
    {
        final Map<String,? extends AttributeAccessor>                                am=
            getOptionsAccessMap();
        final Collection<? extends Map.Entry<String,? extends AttributeAccessor>>    al=
            ((null == am) || (am.size() <= 0)) ? null : am.entrySet();
        if ((null == al) || (al.size() <= 0))
            return null;

        Map<String,OptionCheckbox>    om=null;
        for (final Map.Entry<String,? extends AttributeAccessor> ae : al)
        {
            final String            n=(null == ae) ? null : ae.getKey();
            final AttributeAccessor    a=(null == ae) ? null : ae.getValue();
            if ((null == n) || (n.length() <= 0) || (null == a))
                continue;    // should not happen

            if (null == om)
                om = new TreeMap<String,OptionCheckbox>(String.CASE_INSENSITIVE_ORDER);
            else if (om.containsKey(n))
                throw new IllegalStateException("createOptions(" + n + ") duplicate key");
            om.put(n, new OptionCheckbox(a));
        }

        return om;
    }

    private Map<String,OptionCheckbox>    _optsMap    /* =null */;
    protected Map<String,OptionCheckbox> layoutOptions ()
    {
        if ((null == _optsMap) || (_optsMap.size() <= 0))
        {
            _optsMap = createOptions();
            layoutOptions(_optsMap);
        }

        return _optsMap;
    }

    protected void toCurrentSettings (final V value)
    {
        final Collection<? extends Map.Entry<String,? extends OptionCheckbox>>    ol=
            ((null == _optsMap) || (_optsMap.size() <= 0)) ? null : _optsMap.entrySet();
        if ((null == ol) || (ol.size() <= 0))
            return;

        for (final Map.Entry<String,? extends OptionCheckbox> oe : ol)
        {
            final OptionCheckbox    cb=(null == oe) ? null : oe.getValue();
            final String            n=(null == cb) ? null : cb.getText();
            if (null == cb)
                continue;

            try
            {
                cb.setOptionValue(value);
            }
            catch(Exception e)
            {
                JOptionPane.showMessageDialog(this, e.getMessage() + " while set option=" + n, e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private ActionListener    _applyListener;
    protected ActionListener getApplyListener ()
    {
        if (null == _applyListener)
            _applyListener = new ActionListener() {
                    /*
                     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                     */
                    @Override
                    public void actionPerformed (ActionEvent e)
                    {
                        final Object    src=(null == e) ? null : e.getSource();
                        if (src instanceof AbstractButton)
                        {
                            toCurrentSettings(getAssignedValue());
                            dispose();
                        }
                    }
                };
        return _applyListener;
    }
    // NOTE !!! has no effect if called after "layoutButtons"
    protected void setApplyListener (ActionListener l)
    {
        _applyListener = l;
    }
    /*
     * @see net.community.chest.ui.helpers.dialog.HelperDialog#layoutButtons()
     */
    @Override
    public void layoutButtons ()
    {
        super.layoutButtons();

        final ButtonsPanel        p=getButtonsPanel();
        final AbstractButton    b=
            (null == p) ? null : p.addActionListener("apply", getApplyListener());
        if (null == b)
            throw new IllegalStateException("No APPLY button found");
    }
}
