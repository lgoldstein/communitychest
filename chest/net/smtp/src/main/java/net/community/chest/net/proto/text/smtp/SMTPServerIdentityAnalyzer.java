package net.community.chest.net.proto.text.smtp;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.community.chest.net.proto.text.NetServerIdentityAnalyzer;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 25, 2007 9:12:45 AM
 */
public class SMTPServerIdentityAnalyzer extends NetServerIdentityAnalyzer {
    public SMTPServerIdentityAnalyzer ()
    {
        super();
    }

    // some known SMTP servers names
    public static final String SMTPInterMailSrvrName="InterMail";
    public static final String SMTPMSExchangeSrvrName="Exchange";
    public static final String SMTPMirapointSrvrName="Mirapoint";
    public static final String SMTPLotusDominoSrvrName="Domino";
    public static final String SMTPCommtouchSrvrName="NPlex";
    public static final String SMTPSendmailSrvrName="Sendmail";
    public static final String SMTPInterScanSrvrName="InterScan";
    public static final String SMTPNSMailSrvrName="Netscape";
    public static final String SMTPiPlanetSrvrName="iPlanet";
    public static final String SMTPCommuniGateProSrvrName="CommuniGatePro";
    public static final String SMTPCriticalPathSrvrName="CriticalPath";
    /*
     *        Known templates for welcomes of different servers.
     */
    /* SW.COM-KX4.3:    220 moon.telcotest.server.com ESMTP server (InterMail vK.4.03.00.00 201-232-121 license d3f1e9b0ca21d978198f8b681cb00234) ready Wed, 28 Feb 2001 11:22:59 +0200 */
    public static final String SMTPSWCOMKx4p3WelcomePattern="%I ESMTP server (" + SMTPInterMailSrvrName + " %T=" + SMTPInterMailSrvrName + " %V %*) ready %D";
    /* Exchange: 220 newexch.server.com ESMTP Server (Microsoft Exchange Internet Mail Service 5.5.2448.0) ready */
    public static final String SMTPMSExchangeWelcomePattern="%I ESMTP Server (Microsoft " + SMTPMSExchangeSrvrName + " %T=" + SMTPMSExchangeSrvrName + " Internet Mail Service %V) ready";
    /* Mirapoint: 220 testdrive.mirapoint.com ESMTP Mirapoint 1.1.0; Wed, 28 Feb 2001 04:23:27 -0500 (EST) */
    public static final String SMTPMirapointWelcomePattern="%I ESMTP " + SMTPMirapointSrvrName + " %T=" + SMTPMirapointSrvrName + " %V; %D";
    /* Lotus: 220 ctilotus.server.com ESMTP Service (Lotus Domino Release 5.0.4) ready at Wed, 28 Feb 2001 12:02:37 +0200 */
    public static final String SMTPLotusDominoWelcomePattern="%I ESMTP Service (Lotus " + SMTPLotusDominoSrvrName + " %T=" + SMTPLotusDominoSrvrName + " Release %V) ready at %D";
    /* Commtouch: 220 s0mailgw01.prontomail.co.il ESMTP Service (NPlex 2.0.119) ready */
    public static final String SMTPCommtouchWelcomePattern="%I ESMTP Service (" + SMTPCommtouchSrvrName + " %T=" + SMTPCommtouchSrvrName + " %V) ready";
    /* Sendmail(1): 220 mail2.icomverse.com ESMTP Sendmail 8.9.2/8.9.2; Thu, 8 Mar 2001 10:53:10 +0200 (IST) */
    public static final String SMTPSendmailWelcomePattern1="%I ESMTP " + SMTPSendmailSrvrName + " %T=" + SMTPSendmailSrvrName + " %V; %D";
    /* Sendmail(2): 220 patan.sun.com ESMTP Sendmail ready at Thu, 8 Mar 2001 01:24:39 -0800 (PST) */
    public static final String SMTPSendmailWelcomePattern2="%I ESMTP " + SMTPSendmailSrvrName + " %T=" + SMTPSendmailSrvrName + " ready at %D";
    /* InterScan: 220 mail1.microsoft.com InterScan VirusWall NT ESMTP 3.24 (build 01/19/2000) ready at Thu, 08 Mar 2001 01:24:14 -0800 (Pacific Standard Time) */
    public static final String SMTPInterScanWelcomePattern="%I " + SMTPInterScanSrvrName + " %T=" + SMTPInterScanSrvrName + " %I NT ESMTP %V (build %I) ready at %D";
    /* Netscape mail: 220 ipnew_nts.server.com ESMTP service (Netscape Messaging Server 4.15  (built Dec 14 1999)) */
    public static final String SMTPNSMailWelcomePattern="%I ESMTP service (" + SMTPNSMailSrvrName + " %T=" + SMTPNSMailSrvrName + " Messaging Server %V (built %*))";
    /* iPlanet: 220 venus -- Server ESMTP (iPlanet Messaging Server 5.1 (built May  7 2001)) */
    public static final String SMTPiPlanetWelcomePattern="%I %I Server ESMTP (" + SMTPiPlanetSrvrName + " %T=" + SMTPiPlanetSrvrName + " Messaging Server %V (built %*))";
    /* iPlanet+HotFix: 220 simba1 -- Server ESMTP (iPlanet Messaging Server 5.2 HotFix 1.02 (built Sep 16 2002)) */
    public static final String SMTPiPlanetHotFixWelcomePattern="%I %I Server ESMTP (" + SMTPiPlanetSrvrName + " %T=" + SMTPiPlanetSrvrName + " Messaging Server %V HotFix %I (built %*))";
    /* Exchange2000: 220 exchange.newcti.com Microsoft ESMTP MAIL Service, Version: 5.0.2195.2966 ready at  Mon, 24 Sep 2001 02:52:54 -0700 */
    public static final String SMTPXcg2000WelcomePattern="%I Microsoft %T=" + SMTPMSExchangeSrvrName + " ESMTP MAIL Service, Version: %V ready at %D";
    /* CommuniGate Pro: 220 cgatepro.server.com ESMTP CommuniGate Pro 3.4.8 is glad to see you! */
    public static final String SMTPCommuniGateProWelcomePattern="%I ESMTP CommuniGate Pro %T=" + SMTPCommuniGateProSrvrName + " %V is glad to see you!";
    /* CriticalPath: 220 cticell.cpload.server.com ESMTP Service (6.0.021) ready */
    public static final String SMTPCriticalPathWelcomePattern="%I ESMTP %T=" + SMTPCriticalPathSrvrName + " Service (%V) ready";
    /**
     * All currently known SMTP welcome patterns
     */
    public static final List<String> SMTPKnownWelcomePatterns=Arrays.asList(
                SMTPSWCOMKx4p3WelcomePattern,
                SMTPMSExchangeWelcomePattern,
                SMTPMirapointWelcomePattern,
                SMTPLotusDominoWelcomePattern,
                SMTPCommtouchWelcomePattern,
                SMTPSendmailWelcomePattern1,
                SMTPSendmailWelcomePattern2,
                SMTPInterScanWelcomePattern,
                SMTPNSMailWelcomePattern,
                SMTPiPlanetWelcomePattern,
                SMTPiPlanetHotFixWelcomePattern,
                SMTPXcg2000WelcomePattern,
                SMTPCommuniGateProWelcomePattern,
                SMTPCriticalPathWelcomePattern
        );
    /*
     * @see net.community.chest.net.proto.text.NetServerIdentityAnalyzer#getKnownPatterns()
     */
    @Override
    public Collection<String> getKnownPatterns ()
    {
        return SMTPKnownWelcomePatterns;
    }
    /*
     * @see net.community.chest.net.proto.text.NetServerIdentityAnalyzer#getWelcomePatternMatchStart(java.lang.CharSequence)
     */
    @Override
    public int getWelcomePatternMatchStart (final CharSequence welcomeLine)
    {
        try
        {
            final int    nErr=SMTPResponse.getResponseCode(welcomeLine);
            return ((nErr > 0) ? 3 : 4);    // if continuation line, then skip the '-'
        }
        catch(IllegalStateException    ise)
        {
            return (-2);
        }
    }

    public static final SMTPServerIdentityAnalyzer    DEFAULT=new SMTPServerIdentityAnalyzer();
}
