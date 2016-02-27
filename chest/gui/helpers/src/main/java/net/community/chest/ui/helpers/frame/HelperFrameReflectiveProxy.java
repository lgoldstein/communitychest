/*
 * 
 */
package net.community.chest.ui.helpers.frame;

import net.community.chest.dom.DOMUtils;
import net.community.chest.swing.component.frame.BaseFrameReflectiveProxy;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <F> The reflected {@link HelperFrame}
 * @author Lyor G.
 * @since Dec 11, 2008 3:59:02 PM
 */
public class HelperFrameReflectiveProxy<F extends HelperFrame> extends BaseFrameReflectiveProxy<F> {
	public HelperFrameReflectiveProxy (Class<F> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected HelperFrameReflectiveProxy (Class<F> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public String getSectionName (F src, Element elem)
	{
		if ((null == src) || (null == elem))
			return null;

		return elem.getAttribute(NAME_ATTR);
	}
	/*
	 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#handleUnknownXmlChild(java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	public F handleUnknownXmlChild (F src, Element elem) throws Exception
	{
		final String	n=getSectionName(src, elem);
		if ((n != null) && (n.length() > 0))
		{
			final Element	prev=src.addSection(n, elem);
			if (prev != null)
				throw new IllegalStateException("handleUnknownXmlChild(" + n + "[" + DOMUtils.toString(elem) + "] duplicate section found: " + DOMUtils.toString(prev));

			return src;
		}

		return super.handleUnknownXmlChild(src, elem);
	}

	public static final HelperFrameReflectiveProxy<HelperFrame>	HLPRFRM=
		new HelperFrameReflectiveProxy<HelperFrame>(HelperFrame.class, true) {
			/* Need to override this in order to ensure correct auto-layout
			 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
			 */
			@Override
			public HelperFrame fromXml (Element elem) throws Exception
			{
				return (null == elem) ? null : new HelperFrame(elem);
			}
		};
}
