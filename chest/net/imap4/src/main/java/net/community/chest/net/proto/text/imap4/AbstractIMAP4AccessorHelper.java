package net.community.chest.net.proto.text.imap4;

import java.io.IOException;
import java.util.Calendar;

import net.community.chest.io.EOLStyle;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.lang.math.NumberTables;
import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 27, 2008 9:19:16 AM
 */
public abstract class AbstractIMAP4AccessorHelper extends AbstractIMAP4Accessor {
    protected AbstractIMAP4AccessorHelper ()
    {
        super();
    }
    /**
     * <P>System property to use for setting the tags generator type. Allowed
     * types are (case insensitive):</P></BR>
     * <UL>
     *         <LI>
     *        Incremental - starts with random value and increments by one on
     *        each call (for any single session)
     *        </LI>
     *
     *         <LI>
     *         Random - starts with a random value and generates new random one
     *         based on the previous one
     *         </LI>
     *
     *         <LI>
     *         Fixed - starts with a random value and returns it every time
     *         </LI>
     *
     *         <LI>
     *         Prefix - uses the top 5 digits as a fixed value, and the bottom
     *         5 ones as running sequence number - default if no value set
     *         </LI>
     *
     *         <LI>
     *         Otherwise, this is assumed to be a fully-qualified class path
     *         of a class that implements the {@link IMAP4TagsGenerator} interface
     *         and has a public no-args constructor.
     *         </LI>
     * </UL>
     */
    public static final String    TAGS_GEN_PROP=IMAP4TagsGenerator.class.getName().toLowerCase();
    /**
     * Default tags generator type
     */
    public static final String    DEFAULT_TAGS_GEN_TYPE="prefix";
    private static Class<?>    _genClass    /* =null */;
    /**
     * @return tags generator according to set system property
     * @throws Exception If failed to instantiate a generator
     */
    private static final synchronized IMAP4TagsGenerator getTagsGenerator () throws Exception
    {
        if (null == _genClass)
        {
            // tags generator configuration string
            final String    genType=System.getProperty(TAGS_GEN_PROP, DEFAULT_TAGS_GEN_TYPE);
            if ("prefix".equalsIgnoreCase(genType))
                _genClass = PrefixIMAP4TagsGenerator.class;
            else if ("random".equalsIgnoreCase(genType))
                _genClass = RandomIMAP4TagsGenerator.class;
            else if ("fixed".equalsIgnoreCase(genType))
                _genClass = FixedIMAP4TagsGenerator.class;
            else if ("incremental".equalsIgnoreCase(genType))
                _genClass = IncrementalIMAP4TagsGenerator.class;
            else
            {
                if ((null == (_genClass=ClassUtil.loadClassByName(genType)))
                 || (!IMAP4TagsGenerator.class.isAssignableFrom(_genClass)))
                    throw new IllegalStateException("getTagsGenerator(" + genType + ") bad specification");
            }
        }

        return (IMAP4TagsGenerator) _genClass.newInstance();
    }
    /**
     * The tags generator to use - lazily allocated
     */
    private IMAP4TagsGenerator    _tagGen /* =null */;
    /*
     * @see net.community.chest.net.proto.text.imap4.AbstractIMAP4Accessor#getAutoTag()
     */
    @Override
    protected synchronized int getAutoTag ()
    {
        if (null == _tagGen)
        {
            try
            {
                if (null == (_tagGen=getTagsGenerator()))
                    throw new IllegalStateException("getAutoTag() no " + IMAP4TagsGenerator.class.getSimpleName() + " instance");
            }
            catch(Exception e)
            {
                throw ExceptionUtil.toRuntimeException(e);
            }
        }

        return _tagGen.getNextTag();
    }

    private StringBuilder    _workBuf    /* =null */;
    protected synchronized StringBuilder getWorkBuf (final int minSize)
    {
        if (null == _workBuf)
            _workBuf = new StringBuilder(Math.max(minSize,0) + NumberTables.MAX_UNSIGNED_INT_DIGITS_NUM + 8);
        else
            _workBuf.setLength(0);

        return _workBuf;
    }
    /**
     * Cached modifiable response - re-used in order not to create a lot
     * of objects. Lazy initialized
     */
    private IMAP4TaggedResponse    _rsp    /* =null */;
    /**
     * @return Cached (and reset) instance - creates one if necessary
     */
    protected synchronized IMAP4TaggedResponse getModifiableResponse ()
    {
        if (null == _rsp)
            _rsp = new IMAP4TaggedResponse();
        else
            _rsp.reset();

        return _rsp;
    }
    /**
     * Executes a command for which only the tagged response is of interest - any un-tagged intermediate responses are ignored
     * @param tagValue tag value assumed to be contained in supplied command line
     * @param cmdLine command line (including terminating CRLF) to be issued - assumed to contain the reported tag value
     * @param cmdLen number of characters in the command line to be written
     * @return tagged response
     * @throws IOException if unable to complete command
     */
    protected abstract IMAP4TaggedResponse doFinalCommand (int tagValue, char[] cmdLine, int cmdLen) throws IOException;
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#login(java.lang.String, java.lang.String)
     */
    @Override
    public IMAP4TaggedResponse login (String username, String password) throws IOException
    {
        final int    usrLen=(null == username) ? 0 : username.length(),
                    passLen=(null == password) ? 0 : password.length();

        final int           tagValue=getAutoTag();
        final StringBuilder sb=getWorkBuf(NumberTables.MAX_UNSIGNED_LONG_DIGITS_NUM + 1 + Math.max(0, usrLen) + Math.max(0, passLen) + 4 + EOLStyle.CRLF.length());
        IMAP4Protocol.buildCmdPrefix(sb, tagValue, IMAP4Protocol.IMAP4LoginCmdChars, false, true);
        appendLoginCredentials(sb, username, password);
        sb.append(EOLStyle.CRLF.getStyleChars());

        return doFinalCommand(tagValue, StringUtil.getBackingArray(sb), sb.length());
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.AbstractIMAP4Accessor#doSimpleCommand(int, char[], char[])
     */
    @Override
    protected IMAP4TaggedResponse doSimpleCommand (int tagValue, char[] cmd, char[] arg) throws IOException
    {
        final int cmdLen=(null == cmd) ? 0 : cmd.length;
        if (cmdLen <= 0)
            throw new IMAP4AccessParamsException("Null/empty \"simple\" command");

        final int           argLen=(null == arg) ? 0 : arg.length;
        final StringBuilder    sb=getWorkBuf(cmdLen + argLen + EOLStyle.CRLF.length());
        IMAP4Protocol.buildCmdPrefix(sb, tagValue, cmd, false, (argLen != 0));
        if (argLen != 0)
            sb.append(arg);
        sb.append(EOLStyle.CRLF.getStyleChars());

        return doFinalCommand(tagValue, StringUtil.getBackingArray(sb), sb.length());
    }
    // minimum data buffer required for efficient handling of a STORE command
    private static final int minStoreCmdBufLen=
            NumberTables.MAX_UNSIGNED_LONG_DIGITS_NUM + 1 // tag value
          + IMAP4FetchModifier.IMAP4_UIDChars.length + 1    // [optiononal] UID modifier
          + IMAP4Protocol.IMAP4StoreCmdChars.length + 1
          + IMAP4FetchModifier.IMAP4_FLAGSChars.length + IMAP4Protocol.IMAP4_SILENTChars.length + 1
          + 2;  // flags enclsoure
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#changeMsgFlags(java.lang.String, boolean, net.community.chest.net.proto.text.imap4.IMAP4MessageFlag[], int)
     */
    @Override
    public IMAP4TaggedResponse changeMsgFlags (String msgRange, boolean isUID, IMAP4MessageFlag[] flags, int addRemoveSet) throws IOException
    {
        final int           rangeLen=(null == msgRange) ? 0 : msgRange.length(),
                            numFlags=(null == flags) ? 0 : flags.length,
                            tagValue=getAutoTag();
        final StringBuilder    sb=getWorkBuf(minStoreCmdBufLen + Math.max(rangeLen,0) + Math.max(numFlags,0) * IMAP4MessageFlag.IMAP4_ANSWEREDFLAG.length());
        IMAP4Protocol.buildMsgRangeCmdPrefix(sb, tagValue, IMAP4Protocol.IMAP4StoreCmdChars, msgRange, isUID);
        IMAP4MessageFlag.appendStoreFlags(sb, flags, addRemoveSet);
        sb.append(EOLStyle.CRLF.getStyleChars());

        return doFinalCommand(tagValue, StringUtil.getBackingArray(sb), sb.length());
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#fetchMsgsInfo(char[], boolean, char[], net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler)
     */
    @Override
    public IMAP4TaggedResponse fetchMsgsInfo (char[] msgRange, boolean isUID, char[] mods, IMAP4FetchResponseHandler rspHandler) throws IOException
    {
        final int modsLen=(null == mods) ? 0 : mods.length, tagValue=getAutoTag();
        if (modsLen <= 0)
            throw new IMAP4AccessParamsException("No FETCH modifiers specified");

        final StringBuilder    sb=IMAP4Protocol
            .buildMsgRangeCmdPrefix(getWorkBuf(modsLen), tagValue, IMAP4Protocol.IMAP4FetchCmdChars, msgRange, isUID)
            .append(mods)
            .append(EOLStyle.CRLF.getStyleChars())
            ;

        return fetchFinalResponse(tagValue, StringUtil.getBackingArray(sb), sb.length(), rspHandler);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#fetchMsgsInfo(java.lang.String, boolean, net.community.chest.net.proto.text.imap4.IMAP4FetchModifier[], net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler)
     */
    @Override
    public IMAP4TaggedResponse fetchMsgsInfo (String msgRange, boolean isUID, IMAP4FetchModifier[] mods, IMAP4FetchResponseHandler rspHandler) throws IOException
    {
        final int modsNum=(null == mods) ? 0 : mods.length;
        if (modsNum <= 0)
            throw new IMAP4AccessParamsException("Zero-size fetch modifiers array");

        // if exactly ONE modifier, then we can optimize the call
        if (1 == modsNum)
        {
            final IMAP4FetchModifier  fm=mods[0];
            if (null == fm)  // should not happen
                throw new IMAP4AccessParamsException("No fetch modifier specified in array");

            return fetchMsgsInfo(msgRange, isUID, fm.toString(), rspHandler);
        }

        // at this point we have more than one modifier
        final StringBuilder    sb=getWorkBuf(NumberTables.MAX_UNSIGNED_LONG_DIGITS_NUM + modsNum * (IMAP4FetchModifier.AVG_MODNAME_LEN + 1) + 4);
        final int             tagValue=getAutoTag();
        IMAP4Protocol.buildMsgRangeCmdPrefix(sb, tagValue, IMAP4Protocol.IMAP4FetchCmdChars, msgRange, isUID);
        IMAP4FetchModifier.buildModifiersList(sb, mods);
        sb.append(EOLStyle.CRLF.getStyleChars());

        return fetchFinalResponse(tagValue, StringUtil.getBackingArray(sb), sb.length(), rspHandler);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#append(java.lang.String, java.util.Calendar, net.community.chest.net.proto.text.imap4.IMAP4MessageFlag[], long, net.community.chest.net.proto.text.imap4.IMAP4AppendDataProvider)
     */
    @Override
    public IMAP4TaggedResponse append (String folder, Calendar iDate, IMAP4MessageFlag[] flags, long dataSize, IMAP4AppendDataProvider prov) throws IOException
    {
        final int    numFlags=(null == flags) ? 0 : flags.length;
        String      strFlags=null;
        if (numFlags > 0)
        {
            final StringBuilder   sb=getWorkBuf(NumberTables.MAX_UNSIGNED_LONG_DIGITS_NUM + numFlags * IMAP4MessageFlag.IMAP4_ANSWEREDFLAG.length());
            IMAP4FlagValue.appendFlagsList(sb, flags);

            strFlags = sb.toString();
            if (IMAP4FlagValue.EMPTY_FLAGS_LIST.equalsIgnoreCase(strFlags))
                strFlags = null;
        }

        String  strIDate=null;
        if (iDate != null)
        {
            final StringBuilder   sb=getWorkBuf(32);
            IMAP4Protocol.encodeInternalDate(sb, iDate);

            strIDate = sb.toString();
        }

        return append(folder, strIDate, strFlags, dataSize, prov);
    }
}
