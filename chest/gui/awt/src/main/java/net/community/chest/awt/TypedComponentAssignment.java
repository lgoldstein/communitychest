/*
 *
 */
package net.community.chest.awt;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Represents a UI component that has an "assigned" hidden value</P>
 *
 * @param <V> The assigned" hidden value type
 * @author Lyor G.
 * @since Dec 11, 2008 10:07:49 AM
 */
public interface TypedComponentAssignment<V> {
    V getAssignedValue ();
    void setAssignedValue (V value);
}
