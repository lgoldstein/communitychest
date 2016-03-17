/*
 *
 */
package net.community.apps.tools.adm.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import javax.swing.SwingWorker;

import net.community.chest.db.sql.AbstractResultSetHandler;
import net.community.chest.db.sql.ResultSetRowColumnData;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;
import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 15, 2009 10:15:10 AM
 */
public class ValuesPopulator extends SwingWorker<Void,Map.Entry<String,String>> {
    private static final LoggerWrapper    _logger=WrapperFactoryManager.getLogger(ValuesPopulator.class);

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

    private final String    _secName;
    public final String getSectionName ()
    {
        return _secName;
    }

    public ValuesPopulator (final MainFrame f, final Connection c, final String secName)
    {
        if ((null == (_f=f)) || (null == (_c=c))
         || (null == (_secName=secName)) || (secName.length() <= 0))
            throw new IllegalArgumentException("Incomplete configuration");
    }

    private class ValuesResultSetHandler extends AbstractResultSetHandler {
        protected ValuesResultSetHandler ()
        {
            super();
        }
        /*
         * @see net.community.chest.db.sql.AbstractResultSetHandler#processResultRow(int, java.util.List)
         */
        @SuppressWarnings({ "synthetic-access", "unchecked" })
        @Override
        public boolean processResultRow (int rowIndex, List<? extends ResultSetRowColumnData> cl)
            throws SQLException
        {
            final int    numCols=(null == cl) ? 0 : cl.size();
            if (numCols <= 0)
                return false;

            if (numCols != 2)
                throw new SQLException("Unexpected number of columns (" + numCols + ") in row #" + rowIndex);

            final ResultSetRowColumnData    pnd=cl.get(0), pvd=cl.get(1);
            final Object                    rcn=
                (null == pnd) ? null : pnd.getColumnValue(),
                                            rcv=
                (null == pvd) ? null : pvd.getColumnValue();
            final String                    pName=
                (null == rcn) ? null : rcn.toString(),
                                            pValue=
                (null == rcv) ? null : rcv.toString();
            if ((null == pName) || (pName.length() <= 0))
                throw new SQLException("Null/empty name in row #" + rowIndex);

            publish(new MapEntryImpl<String,String>(pName, pValue));
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
            final ResultSet    rs=s.executeQuery("SELECT cfg.paramname,cfg.paramvalue FROM CONFIG cfg WHERE section = '" + getSectionName() + "' ORDER BY paramname ASC");
            final long        qEnd=System.currentTimeMillis(), qDuration=qEnd - qStart;
            final int        fSize=rs.getFetchSize();
            if (_logger.isDebugEnabled())
                _logger.debug("Fetched " + fSize + " items in " + qDuration + " msec.");

            final ValuesResultSetHandler    rsh=new ValuesResultSetHandler();
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

        f.signalValuesPopulationDone(this, getSectionName());
    }
    /*
     * @see javax.swing.SwingWorker#process(java.util.List)
     */
    @Override
    protected void process (List<Map.Entry<String,String>> chunks)
    {
        final int    numChunks=(null == chunks) ? 0 : chunks.size();
        if (numChunks <= 0)
            return;

        final MainFrame    f=getMainFrame();
        if (null == f)    // should not happen
            return;

        final String    secName=getSectionName();
        if ((null == secName) || (secName.length() <= 0))
            return;    // should not happen

        for (final Map.Entry<String,String> p : chunks)
        {
            final String    pn=(null == p) ? null : p.getKey(),
                            pv=(null == p) ? null : p.getValue();
            if ((null == pn) || (pn.length() <= 0))
                continue;    // should not happen

            f.addConfigValue(secName, pn, pv);
        }
    }
}
