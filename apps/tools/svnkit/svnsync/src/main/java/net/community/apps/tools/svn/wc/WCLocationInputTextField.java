/*
 *
 */
package net.community.apps.tools.svn.wc;

import java.awt.event.KeyListener;

import net.community.apps.tools.svn.SVNBaseMainFrame;
import net.community.chest.ui.helpers.text.InputTextField;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 5, 2009 12:53:02 PM
 */
public class WCLocationInputTextField extends InputTextField {
    /**
     *
     */
    private static final long serialVersionUID = 2512684490224806845L;
    private final SVNBaseMainFrame<?>    _f;
    public final SVNBaseMainFrame<?> getMainFrame ()
    {
        return _f;
    }
    /*
     * @see net.community.chest.ui.helpers.text.InputTextField#createDefaultKeyListener()
     */
    @Override
    protected KeyListener createDefaultKeyListener ()
    {
        return new WCLocationInputTextKeyListener(this, getMainFrame());
    }

    public WCLocationInputTextField (final SVNBaseMainFrame<?> f)
    {
        // delay auto-layout till after setting the frame and input verifier
        super(false);

        if (null == (_f=f))
            throw new IllegalArgumentException("No " + SVNBaseMainFrame.class.getSimpleName() + " instance provided");
        setInputVerifier(WCLocationFileInputVerifier.DEFAULT);
        layoutComponent();
    }
}
