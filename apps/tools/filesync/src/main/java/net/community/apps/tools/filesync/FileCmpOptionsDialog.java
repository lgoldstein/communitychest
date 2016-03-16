/*
 *
 */
package net.community.apps.tools.filesync;

import java.util.Map;
import javax.swing.JFrame;
import net.community.chest.reflect.AttributeAccessor;
import net.community.chest.ui.components.dialog.BooleanOptionsDialog;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 28, 2009 9:00:50 AM
 */
public class FileCmpOptionsDialog extends BooleanOptionsDialog<FileCmpOptions> {
    /**
     *
     */
    private static final long serialVersionUID = 6829530185956082370L;
    public FileCmpOptionsDialog (JFrame owner, FileCmpOptions opts, Element elem, boolean autoInit)
    {
        super(owner, elem, autoInit);
        setContent(opts);
    }

    public FileCmpOptionsDialog (JFrame owner, FileCmpOptions opts, boolean autoInit)
    {
        this(owner, opts, null, autoInit);
    }

    public FileCmpOptionsDialog (JFrame owner, FileCmpOptions opts)
    {
        this(owner, opts, true);
    }

    public FileCmpOptionsDialog (JFrame owner, boolean autoInit)
    {
        this(owner, null, autoInit);
    }

    public FileCmpOptionsDialog (JFrame owner)
    {
        this(owner, true);
    }

    private static Map<String,AttributeAccessor>    _optsAccMap    /* =null */;
    /*
     * @see net.community.chest.ui.components.dialog.BooleanOptionsDialog#getOptionsAccessMap()
     */
    @Override
    protected Map<String,AttributeAccessor> getOptionsAccessMap ()
    {
        synchronized(FileCmpOptionsDialog.class)
        {
            if (null == _optsAccMap)
                _optsAccMap = getOptionsAccessMap(FileCmpOptions.class);
        }

        return _optsAccMap;
    }
}
