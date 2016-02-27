#include <util/string.h>
#include <util/errors.h>

/*---------------------------------------------------------------------------*/

char *stristr (const char *s, const char *ss)
{
#ifdef __cplusplus
	CBoyerMooreSearchString	bmss(ss);
	return ((char *) bmss.FindSubstringIn(s));
#else
	const char	*sp=s;
	UINT32		ssLen=GetSafeStrlen(ss), sLen=GetSafeStrlen(s), ulRemLen=sLen;

	if (IsEmptyStr(ss))
		return ((char *) s);
	if (NULL == s)
		return NULL;

	for (sp=s; (ulRemLen >= ssLen) && (*sp != '\0'); sp++, ulRemLen--)
		if (0 == strnicmp(sp, ss, ssLen))
			return ((char *) sp);

	return NULL;
#endif	/* __cplusplus */
}

//////////////////////////////////////////////////////////////////////////////

BOOLEAN ISearchString::IsSubStringInBuffer (LPCTSTR lpszSearchBuf, const UINT32 ulSearchLen) const
{
	LPCTSTR	lpszSubStr=FindInSearchBuffer(lpszSearchBuf, ulSearchLen);
	return (lpszSubStr != NULL);
}

//////////////////////////////////////////////////////////////////////////////

/* Based on the code by	Mark Crispin
 *		Networks and Distributed Computing
 *		Computing & Communications
 *		University of Washington
 *		Administration Building, AG-44
 *		Seattle, WA  98195
 *		Internet: MRC@CAC.Washington.EDU
 *
 * Date:	5 July 1988
 * Last Edited:	13 January 1999
 *
 * Sponsorship:	The original version of this work was developed in the
 *		Symbolic Systems Resources Group of the Knowledge Systems
 *		Laboratory at Stanford University in 1987-88, and was funded
 *		by the Biomedical Research Technology Program of the National
 *		Institutes of Health under grant number RR-00785.
 *
 * Original version Copyright 1988 by The Leland Stanford Junior University
 * Copyright 1999 by the University of Washington
 *
 *  Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose and without fee is hereby granted, provided
 * that the above copyright notices appear in all copies and that both the
 * above copyright notices and this permission notice appear in supporting
 * documentation, and that the name of the University of Washington or The
 * Leland Stanford Junior University not be used in advertising or publicity
 * pertaining to distribution of the software without specific, written prior
 * permission.  This software is made available "as is", and
 * THE UNIVERSITY OF WASHINGTON AND THE LELAND STANFORD JUNIOR UNIVERSITY
 * DISCLAIM ALL WARRANTIES, EXPRESS OR IMPLIED, WITH REGARD TO THIS SOFTWARE,
 * INCLUDING WITHOUT LIMITATION ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, AND IN NO EVENT SHALL THE UNIVERSITY OF
 * WASHINGTON OR THE LELAND STANFORD JUNIOR UNIVERSITY BE LIABLE FOR ANY
 * SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER
 * RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF
 * CONTRACT, TORT (INCLUDING NEGLIGENCE) OR STRICT LIABILITY, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

const UINT8	CBoyerMooreSearchString::m_AlphaTab[UINT8_MAX+1]={
	255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
	255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
	255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
	255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
	255,223,223,223,223,223,223,223,223,223,223,223,223,223,223,223,
	223,223,223,223,223,223,223,223,223,223,223,255,255,255,255,255,
	255,223,223,223,223,223,223,223,223,223,223,223,223,223,223,223,
	223,223,223,223,223,223,223,223,223,223,223,255,255,255,255,255,
	255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
	255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
	255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
	255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
	255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
	255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
	255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
	255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255
};

/*---------------------------------------------------------------------------*/

EXC_TYPE CBoyerMooreSearchString::SetPattern (LPCTSTR lpszPattern, const UINT32 ulPatLen, const BOOLEAN fCaseSensitive)
{
	::memset(m_PatternMask, 0, (sizeof m_PatternMask));
	::strreleasebuf(m_lpszPattern);
	m_fCaseSensitive = FALSE;
	m_ulPatLen = 0;

	if (0 == ulPatLen)
		return EOK;

	EXC_TYPE	exc=::strupdatebuf(lpszPattern, ulPatLen, m_lpszPattern);
	if (exc != EOK)
		return exc;

	m_ulPatLen = ulPatLen;
	m_fCaseSensitive = fCaseSensitive;

	for (UINT32	i=0; i < m_ulPatLen; i++)
	{
		const UINT8	c=(UINT8) m_lpszPattern[i];
		if (0 == m_PatternMask[c])
		{
			/* mark single character if non-alphabetic */
			if ((m_AlphaTab[c] & 0x20) != 0)
				m_PatternMask[c] = 1;
			else	/* else mark both cases */
			{
				m_PatternMask[(UINT8) (c & 0xdf)] = 1;
				m_PatternMask[(UINT8) (c | 0x20)] = 1;
			}
		}
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

// returns occurance the current pattern is a substring of the search buffer (or NULL)
LPCTSTR CBoyerMooreSearchString::FindInSearchBuffer (LPCTSTR lpszSearchBuf, const UINT32 ulSearchLen) const
{
	// if pattern greater then search, then obviously it cannot be a substring of it...
	if (m_ulPatLen > ulSearchLen)
		return NULL;

	// empty pattern always succeeds
	if (0 == m_ulPatLen)
		return lpszSearchBuf;

	// for case sensitive use "normal" search (for now)
	if (m_fCaseSensitive)
	{
		LPCTSTR	lpszPos=_tcsstr(lpszSearchBuf, m_lpszPattern);
		if (NULL == lpszPos)
			return NULL;

		// make sure returned position does not exceed search buffer length
		const UINT32	ulPosOffset=(lpszPos - lpszSearchBuf);
		if (((ulPosOffset + m_ulPatLen) - 1) <= ulSearchLen)
			return lpszPos;
	}
	else
	{
		UINT8	c=0;
		for (UINT32	patc=m_ulPatLen, i=--patc, j=0, k=0; i < ulSearchLen; i += (m_PatternMask[c] ? 1 : (j + 1)))
		{
			for (j = patc,c =(UINT8) lpszSearchBuf[k = i]; !((c ^ (UINT8) m_lpszPattern[j]) & m_AlphaTab[c]); j--, c=(UINT8) lpszSearchBuf[--k])
				if (0 == j)
					return (lpszSearchBuf + (i - m_ulPatLen) + 1);	/* found a match! */
		}
	}

	return NULL;
}

//////////////////////////////////////////////////////////////////////////////
