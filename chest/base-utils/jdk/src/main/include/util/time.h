#ifndef _TIME_UTLS_H_
#define _TIME_UTLS_H_

/*
 * File: time.h
 *
 * Contents:
 *
 *		Useful procedures for handling time & strings which encode time & date.
 *
 * Created by:
 *		Lyor Goldstein.
 *
 * Version(s):
 *		1.0, 27-Feb-95 (LYOR G.) - imported from RMX.
 *		1.1, 03-Sep-95 (LYOR G.) - added "alarms" support
 *
 *	Remarks:
 *
 *		1. Automatically includes:
 *
 *			  <time.h>
 *
 *		For C++ & multithreading also
 *
 *			  <synch.hpp>
 *
 *		2. if _REENTRANT (i.e. "-mt" option in compilation) is defined, then it
 *			also automatically inludes <synch.hpp> for mutual exclusion mechanisms.
 */

/*---------------------------------------------------------------------------*/

//#ifdef __vxworks
//#include <types/vxTypes.h>
//#endif

#include <util/tables.h>
#include <time.h>	/* O/S include */

#ifndef WIN32
#	ifdef __cplusplus
		inline size_t _tcsftime (LPTSTR dst, size_t maxsize, LPCTSTR fmt, const struct tm *tptr)
		{
			return strftime(dst, maxsize, fmt, tptr);
		}
#	else
#		define _tcsftime(d,s,f,t)		strftime((d),(s),(f),(t))
#	endif	/* __cplusplus */
#endif	/* WIN32 */

#ifdef __cplusplus
const DWORD NSECS_PER_SEC=1000000000UL;
#else
#define NSECS_PER_SEC 1000000000UL
#endif

/*---------------------------------------------------------------------------*/

/*
 * Returns the ABSOLUTE difference between t1 and t2
 */

#ifdef __cplusplus
inline time_t timediff (time_t t1, time_t t2)
{
	if (t1 >= t2)
		return(t1-t2);
	else
		return(t2-t1);
}
#else
#	define timetdiff(t1,t2)  (time_t) absdiff((t1),(t2))
#endif

/*
 * Returns: >0 - if t1 > t2
 *				=0 - if t1 = t2
 *				<0 - if t1 < t2
 */

#ifdef __cplusplus
inline int timecmp (time_t t1, time_t t2)
{
	if (t1 == t2)
		return(0);
	if (t1 > t2)
		return(1);
	return(-1);
}
#else
#	define timetcmp(t1,t2)		(SIGNOF((DWORD) timetdiff((t1),(t2))))
#endif

/*---------------------------------------------------------------------------*/

#define DT_COMP_LEN	 2	 /* number of chars in any component */

#define DT_YEAR_POS	 6
#define DT_YEAR_LEN	 DT_COMP_LEN

#define DT_MONTH_POS	 0	 /* NOTE: American date is assumed */
#define DT_MONTH_LEN	 DT_COMP_LEN
#define DT_DSEP1_POS	 ((DT_MONTH_POS)+(DT_MONTH_LEN))

#define DT_DAY_POS	 3
#define DT_DAY_LEN	 DT_COMP_LEN
#define DT_DSEP2_POS	 ((DT_DAY_POS)+(DT_DAY_LEN))

#define DT_DATE_LEN	 ((DT_DAY_LEN)+(DT_MONTH_LEN)+(DT_YEAR_LEN)+2)

#define DT_HOUR_POS	 0
#define DT_HOUR_LEN	 DT_COMP_LEN
#define DT_TSEP1_POS	 ((DT_HOUR_POS)+(DT_HOUR_LEN))

#define DT_MINUTE_POS 3
#define DT_MINUTE_LEN DT_COMP_LEN
#define DT_TSEP2_POS	 ((DT_MINUTE_POS)+(DT_MINUTE_LEN))

#define DT_SECOND_POS 6
#define DT_SECOND_LEN DT_COMP_LEN

#define DT_TIME_LEN	 ((DT_HOUR_LEN)+(DT_MINUTE_LEN)+(DT_SECOND_LEN)+2)

typedef struct tag_datetimestruct {
						time_t		systemtime;
						char			date[DT_DATE_LEN+1];
						char			time[DT_TIME_LEN+1];
											 } DATETIMESTRUCT;

/*---------------------------------------------------------------------------*/

#define TSEP_CHAR			':'

/* NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!!
 *
 *		Due to alignment issues, it is highly unrecommended to use the following
 * "typedef"-s as aliases for time/date strings
 */

typedef struct tag_time_string_struct_type {
							char		hour[2];
							char		tsep1;			/* ':' */
							char		minute[2];
							char		tsep2;			/* ':' */
							char		second[2];
							char		null_char;	/* should be set to '\0' */
														} TIME_STRING_STRUCT_TYPE;

#define DSEP_CHAR			'/'

typedef struct tag_date_string_struct_type {
						char				month[2];
						char				dsep1;		/* '/' */
						char				day[2];
						char				dsep2;		/* '/' */
						char				year[2];
						char				null_char; /* should be set to '\0' */
														} DATE_STRING_STRUCT_TYPE;

typedef struct tag_date_time_alias_type {
						time_t						systemtime;
						DATE_STRING_STRUCT_TYPE date_struct;
						TIME_STRING_STRUCT_TYPE time_struct;
													 } DATE_TIME_ALIAS_TYPE;

/*
 *		Structure of string return by "ctime" function.
 */

#define CTSEP_CHAR	' '

typedef struct tag_ctime_struct_type {
								char		day_name[3];	/* Sun, Mon, ...	*/
								char		ctsep1;			/* always ' '		*/
								char		month_name[3]; /* Jan, Feb, ...	*/
								char		ctsep2;			/* always ' '		*/
								char		day[2];			/*	 1,  2, ...		*/
								char		ctsep3;			/* always ' '		*/
								char		hour[2];			/* 24 hour clock	*/
								char		ctsep4;			/* always ':'		*/
								char		minute[2];
								char		ctsep5;			/* always ':'		*/
								char		second[2];
								char		ctsep6;			/* always ' '		*/
								char		year[4];			/* 19.., 20..,		*/
								char		newline_char;	/* always '\n'		*/
								char		null_char;		/* always '\0'		*/
												 } CTIME_STRUCT_TYPE;

/*---------------------------------------------------------------------------*/

#define DAY_OF_WEEK_POS		0
#define DAY_OF_WEEK_LEN		3

#define MONTH_OF_YEAR_POS	((DAY_OF_WEEK_POS)+(DAY_OF_WEEK_LEN)+1)
#define MONTH_OF_YEAR_LEN	3

#define DAY_OF_MONTH_POS	((MONTH_OF_YEAR_POS)+(MONTH_OF_YEAR_LEN)+1)
#define DAY_OF_MONTH_LEN	2

#define HOUR_OF_DAY_POS		((DAY_OF_MONTH_POS)+(DAY_OF_MONTH_LEN)+1)
#define HOUR_OF_DAY_LEN		2

#define MINUTE_OF_HOUR_POS	((HOUR_OF_DAY_POS)+(HOUR_OF_DAY_LEN)+1)
#define MINUTE_OF_HOUR_LEN	2

#define SECOND_OF_MINUTE_POS	((MINUTE_OF_HOUR_POS)+(MINUTE_OF_HOUR_LEN)+1)
#define SECOND_OF_MINUTE_LEN	2

#define YEAR_POS					((SECOND_OF_MINUTE_POS)+(SECOND_OF_MINUTE_LEN)+1)
#define YEAR_LEN					4

#define CTIME_NEWLINE_POS		((YEAR_POS)+(YEAR_LEN))
#define CTIME_NULLCHAR_POS		((CTIME_NEWLINE_POS)+1)

#define CTIME_STRING_LEN		CTIME_NULLCHAR_POS
#define CTIME_STRING_SIZE		((CTIME_STRING_LEN)+1)

/*---------------------------------------------------------------------------*/
/*									(dq)enc/decodetime
 *									------------------
 *		 ENCODES (returns the time_t) or DECODES the time value.
 *
 * Parameters:
 *
 *		[IN]	struct_p - pointer to DATETIMESTRUCT.
 *
 * Function returns EOK if successful
 */
/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
extern EXC_TYPE dqencodetime (DATETIMESTRUCT& dt);
#else
extern EXC_TYPE dqencodetime (DATETIMESTRUCT *dt_p);
#endif

#ifdef __cplusplus
extern EXC_TYPE dqdecodetime (DATETIMESTRUCT& dt);
#else
extern EXC_TYPE dqdecodetime (DATETIMESTRUCT *dt_p);
#endif

/*---------------------------------------------------------------------------*/
/*								get_time_seconds
 *								----------------
 *		Returns the number of seconds encoded in the time-string.
 *
 * Parameters:
 *
 *		[IN]	time_str_p - pointer to string of format "hh:mm:ss".
 *		[OUT] exc - exception code - if non-EOK then some problem found in
 *					the time string (e.g. illegal value) and returned value should
 *					be ignored.
 *
 * NOTE: the number of hours may be up to 99.
 */
/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
extern DWORD get_time_seconds (const char *time_str_p, EXC_TYPE& exc);
#else
extern DWORD get_time_seconds (const char *time_str_p, EXC_TYPE *exc);
#endif

/*---------------------------------------------------------------------------*/
/*								get_date_seconds
 *								----------------
 *		Returns the number of seconds encoded in the date-string starting at
 * January 1st, 00:00:00 of that year, and up to 00:00:00 of the encoded date.
 *
 * Parameters:
 *
 *		[IN]	date_str_p - pointer to string of format "mm/dd/yy".
 *		[OUT] exc - exception code - if non-EOK then some problem found in
 *					the date string (e.g. illegal value) and returned value should
 *					be ignored.
 */
/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
extern DWORD get_date_seconds (const char *date_str_p, EXC_TYPE& exc);
#else
extern DWORD get_date_seconds (const char *date_str_p, EXC_TYPE *exc);
#endif

/*---------------------------------------------------------------------------*/
/*									days/seconds_per_month
 *									---------------------
 *		Holds the number of days/seconds per month.
 *
 * NOTE: 1. the months are assumed to range 1-12 - entry #0 is not used !!!
 *			2. February is assumed to have the (normal) 28 days.
 */
/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
const DWORD SECONDS_PER_MINUTE=60;
#else
#define SECONDS_PER_MINUTE	60
#endif
#define MAX_SECOND_VALUE		  ((SECONDS_PER_MINUTE) - 1)

#ifdef __cplusplus
const DWORD MINUTES_PER_HOUR=60;
#else
#	define MINUTES_PER_HOUR 60
#endif
#define MAX_MINUTE_VALUE		  ((MINUTES_PER_HOUR) - 1)

#define SECONDS_PER_HOUR		  ((MINUTES_PER_HOUR) * (SECONDS_PER_MINUTE))

#ifdef __cplusplus
const DWORD HOURS_PER_DAY=24;
#else
#	define HOURS_PER_DAY	24
#endif
#define MAX_HOUR_VALUE			  ((HOURS_PER_DAY) - 1)

#define MINUTES_PER_DAY			  ((MINUTES_PER_HOUR) * (HOURS_PER_DAY))

#define DAYS_PER_YEAR			365
#define DAYS_PER_LEAP_YEAR		((DAYS_PER_YEAR) + 1)
#define MONTHS_PER_YEAR			12

#ifdef __cplusplus
const DWORD SECONDS_PER_DAY=86400UL;
const DWORD SECONDS_PER_YEAR=DAYS_PER_YEAR * SECONDS_PER_DAY;
const DWORD SECONDS_PER_LEAP_YEAR=SECONDS_PER_YEAR + SECONDS_PER_DAY;
#else
#	define SECONDS_PER_DAY 86400UL
#endif

#define DAYS_PER_WEEK			 7
#define WEEKS_PER_YEAR			 ((DAYS_PER_YEAR)/(DAYS_PER_WEEK))

/*---------------------------------------------------------------------------*/

#define FEB_MONTH_CASE	2
#define FEB_DAYS_IN_LEAP_YEAR	  (days_per_month[FEB_MONTH_CASE] + 1)

/*
 * Contain days/secods per month.
 *
 * NOTE: 1. the array(s) start at index "0" (e.g. days_per_month[5] => June,
 *			days_names[3] => "Wed") it is RECOMMENDED to use the indexing MACROs.
 *
 *			2. February is assumed to have the (normal) 28 days.
 */

#ifdef __cplusplus
inline DWORD MONTHS_ARRAY_INDEX (DWORD mth)
{
	return(mth-1);
}

inline DWORD MONTH_INDEX2VALUE (DWORD idx)
{
	return(idx+1);
}
#else
#	define MONTHS_ARRAY_INDEX(mth)	((mth)-1)
#	define MONTH_INDEX2VALUE(idx)	((idx)+1)
#endif

/* 0=January, non-leap years */
extern const BYTE days_per_month[];

/*---------------------------------------------------------------------------*/

#define MONTH_NAME_LENGTH 3

#ifdef __cplusplus
inline DWORD DAYS_ARRAY_INDEX (DWORD day)
{
	return(day-1);
}

inline DWORD DAY_INDEX2VALUE (DWORD idx)
{
	return(idx+1);
}
#else
#	define DAYS_ARRAY_INDEX(day)		((day)-1)
#	define DAY_INDEX2VALUE(idx)		((idx)+1)
#endif

#define DAY_NAME_LENGTH 3

/*---------------------------------------------------------------------------*/

/*		Time Zone is difference in SECONDS between GMT and requested zone. It is
 * POSITIVE for values BEHIND GMT and NEGATIVE for values AHAEAD of GMT (e.g.
 * GMT + 0200 == -7200, GMT - 0200 == +7200).
 */

/* returns hours/minutes difference (regardless of sign) - error if non-zero seconds */
extern EXC_TYPE GetTimeZoneComponents (const int	tmZone,	/* (-1) == current */
													BYTE			*tmHours,
													BYTE			*tmMinutes);

/* returns (-1) if error */
extern int GetTimeZoneValue (const int nSign, const BYTE tmHours, const BYTE tmMinutes);

/* converts the supplied time value and timezone to local timezone ((-1) == error) */
extern time_t RecalculateTmValTimezone (const time_t tmVal, const int orgTZone, const int newTmZone);

#ifdef __cplusplus
class CTmStruct : public tm {
	public:
		virtual void Reset ();

		// use default copy constructor and assignment operator
		CTmStruct ()
		{
			Reset();
		}

		virtual CTmStruct& operator= (const struct tm& tmv);

		virtual CTmStruct& SetTimeValue (const time_t tmVal);

		CTmStruct (const time_t tmVal)
		{
			SetTimeValue(tmVal);
		}

		virtual const time_t GetTimeValue () const
		{
			return ::mktime((CTmStruct *) this);
		}
};

inline time_t AdjustTmValTimezone (const time_t tmVal, const int tmZone)
{
	return RecalculateTmValTimezone(tmVal, tmZone, _timezone);
}
#else
#define AdjustTmValTimezone(tmv,tz)	\
	RecalculateTmValTimezone((tmv),(tz),_timezone)
#endif

/* not suitable for 1/1/1970 00:00:00 +0000 */
extern EXC_TYPE RecalculateTmVal (const struct tm *pOrg, const int orgTmZone, const int newTmZone, struct tm *pNew);

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// base class for deriving different types of duration measurements
class IDurationMeasurer {
	protected:
		DWORD		m_dwStart;
		DWORD		m_dwEnd;
		BOOLEAN	m_fRunning;

		IDurationMeasurer ()
			: m_fRunning(FALSE), m_dwStart(0), m_dwEnd(0)
		{
		}

		virtual DWORD GetCurrentValue () const = 0;

	public:
		// returns measurement start value
		virtual DWORD Start ()
		{
			m_dwStart = GetCurrentValue();
			m_fRunning = TRUE;
			return (m_dwEnd=m_dwStart);
		}

		// Note: if called several times only first call has effect (unless re-started)
		virtual DWORD Stop ()
		{
			if (m_fRunning)
			{
				m_dwEnd = GetCurrentValue();
				m_fRunning = FALSE;
			}

			return m_dwEnd;
		}

		// returns duration since start
		virtual DWORD Duration () const
		{ 
			return (m_fRunning ? (GetCurrentValue() - m_dwStart) : (m_dwEnd - m_dwStart));
		}

		virtual BOOLEAN IsRunning () const
		{ 
			return m_fRunning;
		}

		virtual ~IDurationMeasurer ()
		{
		}
};

/*---------------------------------------------------------------------------*/

class CCumulativeDurationMeasurer : public IDurationMeasurer {
	protected:
		DWORD					m_dwAccValue;
		IDurationMeasurer	*m_pMeasurer;

		virtual DWORD GetCurrentValue () const
		{
			return (m_dwAccValue + ((NULL == m_pMeasurer) ? 0 : m_pMeasurer->Duration()));
		}

	public:
		CCumulativeDurationMeasurer (IDurationMeasurer& m)
			: IDurationMeasurer()
			, m_pMeasurer(&m)
			, m_dwAccValue(0)
		{
		}

		CCumulativeDurationMeasurer ()
			: IDurationMeasurer()
			, m_pMeasurer(NULL)
			, m_dwAccValue(0)
		{
		}

		virtual void Reset ()
		{
			Stop();
			m_dwAccValue = 0;
		}

		virtual void SetMeasurement (IDurationMeasurer& m)
		{
			Reset();
			m_pMeasurer = &m;
		}

		virtual DWORD Start ()
		{
			if (m_pMeasurer != NULL)
				m_pMeasurer->Start();
			
			m_fRunning = TRUE;
			return 0;
		}

		// Note: if called several times only first call has effect (unless re-started)
		virtual DWORD Stop ()
		{
			if (m_fRunning)
			{
				if (m_pMeasurer != NULL)
				{
					m_pMeasurer->Stop();
					m_dwAccValue += m_pMeasurer->Duration();
				}		

				m_fRunning = FALSE;
			}
	
			return m_dwAccValue;
		}

		virtual DWORD Duration () const
		{ 
			return (m_fRunning ? GetCurrentValue() : m_dwAccValue);
		}

		// NOTE: destructor does not stop original measurer
		virtual ~CCumulativeDurationMeasurer ()
		{
		}
};

/*---------------------------------------------------------------------------*/

// class to measure difference in seconds between start/stop
class CSecondsMeasure : public IDurationMeasurer {
	protected:
		virtual DWORD GetCurrentValue () const
		{
			return (DWORD) ::time(NULL);
		}

	public:
		CSecondsMeasure (const BOOLEAN fStartNow=FALSE)
			: IDurationMeasurer()
		{
			if (fStartNow)
				Start();
		}

		virtual ~CSecondsMeasure ()
		{ 
		}
};
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

/* The components are set as follows:
 *
 *		tm_year - number of years in the duration
 *		tm_mon - number of months (after years have been deducted)
 *		tm_mday - number of days (after deducting years/months)
 *		tm_hour - number of hours (after deducting years/months/days)
 *		tm_min - number of minutes (after deducting years/months/days/hours)
 *		tm_sec - number of seconds (after deducting years/months/days/hours/minutes)
 */

extern EXC_TYPE DecodeTimeDurationComponents (const UINT32 ulSeconds, struct tm *pTM);

/*---------------------------------------------------------------------------*/

/* if multiple of 4, but not of 100, or multiple of 400 */
extern BOOLEAN IsLeapYear (const WORD wYear /* only of years >0, otherwise result is undefined */);

/* returns 1-7 (1=Sunday) or <=0 if error. NOTE: assumes only dates A.D. of the gregorian calendar (!) */
extern int GetDayOfWeekForDate (const BYTE bDay, const BYTE bMonth, const WORD wYear);

/*---------------------------------------------------------------------------*/

/* returns offset (sec.) since 1/1/1970 00:00:00 +0000 of specified date/time - 0==error*/
extern DWORD GetDateTimeGMTOffset (const struct tm *pTM);

/* decodes the offset since 1/1/1970 00:00:00 +0000 to its components */
extern EXC_TYPE DecodeGMTDateTimeOffset (const DWORD dwTimeValue, struct tm *pTM);

/*---------------------------------------------------------------------------*/

#endif
