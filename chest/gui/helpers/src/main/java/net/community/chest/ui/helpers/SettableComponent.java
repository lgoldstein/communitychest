/*
 *
 */
package net.community.chest.ui.helpers;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Used to indicate components that change their UI according to some
 * set value (e.g., panels, dialogs).</P>
 *
 * @param <V> The type of object expected by the component
 * @author Lyor G.
 * @since Jan 1, 2009 10:22:26 AM
 */
public interface SettableComponent<V> {
    /**
     * Called to set the UI "from scratch"
     * @param value The value to use for initialization
     */
    void setContent (V value);
    /**
     * Called to indicate that the value used in {@link #setContent(Object)}
     * call may have changed and an UI update is required. Usually should
     * call {@link #setContent(Object)}
     * @param value The <U>updated</U> value.
     */
    void refreshContent (V value);
    /**
     * Called to indicate that the UI display based on some value should
     * be "reset" to a "non-initialized" state
     */
    void clearContent ();
}
