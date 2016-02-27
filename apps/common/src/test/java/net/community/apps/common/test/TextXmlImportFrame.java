/*
 * 
 */
package net.community.apps.common.test;

import java.awt.BorderLayout;
import java.awt.Container;

import net.community.chest.ui.components.dialog.load.xml.XmlImportPanel;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 31, 2009 12:56:35 PM
 */
public class TextXmlImportFrame extends TestMainFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8382212881330611983L;
	private XmlImportPanel	_impPanel;
	/*
	 * @see net.community.apps.common.BaseMainFrame#layoutComponent()
	 */
	@Override
	public void layoutComponent () throws RuntimeException
	{
		super.layoutComponent();

		final Container	ctPane=getContentPane();
		if (null == _impPanel)
			_impPanel = new XmlImportPanel();
		ctPane.add(_impPanel, BorderLayout.CENTER);
	}

	public TextXmlImportFrame (String... args) throws Exception
	{
		super(args);
	}
}
