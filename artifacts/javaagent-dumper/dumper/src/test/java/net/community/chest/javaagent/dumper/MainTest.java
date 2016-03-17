/*
 *
 */
package net.community.chest.javaagent.dumper;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jul 25, 2011 1:36:53 PM
 */
public class MainTest extends Assert {
    public MainTest ()
    {
        super();
    }

    @Test
    public void testParseOptions ()
    {
        final String[]    OPTIONS={
                "no.value.1", null,
                "value.1", "1",
                "no.value.2", null,
                "value.2", "2"
            };

        final Map<String,String>    optsMap=Main.parseOptions(buildOptionsString(OPTIONS));
        assertEquals("Mismatched number of options", OPTIONS.length / 2, optsMap.size());

        for (int    oIndex=0; oIndex < OPTIONS.length; oIndex += 2)
        {
            final String    key=OPTIONS[oIndex],
                            valExpected=OPTIONS[oIndex + 1],
                            valActual=optsMap.get(key);
            if (valExpected == null)
                assertEquals("Mismatched value for no-value option=" + key, key, valActual);
            else
                assertEquals("Mismatched value for valued option=" + key, valExpected, valActual);
        }
    }

    @Test
    public void testParseNullOrEmptyOptions ()
    {
        for (final String options : new String[] { null, "" })
            assertTrue("Non-empty options map for " + options, Main.parseOptions(options).isEmpty());
    }

    @Test(expected=IllegalStateException.class)
    public void testParseDuplicateOptions ()
    {
        Main.parseOptions(buildOptionsString("vAl1", "1", "val2", "2", "VaL1", "1"));
        fail("Unexpected parse success of multiple option values specification");
    }

    private static String buildOptionsString (final String ... OPTIONS)
    {
        if ((OPTIONS == null) || (OPTIONS.length <= 0))
            return "";

        final StringBuilder    sb=new StringBuilder(OPTIONS.length * 16);
        for (int    oIndex=0; oIndex < OPTIONS.length; oIndex += 2)
        {
            final String    key=OPTIONS[oIndex], val=OPTIONS[oIndex + 1];
            if (sb.length() > 0)
                sb.append(Main.OPTIONS_SEP);
            sb.append(key);

            if (val != null)
                sb.append(Main.VALUES_SEP).append(val);
        }

        return sb.toString();
    }

}
