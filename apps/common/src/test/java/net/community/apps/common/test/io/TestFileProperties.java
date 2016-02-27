/*
 * 
 */
package net.community.apps.common.test.io;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JTextField;

import net.community.apps.common.test.TestMainFrame;
import net.community.chest.swing.component.scroll.ScrolledComponent;
import net.community.chest.ui.helpers.panel.input.LRFieldWithButtonPanel;

import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 26, 2009 9:42:43 AM
 */
public class TestFileProperties extends TestMainFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4685738770763665326L;
	private FilePropertiesPanel	_props;
    /*
     * @see net.community.apps.common.FileLoadComponent#loadFile(java.io.File, java.lang.String, org.w3c.dom.Element)
     */
    @Override
	public void loadFile (File f, String cmd, Element dlgElement)
	{
		final String filePath=(null == f) ? null : f.getAbsolutePath();
		if ((null == filePath) || (filePath.length() <= 0) || (null == _props))
			return;
		
		_props.setAssignedValue(f);
		setTitle(filePath);
	}

	private JTextField	_ft;
	protected void loadFileFromText ()
	{
		final String	p=(null == _ft) ? null : _ft.getText();
		if ((null == p) || (p.length() <= 0))
			return;

		loadFile(new File(p), LOAD_CMD, null);
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#layoutComponent()
	 */
	@Override
	public void layoutComponent () throws RuntimeException
	{
		super.layoutComponent();

		final Container	ctPane=getContentPane();
		{
			final LRFieldWithButtonPanel	p=new LRFieldWithButtonPanel();
			if ((_ft=p.getTextField()) != null)
			{
				final JButton	b=p.getButton();
				if (b != null)
				{
					b.setText("Show");
					b.addActionListener(new ActionListener() {
						/*
						 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
						 */
						@Override
						public void actionPerformed (ActionEvent e)
						{
							final Object	s=(null == e) ? null : e.getSource();
							if (s != null)
								loadFileFromText();
						}
					});
				}
			}

			ctPane.add(p, BorderLayout.NORTH);
		}
		if (null == _props)
		{
			_props = new FilePropertiesPanel();
			setDropTarget(new DropTarget(_props, this));
			ctPane.add(new ScrolledComponent<FilePropertiesPanel>(_props), BorderLayout.CENTER);
		}
	}

	public TestFileProperties (String... args) throws Exception
	{
		super(args);

		if ((args != null) && (args.length == 1))
			loadFile(new File(args[0]), LOAD_CMD, null);
	}
}
