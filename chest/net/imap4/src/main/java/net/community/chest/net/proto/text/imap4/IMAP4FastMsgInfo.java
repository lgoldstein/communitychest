package net.community.chest.net.proto.text.imap4;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;

import net.community.chest.CoVariantReturn;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.math.NumberTables;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 20, 2007 10:38:57 AM
 */
public class IMAP4FastMsgInfo implements Serializable, PubliclyCloneable<IMAP4FastMsgInfo> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8482414026221400633L;
	/**
	 * Message sequential number - non-positive if not set
	 */
	private int	_seqNo	/* =0 */;
	public int getSeqNo ()
	{
		return _seqNo;
	}

	public void setSeqNo (int seqNo)
	{
		_seqNo = seqNo;
	}
	/**
	 * Message UID - may be <=0L if not set (e.g., not a UID FETCH command)
	 */
	private long	_uid	/* =0L */;
	public long getUid ()
	{
		return _uid;
	}

	public void setUid (long uid)
	{
		_uid = uid;
	}
	/**
	 * Message size (bytes) - non-positive if not set 
	 */
	private long	_size	/* =0L */;
	public long getSize ()
	{
		return _size;
	}

	public void setSize (long size)
	{
		_size = size;
	}
	/**
	 * Message flags - may be null/empty
	 */
	private Collection<IMAP4MessageFlag> 	_flags	/* =null */;
	public Collection<IMAP4MessageFlag> getFlags ()
	{
		return _flags;
	}
	/**
	 * @param f flag to be added - ignored if null
	 * @return updated flags list - may be null/empty if no previous flags
	 * and none added
	 */
	public Collection<IMAP4MessageFlag> addFlag (final IMAP4MessageFlag f)
	{
		if (f != null)
		{
			if (null == _flags)
				_flags = new LinkedList<IMAP4MessageFlag>();
			_flags.add(f);
		}

		return getFlags();
	}

	public void setFlags (Collection<IMAP4MessageFlag> flags)
	{
		_flags = flags;
	}
	/**
	 * Received date/time - null if not set 
	 */
	private Calendar	_iDate	/* =null */;
	public Calendar getInternalDate ()
	{
		return _iDate;
	}

	public void setInternalDate (Calendar iDate)
	{
		_iDate = iDate;
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@CoVariantReturn
	public IMAP4FastMsgInfo clone () throws CloneNotSupportedException
	{
		final IMAP4FastMsgInfo				iVal=getClass().cast(super.clone());
		final Collection<IMAP4MessageFlag>	oFlags=getFlags();
		if (oFlags != null)
		{
			final Collection<IMAP4MessageFlag>	iFlags=new LinkedList<IMAP4MessageFlag>();
			for (final IMAP4MessageFlag f : oFlags)
			{
				if (f != null)
					iFlags.add(f.clone());
			}

			iVal.setFlags(iFlags);
		}

		final Calendar	oDate=getInternalDate();
		if (oDate != null)
			iVal.setInternalDate((Calendar) oDate.clone());

		return iVal;
	}

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if ((null == obj) || (!(obj instanceof IMAP4FastMsgInfo)))
			return false;
		if (this == obj)
			return true;

		final IMAP4FastMsgInfo	oInfo=(IMAP4FastMsgInfo) obj;
		if ((getSeqNo() != oInfo.getSeqNo())
		 || (getUid() != oInfo.getUid())
		 || (getSize() != oInfo.getSeqNo()))
			return false;

		{
			final Calendar	iDate=getInternalDate(), oDate=oInfo.getInternalDate();
			if (null == iDate)
				return (null == oDate);
			else if (null == oDate)
				return false;
			if (iDate.compareTo(oDate) != 0)
				return false;
		}

		return IMAP4FlagValue.compareFlags(IMAP4FlagValue.buildFlagsMap(getFlags()), IMAP4FlagValue.buildFlagsMap(oInfo.getFlags()));
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return getSeqNo()
			 + NumberTables.getLongValueHashCode(getSize())
			 + NumberTables.getLongValueHashCode(getUid())
			 + IMAP4FlagValue.calculateFlagsHashCode(getFlags())
			 ;
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		final StringBuilder	sb=new StringBuilder(128);

		sb.append(IMAP4Protocol.IMAP4_PARLIST_SDELIM);

		final int	sbStart=sb.length();
		{
			if (sb.length() > sbStart)
				sb.append(' ');	// distance from previous modifier

			sb.append(IMAP4FetchModifier.IMAP4_FLAGSChars);
			sb.append(' ');

			final Collection<? extends IMAP4MessageFlag>	flags=getFlags();
			final int										numFlags=(null == flags) ? 0 : flags.size();
			final IMAP4MessageFlag[]						fa=(numFlags <= 0) ? null : flags.toArray(new IMAP4MessageFlag[numFlags]);
			try
			{
				IMAP4FlagValue.appendFlagsList(sb, fa);
			}
			catch(IOException e)
			{
				// should not happen
			}
		}

		{
			if (sb.length() > sbStart)
				sb.append(' ');	// distance from previous modifier

			sb.append(IMAP4FetchModifier.IMAP4_INTERNALDATEChars);
			sb.append(' ');

			sb.append('"');

			final Calendar	iDate=getInternalDate();
			if (null == iDate)
				sb.append(IMAP4Protocol.IMAP4_NILChars);
			else
				IMAP4Protocol.encodeInternalDate(sb, iDate);  
			sb.append('"');
		}

		{
			if (sb.length() > sbStart)
				sb.append(' ');	// distance from previous modifier

			sb.append(IMAP4FetchModifier.IMAP4_RFC822SIZEChars);
			sb.append(' ');
			sb.append(getSize());
		}

		{
			final long	uid=getUid();
			if (uid > 0L)
			{
				if (sb.length() > sbStart)
					sb.append(' ');	// distance from previous modifier

				sb.append(IMAP4FetchModifier.IMAP4_UIDChars);
				sb.append(' ');
				sb.append(uid);
			}
		}

		sb.append(IMAP4Protocol.IMAP4_PARLIST_EDELIM);

		return sb.toString();
	}
}
