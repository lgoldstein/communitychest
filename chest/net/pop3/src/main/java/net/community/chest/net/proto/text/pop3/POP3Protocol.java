package net.community.chest.net.proto.text.pop3;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Static class holding POP3 related definitions</P>
 *
 * @author Lyor G.
 * @since Sep 19, 2007 10:23:06 AM
 */
public final class POP3Protocol {
    private POP3Protocol ()
    {
        // do not allow instantiation
    }
    /**
     * As defined by RFC821 actually
     */
    public static final int MAX_POP3_LINE_LENGTH=1022;
    /**
     * Default POP3 protocol port
     */
    public static final int    IPPORT_POP3=110;
    /**
     * OK response string
     */
    public static final String POP3_OK="+OK";
        public static final char[] POP3_OKChars=POP3_OK.toCharArray();
    /**
     * ERRor response string
     */
    public static final String POP3_ERR="-ERR";
        public static final char[] POP3_ERRChars=POP3_ERR.toCharArray();

        /* some known/useful command strings */
    public static final String POP3UserCmd="USER";
        public static final char[] POP3UserCmdChars=POP3UserCmd.toCharArray();
    public static final String POP3PassCmd="PASS";
        public static final char[] POP3PassCmdChars=POP3PassCmd.toCharArray();
    public static final String POP3TopCmd="TOP";
        public static final char[] POP3TopCmdChars=POP3TopCmd.toCharArray();
    public static final String POP3RetrCmd="RETR";
        public static final char[] POP3RetrCmdChars=POP3RetrCmd.toCharArray();
    public static final String POP3ListCmd="LIST";
        public static final char[] POP3ListCmdChars=POP3ListCmd.toCharArray();
    public static final String POP3LastCmd="LAST";
        public static final char[] POP3LastCmdChars=POP3LastCmd.toCharArray();
    public static final String POP3UidlCmd="UIDL";
        public static final char[] POP3UidlCmdChars=POP3UidlCmd.toCharArray();
    public static final String POP3DeleCmd="DELE";
        public static final char[] POP3DeleCmdChars=POP3DeleCmd.toCharArray();
    public static final String POP3RsetCmd="RSET";
        public static final char[] POP3RsetCmdChars=POP3RsetCmd.toCharArray();
    public static final String POP3ApopCmd="APOP";
        public static final char[] POP3ApopCmdChars=POP3ApopCmd.toCharArray();
    public static final String POP3StatCmd="STAT";
        public static final char[] POP3StatCmdChars=POP3StatCmd.toCharArray();
    public static final String POP3NoopCmd="NOOP";
        public static final char[] POP3NoopCmdChars=POP3NoopCmd.toCharArray();
    public static final String POP3HelpCmd="HELP";
        public static final char[] POP3HelpCmdChars=POP3HelpCmd.toCharArray();
    public static final String POP3QuitCmd="QUIT";
        public static final char[] POP3QuitCmdChars=POP3QuitCmd.toCharArray();
    public static final String    POP3CapaCmd="CAPA";
        public static final char[] POP3CapaCmdChars=POP3CapaCmd.toCharArray();
}
