#include <stdio.h>

#include <util/string.h>
#include <util/time.h>
#include <util/errors.h>

/*---------------------------------------------------------------------------*/

EXC_TYPE strlinschars (LPTSTR			*lppszCurPos,
							  LPCTSTR		lpszChars,	/* may be NULL if 0 len */
							  const UINT32	ulCLen,
							  UINT32			*pulRemLen)
{
	LPTSTR	lsp=NULL;

	if ((NULL == lppszCurPos) || (NULL == (lsp=*lppszCurPos)) ||
		 ((NULL == lpszChars) && (ulCLen != 0)) || (NULL == pulRemLen))
		return EPARAM;

	if (0 == ulCLen)
		return EOK;

	if (ulCLen >= (*pulRemLen))
		return EOVERFLOW;

	*lppszCurPos = strlncat(lsp, lpszChars, ulCLen);
	lsp[ulCLen] = _T('\0');
	*pulRemLen -= ulCLen;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE strlinsstr (LPTSTR	*lppszCurPos,
							LPCTSTR	lpszStr,	/* may be NULL/empty */
							UINT32	*pulRemLen)
{
	return ((NULL == lpszStr) ? EOK : strlinschars(lppszCurPos, lpszStr, strlen(lpszStr), pulRemLen));
}

/*---------------------------------------------------------------------------*/

EXC_TYPE strlinsch (LPTSTR			*lppszCurPos,
						  const TCHAR	tch,	/* may NOT be '\0' */
						  UINT32			*pulRemLen)
{
	TCHAR	szV[2];

	if (_T('\0') == tch)
		return EPARAM;

	szV[0] = tch;
	szV[1] = '\0';

	return strlinschars(lppszCurPos, szV, 1UL, pulRemLen);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE strlinseos (LPTSTR *lppszCurPos, UINT32 *pulRemLen)
{
	LPTSTR	lsp=NULL;

	if ((NULL == lppszCurPos) || (NULL == (lsp=(*lppszCurPos))) || (NULL == pulRemLen))
		return EPARAM;

	if (*pulRemLen <= 1)
		return EOVERFLOW;

	*lsp = _T('\0');
	lsp++;
	*lsp = _T('\0');
	(*pulRemLen)--;
	*lppszCurPos = lsp;

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE strlinsnum (LPTSTR			*lppszCurPos,
							const UINT32	ulVal,
							UINT32			*pulRemLen)
{
	TCHAR		szV[MAX_DWORD_DISPLAY_LENGTH+2];
	UINT32	ulVLen=dword_to_argument(ulVal, szV);

	return strlinschars(lppszCurPos, szV, ulVLen, pulRemLen);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE strlinspadnum (LPTSTR			*lppszCurPos,
								const UINT32	ulVal,
								const UINT32	ulVLen,	/* may NOT be 0 */
								const TCHAR		padCh,	/* may NOT be '\0' */
								const BOOLEAN	fLeftPad,
								UINT32			*pulRemLen)
{
	EXC_TYPE	exc=EOK;
	TCHAR		szV[MAX_DWORD_DISPLAY_LENGTH+2];
	UINT32	ulVLL=dword_to_argument(ulVal, szV), vIdx=ulVLL;

	if ((0 == ulVLen) || (_T('\0') == padCh))
		return EPARAM;
	if (ulVLL > ulVLen)
		return EOVERFLOW;

	if (fLeftPad)
	{
		/* add padding */
		for (vIdx=ulVLL; vIdx < ulVLen; vIdx++)
			if ((exc=strlinsch(lppszCurPos, padCh, pulRemLen)) != EOK)
				return exc;

		if ((exc=strlinschars(lppszCurPos, szV, ulVLL, pulRemLen)) != EOK)
			return exc;
	}
	else	/* right pad */
	{
		if ((exc=strlinschars(lppszCurPos, szV, ulVLL, pulRemLen)) != EOK)
			return exc;

		/* add padding */
		for (vIdx=ulVLL; vIdx < ulVLen; vIdx++)
			if ((exc=strlinsch(lppszCurPos, padCh, pulRemLen)) != EOK)
				return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE strlinstime (LPTSTR			*lppszCurPos,
							 const UINT8	bHour,
							 const UINT8	bMinute,
							 const UINT8	bSecond,
							 UINT32			*pulRemLen)
{
	EXC_TYPE	exc=EOK;

	if ((bHour >= 24) || (bMinute >= 60) || (bSecond >= 60))
		return EOVERFLOW;

	if ((exc=strlinspadnum(lppszCurPos, bHour, 2, _T('0'), TRUE, pulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsch(lppszCurPos, _T(':'), pulRemLen)) != EOK)
		return exc;

	if ((exc=strlinspadnum(lppszCurPos, bMinute, 2, '0', TRUE, pulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsch(lppszCurPos, _T(':'), pulRemLen)) != EOK)
		return exc;

	if ((exc=strlinspadnum(lppszCurPos, bSecond, 2, _T('0'), TRUE, pulRemLen)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

/*	iTimezone is the difference (in seconds) between the GMT and LOCAL time */
EXC_TYPE strlinsGMTOffset (LPTSTR		*lppszCurPos,
									const int	iTimeZone,	/* (-1) == use internal */
									UINT32		*pulRemLen)
{
	BYTE		hrsOffset=0, minsOffset=0;
	int		itz=(((-1) == iTimeZone) ? _timezone : iTimeZone);
	EXC_TYPE	exc=GetTimeZoneComponents(itz, &hrsOffset, &minsOffset);
	if (exc != EOK)
		return exc;

	if ((exc=strlinsch(lppszCurPos, ((itz <= 0) ? _T('+') : _T('-')), pulRemLen)) != EOK)
		return exc;
	if ((exc=strlinspadnum(lppszCurPos, hrsOffset, 2, _T('0'), TRUE, pulRemLen)) != EOK)
		return exc;
	if ((exc=strlinspadnum(lppszCurPos, minsOffset, 2, _T('0'), TRUE, pulRemLen)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE strlinsvf (LPTSTR	*lppszCurPos, LPCTSTR lpszFmt, va_list ap, UINT32 *pulRemLen)
{
	LPTSTR	lsp=NULL;
	int		wLen=(-1);

	if ((NULL == lppszCurPos) || (NULL == (lsp=(*lppszCurPos))) || IsEmptyStr(lpszFmt) || (NULL == pulRemLen))
		return EPARAM;

	if ((wLen=_vsntprintf(lsp, *pulRemLen, lpszFmt, ap)) <= 0)
	{
		*lsp = _T('\0');
		return ECONTINUED;
	}

	lsp += wLen;
	*lsp = _T('\0');

	*lppszCurPos = lsp;
	*pulRemLen -= (UINT32) wLen;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE strlinsf (LPTSTR	*lppszCurPos, UINT32 *pulRemLen, LPCTSTR lpszFmt, ...)
{
	va_list	ap;
	va_start(ap, lpszFmt);
	EXC_TYPE	exc=strlinsvf(lppszCurPos, lpszFmt, ap, pulRemLen);
	va_end(ap);
	return exc;
}

/*---------------------------------------------------------------------------*/
