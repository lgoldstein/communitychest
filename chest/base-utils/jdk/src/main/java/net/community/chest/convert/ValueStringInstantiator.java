package net.community.chest.convert;

import net.community.chest.lang.TypedValuesContainer;

/**
 * Copyright 2007 as per GPLv2
 *
 * Interface used by implementors of object "factory" using a single
 * {@link String} argument
 *
 * @param <V> Type of constructed object(s)
 * @author Lyor G.
 * @since Jul 10, 2007 1:15:19 PM
 */
public interface ValueStringInstantiator<V> extends TypedValuesContainer<V> {
    /**
     * @param s {@link String} to pass to instantiator - may be
     * null/empty
     * @return generated object
     * @throws Exception unable to generate the object (including if a
     * null/empty string is not allowed by the specific class instantiator)
     */
    V newInstance (String s) throws Exception;
    /**
     * @param inst instance to be converted to {@link String} - may be null
     * @return {@link String} to used to represent the value instance - may
     * be null/empty
     * @throws Exception if unable to convert to string
     */
    String convertInstance (V inst) throws Exception;
}
