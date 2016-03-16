/*
 *
 */
package net.community.apps.tools.svn.wc;

import java.awt.event.KeyEvent;

import net.community.apps.tools.svn.SVNBaseMainFrame;
import net.community.chest.ui.helpers.input.InputFieldValidatorKeyListener;
import net.community.chest.ui.helpers.text.InputTextField;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 5, 2009 12:56:30 PM
 */
public class WCLocationInputTextKeyListener extends InputFieldValidatorKeyListener<WCLocationInputTextField> {
    private final SVNBaseMainFrame<?>    _f;
    public final SVNBaseMainFrame<?> getMainFrame ()
    {
        return _f;
    }

    public WCLocationInputTextKeyListener (final WCLocationInputTextField v, final SVNBaseMainFrame<?> f)
    {
        super(v);

        if (null == (_f=f))
            throw new IllegalArgumentException("No " + SVNBaseMainFrame.class.getSimpleName() + " instance provided");
    }
    /*
     * @see net.community.chest.ui.helpers.input.InputFieldValidatorKeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    @Override
    public void keyReleased (KeyEvent e)
    {
        if ((e != null) && (e.getKeyChar() == KeyEvent.VK_ENTER))
        {
            final InputTextField        v=getValidator();
            final SVNBaseMainFrame<?>    f=getMainFrame();
            if ((v != null) && (f != null) && v.isValidData())
            {
                f.setWCLocation(null, v.getText(), false);
                return;
            }
        }

        super.keyReleased(e);
    }
}
