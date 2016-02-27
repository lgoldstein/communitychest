#include <util/memory.h>
#include <util/errors.h>
#include <util/string.h>

/*--------------------------------------------------------------------------*/

CVSDCollection::CVSDCollection (const UINT32 ulMaxItems, const UINT32 ulGrow)
	: m_ulMaxItems(0), m_ulCurItems(0), m_ulGrwItems(0), m_pItems(NULL)
{
	if (ulMaxItems != 0)
	{
		SetParams(ulMaxItems, ulGrow);
	}
	else	// allow zero initial size, and if such, then store the growth factor
		m_ulGrwItems = ulGrow;
}

/*--------------------------------------------------------------------------*/

// Note(s):
//
//		a. grow factor may be zero - i.e. when limit is reached no more items
//			are added.
//
//		b. cannot be re-initialized !!!
EXC_TYPE CVSDCollection::SetParams (const UINT32 ulMaxItems, const UINT32 ulGrow)
{
	if ((0 == ulMaxItems) && (0 == ulGrow))
		return EPARAM;

	if (m_ulMaxItems != 0)
		return EEXIST;

	m_ulMaxItems = ulMaxItems;
	m_ulGrwItems = ulGrow;
	return EOK;
}

/*--------------------------------------------------------------------------*/

typedef struct {
	UINT32	ulDataLen;	// if 0 then not auto-allocated;
	LPVOID	pData;
	BYTE		data[sizeof(NATIVE_WORD)];
} CVSDE, *LPCVSDE;

// returns an entry large enough to accomodate specified data size - if 0, then assumed only the pointer is saved
static EXC_TYPE GetCVSDEntry (const UINT32 ulDataSize, LPCVSDE& pE)
{
	pE = NULL;

	UINT32	ulSize=(sizeof *pE);
	// add more data only if cannot accomodate in data field
	if (ulDataSize >= (sizeof pE->data))
		ulSize += ulDataSize;

	// we allocate slightly more than necessary to avoid last-byte access errors
	LPBYTE	pBuf=new BYTE[ulSize];
	if (NULL == pBuf)
		return EMEM;

	pE = (LPCVSDE) pBuf;
	memset(pE, 0, (sizeof *pE));
	pE->pData = pE->data;
	pE->ulDataLen = ulDataSize;

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CVSDCollection::AppendEntry (LPVOID pE /* actually an internal structure */)
{
	if (NULL == pE)	// should not happen, as called internally only
		return EFATALEXIT;

	if ((NULL == m_pItems) && (m_ulMaxItems > 0))
	{
		if (NULL == (m_pItems=new LPVOID[m_ulMaxItems]))
			return EMEM;

		for (UINT32 i=0; i < m_ulMaxItems; i++)
			m_pItems[i] = NULL;
	}

	if (m_ulCurItems >= m_ulMaxItems)
	{
		if (0 == m_ulGrwItems)
			return EOVERFLOW;

		UINT32	ulNewMax=(m_ulCurItems + m_ulGrwItems), i=0;
		LPVOID	*pNewItems=new LPVOID[ulNewMax];
		if (NULL == pNewItems)
			return EMEM;

		// copy existing items to new array
		for (i=0 ; i < m_ulCurItems; i++)
			pNewItems[i] = m_pItems[i];
		for ( ; i < ulNewMax; i++)
			pNewItems[i] = NULL;

		if (m_pItems != NULL)
			delete [] m_pItems;

		m_pItems = pNewItems;
		m_ulMaxItems = ulNewMax;
	}

	m_pItems[m_ulCurItems] = pE;
	m_ulCurItems++;

	return EOK;
}

/*--------------------------------------------------------------------------*/

// NULL entries ARE skipped - up to specified number of strings (may be ZERO)
// if zero number of strings then an empty entry is created  (same as if
// all items were "")
EXC_TYPE CVSDCollection::ConcatStringsItem (LPCTSTR s[], const UINT32 ulNumStrs)
{
	if ((NULL == s) && (ulNumStrs != 0))
		return EJOBSIZE;

	UINT32	ulTotalLen=0;
	{
		for (UINT32	ulIndex=0; ulIndex < ulNumStrs; ulIndex++)
			ulTotalLen += GetSafeStrlen(s[ulIndex]);
	}
	if (0 == ulTotalLen)
		return AddChars(NULL, 0);

	LPCVSDE			pE=NULL;
	LPBYTE&			pBuf=(LPBYTE &) pE;
	CBytesBufGuard	bfg(pBuf);
	EXC_TYPE			exc=GetCVSDEntry((ulTotalLen + 1) * sizeof(TCHAR), pE);
	if (exc != EOK)
		return exc;

	{
		CStrlBuilder	strb((LPTSTR) pE->data, (ulTotalLen + 1));
		for (UINT32	ulIndex=0; ulIndex < ulNumStrs; ulIndex++)
			if ((exc=strb.AddStr(GetSafeStrPtr(s[ulIndex]))) != EOK)
				return exc;
	}

	if ((exc=AppendEntry(pE)) != EOK)
		return exc;

	pE = NULL;	// disable auto-release
	return EOK;
}

/*--------------------------------------------------------------------------*/

// last string must be NULL (Note: empty strings ("") ARE not
// considered NULL)
// may be NULL array - in which case, an empty entry is created (same
// as if all items were "")
EXC_TYPE CVSDCollection::ConcatStringsItem (LPCTSTR s[])
{
	if (NULL == s)
		return AddChars(NULL, 0);

	for (UINT32	ulNumStrs=0; ulNumStrs < UINT32_MAX /* just make sure not a forever loop */ ; ulNumStrs++)
		if (NULL == s[ulNumStrs])
			return ConcatStringsItem(s, ulNumStrs);

	// this code is unreachable
	return EABORTEXIT;
}

/*--------------------------------------------------------------------------*/

static EXC_TYPE GetCVSDEntry (LPVOID pData, const UINT32 ulDataSize, LPCVSDE& pE)
{
	pE = NULL;

	if (NULL == pData)
		return EBADBUFF;

	EXC_TYPE	exc=GetCVSDEntry(ulDataSize, pE);
	if (exc != EOK)
		return exc;

	if (pE->ulDataLen != 0)
	{
		memcpy(pE->data, pData, ulDataSize);
		pE->data[ulDataSize] = 0;	// just being nice
	}
	else
	{
		pE->pData = pData;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CVSDCollection::AddEntry (LPVOID pData, const UINT32 ulDataSize)
{
	LPCVSDE			pE=NULL;
	LPBYTE&			pBuf=(LPBYTE &) pE;
	CBytesBufGuard	bfg(pBuf);
	EXC_TYPE			exc=GetCVSDEntry(pData, ulDataSize, pE);
	if (exc != EOK)
		return exc;

	if ((exc=AppendEntry(pE)) != EOK)
		return exc;

	pE = NULL;	// disable auto-release
	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CVSDCollection::SetEntry (const UINT32 ulItemIndex, LPVOID pData, const UINT32 ulDataLen)
{
	if (ulItemIndex >= m_ulCurItems)
		return ERANGE;

	// should not happen...
	if (NULL == m_pItems)
		return ENOLOADERMEM;

	LPCVSDE&	pE=(LPCVSDE &) m_pItems[ulItemIndex];
	// if originally not auto allocated and new item not auto allocated as well then simply replace the pointer
	if ((0 == ulDataLen) && (0 == pE->ulDataLen))
	{
		pE->pData = pData;
		return EOK;
	}

	// if both auto-allocated and can accomodate the new data the simply copy it
	if ((ulDataLen != 0) && (pE->ulDataLen != 0) && (ulDataLen <= pE->ulDataLen))
	{
		memcpy(pE->data, pData, ulDataLen);
		pE->ulDataLen = ulDataLen;
		return EOK;
	}

	LPBYTE	pBuf=(LPBYTE) pE;
	delete [] pBuf;
	pE = NULL;

	EXC_TYPE	exc=GetCVSDEntry(pData, ulDataLen, pE);
	if (exc != EOK)
		return exc;

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CVSDCollection::EnumItems (CVSD_ENUM_CFN lpfnEcfn, LPVOID pArg) const
{
	if (NULL == lpfnEcfn)
		return EBADADDR;

	if ((NULL == m_pItems) && (m_ulCurItems != 0))
		return ESTATE;

	for (UINT32 ulIdx=0; ulIdx < m_ulCurItems; ulIdx++)
	{
		LPCVSDE	pE=(LPCVSDE) m_pItems[ulIdx];
		if (NULL == pE)
			continue;

		// callback for enumerating data collection items
		BOOLEAN	fContEnum=TRUE;
		EXC_TYPE exc=(*lpfnEcfn)(pE->pData, pArg, fContEnum);
		if (exc != EOK)
			return exc;

		if (!fContEnum)
			break;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

// data len is ZERO for items that have only the PTR available		
EXC_TYPE CVSDCollection::GetData (const UINT32 ulIdx, LPVOID& pData, UINT32& ulDataLen) const
{
	pData = NULL;

	if (ulIdx >= m_ulCurItems)
		return EOVERFLOW;

	if (NULL == m_pItems)
		return ENOLOADERMEM;

	LPCVSDE	pE=(LPCVSDE) m_pItems[ulIdx];
	if (NULL == pE)
		return ESTATE;

	pData = pE->pData;
	ulDataLen = pE->ulDataLen;
	return EOK;
}

/*--------------------------------------------------------------------------*/

LPVOID CVSDCollection::operator[] (const UINT32 ulIdx) const
{
	LPVOID	pData=NULL;
	EXC_TYPE	exc=GetData(ulIdx, pData);
	if (exc != EOK)
		return NULL;

	return pData;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CVSDCollection::Merge (const CVSDCollection& dc)
{
	if ((dc.m_ulCurItems != 0) && (NULL == dc.m_pItems))
		return ENOSTART;

	for (UINT32 ulIdx=0; ulIdx < dc.m_ulCurItems; ulIdx++)
	{
		LPCVSDE	pE=(LPCVSDE) dc.m_pItems[ulIdx];
		if (NULL == pE)
			continue;

		// make sure auto-allocated items remain as such
		EXC_TYPE	exc=((0 == pE->ulDataLen) ? AddItem(pE->pData) : AddItem(pE->pData, pE->ulDataLen));
		if (exc != EOK)
			return exc;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CVSDCollection::FindEntry (LPVOID pDE, const UINT32 ulDataSize, UINT32& ulIdx) const
{
	if ((NULL == m_pItems) && (m_ulCurItems != 0))
		return ESTATE;

	for (ulIdx=0 ; ulIdx < m_ulCurItems; ulIdx++)
	{
		LPCVSDE	pE=(LPCVSDE) m_pItems[ulIdx];
		BOOLEAN	fMatch=FALSE;
		if (0 == ulDataSize)
			fMatch = (pE->pData == pDE);
		else
			fMatch = ((ulDataSize == pE->ulDataLen) && (0 == memcmp(pDE, pE->pData, ulDataSize)));

		if (fMatch)
			return EOK;
	}

	return EEXIST;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CVSDCollection::DelEntry (LPVOID pDE, const UINT32 ulDataSize)
{
	UINT32	ulIdx=(UINT32) (-1);
	EXC_TYPE	exc=FindEntry(pDE, ulDataSize, ulIdx);
	if (exc != EOK)
		return exc;

	return RemoveItem(ulIdx);
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CVSDCollection::RemoveItem (const UINT32 ulItemIndex)
{
	if (ulItemIndex >= m_ulCurItems)
		return ERANGE;

	if (NULL == m_pItems)
		return ENOLOADERMEM;

	LPCVSDE	pE=(LPCVSDE) m_pItems[ulItemIndex];
	LPBYTE	pBuf=(LPBYTE) pE;
	delete [] pBuf;

	// "shift" down
	for (UINT32	ulIdx=ulItemIndex; ulIdx < m_ulCurItems; ulIdx++)
		m_pItems[ulIdx] = m_pItems[ulIdx+1];

	m_ulCurItems--;
	m_pItems[m_ulCurItems] = NULL;

	return EOK;
}

/*--------------------------------------------------------------------------*/

// removes all data items
EXC_TYPE CVSDCollection::Reset ()
{
	if (m_pItems != NULL)
	{
		for (UINT32 ulIdx=0; ulIdx < m_ulMaxItems; ulIdx++)
		{
			LPBYTE	pBuf=(LPBYTE) m_pItems[ulIdx];
			if (NULL == pBuf)
				continue;

			delete [] pBuf;
			m_pItems[ulIdx] = NULL;
		}

		delete [] m_pItems;
		m_pItems = NULL;
	}

	m_ulCurItems = 0;
	return EOK;
}

/*--------------------------------------------------------------------------*/

// must be re-initialized afterwards
EXC_TYPE CVSDCollection::Clear ()
{
	EXC_TYPE	exc=Reset();
	if (EOK != exc)
		return exc;

	m_ulMaxItems = 0;
	m_ulGrwItems = 0;

	return exc;
}

//////////////////////////////////////////////////////////////////////////////

// returns EEOF if no more items
EXC_TYPE CVSDCollEnum::GetNextItem (LPVOID& pItem)
{
	while (m_ulCurItem < m_dc.m_ulCurItems)
	{
		LPCVSDE	pE=(LPCVSDE) m_dc.m_pItems[m_ulCurItem++];
		if (NULL == pE)	// skip empty entries
			continue;

		pItem = pE->pData;
		return EOK;
	}

	pItem = NULL;
	return EEOF;
}

/*--------------------------------------------------------------------------*/
