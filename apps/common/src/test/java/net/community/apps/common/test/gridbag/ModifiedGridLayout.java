/*
 * 
 */
package net.community.apps.common.test.gridbag;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;

import net.community.chest.awt.ComponentSizeType;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 23, 2009 12:23:14 PM
 */
public class ModifiedGridLayout extends GridLayout {
	/**
	 * 
	 */
	private static final long serialVersionUID = 611339312462648048L;
	public ModifiedGridLayout (int rows, int cols, int hgap, int vgap) throws IllegalArgumentException
    {
        super(rows, cols, hgap, vgap);
    }

    public ModifiedGridLayout (int rows, int cols) throws IllegalArgumentException
    {
        this(rows, cols, 0, 0);
    }

    protected Dimension calculateLayoutSize (final Container parent, final ComponentSizeType szt)
    {
        final Insets	insets=(null == parent) ? null : parent.getInsets();
        final int		ncomponents=(null == parent) ? 0 : parent.getComponentCount(),
        				rows=getRows(), cols=getColumns();
        int				nrows=rows,ncols=cols;
        if (nrows > 0)
            ncols = (ncomponents + nrows - 1) / nrows;
        else
            nrows = (ncomponents + ncols - 1) / ncols;

        int w=0,h=0;
        for (int i = 0; i < ncomponents; i++)
        {
            final Component comp=parent.getComponent(i);
            final Dimension d=((null == szt) || (null == comp)) ? null : szt.getSize(comp);
            if (null == d)
            	continue;

            if (w < d.width)
                w = d.width;
            if (h < d.height)
                h = d.height;
        }

        final int	iw=(null == insets) ? 0 : insets.left + insets.right,
        			ih=(null == insets) ? 0 : insets.top + insets.bottom,
        			dw=iw + ncols * w + (ncols - 1) * getHgap(),
        			dh=ih + nrows * h + (nrows - 1) * getVgap();
        return new Dimension(dw, dh);
    }
    /*
     * @see java.awt.GridLayout#preferredLayoutSize(java.awt.Container)
     */
    @Override
	public Dimension preferredLayoutSize (final Container parent)
    {
    	return calculateLayoutSize(parent, ComponentSizeType.PREFERRED);
    }
    /*
     * @see java.awt.GridLayout#minimumLayoutSize(java.awt.Container)
     */
    @Override
	public Dimension minimumLayoutSize (Container parent)
    {
    	return calculateLayoutSize(parent, ComponentSizeType.MINIMUM);
    }
    /*
     * @see java.awt.GridLayout#layoutContainer(java.awt.Container)
     */
    @Override
	public void layoutContainer (Container parent)
    {
    	final Insets	insets=(null == parent) ? null : parent.getInsets();
        final int		ncomponents=(null == parent) ? 0 : parent.getComponentCount();
        int 			x=(null == insets) ? 0 : insets.left,
        				y=(null == insets) ? 0 : insets.top,
        				nrows=getRows(), ncols=getColumns();
        if (nrows > 0)
            ncols = (ncomponents + nrows - 1) / nrows;
        else
            nrows = (ncomponents + ncols - 1) / ncols;

        final int	iw=(null == insets) ? 0 : (insets.left + insets.right),
        			ih=(null == insets) ? 0 : (insets.top + insets.bottom),
        			hg=getHgap(), vg=getVgap();
        int 		w=((null == parent) ? 0 : parent.getWidth()) - iw,
        			h=((null == parent) ? 0 : parent.getHeight()) - ih;
        if (ncols > 0)
        	w = (w - (ncols - 1) * hg) / ncols;
        if (nrows > 0)
        	h = (h - (nrows - 1) * vg) / nrows;

        for (int i = 0; i < ncomponents; ++i)
        {
        	final Component comp=parent.getComponent(i);
            final Dimension	d=(null == comp) ? null : comp.getPreferredSize();
            if (null == d)
            	continue;

            if (ncols > 0)
            {
            	comp.setBounds(x, y, w, d.height);
            	x += (d.width + hg);
            }
            else
            {
            	comp.setBounds(x, y, d.width, h);
            	y += (d.height + vg);
            }
        }
    }
}
