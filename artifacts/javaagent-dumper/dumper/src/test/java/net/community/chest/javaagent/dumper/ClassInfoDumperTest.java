/*
 *
 */
package net.community.chest.javaagent.dumper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.dom.DOMUtils;
import net.community.chest.io.file.FileIOUtils;
import net.community.chest.javaagent.dumper.data.ClassInfo;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 3, 2011 8:23:47 AM
 */
public class ClassInfoDumperTest extends Assert {
    protected static final String    LINE_SEP=System.getProperty("line.separator");
    private final Class<?>    TEST_CLASS;
    private final DumperClassFileTransformer    XFORMER;

    public ClassInfoDumperTest ()
    {
        final Map<String,String>    optsMap=new TreeMap<String,String>();
        optsMap.put(DumperClassFileTransformer.OUTPUT_ROOTFOLDER_PROP, System.getProperty("java.io.tmpdir"));
        XFORMER = new DumperClassFileTransformer(optsMap);
        TEST_CLASS = getClass();
    }

    @Test
    public void testBcelClassInfoDumper () throws IOException
    {
        final Appendable    TEST_OUT=new StringBuilder(1024);
        final File            result=runClassBytesTransformer(TEST_CLASS, TEST_OUT);
        assertDumpedContentsEquals("testBcelClassInfoDumper", TEST_OUT.toString(), result);
    }

    @Test
    public void testReflectiveClassInfoDumper () throws IOException
    {
        final Appendable    TEST_OUT=new StringBuilder(1024);
        final File            result=runReflectiveTransformer(TEST_CLASS, TEST_OUT);
        assertDumpedContentsEquals("testReflectiveClassInfoDumper", TEST_OUT.toString(), result);
    }

    @Test
    @Ignore
    public void testInfoDumpersResults () throws Exception
    {
        final StringBuilder    DUMMY_BUFFER=new StringBuilder(1024);
        final String        bcelData=FileIOUtils.readFileAsString(runClassBytesTransformer(TEST_CLASS, DUMMY_BUFFER));
//        System.out.append("BCEL dump contents:").append(LINE_SEP)
//                    .append("--------------------------").append(LINE_SEP)
//                    .append(bcelData)
//              .println();
        DUMMY_BUFFER.setLength(0);

        final String    reflectData=FileIOUtils.readFileAsString(runReflectiveTransformer(TEST_CLASS, DUMMY_BUFFER));
//        System.out.append("Reflective dump contents:").append(LINE_SEP)
//                    .append("--------------------------").append(LINE_SEP)
//                    .append(reflectData)
//              .println();

        final Document    docBcel=DOMUtils.loadDocumentFromString(bcelData),
                        docRflct=DOMUtils.loadDocumentFromString(reflectData);
        final ClassInfo    bcelClass=new ClassInfo(docBcel), rflctClass=new ClassInfo(docRflct);
        assertEquals(bcelClass, rflctClass);
    }

    private File runClassBytesTransformer (final Class<?> clazz, final Appendable out) throws IOException
    {
        return XFORMER.transform(clazz.getName(), clazz.getProtectionDomain(), getClassBytes(clazz), out);
    }

    private File runReflectiveTransformer (final Class<?> clazz, final Appendable out) throws IOException
    {
        return XFORMER.transform(clazz, out);
    }

    private void assertDumpedContentsEquals (final String testName, final String internalData, final File result) throws IOException
    {
        final String    outData=FileIOUtils.readFileAsString(result);
//        System.out.append("External dump contents: ").append(result.getAbsolutePath()).append(LINE_SEP)
//                  .append("----------------------").append(LINE_SEP)
//                  .append(outData)
//            .println();
        assertEquals(testName + "[XML contents]", internalData, outData);
    }

    private static byte[] getClassBytes (final Class<?> clazz) throws IOException
    {
        final URL            loc=resolveClassBytesLocation(clazz);
        final InputStream    in=(loc == null) ? null : loc.openStream();
        if (in == null)
            throw new FileNotFoundException("Cannot resolve location of " + ((clazz == null) ? null : clazz.getName()));

        final int                    COPY_SIZE=4096;
        final ByteArrayOutputStream    out=new ByteArrayOutputStream(COPY_SIZE);
        final byte[]                buf=new byte[COPY_SIZE];
        try
        {
            for (int    readLen=in.read(buf); readLen >= 0; readLen=in.read(buf))
                out.write(buf, 0, readLen);
        }
        finally
        {
            in.close();
        }

        return out.toByteArray();
    }

    private static URL resolveClassBytesLocation (final Class<?> clazz) throws IOException
    {
        final ProtectionDomain    pd=(clazz == null) ? null : clazz.getProtectionDomain();
        final CodeSource        cs=(pd == null) ? null : pd.getCodeSource();
        URL                        loc=(cs == null) ? null : cs.getLocation();
        if (loc == null)
            return loc;

        final String    path=loc.toExternalForm();
        if (path.startsWith("file:"))
        {
            final String    className=clazz.getName().replace('.', '/') + ".class";
            if (!path.endsWith(className))
            {
                final String    effPath=path + ((path.charAt(path.length() - 1) == '/') ? "" : "/") + className;
                try
                {
                    return new URL(effPath);
                }
                catch(MalformedURLException e)
                {
                    throw new IOException("Failed to build effective path=" + effPath + ": " + e.getMessage(), e);
                }
            }
        }

        return loc;
    }
}
