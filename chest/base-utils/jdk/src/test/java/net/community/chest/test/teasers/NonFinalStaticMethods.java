/*
 *
 */
package net.community.chest.test.teasers;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>Shows the effects of non-<code>final</code> static methods
 * @author Lyor G.
 * @since Dec 22, 2010 7:57:30 AM
 */
public class NonFinalStaticMethods {
    public static class Foo {
        public static void doIt () { System.out.println("\tFoo.doingIt"); }
    }

    public static class Bar extends Foo {
        public static void doIt () { System.out.println("\tBar.doingIt"); }
    }

    @SuppressWarnings("static-access")
    public static void main (String[] args)
    {
        final Foo    x=new Foo(), y=new Bar();
        System.out.println("Indirect calls");
        x.doIt();
        y.doIt();

        System.out.println("Direct calls");
        Foo.doIt();
        Bar.doIt();
    }
}
