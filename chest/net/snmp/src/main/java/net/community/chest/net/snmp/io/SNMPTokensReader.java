package net.community.chest.net.snmp.io;

import java.io.IOException;
import java.io.Reader;

import net.community.chest.io.input.TokensReader;
import net.community.chest.net.snmp.SNMPProtocol;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 18, 2007 12:44:58 PM
 */
public class SNMPTokensReader extends TokensReader {
	public SNMPTokensReader (Reader inReader, boolean realClosure)
	{
		super(inReader, realClosure);
	}
	/*
	 * @see net.community.chest.io.TokensReader#isCommentToken(char[], int, int)
	 */
	@Override
	public boolean isCommentToken (char[] cbuf, int offset, int len) throws IOException
	{
		return SNMPProtocol.isCommentToken(cbuf, offset, len);
	}
}
