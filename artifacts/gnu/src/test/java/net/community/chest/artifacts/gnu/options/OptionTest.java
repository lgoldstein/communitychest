/*
 *
 */
package net.community.chest.artifacts.gnu.options;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import net.community.chest.artifacts.gnu.options.Option.OptionParseResult;

import org.junit.Assert;
import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 9, 2011 10:31:50 AM
 */
public class OptionTest extends Assert {
    public OptionTest ()
    {
        super();
    }

    @Test
    public void testNullOrEmptyArgumentsParsing ()
    {
        assertParseResult(0, 0, (String[]) null);
        assertParseResult(0, 0, new String[0]);
    }

    @Test
    public void testIllegalArgumentsParsing ()
    {
        for (final String argVals : new String[] { "-x=", "-xa=3" })
        {
            try
            {
                final OptionParseResult        result=Option.parseArguments(argVals);
                fail("Unexpected success for arg=" + argVals + ": " + result);
            }
            catch(IllegalArgumentException e)
            {
                // expected - ignored
            }
        }
    }

    @Test
    public void testNormalParsing ()
    {
        final OptionParseResult        result=assertParseResult(4, 4, "-a", "-b2", "--ccc", "--ddd=4", "end");
        final Map<String,Option>    optsMap=Option.toMap(result.getParsedOptions());
        assertOptionContents(optsMap.get("a"), "a", null);
        assertOptionContents(optsMap.get("b"), "b", "2");
        assertOptionContents(optsMap.get("ccc"), "ccc", null);
        assertOptionContents(optsMap.get("ddd"), "ddd", "4");
        assertEquals("Mismatched last argument value", "end", result.getArguments()[result.getNumParsed()]);
    }

    @Test
    public void testMappingConverter ()
    {
        final Collection<? extends Option>    OPTIONS=
            Collections.unmodifiableList(Arrays.asList(
                new Option("a", "b"),
                new Option("xxx", "1234"),
                new Option("key")
            ));
        final Map<String,? extends Option>    optsMap=Option.toMap(OPTIONS);

        assertEquals("Mismatched number of mapped options", OPTIONS.size(), optsMap.size());
        for (final Option option : OPTIONS) {
            final String    key=option.getKey();
            assertSame("Mismatched instance for key=" + key, option, optsMap.get(key));
        }
    }

    @Test(expected=IllegalStateException.class)
    public void testDuplicateOptionsMapping ()
    {
        final Collection<? extends Option>    OPTIONS=
            Collections.unmodifiableList(Arrays.asList(
                new Option("a", "b"),
                new Option("a", "1234")
            ));
        final Map<String,? extends Option>    optsMap=Option.toMap(OPTIONS);
        fail("Unexpected duplicate mapping success: " + optsMap);
    }

    @Test
    public void testFindFirstMatchingOption ()
    {
        final Option                        aOption=new Option("a", "1"),
                                            bOption=new Option("b"),
                                            cOption=new Option("c", "3");
        final Map<String,? extends Option>    optsMap=Option.toMap(aOption, bOption, cOption);
        assertSame("Not found aOption", aOption, Option.findFirstMatchingOption(optsMap, "a", "b", "c"));
        assertSame("Not found bOption", bOption, Option.findFirstMatchingOption(optsMap, "b", "a", "c"));
        assertSame("Not found cOption", cOption, Option.findFirstMatchingOption(optsMap, "c", "a", "b"));
        assertNull("Unexpected match for uppercase option", Option.findFirstMatchingOption(optsMap, "A", "AA"));
    }

    private OptionParseResult assertParseResult (int expectedNumParsed, int expectedNumOptions, String ... args)
    {
        final OptionParseResult    result=Option.parseArguments(args);
        assertEquals("Mismatched argument index", expectedNumParsed, result.getNumParsed());
        assertEquals("Mismatched number of options", expectedNumOptions, result.getParsedOptions().size());
        assertNotNull("Null-ified arguments", result.getArguments());
        return result;
    }

    private Option assertOptionContents (Option option, String key, String value)
    {
        assertNotNull("No option to assert", option);
        assertEquals("Mismatched key", key, option.getKey());
        assertEquals("Mismatched value", value, option.getValue());
        return option;
    }
}
