/*
 * @(#)ResourceCatalog.java    1.6 05/11/17
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import jnlp.sample.servlet.download.DownloadRequest;
import jnlp.sample.servlet.download.DownloadResponse;
import jnlp.sample.util.ObjectUtil;
import jnlp.sample.util.VersionID;
import jnlp.sample.util.VersionString;
import jnlp.sample.util.log.Logger;
import jnlp.sample.util.log.LoggerFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXParseException;

public class ResourceCatalog {
    public static final String VERSION_XML_FILENAME="version.xml";

    protected final transient Logger _log;
    private final ServletContext _servletContext;
    public final ServletContext getServletContext ()
    {
        return _servletContext;
    }
    /* Class to contain the information we know
     *  about a specific directory
     */
    static public class PathEntries {
        /* Version-based entries at this particular path */
        private List<JnlpResource> _versionXmlList;
        private List<JnlpResource> _directoryList;
        private List<JnlpResource> _platformList;
        /* Last time this entry was updated */
        private long _lastModified; // Last modified time of entry;

        public PathEntries (List<JnlpResource> versionXmlList, List<JnlpResource> directoryList, List<JnlpResource> platformList, long lastModified)
        {
            _versionXmlList = versionXmlList;
            _directoryList = directoryList;
            _platformList = platformList;
            _lastModified = lastModified;
        }

        public List<JnlpResource> getDirectoryList ()  { return _directoryList; }
        public void setDirectoryList (List<JnlpResource> dirList)
        {
            _directoryList = dirList;
        }

        public List<JnlpResource> getVersionXmlList () { return _versionXmlList; }
        public void setVersionXmlList (List<JnlpResource>  l)
        {
            _versionXmlList = l;
        }

        public List<JnlpResource> getPlatformList ()   { return _platformList; }
        public void setPlatformList (List<JnlpResource>  l)
        {
            _platformList = l;
        }

        public long getLastModified () { return _lastModified; }
        public void setLastModified (long m)
        {
            _lastModified = m;
        }
       }

    private Map<String,PathEntries> _entries;
    // NOTE !!! MAY NOT RETURN NULL
    protected Map<String,PathEntries> getEntriesMap ()
    {
        return _entries;
    }

    protected void setEntriesMap (Map<String,PathEntries> m)
    {
        _entries = m;
    }

    public ResourceCatalog (ServletContext servletContext)
    {
        _entries = new TreeMap<String,PathEntries>();
        _servletContext = servletContext;
        _log = LoggerFactory.getLogger(ResourceCatalog.class);
    }

    protected int lookupRecursive (final DownloadRequest dreq, final String reqVersion, final JnlpResource[] result)
    {
        final String     path=(null == dreq) ? "" : dreq.getPath();
        // Split request up into path and name
        String    name=null, dir=null;
        int     idx=path.lastIndexOf('/');
        if (idx < 0)
        {
            name = path;
        }
        else
        {
            name = path.substring(idx + 1); // Exclude '/'
            dir  = path.substring(0, idx + 1); // Include '/'
        }

        // Lookup up already parsed entries, and scan directory for entries if necessary
        final Map<String,PathEntries>    em=getEntriesMap();
        PathEntries                     pentries=em.get(dir);
        final JnlpResource                 xmlVersionResPath=
            new JnlpResource(getServletContext(), dir + VERSION_XML_FILENAME);
        final long                        xmlResModified=xmlVersionResPath.getLastModified(),
                                        pentriesModified=(null == pentries) ? 0L : pentries.getLastModified();
        if ((pentries == null)
         || (xmlVersionResPath.exists() && (xmlResModified > pentriesModified)))
        {
            if (_log.isInformationalLevel())
                _log.info("servlet.log.scandir", dir);

            // Scan XML file
            final List<JnlpResource>    dirList=scanDirectory(dir, dreq, reqVersion),
                                        versionList=new ArrayList<JnlpResource>(),
                                        platformList=new ArrayList<JnlpResource>();
            parseVersionXML(versionList, platformList, dir, xmlVersionResPath);
            pentries = new PathEntries(versionList, dirList, platformList, xmlVersionResPath.getLastModified());
            em.put(dir, pentries);
        }

        if ((dreq != null) && dreq.isPlatformRequest())
            return findMatch(pentries.getPlatformList(), name, reqVersion, dreq, result);

        // First lookup in versions.xml file
        final int sts1=findMatch(pentries.getVersionXmlList(), name, reqVersion, dreq, result);
        if (sts1 != DownloadResponse.STS_00_OK)
        {
            // Then lookup in directory
            int sts2=findMatch(pentries.getDirectoryList(), name, reqVersion, dreq, result);
            if (sts2 != DownloadResponse.STS_00_OK)
            {
                // fix for 4450104
                // try rescan and see if it helps
                final List<JnlpResource>    dirList=scanDirectory(dir, dreq, reqVersion);
                pentries.setDirectoryList(dirList);
                // try again after rescanning directory
                if ((sts2=findMatch(pentries.getDirectoryList(), name, reqVersion, dreq, result)) != DownloadResponse.STS_00_OK)
                    return Math.max(sts1, sts2);    // Throw the most specific error code
            }
        }

        if (_log.isDebugLevel())
            _log.debug("lookupRecursive(" + path + ")[" + reqVersion + "] => " + result[0]);
        return DownloadResponse.STS_00_OK;
    }
    // use same naming convention as Maven - i.e name-version.ext
    protected int lookupDirect (final DownloadRequest dreq, final String reqVersion, final JnlpResource[] result)
    {
        if ((null == dreq) || (null == reqVersion) || (reqVersion.length() <= 0))
            return DownloadResponse.ERR_10_NO_RESOURCE;

        final String     path=dreq.getPath();
        final int        idx=(null == path) ? (-1) : path.lastIndexOf('.');
        if (idx < 0)    // if no extension then assume no resource
            return DownloadResponse.ERR_10_NO_RESOURCE;

        final String        subPath=path.substring(0, idx),    // excluding the '.;
                            name=path.substring(path.lastIndexOf('/') + 1),
                            ext=path.substring(idx),    // including the '.'
                            newPath=subPath + "-" + reqVersion + ext;
        final JnlpResource    res=
            new JnlpResource(getServletContext(), name, reqVersion, dreq.getOS(), dreq.getArch(), dreq.getLocale(), newPath, reqVersion);
        if (!res.exists())
            return DownloadResponse.ERR_10_NO_RESOURCE;

        if (_log.isDebugLevel())
            _log.debug("lookupDirect(" + path + ")[" + reqVersion + "] => " + res);

        result[0] = res;
        return DownloadResponse.STS_00_OK;
    }

    protected int resolveLookupResult (final JnlpResource     directLookup,
                                       final int            stsDirect,
                                       final JnlpResource    rcrsvLookup,
                                       final int            stsRcrsv,
                                       final JnlpResource[]    result)
    {
        if (stsRcrsv == DownloadResponse.STS_00_OK)
        {
            result[0] = rcrsvLookup;    // prefer more detailed location
            return DownloadResponse.STS_00_OK;
        }
        else if (stsDirect == DownloadResponse.STS_00_OK)
        {
            result[0] = directLookup;
            return DownloadResponse.STS_00_OK;
        }

        return Math.max(stsDirect, stsRcrsv);
    }

    public JnlpResource lookupResource (final DownloadRequest dreq, final String reqVersion)
            throws ErrorResponseException
    {
        final JnlpResource[]    result=new JnlpResource[1];
        final int                stsDirect=lookupDirect(dreq, reqVersion, result);
        final JnlpResource        directLookup=
            (DownloadResponse.STS_00_OK == stsDirect) ? result[0] : null;

        if (result[0] != null)        // start from scratch
            result[0] = null;

        final int            stsRcrsv=lookupRecursive(dreq, reqVersion, result);
        final JnlpResource    rcrsvLookup=
            (DownloadResponse.STS_00_OK == stsRcrsv) ? result[0] : null;

              if (result[0] != null)        // start from scratch
               result[0] = null;

              int                    sts=
                  resolveLookupResult(directLookup, stsDirect, rcrsvLookup, stsRcrsv, result);
              final JnlpResource    res=result[0];
        if ((DownloadResponse.STS_00_OK == sts) && (res == null))
            sts = DownloadResponse.ERR_10_NO_RESOURCE;

        if (sts != DownloadResponse.STS_00_OK)
            throw new ErrorResponseException(DownloadResponse.getJnlpErrorResponse(DownloadResponse.ERR_10_NO_RESOURCE));

        return res;
    }

    public JnlpResource lookupResource (final DownloadRequest dreq) throws ErrorResponseException
    {
        return lookupResource(dreq, (null == dreq) ? null : dreq.getVersion());
    }
       /* This method finds the best match, or return the best error code. The
     *  result parameter must be an array with room for one element.
     *
     *  If a match is found, the method returns DownloadResponse.STS_00_OK
     *  If one or more entries matches on: name, version-id, os, arch, and locale,
     *  then the one with the highest version-id is set in the result[0] field.
     *
     *  If a match is not found, it returns an error code, either: ERR_10_NO_RESOURCE,
     *  ERR_11_NO_VERSION, ERR_20_UNSUP_OS, ERR_21_UNSUP_ARCH, ERR_22_UNSUP_LOCALE,
     *  ERR_23_UNSUP_JRE.
     *
     */
    public static int findMatch (final Collection<? extends JnlpResource>    list,
                                 final String                                name,
                                 final String                                reqVersion,
                                 final DownloadRequest                        dreq,
                                 final JnlpResource[]                         result)
    {
        if ((list == null) || (list.size() <= 0) || (null == dreq))
            return DownloadResponse.ERR_10_NO_RESOURCE;

        // Setup return values
        VersionID            bestVersionId = null;
        int                 error=DownloadResponse.ERR_10_NO_RESOURCE;
        final VersionString    vs=new VersionString(reqVersion);
        // Iterate through entries
        for (final JnlpResource respath : list)
        {
            if (null == respath)
                continue;

            final VersionID vid=new VersionID(respath.getVersionId());
            final int        sts=matchEntry(name, vs, dreq, respath, vid);
            if (sts == DownloadResponse.STS_00_OK)
            {
                if ((result[0] == null) || vid.isGreaterThan(bestVersionId))
                {
                    result[0] = respath;
                    bestVersionId = vid;
                }
            }
            else
            {
                error = Math.max(error, sts);
            }
        }

        return (result[0] != null) ? DownloadResponse.STS_00_OK : error;
    }

    public static int matchEntry (String name, VersionString vs, DownloadRequest dreq, JnlpResource jnlpres, VersionID vid)
    {
        if ((null == jnlpres) || (null == vs))
            return DownloadResponse.ERR_10_NO_RESOURCE;
        if (!name.equals(jnlpres.getName()))
            return DownloadResponse.ERR_10_NO_RESOURCE;
        if (!vs.contains(vid))
            return DownloadResponse.ERR_11_NO_VERSION;
        if (!prefixMatchLists(jnlpres.getOSList(), dreq.getOS()))
            return DownloadResponse.ERR_20_UNSUP_OS;
        if (!prefixMatchLists(jnlpres.getArchList(), dreq.getArch()))
            return DownloadResponse.ERR_21_UNSUP_ARCH;
        if (!prefixMatchLists(jnlpres.getLocaleList(), dreq.getLocale()))
            return DownloadResponse.ERR_22_UNSUP_LOCALE;

        return DownloadResponse.STS_00_OK;
    }

    private static boolean prefixMatchStringList (String[] prefixList, String target)
    {
        // No prefixes matches everything
        if ((prefixList == null) || (prefixList.length <= 0))
            return true;

        // No target, but a prefix list does not match anything
        if ((target == null) || (target.length() <= 0))
            return false;

        for (final String p : prefixList)
        {
            if ((null == p) || (p.length() <= 0))
                continue;
            if (target.startsWith(p))
                return true;
        }

        return false;
    }
    /* Return true if at least one of the strings in 'prefixes' are a prefix
     * to at least one of the 'keys'.
     */
    public static boolean prefixMatchLists (String[] prefixes, String[] keys)
    {
        // The prefixes are part of the server resources. If none is given,
        // everything matches
        if ((prefixes == null) || (prefixes.length <= 0))
            return true;
        // If no os keyes was given, and the server resource is keyed of this,
        // then return false.
        if ((keys  == null) || (keys.length <= 0))
            return false;

        // Check for a match on a key
        for(final String k : keys)
        {
            if (prefixMatchStringList(prefixes, k))
                return true;
        }

        return false;
    }

    private static String appendToFilename (String org, String prefix, String ... comps)
    {
        if ((null == prefix) || (prefix.length() <= 0)
         || (null == comps) || (comps.length <= 0))
            return org;

        String    filename=org;
        for (final String c : comps)
        {
            if ((null == c) || (c.length() <= 0))
                continue;

            filename += prefix + c;
        }

        return filename;
    }

    public static final char    VERSION_SEP_CHAR='V',
                                OS_SEP_CHAR='O',
                                ARCH_SEP_CHAR='A',
                                LOCALE_SEP_CHAR='L';
    public static final String    PATH_PREFIX_SEP="__",
                                VERSION_PATH_PREFIX=PATH_PREFIX_SEP + String.valueOf(VERSION_SEP_CHAR),
                                OS_PATH_PREFIX=PATH_PREFIX_SEP + String.valueOf(OS_SEP_CHAR),
                                ARCH_PATH_PREFIX=PATH_PREFIX_SEP + String.valueOf(ARCH_SEP_CHAR),
                                LOCALE_PATH_PREFIX=PATH_PREFIX_SEP + String.valueOf(LOCALE_SEP_CHAR);
    /* This method scans the directory pointed to by the
     *  given path and creates a list of ResourcePath elements
     *  that contains information about all the entries
     *
     *  The version-based information is encoded in the file name
     *  given the following format:
     *
     *     entry ::= <name> __ ( <options> ). <ext>
     *     options ::= <option> ( __ <options>  )?
     *     option  ::= V<version-id>
     *               | O<os>
     *               | A<arch>
     *               | L<locale>
     *
     */
    public static String jnlpGetPath (final DownloadRequest dreq, final String reqVersion)
    {
        // fix for 4474021
        // try to manually generate the filename
        // extract file name
        String    path=(null == dreq) ? "" : dreq.getPath(),
                filename=path.substring(path.lastIndexOf('/') + 1),
                ext=null;

        path = path.substring(0, path.lastIndexOf('/') + 1);
        int    idx=filename.lastIndexOf('.');
        if (idx >= 0)
        {
            ext = filename.substring(idx + 1);
            filename = filename.substring(0, idx);
        }

        filename = appendToFilename(filename, VERSION_PATH_PREFIX, reqVersion);
        filename = appendToFilename(filename, OS_PATH_PREFIX, (null == dreq) ? null : dreq.getOS());
        filename = appendToFilename(filename, ARCH_PATH_PREFIX, (null == dreq) ? null : dreq.getArch());
        filename = appendToFilename(filename, LOCALE_PATH_PREFIX, (null == dreq) ? null : dreq.getLocale());

        if ((ext != null) && (ext.length() > 0))
            filename += "." + ext;

        return path + filename;
    }

    public List<JnlpResource> scanDirectory (String dirPath, DownloadRequest dreq, String reqVersion)
    {
        final ServletContext    ctx=getServletContext();
        final String            effPath=ctx.getRealPath(dirPath);
        // fix for 4474021
        if ((effPath == null) || (effPath.length() <= 0))
        {
            final String        path=jnlpGetPath(dreq, reqVersion),
                                reqPath=dreq.getPath(),
                                name=reqPath.substring(path.lastIndexOf('/') + 1);
            final JnlpResource    jnlpres=new JnlpResource(ctx, name, reqVersion, dreq.getOS(), dreq.getArch(), dreq.getLocale(), path, reqVersion);
            // the file does not exist
            if (!jnlpres.exists())
                return null;

            // we create a bigger modifiable list since it may be manipulated further down the code
            List<JnlpResource> list=new ArrayList<JnlpResource>();
            list.add(jnlpres);
            return list;
        }

        final File dir = new File(effPath);
        if (_log.isDebugLevel())
            _log.debug("scanDirectory(" + dir + ") => " + effPath);
        if (dir.exists() && dir.isDirectory())
        {
            final File[]                entries=dir.listFiles();
            final int                    numEntries=(null == entries) ? 0 : entries.length;
            final List<JnlpResource>    list=new ArrayList<JnlpResource>(numEntries);
            if (numEntries > 0)
            {
                for (final File f : entries)
                {
                    if (null == f)
                        continue;

                    final JnlpResource jnlpres=parseFileEntry(dirPath, f.getName());
                    if (null == jnlpres)
                        continue;

                    if (_log.isDebugLevel())
                        _log.debug("scanDirectory(" + dir + ") read file resource: " + jnlpres);
                    list.add(jnlpres);
                }
            }
        }

        return null;
    }

    protected JnlpResource parseFileEntry (final String dir, final String filename)
    {
        if ((null == filename) || (filename.length() <= 0))
            return null;

        int idx=filename.indexOf(PATH_PREFIX_SEP);
        if (idx < 0)
            return null;

        // Cut out name and extension
        final String name=filename.substring(0, idx), extension;

        String rest=filename.substring(idx);
        if ((idx=rest.lastIndexOf('.')) >= 0)
        {
            extension = rest.substring(idx);
            rest = rest.substring(0, idx);
        }
        else
            extension = "";

        // Parse options
        final Collection<String>    osList=new LinkedList<String>(),
                                    archList=new LinkedList<String>(),
                                    localeList=new LinkedList<String>();
        String                         versionId=null;
        while ((rest != null) && (rest.length() > 0))
        {
            /* Must start with __ at this point */
            if (!rest.startsWith(PATH_PREFIX_SEP))
                return null;
            rest = rest.substring(PATH_PREFIX_SEP.length());
            // Get option and argument
            final char option=Character.toUpperCase(rest.charAt(0));    // be lenient

            final String arg;
            if ((idx= rest.indexOf(PATH_PREFIX_SEP)) >= 0)
            {
                arg = rest.substring(1);
                rest = "";
            }
            else
            {
                arg = rest.substring(1, idx);
                rest = rest.substring(idx);
            }

            switch(option)
            {
                case VERSION_SEP_CHAR     :
                    versionId = arg;
                    break;

                case OS_SEP_CHAR        :
                    osList.add(arg);
                    break;

                case ARCH_SEP_CHAR        :
                    archList.add(arg);
                    break;

                case LOCALE_SEP_CHAR    :
                    localeList.add(arg);
                    break;

                default    :
                    return null; // error
            }
        }

        return new JnlpResource(getServletContext(),
                                name + extension, /* Resource name in URL request */
                                versionId,
                                listToStrings(osList),
                                listToStrings(archList),
                                listToStrings(localeList),
                                dir + filename, /* Resource name in WAR file */
                                versionId);
    }

    private static String[] listToStrings (Collection<String> list)
    {
        if ((null == list) || (list.size() <= 0))
            return null;

        return list.toArray(new String[list.size()]);
    }

    public static final String    JNLP_VERSIONS_ELEM_NAME="jnlp-versions",
                                RESOURCE_ELEM_NAME="resource",
                                    RESOURCE_ELEM_PATTERN="<" + RESOURCE_ELEM_NAME + ">",
                                PATTERN_ELEM_NAME="pattern",
                                    PATTERN_ELEM_PATTERN="<" + PATTERN_ELEM_NAME + ">",
                                NAME_ELEM_NAME="name",
                                    NAME_ELEM_PATTERN="<" + NAME_ELEM_NAME + ">",
                                VERSION_ID_ELEM_NAME="version-id",
                                    VERSION_ID_ELEM_PATTERN="<" + VERSION_ID_ELEM_NAME + ">",
                                OS_ELEM_NAME="os",
                                    OS_ELEM_PATTERN="<" + OS_ELEM_NAME + ">",
                                ARCH_ELEM_NAME="arch",
                                    ARCH_ELEM_PATTERN="<" + ARCH_ELEM_NAME + ">",
                                LOCALE_ELEM_NAME="locale",
                                    LOCALE_ELEM_PATTERN="<" + LOCALE_ELEM_NAME + ">",
                                FILE_ELEM_NAME="file",
                                    FILE_ELEM_PATTERN="<" + FILE_ELEM_NAME + ">",
                                PLATFORM_ELEM_NAME="platform",
                                    PLATFORM_ELEM_PATTERN="<" + PLATFORM_ELEM_NAME + ">",
                                   PRODUCT_VERSION_ELEM_NAME="product-version-id",
                                       PRODUCT_VERSION_ELEM_PATTERN="<" + PRODUCT_VERSION_ELEM_NAME + ">";

    protected void parseVersionXML (final Collection<JnlpResource> versionList,
                                      final Collection<JnlpResource> platformList,
                                      final String dir, final JnlpResource versionRes)
    {
        if ((null == versionRes) || (!versionRes.exists()))
            return;

        // Parse XML into a more understandable format
        XMLNode root=null;
        try
        {
            final DocumentBuilderFactory    docBuilderFactory=DocumentBuilderFactory.newInstance();
            final DocumentBuilder            docBuilder=docBuilderFactory.newDocumentBuilder();
            final URL                        versionURL=versionRes.getResource();
            InputStream                        inVersion=null;
            final Document                    doc;
            try
            {
                inVersion = new BufferedInputStream(ObjectUtil.openResource(versionURL));

                if (null == (doc=docBuilder.parse(inVersion)))
                    throw new DOMException(DOMException.SYNTAX_ERR, "No document parsed");
            }
            finally
            {
                if (inVersion != null)
                {
                    try
                    {
                        inVersion.close();
                    }
                    catch(IOException e)
                    {
                        // ignored
                    }
                }
            }

            final Element    docElem=doc.getDocumentElement();
            docElem.normalize();
            // Convert document into an XMLNode structure, since we already got utility methods
            //  to handle these. We should really use the data-binding stuff here - but that will come
            //  later
            //
            if (null == (root=XMLParsing.convert(docElem)))
                throw new DOMException(DOMException.NAMESPACE_ERR, "No root found");
        }
        catch (SAXParseException err)
        {
            _log.warn("servlet.log.warning.xml.parsing",
                      versionRes.getPath(),
                      Integer.toString(err.getLineNumber()),
                      err.getMessage());
            return;
        }
        catch (Throwable t)
        {
            _log.warn("servlet.log.warning.xml.reading", t, versionRes.getPath());
            return;
        }

        // Check that root element is a <jnlp> tag
        final String    rootName=root.getName();
        if (!JNLP_VERSIONS_ELEM_NAME.equalsIgnoreCase(rootName))
        {
            _log.warn("servlet.log.warning.xml.missing-jnlp", versionRes.getPath());
            return;
        }

        // Visit all <resource> elements
        XMLParsing.visitElements(root, RESOURCE_ELEM_PATTERN, new XMLParsing.ElementVisitor() {
                /*
                 * @see jnlp.sample.servlet.XMLParsing.ElementVisitor#visitElement(jnlp.sample.servlet.XMLNode)
                 */
                @Override
                public void visitElement(XMLNode node)
                {
                    XMLNode pattern = XMLParsing.findElementPath(node, PATTERN_ELEM_PATTERN);
                    if (pattern == null)
                    {
                        _log.warn("servlet.log.warning.xml.missing-pattern", versionRes.getPath());
                        return;
                    }

                    // Parse pattern
                    final String     name=XMLParsing.getElementContent(pattern , NAME_ELEM_PATTERN, "");
                    final String    versionId=XMLParsing.getElementContent(pattern , VERSION_ID_ELEM_PATTERN);
                    final String[]    os=XMLParsing.getMultiElementContent(pattern, OS_ELEM_PATTERN);
                    final String[]     arch=XMLParsing.getMultiElementContent(pattern, ARCH_ELEM_PATTERN);
                    final String[]     locale=XMLParsing.getMultiElementContent(pattern, LOCALE_ELEM_PATTERN);
                    // Get return request
                    final String    file=XMLParsing.getElementContent(node, FILE_ELEM_PATTERN);
                    if ((versionId == null) || (versionId.length() <= 0)
                     || (file == null) || (file.length() <= 0))
                    {
                        _log.warn("servlet.log.warning.xml.missing-elems", versionRes.getPath());
                        return;
                    }

                    final JnlpResource res=new JnlpResource(getServletContext(),
                                                            name,
                                                            versionId,
                                                            os,
                                                            arch,
                                                            locale,
                                                            dir + file,
                                                            versionId);
                    if (res.exists())
                    {
                        if (_log.isDebugLevel())
                            _log.debug("Read resource: " + res);

                        versionList.add(res);
                    }
                    else
                        _log.warn("servlet.log.warning.missing-file", file, versionRes.getPath());
                }
            });

        // Visit all <resource> elements
        XMLParsing.visitElements(root, PLATFORM_ELEM_PATTERN, new XMLParsing.ElementVisitor() {
                /*
                 * @see jnlp.sample.servlet.XMLParsing.ElementVisitor#visitElement(jnlp.sample.servlet.XMLNode)
                 */
                @Override
                public void visitElement (XMLNode node)
                {
                    XMLNode pattern=XMLParsing.findElementPath(node, PATTERN_ELEM_PATTERN);
                    if (pattern == null)
                    {
                        _log.warn("servlet.log.warning.xml.missing-pattern", versionRes.getPath());
                        return;
                    }

                    // Parse pattern
                    final String     name=XMLParsing.getElementContent(pattern , NAME_ELEM_PATTERN, "");
                    final String     versionId=XMLParsing.getElementContent(pattern , VERSION_ID_ELEM_PATTERN);
                    final String[]     os=XMLParsing.getMultiElementContent(pattern, OS_ELEM_PATTERN);
                    final String[]     arch=XMLParsing.getMultiElementContent(pattern, ARCH_ELEM_PATTERN);
                    final String[]     locale=XMLParsing.getMultiElementContent(pattern, LOCALE_ELEM_PATTERN);
                    // Get return request
                    final String    file=XMLParsing.getElementContent(node, FILE_ELEM_PATTERN);
                    final String    productId=XMLParsing.getElementContent(node, PRODUCT_VERSION_ELEM_PATTERN);
                    if ((versionId == null) || (versionId.length() <= 0)
                     || (file == null) || (file.length() <= 0)
                     || (productId == null) || (productId.length() <= 0))
                    {
                        _log.warn("servlet.log.warning.xml.missing-elems2", versionRes.getPath());
                        return;
                    }

                    final JnlpResource res=new JnlpResource(getServletContext(),
                                                            name,
                                                            versionId,
                                                            os,
                                                            arch,
                                                            locale,
                                                            dir + file,
                                                            productId);
                    if (res.exists())
                    {
                        if (_log.isDebugLevel())
                            _log.debug("Read platform resource: " + res);
                        platformList.add(res);
                    }
                    else
                        _log.warn("servlet.log.warning.missing-file", file, versionRes.getPath());
                }
            });
    }
}


