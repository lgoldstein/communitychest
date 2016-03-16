package net.community.chest;

import net.community.chest.lang.TypedValuesContainer;
import net.community.chest.reflect.ClassUtil;

/**
 * Copyright 2007 as per GPLv2
 *
 * Used by various classes that hold a {@link Class} instance of some generic
 * typed class
 *
 * @param <V> Type of contained object(s)
 * @author Lyor G.
 * @since Jul 9, 2007 3:01:23 PM
 */
public class BaseTypedValuesContainer<V> implements TypedValuesContainer<V> {
    private final Class<V> _objClass;
    /*
     * @see net.community.chest.TypedValuesContainer#getValuesClass()
     */
    @Override
    public final /* no cheating */ Class<V> getValuesClass ()
    {
        return _objClass;
    }
    /**
     * Good for informative text of the exception location - inefficient, so
     * use sparingly (only for exceptions)
     * @param location location indication
     * @return class name + "#" + location
     */
    protected String getExceptionLocation (String location)
    {
        return ClassUtil.getExceptionLocation(getClass(), location);
    }
    /**
     * Good for informative text of exception location that also has
     * some arguments
     * @param location base location
     * @param args arguments - added as a '['/']' comma delimited list
     * @return formatted string
     */
    protected String getArgumentsExceptionLocation (String location, Object... args)
    {
        return ClassUtil.getArgumentsExceptionLocation(getClass(), location, args);
    }
    /**
     * Informative text for exceptions thrown by constructors
     * @return class name + "#<init>"
     */
    protected String getConstructorExceptionLocation ()
    {
        return ClassUtil.getConstructorExceptionLocation(getClass());
    }
    /**
     * Good for informative text of exception in constructor that also has
     * some arguments
     * @param args arguments - added as a '['/']' comma delimited list
     * @return formatted string
     */
    protected String getConstructorArgumentsExceptionLocation (final Object... args)
    {
        return ClassUtil.getConstructorArgumentsExceptionLocation(getClass(), args);
    }
    /**
     * @param objClass contained values {@link Class}
     * @throws IllegalArgumentException if null {@link Class} instance provided
     */
    public BaseTypedValuesContainer (final Class<V> objClass) throws IllegalArgumentException
    {
        if (null == (_objClass=objClass))
            throw new IllegalArgumentException(getConstructorExceptionLocation() + " no value class specified");
    }
}
