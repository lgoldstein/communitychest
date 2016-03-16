package net.community.chest.ui.helpers.combobox;

import java.util.Map;

import net.community.chest.awt.TypedComponentAssignment;
import net.community.chest.util.map.entries.StringMapEntry;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <V> The type of value associated with each row
 * @author Lyor G.
 * @since Mar 19, 2008 3:49:15 PM
 */
public class TypedComboBoxEntry<V> extends StringMapEntry<V> implements TypedComponentAssignment<V> {
    public TypedComboBoxEntry ()
    {
        super();
    }

    public TypedComboBoxEntry (Map.Entry<String, V> e)
    {
        super(e);
    }

    public TypedComboBoxEntry (String key, V value)
    {
        super(key, value);
    }

    public TypedComboBoxEntry (String key)
    {
        super(key);
    }
    /*
     * @see net.community.chest.ui.helpers.TypedComponentAssignment#getAssignedValue()
     */
    @Override
    public V getAssignedValue ()
    {
        return getValue();
    }
    /*
     * @see net.community.chest.ui.helpers.TypedComponentAssignment#setAssignedValue(java.lang.Object)
     */
    @Override
    public void setAssignedValue (V value)
    {
        setValue(value);
    }
    /*
     * @see net.community.chest.util.map.MapEntryImpl#toString()
     */
    @Override
    public String toString ()
    {
        return getKey();
    }
}
