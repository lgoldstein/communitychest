/*
 *
 */
package net.community.apps.eclipse.cp2pom;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

final class EntriesTableKeyListener extends KeyAdapter {
    private final RepositoryEntriesTable    _tbl;
    public RepositoryEntriesTable getRepositoryEntriesTable ()
    {
        return _tbl;
    }

    public EntriesTableKeyListener (final RepositoryEntriesTable tbl)
    {
        if (null == (_tbl=tbl))
            throw new IllegalArgumentException("No " + RepositoryEntriesTable.class.getSimpleName() + " instance provided");
    }

    protected void handleDeleteEvent ()
    {
        final RepositoryEntriesTable    tbl=getRepositoryEntriesTable();
        final RepositoryEntriesModel    m=(null == tbl) ? null : tbl.getTypedModel();
        final int[]                        selRows=(null == m) ? null : tbl.getSelectedRows();
        if ((null == selRows) || (selRows.length <= 0))
            return;

        final Collection<RepositoryEntry>    rl=new ArrayList<RepositoryEntry>(selRows.length);
        for (final int    rowIndex : selRows)
        {
            final int                modelIndex=tbl.convertRowIndexToModel(rowIndex);
            final RepositoryEntry    re=m.get(modelIndex);
            if (re != null)
                rl.add(re);
        }

        m.removeAll(rl);
    }

    protected void handleInsertEvent ()
    {
        // TODO
    }
    /*
     * @see java.awt.event.KeyAdapter#keyPressed(java.awt.event.KeyEvent)
     */
    @Override
    public void keyPressed (KeyEvent e)
    {
        final int    kc=(null == e) ? Integer.MIN_VALUE : e.getKeyCode();
        switch(kc)
        {
            case KeyEvent.VK_DELETE    :
                handleDeleteEvent();
                break;

            case KeyEvent.VK_INSERT    :
                handleInsertEvent();
                break;

            default                    :    // ignored
        }
    }
}
