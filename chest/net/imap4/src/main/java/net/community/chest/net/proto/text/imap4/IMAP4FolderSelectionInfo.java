package net.community.chest.net.proto.text.imap4;

import java.io.IOException;
import java.util.Collection;

import net.community.chest.net.TextNetConnection;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Information extracted from the SELECT/EXAMINE command</P>
 * 
 * @author Lyor G.
 * @since Sep 20, 2007 10:44:01 AM
 */
public class IMAP4FolderSelectionInfo extends IMAP4TaggedResponse {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3535069492293070827L;
	public IMAP4FolderSelectionInfo ()
	{
		super();
	}

	public IMAP4FolderSelectionInfo (IMAP4TaggedResponse rsp)
	{
		super(rsp);
	}
	/**
	 * current number of messages in selected folder
	 */
	private int _numExist	/* =0 */;
	public int getNumExist ()
	{
		return _numExist;
	}

	public void setNumExist (int numExist)
	{
		_numExist = numExist;
	}
	/**
	 * number of recent messages in selected folder
	 */
	private int _numRecent	/* =0 */;
	public int getNumRecent ()
	{
		return _numRecent;
	}

	public void setNumRecent (int numRecent)
	{
		_numRecent = numRecent;
	}
	/**
	 * first unseen message number (0 if unknown)
	 */
	private int _numUnseen	/* =0 */;
	public int getNumUnseen ()
	{
		return _numUnseen;
	}

	public void setNumUnseen (int numUnseen)
	{
		_numUnseen = numUnseen;
	}
	/**
	 * UIDVALIDITY assigned value (0 if unknown)
	 */
	private long _UIDValidity	/* =0L */;
	public long getUIDValidity ()
	{
		return _UIDValidity;
	}

	public void setUIDValidity (long validity)
	{
		_UIDValidity = validity;
	}
	/**
	 * supported/allowed flags
	 */
	private Collection<IMAP4MessageFlag>	_dynFlags	/* =null */;
	public Collection<IMAP4MessageFlag> getDynFlags ()
	{
		return _dynFlags;
	}

	public void setDynFlags (Collection<IMAP4MessageFlag> dynFlags)
	{
		_dynFlags = dynFlags;
	}
	/**
	 * flags that can be changed permanently
	 */
	private Collection<IMAP4MessageFlag>  _prmFlags	/* =null */;
	public Collection<IMAP4MessageFlag> getPrmFlags ()
	{
		return _prmFlags;
	}

	public void setPrmFlags (Collection<IMAP4MessageFlag> prmFlags)
	{
		_prmFlags = prmFlags;
	}
	/**
	 * next UID to be used for the next new message (0 if unknown)
	 */
	private long _UIDNext	/* =0 */;
	public long getUIDNext ()
	{
		return _UIDNext;
	}

	public void setUIDNext (long next)
	{
		_UIDNext = next;
	}
	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4TaggedResponse#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		final Class<?>	oc=(obj == null) ? null : obj.getClass();
		if (oc != getClass())
			return false;
		if (this == obj)
			return true;

		final IMAP4FolderSelectionInfo	selInfo=(IMAP4FolderSelectionInfo) obj;
		if (!isSameResponse(selInfo))
			return false;

		if ((getNumExist() != selInfo.getNumExist())
		 || (getNumRecent() != selInfo.getNumRecent())
		 || (getNumUnseen() != selInfo.getNumUnseen())
		 || (getUIDNext() != selInfo.getUIDNext())
		 || (getUIDValidity() != selInfo.getUIDValidity()))
		 	return false;

		return IMAP4FlagValue.compareFlags(IMAP4FlagValue.buildFlagsMap(getDynFlags()), IMAP4FlagValue.buildFlagsMap(selInfo.getDynFlags()))
			&& IMAP4FlagValue.compareFlags(IMAP4FlagValue.buildFlagsMap(getPrmFlags()), IMAP4FlagValue.buildFlagsMap(selInfo.getPrmFlags()))
			;
	}
	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4TaggedResponse#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return super.hashCode()
				+ getNumExist()
				+ getNumRecent()
				+ getNumUnseen()
				+ (int) (getUIDNext() & 0x7FFFFFFF)
				+ (int) (getUIDValidity() & 0x7FFFFFFF)
				+ IMAP4FlagValue.calculateFlagsHashCode(getDynFlags())
				+ IMAP4FlagValue.calculateFlagsHashCode(getPrmFlags())
			;
	}
	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4TaggedResponse#reset()
	 */
	@Override
	public void reset ()
	{
		super.reset();

		setNumExist(0);
		setNumRecent(0);
		setNumUnseen(0);
		setUIDNext(0L);
		setUIDValidity(0L);

		{
			final Collection<IMAP4MessageFlag>	df=getDynFlags();
			if (df != null)
				df.clear();
		}

		{
			final Collection<IMAP4MessageFlag>	pf=getPrmFlags();
			if (pf != null)
				pf.clear();
		}
	}
	/* some folder selection information modifiers */
	public static final String IMAP4_EXISTS="EXISTS";
		public static final char[] IMAP4_EXISTSChars=IMAP4_EXISTS.toCharArray();
	public static final String IMAP4_PERMANENTFLAGS="PERMANENTFLAGS";
		public static final char[] IMAP4_PERMANENTFLAGSChars=IMAP4_PERMANENTFLAGS.toCharArray();
	/**
	 * Extracts the information for a SELECT/EXAMINE command
	 * @param conn connection from which to read responses
	 * @param tagValue tag value assigned to indicate end of responses
	 * @return selection information
	 * @throws IOException if errors encountered during parsing
	 * @see IMAP4FolderSelectionInfo
	 */
	public static final IMAP4FolderSelectionInfo getFinalResponse (final TextNetConnection conn, final int tagValue) throws IOException
	{
		return (IMAP4FolderSelectionInfo) (new IMAP4FolderSelectionRspHandler(conn)).handleResponse(tagValue);
	}
}
