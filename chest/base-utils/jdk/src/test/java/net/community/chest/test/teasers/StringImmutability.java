/*
 *
 */
package net.community.chest.test.teasers;

import java.io.PrintStream;
import java.lang.reflect.Field;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>Shows that a {@link String} value can be modified via reflection API</P>
 *
 * @author Lyor G.
 * @since Oct 28, 2010 8:52:31 AM
 */
public class StringImmutability {
    private static final String    CHECK_VALUE="Hello, I am an immutable string";
    private static void checkStringImmutability (final PrintStream out, final boolean internIt) throws Exception
    {
        final String    orgValue=internIt ? CHECK_VALUE.intern() : CHECK_VALUE;
        out.append("Before reflection access: ")
           .append(orgValue)
           .println()
           ;

        final Field    valField=String.class.getDeclaredField("value");
        if (!valField.isAccessible())
            valField.setAccessible(true);

        final char[]    orgChars=(char[]) valField.get(orgValue),
                        newChars="Wow, I have been muted ! - help".toCharArray();
        System.arraycopy(newChars, 0, orgChars, 0, Math.min(orgChars.length, newChars.length));
        out.append("After reflection access: ")
           .append(orgValue)
           .println()
           ;
    }

    public static void main (String[] args)
    {
        try
        {
            checkStringImmutability(System.out, false);
            checkStringImmutability(System.out, true);
        }
        catch(Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

}
