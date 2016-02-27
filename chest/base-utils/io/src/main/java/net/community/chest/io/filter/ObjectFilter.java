/*
 * 
 */
package net.community.chest.io.filter;

import net.community.chest.lang.TypedValuesContainer;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * <P>Useful interface for any input filtering</P>
 * @param <V> Type of object being considered 
 * @author Lyor G.
 * @since Jan 28, 2010 5:30:16 PM
 */
public interface ObjectFilter<V> extends TypedValuesContainer<V> {
	/**
	 * @param value Value being considered
	 * @return TRUE if value &quot;passes&quot; the filter or not
	 */
	boolean accept (V value);
}
