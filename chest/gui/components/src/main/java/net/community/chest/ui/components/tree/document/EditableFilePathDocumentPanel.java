/*
 * 
 */
package net.community.chest.ui.components.tree.document;

import javax.swing.JTextField;

import net.community.chest.CoVariantReturn;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 10, 2008 1:30:53 PM
 */
public class EditableFilePathDocumentPanel extends BaseDocumentPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8975195612467302487L;

	/*
	 * @see net.community.chest.ui.helpers.tree.trees.BaseDocumentPanel#createFilePathComponent()
	 */
	@Override
	@CoVariantReturn
	protected JTextField createFilePathComponent ()
	{
		return new JTextField("");
	}
	/*
	 * @see net.community.chest.ui.helpers.tree.trees.BaseDocumentPanel#getFilePathComponent()
	 */
	@Override
	@CoVariantReturn
	public JTextField getFilePathComponent ()
	{
		return (JTextField) super.getFilePathComponent();
	}

	public EditableFilePathDocumentPanel (final boolean autoLayout)
	{
		super(autoLayout);
	}

	public EditableFilePathDocumentPanel ()
	{
		this(true);
	}
}
