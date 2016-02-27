#include <futils/general.h>
#include <util/memory.h>

//////////////////////////////////////////////////////////////////////////////

IBinaryDataReader::IBinaryDataReader ()
	: m_fLclBigEndian(is_lo_endian() ? false : true)
{
}

/*-----------------------------------------------------------------*/

// returns error if not read exactly specified len
EXC_TYPE IBinaryDataReader::ReadData (BYTE bData[], const DWORD dwMaxLen)
{
	DWORD		dwReadLen=0;
	EXC_TYPE	exc=ReadData(bData, dwMaxLen, dwReadLen);
	if (exc != EOK)
		return exc;

	if (dwReadLen != dwMaxLen)
		return EIOSOFT;

	return EOK;
}

/*-----------------------------------------------------------------*/

EXC_TYPE IBinaryDataReader::ReadDWORD (DWORD& dwVal, const bool isBigEndian)
{
	dwVal = 0;

	// if same endian-ness we can simply read the data
	if (m_fLclBigEndian == isBigEndian)
		return ReadData((BYTE *) &dwVal, (sizeof dwVal));

	// at this point we know that the value endian-ness differs from local host
	BYTE		bData[(sizeof dwVal)]={ 0 };
	EXC_TYPE	exc=ReadData(bData, (sizeof bData));
	if (exc != EOK)
		return exc;

	if (isBigEndian)
	{
		// the read value is big endian, but the local machine is little endian
		for (unsigned i=0; i < (sizeof bData); i++)
		{
			const DWORD	dwValMask=((DWORD) bData[i]) & 0x000000FF;
			dwVal = ((dwVal << INT8_BITS_NUM) & 0xFFFFFF00) | dwValMask;
		}
	}
	else	// the read value is little endian, but the local machine is big endian
	{
		for (int i=(sizeof bData)-1; i >= 0; i--)
		{
			const DWORD	dwValMask=((DWORD) bData[i]) & 0x000000FF;
			dwVal = ((dwVal << INT8_BITS_NUM) & 0xFFFFFF00) | dwValMask;
		}
	}

	return EOK;
}

/*-----------------------------------------------------------------*/

EXC_TYPE IBinaryDataReader::ReadWORD (WORD& wVal, const bool isBigEndian)
{
	wVal = 0;

	// if same endian-ness we can simply read the data
	if (m_fLclBigEndian == isBigEndian)
		return ReadData((BYTE *) &wVal, (sizeof wVal));

	BYTE		bData[(sizeof wVal)]={ 0 };
	EXC_TYPE	exc=ReadData(bData, (sizeof bData));
	if (exc != EOK)
		return exc;

	if (isBigEndian)
	{
		// the read value is big endian, but the local machine is little endian
		for (unsigned i=0; i < (sizeof bData); i++)
		{
			const WORD	wValMask=((WORD) bData[i]) & 0x00FF;
			wVal = ((wVal << INT8_BITS_NUM) & 0xFF00) | wValMask;
		}
	}
	else	// the read value is little endian, but the local machine is big endian
	{
		for (int i=(sizeof bData)-1; i >= 0; i--)
		{
			const WORD	wValMask=((WORD) bData[i]) & 0x00FF;
			wVal = ((wVal << INT8_BITS_NUM) & 0xFF00) | wValMask;
		}
	}

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

IBinaryIOReader::IBinaryIOReader (IOREADCALLBACK lpfnRcfn, IOSEEKCALLBACK lpfnScfn, LPVOID pFile)
	: IBinaryDataReader()
	, m_lpfnRcfn(NULL)
	, m_lpfnScfn(NULL)
	, m_pFile(NULL)
{
	SetIOEnvironment(lpfnRcfn, lpfnScfn, pFile);
}

/*-----------------------------------------------------------------*/

EXC_TYPE IBinaryIOReader::SetIOEnvironment (IOREADCALLBACK lpfnRcfn, IOSEEKCALLBACK lpfnScfn, LPVOID pFile)
{
	if ((NULL == lpfnRcfn) && (NULL == lpfnScfn))
		return EPARAM;
	if ((m_lpfnRcfn != NULL) || (m_lpfnScfn != NULL) || (m_pFile != NULL))
		return EEXIST;

	m_lpfnRcfn = lpfnRcfn;
	m_lpfnScfn = lpfnScfn;
	m_pFile = pFile;
	return S_OK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE IBinaryIOReader::SeekData (const int nDir, const UINT32 ulCount)
{
	if (NULL == m_lpfnScfn)
		return ETRANSMISSION;
	else
		return (*m_lpfnScfn)(m_pFile, nDir, ulCount);
}

/*--------------------------------------------------------------------------*/

EXC_TYPE IBinaryIOReader::ReadData (BYTE buf[], const DWORD dwMaxLen, DWORD& dwReadLen)
{
	dwReadLen = 0;

	if (NULL == m_lpfnRcfn)
		return ETRANSMISSION;

	if (0 == dwMaxLen)
		return EOK;

	if (NULL == buf)
		return EBADBUFF;

	if (IOCFN_BAD_LEN == (dwReadLen=(*m_lpfnRcfn)(m_pFile, buf, dwMaxLen)))
		return EIOHARD;

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE IBinaryIOReader::Close ()
{
	if (m_lpfnRcfn != NULL)
		m_lpfnRcfn = NULL;	// just so we can have a debug breakpoint
	if (m_lpfnScfn != NULL)
		m_lpfnScfn = NULL;	// just so we can have a debug breakpoint
	if (m_pFile != NULL)
		m_pFile = NULL;

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

IBinaryFileReader::IBinaryFileReader ()
	: IBinaryIOReader()
	, m_fAutoClose(true)
	, m_fp(NULL)
{
}

/*--------------------------------------------------------------------------*/

// NOTE: returns error if already set
EXC_TYPE IBinaryFileReader::SetFile (FILE *fp, const bool fAutoClose)
{
	if (NULL == fp)
		return EPARAM;
	if (m_fp != NULL)
		return EEXIST;

	EXC_TYPE	exc=SetIOEnvironment(::fileIOReadCfn, ::fileIOSeekCfn, (LPVOID) fp);
	if (exc != EOK)
		return exc;

	m_fp = fp;
	m_fAutoClose = fAutoClose;

	return EOK;
}

/*--------------------------------------------------------------------------*/

IBinaryFileReader::IBinaryFileReader (FILE *fp, const bool fAutoClose)
	: IBinaryIOReader(::fileIOReadCfn, ::fileIOSeekCfn, (LPVOID) fp)
	, m_fAutoClose(fAutoClose)
	, m_fp(fp)
{
}

/*--------------------------------------------------------------------------*/

// NOTE: returns error if already set
EXC_TYPE IBinaryFileReader::SetFile (LPCTSTR lpszFilePath, const bool fAutoClose)
{
	if (IsEmptyStr(lpszFilePath))
		return EPARAM;

	FILE	*fin=::_tfopen(lpszFilePath, _T("r") FBINMODE);
	if (NULL == fin)
		return EFNEXIST;

	CFilePtrGuard	fig(fin);
	EXC_TYPE			exc=SetFile(fin, fAutoClose);
	if (exc != EOK)
		return exc;

	fin = NULL;	// disable auto-release
	return EOK;
}

/*--------------------------------------------------------------------------*/

IBinaryFileReader::IBinaryFileReader (LPCTSTR lpszFilePath, const bool fAutoClose)
	: IBinaryIOReader()
	, m_fAutoClose(true)
	, m_fp(NULL)
{
	SetFile(lpszFilePath, fAutoClose);
}

/*--------------------------------------------------------------------------*/

// NOTE: assumes file pointer is closed by someone else
EXC_TYPE IBinaryFileReader::Detach ()
{
	EXC_TYPE	exc=IBinaryIOReader::Close();
	if (m_fp != NULL)
		m_fp = NULL;	// just so we have a debug breakpoint

	return exc;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE IBinaryFileReader::Close ()
{
	EXC_TYPE	exc=IBinaryIOReader::Close();
	if (m_fp != NULL)
	{
		if (m_fAutoClose)
			::fclose(m_fp);
		m_fp = NULL;
	}

	return exc;
}

//////////////////////////////////////////////////////////////////////////////
