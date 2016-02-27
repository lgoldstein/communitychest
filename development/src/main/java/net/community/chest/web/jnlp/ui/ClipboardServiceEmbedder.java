/*
 * 
 */
package net.community.chest.web.jnlp.ui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;

import javax.jnlp.ClipboardService;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 9, 2009 12:45:46 PM
 */
public class ClipboardServiceEmbedder implements ClipboardService {
	public Clipboard	_c;
	public Clipboard getClipboard ()
	{
		return _c;
	}

	public void setClipboard (Clipboard c)
	{
		_c = c;
	}

	public ClipboardServiceEmbedder (Clipboard c)
	{
		_c = c;
	}

	public ClipboardServiceEmbedder ()
	{
		this(Toolkit.getDefaultToolkit().getSystemClipboard());
	}
	/*
	 * @see javax.jnlp.ClipboardService#getContents()
	 */
	@Override
	public Transferable getContents ()
	{
		final Clipboard	c=getClipboard();
		if (null == c)
			throw new IllegalStateException("getContents() no " + Clipboard.class.getSimpleName() + " instance");

		return c.getContents(this);
	}
	/*
	 * @see javax.jnlp.ClipboardService#setContents(java.awt.datatransfer.Transferable)
	 */
	@Override
	public void setContents (Transferable t)
	{
		final Clipboard	c=getClipboard();
		if (null == c)
			throw new IllegalStateException("setContents(" + t + ") no " + Clipboard.class.getSimpleName() + " instance");

		c.setContents(t, null);
	}
}
