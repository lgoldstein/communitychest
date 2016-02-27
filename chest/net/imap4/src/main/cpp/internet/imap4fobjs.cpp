#include <internet/imap4Lib.h>

//////////////////////////////////////////////////////////////////////////////

CIMAP4BodyStruct::CIMAP4BodyStruct (const UINT32 ulAvgContentsNum)
	: m_StructInfo(), m_Contents(ulAvgContentsNum, ulAvgContentsNum), m_ulAvgCNum(ulAvgContentsNum)
{
}

/*---------------------------------------------------------------------------*/

BOOLEAN CIMAP4BodyStruct::IsMultipartMsg () const
{
	LPCTSTR	lpszType=m_StructInfo.GetMIMEType();
	if (IsEmptyStr(lpszType))
		return FALSE;

	return ((0 == ::_tcsicmp(lpszType, pszMIMEMultipartType)) || (0 == ::_tcsicmp(lpszType, pszMIMEMessageType)));
}

/*---------------------------------------------------------------------------*/

BOOLEAN CIMAP4BodyStruct::HasAttachments () const
{
	UINT32	ulCNum=GetContentsNum();
	if (0 == ulCNum)
		return FALSE;

	if (1 != ulCNum)
		return TRUE;

	// if exactly one member, make sure this is not the body
	LPIMAP4BODYSTRUCT	pInfo=NULL;
	EXC_TYPE				exc=GetContentInfo(0, pInfo);
	if ((exc != EOK) || (NULL == pInfo))
		return FALSE;

	const CAttachInfo&	ai=pInfo->GetInfo();
	LPCTSTR	lpszName=ai.GetName();

	// body has no name...
	return (!IsEmptyStr(lpszName));
}

/*---------------------------------------------------------------------------*/

void CIMAP4BodyStruct::Reset ()
{
	m_StructInfo.Cleanup();

	CVSDCollEnum	cde(m_Contents);
	LPVOID			pV=NULL;

	for (EXC_TYPE	exc=cde.GetFirstItem(pV); EOK == exc; exc = cde.GetNextItem(pV))
	{
		LPIMAP4BODYSTRUCT	pBS=(LPIMAP4BODYSTRUCT) pV;
		pBS->Reset();
		delete pBS;
	}

	m_Contents.Reset();
}

/*---------------------------------------------------------------------------*/

// Note: returns NULL if out of range
EXC_TYPE CIMAP4BodyStruct::GetContentInfo (const UINT32 ulIdx, LPIMAP4BODYSTRUCT& pInfo) const
{
	pInfo = NULL;

	LPVOID	pV=NULL;
	EXC_TYPE	exc=m_Contents.GetData(ulIdx, pV);
	if (exc != EOK)
		return exc;

	pInfo = (LPIMAP4BODYSTRUCT) pV;
	return EOK;
}

/*---------------------------------------------------------------------------*/

// Note: returns NULL if out of range
LPIMAP4BODYSTRUCT CIMAP4BodyStruct::operator[] (const UINT32 ulIdx) const
{
	LPIMAP4BODYSTRUCT	pInfo=NULL;
	EXC_TYPE				exc=GetContentInfo(ulIdx, pInfo);
	return ((EOK == exc) ? pInfo : NULL);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4BodyStruct::EnumContents (IMAP4_BODYSTRUCTENUM_CFN lpfnEcfn, LPVOID pArg, BOOLEAN& fContEnum) const
{
	EXC_TYPE	exc=(*lpfnEcfn)(*this, pArg, fContEnum);
	if (exc != EOK)
		return exc;

	for (UINT32	ulCdx=0, ulCNum=GetContentsNum(); (ulCdx < ulCNum) && fContEnum; ulCdx++)
	{
		LPIMAP4BODYSTRUCT	pInfo=NULL;
		if ((exc=GetContentInfo(ulCdx, pInfo)) != EOK)
			return exc;

		if ((exc=pInfo->EnumContents(lpfnEcfn, pArg, fContEnum)) != EOK)
			return exc;
	}

	return EOK;
}

// Note: 1st call is with "this"
EXC_TYPE CIMAP4BodyStruct::EnumContents (IMAP4_BODYSTRUCTENUM_CFN lpfnEcfn, LPVOID pArg) const
{
	if (NULL == lpfnEcfn)
		return EBADADDR;

	BOOLEAN	fContEnum=TRUE;
	return EnumContents(lpfnEcfn, pArg, fContEnum);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4BodyStruct::GetCurrentStruct (LPCTSTR	lpszMsgPart, LPIMAP4BODYSTRUCT& pBS)
{
	pBS = NULL;

	if (IsEmptyStr(lpszMsgPart))
	{
		pBS = this;
		return EOK;
	}

	// extract part number
	LPCTSTR	lpszMP=lpszMsgPart;
	for ( ; _istdigit(*lpszMP); lpszMP++);
	UINT32	ulMPLen=(lpszMP - lpszMsgPart);

	// skip number delimiter
	if (*lpszMP != _T('\0'))
	{
		if (*lpszMP != IMAP4_BODYPART_DELIM)
			return EPATH;
		lpszMP++;
	}

	EXC_TYPE	exc=EOK;
	UINT32	ulPdx=argument_to_dword(lpszMsgPart, ulMPLen, EXC_ARG(exc));
	if (exc != EOK)
		return exc;
	ulPdx--;	// index starts at zero...

	// index may be at most exactly the number of current structures
	UINT32	ulCNum=GetContentsNum();
	if (ulPdx > ulCNum)
		return EOVERFLOW;

	LPIMAP4BODYSTRUCT	pMP=NULL;
	if (ulPdx < ulCNum)
	{
		if ((exc=GetContentInfo(ulPdx, pMP)) != EOK)
			return exc;
	}
	else	// ulPdx == ulCNum
	{
		if (NULL == (pMP=new CIMAP4BodyStruct(m_ulAvgCNum)))
			return EMEM;

		if ((exc=m_Contents.AddItem((LPVOID) pMP)) != EOK)
		{
			delete pMP;
			return exc;
		}
	}

	if ((exc=pMP->GetCurrentStruct(lpszMP, pBS)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

// callback for handling sub-header
typedef EXC_TYPE (*IMAP4_MSGPARTUPDATE_CFN)(CAttachInfo&	ai,
														  LPCTSTR		lpszKeyword,
														  LPCTSTR		lpszValue);

static EXC_TYPE bsContentTypeHandler (CAttachInfo&	ai,
												  LPCTSTR		lpszKeyword,
												  LPCTSTR		lpszValue)
{
	if (IsEmptyStr(lpszKeyword))
		return ai.SetContentType(lpszValue);

	if (0 == _tcsicmp(lpszKeyword, pszMIMENameKeyword))
		return ai.UpdateName(lpszValue, FALSE);
	else if (0 == _tcsicmp(lpszKeyword, pszMIMECharsetKeyword))
		return ai.UpdateCharSet(lpszValue);
	else	// store all other keywords
		return ai.UpdateCTypeProp(lpszKeyword, lpszValue);
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE bsContentDispHandler (CAttachInfo&	ai,
												  LPCTSTR		lpszKeyword,
												  LPCTSTR		lpszValue)
{
	if (IsEmptyStr(lpszKeyword))
		return ENOSTART;

	if (0 == _tcsicmp(lpszKeyword, pszMIMEFilenameKeyword))
		return ai.UpdateName(lpszValue, FALSE);
	else	// store all other keywords
		return ai.UpdateDispProp(lpszKeyword, lpszValue);
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE bsContentXferEncHandler (CAttachInfo&	ai,
													  LPCTSTR		lpszKeyword,
													  LPCTSTR		lpszValue)
{
	RFC822ENCCASE aEnc=RFC822EncodingStr2Case(lpszValue);
	if (fIsBadRFC822EncCase(aEnc))
		aEnc = RFC822_NONE_ENC;

	return ai.SetEncoding(aEnc);
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE bsContentLengthHandler (CAttachInfo&	ai,
													 LPCTSTR			lpszKeyword,
													 LPCTSTR			lpszValue)
{
	EXC_TYPE	exc=EOK;
	UINT32	ulAttSize=argument_to_dword(lpszValue, _tcslen(lpszValue), EXC_ARG(exc));
	if (exc != EOK)
		return exc;

	ai.SetSize(ulAttSize);
	return EOK;
}

/*---------------------------------------------------------------------------*/

// used for embedded messages only
static EXC_TYPE bsContentDescHandler (CAttachInfo&	ai,
												  LPCTSTR		lpszKeyword,
												  LPCTSTR		lpszValue)
{
	return ai.UpdateName(lpszValue, FALSE);
}

// called for multipart/related
static EXC_TYPE bsContentIdHandler (CAttachInfo&	ai,
												LPCTSTR			lpszKeyword,
												LPCTSTR			lpszValue)
{
	return ai.SetID(lpszValue);
}

static EXC_TYPE bsIgnoreHandler (CAttachInfo&	ai,
											LPCTSTR			lpszKeyword,
											LPCTSTR			lpszValue)
{
	return EOK;
}

/*---------------------------------------------------------------------------*/

static const STR2PTRASSOC mphAssoc[]={
	{	pszStdContentTypeHdr,		(LPVOID) bsContentTypeHandler			},
	{	pszStdContentXferEncoding,	(LPVOID) bsContentXferEncHandler		},
	{	pszStdContentLengthHdr,		(LPVOID) bsContentLengthHandler		},
	{	pszStdContentDescription,	(LPVOID) bsContentDescHandler			},
	{	pszStdContentIDHdr,			(LPVOID) bsContentIdHandler			},
	{	pszStdContentMD5Hdr,			(LPVOID) bsIgnoreHandler				},
	{	pszStdContentDisposition,	(LPVOID) bsContentDispHandler			},
	{	pszStdContentLanguage,		(LPVOID) bsIgnoreHandler				},
	{	IMAP4_RFC822SIZE,				(LPVOID) bsIgnoreHandler				},
	{	NULL,								NULL											}	// mark end
};

// sub-headers handlers map
static const CStr2PtrMapper mphMap(mphAssoc, 0, FALSE);

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4BodyStruct::UpdateMsgPart (LPCTSTR	lpszMsgPart,
														LPCTSTR	lpszSubHdr,
														LPCTSTR	lpszKeyword,
														LPCTSTR	lpszValue)
{
	EXC_TYPE	exc=EOK;

	if (!IsEmptyStr(lpszMsgPart))
	{
		LPCTSTR	lpszDesc=m_StructInfo.GetDescription();
		if (IsEmptyStr(lpszDesc))
		{
			if ((exc=m_StructInfo.SetDescription(lpszMsgPart)) != EOK)
				return exc;
		}
	}

	LPVOID	pV=NULL;
	if ((exc=mphMap.FindKey(lpszSubHdr, pV)) != EOK)
		return exc;

	IMAP4_MSGPARTUPDATE_CFN	lpfnUcfn=(IMAP4_MSGPARTUPDATE_CFN) pV;
	return (*lpfnUcfn)(m_StructInfo, lpszKeyword, lpszValue);
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE ubsCTypeHandler (CIMAP4BodyStruct&		ubs,
											LPCTSTR					lpszMsgPart,
											LPCTSTR					lpszSubHdr,
											const CAttachInfo&	bsInfo)
{
	EXC_TYPE	exc=EOK;
	LPCTSTR	lpszType=bsInfo.GetMIMEType(), lpszSubType=bsInfo.GetMIMESubType();
	if ((!IsEmptyStr(lpszType)) && (!IsEmptyStr(lpszSubType)))
	{
		TCHAR	szMIMETag[MAX_RFC822_MIMETAG_LEN+2]=_T("");
		if ((exc=BuildRFC822MIMETag(lpszType, lpszSubType, szMIMETag, MAX_RFC822_MIMETAG_LEN)) != EOK)
			return exc;
		if ((exc=ubs.ProcessMsgPart(lpszMsgPart, lpszSubHdr, NULL, szMIMETag)) != EOK)
			return exc;
	}

	LPCTSTR	lpszName=bsInfo.GetName();
	if (!IsEmptyStr(lpszName))
	{
		if ((exc=ubs.ProcessMsgPart(lpszMsgPart, lpszSubHdr, pszMIMEFilenameKeyword, lpszName)) != EOK)
			return exc;
	}

	LPCTSTR	lpszCharset=bsInfo.GetCharSet();
	if (!IsEmptyStr(lpszCharset))
	{
		if ((exc=ubs.ProcessMsgPart(lpszMsgPart, lpszSubHdr, pszMIMECharsetKeyword, lpszCharset)) != EOK)
			return exc;
	}

	return EOK;
}

static EXC_TYPE ubsCXferHandler (CIMAP4BodyStruct&		ubs,
											LPCTSTR					lpszMsgPart,
											LPCTSTR					lpszSubHdr,
											const CAttachInfo&	bsInfo)
{
	RFC822ENCCASE encCase=bsInfo.GetMIMEEncoding();
	return ubs.ProcessMsgPart(lpszMsgPart, lpszSubHdr, NULL, RFC822EncodingCase2Str(encCase));
}

static EXC_TYPE ubsCLengthHandler (CIMAP4BodyStruct&	ubs,
											  LPCTSTR				lpszMsgPart,
											  LPCTSTR				lpszSubHdr,
											  const CAttachInfo&	bsInfo)
{
	UINT32	ulAttSize=bsInfo.GetSize();
	TCHAR		szAttSize[MAX_DWORD_DISPLAY_LENGTH+2]=_T("");
	dword_to_argument(ulAttSize, szAttSize);

	return ubs.ProcessMsgPart(lpszMsgPart, lpszSubHdr, NULL, szAttSize);
}

static EXC_TYPE ubsCIdHandler (CIMAP4BodyStruct&	ubs,
										 LPCTSTR					lpszMsgPart,
										 LPCTSTR					lpszSubHdr,
										 const CAttachInfo&	bsInfo)
{
	return ubs.ProcessMsgPart(lpszMsgPart, lpszSubHdr, NULL, bsInfo.GetID());
}

static EXC_TYPE ubsCDispHandler (CIMAP4BodyStruct&	ubs,
										   LPCTSTR				lpszMsgPart,
										   LPCTSTR				lpszSubHdr,
										   const CAttachInfo&	bsInfo)
{
//	return ubs.ProcessMsgPart(lpszMsgPart, lpszSubHdr, $$$, $$$);
	return EOK;
}

/*---------------------------------------------------------------------------*/

typedef EXC_TYPE (*UBS_HANDLER_CFN)(CIMAP4BodyStruct&		ubs,
												LPCTSTR					lpszMsgPart,
												LPCTSTR					lpszSubHdr,
												const CAttachInfo&	bsInfo);

static const STR2PTRASSOC ubshAssocs[]={
	{	pszStdContentTypeHdr,		(LPVOID) ubsCTypeHandler	},
	{	pszStdContentXferEncoding,	(LPVOID) ubsCXferHandler	},
	{	pszStdContentLengthHdr,		(LPVOID) ubsCLengthHandler	},
	{	pszStdContentIDHdr,			(LPVOID) ubsCIdHandler		},
//	{	pszStdContentDisposition,	(LPVOID) ubsCDispHandler	},
	{	NULL,								NULL								}	// mark end
};

/*---------------------------------------------------------------------------*/

static EXC_TYPE ubsCfn (const CIMAP4BodyStruct& bs,
								LPVOID						pArg,
								BOOLEAN&						fContEnum)
{
	if (NULL == pArg)
		return ECONTEXT;

	CIMAP4BodyStruct&		ubs=*((LPIMAP4BODYSTRUCT) pArg);
	const CAttachInfo&	bsInfo=bs.GetInfo();
	LPCTSTR					lpszMsgPart=bsInfo.GetDescription();

	for (UINT32	i=0; ; i++)
	{
		const STR2PTRASSOC&	ubsa=ubshAssocs[i];
		UBS_HANDLER_CFN		lpfnHcfn=(UBS_HANDLER_CFN) ubsa.pVal;
		if (NULL == lpfnHcfn)
			break;

		EXC_TYPE	exc=(*lpfnHcfn)(ubs, lpszMsgPart, ubsa.pszKey, bsInfo);
		if (exc != EOK)
			return exc;
	}

	fContEnum = TRUE;
	return EOK;
}

/*---------------------------------------------------------------------------*/

// Note: resets the object first
EXC_TYPE CIMAP4BodyStruct::UpdateBodyStruct (const CIMAP4BodyStruct& bs)
{
	Reset();

	return bs.EnumContents(ubsCfn, (LPVOID) this);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4BodyStruct::ProcessMsgPart (LPCTSTR	lpszMsgPart,
														 LPCTSTR	lpszSubHdr,
														 LPCTSTR	lpszKeyword,
														 LPCTSTR	lpszValue)
{
	LPIMAP4BODYSTRUCT	pBS=NULL;
	EXC_TYPE				exc=GetCurrentStruct(lpszMsgPart, pBS);
	if (exc != EOK)
		return exc;

	return pBS->UpdateMsgPart(lpszMsgPart, lpszSubHdr, lpszKeyword, lpszValue);
}

//////////////////////////////////////////////////////////////////////////////

CIMAP4FullMsgInfo::CIMAP4FullMsgInfo (const UINT32 ulAvgRcipsNum, const UINT32 ulAvgContentsNum)
	: m_Envelope(ulAvgRcipsNum), m_BodyStruct(ulAvgContentsNum),
	  m_rawFlags(IMAP4_MSGFLAGS_NUM * MAX_IMAP4_FLAGS_ENCLEN,MAX_IMAP4_FLAGS_ENCLEN)
{
	memset(&m_FastInfo, 0, (sizeof m_FastInfo));
	memset(&m_auxFlags, 0, (sizeof m_auxFlags));
}

/*---------------------------------------------------------------------------*/

void CIMAP4FullMsgInfo::ResetFastInfo ()
{
	memset(&m_FastInfo, 0, (sizeof m_FastInfo));
	m_auxFlags.m_fHaveSeqNo = 0;
	m_auxFlags.m_fHaveUID = 0;
	m_auxFlags.m_fHaveSize = 0;
	m_auxFlags.m_fHaveFlags	= 0;
	m_auxFlags.m_fHaveIDate	= 0;

	m_rawFlags.Reset();
}

/*---------------------------------------------------------------------------*/

void CIMAP4FullMsgInfo::Reset ()
{
	ResetEnvelope();
	ResetBodyStructure();
	ResetFastInfo();

	memset(&m_auxFlags, 0, (sizeof m_auxFlags));
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FullMsgInfo::UpdateMsgInfo (const CIMAP4FullMsgInfo& mi)
{
	EXC_TYPE	exc=EOK;
	Reset();

	if (mi.HaveMsgSeqNo())
	{
		m_FastInfo.ulMsgSeqNo = mi.m_FastInfo.ulMsgSeqNo;
		m_auxFlags.m_fHaveSeqNo = 1;
	}

	if (mi.HaveMsgUID())
	{
		m_FastInfo.ulMsgUID = mi.m_FastInfo.ulMsgUID;
		m_auxFlags.m_fHaveUID = 1;
	}

	if (mi.HaveMsgSize())
	{
		m_FastInfo.ulMsgSize = mi.m_FastInfo.ulMsgSize;
		m_auxFlags.m_fHaveSize = 1;
	}

	if (mi.HaveMsgFlags())
	{
		m_FastInfo.flags = mi.m_FastInfo.flags;
		m_auxFlags.m_fHaveFlags = 1;
	}

	if (mi.HaveMsgRawFlags())
	{
		if ((exc=m_rawFlags.AddStr(mi.m_rawFlags)) != EOK)
			return exc;
	}

	if (mi.HaveMsgInternalDate())
	{
		m_FastInfo.iDate = mi.m_FastInfo.iDate;
		m_FastInfo.tmZone = mi.m_FastInfo.tmZone;
		m_auxFlags.m_fHaveIDate = 1;
	}

	if (mi.HaveMsgEnvelope())
	{
		const CIMAP4EnvelopeData& ed=mi.GetMsgEnvelope();
		if ((exc=m_Envelope.UpdateData(ed)) != EOK)
			return exc;
		m_auxFlags.m_fHaveEnvelope = 1;
	}

	if (mi.HaveMsgBodyStructure())
	{
		const CIMAP4BodyStruct&	bs=mi.GetMsgBodyStructure();
		if ((exc=m_BodyStruct.UpdateBodyStruct(bs)) != EOK)
			return exc;
		m_auxFlags.m_fHaveBodyStruct = 1;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FullMsgInfo::HandleFetchResponse (const CIMAP4FetchCfnData&	ftchData,
																 LPCTSTR							lpszModifier,
																 LPCTSTR							lpszSubHdr,		// valid only for complex structures
																 LPCTSTR							lpszKeyword,	// non-NULL for "keyword=value" pair(s)
																 LPVOID							lpModVal)		// actual type depends on modifier
{
	UINT32	ulMsgSeqNo=ftchData.GetMsgSeqNo();
	if (0 == ulMsgSeqNo)
		return EILSEQ;

	// do not check if msg sequence number changed since this may happen if
	// message preceding this one are deleted
	m_FastInfo.ulMsgSeqNo = ulMsgSeqNo;
	m_auxFlags.m_fHaveSeqNo = 1;

	LPCTSTR	lpszStrVal=(LPCTSTR) lpModVal;
	UINT32	ulNumVal=(UINT32) lpModVal;
	LPCTSTR	lpszMsgPart=ftchData.GetMsgPart();
	EXC_TYPE	exc=EOK;

	if (_tcsicmp(lpszModifier, IMAP4_UID) == 0)
	{
		if (HaveMsgUID())
		{
			if (ulNumVal != m_FastInfo.ulMsgUID)
				return ENOTUNIQ;
		}
		else
		{
			m_FastInfo.ulMsgUID = ulNumVal;
			m_auxFlags.m_fHaveUID = 1;
		}
	}
	else if (_tcsicmp(lpszModifier, IMAP4_RFC822SIZE) == 0)
	{
		if (HaveMsgSize())
		{
			if (m_FastInfo.ulMsgSize != ulNumVal)
				return ENOTUNIQ;
		}
		else
		{
			m_FastInfo.ulMsgSize = ulNumVal;
			m_auxFlags.m_fHaveSize = 1;
		}
	}
	else if (_tcsicmp(lpszModifier, IMAP4_ENVELOPE) == 0)
	{
		// non-empty msg part means embedded message envelope
		if (!IsEmptyStr(lpszMsgPart))
			return EOK;

		if ((exc=m_Envelope.ProcessFetchRsp(lpszSubHdr, lpModVal)) != EOK)
			return exc;

		m_auxFlags.m_fHaveEnvelope = 1;
	}
	else if (_tcsicmp(lpszModifier, IMAP4_FLAGS) == 0)
	{
		LPIMAP4MSGFLAGS	pFlags=(LPIMAP4MSGFLAGS) lpModVal;
		if (NULL == pFlags)
			return ECONTEXT;

		m_FastInfo.flags = *pFlags;
		m_auxFlags.m_fHaveFlags = 1;
	}
	else if ((_tcsicmp(lpszModifier, IMAP4_BODYSTRUCT) == 0) || (_tcsicmp(lpszModifier, IMAP4_BODY) == 0))
	{
		if ((exc=m_BodyStruct.ProcessResponse(ftchData, lpszSubHdr, lpszKeyword, lpszStrVal)) != EOK)
			return exc;

		m_auxFlags.m_fHaveBodyStruct = 1;
	}
	else if (0 == _tcsicmp(lpszModifier, IMAP4_INTERNALDATE))
	{
		if ((exc=DecodeIMAP4InternalDate(lpszStrVal, &m_FastInfo.iDate, &m_FastInfo.tmZone)) != EOK)
			return exc;

		m_auxFlags.m_fHaveIDate = 1;
	}
	else if (0 == _tcsicmp(lpszModifier, IMAP4_SILENT))
	{
		// ignore the value - only check its validity
		LPCTSTR			lpszFlag=GetSafeStrPtr(lpszSubHdr);
		const UINT32	ulFlgLen=(UINT32) lpModVal;
		if (0 == ulFlgLen)
			return EEMPTYENTRY;

		const LPCTSTR	lpszCurPos=m_rawFlags.GetCurPos(), lpszBufPos=m_rawFlags;
		if (lpszCurPos > lpszBufPos)
		{
			if ((exc=m_rawFlags.AddChar(_T(' '))) != EOK)
				return exc;
		}

		if ((exc=m_rawFlags.AddChars(lpszFlag, ulFlgLen)) != EOK)
			return exc;
	}
	else
		return ESTATE;

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

// also default constructor
CIMAP4MsgPartHandler::CIMAP4MsgPartHandler (LPCTSTR lpszMsgPart)
	: m_ulCurLen(0), m_staticPart(m_szStaticPart, MAX_IMAP4_MSGPART_LEN), m_dynamicPart((UINT32) 0, MAX_DWORD_DISPLAY_LENGTH)
{
	m_szStaticPart[0] = _T('\0');

	if (!IsEmptyStr(lpszMsgPart))
		SetPartID(lpszMsgPart);
}

/*---------------------------------------------------------------------------*/

void CIMAP4MsgPartHandler::Reset ()
{
	m_ulCurLen = 0;
	m_staticPart.Reset();
	m_dynamicPart.Reset();
}

/*---------------------------------------------------------------------------*/

// any previous part ID is overridden
EXC_TYPE CIMAP4MsgPartHandler::SetPartID (LPCTSTR lpszMsgPart)
{
	Reset();

	if (IsEmptyStr(lpszMsgPart))
		return EEMPTYENTRY;
	else
		return AddStr(lpszMsgPart);
}

CIMAP4MsgPartHandler::CIMAP4MsgPartHandler (const CIMAP4MsgPartHandler& mph)
	: m_ulCurLen(0), m_staticPart(m_szStaticPart, MAX_IMAP4_MSGPART_LEN), m_dynamicPart((UINT32) 0, MAX_DWORD_DISPLAY_LENGTH)
{
	m_szStaticPart[0] = _T('\0');
	SetPartID(mph.GetPartID());
}

LPCTSTR CIMAP4MsgPartHandler::GetPartID () const
{
	if (UseStaticPart())
		return m_szStaticPart;
	else
		return m_dynamicPart;
}

LPCTSTR CIMAP4MsgPartHandler::GetCurPos () const
{
	if (UseStaticPart())
		return m_staticPart.GetCurPos();
	else
		return m_dynamicPart.GetCurPos();
}

EXC_TYPE CIMAP4MsgPartHandler::AddChars (LPCTSTR lpszChars, const UINT32 ulCLen)
{
	EXC_TYPE	exc=EOK;

	if (0 == ulCLen)
		return EOK;

	if (NULL == lpszChars)
		return EPARAM;

	const UINT32	ulNewLen=(m_ulCurLen + ulCLen);
	if (ulNewLen > MAX_IMAP4_MSGPART_LEN)
	{
		// check if need to switch from static to dynamic accumulation
		if (m_ulCurLen <= MAX_IMAP4_MSGPART_LEN)
		{
			m_dynamicPart.Reset();

			if ((exc=m_dynamicPart.AddChars(m_szStaticPart, m_ulCurLen)) != EOK)
				return exc;

			m_staticPart.Reset();
		}

		if ((exc=m_dynamicPart.AddChars(lpszChars, ulCLen)) != EOK)
			return exc;
	}
	else	// can use static part yet
	{
		if ((exc=m_staticPart.AddChars(lpszChars, ulCLen)) != EOK)
			return exc;
	}

	m_ulCurLen = ulNewLen;
	return EOK;
}

//////////////////////////////////////////////////////////////////////////////
