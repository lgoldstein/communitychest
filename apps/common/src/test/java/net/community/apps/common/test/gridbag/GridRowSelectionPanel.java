package net.community.apps.common.test.gridbag;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.community.chest.awt.layout.BaseFlowLayout;
import net.community.chest.awt.layout.FlowLayoutAlignment;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 19, 2008 1:03:53 PM
 */
public class GridRowSelectionPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3057809064030890358L;

	public GridRowSelectionPanel (final String rowName)
	{
		super(new BaseFlowLayout(FlowLayoutAlignment.LEFT, 5, 0));
		setName(rowName);

		add(new JLabel(rowName));

		final GridBagAnchorsChoice	gba=new GridBagAnchorsChoice(true);
		add(gba);

		final GridBagFillsChoice	gbf=new GridBagFillsChoice(true);
		add(gbf);
	}
}
