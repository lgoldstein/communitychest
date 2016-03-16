package net.community.chest.util.datetime;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import net.community.chest.ParsableString;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.ArraysUtils;
import net.community.chest.util.collection.CollectionsUtils;
import net.community.chest.util.locale.LocalesMap;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 22, 2007 10:16:25 AM
 */
public final class DateUtil {
    // some useful date related constants
    public static final int    MSEC_PER_SECOND=1000,
                            SECONDS_PER_MINUTE=60,
                            MINUTES_PER_HOUR=60,
                            SECONDS_PER_HOUR=MINUTES_PER_HOUR * SECONDS_PER_MINUTE,
                            HOURS_PER_DAY=24,
                            MINUTES_PER_DAY=HOURS_PER_DAY * MINUTES_PER_HOUR,
                            SECONDS_PER_DAY=HOURS_PER_DAY * SECONDS_PER_HOUR,
                            DAYS_PER_WEEK=7,
                            HOURS_PER_WEEK=DAYS_PER_WEEK * HOURS_PER_DAY,
                            MINUTES_PER_WEEK=DAYS_PER_WEEK * MINUTES_PER_DAY,
                            SECONDS_PER_WEEK=DAYS_PER_WEEK * SECONDS_PER_DAY,
                            DAYS_PER_YEAR=365,
                            MAX_DAYS_PER_MONTH=31,
                            SECONDS_PER_YEAR=DAYS_PER_YEAR * SECONDS_PER_DAY,
                            MINUTES_PER_YEAR=DAYS_PER_YEAR * MINUTES_PER_DAY,
                            HOURS_PER_YEAR=DAYS_PER_YEAR * HOURS_PER_DAY,
                            WEEKS_PER_YEAR=52,    // +/- a few days
                            MONTHS_PER_YEAR=12,
                            FEB_DAYS_IN_LEAP_YEAR=29,
                            DAYS_PER_LEAP_YEAR=DAYS_PER_YEAR + 1,
                            MAX_WEEKS_PER_MONTH=5;

    private DateUtil()
    {
        // no instance
    }
    // internal indices
    public static final int DATE_DAY=0, DATE_MONTH=1, DATE_YEAR=2, NUM_DATE_COMPONENTS=DATE_YEAR+1;
    /**
     * Convert a {@link String} with <code>ddmmyyyy</code> or <code>ddmmyy</code>
     * form into its component values. The day/month/year components may
     * have an optional separator char
     * @param date The string to be converted - if null/empty then nothing
     * is done
     * @param sepChar Day/month/year components separator - 'no separator' is
     * marked by the 'empty' zero character value
     * @return An array of <code>int</code>-s with the components (use the
     * {@link #DATE_DAY}, {@link #DATE_MONTH} and {@link #DATE_YEAR} as
     * index to access the array)
     * @throws IllegalArgumentException if malformed format
     */
    public static final int[] getDateComponents (final String date, final char sepChar) throws IllegalArgumentException
    {
        final int    dtLen=(null == date) ? 0 : date.length();
        if ((null == date) || (date.length() <= 0))
            return null;

        // day/ month / year
        final List<String>    cl;
        if ('\0' == sepChar)
        {
            if (dtLen < (NUM_DATE_COMPONENTS * 2))
                throw new IllegalArgumentException("getDateComponents(" + date + ") string too short");

            //                        day                    month                    year
            cl = Arrays.asList(date.substring(0, 2), date.substring(2, 4), date.substring(4));
        }
        else
            cl = StringUtil.splitString(date, sepChar);

        final int    numComps=(null == cl) ? 0 : cl.size();
        if (numComps != NUM_DATE_COMPONENTS)
            throw new IllegalArgumentException("getDateComponents(" + date + ") failed to extract components");

        final int [] dateInt=new int[NUM_DATE_COMPONENTS];
        // take the string date
        for (int i=0; i < NUM_DATE_COMPONENTS; i++)
        {
            final String    str=cl.get(i);
            final int        sLen=(null == str) ? 0 : str.length();
            try
            {
                switch(i)
                {
                    case DATE_DAY    :
                        if (sLen > 2)
                            throw new IllegalArgumentException("parseStringToDate(" + date + ") bad day value: " + str);
                        break;

                    case DATE_MONTH    :
                        if (sLen < 2)
                            throw new IllegalArgumentException("parseStringToDate(" + date + ") bad month value: " + str);
                        if (sLen > 2)
                        {
                            // assume a month name - short or long
                            final int    mthIndex;
                            if (ABBREV_MOY_LEN == sLen)
                                mthIndex = getAbbreviatedMonthNameIndex(str, false);
                            else
                                mthIndex = getFullMonthNameIndex(str, false);
                            if ((mthIndex < 0) || (mthIndex >= MONTHS_PER_YEAR))
                                throw new IllegalArgumentException("parseStringToDate(" + date + ") unknown abbrev. month value: " + str);

                            dateInt[i] = mthIndex + 1;
                            continue;
                        }
                        break;

                    case DATE_YEAR    :
                        if (2 == sLen)
                        {
                            final int    cValue=Integer.parseInt(str);
                            if ((cValue < 0) || (cValue >= 100))
                                throw new IllegalArgumentException("parseStringToDate(" + date + ") bad short year value: " + str);

                            // get base century year
                            final Calendar    c=Calendar.getInstance();
                            final int        cYear=c.get(Calendar.YEAR),
                                            rYear=cYear % 100,
                                            bYear=cYear - rYear;
                            dateInt[i] = bYear + cValue;
                            continue;
                        }

                        if (sLen != 4)
                            throw new IllegalArgumentException("parseStringToDate(" + date + ") bad year value: " + str);
                        break;

                    default            :
                        throw new IllegalArgumentException("parseStringToDate(" + date + ") too many components: " + str);
                }

                dateInt[i] = Integer.parseInt(str);
            }
            catch (NumberFormatException e)
            {
                throw new IllegalArgumentException("parseStringToDate(" + date + ") bad/illegal value (" + str + "): " + e.getMessage());
            }
        }

        // make sure ALL values initialized and seem valid
        for (int    dPos=0; dPos < dateInt.length; dPos++)
        {
            if (dateInt[dPos] <= 0)
                throw new IllegalArgumentException("parseStringToDate(" + date + ") bad/illegal component: " + dateInt[dPos]);
        }

        return dateInt;
    }
    /**
     * Convert a {@link String} with <code>dd/mm/yyyy</code> or <code>dd/mm/yy</code>
     * form into its component values
     * @param date The string to be converted - if null/empty then nothing
     * is done
     * @return An array of <code>int</code>-s with the components (use the
     * {@link #DATE_DAY}, {@link #DATE_MONTH} and {@link #DATE_YEAR} as
     * index to access the array)
     * @throws IllegalArgumentException if malformed format
     */
    public static final int[] getDateComponents (final String date) throws IllegalArgumentException
    {
        return getDateComponents(date, '/');
    }
    /**
     * Convert a string in a format <I>dd/mm/yyyy</I> to {@link Calendar} object
     * @param date date string - may NOT be null/empty
     * @param lenient Specify whether or not date/time interpretation is to be lenient.
     * With lenient interpretation, a date such as "28/02/2003" will be treated as being
     * equivalent to the 01/03/2003.
     * With strict interpretation, such dates will cause an exception to be thrown.
     * @return {@link Calendar} object that represent the time of the input
     * string
     * @throws IllegalArgumentException if malformed date value
     */
    public static final Calendar parseStringToDate (final String date, final boolean lenient) throws IllegalArgumentException
    {
        final int[]        dateInt=getDateComponents(date);
        if ((null == dateInt) || (dateInt.length < 3))
            throw new IllegalArgumentException("parseStringToDate(" + date + ") no data");

        final Calendar    cal=Calendar.getInstance();
        cal.setLenient(lenient);
        cal.set(dateInt[DATE_YEAR], Calendar.JANUARY + (dateInt[DATE_MONTH] - 1), dateInt[DATE_DAY], 0, 0, 0);

        // calling getTime will make validation on the date and trow exception if need.
        cal.getTime();
        return cal;
    }
    // internal indices
    public static final int TIME_HOUR=0, TIME_MINUTE=1, TIME_SECOND=2, NUM_TIME_COMPONENTS=TIME_SECOND+1;
    /**
     * Convert a string in a format <I>hh:mm:ss</I> to its components.
     * @param time The time {@link String} to convert - if null/empty then
     * nothing is done
     * @param lenient TRUE=minutes and/or seconds components may be omitted - in
     * which case they will be considered to be zero, FALSE=all values must
     * be specified
     * @return Components array - use {@link #TIME_HOUR}, {@link #TIME_MINUTE}
     * and {@link #TIME_SECOND} for accessing the relevant component
     * @throws IllegalArgumentException if malformed value
     */
    public static final int[] getTimeComponents (final String time, final boolean lenient) throws IllegalArgumentException
    {
        if ((null == time) || (time.length() <= 0))
            return null;

        final List<String>    tl=StringUtil.splitString(time, DEFAULT_TMSEP);
        final int            numComps=(null == tl) ? 0 : tl.size();
        if (numComps != NUM_TIME_COMPONENTS)
        {
            if ((!lenient)
             || (lenient && ((numComps <= 0) || (numComps > NUM_TIME_COMPONENTS))))
                throw new IllegalArgumentException("getTimeComponents(" + time + ") bad format");
        }
        // hour/minute/second
        final int [] timeInt=new int[NUM_TIME_COMPONENTS];
        // take the string date
        for (int i=0; i < numComps; i++)
        {
            final String    str=tl.get(i);
            final int        sLen=(str == null) ? 0 : str.length();
            if (sLen != 2)
            {
                if ((!lenient)
                 || (lenient && ((sLen <= 0) || (sLen > 2))))
                throw new IllegalArgumentException("parseStringToTime(" + time + ") bad component (" + str + ") should be (hh:mm:ss)");
            }

            try
            {
                if ((timeInt[i]=Integer.parseInt(str)) < 0)
                    throw new NumberFormatException("Unexpected negative value");
            }
            catch (NumberFormatException e)
            {
                throw new IllegalArgumentException("parseStringToTime(" + time + ") bad/illegal value (" + str + "): " + e.getMessage());
            }
        }

        return timeInt;
    }
    /**
     * Convert a string in a format <I>hh:mm:ss</I> to its components.
     * <B>Note:</B> minutes and/or seconds components may be omitted - in
     * which case they will be considered to be zero
     * @param time The time {@link String} to convert - if null/empty then
     * nothing is done
     * @return Components array - use {@link #TIME_HOUR}, {@link #TIME_MINUTE}
     * and {@link #TIME_SECOND} for accessing the relevant component
     * @throws IllegalArgumentException if malformed value
     */
    public static final int[] getTimeComponents (final String time) throws IllegalArgumentException
    {
        return getTimeComponents(time, false);
    }
    /**
     * Convert a string in a format <I>hh:mm:ss</I> to {@link Calendar} object.
     * @param time time string - may NOT be null/empty
     * @return {@link Calendar} object that represent the time of the input
     * string (the date is the current date)
     * @throws IllegalArgumentException if malformed time value
     */
    public static final Calendar parseStringToTime (final String time) throws IllegalArgumentException
    {
        final int[]    timeInt=getTimeComponents(time);
        if ((null == timeInt) || (timeInt.length < 3))
            throw new IllegalArgumentException("parseStringToTime(" + time + ") no data");

        final Calendar cal=Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                timeInt[TIME_HOUR], timeInt[TIME_MINUTE], timeInt[TIME_SECOND]);

        // calling getTime will make validation on the date and trow exception if need.
        cal.getTime();
        return cal;
    }
    /**
     * Converts a date/time value formatted as <I>dd/mm/yyyy hh:mm:ss</I> to
     * its {@link Calendar} counterpart
     * @param dt The date/time value - may NOT be null/empty
     * @param lenient TRUE=use leninet rollover date
     * @return Re-constructed {@link Calendar}
     * @throws IllegalArgumentException if malformed data
     */
    public static final Calendar parseStringToDatetime (final String dt, final boolean lenient) throws IllegalArgumentException
    {
        final int    dtLen=(null == dt) ? 0 : dt.length(),
                    spPos=(dtLen <= 1) ? (-1) : dt.indexOf(' ');
        final String    date=(spPos > 0) && (spPos < (dtLen-1)) ? dt.substring(0, spPos) : null,
                        time=(spPos > 0) && (spPos < (dtLen-1)) ? dt.substring(spPos+1) : null;
        final int[]    dateInt=getDateComponents(StringUtil.getCleanStringValue(date)),
                    timeInt=getTimeComponents(StringUtil.getCleanStringValue(time));
        if ((null == dateInt) || (dateInt.length < 3)
         ||    (null == timeInt) || (timeInt.length < 3))
            throw new IllegalArgumentException("parseStringToDatetime(" + dt + ") bad data");

        final Calendar cal=Calendar.getInstance();
        cal.setLenient(lenient);
        cal.set(dateInt[DATE_YEAR], Calendar.JANUARY + (dateInt[DATE_MONTH] - 1), dateInt[DATE_DAY],
                timeInt[TIME_HOUR], timeInt[TIME_MINUTE], timeInt[TIME_SECOND]);

        // calling getTime will make validation on the date and trow exception if need.
        cal.getTime();
        return cal;
    }
    /**
     * Convert date timestamp to a string in the format DD/MM/YYYY.
     * @param cal The {@link Calendar} value to convert - if null
     * then null string returned
     * @return {@link String} in DD/MM/YYYY format that represent the input.
     */
    public static final String parseDateToString (final Calendar cal)
    {
        if (null == cal)
            return null;

        try
        {
            final StringBuilder sb=new StringBuilder(16);
            StringUtil.appendPaddedNum(sb, cal.get(Calendar.DAY_OF_MONTH), 2);
            sb.append('/');
            StringUtil.appendPaddedNum(sb, (1 + (cal.get(Calendar.MONTH) - Calendar.JANUARY)), 2);
            sb.append('/');
            StringUtil.appendPaddedNum(sb, cal.get(Calendar.YEAR), 4);

            return sb.toString();
        }
        catch(IOException e)
        {
            // should not happen since no exceptions expected
            throw new RuntimeException(e);
        }
    }
    /**
     * Convert date timestamp to a string in the format DD/MM/YYYY.
     * @param date the timestamp value to convert
     * @return String in day/month/year format that represent the input.
     */
    public static final String parseDateToString (final long date)
    {
        final Calendar    cal=Calendar.getInstance();
        cal.setTimeInMillis(date);
        return parseDateToString(cal);
    }

    public static final String parseTimeToString (final Calendar timeVal)
    {
        if (null == timeVal)
            return null;

        try
        {
            final StringBuilder sb=new StringBuilder(32);
            StringUtil.appendPaddedNum(sb, timeVal.get(Calendar.HOUR_OF_DAY), 2);
            sb.append(DEFAULT_TMSEP);
            StringUtil.appendPaddedNum(sb, timeVal.get(Calendar.MINUTE), 2);
            sb.append(DEFAULT_TMSEP);
            StringUtil.appendPaddedNum(sb, timeVal.get(Calendar.SECOND), 2);

            return sb.toString();
        }
        catch(IOException e)
        {
            // should not happen since no exceptions expected
            throw new RuntimeException(e);
        }
    }

    public static final String parseTimeToString (final long time)
    {
        final Calendar    cal=Calendar.getInstance();
        cal.setTimeInMillis(time);
        return parseTimeToString(cal);
    }
    /**
     * Create time string in the format DD/MM/YYYY HH:MM:SS.
     * The time is generate from the system current time.
     * @param timeVal The time to parse - if null then use current machine time.
     * @return String of current time int format DD/MM/YYYY HH:MM:SS.
     */
    public static final String parseDateTimeToString (final Calendar timeVal)
    {
        final Calendar    curTime=(null == timeVal) ? Calendar.getInstance() : timeVal;
        return parseDateToString(curTime) + " " + parseTimeToString(curTime);
    }

    public static final String parseDateTimeToString (final long timeVal)
    {
        final Calendar    cal=Calendar.getInstance();
        cal.setTimeInMillis(timeVal);
        return parseDateTimeToString(cal);
    }
    /**
     * Creates a {@link Calendar} instance initialized to the provided date/time/msec. value(s)
     * @param dtv The date components array indexed using {@link #DATE_YEAR},
     * {@link #DATE_MONTH} and {@link #DATE_DAY}
     * @param ttv The time components array indexed using {@link #TIME_HOUR},
     * {@link #TIME_MINUTE}, {@link #TIME_SECOND}
     * @param msec The milliseconds value
     * @return The initialized {@link Calendar} instance
     * @throws IllegalStateException if bad value initialized (e.g., B.C. date)
     */
    public static final Calendar createCalendarValue (int[] dtv, int[] ttv, int msec)
        throws IllegalStateException
    {
        final Calendar    c=Calendar.getInstance();
        c.set(dtv[DATE_YEAR], Calendar.JANUARY + (dtv[DATE_MONTH] - 1), dtv[DATE_DAY],
              ttv[TIME_HOUR], ttv[TIME_MINUTE], ttv[TIME_SECOND]);
        c.set(Calendar.MILLISECOND, msec);

        if (c.get(Calendar.ERA) != GregorianCalendar.AD)    // force a re-calculation
            throw new IllegalStateException("Bad ERA for "
                + dtv[DATE_YEAR] + "-" + dtv[DATE_MONTH] + "-" + dtv[DATE_DAY]
                + " " + ttv[TIME_HOUR] + ":" + ttv[TIME_MINUTE] + ":" + ttv[TIME_SECOND]
                + "." + msec);

        return c;
    }

    public static final boolean isLeapYear (final int year)
    {
        // must be at least a multiple of 4
        if ((year & 0x03) != 0)
            return false;

        // every hundred years not a leap year
        if (0 == (year % 100))
        {
            // every 400 years still a leap year
            if ((year % 400) != 0)
                return false;
        }

        return true;
    }
    // special DoomsDay alogrithm calculation
    private static final boolean doomsdayLeapYear (final int year)
    {
        final int    modFour=year & 0x0003, modHundred=year % 100;
        if (((0 == modFour) && (modHundred != 0)) ||
            ((0 == modHundred) && (0 == (modHundred & 0x0003))))
            return true;
        else
            return false;
    }
    // special DoomsDay alogrithm calculation
    private static final int doomsdayMonth (final int cnMonth /* 1=JANUARY */, final boolean fLeapYear)
    {
        switch(cnMonth)
        {
            case 1    :    /* JAN */
                if (fLeapYear)
                    return 32;
                else
                    return 31;

            case 2    :    /* FEB */
                if (fLeapYear)
                    return 29;
                else
                    return 28;

            case 3    : /* MAR */
                return 7;

            case 4    : /* APR */
                return  4;

            case 5    :    /* MAY */
                return 9;

            case 6    :    /* JUN */
                return 6;

            case 7    :    /* JUL */
                return 11;

            case 8    :    /* AUG */
                return 8;

            case 9    :    /* SEP */
                return 5;

            case 10    :    /* OCT */
                return 10;

            case 11    :    /* NOV */
                return 7;

            case 12    :    /* DEC */
                return 12;

            default    :    /* should not reach this point */
        }

        return (-1);
    }
    // special DoomsDay alogrithm calculation
    private static final int doomsdayCentury (final int nCentury)
    {
        final int    mod400=(nCentury % 400);
        switch(mod400)
        {
            case   0    :    return 2;
            case 100    :    return 0;
            case 200    :    return 5;
            case 300    :    return 3;
            default        :    /* should not reach this point */
        }

        return (-2);
    }
    /**
     * Calculates the day-of-week for the given date
     * @param day day of month
     * @param month month of year - according to the Calendar constants (!)
     * @param year year value - Note: only years >= 0 are supported
     * @return day of week - according to Calendar (!) constants. If error,
     * then value is NOT within Calendar.SUNDAY-Calendar.SATURDAY range
     * @see Calendar for months and days of week constants
     */
    public static int getDayOfWeekForDate (final int day, final int month, final int year)
    {
        final int    cnMonth=1 + (month - Calendar.JANUARY);    // we need month in range 1-12
        if ((day <= 0) || (month < Calendar.JANUARY) || (month > Calendar.DECEMBER) || (year < 0))
            return (-1);

        final boolean    fLeapYear=doomsdayLeapYear(year);
        switch(cnMonth)
        {
            case  4: /* 30 day months */
            case  6:
            case  9:
            case 11:
                if (day > 30)
                    return (-2);
                break;

            case 2:
                if ((fLeapYear && (day > 29)) || ((!fLeapYear) && (day > 28)))
                    return (-3);
                break;

            default    : /* OK */
        }
        final int    nCentury=(year - (year % 100)),
                    ddCentury=doomsdayCentury(nCentury),
                    ddMonth=doomsdayMonth(cnMonth, fLeapYear);
        if ((ddMonth <= 0) || (ddCentury < 0 ))
            return (-4);

        final int    ddDay=(ddMonth > day) ? (7 - ((ddMonth - day) % 7) + ddMonth) : day;

        int    x=(ddDay - ddMonth);
        x %= 7;

        int y = ddCentury + (year - nCentury) + /* floor=> */ ((year - nCentury) >> 2);
        y %= 7;

        final int    weekDay=(1 + ((x + y) % 7));    // 1=Sunday
        return ((weekDay - 1) + Calendar.SUNDAY);
    }
    /**
     * Calculates the day-of-week for the given date
     * @param cal date value (error if null)
     * @return day of week - according to Calendar constants (or <0 if error)
     * @see Calendar for months and days of week constants
     */
    public static int getDayOfWeekForDate (Calendar cal)
    {
        return (null == cal) ? (-1) : getDayOfWeekForDate(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH), cal.get(Calendar.YEAR));
    }
    /**
     * Range of years for which the seconds calculation function works
     */
    public static final int    MIN_GMT_OFFSET_YEAR=1970,
                            MAX_GMT_OFFSET_YEAR=2038,
                            NUM_GMT_OFFSET_YEARS=(MAX_GMT_OFFSET_YEAR - MIN_GMT_OFFSET_YEAR);
    /**
     * Offset (sec.) from 1/1/1970 00:00:00 +0000 of 1/1/yyyy of each year
     */
    public static final long[]    yearGmtOffsets=new long[NUM_GMT_OFFSET_YEARS];
    // initialize the GMT offsets of the various years
    static
    {
        long    yearOffset=0L;
        for (int    year=MIN_GMT_OFFSET_YEAR; year < MAX_GMT_OFFSET_YEAR; year++)
        {
            yearGmtOffsets[year - MIN_GMT_OFFSET_YEAR] = yearOffset;
            yearOffset += SECONDS_PER_YEAR;
            if (doomsdayLeapYear(year))
                yearOffset += SECONDS_PER_DAY;
        }
    }
    /**
     * Number of days per month (0=Jan.) for non-leap years
     */
    public static final int daysPerMonth[]={
            31 /* Jan. */, 28 /* Feb. */, 31 /* Mar. */, 30 /* Apr. */, 31 /* May. */, 30 /* Jun. */,
            31 /* Jul. */, 31 /* Aug. */, 30 /* Sep. */, 31 /* Oct. */, 30 /* Nov. */, 31 /* Dec. */
    };
    public static final int getDaysPerMonth (int m /* Jan.=0 */, int y)
    {
        if ((m < 0) || (m >= daysPerMonth.length))
            return (-1);

        final int    d=daysPerMonth[m];
        if ((1 == m) && isLeapYear(y))
            return d + 1;

        return d;
    }
    /**
     * Initializes an abbreviated names array from the given full names one
     * @param fullNames full names of month/day
     * @param abbrevLen abbreviated number of characters
     * @return abbreviated names array (same length as full names one)
     */
    private static final char[][] initAbbrevNamesChars (final char[][] fullNames, final int abbrevLen)
    {
        final int        numNames=(null == fullNames) ? 0 : fullNames.length;
        final char[][] abbrevNames=new char[numNames][];
        for (int fullIndex=0; fullIndex < numNames; fullIndex++)
        {
            final char[]    fullName=fullNames[fullIndex];
            final int        fnLen=(null == fullName) ? 0 : fullName.length;
            final int        cpyLen=Math.min(fnLen, abbrevLen);
            final char[]    abbrevName=new char[cpyLen];
            if (cpyLen > 0)
                System.arraycopy(fullName, 0, abbrevName, 0, cpyLen);
            abbrevNames[fullIndex] = abbrevName;
        }

        return abbrevNames;
    }
    /**
     * Full names of months (English) as string(s)
     */
    public static final List<String>    fullMonthsNames=
        Collections.unmodifiableList(Arrays.asList(
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December"
        ));
    /**
     * Full names of months (English) as char-array(s)
     */
    public static final char[][]    fullMonthsNamesChars=ArraysUtils.initCharsFromStrings(fullMonthsNames);
    /**
     * @param n String containing the full month name
     * @param caseSensitive TRUE if comparison should be made case-sensitive
     * @return Month index (JAN=0, FEB=1, etc.) - negative if no match found
     */
    public static final int getFullMonthNameIndex (final String n, final boolean caseSensitive)
    {
        return caseSensitive
             ? CollectionsUtils.findElementIndex(n, fullMonthsNames)
             : CollectionsUtils.findElementIndex(n, String.CASE_INSENSITIVE_ORDER, fullMonthsNames)
             ;
    }
    /**
     * Number of characters in abbreviated month name
     */
    public static final int    ABBREV_MOY_LEN=3;
    /**
     * Abbreviated names of months (English) as char-array(s)
     */
    public static final char[][]    abbrevMonthsNamesChars=initAbbrevNamesChars(fullMonthsNamesChars, ABBREV_MOY_LEN);
    /**
     * Abbreviated names of months (English) as string(s)
     */
    public static final List<String>    abbrevMonthsNames=
        Collections.unmodifiableList(Arrays.asList(ArraysUtils.initStringsFromChars(abbrevMonthsNamesChars)));
    /**
     * @param n String containing the abbreviated month name
     * @param caseSensitive TRUE if comparison should be made case-sensitive
     * @return Month index (JAN=0, FEB=1, etc.) - negative if no match found
     */
    public static final int getAbbreviatedMonthNameIndex (final String n, final boolean caseSensitive)
    {
        final int    nLen=(null == n) ? 0 : n.length();
        // if length not EXACTLY as expected no need to go further
        if (nLen != ABBREV_MOY_LEN)
            return (-1);

        return caseSensitive
             ? CollectionsUtils.findElementIndex(n, abbrevMonthsNames)
             : CollectionsUtils.findElementIndex(n, String.CASE_INSENSITIVE_ORDER, abbrevMonthsNames)
             ;
    }
    /**
     * Full names of weekdays (English) as string(s)
     */
    public static final List<String>    fullDaysNames=
        Collections.unmodifiableList(Arrays.asList(
            "Sunday",
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday"
        ));
    /**
     * Full names of weekdays (English)
     */
    public static final char[][]    fullDaysNamesChars=ArraysUtils.initCharsFromStrings(fullDaysNames);
    /**
     * Number of characters in abbreviated day name
     */
    public static final int    ABBREV_DOW_LEN=3;
    /**
     * Abbreviated names of weekdays (English) as char-array(s)
     */
    public static final char[][]    abbrevDaysNamesChars=initAbbrevNamesChars(fullDaysNamesChars, ABBREV_DOW_LEN);
    /**
     * Abbreviated names of weekdays (English) as string(s)
     */
    public static final List<String>    abbrevDaysNames=
        Collections.unmodifiableList(Arrays.asList(ArraysUtils.initStringsFromChars(abbrevDaysNamesChars)));

    private static final <E extends Enum<E>> Map<E,String> initMapFromStrings (final Class<E> eClass, final List<String> names)
    {
        final Map<E,String>    m=new EnumMap<E, String>(eClass);
        final E[]            ea=eClass.getEnumConstants();
        for (final E ev : ea)
        {
            final String    n=names.get(ev.ordinal());
            m.put(ev, n);
        }

        return m;
    }
    /**
     * A {@link Map} of abbreviated/full names of weekdays (English) as string(s)
     * where key={@link DaysValues} and value=the string
     */
    public static final Map<DaysValues,String>    abbrevDaysNamesMap=initMapFromStrings(DaysValues.class, abbrevDaysNames),
                                                fullDaysNamesMap=initMapFromStrings(DaysValues.class, fullDaysNames);
    /**
     * Offset of 00:00:00 1st of each month (0=Jan.) from 1/1 of the dame (non-leap) year
     */
    public static final long    monthGmtOffsets[]=new long[MONTHS_PER_YEAR];
    // initialize months offsets
    static
    {
        long    monthOffset=0L;
        for (int    month=0; month < MONTHS_PER_YEAR; month++)
        {
            monthGmtOffsets[month] = monthOffset;
            monthOffset += daysPerMonth[month] * (long) SECONDS_PER_DAY;
        }
    }
    /**
     * @param c calendar object
     * @return offset (sec.) since 1/1/1970 00:00:00 +0000 of specified date/time - <0 if error
     */
    public static final long getDateTimeGMTOffset (Calendar c)
    {
        long    timeValue=0L;
        if (null == c)
            return (-1L);

        final int        tmYear=c.get(Calendar.YEAR);
        final boolean    fLeapYear=doomsdayLeapYear(tmYear);
        {
            if ((tmYear < MIN_GMT_OFFSET_YEAR) || (tmYear >= MAX_GMT_OFFSET_YEAR))
                return (-2L);
            timeValue += yearGmtOffsets[tmYear - MIN_GMT_OFFSET_YEAR];
        }

        {
            final int    orgMon=c.get(Calendar.MONTH), tmMon=orgMon - Calendar.JANUARY;
            if ((tmMon < 0) || (tmMon >= MONTHS_PER_YEAR))
                return (-3L);
            timeValue += monthGmtOffsets[tmMon];

            /* if date beyond FEB and this is a leap year, add one day offset */
            if ((orgMon > Calendar.FEBRUARY) && fLeapYear)
                timeValue += SECONDS_PER_DAY;

            /* validate day-of-month value */
            final int    tmDay=c.get(Calendar.DAY_OF_MONTH);
            if ((tmDay <= 0) || (tmDay > daysPerMonth[tmMon]))
            {
                /* if this is not Feb., then error */
                if ((orgMon != Calendar.FEBRUARY) || (tmDay > FEB_DAYS_IN_LEAP_YEAR))
                    return (-4L);

                if ((FEB_DAYS_IN_LEAP_YEAR == tmDay) && (!fLeapYear))
                    return (-5L);
            }
            /* calculate TILL 00:00:00 of the specified day */
            timeValue += (tmDay - 1) * (long) SECONDS_PER_DAY;
        }

        {
            final int    tmHour=c.get(Calendar.HOUR_OF_DAY);
            if ((tmHour < 0) || (tmHour >= HOURS_PER_DAY))
                return (-6L);
            timeValue += tmHour * SECONDS_PER_HOUR;
        }

        {
            final int    tmMin=c.get(Calendar.MINUTE);
            if ((tmMin < 0) || (tmMin >= MINUTES_PER_HOUR))
                return (-7L);
            timeValue += tmMin * SECONDS_PER_MINUTE;
        }

        {
            final int    tmSec=c.get(Calendar.SECOND);
            if ((tmSec < 0) || (tmSec >= SECONDS_PER_MINUTE))
                return (-8L);
            timeValue += tmSec;
        }

        return timeValue;
    }
    /**
     * Finds the closest day that a DST state changes (on->off or viceversa)
     * @param midDate date to start looking for the state change
     * @param expState expected state (true=on)
     * @param direction +1 to look forward, (-1) to look backwards
     * @return date where change occured - i.e., expected state is achieved (null if error/not found)
     */
    private static final Calendar findDSTBoundaryChange (final Calendar midDate, final boolean expState, final int direction)
    {
        if ((null == midDate) || ((direction != 1) && (direction != (-1))))
            return null;

        final TimeZone    tz=midDate.getTimeZone();
        if (!tz.useDaylightTime())
            return null;

        final int        midYear=midDate.get(Calendar.YEAR);
        final Calendar    dstStart=Calendar.getInstance();
        dstStart.set(midDate.get(Calendar.YEAR), midDate.get(Calendar.MONTH), midDate.get(Calendar.DATE), 0, 0, 0);
        dstStart.setTimeZone(tz);

        for ( ; dstStart.get(Calendar.YEAR) == midYear; dstStart.add(Calendar.DATE, direction))
        {
            final boolean curState=tz.inDaylightTime(dstStart.getTime());
            if (expState == curState)
                return dstStart;
        }

        return null;
    }
    /**
     * @param midDate date from which to start looking - Note: the search is
     * both ways - upwards and backwards.
     * @return first date/time when DST is in effect (null if error/not found)
     */
    public static final Calendar findDSTStart (final Calendar midDate)
    {
        if (null == midDate)
            return null;

        final TimeZone    tz=midDate.getTimeZone();
        if (!tz.useDaylightTime())
            return null;

        final Date        midTime=midDate.getTime();
        final boolean    midState=tz.inDaylightTime(midTime);

        // if currently in DST, go back till no longer so
        Calendar    dstStart=null;
        if (midState)
        {
            if (null == (dstStart=findDSTBoundaryChange(midDate, false, (-1))))
                return null;

            dstStart.add(Calendar.DATE, 1);
            return dstStart;
        }

        // first try going forward
        if (null == (dstStart=findDSTBoundaryChange(midDate, true, 1)))
        {
            // if found, this is the END of the DST period
            if ((dstStart=findDSTBoundaryChange(midDate, true, (-1))) != null)
            {
                dstStart.add(Calendar.DATE, (-1));    // skip end

                if ((dstStart=findDSTBoundaryChange(dstStart, false, (-1))) != null)
                    dstStart.add(Calendar.DATE, 1);    // mark real start
            }
        }

        return dstStart;
    }
    /**
     * @return first date/time when DST is in effect (null if error/not found)
     * @see #findDSTStart(Calendar midDate)
     */
    public static final Calendar findDSTStart ()
    {
        return findDSTStart(Calendar.getInstance());
    }
    /**
     * @param midDate date from which to start looking - Note: the search is
     * both ways - upwards and backwards.
     * @return first date/time when DST is NOT in effect (null if error/not found)
     */
    public static final Calendar findDSTEnd (final Calendar midDate)
    {
        if (null == midDate)
            return null;

        final TimeZone    tz=midDate.getTimeZone();
        if (!tz.useDaylightTime())
            return null;

        final Date        midTime=midDate.getTime();
        final boolean    midState=tz.inDaylightTime(midTime);
        if (midState)
            return findDSTBoundaryChange(midDate, false, 1);

        // first try going backward
        Calendar    dstStart=findDSTBoundaryChange(midDate, true, (-1));
        if (null == dstStart)    // if failed, try going forward
        {
            // if found, then this is the start of the DST period
            if ((dstStart=findDSTBoundaryChange(midDate, true, 1)) != null)
            {
                dstStart.add(Calendar.DATE, 1);    // skip start
                dstStart = findDSTBoundaryChange(dstStart, false, 1);
            }
        }
        else
        {
            // first date when DST NOT in effect
            dstStart.add(Calendar.DATE, 1);
        }

        return dstStart;
    }
    /**
     * @return first date/time when DST is NOT in effect - starting
     * from current date/time (null if error/not found)
     * @see #findDSTEnd(Calendar midDate)
     */
    public static final Calendar findDSTEnd ()
    {
        return findDSTEnd(Calendar.getInstance());
    }
    /**
     * @param midDate date to start looking for DST boundaries (forward
     * and backwards)
     * @return boundaries array ([0]=start, [1]=end) - null/empty if error
     * or not found
     */
    public static final Calendar[] findDSTBoundaries (final Calendar midDate)
    {
        final Calendar[]    bounds={ findDSTStart(midDate), findDSTEnd(midDate) };
        for (int    bIndex=0; bIndex < bounds.length; bIndex++)
            if (null == bounds[bIndex])
                return null;

        return bounds;
    }
    /**
     * @return boundaries array ([0]=start, [1]=end) - null/empty if error
     * or not found
     * @see #findDSTBoundaries(Calendar midDate)
     */
    public static final Calendar[] findDSTBoundaries ()
    {
        return findDSTBoundaries(Calendar.getInstance());
    }
    /**
     * Appends a date/time value component
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param dtVal date/time value
     * @param width number of digits to represent the value
     * @param dtSep separator char to post-pend - if '\0' then not appended
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendDateTimeValueComp (final A sb, final int dtVal, final int width, final char dtSep) throws IOException
    {
        StringUtil.appendPaddedNum(sb, dtVal, width);
        if (dtSep != '\0')
            sb.append(dtSep);

        return sb;
    }
    /**
     * Appends a date/time value component
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param dtVal date/time value
     * @param width number of digits to represent the value
     * @param minValue minimum allowed value for <I>dtVal</I> (inclusive)
     * @param maxValue maximum allowed value for <I>dtVal</I> (inclusive)
     * @param dtSep separator char to post-pend - if '\0' then not appended
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendDateTimeValueComp (final A sb, final int dtVal, final int width, final int minValue, final int maxValue, final char dtSep) throws IOException
    {
        if ((dtVal >= minValue) && (dtVal <= maxValue))
            return appendDateTimeValueComp(sb, dtVal, width, dtSep);
        else
            throw new StreamCorruptedException(ClassUtil.getArgumentsExceptionLocation(DateUtil.class, "appendDateTimeValueComp", Integer.valueOf(dtVal), Integer.valueOf(minValue), Integer.valueOf(maxValue)) + " value not in specfified range");
    }
    /**
     * Appends a time value(s) string "hh:mm:ss.nnn"
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param hour hour value - 00-23 (error if not within this range)
     * @param minute minute(s) value - 00-59 (error if not within this range)
     * @param second second(s) value - 00-59 (if negative or not within 0-59
     * then ignored). <B>Note:</B> if ignored but millis value is OK then
     * error returned
     * @param tmSep separator to be used between hour/minute/second(s)
     * value(s). If '\0' then no separator is added
     * @param millis milliseconds value - 000-999. <B>Note:</B> if
     * <U>negative</U> or not within 0-999 value then <U>ignored</U>
     * @param msSep separator to be used between the seconds and the milli(s)
     * value(s) - if milliseconds used. If '\0' then no separator is added
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendTimeValues (final A sb, final int hour, final int minute, final int second, final char tmSep, final int millis, final char msSep) throws IOException
    {
        appendDateTimeValueComp(sb, hour, 2, 0, HOURS_PER_DAY, tmSep);

        final boolean haveSeconds=(second >= 0) && (second < SECONDS_PER_MINUTE);
        appendDateTimeValueComp(sb, minute, 2, 0, MINUTES_PER_HOUR, haveSeconds ? tmSep : '\0');

        final boolean    haveMillis=((millis >= 0) && (millis < 1000));
        if (haveSeconds)
        {
            appendDateTimeValueComp(sb, second, 2, 0, SECONDS_PER_MINUTE, haveMillis ? msSep : '\0');
            if (haveMillis)
                StringUtil.appendPaddedNum(sb, millis, 3);
        }
        else    // if seconds ignored the make sure millis ignored as well
        {
            if (haveMillis)
                throw new StreamCorruptedException(ClassUtil.getArgumentsExceptionLocation(DateUtil.class, "appendTimeValues", Integer.valueOf(millis)) + " have msec. but no seconds value(s)");
        }

        return sb;
    }
    /**
     * Default delimiter used between hour/minute/second(s) time values
     */
    public static final char    DEFAULT_TMSEP=':';
    /**
     * Default delimiter used between hour/minute/second(s) and milliseconds
     */
    public static final char    DEFAULT_MSSEP='.';
    /**
     * Appends a time value(s) string "hh:mm:ss.nnn"
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param hour hour value - 00-23
     * @param minute minute(s) value - 00-59
     * @param second second(s) value - 00-59
     * value(s) - if milliseconds used
     * @param millis milliseconds value - 000-999. <B>Note:</B> if
     * <U>negative</U> or not withing 0-999 value then <U>ignored</U>
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendTimeValues (final A sb, final int hour, final int minute, final int second, final int millis) throws IOException
    {
        return appendTimeValues(sb, hour, minute, second, DEFAULT_TMSEP, millis, DEFAULT_MSSEP);
    }
    /**
     * Appends a time value(s) string "hh:mm:ss" (i.e., no milliseconds)
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param hour hour value - 00-23
     * @param minute minute(s) value - 00-59
     * @param second second(s) value - 00-59
     * @param tmSep separator to be used between hour/minute/second(s) value(s)
     * If '\0' then no separator is added
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendTimeValues (final A sb, final int hour, final int minute, final int second, final char tmSep) throws IOException
    {
        return appendTimeValues(sb, hour, minute, second, tmSep, Integer.MIN_VALUE, DEFAULT_MSSEP /* unused */);
    }
    /**
     * Appends a time value(s) string "hh:mm:ss" (i.e., no milliseconds)
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param hour hour value - 00-23
     * @param minute minute(s) value - 00-59
     * @param second second(s) value - 00-59
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendTimeValues (final A sb, final int hour, final int minute, final int second) throws IOException
    {
        return appendTimeValues(sb, hour, minute, second, DEFAULT_TMSEP);
    }
    /**
     * Appends a time value(s) string "hh:mm" (i.e., seconds/no milliseconds)
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param hour hour value - 00-23
     * @param minute minute(s) value - 00-59
     * @param tmSep separator to be used between hour/minute value(s).
     * If '\0' then no separator is added
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendTimeValues (final A sb, final int hour, final int minute, final char tmSep) throws IOException
    {
        return appendTimeValues(sb, hour, minute, Integer.MIN_VALUE, tmSep);
    }
    /**
     * Appends a time value(s) string "hh:mm" (i.e., seconds/no milliseconds)
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param hour hour value - 00-23
     * @param minute minute(s) value - 00-59
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendTimeValues (final A sb, final int hour, final int minute) throws IOException
    {
        return appendTimeValues(sb, hour, minute, DEFAULT_TMSEP);
    }
    /**
     * Appends time part of the calendar object as "hh:mm:ss.nnn"
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param c calendar object - may NOT be null
     * @param tmSep separator to be used between hour/minute/second(s) value(s)
     * @param appendMillis if TRUE then also appends milliseconds value
     * @param msSep separator to be used between the seconds and the milli(s)
     * value(s) - if milliseconds used
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendTimeValues (final A sb, final Calendar c, final char tmSep, final boolean appendMillis, final char msSep) throws IOException
    {
        if (null == c)
            throw new IOException(ClassUtil.getExceptionLocation(DateUtil.class, "appendTimeValues") + " no " + Calendar.class.getName() + " instance");

        return appendTimeValues(sb, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND), tmSep,
                                    appendMillis ? c.get(Calendar.MILLISECOND) : Integer.MIN_VALUE, msSep);
    }
    /**
     * Appends time part of the calendar object as "hh:mm:ss.nnn"
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param c calendar object - may NOT be null
     * @param appendMillis if TRUE then also appends milliseconds value
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendTimeValues (final A sb, final Calendar c, final boolean appendMillis) throws IOException
    {
        return appendTimeValues(sb, c, DEFAULT_TMSEP, appendMillis, DEFAULT_MSSEP);
    }
    /**
     * Appends time part of the calendar object as "hh:mm:ss.nnn"
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param c calendar object - may NOT be null
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendTimeValues (final A sb, final Calendar c) throws IOException
    {
        return appendTimeValues(sb, c, true);
    }
    /**
     * Appends the date value(s)
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param day day of month (1-31)
     * @param month month of year (1-12)
     * @param year (0 - 9999)
     * @param dtSep (if not '\0') separator to use between the components
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendDateValues (final A sb, final int day, final int month, final int year, final char dtSep) throws IOException
    {
        appendDateTimeValueComp(sb, day, 2, 1, 31, dtSep);
        appendDateTimeValueComp(sb, month, 2, 1, 12, dtSep);
        appendDateTimeValueComp(sb, year, 4, 0, 9999, '\0');

        return sb;
    }
    /**
     * Appends the date value(s)
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param c calendar to be used - may not be <I>null</I>
     * @param dtSep (if not '\0') separator to use between the components
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendDateValues (final A sb, final Calendar c, final char dtSep) throws IOException
    {
        if (null == c)
            throw new IOException(ClassUtil.getExceptionLocation(DateUtil.class, "appendDateValues") + " no " + Calendar.class.getName() + " instance");

        return appendDateValues(sb, c.get(Calendar.DAY_OF_MONTH), 1 + (c.get(Calendar.MONTH) - Calendar.JANUARY), c.get(Calendar.YEAR), dtSep);
    }
    /**
     * Appends calendar date according to ISO-8601 format:
     * YYYY-MM-DDThh:mm:ss.sTZD (e.g. 1997-07-16T19:20:30.45+01:00)
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param c calendar object (error if null)
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendISO8601Date (final A sb, final Calendar c) throws IOException
    {
        if ((null == c) || (null == sb))
            throw new IOException(ClassUtil.getExceptionLocation(DateUtil.class, "appendISO8601Date") + " incomplete arguments");

        StringUtil.appendPaddedNum(sb, c.get(Calendar.YEAR), 4);
        sb.append('-');
        StringUtil.appendPaddedNum(sb, 1 + (c.get(Calendar.MONTH) - Calendar.JANUARY), 2);
        sb.append('-');
        StringUtil.appendPaddedNum(sb, c.get(Calendar.DAY_OF_MONTH), 2);

        return sb;
    }
    /**
     * Appends calendar time according to ISO-8601 format:
     * YYYY-MM-DDThh:mm:ss.sTZD (e.g. 1997-07-16T19:20:30.45+01:00)
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param c calendar object (error if null)
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendISO8601Time (final A sb, final Calendar c) throws IOException
    {
        return appendTimeValues(sb, c, true);
    }
    /**
     * @param sb The {@link Appendable} instance to append data to
     * @param <A> The {@link Appendable} generic type
     * @param secsOffset offset (sec.) - including DST
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendISO8601TimeZoneOffset (final A sb, final int secsOffset) throws IOException
    {
        final int    absOffset=Math.abs(secsOffset),
                    hours=absOffset / SECONDS_PER_HOUR,
                    minutes=(absOffset % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE;
        if ((hours < 0) || (hours > /* some zones allow +24 */ HOURS_PER_DAY)
         || (minutes < 0) || (minutes >= MINUTES_PER_HOUR))
            throw new IOException(ClassUtil.getArgumentsExceptionLocation(DateUtil.class, "appendISO8601TimeZoneOffset", Integer.valueOf(hours), Integer.valueOf(minutes)) + " bad values");

        sb.append((secsOffset >= 0) ? '+' : '-');
        StringUtil.appendPaddedNum(sb, hours, 2);
        sb.append(DEFAULT_TMSEP);
        StringUtil.appendPaddedNum(sb, minutes, 2);

        return sb;
    }
    /**
     * Appends calendar time zone according to ISO-8601 format:
     * YYYY-MM-DDThh:mm:ss.sTZD (e.g.s 1997-07-16T19:20:30.45+01:00)
     * @param <A> The generic type
     * @param sb The {@link Appendable} instance to append data to
     * @param c calendar object (error if null)
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendISO8601TimeZone (final A sb, final Calendar c) throws IOException
    {
        final TimeZone    tz=(null == c) ? null : c.getTimeZone();
        final Date        dt=(null == c) ? null : c.getTime();
        if ((null == tz) || (null == dt))    // should not happen
            throw new IOException(ClassUtil.getExceptionLocation(DateUtil.class, "appendISO8601TimeZone") + " missing date/time-zone value(s)");

        final int    secsOffset=tz.getRawOffset() / 1000,
                    dstOffset=((tz.useDaylightTime() && tz.inDaylightTime(dt)) ? tz.getDSTSavings() : 0) / 1000;
        return appendISO8601TimeZoneOffset(sb, secsOffset + dstOffset);
    }
    /**
     * Appends calendar date/time according to ISO-8601 format:
     * YYYY-MM-DDThh:mm:ss.sTZD (e.g. 1997-07-16T19:20:30.45+01:00)
     * @param <A> The generic instance
     * @param sb The {@link Appendable} instance to append data to
     * @param c calendar object (error if null)
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendISO8601 (final A sb, final Calendar c) throws IOException
    {
        appendISO8601Date(sb, c);
        sb.append('T');
        appendISO8601Time(sb, c);
        appendISO8601TimeZone(sb, c);
        return sb;
    }

    public static final int MAX_ISO8601_DATETIME_LEN=4 + 1    /* YYYY */
            + 2 + 1 /* MM */
            + 2 + 1 /* DD */
            + 2 + 1 /* HH */
            + 2 + 1 /* MM */
            + 2 + 1 /* SS */
            + 3 + 1 /* millis */
            + 1 + 2 + 1 + 2 /* zone */
            + 8    /* extra - just in case... */
            ;
    /**
     * String with calendar date/time according to ISO-8601 format:
     * YYYY-MM-DDThh:mm:ss.sTZD (e.g., 1997-07-16T19:20:30.45+01:00)
     * @param c calendar object (error if null)
     * @return string if successful (null/empty otherwise)
     * @throws IOException if bad/null {@link Calendar} object
     */
    public static final String toISO8601 (final Calendar c) throws IOException
    {
        return appendISO8601(new StringBuilder(MAX_ISO8601_DATETIME_LEN), c).toString();
    }

    public static final String toISO8601 (final Date d) throws IOException
    {
        if (null == d)
            throw new IOException("toISO8601() no " + Date.class.getSimpleName() + " instance");

        final Calendar    c=Calendar.getInstance();
        c.setTime(d);
        return toISO8601(c);
    }

    public static final String toISO8601 (final long tsVal) throws IOException
    {
        if (0L == tsVal)
            throw new IOException("toISO8601(" + tsVal + ") bad timestamp value");

        final Calendar    c=Calendar.getInstance();
        c.setTimeInMillis(tsVal);
        return toISO8601(c);
    }
    /**
     * @param cal {@link Calendar} object whose field is to be set
     * @param fldIndex field ID to be set
     * @param fldValue field value
     * @param minValue minimum allowed value (inc.)
     * @param maxValue maximum allowed value (inc.)
     * @throws RuntimeException if wrong range
     */
    public static void setCalendarField (Calendar cal, int fldIndex, int fldValue, int minValue, int maxValue) throws RuntimeException
    {
        if ((fldValue < minValue) || (fldValue > maxValue))
            throw new NumberFormatException("Calendar field=" + fldIndex + " value (" + fldValue + ") not in range " + minValue + "-" + maxValue);

        cal.set(fldIndex, fldValue);
    }
    /**
     * @param fldName tested field name
     * @param cs character sequence
     * @param pos position to be tested
     * @param expSep expected separator
     * @throws RuntimeException if wrong range or separator does not match
     */
    public static void checkCalendarFieldSeparator (String fldName, CharSequence cs, int pos, char expSep) throws RuntimeException
    {
        if (cs.charAt(pos) != expSep)
            throw new NumberFormatException("Missing " + fldName + " field separator=" + expSep + " at index=" + pos + " of " + cs);
    }

    public static final <C extends Calendar> C fromISO8601 (C cal, CharSequence cs, int off, int len) throws RuntimeException
    {
        final ParsableString    ps=new ParsableString(cs, off, len);
        final int                baseIndex=ps.getStartIndex();

        setCalendarField(cal, Calendar.YEAR, ps.getUnsignedInt(baseIndex, baseIndex + 4), 0, 9999);
        checkCalendarFieldSeparator("year", cs, baseIndex + 4, '-');

        setCalendarField(cal, Calendar.MONTH, (ps.getUnsignedInt(baseIndex + 5, baseIndex + 7) - 1) + Calendar.JANUARY, Calendar.JANUARY, Calendar.DECEMBER);
        checkCalendarFieldSeparator("month", cs, baseIndex + 7, '-');

        setCalendarField(cal, Calendar.DAY_OF_MONTH, ps.getUnsignedInt(baseIndex + 8, baseIndex + 10), 1, 31);
        checkCalendarFieldSeparator("day", cs, baseIndex + 10, 'T');

        setCalendarField(cal, Calendar.HOUR_OF_DAY, ps.getUnsignedInt(baseIndex + 11, baseIndex + 13), 0, 23);
        checkCalendarFieldSeparator("hour", cs, baseIndex + 13, DEFAULT_TMSEP);

        setCalendarField(cal, Calendar.MINUTE, ps.getUnsignedInt(baseIndex + 14, baseIndex + 16), 0, 59);
        checkCalendarFieldSeparator("minute", cs, baseIndex + 16, DEFAULT_TMSEP);

        setCalendarField(cal, Calendar.SECOND, ps.getUnsignedInt(baseIndex + 17, baseIndex + 19), 0, 59);
        checkCalendarFieldSeparator("second", cs, baseIndex + 19, '.');

        // find end of milliseconds by locating the TZ sign char
        char    chSign='\0';
        int        curPos=baseIndex + 21;
        for ( ; ; curPos++)
        {
            chSign = ps.getCharAt(curPos);
            if ((chSign != '+') && (chSign != '-'))
                continue;

            setCalendarField(cal, Calendar.MILLISECOND, ps.getUnsignedInt(baseIndex + 20, curPos), 0, 999);
            curPos++;    // skip sign
            break;
        }

        // decode timezone
        checkCalendarFieldSeparator("TZ minutes", cs, curPos + 2, DEFAULT_TMSEP);
        final int    absHours=ps.getUnsignedInt(curPos, curPos + 2),
                    absMinutes=ps.getUnsignedInt(curPos + 3, curPos+5),
                    absOffset=absHours * SECONDS_PER_HOUR + absMinutes * SECONDS_PER_MINUTE,
                    rawOffset=absOffset * 1000 * (('-' == chSign) ? (-1) : 1);

        final String    tzName="GMT"
                            + String.valueOf(chSign)
                            + cs.subSequence(curPos, curPos + 2).toString()
                            + cs.subSequence(curPos + 3, curPos + 5).toString();
        final TimeZone    tzVal=new SimpleTimeZone(rawOffset, tzName);
        cal.setTimeZone(tzVal);

        // this actually forces a re-calculation of the internal milliseconds value
        if (cal.get(Calendar.ERA) != GregorianCalendar.AD)
            throw new IllegalStateException("ERA value mismatch: value=" + cal.get(Calendar.ERA) + " expected=" + GregorianCalendar.AD);

        return cal;
    }

    public static final <C extends Calendar> C fromISO8601 (C cal, CharSequence cs)
    {
        return fromISO8601(cal, cs, 0, (null == cs) ? 0 : cs.length());
    }
    /**
     * Format assumed to be YYYY-MM-DDThh:mm:ss.sTZD (e.g., 1997-07-16T19:20:30.45+01:00)
     * @param cs encoded characters sequence
     * @param off offset in sequence to start parsing
     * @param len number of characters to parse - can be greater than format length
     * @return {@link Calendar} object representing the encoded date/time.
     * <B>Note:</B> the timezone information does not include DST information
     * @throws RuntimeException if unable to parse
     */
    public static final Calendar fromISO8601 (CharSequence cs, int off, int len) throws RuntimeException
    {
        final Calendar    cal=new GregorianCalendar();
        cal.set(Calendar.ERA, GregorianCalendar.AD);    // just making sure

        return fromISO8601(cal, cs, off, len);
    }
    /**
     * Format assumed to be YYYY-MM-DDThh:mm:ss.sTZD (e.g., 1997-07-16T19:20:30.45+01:00)
     * @param cs encoded characters sequence - must <U>start</U> with this
     * format, but may continue with more characters - i.e., only the
     * formatted part is decoded
     * @return {@link Calendar} object representing the encoded date/time.
     * <B>Note:</B> the timezone information does not include DST information
     * @throws RuntimeException if unable to parse
     * @see #fromISO8601(CharSequence, int, int)
     */
    public static final Calendar fromISO8601 (CharSequence cs)
    {
        return fromISO8601(cs, 0, cs.length());
    }
    /**
     * @param c Calendar object whose "hash" is to be calculated - if
     * <I>null</I> then Integer.MIN_VALUE is returned
     * @return some <U>non-negative</U> hash code (if valid Calendar object)
     */
    public static final int getCalendarHashCode (Calendar c)
    {
        final long    t=(null == c) ? Long.MIN_VALUE : c.getTimeInMillis();
        if (t <= 0)
            return Integer.MIN_VALUE;

        final int    lo=(int) t,
                    hi=(int) (t >> 32),
                    hv=lo + hi;
        return (hv >= 0) ? hv : (0 - hv);
    }
    /**
     * Checks which <U>date</U> comes first (time is irrelevant)
     * @param c1 first calendar value to be checked
     * @param c2 second calendar value to be checked
     * @return (<0) if first is "older", (>0) if second, 0 if same
     * @throws NullPointerException if null arguments
     */
    public static final int compareDates (Calendar c1, Calendar c2)
    {
         int    vDiff=c1.get(Calendar.YEAR) - c2.get(Calendar.YEAR);
         if (vDiff != 0)
             return vDiff;

         if ((vDiff=c1.get(Calendar.MONTH) - c2.get(Calendar.MONTH)) != 0)
             return vDiff;

         return c1.get(Calendar.DAY_OF_MONTH) - c2.get(Calendar.DAY_OF_MONTH);
    }
    /**
     * @param c1 first calendar value to be checked
     * @param c2 second calendar value to be checked
     * @return TRUE if same date value (time not checked)
     * @throws NullPointerException if null arguments
     * @see #compareDates(Calendar, Calendar)
     */
    public static final boolean isSameDate (Calendar c1, Calendar c2)
    {
        return (0 == compareDates(c1, c2));
    }
    /**
     * Checks which <U>time</U> comes first (date and time-zone are irrelevant)
     * @param c1 first calendar value to be checked
     * @param c2 second calendar value to be checked
     * @return (<0) if first is "older", (>0) if second, 0 if same
     * @throws NullPointerException if null arguments
     */
    public static final int compareTimes (Calendar c1, Calendar c2)
    {
        int    vDiff=c1.get(Calendar.HOUR) - c2.get(Calendar.HOUR);
        if (vDiff != 0)
            return vDiff;

        if ((vDiff=c1.get(Calendar.MINUTE) - c2.get(Calendar.MINUTE)) != 0)
            return vDiff;

        if ((vDiff=c1.get(Calendar.SECOND) - c2.get(Calendar.SECOND)) != 0)
            return vDiff;

        return c1.get(Calendar.MILLISECOND) - c2.get(Calendar.MILLISECOND);
    }
    /**
     * @param c1 first calendar value to be checked
     * @param c2 second calendar value to be checked
     * @return TRUE if same date value (time not checked)
     * @throws NullPointerException if null arguments
     * @see #compareDates(Calendar, Calendar)
     */
    public static final boolean isSameTime (Calendar c1, Calendar c2)
    {
        return (0 == compareTimes(c1, c2));
    }
    /**
     * @param c original {@link Calendar} object - may NOT be null (not checked)
     * @param tz {@link TimeZone} value to be used to set the (cloned)
     * value - may NOT be null (not checked)
     * @return a <U>clone</U> of the original object with the time-zone value
     * set to supplied time-zone value value
     * @throws IllegalStateException if illegal original object(s) value
     */
    public static final Calendar getAdjustedCalendarValue (Calendar c, TimeZone tz) throws IllegalStateException
    {
        // force a milliseconds value calculation (if not already done so)
        if (c instanceof GregorianCalendar)
        {
            if (c.get(Calendar.ERA) != GregorianCalendar.AD)
                throw new IllegalStateException("ERA value mismatch: value=" + c.get(Calendar.ERA) + " expected=" + GregorianCalendar.AD);
        }
        else
        {
            final int    h=c.get(Calendar.HOUR_OF_DAY);
            if ((h < 0) || (h > c.getMaximum(Calendar.HOUR_OF_DAY)))
                throw new IllegalStateException("Hour value (" + h + ") not in allowed range: 0-" + c.getMaximum(Calendar.HOUR_OF_DAY));
        }

        final Calendar    v=(Calendar) c.clone();
        if ((null == v) || (null == tz))
            throw new IllegalStateException("Bad/illegal clone/local timezone");

        v.setTimeZone(tz);
        return v;
    }
    /**
     * @param c original {@link Calendar} object - may NOT be null (not checked)
     * @return a <U>clone</U> of the original object with the time-zone value
     * set to {@link TimeZone#getDefault()} value
     * @throws IllegalStateException if illegal original object value
     */
    public static final Calendar getLocallyAdjustedCalendarValue (Calendar c) throws IllegalStateException
    {
        return getAdjustedCalendarValue(c, TimeZone.getDefault());
    }
    /**
     * @param hour hour value (0-23)
     * @param minute minute value (0-59)
     * @param second second value (0-59)
     * @param msec milliseconds value (0-999)
     * @return number of msec. till midnight relative to the supplied time
     * value (negative if error)
     */
    public static final long getTimeTillMidnight (final int hour, final int minute, final int second, final int msec)
    {
        if ((hour < 0) || (hour >= HOURS_PER_DAY)
         || (minute < 0) || (minute >= MINUTES_PER_HOUR)
         || (second < 0) || (second >= SECONDS_PER_MINUTE)
         || (msec < 0) || (msec >= MSEC_PER_SECOND))
            return (-1L);

        if ((0 == hour) && (0 == minute) && (0 == second) && (0 == msec))
            return 0L;    // handle special case of EXACTLY midnight

        final int    remSeconds=SECONDS_PER_MINUTE - second - 1,
                    remMinutes=MINUTES_PER_HOUR - minute - 1,
                    remHours=HOURS_PER_DAY - hour - 1;

        return (((remSeconds + remMinutes * SECONDS_PER_MINUTE + remHours * SECONDS_PER_HOUR + 1 /* the msec. completion to next second */) * MSEC_PER_SECOND) - msec);
    }
    /**
     * @param hour hour value (0-23)
     * @param minute minute value (0-59)
     * @param second second value (0-59)
     * @return number of msec. till midnight relative to the supplied time
     * value (negative if error)
     */
    public static final long getTimeTillMidnight (final int hour, final int minute, final int second)
    {
        return getTimeTillMidnight(hour, minute, second, 0);
    }
    /**
     * @param c {@link Calendar} to be used as base time - may NOT be null
     * @param ignoreMsec TRUE=don't take into account current number of msec.
     * @return number of msec. till midnight relative to the supplied time
     * value (negative if error)
     */
    public static final long getTimeTillMidnight (final Calendar c, final boolean ignoreMsec)
    {
        return (null == c) ? (-1L) : getTimeTillMidnight(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND), ignoreMsec ? 0 : c.get(Calendar.MILLISECOND));
    }
    /**
     * @param c {@link Calendar} to be used as base time - may NOT be null
     * @return number of msec. till midnight relative to the supplied time
     * value (negative if error)
     */
    public static final long getTimeTillMidnight (final Calendar c)
    {
        return getTimeTillMidnight(c, false);
    }
    /**
     * @param d {@link Date} to be used as base date - may NOT be null
     * @return number of msec. till midnight relative to the supplied time
     * value (negative if error)
     */
    public static final long getTimeTillMidnight (final Date d)
    {
        if (null == d)
            return (-1L);

        final Calendar    c=Calendar.getInstance();
        c.setTime(d);
        // force fields re-calculation
        if (c.get(Calendar.ERA) != GregorianCalendar.AD)
            return (-2L);

        return getTimeTillMidnight(c);
    }
    /**
     * @param timestamp Timestamp (msec.) to be used as base date/time - must
     * be <U>positive</U>
     * @return number of msec. till midnight relative to the supplied time
     * value (negative if error)
     */
    public static final long getTimeTillMidnight (final long timestamp)
    {
        if (timestamp <= 0L)
            return (-1L);

        final Calendar    c=Calendar.getInstance();
        c.setTimeInMillis(timestamp);

        // force fields re-calculation
        if (c.get(Calendar.ERA) != GregorianCalendar.AD)
            return (-2L);

        return getTimeTillMidnight(c);
    }
    /**
     * @param dtv {@link Date} instance whose contents should be formatted
     * according to the supplied formatter instance
     * @param dtf {@link DateFormat} instance to be used for formatting
     * @return formatting result - null/empty if no {@link Date} or
     * formatter instance(s) supplied. <B><U>Note:</U></B> <U>synchronizes</U>
     * the access to the formatter instance to prevent its internal state from
     * changing while formatting takes place
     */
    public static final String getFormattedDateTimeString (final Date dtv, final DateFormat dtf)
    {
        if ((null == dtv) || (null == dtf))
            return null;

        synchronized(dtf)
        {
            return dtf.format(dtv);
        }
    }
    /**
     * @param c {@link Calendar} instance whose contents should be formatted
     * according to the supplied formatter instance
     * @param dtf {@link DateFormat} instance to be used for formatting
     * @return formatting result - null/empty if no {@link Calendar} or
     * formatter instance(s) supplied. <B><U>Note:</U></B> <U>synchronizes</U>
     * the access to the formatter instance to prevent its internal state from
     * changing while formatting takes place
     */
    public static final String getFormattedDateTimeString (final Calendar c, final DateFormat dtf)
    {
        if ((null == c) || (null == dtf))
            return null;

        return getFormattedDateTimeString(c.getTime(), dtf);
    }
    /**
     * @param hour hour value (0-23)
     * @param minute minute value (0-59)
     * @param second second value (0-59)
     * @param msec milliseconds value (0-999)
     * @return number of msec. from midnight relative to the supplied time
     * value (negative if error)
     */
    public static final long getTimeFromMidnight (final int hour, final int minute, final int second, final int msec)
    {
        if ((hour < 0) || (hour >= HOURS_PER_DAY)
         || (minute < 0) || (minute >= MINUTES_PER_HOUR)
         || (second < 0) || (second >= SECONDS_PER_MINUTE)
         || (msec < 0) || (msec >= MSEC_PER_SECOND))
            return (-1L);

        if ((0 == hour) && (0 == minute) && (0 == second) && (0 == msec))
            return 0L;    // handle special case of EXACTLY midnight

        final int    numSecs=hour * SECONDS_PER_HOUR + minute * SECONDS_PER_MINUTE + second;
        return (numSecs * MSEC_PER_SECOND) + msec;
    }
    /**
     * @param c c {@link Calendar} to be used as base date - may NOT be null
     * @param ignoreMsec TRUE=don't take into account current number of msec.
     * @return Number of msec. since midnight of the supplied date/time
     * (negative if error)
     */
    public static final long getTimeFromMidnight (final Calendar c, final boolean ignoreMsec)
    {
        return (null == c) ? (-1L) : getTimeFromMidnight(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND), ignoreMsec ? 0 : c.get(Calendar.MILLISECOND));
    }
    /**
     * @param c c {@link Calendar} to be used as base date - may NOT be null
     * @return Number of msec. since midnight of the supplied date/time
     * (negative if error)
     */
    public static final long getTimeFromMidnight (final Calendar c)
    {
        return getTimeFromMidnight(c, false);
    }
    /**
     * @param d {@link Date} to be used as base date - may NOT be null
     * @return Number of msec. since midnight of the supplied date/time
     * (negative if error)
     */
    public static final long getTimeFromMidnight (final Date d)
    {
        if (null == d)
            return (-1L);

        final Calendar    c=Calendar.getInstance();
        c.setTime(d);
        // force fields re-calculation
        if (c.get(Calendar.ERA) != GregorianCalendar.AD)
            return (-2L);

        return getTimeFromMidnight(c);
    }
    /**
     * @param timestamp Timestamp (msec.) to be used as base date/time - must
     * be <U>positive</U>
     * @return Number of msec. since midnight of the supplied date/time
     * (negative if error)
     */
    public static final long getTimeFromMidnight (final long timestamp)
    {
        if (timestamp <= 0L)
            return (-1L);

        final Calendar    c=Calendar.getInstance();
        c.setTimeInMillis(timestamp);

        // force fields re-calculation
        if (c.get(Calendar.ERA) != GregorianCalendar.AD)
            return (-2L);

        return getTimeFromMidnight(c);
    }
    /**
     * Copies the specified fields of the source {@link Calendar} to the
     * destination one
     * @param src Source {@link Calendar} if null then nothing is copied
     * @param dst Destination {@link Calendar} if null then nothing is copied
     * @param fields The fields to be copied - see {@link Calendar#get(int)}
     * {@link Calendar#set(int, int)} for identifiers
     * @return Destination {@link Calendar} after updating it (if at all)
     */
    public static final Calendar copyCalendarFields (final Calendar src, final Calendar dst, final int ... fields)
    {
        if ((null == src) || (null == dst)
         || (null == fields) || (fields.length <= 0))
            return dst;

        for (final int    fid : fields)
            dst.set(fid, src.get(fid));

        return dst;
    }
    /**
     * Caches the {@link DateFormatSymbols} for each requested {@link Locale}
     * used in call to {@link #getDateFormatSymbols(Locale)}
     */
    private static LocalesMap<DateFormatSymbols>    _dfsMap    /* =null */;
    public static final DateFormatSymbols getDateFormatSymbols (final Locale l /* null == default */)
    {
        final Locale ll=(null == l) ? null : Locale.getDefault();
        synchronized(DateUtil.class)
        {
            if (null == _dfsMap)
                _dfsMap = new LocalesMap<DateFormatSymbols>();
        }

        DateFormatSymbols    dfs=null;
        synchronized(_dfsMap)
        {
            if (null == (dfs=_dfsMap.get(ll)))
            {
                dfs = new DateFormatSymbols(ll);
                _dfsMap.put(ll, dfs);
            }
        }

        return dfs;
    }
}
