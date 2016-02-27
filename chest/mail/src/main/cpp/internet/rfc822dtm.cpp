#include <_types.h>

#include <util/string.h>
#include <util/tables.h>
#include <util/errors.h>
#include <util/time.h>

#include <internet/rfc822.h>

/*---------------------------------------------------------------------------*/
/*
 *	Contains definitions related to RFC822 date/time handling
 */
/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
EXC_TYPE rfc822AddGMTOffset (const int tmZone, IStrlBuilder& strb)
{
	BYTE		hrsOffset=0, minsOffset=0;
	int		itz=(((-1) == tmZone) ? _timezone : tmZone);
	EXC_TYPE	exc=GetTimeZoneComponents(itz, &hrsOffset, &minsOffset);
	if (exc != EOK)
		return exc;

	if ((exc=strb.AddChar((itz <= 0) ? _T('+') : _T('-'))) != EOK)
		return exc;
	if ((exc=strb.AddPadNum(hrsOffset, 2)) != EOK)
		return exc;
	if ((exc=strb.AddPadNum(minsOffset, 2)) != EOK)
		return exc;

	return EOK;
}
#else
/*	tmZone is the difference (in seconds) between the GMT and LOCAL time */
EXC_TYPE rfc822BuildGMTOffset (const int tmZone, LPTSTR lpszGMTOffset, const UINT32 ulMaxLen)
{
	LPTSTR	lpszCurPos=lpszGMTOffset;
	UINT32	ulRemLen=ulMaxLen;

	return strlinsGMTOffset(&lpszCurPos, tmZone, &ulRemLen);
}
#endif	/* __cplusplus */

/*---------------------------------------------------------------------------*/

/* assumed format : "+/-HHMM" */
EXC_TYPE rfc822XlateExplicitGMTOffset (const char	lpszGMTOffset[],	/* empty == local */
													int			*pnGMTOffset)
{
	if (NULL == pnGMTOffset)
		return EPARAM;
	*pnGMTOffset = (-1);

	if (IsEmptyStr(lpszGMTOffset))
	{
		*pnGMTOffset = _timezone;
		return EOK;
	}

	if ((RFC822_POSITIVE_GMT == *lpszGMTOffset) || (RFC822_NEGATIVE_GMT == *lpszGMTOffset))
	{
		const char	*lpszSign=lpszGMTOffset;
		const char	*lpszHrs=(lpszSign + 1);
		EXC_TYPE		eh=EOK;
		UINT32		ulHours=argument_to_dword(lpszHrs, 2, EXC_ARG(eh));

		const char	*lpszMins=(lpszHrs+2);
		EXC_TYPE		em=EOK;
		/* some (non-STD) GMT offsets use ':' to separate the hours from the minutes */
		UINT32		ulMinutes=argument_to_dword(((':' == *lpszMins) ? (lpszMins+1) : lpszMins), 2, EXC_ARG(em));

		UINT32		ulSecs=(ulHours * 60UL * 60UL) + (ulMinutes * 60UL);

		if (eh != EOK)
			return eh;
		if (em != EOK)
			return em;

		*pnGMTOffset = (int) ((RFC822_POSITIVE_GMT == *lpszSign) ? (0 - ulSecs) : ulSecs);
		return EOK;
	}

	return EINVALIDDATE;
}

/*---------------------------------------------------------------------------*/

/* "Well-known" GMT offset(s) */
typedef struct {
	const char	*lpszGMTName;
	int			nGMTOffset;		/* in seconds */
} XLGMTNAME, *LPXLGMTNAME;

static const XLGMTNAME xlGMT[]={
	{	"GMT",			0			},
	{	"UT",				0			},
   {	"EST",  (5 * 60 * 60)	},	/* - 05:00 */
   {  "EDT",  (4 * 60 * 60)	}, /* - 04:00 */
   {  "CST",  (6 * 60 * 60)   },	/* - 06:00 */
   {  "CDT",  (5 * 60 * 60)   }, /* - 05:00 */
   {  "MST",  (7 * 60 * 60)   }, /* - 07:00 */
   {  "MDT",  (6 * 60 * 60)   }, /* - 06:00 */
   {  "PST",  (8 * 60 * 60)   }, /* - 08:00 */
   {  "PDT",  (7 * 60 * 60)   }, /* - 07:00 */
	{	NULL,			(-1)			}	/* mark end of list */
};

/*---------------------------------------------------------------------------*/

/* translates one of the RFC822 defined GMT codes to its value */
EXC_TYPE rfc822XlateImplicitGMTOffset (const char	lpszGMTOffset[],
													int			*pnGMTOffset)
{
	UINT32	ulIdx=0;

	if ((NULL == pnGMTOffset) || IsEmptyStr(lpszGMTOffset))
		return EPARAM;
	*pnGMTOffset = (-1);

	for (ulIdx=0; ; ulIdx++)
	{
		const XLGMTNAME *pXLGMT=&xlGMT[ulIdx];
		if ((NULL == pXLGMT->lpszGMTName) || ((-1) == pXLGMT->nGMTOffset))
			break;

		if (stricmp(pXLGMT->lpszGMTName, lpszGMTOffset) == 0)
		{
			*pnGMTOffset = pXLGMT->nGMTOffset;
			return EOK;
		}
	}

	/* this point is reached if unrecognized GMT offset code */
	return EFNEXIST;
}

/*---------------------------------------------------------------------------*/

/* translates either implicit or explicit GMT offset(s) to its value */
EXC_TYPE rfc822XlateGMTOffset (const char	lpszGMTOffset[],	/* empty == local */
										 int			*pnGMTOffset)		/* seconds */
{
	const char	*tsp=NULL;

	if (NULL == pnGMTOffset)
		return EPARAM;
	*pnGMTOffset = (-1);

	if (IsEmptyStr(lpszGMTOffset))
	{
		*pnGMTOffset = _timezone;
		return EOK;
	}

	/* check if have explicit offset */
	if (NULL == (tsp=strchr(lpszGMTOffset, RFC822_POSITIVE_GMT)))
		tsp = strchr(lpszGMTOffset, RFC822_NEGATIVE_GMT);

	if (tsp != NULL)
		return rfc822XlateExplicitGMTOffset(tsp, pnGMTOffset);
	else
		return rfc822XlateImplicitGMTOffset(tsp, pnGMTOffset);
}

/*---------------------------------------------------------------------------*/

/* Format is:
 *
 *	"%s, %u %s %u %02u:%02u:%02u +/-GMT",
 *		  day_of_week[tms.tm_wday], tms.tm_mday,
 *		  month_of_year[tms.tm_mon], (tms.tm_year + 1900),
 *		  tms.tm_hour, tms.tm_min, tms.tm_sec, _timezone/3600);
 *
 * Note: if supplied GMT offset is NULL/empty then "_timezone" variable is used
 */

EXC_TYPE BuildRFC822DTSDateTime (const struct tm	*pDT,
											const char			lpszGMTOffset[],
											char					lpszDateTime[],
											const UINT32		ulMaxLen)
{
	EXC_TYPE		exc=EOK;
	char			*lsp=lpszDateTime;
	UINT32		ulRemLen=ulMaxLen;
	int			wDay=(-1);

	if ((NULL == pDT) || (NULL == lpszDateTime) || (0 == ulMaxLen))
		return EPARAM;
	*lsp = '\0';

	if ((pDT->tm_wday > 6) || (pDT->tm_wday < 0) ||
		 (pDT->tm_mon > 11) || (pDT->tm_mon < 0))
		return EINVALIDDATE;

	if (((wDay=GetDayOfWeekForDate((BYTE) pDT->tm_mday, (BYTE) (pDT->tm_mon + 1), (WORD) (pDT->tm_year + 1900))) <= 0) || (wDay > 7))
		wDay = pDT->tm_wday;
	else
		wDay--;	// adjust as index 0-6

	if ((exc=strlinsstr(&lsp, day_of_week[wDay], &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsch(&lsp, ',', &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsch(&lsp, ' ', &ulRemLen)) != EOK)
		return exc;

	if ((exc=strlinsnum(&lsp, (UINT32) pDT->tm_mday, &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsch(&lsp, ' ', &ulRemLen)) != EOK)
		return exc;

	if ((exc=strlinsstr(&lsp, month_of_year[pDT->tm_mon], &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsch(&lsp, ' ', &ulRemLen)) != EOK)
		return exc;

	if ((exc=strlinsnum(&lsp, (UINT32) (pDT->tm_year + 1900), &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsch(&lsp, ' ', &ulRemLen)) != EOK)
		return exc;

	if ((exc=strlinstime(&lsp, pDT->tm_hour, pDT->tm_min, pDT->tm_sec, &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsch(&lsp, ' ', &ulRemLen)) != EOK)
		return exc;

	if (IsEmptyStr(lpszGMTOffset))
	{
		if ((exc=strlinsGMTOffset(&lsp, (-1), &ulRemLen)) != EOK)
			return exc;
	}
	else	/* have external GMT offset supplied */
	{
		if ((exc=strlinsstr(&lsp, lpszGMTOffset, &ulRemLen)) != EOK)
			return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE BuildRFC822TVDateTime (const time_t	tVal,
										  const char	lpszGMTOffset[],
										  char			lpszDateTime[],
										  const UINT32	ulMaxLen)
{
	time_t		t=tVal;
	struct tm	*pDT=NULL, tms;

	if ((-1) == ((long) tVal))
		return EPARAM;

	if (NULL == (pDT=localtime(&t)))
		return EABORTEXIT;
	tms = *pDT;

	return BuildRFC822DTSDateTime(&tms, lpszGMTOffset, lpszDateTime, ulMaxLen);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE BuildRFC822TVZoneDateTime (const time_t	tVal,
												const int		tmZone,
												char				lpszDateTime[],
												const UINT32	ulMaxLen)
{
	TCHAR		szGMTOffset[MAX_RFC822_TIMEZONE_LEN+2];
	EXC_TYPE	exc=rfc822BuildGMTOffset(tmZone, szGMTOffset, MAX_RFC822_TIMEZONE_LEN);
	if (exc != EOK)
		return exc;

	if ((exc=BuildRFC822TVDateTime(tVal, szGMTOffset, lpszDateTime, ulMaxLen)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

/* Note: assumes standard RFC822 date value */
EXC_TYPE rfc822ExtractGMTOffset (const char		*lpszDate,
											char				*lpszOffsetGMT,
											const UINT32	ulMaxGMTLen)
{
	LPCTSTR	tsp=NULL, pszE=NULL, pszS=NULL;
	UINT32	ulCLen=0;

	if (IsEmptyStr(lpszDate) || (NULL == lpszOffsetGMT) || (0 ==ulMaxGMTLen))
		return EPARAM;
	*lpszOffsetGMT = _T('\0');

	/* skip any terminating white space */
	for (pszE=strlast(lpszDate); pszE > lpszDate; pszE--)
	{
		if ((_T('\0') == *pszE) || _istspace(*pszE))
			continue;

		break;
	}

	for (tsp=pszE; tsp > lpszDate; tsp--)
	{
		/* skip comments (e.g. timezone identifier) */
		if (RFC822_HDR_COMMENT_EDELIM == *tsp)
		{
			for (tsp--; tsp > lpszDate; tsp--)
				if (RFC822_HDR_COMMENT_SDELIM == *tsp)
					break;

			if (RFC822_HDR_COMMENT_SDELIM != *tsp)
				return EINVALIDTIME;

			if ((tsp > lpszDate) && _istspace(*(tsp-1)))
			{
				/* go back to previous value */
				for (tsp--; tsp > lpszDate; tsp--)
					if (!_istspace(*tsp))
					{
						pszE = tsp;	/* update new "end" of date */
						tsp++;
						break;
					}

				if (lpszDate == tsp)
					return EINVALIDTIME;
			}

			continue;
		}

		if ((RFC822_POSITIVE_GMT == *tsp) || (RFC822_NEGATIVE_GMT == *tsp))
		{
			pszS = tsp;
			break;
		}

		if (_istspace(*tsp) && (*tsp != '\0'))
		{
			pszS = (tsp + 1);
			break;
		}
	}

	if (lpszDate == tsp)
		return EINVALIDTIME;

	/* return special code for missing offset sign */
	if ((RFC822_POSITIVE_GMT != *pszS) && (RFC822_NEGATIVE_GMT != *pszS))
		return EEMULATORTRAP;

	pszE++;	/* point one space BEYOND the offset */
	if ((ulCLen=(pszE-pszS)) >= ulMaxGMTLen)
		return EMEM;

	strncpy(lpszOffsetGMT, pszS, ulCLen);
	lpszOffsetGMT[ulCLen] = '\0';

	return EOK;
}

/*--------------------------------------------------------------------------*/

static EXC_TYPE GetDTNumComp (const char	**lppszCurPos, UINT32 *pulVal)
{
	const char	*lsp=NULL, *tsp=NULL;
	UINT32		cLen=0;
	EXC_TYPE		exc=EOK;

	if ((NULL == lppszCurPos) || (NULL == pulVal))
		return EPARAM;
	*pulVal = (UINT32) (~0);

	lsp = (*lppszCurPos);
	if (IsEmptyStr(lsp))
		return ENODATA;

	for ( ; (!isdigit(*lsp)) && (*lsp != '\0'); lsp++);
	for (tsp=lsp; isdigit(*lsp) && (*lsp != '\0'); lsp++);

	cLen = (lsp - tsp);
	*pulVal = argument_to_dword(tsp, cLen, EXC_ARG(exc));
	if (exc != EOK)
		return exc;

	*lppszCurPos = lsp;
	return EOK;
}

/*--------------------------------------------------------------------------*/

static EXC_TYPE DecodeRFC822DOM (const char	**lppszCurPos, int *pnDOM)
{
	UINT32	ulDOM=(UINT32) (-1);
	EXC_TYPE	exc=EOK;

	if ((NULL == lppszCurPos) || (NULL == pnDOM))
		return EPARAM;
	*pnDOM = (-1);

	if ((exc=GetDTNumComp(lppszCurPos, &ulDOM)) != EOK)
		return exc;

	if ((0 == ulDOM) || (ulDOM > 31UL))
		return EOVERFLOW;

	*pnDOM = (int) ulDOM;
	return EOK;
}

/*--------------------------------------------------------------------------*/

static EXC_TYPE DecodeRFC822MOY (const char	**lppszCurPos, int *pnMOY)
{
	const char	*lsp=NULL, *tsp=NULL;
	UINT32		cLen=0;

	if ((NULL == lppszCurPos) || (NULL == pnMOY))
		return EPARAM;
	*pnMOY = (-1);

	lsp = (*lppszCurPos);
	if (IsEmptyStr(lsp))
		return ENODATA;

	/* find and translate month of year */
	for (; isspace(*lsp) && (*lsp != '\0'); lsp++);
	for (tsp=lsp ; (!isspace(*lsp)) && (*lsp != '\0'); lsp++);
	cLen = (lsp - tsp);

	/* try using full month name if short not accomodate */
	if (cLen != ABBREV_MOY_LEN)
		*pnMOY = str2fullmoy(tsp, cLen);
	else
		*pnMOY = str2moy(tsp, cLen);
	if (*pnMOY >= 12)
		return EBADADDR;

	*lppszCurPos = lsp;
	return EOK;
}

/*--------------------------------------------------------------------------*/

static EXC_TYPE DecodeRFC822Year (const char	**lppszCurPos, int *pnYear)
{
	UINT32	ulYear=(UINT32) (-1);
	EXC_TYPE	exc=EOK;

	if ((NULL == lppszCurPos) || (NULL == pnYear))
		return EPARAM;
	*pnYear = (-1);

	if ((exc=GetDTNumComp(lppszCurPos, &ulYear)) != EOK)
		return exc;

	if (ulYear >= 1900UL)
		ulYear -= 1900UL;

	/* Y2K correction */
	if (ulYear < 70)
		ulYear += 100;

	*pnYear = (int) ulYear;
	return EOK;
}

/*--------------------------------------------------------------------------*/

static EXC_TYPE DecodeRFC822TimeElement (const char	**lppszCurPos,
													  const char	chSep,
													  const UINT32	ulMaxVal,
													  int				*ptElem)

{
	UINT32	ulElem=(UINT32) (-1);
	EXC_TYPE	exc=EOK;

	if ((NULL == lppszCurPos) || (NULL == ptElem))
		return EPARAM;
	*ptElem = (-1);

	if ((exc=GetDTNumComp(lppszCurPos, &ulElem)) != EOK)
		return exc;

	if (ulElem >= ulMaxVal)
		return EOVERFLOW;

	if (chSep != '\0')
	{
		if ((**lppszCurPos) != chSep)
			return EPREPOSITION;
		(*lppszCurPos)++;
	}

	*ptElem = (int) ulElem;
	return EOK;
}

/*--------------------------------------------------------------------------*/

static EXC_TYPE DecodeRFC822Hour (const char	**lppszCurPos, int *pnHour)
{
	return DecodeRFC822TimeElement(lppszCurPos, ':', 24UL, pnHour);
}

static EXC_TYPE DecodeRFC822Minute (const char	**lppszCurPos, int *pnMin, const BOOLEAN fHaveSep)
{
	return DecodeRFC822TimeElement(lppszCurPos, (fHaveSep ? ':' : '\0'), 60UL, pnMin);
}

static EXC_TYPE DecodeRFC822Second (const char	**lppszCurPos, int *pnSec)
{
	return DecodeRFC822TimeElement(lppszCurPos, '\0', 60UL, pnSec);
}

/*--------------------------------------------------------------------------*/

#define RFC822_MERIDIAN_LEN	2

static EXC_TYPE DecodeRFC822Meridian (const char **lppszCurPos, BOOLEAN *pfIsAM)
{
	const char	*lsp=NULL, *tsp=NULL;
	UINT32		cLen=0;

	if ((NULL == lppszCurPos) || (NULL == pfIsAM) || (NULL == (lsp=*lppszCurPos)))
		return EPARAM;

	for (; isspace(*lsp) && (*lsp != '\0'); lsp++);
	for (tsp=lsp ; (!isspace(*lsp)) && (*lsp != '\0'); lsp++);

	if ((cLen=(lsp - tsp)) != RFC822_MERIDIAN_LEN)
		return EINVALIDTIME;

	if (strnicmp(tsp, "AM", RFC822_MERIDIAN_LEN) == 0)
		*pfIsAM = TRUE;
	else if (strnicmp(tsp, "PM", RFC822_MERIDIAN_LEN) == 0)
		*pfIsAM = FALSE;
	else
		return ELOGNAMENEXIST;

	*lppszCurPos = lsp;
	return EOK;
}

/*--------------------------------------------------------------------------*/

static EXC_TYPE DecodeRFC822TimeValue (const char **lppszCurPos, struct tm *pDT)
{
	EXC_TYPE		exc=EOK;
	const char	*tsp=NULL;
	BOOLEAN		fHaveSeconds=FALSE;

	if ((NULL == lppszCurPos) || (NULL == pDT))
		return EPARAM;

	if ((exc=DecodeRFC822Hour(lppszCurPos, &(pDT->tm_hour))) != EOK)
		return exc;

	/* some non-STD time(s) do not have a seconds count */
	for (tsp=(*lppszCurPos); _istdigit(*tsp) && (*tsp != _T('\0')); tsp++);
	fHaveSeconds = (_T(':') == *tsp);

	if ((exc=DecodeRFC822Minute(lppszCurPos, &(pDT->tm_min), fHaveSeconds)) != EOK)
		return exc;

	if (fHaveSeconds)
	{
		if ((exc=DecodeRFC822Second(lppszCurPos, &(pDT->tm_sec))) != EOK)
			return exc;
	}
	else
	{
		pDT->tm_sec = 0;
	}

	/* some non-STD time(s) use AM/PM instead of 24-hour time */
	for (tsp=(*lppszCurPos); _istspace(*tsp) && (*tsp != _T('\0')); tsp++);
	if (((_T('A') == _totupper(*tsp)) || (_T('P') == _totupper(*tsp))) && (_T('M') == _totupper(*(tsp+1))))
	{
		BOOLEAN	fIsAM=FALSE;

		if ((exc=DecodeRFC822Meridian(lppszCurPos, &fIsAM)) != EOK)
			return exc;

		/* adjust hour for PM */
		if (!fIsAM)
			pDT->tm_hour += 12;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

/*
 *	Example(s):
 *
 *		"Monday, September 13, 1999 2:38 PM"
 *		"Thursday, 30 November 2000, 19:57:16"
 */

static EXC_TYPE DecodeFullRFC822DT (const char		szDTS[],
												const char		*lpszDOW,
												const UINT32	ulDOWLen,
												struct tm		*pDT)
{
	const char	*lsp=szDTS;
	EXC_TYPE		exc=EOK;

	if (IsEmptyStr(szDTS) || IsEmptyStr(lpszDOW) || (0 == ulDOWLen) || (NULL == pDT))
		return EPARAM;

	if ((pDT->tm_wday=str2fulldow(lpszDOW, ulDOWLen)) >= 7)
		return EBADADDR;

	for ( ; _istspace(*lsp) && (*lsp != _T('\0')); lsp++);

	/* check if day of month or month of year first */
	if (_istdigit(*lsp))
	{
		if ((exc=DecodeRFC822DOM(&lsp, &(pDT->tm_mday))) != EOK)
			return exc;

		if ((exc=DecodeRFC822MOY(&lsp, &(pDT->tm_mon))) != EOK)
			return exc;
	}
	else
	{
		if ((exc=DecodeRFC822MOY(&lsp, &(pDT->tm_mon))) != EOK)
			return exc;

		if ((exc=DecodeRFC822DOM(&lsp, &(pDT->tm_mday))) != EOK)
			return exc;
	}

	if ((exc=DecodeRFC822Year(&lsp, &(pDT->tm_year))) != EOK)
		return exc;

	if ((exc=DecodeRFC822TimeValue(&lsp, pDT)) != EOK)
		return exc;

	return EOK;
}

/*--------------------------------------------------------------------------*/

/* Format can be:
 *		a. "Wed, 14 Jul 1999 16:36:43 +0200"		- this is the standard
 *		b. "Sun, 1 Oct 2000 22:29 -0600"				- no seconds
 *		c. "Fri, Sep 29 2000 19:59:23 GMT+0000"	- day/month inversion
 *
 * Note: when it is called, "lpszDOW" points to day of week
 */
static EXC_TYPE DecodeStdRFC822DT (const char	szDTS[],
											  const char	*lpszDOW,
											  const UINT32	ulDOWLen,
											  struct tm		*pDT)
{
	const char	*lsp=szDTS, *tsp=NULL;
	EXC_TYPE		exc=EOK;
	BOOLEAN		fHaveSeconds=TRUE;

	if (IsEmptyStr(szDTS) || IsEmptyStr(lpszDOW) || (0 == ulDOWLen) || (NULL == pDT))
		return EPARAM;

	/* try full day name if abbreviation not found */
	if (ulDOWLen > ABBREV_DOW_LEN)
		return DecodeFullRFC822DT(szDTS, lpszDOW, ulDOWLen, pDT);

	if ((pDT->tm_wday=str2dow(lpszDOW, ulDOWLen)) >= 7)
		return EBADADDR;

	/* some (non-STD) dates contain the day+month inverted */
	for (; isspace(*lsp) && (*lsp != '\0'); lsp++);

	if (isdigit(*lsp))
	{
		if ((exc=DecodeRFC822DOM(&lsp, &(pDT->tm_mday))) != EOK)
			return exc;

		if ((exc=DecodeRFC822MOY(&lsp, &(pDT->tm_mon))) != EOK)
			return exc;
	}
	else	/* assume inversion */
	{
		if ((exc=DecodeRFC822MOY(&lsp, &(pDT->tm_mon))) != EOK)
			return exc;

		if ((exc=DecodeRFC822DOM(&lsp, &(pDT->tm_mday))) != EOK)
			return exc;
	}

	if ((exc=DecodeRFC822Year(&lsp, &(pDT->tm_year))) != EOK)
		return exc;

	if ((exc=DecodeRFC822TimeValue(&lsp, pDT)) != EOK)
		return exc;

	return EOK;
}

/*--------------------------------------------------------------------------*/

/* Format is:
 *
 *	"%s %s %02u %02u:%02u:%02u GMT %04u"
 * Example: "Wed Jun 09 18:38:07 PDT 1999"
 *
 * Note: when it is called, the day has already been successfully decoded
 */
static EXC_TYPE DecodeAVerRFC822DT (const char szDTS[], struct tm *pDT)
{
	const char	*lsp=szDTS;
	EXC_TYPE		exc=EOK;

	if (IsEmptyStr(szDTS) || (NULL == pDT))
		return EPARAM;

	if ((exc=DecodeRFC822MOY(&lsp, &(pDT->tm_mon))) != EOK)
		return exc;

	if ((exc=DecodeRFC822TimeValue(&lsp, pDT)) != EOK)
		return exc;

	for (; isspace(*lsp) && (*lsp != '\0'); lsp++);

	/* skip timezone specifier (if any) */
	if (!isdigit(*lsp))
	{
		char		szTZ[MAX_RFC822_TIMEZONE_LEN+2];
		UINT32	ulTZLen=0;

		for ( ; (ulTZLen <= MAX_RFC822_TIMEZONE_LEN) && (*lsp != '\0'); ulTZLen++, lsp++)
		{
			if (isspace(*lsp))
				break;

			szTZ[ulTZLen] = *lsp;
		}
		szTZ[ulTZLen] = '\0';
	}

	if ((exc=DecodeRFC822Year(&lsp, &(pDT->tm_year))) != EOK)
		return exc;

	return EOK;
}

/*--------------------------------------------------------------------------*/

/* Format is:
 *
 *	"%02u %s %u %02u:%02u[:%02u] GMT"
 * Example(s):
 *		"04 Apr 99 18:38 EST"
 *		"12 Jun 1998 03:25:44 EST"
 *
 * Note: when it is called, pointer to the day of month is supplied
 */
static EXC_TYPE DecodeBVerRFC822DT (const char		szDTS[],
												const char		*lpszDOM,
												const UINT32	ulDOMLen,
												struct tm		*pDT)
{
	const char	*lsp=szDTS;
	EXC_TYPE		exc=EOK;

	if (IsEmptyStr(szDTS) || IsEmptyStr(lpszDOM) || (0 == ulDOMLen) || (NULL == pDT))
		return EPARAM;

	pDT->tm_mday = (int) argument_to_word(lpszDOM, ulDOMLen, EXC_ARG(exc));
	if (exc != EOK)
		return exc;
	if ((pDT->tm_mday > 31) || (pDT->tm_mday < 1))
		return EOVERFLOW;

	if ((exc=DecodeRFC822MOY(&lsp, &(pDT->tm_mon))) != EOK)
		return exc;

	if ((exc=DecodeRFC822Year(&lsp, &(pDT->tm_year))) != EOK)
		return exc;

	if ((exc=DecodeRFC822TimeValue(&lsp, pDT)) != EOK)
		return exc;

	return EOK;
}

/*--------------------------------------------------------------------------*/

/* Format: 07/12/2000 19:23:00 */
static EXC_TYPE DecodeShortRFC822DT (const char szDTS[], struct tm *pDT)
{
	const char	*lsp=szDTS, *tsp=lsp;
	EXC_TYPE		exc=EOK;
	int			c1=0,c2=0;

	if (IsEmptyStr(szDTS) || (NULL == pDT))
		return EPARAM;

	for (tsp=lsp; (*tsp != '/') && (*tsp != '\0'); tsp++);

	c1 = (int) argument_to_word(lsp, (tsp - lsp), EXC_ARG(exc));
	if (exc != EOK)
		return exc;

	for (lsp=(tsp+1), tsp=lsp; (*tsp != '/') && (*tsp != '\0'); tsp++);
	c2 = (int) argument_to_word(lsp, (tsp - lsp), EXC_ARG(exc));
	if (exc != EOK)
		return exc;

	lsp = (tsp + 1);
	if ((exc=DecodeRFC822Year(&lsp, &(pDT->tm_year))) != EOK)
		return exc;

	if ((exc=DecodeRFC822TimeValue(&lsp, pDT)) != EOK)
		return exc;

	/* this is an error, regardless of the format */
	if ((c1 > 31) || (c2 > 31) ||
		 (0 == c1) || (0 == c2))
		return EOVERFLOW;

	/* check if European or American date */

	if (c1 > 12)	/* European format (i.e. C1=day of month) */
	{
		if ((c2 > 12) || (c2 < 1))
			return EOVERFLOW;

		pDT->tm_mon = (c2 - 1);
		pDT->tm_mday = c1;
	}
	else if (c2 > 12)	/* American format (i.e. C2=day of month) */
	{
		if ((c1 > 12) || (c1 < 1))
			return EOVERFLOW;

		pDT->tm_mon = (c1 - 1);
		pDT->tm_mday = c2;
	}
	else	/* ambiguous - get closest time to current one */
	{
		struct tm tm1=*pDT, tm2=*pDT;
		time_t		t1=0, t2=0;

		if ((c1 < 1) || (c2 < 1))
			return EUDFFORMAT;

		tm1.tm_mon = (c1 - 1);	tm2.tm_mon = (c2 - 1);
		tm1.tm_mday = c2;			tm2.tm_mday = c1;
		t1 = mktime(&tm1);		t2 = mktime(&tm2);

		if ((time_t) (-1) == t1)
		{
			if ((time_t) (-1) == t2)
				return EUDFFORMAT;

			*pDT = tm2;
		}
		else if ((time_t) (-1) == t2)
		{
			if ((time_t) (-1) == t1)
				return EUDFFORMAT;

			*pDT = tm1;
		}
		else	/* if ambiguous, then choose most recent */
		{
			if (t1 > t2)
				*pDT = tm1;
			else
				*pDT = tm2;
		}
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

/* Format: Jun 14 2002 04:59:08 */
static EXC_TYPE DecodeCVerRFC822DT (const char szDTS[], struct tm *pDT)
{
	/* NOTE !!! assumes called AFTER month already decoded (and DTS points to rest of it) */
	const char	*lsp=szDTS;
	EXC_TYPE		exc=EOK;

	if ((exc=DecodeRFC822DOM(&lsp, &(pDT->tm_mday))) != EOK)
		return exc;

	if ((exc=DecodeRFC822Year(&lsp, &(pDT->tm_year))) != EOK)
		return exc;

	if ((exc=DecodeRFC822TimeValue(&lsp, pDT)) != EOK)
		return exc;

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE	DecodeRFC822DateTime (const char szDTS[], struct tm *pDT)
{
	const char	*lsp=szDTS, *tsp=NULL;
	UINT32		cLen=0;

	if (IsEmptyStr(szDTS) || (NULL == pDT))
		return EPARAM;

	memset(pDT, 0, (sizeof *pDT));
	for ( ; isspace(*lsp) && (*lsp != '\0'); lsp++);

	for (tsp = lsp; (!isspace(*lsp)) && (*lsp != ',') && (*lsp != '\0'); lsp++);

	/* non-standard dates do not contain a day of week separator */
	cLen = (lsp - tsp);
	if (',' == *lsp)
		return DecodeStdRFC822DT((lsp+1), tsp, cLen, pDT);

	/* check if "day/month/year" */
	if (strnchr(tsp, '/', cLen) != NULL)
		return DecodeShortRFC822DT(tsp, pDT);

	/* check if day of week */
	if (cLen != ABBREV_DOW_LEN)
		pDT->tm_wday = str2fulldow(tsp, cLen);
	else
		pDT->tm_wday = str2dow(tsp, cLen);
	if (pDT->tm_wday < 7)
		return DecodeAVerRFC822DT(lsp, pDT);

	/* check if year of month */
	if (cLen != ABBREV_MOY_LEN)
		pDT->tm_mon = str2fullmoy(tsp, cLen);
	else
		pDT->tm_mon = str2moy(tsp, cLen);
	if (pDT->tm_mon < 12)
		return DecodeCVerRFC822DT(lsp, pDT);

	return DecodeBVerRFC822DT((lsp+1), tsp, cLen, pDT);
}

/*---------------------------------------------------------------------------*/
