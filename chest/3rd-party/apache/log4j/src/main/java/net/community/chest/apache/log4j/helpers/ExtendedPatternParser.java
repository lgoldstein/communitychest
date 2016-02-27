package net.community.chest.apache.log4j.helpers;

import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Adds some more pattern converters to the default ones</P>
 * 
 * @author Lyor G.
 * @since Oct 1, 2007 8:58:19 AM
 */
public class ExtendedPatternParser extends PatternParser {
	/**
	 * Character used for prefixing the pattern name
	 */
	public static final char	PATTERN_PREFIX='%',
	/**
	 * Modifier start delimiter 
	 */
								MODSTART_DELIM='{',
	/**
	 * Modifier end delimiter 
	 */
								MODEND_DELIM='}';

	public ExtendedPatternParser (String extPattern)
	{
		super(extPattern);
	}
	/*
	 * @see org.apache.log4j.helpers.PatternParser#finalizeConverter(char)
	 */
	@Override
	protected void finalizeConverter (char c)
	{
		final PatternConverter	pc;
		switch(c)
		{
        	case 'k':
        		pc = new PackageNameConverter();
                currentLiteral.setLength(0);
                break;

        	case 'e':
                pc = new ThrowableConverter(extractOption());
                currentLiteral.setLength(0);
                break;

        	case 'E':
        		pc = new ThrowableCauseConverter(extractOption());
                currentLiteral.setLength(0);
                break;
        		
            default	:	// OK if not one of our special characters
            	pc = null;
		}

		if (pc != null)
            addConverter(pc);
        else
        	super.finalizeConverter(c);
	}
}
