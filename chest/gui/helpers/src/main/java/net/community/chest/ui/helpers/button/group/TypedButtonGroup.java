package net.community.chest.ui.helpers.button.group;

import java.util.Iterator;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;

import net.community.chest.lang.TypedValuesContainer;
import net.community.chest.swing.component.button.ButtonGroupReflectiveProxy;

/**
 * Copyright 2007 as per GPLv2
 *
 * @param <B> The type of {@link AbstractButton} being grouped
 * @author Lyor G.
 * @since Jul 16, 2007 3:20:24 PM
 */
public class TypedButtonGroup<B extends AbstractButton> extends ButtonGroup
        implements TypedValuesContainer<B>, Iterable<B> {
    /**
     *
     */
    private static final long serialVersionUID = 242808170322624919L;
    private final Class<B> _btnClass;
    /*
     * @see net.community.chest.TypedValuesContainer#getValuesClass()
     */
    @Override
    public final /* no cheating */ Class<B> getValuesClass ()
    {
        return _btnClass;
    }

    public TypedButtonGroup (Class<B> btnClass)
    {
        if (null == (_btnClass=btnClass))
            throw new IllegalArgumentException(getClass().getName() + "#<init> missing button " + Class.class.getSimpleName() + " spec.");
    }
    /*
     * @see java.lang.Iterable#iterator()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Iterator<B> iterator ()
    {
        // NOTE !!! safe to do it since we only add correct buttons...
        return (Iterator<B>) buttons.iterator();
    }

    protected Map<String,B> updateButtonsMap (final Map<String,B> org, final boolean byCommand) throws IllegalStateException
    {
        return ButtonGroupReflectiveProxy.updateButtonsMap(getValuesClass(), this, org, byCommand);
    }

    private Map<String,B>    _cmdMap    /* =null */;
    public void clearCommandsMap ()
    {
        if (_cmdMap != null)
            _cmdMap.clear();
    }

    public synchronized Map<String,B> getCommandsMap ()
    {
        return updateButtonsMap(_cmdMap, true);
    }

    private Map<String,B>    _txtMap    /* =null */;
    public void clearTextsMap ()
    {
        if (_txtMap != null)
            _txtMap.clear();
    }

    public synchronized Map<String,B> getTextsMap ()
    {
        return updateButtonsMap(_txtMap, false);
    }

    public void clearButtonsMaps ()
    {
        clearCommandsMap();
        clearTextsMap();
    }

    public void addButton (final B btn)
    {
        if (btn != null)
        {
            super.add(btn);
            clearButtonsMaps();
        }
    }
    /*
     * @see javax.swing.ButtonGroup#add(javax.swing.AbstractButton)
     * @throws ClassCastException if incompatible button class
     */
    @Override
    public void add (final AbstractButton b) throws ClassCastException
    {
        if (b != null)
        {
            final Class<?>    bClass=b.getClass();
            final Class<B>    gClass=getValuesClass();
            if ((bClass != null) && (gClass != null) && gClass.isAssignableFrom(bClass))
                addButton(gClass.cast(b));
            else
                throw new ClassCastException(getClass().getName() + "#add(" + b + ") incompatible button class: " + ((null == bClass) ? null : bClass.getName()) + " vs. " + ((null == gClass) ? null : gClass.getName()));
        }
    }
    /*
     * @see javax.swing.ButtonGroup#remove(javax.swing.AbstractButton)
     */
    @Override
    public void remove (AbstractButton b)
    {
        super.remove(b);
        clearButtonsMaps();
    }
}
