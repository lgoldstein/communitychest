/*
 * 
 */
package net.community.chest.mail.message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.reflect.AttributeMethodType;
import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Used for &quot;X-Priority&quot; and &quot;Importance&quot; headers</P>
 * @author Lyor G.
 * @since Sep 28, 2008 1:24:51 PM
 */
public enum MessagePriorityType {
	LOW(0),
	NORMAL(1),
	HIGH(2);

	private final int	_priValue;
	public final int getPriorityValue ()
	{
		return _priValue;
	}

	MessagePriorityType (final int priValue)
	{
		_priValue = priValue;
	}

	public static final List<MessagePriorityType>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final MessagePriorityType fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final MessagePriorityType fromPriorityValue (final int v)
	{
		for (final MessagePriorityType p : VALUES)
		{
			if ((p != null) && (p.getPriorityValue() == v))
				return p;
		}

		return null;
	}
	/**
     * Translate the "X-Priority" header value into a {@link MessagePriorityType} value
     * @param hdrValue header value
     * @return priority value - null if cannot resolve it
     */
    public static final MessagePriorityType xlateXPriorityEnum (final CharSequence hdrValue)
    {
    	final int	hvLen=(null == hdrValue) ? 0 : hdrValue.length();
        if (hvLen <= 0)
            return null;

        int	hvStart=0;	// skip anything till first digit (if any)
        for ( ; hvStart < hvLen; hvStart++)
        {	
        	final char	ch=hdrValue.charAt(hvStart);
        	if ((ch >= '0') && (ch <= '9'))
        		break;
        }
        if (hvStart >= hvLen)
        	return null;
        	
        int hvEnd=hvStart+1;	// find end of digits
        for ( ; hvEnd < hvLen; hvEnd++)
        {
        	final char	ch=hdrValue.charAt(hvEnd);
        	if ((ch < '0') || (ch > '9'))
        		break;
        }

        final int			pvLen=(hvEnd - hvStart);	// extract only the numerical value
        final CharSequence	hv=(pvLen != hvLen) ? hdrValue.subSequence(hvStart, hvEnd) : hdrValue;
		try
        {
        	final int 		nXValue=(3 - Integer.parseInt(hv.toString()));
        	if (0 == nXValue)
        		return NORMAL;
        	else if (nXValue < 0)
        		return LOW;
        	else
        		return HIGH;
        }
        catch (NumberFormatException nfe)
        {
        	// should not happen
        	return null;
        }
    }
    /**
     * @param pri required {@link MessagePriorityType}
     * @return the "X-Priority" header string value matching the specified
     * priority - null/empty if null {@link MessagePriorityType} input
     */
    public static final String xlateXPriorityValue (final MessagePriorityType pri)
    {
        if (null == pri)
            return null;

        switch(pri)
        {
        	case LOW	:	return "5";
        	case NORMAL	:	return "3";
        	case HIGH	:	return "1";
        	default		:	// should not happen
        		throw new IllegalArgumentException("xlateXPriorityValue(" + pri + ") N/A");
        }
    }
    /**
     * Translates the "Importance" header to a valid priority value
     * @param hdrValue header value - if null/empty then (@link #NORMAL_PRIORITY) assumed
     * @return priority value - null/(@link #NO_PRIORITY) if cannot resolve it
     */
    public static final MessagePriorityType xlateImportancePriorityEnum (final CharSequence hdrValue)
    {
        if ((null == hdrValue) || (hdrValue.length() <= 0))
            return NORMAL;

        for (int nPos=0, nLen=hdrValue.length(); nPos < nLen; nPos++)
        {
            final char    curPos=hdrValue.charAt(nPos);
            if ((' ' == curPos) || ('\t' == curPos))
                continue;

            if (('l' == curPos) || ('L' == curPos))  	// assume "low"
                return LOW;
            else if (('h' == curPos) || ('H' == curPos)) // assume "high"
                return HIGH;
            else	// unknown value
                return null;
        }

        // this point is reached if unknown value encountered
        return null;
    }
    /**
     * @param pri required {@link MessagePriorityType}
     * @return A {@link String} suitable for the "Importance" header - may
     * be null/empty if no {@link MessagePriorityType} or {@link MessagePriorityType#NORMAL}
     * used.
     */
    public static final String xlateImportanceValue (final MessagePriorityType pri)
    {
    	if ((null == pri) || NORMAL.equals(pri))
    		return null;

    	final String	ps=pri.name().toLowerCase();
    	return AttributeMethodType.getAdjustedAttributeName(ps);
    }
 }
