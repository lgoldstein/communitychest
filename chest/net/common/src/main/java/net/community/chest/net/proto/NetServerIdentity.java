package net.community.chest.net.proto;

import java.io.Serializable;

import net.community.chest.CoVariantReturn;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.StringUtil;
import net.community.chest.net.NetUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Used to hold a server's identity</P>
 * 
 * @author Lyor G.
 * @since Oct 25, 2007 8:25:12 AM
 */
public class NetServerIdentity implements Serializable, PubliclyCloneable<NetServerIdentity>, Comparable<NetServerIdentity> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7259076217907606296L;
	public NetServerIdentity ()
	{
		super();
	}
	/**
	 * Host name to which this information refers - may be null/empty, or
	 * even same as the host address (Note: not validated in any way...)
	 */
	private String	_hostName;
	public String getHostName ()
	{
		return _hostName;
	}

	public void setHostName (String hostName)
	{
		_hostName = hostName;
	}
	/**
	 * Host address to which this information refers - may be null/empty
	 * (Note: not validated in any way...)
	 */
	private String	_hostAddress	/* =null */;
	public String getHostAddress ()
	{
		return _hostAddress;
	}

	public void setHostAddress (String hostAddress)
	{
		_hostAddress = hostAddress;
	}
	/**
	 * Port through which this information was retrieved
	 */
	private int	_port	/* =0 */;
	public int getPort ()
	{
		return _port;
	}

	public void setPort (int port)
	{
		_port = port;
	}
	/**
	 * server type string (may be NULL/empty)
	 */
	private String _type	/* =null */;
	public String getType ()
	{
		return _type;
	}

	public void setType (String type)
	{
		_type = type;
	}
	/**
	 * Server version string (may be NULL/empty)
	 */
	private String _version	/* =null */;
	public String getVersion ()
	{
		return _version;
	}

	public void setVersion (String version)
	{
		_version = version;
	}
	/**
	 * Network protocol to which this identity refers 
	 */
	private String	_proto	/* =null */;
	public String getProtocol ()
	{
		return _proto;
	}

	public void setProtocol (String proto)
	{
		_proto = proto;
	}
	/**
	 * Initializes contents to empty values
	 */
	public void reset ()
	{
		setHostName(null);
		setHostAddress(null);
		setType(null);
		setVersion(null);
		setProtocol(null);
	}
	/* NOTE: checks only version and type
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo (NetServerIdentity o)
	{
		if (this == o)
			return 0;

		int	nRes=StringUtil.compareDataStrings(getType(), o.getType(), false);
		if (0 == nRes)
			nRes = StringUtil.compareDataStrings(getVersion(), o.getVersion(), true);

		return nRes;
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@CoVariantReturn
	public NetServerIdentity clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}

	public boolean isSameIdentity (final NetServerIdentity o)
	{
		if (o == null)
			return false;
		if (this == o)
			return true;

		return (0 == StringUtil.compareDataStrings(getHostName(), o.getHostName(), false))
			&& (0 == StringUtil.compareDataStrings(getHostAddress(), o.getHostAddress(), false))
			&& (0 == StringUtil.compareDataStrings(getProtocol(), o.getProtocol(), false))
			&& (0 == compareTo(o))
			&& (0 == NetUtil.comparePorts(getPort(), o.getPort()))
			;
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		final Class<?>	oc=(obj == null) ? null : obj.getClass();
		if (oc != getClass())
			return false;
		if (this == obj)
			return true;

		return isSameIdentity((NetServerIdentity) obj);
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return StringUtil.getDataStringHashCode(getHostName(), false)
			 + StringUtil.getDataStringHashCode(getHostAddress(), false)
			 + StringUtil.getDataStringHashCode(getType(), false)
			 + StringUtil.getDataStringHashCode(getVersion(), true)
			 + StringUtil.getDataStringHashCode(getProtocol(), false)
			 + NetUtil.getPortHashCode(getPort())
			 ;
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		return getProtocol() + "://" + getHostName() + "@" + getHostAddress() + "(" + getType() + ")[" + getVersion() + "]";
	}
}
