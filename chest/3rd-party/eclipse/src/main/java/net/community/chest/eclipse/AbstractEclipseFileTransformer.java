/*
 * 
 */
package net.community.chest.eclipse;

import net.community.chest.io.dom.AbstractIOTransformer;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 26, 2009 2:11:30 PM
 */
public abstract class AbstractEclipseFileTransformer extends AbstractIOTransformer {
	protected AbstractEclipseFileTransformer ()
	{
		super();
	}
	/**
	 * Output property name to be used for {@link #setOutputProperty(String, String)}
	 * call in order to determine whether the written {@link org.w3c.dom.Element}-s names
	 * are checked if they conform to the Eclipse classpath standard (default=yes/true) 
	 */
	public static final String	VALIDATE_ELEMENT_NAME="x-validate-element-name";
	public boolean isValidatingElementName ()
	{
		return isTrueProperty(getOutputProperty(VALIDATE_ELEMENT_NAME));
	}
	/**
	 * Output property name to be used for {@link #setOutputProperty(String, String)}
	 * call in order to determine whether the written output
	 * entries are written in some lexicographical order (default=yes/true) 
	 */
	public static final String	SORT_ENTRIES_ELEMENTS="x-sort-entries";
	public boolean isSortedOutputEntries ()
	{
		return isTrueProperty(getOutputProperty(SORT_ENTRIES_ELEMENTS));
	}

}
