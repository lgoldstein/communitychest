#include <string.h>
#include <ctype.h>

#include <_types.h>
#include <util/string.h>

#ifndef WIN32
int _sntprintf (LPTSTR dst, size_t maxsize, LPCTSTR lpszFmt, ...)
{
	va_list	ap;
	va_start(ap,lpszFmt);
	int		nRes=_vsntprintf(dst, maxsize, lpszFmt, ap);
	va_end(ap);

	return nRes;
}

int _stprintf (LPTSTR dst, LPCTSTR lpszFmt, ...)
{
	va_list	ap;
	va_start(ap,lpszFmt);
	int		nRes=_vstprintf(dst, lpszFmt, ap);
	va_end(ap);

	return nRes;
}
#endif	/* of WIN32 */
/*
 *    Returns index (starting at zero) of the character in the string. If
 * character not found, it returns a value which is greater than the string's
 * length (NOTE: trying to find '\0' with this function has unforseen results).
 *
 * "strndxr" is the same, only it seeks from end of string.
 */

size_t strndx (const char *s, char c)
{
   const char  *p=s;
   size_t idx;

	if (s == NULL)
		return(0);

   for (idx=0; (*p != c) && (*p != '\0'); idx++, p++);

   /*
    *    The loop is terminated if character found or '\0' encountered - if
    * terminated because of '\0', then return a value greater than strlen.
    */

   if (*p == c)
      return(idx);
   else
      return(idx+1);
}

size_t strndxr (const char *s, char c)
{
   const char  *p;
   size_t   	idx, len=0;

	if (s == NULL)
		return(0);

   if ((len=strlen(s)) == 0)
		return(1);

   for (idx=len, p=(s+len-1); (*p != c) && (idx > 0); idx--, p--);

   if (*p == c)
      return(idx);
   else
      return(len+1);
}

/*
 *	Returns pointer to 1st character in string which equals the required one - 
 * it checks up to end of string or up to "slen" characters - whichever comes
 * first. Returns NULL if not successful
 */

char *strnchr (const char *s, const char c, size_t slen)
{
	size_t		idx;
	const char	*p=s;

	for (idx=0; (idx < slen) && (*p != c) && (*p != '\0'); idx++, p++);

	if ((idx < slen) && (c == *p))
		return((char *) p);
	else
		return(NULL);
}

char *strnrchr (const char *s, const char c, size_t slen)
{
	size_t		idx, sl=0;
	const char	*p=NULL;

	if ((0 == slen) || (NULL == s))
		return NULL;

	if ((sl=strlen(s)) > slen)
		sl = slen;
	if (0 == sl)
		return NULL;

	for (idx=0, p=(s + (sl-1)); idx < sl; idx++, p--)
	{
		if ((*p) == c)
			return ((char *) p);
	}

	return NULL;
}

/*
 * Returns number of skipped characters
 */

size_t strskipwspace (const char str[])
{
	const char *s=str;
	size_t     l=0;

	if (s == NULL)
		return(0);

	for ( ; (isspace((int) *s)) && (*s != '\0'); s++, l++);
	return(l);
}

size_t strnskipwspace (const char str[], size_t n)
{
	const char *s=str;
	size_t     l=0;

	if (s == NULL)
		return(0);

	for ( ; (isspace((int) *s)) && (*s != '\0') && (l < n); s++, l++);
	return(l);
}

const char *strskip (const char str[], char c)
{
	const char *s=str;

	if (s == NULL)
		return(0);

	while ((*s != '\0') && (*s == c)) s++;

	if (*s != '\0')
		return(s);
	else
		return(NULL);
}

const char *strnskip (const char str[], char c, size_t len)
{
	const char *s=str;
	size_t     l;

	if (s == NULL)
		return(0);

	for (l=0; (*s != '\0') && (*s == c) && (l < len) ; s++, l++);

	if ((*s != '\0') && (l < len))
		return(s);
	else
		return(NULL);
}

const char *strskipxnum (const char s[])
{
	const char *p=s;

	if (s == NULL)
		return(NULL);
	for ( ; isxdigit((int) *p) && (*p != '\0'); p++);
	return(p);
}

const char *strskipnum (const char s[])
{
	const char *p=s;

	if (s == NULL)
		return(NULL);
	for ( ; isdigit((int) *p) && (*p != '\0'); p++);
	return(p);
}

/* Note: if padding char is '\0' or string already has requested length then
 *			nothing is done.
 *
 * Returns added padding length
 */
size_t strpad (char arg[], size_t len, char padChar, BOOLEAN leftPad)
{
	size_t	aLen=0;

	if ((NULL == arg) || (0 == len) || ('\0' == padChar))
		return 0;

	if ((aLen=strlen(arg)) >= len)
		return 0;

	if (leftPad)
	{
		size_t	endOffset=(len - aLen), i=0;
		char		*shiftArg=(arg + endOffset);

		/* copy the '\0' as well */
		for (i=0; i <= aLen; i++)
			shiftArg[i] = arg[i];

		for (i=0; i < endOffset; i++)
			arg[i] = padChar;
	}
	else	// right pad
	{
		for ( ; aLen < len; aLen++)
			arg[aLen] = padChar;
	}

	arg[len] = '\0';
	return (len - aLen);
}

size_t strbuildtime (BYTE hour, BYTE minute, BYTE second, char arg[])
{
	char	*lsp=arg;

	lsp += byte_to_fixed_argument(hour, lsp, 2, '0', TRUE);
	lsp = strladdch(lsp, ':');

	lsp += byte_to_fixed_argument(minute, lsp, 2, '0', TRUE);
	lsp = strladdch(lsp, ':');

	lsp += byte_to_fixed_argument(second, lsp, 2, '0', TRUE);

	return strlen(arg);
}
