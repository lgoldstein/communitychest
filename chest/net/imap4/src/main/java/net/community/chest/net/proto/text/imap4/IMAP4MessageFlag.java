package net.community.chest.net.proto.text.imap4;

import java.io.IOException;
import java.io.UTFDataFormatException;
import java.util.ArrayList;
import java.util.Collection;

import net.community.chest.CoVariantReturn;
import net.community.chest.ParsableString;
import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 20, 2007 10:06:20 AM
 */
public class IMAP4MessageFlag extends IMAP4FlagValue {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6652628481930252955L;

	public IMAP4MessageFlag ()
	{
		super();
	}

	public IMAP4MessageFlag (String name) throws IllegalArgumentException
	{
		super(name);
	}

	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4FlagValue#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		final Class<?>	oc=(obj == null) ? null : obj.getClass();
		if (oc != getClass())
			return false;

		return isSameFlag((IMAP4MessageFlag) obj);
	}
	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4FlagValue#clone()
	 */
	@Override
	@CoVariantReturn
	public IMAP4MessageFlag clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}

	/* Known/standard messages flags */
	public static final String IMAP4_SEENFLAG=IMAP4_SYSFLAG_CHAR + "Seen";
		public static final char[] IMAP4_SEENFLAGChars=IMAP4_SEENFLAG.toCharArray();
	public static final String IMAP4_ANSWEREDFLAG=IMAP4_SYSFLAG_CHAR + "Answered";
		public static final char[] IMAP4_ANSWEREDFLAGChars=IMAP4_ANSWEREDFLAG.toCharArray();
	public static final String IMAP4_FLAGGEDFLAG=IMAP4_SYSFLAG_CHAR + "Flagged";
		public static final char[] IMAP4_FLAGGEDFLAGChars=IMAP4_FLAGGEDFLAG.toCharArray();
	public static final String IMAP4_DELETEDFLAG=IMAP4_SYSFLAG_CHAR + "Deleted";
		public static final char[] IMAP4_DELETEDFLAGChars=IMAP4_DELETEDFLAG.toCharArray();
	public static final String IMAP4_DRAFTFLAG=IMAP4_SYSFLAG_CHAR + "Draft";
		public static final char[] IMAP4_DRAFTFLAGChars=IMAP4_DRAFTFLAG.toCharArray();
	public static final String IMAP4_RECENTFLAG=IMAP4_SYSFLAG_CHAR + "Recent";
		public static final char[] IMAP4_RECENTFLAGChars=IMAP4_RECENTFLAG.toCharArray();

	public static final IMAP4MessageFlag    SEEN=new IMAP4MessageFlag(IMAP4_SEENFLAG);
	public static final IMAP4MessageFlag    ANSWERED=new IMAP4MessageFlag(IMAP4_ANSWEREDFLAG);
	public static final IMAP4MessageFlag    FLAGGED=new IMAP4MessageFlag(IMAP4_FLAGGEDFLAG);
	public static final IMAP4MessageFlag    DELETED=new IMAP4MessageFlag(IMAP4_DELETEDFLAG);
	public static final IMAP4MessageFlag    DRAFT=new IMAP4MessageFlag(IMAP4_DRAFTFLAG);
	public static final IMAP4MessageFlag    RECENT=new IMAP4MessageFlag(IMAP4_RECENTFLAG);

	public boolean isSeen ()
	{
		return SEEN.equals(this);
	}

	public boolean isDraft ()
	{
		return DRAFT.equals(this);
	}

	public boolean isDeleted ()
	{
		return DELETED.equals(this);
	}

	public boolean isRecent ()
	{
		return RECENT.equals(this);
	}

	public boolean isFlagged ()
	{
		return FLAGGED.equals(this);
	}

	public boolean isAnswered ()
	{
		return ANSWERED.equals(this);
	}

		/* Special value used to signal that private flags are allowed */
	public static final String IMAP4_PRIVATEFLAGS=IMAP4_SYSFLAG_CHAR + "*";
		/* Note: isSystemFlag method for this object is TRUE */
	public static final IMAP4MessageFlag    PRIVATE=new IMAP4MessageFlag(IMAP4_PRIVATEFLAGS);
	/**
	 * Returns a list of flags values that may appear in the parsable string (Note: NIL is allowed)
	 * @param ps parsable string to be checked for flags values
	 * @param startIndex flags data start index
	 * @return flags array - may be NULL if "NIL" or "()" flags list found
	 * @throws UTFDataFormatException if unable to parse correctly
	 */
	public static final Collection<IMAP4MessageFlag> getMessageFlags (ParsableString ps, int startIndex)
	    throws UTFDataFormatException
	{
	    final Collection<String>   vals=getFlags(ps, startIndex);
		final int					numFlags=(null == vals) ? 0 : vals.size();
		if (numFlags <= 0)   // OK if no flags strings returned (maybe empty list)
		   return null;

		final Collection<IMAP4MessageFlag>	flags=new ArrayList<IMAP4MessageFlag>(numFlags);
		for (final String	v : vals)
		{
			if ((null == v) || (v.length() <= 0))
				continue;	// should not happen
			flags.add(new IMAP4MessageFlag(v));
		}

		return flags;
	}
	/**
	 * Builds the FLAGS argument for a STORE command
	 * @param <A> The {@link Appendable} instance to append to
	 * @param sb string buffer to which to append the FLAGS argument 
	 * @param flags flags to be added/removed/set (may be NULL/empty)
	 * @param addRemoveSet if >0 then flags are added, <0 removed, and =0 set
	 * @return same as input {@link Appendable} instance
	 * @throws IOException If failed to append (or no {@link Appendable} instance provided)
	 */
	public static final <A extends Appendable> A appendStoreFlags (A sb, IMAP4MessageFlag[] flags, int addRemoveSet) throws IOException
	{
		if (null == sb)
			throw new IOException(ClassUtil.getArgumentsExceptionLocation(IMAP4MessageFlag.class, "appendStoreFlags", Integer.valueOf(addRemoveSet)) + " no " + Appendable.class.getName() + " instance provided");

		if (addRemoveSet > 0)
			sb.append('+');
		else if (addRemoveSet < 0)
			sb.append('-');

		sb.append(IMAP4FetchModifier.IMAP4_FLAGS)
		  .append(IMAP4Protocol.IMAP4_SILENT)
		  .append(' ')
		  ;

		return appendFlagsList(sb, flags);
	}
}
