/*
 *
 */
package net.community.chest.ui.helpers.combobox;

import java.awt.Component;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import net.community.chest.lang.TypedValuesContainer;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Class used to render an icon next to the text in the combo box</P>
 *
 * @param <V> The type of value for which the icon is requested
 * @author Lyor G.
 * @since Dec 3, 2008 6:12:21 PM
 */
public abstract class IconedTypedComboBoxRenderer<V> extends JLabel
                implements ListCellRenderer, TypedValuesContainer<V> {
    /**
     *
     */
    private static final long serialVersionUID = -344340926118347861L;
    private final Class<V> _objClass;
    /*
     * @see net.community.chest.TypedValuesContainer#getValuesClass()
     */
    @Override
    public final /* no cheating */ Class<V> getValuesClass ()
    {
        return _objClass;
    }

    protected IconedTypedComboBoxRenderer (final Class<V> valsClass) throws IllegalArgumentException
    {
        if (null == (_objClass=valsClass))
            throw new IllegalArgumentException("No values class provided");

        setOpaque(true);
        setHorizontalAlignment(LEFT);
        setVerticalAlignment(CENTER);
        setBorder(BorderFactory.createEmptyBorder());
    }

    protected void handleUnknownValueElement (final int index, final Object value)
    {
        throw new UnsupportedOperationException("handleUnknownValueElement(index=" + index + ") unknown object: " + value);
    }

    public abstract Icon getValueIcon (int index, V value);
    /*
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    @Override
    public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
        if (list != null)
        {
            if (isSelected)
            {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            }
            else
            {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
        }

        // set the icon and text.  If icon was null, say so.
        if (value instanceof Map.Entry<?,?>)
        {
            final Map.Entry<?,?>    elem=(Map.Entry<?,?>) value;
            final Object            t=elem.getKey(), v=elem.getValue();
            setText((null == t) ? null : t.toString());

            final Class<?>    vc=(null == v) ? null : v.getClass();
            final Class<V>    oc=getValuesClass();
            if ((null == vc) /* allow null value type */ || oc.isAssignableFrom(vc))
            {
                final V        av=(null == v) ? null : oc.cast(v);
                final Icon    icon=getValueIcon(index, av);
                if (icon != null)
                    setIcon(icon);
            }
            else
                handleUnknownValueElement(index, value);
        }
        else
            handleUnknownValueElement(index, value);

        return this;
    }
}
