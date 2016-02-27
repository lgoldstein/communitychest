/*
 * 
 */
package net.community.chest.eclipse.wst;

import org.w3c.dom.Element;

import net.community.chest.dom.ElementDataComparator;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 26, 2009 2:14:29 PM
 */
public class WstDepModuleEntryComparator extends ElementDataComparator<Element> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6064461652561604504L;

	public WstDepModuleEntryComparator (boolean ascending)
	{
		super(Element.class, ascending, false);
	}

	public static final int compareHandles (String h1, String h2)
	{
		final int		l1=(null == h1) ? 0 : h1.length(),
						p1=(l1 > 0) ? h1.lastIndexOf('/') : 0,
						l2=(null == h2) ? 0 : h2.length(),
						p2=(l2 > 0) ? h2.lastIndexOf('/') : 0;
		final String	n1=((p1 > 0) && (p1 < (l1-1))) ? h1.substring(p1+1) : h1,
						n2=((p2 > 0) && (p2 < (l2-1))) ? h2.substring(p2+1) : h2;
		return StringUtil.compareDataStrings(n1, n2, false);
	}

	public static final int compareHandles (Element e1, Element e2)
	{
		final String	h1=(null == e1) ? null : e1.getAttribute(WstUtils.DEPMODULE_HANDLE_ATTR),
						h2=(null == e2) ? null : e2.getAttribute(WstUtils.DEPMODULE_HANDLE_ATTR);
		return compareHandles(h1, h2);
	}
 	/*
	 * @see net.community.chest.dom.ElementDataComparator#compareValues(org.w3c.dom.Element, org.w3c.dom.Element)
	 */
	@Override
	public int compareValues (Element e1, Element e2)
	{
		final String	t1=(null == e1) ? null : e1.getTagName(),
						t2=(null == e2) ? null : e2.getTagName();
		if (WstUtils.DEPMODULE_ELEM_NAME.equalsIgnoreCase(t1)
		 && WstUtils.DEPMODULE_ELEM_NAME.equalsIgnoreCase(t2))
			return compareHandles(e1, e2);

		return super.compareValues(e1, e2);
	}

	public static final WstDepModuleEntryComparator	ASCENDING=new WstDepModuleEntryComparator(true),
													DESCENDING=new WstDepModuleEntryComparator(false);
}
