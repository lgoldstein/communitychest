/*
 *
 */
package net.community.chest.win32.core.format.pe;

import java.io.File;
import java.net.URL;

import net.community.chest.io.FileUtil;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 15, 2009 2:50:54 PM
 */
public final class PEFormatDetails {
    private PEFormatDetails ()
    {
        // no instance
    }

    /**
     * Magic number(s) used to indicate type of PE format
     */
    public static final short    PE32_MAGIC_NUMBER=(short) 0x010b,
                                PE32PLUS_MAGIC_NUMBER=(short) 0x020b;

    public static final String    EXE_SUFFIX="exe", DLL_SUFFIX="dll";
    public static final boolean isExeFile (final String filePath)
    {
        return FileUtil.isMatchingFileSuffix(filePath, EXE_SUFFIX);
    }

    public static final boolean isExeFile (final URL fileURL)
    {
        return (null == fileURL) ? false : isExeFile(fileURL.getPath());
    }

    public static final boolean isExeFile (final File f)
    {
        return ((null == f) || (!f.isFile())) ? false : isExeFile(f.getName());
    }

    public static final boolean isDllFile (final String filePath)
    {
        return FileUtil.isMatchingFileSuffix(filePath, DLL_SUFFIX);
    }

    public static final boolean isDllFile (final URL fileURL)
    {
        return (null == fileURL) ? false : isDllFile(fileURL.getPath());
    }

    public static final boolean isDllFile (final File f)
    {
        return ((null == f) || (!f.isFile())) ? false : isDllFile(f.getName());
    }
}
