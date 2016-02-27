#include <wtypes.h>
#include <tchar.h>
#include <winbase.h>

#include <util/time.h>
#include <win32/general.h>

/*--------------------------------------------------------------------------*/

/*
 * According to MSDN (ms-help://MS.MSDNQTR.2004OCT.1033/enu_kbvisualc/visualc/148681.htm):
 *
 *			The C run-time function localtime() incorrectly fills the tm_isdst
 *		member of the returning struct tm when it is executed in a time zone
 *		that does not switch from Daylight Savings to Standard time on the same
 *		date that the U.S. time zones make the switch. 
 *
 *	RESOLUTION:
 *
 * 1. Use the return value of the Win32 SDK function GetTimeZoneInformation, 
 *	which returns a DWORD. A return value of TIME_ZONE_ID_STANDARD or
 * TIME_ZONE_ID_DAYLIGHT will indicate whether the system time has been adjusted
 * for daylight savings time. GetTimeZoneInformation correctly considers the
 *	current system time zone settings and adjusts on the proper date. (See the
 * documentation for GetTimeZoneInformation, as there are other possible return
 * values.) 
 *	
 *		-or- 
 *
 * 2. Obtain the source for the C Run-time Library, and modify the localtime
 * function to adjust according to the current system time zone setting. The C
 * Run-time Library source is available in several locations.
 */
bool IsDSTActive (const DWORD dwRes, const TIME_ZONE_INFORMATION& tzi)
{
		 // return value from GetTimeZoneInformation MUST indicate this
	if ((TIME_ZONE_ID_DAYLIGHT == dwRes) &&
		 // according to MSDN "If this date is not specified, the wMonth member in the SYSTEMTIME structure must be zero"
		 (tzi.DaylightDate.wMonth != 0) &&
		 // according to MSDN "If DaylightDate is specified, the StandardDate value in the TIME_ZONE_INFORMATION structure must also be specified"
		 (tzi.StandardDate.wMonth != 0) &&
		 // makes no sense to have DST on and a zero bias (some kind of error
		 (tzi.DaylightBias != 0))
		return true;
	else
		return false;
}

bool IsDSTActive ()
{
	TIME_ZONE_INFORMATION	tzi={ 0 };
	const DWORD					dwRes=GetTimeZoneInformation(&tzi);
	return IsDSTActive(dwRes, tzi);
}

/*--------------------------------------------------------------------------*/

/* offset in sec. from GMT of local system - GMT+0200 => -7200, GMT-0200 => +7200 */
int GetLocalTimezoneOffset ()
{
	TIME_ZONE_INFORMATION	tzi={ 0 };
	const DWORD					dwRes=GetTimeZoneInformation(&tzi);
	LONG							curBias=tzi.Bias;

	if (IsDSTActive(dwRes, tzi))	// check if DST in effect
		curBias += tzi.DaylightBias;

	return ((int) curBias) * 60;
}

/*--------------------------------------------------------------------------*/

/* returns 0 if unsuccessful */
time_t SystemTimeToTime (const SYSTEMTIME *pst)
{
	struct tm	tms={ 0 };
	time_t		t=0;

	if (NULL == pst)
		return 0;

	memset(&tms, 0, (sizeof tms));

	tms.tm_year = (pst->wYear - 1900);
	tms.tm_mon = (pst->wMonth - 1);
	tms.tm_mday = pst->wDay;
	tms.tm_hour = pst->wHour;
	tms.tm_min = pst->wMinute;
	tms.tm_sec = pst->wSecond;
	/*
	 * According to MSDN (ms-help://MS.MSDNQTR.2004OCT.1033/vclib/html/_crt_localtime.htm):
	 *		"The target environment should try to determine whether daylight saving time is in effect."
	 */
	tms.tm_isdst = IsDSTActive() ? 1 : 0;

	_tzset();	// make sure any DST/TZ changes are detected

	if ((-1) == ((LONG) (t=mktime(&tms))))
		return 0;

	return t;
}

/*--------------------------------------------------------------------------*/

HRESULT TmValToSystemTime (const time_t tmVal, SYSTEMTIME& syst)
{
	_tzset();	// detect any TZ/DST changes
	return TmStructToSystemTime(localtime(&tmVal), &syst);
}

/*--------------------------------------------------------------------------*/

static int CompareTimeComponentValues (const WORD v1, const WORD v2)
{
	if (v1 < v2)
		return 1;
	else if (v1 == v2)
		return 0;
	else	// v1 > v2
		return (-1);
}

/* Compares only the DATE part - returns
 *		>0 - if v1 < v2
 *		=0 - if v1 == v2
 *		<0 - if v1 > v2
 */
int CompareSystemTimeDates (const SYSTEMTIME& v1, const SYSTEMTIME& v2)
{
	int	nRes=CompareTimeComponentValues(v1.wYear, v2.wYear);
	if (nRes != 0)
		return nRes;
	if ((nRes=CompareTimeComponentValues(v1.wMonth, v2.wMonth)) != 0)
		return nRes;
	if ((nRes=CompareTimeComponentValues(v1.wDay, v2.wDay)) != 0)
		return nRes;

	return 0;
}

/* Compares only the TIME part - returns
 *		>0 - if v1 < v2
 *		=0 - if v1 == v2
 *		<0 - if v1 > v2
 */
int CompareSystemTimeTimes (const SYSTEMTIME& v1, const SYSTEMTIME& v2)
{
	int	nRes=CompareTimeComponentValues(v1.wHour, v2.wHour);
	if (nRes != 0)
		return nRes;
	if ((nRes=CompareTimeComponentValues(v1.wMinute, v2.wMinute)) != 0)
		return nRes;
	if ((nRes=CompareTimeComponentValues(v1.wSecond, v2.wSecond)) != 0)
		return nRes;
	if ((nRes=CompareTimeComponentValues(v1.wMilliseconds, v2.wMilliseconds)) != 0)
		return nRes;

	return 0;
}

/* Returns:
 *		>0 - if v1 < v2
 *		=0 - if v1 == v2
 *		<0 - if v1 > v2
 */
int CompareSystemTimesValues (const SYSTEMTIME& v1, const SYSTEMTIME& v2)
{
	const int	nRes=CompareSystemTimeDates(v1, v2);
	if (nRes != 0)
		return nRes;
	else
		return CompareSystemTimeTimes(v1, v2);
}

/*--------------------------------------------------------------------------*/

/* returns 0 if unsuccessful */
time_t FileTimeToTime (const FILETIME *pft, const BOOL fIsLocal)
{
	FILETIME		localFTime={ 0 };
	SYSTEMTIME	sysTime={ 0 };

	if (NULL == pft)
		return 0;

	if (fIsLocal)
	{
		localFTime = *pft;
	}
	else
	{
		if (!FileTimeToLocalFileTime(pft, &localFTime))
			return 0;
	}

	if (!FileTimeToSystemTime(&localFTime, &sysTime))
		return 0;

	return SystemTimeToTime(&sysTime);
}

/*--------------------------------------------------------------------------*/

HRESULT TimeToFileTime (const time_t tVal, FILETIME *ft)
{
	SYSTEMTIME	sysTime={ 0 };
	HRESULT		hr=TmValToSystemTime(tVal, sysTime);
	if (hr != S_OK)
		return hr;

	if (NULL == ft)
		return ERROR_BAD_ARGUMENTS;

	if (!SystemTimeToFileTime(&sysTime, ft))
	{
		if (S_OK == (hr=GetLastError()))
			hr = ERROR_BAD_ENVIRONMENT;
		return hr;
	}

	return S_OK;
}

/*--------------------------------------------------------------------------*/

HRESULT TmStructToSystemTime (const struct tm *ptm, SYSTEMTIME *pst)
{
	if ((NULL == ptm) || (NULL == pst))
		return ERROR_BAD_ARGUMENTS;
	memset(pst, 0, (sizeof *pst));

	pst->wYear = (ptm->tm_year + 1900);
	pst->wMonth = (ptm->tm_mon + 1);
	pst->wDay = ptm->tm_mday;
	pst->wDayOfWeek = ptm->tm_wday;
	pst->wHour = ptm->tm_hour;
	pst->wMinute = ptm->tm_min;
	pst->wSecond = ptm->tm_sec;

	return ERROR_SUCCESS;
}

/*--------------------------------------------------------------------------*/

HRESULT RecalculateSystemTimeTimezone (const SYSTEMTIME	*pst,
													const int			orgTmZone,
													const int			newTmZone,
													SYSTEMTIME			*ost)
{
	if ((NULL == pst) || (NULL == ost))
		return ERROR_BAD_ARGUMENTS;
	memset(ost, 0, (sizeof *ost));

	if (orgTmZone != newTmZone)
	{
		time_t	tmVal=SystemTimeToTime(pst), tmAdjusted=RecalculateTmValTimezone(tmVal, orgTmZone, newTmZone);
		if (0 == tmVal)
			return ERROR_BAD_ENVIRONMENT;

		return TmValToSystemTime(tmAdjusted, *ost);
	}

	*ost = *pst;
	return S_OK;
}

/*--------------------------------------------------------------------------*/

HRESULT AdjustSystemTimeTimezone (const SYSTEMTIME	*pst,
											 const int			tmZone,
											 SYSTEMTIME			*ost)
{
	if ((NULL == pst) || (NULL == ost))
		return ERROR_BAD_ARGUMENTS;
	memset(ost, 0, (sizeof *ost));

	time_t	tmVal=SystemTimeToTime(pst), tmAdjusted=RecalculateTmValTimezone(tmVal, tmZone, GetLocalTimezoneOffset());
	if (0 == tmVal)
		return ERROR_BAD_ENVIRONMENT;

	return TmValToSystemTime(tmAdjusted, *ost);
}

/*--------------------------------------------------------------------------*/

HRESULT GetAdjustedLocalSysTime (const int newTmZone, SYSTEMTIME *ost)
{
	SYSTEMTIME	lclTime={ 0 };

	if (NULL == ost)
		return ERROR_BAD_ARGUMENTS;

	GetLocalTime(&lclTime);
	return AdjustSystemTimeTimezone(&lclTime, newTmZone, ost);
}

/*--------------------------------------------------------------------------*/

HRESULT SystemTimeToTmStruct (const SYSTEMTIME *pst,  struct tm *ptm)
{
	if ((NULL == ptm) || (NULL == pst))
		return ERROR_BAD_ARGUMENTS;
	memset(ptm, 0, (sizeof *ptm));

	ptm->tm_year = pst->wYear - 1900;
	ptm->tm_mon = pst->wMonth - 1;
	ptm->tm_mday = pst->wDay;
	ptm->tm_wday = pst->wDayOfWeek;
	ptm->tm_hour = pst->wHour;
	ptm->tm_min = pst->wMinute;
	ptm->tm_sec = pst->wSecond;

	return ERROR_SUCCESS;
}

/*--------------------------------------------------------------------------*/

HRESULT GetUniqueFileName (const char	pszPrefix[],
									const char	pszSuffix[],
									char			szPath[],
									const ULONG	ulPathLen)
{
	char	szTempPath[MAX_PATH+1]="", *pszd=NULL;
	DWORD	dwSize=0;

	if ((NULL == pszPrefix) || (NULL == pszSuffix) || (NULL == szPath) || (0 == ulPathLen))
		return ERROR_BAD_ARGUMENTS;

	dwSize = GetTempPath(ulPathLen, szPath);
	if ((0 == dwSize) || (dwSize >= ulPathLen))
		return ERROR_BAD_ENVIRONMENT;

	dwSize = GetTempFileName(szPath, pszPrefix, 0, szTempPath);
	if (0 == dwSize)
		return GetLastError();

	if (NULL == (pszd=strrchr(szTempPath, '.')))
		return ERROR_BAD_FORMAT;

	if ('.' != *pszSuffix) pszd++;
	*pszd = '\0';
	strcpy(pszd, pszSuffix);
	if (strlen(szTempPath) >= ulPathLen)
		return ERROR_NOT_ENOUGH_MEMORY;

	strcpy(szPath, szTempPath);
	return ERROR_SUCCESS;
}

/*--------------------------------------------------------------------------*/

HRESULT UnicodeToAnsi (LPCWSTR pszW, LPSTR pszA, const ULONG ulASize)
{
    ULONG cbAnsi=0, cCharacters=0, dwError=ERROR_SUCCESS;

    // If input is null then just return the same.
    if ((pszW == NULL) || (pszA == NULL))
		 return ERROR_BAD_ARGUMENTS;
	 *pszA = '\0';

    cCharacters = wcslen(pszW) + 1;

    // Determine number of bytes to be allocated for ANSI string. An
    // ANSI string can have at most 2 bytes per character (for Double
    // Byte Character Strings.) =
    if ((cbAnsi=cCharacters*2) >= ulASize)
		 return ERROR_NOT_ENOUGH_MEMORY;

    // Convert to ANSI.
    if (0 == (dwError=WideCharToMultiByte(CP_ACP, 0, pszW, cCharacters, pszA, cbAnsi, NULL, NULL)))
    {
        dwError = GetLastError();
        return dwError;
    }

    return ERROR_SUCCESS;
}

/*--------------------------------------------------------------------------*/

/*
 * AnsiToUnicode converts the ANSI string pszA to a Unicode string
 * and returns the Unicode string through ppszW.
 */
HRESULT AnsiToUnicode (LPCSTR pszA, LPWSTR pszW, const ULONG ulWLen)
{ 
	ULONG cCharacters=0, dwError=ERROR_SUCCESS;
	
   // If input is null then just return the same.
   if ((NULL == pszA) || (NULL == pszW))
		return ERROR_BAD_ARGUMENTS;
	*pszW = 0;

   // Determine number of wide characters to be allocated for the
   // Unicode string.
   cCharacters = (strlen(pszA)+1);
	if (cCharacters >= ulWLen)
		 return ERROR_NOT_ENOUGH_MEMORY;

	// Covert to Unicode.
   if (0 == (dwError=MultiByteToWideChar(CP_ACP, 0, pszA, cCharacters, pszW, cCharacters)))
	{
		dwError = GetLastError();
		return dwError;
	}

	return ERROR_SUCCESS;
} 

/*--------------------------------------------------------------------------*/

HRESULT GetSysErrorText (const HRESULT	rhr,
								 LPTSTR			lpszErrStr,
								 const ULONG	ulMaxLen)
{
	if ((NULL == lpszErrStr) || (0 == ulMaxLen))
		return ERROR_BAD_ARGUMENTS;
	*lpszErrStr = _T('\0');

	LPVOID	lpMsgBuf=NULL;
	ULONG		ulMsgLen=FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER | 
													FORMAT_MESSAGE_FROM_SYSTEM | 
													FORMAT_MESSAGE_IGNORE_INSERTS,
											  NULL,
											  rhr,
											  MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), /* Default language */
											  (LPTSTR) &lpMsgBuf,
											  0,
											  NULL);
	HRESULT	hr=ERROR_SUCCESS;
	if (ulMsgLen > 1)
	{
		ULONG	ulCLen=min(ulMsgLen,ulMaxLen)-1;
		_tcsncpy(lpszErrStr, (LPCTSTR) lpMsgBuf, ulCLen);
		lpszErrStr[ulCLen] = _T('\0');

		if ((ulCLen > 0) && (_T('\n') == lpszErrStr[ulCLen-1]))
		{
			ulCLen--;
			lpszErrStr[ulCLen] = _T('\0');
		}

		if ((ulCLen > 0) && (_T('\r') == lpszErrStr[ulCLen-1]))
		{
			ulCLen--;
			lpszErrStr[ulCLen] = _T('\0');
		}
	}
	else
	{
		hr = GetLastError();
	}


	if (lpMsgBuf != NULL)
	{
		LocalFree(lpMsgBuf);
		lpMsgBuf = NULL;
	}

	return hr;
}

#ifdef __cplusplus
//////////////////////////////////////////////////////////////////////////////

// Note: setting a new string deletes the old one
HRESULT CUTF8ToWideCharString::SetString (LPCSTR lpszUTF8Str, const ULONG ulUTF8Len)
{
	m_wsg.Release();

	if (0 == ulUTF8Len)
		return S_OK;

	if (NULL == lpszUTF8Str)
		return ERROR_BAD_ARGUMENTS;

	if (NULL == (m_pW=new WCHAR[ulUTF8Len+2]))
		return ERROR_OUTOFMEMORY;
	*m_pW = L'\0';

	int	wLen=::MultiByteToWideChar(CP_UTF8, 0, lpszUTF8Str, (int) ulUTF8Len, m_pW, (int) (ulUTF8Len+1));
	if (0 == wLen)
	{
		HRESULT	hr=GetLastError();
		m_wsg.Release();
		return hr;
	}

	m_pW[wLen] = L'\0';	// just making sure
	return S_OK;
}

/*--------------------------------------------------------------------------*/

// Note: setting a new string deletes the old one
HRESULT CWideCharToUTF8String::SetString (LPCWSTR lpwStr, const ULONG ulWLen, const size_t sAvgMBEncLen)
{
	m_ssg.Release();

	if (0 == ulWLen)
		return S_OK;

	if ((NULL == lpwStr) || (sAvgMBEncLen <= 0))
		return ERROR_BAD_ARGUMENTS;

	const int nBufSize=((ulWLen + 2) * sAvgMBEncLen);
	if (NULL == (m_pS=new TCHAR[nBufSize+2]))
		return ERROR_OUTOFMEMORY;
	*m_pS = L'\0';

	int	wLen=::WideCharToMultiByte(CP_UTF8, 0, lpwStr, (int) ulWLen,  m_pS, nBufSize , NULL, NULL);
	if (0 == wLen)
	{
		HRESULT	hr=GetLastError();
		m_ssg.Release();
		return hr;
	}

	m_pS[wLen] = _T('\0');	// just making sure
	return S_OK;
}

//////////////////////////////////////////////////////////////////////////////

// resolution may be 1 (seconds), 1000 (msec.), 1000000 (microsec), 1000000000 (nanosec.)
CHiResPerfMeasure::CHiResPerfMeasure (const DWORD dwSecTicks, const BOOLEAN fStartNow)
	: IDurationMeasurer(), m_dwSecTicks(dwSecTicks)
{
#if FALSE
	if ((dwSecTicks != 1) &&
		 (dwSecTicks != 1000UL)
		 (dwSecTicks != 1000000UL)
		 (dwSecTicks != 1000000000UL)
		throw $$$;
#endif

	if (!::QueryPerformanceFrequency(&m_freq))
	{
		HRESULT	hr=::GetLastError();
#if FALSE
		throw $$$;
#endif
		::memset(&m_freq, 0, (sizeof m_freq));
	}

	::memset(&m_cStart, 0, (sizeof m_cStart));
	::memset(&m_cEnd, 0, (sizeof m_cEnd));

	if (fStartNow)
		Start();
}

/*--------------------------------------------------------------------------*/

void CHiResPerfMeasure::GetCurrentValue (LARGE_INTEGER& cv) const
{
	if (!::QueryPerformanceCounter(&cv))
	{
		HRESULT	hr=::GetLastError();
#if FALSE
		throw $$$;
#endif
		::memset(&cv, 0, (sizeof cv));
	}
}

/*--------------------------------------------------------------------------*/

DWORD CHiResPerfMeasure::GetCurrentValue () const
{
	LARGE_INTEGER	cv={ 0 };
	GetCurrentValue(cv);
	return cv.LowPart;
}

/*--------------------------------------------------------------------------*/

DWORD CHiResPerfMeasure::Start ()
{
	GetCurrentValue(m_cStart);
	m_cEnd = m_cStart;
	m_fRunning = TRUE;
	return m_cStart.LowPart;
}

/*--------------------------------------------------------------------------*/

DWORD CHiResPerfMeasure::Stop ()
{
	if (m_fRunning)
	{
		GetCurrentValue(m_cEnd);
		m_fRunning = FALSE;
	}

	return m_cEnd.LowPart;
}

/*--------------------------------------------------------------------------*/

// returns duration not normalized to the ticks measure
const LARGE_INTEGER CHiResPerfMeasure::RawDuration () const
{
	LARGE_INTEGER	ev(m_cEnd);
	if (m_fRunning)
		GetCurrentValue(ev);

	LARGE_INTEGER df={ 0 };
	df.QuadPart=(ev.QuadPart - m_cStart.QuadPart);
	return df;
}

/*--------------------------------------------------------------------------*/

// returns duration normalized to the ticks measure
const LARGE_INTEGER CHiResPerfMeasure::FullDuration () const
{
	LARGE_INTEGER	du={ 0 };
	if (m_freq.QuadPart != 0)
	{
		const LARGE_INTEGER	df=RawDuration();
		du.QuadPart = ((df.QuadPart * m_dwSecTicks) / m_freq.QuadPart);
	}

	return du;
}

/*--------------------------------------------------------------------------*/

// Note: returns ZERO if duration cannot be contained in a single DWORD
DWORD CHiResPerfMeasure::Duration () const
{
	const LARGE_INTEGER	du=FullDuration();
	if (du.HighPart != 0)
	{
#if FALSE
		throw $$$;
#endif
		return 0;
	}

	return du.LowPart;
}

//////////////////////////////////////////////////////////////////////////////

HRESULT CSystemTime::SetTime (const SYSTEMTIME& sysTime)
{
	SYSTEMTIME&	dst=(*this);
	dst = sysTime;
	return S_OK;
}

//////////////////////////////////////////////////////////////////////////////

HRESULT CSystemTimeUpdater::SetTimeValue (const time_t tmVal)
{
	HRESULT	hr=CSystemTime::SetTimeValue(tmVal);
	if (hr != S_OK)
		return hr;

	m_tmVal = tmVal;
	return S_OK;
}

/*--------------------------------------------------------------------------*/

HRESULT CSystemTimeUpdater::SetTmStructValue (const struct tm& tmv)
{
	HRESULT	hr=CSystemTime::SetTmStructValue(tmv);
	if (hr != S_OK)
		return hr;

	if (0 == (m_tmVal=::SystemTimeToTime(this)))
		return ERROR_INVALID_SERVER_STATE;

	return S_OK;
}

/*--------------------------------------------------------------------------*/

HRESULT CSystemTimeUpdater::SetTime (const SYSTEMTIME& sysTime)
{
	if (0 == (m_tmVal=::SystemTimeToTime(&sysTime)))
		return ERROR_INVALID_SERVER_STATE;

	return CSystemTime::SetTime(sysTime);
}

/*--------------------------------------------------------------------------*/

HRESULT CSystemTimeUpdater::AddSeconds (const LONG lSecs)
{
	if (lSecs != 0L)
	{
		const LONG	lCurTime=(LONG) GetTimeValue(),
						lNewTime=lCurTime + lSecs;
		return SetTimeValue((time_t) lNewTime);
	}
	else
		return S_OK;
}

//////////////////////////////////////////////////////////////////////////////

#endif	/* of __cplusplus */
