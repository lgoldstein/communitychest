/*
 *
 */
package net.community.apps.eclipse.cparrange;

import java.io.File;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.community.chest.awt.attributes.Textable;
import net.community.chest.io.FileUtil;
import net.community.chest.io.IOCopier;
import net.community.chest.lang.StringUtil;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

import org.w3c.dom.Document;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 19, 2009 1:47:29 PM
 */
final class TransformerWorker extends SwingWorker<Void,File> {
    private static final LoggerWrapper    _logger=WrapperFactoryManager.getLogger(TransformerWorker.class);

    private final Textable _txField;
    public final Textable getTextField ()
    {
        return _txField;
    }

    private final File    _srcFile;
    public final File getSrcFile ()
    {
        return _srcFile;
    }

    private final String    _filename;
    public final String getFilename ()
    {
        return _filename;
    }

    private final MainFrame    _frame;
    public final MainFrame getMainFrame ()
    {
        return _frame;
    }

    private final boolean    _recursiveScan;
    public final boolean isRecursiveScan ()
    {
        return _recursiveScan;
    }

    public final String getBackupFileSuffix ()
    {
        return ".bak";
    }

    TransformerWorker (final MainFrame frame,
                       final File         srcFile,
                       final String     filename,    // if not recursive scan then contains output file path
                       final boolean    recursiveScan,
                       final Textable    txField) throws IllegalArgumentException
    {
        if ((null == (_frame=frame))
         || (null == (_srcFile=srcFile))
         || (null == (_filename=filename)) || (filename.length() <= 0))
            throw new IllegalArgumentException("Incomplete arguments");

        _txField = txField;    // null means "no update required"
        _recursiveScan = recursiveScan;
    }
    // returns removed backup file if previously existed
    private static final File createBackupFile (final File srcFile, final String bakSuffix) throws IOException
    {
        final File        srcDir=(null == srcFile) ? null : srcFile.getParentFile();
        final String    srcName=(null == srcFile) ? null : srcFile.getName();
        if ((null == srcDir)
         || (null == srcName) || (srcName.length() <= 0)
         || (null == bakSuffix) || (bakSuffix.length() <= 0))
            return null;

        final String    bakName=srcName + FileUtil.adjustExtension(bakSuffix, true);
        final File        bakFile=new File(srcDir, bakName);
        final boolean    bakExists=bakFile.exists();
        if (bakExists)
        {
            if (!bakFile.isFile())
                throw new StreamCorruptedException("Backup 'file' is a directory: " + bakFile);

            if (!bakFile.delete())
                throw new StreamCorruptedException("Failed to delete backup file: " + bakFile);
        }

        final long    copyLen=IOCopier.copyFile(srcFile, bakFile);
        if (copyLen < 0L)
            throw new StreamCorruptedException("Failed (err=" + copyLen + ") to backup file=" + srcFile + " to " + bakFile);

        return bakExists ? bakFile : null;
    }

    private static DocumentBuilderFactory    _defFactory    /* =null */;
    private static synchronized DocumentBuilderFactory getDocumentsFactory ()
    {
        if (null == _defFactory)
        {
            _defFactory = DocumentBuilderFactory.newInstance();
            _defFactory.setCoalescing(false);
            _defFactory.setValidating(false);
            _defFactory.setIgnoringComments(false);
            _defFactory.setIgnoringElementContentWhitespace(true);
            _defFactory.setNamespaceAware(true);
        }

        return _defFactory;
    }

    // returns duration in msec. - negative if failed/aborted - Long.MIN_VALUE if canceled
    private long transformFiles (final File srcFile, final File dstFile)
    {
        if (srcFile.isDirectory() || dstFile.isDirectory())
            throw new IllegalStateException("Source/Destination is a directory");

        if ((srcFile == dstFile) || srcFile.equals(dstFile))
        {
            final int    nRes=JOptionPane.showConfirmDialog(getMainFrame(), "Target file= " + dstFile.getAbsolutePath() + " already exists - overwrite ?", "Target overwrite confirm", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (nRes != JOptionPane.YES_OPTION)
            {
                if (JOptionPane.CANCEL_OPTION == nRes)
                    return Long.MIN_VALUE;
                return (-1L);
            }

            if (_logger.isDebugEnabled())
                _logger.debug("transformFiles(" + srcFile + ")");
        }
        else
        {
            if (_logger.isDebugEnabled())
                _logger.debug("transformFiles(" + srcFile + ") => " + dstFile);
        }

        final Document    doc;
        final long        xStart=System.currentTimeMillis();
        try
        {
            final DocumentBuilderFactory    docFactory=getDocumentsFactory();
            final DocumentBuilder            docBuilder=docFactory.newDocumentBuilder();
            if (null == (doc=docBuilder.parse(srcFile)))
                throw new IllegalStateException("No " + Document.class.getSimpleName() + " parsed");
        }
        catch(Exception e)
        {
            final long    xEnd=System.currentTimeMillis(), xDuration=xEnd - xStart;
            _logger.error("transformFiles(" + srcFile + ") => (" + dstFile + ") " + e.getClass().getName() + " while loading source file: " + e.getMessage());
            JOptionPane.showMessageDialog(getMainFrame(), "Failed to parse input file: " + e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
            return (0L - xDuration - 1L /* in case zero duration */);
        }

        try
        {
            Arranger.ARRANGER.transform(doc, dstFile);
        }
        catch(Exception e)
        {
            final long    xEnd=System.currentTimeMillis(), xDuration=xEnd - xStart;
            _logger.error("transformFiles(" + srcFile + ") => (" + dstFile + ") " + e.getClass().getName() + " while transforming: " + e.getMessage());
            JOptionPane.showMessageDialog(getMainFrame(), "Failed to transform: " + e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
            return (0L - xDuration - 1L /* in case zero duration */);
        }

        final long    xEnd=System.currentTimeMillis(), xDuration=xEnd - xStart;
        if (_logger.isDebugEnabled())
            _logger.debug("transformFiles(" + srcFile + ") => (" + dstFile + ") completed in " + xDuration + " msec.");
        JOptionPane.showMessageDialog(getMainFrame(), "Execution completed in " + xDuration + " msec.", "Done", JOptionPane.INFORMATION_MESSAGE);
        return xDuration;
    }

    protected Collection<File> transformFolder (final File srcDir, final String filename) throws IOException
    {
        if (!srcDir.isDirectory())
            throw new IllegalStateException("Source (" + srcDir + ") is not a directory");
        if ((null == filename) || (filename.length() <= 0))
            throw new IllegalArgumentException("No .classpath filename specified");

        final File[]    fa=srcDir.listFiles();
        if ((null == fa) || (fa.length <= 0))
            return null;

        Collection<File>    ret=null;
        for (final File f : fa)
        {
            final String    path=(null == f) ? null : f.getAbsolutePath();
            if ((null == path) || (path.length() <= 0))
                continue;

            if (isCancelled())
                return ret;

            publish(f);

            if (f.isDirectory())
            {
                final Collection<File>    sl=transformFolder(f, filename);
                if ((null == sl) || (sl.size() <= 0))
                    continue;

                if (null == ret)
                    ret = sl;
                else
                    ret.addAll(sl);
            }
            else if (f.isFile())
            {
                // TODO allow for REGEX as filename
                final String    n=f.getName();
                if (StringUtil.compareDataStrings(n, filename, true) != 0)
                    continue;

                final File    bakFile=createBackupFile(f, getBackupFileSuffix());
                if (bakFile != null)
                    _logger.info("transformFolder(" + srcDir + ")[" + n + "] removed backup file");

                if (Long.MIN_VALUE == transformFiles(f, f))
                    throw new IllegalStateException("Aborted by user request");

                if (null == ret)
                    ret = new LinkedList<File>();
                ret.add(f);
            }
            else
                _logger.warn("transformFolder(" + path + ") not a file or a directory");
        }

        return ret;
    }
    /*
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    protected Void doInBackground () throws Exception
    {
        if (!isCancelled())
        {
            final File        srcFile=getSrcFile();
            final String    fileName=getFilename();
            if (isRecursiveScan())
                transformFolder(srcFile, fileName);
            else
                transformFiles(srcFile, new File(fileName));
        }

        return null;
    }
    /*
     * @see javax.swing.SwingWorker#process(java.util.List)
     */
    @Override
    protected void process (List<File> chunks)
    {
        final int    numFiles=(null == chunks) ? 0 : chunks.size();
        if (numFiles <= 0)
            return;

        final Textable    t=getTextField();
        if (null == t)
            return;

        final File    f=chunks.get(numFiles-1);    // we show only the last one
        if (f != null)
            t.setText(f.getAbsolutePath());
    }
    /*
     * @see javax.swing.SwingWorker#done()
     */
    @Override
    protected void done ()
    {
        getMainFrame().setTransformerWorker(this);
        super.done();
    }
}
