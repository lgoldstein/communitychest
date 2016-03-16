/*
 * @(#)JarDiffPatcher.java    1.7 05/11/17
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

package jnlp.sample.jardiff;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * JarDiff is able to create a jar file containing the delta between two
 * jar files (old and new). The delta jar file can then be applied to the
 * old jar file to reconstruct the new jar file.
 * <p>
 * Refer to the JNLP spec for details on how this is done.
 *
 * @version 1.11, 06/26/03
 */
public class JarDiffPatcher implements JarDiffConstants, Patcher {
    private static final int DEFAULT_READ_SIZE = 2048;
    private static final byte[] newBytes = new byte[DEFAULT_READ_SIZE];

    public static ResourceBundle getResources ()
    {
        return JarDiff.getResources();
    }

    public JarDiffPatcher ()
    {
        super();
    }
    /*
     * @see jnlp.sample.jardiff.Patcher#applyPatch(jnlp.sample.jardiff.Patcher.PatchDelegate, java.lang.String, java.lang.String, java.io.OutputStream)
     */
    @Override
    public void applyPatch (Patcher.PatchDelegate delegate, String oldJarPath, String jarDiffPath, OutputStream result) throws IOException
    {
        final File                     oldFile=new File(oldJarPath), diffFile=new File(jarDiffPath);
        final JarFile                oldJar=new JarFile(oldFile), jarDiff=new JarFile(diffFile);
        final Collection<String>    ignoreSet=new HashSet<String>();
        final Map<String,String>    renameMap=new TreeMap<String,String>();

        determineNameMapping(jarDiff, ignoreSet, renameMap);

        // Files to implicit move
        final Collection<String> oldjarNames=new HashSet<String>();
        for (final Enumeration<JarEntry> oldEntries = oldJar.entries(); (oldEntries != null) && oldEntries.hasMoreElements(); )
        {
            final JarEntry    e=oldEntries.nextElement();
            final String    en=(null == e) ? null : e.getName();
            if ((null == en) || (en.length() <= 0))
                continue;

            oldjarNames.add(en);
        }

        // size depends on the three parameters below, which is
        // basically the counter for each loop that do the actual
        // writes to the output file
        // since oldjarNames.size() changes in the first two loop
        // below, we need to adjust the size accordingly also when
        // oldjarNames.size() changes
        // get all keys in renameMap
        final Collection<? extends Map.Entry<String,String>>    pairs=renameMap.entrySet();
        long size = oldjarNames.size() + ((null == pairs) ? 0 : pairs.size()) + jarDiff.size(), currentEntry = 0L;

        // Handle all remove commands
        oldjarNames.removeAll(ignoreSet);
        size -= ignoreSet.size();


        // Add content from JARDiff
        final JarOutputStream    jos=new JarOutputStream(result);
        try
        {
            for (final Enumeration<JarEntry> entries=jarDiff.entries(); (entries != null)&& entries.hasMoreElements(); )
            {
                final JarEntry entry=entries.nextElement();
                final String    en=(null == entry) ? null : entry.getName();
                if (!INDEX_NAME.equals(en))
                {
                    updateDelegate(delegate, currentEntry, size);
                    currentEntry++;

                    writeEntry(jos, entry, jarDiff);

                    // Remove entry from oldjarNames since no implicit
                    //    move is needed
                    final boolean wasInOld = oldjarNames.remove(en);
                    // Update progress counters. If it was in old, we do
                    // not need an implicit move, so adjust total size.
                    if (wasInOld)
                        size--;
                }
                else
                {
                    // no write is done, decrement size
                    size--;
                }
            }

            // go through the renameMap and apply move for each entry
            if ((pairs != null) && (pairs.size() > 0))
            {
                for (final Map.Entry<String,String> p : pairs)
                {
                    // Apply move <oldName> <newName> command
                    final String    newName=(null == p) ? null : p.getKey(),
                                    oldName=(null == p) ? null : p.getValue();
                    // Get source JarEntry
                    final JarEntry oldEntry=
                        ((null == oldName) || (oldName.length() <= 0)) ? null : oldJar.getJarEntry(oldName);
                    if (oldEntry == null)
                    {
                        final String moveCmd=MOVE_COMMAND + oldName + " " + newName;
                        handleException("jardiff.error.badmove", moveCmd);
                    }

                    // Create dest JarEntry
                    final JarEntry newEntry=new JarEntry(newName);
                    newEntry.setTime(oldEntry.getTime());
                    newEntry.setSize(oldEntry.getSize());
                    newEntry.setCompressedSize(oldEntry.getCompressedSize());
                    newEntry.setCrc(oldEntry.getCrc());
                    newEntry.setMethod(oldEntry.getMethod());
                    newEntry.setExtra(oldEntry.getExtra());
                    newEntry.setComment(oldEntry.getComment());

                    updateDelegate(delegate, currentEntry, size);
                    currentEntry++;

                    writeEntry(jos, newEntry, oldJar.getInputStream(oldEntry));

                    //     Remove entry from oldjarNames since no implicit move is needed
                    final boolean wasInOld=oldjarNames.remove(oldName);
                    // Update progress counters. If it was in old, we do
                    // not need an implicit move, so adjust total size.
                    if (wasInOld)
                        size--;
                }
            }

            // implicit move
            for (final String name : oldjarNames)
            {
                final JarEntry entry=
                    ((null == name) || (name.length() <= 0)) ? null : oldJar.getJarEntry(name);
                if (null == entry)
                    continue;

                updateDelegate(delegate, currentEntry, size);
                currentEntry++;

                writeEntry(jos, entry, oldJar);
            }

            updateDelegate(delegate, currentEntry, size);

            jos.finish();
        }
        finally
        {
            jos.close();
        }
    }

    private static void updateDelegate (Patcher.PatchDelegate delegate, long currentSize, long size)
    {
        if (delegate != null)
            delegate.patching((size != 0L) ? (int)(currentSize/size) : 0);
    }

    private void determineNameMapping (JarFile jarDiff, Collection<String> ignoreSet, Map<String,String> renameMap) throws IOException
    {
        final InputStream is=jarDiff.getInputStream(jarDiff.getEntry(INDEX_NAME));
        if (is == null)
        {
            handleException("jardiff.error.noindex", null);
            return;
        }

        final LineNumberReader indexReader=new LineNumberReader(new InputStreamReader(is, "UTF-8"));
        try
        {
            String line = indexReader.readLine();
            if ((line == null) || !line.equals(VERSION_HEADER))
            {
                handleException("jardiff.error.badheader", line);
                return;
            }

            while ((line=indexReader.readLine()) != null)
            {
                if (line.startsWith(REMOVE_COMMAND))
                {
                    final List<String> sub=getSubpaths(line.substring(REMOVE_COMMAND.length()));
                    if ((null == sub) || (sub.size() != 1))
                    {
                        handleException("jardiff.error.badremove", line);
                        return;
                    }
                    ignoreSet.add(sub.get(0));
                }
                else if (line.startsWith(MOVE_COMMAND))
                {
                    final List<String> sub=getSubpaths(line.substring(MOVE_COMMAND.length()));
                    if ((null == sub) || (sub.size() != 2))
                    {
                        handleException("jardiff.error.badmove", line);
                        return;
                    }
                    // target of move should be the key
                    final String    prev=renameMap.put(sub.get(1), sub.get(0));
                    if (prev != null)
                    {
                        // invalid move - should not move to same target twice
                        handleException("jardiff.error.badmove", line);
                        return;
                    }
                }
                else if (line.length() > 0)
                {
                    handleException("jardiff.error.badcommand", line);
                    return;
                }
            }
        }
        finally
        {
            indexReader.close();
        }
    }

    private void handleException (String errorMsg, String line) throws IOException
    {
        try
        {
            final ResourceBundle    rb=getResources();
            if (!rb.containsKey(errorMsg))
                throw new StreamCorruptedException(errorMsg + ": " + line);
        }
        catch(MissingResourceException mre)
        {
            throw new IOException(mre.getClass().getName() + "[" + errorMsg + "](" + mre.getMessage() + "): " + line);
        }
    }

    private static List<String> getSubpaths (String path)
    {
        int                 index=0, length=(null == path) ? 0  : path.length();
        final List<String>    sub=new ArrayList<String>();

        while (index < length)
        {
            while (index < length && Character.isWhitespace(path.charAt(index)))
                index++;

            if (index < length)
            {
                int start = index;
                int last = start;
                String subString = null;

                while (index < length)
                {
                    char aChar = path.charAt(index);
                    if ((aChar == '\\') && ((index + 1) < length) && (path.charAt(index + 1) == ' '))
                    {
                        if (subString == null)
                            subString = path.substring(last, index);
                        else
                            subString += path.substring(last, index);
                        last = ++index;
                    }
                    else if (Character.isWhitespace(aChar))
                        break;

                    index++;
                }
                if (last != index)
                {
                    if (subString == null)
                        subString = path.substring(last, index);
                    else
                        subString += path.substring(last, index);
                }
                sub.add(subString);
            }
        }

        return sub;
    }

    private void writeEntry(JarOutputStream jos, JarEntry entry,
                            JarFile file) throws IOException {
        writeEntry(jos, entry, file.getInputStream(entry));
    }

    private void writeEntry(JarOutputStream jos, JarEntry entry,
                            InputStream data) throws IOException {
        //Create a new ZipEntry to clear the compressed size. 5079423
        jos.putNextEntry(new ZipEntry(entry.getName()));

        // Read the entry
        int size = data.read(newBytes);

        while (size != -1) {
            jos.write(newBytes, 0, size);
            size = data.read(newBytes);
        }
        data.close();
    }
}


