package net.community.apps.tools.jarscanner;

import net.community.chest.ui.helpers.table.EnumTableColumn;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 22, 2007 11:34:41 AM
 */
public class JarEntriesTableColInfo extends EnumTableColumn<JarEntriesTableColumns> {
    /**
     *
     */
    private static final long serialVersionUID = 4944373054409954437L;

    public JarEntriesTableColInfo (JarEntriesTableColumns colIndex, String colName, int colWidth)
    {
        super(JarEntriesTableColumns.class, colIndex, colWidth);
        setColumnName(colName);
    }
}
