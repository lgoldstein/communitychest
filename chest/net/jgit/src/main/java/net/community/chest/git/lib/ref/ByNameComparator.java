/*
 * 
 */
package net.community.chest.git.lib.ref;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 16, 2011 11:46:26 AM
 */
public class ByNameComparator extends AbstractRefComparator {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5162045984834688329L;

	public ByNameComparator (boolean ascending)
	{
		super(RefAttributeType.NAME, ascending);
	}

	public static final ByNameComparator	ASCENDING=new ByNameComparator(true),
											DESCENDING=new ByNameComparator(false);
}
