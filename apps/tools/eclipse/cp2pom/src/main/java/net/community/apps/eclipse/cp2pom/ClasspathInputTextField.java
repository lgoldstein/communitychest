/*
 *
 */
package net.community.apps.eclipse.cp2pom;

import net.community.chest.ui.helpers.text.InputTextField;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 30, 2009 8:47:07 AM
 */
public class ClasspathInputTextField extends InputTextField {
    /**
     *
     */
    private static final long serialVersionUID = -8781385179501151078L;

    public ClasspathInputTextField ()
    {
        super(false);    // delay auto-layout till AFTER verifier set
        setInputVerifier(new ClasspathFileInputVerifier());
        layoutComponent();
    }
}
