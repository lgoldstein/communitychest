/*
 * 
 */
package net.community.chest.ui.components.input.panel.img;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import net.community.chest.awt.AWTUtils;
import net.community.chest.awt.image.ImageUtils;
import net.community.chest.awt.layout.border.BorderLayoutPosition;
import net.community.chest.dom.impl.StandaloneDocumentImpl;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.ui.helpers.panel.HelperPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Panel with an optional background {@link Image}</P>
 * 
 * @author Lyor G. - based on this <a href="http://technocrataditi.wordpress.com/2007/03/16/java-how-to-put-background-image-in-swings/">code</a>
 * @since Mar 8, 2009 2:54:39 PM
 */
public class BgImagePanel extends HelperPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8221986617011159727L;
	private Image	_img;
	public Image getBgImage ()
	{
		return _img;
	}

	protected void setOpaque (Component comp, boolean opaque)
	{
		AWTUtils.setOpaque(comp, opaque);
	}
	// Note: may require calling repaint() if set after panel is displayed
	public void setBgImage (Image img)
	{
		_img = img;
	}

	private BorderLayoutPosition	_bgImgPos=BorderLayoutPosition.CENTER;
	public BorderLayoutPosition getBgImagePosition ()
	{
		return _bgImgPos;
	}
	// Note: may require calling repaint() if set after panel is displayed
	public void setBgImagePosition (BorderLayoutPosition pos)
	{
		_bgImgPos = pos;
	}
	// Note: may require calling repaint() if set after panel is displayed
	private Insets	_bgImgMargin;
	public Insets getBgImageMargin ()
	{
		return _bgImgMargin;
	}

	public void setBgImageMargin (Insets margin)
	{
		_bgImgMargin = margin;
	}

	public BgImagePanel (Image img, LayoutManager layout, Document doc, boolean autoInit)
	{
		super(layout, doc, autoInit && (null == img) /* delay auto-init till image initialization */);

		if (((_img=img) != null) && autoInit)
			layoutComponent();
	}
	
	public BgImagePanel (Image img, LayoutManager layout, boolean autoInit)
	{
		this(img, layout, (Document) null, autoInit);
	}

	public BgImagePanel (Image img, LayoutManager layout)
	{
		this(img, layout, true);
	}

	public BgImagePanel (LayoutManager layout, Document doc, boolean autoInit)
	{
		this(null, layout, doc, autoInit);
	}

	public BgImagePanel (LayoutManager layout, boolean autoInit)
	{
		this(layout, (Document) null, autoInit);
	}

	public BgImagePanel (LayoutManager layout)
	{
		this(layout, true);
	}

	public BgImagePanel (Image img, Document doc, boolean autoInit)
	{
		super(doc, autoInit && (null == img) /* delay auto-init till image initialization */);

		if (((_img=img) != null) && autoInit)
			layoutComponent();
	}

	public BgImagePanel (Image img, boolean autoInit)
	{
		this(img, (Document) null, autoInit);
	}

	public BgImagePanel (Image img)
	{
		this(img, true);
	}

	public BgImagePanel ()
	{
		this((Image) null);
	}

	public BgImagePanel (Image img, LayoutManager layout, Element elem, boolean autoInit)
	{
		this(img, layout, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoInit);
	}
	/*
	 * @see net.community.chest.ui.helpers.panel.HelperPanel#getPanelConverter(org.w3c.dom.Element)
	 */
	@Override
	protected XmlProxyConvertible<?> getPanelConverter (Element elem)
	{
		return (null == elem) ? null : BgImagePanelReflectiveProxy.BGIMGPNL;
	}
	/*
	 * @see java.awt.Container#addImpl(java.awt.Component, java.lang.Object, int)
	 */
	@Override
	protected void addImpl (Component comp, Object constraints, int index)
	{
		setOpaque(comp, false);
		super.addImpl(comp, constraints, index);
	}

	protected Rectangle getImageDisplayArea ()
	{
		return AWTUtils.getDisplayArea(this, getBgImageMargin());
	}

	protected void drawBgImage (final Graphics g)
	{
		final Rectangle	r=getImageDisplayArea();
		ImageUtils.drawImage(g, getBgImage(), getBgImagePosition(), r, this);
	}
	/*
	 * @see java.awt.Container#paintComponents(java.awt.Graphics)
	 */
	@Override
	public void paintComponents (Graphics g)
	{
		super.paintComponents(g);
		drawBgImage(g);
	}
	/*
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent (Graphics g)
	{
		super.paintComponent(g);
		drawBgImage(g);
	}
}
