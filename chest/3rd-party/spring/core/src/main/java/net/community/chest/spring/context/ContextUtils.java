/*
 *
 */
package net.community.chest.spring.context;

import org.springframework.beans.factory.BeanFactory;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 11, 2010 4:24:24 PM
 */
public final class ContextUtils {
    private ContextUtils ()
    {
        // no instance
    }
    /**
     * @param <T> Expected bean type
     * @param ctx The {@link BeanFactory} to query - ignored if <code>null</code>
     * (<B>Note:</B> an {@link org.springframework.context.ApplicationContext} is
     * also a {@link BeanFactory} so it may be used directly)
     * @param id Bean ID string - ignored if <code>null</code>/empty
     * @param beanClass The expected bean {@link Class}
     * @return The bean value cast to the expected type - <code>null</code> if
     * bean not defined or no context/ID provided
     * @throws ClassCastException If retrieved bean cannot be cast to the specified type
     * @see BeanFactory#getBean(Class)
     */
    public static final <T> T getOptionalBean (BeanFactory ctx, String id, Class<T> beanClass)
        throws ClassCastException
    {
        if ((null == ctx) || (null == id) || (id.length() <= 0))
            return null;

        // avoid BeanNotFoundException if bean not defined
        if (!ctx.containsBean(id))
            return null;

        return ctx.getBean(id, beanClass);
    }
}
