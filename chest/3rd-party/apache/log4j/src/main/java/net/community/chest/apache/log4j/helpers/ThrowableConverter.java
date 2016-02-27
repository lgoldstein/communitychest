package net.community.chest.apache.log4j.helpers;

import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Displays a stack trace information using a configurable "depth"</P>
 * 
 * @author Lyor G.
 * @since Sep 26, 2007 12:21:31 PM
 */
public class ThrowableConverter extends PatternConverter {
    /**
     * Default displayed stack depth (if any)
     */
    public static final int DEFAULT_STACK_DEPTH=5;
    /**
     * Max. stack trace depth to display
     */
    private int	_maxStackDepth=DEFAULT_STACK_DEPTH;
    public int getMaxStackDepth ()
    {
    	return _maxStackDepth;
    }
    
    public void setMaxStackDepth (int maxStackDepth)
    {
    	_maxStackDepth = maxStackDepth;
    }

    public ThrowableConverter (int maxStackDepth)
    {
    	if ((_maxStackDepth=maxStackDepth) <= 0)
    		_maxStackDepth = DEFAULT_STACK_DEPTH;
    }
    /**
     * @param maxDepth value as a string - if null/empty or non-integer then
     * {@link #DEFAULT_STACK_DEPTH} value is used
     */
    public ThrowableConverter (final String maxDepth)
    {
    	int	maxDepthVal=0;
    	try
    	{
    		maxDepthVal = ((null == maxDepth) || (maxDepth.length() <= 0))
    			? DEFAULT_STACK_DEPTH
    			: Integer.parseInt(maxDepth)
    			;
    	}
    	catch(Exception e)
    	{
    		// ignored
    		maxDepthVal = Integer.MIN_VALUE;
    	}

    	if ((_maxStackDepth=maxDepthVal) <= 0)
    		_maxStackDepth = DEFAULT_STACK_DEPTH;
    }

    public ThrowableConverter ()
    {
    	this(DEFAULT_STACK_DEPTH);
    }

    public String convert (final Throwable t)
    {
        if (null == t)	// obviously, since no object
        	return "";

        final Class<?>				tClass=t.getClass();
        final String				tcName=(null == tClass) /* should not happen */ ? null : tClass.getName(),
        							tMessage=t.getMessage();
        final StackTraceElement[]	st=t.getStackTrace();
        final int					stDepth=(null == st) ? 0 : st.length,
        							maxDisp=Math.min(getMaxStackDepth(), stDepth),
        							cnLen=(null == tcName) ? 0 : tcName.length(),
        							tmLen=(null == tMessage) ? 0 : tMessage.length(),
        							outLen=((maxDisp <= 0) ? 0 : maxDisp * 128)
        								 + ((cnLen <= 0) ? 0 : (cnLen + 2))
        								 + ((tmLen <= 0) ? 0 : (tmLen + 2));
        if (outLen <= 0)
            return "";

        final StringBuilder	sb=new StringBuilder(outLen + 64);
       	sb.append((cnLen > 0) ? tcName : "<UNKNOWN>")
       	  .append(": ")
       	  .append((tmLen > 0) ? tMessage : "")
       	  .append(" ::> ")
       	  ;
        for (int i=0; i < maxDisp; i++)
        {
        	final StackTraceElement	e=st[i];
        	final String			eValue=(null == e)	/* should not happen */ ? null : e.toString();
        	if (i > 0)
        		sb.append(';');
        	if ((null == eValue) || (eValue.length() <= 0))
        		sb.append("MISSING ELEMENT");	// should not happen
        	else
        		sb.append(eValue);
        }

        return sb.toString();
    }
    /*
     * @see org.apache.log4j.helpers.PatternConverter#convert(org.apache.log4j.spi.LoggingEvent)
     */
    @Override
	public String convert (final LoggingEvent event)
    {
        final ThrowableInformation	ti=(null == event) /* should not happen */ ? null : event.getThrowableInformation();
        final Throwable				t=(null == ti) ? null : ti.getThrowable();
        return convert(t);
    }
}
