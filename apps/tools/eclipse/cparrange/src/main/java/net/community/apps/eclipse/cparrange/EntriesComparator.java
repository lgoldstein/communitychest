/**
 * 
 */
package net.community.apps.eclipse.cparrange;

import org.w3c.dom.Element;

import net.community.chest.eclipse.classpath.ClasspathEntryComparator;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 12, 2008 10:30:47 AM
 */
public class EntriesComparator extends ClasspathEntryComparator<Element> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1886329864552213537L;
	private final LoggerWrapper	_logger;
	public EntriesComparator ()
	{
		super(Element.class, true);
		_logger = WrapperFactoryManager.getLogger(getClass());
	}
	/*
	 * @see net.community.chest.eclipse.classpath.ClasspathEntryComparator#comparePathComponent(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public int comparePathComponent (final String p1, final String p2, final String c)
	{
		final int	nRes=super.comparePathComponent(p1, p2, c);
		if (_logger.isDebugEnabled())
			_logger.debug("comparePathComponent(" + p1 + "/" + p2 + ")[" + c + "]: " + nRes);
		return nRes;
	}

	public static final EntriesComparator	DEFAULT=new EntriesComparator();
}
