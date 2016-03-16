/*
 *
 */
package net.community.apps.tools.svn.svnsync;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import net.community.apps.tools.svn.SVNSyncFilesFilter;
import net.community.chest.Triplet;
import net.community.chest.io.FileUtil;
import net.community.chest.io.file.FileDiffOutputStream;
import net.community.chest.io.file.FileDiffOutputStream.FileDiffException;
import net.community.chest.io.file.FileIOUtils;
import net.community.chest.lang.StringUtil;
import net.community.chest.svnkit.SVNFileLocation;
import net.community.chest.svnkit.SVNLocation;
import net.community.chest.svnkit.SVNLocation.SVNLocationType;
import net.community.chest.svnkit.core.CoreUtils;
import net.community.chest.svnkit.core.wc.SVNEventActionEnum;
import net.community.chest.svnkit.core.wc.SVNPropsMap;
import net.community.chest.util.datetime.DateUtil;
import net.community.chest.util.datetime.Duration;
import net.community.chest.util.datetime.TimeUnits;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.wc.ISVNPropertyHandler;
import org.tmatesoft.svn.core.wc.SVNWCClient;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 19, 2010 1:13:40 PM
 */
class SVNSynchronizer extends SwingWorker<Void,SVNSyncEvent> {
    private final SVNSyncMainFrame    _f;
    public final SVNSyncMainFrame getMainFrame ()
    {
        return _f;
    }

    private static EventQueue    _eventQueue;
    // no need to synchronize since only one thread accesses it
    private static final EventQueue getEventQueue ()
    {
        if (_eventQueue == null)
        {
            final Toolkit    tk=Toolkit.getDefaultToolkit();
            _eventQueue = tk.getSystemEventQueue();
        }

        return _eventQueue;
    }

    private SVNSyncHeartbeatEvent    _event;
    private void postHeartbeatEvent ()
    {
        // if showing skipped targets no need for the heartbeat since the skip events update the heartbeat anyway
        if (isShowSkippedTargetsEnabled())
            return;

        if (_event == null)
            _event = new SVNSyncHeartbeatEvent(this, getMainFrame());

        final EventQueue    q=getEventQueue();
        q.postEvent(_event);
    }

    private final SVNWCClient _wcc;
    public final SVNWCClient getWCClient ()
    {
        return _wcc;
    }

    private int    _numProcessedFiles, _numProcessedFolders,
                _numAddedNodes, _numDeletedNodes, _numUpdatedNodes;
    public final int getNumProcessedFiles ()
    {
        return _numProcessedFiles;
    }

    public final int getNumProcessedFolders ()
    {
        return _numProcessedFolders;
    }

    public final int getNumAddedNodes ()
    {
        return _numAddedNodes;
    }

    public final int getNumDeletedNodes ()
    {
        return _numDeletedNodes;
    }

    public final int getNumUpdatedNodes ()
    {
        return _numUpdatedNodes;
    }

    SVNSynchronizer (SVNSyncMainFrame f, SVNWCClient wcc)
    {
        if (null == (_f=f))
            throw new IllegalStateException("No main frame instance provided");
        if (null == (_wcc=wcc))
            throw new IllegalStateException("No SVN WC client instance provided");
    }

    private static boolean showSVNException (final SVNException e)
    {
        return !CoreUtils.isDefaultIgnoredError(e);
    }

    private static Map<String,String> getSVNProperties (final SVNWCClient wcc, final SVNLocation f)
        throws SVNException, IOException
    {
        try
        {
            final SVNPropsMap    propsMap=f.getProperties(wcc);
            if ((propsMap == null) || (propsMap.size() <= 0))
                return null;
            return propsMap.toStringsMap();
        }
        catch(SVNException e)
        {
            if (showSVNException(e))
                throw e;

            return null;
        }
    }

    private boolean isShowSkippedTargetsEnabled ()
    {
        final SVNSyncMainFrame    f=getMainFrame();
        return (f != null) && f.isShowSkippedTargetsEnabled();
    }

    private void publishEvent (SVNSyncEvent event)
    {
        if (event == null)
            return;

        if (SVNEventActionEnum.SKIP.equals(event.getSyncAction()) && (!isShowSkippedTargetsEnabled()))
               return;

        publish(event);
    }

    private static void doSetProperty (final SVNWCClient wcc, final SVNLocation loc,
            String propName, SVNPropertyValue propValue, boolean skipChecks,
            SVNDepth depth, ISVNPropertyHandler handler, Collection<?> changeLists)
        throws IOException, SVNException
    {
        if (wcc == null)
            throw new EOFException("No WC client instance provided");

        final SVNLocationType    dType=(loc == null) ? null : loc.getLocationType();
        if (dType == null)
            throw new StreamCorruptedException("doSetProperty(" + loc + ")"
                                               + propName + "=" + ((propValue == null) ? null : SVNPropertyValue.getPropertyAsString(propValue))
                                               + " - no type");

        switch(dType)
        {
            case FILE    :
                wcc.doSetProperty(loc.getFile(), propName, propValue, skipChecks, depth, handler, changeLists);
                break;

            case URL    :    // TODO implement it
            default        :
                throw new StreamCorruptedException("doSetProperty(" + loc + ")"
                           + propName + "=" + ((propValue == null) ? null : SVNPropertyValue.getPropertyAsString(propValue))
                           + " - unknown type: " + dType);
        }
    }
    // returns TRUE if something modified
    private boolean SVNPropertiesSync (final SVNWCClient wcc, final SVNLocation srcFile, final SVNLocation dstFile)
        throws IOException, SVNException
    {
        Map<String,String>    srcProps=getSVNProperties(wcc, srcFile),
                            dstProps=getSVNProperties(wcc, dstFile);
        boolean                propsChanged=false;
        Map<String,String>    addProps=null, delProps=null, updProps=null;
        // checks for properties that need to be added to the destination
        if ((srcProps != null) && (srcProps.size() > 0))
        {
            for (final Map.Entry<String,String> sp : srcProps.entrySet())
            {
                final String    sKey=sp.getKey(), sValue=sp.getValue(),
                                dValue=(dstProps == null) ? null : dstProps.get(sKey);
                if ((dValue != null) && sValue.equals(dValue))
                    continue;

                final SVNPropertyValue    value=SVNPropertyValue.create(sValue);
                doSetProperty(wcc, dstFile, sKey, value, false, SVNDepth.EMPTY, null, Collections.emptyList());

                if (dstProps == null)
                    dstProps = new TreeMap<String,String>();
                dstProps.put(sKey, sValue);

                if (dValue == null)
                {
                    if (addProps == null)
                        addProps = new TreeMap<String,String>();
                    addProps.put(sKey, sValue);
                }
                else
                {
                    if (updProps == null)
                        updProps = new TreeMap<String,String>();
                    updProps.put(sKey, sValue);
                }

                propsChanged = true;
            }
        }

        // check of properties that no longer exists in the source
        if ((dstProps != null) && (dstProps.size() > 0))
        {
            for (final Map.Entry<String,String> dp : dstProps.entrySet())
            {
                final String    dKey=dp.getKey();
                if ((srcProps != null) && srcProps.containsKey(dKey))
                    continue;

                doSetProperty(wcc, dstFile, dKey, null /* deletes it */, false, SVNDepth.EMPTY, null, Collections.emptyList());

                if (delProps == null)
                    delProps = new TreeMap<String,String>();
                delProps.put(dKey, dp.getValue());

                propsChanged = true;
            }
        }

        if (propsChanged)
            publishEvent(new SVNSyncEvent(srcFile, dstFile, SVNEventActionEnum.UPDATE_REPLACE, addProps, delProps, updProps));

        return propsChanged;
    }

    private boolean isSyncConfirmationRequired (String srcPath)
    {
        final SVNSyncMainFrame    f=getMainFrame();
        return (f != null) && f.isSyncConfirmationRequired(srcPath);
    }

    private boolean isSyncConfirmed (String srcPath)
    {
        if ((srcPath == null) || (srcPath.length() <= 0))
            return true;

        final SVNSyncMainFrame    f=getMainFrame();
        if (f == null)
            return true;

        final int    nRes=JOptionPane.showConfirmDialog(f, "Synchronize " + srcPath + " ?", "Confirm synchronization", JOptionPane.YES_NO_OPTION);
        if (nRes != JOptionPane.YES_OPTION)
            return false;

        return true;
    }

    private File getTempDir ()
    {
        return null;    // TODO get it from SVNSyncFrame + allow command line argument
    }

    private static final File[]    _tempDiffFiles=new File[3];
    // no need to synchronized since only the worker thread access it
    private File getTempDiffFile (final int fIndex) throws IOException
    {
        if ((fIndex < 0) || (fIndex >= _tempDiffFiles.length))
            throw new FileNotFoundException("getTempDiffFile(" + fIndex + ") bad/illegal index");

        File    f=_tempDiffFiles[fIndex];
        if (f != null)
            return f;

        f = File.createTempFile("svnsync-" + fIndex + "-", "-.diff", getTempDir());
        _tempDiffFiles[fIndex] = f;
        return f;
    }

    private Map<SVNLocation,File> createComparedFiles (final SVNWCClient wcc, final SVNLocation ... locs)
        throws IOException, SVNException
    {
        if (wcc == null)
            throw new EOFException("No WC client instance provided");
        if ((locs == null) || (locs.length <= 0))
            return null;

        Map<SVNLocation,File>    diffFiles=null;
        for (int    fIndex=0; fIndex < locs.length; fIndex++)
        {
            final SVNLocation l=locs[fIndex];
            if (l == null)
                continue;

            File    f=l.getFile();
            if (f != null)
                continue;

            f = getTempDiffFile(fIndex);
            f.deleteOnExit();

            if (diffFiles == null)
                diffFiles = new TreeMap<SVNLocation,File>();

            final File    prev=diffFiles.put(l, f);
            if (prev != null)
                throw new StreamCorruptedException("Multiple mappings found for location=" + l);

            final OutputStream    out=new FileOutputStream(f);
            try
            {
                l.copyTo(wcc, out);
            }
            finally
            {
                FileUtil.closeAll(out);
            }
        }

        return diffFiles;
    }

    private Triplet<Long,Byte,Byte> findHybridDifference (
            final SVNWCClient wcc, final SVNLocation srcFile, final SVNLocation dstFile)
        throws IOException, SVNException
    {
        if (wcc == null)
            throw new EOFException("No WC client instance provided");

        final SVNLocationType    srcType=(srcFile == null) ? null : srcFile.getLocationType(),
                                dstType=(dstFile == null) ? null : dstFile.getLocationType();
        if ((srcType == null) || (dstType == null))
            throw new StreamCorruptedException("Missing hybrid diffs source (" + srcFile + ")/destination (" + dstFile + ") type");
        if (srcType.equals(dstType))
            throw new StreamCorruptedException("Compared files not hybrid: source (" + srcFile + ")/destination (" + dstFile + ") type=" + srcType);

        final SVNLocation    l=SVNLocationType.FILE.equals(srcType) ? dstFile : srcFile;
         final File            f=SVNLocationType.FILE.equals(srcType) ? srcFile.getFile() : dstFile.getFile();
         final InputStream    in=new FileInputStream(f);
         try
         {
             final FileDiffOutputStream    out=new FileDiffOutputStream(in, true);
             l.copyTo(wcc, out);
             FileUtil.closeAll(out);

             final FileDiffException    diffExc=out.getFileDiffException();
             if (diffExc != null)
                 return diffExc.getDiffInfo();
         }
         finally
         {
             FileUtil.closeAll(in);
         }

         return null;
    }

    private Map<SVNLocation,File> removeTempFiles (final Map<SVNLocation,File> locsMap)
        throws IOException
    {
        if ((locsMap == null) || locsMap.isEmpty())
            return locsMap;

        IOException e=null;
        for (final File f : locsMap.values())
        {
            if ((f != null) && (!f.delete()))
                e = new IOException("Failed to deleted temp file=" + f);
        }

        if (e != null)
            throw e;

        return locsMap;
    }

    private Triplet<Long,Byte,Byte> findDifference (
            final SVNWCClient wcc, final SVNLocation srcFile, final SVNLocation dstFile)
        throws IOException, SVNException
    {
        if (wcc == null)
            throw new EOFException("No WC client instance provided");

        final SVNLocationType    srcType=(srcFile == null) ? null : srcFile.getLocationType(),
                                dstType=(dstFile == null) ? null : dstFile.getLocationType();
        if ((srcType == null) || (dstType == null))
            throw new StreamCorruptedException("Missing diffs source (" + srcFile + ")/destination (" + dstFile + ") type");

        final File[]    files={ srcFile.getFile(), dstFile.getFile() };
        int                numFiles=0;
        for (final File f : files)
        {
            if (f != null)
                numFiles++;
        }

           switch(numFiles)
           {
               case 0    :
                   {
                       final Map<SVNLocation,File>    locsMap=createComparedFiles(wcc, srcFile, dstFile);
                       try
                       {
                           return FileIOUtils.findDifference(locsMap.get(srcFile), locsMap.get(dstFile));
                       }
                       finally
                       {
                           removeTempFiles(locsMap);
                    }
                   }

               case 1    :
                   return findHybridDifference(wcc, srcFile, dstFile);

               case 2    :
                   return FileIOUtils.findDifference(files[0], files[1]);

               default    :
                   throw new StreamCorruptedException("Unsupported number of files to compare: " + numFiles);
           }
    }

    private static void doAdd (final SVNWCClient wcc, final SVNLocation d,
                               boolean force, boolean mkdir, boolean climbUnversionedParents,
                               SVNDepth depth, boolean depthIsSticky, boolean includeIgnored, boolean makeParents)
        throws IOException, SVNException
    {
        if (wcc == null)
            throw new EOFException("No WC client instance provided");

        final SVNLocationType    dType=(d == null) ? null : d.getLocationType();
        if (dType == null)
            throw new StreamCorruptedException("doAdd(" + d + ") no type");

        switch(dType)
        {
            case FILE    :
                wcc.doAdd(d.getFile(), force, mkdir, climbUnversionedParents, depth, depthIsSticky, includeIgnored, makeParents);
                break;

            case URL    :    // TODO implement it
            default        :
                throw new StreamCorruptedException("doAdd(" + d + ") unknown type: " + dType);
        }
    }

    private static void doDelete (final SVNWCClient wcc, final SVNLocation d,
                                  boolean force, boolean deleteFiles, boolean dryRun)
        throws IOException, SVNException
    {
        if (wcc == null)
            throw new EOFException("No WC client instance provided");

        final SVNLocationType    dType=(d == null) ? null : d.getLocationType();
        if (dType == null)
            throw new StreamCorruptedException("doDelete(" + d + ") no type");

        switch(dType)
        {
            case FILE    :
                wcc.doDelete(d.getFile(), force, deleteFiles, dryRun);
                break;

            case URL    :    // TODO implement it
            default        :
                throw new StreamCorruptedException("doAdd(" + d + ") unknown type: " + dType);
        }
    }

    private boolean isPropertiesSyncAllowed ()
    {
        final SVNSyncMainFrame    f=getMainFrame();
        return (f != null) && f.isPropertiesSyncAllowed();
    }

    private boolean isUseMergeForUpdate ()
    {
        final SVNSyncMainFrame    f=getMainFrame();
        return (f != null) && f.isUseMergeForUpdate();
    }

    private boolean doMerge (final SVNWCClient wcc, final SVNLocation srcLoc, final SVNLocation dstLoc)
        throws IOException, SVNException
    {
        Map<SVNLocation,File>    locsMap=createComparedFiles(wcc, srcLoc, dstLoc);
        try
        {
            File    srcFile=(locsMap == null) ? null : locsMap.get(srcLoc),
                    dstFile=(locsMap == null) ? null : locsMap.get(dstLoc);
            if (srcFile == null)
                srcFile = srcLoc.getFile();
            if (dstFile == null)
                dstFile = dstLoc.getFile();

            final File        resFile=getTempDiffFile(2);
            if (locsMap == null)
                locsMap = new TreeMap<SVNLocation,File>();
            locsMap.put(new SVNFileLocation(resFile), resFile);

            final SVNSyncMainFrame    f=getMainFrame();
            final Process            p=f.executeMergeCommand(srcFile, dstFile, resFile);
            if (p == null)
                return false;

            final int    nErr;
            try
            {
                if ((nErr=p.waitFor()) != 0)
                {
                    final int    nRes=JOptionPane.showConfirmDialog(f, "Failed to merge: " + nErr + " - continue ?", "Merge command failure", JOptionPane.YES_NO_OPTION);
                    if (nRes != JOptionPane.YES_OPTION)
                        return false;
                }
            }
            catch(InterruptedException e)
            {
                Thread.currentThread().interrupt();    // propagate
                throw new StreamCorruptedException("Interruped during merge: " + e.getMessage());
            }

            // TODO copy the results back to the destination file
            return false;
        }
        finally
        {
            removeTempFiles(locsMap);
        }
    }

    private SVNLocation    _lastSrcFile, _lastDstFile;
    // returns true if OK to continue
    private boolean SVNFoldersSync (final SVNWCClient wcc, final SVNLocation srcFile, final SVNLocation dstFile)
        throws IOException, SVNException
    {
        _lastSrcFile = srcFile;
        _lastDstFile = dstFile;

        final String    srcPath=srcFile.toString(), dstPath=dstFile.toString();
        if (0 == StringUtil.compareDataStrings(srcPath, dstPath, false))
        {
            publishEvent(new SVNSyncEvent(srcFile, dstFile, SVNEventActionEnum.FAILED_EXTERNAL, new UnsupportedOperationException("Cannot sync file with itself")));
            return false;
        }

        final boolean    srcIsFile=srcFile.isFile(wcc), dstIsFile=dstFile.isFile(wcc);
        if (srcIsFile != dstIsFile)
        {
            publishEvent(new SVNSyncEvent(srcFile, dstFile, SVNEventActionEnum.FAILED_EXTERNAL, new UnsupportedOperationException("Cannot sync folder with file")));
            return false;
        }

        // do not synchronize source files that are not under SVN
        if (!CoreUtils.isSVNFile(wcc, srcFile))
        {
            if (isShowSkippedTargetsEnabled())
                publishEvent(new SVNSyncEvent(srcFile, dstFile, SVNEventActionEnum.SKIP));
            return !isCancelled();
        }

        if (isSyncConfirmationRequired(srcPath) && (!isSyncConfirmed(srcPath)))
        {
            if (isShowSkippedTargetsEnabled())
                publishEvent(new SVNSyncEvent(srcFile, dstFile, SVNEventActionEnum.SKIP));
            return !isCancelled();
        }

        if (srcIsFile)
        {
            _numProcessedFiles++;

            final Triplet<Long,Byte,Byte>    diff=findDifference(wcc, srcFile, dstFile);
            if (null == diff)
            {
                if (isPropertiesSyncAllowed() && (!SVNPropertiesSync(wcc, srcFile, dstFile)) && isShowSkippedTargetsEnabled())
                    publishEvent(new SVNSyncEvent(srcFile, dstFile, SVNEventActionEnum.SKIP));
                return !isCancelled();
            }

            final SVNEventActionEnum    action;
            if (isUseMergeForUpdate())
            {
                if (!doMerge(wcc, srcFile, dstFile))
                    return false;
                action = SVNEventActionEnum.MERGE_COMPLETE;
            }
            else
            {
                srcFile.copyTo(wcc, dstFile);
                action = SVNEventActionEnum.UPDATE_EXISTS;
            }

            if (isPropertiesSyncAllowed())
                SVNPropertiesSync(wcc, srcFile, dstFile);

            _numUpdatedNodes++;

            publishEvent(new SVNSyncEvent(srcFile, dstFile, action));
            return !isCancelled();
        }

        boolean    sendHeartbeat=true;
        _numProcessedFolders++;
        if (isPropertiesSyncAllowed() && SVNPropertiesSync(wcc, srcFile, dstFile))
        {
            _numUpdatedNodes++;
            publishEvent(new SVNSyncEvent(srcFile, dstFile, SVNEventActionEnum.UPDATE_EXISTS));
        }

        final Collection<? extends SVNLocation>        srcFiles=srcFile.listFiles(wcc, SVNSyncFilesFilter.DEFAULT);
        final Set<String>                            srcProcd=new TreeSet<String>(), dstProcd=new TreeSet<String>();
        final int                                    numSources=(null == srcFiles) ? 0 : srcFiles.size();
        if (numSources > 0)
        {
            for (final SVNLocation    f : srcFiles)
            {
                if (isCancelled())
                    return false;

                final String    n=f.getName();
                srcProcd.add(n);

                final SVNLocation    d=dstFile.appendSubPath(n);
                _lastSrcFile = f;
                _lastDstFile = d;

                // do not synchronize source files that are not under SVN
                if (!CoreUtils.isSVNFile(wcc, f))
                {
                    if (isShowSkippedTargetsEnabled())
                        publishEvent(new SVNSyncEvent(f, d, SVNEventActionEnum.SKIP));
                    continue;
                }

                if (d.exists(wcc))
                {
                    if (!SVNFoldersSync(wcc, f, d))
                        return false;
                }
                else if (f.isFile(wcc))
                {
                    f.copyTo(wcc, d);
                    doAdd(wcc, d, false, false, true, SVNDepth.EMPTY, false, false, true);
                    publishEvent(new SVNSyncEvent(f, d, SVNEventActionEnum.ADD));
                    _numAddedNodes++;
                    sendHeartbeat = false;    // no need since this event triggers a heartbeat update anyway
                }
                else if (f.isDirectory(wcc))
                {
                    doAdd(wcc, d, true, true, true, SVNDepth.INFINITY, false, false, true);
                    publishEvent(new SVNSyncEvent(f, d, SVNEventActionEnum.ADD));
                    _numAddedNodes++;

                    if (!SVNFoldersSync(wcc, f, d))
                        return false;

                    sendHeartbeat = false;    // no need since this event triggers a heartbeat update anyway
                }
                else
                    throw new StreamCorruptedException("Unknwn location type: " + f);

                dstProcd.add(n);
            }
        }

        final Collection<? extends SVNLocation>    dstFiles=dstFile.listFiles(wcc, SVNSyncFilesFilter.DEFAULT);
        final int                                numDests=(null == dstFiles) ? 0 : dstFiles.size();
        if (numDests <= 0)
        {
            if (isCancelled())
                return false;

            if (sendHeartbeat)
                postHeartbeatEvent();
            return true;
        }

        for (final SVNLocation d : dstFiles)
        {
            if (isCancelled())
                return false;

            final String    n=d.getName();
            if (dstProcd.contains(n))
                continue;

            // if target file is not under SVN no need to delete it
            final SVNLocation    sFile=srcFile.appendSubPath(n);
            _lastSrcFile = sFile;
            _lastDstFile = d;

            if (!CoreUtils.isSVNFile(wcc, d))
            {
                if (isShowSkippedTargetsEnabled())
                    publishEvent(new SVNSyncEvent(sFile, d, SVNEventActionEnum.SKIP));
                continue;
            }

            doDelete(wcc, d, true, true, false);
            publishEvent(new SVNSyncEvent(sFile, d, SVNEventActionEnum.DELETE));
            _numDeletedNodes++;
            sendHeartbeat = false;    // no need since this event triggers a heartbeat update anyway
        }

        if (isCancelled())
            return false;

        if (sendHeartbeat)
            postHeartbeatEvent();
        return true;
    }

    private long    _startTime, _endTime;
    public long getElapsedTime ()
    {
        if (_startTime <= 0L)
            return 0L;

        if (_endTime < _startTime)
        {
            final long    now=System.currentTimeMillis();
            return (now - _startTime);
        }

        return _endTime - _startTime;
    }

    public String getFormattedElapsedTime ()
    {
        final Map<TimeUnits,? extends Number> sm=Duration.fromTimespec(getElapsedTime());
        if ((sm == null) || (sm.size() <= 0))
            return null;

        final int[]    timeVals=new int[3];
        for (final Map.Entry<TimeUnits,? extends Number> de : sm.entrySet())
        {
            TimeUnits    u=de.getKey();
            if ((u == null) || (TimeUnits.SECOND.compareTo(u) > 0))
                continue;    // skip any time units below second(s)

            // convert anything above hour(s) to hours
            Number    n=de.getValue();
            if (TimeUnits.HOUR.compareTo(u) < 0)
            {
                final double    v=TimeUnits.HOUR.convertToThisUnit(n.doubleValue(), u);
                n = Integer.valueOf((int) v);
                u = TimeUnits.HOUR;
            }

            final int    vIndex=u.ordinal() - TimeUnits.SECOND.ordinal(),
                        vValue=n.intValue();
            timeVals[vIndex] += vValue;
        }

        final StringBuilder    sb=new StringBuilder(12);
        for (int    vIndex=timeVals.length - 1; vIndex >= 0; vIndex--)
        {
            if (sb.length() > 0)
                sb.append(DateUtil.DEFAULT_TMSEP);

            final int    n=timeVals[vIndex];
            try
            {
                if ((n < 100) || (vIndex < (timeVals.length - 1)))    // special treatment for hours > 100
                    StringUtil.appendPaddedNum(sb, timeVals[vIndex], 2);
                else
                    sb.append(n);
            }
            catch(IOException e)
            {
                // unexpected
                sb.append(e.getClass().getName() + ": " + e.getMessage());
            }
        }

        return sb.toString();
    }
    /*
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    protected Void doInBackground () throws Exception
    {
        final SVNSyncMainFrame    f=getMainFrame();
        final String            srcFolder=(null == f) ? null : f.getSynchronizationSource(),
                                dstFolder=(null == f) ? null : f.getWCLocation();
        if ((null == srcFolder) || (srcFolder.length() <= 0)
         || (null == dstFolder) || (dstFolder.length() <= 0))
            return null;

        try
        {
            _lastSrcFile = SVNLocation.fromString(srcFolder);
            _lastDstFile = SVNLocation.fromString(dstFolder);

            _startTime = System.currentTimeMillis();
            SVNFoldersSync(getWCClient(), _lastSrcFile, _lastDstFile);
        }
        catch(Throwable t)
        {
            publishEvent(new SVNSyncEvent(_lastSrcFile, _lastDstFile, SVNEventActionEnum.FAILED_EXTERNAL, t));
            if (t instanceof Exception)
                throw (Exception) t;
        }
        finally
        {
            _endTime = System.currentTimeMillis();
        }

        return null;
    }
    /*
     * @see javax.swing.SwingWorker#process(java.util.List)
     */
    @Override
    protected void process (List<SVNSyncEvent> chunks)
    {
        final int    numEvents=(null == chunks) ? 0 : chunks.size();
        if (numEvents <= 0)
            return;

        final SVNSyncMainFrame    f=getMainFrame();
        if (null == f)
            return;

        for (final SVNSyncEvent event : chunks)
        {
            if (!f.handleSyncEvent(event))
            {
                cancel(false);
                break;
            }
        }
    }
    /*
     * @see javax.swing.SwingWorker#done()
     */
    @Override
    protected void done ()
    {
        final SVNSyncMainFrame    f=getMainFrame();
        if (null == f)
            return;

        f.signalSynchronizationDone(this);
    }
}
