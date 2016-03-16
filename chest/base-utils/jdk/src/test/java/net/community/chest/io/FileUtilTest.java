/*
 *
 */
package net.community.chest.io;

import java.util.Arrays;
import java.util.Collection;

import net.community.chest.AbstractTestSupport;

import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since May 19, 2011 3:25:51 PM
 */
public class FileUtilTest extends AbstractTestSupport {
    public FileUtilTest ()
    {
        super();
    }

    @Test
    public void testAdjustExtension ()
    {
        /*
         * Triplets - X=initial value, X+1=addDot value, X+2 expected result
         */
        final Object[]    CONFIGS={
                null, Boolean.TRUE, null,
                "", Boolean.FALSE, "",
                null, Boolean.TRUE, null,
                "", Boolean.FALSE, "",
                ".a", Boolean.TRUE, ".a",
                "a", Boolean.TRUE, ".a",
                ".a", Boolean.FALSE, "a",
                "a", Boolean.FALSE, "a"
            };
        for (int    cIndex=0; cIndex < CONFIGS.length; cIndex += 3)
        {
            final String    orgValue=(String) CONFIGS[cIndex];
            final Boolean    dotValue=(Boolean)  CONFIGS[cIndex + 1];
            final String    expValue=(String) CONFIGS[cIndex + 2];
            assertEquals("Mismatched result for '" + orgValue + "'[addDot=" + dotValue + "]", expValue, FileUtil.adjustExtension(orgValue, dotValue.booleanValue()));
        }
    }

    @Test
    public void testGetExtension ()
    {
        /*
         * Triplets - X=initial value, X+1=addDot value, X+2 expected result
         */
        final Object[]    CONFIGS={
                null, Boolean.TRUE, null,
                "", Boolean.TRUE, null,
                null, Boolean.FALSE, null,
                "", Boolean.FALSE, null,
                ".a", Boolean.TRUE, ".a",
                "a", Boolean.TRUE, null,
                ".a", Boolean.FALSE, "a",
                "a", Boolean.FALSE, null,
                "a.b", Boolean.TRUE, ".b",
                "a.b", Boolean.FALSE, "b"
            };
        for (int    cIndex=0; cIndex < CONFIGS.length; cIndex += 3)
        {
            final String    orgValue=(String) CONFIGS[cIndex];
            final Boolean    dotValue=(Boolean)  CONFIGS[cIndex + 1];
            final String    expValue=(String) CONFIGS[cIndex + 2];
            assertEquals("Mismatched result for '" + orgValue + "'[withDot=" + dotValue + "]", expValue, FileUtil.getExtension(orgValue, dotValue.booleanValue()));
        }
    }

    @Test
    public void testAdjustFilename ()
    {
        /*
         * Triplets - X=initial value, X+1=extension value, X+2 expected result
         */
        final String[]    CONFIGS={
                null, null, null,
                "", null, "",
                null, "a", null,
                "a", null, "a",
                "a", "b", "a.b",
                "a.b", "b", "a.b"
            };
        for (int    cIndex=0; cIndex < CONFIGS.length; cIndex += 3)
        {
            final String    orgValue=CONFIGS[cIndex], extValue=CONFIGS[cIndex + 1], expValue=CONFIGS[cIndex + 2];
            assertEquals("Mismatched result for '" + orgValue + "'[extension=" + extValue + "]", expValue, FileUtil.adjustFileName(orgValue, extValue));
        }
    }

    @Test
    public void testStripExtension ()
    {
        /*
         * Triplets - X=initial value, X+1=extension value, X+2 expected result
         */
        final String[]    CONFIGS={
                null, null, null,
                "", null, "",
                null, "a", null,
                "", "a", "",
                "a", null, "a",
                "a", "b", "a",
                "a.b", "b", "a",
                "a.c", "b", "a.c"
            };
        for (int    cIndex=0; cIndex < CONFIGS.length; cIndex += 3)
        {
            final String    orgValue=CONFIGS[cIndex], extValue=CONFIGS[cIndex + 1], expValue=CONFIGS[cIndex + 2];
            assertEquals("Mismatched result for '" + orgValue + "'[extension=" + extValue + "]", expValue, FileUtil.stripExtension(orgValue, extValue));
        }
    }

    @Test
    public void testIsMatchingFileSuffix ()
    {
        final Collection<String>    EXTS=Arrays.asList("a", "b", "c", "d");
        assertTrue(FileUtil.isMatchingFileSuffix("x.d", EXTS));
        assertTrue(FileUtil.isMatchingFileSuffix("a.c", EXTS));
        assertFalse(FileUtil.isMatchingFileSuffix("d.x", EXTS));
        assertFalse(FileUtil.isMatchingFileSuffix(null, EXTS));
        assertFalse(FileUtil.isMatchingFileSuffix("", EXTS));
        assertFalse(FileUtil.isMatchingFileSuffix("x.d", (Collection<String>) null));
    }
}
