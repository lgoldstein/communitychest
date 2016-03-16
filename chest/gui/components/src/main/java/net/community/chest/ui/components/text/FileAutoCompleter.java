/*
 *
 */
package net.community.chest.ui.components.text;

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import net.community.chest.util.ArraysUtils;

/**
 * <P>Copyright as per GPLv2</P>
 * Based on code published by <A HREF="mailto:santhosh@in.fiorano.com">Santhosh Kumar</A>
 * in the <A HREF="http://www.jroller.com/santhosh/entry/file_path_autocompletion">weblog</A>
 * @param <C> Type of {@link JTextComponent} being used for auto-completion
 * @author Lyor G.
 * @since Feb 24, 2011 12:40:35 PM
 */
public class FileAutoCompleter<C extends JTextComponent> extends AutoCompleter<C> {
    private FilenameFilter    _filter;
    public FilenameFilter getFilter ()
    {
        return _filter;
    }

    public void setFilter (FilenameFilter filter)
    {
        _filter = filter;
    }

    public FileAutoCompleter (C comp, FilenameFilter filter) throws IllegalArgumentException
    {
        super(comp);
        _filter = filter;
    }

    public FileAutoCompleter (C comp) throws IllegalArgumentException
    {
        this(comp, null);
    }
    /**
     * Called by {@link #updateListData()} <U>before</U> proposing candidates,
     * provided no current filter already set via constructor or {@link #setFilter(FilenameFilter)}.
     * @param dir The directory about to be scanned
     * @param prefix The prefix that the user already typed in (may be
     * <code>null</code>/empty)
     * @return The {@link FilenameFilter} to be used - <code>null</code> means
     * no filtering
     */
    protected FilenameFilter resolveFilter (final File dir, final String prefix)
    {
        final FilenameFilter    curFilter=getFilter();
        if (curFilter != null)
            return curFilter;

        if ((dir == null) || (!dir.isDirectory()))
            return null;

        return new FilenameFilter() {
                @Override
                public boolean accept (File parent, String name)
                {
                    return (prefix != null) ? name.toLowerCase().startsWith(prefix) : true;
                }
            };
    }

    public static final String[]    EMPTY_FILES=new String[0];
    /*
     * @see net.community.chest.ui.components.text.AutoCompleter#updateListData()
     */
    @Override
    protected boolean updateListData ()
    {
        final String     value=getTextComponent().getText();
        final int        index=value.lastIndexOf(File.separatorChar);
        if (index <= 0)
            return false;

        final String            dir=value.substring(0, index + 1),
                                prefix=(index == (value.length() - 1)) ? null : value.substring(index + 1).toLowerCase();
        final File                dirFile=new File(dir);
        final FilenameFilter    filter=resolveFilter(dirFile, prefix);
        final String[]             files=(filter == null) ? dirFile.list() : dirFile.list(filter);

        final JList<String>    choicesList=getChoicesList();
        if (ArraysUtils.length(files) <= 0)
        {
            choicesList.setListData(EMPTY_FILES);
        }
        else
        {
            if ((files.length == 1) && files[0].equalsIgnoreCase(prefix))
                choicesList.setListData(EMPTY_FILES);
            else
                choicesList.setListData(files);
        }

        return true;
    }
    /*
     * @see net.community.chest.ui.components.text.AutoCompleter#acceptedListItem(java.lang.String)
     */
    @Override
    protected void acceptedListItem (String selected)
    {
        if ((selected == null) || (selected.length() <= 0))
            return;

        final JTextComponent    textComp=getTextComponent();
        final String            value=textComp.getText();
        final int                index=value.lastIndexOf(File.separatorChar);
        if (index < 0)
            return;

        final Document    doc=textComp.getDocument();
        final int        prefixLen=doc.getLength() - index - 1;
        try
        {
            doc.insertString(textComp.getCaretPosition(), selected.substring(prefixLen), null);
        }
        catch(BadLocationException e)    // should not happen
        {
            throw new RuntimeException("acceptedListItem(" + selected + ")", e);
        }
    }
}
