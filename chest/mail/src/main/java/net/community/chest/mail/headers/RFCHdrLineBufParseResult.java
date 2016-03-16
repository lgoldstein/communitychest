package net.community.chest.mail.headers;

import java.io.Serializable;

import net.community.chest.CoVariantReturn;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 19, 2007 9:27:36 AM
 */
public class RFCHdrLineBufParseResult implements Serializable, Cloneable {
    /**
     *
     */
    private static final long serialVersionUID = 8425419161649939421L;
    private int        _nErr, _nOffset=Integer.MIN_VALUE;
    private boolean    _needMoreData=true, _haveCR;
    /**
     * @return true if after parsing more data is required (default=TRUE)
     */
    public boolean isMoreDataRequired ()
    {
        return _needMoreData;
    }

    public void setMoreDataRequired (boolean needMoreData)
    {
        _needMoreData = needMoreData;
    }
    /**
     * @return TRUE if CR was detected before the LF in the parsed data
     * @see #getOffset()
     */
    public boolean isCRDetected ()
    {
        return _haveCR;
    }

    public void setCRDetected (boolean haveCR)
    {
        _haveCR = haveCR;
    }
    /**
     * @return parsing error code - if non-zero then error
     */
    public int getErrCode ()
    {
        return _nErr;
    }

    public void setErrCode (int errCode)
    {
        _nErr = errCode;
    }
    /**
     * @return offset within parsed buffer of LF - <0 if error/not set
     */
    public int getOffset ()
    {
        return _nOffset;
    }

    public void setOffset (int nOffset)
    {
        _nOffset = nOffset;
    }
    /**
     * Resets contents to default values
     */
    public void reset ()
    {
        setMoreDataRequired(true);
        setErrCode(0);
        setOffset(Integer.MIN_VALUE);
        setCRDetected(false);
    }
    /**
     * Default constructor
     */
    public RFCHdrLineBufParseResult ()
    {
        super();
    }
    /**
     * Constructor to be used for returning an error
     * @param nErr the error to be returned - if non-zero then more-data-required
     * is automatically set to FALSE
     */
    public RFCHdrLineBufParseResult (final int nErr)
    {
        setErrCode(nErr);

        if (nErr != 0)    // if error, then more data will not help
            setMoreDataRequired(false);
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    @CoVariantReturn
    public RFCHdrLineBufParseResult clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        if ((null == obj) || (!(obj instanceof RFCHdrLineBufParseResult)))
            return false;
        if (this == obj)
            return true;

        final RFCHdrLineBufParseResult    bpr=(RFCHdrLineBufParseResult) obj;
        return (bpr.isCRDetected() == isCRDetected())
            && (bpr.isMoreDataRequired() == isMoreDataRequired())
            && (bpr.getErrCode() == getErrCode())
            && (bpr.getOffset() == getOffset())
            ;
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return (isCRDetected() ? 1 : 0)
             + (isMoreDataRequired() ? 1 : 0)
             + getErrCode()
             + getOffset()
             ;
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return new StringBuilder(64)
            .append("CR=").append(isCRDetected())
            .append(";More=").append(isMoreDataRequired())
            .append(";ERR=").append(getErrCode())
            .append(";Offset=").append(getOffset())
            .toString()
            ;
    }
}
