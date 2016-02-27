package net.community.chest.net.proto.text.imap4.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;

import net.community.chest.mail.address.MessageAddressType;
import net.community.chest.mail.headers.RFCHeaderDefinitions;
import net.community.chest.net.proto.text.NetServerWelcomeLine;
import net.community.chest.net.proto.text.imap4.IMAP4Accessor;
import net.community.chest.net.proto.text.imap4.IMAP4Capabilities;
import net.community.chest.net.proto.text.imap4.IMAP4FastMsgInfo;
import net.community.chest.net.proto.text.imap4.IMAP4FastResponse;
import net.community.chest.net.proto.text.imap4.IMAP4FetchModifier;
import net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler;
import net.community.chest.net.proto.text.imap4.IMAP4FolderFlag;
import net.community.chest.net.proto.text.imap4.IMAP4FolderInfo;
import net.community.chest.net.proto.text.imap4.IMAP4FolderSelectionInfo;
import net.community.chest.net.proto.text.imap4.IMAP4FoldersListInfo;
import net.community.chest.net.proto.text.imap4.IMAP4MessageFlag;
import net.community.chest.net.proto.text.imap4.IMAP4Namespace;
import net.community.chest.net.proto.text.imap4.IMAP4NamespacesInfo;
import net.community.chest.net.proto.text.imap4.IMAP4Protocol;
import net.community.chest.net.proto.text.imap4.IMAP4QuotarootInfo;
import net.community.chest.net.proto.text.imap4.IMAP4ServerIdentityAnalyzer;
import net.community.chest.net.proto.text.imap4.IMAP4Session;
import net.community.chest.net.proto.text.imap4.IMAP4StatusInfo;
import net.community.chest.net.proto.text.imap4.IMAP4TaggedResponse;
import net.community.chest.test.TestBase;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 14, 2008 12:19:33 PM
 */
public class IMAP4Tester extends TestBase {

	public static class TestIMAP4FetchRspHandler implements IMAP4FetchResponseHandler {
		private final PrintStream	_out /* =null */;
		private int					_numMsgs /* =0 */;

		protected TestIMAP4FetchRspHandler (PrintStream out)
		{
			_out=out;
		}

		public int getProcessedMsgs ()
		{
			return _numMsgs;
		}
		/*
		 * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleMsgResponseState(int, boolean)
		 */
		@Override
		public int handleMsgResponseState (int msgSeqNo, boolean fStarting)
		{
			if (_out != null)
				_out.println('\t' + IMAP4Protocol.IMAP4FetchCmd + " " + (fStarting ? "start" : "end") + " " + msgSeqNo);
			if (fStarting)
				_numMsgs++;
			return 0;
		}
		/*
		 * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleUID(int, long)
		 */
		@Override
		public int handleUID (int msgSeqNo, long msgUID)
		{
			if (_out != null)
				_out.println("\t\t" + msgSeqNo + ":\t" + IMAP4FetchModifier.IMAP4_UID + "=" + msgUID);
			return 0;
		}

		private int numIndent=0;
		private String getMsgPartDisplay (String msgPart)
		{
			final int         	partLen=(null == msgPart) ? 0 : msgPart.length();
			final StringBuilder	sb=new StringBuilder(numIndent + 2 * partLen + 2);
			for (int    index=0; index < numIndent; index++)
				sb.append('\t');
			for (int    index=0; index < partLen; index += 2)
				sb.append('\t');
			if (partLen != 0)
				sb.append(msgPart);
			else
				sb.append("???");

			return sb.toString();
		}
		/*
		 * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleMsgPartStage(int, java.lang.String, boolean)
		 */
		@Override
		public int handleMsgPartStage (int msgSeqNo, String msgPart, boolean fStarting)
		{
			if (!fStarting)
				numIndent--;

			if (_out != null)
				_out.println("\t\t" + msgSeqNo + ":\t" + "Part=" + getMsgPartDisplay(msgPart) + " " + (fStarting ? "start" : "end"));

			if (fStarting)
				numIndent++;

			return 0;
		}
		/*
		 * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleMsgPartHeader(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public int handleMsgPartHeader (int msgSeqNo, String msgPart, String hdrName, String attrName, String attrValue)
		{
			if (_out != null)
			{	
				String  p1="\t\t" + msgSeqNo + ":\tPart=" + getMsgPartDisplay(msgPart) + " " + hdrName + ' ';
				boolean emptyAttrName=(null == attrName) || (0 == attrName.length());
				String  p2=(emptyAttrName ? "" : ('\t' + attrName)), p4=null;
				boolean emptyAttrValue=(null == attrValue) || (0 == attrValue.length());
				if (!emptyAttrValue)
				{	
					final String	realValue=RFCHeaderDefinitions.decodeHdrValue(attrValue, true);
					if (!realValue.equals(attrValue))
						p4 = realValue;	
				}
				String  p3="";
				if (emptyAttrName)
					p3 = emptyAttrValue ? "" : attrValue;
				else if (!emptyAttrValue)
					p3 = "=\"" + attrValue + '\"';

				if (p4 != null)
					_out.println(p1 + p2 + p3 + "[" + p4 + "]");
				else
					_out.println(p1 + p2 + p3);
			}
			return 0;
		}
		/*
		 * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleMsgPartAddress(int, java.lang.String, net.community.chest.mail.address.MessageAddressType, java.lang.String, java.lang.String)
		 */
		@Override
		public int handleMsgPartAddress (int msgSeqNo, String msgPart, MessageAddressType addrType, String dispName, String addrVal)
		{
			if (_out != null)
			{
				final String	msgPartDisp=getMsgPartDisplay(msgPart);
				_out.println("\t\t" + msgSeqNo + ":\tPart=" + msgPartDisp + ":\ttype=" + addrType.toString()); 
				_out.println("\t\t\t\t\tName: " + ((null == dispName) ? "NONE" : dispName));
				_out.println("\t\t\t\t\tAddress: " + addrVal);
			}
			
			return 0;
		}
		/*
		 * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleFlagsStage(int, boolean)
		 */
		@Override
		public int handleFlagsStage (int msgSeqNo, boolean fStarting)
		{
			if (_out != null)
				_out.println("\t\t" + msgSeqNo + ":\t" + IMAP4FetchModifier.IMAP4_FLAGS + " " + (fStarting ? "start" : "end"));
			return 0;
		}
		/*
		 * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleFlagValue(int, java.lang.String)
		 */
		@Override
		public int handleFlagValue (int msgSeqNo, String flagValue)
		{
			if (_out != null)
				_out.println("\t\t" + msgSeqNo + ":\t\t" + flagValue);
			return 0;
		}
		/*
		 * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleInternalDate(int, java.lang.String)
		 */
		@Override
		public int handleInternalDate (int msgSeqNo, String dateValue)
		{
			if (_out != null)
				_out.println("\t\t" + msgSeqNo + ":\t" + IMAP4FetchModifier.IMAP4_INTERNALDATE + "=" + dateValue);
			return 0;
		}
		/*
		 * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleMsgPartSize(int, java.lang.String, long)
		 */
		@Override
		public int handleMsgPartSize (int msgSeqNo, String msgPart, long partSize)
		{
			if (_out != null)
				_out.println("\t\t" + msgSeqNo + ":\tPart=" + getMsgPartDisplay(msgPart) + " [" + IMAP4FetchModifier.IMAP4_RFC822SIZE + "]=" + partSize);
			return 0;
		}
		/*
		 * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handlePartDataStage(int, java.lang.String, boolean)
		 */
		@Override
		public int handlePartDataStage (int msgSeqNo, String msgPart, boolean fStarting)
		{
			if (_out != null)
				_out.println("\t\t" + msgSeqNo + ":\t" + "Data Part=" + getMsgPartDisplay(msgPart) + " " + (fStarting ? "start" : "end"));
			return 0;
		}
		/*
		 * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handlePartData(int, java.lang.String, byte[], int, int)
		 */
		@Override
		public int handlePartData (int msgSeqNo, String msgPart, byte[] bData, int nOffset, int nLen)
		{
			if (_out != null)
			{	
				int ctlsCount=0;

				// run through the data buffer to make sure it is accessible, and count the non-printable characters num
				for (int    index=0, offset=nOffset; index < nLen; index++, offset++)
					if ((bData[offset] < 0x20) || (bData[offset] > 0x7E))
						ctlsCount++;

				_out.println("\t\t" + msgSeqNo + ":\t\t" + "Data Part=" + getMsgPartDisplay(msgPart) + " len=" + nLen + " (CTLs=" + ctlsCount + ")");
			}

			return 0;
		}
	}

	private static final void showFlags (final PrintStream out, final String flagsType, final Collection<? extends IMAP4MessageFlag> flags)
	{
		out.print("\t" + flagsType + " (");
		if ((flags != null) && (flags.size() > 0))
		{
			for (final IMAP4MessageFlag f : flags)
				out.print(" " + f);
		}
		out.println(" )");
	}

	private static final void showAllFolders (final PrintStream out, final IMAP4Accessor sess) throws IOException

	{
		final long					listStart=System.currentTimeMillis();
		final IMAP4FoldersListInfo  rsp=sess.listAllFolders();
		final long					listEnd=System.currentTimeMillis(), listDuration=(listEnd - listStart);
		if (rsp.isOKResponse())
		{
			final Collection<IMAP4FolderInfo>	fldrs=rsp.getFolders();
			final int							numFldrs=(null == fldrs) ? 0 : fldrs.size();
			out.println(IMAP4Protocol.IMAP4ListCmd);

			if (numFldrs > 0)
			{
				for (final IMAP4FolderInfo f : fldrs)
				{
					if (f != null)
					{	
						char[] c={ f.getHierarchySeparator() };
						out.println("\t" + f.toString() + " sep=" + (('\0' == c[0]) ? "NIL" : String.valueOf(c)));
					}

					final Collection<IMAP4FolderFlag>   flags=(null == f) ? null : f.getFlags();
					final int                 			numFlags=(null == flags) ? 0 : flags.size();
					if (numFlags > 0)
					{
						for (final IMAP4FolderFlag ff : flags)
						{
							if (ff != null)
								out.println("\t\t" + ff);
						}
					}
				}
			}
				
			out.println(rsp + " (" + numFldrs + " folders in " + listDuration + " msec.)");
		}
		else
			System.err.println("Failed to LIST (after " + listDuration + " msec.) : " + rsp);
	}

	private static final int testIMAP4FolderAccess (final PrintStream out, final BufferedReader in, final String folder, final IMAP4Accessor sess) throws IOException
	{
		final TestIMAP4FetchRspHandler	hndlr=new TestIMAP4FetchRspHandler(out);
		for ( ; ; )
		{
			final long				fastStart=System.currentTimeMillis();
			final IMAP4FastResponse	rspFast=sess.fetchFastMsgsAllInfo(true);
			final long				fastEnd=System.currentTimeMillis(), fastDuration=(fastEnd - fastStart);
			if (rspFast.isOKResponse())
			{
				final Collection<? extends IMAP4FastMsgInfo>	msgs=rspFast.getMessages();
				final int										numMsgs=(null == msgs) ? 0 : msgs.size();
				out.println("Got[" + folder + "] " + numMsgs + " entries in " + fastDuration + " msec.: " + rspFast);
				if (numMsgs > 0)
				{
					for (final IMAP4FastMsgInfo mi : msgs)
					{
						if (null == mi)	// should not happen
							continue;
						out.println("\t" + mi.getSeqNo() + ": " + mi);
					}
				}
			}
			else
				System.err.println("Failed to [" + folder + "] FETCH FAST (after " + fastDuration + " msec.): " + rspFast);

			final String	msgId=getval(out, in, "message ID to FETCH (or Quit)");
			if ((null == msgId) || (msgId.length() <= 0))
				continue;
			if (isQuit(msgId)) break;

			final long	idValue;
			try
			{
				if ((idValue=Long.parseLong(msgId)) <= 0L)
					throw new NumberFormatException("ID must be POSITIVE");
			}
			catch(NumberFormatException e)
			{
				System.err.println("Bad/Illegal ID value: " + e.getMessage());
				continue;
			}

			final String	ans=getval(out, in, "use UID (y)/[n]/q");
			if (isQuit(ans)) break;

			final boolean	useUID=(ans != null) && (ans.length() > 0) && ('y' == Character.toLowerCase(ans.charAt(0)));

			final String	part=getval(out, in, "[E]nvelope/(B)ody/(S)tructure/(Q)uit");
			if (isQuit(part)) break;

			final char	pChar=((null == part) || (part.length() <= 0)) ? '\0' : Character.toUpperCase(part.charAt(0));
			final long	fStart=System.currentTimeMillis();
			try
			{
				final IMAP4TaggedResponse	rsp;
				switch(pChar)
				{
					case '\0'	:
					case 'E'	:
						rsp = sess.fetchMsgInfo(idValue, useUID, new IMAP4FetchModifier[] { IMAP4FetchModifier.ENVELOPE }, hndlr);
						break;

					case 'S'	:
						rsp = sess.fetchMsgInfo(idValue, useUID, new IMAP4FetchModifier[] { IMAP4FetchModifier.BODYSTRUCTURE }, hndlr);
						break;

					case 'B'	:
					default		:
						throw new UnsupportedOperationException("Unknown part requested: " + part);
				}
				final long	fEnd=System.currentTimeMillis(), fDuration=fEnd - fStart;
				out.println("Got response after " + fDuration + " msec.: " + rsp);
			}
			catch(Exception e)
			{
				final long	fEnd=System.currentTimeMillis(), fDuration=fEnd - fStart;
				System.err.println(e.getClass().getName() + " on FETCH data after " + fDuration + " msec.: " + e.getMessage());
			}
		}

		return 0;
	}

	private static final int testIMAP4Access (final PrintStream out, final BufferedReader in, final IMAP4Accessor sess) throws IOException
	{
		for ( ; ; )
		{
			showAllFolders(out, sess);

			String	ans=getval(out, in, "choose folder (ENTER=" + IMAP4FolderInfo.IMAP4_INBOX + "/(Q)uit)");
			if (isQuit(ans)) break;
			if ((null == ans) || (ans.length() <= 0))
				ans = IMAP4FolderInfo.IMAP4_INBOX;

			final long						fStart=System.currentTimeMillis();
			final IMAP4FolderSelectionInfo	rsp=sess.select(ans);
			final long						fEnd=System.currentTimeMillis(), fDuration=fEnd - fStart;
			if (!rsp.isOKResponse())
			{
				System.err.println("Failed to select folder=" + ans + " after " + fDuration + " msec.: " + rsp);
			}

			out.println("Selected folder=" + ans + " in " + fDuration + " msec.: " + rsp);
			out.println("\t" + IMAP4FolderSelectionInfo.IMAP4_EXISTS + "=" + rsp.getNumExist());
			out.println("\t" + IMAP4StatusInfo.IMAP4_RECENT + "=" + rsp.getNumRecent());
			out.println("\t" + IMAP4StatusInfo.IMAP4_UNSEEN + "=" + rsp.getNumRecent());
			out.println("\t" + IMAP4StatusInfo.IMAP4_UIDNEXT + "=" + rsp.getUIDNext());
			out.println("\t" + IMAP4StatusInfo.IMAP4_UIDVALIDITY + "=" + rsp.getUIDValidity());
			showFlags(out, IMAP4FolderSelectionInfo.IMAP4_PERMANENTFLAGS, rsp.getPrmFlags());
			showFlags(out, IMAP4FetchModifier.IMAP4_FLAGS, rsp.getDynFlags());
			
			testIMAP4FolderAccess(out, in, ans, sess);
		}

		return 0;
	}

	private static final void showNamespace (PrintStream out, String nsType, IMAP4Namespace nsInfo)
	{
		if (out != null)
		{
			final String	nsPrefix=(null == nsInfo) ? null : nsInfo.getPrefix();
			final char[]	delim={ (null == nsInfo) ? '\0' : nsInfo.getDelimiter() };

			out.println("\t" + nsType + ": "
					+ (((null == nsPrefix) || (0 == nsPrefix.length())) ? "NIL" : nsPrefix) + " delim="
					+ (('\0' == delim[0]) ? "EOS" : String.valueOf(delim)));
		}
	}

	private static final int testIMAP4Access (final PrintStream out, final BufferedReader in,
											  final String url, final String user, final String pass)
	{
		final int		pp=url.lastIndexOf(':');
		final String	host=(pp < 0) ? url : url.substring(0, pp);
		final int		port=(pp < 0) ? IMAP4Protocol.IPPORT_IMAP4 : Integer.parseInt(url.substring(pp+1));
		final NetServerWelcomeLine	wl=new NetServerWelcomeLine();
		for ( ; ; )
		{
			final String	ans=getval(out, in, "(re-)run test ([y]/n)");
			if ((ans != null) && (ans.length() > 0) && (Character.toUpperCase(ans.charAt(0)) != 'Y'))
				break;

			final IMAP4Session	sess=new IMAP4Session();
			try
			{
				sess.setReadTimeout(30 * 1000);

				{
					final long	cStart=System.currentTimeMillis();
					sess.connect(host, port, wl);
					final long	cEnd=System.currentTimeMillis(), cDuration=cEnd - cStart;
					out.println("Connected to " + host + " on port " + port + " in " + cDuration + " msec.: " + wl);
				}

				{
					final Map.Entry<String,String>	ident=
						IMAP4ServerIdentityAnalyzer.DEFAULT.getServerIdentity(wl.getLine());
					if (null == ident)
						System.err.println("Failed to identify server");
					else
						out.println("\tType=" + ident.getKey() + "/Version=" + ident.getValue());
				}

				boolean	hasQuota=false, hasNamespace=false;
				{
					final long				capStart=System.currentTimeMillis();
					final IMAP4Capabilities	rsp=sess.capability();
					final long				capEnd=System.currentTimeMillis(), capDuration=(capEnd - capStart);
					if (rsp.isOKResponse())
					{
						final Collection<String>	caps=rsp.getCapabilities();
						final int					numCaps=(null == caps) ? 0 : caps.size();

						out.println("got " + numCaps + " capabilities in " + capDuration + "msec.: " + rsp);
						if (numCaps > 0)
						{
							out.println(IMAP4Protocol.IMAP4CapabilityCmd);
							for (final String c : caps)
								out.println("\t" + c);
						}

						hasQuota = rsp.hasQuota();
						hasNamespace = rsp.hasNamespace();
					}
					else
						System.err.println("Failed to get CAPABILITY after " + capDuration + " msec.: " + rsp);
				}

				{
					final long					aStart=System.currentTimeMillis();
					final IMAP4TaggedResponse	rsp=sess.login(user, pass);
					final long	aEnd=System.currentTimeMillis(), aDuration=aEnd - aStart;
					if (!rsp.isOKResponse())
					{
						System.err.println("Authentication failed in " + aDuration + " msec.: " + rsp);
						continue;
					}
					out.println("Authenticated in " + aDuration + " msec.: " + rsp);
				}

				if (hasQuota)
				{
					final long					qtStart=System.currentTimeMillis();
					final IMAP4QuotarootInfo	qtInfo=sess.getquotaroot();
					final long					qtEnd=System.currentTimeMillis(), qtDuration=(qtEnd - qtStart);
					if (qtInfo.isOKResponse())
					{
						out.println("Got response in " + qtDuration + " msec.: " + qtInfo);
						out.println(IMAP4Protocol.IMAP4GetQuotaRootCmd);
						out.println("\t" + IMAP4Protocol.IMAP4QuotaStorageRes + ": " + qtInfo.getCurStorageKB() + " out of " + qtInfo.getMaxStorageKB());
						out.println("\t" + IMAP4Protocol.IMAP4QuotaMessageRes + ": " + qtInfo.getCurMessages() + " out of " + qtInfo.getMaxMessages());
					}
					else
						System.err.println("Failed to get QUOTAROOT (after " + qtDuration + " msec.) : " + qtInfo);
				}
				
				if (hasNamespace)
				{
					final long					nsStart=System.currentTimeMillis();
					final IMAP4NamespacesInfo	nsInfo=sess.namespace();
					final long					nsEnd=System.currentTimeMillis(), nsDuration=(nsEnd - nsStart);
					if (nsInfo.isOKResponse())
					{
						out.println("Got response in " + nsDuration + " msec.: " + nsInfo);
						out.println(IMAP4Protocol.IMAP4NamespaceCmd);
						showNamespace(out, "Pesonal", nsInfo.getPersonal());
						showNamespace(out, "Shared", nsInfo.getShared());
						showNamespace(out, "Other", nsInfo.getOther());
					}
					else
						System.err.println("Failed to get NAMESPACE (after " + nsDuration + " msec.) : " + nsInfo);
				}

				testIMAP4Access(out, in, sess);
			}
			catch(IOException ce)
			{
				System.err.println(ce.getClass().getName() + " on handle session: " + ce.getMessage());
			}
			finally
			{
				try
				{
					sess.close();
				}
				catch(IOException ce)
				{
					System.err.println(ce.getClass().getName() + " on close session: " + ce.getMessage());
				}
			}
		}

		return 0;
	}

	// arg[0]=server, arg[1]=username, arg[2]=password
	private static final int testIMAP4Access (final PrintStream out, final BufferedReader in, final String[] args)
	{
		final String[]	prompts={ "Server", "Username", "Password" },
		             	tpa=resolveTestParameters(out, in, args, prompts);
		if ((null == tpa) || (tpa.length < prompts.length))
			return (-1);

		return testIMAP4Access(out, in, tpa[0], tpa[1], tpa[2]);
	}

	//////////////////////////////////////////////////////////////////////////

	public static final void main (final String args[])
	{
		final BufferedReader	in=getStdin();
		final int				nErr=testIMAP4Access(System.out, in, args);
		if (nErr != 0)
			System.err.println("test failed (err=" + nErr + ")");
		else
			System.out.println("OK");
	}
}
