/*
 * 
 */
package net.community.chest.io.url;

import java.net.URL;

import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Feb 23, 2011 10:58:43 AM
 */
public abstract class AbstractURLComparator extends AbstractComparator<URL> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8841960952142103990L;
	protected AbstractURLComparator (boolean ascending)
	{
		super(URL.class, !ascending);
	}
	/**
	 * Removes any trailing '/' from the path - provided it is not the only character
	 * @param path The original path value - ignored if <code>null</code>/empty
	 * @return Adjusted path - may be same as input if nothing executed
	 */
	public static final String adjustPathValue (final String path)
	{
		final int	pLen=(path == null) ? 0 : path.length();
		if ((pLen <= 1) || (path.charAt(pLen - 1) != '/'))
			return path;

		return path.substring(0, pLen - 1);
	}
}
