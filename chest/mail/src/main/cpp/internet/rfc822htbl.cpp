#include <util/string.h>
#include <util/errors.h>

#include <internet/rfc822.h>

/*---------------------------------------------------------------------------*/

// also default constructor
CRFC822HdrsTbl::CRFC822HdrsTbl (const UINT32 ulMaxHdrs)
	: m_pHdrs(NULL), m_ulHNum(0), m_ulCNum(0), m_Mapper()
{
	if (ulMaxHdrs != 0)
	{
		SetSize(ulMaxHdrs);
	}
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRFC822HdrsTbl::SetSize (const UINT32 ulMaxHdrs)
{
	if (m_pHdrs != NULL)
		return EJOBPARAM;

	if (0 == ulMaxHdrs)
		return EPARAM;

	Reset();

	if (NULL == (m_pHdrs=new LPCRFC822HDRDATA[ulMaxHdrs]))
		return EMEM;

	m_ulHNum = ulMaxHdrs;
	for (UINT32 ulHdx=0; ulHdx < m_ulHNum; ulHdx++)
		m_pHdrs[ulHdx] = NULL;

	return m_Mapper.InitMap(m_ulHNum, FALSE);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRFC822HdrsTbl::Reset ()
{
	if (m_pHdrs != NULL)
	{
		for (UINT32 ulIdx=0; ulIdx < m_ulHNum; ulIdx++)
		{
			LPCRFC822HDRDATA	pHdrData=m_pHdrs[ulIdx];
			if (pHdrData != NULL)
				pHdrData->Reset();
		}
	}

	m_ulCNum = 0;
	m_Mapper.Reset();

	return EOK;
}

/*---------------------------------------------------------------------------*/

void CRFC822HdrsTbl::Cleanup ()
{
	m_Mapper.Clear();

	if (m_pHdrs != NULL)
	{
		for (UINT32 ulIdx=0; ulIdx < m_ulHNum; ulIdx++)
		{
			LPCRFC822HDRDATA&	pHdrData=m_pHdrs[ulIdx];
			if (pHdrData != NULL)
			{
				delete pHdrData;
				pHdrData = NULL;
			}
		}

		delete [] m_pHdrs;
		m_pHdrs = NULL;
	}

	m_ulHNum = 0;
	m_ulCNum = 0;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRFC822HdrsTbl::AllocateHdrData (LPCRFC822HDRDATA& pHdrData)
{
	if (m_ulCNum >= m_ulHNum)
		return EOVERFLOW;

	// do greedy allocation
	if (NULL == (pHdrData=m_pHdrs[m_ulCNum]))
	{
		if (NULL == (pHdrData=new CRFC822HdrData))
			return EMEM;

		m_pHdrs[m_ulCNum] = pHdrData;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRFC822HdrsTbl::AddHdrData (const char pszHdrName[], const char pszHdrValue[], const UINT32 ulVLen)
{
	if (IsEmptyStr(pszHdrName))
		return EPARAM;

	// check if already have some data for this header
	LPCRFC822HDRDATA	pHdrData=NULL;
	EXC_TYPE				exc=GetHdrData(pszHdrName, pHdrData);
	if (EOK == exc)	// if have some data, then append it
		return pHdrData->AddData(pszHdrName, pszHdrValue, ulVLen);

	if ((exc=AllocateHdrData(pHdrData)) != EOK)
		return exc;
	if ((exc=pHdrData->AddData(pszHdrName, pszHdrValue, ulVLen)) != EOK)
		return exc;
	if ((exc=m_Mapper.AddKey(pszHdrName, (LPVOID) pHdrData)) != EOK)
		return exc;

	m_ulCNum++;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRFC822HdrsTbl::SetHdrData (const char pszHdrName[], const char pszHdrValue[], const UINT32 ulVLen)
{
	if (IsEmptyStr(pszHdrName))
		return EPARAM;

	// check if already have some data for this header
	LPCRFC822HDRDATA	pHdrData=NULL;
	EXC_TYPE				exc=GetHdrData(pszHdrName, pHdrData);
	if (EOK == exc)	// if have some data, then overwrite it
		return pHdrData->SetData(pszHdrName, pszHdrValue, ulVLen);

	return AddHdrData(pszHdrName, pszHdrValue, ulVLen);
}

/*---------------------------------------------------------------------------*/

// Note: the header value is auto-released (deallocated...)
EXC_TYPE CRFC822HdrsTbl::ReplaceHdrData (const char pszHdrName[], char *pszHdrValue)
{
	if (IsEmptyStr(pszHdrName))
		return EPARAM;

	// check if already have some data for this header
	LPCRFC822HDRDATA	pHdrData=NULL;
	EXC_TYPE				exc=GetHdrData(pszHdrName, pHdrData);
	if (EOK == exc)	// if have some data, then overwrite it
		return pHdrData->ReplaceData(pszHdrName, pszHdrValue);

	if ((exc=AllocateHdrData(pHdrData)) != EOK)
		return exc;
	if ((exc=pHdrData->ReplaceData(pszHdrName, pszHdrValue)) != EOK)
		return exc;
	if ((exc=m_Mapper.AddKey(pszHdrName, (LPVOID) pHdrData)) != EOK)
		return exc;

	m_ulCNum++;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRFC822HdrsTbl::GetHdrData (const char pszHdrName[], LPCRFC822HDRDATA& pHdrData) const
{
	pHdrData = NULL;

	if (IsEmptyStr(pszHdrName))
		return EPARAM;

	LPVOID	pV=NULL;
	EXC_TYPE	exc=m_Mapper.FindKey(pszHdrName, pV);
	if (exc != EOK)
		return exc;

	pHdrData = (LPCRFC822HDRDATA) pV;
	return EOK;
}

/*---------------------------------------------------------------------------*/

BOOLEAN CRFC822HdrsTbl::IsHdrInTbl (const char pszHdrName[]) const
{
	LPCRFC822HDRDATA	pHdrData=NULL;
	EXC_TYPE				exc=GetHdrData(pszHdrName, pHdrData);
	return ((EOK == exc) && (pHdrData != NULL));
}

/*---------------------------------------------------------------------------*/

LPCRFC822HDRDATA CRFC822HdrsTbl::operator[] (const char pszHdrName[]) const
{
	LPCRFC822HDRDATA	pHdrData=NULL;
	EXC_TYPE				exc=GetHdrData(pszHdrName, pHdrData);
	return ((EOK == exc) ? pHdrData : NULL);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRFC822HdrsTbl::GetHdrData (const UINT32 ulIdx, LPCRFC822HDRDATA& pHdrData) const
{
	pHdrData = NULL;
	if (ulIdx >= m_ulCNum)
		return EOVERFLOW;

	pHdrData = (LPCRFC822HDRDATA) m_pHdrs[ulIdx];
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRFC822HdrsTbl::GetHdrData (const char pszHdrName[], LPCTSTR& lpszHdrValue) const
{
	lpszHdrValue = NULL;

	LPCRFC822HDRDATA	pHD=NULL;
	EXC_TYPE				exc=GetHdrData(pszHdrName, pHD);
	if (exc != EOK)
		return exc;
	if (NULL == pHD)
		return EEMPTYENTRY;

	lpszHdrValue = pHD->GetHdrValue();
	return EOK;
}

/*--------------------------------------------------------------------------*/

LPCRFC822HDRDATA CRFC822HdrsTbl::operator[] (const UINT32 ulIdx) const
{
	LPCRFC822HDRDATA	pHdrData=NULL;
	EXC_TYPE				exc=GetHdrData(ulIdx, pHdrData);
	return ((EOK == exc) ? pHdrData : NULL);
}

/*---------------------------------------------------------------------------*/

// copies supplied table OVER this one
EXC_TYPE CRFC822HdrsTbl::UpdateHdrsTbl (const CRFC822HdrsTbl& ht)
{
	EXC_TYPE	exc=Reset();
	if (exc != EOK)
		return exc;

	const UINT32	ulHNum=ht.GetSize();
	for (UINT32 ulHdx=0; ulHdx < ulHNum; ulHdx++)
	{
		LPCRFC822HDRDATA	pHdrData=NULL;
		if ((exc=ht.GetHdrData(ulHdx, pHdrData)) != EOK)
			return exc;

		if (NULL == pHdrData)
			return ENODATA;
		
		if ((exc=AddHdrData(*pHdrData)) != EOK)
			return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

// copy constructor
CRFC822HdrsTbl::CRFC822HdrsTbl (const CRFC822HdrsTbl& ht)
	: m_pHdrs(NULL), m_ulHNum(0), m_ulCNum(0), m_Mapper()
{
	if (ht.m_ulHNum != 0)
	{
		EXC_TYPE	exc=SetSize(ht.m_ulHNum);
		if (EOK == exc)
			exc = UpdateHdrsTbl(ht);
	}
}

/*---------------------------------------------------------------------------*/
