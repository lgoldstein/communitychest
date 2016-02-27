/*
 * 
 */
package net.community.apps.tools.filesync;

import javax.swing.JFileChooser;

import net.community.chest.ui.helpers.panel.input.FileInputTextPanel;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 2, 2009 2:26:38 PM
 */
public class FilePairInputTextPanel extends FileInputTextPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5765099240910341740L;
	public FilePairInputTextPanel (String title)
	{
		setTitle(title);
	}
	/*
	 * @see net.community.chest.ui.helpers.panel.input.FileInputTextPanel#getFileChooser(boolean)
	 */
	@Override
	protected JFileChooser getFileChooser (boolean createIfNotExist)
	{
		final JFileChooser	fc=super.getFileChooser(createIfNotExist);
		if ((fc != null) && (JFileChooser.DIRECTORIES_ONLY != fc.getFileSelectionMode()))
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		return fc;
	}

}
