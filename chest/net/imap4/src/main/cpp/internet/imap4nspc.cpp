#include <internet/imap4Exts.h>

#ifdef __cplusplus
#define SZXTRN extern
#else
#define SZXTRN /* nothing */
#endif	/* __cplusplus */

SZXTRN const TCHAR szIMAP4NamespaceCmd[]=_T("NAMESPACE");

//////////////////////////////////////////////////////////////////////////////

EXC_TYPE CIMAP4NamespaceGroup::AddItem (LPCTSTR lpszPrefix, const TCHAR chDelim)
{
	LPIMAP4NAMESPACEDEF	pDef=NULL;
	UINT32					ulTotalSize=(sizeof *pDef) + ((1+GetSafeStrlen(lpszPrefix)) * sizeof(TCHAR));
	LPBYTE					pBuf=new BYTE[ulTotalSize+sizeof(NATIVE_WORD)];
	if (NULL == pBuf)
		return EMEM;
	memset(pBuf, 0, ulTotalSize);

	pDef = (LPIMAP4NAMESPACEDEF) pBuf;
	pDef->chDelim = chDelim;
	if (!IsEmptyStr(lpszPrefix))
	{
		LPTSTR	lpszDst=(LPTSTR) (pBuf + (sizeof *pDef));
		_tcscpy(lpszDst, lpszPrefix);
		pDef->lpszPrefix = lpszDst;
	}

	EXC_TYPE	exc=m_nsc.AddItem(pDef);
	if (exc != EOK)
	{
		delete [] pBuf;
		return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

// Note: pointer becomes invalid if group changes (grows, decreases, destroyed)
EXC_TYPE CIMAP4NamespaceGroup::GetNamespaceDefinition (const UINT32 ulIdx, LPIMAP4NAMESPACEDEF& pDef) const
{
	LPVOID	pData=NULL;
	EXC_TYPE	exc=m_nsc.GetData(ulIdx, pData);
	if (exc != EOK)
	{
		pDef = NULL;
		return exc;
	}

	pDef = (LPIMAP4NAMESPACEDEF) pData;
	return EOK;
}

// if NULL returned then bad/illegal index
LPIMAP4NAMESPACEDEF CIMAP4NamespaceGroup::operator[] (const UINT32 ulIdx) const
{
	LPIMAP4NAMESPACEDEF	pDef=NULL;
	EXC_TYPE	exc=GetNamespaceDefinition(ulIdx, pDef);
	if (exc != EOK)
		return NULL;
	else
		return pDef;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4NamespaceGroup::EnumItems (IMAP4_NSENUM_CFN lpfnEcfn, LPVOID pArg) const
{
	if (NULL == lpfnEcfn)
		return EBADADDR;

	BOOLEAN	fContEnum=TRUE;
	for (UINT32 ulIdx=0; (ulIdx < GetSize()) && fContEnum; ulIdx++)
	{
		LPIMAP4NAMESPACEDEF	pDef=(LPIMAP4NAMESPACEDEF) m_nsc[ulIdx];
		if (NULL == pDef)
			return EBADHEADER;

		EXC_TYPE	exc=(*lpfnEcfn)(ulIdx, *pDef, pArg, fContEnum);
		if (exc != EOK)
			return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE nsMrgCfn (const UINT32					ulItemNdx,
								  const IMAP4NAMESPACEDEF&	nsDef,
								  LPVOID							pArg,
								  BOOLEAN&						fContEnum)
{
	fContEnum = TRUE;

	if (NULL == pArg)
		return ECONTEXT;

	CIMAP4NamespaceGroup&	nsg=*((CIMAP4NamespaceGroup *) pArg);
	return nsg.AddItem(nsDef);
}

EXC_TYPE CIMAP4NamespaceGroup::Merge (const CIMAP4NamespaceGroup& nsg)
{
	return nsg.EnumItems(nsMrgCfn, (LPVOID) this);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4NamespaceGroup::Reset ()
{
	for (UINT32 ulIdx=0; ulIdx < GetSize(); ulIdx++)
	{
		LPIMAP4NAMESPACEDEF	pDef=(LPIMAP4NAMESPACEDEF) m_nsc[ulIdx];
		if (NULL == pDef)
			continue;

		LPBYTE	pBuf=(LPBYTE) pDef;
		delete [] pBuf;
	}

	return m_nsc.Reset();
}

//////////////////////////////////////////////////////////////////////////////

// Note(s):
//
//		a. grow factor may be zero - i.e. when limit is reached no more items
//			are added.
//
//		b. cannot be re-initialized !!!
EXC_TYPE CIMAP4Namespaces::SetParams (const UINT32 ulMaxItems, const UINT32 ulGrow)
{
	EXC_TYPE	exc=EOK;

	if ((exc=m_nsPersonal.SetParams(ulMaxItems, ulGrow)) != EOK)
		return exc;
	if ((exc=m_nsShared.SetParams(ulMaxItems, ulGrow)) != EOK)
		return exc;
	if ((exc=m_nsOther.SetParams(ulMaxItems, ulGrow)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

// prefix and delimiter may be empty/NULL
EXC_TYPE CIMAP4Namespaces::AddItem (const IMAP4NAMESPACECASE nsCase, LPCTSTR lpszPrefix, const TCHAR chDelim)
{
	const CIMAP4NamespaceGroup *pNS=GetNamespace(nsCase);
	if (NULL == pNS)
		return ETYPE;

	return ((CIMAP4NamespaceGroup *) pNS)->AddItem(lpszPrefix, chDelim);
}

/*---------------------------------------------------------------------------*/

// returns NULL if bad/illegal namespace case requested
const CIMAP4NamespaceGroup *CIMAP4Namespaces::GetNamespace (const IMAP4NAMESPACECASE nsCase) const
{
	switch(nsCase)
	{
		case IMAP4_NSPERSONAL_CASE	: return &m_nsPersonal;
		case IMAP4_NSSHARED_CASE	: return &m_nsShared;
		case IMAP4_NSOTHER_CASE		: return &m_nsOther;
		default							: /* do nothing */;
	}

	return NULL;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4Namespaces::EnumItems (const IMAP4NAMESPACECASE nsCase, IMAP4_NSENUM_CFN lpfnEcfn, LPVOID pArg) const
{
	const CIMAP4NamespaceGroup *pNS=GetNamespace(nsCase);
	if (NULL == pNS)
		return EFTYPE;

	return pNS->EnumItems(lpfnEcfn, pArg);
}

/*---------------------------------------------------------------------------*/

// removes all data items
EXC_TYPE CIMAP4Namespaces::Reset ()
{
	EXC_TYPE	exc=EOK, terr=EOK;

	if ((terr=m_nsPersonal.Reset()) != EOK)
		exc = terr;
	if ((terr=m_nsShared.Reset()) != EOK)
		exc = terr;
	if ((terr=m_nsOther.Reset()) != EOK)
		exc = terr;

	return exc;
}

//////////////////////////////////////////////////////////////////////////////

class CIMAP4NamespaceRspParser : public CIMAP4RspParser {
	private:
		CIMAP4Namespaces&	m_ns;

		// disable copy constructor and assignment operator
		CIMAP4NamespaceRspParser (const CIMAP4NamespaceRspParser& );
		CIMAP4NamespaceRspParser& operator= (const CIMAP4NamespaceRspParser& );

		EXC_TYPE ParseNamespaceList (CIMAP4NamespaceGroup& nsg);

		EXC_TYPE BuildNamespace (const IMAP4NAMESPACECASE nsCase);

	public:
		CIMAP4NamespaceRspParser (ISockioInterface&	SBSock,
										  LPCTSTR				lpszTag,
										  CIMAP4Namespaces&	ns,
										  LPTSTR					lpszRspBuf,
										  const UINT32			ulMaxLen,
										  const UINT32			ulRspTimeout)
			: CIMAP4RspParser(SBSock, lpszTag, lpszRspBuf, ulMaxLen, ulRspTimeout)
			, m_ns(ns)
		{
		}

		virtual EXC_TYPE HandleUntaggedResponse (LPCTSTR lpszTag, LPCTSTR lpszOp);

		virtual ~CIMAP4NamespaceRspParser ()
		{
		}
};

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4NamespaceRspParser::ParseNamespaceList (CIMAP4NamespaceGroup& nsg)
{
	EXC_TYPE	exc=FillNonEmptyParseBuffer();
	if (exc != EOK)
		return exc;

	while (*m_lpszCurPos != IMAP4_PARLIST_EDELIM)
	{
		if (*m_lpszCurPos != IMAP4_PARLIST_SDELIM)
		{
			if ((exc=CheckNILParseBuffer(FALSE)) != EOK)
				return exc;
			continue;
		}

		// format is: (prefix delim) - e.g. ("" "/"), ("user." ".")
		m_lpszCurPos++;

		LPCTSTR	lpszPrefix=NULL, lpszDelim=NULL;
		UINT32	ulPrefLen=0, ulDelimLen=0;

		if ((exc=ExtractStringHdrVal(lpszPrefix, ulPrefLen, FALSE, FALSE)) != EOK)
			return exc;

		if ((exc=ExtractStringHdrVal(lpszDelim, ulDelimLen, FALSE, FALSE)) != EOK)
			return exc;

		// make sure delimiter is single char
		if (ulDelimLen != 1)
			return EBADSLT;

		LPCTSTR	lpszPE=NULL;
		TCHAR		chPE=_T('\0');
		if (0 != ulPrefLen)
		{
			lpszPE = (lpszPrefix + ulPrefLen);
			chPE = *lpszPE;
			*((LPTSTR) lpszPE) = _T('\0');
		}
		else
		{
			lpszPrefix = NULL;
		}

		exc = nsg.AddItem(lpszPrefix, *lpszDelim);

		// restore original prefix end
		if ((lpszPE != NULL) && (chPE != _T('\0')))
			*((LPTSTR) lpszPE) = chPE;

		if (exc!= EOK)
			return exc;

		if ((exc=FillNonEmptyParseBuffer()) != EOK)
			return exc;
	}

	m_lpszCurPos++;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4NamespaceRspParser::BuildNamespace (const IMAP4NAMESPACECASE nsCase)
{
	const CIMAP4NamespaceGroup *pNS=m_ns.GetNamespace(nsCase);
	if (NULL == pNS)
		return ETYPE;

	EXC_TYPE	exc=FillNonEmptyParseBuffer();
	if (exc != EOK)
		return exc;

	if (*m_lpszCurPos != IMAP4_PARLIST_SDELIM)
		return CheckNILParseBuffer(IMAP4_NSSHARED_CASE == nsCase);

	m_lpszCurPos++;	// skip list start

	if ((exc=ParseNamespaceList((CIMAP4NamespaceGroup &) *pNS)) != EOK)
		return exc;

	if ((exc=FillNonEmptyParseBuffer()) != EOK)
		return exc;

	// parsing should stop at end of returned namespace list
	if (*m_lpszCurPos != IMAP4_PARLIST_EDELIM)
		return EUDFFORMAT;

	m_lpszCurPos++;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4NamespaceRspParser::HandleUntaggedResponse (LPCTSTR lpszTag, LPCTSTR lpszOp)
{
	EXC_TYPE	exc=EOK;

	// ignore non-NAMESPACE results
	if (_tcsicmp(szIMAP4NamespaceRsp, lpszTag) != 0)
		return EOK;

	// Note: order is important (see rfc2342)
	if ((exc=BuildNamespace(IMAP4_NSPERSONAL_CASE)) != EOK)
		return exc;
	if ((exc=BuildNamespace(IMAP4_NSOTHER_CASE)) != EOK)
		return exc;
	if ((exc=BuildNamespace(IMAP4_NSSHARED_CASE)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4GetNamespaceSync (ISockioInterface&	SBSock,
										  LPCTSTR				lpszITag,
										  CIMAP4Namespaces&	ns,
										  LPTSTR					lpszRspBuf,
										  const UINT32			ulMaxLen,
										  const UINT32			ulRspTimeout)
{
	if ((NULL == lpszRspBuf) || (ulMaxLen < MAX_IMAP4_CMD_LEN))
		return EBADBUFF;

	TCHAR				szCmd[MAX_IMAP4_CMD_LEN+4]=_T("");
	CStrlBuilder	strb(szCmd, MAX_IMAP4_CMD_LEN);
	LPCTSTR			lpszTag=lpszITag;
	TCHAR				szTag[MAX_IMAP4_TAG_LEN+2]=_T("");
	EXC_TYPE			exc=InitIMAP4Cmd(lpszTag, szIMAP4NamespaceCmd, FALSE, szTag, MAX_IMAP4_TAG_LEN, strb);
	if (exc != EOK)
		return exc;

	if ((exc=strb.AddCRLF()) != EOK)
		return exc;

	int	wLen=SBSock.Write(szCmd);
	if (wLen <= 0)
		return ENOTCONNECTION;

	CIMAP4NamespaceRspParser	nsrp(SBSock, lpszTag, ns, lpszRspBuf, ulMaxLen, ulRspTimeout);
	if ((exc=nsrp.ParseResponse(FALSE)) != EOK)
		return exc;

	return EOK;
}

EXC_TYPE imap4GetNamespaceSync (ISockioInterface&	SBSock,
										  LPCTSTR				lpszITag,
										  CIMAP4Namespaces&	ns,
										  const UINT32			ulRspTimeout)
{
	TCHAR	szRsp[MAX_IMAP4_DATA_LEN+2]=_T("");
	return imap4GetNamespaceSync(SBSock, lpszITag, ns, szRsp, MAX_IMAP4_DATA_LEN, ulRspTimeout);
}

/*---------------------------------------------------------------------------*/
