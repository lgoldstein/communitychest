package net.community.chest.util.logging.format;

import java.util.Collection;
import java.util.LinkedList;

import net.community.chest.util.logging.LogLevelWrapper;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Some useful formatter(s)
 * 
 * @param <V> Type of value being formatted
 * @author Lyor G.
 * @since Jun 26, 2007 1:57:18 PM
 */
public abstract class LogMsgComponentFormatter<V> {
	// modifier options delimiter
	public static final char	MODOPT_START_DELIM='{', MODOPT_END_DELIM='}';
	// some available modifiers
	public static final char	MODIFIER_CHAR='%',
								SIMPLE_CLASS_NAME='c',
								FULL_CLASS_NAME='C',
								PACKAGE_NAME='k',
								LEVEL_NAME='P',
								THREAD_NAME='t',
								CONTEXT_DATA='x',
								TIMESTAMP='d',
								MESSAGE='m',
								STACKTRACE='T',
								CONSTVAL='!';
	private final char	_modifier;
	/**
	 * @return modifier character used to signal the usage of this formatter
	 * (case <U>sensitive</I>)
	 */
	public final /* no cheating */ char getModifier ()
	{
		return _modifier;
	}

	protected LogMsgComponentFormatter (final char modifier) throws IllegalArgumentException
	{
		if (((_modifier=modifier) <= ' ') || (modifier > 0x007E))
			throw new IllegalArgumentException("Bad/Illegal parameters to " + getClass().getSimpleName() + " constructor");
	}
	// special formatting for type-specific formatters
	public abstract String formatValue (V value);
	/**
	 * @param th {@link Thread} that issued the message
	 * @param logTime timestamp when log message was issued
	 * @param logClass logger wrapper {@link Class} that invoked this formatter
	 * @param l {@link LogLevelWrapper} at which this message was issued
	 * @param ctx thread-specific context of the message
	 * @param msg original message
	 * @param t associated {@link Throwable} data (if any)
	 * @return string to be displayed according to the formatter (which might
	 * take into account one,some,all or none of the arguments). <B>Note:</B>
	 * <I>null</I> will be displayed as the "null" string - so if you don't
	 * want anything to display then return an <U>empty</U> string ("").
	 */
	public abstract String format (Thread th, long logTime, Class<?> logClass, LogLevelWrapper l, Object ctx, String msg, Throwable t);

	private static final Collection<LogMsgComponentFormatter<?>> addFormatter (final Collection<LogMsgComponentFormatter<?>> c, final LogMsgComponentFormatter<?> finst)
	{
		if (null == finst)
			return c;

		final Collection<LogMsgComponentFormatter<?>>	fmts=(null == c) ? new LinkedList<LogMsgComponentFormatter<?>>() : c;
		fmts.add(finst);
		return fmts;
	}
	// TODO add a parameter that enables creating formatters for modifiers that are not part of the above list
	public static final LogMsgComponentFormatter<?>[] parseFormat (final String fmt)
	{
		final int								fmtLen=(null == fmt) ? 0 : fmt.length();
		Collection<LogMsgComponentFormatter<?>>	fmts=null;
		for (int	curPos=0; curPos < fmtLen; )
		{
			final int	modPos=fmt.indexOf(MODIFIER_CHAR, curPos);
			if ((modPos >= curPos) && (modPos < fmtLen))
			{
				// check if have any "clear" text up to the modifier
				if (modPos > curPos)
				{
					final String textVal=fmt.substring(curPos, modPos);
					fmts = addFormatter(fmts, new ConstValueFormatter<String>(textVal));
				}

				curPos = modPos + 1;	// skip modifier character

				final char	modChar=(curPos < fmtLen) ? fmt.charAt(curPos) : /* should not happen */ '\0';
				curPos++;	// skip modifier character

				// check if have any options
				String	opts=null;
				if ((curPos < fmtLen) && (MODOPT_START_DELIM == fmt.charAt(curPos)))
				{
					// look for end delimiter - if not found, assume not an option
					final int	endPos=fmt.indexOf(MODOPT_END_DELIM, curPos);
					if ((endPos > curPos) && (endPos < fmtLen))
					{
						curPos++;	// skip start delimiter

						opts = (endPos > curPos) ? fmt.substring(curPos, endPos) : /* OK if empty options */ "";

						curPos = (endPos + 1);	// skip end delimiter
					}
				}

				switch(modChar)
				{
					case SIMPLE_CLASS_NAME 	: fmts = addFormatter(fmts, new SimpleClassNameFormatter()); break;
					case FULL_CLASS_NAME	: fmts = addFormatter(fmts, new FullClassNameFormatter()); break;
					case PACKAGE_NAME		: fmts = addFormatter(fmts, new PackageNameFormatter()); break;
					case LEVEL_NAME			: fmts = addFormatter(fmts, new LevelNameFormatter()); break;
					case THREAD_NAME		: fmts = addFormatter(fmts, new ThreadNameFormatter()); break;
					case CONTEXT_DATA		: fmts = addFormatter(fmts, new ThreadContextFormatter()); break;
					case TIMESTAMP			: fmts = addFormatter(fmts, new TimestampFormatter(opts)); break;
					case MESSAGE			: fmts = addFormatter(fmts, new MessageTextFormatter()); break;
					case STACKTRACE			: fmts = addFormatter(fmts, new StackTraceFormatter(opts)); break;
					case CONSTVAL			: fmts = addFormatter(fmts, new ConstValueFormatter<String>(opts)); break;

					default					:
						// TODO add a parameter that enables creating formatters for modifiers that are not part of the above list
						{
							final String	textVal=fmt.substring(modPos, curPos);
							fmts = addFormatter(fmts, new ConstValueFormatter<String>(textVal));
						}
				}
			}
			else	// add the remaining part of the format as "clear" text
			{
				final String	textVal=fmt.substring(curPos);
				fmts = addFormatter(fmts, new ConstValueFormatter<String>(textVal));
			}
		}

		final int	numFormatters=(null == fmts) /* should not happen... */ ? 0 : fmts.size();
		return (numFormatters <= 0) ? null : fmts.toArray(new LogMsgComponentFormatter[numFormatters]);
	}
}
