package net.community.chest.swing.models;

import java.util.Map;

/**
 * Copyright 2007 as per GPLv2
 *
 * Represents a modeler where items have text(s) and associated values
 *
 * @param <V> The type of value associated with each row
 * @author Lyor G.
 * @since Jul 5, 2007 9:56:27 AM
 */
public interface SingleSelectionModeler<V> {
    /**
     * @return number of current items in selection modeler
     */
    int getItemCount();
    /**
     * @return index of selected item - negative if none selected
     */
    int getSelectedIndex ();
    /**
     * @param index item index to be selected - ignored if out of range
     */
    void setSelectedIndex (int index);
    /**
     * @return {@link java.util.Map.Entry} whose key is the displated item text and
     * value is the associated value
     */
    Map.Entry<String, V> getSelectedItem ();
    /**
     * @return associated value for the selected item
     */
    V getSelectedValue ();
    /**
     * @param value value to be set as currently selected
     * @return index of selected item (negative if no match found)
     */
    int setSelectedValue (V value);
    /**
     * @return text of the selected item
     */
    String getSelectedText ();
}
