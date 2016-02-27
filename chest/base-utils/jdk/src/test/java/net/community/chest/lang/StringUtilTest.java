/*
 * 
 */
package net.community.chest.lang;

import java.util.Arrays;

import javax.management.ObjectName;

import net.community.chest.AbstractTestSupport;

import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Dec 22, 2010 7:17:09 AM
 */
public class StringUtilTest extends AbstractTestSupport {
	public StringUtilTest ()
	{
		super();
	}

	@Test
	public void testStringBuilderBackingArray ()
	{
		final String		testString=getClass().getName();
		final StringBuilder	sb=new StringBuilder().append(testString);
		final char[]		internalArray=StringUtil.getBackingArray(sb);
		assertNotNull("Missing internal array", internalArray);
		assertEquals("Initial test string value", testString, sb.toString());
		assertEquals("Same internal value", testString, new String(internalArray, 0, sb.length()));

		final String	modString=String.valueOf(System.currentTimeMillis());
		final char[]	modArray=modString.toCharArray();
		final int		modLen=Math.min(modArray.length, internalArray.length);
		System.arraycopy(modArray, 0, internalArray, 0, modLen);

		final String	sbString=sb.toString(),
						expectedModPart=(modLen < modString.length()) ? modString.substring(0, modLen) : modString,
						actualModPart=sbString.substring(0, modLen),
						actualUnmodPart=(modLen < sbString.length()) ?  sbString.substring(modLen) : "",
						expectedUnmodPart=(modLen < testString.length()) ? testString.substring(modLen) : "";
		assertEquals("Same modified part", expectedModPart, actualModPart);
		assertEquals("Same un-modified part", expectedUnmodPart, actualUnmodPart);
	}
	
	@Test
	public void testStartsWith ()
	{
		final String	testPrefixString=getClass().getName(),
						upcaseTestPrefixString=testPrefixString.toUpperCase(),
						testString=testPrefixString + "#testStartsWith()",
						lowcaseTestString=testString.toLowerCase();
		assertTrue("Strict case insensitive partial starts-with success", StringUtil.startsWith(testString, upcaseTestPrefixString, true, false));
		assertFalse("Strict case sensitive partial starts-with failure", StringUtil.startsWith(testString, upcaseTestPrefixString, true, true));
		assertTrue("Strict case sensitive partial starts-with success", StringUtil.startsWith(testString, testPrefixString, true, true));
		assertFalse("Strict case sensitive full starts-with failure", StringUtil.startsWith(testString, testString, true, true));

		assertFalse("Lenient case sensitive full starts-with failure", StringUtil.startsWith(testString, lowcaseTestString, false, true));
		assertTrue("Lenient case insensitive full starts-with success", StringUtil.startsWith(testString, lowcaseTestString, false, false));
		assertTrue("Lenient case sensitive full starts-with success", StringUtil.startsWith(testString, testString, false, true));
	}

	@Test
	public void testEndsWith ()
	{
		final String	testSuffixString="testStartsWith()",
						upcaseTestSuffixString=testSuffixString.toUpperCase(),
						testString=getClass().getName() + "#" + testSuffixString,
						lowcaseTestString=testString.toLowerCase();
		assertTrue("Strict case insensitive partial ends-with success", StringUtil.endsWith(testString, upcaseTestSuffixString, true, false));
		assertFalse("Strict case sensitive partial ends-with failure", StringUtil.endsWith(testString, upcaseTestSuffixString, true, true));
		assertTrue("Strict case sensitive partial ends-with success", StringUtil.endsWith(testString, testSuffixString, true, true));
		assertFalse("Strict case sensitive full ends-with failure", StringUtil.endsWith(testString, testString, true, true));

		assertFalse("Lenient case sensitive full ends-with failure", StringUtil.endsWith(testString, lowcaseTestString, false, true));
		assertTrue("Lenient case insensitive full ends-with success", StringUtil.endsWith(testString, lowcaseTestString, false, false));
		assertTrue("Lenient case sensitive full ends-with success", StringUtil.endsWith(testString, testString, false, true));
	}

	@Test
    public void testIsEmptyOnNonEmpty() {
        String origStr = "foo bar baz bong";        
        assertFalse("Unexpected empty String", StringUtil.isEmpty(origStr));

        StringBuilder   sb=new StringBuilder(origStr);
        assertFalse("Unexpected empty StringBuilder", StringUtil.isEmpty(sb));
    }
	
	@Test
    public void testIsEmptyOnEmpty() {
        assertTrue("Null not empty", StringUtil.isEmpty(null));
        assertTrue("Empty string not empty", StringUtil.isEmpty(""));
        assertTrue("Empty string builder not empty", StringUtil.isEmpty(new StringBuilder()));
    }
    @Test
    public void testChopHeadAndEllipsify() {
        assertEquals("", StringUtil.chopHeadAndEllipsify("", 5));
        assertEquals("a", StringUtil.chopHeadAndEllipsify("a", 5));
        assertEquals("abcde", StringUtil.chopHeadAndEllipsify("abcde", 5));
        assertEquals(StringUtil.ELLIPSIS + "ef", StringUtil.chopHeadAndEllipsify("abcdef", 5));

        char[]  testChars=new char[StringUtil.ELLIPSIS.length()];
        Arrays.fill(testChars, 'x');
        final String    TEST_STRING=new String(testChars);
        for (int    index=1; index < StringUtil.ELLIPSIS.length(); index++) {
            String  expected=StringUtil.ELLIPSIS.substring(0, index),
                    actual=StringUtil.chopHeadAndEllipsify(TEST_STRING, index);
            assertEquals("Mismatched result for index=" + index, expected, actual);
        }
    }

    @Test
    public void testChopHead() {
        String str_10 = "I am short";
        for (int    index=1; index <= Byte.SIZE; index++) {
            int maxLen=str_10.length() + index;
            assertSame("Mismatched instance for len=" + maxLen, str_10, StringUtil.chopHead(str_10, maxLen));
        }
        assertEquals("short", StringUtil.chopHead(str_10, 5));
        assertEquals("", StringUtil.chopHead(str_10, 0));                        
    }

    @Test
    public void testChopTailAndEllipsify() {
        assertEquals("", StringUtil.chopTailAndEllipsify("", 5));
        assertEquals("a", StringUtil.chopTailAndEllipsify("a", 5));
        assertEquals("abcde", StringUtil.chopTailAndEllipsify("abcde", 5));
        assertEquals("ab" + StringUtil.ELLIPSIS, StringUtil.chopTailAndEllipsify("abcdef", 5));

        char[]  testChars=new char[StringUtil.ELLIPSIS.length()];
        Arrays.fill(testChars, 'x');
        final String    TEST_STRING=new String(testChars);
        for (int    index=1; index < StringUtil.ELLIPSIS.length(); index++) {
            String  expected=StringUtil.ELLIPSIS.substring(0, index),
                    actual=StringUtil.chopTailAndEllipsify(TEST_STRING, index);
            assertEquals("Mismatched result for index=" + index, expected, actual);
        }
    }
    
    @Test
    public void testChopTail() {
        String str_10 = "I am short";
        for (int    index=1; index <= Byte.SIZE; index++) {
            int maxLen=str_10.length() + index;
            assertSame("Mismatched instance for len=" + maxLen, str_10, StringUtil.chopTail(str_10, maxLen));
        }
        assertEquals("I am ", StringUtil.chopTail(str_10, 5));
        assertEquals("", StringUtil.chopTail(str_10, 0));                        
    }

    @Test
    public void testSafeToString () {
        final String    TEST_NAME="testSafeToString";
        assertNull("Non null result", StringUtil.safeToString(null));
        assertSame("Mismatched string result", TEST_NAME, StringUtil.safeToString(TEST_NAME));
        assertEquals("Mismatched builder result", TEST_NAME, StringUtil.safeToString(new StringBuilder(TEST_NAME)));
    }

    @Test
    public void testSmartQuoteObjectName () {
        for (String value : new String[] { "unqoted", "\"quoted\"" }) {
            String  result=StringUtil.smartQuoteObjectName(value);
            if (value.charAt(0) == '"')
                assertSame("Mismatched quoted value instance", value, result);
            else
                assertEquals("Mismatched quoted result", ObjectName.quote(value), result);
        }
    }

    @Test
    public void testSmartQuoteObjectNameOnEmptyStrings() {
        final String    QUOTED_EMPTY="\"\"";
        assertNull("Mismatched null string value", StringUtil.smartQuoteObjectName(null));
        assertEquals("Mismatched empty string value", QUOTED_EMPTY, StringUtil.smartQuoteObjectName(""));
        assertSame("Mismatched quoted empty string value", QUOTED_EMPTY, StringUtil.smartQuoteObjectName(QUOTED_EMPTY));
    }

    @Test
    public void testSmartQuoteObjectNameOnImbalancedQuotesOrNull() {
        for (String value : new String[] { "\"", "\"abcd", "abcd\"" }) {
            try {
                String  result=StringUtil.smartQuoteObjectName(value);
                fail("Unexpected result for " + value + ": " + result);
            } catch(IllegalArgumentException e) {
                // expected - ignored
            }
        }
    }

    @Test
    public void testSmartUnquoteObjectName() {
        for (String value : new String[] { "unqoted", "\"quoted\"" }) {
            String  result=StringUtil.smartUnquoteObjectName(value);
            if (value.charAt(0) == '"')
                assertEquals("Mismatched unquoted result", ObjectName.unquote(value), result);
            else
                assertSame("Mismatched unquoted value instance", value, result);
        }
    }

    @Test
    public void testSmartUnquoteObjectNameOnEmptyStrings() {
        final String    EMPTY_STRING="";
        assertNull("Mismatched null string value", StringUtil.smartUnquoteObjectName(null));
        assertSame("Mismatched empty string value", EMPTY_STRING, StringUtil.smartUnquoteObjectName(EMPTY_STRING));
        assertEquals("Mismatched quoted empty string value", EMPTY_STRING, StringUtil.smartUnquoteObjectName("\"\""));
    }

    @Test
    public void testSmartUnquoteObjectNameOnImbalancedQuotesOrNull() {
        for (String value : new String[] { "\"", "\"abcd", "abcd\"" }) {
            try {
                String  result=StringUtil.smartUnquoteObjectName(value);
                fail("Unexpected result for " + value + ": " + result);
            } catch(IllegalArgumentException e) {
                // expected - ignored
            }
        }
    }
}
