/*
 * 
 */
package net.community.chest.io.jsch;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 24, 2009 8:36:14 AM
 */
public class UserInfoPrompter implements UserInfo, UIKeyboardInteractive {
	public UserInfoPrompter ()
	{
		super();
	}

	private String passwd;
	/*
	 * @see com.jcraft.jsch.UserInfo#getPassword()
	 */
	@Override
	public String getPassword ()
	{ 
		return passwd;
	}
	/*
	 * @see com.jcraft.jsch.UserInfo#promptYesNo(java.lang.String)
	 */
	@Override
	public boolean promptYesNo (final String str)
	{
		final Object[]	options={ "yes", "no" };
		final int 		foo=JOptionPane.showOptionDialog(null, 
             str,
             "Warning", 
             JOptionPane.DEFAULT_OPTION, 
             JOptionPane.WARNING_MESSAGE,
             null, options, options[0]);
       return foo==0;
    }
    /*
     * @see com.jcraft.jsch.UserInfo#getPassphrase()
     */
    @Override
	public String getPassphrase ()
    {
    	return null;
    }
    /*
     * @see com.jcraft.jsch.UserInfo#promptPassphrase(java.lang.String)
     */
    @Override
	public boolean promptPassphrase (String message)
    { 
    	return true;
    }

    private JTextField passwordField=new JPasswordField(20);
    /*
     * @see com.jcraft.jsch.UserInfo#promptPassword(java.lang.String)
     */
    @Override
	public boolean promptPassword (String message)
    {
    	final Object[] 	ob={ passwordField }; 
    	final int		result=JOptionPane.showConfirmDialog(null, ob, message, JOptionPane.OK_CANCEL_OPTION);
    	if(result==JOptionPane.OK_OPTION)
    	{
    		passwd=passwordField.getText();
    		return true;
    	}

    	return false; 
    }
    /*
     * @see com.jcraft.jsch.UserInfo#showMessage(java.lang.String)
     */
    @Override
	public void showMessage(String message)
    {
      JOptionPane.showMessageDialog(null, message);
    }
    /*
     * @see com.jcraft.jsch.UIKeyboardInteractive#promptKeyboardInteractive(java.lang.String, java.lang.String, java.lang.String, java.lang.String[], boolean[])
     */
    @Override
	public String[] promptKeyboardInteractive (final String destination,
                                               final String name,
                                               final String instruction,
                                               final String[] prompt,
                                               final boolean[] echo)
    {
        final Container 			panel=new JPanel(new GridBagLayout());
        final GridBagConstraints	gbc= 
          new GridBagConstraints(0,0,1,1,1,1,
                                 GridBagConstraints.NORTHWEST,
                                 GridBagConstraints.NONE,
                                 new Insets(0,0,0,0),0,0);
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridx = 0;
        panel.add(new JLabel(instruction), gbc);
        gbc.gridy++;
        gbc.gridwidth = GridBagConstraints.RELATIVE;

        final JTextField[] texts=new JTextField[prompt.length];
        for (int i=0; i<prompt.length; i++)
        {
        	gbc.fill = GridBagConstraints.NONE;
        	gbc.gridx = 0;
        	gbc.weightx = 1;
        	panel.add(new JLabel(prompt[i]),gbc);

        	gbc.gridx = 1;
        	gbc.fill = GridBagConstraints.HORIZONTAL;
        	gbc.weighty = 1;
        	if(echo[i])
        		texts[i]=new JTextField(20);
        	else
        		texts[i]=new JPasswordField(20);
        	panel.add(texts[i], gbc);
        	gbc.gridy++;
        }

        if (JOptionPane.showConfirmDialog(null, panel, destination+": "+name,
        		JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION)
        {
        	final String[] response=new String[prompt.length];
        	for(int i=0; i<prompt.length; i++)
        		response[i]=texts[i].getText();
        	return response;
        }
      
        return null;  // cancel
    }
}
