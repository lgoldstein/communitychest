package net.community.chest.net.proto.text;

import net.community.chest.CoVariantReturn;
import net.community.chest.net.proto.NetServerIdentity;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 25, 2007 8:33:44 AM
 */
public class TextProtocolNetServerIdentity extends NetServerIdentity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2349473903300207144L;
	public TextProtocolNetServerIdentity ()
	{
		super();
	}

	private NetServerWelcomeLine	_wl;
	public NetServerWelcomeLine getWelcomeLine ()
	{
		return _wl;
	}

	public String getWelcomeLineText ()
	{
		final NetServerWelcomeLine	wl=getWelcomeLine();
		if (null == wl)
			return null;
		else
			return wl.getLine();
	}

	public void setWelcomeLine (NetServerWelcomeLine wl)
	{
		_wl = wl;
	}

	public void setWelcomeLineText (String text)
	{
		final NetServerWelcomeLine	wl=getWelcomeLine();
		if (null == wl)
		{
			if ((text != null) && (text.length() > 0))
				setWelcomeLine(new NetServerWelcomeLine(text));
		}
		else
			wl.setLine(text);
	}
	/*
	 * @see net.community.chest.net.proto.NetServerIdentity#clone()
	 */
	@Override
	@CoVariantReturn
	public TextProtocolNetServerIdentity clone () throws CloneNotSupportedException
	{
		final TextProtocolNetServerIdentity	tsi=getClass().cast(super.clone());
		final NetServerWelcomeLine			wl=getWelcomeLine();
		if (wl != null)
			tsi.setWelcomeLine(wl.clone());

		return tsi;
	}
	/*
	 * @see net.community.chest.net.proto.NetServerIdentity#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		final Class<?>	oc=(obj == null) ? null : obj.getClass();
		if (oc != getClass())
			return false;
		if (this == obj)
			return true;

		final TextProtocolNetServerIdentity	tsi=(TextProtocolNetServerIdentity) obj;
		if (!isSameIdentity(tsi))
			return false;

		final NetServerWelcomeLine	w1=getWelcomeLine(), w2=tsi.getWelcomeLine();
		if (!AbstractComparator.compareObjects(w1, w2))
			return false;

		return true;
	}
	/*
	 * @see net.community.chest.net.proto.NetServerIdentity#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		final NetServerWelcomeLine	wl=getWelcomeLine();
		return super.hashCode() + ((null == wl) ? 0 : wl.hashCode());
	}
	/*
	 * @see net.community.chest.net.proto.NetServerIdentity#reset()
	 */
	@Override
	public void reset ()
	{
		super.reset();
		setWelcomeLine(null);
	}
	/*
	 * @see net.community.chest.net.proto.NetServerIdentity#toString()
	 */
	@Override
	public String toString ()
	{
		return super.toString() + ": " + getWelcomeLineText();
	}
}
