package net.community.chest.swing.models;

import java.util.Collection;
import java.util.Map;

/**
 * Copyright 2007 as per GPLv2
 *
 * Implemented by various "typed" display components where each displayed item
 * may have a text that differs from its {@link Object#toString()}
 * implementation (e.g., combo boxes, list boxes, etc.)
 *
 * @param <V> The type of value associated with each row
 * @author Lyor G.
 * @since Jul 5, 2007 9:39:36 AM
 */
public interface TypedDisplayModeler<V> {
    /**
     * @param item item to be added to the display - ignored if null. The
     * {@link java.util.Map.Entry} key is the text to be displayed for the item
     * represented by the associated entry value
     * @throws IllegalArgumentException if null/empty text for item
     * (<B>Note:</B> some implementors may decide not to throw it)
     */
    void addItem (final Map.Entry<String,? extends V> item) throws IllegalArgumentException;
    /**
     * @param text text to be displayed for the added item
     * @param value associated item value (may be null)
     * @return the generated {@link java.util.Map.Entry} &quot;pair&quot;
     * @throws IllegalArgumentException if null/empty text for item
     * (<B>Note:</B> some implementors may decide not to throw it)
     */
    Map.Entry<String,V> addItem (final String text, final V value) throws IllegalArgumentException;
    /**
     * Calls {@link #addItem(java.util.Map.Entry)} for each of the items in
     * the supplied {@link Collection} - ignoring null entries
     * @param items items to be added - if null/empty then nothing is added
     */
    void addItems (final Collection<? extends Map.Entry<String,? extends V>> items);
    /**
     * Adds all items in the entries set of the map via call to {@link #addItems(Collection)}
     * @param items items to be added - if null/empty then nothing is added
     */
    void addItems (final Map<String,? extends V> items);
    /**
     * Adds all items to the display (ignores null ones...)
     * @param items items to be added to the model
     */
    void addItems (final Map.Entry<String, V> ... items);
    /**
     * @param value item value to be added for which a name is requested
     * @return text to use - default <I>toString</I> of the supplied value
     * <B>Note:</B> you must override this method if null values are allowed
     * (have some "display-able" meaning" since by default this method returns
     * null if null value to begin with.
     * @see #addValue(Object)
     * @see #addItem(String, Object)
     */
    String getValueDisplayText (final V value);
    /**
     * @param value item to be added - associated display text is taken via
     * call to {@link #getValueDisplayText(Object)}. <B>Note:</B> null
     * value(s) are <U>not</U> ignored since they might have some meaning
     * (and an associated text)
     * @return the generated {@link java.util.Map.Entry} &quot;pair&quot;
     */
    Map.Entry<String,V> addValue (V value);
    /**
     * Calls {@link #addValue(Object)} for each member of the supplied {@link Collection}
     * @param vals values to be added - if null/empty collection nothing added
     * <B>Note:</B> null values are <U>not</U> ignored since they might have
     * some meaning
     */
    void addValues (final Collection<? extends V> vals);
    /**
     * Calls {@link #addValue(Object)} for each member of the supplied array
     * @param vals values to be added - if null/empty array nothing added.
     * <B>Note:</B> null values are <U>not</U> ignored since they might have
     * some meaning
     */
    void addValues (final V ... vals);
    /**
     * @return number of current items in the display modeler
     */
    int getItemCount ();
    /**
     * @param index index of requested item
     * @return registered item - null if invalid index
     */
    Map.Entry<String,V> getItemAt (int index);
    /**
     * @param index index of item whose value is to be retrieved
     * @return associated value - null if illegal index. <B>Note:</B> if
     * null values have a meaning then this method cannot be used in order
     * to distinguish between bad index and "real" null value(s).
     */
    V getItemValue (int index);
    /**
     * @param index index of item whose value is to be retrieved
     * @return associated text - null if illegal index. <B>Note:</B> empty
     * text(s) may have a meaning so only null indicates an illegal index
     */
    String getItemText (int index);
}
