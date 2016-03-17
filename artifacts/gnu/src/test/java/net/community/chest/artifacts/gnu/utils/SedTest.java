/*
 *
 */
package net.community.chest.artifacts.gnu.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;

import net.community.chest.artifacts.gnu.options.Option;
import net.community.chest.io.EOLStyle;
import net.community.chest.io.file.FileIOUtils;
import net.community.chest.lang.SysPropsEnum;
import net.community.chest.util.BooleanIterator;

import org.junit.Assert;
import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 9, 2011 12:33:51 PM
 */
public class SedTest extends Assert {
    public SedTest ()
    {
        super();
    }

    @Test
    public void testBackupSuffixResolution ()
    {
        final String[]    ARGVALS={
                "-i", null,
                "-ibak", "bak",
                "--in-place", null,
                "--in-place=bak", "bak"
            };
        for (int    aIndex=0; aIndex < ARGVALS.length; aIndex += 2)
            assertEquals("Mismatched resolved suffix", ARGVALS[aIndex + 1], Sed.resolveBackupSuffix(Option.toMap(Option.parseArguments(ARGVALS[aIndex]))));
        assertNull("Unexpected suffix resolved for missing option", Sed.resolveBackupSuffix(null));
    }

    @Test
    public void testOutputModeResolution ()
    {
        for (final String key : new String[] { "i", "in-place" })
            assertTrue("Mismatched output mode for option=" + key,  Sed.resolveOutputMode(Option.toMap(new Option(key))));
        assertFalse("Unexpected enabled output mode for missing option", Sed.resolveOutputMode(null));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testResolveEOLStyle ()
    {
        for (final EOLStyle style : EOLStyle.VALUES)
            assertSame("Mismatched resolved style", style, Sed.resolveEOLStyle(Option.toMap(new Option("eol", style.name()))));
        assertSame("Unexpected EOL style for missing option", EOLStyle.LOCAL,  Sed.resolveEOLStyle(null));

        final EOLStyle    style=Sed.resolveEOLStyle(Option.toMap(new Option("eol", String.valueOf(System.nanoTime()))));
        fail("Unexpected resolution of bad style: " + style);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testExtractArgument ()
    {
        final String[]    ARGS={ "Hello", "World" };
        for (int    aIndex=0; aIndex < ARGS.length; aIndex++)
            assertSame("Mismatched argument at index=" + aIndex, ARGS[aIndex], Sed.extractArgument(String.valueOf(aIndex), aIndex, ARGS));

        final String    argVal=Sed.extractArgument(String.valueOf(Byte.MAX_VALUE), Byte.MAX_VALUE, ARGS);
        fail("Unexpected argument value: " + argVal);
    }

    @Test
    public void testResolveOutputFile ()
    {
        final File        TEST_FILE=new File(SysPropsEnum.JAVAIOTMPDIR.getPropertyValue(), "testResolveOutputFile.txt");
        final String    TEST_PATH=TEST_FILE.getAbsolutePath();
        final String[]    CONFIGS={
                "-i", TEST_PATH,
                "-ibak", TEST_PATH + ".bak",
                "--in-place", TEST_PATH,
                "--in-place=bak", TEST_PATH + ".bak",
                "-none", null
            };
        for (int    cIndex=0; cIndex < CONFIGS.length; cIndex += 2)
        {
            final String    argVal=CONFIGS[cIndex];
            final Sed        sed=new Sed(Option.parseArguments(argVal, "blah", "blah"));
            final File        outFile=sed.resolveOutputFile(TEST_FILE);
            final String    outPath=(outFile == null) ? null : outFile.getAbsolutePath();
            assertEquals("Mismatched output path for option=" + argVal, CONFIGS[cIndex + 1], outPath);
        }
    }

    @Test
    public void testProcessStreams () throws IOException
    {
        /*
         * Order is: original text, replacement pattern, replacement value, expected result
         */
        final String[]    CONFIGS={
                "dashboard.connect.uri: ${dashboard.connect.uri:nio://127.0.0.1:21234}",
                    "\\$\\{dashboard\\.connect\\.uri\\:nio\\://127\\.0\\.0\\.1\\:21234\\}",
                    "nio://7.3.19.65:21234",
                    "dashboard.connect.uri: nio://7.3.19.65:21234",
                "agent.auth: ${agent.auth:agent}",
                    "\\$\\{agent\\.auth\\:agent\\}",
                    "agent",
                    "agent.auth: agent",
                "agent.auth.password: ${agent.auth.password:insight}",
                    "\\$\\{agent\\.auth\\.password\\:insight\\}",
                    "insight",
                    "agent.auth.password: insight",
                "nothing to replace",
                    "something",
                    "everything",
                    "nothing to replace"

            };
        for (int    cIndex=0; cIndex < CONFIGS.length; cIndex += 4)
        {
            final String    inpValue=CONFIGS[cIndex],
                            repPattern=CONFIGS[cIndex + 1],
                            repValue=CONFIGS[cIndex + 2],
                            expValue=CONFIGS[cIndex + 3],
                            rgxValue=inpValue.replaceAll(repPattern, repValue);
            // make sure we did not mis-configure the expected result
            assertEquals("Mismatched expected config. value for input=" + inpValue, expValue, rgxValue);

            final Sed                sed=new Sed(Option.parseArguments(repPattern, repValue));
            final BufferedReader    rdr=new BufferedReader(new StringReader(inpValue));
            final StringBuilder        out=new StringBuilder(inpValue.length());
            assertEquals("Unexpected number of lines processed for input=" + inpValue, 1, sed.processStreams(rdr, out));
            assertEquals("Mismatched output for input=" + inpValue, expValue, out.toString());
        }
    }

    @Test
    public void testResolveOutputStream () throws IOException
    {
        final File        TEST_FILE=new File(SysPropsEnum.JAVAIOTMPDIR.getPropertyValue(), "testResolveOutputStream.txt");
        final Sed        sed=new Sed(Option.parseArguments("blah", "blah"));
        for (final Iterator<Boolean> bools=new BooleanIterator(); bools.hasNext(); )
        {
            final Boolean    toStdout=bools.next();
            assertTrue("Not a StringBuilder when toStdout=" + toStdout,
                        sed.resolveOutputStream(TEST_FILE, true, toStdout.booleanValue()) instanceof StringBuilder);
        }

        assertSame("Not STDOUT", System.out, sed.resolveOutputStream(TEST_FILE, false, true));

        final Appendable    out=sed.resolveOutputStream(TEST_FILE, false, false);
        try
        {
            assertTrue("Not a BufferedWriter", out instanceof BufferedWriter);
        }
        finally
        {
            if (out instanceof Closeable)
                ((Closeable) out).close();
        }
    }

    @Test
    public void testReplaceOriginalFile () throws IOException
    {
        final File        TEST_FILE=new File(SysPropsEnum.JAVAIOTMPDIR.getPropertyValue(), "testReplaceOriginalFile.txt");
        final String    TEST_DATA="This is test done at "
                            + DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis())) + EOLStyle.LOCAL.getStyleString()
                            + "Tested by: " + SysPropsEnum.USERNAME
                            ;
        final Sed        sed=new Sed(Option.parseArguments("blah", "blah"));
        assertEquals("Mismatched number of characters", TEST_DATA.length(), sed.replaceOriginalFile(TEST_FILE, new StringBuilder(TEST_DATA)));

        final String    readData=FileIOUtils.readFileAsString(TEST_FILE);
        assertEquals("Mismatched file data", TEST_DATA, readData);
    }
}
