package net.community.chest.lang;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import net.community.chest.AbstractTestSupport;

import org.junit.Test;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 17, 2012 10:29:10 AM
 */
public class ExceptionUtilTest extends AbstractTestSupport {
    public ExceptionUtilTest() {
        super();
    }

    /**
     * Makes sure that if the original exception is already a {@link RuntimeException}
     * then no wrapping occurs - i.e., the original instance is returned (after casting)
     */
    @Test
    public void testUnwrappedException () {
        Throwable   th=new IllegalArgumentException("testUnwrappedException");
        assertTrue("Test un-wrapped exception not runtime", th instanceof RuntimeException);

        RuntimeException    re=ExceptionUtil.toRuntimeException(th);
        assertSame("Mismatched converted instances", th, re);
    }

    /**
     * Makes sure that if the original exception is not a {@link RuntimeException}
     * then it is wrapped into one using the {@link RuntimeException#RuntimeException(Throwable)}
     * constructor, thus making it it's cause value
     */
    @Test
    public void testWrappedException () {
        Throwable   th=new NoSuchFieldException("testWrappedException");
        assertFalse("Test wrapped exception already runtime", th instanceof RuntimeException);

        RuntimeException    re=ExceptionUtil.toRuntimeException(th);
        assertNotSame("Unconverted instances", th, re);
        assertSame("Mismatched wrapped instances", th, re.getCause());
    }

    @Test
    public void testPeeledException () {
        RuntimeException    ex=new IllegalArgumentException("peeled");
        Throwable           th=new InvocationTargetException(ex, "wrapper");
        RuntimeException    re=ExceptionUtil.toRuntimeException(th, true);
        assertSame("Mismatched peeled instance", ex, re);
    }

    @Test
    public void testRethrowException ()
    {
        final String msg="testRethrowException";
        for (Throwable expected : new Throwable[] { // mixed checked and unchecked exception, errors and exceptions
                new SQLException(msg),
                new IllegalArgumentException(msg),
                new NoSuchMethodError(msg),
                new UnsupportedOperationException(msg),
                new LinkageError(msg),
                new ClassCastException(msg) })
        {
            try
            {
                ExceptionUtil.rethrowException(expected);
                fail("Unexpected success for " + expected.getClass().getSimpleName());
            }
            catch (Throwable actual)
            {
                assertSame("Mismatched caught exception for " + expected.getClass().getSimpleName()
                         + ": " + actual.getClass().getSimpleName()
                         + "[" + actual.getMessage() + "]",
                           expected, actual);
            }
        }
    }
}
