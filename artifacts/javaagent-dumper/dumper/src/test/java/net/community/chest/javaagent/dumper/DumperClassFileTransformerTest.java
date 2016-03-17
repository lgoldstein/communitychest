/*
 *
 */
package net.community.chest.javaagent.dumper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.community.chest.io.FileUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jul 25, 2011 1:47:00 PM
 */
public class DumperClassFileTransformerTest extends Assert {
    private final Map<String,String>    optsMap=new HashMap<String,String>();
    public DumperClassFileTransformerTest ()
    {
        super();
    }

    @Before
    public void setUp ()
    {
        if (optsMap.size() > 0)
            optsMap.clear();
        System.clearProperty(DumperClassFileTransformer.OUTPUT_ROOTFOLDER_PROP);
    }

    @Test
    public void testResolveOutputFileValueFromOptionsMap ()
    {
        final String    TEST_NAME="testResolveOutputFileValueFromOptionsMap";
        optsMap.put(DumperClassFileTransformer.OUTPUT_ROOTFOLDER_PROP, TEST_NAME);
        assertEquals("Mismatched option value", TEST_NAME, DumperClassFileTransformer.resolveOutputFileValue(optsMap));
    }

    @Test
    public void testResolveOutputFileValueFromSysProp ()
    {
        final String    TEST_NAME="testResolveOutputFileValueFromSysProp";
        assertNull("Unexpected definition of property=" + DumperClassFileTransformer.OUTPUT_ROOTFOLDER_PROP,
                    System.getProperty(DumperClassFileTransformer.OUTPUT_ROOTFOLDER_PROP));
        System.setProperty(DumperClassFileTransformer.OUTPUT_ROOTFOLDER_PROP, TEST_NAME);

        assertEquals("Mismatched property value", TEST_NAME, DumperClassFileTransformer.resolveOutputFileValue(optsMap));
    }

    @Test
    public void testResolveDefaultOutputFileValue ()
    {
        assertEquals("Mismatched default value",
                     System.getProperty("user.dir") + File.separator + DumperClassFileTransformer.class.getSimpleName(),
                     DumperClassFileTransformer.resolveOutputFileValue(optsMap));
    }

    @Test(expected=IllegalStateException.class)
    public void testResolveExistingOutputFilePath () throws IOException
    {
        File    dummyFile=new File(System.getProperty("java.io.tmpdir"), "testResolveExistingOutputFilePath.txt");
        Writer    w=new FileWriter(dummyFile);
        try
        {
            w.write(DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis())));
        }
        finally
        {
            w.close();
        }

        File    result=DumperClassFileTransformer.resolveOutputRootFolder(dummyFile.getAbsolutePath());
        fail("Unexpected output file path resolution: " + result.getAbsolutePath());
    }

    @Test
    public void testResolveOutputFilePath ()
    {
        File    tmpDir=new File(System.getProperty("java.io.tmpdir")),
                tstDir=new File(tmpDir, "testResolveOutputFilePath");
        if (tstDir.exists() && (!tstDir.delete()))
            fail("Failed to clear test folder: " + tstDir.getAbsolutePath());

        File    rootFolder=DumperClassFileTransformer.resolveOutputRootFolder(tstDir.getAbsolutePath());
        assertEquals("Resolved folder does not match original", tstDir.getAbsolutePath(), rootFolder.getAbsolutePath());
        assertTrue("Test folder not re-created: " + tstDir.getAbsolutePath(), tstDir.exists() && tstDir.canRead() && tstDir.canWrite());
    }

    @Test
    public void testResolveConfigurationLocation () throws MalformedURLException
    {
        final Class<?>    anchor=getClass();
        {
            final URL        defaultURL=anchor.getResource(DumperClassFileTransformer.DEFAULT_CONFIG_RESOURCE);
            assertNotNull("Not found default configuration resource", defaultURL);

            final URL    resURL=DumperClassFileTransformer.resolveConfigurationLocation(anchor, optsMap);
            assertNotNull("Not resolved default configuration resource", resURL);
            assertEquals("Mismatched default resource location", defaultURL.toExternalForm(), resURL.toExternalForm());
        }

        final File    tmpDir=new File(System.getProperty("java.io.tmpdir"));
        final URL    tmpURL=FileUtil.toURL(tmpDir);
        {
            optsMap.put(DumperClassFileTransformer.CONFIG_URL_PROP, tmpDir.getAbsolutePath());

            final URL    resURL=DumperClassFileTransformer.resolveConfigurationLocation(anchor, optsMap);
            assertNotNull("Not resolved file configuration resource", resURL);
            assertEquals("Mismatched file resource location", tmpURL.toExternalForm(), resURL.toExternalForm());
        }

        {
            optsMap.put(DumperClassFileTransformer.CONFIG_URL_PROP, tmpURL.toExternalForm());
            final URL    resURL=DumperClassFileTransformer.resolveConfigurationLocation(anchor, optsMap);
            assertNotNull("Not resolved URL configuration resource", resURL);
            assertEquals("Mismatched URL resource location", tmpURL.toExternalForm(), resURL.toExternalForm());
        }
    }
}
