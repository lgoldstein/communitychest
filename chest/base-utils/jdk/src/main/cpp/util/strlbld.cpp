#include <stdio.h>

#include <util/string.h>
#include <util/time.h>
#include <util/errors.h>

#ifndef __cplusplus
#error "This file requires a C++ compiler !!!"
#endif	/* of __cplusplus */

//////////////////////////////////////////////////////////////////////////////

CGreedyString::CGreedyString (const UINT32 ulGrowLen)
	: m_lpszBuf(NULL)
	, m_ulMaxLen(0)
	, m_ulCurLen(0)
	, m_ulGrowLen(ulGrowLen)
{
}

/*---------------------------------------------------------------------------*/

// can be NULL/empty
EXC_TYPE CGreedyString::Set (LPCTSTR lpszVal, const UINT32 ulVLen)
{
	if (0 != ulVLen)
	{
		if (ulVLen > m_ulMaxLen)
		{
			const UINT32	ulNewLen=(ulVLen + m_ulGrowLen);

			::strreleasebuf(m_lpszBuf);
			m_ulMaxLen = 0;
			m_ulCurLen = 0;

			if (NULL == (m_lpszBuf=new TCHAR[ulNewLen+2]))
				return EMEM;

			*m_lpszBuf = _T('\0');
			m_ulMaxLen = ulNewLen;
		}

		_tcsncpy(m_lpszBuf, lpszVal, ulVLen);
		m_lpszBuf[ulVLen] = _T('\0');
	}
	else
	{
		if (m_lpszBuf != NULL)
			*m_lpszBuf = _T('\0');
	}

	m_ulCurLen = ulVLen;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE IStrlBuilder::AddChar (const TCHAR tch)
{
	if (_T('\0') == tch)
		return EPARAM;

	TCHAR	sz[2]={ tch, _T('\0') };

	return AddChars(sz, 1);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE IStrlBuilder::Repeat (const TCHAR tch, const UINT32 ulNumReps)
{
	for (UINT32	rIndex=0; rIndex < ulNumReps; rIndex++)
	{
		EXC_TYPE	exc=AddChar(tch);
		if (exc != EOK)
			return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE IStrlBuilder::AddNum (const UINT32 ulVal)
{
	TCHAR		szVal[MAX_DWORD_DISPLAY_LENGTH+2]=_T("");
	UINT32	vLen=::dword_to_argument(ulVal, szVal);

	return AddChars(szVal, vLen);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE IStrlBuilder::AddPadNum (const UINT32	ulVal,
											 const UINT32	ulVLen,	/* may NOT be 0 */
											 const TCHAR	padCh,	/* may NOT be '\0' */
											 const BOOLEAN	fLeftPad)
{
	if ((0 == ulVLen) || (_T('\0') == padCh))
		return EPARAM;

	TCHAR		szV[MAX_DWORD_DISPLAY_LENGTH+2]=_T("");
	UINT32	ulVLL=::dword_to_argument(ulVal, szV), vIdx=ulVLL;
	if (ulVLL > ulVLen)
		return EOVERFLOW;

	EXC_TYPE	exc=EOK;
	if (fLeftPad)
	{
		/* add padding */
		for (vIdx=ulVLL; vIdx < ulVLen; vIdx++)
			if ((exc=AddChar(padCh)) != EOK)
				return exc;

		if ((exc=AddChars(szV, ulVLL)) != EOK)
			return exc;
	}
	else	/* right pad */
	{
		if ((exc=AddChars(szV, ulVLL)) != EOK)
			return exc;

		/* add padding */
		for (vIdx=ulVLL; vIdx < ulVLen; vIdx++)
			if ((exc=AddChar(padCh)) != EOK)
				return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE IStrlBuilder::AddTime (const UINT8	bHour,
										  const UINT8	bMinute,
										  const UINT8	bSecond)
{
	EXC_TYPE	exc=EOK;

	if ((bHour >= 24) || (bMinute >= 60) || (bSecond >= 60))
		return EINVALIDTIME;

	if ((exc=AddPadNum(bHour, 2)) != EOK)
		return exc;
	if ((exc=AddChar(_T(':'))) != EOK)
		return exc;

	if ((exc=AddPadNum(bMinute, 2)) != EOK)
		return exc;
	if ((exc=AddChar(_T(':'))) != EOK)
		return exc;

	if ((exc=AddPadNum(bSecond, 2)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE IStrlBuilder::AddGMTOffset (const int	iTimeZone)	/* (-1) == use internal */
{
	BYTE		hrsOffset=0, minsOffset=0;
	int		itz=(((-1) == iTimeZone) ? _timezone : iTimeZone);
	EXC_TYPE	exc=::GetTimeZoneComponents(itz, &hrsOffset, &minsOffset);
	if (exc != EOK)
		return exc;

	if ((exc=AddChar(((itz < 0) ? _T('+') : _T('-')))) != EOK)
		return exc;
	if ((exc=AddPadNum(hrsOffset, 2)) != EOK)
		return exc;
	if ((exc=AddPadNum(minsOffset, 2)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE IStrlBuilder::AddNonEmptyChars (LPCTSTR lpszChars, const UINT32 ulCLen)
{
	if (0 == ulCLen)
		return EEMPTYENTRY;
	else
		return AddChars(lpszChars, ulCLen);
}

/*---------------------------------------------------------------------------*/

UINT32 IStrlBuilder::GetCurLen () const
{
	LPCTSTR	lpszCurPos=GetCurPos(), lpszBufPos=GetBuffer();
	if (IsEmptyStr(lpszBufPos) || (NULL == lpszCurPos))
		return 0;
	else
		return (lpszCurPos - lpszBufPos);
}

//////////////////////////////////////////////////////////////////////////////

CStrlBuilder::CStrlBuilder (LPTSTR& lpszCurPos, UINT32& ulRemLen, bool fDummy /* only to differentiate it from the other constructor */)
	: IStrlBuilder()
	, m_lpszIBuf(lpszCurPos)
	, m_ulILen(ulRemLen)
	, m_lpszBuf(lpszCurPos)
	, m_ulMaxLen(ulRemLen)
	, m_lpszCurPos(lpszCurPos)
	, m_ulRemLen(ulRemLen)
{
}

/*---------------------------------------------------------------------------*/

CStrlBuilder::CStrlBuilder (LPTSTR lpszBuf, const UINT32 ulMaxLen)
	: IStrlBuilder()
	, m_lpszIBuf(lpszBuf)
	, m_ulILen(ulMaxLen)
	, m_lpszBuf(lpszBuf)
	, m_ulMaxLen(ulMaxLen)
	, m_lpszCurPos(m_lpszBuf)
	, m_ulRemLen(m_ulMaxLen)
{
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CStrlBuilder::Repeat (const TCHAR tch, const UINT32 ulNumReps /* may be zero */)
{
	if (_T('\0') == tch)
		return EPARAM;

	if (m_ulRemLen < ulNumReps)	// check if have enough space
		return EOVERFLOW;

	for (UINT32	rIndex=0; rIndex < ulNumReps; rIndex++, m_lpszCurPos++, m_ulRemLen--)
		*m_lpszCurPos = tch;

	*m_lpszCurPos = _T('\0');	// mark end
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CStrlBuilder::Addf (LPCTSTR lpszFmt, ...)
{
	va_list	ap;
	va_start(ap, lpszFmt);
	EXC_TYPE	exc=VAddf(lpszFmt, ap);
	va_end(ap);
	return exc;
}

//////////////////////////////////////////////////////////////////////////////

CIncStrlBuilder::CIncStrlBuilder (const UINT32 ulInitialSize, const UINT32 ulGrowSize)
	: IStrlBuilder()
	, m_lpszBuf(NULL)
	, m_ulMaxLen(0)
	, m_ulGrowLen(ulGrowSize)
	, m_lpszCurPos(NULL)
	, m_ulCurLen(0)
{
	if (0 == ulInitialSize)
		return;

	if ((m_lpszBuf=new TCHAR[ulInitialSize+sizeof(NATIVE_WORD)]) != NULL)
	{
		*m_lpszBuf = _T('\0');
		m_lpszCurPos = m_lpszBuf;
		m_ulMaxLen = ulInitialSize;
	}
}

/*---------------------------------------------------------------------------*/

CIncStrlBuilder::CIncStrlBuilder (LPCTSTR lpszInitialValue, const UINT32 ulGrowSize)
	: IStrlBuilder()
	, m_lpszBuf(NULL)
	, m_ulMaxLen(0)
	, m_ulGrowLen(ulGrowSize)
	, m_lpszCurPos(NULL)
	, m_ulCurLen(0)
{
	if (IsEmptyStr(lpszInitialValue))
		return;

	UINT32	ulInitialSize=::_tcslen(lpszInitialValue);
	if ((m_lpszBuf=new TCHAR[ulInitialSize+sizeof(NATIVE_WORD)]) != NULL)
	{
		_tcscpy(m_lpszBuf, lpszInitialValue);
		m_ulMaxLen = ulInitialSize;
		m_ulCurLen = ulInitialSize;
		m_lpszCurPos = (m_lpszBuf + m_ulCurLen);
	}
}

/*---------------------------------------------------------------------------*/

void CIncStrlBuilder::Reset ()
{
	if ((m_lpszCurPos=m_lpszBuf) != NULL)
		*m_lpszCurPos = _T('\0');

	m_ulCurLen = 0;
}

/*---------------------------------------------------------------------------*/

// re-allocates if necessary
EXC_TYPE CIncStrlBuilder::CheckAvailability (const UINT32 ulCLen)
{
	UINT32	ulReqLen=(m_ulCurLen + ulCLen);
	if (ulReqLen <= m_ulMaxLen)
		return EOK;

	if (0 == m_ulGrowLen)
		return EOVERFLOW;

	LPTSTR	lpszNewBuf=new TCHAR[ulReqLen+m_ulGrowLen+sizeof(NATIVE_WORD)];
	if (NULL == lpszNewBuf)
		return EMEM;
	*lpszNewBuf = _T('\0');

	if (m_lpszBuf != NULL)
	{
		// using "memcpy" in case have EOS in mid-string
		::memcpy(lpszNewBuf, m_lpszBuf, (m_ulCurLen * sizeof(TCHAR)));
		lpszNewBuf[m_ulCurLen] = _T('\0');
		delete [] m_lpszBuf;
	}

	m_lpszBuf = lpszNewBuf;
	m_ulMaxLen = (ulReqLen + m_ulGrowLen);
	m_lpszCurPos = (m_lpszBuf + m_ulCurLen);
	*m_lpszCurPos = _T('\0');

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIncStrlBuilder::Repeat (const TCHAR tch, const UINT32 ulNumReps /* may be zero */)
{
	if (_T('\0') == tch)
		return EPARAM;

	EXC_TYPE	exc=CheckAvailability(ulNumReps);
	if (exc != EOK)
		return exc;

	for (UINT32	rIndex=0; rIndex < ulNumReps; rIndex++, m_lpszCurPos++, m_ulCurLen++)
		*m_lpszCurPos = tch;

	*m_lpszCurPos = _T('\0');	// mark end
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIncStrlBuilder::AddChars (LPCTSTR lpszChars, const UINT32 ulCLen)
{
	if ((NULL == lpszChars) && (ulCLen != 0))
		return EUDFFORMAT;

	EXC_TYPE	exc=CheckAvailability(ulCLen);
	if (exc != EOK)
		return exc;

	if (ulCLen != 0)
	{
		::_tcsncpy(m_lpszCurPos, lpszChars, ulCLen);
		m_lpszCurPos += ulCLen;
		*m_lpszCurPos = _T('\0');
		m_ulCurLen += ulCLen;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIncStrlBuilder::AddEOS ()
{
	EXC_TYPE	exc=CheckAvailability(1);
	if (exc != EOK)
		return exc;

	*m_lpszCurPos = _T('\0');
	m_lpszCurPos++;
	m_ulCurLen++;

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////
