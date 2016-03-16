/*
 *
 */
package net.community.chest.math.test.euler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.math.RomanNumeral;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The rules for writing Roman numerals allow for many ways of writing each
 * number. However, there is always a "best" way of writing a particular
 * number so that it contains the least number of characters allowed for
 * it. The <I>roman.txt<I> file, contains one many numbers written in valid,
 * but not necessarily minimal, Roman numerals.
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jun 13, 2011 12:58:43 PM
 */
public class Problem89Test extends Assert {
    public Problem89Test ()
    {
        super();
    }

    private static final Map<String,Integer>    _valuesMap=new TreeMap<String,Integer>();
    private static final List<String>    _valuesList=new ArrayList<String>(1000);

    @BeforeClass
    public static void readTestNumerals () throws IOException
    {
        final InputStream    in=Problem89Test.class.getResourceAsStream("roman.txt");
        assertNotNull("Cannot find input file", in);
        try
        {
            final BufferedReader    rdr=new BufferedReader(new InputStreamReader(in));
            try
            {
                for (String    value=rdr.readLine(); value != null; value=rdr.readLine())
                {
                    if (value.charAt(value.length() - 1) == '\n')
                        value = value.substring(0, value.length() - 1);
                    if (value.charAt(value.length() - 1) == '\r')
                        value = value.substring(0, value.length() - 1);

                    value = value.trim();
                    if ((value.length() <= 0) || (value.charAt(0) == '#'))
                        continue;    // ignore empty lines and comments

                    final Integer    number=Integer.valueOf(RomanNumeral.fromRoman(value)),
                                    prev=_valuesMap.put(value, number);
                    if (prev != null)
                        System.err.println("Multiple pattern: " + value);
                    else
                        _valuesList.add(value);
                }
            }
            finally
            {
                rdr.close();
            }
        }
        finally
        {
            in.close();
        }
    }

    @Test
    public void solve ()
    {
        for (final String strValue : _valuesList)
            solve(strValue, _valuesMap.get(strValue));
    }

    private void solve (final String strValue, final Number expValue)
    {
        final CharSequence    minString=RomanNumeral.minimizeRepresentation(strValue);
        final Number        actValue=Integer.valueOf(RomanNumeral.fromRoman(minString));
        assertNotNull(strValue + " not decoded", expValue);
        assertEquals("Mismatched minimal form for " + strValue + " => " + minString, expValue, actValue);
        System.out.append('\t').append(strValue).append(" => ").append(minString).append(" (").append(expValue.toString()).println(")");
    }
}
