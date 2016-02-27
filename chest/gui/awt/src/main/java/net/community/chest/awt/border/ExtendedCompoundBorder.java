package net.community.chest.awt.border;

import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>Provides inside/outside border setter(s)</P>
 * 
 * @author Lyor G.
 * @since Jul 1, 2009 8:57:16 AM
 */
public class ExtendedCompoundBorder extends CompoundBorder {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2295453288284390495L;

	public ExtendedCompoundBorder (Border outBorder, Border inBorder)
	{
		super(outBorder, inBorder);
	}

	public ExtendedCompoundBorder ()
	{
		this(null, null);
	}
	
    public void setOutsideBorder (Border b)
    {
        outsideBorder = b;
    }

    public void setInsideBorder (Border b)
    {
        insideBorder = b;
    }

    public static final <B extends CompoundBorder> B setBorder (B cb, Border b, boolean useInside) throws Exception
    {
    	if (b instanceof ExtendedCompoundBorder)
    	{
    		final ExtendedCompoundBorder	eb=(ExtendedCompoundBorder) cb;
    		if (useInside)
    			eb.setInsideBorder(b);
    		else
    			eb.setOutsideBorder(b);
    	}
    	else
    	{
    		if (useInside)
    			CompoundBorderFieldsAccessor.DEFAULT.setInsideBorder(cb, b);
    		else
    			CompoundBorderFieldsAccessor.DEFAULT.setOutsideBorder(cb, b);
    			
    	}

    	return cb;
    }
}
