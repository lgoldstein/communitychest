package net.community.chest.net.proto.text.pop3;

import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StreamCorruptedException;
import java.io.SyncFailedException;
import java.io.Writer;

import net.community.chest.ParsableString;
import net.community.chest.io.FileUtil;
import net.community.chest.io.output.OutputStreamEmbedder;
import net.community.chest.net.TextNetConnection;
import net.community.chest.net.proto.text.AbstractTextProtocolNetConnectionHelper;
import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 19, 2007 11:12:37 AM
 */
public abstract class AbstractPOP3Accessor extends
			AbstractTextProtocolNetConnectionHelper implements POP3Accessor {
	protected AbstractPOP3Accessor ()
	{
		super();
	}
	/*
	 * @see net.community.chest.net.proto.ProtocolNetConnection#getDefaultPort()
	 */
	@Override
	public int getDefaultPort ()
	{
		return POP3Protocol.IPPORT_POP3;
	}
	/**
	 * Sends the specified command and returns the response line
	 * @param cmd command to be sent
	 * @param arg argument (may be null/empty)
	 * @return POP3 response
	 * @throws IOException if network errors
	 */
	protected abstract POP3Response sendCommand (char[] cmd, char[] arg) throws IOException;
	/**
	 * Sends the specified command regarding a specific message number
	 * @param cmd command to be sent
	 * @param msgNum message sequence number to which the command refers
	 * @param msgArg additional argument - if (<0) then ignored (used by TOP/RETR command(s))
	 * @return POP3 response
	 * @throws IOException if network errors
	 */
	protected abstract POP3Response sendMsgCommand (char[] cmd, int msgNum, int msgArg) throws IOException;
	/*
	 * @see net.community.chest.net.proto.text.pop3.POP3Accessor#user(java.lang.String)
	 */
	@Override
	public POP3Response user (String username) throws IOException
	{
		final char[]	userChars=((null == username) || (username.length() <= 0)) ? null : username.toCharArray();
		if ((null == userChars) || (userChars.length <= 0))
			throw new IOException("No username characters supplied");
		
		return sendCommand(POP3Protocol.POP3UserCmdChars, userChars);
	}
	/*
	 * @see net.community.chest.net.proto.text.pop3.POP3Accessor#pass(java.lang.String)
	 */
	@Override
	public POP3Response pass (String password) throws IOException
	{
		final char[]	passChars=((null == password) || (password.length() <= 0)) ? null : password.toCharArray();
		if ((null == passChars) || (passChars.length <= 0))
			throw new IOException("No password characters supplied");
		
		return sendCommand(POP3Protocol.POP3PassCmdChars, passChars);
	}
	/*
	 * @see net.community.chest.net.proto.text.pop3.POP3Accessor#login(java.lang.String, java.lang.String)
	 */
	@Override
	public POP3Response login (String username, String password) throws IOException
	{
		final POP3Response	rsp=user(username);
		if (!rsp.isOKResponse())
			return rsp;
		
		return pass(password);
	}
	/*
	 * @see net.community.chest.net.proto.text.pop3.POP3Accessor#stat()
	 */
	@Override
	public POP3StatusResponse stat () throws IOException
	{
		final POP3Response	rsp=sendCommand(POP3Protocol.POP3StatCmdChars, null);
		try
		{
			return POP3StatusResponse.getStatusResponse(rsp.toString());
		}
		catch(NumberFormatException nfe)
		{
			throw new IOException("Bad/Illegal STAT command response values: " + rsp.toString());
		}
	}
	/**
	 * Handles the result of a UIDL/LIST command
	 * @param isUIDL TRUE if original command was UIDL
	 * @param rspLine response line to be parsed
	 * @param startPos index to start parsing of line (inclusive)
	 * @param handler handler object to be invoked
	 * @return 0 if successful
	 */
	protected int handleSingleMsgInfo (boolean isUIDL, CharSequence rspLine, int startPos, Object handler)
	{
		// extract message number (appears for both commands)
		final ParsableString	ps=new ParsableString(rspLine, startPos, rspLine.length() - startPos);
		int	curPos=ps.findNonEmptyDataStart(), nextPos=ps.findNonEmptyDataEnd(curPos+1);
		try
		{
			final int	rspNum=ps.getUnsignedInt(curPos, nextPos);

			// prepare for argument
			curPos = ps.findNonEmptyDataStart(nextPos + 1);
			nextPos = ps.findNonEmptyDataEnd(curPos + 1);

			return isUIDL ?
					  ((POP3MsgUIDLHandler) handler).handleMsgUIDL(rspNum, ps.substring(curPos, nextPos)) :
					  ((POP3MsgSizeHandler) handler).handleMsgSize(rspNum, ps.getUnsignedLong(curPos, nextPos));
		}
		catch(NumberFormatException nfe)
		{
			return Integer.MIN_VALUE;
		}
	}

	protected String readLine () throws IOException
	{
		final TextNetConnection	conn=getTextNetConnection();
		if (null == conn)
			throw new IOException(ClassUtil.getExceptionLocation(getClass(), "readLine") + " no " + TextNetConnection.class.getName() + "instance to read from");
		return conn.readLine();
	}
	/**
	 * Helper function used to retrieve message info - UIDL/LIST
	 * @param msgNum message sequence number whose UIDL is requested (Note: if (<=0) then ALL messages are handled)
	 * @param isUIDL command to be used (true=UIDL/false=LIST)
	 * @param handler handler object
	 * @return POP3 response
	 * @throws IOException if network errors
	 */
	protected POP3Response getMsgInfo (int msgNum, boolean isUIDL, Object handler) throws IOException
	{
		final char[]	cmd=isUIDL ? POP3Protocol.POP3UidlCmdChars : POP3Protocol.POP3ListCmdChars;
		if (msgNum < 0)
			throw new IOException("Bad/Illegal " + new String(cmd) + " info message number: " + msgNum);
		
		final POP3Response	rsp=(msgNum <= 0) ? sendCommand(cmd, null) : sendMsgCommand(cmd, msgNum, (-1));
		if (!rsp.isOKResponse())
			return rsp;

		// if single message requested, then result is in the response line
		if (msgNum > 0)
		{
			int	nErr=handleSingleMsgInfo(isUIDL, rsp.toString(), POP3Protocol.POP3_OKChars.length, handler);
			if (nErr != 0)
				throw new SyncFailedException(new String(cmd) + " (single-)handler call=" + nErr);
			return rsp;
		}

		// we limit ourselves to ~2GB responses to avoid infinite loops
		for (int	rspIndex=0; rspIndex < (Integer.MAX_VALUE-1); rspIndex++)
		{
			final String	rspLine=readLine();
			final int		rspLen=(null == rspLine) ? 0 : rspLine.length();
			if (rspLen <= 0)
				throw new StreamCorruptedException("Unexpected " + new String(cmd) + " empty response line");

			// check if EOM signal
			if ((1 == rspLen) && ('.' == rspLine.charAt(0)))
				return rsp;

			final int	nErr=handleSingleMsgInfo(isUIDL, rspLine, 0, handler);
			if (nErr != 0)
				throw new SyncFailedException(new String(cmd) + " (multi-)handler err=" + nErr);
		}
		
		throw new EOFException("Virtual infinite " + new String(cmd) + " loop exit");
	}
	/*
	 * @see net.community.chest.net.proto.text.pop3.POP3Accessor#uidl(int, net.community.chest.net.proto.text.pop3.POP3MsgUIDLHandler)
	 */
	@Override
	public POP3Response uidl (int msgNum, POP3MsgUIDLHandler handler) throws IOException
	{
		return getMsgInfo(msgNum, true, handler);
	}
	/*
	 * @see net.community.chest.net.proto.text.pop3.POP3Accessor#uidl(net.community.chest.net.proto.text.pop3.POP3MsgUIDLHandler)
	 */
	@Override
	public POP3Response uidl (POP3MsgUIDLHandler handler) throws IOException
	{
		return uidl(0, handler);
	}
	/*
	 * @see net.community.chest.net.proto.text.pop3.POP3Accessor#list(int, net.community.chest.net.proto.text.pop3.POP3MsgSizeHandler)
	 */
	@Override
	public POP3Response list (int msgNum, POP3MsgSizeHandler handler) throws IOException
	{
		return getMsgInfo(msgNum, false, handler);
	}
	/*
	 * @see net.community.chest.net.proto.text.pop3.POP3Accessor#list(net.community.chest.net.proto.text.pop3.POP3MsgSizeHandler)
	 */
	@Override
	public POP3Response list (POP3MsgSizeHandler handler) throws IOException
	{
		return list(0, handler);
	}
	/**
	 * Reads all data and writes it to the specified {@link Writer} instance
	 * until EOM detected (<B>Note:</B> EOM is NOT written)
	 * @param msgNum message number to be retrieved - must be POSITIVE
	 * @param out {@link Writer} instance to write retrieved date into - may
	 * NOT be null
	 * @return 0 if successful
	 * @throws IOException if unable to communicate with the server
	 */
	protected abstract int exhaustMessageData (int msgNum, Writer out) throws IOException;
	/**
	 * Wraps a handler into a {@link Writer} object
	 * @author lyorg
	 * @since Nov 2, 2005 11:29:06 AM
	 */
	protected static final class POP3MsgDataHandlerWriterWrapper extends Writer {
		private final int			_msgNum;
		private POP3MsgDataHandler	_handler	/* =null */;
		/**
		 * @param msgNum message number whose data is being retrieved - MUST
		 * be POSITIVE
		 * @param handler handler whose {@link POP3MsgDataHandler#handleMsgData(int, char[], int, int)}
		 * method is to be called - may NOT be null
		 * @throws IOException if bad/illegal message number/handler
		 */
		protected POP3MsgDataHandlerWriterWrapper (final int msgNum, final POP3MsgDataHandler handler) throws IOException
		{
			if ((_msgNum=msgNum) <= 0)
				throw new IOException("Bad/Illegal message number to handle: " + msgNum);

			if (null == (_handler=handler))
				throw new IOException("No handler to wrap as writer");
		}
		/*
		 * @see java.io.Writer#close()
		 */
		@Override
		public void close () throws IOException
		{
			if (_handler != null)
				_handler = null;
		}
		/*
		 * @see java.io.Writer#flush()
		 */
		@Override
		public void flush () throws IOException
		{
			if (null == _handler)
				throw new IOException("flush() - no current handler");
		}
		/*
		 * @see java.io.Writer#write(char[], int, int)
		 */
		@Override
		public void write (char[] cbuf, int off, int len) throws IOException
		{
			if (null == _handler)
				throw new IOException("write() - no current handler");

			final int	nErr=_handler.handleMsgData(_msgNum, cbuf, off, len);
			if (nErr != 0)
				throw new IOException("write() - error (" + nErr + ") from handler");
		}
		/*
		 * @see java.io.Writer#write(char[])
		 */
		@Override
		public void write (char[] cbuf) throws IOException
		{
			write(cbuf, 0, cbuf.length);
		}
		/*
		 * @see java.io.Writer#write(int)
		 */
		@Override
		public void write (int c) throws IOException
		{
			write(new char[] { (char) c }, 0, 1);
		}
	}
	/**
	 * Extracts the actual data (till EOM) and calls the handler - Note: no need to call the
	 * <I>"handleMsgDataStage"</I> member (done by <I>"retrieveMsgData"</I>)
	 * @param msgNum message number data to be fetched
	 * @param handler handler used to informa caller about extracted message data
	 * @return 0 if successful
	 * @throws IOException if errors
	 */
	protected int exhaustMessageData (int msgNum, POP3MsgDataHandler handler) throws IOException
	{
		return exhaustMessageData(msgNum, new POP3MsgDataHandlerWriterWrapper(msgNum, handler));
	}
	/**
	 * Retrieves and calls the handler for a message data retrieved by RETR/TOP command
	 * @param cmd command to be used (RETR/TOP)
	 * @param msgNum message number data to be fetched
	 * @param linesNum number of lines (used by TOP command)
	 * @param handler handler used to informa caller about extracted message data
	 * @return POP3 response
	 * @throws IOException if errors
	 */
	protected POP3Response retrieveMsgData (char[] cmd, int msgNum, int linesNum, POP3MsgDataHandler handler) throws IOException
	{
		final POP3Response	rsp=sendMsgCommand(cmd, msgNum, linesNum);
		if (!rsp.isOKResponse())
			return rsp;
		
		int	nErr=handler.handleMsgDataStage(msgNum, true);
		if (nErr != 0)
			throw new IOException(new String(cmd) + " message #" + msgNum + " starting error=" + nErr);
		if ((nErr=exhaustMessageData(msgNum, handler)) != 0)
			throw new IOException(new String(cmd) + " message #" + msgNum + " data exhaustion error=" + nErr);
		if ((nErr=handler.handleMsgDataStage(msgNum, false)) != 0)
			throw new IOException(new String(cmd) + " message #" + msgNum + " ending error=" + nErr);
		
		return rsp;
	}
	/*
	 * @see net.community.chest.net.proto.text.pop3.POP3Accessor#retr(int, net.community.chest.net.proto.text.pop3.POP3MsgDataHandler)
	 */
	@Override
	public POP3Response retr (int msgNum, POP3MsgDataHandler handler) throws IOException
	{
		return retrieveMsgData(POP3Protocol.POP3RetrCmdChars, msgNum, (-1), handler);
	}
	/**
	 * Helper class used to dump a message data into an output stream
	 * @author lyorg
	 * 23/09/2004
	 */
	protected static class POP3DumpMsgWriter implements POP3MsgDataHandler {
		private int		_msgNum /* =0 */;
		private Writer	_out /* =null */;
		private boolean	_okToWrite /* =false */;

		protected POP3DumpMsgWriter (int msgNum, Writer out) throws IOException
		{
			super();

			if (null == (_out=out))
				throw new IOException("No output write to dump message num=" + msgNum);
			if ((_msgNum=msgNum) <= 0)
				throw new IOException("Bad/Illegal dump message number: " + msgNum);
		}
		/*
		 * @see POP3MsgDataHandler#handleMsgData(int, char[], int, int)
		 */
		@Override
		public int handleMsgData (int msgNum, char[] data, int startPos, int maxLen)
		{
			if (null == _out)
				return (-1);
			if (_msgNum != msgNum)
				return (-2);
			if (!_okToWrite)
				return (-3);

			try
			{
				_out.write(data, startPos, maxLen);
			}
			catch(IOException ioe)
			{
				return Integer.MIN_VALUE;
			}

			return 0;
		}
		/*
		 * @see POP3MsgDataHandler#handleMsgDataStage(int, boolean)
		 */
		@Override
		public int handleMsgDataStage (int msgNum, boolean fStarting)
		{
			if (null == _out)
				return (-1);
			if (_msgNum != msgNum)
				return (-2);

			if (fStarting)
			{
				// make sure this is first call
				if (_okToWrite)
					return (-3);

				_okToWrite = true;
			}
			else
			{
				// make sure this is the end
				if (!_okToWrite)
					return (-4);

				try
				{
					_out.flush();
				}
				catch(IOException ioe)
				{
					return Integer.MIN_VALUE;
				}

				// disable any further write
				_okToWrite = false;
				_out = null;
				_msgNum = 0;
			}

			return 0;
		}
		/**
		 * @return TRUE if "handleMsgData" has been called once for start and once for end
		 */
		protected boolean exhaustedAllData ()
		{
			return ((null == _out) && (_msgNum <= 0) && (!_okToWrite));
		}
	}
	/*
	 * @see net.community.chest.net.proto.text.pop3.POP3Accessor#dumpMsg(int, java.io.Writer)
	 */
	@Override
	public POP3Response dumpMsg (int msgNum, Writer out) throws IOException
	{
		final POP3DumpMsgWriter	mw=new POP3DumpMsgWriter(msgNum, out);
		final POP3Response		rsp=retr(msgNum, mw);
		if (rsp.isOKResponse())
		{
			// make sure that all data written if OK response received
			if (!mw.exhaustedAllData())
				throw new IOException("Not all data written for dump msg=" + msgNum);
		}

		return rsp;
	}
	/*
	 * @see net.community.chest.net.proto.text.pop3.POP3Accessor#dumpMsg(int, java.io.OutputStream)
	 */
	@Override
	public POP3Response dumpMsg (int msgNum, OutputStream out) throws IOException
	{
		Writer	w=(null == out) ? null : new OutputStreamWriter(new OutputStreamEmbedder(out, false));
		try
		{
			return dumpMsg(msgNum, w);
		}
		finally
		{
			FileUtil.closeAll(w);
		}
	}
	/*
	 * @see net.community.chest.net.proto.text.pop3.POP3Accessor#dumpMsg(int, java.lang.String)
	 */
	@Override
	public POP3Response dumpMsg (int msgNum, String filePath) throws IOException
	{
		if ((null == filePath) || (filePath.length() <= 0))
			throw new IOException("No POP3 dump file path specified for msg=" + msgNum);

		FileOutputStream	fout=null;
		try
		{
			fout = new FileOutputStream(filePath);
			return dumpMsg(msgNum, fout);
		}
		finally
		{
			FileUtil.closeAll(fout);
		}
	}
	/*
	 * @see net.community.chest.net.proto.text.pop3.POP3Accessor#top(int, int, net.community.chest.net.proto.text.pop3.POP3MsgDataHandler)
	 */
	@Override
	public POP3Response top (int msgNum, int linesNum, POP3MsgDataHandler handler) throws IOException
	{
		return retrieveMsgData(POP3Protocol.POP3TopCmdChars, msgNum, linesNum, handler);
	}
	/*
	 * @see net.community.chest.net.proto.text.pop3.POP3Accessor#dumpPartialMsg(int, int, java.io.Writer)
	 */
	@Override
	public POP3Response dumpPartialMsg (int msgNum, int linesNum, Writer out) throws IOException
	{
		final POP3DumpMsgWriter	mw=new POP3DumpMsgWriter(msgNum, out);
		final POP3Response		rsp=top(msgNum, linesNum, mw);
		if (rsp.isOKResponse())
		{
			// make sure that all data written if OK response received
			if (!mw.exhaustedAllData())
				throw new IOException("Not all data written for dump msg=" + msgNum + " lines=" + linesNum);
		}

		return rsp;
	}
	/*
	 * @see net.community.chest.net.proto.text.pop3.POP3Accessor#dumpPartialMsg(int, int, java.io.OutputStream)
	 */
	@Override
	public POP3Response dumpPartialMsg (int msgNum, int linesNum, OutputStream out) throws IOException
	{
		Writer	w=(null == out) ? null : new OutputStreamWriter(new OutputStreamEmbedder(out, false));
		try
		{
			return dumpPartialMsg(msgNum, linesNum, w);
		}
		finally
		{
			FileUtil.closeAll(w);
		}
	}
	/*
	 * @see net.community.chest.net.proto.text.pop3.POP3Accessor#dele(int)
	 */
	@Override
	public POP3Response dele (int msgNum) throws IOException
	{
		return sendMsgCommand(POP3Protocol.POP3DeleCmdChars, msgNum, (-1));
	}
	/*
	 * @see net.community.chest.net.proto.text.pop3.POP3Accessor#rset()
	 */
	@Override
	public POP3Response rset () throws IOException
	{
		return sendCommand(POP3Protocol.POP3RsetCmdChars, null);
	}
	/*
	 * @see net.community.chest.net.proto.text.pop3.POP3Accessor#noop()
	 */
	@Override
	public POP3Response noop () throws IOException
	{
		return sendCommand(POP3Protocol.POP3NoopCmdChars, null);
	}
	/*
	 * @see net.community.chest.net.proto.text.pop3.POP3Accessor#loadMessages()
	 */
	@Override
	public POP3MessageInfoResponse loadMessages () throws IOException
	{
		final POP3MessageInfoResponse	rsp=new POP3MessageInfoResponse();
		final POP3Response				listRsp=list(rsp);
		rsp.setResponseAndLine(listRsp.isOKResponse(), listRsp.getResponseLine());

		if (listRsp.isOKResponse())
		{
			final POP3Response	uidlRsp=uidl(rsp);
			rsp.setResponseAndLine(uidlRsp.isOKResponse(), uidlRsp.getResponseLine());
		}

		return rsp;
	}
	/*
	 * @see net.community.chest.net.proto.text.pop3.POP3Accessor#quit()
	 */
	@Override
	public POP3Response quit () throws IOException
	{
		try
		{
			return sendCommand(POP3Protocol.POP3QuitCmdChars, null);
		}
		finally
		{
			try
			{
				close();
			}
			catch(IOException ioe)
			{
				// ignore closure error
			}
		}
	}
}
