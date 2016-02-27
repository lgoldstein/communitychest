#include <internet/rfc822.h>

/*---------------------------------------------------------------------------*/

void CAttachInfo::Cleanup ()
{
	strreleasebuf(m_lpszPath);
	strreleasebuf(m_lpszType);
	strreleasebuf(m_lpszSubType);
	strreleasebuf(m_lpszSuperType);
	strreleasebuf(m_lpszSuperSubType);
	strreleasebuf(m_lpszID);
	strreleasebuf(m_lpszDesc);

	m_CDispProps.Reset();
	m_CTypeProps.Reset();

	m_ulSize = ((UINT32) (-1));
	m_ulDuration = 0;
	m_attEnc = RFC822_NONE_ENC;
	m_fIsVirtualName = FALSE;
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE SetStr (LPTSTR& lpszDst, LPCTSTR lpszSrc, UINT32&	ulSLen)
{
	lpszDst = NULL;
	ulSLen = 0UL;

	if (IsEmptyStr(lpszSrc))
		return EEMPTYENTRY;

	ulSLen = _tcslen(lpszSrc);
	if (NULL == (lpszDst=new TCHAR[ulSLen + 2UL]))
		return EMEM;

	_tcscpy(lpszDst, lpszSrc);
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CAttachInfo::UpdateStr (LPTSTR& lpszDst, LPCTSTR lpszSrc)
{
	LPTSTR	lpszD=NULL;

	if (!IsEmptyStr(lpszSrc))
	{
		UINT32	ulSLen=0;
		EXC_TYPE	exc=SetStr(lpszD, lpszSrc, ulSLen);
		if (exc != EOK)
			return exc;
	}

	strreleasebuf(lpszDst);
	lpszDst = lpszD;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CAttachInfo::SetName (LPCTSTR lpszName, const BOOLEAN fIsVirtual)
{
	EXC_TYPE	exc=EOK;

	if (IsEmptyStr(lpszName))
	{
		exc = m_CTypeProps.RemoveKey(pszMIMENameKeyword);
		exc = m_CDispProps.RemoveKey(pszMIMEFilenameKeyword);
		return EOK;
	}

	if ((exc=m_CTypeProps.AddKey(pszMIMENameKeyword, lpszName)) != EOK)
		return exc;
	if ((exc=m_CDispProps.AddKey(pszMIMEFilenameKeyword, lpszName)) != EOK)
		return exc;

	SetVirtualNameState(fIsVirtual);
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CAttachInfo::UpdateName (LPCTSTR lpszName, const BOOLEAN fIsVirtual)
{
	LPCTSTR	lpszThisName=GetName();

	if (IsEmptyStr(lpszThisName))
		return SetName(lpszName, fIsVirtual);
	else
		return EOK;
}

/*---------------------------------------------------------------------------*/

LPCTSTR CAttachInfo::GetAttProp (const CStr2StrMapper& aProp, LPCTSTR lpszPropName)
{
	LPCTSTR	lpszPropVal=NULL;
	EXC_TYPE	exc=aProp.FindKey(lpszPropName, lpszPropVal);
	if (exc != EOK)
		return NULL;
	else
		return lpszPropVal;
}

/*---------------------------------------------------------------------------*/

LPCTSTR CAttachInfo::GetName () const
{
	LPCTSTR	lpszThisName=GetAttProp(m_CTypeProps, pszMIMENameKeyword);
	if (IsEmptyStr(lpszThisName))
		lpszThisName = GetAttProp(m_CDispProps, pszMIMEFilenameKeyword);

	return lpszThisName;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CAttachInfo::SetCharSet (LPCTSTR lpszCharset)
{
	EXC_TYPE	exc=EOK;

	if (IsEmptyStr(lpszCharset))
	{
		exc = m_CTypeProps.RemoveKey(pszMIMECharsetKeyword);
		return EOK;
	}

	if ((exc=m_CTypeProps.AddKey(pszMIMECharsetKeyword, lpszCharset)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CAttachInfo::UpdatePropVal (CStr2StrMapper& aProp, LPCTSTR lpszPropName, LPCTSTR lpszPropVal)
{
	LPCTSTR	lpszThisValue=NULL;
	EXC_TYPE	exc=aProp.FindKey(lpszPropName, lpszThisValue);
	if ((EOK == exc) && (!IsEmptyStr(lpszThisValue)))
		return exc;

	if ((exc=aProp.AddKey(lpszPropName, lpszPropVal)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CAttachInfo::SetEncoding (const RFC822ENCCASE eEncCase)
{
	if (fIsBadRFC822EncCase(eEncCase))
		return ETYPE;

	m_attEnc = eEncCase;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CAttachInfo::SetNumProp (LPCTSTR lpszVal, UINT32& ulVal)
{
	EXC_TYPE	exc=EOK;

	ulVal = argument_to_dword(lpszVal, GetSafeStrlen(lpszVal), EXC_ARG(exc));
	if (exc != EOK)
	{
		ulVal = (UINT32) (-1);
		return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CAttachInfo::SetContentType (LPCTSTR lpszType, LPCTSTR lpszSubType)
{
	if (IsEmptyStr(lpszType) || IsEmptyStr(lpszSubType))
		return EPARAM;

	EXC_TYPE	exc=EOK;

	if ((exc=UpdateSuperMIMEType(m_lpszType)) != EOK)
		return exc;
	if ((exc=UpdateStr(m_lpszType, lpszType)) != EOK)
		return exc;

	if ((exc=UpdateSuperMIMESubType(m_lpszSubType)) != EOK)
		return exc;
	if ((exc=UpdateStr(m_lpszSubType, lpszSubType)) != EOK)
		return exc;

	return exc;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CAttachInfo::UpdateContentType (LPCTSTR lpszType, LPCTSTR lpszSubType)
{
	if (IsEmptyStr(lpszType) || IsEmptyStr(lpszSubType))
		return EPARAM;

	EXC_TYPE	exc=ReInitStr(m_lpszType, lpszType);
	if (EOK == exc)
		exc = ReInitStr(m_lpszSubType, lpszSubType);

	return exc;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CAttachInfo::InitContentType (LPCTSTR lpszMIMEType, const BOOLEAN fUpdate)
{
	if (IsEmptyStr(lpszMIMEType))
		return EPARAM;

	LPCTSTR	lpszMTD=_tcschr(lpszMIMEType, RFC822_MIMETAG_SEP);
	if (NULL == lpszMTD)
		return ENOTPRESENT;

	*((LPTSTR) lpszMTD) = _T('\0');
	EXC_TYPE	exc=(fUpdate ? UpdateContentType(lpszMIMEType, (lpszMTD+1)) : SetContentType(lpszMIMEType, (lpszMTD+1)));
	*((LPTSTR) lpszMTD) = RFC822_MIMETAG_SEP;

	return exc;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CAttachInfo::SetInfo (LPCTSTR					lpszName,
										 const UINT32			ulSize,	// may be zero
										 LPCTSTR					lpszPath, // may be NULL/empty
										 LPCTSTR					lpszType,
										 LPCTSTR					lpszSubType,
										 LPCTSTR					lpszCharSet,	// may be NULL/empty
										 const RFC822ENCCASE	attEnc,
										 LPCTSTR					lpszDesc,
										 LPCTSTR					lpszID)
{
	Cleanup();

	EXC_TYPE	exc=EOK;
	if (EOK == exc)
		exc = SetName(lpszName, FALSE);

	if (EOK == exc)
		exc = SetPath(lpszPath);

	if (EOK == exc)
		exc = SetContentType(lpszType, lpszSubType);

	if (EOK == exc)
		exc = SetCharSet(lpszCharSet);

	if (EOK == exc)
		exc = SetEncoding(attEnc);

	if (EOK == exc)
		exc = SetDescription(lpszDesc);

	if (EOK == exc)
		exc = SetID(lpszID);

	if (exc != EOK)
		Cleanup();
	else
		m_ulSize = ulSize;

	return exc;
}

/*---------------------------------------------------------------------------*/

// also default constructor
CAttachInfo::CAttachInfo (const UINT32 ulAvgAttProps)
	: m_lpszPath(NULL), m_lpszDesc(NULL), m_lpszID(NULL),
	  m_lpszType(NULL), m_lpszSubType(NULL),
	  m_lpszSuperType(NULL), m_lpszSuperSubType(NULL),
	  m_attEnc(RFC822_NONE_ENC), m_ulSize((UINT32) (-1)), m_ulDuration(0),
	  m_CDispProps(ulAvgAttProps,FALSE), m_CTypeProps(ulAvgAttProps,FALSE),
	  m_fIsVirtualName(FALSE)
{
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CAttachInfo::AddPropPair (CStr2StrMapper& aMap, LPCTSTR lpszPropPair)
{
	if (IsEmptyStr(lpszPropPair))
		return EEMPTYENTRY;

	LPCTSTR	lpszPropVal=_tcschr(lpszPropPair, RFC822_KEYWORD_VALUE_DELIM);
	if (NULL != lpszPropVal)
	{
		*((LPTSTR) lpszPropVal) = _T('\0');
		EXC_TYPE	exc=aMap.AddKey(lpszPropPair, (lpszPropVal+1));
		*((LPTSTR) lpszPropVal) = RFC822_KEYWORD_VALUE_DELIM;

		return exc;
	}

	// if this is a singleton, then add it as such
	return aMap.AddKey(lpszPropPair);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CAttachInfo::UpdateInfo (const CAttachInfo& ai)
{
	EXC_TYPE	exc=UpdateName(ai.GetName(), ai.m_fIsVirtualName);
	if (EOK == exc)
		exc = UpdateStr(m_lpszPath, ai.m_lpszPath);
	if (EOK == exc)
		exc = UpdateStr(m_lpszType,  ai.m_lpszType);
	if (EOK == exc)
		exc = UpdateStr(m_lpszSubType, ai.m_lpszSubType);
	if (EOK == exc)
		exc = UpdateStr(m_lpszSuperType,  ai.m_lpszSuperType);
	if (EOK == exc)
		exc = UpdateStr(m_lpszSuperSubType, ai.m_lpszSuperSubType);
	if (EOK == exc)
		exc = UpdateCharSet(ai.GetCharSet());
	if (EOK == exc)
		exc = UpdateStr(m_lpszDesc, ai.m_lpszDesc);
	if (EOK == exc)
		exc = UpdateStr(m_lpszID, ai.m_lpszID);

	if (EOK == exc)
		exc = AddDispProps(ai.GetDispositionProps());
	if (EOK == exc)
		exc = AddCTypeProps(ai.GetContentTypeProps());

	if (EOK == exc)
	{
		m_ulSize = ai.m_ulSize;
		m_ulDuration = ai.m_ulDuration;
		m_attEnc = ai.m_attEnc;
	}

	return exc;
}

/*---------------------------------------------------------------------------*/

// copy constructor
CAttachInfo::CAttachInfo (const CAttachInfo& ai)
	: m_lpszPath(NULL), m_lpszDesc(NULL), m_lpszID(NULL),
	  m_lpszType(NULL), m_lpszSubType(NULL),
	  m_lpszSuperType(NULL), m_lpszSuperSubType(NULL),
	  m_attEnc(RFC822_NONE_ENC), m_ulSize((UINT32) (-1)), m_ulDuration(0),
	  m_CDispProps((UINT32) 0, FALSE), m_CTypeProps((UINT32) 0, FALSE),
	  m_fIsVirtualName(FALSE)
{
	const CStr2StrMapper&	dp=ai.GetDispositionProps();
	const CStr2StrMapper&	cp=ai.GetContentTypeProps();

	EXC_TYPE	exc1=m_CDispProps.InitMap(dp.GetSize(), FALSE), exc2=m_CTypeProps.InitMap(cp.GetSize(), FALSE);

	UpdateInfo(ai);
}

/*---------------------------------------------------------------------------*/

// Note: pointers stop being valid if object destroyed
EXC_TYPE CAttachInfo::GetInfo (RFC822ATTACHINFO& ai) const
{
	memset(&ai, 0, (sizeof ai));

	ai.m_lpszName = GetName();
	ai.m_fIsVirtualName = m_fIsVirtualName;
	ai.m_attEnc = m_attEnc;
	ai.m_lpszCharSet = GetCharSet();
	ai.m_lpszDesc = m_lpszDesc;
	ai.m_lpszID = m_lpszID;
	ai.m_lpszSubType = m_lpszSubType;
	ai.m_lpszType = m_lpszType;
	ai.m_ulDuration = m_ulDuration;
	ai.m_ulSize = m_ulSize;
	ai.m_pDispProps = &m_CDispProps;
	ai.m_pCTypeProps = &m_CTypeProps;

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

// also default constructor
CAttachSet::CAttachSet (const UINT32 ulInitialNum, const UINT32 ulGrowNum)
{
	if ((m_ulMaxNum=ulInitialNum) != 0)
	{
		if ((m_pAtts=new LPATTINFO[m_ulMaxNum]) != NULL)
		{
			for (UINT32 i=0; i < m_ulMaxNum; i++)
				m_pAtts[i] = NULL;
		}
		else	// cannot allocate pointers
			m_ulMaxNum = 0;
	}
	else	// initially zero size
		m_pAtts = NULL;

	m_ulGrowNum = ulGrowNum;
	m_ulCurNum = 0;
}

/*---------------------------------------------------------------------------*/

// Note(s):
//
//		a. info is duplicated internally
//		b. set is not checked for duplicated entries
EXC_TYPE CAttachSet::AddAttInfo (const CAttachInfo& ai)
{
	// check if have room for attachment
	if (m_ulCurNum >= m_ulMaxNum)
	{
		// check if allowed to grow
		if (0 == m_ulGrowNum)
			return EOVERFLOW;

		UINT32		ulNewMax=(m_ulMaxNum + m_ulGrowNum);
		LPATTINFO	*pAtts=new LPATTINFO[ulNewMax];
		if (NULL == pAtts)
			return EMEM;

		// copy previous set contents
		if (m_pAtts != NULL)
		{
			for (UINT32 i=0; i < m_ulCurNum; i++)
				pAtts[i] = m_pAtts[i];

			delete [] m_pAtts;
		}

		m_pAtts = pAtts;
		m_ulMaxNum = ulNewMax;

		for (UINT32 j=m_ulCurNum; j < m_ulMaxNum; j++)
			m_pAtts[j] = NULL;
	}

	// check if have an entry we can use
	LPATTINFO	pAI=m_pAtts[m_ulCurNum];
	if (NULL == pAI)
	{
		if (NULL == (pAI=new CAttachInfo))
			return EMEM;

		m_pAtts[m_ulCurNum] = pAI;
	}

	EXC_TYPE	exc=pAI->UpdateInfo(ai);
	if (exc != EOK)
		return exc;

	m_ulCurNum++;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CAttachSet::AddAttInfo (LPCTSTR					lpszName,
											const UINT32			ulSize,	// may be zero
											LPCTSTR					lpszPath, // may be NULL/empty
											LPCTSTR					lpszType,
											LPCTSTR					lpszSubType,
											LPCTSTR					lpszCharSet, // may be NULL/empty
											const RFC822ENCCASE	attEnc)
{
	CAttachInfo	attInfo;
	EXC_TYPE		exc=attInfo.SetInfo(lpszName, ulSize, lpszPath, lpszType, lpszSubType, lpszCharSet, attEnc);
	if (EOK == exc)
		exc = AddAttInfo(attInfo);

	return exc;
}

/*---------------------------------------------------------------------------*/

void CAttachSet::Cleanup ()
{
	if (m_pAtts != NULL)
	{
		for (UINT32 i=0; i < m_ulMaxNum; i++)
		{
			LPATTINFO&	pAI=m_pAtts[i];
			if (NULL == pAI)
				continue;

			delete pAI;
			pAI = NULL;
		}

		delete [] m_pAtts;
		m_pAtts = NULL;
	}

	m_ulMaxNum = 0;
	m_ulCurNum = 0;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CAttachSet::GetAttInfo (const UINT32 ulIdx, LPATTINFO& pInfo) const
{
	pInfo = NULL;
	if (ulIdx >= m_ulCurNum)
		return EOVERFLOW;

	pInfo = m_pAtts[ulIdx];
	return EOK;
}

/*---------------------------------------------------------------------------*/

// returns NULL if illegal index
LPATTINFO CAttachSet::operator[] (const UINT32 ulIdx) const
{
	LPATTINFO	pInfo=NULL;
	EXC_TYPE		exc=GetAttInfo(ulIdx, pInfo);
	return ((EOK == exc) ? pInfo : NULL);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CAttachSet::EnumAttachments (ATS_ENUM_CFN lpfnEcfn, LPVOID pArg) const
{
	if (NULL == lpfnEcfn)
		return EBADADDR;

	for (UINT32 ulIdx=0; ulIdx < m_ulCurNum; ulIdx++)
	{
		LPATTINFO	pInfo=m_pAtts[ulIdx];
		if (NULL == pInfo)
			return EBADBUFF;

		BOOLEAN		fContEnum=TRUE;
		EXC_TYPE exc=(*lpfnEcfn)(*pInfo, ulIdx, m_ulCurNum, pArg, fContEnum);
		if (exc != EOK)
			return exc;

		if (!fContEnum)
			break;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CAttachSet::ascCfn (const CAttachInfo&	attInfo,
									  const UINT32			/* ulAttIdx */,
									  const UINT32			/* ulTotNum */,
									  LPVOID					pArg,
									  BOOLEAN&				fContEnum)
{
	LPCATTSET	pSet=(LPCATTSET) pArg;
	if (NULL == pSet)
		return ECONTEXT;

	EXC_TYPE	exc=pSet->AddAttInfo(attInfo);
	fContEnum = (EOK == exc);
	return exc;
}

/*---------------------------------------------------------------------------*/
