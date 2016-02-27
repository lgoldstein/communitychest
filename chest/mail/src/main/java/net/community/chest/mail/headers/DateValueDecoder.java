package net.community.chest.mail.headers;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.TreeMap;

import net.community.chest.ParsableString;
import net.community.chest.util.datetime.DateUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 22, 2007 10:14:24 AM
 */
public final class DateValueDecoder {
	private final ParsableString	_ps;
	private final int				_startIndex,_maxIndex;
	private final char[][]			_fullMonthsNamesChars, _abbrevMonthsNamesChars;
	private final char[][]			_fullDaysNamesChars, _abbrevDaysNamesChars;
	private final int				_abbrevMOYLen, _abbrevDOWLen;
	/**
	 * Full constructor
	 * @param dtValue date/time value to be parsed - may be null/empty
	 * @param startPos start position in value to start parsing (inclusive)
	 * @param len number of parsable characters - may be zero/negative
	 * @param fullMonthsNamesChars array of <U>at least</U> 12 elements
	 * containing <U>full</U> names of months that may be encountered.
	 * <B>Note:</B> may be omitted if an array of <U>abbreviated</U> names
	 * is supplied
	 * @param abbrevMonthsNamesChars array of <U>at least</U> 12 elements
	 * containing <U>abbreviated</U> names of months that may be encountered.
	 * <B>Note:</B> may be omitted if an array of <U>full</U> names
	 * is supplied
	 * @param abbrevMOYLen length of abbreviated month name - may be <=0 if
	 * no array of abbreviated months names supplied
	 * @param fullDaysNamesChars array of <U>at least</U> 7 elements
	 * containing <U>full</U> names of day-of-week that may be encountered.
	 * <B>Note:</B> may be omitted if an array of <U>abbreviated</U> names
	 * is supplied
	 * @param abbrevDaysNamesChars array of <U>at least</U> 7 elements
	 * containing <U>abbreviated</U> names of day-of-week that may be encountered.
	 * <B>Note:</B> may be omitted if an array of <U>full</U> names
	 * is supplied
	 * @param abbrevDOWLen length of abbreviated day-of-week name - may be <=0 if
	 * no array of abbreviated day-of-week names supplied
	 */
	public DateValueDecoder (CharSequence dtValue, int startPos, int len,
							 char[][] fullMonthsNamesChars, char[][] abbrevMonthsNamesChars, int abbrevMOYLen,
							 char[][] fullDaysNamesChars, char[][] abbrevDaysNamesChars, int abbrevDOWLen)
	{
		super();

		if ((null == dtValue) || (dtValue.length() <= 0))
		{
			_ps = null;
			_startIndex = 0;
			_maxIndex = 0;
		}
		else
		{	
			_ps = new ParsableString(dtValue, startPos, Math.max(0, len));
			_startIndex=_ps.getStartIndex();
			_maxIndex=_ps.getMaxIndex();
		}

		if ((_fullMonthsNamesChars=fullMonthsNamesChars) != null)
		{
			if (_fullMonthsNamesChars.length < 12)
				throw new IllegalArgumentException("Too few (full) months names: " + _fullMonthsNamesChars.length);
		}
		else
		{
			if (null == abbrevMonthsNamesChars)
				throw new IllegalArgumentException("No month(s) names array(s) supplied");
		}

		if ((_abbrevMonthsNamesChars=abbrevMonthsNamesChars) != null)
		{
			if (_abbrevMonthsNamesChars.length < 12)
				throw new IllegalArgumentException("Too few (abbrev.) months names: " + _abbrevMonthsNamesChars.length);
		}
		else
		{
			if (abbrevMOYLen > 0)
				throw new IllegalArgumentException("Positive abbrev. month name len (" + abbrevMOYLen + ") even though no names array");
		}

		_abbrevMOYLen = abbrevMOYLen;

		if ((_fullDaysNamesChars=fullDaysNamesChars) != null)
		{
			if (_fullDaysNamesChars.length < 7)
				throw new IllegalArgumentException("Too few (full) days names: " + _fullDaysNamesChars.length);
		}
		else
		{
			if (null == abbrevDaysNamesChars)
				throw new IllegalArgumentException("No day(s) names array(s) supplied");
		}

		if ((_abbrevDaysNamesChars=abbrevDaysNamesChars) != null)
		{
			if (_abbrevDaysNamesChars.length < 7)
				throw new IllegalArgumentException("Too few (abbrev) days names: " + _abbrevDaysNamesChars.length);
		}
		else
		{
			if (abbrevDOWLen > 0)
				throw new IllegalArgumentException("Positive abbrev. day name len (" + abbrevDOWLen + ") even though no names array");
		}

		_abbrevDOWLen = abbrevDOWLen;
	}
	/**
	 * Parses date/time strings using <U>English</U> names of months/days (if any)
	 * @param dtValue date/time value to be parsed - may be null/empty
	 * @param startPos start position in value to start parsing (inclusive)
	 * @param len number of parsable characters - may be zero/negative
	 */
	public DateValueDecoder (CharSequence dtValue, int startPos, int len)
	{
		this(dtValue, startPos, len,
			 DateUtil.fullMonthsNamesChars, DateUtil.abbrevMonthsNamesChars, DateUtil.ABBREV_MOY_LEN,
			 DateUtil.fullDaysNamesChars, DateUtil.abbrevDaysNamesChars, DateUtil.ABBREV_DOW_LEN);
	}
	/**
	 * Parses date/time strings using <U>English</U> names of months/days (if any)
	 * @param dtValue date/time value to be parsed - may be null/empty
	 */
	public DateValueDecoder (CharSequence dtValue)
	{
		this(dtValue, 0, (null == dtValue) ? 0 : dtValue.length());
	}
	/**
	 * Converts a date element (month/day) name into its value (+1)
	 * @param fullNames full names of the element
	 * @param abbrevNames abbreviated names of the element - may be null if abbrevLen==0
	 * @param startPos position of 1st character in element name parse buffer (inclusive)
	 * @param lastPos position of last character in element name parse buffer (inclusive)
	 * @param abbrevLen number of character in an abbreviated name
	 * @return index (+1) of found value, or <=0 if error/un-successful
	 */
	private byte xlateDateElementName (final char[][] fullNames, final char[][] abbrevNames, final int startPos, final int lastPos, final int abbrevLen)
	{
		final int	nameLen=(lastPos - startPos);
		if (nameLen < abbrevLen)
			return Byte.MIN_VALUE;
		
		final char[][]	vals=(nameLen > abbrevLen) ? fullNames : abbrevNames;
		for (int	valIndex=0; valIndex < vals.length; valIndex++)
			if (_ps.compareTo(startPos, lastPos, vals[valIndex], false))
				return (byte) (valIndex+1);

		return 0;
	}
	/**
	 * Translates a month (English) name into its index 1-12
	 * @param startPos position is parse buffer where month name starts (inclusive)
	 * @param lastPos position is parse buffer where month name ends (exclusive)
	 * @return month number (1-12) - or <=0 if error
	 */
	private byte xlateMonthName (final int startPos, final int lastPos)
	{
		return xlateDateElementName(_fullMonthsNamesChars, _abbrevMonthsNamesChars, startPos, lastPos, _abbrevMOYLen);
	}
	/**
	 * Translates a weekday (English) name into its index 1=Sunday-7
	 * @param startPos position is parse buffer where weekday name starts (inclusive)
	 * @param lastPos position is parse buffer where weekday name ends (exclusive)
	 * @return month number (1=Sunday-7) - or <=0 if error
	 */
	private byte xlateWeekdayName (final int startPos, final int lastPos)
	{
		return xlateDateElementName(_fullDaysNamesChars, _abbrevDaysNamesChars, startPos, lastPos, _abbrevDOWLen);
	}
	/**
	 * Extracts day-of-month value
	 * @param startPos position in parsing buffer of 1st digit (inclusive)
	 * @param lastPos position in parsing buffer after last digit
	 * @return day value (<=0 if error)
	 */
	private byte xlateDay (final int startPos, final int lastPos)
	{
		try
		{
			final int	dayVal=_ps.getUnsignedInt(startPos, lastPos);
			if ((dayVal <= 0) || (dayVal > 31))
				return (byte) (-1);

			return (byte) dayVal;
		}
		catch(NumberFormatException nfe)
		{
			return Byte.MIN_VALUE;
		}
	}
	/**
	 * Translates a month/day pair value
	 * @param startPos start position of first element
	 * @param lastPos end position of first element
	 * @param monthAndDay array to place results - index [0]=day, [1]=month
	 * @param isDayFirst if TRUE then first element is the day, otherwise it is the month
	 * @return next (non-empty) position to be parsed (or <0 if error)
	 */
	private int xlateMonthAndDay (final int startPos, final int lastPos, final byte[] monthAndDay, final boolean isDayFirst)
	{
		if (isDayFirst)
		{
			if ((monthAndDay[0]=xlateDay(startPos, lastPos)) <= 0)
				return (-1);
		}
		else
		{
			if ((monthAndDay[1]=xlateMonthName(startPos, lastPos)) <= 0)
				return (-2);
		}
		
		final int	curPos=_ps.findNonEmptyDataStart(lastPos), nextPos=_ps.findNonEmptyDataEnd(curPos+1);
		if ((curPos < lastPos) || (curPos >= nextPos) || (nextPos >= _maxIndex))
			return (-3);
		
		if (isDayFirst)
		{
			if ((monthAndDay[1]=xlateMonthName(curPos, nextPos)) <= 0)
				return (-2);
		}
		else
		{
			if ((monthAndDay[0]=xlateDay(curPos, nextPos)) <= 0)
				return (-1);
		}
		
		return _ps.findNonEmptyDataStart(nextPos);
	}
	/**
	 * Extracts a time element from a "hh:mm:ss" format
	 * @param startPos start position of first element digit (inclusive)
	 * @param delim expected delimiter - if '\0' then none expected
	 * @param tmVals array of values to be update
	 * @param tmPos position to update in the values array
	 * @param maxVal maximum allowed value
	 * @return next position to be parsed (<0 if error)
	 */
	private int xlateTimeElement (final int startPos, final char delim /* may be '\0' */, final byte[] tmVals, final int tmPos, final byte maxVal)
	{
		final int	nextPos=_ps.findNumberEnd(startPos+1);
		if ((nextPos <= startPos) || (nextPos > _maxIndex))
			return (-1);

		if ((delim != '\0') && (_ps.getCharAt(nextPos) != delim))
			return (-2);

		try
		{
			final int	eVal=_ps.getUnsignedInt(startPos, nextPos);
			if ((eVal < 0) || (eVal >= maxVal))
				return (-3);

			if ((null == tmVals) || (tmPos < 0) || (tmPos >= tmVals.length))
				return (-4);

			tmVals[tmPos] = (byte) eVal;
		}
		catch(NumberFormatException nfe)
		{
			return (-111);
		}

		if (nextPos < _maxIndex)
		{	
			if ('\0' == delim)
			{
				final int	retPos=_ps.findNonEmptyDataStart(nextPos);
				if (retPos <= startPos)
					return _maxIndex;
				else
					return retPos;
			}
			else
				return (nextPos+1);
		}
		
		return _maxIndex;
	}
	/**
	 * Extracts hour value and places it in the first <I>tmVals</I> element
	 * @param startPos position to start parsing
	 * @param tmVals hour/minute/second elements array
	 * @return next position to be parsed (<0 if error)
	 */
	private int xlateHour (final int startPos, final byte[] tmVals)
	{
		return xlateTimeElement(startPos, DateUtil.DEFAULT_TMSEP, tmVals, 0, (byte) 24);
	}
	/**
	 * Extracts minute value and places it in the second <I>tmVals</I> element
	 * @param startPos position to start parsing
	 * @param tmVals hour/minute/second elements array
	 * @param haveSep if TRUE then use the ':' to detect end of value
	 * @return next position to be parsed (<0 if error)
	 */
	private int xlateMinute (final int startPos, final byte[] tmVals, final boolean haveSep)
	{
		return xlateTimeElement(startPos, (haveSep ? DateUtil.DEFAULT_TMSEP : '\0'), tmVals, 1, (byte) 60);
	}
	/**
	 * Extracts seconds value and places it in the third <I>tmVals</I> element
	 * @param startPos position to start parsing
	 * @param tmVals hour/minute/second elements array
	 * @return next position to be parsed (<0 if error)
	 */
	private int xlateSecond (final int startPos, final byte[] tmVals)
	{
		return xlateTimeElement(startPos, '\0', tmVals, 2, (byte) 60);
	}
	/**
	 * Checks if AM/PM value and adds 12 hours (if PM) to hours element
	 * @param startPos position to start parsing
	 * @param tmVals hour/minute/second elements array
	 * @return next position to be parsed (<0 if error)
	 */
	private int adjustMeridianHour (final int startPos, final byte[] tmVals)
	{
		if ((null == tmVals) || (tmVals.length < 1))
			return (-1);

		if ('P' == Character.toUpperCase(_ps.getCharAt(startPos)))
			tmVals[0] += 12;
		
		return (startPos + 2);
	}
	/**
	 * Translates an hour specification with various formats
	 * @param startPos start position of first element (inclusive)
	 * @param tmVals hour/minute/second values to be filled
	 * @return next position (<0 if error) to be parsed
	 */
	private int xlateTimeValues (final int startPos, final byte[] tmVals /* hour, minute, second */)
	{
		int	curPos=xlateHour(startPos, tmVals);
		if ((curPos <= startPos) || (curPos >= _maxIndex))
			return (-1);
		
		// some non-STD time(s) do not have a seconds count
		boolean	haveSeconds=false;
		{
			final int	nextPos=_ps.findNumberEnd(curPos);
			haveSeconds = (nextPos < _maxIndex) && (DateUtil.DEFAULT_TMSEP == _ps.getCharAt(nextPos));

			if (((curPos=xlateMinute(curPos, tmVals, haveSeconds)) <= startPos) || (curPos > _maxIndex))
				return (-2);
		}
		
		if (haveSeconds)
		{	
			if (((curPos=xlateSecond(curPos, tmVals)) <= startPos) || (curPos > _maxIndex))
				return (-3);
		}

		// some non-STD time(s) use AM/PM instead of 24-hour time
		if ((curPos <= (_maxIndex - 2)) &&
			('M' == Character.toUpperCase(_ps.getCharAt(curPos+1))) &&
			(('A' == Character.toUpperCase(_ps.getCharAt(curPos))) || ('P' == Character.toUpperCase(_ps.getCharAt(curPos)))))
		{
			if (((curPos=adjustMeridianHour(curPos, tmVals)) <= startPos) || (curPos > _maxIndex))
				return (-4);
		}

		if (curPos < _maxIndex)
		{	
			if ((curPos=_ps.findNonEmptyDataStart(curPos)) <= startPos)
				curPos = _maxIndex;
		}

		return curPos;
	}
	/**
	 * Abbreviated time zones map - key=ID(e.g., PDT, EST), value=TimeZone object
	 */
	private static final Map<String,TimeZone>	_tzAbbrevMap;
	/**
	 * Helper class for abbreviate time zone values
	 * @author lyorg
	 * Created on 09/03/2005
	 */
	private static final class AbbreviatedTimeZone extends SimpleTimeZone {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1358097859907725693L;
		private final String	_displayName;
		/**
		 * Base constructor
		 * @param rawOffset offset in msec. from GMT
		 * @param id abbreviated name (e.g., PDT, EST) - may NOT be null empty
		 * @param dispName display name (may be null/empty)
		 * @throws IllegalArgumentException if null/empty abbreviated ID name
		 */
		protected AbbreviatedTimeZone (int rawOffset, String id, String dispName)
		{
			super(rawOffset, id);

			if ((null == id) || (id.length() <= 0))
				throw new IllegalArgumentException("No abbreviated ID supplied");

			_displayName = dispName;
		}
		/**
		 * Simplified constructor
		 * @param hours hours relative to GMT (may be negative)
		 * @param minutes minutes relative to GMT (may be negative)
		 * @param id abbreviated name (e.g., PDT, EST)
		 * @param dispName display name (may be null/empty)
		 * @throws IllegalArgumentException if null/empty abbreviated ID name
		 */
		protected AbbreviatedTimeZone (int hours, int minutes, String id, String dispName)
		{
			this((((hours < 0) || (minutes < 0)) ? (-1000) : 1000 /* msec. */) * (Math.abs(hours) * DateUtil.SECONDS_PER_HOUR + Math.abs(minutes) * DateUtil.SECONDS_PER_MINUTE), id, dispName);
		}
		/*
		 * @see java.util.TimeZone#getDisplayName(boolean, int, java.util.Locale)
		 */
		@Override
		public String getDisplayName (boolean daylight, int style, Locale locale)
		{
			return _displayName;
		}
	}
	// initialize the abbreviated time zones objects
	static
	{
		// see http://www.timeanddate.com/library/abbreviations/timezones/
		// used as default if equivalent known ID(s) not found
		final SimpleTimeZone[]	tzVals={
				new AbbreviatedTimeZone(-3, 0, "ADT", "Atlantic Daylight Time"),
				new AbbreviatedTimeZone(-4, 0, "AST", "Atlantic Standard Time"),

				new AbbreviatedTimeZone(-5, 0, "CDT", "Central Daylight Time"),
				new AbbreviatedTimeZone(-6, 0, "CST", "Central Standard Time"),

				new AbbreviatedTimeZone(1, 0, "CET", "Central European Time"),
				new AbbreviatedTimeZone(2, 0, "EET", "Eastern European Time"),
				new AbbreviatedTimeZone(0, 0, "WET", "Western European Time"),

// Australia	new AbbreviatedTimeZone(10, 30, "CDT", "Central Daylight Time"),
// Australia	new AbbreviatedTimeZone( 9, 30, "CST", "Central Standard Time"),

				new AbbreviatedTimeZone(-4, 0, "EDT", "Eastern Daylight Time"),
				new AbbreviatedTimeZone(-5, 0, "EST", "Eastern Standard Time"),

				// Australia	new AbbreviatedTimeZone(11, 0, "EDT", "Eastern Daylight Time"),
// Australia	new AbbreviatedTimeZone(10, 0, "EST", "Eastern Standard Time"),
					
				new AbbreviatedTimeZone(0, 0, "GMT", "Greenwich Mean Time"),
				new AbbreviatedTimeZone(0, 0, "UTC", "Coordinated Universal Time"),

				new AbbreviatedTimeZone(-6, 0, "MDT", "Mountain Daylight Time"),
				new AbbreviatedTimeZone(-7, 0, "MST", "Mountain Standard Time"),

				new AbbreviatedTimeZone(-7, 0, "PDT", "Pacific Daylight Time"),
				new AbbreviatedTimeZone(-8, 0, "PST", "Pacific Standard Time"),

				new AbbreviatedTimeZone(0, 0, "Z", "Zulu Time Zone")
		};
		_tzAbbrevMap = new TreeMap<String, TimeZone>(String.CASE_INSENSITIVE_ORDER);

		// first of all populate with default values
		for (int	tzIndex=0; tzIndex < tzVals.length; tzIndex++)
		{
			final TimeZone	tz=tzVals[tzIndex];
			if (null == tz)	// should not happen
				continue;
			_tzAbbrevMap.put(tz.getID(), tz);
		}

		/* - some known mappings between abbreviated time zones names and
		 * their "known" JVM ID(s)
		 * - first string in array if the abbreviated name, the next ones
		 * are some possible known ID(s)
		 * - ORDER is important - first found known ID is used to replace
		 * the default created object
		 */
		final String[][]	internalMappings={
			// NOTE !!! Australia has some similar abbreviations
			new String[] { "AST",	"SystemV/AST4"							},
			new String[] { "CST",	"US/Central",		"SystemV/CST6"		},
			new String[] { "EST",	"US/Eastern",		"SystemV/EST5"		},
			new String[] { "GMT",	"Etc/GMT"								},
			new String[] { "MST",	"US/Mountain",		"SystemV/MST7" 		},
			new String[] { "PST",	"PST", 				"US/Pacific" 		},
			new String[] { "UTC",	"Etc/UTC"								},
			new String[] { "Z",		"Zulu",				"Etc/Zulu"			}
		};

		for (int	i=0; i < internalMappings.length; i++)
		{
			final String[]	mVals=internalMappings[i];
			if ((null == mVals) || (mVals.length <= 1))
				continue;	// should not happen

			final String	aName=mVals[0];
			if ((null == aName) || (aName.length() <= 0))
				continue;	// should not happen

			for (int j=1; j < mVals.length; j++)
			{
				final String	idVal=mVals[j];
				if ((null == idVal) || (idVal.length() <= 0))
					continue;

				final TimeZone	ktz=TimeZone.getTimeZone(idVal);
				if ((ktz != null) && idVal.equals(ktz.getID()))
				{
					// stop on first found known ID
					_tzAbbrevMap.put(aName, ktz);
					break;
				}
			}
		}
	}
	/**
	 * Checks if the given char sequence is a known abbreviation for a
	 * timezone - e.g., PDT, EST, etc.
	 * @param cs char sequence - may be null/empty
	 * @param startPos - position in char sequence of the abbreviated name
	 * @param len number of characters to be checked
	 * @return time zone object (null if not found)
	 */
	public static final TimeZone getAbbreviatedTimeZone (CharSequence cs, int startPos, int len)
	{
		if ((null == cs) || (startPos < 0) || (len <= 0) || ((startPos+len) > cs.length()))
			return null;

		// check if we can use a known ID
		final String	abbrevId=cs.subSequence(startPos, startPos + len).toString().toUpperCase();
		final TimeZone	knownTz=TimeZone.getTimeZone(abbrevId);
		// make sure we got a "real" time zone and not a synthetic one
		if ((knownTz != null) && abbrevId.equalsIgnoreCase(knownTz.getID()))
			return knownTz;

		return (null == _tzAbbrevMap) ? null : _tzAbbrevMap.get(abbrevId);
	}
	/**
	 * Checks if the given char sequence is a known abbreviation for a
	 * timezone - e.g., PDT, EST, etc.
	 * @param cs char sequence - may be null/empty
	 * @return time zone object (null if not found)
	 */
	public static final TimeZone getAbbreviatedTimeZone (CharSequence cs)
	{
		return getAbbreviatedTimeZone(cs, 0, (null == cs) ? 0 : cs.length());
	}
	/**
	 * Checks if the given char sequence is a known abbreviation for a
	 * timezone - e.g., PDT, EST, etc.
	 * @param cs char sequence - may be null/empty
	 * @param startPos - position in char sequence of the abbreviated name
	 * @param len number of characters to be checked
	 * @return time zone object (null if not found)
	 */
	public static final TimeZone getAbbreviatedTimeZone (char[] cs, int startPos, int len)
	{
		if ((null == _tzAbbrevMap) || (_tzAbbrevMap.size() <= 0)
		 || (null == cs) || (cs.length <= 0)
		 || (startPos <= 0) || (len <= 0)
		 || ((startPos+len) > cs.length))
			return null;

		return getAbbreviatedTimeZone(new String(cs, startPos, len));
	}
	/**
	 * Checks if the given char sequence is a known abbreviation for a
	 * timezone - e.g., PDT, EST, etc.
	 * @param cs char sequence - may be null/empty
	 * @return time zone object (null if not found)
	 */
	public static final TimeZone getAbbreviatedTimeZone (char[] cs)
	{
		return getAbbreviatedTimeZone(cs, 0, (null == cs) ? 0 : cs.length);
	}
	/**
	 * Parses and adjusts the timezone (if any)
	 * @param startPos start position in parse buffer to look for timezone (inclusive)
	 * @param cdt calendar object whose timezone is to be set (if successful)
	 * @return next position in parse buffer (or <0 if error)
	 */
	private int adjustTimezone (final int startPos, final Calendar cdt)
	{
		final int	curPos=_ps.findNonEmptyDataStart(startPos);
		if ((curPos < startPos) || (curPos >= _maxIndex))
			return (-1);

		// sometimes the GMT offset is preceded by some text (e.g. "GMT+0200")
		for (int	chkPos=curPos; chkPos < _maxIndex; chkPos++)
		{
			final char	c=_ps.getCharAt(chkPos);
			if (((c >= '0') && (c <= '9')) || ('+' == c) || ('-' == c))
			{
				// check if can decode standard GMT offset
				final int	nextPos=RFCHeaderDefinitions.adjustTimezone(_ps, chkPos, cdt);
				if (nextPos > chkPos)
					return nextPos;
				break;
			}
		}

		// Check if some known GMT zones names (e.g., PDT, EST, etc.)
		final int	nextPos=_ps.findNonEmptyDataEnd(curPos+1);
		if ((nextPos <= curPos) || (nextPos > _maxIndex))
			return (-3);

		final TimeZone	abbrevTZ=getAbbreviatedTimeZone(_ps, curPos - _ps.getStartIndex(), (nextPos-curPos));
		if (null == abbrevTZ)
			return (-4);

		cdt.setTimeZone(abbrevTZ);
		return 0;
	}
	/**
	 * Translates a year value assumed to reside between specified positions
	 * @param curPos position of 1st digit of the year value (inclusive)
	 * @param nextPos position beyond last digit (exclusive)
	 * @return year value (or <0 if error)
	 */
	private short xlateYearValue (final int curPos, final int nextPos)
	{
		short	yearVal=0;
		try
		{
			final int	numYear=_ps.getUnsignedInt(curPos, nextPos);
			if ((numYear < 0) || (numYear > Short.MAX_VALUE))
				return (-1);
			else
				yearVal = (short) numYear;
		}
		catch(NumberFormatException nfe)
		{
			return (-200);
		}

		if (yearVal < 70)
			yearVal += 100;	// Y2K correction
		if (yearVal < 1900)
			yearVal += 1900;	// short year value
		
		return yearVal;
	}
	/**
	 * Updates the Calendar.DAY_OF_WEEK field
	 * @param bWeekday extracted weekday - 1=Sunday,...7=Saturday. If a value
	 * other than 1-7 supplied, then auto-detect using {@link DateUtil#getDayOfWeekForDate(Calendar)}
	 * @param cdt calendar object to be updated
	 * @return calendar object after update - same as input
	 */
	private static final Calendar adjustDayOfWeek (final byte bWeekday, final Calendar cdt)
	{
		if ((bWeekday >= 1) && (bWeekday <= 7))
			cdt.set(Calendar.DAY_OF_WEEK, (bWeekday - 1) + Calendar.SUNDAY);
		else
			cdt.set(Calendar.DAY_OF_WEEK, DateUtil.getDayOfWeekForDate(cdt));

		return cdt;
	}
	/**
	 * Updates the rest of the values - assumes only year, time and TZ value(s)
	 * remain to be set from starting position
	 * @param bDay extracted day-of-month (1-31)
	 * @param bMonth extract month-of-year (1-12)
	 * @param bWeekday weekday - if in range 1-7 then used, otherwise auto-detected
	 * @param yearStartPos position in parse buffer where year value starts (inclusive)
	 * @param yearEndPos position in parse buffer where year value ends (exclusive)
	 * @return updated calendar object (null if error(s))
	 * @throws IllegalStateException if ERA not "AD" (which should be NEVER...)
	 */
	private Calendar xlateRemainingYearAndTime (final byte bDay, final byte bMonth, final byte bWeekday, final int yearStartPos, final int yearEndPos) throws IllegalStateException
	{
		final short	yearVal=xlateYearValue(yearStartPos, yearEndPos);
		if (yearVal < 0)
			return null;

		final byte[]	tmVals={ 0, 0, 0 };
		// some non standard dates have ',' after the year, so we skip it
		int	curPos=xlateTimeValues(_ps.findNumberStart(yearEndPos), tmVals);
		if ((curPos <= yearEndPos) || (curPos > _maxIndex))
			return null;

		final Calendar	cdt=new GregorianCalendar(yearVal, (bMonth - 1) + Calendar.JANUARY, bDay, tmVals[0], tmVals[1], tmVals[2]);
		cdt.set(Calendar.ERA, GregorianCalendar.AD);	// don't take any chances
		cdt.set(Calendar.MILLISECOND, 0);

		adjustDayOfWeek(bWeekday, cdt);
		adjustTimezone(curPos, cdt);	// ignore any errors

		// this actually forces a re-calculation of the internal milliseconds value
		if (cdt.get(Calendar.ERA) != GregorianCalendar.AD)
			throw new IllegalStateException("ERA value mismatch: value=" + cdt.get(Calendar.ERA) + " expected=" + GregorianCalendar.AD);

		return cdt;
	}
	/* Standard format can be:
	 *		a. "Wed, 14 Jul 1999 16:36:43 +0200"	- this is the standard
	 *		b. "Sun, 1 Oct 2000 22:29 -0600"		- no seconds
	 *		c. "Fri, Sep 29 2000 19:59:23 GMT+0000"	- day/month inversion
	 *
	 * Note: some other non-standard formats are also covered by this function
	 * 
	 *		a. "Monday, September 13, 1999 2:38 PM"
	 *		a. "Thursday, 30 November 2000, 19:57:16"
	 */
	private Calendar xlateStdDateValue (final int startPos /* position beyond ',' - not checked */, final byte bWeekday /* 1=Sunday, 2=Monday, etc. */)
	{
		int	curPos=_ps.findNonEmptyDataStart(startPos);
		if ((curPos < startPos) || (curPos >= _maxIndex))
			return null;
		
		int	nextPos=_ps.findNonEmptyDataEnd(curPos+1);
		if ((nextPos <= curPos) || (nextPos >= _maxIndex))
			return null;

		// if not a digit, then MUST be a month name (full or abbreviated)
		final byte[]	monthAndDay={ 0, 0 };
		if (((curPos=xlateMonthAndDay(curPos, nextPos, monthAndDay, _ps.isDigit(curPos))) <= _startIndex) || (curPos >= _maxIndex))
			return null;

		// some non standard dates have ',' after the year, so we stop before it
		if (((nextPos=_ps.findNumberEnd(curPos+1)) <= curPos) || (nextPos > _maxIndex))
			return null;

		return xlateRemainingYearAndTime(monthAndDay[0], monthAndDay[1], bWeekday, curPos, nextPos);
	}
	/* Format: 07/12/2000 19:23:00 */
	private Calendar xlateShortDateValue (final int startPos, final int endPos)
	{
		final byte[]	dayAndMonth={ 0, 0 };
		int				nextPos=xlateTimeElement(startPos, '/', dayAndMonth, 0, (byte) 31);
		if ((nextPos <= startPos) || (nextPos > endPos))
			return null;

		if (((nextPos=xlateTimeElement(nextPos, '/', dayAndMonth, 1, (byte) 31)) <= startPos) || (nextPos > endPos))
			return null;
		
		// now try and see which is the day and which is the month - NOTE: ambiguity may occur
		if (dayAndMonth[1] > 12)
		{	
			final byte	bDay=dayAndMonth[1];
			dayAndMonth[1] = dayAndMonth[0];
			dayAndMonth[0] = bDay;
		}

		return xlateRemainingYearAndTime(dayAndMonth[0], dayAndMonth[1], Byte.MIN_VALUE /* unknown weekday */, nextPos, endPos);
	}
	/**
	 * Updates the rest of the values - assumes only year, time and TZ value(s)
	 * remain to be set from starting position
	 * @param bDay extracted day-of-month (1-31)
	 * @param bMonth extract month-of-year (1-12)
	 * @param startPos position in parse buffer to use (inclusive)
	 * @return updated calendar object (null if error(s))
	 */
	private Calendar xlateRemainingYearAndTime (final byte bDay, final byte bMonth, final int startPos)
	{
		final int	yearStart=_ps.findNumberStart(startPos), yearEnd=_ps.findNumberEnd(yearStart+1);
		if ((yearStart < startPos) || (yearStart >= _maxIndex) || (yearEnd <= yearStart) || (yearEnd > _maxIndex))
			return null;
		
		return xlateRemainingYearAndTime(bDay, bMonth, Byte.MIN_VALUE /* unknown weekday */, yearStart, yearEnd);
	}
	/* Format: Jun 14 2002 04:59:08 */
	private Calendar xlateVersionC (final byte bMonth, final int startPos)
	{
		final int	dayStart=_ps.findNumberStart(startPos), dayEnd=_ps.findNumberEnd(dayStart+1);
		if ((dayStart < startPos) || (dayStart >= _maxIndex) || (dayEnd <= dayStart) || (dayEnd > _maxIndex))
			return null;

		final byte	bDay=xlateDay(dayStart, dayEnd);
		if ((bDay < 1) || (bDay > 31))
			return null;

		return xlateRemainingYearAndTime(bDay, bMonth, dayEnd);
	}
	/* Format is:
	 *
	 *	"%02u %s %u %02u:%02u[:%02u] GMT"
	 *
	 * Example(s):
	 *		"04 Apr 99 18:38 EST"
	 *		"12 Jun 1998 03:25:44 EST"
	 */
	private Calendar xlateVersionB (final int dayStart, final int dayEnd)
	{
		final byte	bDay=xlateDay(dayStart, dayEnd);
		if ((bDay < 1) || (bDay > 31))
			return null;
		
		final int	monthStart=_ps.findNonEmptyDataStart(dayEnd), monthEnd=_ps.findNonEmptyDataEnd(monthStart+1);
		if ((monthStart <= dayEnd) || (monthEnd <= monthStart) || (monthEnd > _maxIndex))
			return null;
		
		final byte	bMonth=xlateMonthName(monthStart, monthEnd);
		if ((bMonth < 1) || (bMonth > 12))
			return null;

		return xlateRemainingYearAndTime(bDay, bMonth, monthEnd);
	}
	/**
	 * Format is:</BR>
	 * <PRE>
	 *		"%s %s %02u %02u:%02u:%02u GMT %04u"
	 * 		Example: "Wed Jun 09 18:38:07 PDT 1999"
	 * </PRE>
	 * @param startPos first position <U>after</U> the weekday to start
	 * parsing (inclusive)
	 * @param bWeekday extracted weekday - 1=Sunday, 2=Monday, etc...
	 * @return calendar value representing the extracted date/time values
	 * @throws IllegalStateException if ERA not "AD" (which should be NEVER...)
	 */
	private Calendar xlateVersionA (final int startPos, final byte bWeekday) throws IllegalStateException
	{
		final int	monthStart=_ps.findNonEmptyDataStart(startPos), monthEnd=_ps.findNonEmptyDataEnd(monthStart+1);
		if ((monthStart < startPos) || (monthEnd <= monthStart) || (monthEnd > _maxIndex))
			return null;

		final byte	bMonth=xlateMonthName(monthStart, monthEnd);
		if ((bMonth < 1) || (bMonth > 12))
			return null;
		
		final int	dayStart=_ps.findNumberStart(monthEnd), dayEnd=_ps.findNumberEnd(dayStart+1);
		if ((dayStart < monthEnd) || (dayEnd <= dayStart) || (dayEnd > _maxIndex))
			return null;

		final byte	bDay=xlateDay(dayStart, dayEnd);
		if ((bDay < 1) || (bDay > 31))
			return null;

		final byte[]	tmVals={ 0, 0, 0 };
		int	curPos=xlateTimeValues(_ps.findNumberStart(dayEnd), tmVals);
		if ((curPos <= dayEnd) || (curPos > _maxIndex))
			return null;
		
		final int	gmtStart=_ps.findNonEmptyDataStart(curPos), gmtEnd=_ps.findNonEmptyDataEnd(gmtStart+1);
		if ((gmtStart < curPos) || (gmtEnd <= gmtStart) || (gmtEnd > _maxIndex))
			return null;

		// if digit found, then no GMT zone specified
		final boolean	haveGmt=(!_ps.isDigit(gmtStart));
		short			yearVal=(-1);
		if (haveGmt)
		{	
			final int	yearStart=_ps.findNumberStart(gmtEnd), yearEnd=_ps.findNumberEnd(yearStart+1);
			if ((yearStart < gmtEnd) || (yearStart >= _maxIndex) || (yearEnd <= yearStart) || (yearEnd > _maxIndex))
				return null;

			if ((yearVal=xlateYearValue(yearStart, yearEnd)) < 0)
				return null;
		}
		else
		{
			if ((yearVal=xlateYearValue(gmtStart, gmtEnd)) < 0)
				return null;
		}

		final Calendar	cdt=new GregorianCalendar(yearVal, (bMonth - 1) + Calendar.JANUARY, bDay, tmVals[0], tmVals[1], tmVals[2]);
		cdt.set(Calendar.ERA, GregorianCalendar.AD);
		cdt.set(Calendar.MILLISECOND, 0);
		adjustDayOfWeek(bWeekday, cdt);

		if (haveGmt)
		{
			final TimeZone	abbrevTZ=getAbbreviatedTimeZone(_ps, gmtStart - _ps.getStartIndex(), (gmtEnd - gmtStart));
			if (abbrevTZ != null)
				cdt.setTimeZone(abbrevTZ);
		}

		// this actually forces a re-calculation of the internal milliseconds value
		if (cdt.get(Calendar.ERA) != GregorianCalendar.AD)
			throw new IllegalStateException("ERA value mismatch: value=" + cdt.get(Calendar.ERA) + " expected=" + GregorianCalendar.AD);

		return cdt;
	}
	/**
	 * Attempts to decode the value supplied in the constructor
	 * @return calendar object - null if unable to parse
	 * @throws IllegalStateException if ERA not "AD" (which should be NEVER...)
	 */
	public final Calendar decodeValue () throws IllegalStateException
	{
		if (null == _ps)	// can occur if null/empty value to begin with
			return null;

		final int	startPos=_ps.findNonEmptyDataStart();
		if ((startPos < _startIndex) || (startPos >= _maxIndex))
			return null;

		int	curPos=_ps.findNonEmptyDataEnd(startPos+1);
		if ((curPos <= startPos) || (curPos > _maxIndex))
			return null;

		// some non-standard dates do not contain a day of week separator
		if (',' == _ps.getCharAt(curPos-1))
			return xlateStdDateValue(curPos, xlateWeekdayName(startPos, curPos-1));

		// check if "day/month/year"
		final int	sepPos=_ps.indexOf('/', startPos, curPos);
		if ((sepPos > startPos) && (sepPos < curPos))
			return xlateShortDateValue(startPos, curPos);
		
		final byte	bMonth=xlateMonthName(startPos, curPos);
		if ((bMonth >= 1) && (bMonth <= 12))
			return xlateVersionC(bMonth, curPos);
		
		if (_ps.isDigit(startPos))
			return xlateVersionB(startPos, curPos);
		
		final byte	bWeekday=xlateWeekdayName(startPos, curPos);
		if ((bWeekday >= 1) && (bWeekday <= 7))
			return xlateVersionA(curPos, bWeekday);

		return null;
	}
}
