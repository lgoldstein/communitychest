package net.community.chest.lang;

/**
 * Copyright 2007 as per GPLv2
 * 
 * @param <V> Type of contained object(s)
 * @author Lyor G.
 * @since Jul 9, 2007 3:10:40 PM
 */
public interface TypedValuesContainer<V> {
	/**
	 * @return {@link Class} of contained values
	 */
	Class<V> getValuesClass ();
}
