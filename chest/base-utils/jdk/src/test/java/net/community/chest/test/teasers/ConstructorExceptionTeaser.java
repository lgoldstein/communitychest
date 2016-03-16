package net.community.chest.test.teasers;

import java.io.BufferedReader;
import java.lang.reflect.Field;

import net.community.chest.test.TestBase;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * Can an incomplete construction be used to hack a class ?
 *
 * @author Lyor G.
 * @since Mar 12, 2008 11:58:44 AM
 */
public class ConstructorExceptionTeaser extends TestBase {

    static class Foo {
        private final String    m_password;
        Foo (String pass) throws SecurityException
        {
            m_password = "Q29uZ3JhdHVsYXRpb25z";
            if (!m_password.equals(pass))
                throw new SecurityException("Unauthorized access - guess again");
        }
    }
    // class exploiting the above code
    static class Bar extends Foo {
        public static Foo    m_foo    /* =null */;
        public Bar (String pass) throws SecurityException
        {
            super(pass);
        }
        /*
         * @see java.lang.Object#finalize()
         */
        @Override
        protected void finalize () throws Throwable
        {
            m_foo = this;
            System.out.println("\tFinalized");
            super.finalize();
        }

    }
    public static void main (String[] args)
    {
        final int                numArgs=(null == args) ? 0 : args.length;
        final BufferedReader    in=getStdin();
        Field                    f=null;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    pass=(aIndex < numArgs) ? args[aIndex] : getval(System.out, in, "password (or Quit)");
            if (isQuit(pass)) break;

            final String    ans=getval(System.out, in, "use solution [y]/n");
            try
            {
                if (null == f)
                {
                    f = Foo.class.getDeclaredField("m_password");
                    if (!f.isAccessible())
                        f.setAccessible(true);
                }

                if ((null == ans) || (ans.length() <= 0) || ('y' == Character.toLowerCase(ans.charAt(0))))
                {
                    try
                    {
                        System.out.println(new Bar(pass));
                    }
                    catch(Exception e)
                    {
                        System.gc();
                        System.runFinalization();
                    }

                    final Object    v=(null == Bar.m_foo) ? null : f.get(Bar.m_foo);
                    System.out.println("Value=" + v);
                }
                else
                {
                    final Foo        o=new Foo(pass);
                    final Object    v=f.get(o);
                    System.out.println("Value=" + v);
                }
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
        }
    }
}
