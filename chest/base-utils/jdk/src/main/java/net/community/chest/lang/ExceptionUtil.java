/*
 * 
 */
package net.community.chest.lang;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.management.MBeanException;
import javax.management.ReflectionException;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 22, 2008 2:17:10 PM
 */
public final class ExceptionUtil {
	private ExceptionUtil ()
	{
		// no instance
	}
	/**
	 * Converts a {@link String} that encodes a {@link StackTraceElement}
	 * according to its {@link StackTraceElement#toString()} format (e.g.,
	 * <code>foo.bar.FooBar.myMethod(FooBar.java:96)</code>
	 * @param s The {@link String} input - may be null/empty, in which case
	 * a <code>null</code> result is returned
	 * @return The parsed {@link StackTraceElement} - <code>null</code> if
	 * null/empty argument to begin with
	 * @throws IllegalArgumentException If bad format
	 * @throws NumberFormatException If bad/missing line number
	 */
	public static final StackTraceElement fromString (final String s)
		throws IllegalArgumentException
	{
		final int	sLen=(null == s) ? 0 : s.length();
		if (sLen <= 0)
			return null;

		final int	fStart=s.lastIndexOf('('), fEnd=s.lastIndexOf(')');
		if ((fStart <= 0) || (fEnd <= 0) || (fEnd <= (fStart+1)))
			throw new IllegalArgumentException("fromString(" + s + ") missing file part");

		final String	locPart=s.substring(0, fStart),
						fPart=s.substring(fStart+1, fEnd);
		final int		locLen=locPart.length(), fLen=fPart.length(),
						mPos=locPart.lastIndexOf('.'),
						lPos=fPart.lastIndexOf(':');
		if ((mPos <= 0) || (mPos >= (locLen-1)))
			throw new IllegalArgumentException("fromString(" + s + ") bad class+method part: " + locPart);

		final String	clsPart=locPart.substring(0, mPos),
						mthPart=locPart.substring(mPos + 1),
						fileName;
		final int		lineNum;
		if (lPos >= 0)
		{
			if ((0 == lPos) || (lPos >= (fLen-1)))
				throw new IllegalArgumentException("fromString(" + s + ") bad file+line part: " + fPart);

			final String	linePart=fPart.substring(lPos + 1);
			try
			{
				lineNum = Integer.parseInt(linePart); 
			}
			catch(NumberFormatException nfe)
			{
				throw new IllegalArgumentException("fromString(" + s + ") bad line number (" + linePart + "): " + nfe.getMessage());
			}

			fileName = fPart.substring(0, lPos);
		}
		else
		{
			fileName = fPart;
			lineNum = 0;
		}

		// TODO handle (Native Method) - see StackTraceElement#toString()
		return new StackTraceElement(clsPart, mthPart, fileName, lineNum);
	}
	/**
	 * @param s A list of encoded {@link StackTraceElement}-s separated by the
	 * specified character - may be null/empty
	 * @param sepChar The separation character
	 * @return A {@link List} of the parsed {@link StackTraceElement}-s - may
	 * be null/empty if null/empty {@link String} parameter to begin with
	 * @throws IllegalArgumentException If bad format encountered
	 * @see #fromString(String)
	 */
	public static final List<StackTraceElement> fromString (final String s, final char sepChar)
		throws IllegalArgumentException
	{
		final Collection<String>	sl=StringUtil.splitString(s, sepChar);
		final int					numElems=(null == sl) ? 0 : sl.size();
		if (numElems <= 0)
			return null;

		List<StackTraceElement>	ret=null;
		for (final String es : sl)
		{
			final StackTraceElement	el=fromString(es);
			if (null == el)
				continue;

			if (null == ret)
				ret = new ArrayList<StackTraceElement>(numElems);
			ret.add(el);
		}

		return ret;
	}
	/**
	 * @param e The original {@link Throwable}
	 * @param peelWrapper If TRUE then some special exceptions are &quot;peeled&quot;
	 * to get at the real cause (e.g., {@link InvocationTargetException}
	 * @return <P>A {@link RuntimeException} representing the original one:</P></BR>
	 * <UL>
	 * 		<LI><code>null</code> if original was <code>null</code></LI>
	 * 
	 * 		<LI>Same as input if input already a {@link RuntimeException}</LI>
	 * 
	 * 		<LI>A {@link RuntimeException} encapsulating the original one</LI>
	 * </UL>
	 */
	public static final RuntimeException toRuntimeException (final Throwable e, final boolean peelWrapper)
	{
		if (null == e)
			return null;
		if (e instanceof RuntimeException)
			return (RuntimeException) e;
		if (peelWrapper)
		{
            // some known wrappers
            if (e instanceof InvocationTargetException)
                return toRuntimeException(((InvocationTargetException) e).getTargetException(), peelWrapper);
            if (e instanceof MBeanException)
                return toRuntimeException(((MBeanException) e).getTargetException(), peelWrapper);
            if (e instanceof ReflectionException)
                return toRuntimeException(((ReflectionException) e).getTargetException(), peelWrapper);
		}

		return new RuntimeException(e);
	}
	/**
	 * @param e The original {@link Throwable}
	 * @return <P>A {@link RuntimeException} representing the original one:</P></BR>
	 * <UL>
	 * 		<LI><code>null</code> if original was <code>null</code></LI>
	 * 
	 * 		<LI>Same as input if input already a {@link RuntimeException}</LI>
	 * 
	 * 		<LI>A {@link RuntimeException} encapsulating the original one</LI>
	 * </UL>
	 * @see #toRuntimeException(Throwable, boolean)
	 */
	public static final RuntimeException toRuntimeException (final Throwable e)
	{
		return toRuntimeException(e, false);
	}
    /**
     * Provides a way to re-throw any {@link Throwable} without having to
     * declare the caller as <I>throws</I> that exception. Based on
     * <A HREF="http://java.dzone.com/articles/throwing-undeclared-checked">this article</A>
     * @param t The {@link Throwable} instance to throw
     */
    @SuppressWarnings("synthetic-access")
    public static void rethrowException (Throwable t)
    {
        ExceptionThrower.spit(t);
    }

    private static final class ExceptionThrower
    {
        private static Throwable throwable;

        @SuppressWarnings("unused")
        public ExceptionThrower () throws Throwable
        {
            if (throwable != null)    // just a safety
                throw throwable;
        }

        private static synchronized void spit (Throwable t) {
            if (t == null)
                throw new IllegalArgumentException("No throwable to rethrow");
            
            ExceptionThrower.throwable = t;
            try
            {
                ExceptionThrower.class.newInstance();
            }
            catch(InstantiationException e)
            {
                throw new IllegalStateException("spit(" + t.getClass().getSimpleName() + ")"
                                              + " unexpected instantiation exception: " + e.getMessage());
            }
            catch(IllegalAccessException e)
            {
                throw new IllegalStateException("spit(" + t.getClass().getSimpleName() + ")"
                                              + " unexpected access exception: " + e.getMessage());
            }
            finally
            {
                ExceptionThrower.throwable = null;
            }
        }
    }
}
