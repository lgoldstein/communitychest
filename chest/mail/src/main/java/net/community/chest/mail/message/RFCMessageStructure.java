/*
 *
 */
package net.community.chest.mail.message;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.ParsableString;
import net.community.chest.mail.RFCMimeDefinitions;
import net.community.chest.mail.headers.EncodedHeaderSection;
import net.community.chest.mail.headers.RFCHdrLineBufParseResult;
import net.community.chest.mail.headers.RFCHeaderDefinitions;
import net.community.chest.mail.headers.RFCMessageHeadersParser;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 4, 2009 2:05:07 PM
 */
public class RFCMessageStructure extends RFCMessageHeadersParser {
    /**
     * Numerical value of part ID used to denote an envelope part
     */
    public static final int ENVELOPEPARTIDVALUE=0;
    /**
     * "Suffix" of envelope parts - message parts (including the "top"
     * level one) have an additional VIRTUAL part - the envelope - that
     * contains the actual headers. This part is always ".0" (or "0" for
     * the top level message).
     */
    public static final String ENVELOPEPARTIDSUFFIX=String.valueOf(ENVELOPEPARTIDVALUE);
    /**
     * "dot" notation part ID
     */
    private String _partId    /* =null */;
    /**
     * @return "dot" notation part ID - the "top-level" message is empty.
     * Any of its sub parts start with "1, 2, 3...". Any of their sub-parts
     * (if any) start with the parent part - e.g. "2.1.3, 1.1, etc.". May
     * be null/empty if not initialized
     */
    public String getPartId ()
    {
        return _partId;
    }
    /**
     * @param partId - "dot" notation part ID. Note: validity is NOT
     * checked (garbage in->garbage out) - may be null/empty
     */
    public void setPartId (String partId)
    {
        _partId = partId;
    }
    /**
     * @return TRUE if this is a (virtual) envelope part
     */
    public boolean isEnvelopePart ()
    {
        final String    partId=getPartId();
        if ((null == partId) || (partId.length() <= 0))
            return false;

        final int    lIndex=partId.lastIndexOf('.');
        if (lIndex < 0)    // parent is top-level
            return ENVELOPEPARTIDSUFFIX.equals(partId);
        else
            return partId.regionMatches(lIndex + 1, ENVELOPEPARTIDSUFFIX, 0, ENVELOPEPARTIDSUFFIX.length());
    }
    /**
     * @param parent parent part of the requested part
     * @return new part ID derived from the parent + next number of parts
     * in the parent (null/empty if error)
     */
    public static final String getNextPartId (RFCMessageStructure parent)
    {
        final String    parentId=(null == parent) ? null : parent.getPartId();
        if (null == parentId)
            return null;

        final String    nextSubId=String.valueOf(parent.getEffectiveNumSubParts() + 1);
        return parent.isRootPart() ? nextSubId : parentId + "." + nextSubId;
    }
    /**
     * @param parent parent part of the envelope
     * @return envelope part ID for the derived from the parent (null/empty if error)
     */
    public static final String getEnvelopePartId (RFCMessageStructure parent)
    {
        if (null == parent)
            return null;
        if (parent.isRootPart())
            return ENVELOPEPARTIDSUFFIX;

        final String    parentId=parent.getPartId();
        if ((null == parentId) || (parentId.length() <= 0))
            return null;

        return parentId + "." + ENVELOPEPARTIDSUFFIX;
    }
    /**
     * Parent part (if any)
     */
    private RFCMessageStructure    _parent    /* =null */;
    /**
     * @return parent part - Note: null may mean either root part or error.
     */
    public RFCMessageStructure getParent ()
    {
        return _parent;
    }
    /**
     * @return TRUE if this is the top-level part
     */
    public boolean isRootPart ()
    {
        return ((null == getParent()) && "".equals(getPartId()));
    }
    /**
     * @param parent parent part - may be null (garbage in/garbage out)
     */
    public void setParent (RFCMessageStructure parent)
    {
        _parent = parent;
    }
    /**
     * Average number of part headers of interest
     */
    public static final int AVGPARTHDRSNUM=6;
    /**
     * Average number of message envelope headers of interest
     */
    public static final int    AVGMSGENVHDRSNUM=AVGPARTHDRSNUM*4;
    /**
     * part headers - key=header name, value=accumulated header value
     */
    private Map<String,String>    _partHdrs    /* =null */;
    /**
     * @return part headers - key=header name, value=accumulated header value
     * (may be null/empty if no headers)
     */
    public Map<String,String> getPartHeaders ()
    {
        return _partHdrs;
    }
    /**
     * Adds the specified to the mapped header - if the header already exists
     * then APPENDS the value
     * @param hdrName header name (should not include the ':' - not checked)
     * @param hdrValue header value (may be null/empty)
     * @return 0 if successful
     */
    public int addPartHeader (final String hdrName, final String hdrValue)
    {
        if ((null == hdrName) || (hdrName.length() <= 0))
            return (-1);

        if ((hdrValue != null) && (hdrValue.length() > 0))
        {
            if (null == _partHdrs)
                _partHdrs = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);

            final String    prevValue=_partHdrs.get(hdrName);
            // if no previous value, then simply set this one
            if ((null == prevValue) || (prevValue.length() <= 0))
                _partHdrs.put(hdrName, hdrValue);
            // if new non-empty value then append it
            else
                _partHdrs.put(hdrName, prevValue + hdrValue);
        }

        return 0;
    }
    /**
     * Prefix of all "Content-ZZZ:" headers to be used in the
     * (@link #addPartHeaders(Map, boolean)) implementation if such filtering
     * is requested
     */
    private static final String CONTENT_HDRS_PREFIX="Content-";
    private static final int CONTENT_HDRS_PREFIX_LEN=CONTENT_HDRS_PREFIX.length();
    /**
     * Adds the specified headers to the current ones - <B>Note:</B>
     * <U>overwrites</U> existing ones with same header name
     * @param hdrs headers map - key=name (including ':'), value=value (if
     * null/empty then header is deleted from the map). OK if null/empty
     * or has no "Content-ZZZ:" headers (if <I>contentOnly</I> flag set)
     * @param contentOnly if TRUE then only "Content-ZZZ:" headers are copied
     * @return 0 if successful
     */
    public int addPartHeaders (final Map<String,String> hdrs, final boolean contentOnly)
    {
        final Collection<? extends Map.Entry<String,String>>    hdrSet=
            ((null == hdrs) || (hdrs.size() <= 0)) ? null : hdrs.entrySet();
        final int                            numEntries=
            (null == hdrSet) ? 0 : hdrSet.size();
        if (numEntries > 0)
        {
            if (null == _partHdrs)    // if no current map then allocate one
                _partHdrs = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);

            for (final Map.Entry<String,String>    eHdr : hdrSet)
            {
                if (null == eHdr)    // should not happen
                    continue;

                final String    hdrName=eHdr.getKey();
                final int        hdrLen=(null == hdrName) ? 0 : hdrName.length();
                if (hdrLen <= 0)    // not allowed
                    return (-1);

                // check if required to copy only the "Content-ZZZ:" headers
                if (contentOnly)
                {
                    // obviously, if header name too short it cannot be a "Content-ZZZ:" header
                    if (hdrLen <= CONTENT_HDRS_PREFIX_LEN)
                        continue;

                    // check if it is indeed a case INSENSITIVE prefix
                    final String    hdrPrefix=hdrName.substring(0, CONTENT_HDRS_PREFIX_LEN);
                    if (!CONTENT_HDRS_PREFIX.equalsIgnoreCase(hdrPrefix))
                        continue;
                }

                final String    hdrValue=eHdr.getValue(), prevValue;
                // null/empty value means
                if ((null == hdrValue) || (hdrValue.length() <= 0))
                    prevValue = _partHdrs.remove(hdrName);
                else
                    prevValue = _partHdrs.put(hdrName, hdrValue);

                /* NOTE: this statement is for DEBUG purposes so we can place
                 *         a breakpoint here and examine the replaced value
                 */
                if (prevValue != null)
                    continue;
            }
        }

        return 0;
    }
    /**
     * Copies all headers from the specified instance to this one
     * @param msgStruct instance to copy from - may NOT be null, but OK if
     * has no headers (nothing copied)
     * @param contentOnly if TRUE then only "Content-ZZZ:" headers are copied
     * @return 0 if successful
     */
    public int copyHeaders (final RFCMessageStructure msgStruct, final boolean contentOnly)
    {
        if (null == msgStruct)
            return (-1);
        else
            return addPartHeaders(msgStruct.getPartHeaders(), contentOnly);
    }
    /* updates the headers end offset (assumes start is set)
     * @see com.cti2.util.mail.RFCMessageHeadersParser#parseHeadersData(byte[], int, int)
     */
    @Override
    public RFCHdrLineBufParseResult parseHeadersData (byte[] data, int offset, int len)
    {
        RFCHdrLineBufParseResult    res=super.parseHeadersData(data, offset, len);
        if (res.getErrCode() != 0)
            return res;

        if (!res.isMoreDataRequired())
        {
            // we need to clone the result since it is a cached instance that is used by "finishHeadersParsing"
            try
            {
                res = res.clone();
            }
            catch(CloneNotSupportedException e)    // should not happen
            {
                return getParseResultError(Integer.MIN_VALUE);
            }

            final RFCHdrLineBufParseResult    lastRes=finishHeadersParsing();
            if (lastRes.getErrCode() != 0)
                return lastRes;
        }

        final long    curEndOffset=getHeadersEndOffset();
        if (!res.isMoreDataRequired())
        {
            final int    moreOffset=res.isCRDetected() ? res.getOffset() - 2 : res.getOffset() - 1;
            setHeadersEndOffset(curEndOffset + (moreOffset - offset));
        }
        else
            setHeadersEndOffset(curEndOffset + len);

        return res;
    }
    /**
     * Replaces the specified to the mapped header - if the header does not exist
     * then it is simply added
     * @param hdrName header name (should include the ':' - not checked)
     * @param hdrValue header value (may be null/empty)
     * @return TRUE if successful
     */
    public boolean replacePartHeader (final String hdrName, final String hdrValue)
    {
        if ((null == hdrName) || (hdrName.length() <= 0))
            return false;

        if (null == _partHdrs)
            _partHdrs = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
        _partHdrs.put(hdrName, hdrValue);

        return true;
    }
    /**
     * @param hdrName header name whose value is required (may be null/empty)
     * @return header value - may be null if bad header name/value not found/no value
     */
    public String getPartHeader (final String hdrName)
    {
        if ((null == hdrName) || (hdrName.length() <= 0))
            return null;

        final Map<String,String>    hdrsMap=getPartHeaders();
        if (null == hdrsMap)
            return null;

        return hdrsMap.get(hdrName);
    }
    /**
     * collection of sub parts - each a RFCMessageStructure
     */
    private Collection<RFCMessageStructure>    _subParts    /* =null */;
    /**
     * @return collection of sub parts - each a RFCMessageStructure - null
     * if no sub parts
     */
    public Collection<RFCMessageStructure> getSubParts ()
    {
        return _subParts;
    }
    /**
     * @return number of sub-parts (including envelope if have it) - may be <=0 if none
     */
    public int getNumSubParts ()
    {
        final Collection<RFCMessageStructure>    subParts=getSubParts();
        return (null == subParts) ? 0 : subParts.size();
    }
    /**
     * @return envelope part (if any) - null if none found
     */
    public RFCMessageStructure getEnvelopePart ()
    {
        final Collection<RFCMessageStructure>    subParts=getSubParts();
        if ((subParts != null) && (subParts.size() > 0))
        {
            for (final RFCMessageStructure    p : subParts)
            {
                if ((p != null) && p.isEnvelopePart())
                    return p;
            }
        }

        // this point is reached if no match found
        return null;
    }
    /**
     * @return TRUE if part contains an envelope sub-part
     * @see #getEnvelopePart()
     */
    public boolean isEnvelopePartAvailable ()
    {
        return (getEnvelopePart() != null);
    }
    /**
     * @return number of "real" parts - without envelope
     */
    public int getEffectiveNumSubParts ()
    {
        final int    numParts=getNumSubParts();
        if (isEnvelopePartAvailable())
            return numParts - 1;
        else
            return numParts;
    }
    /**
     * Adds the specified part to the collection of sub-parts (creating
     * it if necessary).
     * @param msgPart message part to be added - if null, then nothing is done
     * @param updateMsgPart if TRUE then makes this part the parent of the message part
     * @return new sub parts collection (may be null if no previous parts
     * and null message part supplied)
     * @throws IllegalStateException if circular direct link attempted
     */
    public Collection<RFCMessageStructure> addSubPart (final RFCMessageStructure msgPart, final boolean updateMsgPart)
    {
        if (msgPart != null)
        {
            if (this == msgPart)
                throw new IllegalStateException("Circular direct message sub-parts linkage attempted");

            if (null == _subParts)
                _subParts = new LinkedList<RFCMessageStructure>();
            _subParts.add(msgPart);

            if (updateMsgPart)
                msgPart.setParent(this);
        }

        return _subParts;
    }
    /**
     * Looks for the specified part within the sub-parts of this one.
     * @param partId part ID to be searched (if null/empty then same
     * as if part not found).
     * @return sub-part with the matching (case-sensitive) ID - null if
     * not found. Note: if part ID is same as THIS object, then it is
     * returned as the result
     */
    public RFCMessageStructure getSubPartById (final String partId)
    {
        if ((null == partId) || (partId.length() <= 0))
            return null;

        if (partId.equals(getPartId()))
            return this;

        final Collection<RFCMessageStructure>    subParts=getSubParts();
        if ((subParts != null) && (subParts.size() > 0))
        {
            for (final RFCMessageStructure    part : subParts)
            {
                if (null == part)
                    continue;    // should not happen

                // recursive check
                final RFCMessageStructure    res=part.getSubPartById(partId);
                if (res != null)
                    return res;
            }
        }

        // this point is reached if no match found
        return null;
    }
    /**
     * MIME boundary used to delimit the parts
     */
    private String    _mmBoundary    /* =null */;
    /**
     * @return MIME boundary used to delimit the parts (may be null/empty)
     */
    public String getMIMEBoundary ()
    {
        return _mmBoundary;
    }
    /**
     * @param mmBoundary MIME boundary used to delimit the parts (may be null/empty)
     */
    public void setMIMEBoundary (String mmBoundary)
    {
        _mmBoundary = mmBoundary;
    }
    /**
     * @return TRUE if MIME boundary is non-null
     */
    public boolean isMIMEBoundarySet ()
    {
        final String    mmBoundary=getMIMEBoundary();
        return ((mmBoundary != null) && (mmBoundary.length() > 0));
    }
    /**
     * Extracts the MIME boundary string from a "Content-Type:" value
     * @param ctValue value to be processed - Note: boundary may NOT
     * be the first value in this string
     * @return extracted MIME boundary - null/empty if unsuccessful
     */
    public static final String extractMIMEBoundary (final String ctValue)
    {
        final Map<String,String>    ctProps=EncodedHeaderSection.getAttributesList(ctValue);
        if ((null == ctProps) || (ctProps.size() <= 0))
            return null;

        return ctProps.get(RFCMimeDefinitions.MIMEBoundaryKeyword);
    }
    /**
     * Attempts to extract the MIME boundary from the currently available
     * headers (mainly "Content-Type").
     * @param updateInternal if TRUE, and successful, then also updates the
     * cached internal value
     * @return extracted MIME boundary - null/empty if unsuccessful
     * @see #setMIMEBoundary(String mmBoundary) - called if "updateInternal == TRUE"
     */
    public String extractMIMEBoundary (boolean updateInternal)
    {
        final String    ctValue=getPartHeader(RFCHeaderDefinitions.stdContentTypeHdr),
                        mmb=extractMIMEBoundary(ctValue);
        if ((null == mmb) || (mmb.length() <= 0) || (!updateInternal))
            return mmb;

        setMIMEBoundary(mmb);
        return getMIMEBoundary();    // just to ensure consistency
    }
    /**
     * MIME content type
     */
    private String    _mimeType    /* =null */;
    /**
     * @return MIME content type - null/empty if not set
     */
    public String getMIMEType ()
    {
        return _mimeType;
    }
    /**
     * @param mimeType MIME content type - may be null/empty
     */
    public void setMIMEType (String mimeType)
    {
        _mimeType = mimeType;
    }
    /**
     * MIME content sub-type
     */
    private String    _mimeSubType    /* =null */;
    /**
     * @return MIME content sub-type - null/empty if not set
     */
    public String getMIMESubType ()
    {
        return _mimeSubType;
    }
    /**
     * @param mimeSubType MIME content sub-type - may be null/empty
     */
    public void setMIMESubType (String mimeSubType)
    {
        _mimeSubType = mimeSubType;
    }
    /**
     * @return TRUE if BOTH MIME type & sub-type are non-empty
     */
    public boolean isContentTypeSet ()
    {
        final String    mmType=getMIMEType(), mmSubType=getMIMESubType();
        return (mmType != null) && (mmSubType != null)
            && (mmType.length() > 0) && (mmSubType.length() > 0)
            ;
    }
    /**
     * @param vals as returned by (@link #extractContentType(String))
     * @return original array if length >= 2 and at least one of the
     * strings at index 0/1 is non-null/empty - null otherwise
     */
    public static final String[] adjustContentTypeValues (final String[] vals)
    {
        if ((null == vals) || (vals.length < 2))
            return null;

        if (((null == vals[0]) || (vals[0].length() <= 0))
         && ((null == vals[1]) || (vals[1].length() <= 0)))
            return null;

        return vals;
    }
    /**
     * Extracts the MIME content type/sub-type pair from the current headers
     * value (namely "Content-Type:"). NOTE: if current value is "incomplete"
     * it may return "incomplete" value(s) - e.g., "image/" is "valid" - i.e.,
     * index 0="image", index 1=null (same applies for "/gif" - index 0=null,
     * index 1="gif")
     * @param ctValue current header value
     * @return array (index 0=type, 1=sub-type) - null/empty if error
     */
    public static final String[] extractContentType (final String ctValue)
    {
        final int    ctvLen=(null == ctValue) ? 0 : ctValue.length();
        if (ctvLen <= 0)
            return null;

        final ParsableString    ps=new ParsableString(ctValue);
        final int                psEnd=ps.getMaxIndex(), typStart=ps.findNonEmptyDataStart();
        if ((typStart < 0) || (typStart >= psEnd))
            return null;

        final int    sepPos=ps.indexOf(RFCMimeDefinitions.RFC822_MIMETAG_SEP, typStart);
        if ((sepPos < typStart) || (sepPos >= psEnd))
            return null;    // regardless, the separator MUST appear

        // find end of type by checking if the '/' or a white space appears first
        final String    typVal=ps.substring(typStart, sepPos).trim();
        final String[]    vals={ ((null == typVal) || (typVal.length() <= 0)) ? null : typVal , null /* filled up later */ };
        final int        subStart=ps.findNonEmptyDataStart(sepPos + 1);
        // if no sub-type or ';' found as first non-empty data then return whatever we have so far
        if ((subStart <= sepPos) || (subStart >= psEnd) || (RFCMimeDefinitions.RFC822_ATTRS_LIST_DELIM == ps.charAt(subStart)))
            return adjustContentTypeValues(vals);

        int    subEnd=ps.findNonEmptyDataEnd(subStart+1), clPos=ps.indexOf(RFCMimeDefinitions.RFC822_ATTRS_LIST_DELIM, subStart+1);
        // check if ';' appears BEFORE end of whitespace
        if ((clPos > subStart) && (clPos < subEnd))
            subEnd = clPos;
        // if no sub-type then return whatever we have so far
        if (subEnd <= subStart)
            return adjustContentTypeValues(vals);

        final String    subVal=ps.substring(subStart, subEnd).trim();
        vals[1] = subVal;

        // just in case sub-type is also null/empty
        return adjustContentTypeValues(vals);
    }
    /**
     * Extracts the MIME content type/sub-type pair from the current headers
     * value (namely "Content-Type:"). NOTE: if current value is "incomplete"
     * it may return a value - e.g., "image/g" is "valid" although the real
     * value is more likely "image/gif"
     * @param updateInternal if TRUE and successful, then updates the internal
     * cached values.
     * @return array (index 0=type, 1=sub-type) - null/empty if error
     */
    public String[] extractContentType (boolean updateInternal)
    {
        final String[]    vals=extractContentType(getPartHeader(RFCHeaderDefinitions.stdContentTypeHdr));
        if ((null == vals) || (vals.length < 2) || (!updateInternal))
            return vals;

        setMIMEType(vals[0]);
        setMIMESubType(vals[1]);

        return vals;
    }
    /**
     * @return TRUE if this is a message/rfc882 content type part
     */
    public boolean isEmbeddedMessagePart ()
    {
        final String    mmType=getMIMEType(), mmSubType=getMIMESubType();
        return RFCMimeDefinitions.MIMEMessageType.equalsIgnoreCase(mmType) &&
               RFCMimeDefinitions.MIMERfc822SubType.equalsIgnoreCase(mmSubType)
               ;
    }
    /**
     * @return TRUE if this is a multipart/message MIME type part
     */
    public boolean isCompoundPart ()
    {
        final String    mmType=getMIMEType();
        return RFCMimeDefinitions.MIMEMultipartType.equalsIgnoreCase(mmType)
             || isEmbeddedMessagePart()
             ;
    }
    /**
     * @return TRUE if this is a message (and thus may have am envelope part)
     */
    public boolean isMsgPart ()
    {
        return (isRootPart() || isEmbeddedMessagePart());
    }
    /**
     * offset of part headers start from "top" of message
     */
    private long    _hdrsStartOffset    /* =0L */;
    /**
     * @return offset of part headers start from "top" of message
     */
    public long getHeadersStartOffset ()
    {
        return _hdrsStartOffset;
    }
    /**
     * @param hdrsStartOffset offset of part headers start from "top" of message
     */
    public void setHeadersStartOffset (long hdrsStartOffset)
    {
        _hdrsStartOffset = hdrsStartOffset;
    }
    /**
     * offset of part headers end from "top" of message
     */
    private long    _hdrsEndOffset    /* =0L */;
    /**
     * @return offset of part headers end from "top" of message (may include
     * the blank line separating the headers from the data)
     */
    public long getHeadersEndOffset ()
    {
        return _hdrsEndOffset;
    }
    /**
     * @param hdrsEndOffset offset of part headers end from "top" of message
     */
    public void setHeadersEndOffset (long hdrsEndOffset)
    {
        _hdrsEndOffset = hdrsEndOffset;
    }
    /**
     * @return size of headers section
     */
    public long getHeadersSize ()
    {
        return (getHeadersEndOffset() - getHeadersStartOffset());
    }
    /**
     * offset of part data start from "top" of message
     */
    private long    _dataStartOffset    /* =0L */;
    /**
     * @return offset of part data start from "top" of message - Note:
     * for a message(embedded or top-level) this is considered to be the first
     * position after the blank line separating the envelope from the rest
     * of the parts.
     */
    public long getDataStartOffset ()
    {
        return _dataStartOffset;
    }
    /**
     * @param dataStartOffset offset of part data start from "top" of message
     */
    public void setDataStartOffset (long dataStartOffset)
    {
        _dataStartOffset = dataStartOffset;
    }
    /**
     * offset of part data end from "top" of message
     */
    private long    _dataEndOffset    /* =0L */;
    /**
     * @return offset of part data end from "top" of message - Note:
     * for a message(embedded or top-level) this is considered to be the first
     * position after the ending MIME boundary (or data if direct attachment)
     */
    public long getDataEndOffset ()
    {
        return _dataEndOffset;
    }
    /**
     * @param dataEndOffset offset of part data end from "top" of message
     */
    public void setDataEndOffset (long dataEndOffset)
    {
        _dataEndOffset = dataEndOffset;
    }
    /**
     * @return size of part data section
     */
    public long getDataSize ()
    {
        return (getDataEndOffset() - getDataStartOffset());
    }
    /*
     * @see com.cti2.util.mail.IRFCMessageHeadersHandler#handleHeaderStage(java.lang.String, boolean)
     */
    @Override
    public int handleHeaderStage (String hdrName, boolean fStarting)
    {
        // auto-extract content-type related important information
        if ((!fStarting) && RFCHeaderDefinitions.stdContentTypeHdr.equalsIgnoreCase(hdrName))
        {
            extractContentType(true);
            extractMIMEBoundary(true);
        }

        return 0;
    }
    /*
     * @see com.cti2.util.mail.IRFCMessageHeadersHandler#handleHeaderData(java.lang.String, java.lang.String, int)
     */
    @Override
    public int handleHeaderData (String hdrName, String hdrValue, int callIndex)
    {
        return addPartHeader(hdrName, hdrValue);
    }
    /*
     * @param o object to be compared
     * @return TRUE if compared object is an non-null instance of an
     * RFCMessageStructure and has the same part ID (case-sensitive)
     * as this one.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object o)
    {
        if (o == this)
            return true;
        if (!(o instanceof RFCMessageStructure))
            return false;

        final String    oid=((RFCMessageStructure) o).getPartId(), tid=getPartId();
        if ((null == oid) || (oid.length() <= 0))    // OK if both null/empty
            return ((null == tid) || (tid.length() <= 0));
        else
            return oid.equals(tid);
    }
    /* since we override "equals"...
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        final String    pid=getPartId();
        if ((null == pid) || (pid.length() <= 0))
            return 0;
        else
            return pid.hashCode();
    }
}
