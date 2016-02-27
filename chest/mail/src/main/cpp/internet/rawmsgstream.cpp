#include <internet/rawmsgparser.h>

//////////////////////////////////////////////////////////////////////////////

// Note: removes any previous file object
EXC_TYPE CRawRFC2046MsgFileInputStream::SetFile (FILE *fp, const BOOLEAN fAutoClose)
{
	Close();

	if (NULL == (m_fp=fp))
		return ESLOT;

	m_fAutoClose = fAutoClose;
	return EOK;
}

/*---------------------------------------------------------------------------*/

// no effect if already closed
void CRawRFC2046MsgFileInputStream::Close ()
{
	if (m_fAutoClose && (m_fp != NULL))
		::fclose(m_fp);

	m_fp = NULL;
	m_fAutoClose = FALSE;
}

/*---------------------------------------------------------------------------*/

// returns current stream position (starting at zero)
EXC_TYPE CRawRFC2046MsgFileInputStream::GetCurReadOffset (UINT32& ulOffset) const
{
	if (NULL == m_fp)
	{
		ulOffset = (UINT32) (-1);
		return ESLOT;
	}

	if ((-1) == (LONG) (ulOffset=(UINT32) ::ftell(m_fp)))
		return EIOSOFT;

	return EOK;
}

/*---------------------------------------------------------------------------*/

// Note: returns ECONTINUED if entire line buffer filled (including terminating '\0') but no end-of-line found
EXC_TYPE CRawRFC2046MsgFileInputStream::ReadLine (LPTSTR lpszLine, const UINT32 ulMaxLen, UINT32& ulCurLen)
{
	ulCurLen = 0;

	// length must be at least 2 ('\n' and '\0')
	if ((NULL == lpszLine) || (ulMaxLen < 2))
		return EBADBUFF;

	if (NULL == m_fp)
		return ESLOT;

	LPCTSTR	lpszRead=::_fgetts(lpszLine, (int) ulMaxLen, m_fp);
	if (IsEmptyStr(lpszRead))
		return EEOF;

	ulCurLen = ::_tcslen(lpszRead);
	if (_T('\n') != lpszRead[ulCurLen - 1])
		return ECONTINUED;

	// remove the terminating '\n'
	ulCurLen--;
	lpszLine[ulCurLen] = _T('\0');

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////
