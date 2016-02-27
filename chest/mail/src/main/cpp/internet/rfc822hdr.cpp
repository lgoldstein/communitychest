#include <_types.h>

#include <util/string.h>
#include <util/tables.h>
#include <util/errors.h>

#include <internet/rfc822.h>

/*---------------------------------------------------------------------------*/

// also default constructor
CRFC822HdrData::CRFC822HdrData (LPCTSTR		lpszHdrName,
										  LPCTSTR		lpszHdrValue,
										  const UINT32	ulGrowLen)
	: m_lpszHdrValue(NULL), m_ulVLen(0), m_ulCLen(0), m_ulGLen(ulGrowLen)
{
	m_szHdrName[0] = _T('\0');

	if (!IsEmptyStr(lpszHdrName))
	{
		AddData(lpszHdrName, lpszHdrValue);
	}
}

/*---------------------------------------------------------------------------*/

CRFC822HdrData::CRFC822HdrData (const UINT32	ulGrowLen)
	: m_lpszHdrValue(NULL), m_ulVLen(0), m_ulCLen(0), m_ulGLen(ulGrowLen)
{
	m_szHdrName[0] = _T('\0');
}

/*---------------------------------------------------------------------------*/

// copy constructor
CRFC822HdrData::CRFC822HdrData (const CRFC822HdrData& hd)
	: m_lpszHdrValue(NULL), m_ulVLen(0), m_ulCLen(0), m_ulGLen(hd.m_ulGLen)
{
	m_szHdrName[0] = _T('\0');

	UpdateData(hd);
}

/*---------------------------------------------------------------------------*/

void CRFC822HdrData::Reset ()
{
	m_szHdrName[0] = _T('\0');
	if (m_lpszHdrValue != NULL)
		*m_lpszHdrValue = _T('\0');
	m_ulCLen = 0;
}

/*---------------------------------------------------------------------------*/

void CRFC822HdrData::ReleaseValue ()
{
	if (m_lpszHdrValue != NULL)
	{
		delete [] m_lpszHdrValue;
		m_lpszHdrValue = NULL;
	}
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRFC822HdrData::CheckHeaderName (LPCTSTR lpszHdrName)
{
	if (IsEmptyStr(lpszHdrName))
		return EPARAM;

	// check if changed hdr name
	if (_tcsicmp(lpszHdrName, m_szHdrName) != 0)
	{
		UINT32	ulHNLen=strlen(lpszHdrName);

		if (ulHNLen > MAX_RFC822_HDR_NAME_LEN)
			return EOVERFLOW;

		Reset();
		strcpy(m_szHdrName, lpszHdrName);
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

// if header name mismatch, then resets data
EXC_TYPE CRFC822HdrData::AddData (LPCTSTR lpszHdrName, LPCTSTR lpszHdrValue, const UINT32 ulVLen)
{
	EXC_TYPE	exc=CheckHeaderName(lpszHdrName);
	if (exc != EOK)
		return exc;

	UINT32	ulRLen=(m_ulCLen + ulVLen);	// calculate required length
	LPTSTR	lsp=((m_lpszHdrValue != NULL) ? (m_lpszHdrValue + m_ulCLen) : NULL);

	// check if need to reallocate data buffer
	if (ulRLen > m_ulVLen)
	{
		UINT32	ulALen=(ulRLen + m_ulGLen);
		LPTSTR	lpszV=new TCHAR[ulALen + 2];
		if (NULL == lpszV)
			return EMEM;

		lsp = lpszV;
		*lsp = _T('\0');

		// copy previous value (if any)
		if (m_lpszHdrValue != NULL)
		{
			lsp = strlcpy(lsp, m_lpszHdrValue);
			delete [] m_lpszHdrValue;
		}

		m_lpszHdrValue = lpszV;
		m_ulVLen = ulALen;
	}

	// append data to end of previous value (if any)
	if (ulVLen > 0)
	{
		lsp = strlncat(lsp, lpszHdrValue, ulVLen);
		m_ulCLen += ulVLen;
		*lsp = _T('\0');
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRFC822HdrData::ReplaceData (LPCTSTR lpszHdrName, LPTSTR lpszHdrValue)
{
	EXC_TYPE	exc=CheckHeaderName(lpszHdrName);
	if (exc != EOK)
		return exc;

	if (NULL == lpszHdrValue)
		return ENUCBADBUF;

	ReleaseValue();

	m_lpszHdrValue = lpszHdrValue;
	m_ulCLen = _tcslen(lpszHdrValue);
	m_ulVLen = m_ulCLen;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRFC822HdrData::SetData (LPCTSTR lpszHdrName, LPCTSTR lpszHdrValue, const UINT32 ulVLen)
{
	Reset();
	return AddData(lpszHdrName, lpszHdrValue, ulVLen);
}

/*---------------------------------------------------------------------------*/

// class for handling headers which may span several lines
EXC_TYPE CRFC822HdrParser::ProcessHdr (LPCTSTR lpszHdr)
{
	EXC_TYPE	exc=EOK;

	if (NULL == lpszHdr)
		return EPARAM;

	if (IsSafeSpace(*lpszHdr) || (_T('\0') == *lpszHdr))
	{
		// make sure continuation is AFTER some known hdr name
		if (_T('\0') == m_szHdrName[0])
			return EBADHEADER;

		m_fIsContHdr = TRUE;
		for (m_lpszHdrValue=lpszHdr;
			  isspace(*m_lpszHdrValue) && (*m_lpszHdrValue != _T('\0'));
			  m_lpszHdrValue++);
	}
	else
	{
		LPCTSTR	lpszHdrName=NULL;
		UINT32	ulNameLen=0;
		EXC_TYPE	exc=rfc822ExtractHdrPtrs(lpszHdr, &lpszHdrName, &ulNameLen, &m_lpszHdrValue);
		if (exc != EOK)
		{
			Reset();
			return exc;
		}

		// header name must contain at least one letter + separator
		if (ulNameLen <= 1)
			return EIOSOFT;

		if (ulNameLen > MAX_RFC822_HDR_NAME_LEN)
			return EOVERFLOW;

		_tcsncpy(m_szHdrName, lpszHdrName, ulNameLen);
		m_szHdrName[ulNameLen] = _T('\0');
		m_fIsContHdr = FALSE;
	}

	return exc;
}

/*---------------------------------------------------------------------------*/
