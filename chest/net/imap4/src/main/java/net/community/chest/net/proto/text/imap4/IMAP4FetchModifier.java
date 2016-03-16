package net.community.chest.net.proto.text.imap4;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Base class for all IMAP4 FETCH command modifiers</P>
 *
 * @author Lyor G.
 * @since Sep 20, 2007 9:32:39 AM
 */
public class IMAP4FetchModifier {
    /**
     * Modifier name
     */
    protected String _modName;
    /**
     * Final modifier characters to be used as "quick" return result of "toCharArray()"
     * @see #IMAP4FetchModifier(String modName, char[] modChars)
     */
    protected char[] _modChars;
    /**
     * @param modName modifier name - may NOT be null/empty
     * @param modChars modifier characters to be used as "quick" result of "toCharArray()" - if null/empty then we do
     * it the "hard" way via "toString().toCharArray()". The usage of this constructor is highly recommended for
     * modifiers whose sub-modifier data does not change (e.g., UID, FLAGS, INTERNALDATE, etc.), since managing them
     * in a string buffer is more efficient
     * @see #toCharArray()
     */
    public IMAP4FetchModifier (String modName, char[] modChars)
    {
        super();
        if ((null == (_modName=modName)) || (modName.length() <= 0))
            throw new IllegalArgumentException("Bad/Illegal modifier name");
        _modChars = modChars;
    }
    /**
     * @param modName modifier name - may NOT be null/empty
     * @see #IMAP4FetchModifier(String modName, char[] modChars)
     */
    public IMAP4FetchModifier (String modName)
    {
        this(modName, null);
    }
    /**
     * Used for modifiers that have additional info
     * @return string to be used as sub-modifier info (or null/empty)
     */
    public String getSubModifierInfo ()
    {
        return null;
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        final String  subInfo=getSubModifierInfo();
        return (null == subInfo) ? _modName : _modName + subInfo;
    }
    /**
     * @return modifier name + payload as char[]
     */
    public char[] toCharArray ()
    {
        if ((_modChars != null) && (_modChars.length > 0))
            return _modChars;

        final String  s=toString();
        return ((null == s) || (s.length() <= 0)) ? null : s.toCharArray();
    }
        /* known FETCH modifiers */
    public static final String IMAP4_UID="UID";
        public static final char[] IMAP4_UIDChars=IMAP4_UID.toCharArray();
    public static final String IMAP4_ALL="ALL";    /* equivalent to (FLAGS INTERNALDATE RFC822.SIZE ENVELOPE) */
        public static final char[] IMAP4_ALLChars=IMAP4_ALL.toCharArray();
    public static final String IMAP4_BODYSTRUCTURE="BODYSTRUCTURE";
        public static final char[] IMAP4_BODYSTRUCTUREChars=IMAP4_BODYSTRUCTURE.toCharArray();
    public static final String IMAP4_ENVELOPE="ENVELOPE";
        public static final char[] IMAP4_ENVELOPEChars=IMAP4_ENVELOPE.toCharArray();
    public static final String IMAP4_FAST="FAST";    /* equivalent to (FLAGS INTERNALDATE RFC822.SIZE) */
        public static final char[] IMAP4_FASTChars=IMAP4_FAST.toCharArray();
    public static final String IMAP4_FLAGS="FLAGS";
        public static final char[] IMAP4_FLAGSChars=IMAP4_FLAGS.toCharArray();
    public static final String IMAP4_FULL="FULL";    /* equivalent to (FLAGS INTERNALDATE RFC822.SIZE ENVELOPE BODY) */
        public static final char[] IMAP4_FULLChars=IMAP4_FULL.toCharArray();
    public static final String IMAP4_INTERNALDATE="INTERNALDATE";
        public static final char[] IMAP4_INTERNALDATEChars=IMAP4_INTERNALDATE.toCharArray();
    public static final String IMAP4_RFC822="RFC822";    /* euqivalent to BODY[] */
        public static final char[] IMAP4_RFC822Chars=IMAP4_RFC822.toCharArray();
    public static final String IMAP4_RFC822HDR="RFC822.HEADER"; /* equivalent to BODY.PEEK[HEADER] */
        public static final char[] IMAP4_RFC822HDRChars=IMAP4_RFC822HDR.toCharArray();
    public static final String IMAP4_RFC822SIZE="RFC822.SIZE";
        public static final char[] IMAP4_RFC822SIZEChars=IMAP4_RFC822SIZE.toCharArray();
    public static final String IMAP4_RFC822TEXT="RFC822.TEXT"; /* equivalent to BODY[TEXT] */
        public static final char[] IMAP4_RFC822TEXTChars=IMAP4_RFC822TEXT.toCharArray();
    /* message parts */
    public static final String IMAP4_BODY="BODY";
        public static final char[] IMAP4_BODYChars=IMAP4_BODY.toCharArray();
    public static final String IMAP4_BODYPEEK="BODY.PEEK";
        public static final char[] IMAP4_BODYPEEKChars=IMAP4_BODYPEEK.toCharArray();

    public static final int AVG_MODNAME_LEN=16;

        /* ready-made modifiers */
    public static final IMAP4FetchModifier  UID=new IMAP4FetchModifier(IMAP4_UID, IMAP4_UIDChars);
    public static final IMAP4FetchModifier  ALL=new IMAP4FetchModifier(IMAP4_ALL, IMAP4_ALLChars);
    public static final IMAP4FetchModifier  BODYSTRUCTURE=new IMAP4FetchModifier(IMAP4_BODYSTRUCTURE, IMAP4_BODYSTRUCTUREChars);
    public static final IMAP4FetchModifier  ENVELOPE=new IMAP4FetchModifier(IMAP4_ENVELOPE, IMAP4_ENVELOPEChars);
    public static final IMAP4FetchModifier  FAST=new IMAP4FetchModifier(IMAP4_FAST, IMAP4_FASTChars);
    public static final IMAP4FetchModifier  FLAGS=new IMAP4FetchModifier(IMAP4_FLAGS, IMAP4_FLAGSChars);
    public static final IMAP4FetchModifier  INTERNALDATE=new IMAP4FetchModifier(IMAP4_INTERNALDATE, IMAP4_INTERNALDATEChars);
    public static final IMAP4FetchModifier  RFC822SIZE=new IMAP4FetchModifier(IMAP4_RFC822SIZE, IMAP4_RFC822SIZEChars);
    /**
     * Builds a list of modifiers for the FETCH command
     * @param sb The {@link StringBuilder} into which to append (!) the list
     * @param mods modifiers
     * @return same as input buffer
     */
    public static final StringBuilder buildModifiersList (final StringBuilder sb, final IMAP4FetchModifier[] mods)
    {
        int modsNum=(null == mods) ? 0 : mods.length, curMod=0;
        if ((modsNum <= 0) || (null == sb))
            return sb;

        sb.append(IMAP4Protocol.IMAP4_PARLIST_SDELIM);

        for (int    modNdx=0; modNdx < modsNum; modNdx++)
        {
            IMAP4FetchModifier  mod=mods[modNdx];
            if (null == mod)    // should not happen
                continue;

            char[]  modChars=mod.toCharArray();
            if ((null == modChars) || (modChars.length <= 0))   // should not happen
                continue;

            if (curMod > 0)
                sb.append(' ');
            sb.append(modChars);

            curMod++;
        }

        sb.append(IMAP4Protocol.IMAP4_PARLIST_EDELIM);

        return sb;
    }
    /**
     * Builds a list of modifiers for the FETCH command
     * @param mods modifiers
     * @return string value if successful (or null)
     */
    public static final String buildModifiersList (IMAP4FetchModifier[] mods)
    {
        final int    numMods=(null == mods) ? 0 : mods.length;
        if (numMods <= 0)
            return IMAP4Protocol.IMAP4_NIL;

        return buildModifiersList(new StringBuilder(numMods * (AVG_MODNAME_LEN + 1) + 4),  mods).toString();
    }
}
