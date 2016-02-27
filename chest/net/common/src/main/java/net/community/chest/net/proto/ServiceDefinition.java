/*
 * 
 */
package net.community.chest.net.proto;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import net.community.chest.CoVariantReturn;
import net.community.chest.ParsableString;
import net.community.chest.io.EOLStyle;
import net.community.chest.io.input.TokensReader;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.StringUtil;
import net.community.chest.lang.math.NumberTables;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * <P>Describes one entry of a &quot;services&quot; file.</P>
 * 
 * @author Lyor G.
 * @since Nov 12, 2009 10:18:29 AM
 */
public class ServiceDefinition implements Serializable,
										  PubliclyCloneable<ServiceDefinition>,
										  Comparable<ServiceDefinition> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2840743684198655097L;
	public ServiceDefinition ()
	{
		super();
	}

	private String	_name;
	public String getName ()
	{
		return _name;
	}

	public void setName (String name)
	{
		_name = name;
	}

	private int	_port;
	public int getPort ()
	{
		return _port;
	}

	public void setPort (int port)
	{
		_port = port;
	}

	private String	_protocol;
	public String getProtocol ()
	{
		return _protocol;
	}

	public void setProtocol (String protocol)
	{
		_protocol = protocol;
	}

	private Set<String>	_aliases;
	public Set<String> getAliases ()
	{
		return _aliases;
	}

	public void setAliases (Set<String> aliases)
	{
		_aliases = aliases;
	}
	// returns TRUE if alias added
	public boolean addAlias (String a)
	{
		if ((null == a) || (a.length() <= 0))
			return false;

		Set<String>	al=getAliases();
		if (null == al)
		{
			setAliases(new TreeSet<String>(String.CASE_INSENSITIVE_ORDER));
			if (null == (al=getAliases()))
				throw new IllegalStateException("addAlias(" + a + ") no set available though created");
		}

		return al.add(a);
	}

	private String	_description;
	public String getDescription ()
	{
		return _description;
	}

	public void setDescription (String description)
	{
		_description = description;
	}

	public void clear ()
	{
		setName(null);
		setPort(0);
		setProtocol(null);
		setAliases(null);
		setDescription(null);
	}
	// NOTE: clears the value before setting its values
	public static final <D extends ServiceDefinition> D fromString (CharSequence s, D def)
		throws IllegalArgumentException
	{
		if ((null == def) || (null == s) || (s.length() <= 0))
			return def;

		final ParsableString	ps=new ParsableString(s);
		final int				startPos=ps.getStartIndex(), endPos=ps.getMaxIndex();
		int						curPos=ps.findNonEmptyDataStart();
		if ((curPos < startPos) || (curPos >= endPos))
			throw new IllegalArgumentException("fromString(" + s + ") missing service name");

		int	nextPos=ps.findNonEmptyDataEnd(curPos);
		if (nextPos <= curPos)
			throw new IllegalArgumentException("fromString(" + s + ") null/empty service name");

		final String	n=ps.substring(curPos, nextPos);
		if (n.charAt(0) == '#')	// ignore if comment
			return def;

		if ((curPos=ps.findNonEmptyDataStart(nextPos+1)) <= nextPos)
			throw new IllegalArgumentException("fromString(" + s + ") missing service port/protocol");
		if ((nextPos=ps.findNonEmptyDataEnd(curPos+1)) <= curPos)
			throw new IllegalArgumentException("fromString(" + s + ") null/empty service port/protocol");

		final String	pp=ps.substring(curPos, nextPos);
		final int		ppLen=(null == pp) ? 0 : pp.length(),
						sepPos=(ppLen <= 2) /* at least 1/a */ ? (-1) : pp.indexOf('/');
		if ((sepPos <= 0) || (sepPos >= (ppLen - 1)))
			throw new IllegalArgumentException("fromString(" + s + ") malformed service port/protocol");

		final String	pn=pp.substring(0, sepPos),
						pv=pp.substring(sepPos + 1);
		final Boolean	pt=NumberTables.checkNumericalValue(pn);
		if (!Boolean.TRUE.equals(pt))
			throw new IllegalArgumentException("fromString(" + s + ") malformed service port: " + pn);

		final int		p=Integer.parseInt(pn);
		if ((p < 0) || (p >= 65535))
			throw new IllegalArgumentException("fromString(" + s + ") bad service port value: " + pn);

		def.clear();
		def.setName(n);
		def.setPort(p);
		def.setProtocol(pv);

		curPos = ps.findNonEmptyDataStart(nextPos);
		while ((curPos > nextPos) && (curPos < endPos))
		{
			nextPos = ps.findNonEmptyDataEnd(curPos);

			final String	v=ps.substring(curPos, nextPos);
			// if comment detected then no more data to parse
			if ('#' == v.charAt(0))
			{
				final String	d=
					(curPos < (endPos-1)) ? ps.substring(curPos + 1).trim() : null;
				def.setDescription(d);
				break;
			}

			def.addAlias(v);
			curPos = ps.findNonEmptyDataStart(nextPos);
		}

		return def;
 	}

	public ServiceDefinition (String s) throws IllegalArgumentException
	{
		final Object o=fromString(s, this);
		if (o != this)
			throw new IllegalArgumentException("Mismatched 'fromString' values");
	}

	public static final Collection<ServiceDefinition> readServices (final Reader in)
		throws IOException
	{
		final StringBuilder				workBuf=new StringBuilder(80);
		Collection<ServiceDefinition>	ret=null;
		for (EOLStyle	eos=TokensReader.appendLine(workBuf, in);
			 ;
			 workBuf.setLength(0), eos=TokensReader.appendLine(workBuf, in))
		{
			final int		dLen=workBuf.length(),
							dPos=
				(dLen <= 0) ? (-1) : ParsableString.findNonEmptyDataStart(workBuf);
			if ((dPos >= 0) && (workBuf.charAt(dPos) != '#'))
			{
				try
				{
					final ServiceDefinition	def=new ServiceDefinition(workBuf.toString());
					if (null == ret)
						ret = new LinkedList<ServiceDefinition>();
					ret.add(def);
				}
				catch(IllegalArgumentException e)
				{
					throw new StreamCorruptedException(e.getMessage());
				}
			}

			if (null == eos)
				break;
		}

		return ret;
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@CoVariantReturn
	public ServiceDefinition clone () throws CloneNotSupportedException
	{
		final ServiceDefinition	ret=getClass().cast(super.clone());
		final Set<String>		al=ret.getAliases();
		if (al != null)
			ret.setAliases(new HashSet<String>(al));
		return ret;
	}
	/* Note: checks port followed by protocol
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo (ServiceDefinition o)
	{
		if (null == o)
			return (-1);
		if (this == o)
			return 0;

		final int	p1=getPort(), p2=o.getPort(), nRes=p1 - p2;
		if (nRes != 0)
			return nRes;
		else
			return StringUtil.compareDataStrings(getProtocol(), o.getProtocol(), false);
	}
	/* Note: checks only port and protocol
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (!(obj instanceof ServiceDefinition))
			return false;
		if (this == obj)
			return true;

		return (0 == compareTo((ServiceDefinition) obj));
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return getPort() + StringUtil.getDataStringHashCode(getProtocol(), false);
	}
	/* Returns a line formatted according to the services file format
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		final String				n=getName(),
									p=getProtocol(),
									d=getDescription();
		final Collection<String>	al=getAliases();
		final int					nLen=(null == n) ? 0 : n.length(),
									pLen=(null == p) ? 0 : p.length(),
									dLen=(null == d) ? 0 : d.length(),
								    aNum=(null == al) ? 0 : al.size(),
								    aLen=(aNum <= 0) ? 0 : aNum * 8,
								    tLen=nLen + pLen + dLen + aLen + 16;
		final StringBuilder			sb=new StringBuilder(Math.max(tLen,16))
											.append(n)
											.append('\t')
											.append(getPort())
											.append('/')
											.append(p)
											;
		if (aNum > 0)
		{
			final int	sLen=sb.length();
			for (final String a : al)
			{
				if ((null == a) || (a.length() <= 0))
					continue;
				// separate 1st alias with TAB from previous data
				if (sb.length() >= sLen)
					sb.append(' ');
				else
					sb.append('\t');
				sb.append(a);
			}
		}

		if (dLen > 0)
			sb.append("\t # ").append(d);
		return sb.toString();
	}
}
