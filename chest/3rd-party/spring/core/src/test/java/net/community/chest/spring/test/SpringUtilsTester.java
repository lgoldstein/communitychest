/*
 *
 */
package net.community.chest.spring.test;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.Serializable;

import org.springframework.util.ClassUtils;

import net.community.chest.test.TestBase;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 23, 2010 7:25:14 AM
 */
public class SpringUtilsTester extends TestBase {
    public SpringUtilsTester ()
    {
        super();
    }

    //////////////////////////////////////////////////////////////////////////

    // see LangTester for test with same name but different implementation
    public static final void testInterfacesInheritance (
            final PrintStream out, final BufferedReader in, final Object o)
    {
        final Class<?>    c=o.getClass();
        for ( ; ; )
        {
            out.append(c.getName()).append(" interfaces:").println();
            for (final Class<?> ifc : ClassUtils.getAllInterfacesAsSet(o))
                out.append('\t').append(ifc.getSimpleName()).println();

            final String    ans=getval(out, in, "again [y]/n");
            if ((ans != null) && (ans.length() > 0) && (Character.toLowerCase(ans.charAt(0)) != 'y'))
                break;
        }
    }

    /* -------------------------------------------------------------------- */

    private static interface Ifc1 extends CharSequence, Serializable {
        void helloWorld ();
    }

    private static class Ifc1Impl implements Ifc1 {
        /**
         *
         */
        private static final long serialVersionUID = 1723188226441111237L;
        public Ifc1Impl ()
        {
            super();
        }
        /*
         * @see java.lang.CharSequence#length()
         */
        @Override
        public int length ()
        {
            return 0;
        }
        /*
         * @see java.lang.CharSequence#charAt(int)
         */
        @Override
        public char charAt (int index)
        {
            return '\0';
        }
        /*
         * @see java.lang.CharSequence#subSequence(int, int)
         */
        @Override
        public CharSequence subSequence (int start, int end)
        {
            return this;
        }
        /*
         * @see net.community.chest.test.LangTester.Ifc1#helloWorld()
         */
        @Override
        public void helloWorld ()
        {
            // do nothing
        }
    }

    /* -------------------------------------------------------------------- */

    // see LangTester for test with same name but different implementation
    public static final void testInterfacesInheritance (
            final PrintStream out, final BufferedReader in, final String ... args)
    {
        /*         This test exposes the fact that ClassUtils.getAllInterfacesAsSet
         * does not handle transitive interfaces inheritance as shown in this example
         */
        testInterfacesInheritance(out, in, new Ifc1Impl());
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (String[] args)
    {
        final BufferedReader    in=getStdin();
        testInterfacesInheritance(System.out, in, args);
    }

}
