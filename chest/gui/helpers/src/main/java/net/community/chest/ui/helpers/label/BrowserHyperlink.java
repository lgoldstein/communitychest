/*
 * 
 */
package net.community.chest.ui.helpers.label;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.Icon;

import net.community.chest.lang.ExceptionUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 28, 2009 9:09:53 AM
 */
public class BrowserHyperlink extends JHyperlink implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5902405764188920063L;
	public BrowserHyperlink (String text, Icon icon, int horizontalAlignment, String url)
	{
		super(text, icon, horizontalAlignment, url);
	}

	public BrowserHyperlink (String text, Icon icon, int horizontalAlignment)
	{
		this(text, icon, horizontalAlignment, null);
	}

	public BrowserHyperlink (Icon image)
	{
        this(null, image, CENTER);
	}

	public BrowserHyperlink (Icon image, String url)
	{
        this(null, image, CENTER, url);
	}

	public BrowserHyperlink (String text, int horizontalAlignment)
	{
		this(text, null, horizontalAlignment);
	}

	public BrowserHyperlink (String text, int horizontalAlignment, String url)
	{
		this(text, null, horizontalAlignment, url);
	}

	public BrowserHyperlink (Icon image, int horizontalAlignment)
	{
		this(null, image, horizontalAlignment);
	}

	public BrowserHyperlink (Icon image, int horizontalAlignment, String url)
	{
		this(null, image, horizontalAlignment, url);
	}

	public BrowserHyperlink (String text, String url)
	{
		this(text, null, LEADING, url);
	}

	public BrowserHyperlink (String text)
	{
		this(text, null);
	}

	public BrowserHyperlink ()
	{
		this("");
	}
	/*
	 * @see net.community.chest.ui.helpers.label.JHyperlink#layoutComponent()
	 */
	@Override
	public void layoutComponent () throws RuntimeException
	{
		super.layoutComponent();
		addActionListener(this);
	}
	/*
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed (ActionEvent event)
	{
		final Object	src=(null == event) ? null : event.getSource();
		if (src != this)	// should not happen
			return;

		final String	s=getUrl();
		final Desktop	d=Desktop.getDesktop();
		try
		{
			d.browse(new URI(s));
		}
		catch(Exception e)
		{
			throw ExceptionUtil.toRuntimeException(e);
		}
	}
}
