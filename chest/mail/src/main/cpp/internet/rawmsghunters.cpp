#include <internet/rawmsgparser.h>

//////////////////////////////////////////////////////////////////////////////

EXC_TYPE CRFC822MsgEOM::ProcessBuf (LPCTSTR lpszBuf, const UINT32 ulBufLen)
{
	UINT32	aLen=min(ulBufLen, RFC822MSG_EOM_PATLEN);
	if (RFC822MSG_EOM_PATLEN == aLen)
	{
		// if enough data to fill entire pattern buffer then do so
		LPCTSTR	tsp=(lpszBuf + (ulBufLen - RFC822MSG_EOM_PATLEN));
		_tcsncpy(m_szEOMPattern, tsp, RFC822MSG_EOM_PATLEN);
		m_szEOMPattern[RFC822MSG_EOM_PATLEN] = _T('\0');
	}
	else	/* have to concatenate to current pattern */
	{
		UINT32 pLen=_tcslen(m_szEOMPattern), sLen=(RFC822MSG_EOM_PATLEN-pLen);

		// if cannot copy entire buffer then "shift" pattern to make room
		if (aLen > sLen)
		{
			int dLen=(aLen - sLen); /* how much is left out */

			_tcscpy(m_szEOMPattern, &m_szEOMPattern[dLen]);
			pLen -= dLen;	// total length decreased by shift length
		}

		// concatenate new buffer
		_tcscpy(&m_szEOMPattern[pLen], lpszBuf);
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

// returns TRUE if current pattern is an end-of-message
BOOLEAN CRFC822MsgEOM::IsMsgEOM () const
{
	/* go backwards and look for the pattern */
	LPCTSTR	lpszPattern=m_szEOMPattern, tsp=strlast(lpszPattern);
	if (tsp == lpszPattern)
		return FALSE;
	tsp--;

	/* pattern must end in LF */
	if (_T('\n') != *tsp)
		return FALSE;
	tsp--;
	if (lpszPattern > tsp)
		return FALSE;

	/* if pattern contains CR, skip it */
	if (_T('\r') == *tsp)
	{
		tsp--;
		if (lpszPattern > tsp)
			return FALSE;
	}

	/* pattern must contain '.' */
	if (*tsp != _T('.'))
		return FALSE;
	tsp--;
	if (lpszPattern > tsp)
		return FALSE;

	/* before '.' there must be LF !!! (we are lenient and don't care if there is a CR) */
	if (*tsp != _T('\n'))
		return FALSE;

	return TRUE;
}

//////////////////////////////////////////////////////////////////////////////

/*		Ignores non-"Content-Type:" headers or non-continuation data - i.e.,
 * assumes the boundary property (and for that matter ANY property) resides
 * on a separate line of data.
 */
EXC_TYPE CMIMEBoundaryHunter::ProcessHdr (LPCTSTR lpszHdrName, const BOOLEAN fIsContHdr, LPCTSTR lpszHdrValue)
{
	if (IsEmptyStr(lpszHdrName) || (!fIsContHdr) || IsEmptyStr(lpszHdrValue) || (::_tcsicmp(lpszHdrName, pszStdContentTypeHdr) != 0))
		return EOK;

	// skip to start of attribute name
	LPCTSTR	lpszCurPos=lpszHdrValue;
	for (; ((*lpszCurPos == _T('\t')) || (*lpszCurPos == _T(' '))) && (*lpszCurPos != _T('\0')); lpszCurPos++);

	// skip to end of attribute name
	LPCTSTR	lpszKWStart=lpszCurPos;
	for (; (*lpszCurPos != _T('\t')) && (*lpszCurPos != _T(' ')) && (*lpszCurPos != RFC822_KEYWORD_VALUE_DELIM) && (*lpszCurPos != _T('\0')); lpszCurPos++);

	// skip to start of attribute value
	LPCTSTR	lpszKWEnd=lpszCurPos;
	for (; (*lpszCurPos != RFC822_KEYWORD_VALUE_DELIM) && (*lpszCurPos != _T('\0')); lpszCurPos++);
	// make sure stopped because found value separator
	if (*lpszCurPos != RFC822_KEYWORD_VALUE_DELIM)
		return EOK;

	// check if this is the boundary keyword
	const UINT32	kwLen=(lpszKWEnd - lpszKWStart), mmLen=_tcslen(pszMIMEBoundaryKeyword);
	if ((kwLen != mmLen) || (_tcsnicmp(lpszKWStart, pszMIMEBoundaryKeyword, mmLen) != 0))
		return S_OK;
	// deny re-initialization of the data
	if (HaveBoundary())
		return EEXIST;

	// find start of attribute value
	for (lpszCurPos++; (*lpszCurPos != _T('\"')) && (*lpszCurPos != _T('\0')); lpszCurPos++);
	// make sure found start of value
	if (*lpszCurPos != _T('\"'))
		return EUDFFORMAT;
	else
		lpszCurPos++;	// skip delimiter

	// find end of attribute value
	LPCTSTR	lpszValStart=lpszCurPos;
	for (; (*lpszCurPos != _T('\"')) && (*lpszCurPos != _T('\0')); lpszCurPos++);
	// make sure found end of attribute value
	if (*lpszCurPos != _T('\"'))
		return EUDFFORMAT;

	const UINT32	ulVLen=(lpszCurPos - lpszValStart);
	if ((0 == ulVLen) || (ulVLen > MAX_RFC822_MIME_BOUNDARY_LEN))
		return EOVERFLOW;

	_tcsncpy(m_szMIMEBoundary, lpszValStart, ulVLen);
	m_szMIMEBoundary[ulVLen] = _T('\0');

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////
