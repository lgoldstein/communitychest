package net.community.chest.awt.layout;

import java.awt.Container;

import javax.swing.BoxLayout;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 20, 2008 8:11:46 AM
 */
public class BaseBoxLayout extends BoxLayout {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1112352771486102570L;

	public BaseBoxLayout (Container target, int axis)
	{
		super(target, axis);
	}

	public BaseBoxLayout (Container target, BoxLayoutAxis axis)
	{
		this(target, axis.getAxis());
	}

	public BaseBoxLayout (Container target, Element elem)
	{
		this(target, BoxLayoutAxis.fromElement(elem));
	}
}
