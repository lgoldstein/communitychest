package net.community.chest.convert;

import net.community.chest.lang.TypedValuesContainer;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Useful factory for converting between objects and their numerical
 * representation - e.g., {@link Integer} - {@link Enum} via {@link Enum#ordinal()}</P>
 * 
 * @param <N> Type of constructed {@link Number}
 * @param <V> Type of converted object(s)
 * @author Lyor G.
 * @since Jul 26, 2007 1:39:51 PM
 */
public interface ValueNumberInstantiator<N extends Number,V> extends TypedValuesContainer<V> {
	/**
	 * @param num {@link Number} to pass to instantiator
	 * @return instantiated object
	 * @throws Exception if unable to generate an instance - including if
	 * null number not allowed
	 */
	V newInstance (N num) throws Exception;
	/**
	 * @param inst instance to be converted to a {@link Number}
	 * @return number representing the instance
	 * @throws Exception if unable to generate a number - including if
	 * null instance not allowed
	 */
	N getInstanceNumber (V inst) throws Exception;
}
