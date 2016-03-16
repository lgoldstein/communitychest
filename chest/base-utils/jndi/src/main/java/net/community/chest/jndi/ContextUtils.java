package net.community.chest.jndi;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Useful static methods for handling JNDI {@link Context}-s</P>
 *
 * @author Lyor G.
 * @since Sep 19, 2007 9:37:54 AM
 */
public final class ContextUtils {
    private ContextUtils ()
    {
        // disable instance
    }
    /**
     * Makes sure that the specified context name exists as a sub-context of
     * the supplied "root" context
     * @param ctx "root" context to check/create sub-context of
     * @param name name/path of sub-context to check/create
     * @return sub-context at specified name - may (or may not) have been
     * created (NOTE: may be same as input if "empty" name
     * @throws NamingException if internal errors encountered
     */
    public static final Context createSubContext (final Context ctx, final Name name) throws NamingException
    {
        final int    nSize=(null == name) ? 0 : name.size();
        if ((null == ctx) || (nSize < 0))
            throw new NamingException("no initial context/name for sub-context creation");

        Context curCtx=ctx, lastCtx=null;
        for (int pos=0; pos < nSize; pos++)
        {
            final String    ctxName=name.get(pos);
            Context            subCtx;
            try
            {
                subCtx = (Context) ctx.lookup(ctxName);
            }
            catch (NameNotFoundException e)
            {
                subCtx = ctx.createSubcontext(ctxName);
            }

            // no longer need the parent context
            if (lastCtx != null)
                lastCtx.close();
            // do not close the initial context
            if (curCtx != ctx)
                lastCtx = curCtx;

            // The current subctx will be the ctx for the next name component
            curCtx = subCtx;
        }

        return curCtx;
    }
    /**
     * Makes sure that the specified context name exists as a sub-context of
     * the supplied "root" context
     * @param ctx "root" context to check/create sub-context of
     * @param jndiName name/path of sub-context to check/create - may not be
     * null/empty
     * @return sub-context at specified name - may (or may not) have been
     * created
     * @throws NamingException if internal errors encountered
     */
    public static final Context createSubContext (final Context ctx, final String jndiName) throws NamingException
    {
        if ((null == ctx) || (null == jndiName) || (jndiName.length() <= 0))
            throw new NamingException("No object/JNDI name argument supplied");

        return createSubContext(ctx, ctx.getNameParser("").parse(jndiName));
    }
    /**
     * Binds the specified object at the provided JNDI location
     * @param rootCtx root context realtive to which the JNDI location is
     * specified.
     * @param o object to be bound to the location - may be null
     * @param fullName relative path to the root context - if the sub-context
     * does not exist it will be created
     * @return bound object - should be same as original parameter
     * @throws NamingException if problems encountered
     */
    public static final Object bindJNDIObject (final Context rootCtx, final Object o, final Name fullName) throws NamingException
    {
        final int    fnLen=(null == fullName) ? 0 : fullName.size();
        if ((null == o) || (fnLen <= 0))
            throw new NamingException("No object/JNDI full name argument supplied");

        if (fnLen > 1)
        {
            final Context    subCtx=createSubContext(rootCtx, fullName.getPrefix(fnLen - 1));
            if (subCtx != rootCtx)
                subCtx.close();    // nice cleanup of unused object
        }

        rootCtx.bind(fullName, o);
        return o;
    }
    /**
     * Binds the specified object at the provided JNDI location
     * @param rootCtx root context realtive to which the JNDI location is
     * specified.
     * @param o object to be bound to the location - may be null
     * @param jndiName relative path to the root context - if the sub-context
     * does not exist it will be created (Note: may not be null/empty)
     * @return bound object - should be same as original parameter
     * @throws NamingException if problems encountered
     */
    public static final Object bindJNDIObject (final Context rootCtx, final Object o, final String jndiName) throws NamingException
    {
        if ((null == rootCtx) || (null == jndiName) || (jndiName.length() <= 0))
            throw new NamingException("No object/JNDI name argument supplied");

        return bindJNDIObject(rootCtx, o, rootCtx.getNameParser("").parse(jndiName));
    }
}
