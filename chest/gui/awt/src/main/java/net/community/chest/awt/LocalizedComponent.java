/*
 *
 */
package net.community.chest.awt;

import java.util.Locale;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Represents a component whose display can differ from one {@link Locale}
 * to another</P>
 *
 * @author Lyor G.
 * @since Dec 16, 2008 8:30:13 AM
 */
public interface LocalizedComponent {
    /**
     * @return The {@link Locale} to be used for localizing the component.
     * <B>Note:</B> recommend always returning a non-<code>null</code> value
     * using {@link Locale#getDefault()} as the default - unless overridden by
     * call to {@link #setDisplayLocale(Locale)}
     */
    Locale getDisplayLocale ();
    void setDisplayLocale (Locale l);
}
