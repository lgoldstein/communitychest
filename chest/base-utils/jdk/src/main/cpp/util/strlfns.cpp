#include <_types.h>
#include <util/string.h>
#include <util/errors.h>

/*
 *    Copies a string to another, and returns the pointer to end of result
 * string (i.e. '\0' char).
 *
 * NOTE: does not check that destination is indeed '\0'...
 */

char *strlcpy (char *lps, const char *s)
{
   const char  *src;
	char			*dst;

   for (src=s,dst=lps ; (*src != '\0') ; src++, dst++)
      *dst = *src;
   *dst = '\0';

   return(dst);
}

/*
 *    Copies a string to another, and returns the pointer to end of result
 * string (i.e. '\0' char). Stops when either whole string has been copied, or
 * "max" characters reached - whichever comes first
 *
 * NOTE: does not check that destination is indeed '\0'...
 */

char *strlncpy (char *lps, const char *s, size_t maxlen)
{
   const char	*src=(char *) s;
	char			*dst=lps;
   size_t   	idx;

   for (idx = 0;(*src != '\0') && (idx < maxlen);src++,dst++,idx++)
      *dst = *src;

   /*
    * At this point dst points to last character iff.
    */

	if (idx < maxlen)
		*dst = '\0';

   return(dst);
}

/*
 * Adds a character to the string and returns pointer to position after it
 * (NOTE: "lps" is ASSUMED to point to last char (i.e. '\0'))
 */

char *strladdch (char *lps, char c)
{
   *lps = c;
   lps++;
   *lps = '\0';
   return(lps);
}

#ifdef __cplusplus
EXC_TYPE strupdatebuf (LPCTSTR lpszSrc, const UINT32 ulSLen, LPTSTR& lpszDst)
{
	lpszDst = NULL;

	if (ulSLen > 0)
	{
		if (NULL == (lpszDst=new TCHAR[ulSLen+2]))
			return EMEM;

		_tcsncpy(lpszDst, lpszSrc, ulSLen);
		lpszDst[ulSLen] = _T('\0');
	}

	return EOK;
}
#endif	/* __cplusplus */
