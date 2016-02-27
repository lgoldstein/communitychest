/*
 * 
 */
package net.community.chest.javaagent.dumper.filter;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.StringUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 11, 2011 2:21:29 PM
 */
public class PatternClassFilter implements XmlConvertibleClassFilter, Serializable, Comparable<PatternClassFilter> {
	private static final long serialVersionUID = 4941044513207052023L;

	public PatternClassFilter ()
	{
		this((String) null);
	}

	public PatternClassFilter (String pattern) throws PatternSyntaxException
	{
		setPattern(pattern);
	}

	public PatternClassFilter (Element root) throws Exception
	{
		final PatternClassFilter	filter=fromXml(root);
		if (this != filter)
			throw new IllegalStateException("Mismatched re-constructed instances");
	}

	private String	_pattern;
	private Pattern	_matcher;
	public String getPattern ()
	{
		return _pattern;
	}

	public void setPattern (String pattern) throws PatternSyntaxException
	{
		if ((_pattern=pattern) != null)
			_matcher = Pattern.compile(convertToRegexp(pattern));
		else
			_matcher = null;
	}
	/*
	 * @see net.community.chest.javaagent.dumper.ClassFilter#accept(java.lang.String)
	 */
	@Override
	public boolean accept (String className)
	{
		if ((className == null) || (className.length() <= 0))
			return false;

		final Matcher	m=(_matcher == null) ? null : _matcher.matcher(className);
		if ((m != null)&& m.matches())
			return true;
		else
			return false;
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return StringUtil.getDataStringHashCode(getPattern(), true);
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof PatternClassFilter))
			return false;
		if (compareTo((PatternClassFilter) obj) != 0)
			return false;	// debug breakpoint

		return true;
	}
	/*
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo (PatternClassFilter o)
	{
		if (o == null)
			return (-1);
		if (this == o)
			return 0;

		final int	nRes=StringUtil.compareDataStrings(getPattern(), o.getPattern(), true);
		if (nRes != 0)
			return nRes;	// debug breakpoint

		return 0;
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		return getPattern();
	}

	public static final String	PATTERN_ELEMENT="pattern", VALUE_ATTR="value";
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (Document doc) throws Exception
	{
		return DOMUtils.addNonEmptyAttribute(doc.createElement(PATTERN_ELEMENT), VALUE_ATTR, getPattern());
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public PatternClassFilter fromXml (Element root) throws Exception
	{
		setPattern(root);
		return this;
	}

	protected String setPattern (Element elem) throws PatternSyntaxException
	{
		final String pattern=elem.getAttribute(VALUE_ATTR);
		if (pattern != null)
			setPattern(pattern);
		return pattern;
	}

	public static final String convertToRegexp (final String pattern) throws PatternSyntaxException
	{
		final int	pLen=(pattern == null) ? 0 : pattern.length();
		if (pLen <= 0)
			throw new PatternSyntaxException("No pattern provided", pattern, (-1));

		{
			final int	dblPos=pattern.indexOf("**");
			if (dblPos >= 0)
				throw new PatternSyntaxException("Successive wildcards not allowed", pattern, dblPos);
		}

		{
			final int	dblPos=pattern.indexOf("..");
			if (dblPos >= 0)
				throw new PatternSyntaxException("Successive dots not allowed", pattern, dblPos);
		}

		if (".*".equals(pattern))
			return pattern;

		final int	dotPos=pattern.lastIndexOf('.');
		if (dotPos < 0)
			throw new PatternSyntaxException("No package specified", pattern, (-1));
		if (dotPos >= (pLen - 1))
			throw new PatternSyntaxException("No class pattern specified", pattern, dotPos);

		final String	pkgPattern=pattern.substring(0, dotPos + 1),	// including the dot
						pkgRegexp,
						classPattern=pattern.substring(dotPos + 1),		// excluding the dot
						classRegexp=classPattern.replaceAll("\\*", "\\.*");
		if (classPattern.charAt(0) == '*')
		{
			if (dotPos == 0)
				pkgRegexp = "";
			else
				pkgRegexp = pkgPattern.replaceAll("\\.", "\\\\.");
		}
		else
			pkgRegexp = pkgPattern.replaceAll("\\.", "\\\\.") + ".*";

		return pkgRegexp + classRegexp;
	}
}
