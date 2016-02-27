package net.community.chest.net.proto.text.imap4;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import net.community.chest.ParsableString;
import net.community.chest.io.encode.base64.Base64;
import net.community.chest.lang.StringUtil;
import net.community.chest.net.TextNetConnection;
import net.community.chest.util.datetime.DateUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>This class holds various IMAP4 protocol related defintions (see also RFC2060)</P>
 * 
 * @author Lyor G.
 * @since Sep 20, 2007 9:15:47 AM
 */
public final class IMAP4Protocol {
	private IMAP4Protocol ()
	{
		// disable any instanciation or derivation
	}
	/**
	 * Default IMAP4 listen port
	 */
	public static final int IPPORT_IMAP4=143;
	/**
	 * Default auto-logout timeout (as defined by RFC2060)
	 */
	public static final int IMAP4_DEFAULT_AUTOLOGOUT_MINUTES=30;
	/* NOTE: These limits are NOT defined by the RFC but are empirically (!) good enough */
	public static final int MAX_IMAP4_TAG_LEN=36;
	public static final int MAX_IMAP4_OPCODE_LEN=24;
	public static final int MAX_IMAP4_CMD_LEN=516;
	public static final int MAX_IMAP4_DATA_LEN=1028;
	/* Some keywords of interest that appear in responses */
	public static final String IMAP4ReferralBracketKwd="REFERRAL";
	public static final String IMAP4ReadOnlyBracketKwd="READ-ONLY";
	public static final String IMAP4ReadWriteBracketKwd="READ-WRITE";
	/* IMAP4 commands */
	public static final String IMAP4LoginCmd="LOGIN";
		public static final char[] IMAP4LoginCmdChars=IMAP4LoginCmd.toCharArray();
	public static final String IMAP4LogoutCmd="LOGOUT";
		public static final char[] IMAP4LogoutCmdChars=IMAP4LogoutCmd.toCharArray();
	public static final String IMAP4FetchCmd="FETCH";
		public static final char[] IMAP4FetchCmdChars=IMAP4FetchCmd.toCharArray();
	public static final String IMAP4CapabilityCmd="CAPABILITY";
		public static final char[] IMAP4CapabilityCmdChars=IMAP4CapabilityCmd.toCharArray();
	public static final String IMAP4NoopCmd="NOOP";
		public static final char[] IMAP4NoopCmdChars=IMAP4NoopCmd.toCharArray();
	public static final String IMAP4SelectCmd="SELECT";
		public static final char[] IMAP4SelectCmdChars=IMAP4SelectCmd.toCharArray();
	public static final String IMAP4ListCmd="LIST";
		public static final char[] IMAP4ListCmdChars=IMAP4ListCmd.toCharArray();
	public static final String IMAP4LSUBCmd="LSUB";
		public static final char[] IMAP4LSUBCmdChars=IMAP4LSUBCmd.toCharArray();
	public static final String IMAP4AuthCmd="AUTHENTICATE";
		public static final char[] IMAP4AuthCmdChars=IMAP4AuthCmd.toCharArray();
	public static final String IMAP4ExamineCmd="EXAMINE";
		public static final char[] IMAP4ExamineCmdChars=IMAP4ExamineCmd.toCharArray();
	public static final String IMAP4CreateCmd="CREATE";
		public static final char[] IMAP4CreateCmdChars=IMAP4CreateCmd.toCharArray();
	public static final String IMAP4DeleteCmd="DELETE";
		public static final char[] IMAP4DeleteCmdChars=IMAP4DeleteCmd.toCharArray();
	public static final String IMAP4RenameCmd="RENAME";
		public static final char[] IMAP4RenameCmdChars=IMAP4RenameCmd.toCharArray();
	public static final String IMAP4SubsCmd="SUBSCRIBE";
	public static final String IMAP4UnSubsCmd="UNSUBSCRIBE";
	public static final String IMAP4StatusCmd="STATUS";
		public static final char[] IMAP4StatusCmdChars=IMAP4StatusCmd.toCharArray();
	public static final String IMAP4AppendCmd="APPEND";
		public static final char[] IMAP4AppendCmdChars=IMAP4AppendCmd.toCharArray();
	public static final String IMAP4CheckCmd="CHECK";
	public static final String IMAP4CloseCmd="CLOSE";
		public static final char[] IMAP4CloseCmdChars=IMAP4CloseCmd.toCharArray();
	public static final String IMAP4XpngCmd="EXPUNGE";
		public static final char[] IMAP4XpngCmdChars=IMAP4XpngCmd.toCharArray();
	public static final String IMAP4SearchCmd="SEARCH";
		public static final char[] IMAP4SearchCmdChars=IMAP4SearchCmd.toCharArray();
	public static final String IMAP4StoreCmd="STORE";
		public static final char[] IMAP4StoreCmdChars=IMAP4StoreCmd.toCharArray();
	public static final String IMAP4CopyCmd="COPY";
		public static final char[] IMAP4CopyCmdChars=IMAP4CopyCmd.toCharArray();
		/* known strings */
	public static final String IMAP4_NIL="NIL";
		public static final char[] IMAP4_NILChars=IMAP4_NIL.toCharArray();
	public static final String IMAP4_BYE="BYE";
	public static final String IMAP4_SILENT=".SILENT";
		public static final char[] IMAP4_SILENTChars=IMAP4_SILENT.toCharArray();
	    /* SEARCH keywords */
	public static final String IMAP4AllSearchKwd="ALL";
	public static final String IMAP4AnsweredSearchKwd="ANSWERED";
	public static final String IMAP4BccSearchKwd="BCC";
	public static final String IMAP4BeforeSearchKwd="BEFORE";
	public static final String IMAP4BodySearchKwd="BODY";
	public static final String IMAP4CcSearchKwd="CC";
	public static final String IMAP4DeletedSearchKwd="DELETED";
	public static final String IMAP4DraftSearchKwd="DRAFT";
	public static final String IMAP4FlaggedSearchKwd="FLAGGED";
	public static final String IMAP4FromSearchKwd="FROM";
	public static final String IMAP4HeaderSearchKwd="HEADER";
		public static final char[] IMAP4HeaderSearchKwdChars=IMAP4HeaderSearchKwd.toCharArray();
	public static final String IMAP4KeywordSearchKwd="KEYWORD";
	public static final String IMAP4LargerSearchKwd="LARGER";
	public static final String IMAP4NewSearchKwd="NEW";
	public static final String IMAP4NoteSearchKwd="NOT";
	public static final String IMAP4OldSearchKwd="OLD";
	public static final String IMAP4OnSearchKwd="ON";
	public static final String IMAP4OrSearchKwd="OR";
	public static final String IMAP4SeenSearchKwd="SEEN";
	public static final String IMAP4SentBeforeSearchKwd="SENTBEFORE";
	public static final String IMAP4SentOnSearchKwd="SENTON";
	public static final String IMAP4SentSinceSearchKwd="SENTSINCE";
	public static final String IMAP4SinceSearchKwd="SINCE";
	public static final String IMAP4SmallerSearchKwd="SMALLER";
	public static final String IMAP4SubjectSearchKwd="SUBJECT";
	public static final String IMAP4TextSearchKwd="TEXT";
	public static final String IMAP4ToSearchKwd="TO";
	public static final String IMAP4UnAnsweredSearchKwd="UNASWERED";
	public static final String IMAP4UnDeletedSearchKwd="UNDELETED";
	public static final String IMAP4UnDraftSearchKwd="UNDRAFT";
	public static final String IMAP4UnFlaggedSearchKwd="UNFLAGGED";
	public static final String IMAP4UnKeywordSearchKwd="UNKEYWORD";
	public static final String IMAP4UnSeenSearchKwd="UNSEEN";
	/* some delimiters */
	public static final char IMAP4_CONTINUE_RSP='+';
	public static final char IMAP4_UNTAGGED_RSP='*';
	/* octet count delimiter(s) */
	public static final char IMAP4_OCTCNT_SDELIM='{';
	public static final char IMAP4_OCTCNT_EDELIM='}';
	/* LITERAL+ character */
	public static final char IMAP4_LITPLUS_CHAR='+';
	public static final char IMAP4_LISTWILDCARD='%';
	/* parenthesized list delimiter(s) */
	public static final char IMAP4_PARLIST_SDELIM='(';
	public static final char IMAP4_PARLIST_EDELIM=')';
	/* bracketed response delimiter(s) */
	public static final char IMAP4_BRCKT_SDELIM	='[';
	public static final char IMAP4_BRCKT_EDELIM	=']';
	/* quoted string(s) delimiter */
	public static final char IMAP4_QUOTE_DELIM	='\"';
	public static final char IMAP4_AMPERSAND_DELIM='&';
	/* message ranges delimiters */
	public static final char IMAP4_MSGRANGE_DELIM=':';
	public static final char IMAP4_MSGLIST_DELIM=',';
	public static final char IMAP4_MSGRANGE_WILDCARD='*';
    /* body part offset specification */
	public static final char IMAP4_OFFSET_SDELIM='<';
	public static final char IMAP4_OFFSET_EDELIM='>';

	public static final char IMAP4_NAMESPACE_DELIM='#';
	public static final char IMAP4_BODYPART_DELIM='.';
	public static final char IMAP4_RCVDATE_DELIM='-';
	public static final char IMAP4_RCVTIME_DELIM=DateUtil.DEFAULT_TMSEP;
		/* some known extensions/capabilities */
	public static final String IMAP4GetQuotaCmd="GETQUOTA";
	public static final String IMAP4GetQuotaRootCmd="GETQUOTAROOT";
		public static final char[] IMAP4GetQuotaRootCmdChars=IMAP4GetQuotaRootCmd.toCharArray();
	public static final String IMAP4SetQuotaCmd="SETQUOTA";
	public static final String IMAP4QuotaRsp="QUOTA";
		public static final char[] IMAP4QuotaRspChars=IMAP4QuotaRsp.toCharArray();
	public static final String IMAP4QuotaRootRsp="QUOTAROOT";
		public static final char[] IMAP4QuotaRootRspChars=IMAP4QuotaRootRsp.toCharArray();
	public static final String IMAP4QuotaStorageRes="STORAGE";
	public static final String IMAP4QuotaMessageRes="MESSAGE";

	public static final String IMAP4NamespaceCmd="NAMESPACE";
		public static final char[] IMAP4NamespaceCmdChars=IMAP4NamespaceCmd.toCharArray();

	public static final String IMAP4IdleCmd="IDLE";
	public static final String IMAP4DoneCmd="DONE";
	/**
	 * Translates the folder path to use the specified hierarchy separator
	 * @param folder folder path to be prepared
	 * @param orgSep separator used in the string to separate path components
	 * @param hierSep separator expected by the server as separator
	 * @return string where separators have been replaced
	 * @see IMAP4Accessor#list(String ref, String mbox)
	 * @see IMAP4FoldersListInfo
	 * @throws IllegalStateException if BOTH the original AND the hierarchy separators appear in the folder string
	 */
	public static final String prepareFolder (String folder, char orgSep, char hierSep)
	{
		// if null/empty folder, or same character used as separator, then return same as input
		if ((null == folder) || (folder.length() <= 0) || (orgSep == hierSep))
			return folder;

		// if neither the original separator appears, nor the hierarchy one, then nothing to do
		int nOrgIndex=folder.indexOf(orgSep), nHierIndex=folder.indexOf(hierSep);
		if (((-1) == nOrgIndex) && ((-1) == nHierIndex))
			return folder;

		// if BOTH the original AND the hierarchy separators appear, then error since simple replacement will yield
		// bad results - as if the adjusted folder has more components
		if ((nOrgIndex != (-1)) && ((nHierIndex != (-1))))
			throw new IllegalStateException("Ambiguous folder path separators in folder=" + folder);

		return folder.replace(orgSep, hierSep);
	}
	/**
	 * Adjusts a folder name/path to conform to RFC2060 requirements - especially non-ASCII names
	 * @param folder folder name/path to be adjusted
	 * @param orgSep separator used in the string to separate path components
	 * @param hierSep separator expected by the server as separator
	 * @param wb work buffer - if null, then function will allocate its own
	 * @return adjusted string (which may be same as input if no changes required)
	 * @throws IllegalStateException if problems encountered
	 */
	public static final String adjustFolderName (String folder, char orgSep, char hierSep, StringBuilder wb)
	{
		final String  prepFldr=prepareFolder(folder, orgSep, hierSep);
		final int     prepLen=(null == prepFldr) ? 0 : prepFldr.length();
		if (prepLen <= 0)
			return prepFldr;

		int				lastPos=0;
		StringBuilder   sb=null;    // will be allocated if necessary
		for (int    nPos=0; nPos < prepLen; nPos++)
		{
			char    c=prepFldr.charAt(nPos);

			// replace '&' with "&-"
			if (IMAP4_AMPERSAND_DELIM == c)
			{
				if (null == sb)
				{
					if (null == (sb=wb))
						sb = new StringBuilder(prepLen);
					else
						sb.setLength(0);
				}

				// add whatever "clear" text we have so far - including the '&' itself
				final String	clrText=prepFldr.substring(lastPos, nPos+1);
				sb.append(clrText);
				sb.append('-');
				lastPos = (nPos + 1);
				continue;
			}

			// skip "clear" characters
			if ((c >= 0x0020) && (c <= 0x007e))
				continue;

			if (null == sb)
			{
				if (null == (sb=wb))
					sb = new StringBuilder(prepLen);
				else
					sb.setLength(0);
			}

			// add whatever "clear" text we have so far - except for the "offending" character
			if (lastPos < nPos)
			{
				final String	clrText=prepFldr.substring(lastPos, nPos);
				sb.append(clrText);
			}

			// mark the "shift-in"
			sb.append(IMAP4_AMPERSAND_DELIM);

			// find end of block that requires (modified) BASE64 encoding
			for (lastPos = nPos; nPos < prepLen; nPos++)
			{
				c = prepFldr.charAt(nPos);
				if ((c >= 0x0020) && (c <= 0x007e))
					break;
			}

			// in modified UTF7 the MSB comes first, so if this is a little endian system we need to transpose the values
			final String  str2Enc=prepFldr.substring(lastPos, nPos);
			final char[]  chars2Enc=str2Enc.toCharArray();
			final byte[]  buf2Enc=new byte[2 * chars2Enc.length];

	        // encode each character into 2 bytes - MSB first
			for (int cPos=0, bPos=0; cPos < chars2Enc.length; cPos++, bPos += 2)
			{
				c = chars2Enc[cPos];
				buf2Enc[bPos] = (byte) ((c >> 8) & 0x00FF);
				buf2Enc[bPos + 1] = (byte) (c & 0x00FF);
			}
			final byte[]  encBytes=Base64.encode(buf2Enc);

			// adjust encoded bytes
			int     encLen=0;
			for (encLen = 0; encLen < encBytes.length; encLen++)
			{
				// remove any padding at end of encoding
				if (Base64.BASE64_PAD_CHAR == encBytes[encLen])
					break;
				// replace '/' with ',' as required by modified BASE64
				if ('/' == encBytes[encLen])
					encBytes[encLen] = ',';
			}

			try
			{
				final String ascString=new String(encBytes, 0, encLen, "US-ASCII");
				sb.append(ascString);
			}
			catch (UnsupportedEncodingException e)
			{
				throw new IllegalStateException("US-ASCII xlate N/A");  // should not happen
			}

			// add "shift-out" sign
			sb.append('-');

			lastPos = nPos;
			nPos--; // compensate for the automatic ++ when looking for UNICODE end
		}

		// if did not need to allocate the string buffer, then no changes were required
		if (null == sb)
			return prepFldr;

		// check if have any "clear" part left (at this stage a string buffer MUST have been allocated)
		if (lastPos < prepLen)
		{
			final String	clrText=prepFldr.substring(lastPos, prepLen);
			sb.append(clrText);
		}

		return sb.toString();
	}
	/**
	 * Adjusts a folder name/path to conform to RFC2060 requirements - especially non-ASCII names
	 * @param folder folder name/path to be adjusted
	 * @param orgSep separator used in the string to separate path components
	 * @param hierSep separator expected by the server as separator
	 * @return adjusted string (which may be same as input if no changes required)
	 * @throws IllegalStateException if problems encountered
	 */
	public static final String adjustFolderName (String folder, char orgSep, char hierSep)
	{
		return adjustFolderName(folder, orgSep, hierSep, null);
	}
	/**
	 * Padding to be used - index=string length MODULU 4
	 */
	private static final String[]	strBase64Pad={ "", "===", "==", "=" };
	/**
	 * Adjusts a folder name/path that conforms to RFC2060 requirements to a (Java) string
	 * @param folder folder name/path to be adjusted
	 * @param orgSep separator to be used in the string to separate path components
	 * @param hierSep separator used the server in path components
	 * @param wb work buffer - if null, then function will allocate its own
	 * @return adjusted string (which may be same as input if no changes required)
	 * @throws IllegalStateException if problems encountered
	 */
	public static final String restoreFolderName (String folder, char orgSep, char hierSep, StringBuilder wb)
	{
		final String	 prepFldr=prepareFolder(folder, hierSep, orgSep);
		final int		prepLen=(null == prepFldr) ? 0 : prepFldr.length();
		if (prepLen <= 0)
			return prepFldr;

		int				lastPos=0;
		StringBuilder   sb=null;    // will be allocated if necessary
		for (int    nPos=0; nPos < prepLen; nPos++)
		{
			final char	c=prepFldr.charAt(nPos);
			// skip "clear" characters
			if ((c >= 0x0020) && (c <= 0x007e))
			{
				if (c != IMAP4_AMPERSAND_DELIM)
					continue;
			}
			else	// we expect only printable ASCII characters
				throw new IllegalStateException("un-encoded non-ASCII characters");
			
			if (null == sb)
			{
				if (null == (sb=wb))
					sb = new StringBuilder(prepLen);
				else
					sb.setLength(0);
			}

			if ((nPos + 1) >= prepLen)
				throw new IllegalStateException("No continuation for shift-in sign");
			nPos++;	// skip the '&'

			// a "shift-out" sign MUST exist - either as a BASE64 delimiter or a '&' stand-in
			final int	nextPos=prepFldr.indexOf('-', nPos);
			if (nextPos < nPos)
				throw new IllegalStateException("No shift-out sign");
					
			if (nextPos == nPos)	// no BASE64 - just a '&' stand-in
			{
				// include the '&' in the appended substring
				final String	clrText=prepFldr.substring(lastPos, nPos);

				sb.append(clrText);
				lastPos = (nPos + 1); // skip the '-'
				continue;
			}
			
			if (lastPos < nPos)	// exclude the '&' from the appended text
			{
				final String	clrText=prepFldr.substring(lastPos, nPos-1);
				sb.append(clrText);
			}

			// replace ',' back with '/' as required by modified BASE64 + pad it to closest multiple of 4
			final int		decMod4=((nextPos - nPos) & 0x03);
			final String	strDec=prepFldr.substring(nPos, nextPos).replace(',', '/') + strBase64Pad[decMod4];
			try
			{
				final byte[]	decBytes=Base64.decodeToBytes(strDec);
				// we expect "wide" UNICODE characters - each 2 bytes long
				if ((decBytes.length & 0x01) != 0)
					throw new IllegalStateException("Non UNICODE-2 characters");

				// in modified UTF7 the MSB comes first, so if this is a little endian system we need to transpose the values
				final char[]	decChars=new char[decBytes.length / 2];
				for (int	chIndex=0, bIndex=0; chIndex < decChars.length; chIndex++, bIndex += 2)
				{
					final short	hiByte=decBytes[bIndex], loByte=decBytes[bIndex+1],
					wchVal=(short) (((hiByte << 8) & 0xFF00) | (loByte & 0x00FF)); 
					decChars[chIndex] =(char) (wchVal & 0x0000FFFF);
				}
				sb.append(decChars);
			}
			catch(IOException ioe)
			{
				// thrown by BASE64 decode attempt
				throw new IllegalStateException("Bad/Illegal BASE64 data in folder name=" + strDec);
			}

			nPos = nextPos;	// continue from the "shift-out" (skipped by the automatic loop ++)
			lastPos = (nPos + 1);
		}

		// if did not need to allocate the string buffer, then no changes were required
		if (null == sb)
			return prepFldr;

		// check if have any "clear" part left (at this stage a string buffer MUST have been allocated)
		if (lastPos < prepLen)
		{	
			final String	clrText=prepFldr.substring(lastPos, prepLen);
			sb.append(clrText);
		}

		return sb.toString();
	}
	/**
	 * Adjusts a folder name/path that conforms to RFC2060 requirements to a (Java) string
	 * @param folder folder name/path to be adjusted
	 * @param orgSep separator to be used in the string to separate path components
	 * @param hierSep separator used the server in path components
	 * @return adjusted string (which may be same as input if no changes required)
	 * @throws IllegalStateException if problems encountered
	 */
	public static final String restoreFolderName (String folder, char orgSep, char hierSep)
	{
		return restoreFolderName(folder, orgSep, hierSep, null);
	}
	/**
	 * Checks the given response if it contains a referral
	 * @param refRsp response received from LOGIN attempt (usually) to be cheked for referrals
	 * @return referral value (or null/empty if none found)
	 */
	public static final String getReferral (String refRsp)
	{
		/* format: tag OK/NO/BAD [REFERRAL imap://<user>;AUTH=*@<server>/] */
		if (null == refRsp)
			return null;

		// find first bracket
		int brcktPos=refRsp.indexOf(IMAP4_BRCKT_SDELIM);
		if ((-1) == brcktPos)
			return null;
		brcktPos++;

		// find keyword and make sure it is "REFERRAL"
		int kwdPos=refRsp.indexOf(' ', brcktPos);
		if ((-1) == kwdPos)
			return null;
		// make sure found keyword length matches what we seek
        if ((kwdPos - brcktPos) != IMAP4ReferralBracketKwd.length())
            return null;
		// make sure this is the REFERRAL keyword
		if (!refRsp.regionMatches(true, brcktPos, IMAP4ReferralBracketKwd, 0, IMAP4ReferralBracketKwd.length()))
			return null;

		// find referral server
		int refPos=refRsp.indexOf('@', (kwdPos+1));
		if ((-1) == refPos)
			return null;
		refPos++;

		// find end of server name/address
		int refEnd=refRsp.indexOf('/', refPos);
		if ((-1) == refEnd)
		{
			if ((-1) == (refEnd=refRsp.indexOf(IMAP4_BRCKT_EDELIM)))
				return null;
		}

		return refRsp.substring(refPos, refEnd-1);
	}
    /**
     * Builds the prefix of the command - including the tag value and optional UID modifier
     * @param sb string buffer into which to build the command - if already
     * contains some text then it is cleaned before appending
     * @param tagValue tag value to be used
     * @param cmd command characters
     * @param isUID if TRUE, then precedes the command by a UID modifier
     * @param moreToFollow if TRUE, then command has more arguments, and a ' ' is appended
     * @return same as input argument
     */
	public static final StringBuilder buildCmdPrefix (final StringBuilder	sb,
													  final int				tagValue,
													  final char[]			cmd,
													  final boolean			isUID,
													  final boolean			moreToFollow)
	{
		if ((null == sb) || (null == cmd) || (cmd.length <= 0))
			return sb;

		if (sb.length() > 0)
			sb.setLength(0);

		sb.append(tagValue)
			.append(' ')
			;
	    if (isUID)
		    sb.append(IMAP4FetchModifier.IMAP4_UIDChars)
		      .append(' ')
		      ;

		sb.append(cmd);
		if (moreToFollow)
			sb.append(' ');

		return sb;
	}
	/**
	 * Builds the prefix of a command that refers to a message range
	 * @param sb string buffer into which to build the command
	 * @param tagValue tag value to be used
	 * @param cmd command characters
	 * @param msgRange range of messages to be added
	 * @param isUID if TRUE, then precedes the command by a UID modifier
	 * @return same as input argument
	 */
	public static final StringBuilder buildMsgRangeCmdPrefix (StringBuilder sb, int tagValue, char[] cmd, char[] msgRange, boolean isUID)
	{
		final int rangeLen=(null == msgRange) ? 0 : msgRange.length;
		if (rangeLen <= 0)
	        return sb;

		IMAP4Protocol.buildCmdPrefix(sb, tagValue, cmd, isUID, true)
					 .append(msgRange)
					 .append(' ')
					 ;

		return sb;
	}
	/**
	 * Builds the prefix of a command that refers to a message range
	 * @param sb string buffer into which to build the command
	 * @param tagValue tag value to be used
	 * @param cmd command characters
	 * @param msgRange range of messages to be added
	 * @param isUID if TRUE, then precedes the command by a UID modifier
	 * @return same as input
	 */
	public static final StringBuilder buildMsgRangeCmdPrefix (StringBuilder sb, int tagValue, char[] cmd, String msgRange, boolean isUID)
	{
		return buildMsgRangeCmdPrefix(sb, tagValue, cmd, (null == msgRange) ? null : msgRange.toCharArray(), isUID);
	}
    /**
     * Adds a literal count to the string buffer
     * @param sb string buffer to append to
     * @param litLen number of octets in the literal (may NOT be negative)
	 * @return same as input
     */
	public static final StringBuilder addLiteralCount (StringBuilder sb, long litLen)
	{
	    if ((null == sb) || (litLen < 0))
	        return sb;

		sb.append(IMAP4_OCTCNT_SDELIM)
		  .append(litLen)
		  .append(IMAP4_OCTCNT_EDELIM)
		  ;

		return sb;
	}
	/**
	 * Adds the (optional) APPEND command arguments
     * @param sb string buffer to append to
     * @param iDate INTERNALDATE value to be associated - formatted according to RFC2060 requirements (may be NULL/empty)
     * @param flags flags values to be set for the data - formatted acccording to RFC2060 requirements (may be NULL/empty)
     * @param dataSize (exactly) expected total data size (in bytes)
	 * @return same as input
	 */
	public static final StringBuilder addAppendCmdArgs (StringBuilder sb, String iDate, String flags, long dataSize)
	{
		// add (optional) flags
		if ((flags != null) && (flags.length() > 0))
			sb.append(' ')
			  .append(flags)
			  ;

		// add (optional) date
		if ((iDate != null) && (iDate.length() > 0))
			sb.append(' ')
			  .append(iDate)
			  ;

		sb.append(' ');
		addLiteralCount(sb, dataSize);
		
		return sb;
	}
	/**
	 * INTERNALDATE months values
	 */
	private static final char[][]	iDateMonths={
		"Jan".toCharArray(),
		"Feb".toCharArray(),
		"Mar".toCharArray(),
		"Apr".toCharArray(),
		"May".toCharArray(),
		"Jun".toCharArray(),
		"Jul".toCharArray(),
		"Aug".toCharArray(),
		"Sep".toCharArray(),
		"Oct".toCharArray(),
		"Nov".toCharArray(),
		"Dec".toCharArray()
	};
	/**
	 * Builds/appends an RFC2060 INTERNALDATE value from the given date/time object 
	 * @param sb The {@link StringBuilder} instance to append to
	 * @param iDate date/time value to append (Note: its validity is not checked)
	 * @return Same {@link StringBuilder} instance as input
	 */
	public static final StringBuilder encodeInternalDate (final StringBuilder sb, final Calendar iDate)
	{
		if ((null == sb) || (null == iDate))
			return sb;

		try
		{
			StringUtil.appendPaddedNum(sb, (byte) iDate.get(Calendar.DAY_OF_MONTH), 2);
			sb.append(IMAP4Protocol.IMAP4_RCVDATE_DELIM);

			{
				final int	monthIndex=iDate.get(Calendar.MONTH) - Calendar.JANUARY;
				if ((monthIndex < 0) || (monthIndex >= iDateMonths.length))
					throw new IllegalArgumentException("Unknown month index: " + monthIndex);

				sb.append(iDateMonths[monthIndex])
				  .append(IMAP4Protocol.IMAP4_RCVDATE_DELIM)
				  ;
			}
		
			sb.append((short) iDate.get(Calendar.YEAR))
			  .append(' ')
			  ;

			StringUtil.appendPaddedNum(sb, (byte) iDate.get(Calendar.HOUR_OF_DAY), 2);
			sb.append(IMAP4Protocol.IMAP4_RCVTIME_DELIM);
			StringUtil.appendPaddedNum(sb, (byte) iDate.get(Calendar.MINUTE), 2);
			sb.append(IMAP4Protocol.IMAP4_RCVTIME_DELIM);
			StringUtil.appendPaddedNum(sb, (byte) iDate.get(Calendar.SECOND), 2);
			sb.append(' ');
		
			final TimeZone	tz=iDate.getTimeZone();
			int 			tzOffset=(null == tz) ? 0 : (tz.getRawOffset() / 1000);
			if ((tz != null) && tz.useDaylightTime() && tz.inDaylightTime(iDate.getTime()))
				tzOffset += tz.getDSTSavings();
			sb.append((tzOffset <= 0) ? '-' : '+');
		
			final int absOffset=(tzOffset < 0) ? (0 - tzOffset) : tzOffset, tzHours=(absOffset / 3600), tzMinutes=(absOffset % 3600);
			StringUtil.appendPaddedNum(sb, (byte) tzHours, 2);
			StringUtil.appendPaddedNum(sb, (byte) (tzMinutes / 60), 2);
		}
		catch(IOException e)	// should not happen
		{
			throw new RuntimeException(e);
		}

		return sb;
	}
	/**
	 * Builds/appends an RFC2060 INTERNALDATE value from the given date/time object 
	 * @param iDate date/time value to append (Note: its validity is not checked)
	 * @return INTERNALDATE string value (or null/empty if failed
	 */
	public static final String encodeInternalDate (final Calendar iDate)
	{
		if (null == iDate)
			return null;
		
		return encodeInternalDate(new StringBuilder(32), iDate).toString();
	}
	/**
	 * Extracts and sets the month value of an INTERNALDATE string - provided it is a STRING value (e.g. "Jan/Feb")
	 * @param ps parsable string to be checked
	 * @param curIndex starting position to hunt for the month name (inclusive)
	 * @param dtv date/time whose month is to be set
	 * @param fldIndex position in DTV array where month value is to be placed
	 * @return next position in INTERNALDATE string (or <0 if error)
	 */
	private static final int decodeInternalDateMonth (final ParsableString ps, final int curIndex, final int[] dtv, final int fldIndex)
	{
		final int	startIndex=ps.getStartIndex(), maxIndex=ps.getMaxIndex();
		int			curPos=Math.max(startIndex, curIndex);

		for ( ; curPos < maxIndex; curPos++)
			if (Character.isLetter(ps.getCharAt(curPos)))
				break;
	
		if (curPos >= maxIndex)
			return (-1);
		
		int	nextPos=curPos+1;
		for ( ; nextPos < maxIndex; nextPos++)
			if (!Character.isLetter(ps.getCharAt(nextPos)))
				break;

		for (int	mthIndex=0; mthIndex < iDateMonths.length; mthIndex++)
			if (ps.compareTo(curPos, nextPos, iDateMonths[mthIndex], false))
			{
				dtv[fldIndex] = mthIndex;
				return nextPos;
			}
	
		// This point is reached if no match found
		return (-2);
	}
	/**
	 * INTERNALDATE encoded fields order
	 */
	private static final int	iDateEncFields[]={
		Calendar.DAY_OF_MONTH,
		Calendar.MONTH,
		Calendar.YEAR,
		Calendar.HOUR,
		Calendar.MINUTE,
		Calendar.SECOND
	};
	/**
	 * Decodes an INTERNALDATE value into its components
	 * @param cs INTERNALDATE value (if null/empty then null/empty object returned)
	 * @return encoded date/time (or null if failed to decode)
	 * @throws IllegalStateException if ERA not "AD" (which should be NEVER...)
	 */
	public static final Calendar decodeInternalDate (CharSequence cs) throws IllegalStateException
	{
		final int	csLen=(null == cs) ? 0 : cs.length();
		if (csLen <= 0)
			return null;
		
		final ParsableString	ps=new ParsableString(cs);
		final int				startIndex=ps.getStartIndex(), maxIndex=ps.getMaxIndex();
		final int[]				dtv=new int[iDateEncFields.length];
		int						curPos=0;

		// update the "standard" fields (day, month, year, etc...)
		for (int	fldIndex=0; fldIndex < iDateEncFields.length; fldIndex++)
		{
			// month field requires special calculation
			final int	fieldId=iDateEncFields[fldIndex];
			if (Calendar.MONTH == fieldId)
			{
				final int	nextPos=decodeInternalDateMonth(ps, curPos, dtv, fldIndex);
				if (nextPos > curPos)
				{
					curPos = nextPos;
					continue;
				}
				
				// NOTE: if not a string value, then attempt a numerical month value (1-12)
			}

			if (((curPos=ps.findNumberStart(curPos)) < startIndex) || (curPos >= maxIndex))
				return null;
			
			final int	nextPos=ps.findNumberEnd(curPos+1);
			if ((nextPos <= curPos) || (nextPos >= maxIndex))
				return null;
			
			try
			{
				final int	numVal=ps.getUnsignedInt(curPos, nextPos);
				// month field requires special calculation
				if (Calendar.MONTH == fieldId)
					dtv[fldIndex] = numVal - 1;
				else
					dtv[fldIndex] = numVal;
			}
			catch(NumberFormatException nfe)
			{
				return null;	// should not happen
			}
			
			curPos = nextPos+1;
		}

		final Calendar	iDate=new GregorianCalendar(dtv[2], dtv[1] + Calendar.JANUARY, dtv[0], dtv[3], dtv[4], dtv[5]);
		iDate.set(Calendar.ERA, GregorianCalendar.AD);	// just making sure

		// NOTE: we look for the "+/-" of the GMT offset, but if not found, then be lenient (and assume "+/-0000")
		if (((curPos=ps.findNonEmptyDataStart(curPos)) <= startIndex) || (curPos >= maxIndex))
			return iDate;

		int		tzSign=0;
		char	chSize=ps.getCharAt(curPos);
		if ('+' == chSize)
		{	
			tzSign = 1;
			curPos++;
		}
		else if ('-' == chSize)
		{	
			tzSign = (-1);
			curPos++;
		}
		// NOTE: we allow omitting the '+/-' if digit straight away
		else if ((chSize >= '0') && (chSize <= '9'))
		{	
			tzSign = 1;
			chSize = '+';
		}
		else	// error - neither a sign nor a digit
			return null;
		
		int	tzHours=0, tzMinutes=0;
		try
		{
			tzHours = ps.getUnsignedInt(curPos, curPos+2);
			tzMinutes = ps.getUnsignedInt(curPos+2, curPos+4);
		}
		catch(NumberFormatException nfe)
		{
			return null;	// should not happen - we expect EXACTLY 4 digits
		}

		final int		absOffset=(tzHours * 3600) + (tzMinutes * 60), tzOffset=absOffset * tzSign;
		final TimeZone	tz=new SimpleTimeZone(tzOffset * 1000, "GMT" + ((chSize != '+') ? /* '-' is added automatically */ "" : "+") + tzHours);
		iDate.setTimeZone(tz);

		// this actually forces a re-calculation of the internal milliseconds value
		if (iDate.get(Calendar.ERA) != GregorianCalendar.AD)
			throw new IllegalStateException("ERA value mismatch: value=" + iDate.get(Calendar.ERA) + " expected=" + GregorianCalendar.AD);

		return iDate;
	}
    /**
     * Waits for the server to request more data
     * @param conn The {@link TextNetConnection} to wait for - if null or not
     * connected, then fails (no exception...)
     * @return true if successful
     * @throws IOException if error encountered
     */
	public static final boolean waitForServerContinuation (final TextNetConnection	conn) throws IOException
	{
		if ((null == conn) || (!conn.isOpen()))
			return false;

		/*      This is actually a FOREVER loop, but we do not expect ~32K responses (we expect to time-out before that).
		 * We use this to only to break infinite loops that may occur due to coding errors.
		 */
		for (int    rspIndex=0; rspIndex < Short.MAX_VALUE; rspIndex++)
		{
			final String  rspLine=conn.readLine();
			if ((null == rspLine) || (rspLine.length() <= 0))   // skip empty lines
				continue;

			final char	c=rspLine.charAt(0);
			if (IMAP4Protocol.IMAP4_UNTAGGED_RSP == c)
				continue;	// skip untagged response

			return (IMAP4Protocol.IMAP4_CONTINUE_RSP == c);
		}

		throw new IMAP4RspHandleException("Virtually infinite loop in waiting for continuation response");
	}
}
