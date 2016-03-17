/*
 *
 */
package net.community.apps.tools.adm.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.SwingWorker;

import net.community.chest.db.sql.AbstractResultSetHandler;
import net.community.chest.db.sql.ResultSetRowColumnData;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 15, 2009 8:46:39 AM
 */
public class SectionsPopulator extends SwingWorker<Void,String> {
    private static final LoggerWrapper    _logger=WrapperFactoryManager.getLogger(SectionsPopulator.class);

    private final MainFrame    _f;
    public final MainFrame getMainFrame ()
    {
        return _f;
    }

    private final Connection    _c;
    public final Connection getConnection ()
    {
        return _c;
    }

    private final Set<String>    _names=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    public SectionsPopulator (final MainFrame f, final Connection c)
    {
        if ((null == (_f=f)) || (null == (_c=c)))
            throw new IllegalArgumentException("Incomplete configuration");
    }

    private class SectionResultSetHandler extends AbstractResultSetHandler {
        protected SectionResultSetHandler ()
        {
            super();
        }
        /*
         * @see net.community.chest.db.sql.AbstractResultSetHandler#processResultRow(int, java.util.List)
         */
        @SuppressWarnings("synthetic-access")
        @Override
        public boolean processResultRow (int rowIndex, List<? extends ResultSetRowColumnData> cl)
            throws SQLException
        {
            final int    numCols=(null == cl) ? 0 : cl.size();
            if (numCols <= 0)
                return false;

            if (numCols != 1)
                throw new SQLException("Unexpected number of columns (" + numCols + ") in row #" + rowIndex);

            final ResultSetRowColumnData    rcd=cl.get(0);
            final Object                    rcv=
                (null == rcd) ? null : rcd.getColumnValue();
            final String                    sName=
                (null == rcv) ? null : rcv.toString();
            if ((null == sName) || (sName.length() <= 0))
                throw new SQLException("Null/empty data in row #" + rowIndex);
            publish(sName);
            return true;
        }
    }
    /*
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    protected Void doInBackground () throws Exception
    {
        Statement    s=null;
        try
        {
            final Connection    conn=getConnection();
            if (null == (s=conn.createStatement()))
                throw new IllegalStateException("No statement created");

            final long        qStart=System.currentTimeMillis();
            final ResultSet    rs=s.executeQuery("SELECT DISTINCT(cfg.section) AS sectionName FROM CONFIG cfg ORDER BY sectionName ASC");
            final long        qEnd=System.currentTimeMillis(), qDuration=qEnd - qStart;
            final int        fSize=rs.getFetchSize();
            if (_logger.isDebugEnabled())
                _logger.debug("Fetched " + fSize + " items in " + qDuration + " msec.");

            final SectionResultSetHandler    rsh=new SectionResultSetHandler();
            rsh.processResultSetRows(rs);
        }
        catch(Exception e)
        {
            _logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
            BaseOptionPane.showMessageDialog(getMainFrame(), e);
        }
        finally
        {
            if (s != null)
            {
                try
                {
                    s.close();
                }
                catch(Exception ce)
                {
                    _logger.error(ce.getClass().getName() + " on close statement: " + ce.getMessage(), ce);
                }
            }
        }

        return null;
    }
    /*
     * @see javax.swing.SwingWorker#done()
     */
    @Override
    protected void done ()
    {
        final MainFrame    f=getMainFrame();
        if (null == f)    // should not happen
            return;

        f.signalSectionsPopulationDone(this);
    }
    /*
     * @see javax.swing.SwingWorker#process(java.util.List)
     */
    @Override
    protected void process (List<String> chunks)
    {
        final int    numChunks=(null == chunks) ? 0 : chunks.size();
        if (numChunks <= 0)
            return;

        final MainFrame    f=getMainFrame();
        if (null == f)    // should not happen
            return;

        for (final String n : chunks)
        {
            if ((null == n) || (n.length() <= 0) || _names.contains(n))
                continue;
            f.addConfigSection(n);
        }
    }
}
