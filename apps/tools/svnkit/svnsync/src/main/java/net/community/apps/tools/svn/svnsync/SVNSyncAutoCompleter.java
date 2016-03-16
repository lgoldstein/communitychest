/*
 *
 */
package net.community.apps.tools.svn.svnsync;

import javax.swing.text.JTextComponent;

import net.community.chest.ui.components.text.FileAutoCompleter;

/**
 * <P>Copyright as per GPLv2</P>
 * @param <C> Type of {@link JTextComponent} being auto-completed
 * @author Lyor G.
 * @since Feb 24, 2011 1:15:18 PM
 */
public class SVNSyncAutoCompleter<C extends JTextComponent> extends FileAutoCompleter<C> {
    public SVNSyncAutoCompleter (C comp) throws IllegalArgumentException
    {
        super(comp);
    }

    public boolean isSVNURL ()
    {
        final String    text=getText();
        return (text != null) && (text.length() > 0) && (text.indexOf("://") > 0);
    }
    /*
     * @see net.community.chest.ui.components.text.FileAutoCompleter#updateListData()
     */
    @Override
    protected boolean updateListData ()
    {
        if (isSVNURL())
            return false;

        return super.updateListData();
    }
    /*
     * @see net.community.chest.ui.components.text.FileAutoCompleter#acceptedListItem(java.lang.String)
     */
    @Override
    protected void acceptedListItem (String selected)
    {
        if (!isSVNURL())
            super.acceptedListItem(selected);
    }
}
