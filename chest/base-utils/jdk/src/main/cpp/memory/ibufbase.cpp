#include <util/memory.h>

//////////////////////////////////////////////////////////////////////////////

EXC_TYPE IBufBuilder::AddData (const BYTE bData[], const UINT32 ulDataLen)
{
	if (0 == ulDataLen)
		return EOK;
	if (NULL == bData)
		return ENOREMOTEBUFFER;

	UINT32	ulAvailLen=(m_ulMaxLen - m_ulCurLen);
	if (ulAvailLen < ulDataLen)
	{
		EXC_TYPE	exc=ReadjustBuffer(ulDataLen - ulAvailLen);
		if (exc != EOK)
			return exc;

		ulAvailLen = (m_ulMaxLen - m_ulCurLen);
	}

	if (NULL == m_pBuf)
		return ENOLOCALBUFFER;

	LPBYTE	pDst=(m_pBuf + m_ulCurLen);
	::memcpy(pDst, bData, ulDataLen);
	m_ulCurLen += ulDataLen;
	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE IBufBuilder::AddWord (const WORD bVal, const bool fLittleEndian)
{
	const BYTE	bData[sizeof bVal]={
		(BYTE) (fLittleEndian ? bVal			: (bVal >> 8)) & 0x00FF,
		(BYTE) (fLittleEndian ? (bVal >> 8)	:		bVal	 )	& 0x00FF
	};

	return AddData(bData, (sizeof bData));
}

/*--------------------------------------------------------------------------*/

EXC_TYPE IBufBuilder::AddDword (const DWORD bVal, const bool fLittleEndian)
{
	const BYTE	bData[sizeof bVal]={
	 (BYTE)	(fLittleEndian ? bVal			: (bVal >> 24)) & 0x00000000FF,
	 (BYTE)	(fLittleEndian ? (bVal >> 8)	: (bVal >> 16)) & 0x00000000FF,
	 (BYTE)	(fLittleEndian ? (bVal >> 16) : (bVal >>  8)) & 0x00000000FF,
	 (BYTE)	(fLittleEndian ? (bVal >> 24) :		bVal	  ) & 0x00000000FF
	};

	return AddData(bData, (sizeof bData));
}

//////////////////////////////////////////////////////////////////////////////

// Note: can be called only once !!!
EXC_TYPE CIncBufBuilder::Init (const UINT32 ulInitialSize, const UINT32 ulGrowSize, const BOOLEAN fAutoRelease)
{
	if (0 == ulInitialSize)
		return EPARAM;

	if ((m_pBuf != NULL) || (m_ulCurLen != 0) || (m_ulMaxLen != 0))
		return EEXIST;

	if (NULL == (m_pBuf=new BYTE[ulInitialSize]))
		return EMEM;
	::memset(m_pBuf, 0, ulInitialSize);

	m_ulMaxLen = ulInitialSize;
	m_ulGrowSize = ulGrowSize;

	SetAutoRelease(fAutoRelease);
	return EOK;
}

/*--------------------------------------------------------------------------*/

// called whenever there is not enough space to add data
EXC_TYPE CIncBufBuilder::ReadjustBuffer (const UINT32 ulMinGrow)
{
	if (0 == m_ulGrowSize)
		return ELIMIT;

	const UINT32	ulNewSize=(m_ulMaxLen + ulMinGrow + m_ulGrowSize);
	BYTE				*pNewBuf=new BYTE[ulNewSize];
	if (NULL == pNewBuf)
		return EMEM;
	::memset(pNewBuf, 0, ulNewSize);

	// copy any previous data
	if (m_pBuf != NULL)
	{
		if (m_ulCurLen != 0)
			::memcpy(pNewBuf, m_pBuf, m_ulCurLen);
		delete [] m_pBuf;
	}

	m_pBuf = pNewBuf;
	m_ulMaxLen = ulNewSize;
	return EOK;
}

/*--------------------------------------------------------------------------*/

CIncBufBuilder::~CIncBufBuilder ()
{
	if (m_fAutoRelease && (m_pBuf != NULL))
	{
		delete [] m_pBuf;
		m_pBuf = NULL;
	}
}

//////////////////////////////////////////////////////////////////////////////

EXC_TYPE IBufConsumer::SetBuffer (LPBYTE pBuf, const DWORD dwStartPos, const DWORD dwMaxLen)
{
	if ((NULL == (m_pBuf=pBuf)) || ((m_dwStartPos=dwStartPos) > (m_dwMaxLen=dwMaxLen)))
		return EPARAM;

	m_dwCurPos = dwStartPos;
	return EOK;
}

/*--------------------------------------------------------------------------*/

UINT32 extractBufferDataValue (const BYTE buf[], const BYTE len, const bool fLittleEndian)
{
	UINT32	v=0;

	for (BYTE	i=0; i < len; i++)
	{
		UINT32	dwVal=((UINT32) (fLittleEndian ? buf[len - i - 1]: buf[i])) & 0x000000FF;
		v <<= 8;
		v |= dwVal;
	}

	return v;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE IBufConsumer::GetWord (WORD& v, const bool fLittleEndian)
{
	v = 0;

	BYTE		vData[sizeof v]={ 0 };
	EXC_TYPE	exc=GetData(vData, (sizeof vData));
	if (exc != EOK)
		return exc;

	v = (WORD) ::extractBufferDataValue(vData, (sizeof vData), fLittleEndian);
	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE IBufConsumer::GetDword (DWORD& v, const bool fLittleEndian)
{
	v = 0;

	BYTE		vData[sizeof v]={ 0 };
	EXC_TYPE	exc=GetData(vData, (sizeof vData));
	if (exc != EOK)
		return exc;

	v = ::extractBufferDataValue(vData, (sizeof vData), fLittleEndian);
	return EOK;
}

/*--------------------------------------------------------------------------*/

// Note: returns error if not enough data remaining
EXC_TYPE IBufConsumer::GetData (BYTE v[], const DWORD dwLen)
{
	if (NULL == v)
		return EPARAM;
	if (NULL == m_pBuf)
		return EBADBUFF;

	const DWORD	dwNewPos=m_dwCurPos + dwLen;
	if (dwNewPos > m_dwMaxLen)
		return EOVERFLOW;

	::memcpy(v, (m_pBuf + m_dwCurPos), dwLen);
	m_dwCurPos = dwNewPos;

	return EOK;
}

/*--------------------------------------------------------------------------*/

// Note: returns error if asked to seek beyond start/end
EXC_TYPE IBufConsumer::Seek (const SINT32 sLen)
{
	if (NULL == m_pBuf)
		return EBADBUFF;

	const DWORD	dwNewPos=m_dwCurPos + sLen;
	if (sLen > 0)
	{
		if (dwNewPos > m_dwMaxLen)
			return EOVERFLOW;
	}
	else if (dwNewPos < m_dwStartPos)
		return EDEFAULTSO;

	m_dwCurPos = dwNewPos;
	return EOK;
}

/*--------------------------------------------------------------------------*/

void IBufConsumer::Clear ()
{
	m_pBuf = NULL;
	m_dwStartPos = 0;
	m_dwCurPos = 0;
	m_dwMaxLen = 0;
}

//////////////////////////////////////////////////////////////////////////////
