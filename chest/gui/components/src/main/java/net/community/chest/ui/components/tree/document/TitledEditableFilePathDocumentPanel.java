/*
 * 
 */
package net.community.chest.ui.components.tree.document;

import java.util.Map;

import net.community.chest.CoVariantReturn;
import net.community.chest.ui.helpers.panel.input.LRFieldWithLabelPanel;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Provides a title as well for the text field of the file path
 * @author Lyor G.
 * @since Dec 10, 2008 2:02:26 PM
 */
public class TitledEditableFilePathDocumentPanel extends BaseDocumentPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2228093755422362916L;
	private boolean	_lblRightPos	/* =false */;
	public boolean isLabelRightPos ()
	{
		return _lblRightPos;
	}
	// CAVEAT EMPTOR !!! should not be called after "layoutComponent"
 	public void setLabelRightPos (final boolean lblRightPos)
	{
		if (lblRightPos != isLabelRightPos())
			_lblRightPos = lblRightPos;
	}
 	/**
 	 * Default section name for the {@link #createFilePathComponent()} XML
 	 * {@link Element} section 
 	 */
 	public static final String	FILE_PATH_COMP_SECTION_NAME="file-path-comp";
	/*
	 * @see net.community.chest.ui.helpers.tree.trees.BaseDocumentPanel#createFilePathComponent()
	 */
	@Override
	@CoVariantReturn
	protected LRFieldWithLabelPanel createFilePathComponent ()
	{
		final Map<String,? extends Element>	sm=getSectionsMap();
		final Element						ce=
			((null == sm) || (sm.size() <= 0)) ? null : sm.get(FILE_PATH_COMP_SECTION_NAME);
		return new LRFieldWithLabelPanel(isLabelRightPos(), ce, true);
	}
	/*
	 * @see net.community.chest.ui.helpers.tree.trees.BaseDocumentPanel#getFilePathComponent()
	 */
	@Override
	@CoVariantReturn
	public LRFieldWithLabelPanel getFilePathComponent ()
	{
		return (LRFieldWithLabelPanel) super.getFilePathComponent();
	}

	public TitledEditableFilePathDocumentPanel (final boolean autoLayout, final boolean lblRightPos)
	{
		super(false /* delay layout till _lblRightPos initialized */);
		_lblRightPos = lblRightPos;
		if (autoLayout)
			layoutComponent();
	}

	public TitledEditableFilePathDocumentPanel (final boolean autoLayout)
	{
		this(autoLayout, false);
	}

	public TitledEditableFilePathDocumentPanel ()
	{
		this(true);
	}

}
