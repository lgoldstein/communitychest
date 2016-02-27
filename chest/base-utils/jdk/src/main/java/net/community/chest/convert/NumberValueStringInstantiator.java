package net.community.chest.convert;


/**
 * Copyright 2007 as per GPLv2
 * 
 * Used as a "factory" to convert {@link String} to {@link Number}-s (e.g.,
 * {@link Integer}, {@link Double})
 * 
 * @param <N> Type of constructed {@link Number}
 * @author Lyor G.
 * @since Jul 11, 2007 9:50:01 AM
 */
public interface NumberValueStringInstantiator<N extends Number>
		extends ValueStringInstantiator<N>, ValueNumberInstantiator<N,N> {
	/**
	 * @return the <I>TYPE</I> {@link Class} representing the primitive type
	 */
	Class<N> getPrimitiveValuesClass ();
}
