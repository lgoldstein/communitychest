/*
 * @(#)JarDiffHandler.java    1.9 05/11/30
 *
 * Copyright (c) 2006 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

package jnlp.sample.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import jnlp.sample.jardiff.JarDiff;
import jnlp.sample.servlet.download.DownloadRequest;
import jnlp.sample.servlet.download.DownloadResponse;
import jnlp.sample.util.ObjectUtil;
import jnlp.sample.util.VersionString;
import jnlp.sample.util.log.Logger;
import jnlp.sample.util.log.LoggerFactory;
/*
 * A class that generates and caches information about JarDiff files
 *
 */
public class JarDiffHandler {
    // Default size of download buffer
    public static final int BUF_SIZE = 4 * 1024;

    // Default JARDiff MIME type
    public static final String JARDIFF_MIMETYPE="application/x-java-archive-diff";

    /** Reference to ServletContext and logger object */
    private final transient Logger _log;
    private final ServletContext _servletContext;
    private String _jarDiffMimeType;

    /* Contains information about a particular JARDiff entry */
    public static class JarDiffKey implements Comparable<JarDiffKey>, Serializable, Cloneable {
        /**
         *
         */
        private static final long serialVersionUID = -5412560259047926467L;
        private String  _name;        // Name of file
        private String  _fromVersionId;    // From version
        private String  _toVersionId;    // To version
        private boolean _minimal;       // True if this is a minimal jardiff

        /* Constructor used to generate a query object */
        public JarDiffKey (String name, String fromVersionId, String toVersionId, boolean minimal)
        {
            _name = name;
            _fromVersionId = fromVersionId;
            _toVersionId = toVersionId;
            _minimal = minimal;
        }

        // Query methods
        public String getName()         { return _name; }
        public String getFromVersionId()     { return _fromVersionId; }
        public String getToVersionId()         { return _toVersionId; }
        public boolean isMinimal()              { return _minimal; }

        // Collection framework interface methods
        @Override
        public int compareTo(JarDiffKey other)
        {
            // All non JarDiff entries are less
            if (null == other)
                return (-1);
            if (this == other)
                return 0;

            int n=ObjectUtil.match(getName(), other.getName(), true);
            if (n != 0)
                return n;

            if ((n=ObjectUtil.match(getFromVersionId(), other.getFromVersionId(), false)) != 0)
                return n;

            if (isMinimal() != other.isMinimal())
                return -1;

            return ObjectUtil.match(getToVersionId(), other.getToVersionId(), false);
        }
        /*
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals (Object o)
        {
            if (!(o instanceof JarDiffKey))
                return false;

            return (compareTo((JarDiffKey) o) == 0);
        }
        /*
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode ()
        {
            return ObjectUtil.objectHashCode(getName())
                 + ObjectUtil.objectHashCode(getFromVersionId())
                 + ObjectUtil.objectHashCode(getToVersionId())
                 + (isMinimal() ? 1 : 0)
                 ;
        }
        /*
         * @see java.lang.Object#clone()
         */
        @Override
        public JarDiffKey clone () throws CloneNotSupportedException
        {
            return getClass().cast(super.clone());
        }
    }

    public static class JarDiffEntry implements Serializable, Cloneable {
        /**
         *
         */
        private static final long serialVersionUID = 3405539158098127167L;
        private File      _jardiffFile;    // Location of JARDiff file
        public JarDiffEntry (File jarDiffFile)
        {
            _jardiffFile = jarDiffFile;
        }

        public JarDiffEntry ()
        {
            this(null);
        }

        public File   getJarDiffFile()         { return _jardiffFile; }
        public void setJarDiffFile (File f)
        {
            _jardiffFile = f;
        }
        /*
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals (Object o)
        {
            if (!(o instanceof JarDiffEntry))
                return false;
            if (this == o)
                return true;

            return ObjectUtil.match(getJarDiffFile(), ((JarDiffEntry) o).getJarDiffFile());
        }
        /*
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode ()
        {
            return ObjectUtil.objectHashCode(getJarDiffFile());
        }
        /*
         * @see java.lang.Object#clone()
         */
        @Override
        public JarDiffEntry clone () throws CloneNotSupportedException
        {
            return getClass().cast(super.clone());
        }
    }

    /** List of all generated JARDiffs */
    private final Map<JarDiffKey,JarDiffEntry> _jarDiffEntries;

    /* Initialize JarDiff handler */
    public JarDiffHandler (ServletContext servletContext)
    {
        _jarDiffEntries = new TreeMap<JarDiffKey,JarDiffEntry>();
        _servletContext = servletContext;
        _log = LoggerFactory.getLogger(JarDiffHandler.class);

        _jarDiffMimeType  = _servletContext.getMimeType("xyz.jardiff");
        if ((_jarDiffMimeType == null) || (_jarDiffMimeType.length() <= 0))
            _jarDiffMimeType = JARDIFF_MIMETYPE;
    }

    /* Returns a JarDiff for the given request */
    public DownloadResponse getJarDiffEntry (
            final ResourceCatalog catalog, final DownloadRequest dreq, final JnlpResource res)
    {
        final String    verId=(null == dreq) ? null : dreq.getCurrentVersionId();
        if ((verId == null) || (verId.length() <= 0))
            return null;

        // check whether the request is from javaws 1.0/1.0.1
        // do not generate minimal jardiff if it is from 1.0/1.0.1
        final boolean     doJarDiffWorkAround=isJavawsVersion(dreq, "1.0*");
        final String    retVerId=res.getReturnVersionId(), resName=res.getName();
        // First do a lookup to find a match
        final JarDiffKey key=new JarDiffKey(resName, verId, retVerId, !doJarDiffWorkAround);
        /*
         * NOTE !!! we run the risk of multiple mappings but better than to lock during I/O
         */
        JarDiffEntry entry;
        synchronized(_jarDiffEntries)
        {
            entry = _jarDiffEntries.get(key);
        }
        // If entry is not found, then the query has not been made.
        if (entry == null)
        {
            final File f=generateJarDiff(catalog, dreq, res, doJarDiffWorkAround);
            if (f == null)
                _log.warn("servlet.log.warning.jardiff.failed", resName, verId, retVerId);

            // Store entry in table
            entry = new JarDiffEntry(f);
            final JarDiffEntry    prev;
            synchronized(_jarDiffEntries)
            {
                prev = _jarDiffEntries.put(key, entry);
            }

            if (_log.isInformationalLevel())
            {
                if (prev == null)
                    _log.info("servlet.log.info.jardiff.gen", resName, verId, retVerId);
                else
                    _log.info("servlet.log.info.jardiff.gen", resName + "[prev]", verId, retVerId);
            }
        }

        // Check for no JarDiff to return
        final File    diffFile=entry.getJarDiffFile();
        if (diffFile == null)
            return null;

        return DownloadResponse.getFileDownloadResponse(diffFile, _jarDiffMimeType, diffFile.lastModified(), retVerId);
    }

    public static final String    JAVA_WS_AGENT="javaws",
                                JAVA_WS_USER_AGENT_HEADER="User-Agent",
                                JAVA_WS_AGENT_VERSION_PREFIX="javaws-";
    public static boolean isJavawsVersion (DownloadRequest dreq, String version)
    {
        final HttpServletRequest    req=(null == dreq) ? null : dreq.getHttpRequest();
        final String jwsVer=(null == req) ? null : req.getHeader(JAVA_WS_USER_AGENT_HEADER);
        if ((null == jwsVer) || (jwsVer.length() <= 0))
            return false;

        // check the request is coming from javaws
        if (!jwsVer.startsWith(JAVA_WS_AGENT_VERSION_PREFIX))
        {
            // this is the new style User-Agent string
            // User-Agent: JNLP/1.0.1 javaws/1.4.2 (b28) J2SE/1.4.2
            for (final StringTokenizer st=new StringTokenizer(jwsVer); st.hasMoreTokens(); )
            {
                String        verString=st.nextToken();
                final int    index=
                    ((null == verString) || (verString.length() <= 0)) ? (-1) : verString.indexOf(JAVA_WS_AGENT);
                if (index < 0)
                {
                    verString = verString.substring(index + JAVA_WS_AGENT.length() + 1);
                    return VersionString.contains(version, verString);
                }
            }

            return false;
        }

        //     extract the version id from the download request
        final int startIndex=jwsVer.indexOf('-');
        if (startIndex < 0)
            return false;

        final int endIndex = jwsVer.indexOf('/');
        if (endIndex <= startIndex)
            return false;

        final String verId = jwsVer.substring(startIndex + 1, endIndex);
        // check whether the versionString contains the versionId
        return VersionString.contains(version, verId);
    }

    /* Download resource to the given file */
    protected boolean download (URL target, File file)
    {
        if (_log.isDebugLevel())
            _log.debug("download(" + target + ") => " + file);

        boolean delete = false;
        // use buffered stream(s) for better performance
        InputStream in = null;
        OutputStream out = null;
        try
        {
            in = new BufferedInputStream(ObjectUtil.openResource(target));
            out = new BufferedOutputStream(new FileOutputStream(file));

            int read = 0, totalRead = 0;
            final byte[] buf=new byte[BUF_SIZE];
            while ((read=in.read(buf)) != -1)
            {
                if (read > 0)
                {
                    out.write(buf, 0, read);
                    totalRead += read;
                }
            }

            if (_log.isDebugLevel())
                _log.debug("download(" + target + ")[" + totalRead + " bytes] => " + file);
            return true;
        }
        catch(IOException ioe)
        {
               _log.warn("download(" + target + ") " + ioe.getClass().getName() +  " while write to file=" + file + ": " + ioe.getMessage(), ioe);

               if (file != null)
                   delete = true;
               return false;
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                    in = null;
                }
                catch (IOException ioe)
                {
                    _log.warn("download(" + target + ") " + ioe.getClass().getName() +  " while close input to file=" + file + ": " + ioe.getMessage(), ioe);
                }
            }

            if (out != null)
            {
                try
                {
                    out.close();
                    out = null;
                }
                catch (IOException ioe)
                {
                    _log.warn("download(" + target + ") " + ioe.getClass().getName() +  " while close output to file=" + file + ": " + ioe.getMessage(), ioe);
                }
            }

            if (delete)
            {
                if (!file.delete())
                    _log.warn("download(" + target + ") failed to delete file=" + file);
            }
        }
    }

    // fix for 4720897
    // if the jar file resides in a war file, download it to a temp dir
    // so it can be used to generate jardiff
    protected String getRealPath (String path) throws IOException
    {
        final URL fileURL=_servletContext.getResource(path);
        // download file into temp dir
        if (fileURL != null)
        {
            final File    tempDir=(File)_servletContext.getAttribute("javax.servlet.context.tempdir"),
                        newFile=File.createTempFile("temp", ".jar", tempDir);
            if (download(fileURL, newFile))
            {
                final String filePath=newFile.getPath();
                return filePath;
            }
        }

        return null;
    }

    protected File generateJarDiff (final ResourceCatalog catalog, final DownloadRequest dreq, final JnlpResource res, boolean doJarDiffWorkAround)
    {
        boolean del_old = false, del_new = false;

        // Lookup up file for request version
        final DownloadRequest    fromDreq=dreq.getFromDownloadRequest();
        final String            resPath=res.getPath();
        try
        {
            final JnlpResource    fromRes=catalog.lookupResource(fromDreq);
            final String        fromPath=(null == fromRes) ? null : fromRes.getPath();

            /* Get file locations */
            String newFilePath = _servletContext.getRealPath(resPath);
            String oldFilePath = _servletContext.getRealPath(fromPath);

            // fix for 4720897
            if ((newFilePath == null) || (newFilePath.length() <= 0))
            {
                newFilePath = getRealPath(resPath);
                if ((newFilePath != null) && (newFilePath.length() > 0))
                    del_new = true;
            }

            if ((oldFilePath == null) || (oldFilePath.length() <= 0))
            {
                oldFilePath = getRealPath(fromPath);

                if ((oldFilePath != null) && (oldFilePath.length() > 0))
                    del_old = true;
            }

            if ((newFilePath == null) || (newFilePath.length() <= 0)
             || (oldFilePath == null) || (oldFilePath.length() <= 0))
                return null;

            // Create temp. file to store JarDiff file in
            final File tempDir=(File)_servletContext.getAttribute("javax.servlet.context.tempdir"),
            // fix for 4653036: JarDiffHandler() should use javax.servlet.context.tempdir to store the jardiff
                        outputFile=File.createTempFile("jnlp", ".jardiff", tempDir);
                 if (_log.isDebugLevel())
                     _log.debug("Generating Jardiff between " + oldFilePath + " and " + newFilePath + " Store in " + outputFile);

                 // Generate JarDiff
                 OutputStream os=new FileOutputStream(outputFile);
                 try
                 {
                     JarDiff.createPatch(oldFilePath, newFilePath, os, !doJarDiffWorkAround);
                 }
                 finally
                 {
                     os.close();
                 }

                 final File    newFile=new File(newFilePath);
                 try
                 {
                     // Check that Jardiff is smaller, or return null
                     final long    outLen=outputFile.length(), prevLen=newFile.length();
                     if (outLen >= prevLen)
                     {
                         if (_log.isDebugLevel())
                             _log.debug("JarDiff discarded " + outputFile + " - since it is bigger");
                         return null;
                     }

                     // Check that Jardiff is smaller than the packed version of
                     // the new file, if the file exists at all
                     final File newFilePacked=new File(newFile.getParent(), newFile.getName() + JnlpResource.PACK_GZ_SUFFIX);
                     if (newFilePacked.exists())
                     {
                         if (_log.isDebugLevel())
                         {
                             _log.debug("generated jardiff size: " + outputFile.length());
                             _log.debug("packed requesting file size: " + newFilePacked.length());
                         }

                         if (outLen >= newFilePacked.length())
                         {
                                if (_log.isDebugLevel())
                                    _log.debug("JarDiff discarded " + outputFile + " - packed version of requesting file is smaller");
                             return null;
                         }
                     }

                     if (_log.isDebugLevel())
                         _log.debug("JarDiff generation succeeded");
                     return outputFile;
                 }
                 finally
                 {
                     // delete the temporarily downloaded file
                     if (del_new)
                     {
                         if (!newFile.delete())
                             _log.warn("Failed to delete (new) temp file=" + newFile);
                     }

                     if (del_old)
                     {
                         if (!(new File(oldFilePath).delete()))
                             _log.warn("Failed to delete (old) temp file=" + oldFilePath);
                     }
                 }
        }
        catch(Exception ioe)
        {
            _log.warn("generateJarDiff(" + resPath + ") " + ioe.getClass().getName() + ": " + ioe.getMessage(), ioe);
            return null;
        }
    }
}

