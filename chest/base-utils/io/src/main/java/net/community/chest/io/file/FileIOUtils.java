/*
 *
 */
package net.community.chest.io.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StreamCorruptedException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import net.community.chest.Triplet;
import net.community.chest.io.FileUtil;
import net.community.chest.io.IOCopier;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 26, 2009 3:56:00 PM
 */
public final class FileIOUtils {
    private FileIOUtils ()
    {
        // no instance
    }

    public static final byte[]    EMPTY_BYTES=new byte[0];
    public static final Triplet<Long,Byte,Byte> findDifference (
            final long    readOffset,
            final byte[] srcBuf, final int srcOffset, final int    srcRead,
            final byte[] dstBuf, final int dstOffset, final int    dstRead)
    {
        if (srcRead <= 0)
        {
            if (dstRead <= 0)    // both ended at the same time
                return null;

            return new Triplet<Long,Byte,Byte>(Long.valueOf(readOffset), null, Byte.valueOf(dstBuf[0]));
        }
        else if (dstRead <= 0)
            return new Triplet<Long,Byte,Byte>(Long.valueOf(readOffset), Byte.valueOf(srcBuf[0]), null);

        final int    cmpLen=Math.min(srcRead, dstRead);
        for (int    cIndex=0, sIndex=srcOffset, dIndex=dstOffset; cIndex < cmpLen; cIndex++, sIndex++, dIndex++)
        {
            if (srcBuf[sIndex] != dstBuf[dIndex])
                return new Triplet<Long,Byte,Byte>(Long.valueOf(readOffset + cIndex), Byte.valueOf(srcBuf[sIndex]), Byte.valueOf(dstBuf[dIndex]));
        }

        if (cmpLen < srcRead)
            return new Triplet<Long,Byte,Byte>(Long.valueOf(readOffset + cmpLen), null, Byte.valueOf(dstBuf[cmpLen]));
        else if (cmpLen < dstRead)
            return new Triplet<Long,Byte,Byte>(Long.valueOf(readOffset + cmpLen), Byte.valueOf(srcBuf[cmpLen]), null);

        return null;
    }

    public static final Triplet<Long,Byte,Byte> findDifference (
            final long    readOffset,
            final byte[] srcBuf, final int    srcRead,
            final byte[] dstBuf, final int    dstRead)
    {
        return findDifference(readOffset, srcBuf, 0, srcRead, dstBuf, 0, dstRead);
    }
    /**
     * Compares the contents of the {@link InputStream}-s
     * @param srcFile First stream
     * @param dstFile Second stream
     * @param maxRead Max. number of bytes to compare - if negative then
     * <U>all</U> bytes are compared
     * @param readSize work buffer size to be used to read data from the files
     * @return A {@link Triplet} containing the difference offset and the
     * different {@link Byte} value(s) - <code>null</code> if no difference
     * @throws IOException If failed to access the file(s)
     */
    public static final Triplet<Long,Byte,Byte> findDifference (
            final InputStream srcFile, final InputStream dstFile, final long maxRead, final int readSize)
        throws IOException
    {
        if ((null == srcFile) || (null == dstFile) || (readSize < Byte.MAX_VALUE))
            throw new IOException("findDifference(" + InputStream.class.getSimpleName() + ")[" + readSize + "/" + maxRead + "] bad arguments");

        long    readOffset=0L;
        for (final byte[]    srcBuf=new byte[readSize], dstBuf=new byte[readSize]; ; )
        {
            final int    remLen;
            if (maxRead >= 0L)
            {
                final long    remRead=maxRead - readOffset;
                if (remRead < readSize)
                    remLen = (int) remRead;
                else
                    remLen = readSize;
            }
            else
                remLen = readSize;

            if (remLen <= 0)
                break;

            final int                        srcRead=srcFile.read(srcBuf, 0, remLen),
                                            dstRead=dstFile.read(dstBuf, 0, remLen);
            final Triplet<Long,Byte,Byte>    cmpRes=findDifference(readOffset, srcBuf, srcRead, dstBuf, dstRead);
            if (cmpRes != null)
                return cmpRes;

            if ((srcRead < 0) || (dstRead < 0))
                break;

            readOffset += remLen;
        }

        return null;
    }
    /**
     * Compares the contents of the {@link InputStream}-s
     * @param srcFile First stream
     * @param dstFile Second stream
     * @param maxRead Max. number of bytes to compare - if negative then
     * <U>all</U> bytes are compared
     * @return A {@link Triplet} containing the difference offset and the
     * different {@link Byte} value(s) - <code>null</code> if no difference
     * @throws IOException If failed to access the file(s)
     */
    public static final Triplet<Long,Byte,Byte> findDifference (
            final InputStream srcFile, final InputStream dstFile, final long maxRead)
        throws IOException
    {
        return findDifference(srcFile, dstFile, maxRead, IOCopier.DEFAULT_COPY_SIZE);
    }
    /**
     * Compares the contents of the {@link InputStream}-s
     * @param srcFile First stream
     * @param dstFile Second stream
     * @return A {@link Triplet} containing the difference offset and the
     * different {@link Byte} value(s) - <code>null</code> if no difference
     * @throws IOException If failed to access the file(s)
     */
    public static final Triplet<Long,Byte,Byte> findDifference (
            final InputStream srcFile, final InputStream dstFile)
        throws IOException
    {
        return findDifference(srcFile, dstFile, (-1L));
    }
    /**
     * Compares the contents of the {@link File}-s
     * @param srcFile First file
     * @param dstFile Second file
     * @param maxRead Max. number of bytes to compare - if negative then
     * <U>all</U> bytes are compared
     * @param readSize work buffer size to be used to read data from the files
     * @return A {@link Triplet} containing the difference offset and the
     * different {@link Byte} value(s) - <code>null</code> if no difference
     * @throws IOException If failed to access the file(s)
     */
    public static final Triplet<Long,Byte,Byte> findDifference (
            final File srcFile, final File dstFile, final long maxRead, final int readSize)
        throws IOException
    {
        if ((null == srcFile) || (null == dstFile) || (readSize < Byte.MAX_VALUE))
            throw new IOException("findDifference(" + srcFile + "/" + dstFile + "){" + readSize + "/" + maxRead + "} bad arguments");

        InputStream    src=null, dst=null;
        try
        {
            src = new FileInputStream(srcFile);
            dst = new FileInputStream(dstFile);

            return findDifference(src, dst, maxRead, readSize);
        }
        finally
        {
            FileUtil.closeAll(src, dst);
        }
    }
    /**
     * Compares the contents of the {@link File}-s
     * @param srcFile First file
     * @param dstFile Second file
     * @param maxRead Max. number of bytes to compare - if negative then
     * <U>all</U> bytes are compared
     * @return A {@link Triplet} containing the difference offset and the
     * different {@link Byte} value(s) - <code>null</code> if no difference
     * @throws IOException If failed to access the file(s)
     */
    public static final Triplet<Long,Byte,Byte> findDifference (
            final File srcFile, final File dstFile, final long maxRead)
        throws IOException
    {
        return findDifference(srcFile, dstFile, maxRead, IOCopier.DEFAULT_COPY_SIZE);
    }
    /**
     * Compares the contents of the {@link File}-s
     * @param srcFile Source file
     * @param dstFile Destination file
     * @return A {@link Triplet} containing the difference offset and the
     * different {@link Byte} value(s) - <code>null</code> if no difference
     * @throws IOException If failed to access the file(s)
     */
    public static final Triplet<Long,Byte,Byte> findDifference (
            final File srcFile, final File dstFile)
        throws IOException
    {
        return findDifference(srcFile, dstFile, (-1L), IOCopier.DEFAULT_COPY_SIZE);
    }
    /**
     * Compares 2 {@link File}-s
     * @param srcFile First file
     * @param dstFile Second file
     * @param checkData <code>true</code>=check also contents, <code>false</code>=check
     * only file size and (optionally) last-modified timestamp
     * @param timestampDiff Minimum difference (msec.) between the last-modified
     * timestamp(s) for the files to be considered different even if same size.
     * If non-positive then no such check is executed
     * @return {@link Boolean#TRUE} if files are the same, {@link Boolean#FALSE}
     * if not same, <code>null</code> if cannot determine
     * @throws IOException
     */
    public static final Boolean compareFileContents (
            final File srcFile, final File dstFile, final boolean checkData, final long timestampDiff)
        throws IOException
    {
        if ((null == srcFile) || (!srcFile.exists()) || (!srcFile.isFile())
         || (null == dstFile) || (!dstFile.exists()) || (!dstFile.isFile()))
            return null;

        final long    srcLen=srcFile.length(), dstLen=dstFile.length();
        if (srcLen != dstLen)
            return Boolean.FALSE;

        if (timestampDiff > 0L)
        {
            final long    srcMod=srcFile.lastModified(),
                        dstMod=dstFile.lastModified(),
                        modDiff=dstMod - srcMod;
            // if destination is less up-to-date by less than one minute then declare files the same
            if ((modDiff < 0L) && (Math.abs(modDiff) < timestampDiff))
                return Boolean.FALSE;
        }

        if (checkData)
        {
            final Triplet<Long,Byte,Byte>    cmpOffset=findDifference(srcFile, dstFile);
            if (cmpOffset != null)
                return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }
    /**
     * Makes sure the <U>exact</U> amount of data is skipped
     * @param in The {@link InputStream} to skip from
     * @param skipSize The <U>exact</U> number of bytes to skip - ignored if
     * not positive
     * @throws IOException If failed to skip the required number of bytes
     */
    public static final void skipFully (final InputStream in, final long skipSize)
        throws IOException
    {
        if (null == in)
            throw new IOException("skipFully(" + skipSize + ") no " + InputStream.class.getSimpleName() + " instance");

        for (long    remSkip=skipSize; remSkip > 0L; )
        {
            final long    accSkip=in.skip(remSkip);
            if (accSkip <= 0L)
                throw new StreamCorruptedException("skipFully(" + skipSize + ") expected=" + remSkip + "/got=" + accSkip);
            remSkip -= accSkip;
        }
    }
    /**
     * Reads the <U>exact</U> number of required bytes
     * @param in The {@link InputStream} to read from
     * @param buf Buffer to read data into
     * @param off Offset in buffer to place read data
     * @param len The <U>exact</U> number of bytes to read
     * @throws IOException If failed to read the required number of bytes
     */
    public static final void readFully (
            final InputStream in, final byte[] buf, final int off, final int len)
        throws IOException
    {
        if (null == in)
            throw new IOException("readFully(" + len + ") no " + InputStream.class.getSimpleName() + " instance");

        for (int    remLen=len, curPos=off; remLen > 0; )
        {
            final int    readLen=in.read(buf, curPos, remLen);
            if (readLen <= 0)
                throw new StreamCorruptedException("readFully(" + len + ") expected=" + remLen + "/got=" + readLen);
            curPos += readLen;
            remLen -= readLen;
        }
    }
    /**
     * Reads the <U>exact</U> number of bytes as can be fit in the buffer
     * @param in The {@link InputStream} to read from
     * @param buf Buffer to read data into
     * @throws IOException If failed to read the required number of bytes
     */
    public static final void readFully (final InputStream in, final byte[] buf)
        throws IOException
    {
        readFully(in, buf, 0, (null == buf) ? 0 : buf.length);
    }
    /**
     * Reads an entire file contents as a {@link String}
     * @param f The {@link File} to read from
     * @return File contents as a {@link String}
     * @throws IOException If failed to read or file too big
     */
    public static final String readFileAsString (final File f) throws IOException
    {
        final long    fSize=(null == f) ? 0L : f.length();
        if (fSize <= 0L)
            return "";
        if (fSize >= Integer.MAX_VALUE)
            throw new StreamCorruptedException("readFileAsString(" + f + ") reported file size (" + fSize + ") beyond integer limits");

        final StringWriter    w=new StringWriter((int) fSize);
        final Reader        r=new FileReader(f);
        try
        {
            final long    cpyLen=IOCopier.copyReaderToWriter(r, w);
            if (cpyLen < 0L)
                throw new StreamCorruptedException("readFileAsString(" + f + ") error (" + cpyLen + ") while reading contents");
        }
        finally
        {
            FileUtil.closeAll(r);
        }

        return w.toString();
    }
    /**
     * @param components The file path components - ignored if <code>null</code>/empty
     * @return A {@link File} representing the path composed of all the components - may
     * be <code>null</code> if no components provided
     * @see #buildFilePath(List)
     */
    public static final File buildFilePath (String ... components)
    {
        return ((components == null) || (components.length <= 0)) ? null : buildFilePath(Arrays.asList(components));
    }
    /**
     * @param components The file path components - ignored if <code>null</code>/empty
     * @return A {@link File} representing the path composed of all the components - may
     * be <code>null</code> if no components provided
     * @see #buildPath(char, List)
     */
    public static final File buildFilePath (List<String> components)
    {
        final String    path=buildPath(File.separatorChar, components);
        if ((path == null) || (path.length() <= 0))
            return null;

        return new File(path);
    }
    /**
     * @param components The file path components - ignored if <code>null</code>/empty
     * @return A {@link String} representing the path composed of all the components separated
     * by the {@link File#separatorChar} - may be <code>null</code> if no components provided
     * @see #buildPath(char, String...)
     */
    public static final String buildPath (String ... components)
    {
        return buildPath(File.separatorChar, components);
    }
    /**
     * @param sepChar Separator to be used for separating the components
     * @param components The file path components - ignored if <code>null</code>/empty
     * @return A {@link String} representing the path composed of all the components separated
     * by the specified separator - may be <code>null</code> if no components provided
     * @see #buildPath(char, List)
     */
    public static final String buildPath (char sepChar, String ... components)
    {
        return ((components == null) || (components.length <= 0)) ? null : buildPath(sepChar, Arrays.asList(components));
    }
    /**
     * @param components The file path components - ignored if <code>null</code>/empty
     * @return A {@link String} representing the path composed of all the components separated
     * by the {@link File#separatorChar} - may be <code>null</code> if no components provided
     * @see #buildPath(char, List)
     */
    public static final String buildPath (List<String> components)
    {
        return buildPath(File.separatorChar, components);
    }
    /**
     * @param sepChar Separator to be used for separating the components
     * @param components The file path components - ignored if <code>null</code>/empty
     * @return A {@link String} representing the path composed of all the components separated
     * by the specified separator - may be <code>null</code> if no components provided
     */
    public static final String buildPath (char sepChar, List<String> components)
    {
        final int    numComponents=(components == null) ? 0 : components.size();
        if (numComponents <= 0)
            return null;

        final String    baseComponent=components.get(0);
        if (numComponents == 1)
            return baseComponent;

        final StringBuilder    sb=new StringBuilder(baseComponent.length() + 16 * (numComponents - 1)).append(baseComponent);
        for (final String c : components)
        {
            if ((c == null) || (c.length() <= 0))
                continue;
            sb.append(sepChar).append(c);
        }

        return sb.toString();
    }
}
