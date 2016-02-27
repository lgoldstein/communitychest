package net.community.chest.test.teasers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.community.chest.test.TestBase;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Shows the dangers of doing date/time arithmetic without taking into
 * account the time-zone offset and/or DST</P>
 * 
 * @author Lyor G.
 * @since Apr 28, 2008 2:53:02 PM
 */
public class TimestampOffset extends TestBase {
	public static void main (String[] args)
	{
		final long	now=System.currentTimeMillis(),
		/*
		 * We can easily calculate the "offset" of the current date/time
		 * value from midnight by getting the remainder of the division
		 * by how long 1-day is (86,400 sec. => 86,400,000 msec.)
		 */
					midnightOffset=now % 86400000L,
		// calculate number of hours since midnight
					hoursOffset=midnightOffset / 3600000L,
		// calculate number of minutes after the hour
					minutesOffset=(midnightOffset % 3600000L) / 60000L;

		// lets compare the results
		final DateFormat	dtf=new SimpleDateFormat("HH:mm");
		final String		nowString=dtf.format(new Date(now)),
		// use a zero-padded display to match the DateFormat
							hrsString=((hoursOffset < 10L) ? "0" : "") + String.valueOf(hoursOffset),
							minString=((minutesOffset < 10L) ? "0" : "") + String.valueOf(minutesOffset);
		System.out.println("now=" + nowString + " and also " + hrsString + ":" + minString);
	}
}
