/*--------------------------------------------------------------------------*/

#include <util/string.h>
#include <util/errors.h>

#include <futils/general.h>

/*--------------------------------------------------------------------------*/

// Note: returns EOK for NULL/empty line
EXC_TYPE EnumCSValuesLine (LPCTSTR lpszLine, CSV_ENUM_CFN lpfnEcfn, LPVOID pArg)
{
	LPCTSTR	lpszCurPos=lpszLine;

	if (NULL == lpfnEcfn)
		return EPARAM;
	if (IsEmptyStr(lpszLine))
		return EOK;

	for (UINT32	ulColNdx=0; *lpszCurPos != _T('\0'); ulColNdx++)
	{
		LPCTSTR	lpszNextPos=lpszCurPos;
		for (; (*lpszNextPos != _T(',')) && (*lpszNextPos != _T('\0')); lpszNextPos++)
		{
			// handle quoted values
			if (_T('\"') == *lpszNextPos)
			{
				const TCHAR	chDelim=*lpszNextPos;

				for (lpszNextPos++, lpszCurPos=lpszNextPos; *lpszNextPos != _T('\0'); lpszNextPos++)
					if (chDelim == *lpszNextPos)
					{
						// if double delimiter then this is the delimiter itself
						if (*(lpszNextPos + 1) != chDelim)
							break;

						lpszNextPos++;	// skip 2nd instance of delimiter
					}

				// make sure we found the matching delimiter
				if (*lpszNextPos != chDelim)
					return ELITERAL;

				// assume exhausted field value
				break;
			}
		}

		BOOLEAN	fContEnum=TRUE;
		EXC_TYPE	exc=(*lpfnEcfn)(lpszLine, ulColNdx, lpszCurPos, (lpszNextPos - lpszCurPos), pArg, fContEnum);
		if (exc != EOK)
			return exc;

		// check if user requested to stop or reached end of line
		if ((!fContEnum) || (_T('\0') == *lpszNextPos))
			break;

		lpszCurPos = (lpszNextPos + 1);	// skip ','
	}

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

class PSARGS {
	public:
		CCSVParser&		m_sp;
		const UINT32	m_ulLineNum;

		PSARGS (CCSVParser& sp, const UINT32 ulLineNum)
			: m_sp(sp), m_ulLineNum(ulLineNum)
		{
		}

		~PSARGS ()
		{
		}
};

EXC_TYPE CCSVParser::HandleCSVValue (LPCTSTR			lpszLine,	// original line
												 const UINT32	ulColNdx,	// starts at zero
												 LPCTSTR			lpszVal,
												 const UINT32	ulVLen,
												 LPVOID			pArg,
												 BOOLEAN&		fContEnum)
{
	if (NULL == pArg)
		return ECONTEXT;
	else
		return ((PSARGS *) pArg)->m_sp.HandleValue(((PSARGS *) pArg)->m_ulLineNum, lpszLine, ulColNdx, lpszVal, ulVLen, fContEnum);
}

/*--------------------------------------------------------------------------*/

// uses the specified buffer for parsing
EXC_TYPE CCSVParser::ParseStream (LPTSTR lpszLine, const UINT32 ulMaxLineLen)
{
	if ((NULL == lpszLine) || (ulMaxLineLen <= 1))
		return EPARAM;

	BOOLEAN	fContEnum=TRUE;
	for (UINT32	ulLineNum=0; fContEnum; ulLineNum++)
	{
		UINT32	ulReadLen=0;
		EXC_TYPE	exc=ReadLine(ulLineNum, lpszLine, ulMaxLineLen, ulReadLen);
		if (EEOF == exc)	// check if EOF reached
			break;

		if (exc != EOK)
			return exc;

		if (0 == ulReadLen)
			continue;
		lpszLine[ulReadLen] = _T('\0');	// just making sure

		BOOLEAN	fSkipLine=FALSE;
		if ((exc=HandleLineStart(ulLineNum, fSkipLine, fContEnum)) != EOK)
			return exc;
		if (fSkipLine)
			continue;
		if (!fContEnum)
			break;

		PSARGS	psa(*this, ulLineNum);
		if ((exc=::EnumCSValuesLine(lpszLine, HandleCSVValue, &psa)) != EOK)
		{
			if (EOK == HandleParseError(ulLineNum, exc))
				continue;

			return exc;
		}

		if ((exc=HandleLineEnd(ulLineNum, fContEnum)) != EOK)
			return exc;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

// automatically allocates/frees a buffer of specified length and uses it
EXC_TYPE CCSVParser::ParseStream (const UINT32 ulMaxLineLen)
{
	if (ulMaxLineLen <= 1)
		return EPARAM;

	LPTSTR			lpszLine=new TCHAR[ulMaxLineLen + sizeof(NATIVE_WORD)];
	CStrBufGuard	llg(lpszLine);
	if (NULL == lpszLine)
		return EMEM;

	return ParseStream(lpszLine, ulMaxLineLen);
}

//////////////////////////////////////////////////////////////////////////////

EXC_TYPE CCSVStreamParser::ReadLine (const UINT32	ulLineNum,	// starts at zero
												 LPTSTR			lpszLine,
												 const UINT32	ulMaxLen,
												 UINT32&			ulReadLen)
{
	if (NULL == m_lpfnRcfn)
		return ECONTEXT;
	if ((NULL == lpszLine) || (ulMaxLen <= 1))
		return ELIMIT;

	for (ulReadLen = 0; ulReadLen < ulMaxLen; ulReadLen++)
	{
		TCHAR&	tch=lpszLine[ulReadLen];
		UINT32	ulRLen=(*m_lpfnRcfn)(m_pArg, (UINT8 *) &tch, (sizeof tch));

		// if not read a character, then assume EEOF if nothing else read
		if (ulRLen != (sizeof tch))
		{
			if (0 == ulReadLen)
				return EEOF;
			else	// if something read, then assume EOF is AFTER the current line
				return EOK;
		}

		// if found CR, then MUST have LF following it
		if (_T('\r') == tch)
		{
			if ((ulRLen=(*m_lpfnRcfn)(m_pArg, (UINT8 *) &tch, (sizeof tch))) != (sizeof tch))
				return EIOHARD;
			if (tch != _T('\n'))
				return EUDFFORMAT;

			tch = _T('\0');	// mark end of line
			return EOK;
		}

		// if found LF, then mark end of line
		if (_T('\n') == tch)
		{
			tch = _T('\0');
			return EOK;
		}
	}

	// this point is reached if filled entire line, but not found a CR/LF (or EOF)
	return EOVERFLOW;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CCSVStreamParser::SetStream (IOREADCALLBACK lpfnRcfn, LPVOID pArg)
{
	if (NULL == (m_lpfnRcfn=lpfnRcfn))
	{
		m_pArg = NULL;
		return EPARAM;
	}

	m_pArg = pArg;
	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

EXC_TYPE CCSVFileParser::ReadLine (const UINT32	ulLineNum,	// starts at zero
											  LPTSTR			lpszLine,
											  const UINT32	ulMaxLen,
											  UINT32&		ulReadLen)
{
	if (NULL == m_fp)
		return ECONTEXT;

	if ((NULL == lpszLine) || (ulMaxLen <= 1))
		return ELIMIT;

	if (NULL == ::_fgetts(lpszLine, ulMaxLen, m_fp))
	{
		if (feof(m_fp))
			return EEOF;
		else
			return EIOHARD;
	}

	if ((ulReadLen=::_tcslen(lpszLine)) > 0)
	{
		// make sure we have the terminating LF
		if (lpszLine[ulReadLen-1] != _T('\n'))
			return EUDFFORMAT;
		ulReadLen--;

		if ((ulReadLen > 0) && (_T('\r') == lpszLine[ulReadLen-1]))
			ulReadLen--;
	}

	lpszLine[ulReadLen] = _T('\0');
	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CCSVFileParser::ParseFile (FILE *fp, LPTSTR lpszLine, const UINT32 ulMaxLineLen)
{
	if ((NULL == fp) || (NULL == lpszLine) || (ulMaxLineLen <= 1))
		return EPARAM;
	if (m_fp != NULL)	// should not happend
		return ESLOT;

	m_fp = fp;
	EXC_TYPE	exc=ParseStream(lpszLine, ulMaxLineLen);
	m_fp = NULL;

	return exc;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CCSVFileParser::ParseFile (FILE *fp, const UINT32 ulMaxLineLen)
{
	if ((NULL == fp) || (ulMaxLineLen <= 1))
		return EPARAM;

	LPTSTR			lpszLine=new TCHAR[ulMaxLineLen + sizeof(NATIVE_WORD)];
	CStrBufGuard	llg(lpszLine);
	if (NULL == lpszLine)
		return EMEM;

	return ParseFile(fp, lpszLine, ulMaxLineLen);
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CCSVFileParser::ParseFile (LPCTSTR lpszFilePath, LPTSTR lpszLine, const UINT32 ulMaxLineLen)
{
	if (IsEmptyStr(lpszFilePath) || (NULL == lpszLine) || (ulMaxLineLen <= 1))
		return EPARAM;

	FILE	*fp=::_tfopen(lpszFilePath, _T("r"));
	if (NULL == fp)
		return EFNEXIST;
	CFilePtrGuard	fpg(fp);

	return ParseFile(fp, lpszLine, ulMaxLineLen);
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CCSVFileParser::ParseFile (LPCTSTR lpszFilePath, const UINT32 ulMaxLineLen)
{
	if (ulMaxLineLen <= 1)
		return EPARAM;

	LPTSTR			lpszLine=new TCHAR[ulMaxLineLen + sizeof(NATIVE_WORD)];
	CStrBufGuard	llg(lpszLine);
	if (NULL == lpszLine)
		return EMEM;

	return ParseFile(lpszFilePath, lpszLine, ulMaxLineLen);
}

//////////////////////////////////////////////////////////////////////////////
