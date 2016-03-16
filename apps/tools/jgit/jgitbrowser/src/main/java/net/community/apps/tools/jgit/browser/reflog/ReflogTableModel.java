/*
 *
 */
package net.community.apps.tools.jgit.browser.reflog;

import java.util.NoSuchElementException;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.w3c.dom.Element;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 20, 2011 11:54:08 AM
 */
public class ReflogTableModel extends EnumColumnAbstractTableModel<ReflogEntryColumns,ReflogEntryRow> {
    /**
     *
     */
    private static final long serialVersionUID = -1501101069238254273L;
    public ReflogTableModel (Element elem) throws Exception
    {
        super(ReflogEntryColumns.class, ReflogEntryRow.class);
        setColumnsValues(ReflogEntryColumns.VALUES);
        fromXml(elem);
    }
    /*
     * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#fromColumnElement(org.w3c.dom.Element)
     */
    @Override
    @CoVariantReturn
    public ReflogEntryColInfo fromColumnElement (Element colElem) throws Exception
    {
        return new ReflogEntryColInfo(colElem);
    }
    /*
     * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#fromXml(org.w3c.dom.Element)
     */
    @Override
    @CoVariantReturn
    public ReflogTableModel fromXml (Element root) throws Exception
    {
        final Object    instance=super.fromXml(root);
        if (instance != this)
            throw new IllegalStateException("fromXml(" + DOMUtils.toString(root) + "] mismatched reconstructed instances");

        return this;
    }
    /*
     * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#getColumnValue(int, java.lang.Object, java.lang.Enum)
     */
    @Override
    public Object getColumnValue (int rowIndex, ReflogEntryRow row, ReflogEntryColumns colIndex)
    {
        if (null == colIndex)
            throw new IllegalStateException("getColumnValue(" + rowIndex + ") no column");

        final RevCommit        entry=(row == null) ? null : row.getLogEntry();
        final PersonIdent    ident=(entry == null) ? null : entry.getCommitterIdent();
        if (null == ident)
            throw new IllegalStateException("getColumnValue(" + rowIndex + "/" + colIndex + ") no row data");

        switch(colIndex)
        {
            case NAME    :
                return ident.getName();
            case EMAIL    :
                return ident.getEmailAddress();
            case TIMESTAMP    :
                return ident.getWhen();
            case COMMENT    :
                return entry.getShortMessage().trim();
            default            :
                throw new NoSuchElementException("getColumnValue(" + rowIndex + "/" + colIndex + ") unknown column requested");
        }
    }
    /*
     * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#setValueAt(int, java.lang.Object, int, java.lang.Enum, java.lang.Object)
     */
    @Override
    public void setValueAt (int rowIndex, ReflogEntryRow row, int colNum, ReflogEntryColumns colIndex, Object value)
    {
        throw new UnsupportedOperationException("setValueAt(" + rowIndex + ":" + colNum + "/" + colIndex + ")::=" + value + " - N/A");
    }
}
