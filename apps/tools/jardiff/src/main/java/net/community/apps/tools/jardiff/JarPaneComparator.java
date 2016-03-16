/*
 *
 */
package net.community.apps.tools.jardiff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;

import net.community.chest.io.zip.FullDataZipEntryComparator;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 8, 2011 10:53:56 AM
 *
 */
class JarPaneComparator extends AbstractJarPaneProcessor {
    private final Map<String,Map<String,JarEntriesMatchRow>>    _entriesMaps=
        new TreeMap<String,Map<String,JarEntriesMatchRow>>();
    JarPaneComparator (MainFrame frame, Collection<? extends JarComparisonPane> panes)
    {
        super(frame, panes);
    }
    /*
     * @see net.community.apps.tools.jardiff.AbstractJarPaneProcessor#processPane(net.community.apps.tools.jardiff.JarComparisonPane)
     */
    @Override
    protected void processPane (JarComparisonPane pane) throws Exception
    {
        final String                    name=(pane == null) ? null : pane.getName();
        Map<String,JarEntriesMatchRow>    rowsMap=
            ((name == null) || (name.length() <= 0)) ? null : _entriesMaps.get(name);
        if (rowsMap == null)
        {
            rowsMap = buildRowsMap(pane.getModel());
            _entriesMaps.put(name, rowsMap);
        }

        final MainFrame    frame=getMainFrame();
        processRowMaps(new ArrayList<Map<String,JarEntriesMatchRow>>(_entriesMaps.values()), (frame != null) && frame.isCheckContents());
    }

    private Map<String,JarEntriesMatchRow> buildRowsMap (final Collection<? extends JarEntriesMatchRow> rows)
    {
        if ((rows == null) || (rows.size() <= 0))
            return Collections.emptyMap();

        final Map<String,JarEntriesMatchRow>    rowsMap=new TreeMap<String,JarEntriesMatchRow>();
        for (final JarEntriesMatchRow r : rows)
        {
            if (isCancelled())
                break;

            final ZipEntry    ze=(r == null) ? null : r.getCurrentJarEntry();
            final String    zn=(ze == null) ? null : ze.getName();
            if ((zn == null) || (zn.length() <= 0))
                continue;

            final JarEntriesMatchRow    prev=rowsMap.put(zn, r);
            if (prev != null)
                throw new IllegalStateException("Multiple rows for entry=" + zn);
        }

        return rowsMap;
    }

    private void processRowMaps (final List<? extends Map<String,JarEntriesMatchRow>> mapsList, final boolean checkContents)
    {
        final int    numMaps=(mapsList == null) ? 0 : mapsList.size();
        if (numMaps != 2)
            return;

        final Map<String,JarEntriesMatchRow>    leftMap=mapsList.get(0), rightMap=mapsList.get(1);
        final Collection<? extends Map.Entry<String,JarEntriesMatchRow>>    leftEntries=
            ((leftMap == null) || leftMap.isEmpty() || (rightMap == null) || rightMap.isEmpty()) ? null : leftMap.entrySet();
        if ((leftEntries == null) || (leftEntries.size() <= 0))
            return;

        for (final Map.Entry<String,JarEntriesMatchRow> rowEntry : leftEntries)
        {
            if (isCancelled())
                break;

            final String                rowName=(rowEntry == null) ? null : rowEntry.getKey();
            final JarEntriesMatchRow    rowValue=(rowEntry == null) ? null : rowEntry.getValue(),
                                        mapValue=
                ((rowName == null) || (rowName.length() <= 0) || (rowValue == null)) ? null : rightMap.get(rowName);
            if ((rowValue == null) || (mapValue == null))
                continue;    // no match

            // create xref
            final ZipEntry    leftEntry=rowValue.getCurrentJarEntry(), rightEntry=mapValue.getCurrentJarEntry();
            rowValue.setMatchingJarEntry(rightEntry);
            mapValue.setMatchingJarEntry(leftEntry);

            if ((FullDataZipEntryComparator.ASCENDING.compare(leftEntry, rightEntry) != 0)
             || (checkContents && (findDifference(leftEntry, rightEntry) != null)))
            {
                rowValue.setDataMismatch(true);
                mapValue.setDataMismatch(true);
            }

            publish(rowValue, mapValue);
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
            frame.update(chunks);
    }
    /*
     * @see javax.swing.SwingWorker#done()
     */
    @Override
    protected void done ()
    {
        final MainFrame    frame=getMainFrame();
        if (frame != null)
            frame.doneComparing(this);
    }

    private static final Object findDifference (final ZipEntry leftEntry, final ZipEntry rightEntry)
    {
        // TODO implement it
        return null;
    }
}
