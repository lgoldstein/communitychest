#include <internet/rawmsgparser.h>

//////////////////////////////////////////////////////////////////////////////

CRawRFC2046MsgPart::CRawRFC2046MsgPart (const UINT32 ulMaxHdrs)
	: m_lpszPartName(NULL), m_partHdrs(ulMaxHdrs)
{
	Reset();
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRawRFC2046MsgPart::Init (const UINT32 ulMaxHdrs)
{
	Reset();

	return m_partHdrs.SetSize(ulMaxHdrs);
}

/*---------------------------------------------------------------------------*/

void CRawRFC2046MsgPart::Reset ()
{
	EXC_TYPE	exc=m_partHdrs.Reset();

	m_ulPartID = (UINT32) (-1);
	m_ulHdrsStartOffset = 0;
	m_ulHdrsEndOffset = 0;
	m_ulDataStartOffset = 0;
	m_ulDataEndOffset = 0;

	::strreleasebuf(m_lpszPartName);
}

/*---------------------------------------------------------------------------*/

// Note: does NOTE copy the headers !!!
EXC_TYPE CRawRFC2046MsgPart::UpdatePartDescriptor (const CRawRFC2046MsgPart& mp)
{
	EXC_TYPE	exc=::strupdatebuf(mp.m_lpszPartName, m_lpszPartName);
	if (exc != EOK)
		return exc;

	m_ulPartID = mp.m_ulPartID;
	m_ulHdrsStartOffset = mp.m_ulHdrsStartOffset;
	m_ulHdrsEndOffset = mp.m_ulHdrsEndOffset;
	m_ulDataStartOffset = mp.m_ulDataStartOffset;
	m_ulDataEndOffset = mp.m_ulDataEndOffset;

	return EOK;
}

/*---------------------------------------------------------------------------*/

// Note: resets and updates all information
EXC_TYPE CRawRFC2046MsgPart::UpdateRawMsgPart (const CRawRFC2046MsgPart& mp)
{
	Reset();

	EXC_TYPE	exc=UpdatePartDescriptor(mp);
	if (exc != EOK)
		return exc;

	if ((exc=UpdatePartHdrs(mp)) != EOK)
		return exc;

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

// Note: the part is added as a POINTER (!!!) and will be released upon destruction
EXC_TYPE CRawRFC2046MsgPartsCollection::AddPart (const UINT32 ulMaxHdrs, CRawRFC2046MsgPart*	&mp)
{
	mp = NULL;

	CRawRFC2046MsgPart	*pDumPart=new CRawRFC2046MsgPart;
	if (NULL == pDumPart)
		return EMEM;
	CRawRFC2046MPGuard	dpg(pDumPart);

	EXC_TYPE	exc=pDumPart->Init(ulMaxHdrs);
	if (exc != EOK)
		return exc;

	if ((exc=m_MsgParts.AddItem((LPVOID) pDumPart)) != EOK)
		return exc;

	mp = pDumPart;
	pDumPart = NULL;	// disable auto-release

	return EOK;
}

/*---------------------------------------------------------------------------*/

void CRawRFC2046MsgPartsCollection::Clear ()
{
	for (UINT32 ulMPdx=0; ulMPdx < m_MsgParts.GetSize(); ulMPdx++)
	{
		CRawRFC2046MsgPart	*pMP=(CRawRFC2046MsgPart *) m_MsgParts[ulMPdx];
		CRawRFC2046MPGuard	mpg(pMP);
	}

	m_MsgParts.Clear();
}

//////////////////////////////////////////////////////////////////////////////
