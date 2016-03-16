/*
 *
 */
package net.community.apps.common.test.table;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JMenuItem;

import net.community.apps.common.test.TestMainFrame;
import net.community.chest.swing.component.menu.MenuItemExplorer;
import net.community.chest.swing.component.menu.MenuUtil;
import net.community.chest.swing.component.table.DefaultTableScroll;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 22, 2009 8:46:58 AM
 */
public class TestTableFrame extends TestMainFrame {
    /**
     *
     */
    private static final long serialVersionUID = 2410283975050843964L;
    private TestTable    _tbl;
    protected void addTableRows ()
    {
        if (null == _tbl)
            return;

        for (int    rIndex=0; rIndex < 10; rIndex++)
        {
            final Integer            rValue=Integer.valueOf(rIndex);
            final TestTableRowData    row=new TestTableRowData(rIndex);
            for (final TestTableColumnType c : TestTableColumnType.VALUES)
                row.put(c, rValue);
            _tbl.addValues(row);
        }
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#setMainMenuItemsActionHandlers(net.community.chest.swing.component.menu.MenuItemExplorer)
     */
    @Override
    protected Map<String,JMenuItem> setMainMenuItemsActionHandlers (MenuItemExplorer ie)
    {
        Map<String,JMenuItem>    im=super.setMainMenuItemsActionHandlers(ie);
        final JMenuItem            item=MenuUtil.addMenuItemActionHandler(ie, "refresh", new ActionListener() {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                @Override
                public void actionPerformed (ActionEvent event)
                {
                    addTableRows();
                }
            });

        if (item != null)
        {
            if (null == im)
                im = new TreeMap<String,JMenuItem>(String.CASE_INSENSITIVE_ORDER);
            im.put(item.getActionCommand(), item);
        }

        return im;
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();

        if (null == _tbl)
        {
            _tbl = new TestTable();

            for (final TestTableColumnType c : TestTableColumnType.VALUES)
                _tbl.addColumn(new TestTableColumn(c));

            final TestTableModel        m=_tbl.getTypedModel();
            _tbl.setRowSorter(new TestTableSorter(m));
//            _tbl.setAutoCreateRowSorter(true);
        }

        final Container    ctPane=getContentPane();
        ctPane.add(new DefaultTableScroll(_tbl), BorderLayout.CENTER);
    }

    public TestTableFrame (String... args) throws Exception
    {
        super(args);
    }
}
