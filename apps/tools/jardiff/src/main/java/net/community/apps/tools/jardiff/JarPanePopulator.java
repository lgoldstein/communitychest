/*
 *
 */
package net.community.apps.tools.jardiff;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.community.chest.io.IOCopier;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 7, 2011 1:42:36 PM
 */
class JarPanePopulator extends AbstractJarPaneProcessor {
    JarPanePopulator (MainFrame frame, Collection<? extends JarComparisonPane> panes)
    {
        super(frame, panes);
    }
    /*
     * @see net.community.apps.tools.jardiff.AbstractJarPaneProcessor#processPane(net.community.apps.tools.jardiff.JarComparisonPane)
     */
    @Override
    protected void processPane (JarComparisonPane pane) throws Exception
    {
        final JarEntriesTableModel    model=(pane == null) ? null : pane.getModel();
        if (model != null)
            model.clear();

        final String            filePath=(pane == null) ? null : pane.getText();
        final ZipInputStream    zipStream=
            ((filePath == null) || (filePath.length() <= 0)) ? null
                    : new ZipInputStream(new BufferedInputStream(new FileInputStream(filePath), IOCopier.DEFAULT_COPY_SIZE));
        final String            ownerId=(pane == null) ? null : pane.getName();
        try
        {
            for (ZipEntry entry=(zipStream == null) ? null : zipStream.getNextEntry(); entry != null; entry=zipStream.getNextEntry())
            {
                if (isCancelled())
                    break;

                publish(new JarEntriesMatchRow(ownerId, entry, null));
            }
        }
        finally
        {
            if (zipStream != null)
                zipStream.close();
        }
    }
    /*
     * @see javax.swing.SwingWorker#process(java.util.List)
     */
    @Override
    protected void process (List<JarEntriesMatchRow> chunks)
    {
        final MainFrame    frame=getMainFrame();
        if (frame != null)
            frame.populate(chunks);
    }
    /*
     * @see javax.swing.SwingWorker#done()
     */
    @Override
    protected void done ()
    {
        final MainFrame    frame=getMainFrame();
        if (frame != null)
            frame.donePopulating(this);
    }
}
