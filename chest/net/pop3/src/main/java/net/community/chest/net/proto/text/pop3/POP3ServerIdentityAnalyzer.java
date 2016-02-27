package net.community.chest.net.proto.text.pop3;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.community.chest.net.proto.text.NetServerIdentityAnalyzer;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 25, 2007 9:14:55 AM
 */
public class POP3ServerIdentityAnalyzer extends NetServerIdentityAnalyzer {
	public POP3ServerIdentityAnalyzer ()
	{
		super();
	}

		/* some known server's names */
	public static final String POP3InterMailSrvrName="InterMail";
	public static final String POP3MSExchangeSrvrName="Exchange";
	public static final String POP3CriticalPathSrvrName="CriticalPath";
	
	/* SW.COM-KX4.3: +OK InterMail POP3 server ready */
	public static final String POP3SWCOMKx4p3WelcomePattern=POP3InterMailSrvrName + " %T=" + POP3InterMailSrvrName + " %V=KX4.3 POP3 server ready.";
	/* Exchange: +OK Microsoft Exchange POP3 server version 5.5.2653.23 ready */
	public static final String POP3MSExchangeWelcomePattern="Microsoft " + POP3MSExchangeSrvrName + " %T=" + POP3MSExchangeSrvrName + " POP3 server version %V ready";
	/* Exchange2000: +OK Microsoft Exchange 2000 POP3 server version 6.0.5770.28 (exchange.newcti.com) ready. */
	public static final String POP3Xcg2000WelcomePattern="Microsoft " + POP3MSExchangeSrvrName + " %T=" + POP3MSExchangeSrvrName + " 2000 POP3 server version %V (%I) ready.";
	/* Exchange 2003: +OK Microsoft Exchange Server 2003 POP3 server version 6.5.6944.0 (test-exch2003.newcti.com) ready. */
	public static final String POP3Xcg2003WelcomePattern="Microsoft " + POP3MSExchangeSrvrName + " %T=" + POP3MSExchangeSrvrName + " Server 2003 POP3 server version %V (%I) ready.";
	/* CriticalPath: +OK POP3 server ready (6.0.021) <B97C4FDB4C676A668B323E13D1E26B7673362ABC@server.com> */
	public static final String POP3CriticalPathWelcomePattern="POP3 server %T=" + POP3CriticalPathSrvrName + " ready (%V) <%*>";
	
	public static final List<String> POP3KnownWelcomePatterns=Arrays.asList(
			POP3SWCOMKx4p3WelcomePattern,
			POP3MSExchangeWelcomePattern,
			POP3Xcg2000WelcomePattern,
			POP3Xcg2003WelcomePattern,
			POP3CriticalPathWelcomePattern
		);
	/*
	 * @see net.community.chest.net.proto.text.NetServerIdentityAnalyzer#getKnownPatterns()
	 */
	@Override
	public Collection<String> getKnownPatterns ()
	{
		return POP3KnownWelcomePatterns;
	}
	/*
	 * @see net.community.chest.net.proto.text.NetServerIdentityAnalyzer#getWelcomePatternMatchStart(java.lang.CharSequence)
	 */
	@Override
	public int getWelcomePatternMatchStart (final CharSequence welcomeLine)
	{
		final int	nErr=POP3Response.isOKResponse(welcomeLine);
		if (nErr < 0)
			return nErr;
		
		return 1 + ((0 == nErr) ? POP3Protocol.POP3_OKChars.length : POP3Protocol.POP3_ERRChars.length); 
	}

	public static final POP3ServerIdentityAnalyzer	DEFAULT=new POP3ServerIdentityAnalyzer();
}
