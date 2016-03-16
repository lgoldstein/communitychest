package net.community.chest.io.jar;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import net.community.chest.io.FileUtil;
import net.community.chest.lang.SysPropsEnum;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.resources.ResourceDataRetriever;
import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Used to access the manifest file</P>
 *
 * @author Lyor G.
 * @since Aug 6, 2007 7:40:34 AM
 */
public final class ManifestInfoExtractor {
    private ManifestInfoExtractor ()
    {
        // no instance
    }
    /**
     * @param anchor "anchor" class that resides in the same JAR as the
     * manifest we wish to extract - may NOT be null
     * @return extracted {@link Manifest} from the JAR
     * @throws IOException if unable to access the manifest
     */
    public static final Manifest getAnchorClassManifest (final Class<?> anchor) throws IOException
    {
        final URL            ul=ResourceDataRetriever.getAnchorClassLocation(anchor);
        final URLConnection    uc=(null == ul) ? null : ul.openConnection();
        if (null == uc)
            throw new FileNotFoundException(ClassUtil.getArgumentsExceptionLocation(ManifestInfoExtractor.class, "getAnchorClassManifest", (null == anchor) ? null : anchor.getName()) + " no URL connection to the class manifest");

        uc.setAllowUserInteraction(false);
        uc.setDoInput(true);
        uc.setDoOutput(false);
        uc.setDefaultUseCaches(true);
        uc.connect();

        InputStream        in=null;
        JarInputStream    ji=null;
        try
        {
            in = uc.getInputStream();
            ji = (null == in) /* should not happen */ ? null : new JarInputStream(in);

            return ji.getManifest();
        }
        finally
        {
            FileUtil.closeAll(ji, in);
        }
    }

    public static final String    DEFAULT_MANIFEST_FILENAME="MANIFEST.MF",
                                DEFAULT_MANIFEST_FOLDER_LOCATION="META-INF",
                                DEFAULT_MANIFEST_ENTRY_LOCATION=
                                    DEFAULT_MANIFEST_FOLDER_LOCATION + "/" + DEFAULT_MANIFEST_FILENAME;
    /**
     * @param jarFile A {@link JarFile} instance to extract the manifest from
     * (ignored if <code>null</code>)
     * @return The extracted {@link Manifest} - <code>null</code> if not found
     * or no {@link JarFile} instance to begin with.
     * @throws IOException if failed to read manifest data from file
     */
    public static final Manifest getManifestFile (final JarFile jarFile) throws IOException
    {
        JarEntry je=
            (null == jarFile) ? null : jarFile.getJarEntry(DEFAULT_MANIFEST_ENTRY_LOCATION);
        // sometimes there is a case-sensitivity issue
        if (null == je)
        {
            for (final Enumeration<JarEntry> entries=(null == jarFile) ? null : jarFile.entries();
                 (entries != null) && entries.hasMoreElements();
                 )
            {
                if (null == (je=entries.nextElement()))
                    continue;

                final String    n=je.getName();
                if (DEFAULT_MANIFEST_ENTRY_LOCATION.equalsIgnoreCase(n))
                    break;
            }
        }

        // create the manifest object
        InputStream    in=null;
        try
        {
            if ((in=(null == je) ? null : jarFile.getInputStream(je)) != null)
                return new Manifest(in);

            return null;
        }
        finally
        {
            FileUtil.closeAll(in);
        }
    }
    /**
     * Given a manifest file and given a jar file, make sure that
     * the contents of the manifest file is correct and return a
     * map of all the valid entries from the manifest.
     * @param manifest The {@link Manifest} to check - ignored
     * if <code>null</code>
     * @param jarFile The {@link JarFile} to use for validation - ignored
     * if <code>null</code>
     * @return A {@link Map} of all valid entries - key=entry name,
     * value=entry {@link Attributes}
     * @throws IOException If failed to read from jar file or if duplicate
     * {@link Attributes} found for an entry
     */
    public static final Map<String,Attributes> pruneManifest (
                final Manifest manifest, final JarFile jarFile) throws IOException
    {
        final Map<String,? extends Attributes>                                eMap=
            ((null == manifest) || (null == jarFile)) ? null : manifest.getEntries();
        final Collection<? extends Map.Entry<String,? extends Attributes>>    eList=
            ((null == eMap) || (eMap.size() <= 0)) ? null : eMap.entrySet();
        if ((null == eList) || (eList.size() <= 0))
            return null;

        Map<String,Attributes>    ret=null;
        for (final Map.Entry<String,? extends Attributes> ee : eList)
        {
            final String        elem=(null == ee) ? null : ee.getKey();
            final Attributes    attrs=(null == ee) ? null : ee.getValue();
            final ZipEntry        je=
                ((null == elem) || (elem.length() <= 0)) ? null : jarFile.getEntry(elem);
            if (null == je)
                continue;

            if (null == ret)
                ret = new TreeMap<String,Attributes>();

            final Attributes    prev=ret.put(elem, attrs);
            if (prev != null)
                throw new StreamCorruptedException("Multiple attributes for entry=" + elem);
        }

        return ret;
    }
    // returns deleted entries
    public static final Collection<Map.Entry<String,Attributes>> updateManifestEntries (
            final Map<String,? extends Attributes>    oMap,
            final Collection<String>                 vKeys)
    {
        final Collection<String>    oKeys=
            ((null == oMap) || (oMap.size() <= 0)) ? null : oMap.keySet();
        if ((null == oKeys) || (oKeys.size() <= 0))
            return null;
        // if no valid keys then simply reset the original map
        if ((null == vKeys) || (vKeys.size() <= 0))
        {
            final Collection<? extends Map.Entry<String,? extends Attributes>>    eSet=
                oMap.entrySet();    // cannot be null/empty since we already checked the oMap.size()
            final Collection<Map.Entry<String,Attributes>>                        ret=
                new ArrayList<Map.Entry<String,Attributes>>(eSet.size());
            for (final Map.Entry<String,? extends Attributes> ee : eSet)
            {
                if (null == ee)
                    continue;
                ret.add(new MapEntryImpl<String,Attributes>(ee.getKey(), ee.getValue()));
            }
            oMap.clear();
            return ret;
        }

        Collection<String>    delKeys=null;
        for (final String k : oKeys)
        {
            if (vKeys.contains(k))
                continue;
            if (null == delKeys)
                delKeys = new LinkedList<String>();
            delKeys.add(k);
        }

        final int    numDel=(null == delKeys) ? 0 : delKeys.size();
        if (numDel <= 0)
            return null;    // nothing to delete

        final Collection<Map.Entry<String,Attributes>>    ret=
            new ArrayList<Map.Entry<String,Attributes>>(numDel);
        for (final String k : delKeys)
        {
            final Attributes    aa=oMap.remove(k);
            ret.add(new MapEntryImpl<String,Attributes>(k, aa));
        }

        return ret;
    }
    // returns deleted entries
    public static final Collection<Map.Entry<String,Attributes>> updateManifestEntries (
            final Map<String,? extends Attributes> oMap,
            final Map<String,? extends Attributes> vMap)
    {
        return updateManifestEntries(oMap, ((null == vMap) || (vMap.size() <= 0)) ? null : vMap.keySet());
    }

    public static final String    DEFAULT_MANIFEST_VERSION="1.0";
    /**
     * Make sure all the manifest entries are valid (via calling
     * {@link #pruneManifest(Manifest, JarFile)}). If we do not
     * have a manifest, then we create a new manifest file by adding the
     * appropriate headers
     * @param org Original {@link Manifest} - if <code>null</code> one
     * will be created
     * @param jarFile The {@link JarFile} to use in order to validate/create
     * a new manifest
     * @return The updated/created {@link Manifest}
     * @throws IOException if failed to read/validate the manifest
     */
    public static final Manifest validateEntries (
            final Manifest org, final JarFile jarFile) throws IOException
    {
        Manifest                                manifest=org;
        final Map<String,? extends Attributes>    eMap=(null == manifest) ? null : manifest.getEntries();
        if ((eMap != null) && (eMap.size() > 0))
        {
            final Map<String,? extends Attributes>    vMap=pruneManifest(manifest, jarFile);
            updateManifestEntries(eMap, vMap);
        }
        else // if there are no pre-existing entries in the manifest, then we put a few default ones in
        {
            final String    javaVerProp=SysPropsEnum.JAVAVERSION.getPropertyValue(),
                            javaVndrProp=SysPropsEnum.JAVAVENDOR.getPropertyValue();
            if (null == manifest)    // TODO add Attributes for the JAR entries (?)
                manifest = new Manifest();

            final Attributes attributes=manifest.getMainAttributes();
            attributes.putValue(Attributes.Name.MANIFEST_VERSION.toString(), DEFAULT_MANIFEST_VERSION);
            attributes.putValue("Created-By",  javaVerProp + " (" + javaVndrProp + ")" );
        }
        return manifest;
    }

    public static final byte[] getManifestBytes (final Manifest m) throws IOException
    {
        if (null == m)
            return null;

        final Map<String,? extends Attributes>    eMap=m.getEntries();
        final Attributes                        aMain=m.getMainAttributes();
        final int                                numEntries=(null == eMap) ? 0 : eMap.size(),
                                                numAttrs=(null == aMain) ? 0 : aMain.size(),
                                                numTotal=
            Math.max(0, numEntries) + Math.max(0, numAttrs) + 1;
        try(ByteArrayOutputStream baos=new ByteArrayOutputStream(numTotal * 64)) {
            m.write(baos);
            return baos.toByteArray();
        }
    }
}
