package net.community.chest.test.teasers;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Shows that non-static initializers are called <U>before</U> the
 * constructor code</P>
 *
 * @author Lyor G.
 * @since May 22, 2008 8:01:02 AM
 */
public final class PreConstructorCode {
    private PreConstructorCode ()
    {
        // do nothing
    }

    private static class BaseClass {
        protected BaseClass ()
        {
            System.out.println("BaseClass constructor");
        }
        /*
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString ()
        {
            return getClass().getSimpleName();
        }
    }

    private static class DerivedClass extends BaseClass {
        {
            System.out.println("DerivedClass pre-constructor");
        }
        protected DerivedClass ()
        {
            System.out.println("DerivedClass constructor");
        }
    }

    public static void main (String[] args)
    {
        try
        {
            System.out.println(new DerivedClass() + " ready");
        }
        catch(Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
