/*
 *
 */
package net.community.chest.javaagent.dumper.filter;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.junit.Assert;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 11, 2011 3:24:14 PM
 */
public abstract class AbstractFilterTest extends Assert {
    protected AbstractFilterTest ()
    {
        super();
    }

    protected String assertCompiledRegexpPattern (String pattern)
    {
        final String    rgx=PatternClassFilter.convertToRegexp(pattern);
        try
        {
            final Pattern    p=Pattern.compile(rgx);
            if (p == null)
                throw new PatternSyntaxException("No pattern generated", rgx, (-1));
        }
        catch(PatternSyntaxException e)
        {
            fail("Exception while compiling " + rgx + ": " + e.getDescription());
        }

        return rgx;
    }

    protected void assertFilterResult (final ClassFilter filter, final Boolean expResult, final String ... names)
    {
        for (final String className : names)
            assertEquals("Mismatched acceptance for " + filter + "[" + className + "]", expResult, Boolean.valueOf(filter.accept(className)));
    }

    protected void assertFilterResult (final ClassFilter filter, final Boolean expResult, final Class<?> ... classes)
    {
        for (final Class<?> clazz : classes)
        {
            final String    className=clazz.getName();
            assertEquals("Mismatched acceptance for " + filter + "[" + className + "]", expResult, Boolean.valueOf(filter.accept(className)));
        }
    }
}
