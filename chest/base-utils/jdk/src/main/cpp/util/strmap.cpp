#include <util/string.h>
#include <util/math.h>
#include <util/errors.h>

/*---------------------------------------------------------------------------*/

typedef struct tag_s2pe {
	struct tag_s2pe	*pNext;
	struct tag_s2pe	*pPrev;
	LPVOID				pVal;
	UINT32				ulKeyLen;
	char					szKey[2];	// actual size is dynamically allocated
} S2PE, *LPS2PE;

static EXC_TYPE GetS2PE (const char pszKey[], const UINT32 ulKeyLen, LPVOID pVal, LPS2PE& pE)
{
	pE = NULL;

	if (IsEmptyStr(pszKey) || (0 == ulKeyLen))
		return EEMPTYENTRY;

	LPBYTE	pBuf=new BYTE[(sizeof *pE) + ulKeyLen];
	if (NULL == pBuf)
		return EMEM;

	pE = (LPS2PE) pBuf;
	memset(pE, 0, (sizeof *pE));
	strncpy(pE->szKey, pszKey, ulKeyLen);
	pE->szKey[ulKeyLen] = '\0';
	pE->ulKeyLen = ulKeyLen;
	pE->pVal = pVal;

	return EOK;
}

/*---------------------------------------------------------------------------*/

/* returns 0 if s2pa is NULL as well */
UINT32 CountStr2PtrAssocs (const STR2PTRASSOC s2pa[])
{
	UINT32	ulANum=0;

	if (0 == s2pa)
		return 0;

	for (ulANum=0; ; ulANum++)
		if (IsEmptyStr(s2pa[ulANum].pszKey))
			return ulANum;

	return ((UINT32) (-1));	/* should not be reached !!! */
}

//////////////////////////////////////////////////////////////////////////////

EXC_TYPE CStr2PtrMapper::InitMap (const UINT32 ulMapSize, const BOOLEAN fCaseSensitive)
{
	if (0UL == ulMapSize)
		return EPARAM;

	// do not allow re-initialization
	if (m_pAssocs != NULL)
		return EEXIST;

	UINT32	ulMSize=FindClosestPrime(ulMapSize);
	if (NULL == (m_pAssocs=new LPVOID[ulMSize]))
		return EMEM;

	m_ulAssocsNum = ulMSize;
	for (UINT32 i=0; i < m_ulAssocsNum; i++)
		m_pAssocs[i] = NULL;

	m_ulItemsCount = 0UL;
	m_fCase = fCaseSensitive;

	return EOK;
}

/*---------------------------------------------------------------------------*/

// also default constructor
CStr2PtrMapper::CStr2PtrMapper (const UINT32 ulMapSize, const BOOLEAN fCaseSensitive)
	: m_pAssocs(NULL), m_ulAssocsNum(0UL), m_ulItemsCount(0UL), m_fCase(FALSE)
{
	if (ulMapSize != 0)
	{
		InitMap(ulMapSize, fCaseSensitive);
	}
}

/*---------------------------------------------------------------------------*/

// removes all associations but not the internal hash table
void CStr2PtrMapper::Reset ()
{
	if (NULL == m_pAssocs)
		return;

	for (UINT32 i=0UL; i < m_ulAssocsNum; i++)
	{
		LPS2PE pE=(LPS2PE) m_pAssocs[i];

		while (pE != NULL)
		{
			LPS2PE	pNext=pE->pNext;
			LPBYTE	pBuf=(LPBYTE) pE;
			delete [] pBuf;
			pE = pNext;
		}

		m_pAssocs[i] = NULL;
	}

	m_ulItemsCount = 0UL;
}

/*---------------------------------------------------------------------------*/

// removes all associations and the internal hash table itself.
void CStr2PtrMapper::Clear ()
{
	Reset();

	if (m_pAssocs != NULL)
	{
		delete [] m_pAssocs;
		m_pAssocs = NULL;
	}

	m_ulAssocsNum = 0UL;
	m_fCase = FALSE;
}

/*---------------------------------------------------------------------------*/
/*
 *  An adaptation of Peter Weinberger's (PJW) generic hashing
 *  algorithm based on Allen Holub's version. Accepts a pointer
 *  to a datum to be hashed and returns an unsigned 32bit integer.
 *-------------------------------------------------------------*/

#define BITS_IN_UINT32  32UL
#define THREE_QUARTERS  ((UINT32) ((BITS_IN_UINT32 * 3) / 4))
#define ONE_EIGHTH      ((UINT32) (BITS_IN_UINT32 / 8))
#define HIGH_BITS       ((UINT32) ~((UINT32)(~0) >> ONE_EIGHTH))

// returns hash value regardless of actual table size
EXC_TYPE CStr2PtrMapper::GetHashValue (const char		pszKey[],
													const UINT32	ulKeyLen,
													const BOOLEAN	fCaseSensitive,
													UINT32&			ulHashVal)
{
	const char *lsp=pszKey;
	UINT32		ulKdx=0;

	ulHashVal = 0;

	if (IsEmptyStr(pszKey) || (0 == ulKeyLen))
		return EEMPTYENTRY;

	for (ulKdx=0, lsp=pszKey; (*lsp != '\0') && (ulKdx < ulKeyLen); lsp++, ulKdx++)
	{
		const char	tch=(fCaseSensitive ? (*lsp) : (char) tolower(*lsp));

		ulHashVal = (ulHashVal << ONE_EIGHTH ) + (UINT32) tch;
		UINT32	ulHBits=(ulHashVal & HIGH_BITS);

      if (ulHBits  != 0UL)
			ulHashVal = ((ulHashVal ^ (ulHBits >> THREE_QUARTERS )) & (~HIGH_BITS));
	}

	// make sure we did not stop prematurely
	if (ulKdx < ulKeyLen)
		return EUDFFORMAT;

	return EOK;
}

/*---------------------------------------------------------------------------*/

// returns hash value normalized to actual table size
EXC_TYPE CStr2PtrMapper::GetHashIndex (const char pszKey[], const UINT32 ulKeyLen, UINT32& ulHashIdx) const
{
	ulHashIdx = 0UL;

	if (0UL == m_ulAssocsNum)
		return EEMPTYENTRY;

	UINT32	ulHashVal=0UL;
	EXC_TYPE	exc=GetHashValue(pszKey, ulKeyLen, m_fCase, ulHashVal);
	if (exc != EOK)
		return exc;

	ulHashIdx = (ulHashVal % m_ulAssocsNum);
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CStr2PtrMapper::FindKey (const char pszKey[], const UINT32 ulKeyLen, UINT32& ulAdx, LPVOID& pE) const
{
	ulAdx = 0UL;
	pE = NULL;

	if (IsEmptyStr(pszKey) || (0 == ulKeyLen))
		return EEMPTYENTRY;

	EXC_TYPE exc=GetHashIndex(pszKey, ulKeyLen, ulAdx);
	if (exc != EOK)
		return exc;

	for (LPS2PE	pA=(LPS2PE) m_pAssocs[ulAdx]; pA != NULL; pA = pA->pNext)
	{
		// if not of same length, no need to check
		if (pA->ulKeyLen != ulKeyLen)
			continue;

		const int		nRes=(m_fCase ? _tcsncmp(pszKey, pA->szKey, ulKeyLen) : _tcsnicmp(pszKey, pA->szKey, ulKeyLen));
		if (0 != nRes)
			continue;


		pE = (LPVOID) pA;
		return EOK;
	}

	return EEXIST;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CStr2PtrMapper::AddKey (const char pszKey[], const UINT32 ulKeyLen, LPVOID pVal)
{
	if (IsEmptyStr(pszKey) || (0 == ulKeyLen))
		return EEMPTYENTRY;

	if ((NULL == m_pAssocs) || (0UL == m_ulAssocsNum))
		return ECONTEXT;

	UINT32	ulAdx=0UL;
	LPVOID	pV=NULL;
	EXC_TYPE	exc=FindKey(pszKey, ulKeyLen, ulAdx, pV);
	LPS2PE	pE=(LPS2PE) pV;

	// if key already exists, simply update the value
	if (EOK == exc)
	{
		if (NULL == pE)
			return ECONTEXT;
		pE->pVal = pVal;
	}
	else
	{
		if ((exc=GetHashIndex(pszKey, ulKeyLen, ulAdx)) != EOK)
			return exc;
		LPS2PE	pA=(LPS2PE) m_pAssocs[ulAdx];

		if ((exc=GetS2PE(pszKey, ulKeyLen, pVal, pE)) != EOK)
			return exc;

		pE->pNext = pA;
		if (pA != NULL)
			pA->pPrev = pE;
		m_pAssocs[ulAdx] = pE;
		m_ulItemsCount++;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

CStr2PtrMapper::CStr2PtrMapper (const STR2PTRASSOC aList[], const UINT32 ulMapSize, const BOOLEAN fCaseSensitive)
	: m_pAssocs(NULL), m_ulAssocsNum(0UL), m_ulItemsCount(0UL), m_fCase(FALSE)
{
	UINT32	ulMSize=ulMapSize;
	if (0UL == ulMSize)
		ulMSize = CountStr2PtrAssocs(aList);

	EXC_TYPE	exc=InitMap(ulMSize, fCaseSensitive);
	if (EOK == exc)
		exc = Populate(aList);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CStr2PtrMapper::Populate (const STR2PTRASSOC aList[], const UINT32 ulNum)
{
	if ((NULL == aList) && (ulNum != 0UL))
		return EPARAM;

	for (UINT32 i=0UL; i < ulNum; i++)
	{
		const STR2PTRASSOC&	ae=aList[i];
		EXC_TYPE					exc=AddKey(ae.pszKey, (LPVOID) ae.pVal);
		if (exc != EOK)
			return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

// if map size not specified then list size is used
CStr2PtrMapper::CStr2PtrMapper (const STR2PTRASSOC aList[], const UINT32 ulNum, const UINT32 ulMapSize, const BOOLEAN fCaseSensitive)
	: m_pAssocs(NULL), m_ulAssocsNum(0UL), m_ulItemsCount(0UL), m_fCase(FALSE)
{
	UINT32	ulMSize=((0UL == ulMapSize) ? ulNum : ulMapSize);
	EXC_TYPE	exc=InitMap(ulMSize, fCaseSensitive);
	if (EOK == exc)
		exc = Populate(aList, ulNum);
}

/*---------------------------------------------------------------------------*/

// returns associated value before deleting
EXC_TYPE CStr2PtrMapper::RemoveKey (const char pszKey[], const UINT32 ulKeyLen, LPVOID& pVal)
{
	pVal = NULL;

	UINT32	ulAdx=0UL;
	LPVOID	pV=NULL;
	EXC_TYPE	exc=FindKey(pszKey, ulKeyLen, ulAdx, pV);
	if (exc != EOK)
		return exc;

	LPS2PE	pE=(LPS2PE) pV;
	LPS2PE	pNext=pE->pNext, pPrev=pE->pPrev;

	// make necessary overriding connections
	if (pPrev != NULL)
		pPrev->pNext = pNext;
	if (pNext != NULL)
		pNext->pPrev = pPrev;

	if (m_pAssocs[ulAdx] == pE)
		m_pAssocs[ulAdx] = pNext;

	pVal = pE->pVal;
	LPBYTE	pBuf=(LPBYTE) pE;
	delete [] pBuf;

	m_ulItemsCount--;

	return EOK;
}

/*---------------------------------------------------------------------------*/

// if successful (EOK) then returns associated value
EXC_TYPE CStr2PtrMapper::FindKey (const char pszKey[], const UINT32 ulKeyLen, LPVOID& pVal) const
{
	UINT32	ulAdx=0UL;
	LPVOID	pV=NULL;
	EXC_TYPE	exc=FindKey(pszKey, ulKeyLen, ulAdx, pV);
	if (exc != EOK)
		return exc;

	LPS2PE	pE=(LPS2PE) pV;
	pVal = pE->pVal;
	return EOK;
}

/*---------------------------------------------------------------------------*/

// if successful (EOK) then returns internal key value
EXC_TYPE CStr2PtrMapper::GetKey (const char pszKey[], const UINT32 ulKeyLen, LPCTSTR& lpszKey) const
{
	UINT32	ulAdx=0UL;
	LPVOID	pV=NULL;
	EXC_TYPE	exc=FindKey(pszKey, ulKeyLen, ulAdx, pV);
	if (exc != EOK)
		return exc;

	LPS2PE	pE=(LPS2PE) pV;
	lpszKey = pE->szKey;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CStr2PtrMapper::EnumKeys (STR2PTR_ASSOC_ENUM lpfnEcfn, LPVOID pArg) const
{
	if (NULL == lpfnEcfn)
		return EPARAM;

	BOOLEAN	fCont=TRUE;
	for (UINT32 i=0UL; (fCont) && (i < m_ulAssocsNum); i++)
	{
		LPS2PE pE=(LPS2PE) m_pAssocs[i];

		while (fCont && (pE != NULL))
		{
			LPS2PE	pNext=pE->pNext;
			fCont = (*lpfnEcfn)(pE->szKey, pE->pVal, pArg);
			pE = pNext;
		}
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

typedef struct {
	CStr2PtrMapper	*pDst;
	EXC_TYPE			exc;
} PMRGARGS, *LPPMRGARGS;

static BOOLEAN pmrgCfn (const char pszKey[], LPVOID pVal, LPVOID pArg)
{
	LPPMRGARGS	pMA=(LPPMRGARGS) pArg;
	if (NULL == pMA)
		return FALSE;

	CStr2PtrMapper	*pDst=pMA->pDst;
	if (NULL == pDst)
		return FALSE;

	pMA->exc = pDst->AddKey(pszKey, pVal);
	return (EOK == pMA->exc);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CStr2PtrMapper::Merge (const CStr2PtrMapper& m)
{
	PMRGARGS	mrga={ this, EOK };
	EXC_TYPE	exc=m.EnumKeys(pmrgCfn, (LPVOID) &mrga);
	if (EOK == exc)
		exc = mrga.exc;
	return exc;
}

///////////////////////////////////////////////////////////////////////////////

CStr2PtrMapEnum::CStr2PtrMapEnum (const CStr2PtrMapper& mapper)
 : m_Mapper(mapper), m_pCurE(NULL), m_ulAdx(0)
{
}

/*---------------------------------------------------------------------------*/

// returns EEOF if no more items
EXC_TYPE CStr2PtrMapEnum::GetFirst (LPCTSTR& lpszKey, LPVOID& pVal)
{
	lpszKey = NULL;
	pVal = NULL;

	for (m_pCurE=NULL, m_ulAdx=0; m_ulAdx < m_Mapper.m_ulAssocsNum; m_ulAdx++)
		if ((m_pCurE=m_Mapper.m_pAssocs[m_ulAdx]) != NULL)
			break;

	if (NULL == m_pCurE)
		return EEOF;

	LPS2PE	pE=(LPS2PE) m_pCurE;
	lpszKey = pE->szKey;
	pVal = pE->pVal;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CStr2PtrMapEnum::GetNext (LPCTSTR& lpszKey, LPVOID& pVal)
{
	lpszKey = NULL;
	pVal = NULL;

	if (NULL == m_pCurE)
		return EEOF;

	// check if have next in chain
	LPS2PE	pE=(LPS2PE) m_pCurE;
	pE = pE->pNext;
	m_pCurE = (LPVOID) pE;

	// if not have next in chain look for next association
	if (NULL == m_pCurE)
	{
		if (m_ulAdx < m_Mapper.m_ulAssocsNum)
			m_ulAdx++;

		for (m_pCurE=NULL ; m_ulAdx < m_Mapper.m_ulAssocsNum; m_ulAdx++)
			if ((m_pCurE=m_Mapper.m_pAssocs[m_ulAdx]) != NULL)
				break;

		if (NULL == m_pCurE)
			return EEOF;
	
		pE = (LPS2PE) m_pCurE;
	}

	lpszKey = pE->szKey;
	pVal = pE->pVal;
	return EOK;
}

///////////////////////////////////////////////////////////////////////////////

/* returns 0 if s2pa is NULL as well */
UINT32 CountStr2StrAssocs (const STR2STRASSOC s2sa[])
{
	UINT32	ulANum=0;

	if (0 == s2sa)
		return 0;

	for (ulANum=0; ; ulANum++)
		if (IsEmptyStr(s2sa[ulANum].pszKey))
			return ulANum;

	return ((UINT32) (-1));	/* should not be reached !!! */
}

/*--------------------------------------------------------------------------*/

// if map size not specified then list size is used
CStr2StrMapper::CStr2StrMapper (const STR2STRASSOC aList[], const UINT32 ulMapSize, const BOOLEAN fCaseSensitive)
	: m_Mapper()
{
	UINT32	ulMSize=ulMapSize;
	if (0UL == ulMSize)
		ulMSize = CountStr2StrAssocs(aList);

	EXC_TYPE	exc=InitMap(ulMSize, fCaseSensitive);
	if (EOK == exc)
		exc = Populate(aList);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CStr2StrMapper::Populate (const STR2STRASSOC aList[], const UINT32 ulNum)
{
	if ((NULL == aList) && (ulNum != 0UL))
		return EPARAM;

	for (UINT32 i=0UL; i < ulNum; i++)
	{
		const STR2STRASSOC&	ae=aList[i];
		EXC_TYPE					exc=AddKey(ae.pszKey, ae.pszVal);
		if (exc != EOK)
			return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

// if map size not specified then list size is used
CStr2StrMapper::CStr2StrMapper (const STR2STRASSOC aList[], const UINT32 ulNum, const UINT32 ulMapSize, const BOOLEAN fCaseSensitive)
	: m_Mapper(((0UL == ulMapSize) ? ulNum : ulMapSize), fCaseSensitive)
{
	Populate(aList, ulNum);
}

/*---------------------------------------------------------------------------*/

typedef struct {
	UINT32	ulMaxLen;
	char		szVal[2];	// actual size is dynamically allocated
} S2SE, *LPS2SE;

static EXC_TYPE GetS2SE (const char pszVal[], const UINT32 ulVLen, LPS2SE& pE)
{
	pE = NULL;

	LPBYTE	pBuf=new BYTE[(sizeof *pE) + ulVLen];
	if (NULL == pBuf)
		return EMEM;

	pE = (LPS2SE) pBuf;
	memset(pE, 0, (sizeof *pE));

	if ((pE->ulMaxLen=ulVLen) > 0)
	{
		strncpy(pE->szVal, pszVal, ulVLen);
		pE->szVal[ulVLen] = '\0';
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

static BOOLEAN clrCfn (const char pszKey[], LPVOID	/* pVal */, LPVOID pArg)
{
	CStr2StrMapper	*pMP=(CStr2StrMapper *) pArg;
	if (NULL == pMP)
		return FALSE;

	pMP->RemoveKey(pszKey);
	return TRUE;
}

// removes all associations but not the internal hash table
void CStr2StrMapper::Reset ()
{
	m_Mapper.EnumKeys(clrCfn, (LPVOID) this);
	m_Mapper.Reset();
}

/*---------------------------------------------------------------------------*/

// removes all associations and the internal has table itself.
void CStr2StrMapper::Clear ()
{
	Reset();
	m_Mapper.Clear();
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CStr2StrMapper::AddKey (const char		pszKey[],
											const UINT32	ulKeyLen,
											const char		pszVal[],
											const UINT32	ulVLen)
{
	if (IsEmptyStr(pszKey) || (0 == ulKeyLen))
		return EEMPTYENTRY;

	LPVOID	pV=NULL;
	EXC_TYPE	exc=m_Mapper.FindKey(pszKey, ulKeyLen, pV);
	LPS2SE	pE=(LPS2SE) pV;

	if (EOK == exc)
	{
		if (ulVLen <= pE->ulMaxLen)
		{
			if (ulVLen != 0)
			{
				strncpy(pE->szVal, pszVal, ulVLen);
				pE->szVal[ulVLen] = '\0';
			}
			else
				pE->szVal[0] = '\0';

			return EOK;
		}

		// have to replace the value since it is larger
		LPS2SE	pNE=NULL;
		if ((exc=GetS2SE(pszVal, ulVLen, pNE)) != EOK)
			return exc;

		LPBYTE	pBuf=NULL;
		if ((exc=m_Mapper.AddKey(pszKey, ulKeyLen, (LPVOID) pNE)) != EOK)
			pBuf = (LPBYTE) pNE;
		else // succeeded so delete previous value
			pBuf = (LPBYTE) pE;

		delete [] pBuf;
	}
	else	// new item
	{
		if ((exc=GetS2SE(pszVal, ulVLen, pE)) != EOK)
			return exc;

		if ((exc=m_Mapper.AddKey(pszKey, ulKeyLen, (LPVOID) pE)) != EOK)
		{
			LPBYTE	pBuf=(LPBYTE) pE;
			delete [] pBuf;
		}
	}

	return exc;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CStr2StrMapper::AccumulateKey (const char pszKey[], const UINT32 ulKeyLen, const char pszVal[], const UINT32 ulMaxLen)
{
	if (IsEmptyStr(pszKey) || (0 == ulKeyLen))
		return EEMPTYENTRY;

	LPVOID	pV=NULL;
	EXC_TYPE	exc=m_Mapper.FindKey(pszKey, ulKeyLen, pV);
	if (exc != EOK)	// if not found, then same as simple add
		return AddKey(pszKey, ulKeyLen, pszVal, ulMaxLen);

	LPS2SE			pE=(LPS2SE) pV, pNE=NULL;
	const UINT32	ulNewLen=(pE->ulMaxLen + ulMaxLen);

	// allocate a new (larger) entry
	if ((exc=GetS2SE(pE->szVal, ulNewLen, pNE)) != EOK)
		return exc;

	// the new value should appear immediately after
	LPTSTR	lpszAccVal=&pNE->szVal[pE->ulMaxLen];
	::_tcsncpy(lpszAccVal, pszVal, ulMaxLen);
	lpszAccVal[ulMaxLen] = _T('\0');

	LPBYTE	pBuf=NULL;
	if ((exc=m_Mapper.AddKey(pszKey, ulKeyLen, (LPVOID) pNE)) != EOK)
		pBuf = (LPBYTE) pNE;
	else // succeeded so delete previous value
		pBuf = (LPBYTE) pE;

	delete [] pBuf;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CStr2StrMapper::AccumulateNumKey (const char pszKey[], const UINT32 ulKeyLen, const UINT32 ulVal)
{
	char		szVal[MAX_DWORD_DISPLAY_LENGTH+2]="";
	size_t	vLen=dword_to_argument(ulVal, szVal);

	return AccumulateKey(pszKey, ulKeyLen, szVal, vLen);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CStr2StrMapper::AddNumKey (const char pszKey[], const UINT32 ulKeyLen, const UINT32 ulVal)
{
	char		szVal[MAX_DWORD_DISPLAY_LENGTH+2]="";
	size_t	vLen=dword_to_argument(ulVal, szVal);

	return AddKey(pszKey, ulKeyLen, szVal, vLen);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CStr2StrMapper::RemoveKey (const char pszKey[], const UINT32 ulKeyLen)
{
	LPVOID	pV=NULL;
	EXC_TYPE	exc=m_Mapper.RemoveKey(pszKey, ulKeyLen, pV);
	if (exc != EOK)
		return exc;

	LPS2SE	pE=(LPS2SE) pV;
	LPBYTE	pBuf=(LPBYTE) pE;
	delete [] pBuf;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CStr2StrMapper::FindKey (const char pszKey[], const UINT32 ulKeyLen, LPCTSTR& lppVal) const
{
	lppVal = NULL;

	LPVOID	pV=NULL;
	EXC_TYPE	exc=m_Mapper.FindKey(pszKey, ulKeyLen, pV);
	if (exc != EOK)
		return exc;

	LPS2SE	pE=(LPS2SE) pV;
	lppVal = pE->szVal;
	return EOK;
}

/*---------------------------------------------------------------------------*/

// Note: may return error if string found but cannot be translated to number
EXC_TYPE CStr2StrMapper::FindNumKey (const char pszKey[], const UINT32 ulKeyLen, UINT32& ulVal) const
{
	ulVal = 0;

	LPCTSTR		lpVal=NULL;
	EXC_TYPE		exc=FindKey(pszKey, ulKeyLen, lpVal);
	if (exc != EOK)
		return exc;

	if (NULL == lpVal)
		return EBADADDR;

	ulVal = argument_to_dword(lpVal, GetSafeStrlen(lpVal), EXC_ARG(exc));
	return exc;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CStr2StrMapper::FindKey (const char pszKey[], const UINT32 ulKeyLen, char lpszVal[], const UINT32 ulMaxLen) const
{
	if ((NULL == lpszVal) || (0UL == ulMaxLen))
		return EPARAM;
	*lpszVal = '\0';

	LPCTSTR		lppVal=NULL;
	EXC_TYPE		exc=FindKey(pszKey, ulKeyLen, lppVal);
	if (exc != EOK)
		return exc;

	UINT32	ulVLen=GetSafeStrlen(lppVal);
	if (ulVLen >= ulMaxLen)
		return EOVERFLOW;

	strcpy(lpszVal, lppVal);
	return EOK;
}

/*---------------------------------------------------------------------------*/

typedef struct {
	STR2STR_ASSOC_ENUM	lpfnEcfn;
	LPVOID					pArg;
} EKARGS, *LPEKARGS;

static BOOLEAN ekCfn (const char	pszKey[], LPVOID	pVal, LPVOID pArg)
{
	LPEKARGS	pEK=(LPEKARGS) pArg;
	if ((NULL == pEK) || (NULL == pVal))
		return FALSE;

	LPS2SE					pE=(LPS2SE) pVal;
	STR2STR_ASSOC_ENUM	lpfnEcfn=pEK->lpfnEcfn;
	if (NULL == lpfnEcfn)
		return FALSE;

	return (*lpfnEcfn)(pszKey, pE->szVal, pEK->pArg);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CStr2StrMapper::EnumKeys (STR2STR_ASSOC_ENUM lpfnEcfn, LPVOID pArg) const
{
	if (NULL == lpfnEcfn)
		return EPARAM;

	EKARGS	eka;
	memset(&eka, 0, (sizeof eka));
	eka.lpfnEcfn = lpfnEcfn;
	eka.pArg = pArg;

	return m_Mapper.EnumKeys(ekCfn, (LPVOID) &eka);
}

/*---------------------------------------------------------------------------*/

typedef struct {
	CStr2StrMapper	*pDst;
	EXC_TYPE			exc;
} SMRGARGS, *LPSMRGARGS;

static BOOLEAN smrgCfn (const char pszKey[], const char pszVal[], LPVOID pArg)
{
	LPSMRGARGS	pMS=(LPSMRGARGS) pArg;
	if (NULL == pMS)
		return FALSE;

	CStr2StrMapper	*pDst=pMS->pDst;
	if (NULL == pDst)
	{
		pMS->exc = ECONTEXT;
		return FALSE;
	}

	pMS->exc = pDst->AddKey(pszKey, pszVal);
	return (EOK == pMS->exc);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CStr2StrMapper::Merge (const CStr2StrMapper& m)
{
	SMRGARGS	mrgs={ this, EOK };
	EXC_TYPE	exc=m.EnumKeys(smrgCfn, (LPVOID) &mrgs);
	if (EOK == exc)
		exc = mrgs.exc;
	return exc;
}

///////////////////////////////////////////////////////////////////////////////

CStr2StrMapEnum::CStr2StrMapEnum (const CStr2StrMapper& mapper)
	: m_s2pe(mapper.m_Mapper)
{
}

/*---------------------------------------------------------------------------*/

// returns EEOF if no more items
EXC_TYPE CStr2StrMapEnum::GetFirst (LPCTSTR& lpszKey, LPCTSTR& lpszVal)
{
	lpszVal = NULL;

	LPVOID	pV=NULL;
	EXC_TYPE	exc=m_s2pe.GetFirst(lpszKey, pV);
	if (exc != EOK)
		return exc;

	lpszVal = ((LPS2SE) pV)->szVal;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CStr2StrMapEnum::GetNext (LPCTSTR& lpszKey, LPCTSTR& lpszVal)
{
	lpszVal = NULL;

	LPVOID	pV=NULL;
	EXC_TYPE	exc=m_s2pe.GetNext(lpszKey, pV);
	if (exc != EOK)
		return exc;

	lpszVal = ((LPS2SE) pV)->szVal;
	return EOK;
}

///////////////////////////////////////////////////////////////////////////////

UINT32 CountPtr2PtrAssocs (const PTR2PTRASSOC aList[])
{
	if (NULL == aList)
		return 0;

	for (UINT32 i=0UL; ; i++)
	{
		const PTR2PTRASSOC& ps2p=aList[i];
		if (NULL == ps2p.lpKey)
			return i;
	}

	// should not be reached
	return ((UINT32) (-1));
}

/*---------------------------------------------------------------------------*/

#define P2PKEY_PREFIX_LEN	2
#define P2PKEYPREFIXSTR		"0x"
#define P2PKEYLEN	(MAX_DWORD_HEX_DISPLAY_LENGTH+P2PKEY_PREFIX_LEN)

static EXC_TYPE BuildP2PKey (LPVOID lpKey, char szKey[], const UINT32 ulMaxLen)
{
	if ((NULL == szKey) || (0 == ulMaxLen))
		return EBADBUFF;

	char		*lsp=szKey;
	UINT32	ulRemLen=ulMaxLen;
	EXC_TYPE	exc=strlinsstr(&lsp, P2PKEYPREFIXSTR, &ulRemLen);
	if (exc != EOK)
		return exc;

	if (ulRemLen <= MAX_DWORD_HEX_DISPLAY_LENGTH)
		return EOVERFLOW;

	lsp += dword_to_hex_argument((DWORD) lpKey, lsp);
	return EOK;
}

static EXC_TYPE DecodeP2PKey (const char szKey[], const UINT32 ulKeyLen, LPVOID& pKey)
{
	pKey = NULL;

	if (IsEmptyStr(szKey) || (ulKeyLen != P2PKEYLEN))
		return EUDFFORMAT;
	if (_tcsnicmp(szKey, P2PKEYPREFIXSTR, P2PKEY_PREFIX_LEN) != 0)
		return ECONTEXT;

	EXC_TYPE	exc=EOK;
	pKey = (LPVOID) hex_argument_to_dword(&szKey[P2PKEY_PREFIX_LEN], ulKeyLen - P2PKEY_PREFIX_LEN, EXC_ARG(exc));
	if (exc != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CPtr2PtrMapper::AddKey (LPVOID lpKey, LPVOID lpVal)
{
	char		szKey[P2PKEYLEN+2]="";
	EXC_TYPE	exc=BuildP2PKey(lpKey, szKey, P2PKEYLEN+1);
	if (EOK == exc)
		exc = m_Mapper.AddKey(szKey, lpVal);

	return exc;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CPtr2PtrMapper::RemoveKey (LPVOID lpKey)
{
	char		szKey[P2PKEYLEN+2]="";
	EXC_TYPE	exc=BuildP2PKey(lpKey, szKey, P2PKEYLEN+1);
	if (EOK == exc)
		exc = m_Mapper.RemoveKey(szKey);

	return exc;
}

/*---------------------------------------------------------------------------*/

// if successful (EOK) then returns associated value
EXC_TYPE CPtr2PtrMapper::FindKey (LPVOID lpKey, LPVOID& lppVal) const
{
	lppVal = NULL;

	char		szKey[P2PKEYLEN+2]="";
	EXC_TYPE	exc=BuildP2PKey(lpKey, szKey, P2PKEYLEN+1);
	if (EOK == exc)
		exc = m_Mapper.FindKey(szKey, lppVal);

	return exc;
}

/*---------------------------------------------------------------------------*/

// if map size not specified then list size is used
CPtr2PtrMapper::CPtr2PtrMapper (const PTR2PTRASSOC aList[], const UINT32 ulMapSize)
	: m_Mapper()
{
	UINT32	ulMSize=ulMapSize;
	if (0UL == ulMSize)
		ulMSize = CountPtr2PtrAssocs(aList);

	EXC_TYPE	exc=InitMap(ulMSize);
	if (EOK == exc)
		exc = Populate(aList);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CPtr2PtrMapper::Populate (const PTR2PTRASSOC aList[], const UINT32 ulNum)
{
	if ((NULL == aList) && (ulNum != 0UL))
		return EPARAM;

	for (UINT32 i=0UL; i < ulNum; i++)
	{
		const PTR2PTRASSOC&	ae=aList[i];
		EXC_TYPE					exc=AddKey((LPVOID) ae.lpKey, (LPVOID) ae.lpVal);
		if (exc != EOK)
			return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

// if map size not specified then list size is used
CPtr2PtrMapper::CPtr2PtrMapper (const PTR2PTRASSOC aList[], const UINT32 ulNum, const UINT32 ulMapSize)
	: m_Mapper(((0UL == ulMapSize) ? ulNum : ulMapSize), FALSE)
{
	Populate(aList, ulNum);
}

/*---------------------------------------------------------------------------*/

typedef struct {
	PTR2PTR_ASSOC_ENUM	lpfnEcfn;
	LPVOID					pArg;
	EXC_TYPE					exc;
} P2PEA, *LPP2PEA;

static BOOLEAN p2pEcfn (const char	pszKey[], LPVOID pVal, LPVOID	pArg)
{
	LPP2PEA	p2EA=(LPP2PEA) pArg;
	if (NULL == p2EA)
		return FALSE;

	EXC_TYPE&	exc=p2EA->exc;

	PTR2PTR_ASSOC_ENUM	lpfnEcfn=p2EA->lpfnEcfn;
	if (NULL == lpfnEcfn)
	{
		exc = EBADADDR;
		return FALSE;
	}

	if (IsEmptyStr(pszKey))
	{
		exc = ECONTEXT;
		return FALSE;
	}

	DWORD	dwVal=arg2dw(pszKey, strlen(pszKey), EXC_ARG(exc));
	if (exc != EOK)
		return FALSE;

	return (*lpfnEcfn)((LPVOID) dwVal, pVal, p2EA->pArg);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CPtr2PtrMapper::EnumKeys (PTR2PTR_ASSOC_ENUM lpfnEcfn, LPVOID pArg) const
{
	if (NULL == lpfnEcfn)
		return EBADADDR;

	P2PEA	p2pa={ lpfnEcfn, pArg, EOK };
	EXC_TYPE	exc=m_Mapper.EnumKeys(p2pEcfn, (LPVOID) &p2pa);
	if (EOK == exc)
		exc = p2pa.exc;

	return exc;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CPtr2PtrMapper::Merge (const CPtr2PtrMapper& m)
{
	CPtr2PtrMapEnum	mme(m);
	LPVOID				pKey=NULL, pVal=NULL;
	for (EXC_TYPE	exc=mme.GetFirst(pKey, pVal); EOK == exc; exc=mme.GetNext(pKey, pVal))
		if ((exc=AddKey(pKey, pVal)) != EOK)
			return exc;

	return EOK;
}

///////////////////////////////////////////////////////////////////////////////

CPtr2PtrMapEnum::CPtr2PtrMapEnum (const CPtr2PtrMapper& mapper)
	: m_s2pe(mapper.m_Mapper)
{
}

// returns EEOF if no more items
EXC_TYPE CPtr2PtrMapEnum::GetFirst (LPVOID& pKey, LPVOID& pVal)
{
	pKey = NULL;

	LPCTSTR	lpszKey=NULL;
	LPVOID	pV=NULL;
	EXC_TYPE	exc=m_s2pe.GetFirst(lpszKey, pVal);
	if (exc != EOK)
		return exc;

	if ((exc=::DecodeP2PKey(GetSafeStrPtr(lpszKey), GetSafeStrlen(lpszKey), pKey)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

// returns EEOF if no more items
EXC_TYPE CPtr2PtrMapEnum::GetNext (LPVOID& pKey, LPVOID& pVal)
{
	pKey = NULL;

	LPCTSTR	lpszKey=NULL;
	LPVOID	pV=NULL;
	EXC_TYPE	exc=m_s2pe.GetNext(lpszKey, pVal);
	if (exc != EOK)
		return exc;

	if ((exc=::DecodeP2PKey(GetSafeStrPtr(lpszKey), GetSafeStrlen(lpszKey), pKey)) != EOK)
		return exc;

	return EOK;
}

///////////////////////////////////////////////////////////////////////////////

CStr2StructMapper::CStr2StructMapper ()
	: m_dColl(), m_dMap()
{
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CStr2StructMapper::InitMap (const UINT32	ulMapSize,
												 const BOOLEAN	fCaseSensitive,
												 const UINT32	ulIDtSize,
												 const UINT32	ulIDtGrow)
{
	if ((m_dMap.GetSize() != 0) || (m_dColl.GetSize() != 0))
		return EEXIST;

	EXC_TYPE	exc=m_dMap.InitMap(ulMapSize, fCaseSensitive);
	if (exc != EOK)
		return exc;

	if ((exc=m_dColl.SetParams(ulIDtSize, ulIDtGrow)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

CStr2StructMapper::CStr2StructMapper (const UINT32		ulMapSize,
												  const BOOLEAN	fCaseSensitive,
												  const UINT32		ulIDtSize,
												  const UINT32		ulIDtGrow)
	: m_dColl(), m_dMap()
{
	EXC_TYPE	exc=InitMap(ulMapSize, fCaseSensitive, ulIDtSize, ulIDtGrow);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CStr2StructMapper::AddItem (LPCTSTR lpszItemName, const UINT32 ulINLen, const LPVOID pVal, const UINT32 ulVSize /* cannot be zero */)
{
	if ((0 == ulVSize) || (NULL == pVal) || IsEmptyStr(lpszItemName) || (0 == ulINLen))
		return EPARAM;

	EXC_TYPE	exc=m_dColl.AddItem(pVal, ulVSize);
	if (exc != EOK)
		return exc;

	const UINT32	ulVIndex=m_dColl.GetSize()-1;
	LPVOID			pCVal=NULL;
	if ((exc=m_dColl.GetData(ulVIndex, pCVal)) != EOK)
		return exc;
	if (NULL == pCVal)
		return ESTATE;

	if ((exc=m_dMap.AddKey(lpszItemName, ulINLen, pCVal)) != EOK)
	{
		EXC_TYPE	dErr=m_dColl.RemoveItem(ulVIndex);
		return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CStr2StructMapper::RemoveItem (LPCTSTR lpszItemName, const UINT32 ulINLen)
{
	LPVOID	pVIndex=NULL;
	EXC_TYPE	exc=m_dMap.FindKey(lpszItemName, ulINLen, pVIndex);
	if (exc != EOK)
		return exc;

	EXC_TYPE	mErr=m_dMap.RemoveKey(lpszItemName, ulINLen), dErr=m_dColl.RemoveItem(pVIndex);
	if (mErr != EOK)
		exc = mErr;
	else if (dErr != EOK)
		exc = dErr;
	else
		exc = EOK;

	return exc;
}

/*---------------------------------------------------------------------------*/

// removes contents but mapper remains initialized
EXC_TYPE CStr2StructMapper::Reset ()
{
	m_dMap.Reset();
	return m_dColl.Reset();
}

/*---------------------------------------------------------------------------*/

// Note: requires re-initialization after this call
EXC_TYPE CStr2StructMapper::Clear ()
{
	m_dMap.Clear();
	return m_dColl.Clear();
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CStr2StructMapper::Merge (const CStr2StructMapper& m)
{
	CStr2PtrMapEnum			nme(m.m_dMap);
	const CVSDCollection&	newColl=m.m_dColl;
	LPCTSTR						lpszItemName=NULL;
	LPVOID						pNewVal=NULL;

	for (EXC_TYPE	exc=nme.GetFirst(lpszItemName, pNewVal); EOK == exc; exc=nme.GetNext(lpszItemName, pNewVal))
	{
		UINT32	ulNIndex=0;
		if ((exc=newColl.GetItemIndex(pNewVal, ulNIndex)) != EOK)
			return exc;

		UINT32	ulNewLen=0;
		if ((exc=newColl.GetData(ulNIndex, pNewVal, ulNewLen)) != EOK)
			return exc;

		// if new item then no problem - if existing one, then remove it
		LPVOID	pOldVal=NULL;
		if (EOK == (exc=m_dMap.FindKey(lpszItemName, pOldVal)))
		{
			if ((exc=RemoveItem(lpszItemName)) != EOK)
				return exc;
		}

		if ((exc=AddItem(lpszItemName, pNewVal, ulNewLen)) != EOK)
			return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

CStr2StructMapper::CStr2StructMapper (const CStr2StructMapper& m)
	: m_dColl(m.m_dColl.GetSize(), m.m_dColl.GetGrowFactor()), m_dMap(m.m_dMap.GetSize(), m.m_dMap.IsCaseSensitive())
{
	EXC_TYPE	exc=Merge(m);
}

/*---------------------------------------------------------------------------*/

// Note: resets the contents first
CStr2StructMapper& CStr2StructMapper::operator= (const CStr2StructMapper& m)
{
	EXC_TYPE	exc=Reset();
	exc = Merge(m);
	return *this;
}

LPVOID CStr2StructMapper::operator[] (LPCTSTR lpszItemName) const
{
	LPVOID	pVal=NULL;
	EXC_TYPE	exc=m_dMap.FindKey(lpszItemName, pVal);
	if (exc != EOK)
		return NULL;
	else
		return pVal;
}

///////////////////////////////////////////////////////////////////////////////
