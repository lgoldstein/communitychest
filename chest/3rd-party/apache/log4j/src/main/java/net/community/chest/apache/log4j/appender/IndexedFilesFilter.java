/*
 *
 */
package net.community.chest.apache.log4j.appender;

import java.io.File;
import java.io.FileFilter;

public class IndexedFilesFilter implements FileFilter {
    public IndexedFilesFilter ()
    {
        super();
    }

    private String    _baseFileName    /* =null */;
    public String getBaseFileName ()
    {
        return _baseFileName;
    }

    public void setBaseFileName (String baseFileName)
    {
        _baseFileName = baseFileName;
    }

    private String    _extension    /* =null */;
    public String getExtension ()
    {
        return _extension;
    }

    public void setExtension (String extension)
    {
        _extension = extension;
    }

    public IndexedFilesFilter (String baseFileName, String extension)
    {
        _baseFileName = baseFileName;
        _extension = extension;
    }
    /*
     * @see java.io.FileFilter#accept(java.io.File)
     */
    @Override
    public boolean accept (File pathname)
    {
        if ((null == pathname) || pathname.isDirectory())
            return false;    // ignore directories

        final String    fileName=pathname.getName(),
                        baseName=getBaseFileName();
        final int        fnLen=(null == fileName) ? 0 : fileName.length(),
                        bnLen=(null == baseName) ? 0 : baseName.length();
        if ((fnLen<= 0)
         || (bnLen <= 0)
         || (fnLen <= bnLen)
         || (!fileName.startsWith(baseName)))
            return false;    // check the base name

        final String    fileExt=getExtension();
        final int        extLen=(null == fileExt) ? 0 : fileExt.length();
        if (extLen > 1)
        {
            final int    fnSPos=fileName.lastIndexOf('.');
            if ((fnSPos <= 0) || (fnSPos >= (fnLen-1)))
                return false;

            final String    fnExt=fileName.substring(fnSPos);
            if (!fileExt.equalsIgnoreCase(fnExt))
                return false;
        }

        return true;
    }
}
