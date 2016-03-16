/*
 *
 */
package net.community.chest.math.test;

import net.community.chest.math.RomanNumeral;

import org.junit.Assert;
import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jun 13, 2011 12:38:24 PM
 */
public class RomanNumeralTest extends Assert {
    public RomanNumeralTest ()
    {
        super();
    }

    @Test
    public void testFromString ()
    {
        for (final RomanNumeral expected : RomanNumeral.VALUES)
        {
            final String    name=expected.name();
            assertSame("Mismatched 'fromString' result", expected, RomanNumeral.fromString(name));
            assertNull("Unexpected case insensitive result for " + name, RomanNumeral.fromString(name.toLowerCase()));
            assertNull("Unexpected long name result for " + name, RomanNumeral.fromString(name + "III"));
        }
    }

    @Test
    public void testNumeralConversion ()
    {
        final String[]    VALUES={
                "MCMLXV",     "1965",
                "MMXI",          "2011",
                "MCMXLIV",    "1944",
                "MCDXCII",    "1492",
                "MCMXCVII",    "1997",
                "MCMXCIII",    "1993",
                "MMIII",    "2003"
            };
        for (int    vIndex=0; vIndex < VALUES.length; vIndex += 2)
        {
            final String    romanValue=VALUES[vIndex], numValue=VALUES[vIndex + 1];
            final int        intValue=Integer.parseInt(numValue);
            assertEquals("Mismatched number value for " + romanValue, intValue, RomanNumeral.fromRoman(romanValue));
            assertEquals("Mismatched roman value for " + numValue, romanValue, RomanNumeral.toRoman(intValue));
        }
    }
}
