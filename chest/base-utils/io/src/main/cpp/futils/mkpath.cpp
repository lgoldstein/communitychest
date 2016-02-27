#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <limits.h>

/*---------------------------------------------------------------------------*/

#include <_types.h>

#ifdef WIN32
#include <direct.h>
#include <io.h>
#endif

/*---------------------------------------------------------------------------*/

#include <util/string.h>
#include <util/errors.h>
#include <util/tables.h>

#include <futils/general.h>

/*---------------------------------------------------------------------------*/

/* creates the specified directoy - including missing components in between */
EXC_TYPE mkpath (LPCTSTR dirp)
{
	EXC_TYPE	exc=EOK;
	TCHAR		dpath[MAX_DNLEN+2]=_T(""), *lp=NULL, *tsp=NULL;

	if (NULL == dirp)
		return EPARAM;
	if ('\0' == *dirp)
		return EEMPTYENTRY;
	if (strlen(dirp) >= MAX_DNLEN)
		return EMEM;

	lp = strlcpy(dpath, dirp);
	tsp = lp;

	/* make sure no terminating path char exists */
	if (FULL_PATH_CHAR == *(tsp-1))
	{
		tsp--;
		lp--;
		*tsp = '\0';
	}

	/*		Find 1st component that does exist - after this loop "tsp" points to
	 * remaining path to be created.
	 */

	while (_taccess(dpath, 0) == (-1))
	{
		/* restore previous char */
		if (tsp != lp)
			*tsp = FULL_PATH_CHAR;

		for (tsp--; (tsp != dpath); tsp--)
			if (FULL_PATH_CHAR == *tsp)
				break;

		/* check if exhausted all options */
		if (tsp == dpath)
			break;

		*tsp = '\0';
	}

	while ((tsp != lp) && (tsp != NULL))
	{
		*tsp = FULL_PATH_CHAR;

		/* find end of next component */
		if ((tsp=strchr((tsp+1), FULL_PATH_CHAR)) != NULL)
			*tsp = '\0';	/* create null-terminated string */

		if ((exc=mkdir(dpath)) != EOK)
			return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

#define GetReplacementChar(tch) hex_digits_chars[(tch) & 0x0000000F]

/*---------------------------------------------------------------------------*/

/*
 *		Replaces "dangerous" characters that cannot appear in a path component
 * with ones that are allowed (Note: '/' characters are translated to path separators).
 */
void AdjustFileComponentCharacters (/* In/Out */ LPTSTR lpszSubPathStr /* may be NULL/empty */)
{
	if (IsEmptyStr(lpszSubPathStr))
		return;

	// replace any special characters
	LPTSTR msp=lpszSubPathStr;
	for ( ; *msp != _T('\0'); msp++)
	{
		static const TCHAR szMISpecial[]=_T("@`~!@#$%^&*()+={}[];:\'\"<>,?|");

		const TCHAR	tch=*msp;

		if ((tch >= _T('0')) && (tch <= _T('9')))
			continue;
		if ((tch >= _T('a')) && (tch <= _T('z')))
			continue;
		if ((tch >= _T('A')) && (tch <= _T('Z')))
			continue;

		// check if "bad" char and replace it
		if (_tcschr(szMISpecial, tch) != NULL)
		{
			*msp = GetReplacementChar(tch);
			continue;
		}

		if (_T('/') == tch)
			*msp = FULL_PATH_CHAR;

		// adjust 8-bit characters to 7-bit
		const UINT16	wch=(((UINT16) tch) & 0x00ff);
		if ((wch < (UINT16) 0x0020) || (wch > (UINT16) 0x007F))
		{
			*msp = base64_encode_tbl[wch & 0x003f];

			if ((_T('/') == *msp) || (_T('+') == *msp))
				*msp = GetReplacementChar(*msp);
		}

		// remove preceding/successive dots (not allowed in Win2K)
		if (_T('.') == *msp)
		{
			// if followed or preceded by path separator then need to replace it
			// (Note: if prefix/suffix of entire path, it will be taken care of on exit from this loop)
			if ((FULL_PATH_CHAR == *(msp+1)) || ((msp > lpszSubPathStr) && (FULL_PATH_CHAR == *(msp-1))))
				*msp = GetReplacementChar(*msp);
		}
	}

	// replace any '.' at end of path (Note: at this point "msp" points to terminating '\0')
	for (msp--; (_T('.') == *msp) && (msp > lpszSubPathStr); msp--)
		*msp = GetReplacementChar(*msp);

	// replace any prefix '.'
	for (msp=lpszSubPathStr; _T('.') == *msp; msp++)
		*msp = GetReplacementChar(*msp);
}

/*---------------------------------------------------------------------------*/
