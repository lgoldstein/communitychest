/*
 *
 */
package net.community.chest.math.test.euler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * A common security method used for online banking is to ask the user for
 * three random characters from a passcode. For example, if the passcode was
 * 531278, they may ask for the 2nd, 3rd, and 5th characters; the expected
 * reply would be: 317. Given the 50 login attempts below, and that the three
 * characters are always asked for in order, analyze them so as to determine
 * the shortest possible secret passcode of unknown length.
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jun 13, 2011 8:37:28 AM
 * @see <A HREF="http://projecteuler.net/index.php?section=problems&id=79">Problem 79</A>
 */
public class Problem79Test extends Assert {
    public Problem79Test ()
    {
        super();
    }

    @Test
    public void testValidatePasscode ()
    {
        validatePasscode("531278", Arrays.asList("317", "527", "178", "531", "278"));
    }

    @Test
    public void testPasscodeValidationFailures ()
    {
        final String    PASSCODE="531278";
        final String[]    BAD_VALUES={
                "582",    // digits in wrong order
                "179",    // digits in right order but one of them is missing
                "904"    // no digits in original passcode
            };
        for (final String v : BAD_VALUES)
        {
            try
            {
                validatePasscode(PASSCODE, v);
                fail("Unexpected validation of pattern=" + v);
            }
            catch(AssertionError e)
            {
                // OK expected
            }
        }
    }

    @Test
    public void testDefaultSolution ()
    {
        final CharSequence    result=solveDefault();
        System.out.append("Result: ").append(result).println();
    }

    private final List<String>    VALUES=Arrays.asList(
            "319", "680", "180", "690", "129", "620", "762", "689", "762", "318",
            "368", "710", "720", "710", "629", "168", "160", "689", "716", "731",
            "736", "729", "316", "729", "729", "710", "769", "290", "719", "680",
            "318", "389", "162", "289", "162", "718", "729", "319", "790", "680",
            "890", "362", "319", "760", "316", "729", "380", "319", "728", "716"
        );
    @Test
    @Ignore    // TODO some permutations yield bad results
    public void testRandomizedSolution ()
    {
        final List<String>    TEST_VALUES=new ArrayList<String>(VALUES);
        final Random        rnd=new Random(System.nanoTime());
        final CharSequence    defaultValue=solveDefault().toString();
        for (int    tIndex=0, numValues=VALUES.size(); tIndex < Byte.MAX_VALUE; tIndex++)
        {
            for (int    vIndex=0; vIndex < numValues; vIndex++)
            {
                final int    i1=rnd.nextInt(numValues), i2=rnd.nextInt(numValues);
                if (i1 == i2)
                    continue;

                final String    v1=TEST_VALUES.get(i1), v2=TEST_VALUES.get(i2);
                TEST_VALUES.set(i1, v2);
                TEST_VALUES.set(i2, v1);
            }

            final CharSequence    testValue=solve(TEST_VALUES).toString();
            if (!defaultValue.equals(testValue))
            {
                System.err.append("===> ")
                          .append(testValue)
                          .append(": ")
                          .append(Arrays.toString(TEST_VALUES.toArray(new String[numValues])))
                        .println();
            }
        }
    }

    private CharSequence solveDefault ()
    {
        return solve(VALUES);
    }

    private CharSequence solve (final List<? extends CharSequence> values)
    {
        final CharSequence    passcode=resolvePasscode(new StringBuilder(values.size() * 3).append(values.get(0)), values.subList(1, values.size()));
        validatePasscode(passcode, values);
        return passcode;
    }

    // TODO find out a way to ensure the minimum length
    private CharSequence resolvePasscode (final StringBuilder sb, final Collection<? extends CharSequence> org)
    {
        String    lastValue=sb.toString();
        for (Collection<CharSequence>    digits=(org == null) ? null : new ArrayList<CharSequence>(org), deferred=null;
             (digits != null) && (digits.size() > 0);
             digits = deferred, deferred = null)
        {
            for (final CharSequence    pValue : digits)
            {
                final CharSequence    dValue=updatePasscode(sb, pValue);
                if (dValue == null)
                {
                    final String    newValue=sb.toString();
                    if (!lastValue.equals(newValue))    // check if anything changed in the sequence
                    {
                        System.out.append("\t[").append(pValue).append("] => ").append(sb).println();
                        lastValue = newValue;
                    }
                    continue;
                }

                if (deferred == null)
                    deferred = new ArrayList<CharSequence>(digits.size());
                deferred.add(dValue);
            }

            if ((deferred != null) && (deferred.size() == digits.size()))
                throw new IllegalStateException("No new data has been acquired for " + sb.toString());
        }

        return sb;
    }
    // returns null if digits incorporated in passcode
    private CharSequence updatePasscode (final StringBuilder sb, final CharSequence digits)
    {
        if (checkPasscode(sb, digits) == '\0')
            return null;    // if digits already in order, nothing new to gain

        final int    numDigits=digits.length();
        int            lIndex=(-1), pPos=0;
        for (int    dIndex=0; dIndex < numDigits; dIndex++)
        {
            final char    dChar=digits.charAt(dIndex);
            final int    cIndex=indexOf(sb, lIndex + 1, dChar),
                        xIndex=indexOf(sb, dChar);
            // found the digit and it is at the right position
            if (cIndex > lIndex)
            {
                insertIntoPasscode(sb, cIndex, digits, pPos, dIndex);
                pPos = dIndex + 1;
                lIndex = cIndex;
                continue;
            }

            if (cIndex >= 0)    // found the digit but it violates a previous digit order
                throw new IllegalStateException("Digits order violation for " + digits + " in " + sb);

            // found the digit in a previous location - move it beyond lIndex
            if ((xIndex >= 0) && (xIndex < lIndex))
            {
                insertIntoPasscode(sb, lIndex + 1, digits, dIndex, dIndex+1);
                sb.deleteCharAt(xIndex);
                lIndex--;    // compensate for the deletion prior to lIndex
                pPos = dIndex + 1;
            }
        }

        // check if any leftovers
        if ((pPos > 0) && (pPos < numDigits))
        {
            insertIntoPasscode(sb, lIndex + 1, digits, pPos, numDigits);
            return null;
        }

        if (lIndex >= 0)
            return null;

        return digits;    // no digit was processed
    }

    private static StringBuilder insertIntoPasscode (final StringBuilder        sb,
                                                     final int                     topPosition,
                                                     final CharSequence            digits,
                                                     final int /* inclusive */    startPos,
                                                     final int /* exclusive */    endPos)
    {
        return sb.insert(topPosition, digits, startPos, endPos);
    }

    public static final int indexOf (final CharSequence s, final char ch)
    {
        return indexOf(s, 0, ch);
    }

    public static final int indexOf (final CharSequence s, final int startPos, final char ch)
    {
        for (int    sIndex=startPos; sIndex < s.length(); sIndex++)
        {
            if (s.charAt(sIndex) == ch)
                return sIndex;
        }

        return (-1);

    }
    private static CharSequence validatePasscode (final CharSequence passcode, final Collection<? extends CharSequence> digits)
    {
        for (final CharSequence dValue : digits)
            validatePasscode(passcode, dValue);
        return passcode;
    }
    /**
     * Validates that the specified digits appear in the passcode in the same
     * order
     * @param passcode The entire passcode
     * @param digits The expected digits
     * @return Same as input passcode if validation successful
     */
    private static CharSequence validatePasscode (final CharSequence passcode, final CharSequence digits)
    {
        final char    dChar=checkPasscode(passcode, digits);
        if (dChar != '\0')
            fail("Missing digit=" + String.valueOf(dChar));
        return passcode;
    }

    // returns non '\0' if mismatched digit
    private static char checkPasscode (final CharSequence passcode, final CharSequence digits)
    {
        final int    pLen=passcode.length();
        for (int pIndex=0, dIndex=0, fIndex=(-1); dIndex < digits.length(); dIndex++)
        {
            final char    dChar=digits.charAt(dIndex);
            for (fIndex=(-1); (pIndex < pLen) && (fIndex < 0); pIndex++)
            {
                final char    pChar=passcode.charAt(pIndex);
                if (pChar == dChar)
                    fIndex = pIndex;
            }

            if (fIndex < 0)
                return dChar;
        }

        return '\0';
    }
}
