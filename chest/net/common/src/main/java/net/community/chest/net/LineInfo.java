package net.community.chest.net;

import java.io.Serializable;

import net.community.chest.CoVariantReturn;
import net.community.chest.lang.PubliclyCloneable;

/**
 * Copyright 2007 as per GPLv2
 *
 * Helper class for returning information about a "line" read from a
 * network connection
 *
 * @author Lyor G.
 * @since Jun 28, 2007 1:45:32 PM
 */
public class LineInfo implements Serializable, PubliclyCloneable<LineInfo> {
     /**
     *
     */
    private static final long serialVersionUID = 9205023964786781411L;
    private int _length    /* =0 */;
     /**
      * @return actual number of characters in the read line (without CR/LF)
      */
     public int getLength ()
     {
         return _length;
     }

     public void setLength (int length)
     {
         _length = length;
     }
     /**
      * Increases (++) the current length
      * @return new length value
      */
     public int incLength ()
     {
         _length++;
         return _length;
     }

     private boolean    _haveCR    /* =false */;
     /**
      * @return TRUE if have seen CR prior to LF
      */
     public boolean isCRDetected ()
     {
         return _haveCR;
     }

     public void setCRDetected (boolean haveCR)
     {
         _haveCR = haveCR;
     }

     private boolean    _haveLF    /* =false */;
     /**
      * @return TRUE if have seen LF (or stopped because ran out of buffer space)
      */
     public boolean isLFDetected ()
     {
         return _haveLF;
     }

     public void setLFDetected (boolean haveLF)
     {
         _haveLF = haveLF;
     }
     /**
      * Resets the contents to an "empty"/illegal line
      */
     public void reset ()
     {
         setLength(0);
         setCRDetected(false);
         setLFDetected(false);
     }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    @CoVariantReturn
    public LineInfo clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        if ((null == obj) || (!(obj instanceof LineInfo)))
            return false;
        if (this == obj)
            return true;

        final LineInfo    li=(LineInfo) obj;
        return (li.getLength() == getLength())
            && (li.isCRDetected() == isCRDetected())
            && (li.isLFDetected() == isLFDetected())
            ;
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return getLength()
             + (isCRDetected() ? 1 : 0)
             + (isLFDetected() ? 1 : 0)
             ;
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return "Length=" + getLength()
            + ";CR=" + isCRDetected()
            + ";LF=" + isLFDetected()
            ;
    }
}
