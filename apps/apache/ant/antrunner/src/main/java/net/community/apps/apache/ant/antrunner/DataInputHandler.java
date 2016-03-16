/*
 *
 */
package net.community.apps.apache.ant.antrunner;

import javax.swing.JOptionPane;

import net.community.apps.common.BaseMainFrame;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.input.InputHandler;
import org.apache.tools.ant.input.InputRequest;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 31, 2008 1:08:16 PM
 */
public class DataInputHandler implements InputHandler {
    public DataInputHandler ()
    {
        // must exist for instantiation of class via (@link Class#forName(java.lang.String)
    }
    /*
     * @see org.apache.tools.ant.input.InputHandler#handleInput(org.apache.tools.ant.input.InputRequest)
     */
    @Override
    public void handleInput (final InputRequest req) throws BuildException
    {
        if (req == null)
            throw new BuildException("Unexpected ant input request");

        final Object    o=JOptionPane.showInputDialog(BaseMainFrame.getMainFrameInstance(), req.getPrompt(), "", JOptionPane.QUESTION_MESSAGE, null, null, req.getInput());
        if (!(o instanceof String))
             throw new BuildException("Cancelled by user request");

        req.setInput((String) o);
    }
}
