package net.community.chest.lang;

/**
 * Copyright 2007 as per GPLv2
 *
 * Used to automatically promote {@link Object#clone()} to <I>public</I> status
 *
 * @param <V> Type of cloned value
 * @author Lyor G.
 * @since Jul 4, 2007 7:34:38 AM
 */
public interface PubliclyCloneable<V> extends Cloneable {
    /**
     * @return cloned object
     * @throws CloneNotSupportedException if unable to clone it
     */
    V /* NOTE !!! causes co-variant return */ clone () throws CloneNotSupportedException;
}
