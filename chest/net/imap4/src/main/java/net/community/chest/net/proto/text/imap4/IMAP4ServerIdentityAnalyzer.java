package net.community.chest.net.proto.text.imap4;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.community.chest.ParsableString;
import net.community.chest.net.proto.text.NetServerIdentityAnalyzer;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 25, 2007 9:16:56 AM
 */
public class IMAP4ServerIdentityAnalyzer extends NetServerIdentityAnalyzer {
	public IMAP4ServerIdentityAnalyzer ()
	{
		super();
	}

		/* some known server types */
	public static final String IMAP4InterMailSrvrName="InterMail";
	public static final String IMAP4MSExchangeSrvrName="Exchange";
	public static final String IMAP4MirapointSrvrName="Mirapoint";
	public static final String IMAP4LotusDominoSrvrName="Domino";
	public static final String IMAP4CommtouchSrvrName="NPlex";
	public static final String IMAP4NSMailSrvrName="Netscape";
	public static final String IMAP4iPlanetSrvrName="iPlanet";
	public static final String IMAP4CommuniGateProSrvrName="CommuniGatePro";
	public static final String IMAP4CriticalPathSrvrName="CriticalPath";
	/*
	 *		Known templates for welcomes of different servers.
	 */
	/* SW.COM-KX4.3:	* OK IMAP4 server (InterMail vM.5.00.00.09 201-237-107-107) ready Tue, 30 Jan 2001 11:05:57 +0200 (IST) */
	public static final String	IMAP4SWCOMKx4p3WelcomePattern="IMAP4 server (" + IMAP4InterMailSrvrName + " %T=" + IMAP4InterMailSrvrName + " %V %I) ready %D";
	/* Exchange: * OK Microsoft Exchange IMAP4rev1 server version 5.5.2448.8 (server.domain.com) ready */
	public static final String	IMAP4MSExchangeWelcomePattern="Microsoft %T IMAP4rev1 server version %V (%I) ready";
	/* Exchange 2000: * OK Microsoft Exchange 2000 IMAP4rev1 server version 6.0.4417.0 (demoser2000.exch2000.domain.com) ready. */
	public static final String	IMAP4MSExch2000WelcomePattern="Microsoft %T 2000 IMAP4rev1 server version %V (%I) ready.";
	/* Exchange 2003: * OK Microsoft Exchange Server 2003 IMAP4rev1 server version 6.5.7638.1 (CORPUSMX70C.corp.emc.com) ready. */
	public static final String	IMAP4MSExch2003WelcomePattern="Microsoft %T Server 2003 IMAP4rev1 server version %V (%I) ready.";
	/* Mirapoint: * OK testdrive.mirapoint.com Mirapoint IMAP4 1.0 server ready */
	public static final String	IMAP4MirapointWelcomePattern="%I " + IMAP4MirapointSrvrName + " %T=" + IMAP4MirapointSrvrName + " IMAP4 %V server ready";
	/* Mirapoint IMAP4 proxy: * OK Mirapoint IMAP4PROXY 1.0 server ready */
	public static final String	IMAP4MiraptProxyWelcomePattern=IMAP4MirapointSrvrName + " %T=" + IMAP4MirapointSrvrName + " IMAP4PROXY %V server ready";
	/* Lotus: * OK Domino IMAP4 Server Release 5.0.4  ready Tue, 30 Jan 2001 13:59:48 +0200 */
	public static final String	IMAP4LotusDominoWelcomePattern=IMAP4LotusDominoSrvrName + " %T=" + IMAP4LotusDominoSrvrName + " IMAP4 Server Release %V ready %D";
	/* Commtouch: * OK IMAP4 server ready (NPlex 2.0.085) */
	public static final String	IMAP4CommtouchWelcomePattern="IMAP4 server ready (" + IMAP4CommtouchSrvrName + " %T=" + IMAP4CommtouchSrvrName + " %V)";
	/* Netscape Mail:	* OK ipnew_nts.server.com IMAP4 service (Netscape Messaging Server 4.15  (built Dec 14 1999)) */
	public static final String	IMAP4NSMailWelcomePattern="%I IMAP4 service (" + IMAP4NSMailSrvrName + " %T=" + IMAP4NSMailSrvrName + " Messaging Server %V (built %*))";
	/* iPlanet: * OK venus.server.com IMAP4 service (iPlanet Messaging Server 5.1 (built May  7 2001)) */
	public static final String	IMAP4iPlanetWelcomePattern="%I IMAP4 service (" + IMAP4iPlanetSrvrName + " %T=" + IMAP4iPlanetSrvrName + " Messaging Server %V (built %*))";
	/* iPlanet+HotFix: *OK mail.cti.com IMAP4 service (iPlanet Messaging Server 5.2 HotFix 1.02 (built Sep 16 2002)) */
	public static final String	IMAP4iPlanetHotFixWelcomePattern="%I IMAP4 service (" + IMAP4iPlanetSrvrName + " %T=" + IMAP4iPlanetSrvrName + " Messaging Server %V HotFix %I (built %*))";
	/* CommuniGate Pro: * OK CommuniGate Pro IMAP Server 3.4.8 at cgatepro.server.com ready */
	public static final String	IMAP4CommuniGateProWelcomePattern="CommuniGate Pro %T=" + IMAP4CommuniGateProSrvrName + " IMAP Server %V at %I ready";
	/* CriticalPath: * OK IMAP4 server ready (6.0.021) */
	public static final String	IMAP4CriticalPathWelcomePattern="IMAP4 %T=" + IMAP4CriticalPathSrvrName + " server ready (%V)";
	/* CriticalPath PROXY: * OK IMAP4 PROXY server ready (7.3.104) */
	public static final String	IMAP4CriticalPathProxyWelcomePattern="IMAP4 PROXY %T=" + IMAP4CriticalPathSrvrName + " server ready (%V)";
	/**
	 * Some of the known patterns for known servers
	 */
	public static final List<String>	IMAP4KnownWelcomePatterns=Arrays.asList(
			IMAP4SWCOMKx4p3WelcomePattern,
			IMAP4MSExchangeWelcomePattern,
			IMAP4MirapointWelcomePattern,
			IMAP4MiraptProxyWelcomePattern,
			IMAP4LotusDominoWelcomePattern,
			IMAP4CommtouchWelcomePattern,
			IMAP4NSMailWelcomePattern,
			IMAP4MSExch2000WelcomePattern,
			IMAP4MSExch2003WelcomePattern,
			IMAP4iPlanetWelcomePattern,
			IMAP4iPlanetHotFixWelcomePattern,
			IMAP4CommuniGateProWelcomePattern,
			IMAP4CriticalPathWelcomePattern,
			IMAP4CriticalPathProxyWelcomePattern
		);
	/*
	 * @see net.community.chest.net.proto.text.NetServerIdentityAnalyzer#getKnownPatterns()
	 */
	@Override
	public Collection<String> getKnownPatterns ()
	{
		return IMAP4KnownWelcomePatterns;
	}
	/**
	 * Minimum length of welcome line to be able to retrieve pattern matching information from
	 */
	private static final int	MIN_IMAP4_WELCOME_LINE_LEN=1 + 1 + IMAP4TaggedResponse.IMAP4_OKChars.length + 1;
	/*
	 * @see net.community.chest.net.proto.text.NetServerIdentityAnalyzer#getWelcomePatternMatchStart(java.lang.CharSequence)
	 */
	@Override
	public int getWelcomePatternMatchStart (final CharSequence welcomeLine)
	{
		final int	wlLen=(null == welcomeLine) ? 0 : welcomeLine.length();
		if (wlLen <= 0)
			return (-1);

		// make sure welcome line start with at least "* OK " string
		if (wlLen <= MIN_IMAP4_WELCOME_LINE_LEN)
			return (-2);

		final ParsableString	ps=new ParsableString(welcomeLine, 0, MIN_IMAP4_WELCOME_LINE_LEN);
		final int				rspPos=0;
		if (ps.getCharAt(rspPos) != IMAP4Protocol.IMAP4_UNTAGGED_RSP)
			return (-3);

		// make sure OK response
		final int	rspCodeStart=rspPos+2, rspCodeEnd=rspCodeStart + IMAP4TaggedResponse.IMAP4_OKChars.length; 
		if (!ps.compareTo(rspCodeStart, rspCodeEnd, IMAP4TaggedResponse.IMAP4_OKChars, true))
			return (-4);
		
		return ParsableString.findNonEmptyDataStart(welcomeLine, rspCodeEnd+1);
	}

	public static final IMAP4ServerIdentityAnalyzer	DEFAULT=new IMAP4ServerIdentityAnalyzer();
}
