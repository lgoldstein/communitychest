#include <internet/imap4Lib.h>

//////////////////////////////////////////////////////////////////////////////

class CIMAP4CfnInvoker : public CIMAP4FetchCfnData {
	private:
		LPCTSTR					m_lpszFetchRsp;
		LPCTSTR					m_lpszMsgPart;
		ISockioInterface&		m_SBSock;
		IMAP4_FETCHRSP_CFN	m_lpfnHcfn;
		LPVOID					m_pArg;
		UINT32					m_ulMsgSeqNo;

	public:
		CIMAP4CfnInvoker (ISockioInterface&		SBSock,
								IMAP4_FETCHRSP_CFN	lpfnHcfn,
								LPVOID					pArg)
			: m_SBSock(SBSock), m_lpfnHcfn(lpfnHcfn), m_pArg(pArg), m_ulMsgSeqNo(0),
			  m_lpszFetchRsp(NULL), m_lpszMsgPart(NULL)
		{
		}

		void Reset ();

		EXC_TYPE SetModifier (LPCTSTR lpszFetchRsp);

		void ClearModifier ()
		{
			m_lpszFetchRsp = NULL;
		}

		// nothing is checked
		void SetMsgPart (LPCTSTR lpszMsgPart)
		{
			m_lpszMsgPart = lpszMsgPart;
		}

		void ClearMsgPart ()
		{ 
			m_lpszMsgPart = NULL;
		}

		EXC_TYPE SetCurSeqNo (LPCTSTR lpszSeqNo);

		void ClearCurSeqNo ()
		{
			m_ulMsgSeqNo = 0;
		}

		virtual ISockioInterface& GetServerConn () const
		{
			return m_SBSock;
		}

		virtual UINT32 GetMsgSeqNo () const
		{
			return m_ulMsgSeqNo;
		}

		// Note: may differ from callback modifier for embedded messages - e.g.
		//			callback modifier may be ENVELOPE, but original modifier will
		//			still be BODY/BODYSTRUCTURE for the embedded message headers part.
		virtual LPCTSTR GetModifier () const
		{
			return m_lpszFetchRsp;
		}

		// Note: returns non-NULL only for BODY/BODYSTRUCTURE modifier(s)
		virtual LPCTSTR GetMsgPart () const
		{
			return m_lpszMsgPart;
		}

		virtual LPVOID GetCallbackArg () const
		{
			return m_pArg;
		}

		EXC_TYPE CallBodyHdrListVal (LPCTSTR lpszHdrName)
		{
			return EOK;
		}

		EXC_TYPE CallMsgFlags (LPCTSTR lpszFetchRsp, const IMAP4_MSGFLAGS& msgFlags)
		{
			return (*m_lpfnHcfn)(*this, lpszFetchRsp, NULL, NULL, (LPVOID) &msgFlags);
		}

		EXC_TYPE CallMsgRawFlag (LPCTSTR lpszFetchRsp, LPCTSTR lpszFlag, const UINT32 ulFlgLen)
		{
			return (*m_lpfnHcfn)(*this, lpszFetchRsp, lpszFlag, NULL, (LPVOID) ulFlgLen);
		}

		EXC_TYPE CallNumResponse (LPCTSTR lpszFetchRsp, const UINT32 ulNum)
		{
			return (*m_lpfnHcfn)(*this, lpszFetchRsp, NULL, NULL, (LPVOID) ulNum);
		}

		EXC_TYPE CallEnvelopeHdrVal (LPCTSTR lpszFetchRsp, LPCTSTR lpszHdrName, LPCTSTR lpszHdrVal)
		{
			return (*m_lpfnHcfn)(*this, lpszFetchRsp, lpszHdrName, NULL, (LPVOID) lpszHdrVal);
		}

		EXC_TYPE CallInternalDateHdr (LPCTSTR lpszFetchRsp, LPCTSTR lpszIDate)
		{
			return (*m_lpfnHcfn)(*this, lpszFetchRsp, pszStdDateHdr, NULL, (LPVOID) lpszIDate);
		}

		EXC_TYPE CallBodyHdrVal (LPCTSTR lpszFetchRsp, LPCTSTR lpszSection, LPCTSTR lpszHdrVal)
		{
			return (*m_lpfnHcfn)(*this, lpszFetchRsp, lpszSection, NULL, (LPVOID) lpszHdrVal);
		}

		EXC_TYPE CallBodyDataVal (LPCTSTR lpszFetchRsp, LPCTSTR lpszSection, LPCTSTR lpszHdrVal)
		{
			return (*m_lpfnHcfn)(*this, lpszFetchRsp, lpszSection, szXBody, (LPVOID) lpszHdrVal);
		}

		EXC_TYPE CallMsgPartHdr (LPCTSTR lpszFetchRsp, LPCTSTR lpszHdrName, LPCTSTR lpszHdrVal)
		{
			return (*m_lpfnHcfn)(*this, lpszFetchRsp, lpszHdrName, NULL, (LPVOID) lpszHdrVal);
		}

		EXC_TYPE CallMsgPairHdr (LPCTSTR lpszFetchRsp, LPCTSTR lpszHdrName, LPCTSTR lpszKeyword, LPCTSTR lpszHdrVal)
		{
			return (*m_lpfnHcfn)(*this, lpszFetchRsp, lpszHdrName, lpszKeyword, (LPVOID) lpszHdrVal);
		}

		virtual ~CIMAP4CfnInvoker () { }
};

/*---------------------------------------------------------------------------*/

void CIMAP4CfnInvoker::Reset ()
{
	m_ulMsgSeqNo = 0;
	m_lpszFetchRsp = NULL;
	m_lpszMsgPart = NULL;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4CfnInvoker::SetCurSeqNo (LPCTSTR lpszSeqNo)
{
	if (m_ulMsgSeqNo != 0)
		return ERESOURCELIMIT;

	EXC_TYPE	exc=EOK;
	m_ulMsgSeqNo = argument_to_dword(lpszSeqNo, _tcslen(lpszSeqNo), EXC_ARG(exc));
	return exc;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4CfnInvoker::SetModifier (LPCTSTR lpszFetchRsp)
{
	if (IsEmptyStr(lpszFetchRsp))
		return EILLEGALOPCODE;

	if (IsEmptyStr(m_lpszFetchRsp))
		m_lpszFetchRsp = lpszFetchRsp;
	else
		return ERESOURCELIMIT;

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

class CIMAP4FetchRspParser : public CIMAP4RspParser {
	private:
		CIMAP4CfnInvoker	m_cfnCaller;

		// disable copy constructor and assignment operator
		CIMAP4FetchRspParser (const CIMAP4FetchRspParser& );
		CIMAP4FetchRspParser& operator= (const CIMAP4FetchRspParser& );

		EXC_TYPE PreProcessSectionFetchRsp (LPTSTR lpszSection, const UINT32 ulMaxLen);

		EXC_TYPE ParseNumFetchRsp (LPCTSTR	lpszFetchRsp);
		EXC_TYPE ParseFetchContents ();

		EXC_TYPE ParseEnvelopeUnlimitedLiteralHdrVal (LPCTSTR lpszFetchRsp, LPCTSTR lpszSubHdr);

		EXC_TYPE ParseEnvelopeUnlimitedUnquotedHdrVal (LPCTSTR lpszFetchRsp, LPCTSTR lpszSubHdr);
		EXC_TYPE ParseEnvelopeUnlimitedQuotedHdrVal (LPCTSTR lpszFetchRsp, LPCTSTR lpszSubHdr);

		EXC_TYPE ParseEnvelopeUnlimitedStringHdrVal (LPCTSTR lpszFetchRsp, LPCTSTR lpszSubHdr);

		EXC_TYPE ParseEnvelopeStringHdrVal (LPCTSTR lpszFetchRsp, LPCTSTR lpszSubHdr, const BOOLEAN fAllowOverflow);

		// Note: if header length exceeds maximum then it is silently ignored !!!
		EXC_TYPE AddEnvelopeAddrHdrVal (const UINT32 ulMaxHdrLen, LPTSTR& lpszAPos, UINT32& ulRemLen, UINT32& ulHdrLen);
		EXC_TYPE ParseEnvelopeAddrHdrVal (LPCTSTR lpszFetchRsp, LPCTSTR lpszSubHdr, const BOOLEAN fSingleInstance);

		EXC_TYPE ReReportContentType (LPCTSTR	lpszFetchRsp,
												LPCTSTR	lpszType,
												LPCTSTR	lpszSubType);

		EXC_TYPE HandleMsgPartContentType (LPCTSTR		lpszFetchRsp,
													  LPTSTR			lpszMIMETag,
													  const UINT32	ulMaxLen);

		EXC_TYPE HandleMsgPartHdr (LPCTSTR	lpszFetchRsp, LPCTSTR	lpszSubHdr);
		EXC_TYPE HandleOptionalMsgPartHdr (LPCTSTR	lpszFetchRsp, LPCTSTR	lpszSubHdr);

		EXC_TYPE HandleMsgPartList (LPCTSTR lpszFetchRsp, LPCTSTR lpszSubHdr);
		EXC_TYPE HandleOptionalMsgPartLanguage (LPCTSTR lpszFetchRsp);
		EXC_TYPE HandleOptionalMsgPartDisposition (LPCTSTR lpszFetchRsp);

		EXC_TYPE HandleMsgPartParamsList (LPCTSTR	lpszFetchRsp, LPCTSTR	lpszSubHdr);
		EXC_TYPE HandleOptionalMsgPartParamsList (LPCTSTR	lpszFetchRsp, LPCTSTR lpszSubHdr);

		EXC_TYPE ParseMsgPart (LPCTSTR lpszFetchRsp, LPCTSTR lpszPartPrefix);

		EXC_TYPE HandleMsgPartSimple (LPCTSTR lpszFetchRsp);
		EXC_TYPE HandleMsgPartMultipart (LPCTSTR lpszFetchRsp);
		EXC_TYPE HandleEmbeddedMsgPart (LPCTSTR lpszFetchRsp);

		EXC_TYPE ParseFetchEquivalentRsp (LPCTSTR	lpszFetchRsp, LPCTSTR lpszSection);

		virtual EXC_TYPE ResyncResponse (LPCTSTR lpszTag, const EXC_TYPE orgErr);

	public:
		CIMAP4FetchRspParser (ISockioInterface&	SBSock,
									 LPCTSTR					lpszTag,
									 IMAP4_FETCHRSP_CFN	lpfnHcfn,
									 LPVOID					pArg,
									 LPTSTR					lpszRspBuf,
									 const UINT32			ulMaxRspLen,
									 const UINT32			ulRspTimeout)
			: CIMAP4RspParser(SBSock, lpszTag, lpszRspBuf, ulMaxRspLen, ulRspTimeout), m_cfnCaller(SBSock, lpfnHcfn, pArg)
		{
		}

		EXC_TYPE ParseUIDFetchRsp (LPCTSTR	lpszFetchRsp=IMAP4_UID)
		{
			return ParseNumFetchRsp(lpszFetchRsp);
		}

		EXC_TYPE ParseFlagsFetchRsp (LPCTSTR lpszFetchRsp=IMAP4_FLAGS);
		EXC_TYPE ParseInternalDateFetchRsp (LPCTSTR lpszFetchRsp=IMAP4_INTERNALDATE);
		EXC_TYPE ParseEnvelopeFetchRsp (LPCTSTR	lpszFetchRsp=IMAP4_ENVELOPE);
		EXC_TYPE ParseBodyStructFetchRsp (LPCTSTR lpszFetchRsp=IMAP4_BODYSTRUCT, const BOOL fIsRecursive=FALSE);
		EXC_TYPE ParseBodyFetchRsp (LPCTSTR lpszFetchRsp=IMAP4_BODY);

		EXC_TYPE ParseRFC822FetchRsp (LPCTSTR	lpszFetchRsp=IMAP4_RFC822)
		{
			return ParseFetchEquivalentRsp(IMAP4_BODY, _T("[]"));
		}

		EXC_TYPE ParseRFC822HdrFetchRsp (LPCTSTR	lpszFetchRsp=IMAP4_RFC822HDR)
		{
			return ParseFetchEquivalentRsp(IMAP4_BODY, szIMAP4BodyHeaders);
		}

		EXC_TYPE ParseRFC822TextFetchRsp (LPCTSTR lpszFetchRsp=IMAP4_RFC822TEXT)
		{
			return ParseFetchEquivalentRsp(IMAP4_BODY, szIMAP4BodyText);
		}

		EXC_TYPE ParseRFC822SizeFetchRsp (LPCTSTR	lpszFetchRsp=IMAP4_RFC822SIZE)
		{
			return ParseNumFetchRsp(lpszFetchRsp);
		}

		virtual EXC_TYPE HandleUntaggedResponse (LPCTSTR lpszTag, LPCTSTR lpszOp);

		virtual ~CIMAP4FetchRspParser () { }
};

//////////////////////////////////////////////////////////////////////////////

EXC_TYPE CIMAP4FetchRspParser::ResyncResponse (LPCTSTR lpszTag, const EXC_TYPE orgErr)
{
	// try finding either a tagged response or "* XX FETCH ,,,"
	for (UINT32	ulRdx=0; ; ulRdx++)
	{
		EXC_TYPE	exc=SkipToEndOfLine();
		if (exc != EOK)
			return exc;

		const int	rLen=m_SBSock.ReadCmd(m_lpszRspBuf, m_ulRspBufLen, (SINT32) m_ulRspTimeout, &m_fStripCRLF);
		if (rLen < 0)
			return ENOTCONNECTION;
		if (0 == rLen)
			continue;

		TCHAR		szTag[MAX_IMAP4_TAG_LEN+2]=_T(""), szOp[MAX_IMAP4_OPCODE_LEN+2]=_T("");
		exc = imap4ExtractRsp(m_lpszRspBuf, szTag, MAX_IMAP4_TAG_LEN, szOp, MAX_IMAP4_OPCODE_LEN, &m_lpszCurPos);
		if ((EOK == exc) || (EPERMISSION == exc) || (ECONTEXT == exc))
		{
			if (_tcsicmp(szTag, lpszTag) != 0)
				continue;

			// found a tagged response (which could be OK/NO/BAD)
			return exc;
		}

		if (exc != EDATACHAIN)
			continue;

		// make sure the element following the untagged response is a number
		const UINT32	ulMsgSeqNo=argument_to_dword(szTag, _tcslen(szTag), EXC_ARG(exc));
		if ((exc != EOK) || (0 == ulMsgSeqNo))
			continue;

		// ignore non-FETCH responses
		if (_tcsicmp(szOp, szIMAP4FetchCmd) != 0)
			continue;

		// found an untagged response
		break;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE rfiCfn (const UINT32	ulFlgIndex,	/* starts at zero */
								LPCTSTR			lpszFlag,	/* raw flag string - NOTE: not necessarily EOS terminated !!! */
								const UINT32	ulFlgLen,	/* length of string data */
								LPVOID			pArg,
								BOOLEAN			*pfContEnum)
{
	if (NULL == pArg)
		return ECONTEXT;
	else
		return ((CIMAP4CfnInvoker *) pArg)->CallMsgRawFlag(IMAP4_SILENT, lpszFlag, ulFlgLen);
}

EXC_TYPE CIMAP4FetchRspParser::ParseFlagsFetchRsp (LPCTSTR	lpszFetchRsp)
{
	EXC_TYPE	exc=EOK;

	if ((exc=FillNonEmptyParseBuffer()) != EOK)
		return exc;

	IMAP4_MSGFLAGS	msgFlags={ 0 };
	if ((exc=ExtractMsgFlags(msgFlags, ::rfiCfn, (LPVOID) &m_cfnCaller)) != EOK)
		return exc;

	return m_cfnCaller.CallMsgFlags(lpszFetchRsp, msgFlags);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::ParseNumFetchRsp (LPCTSTR	lpszFetchRsp)
{
	UINT32	ulNum=0;

	for (UINT32 ulRdx=0; ; ulRdx++)
	{
		EXC_TYPE	exc=FillNonEmptyParseBuffer();
		if (exc != EOK)
			return exc;

		// make sure UID starts with digit
		if (!_istdigit(*m_lpszCurPos))
			return EINVALIDNUMERIC;

		// find end of UID
		LPCTSTR	lpszNum=m_lpszCurPos;
		for (; _istdigit(*m_lpszCurPos) && (*m_lpszCurPos != _T('\0')); m_lpszCurPos++);

		// if reached end of string then refill and retry - UID is terminated by either a space or a list end
		if (_T('\0') == *m_lpszCurPos)
		{
			if ((exc=RefillFetchParseBuffer(lpszNum)) != EOK)
				return exc;

			continue;
		}

		UINT32	ulULen=(m_lpszCurPos - lpszNum);
		ulNum = argument_to_dword(lpszNum, ulULen, EXC_ARG(exc));
		if (exc != EOK)
			return exc;

		break;
	}

	return m_cfnCaller.CallNumResponse(lpszFetchRsp, ulNum);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::ParseBodyFetchRsp (LPCTSTR lpszFetchRsp)
{
	// check for "BODY" only
	LPCTSTR	lpszSectStart=_tcschr(lpszFetchRsp, IMAP4_BRCKT_SDELIM);
	if (NULL == lpszSectStart)
		return ParseBodyStructFetchRsp(lpszFetchRsp);

	// check for BODY[section] and extract only the "section" response
	LPCTSTR	lpszSection=NULL, lpszHdrsList=NULL;
	UINT32	ulSectionLen=0, ulHListLen=0, ulOriginOctet=(UINT32) (-1);
	EXC_TYPE	exc=imap4AnalyzeBodyRsp(lpszFetchRsp, &lpszSection, &ulSectionLen, &lpszHdrsList, &ulHListLen, &ulOriginOctet);
	if (exc != EOK)
		return exc;
	*((LPTSTR) lpszSectStart) = _T('\0');

	// if have headers list then make sure it starts at least one space AFTER the section
	if (ulSectionLen != 0)
	{
		if (ulHListLen != 0)
		{
			if ((lpszSection + ulSectionLen) >= lpszHdrsList)
				return ENOPARAMETERS;
		}

		*((LPTSTR) (lpszSection + ulSectionLen)) = _T('\0');
	}

	if ((exc=FillNonEmptyParseBuffer()) != EOK)
		return exc;

	if (IMAP4_OCTCNT_SDELIM != *m_lpszCurPos)
	{
		// if no octet count then assume a string atom
		LPCTSTR	lpszHdrVal=NULL;
		UINT32	ulHdrLen=0;

		if ((exc=ExtractStringHdrVal(lpszHdrVal, ulHdrLen, FALSE, FALSE)) != EOK)
			return exc;

		LPCTSTR	lpszHE=(lpszHdrVal + ulHdrLen);
		TCHAR		tch=*lpszHE;

		*((LPTSTR) lpszHE) = _T('\0');
		exc = m_cfnCaller.CallBodyHdrVal(lpszFetchRsp, lpszSection, lpszHdrVal);
		*((LPTSTR) lpszHE) = tch;

		return exc;
	}

	UINT32	ulDataLen=0;
	if ((exc=ExtractLiteralCount(ulDataLen)) != EOK)
		return exc;

	for (UINT32	ulRemLen=ulDataLen; ulRemLen > 0; )
	{
		UINT32	ulALen=min(ulRemLen, m_ulRspBufLen);
		int		rLen=m_SBSock.Fill(m_lpszRspBuf, (size_t) ulALen, (SINT32) m_ulRspTimeout);
		if ((UINT32) rLen != ulALen)
			return ((rLen <= 0) ? ENOTCONNECTION : EIOWRPROT);
		ulRemLen -= ulALen;

		m_lpszCurPos = &m_lpszRspBuf[rLen];
		*((LPTSTR) m_lpszCurPos) = _T('\0');

		if ((exc=m_cfnCaller.CallBodyDataVal(lpszFetchRsp, lpszSection, m_lpszRspBuf)) != EOK)
			return exc;
	}

	// skip to next available FETCH response
	if ((exc=FillNonEmptyParseBuffer()) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE resolveEmptyMIMETagValue (LPTSTR			lpszCurValue,
														const UINT32	ulMaxLen,
														LPCTSTR			lpszDefValue,
														UINT32&			ulUsedLen)
{
	if (NULL == lpszCurValue)
		return EBADBUFF;
	if (*lpszCurValue != _T('\0'))
		return EOK;

	UINT32	ulRemLen=ulMaxLen;
	LPTSTR	lpszMT=lpszCurValue;
	EXC_TYPE	exc=strlinsstr(&lpszMT, lpszDefValue, &ulRemLen);
	if (exc != EOK)
		return exc;

	ulUsedLen = (ulMaxLen - ulRemLen);
	return EOK;
}

#define resolveEmptyMIMEType(lpszCurType,ulMaxLen,ulUsedLen)	\
	resolveEmptyMIMETagValue(lpszCurType,ulMaxLen,_T("X-MIME-TYPE"),ulUsedLen)
#define resolveEmptyMIMESubType(lpszCurType,ulMaxLen,ulUsedLen)	\
	resolveEmptyMIMETagValue(lpszCurType,ulMaxLen,_T("X-MIME-SUB-TYPE"),ulUsedLen)

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::HandleMsgPartContentType (LPCTSTR			lpszFetchRsp,
																			LPTSTR			lpszMIMETag,
																			const UINT32	ulMaxLen)
{
	EXC_TYPE	exc=EOK;
	LPTSTR	lpszMT=lpszMIMETag;
	UINT32	ulRemLen=ulMaxLen, ulTLen=0, ulSTLen=0;

	// build "Content-Type: xxx/yyy"
	if ((exc=CopyStringHdrVal(lpszMT, ulRemLen, ulTLen, FALSE, FALSE)) != EOK)
		return exc;
	// if empty MIME type then replace with "X-MIME-TYPE"
	if ((exc=resolveEmptyMIMEType(lpszMT, ulRemLen, ulTLen)) != EOK)
		return exc;

	ulRemLen -= ulTLen;
	lpszMT += ulTLen;
	if ((exc=strlinsch(&lpszMT, RFC822_MIMETAG_SEP, &ulRemLen)) != EOK)
		return exc;

	if ((exc=CopyStringHdrVal(lpszMT, ulRemLen, ulSTLen, FALSE, FALSE)) != EOK)
		return exc;
	// if empty MIME type then replace with "X-MIME-SUB-TYPE"
	if ((exc=resolveEmptyMIMESubType(lpszMT, ulRemLen, ulTLen)) != EOK)
		return exc;

	return m_cfnCaller.CallMsgPartHdr(lpszFetchRsp, pszStdContentTypeHdr, lpszMIMETag);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::HandleMsgPartHdr (LPCTSTR	lpszFetchRsp, LPCTSTR	lpszSubHdr)
{
	LPCTSTR	lpszHdrVal=NULL;
	UINT32	ulHdrLen=0;
	EXC_TYPE	exc=ExtractStringHdrVal(lpszHdrVal, ulHdrLen, FALSE, FALSE);
	if (exc != EOK)
		return exc;

	// skip empty header data
	if (0 == ulHdrLen)
		return EOK;

	LPCTSTR	lpszHE=(lpszHdrVal + ulHdrLen);
	TCHAR		tch=*lpszHE;

	*((LPTSTR) lpszHE) = _T('\0');
	exc = m_cfnCaller.CallMsgPartHdr(lpszFetchRsp, lpszSubHdr, lpszHdrVal);
	*((LPTSTR) lpszHE) = tch;

	return exc;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::HandleMsgPartParamsList (LPCTSTR	lpszFetchRsp, LPCTSTR	lpszSubHdr)
{
	EXC_TYPE	exc=FillNonEmptyParseBuffer();
	if (exc != EOK)
		return exc;

	if (*m_lpszCurPos != IMAP4_PARLIST_SDELIM)
		return CheckNILParseBuffer(FALSE);

	m_lpszCurPos++;

	while (*m_lpszCurPos != IMAP4_PARLIST_EDELIM)
	{
		TCHAR		szAttrName[MAX_RFC822_KEYWORD_LEN+2]=_T("");
		UINT32 ulANLen=0;
		if ((exc=CopyStringHdrVal(szAttrName, MAX_RFC822_KEYWORD_LEN, ulANLen, FALSE, FALSE)) != EOK)
			return exc;

		if ((exc=FillNonEmptyParseBuffer()) != EOK)
			return exc;

		if (IMAP4_PARLIST_SDELIM == *m_lpszCurPos)
		{
			if ((exc=HandleMsgPartParamsList(lpszFetchRsp, lpszSubHdr)) != EOK)
				return exc;

			continue;
		}

		// simple attribute value
		LPCTSTR	lpszAttrVal=NULL;
		UINT32	ulAVLen=0;
		if ((exc=ExtractStringHdrVal(lpszAttrVal, ulAVLen, FALSE, FALSE)) != EOK)
			return exc;

		if ((0 == ulAVLen) && (0 == ulANLen))
			continue;

		LPTSTR	lpszAVE=(LPTSTR) (lpszAttrVal + ulAVLen);
		TCHAR		chAVE=*lpszAVE;

		*lpszAVE = _T('\0');
		exc = m_cfnCaller.CallMsgPairHdr(lpszFetchRsp, lpszSubHdr, szAttrName, lpszAttrVal);
		*lpszAVE = chAVE;
		if (exc != EOK)
			return exc;
	}

	// skip terminating paranthesis
	m_lpszCurPos++;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::HandleEmbeddedMsgPart (LPCTSTR lpszFetchRsp)
{
	EXC_TYPE	exc=EOK;

	if ((exc=ParseEnvelopeFetchRsp(/* empty == IMAP4_ENVELOPE by design !!! */)) != EOK)
		return exc;

	if ((exc=ParseBodyStructFetchRsp(lpszFetchRsp, TRUE)) != EOK)
		return exc;

	if ((exc=FillNonEmptyParseBuffer()) != EOK)
		return exc;

	// embedded message size in text lines follows
	if (!_istdigit(*m_lpszCurPos))
		return EILSEQ;

	if ((exc=HandleMsgPartHdr(lpszFetchRsp, IMAP4_RFC822SIZE)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::HandleOptionalMsgPartHdr (LPCTSTR	lpszFetchRsp, LPCTSTR	lpszSubHdr)
{
	EXC_TYPE	exc=FillNonEmptyParseBuffer();
	if (exc != EOK)
		return exc;

	if (IMAP4_PARLIST_EDELIM == *m_lpszCurPos)
		return EOK;

	return HandleMsgPartHdr(lpszFetchRsp, lpszSubHdr);
}

/*---------------------------------------------------------------------------*/

// simple list of values
EXC_TYPE CIMAP4FetchRspParser::HandleMsgPartList (LPCTSTR lpszFetchRsp, LPCTSTR lpszSubHdr)
{
	EXC_TYPE	exc=FillNonEmptyParseBuffer();
	if (exc != EOK)
		return exc;

	if (IMAP4_PARLIST_SDELIM != *m_lpszCurPos)
		return EUDFFORMAT;
	m_lpszCurPos++;

	if ((exc=FillNonEmptyParseBuffer()) != EOK)
		return exc;

	while (*m_lpszCurPos != IMAP4_PARLIST_EDELIM)
	{
		LPCTSTR	lpszHdrVal=NULL;
		UINT32	ulHdrLen=0;

		if ((exc=ExtractStringHdrVal(lpszHdrVal, ulHdrLen, FALSE, FALSE)) != EOK)
			return exc;

		if (ulHdrLen != 0)
		{
			LPCTSTR	lpszHE=(lpszHdrVal + ulHdrLen);
			TCHAR		tch=*lpszHE;

			*((LPTSTR) lpszHE) = _T('\0');
			exc = m_cfnCaller.CallMsgPartHdr(lpszFetchRsp, lpszSubHdr, lpszHdrVal);
			*((LPTSTR) lpszHE) = tch;

			if (exc != EOK)
				return exc;
		}
	}

	// skip end of paranthesised list
	m_lpszCurPos++;

	return FillNonEmptyParseBuffer();
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::HandleOptionalMsgPartParamsList (LPCTSTR	lpszFetchRsp, LPCTSTR lpszSubHdr)
{
	EXC_TYPE	exc=FillNonEmptyParseBuffer();
	if (exc != EOK)
		return exc;

	if (IMAP4_PARLIST_EDELIM == *m_lpszCurPos)
		return EOK;

	return HandleMsgPartParamsList(lpszFetchRsp, lpszSubHdr);
}

/*---------------------------------------------------------------------------*/

// body-language (if existing) may be either a string or a paranthesized list of values
EXC_TYPE CIMAP4FetchRspParser::HandleOptionalMsgPartLanguage (LPCTSTR lpszFetchRsp)
{
	EXC_TYPE	exc=FillNonEmptyParseBuffer();
	if (exc != EOK)
		return exc;

	if (IMAP4_PARLIST_SDELIM == *m_lpszCurPos)
	{
		if ((exc=HandleMsgPartList(lpszFetchRsp, pszStdContentLanguage)) != EOK)
			return exc;
	}
	else	// a simple  string value (or NIL)
	{
		if ((exc=HandleOptionalMsgPartHdr(lpszFetchRsp, pszStdContentLanguage)) != EOK)
			return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::HandleOptionalMsgPartDisposition (LPCTSTR lpszFetchRsp)
{
	EXC_TYPE	exc=FillNonEmptyParseBuffer();
	if (exc != EOK)
		return exc;

	if (IMAP4_PARLIST_SDELIM != *m_lpszCurPos)
		return HandleOptionalMsgPartHdr(lpszFetchRsp, pszStdContentDisposition);
	m_lpszCurPos++;

	// structure is disposition type string, followed by attribute/value pairs
	LPCTSTR	lpszHdrVal=NULL;
	UINT32	ulHdrLen=0;
	if ((exc=ExtractStringHdrVal(lpszHdrVal, ulHdrLen, FALSE, FALSE)) != EOK)
		return exc;
	if (0 == ulHdrLen)
		return EEMPTYENTRY;

	LPCTSTR	lpszHE=(lpszHdrVal + ulHdrLen);
	TCHAR		tch=*lpszHE;

	*((LPTSTR) lpszHE) = _T('\0');
	exc = m_cfnCaller.CallMsgPairHdr(lpszFetchRsp, pszStdContentDisposition, lpszHdrVal, _T(""));
	*((LPTSTR) lpszHE) = tch;

	if (exc != EOK)
		return exc;

	if ((exc=HandleOptionalMsgPartParamsList(lpszFetchRsp, pszStdContentDisposition)) != EOK)
		return exc;

	if ((exc=FillNonEmptyParseBuffer()) != EOK)
		return exc;

	// skip end of list
	if (IMAP4_PARLIST_EDELIM != *m_lpszCurPos)
		return EUNMATCHEDLISTS;
	m_lpszCurPos++;

	return FillNonEmptyParseBuffer();
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::ReReportContentType (LPCTSTR	lpszFetchRsp,
																	 LPCTSTR	lpszType,
																	 LPCTSTR lpszSubType)
{
	TCHAR		szMIMETag[MAX_RFC822_MIMETAG_LEN+2]=_T("");
	EXC_TYPE	exc=::BuildRFC822MIMETag(lpszType, lpszSubType, szMIMETag, MAX_RFC822_MIMETAG_LEN);
	if (exc != EOK)
		return exc;

	if ((exc=m_cfnCaller.CallMsgPartHdr(lpszFetchRsp, pszStdContentTypeHdr, szMIMETag)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::HandleMsgPartSimple (LPCTSTR lpszFetchRsp)
{
	// order is: type, sub-type, params-list, id, description, encoding, size
	TCHAR		szMIMETag[MAX_RFC822_MIMETAG_LEN+2]=_T("");
	EXC_TYPE	exc=HandleMsgPartContentType(lpszFetchRsp, szMIMETag, MAX_RFC822_MIMETAG_LEN);
	if (exc != EOK)
		return exc;

	LPCTSTR	lpszType=szMIMETag, lpszSubType=_tcschr(szMIMETag, RFC822_MIMETAG_SEP);
	if (NULL == lpszSubType)
		return ELIST;

	*((LPTSTR) lpszSubType) = _T('\0');
	UINT32	ulTLen=(lpszSubType - lpszType);

	lpszSubType++;
	UINT32	ulSTLen=_tcslen(lpszSubType);

	if ((exc=HandleMsgPartParamsList(lpszFetchRsp, pszStdContentTypeHdr)) != EOK)
		return exc;

	if ((exc=HandleMsgPartHdr(lpszFetchRsp, pszStdContentIDHdr)) != EOK)
		return exc;
	if ((exc=HandleMsgPartHdr(lpszFetchRsp, pszStdContentDescription)) != EOK)
		return exc;
	if ((exc=HandleMsgPartHdr(lpszFetchRsp, pszStdContentXferEncoding)) != EOK)
		return exc;
	if ((exc=HandleMsgPartHdr(lpszFetchRsp, pszStdContentLengthHdr)) != EOK)
		return exc;

	BOOL	fIsExtensibleBody=(0 == _tcsicmp(lpszFetchRsp, IMAP4_BODYSTRUCT));

	// this is a special case (observed in Exchange 2000) that needs handling
	BOOL	fIsMultipartEmbedded=(IsRFC822MIMEMultipartType(lpszType, ulTLen) && (ulSTLen != 0));

	if (_tcsicmp(lpszType, pszMIMETextType) == 0)
	{
		if ((exc=FillNonEmptyParseBuffer()) != EOK)
			return exc;

		if (_istdigit(*m_lpszCurPos))
		{
			if ((exc=HandleMsgPartHdr(lpszFetchRsp, IMAP4_RFC822SIZE)) != EOK)
				return exc;
		}
#if FALSE
		// this is a special case (observed in Exchange2000)
		else if (IMAP4_PARLIST_SDELIM == *m_lpszCurPos)
		{
			//		Call again the callback with the "Content-Type: message/rcf822" header
			// in order to make the "multipart/xxx" the "super" type
			if ((exc=ReReportContentType(lpszFetchRsp, pszMIMEMessageType, pszMIMERfc822SubType)) != EOK)
				return exc;

			if ((exc=HandleEmbeddedMsgPart(lpszFetchRsp)) != EOK)
				return exc;
		}
#endif
	}
	else if (IsRFC822EmbeddedMsg(lpszType, ulTLen, lpszSubType, ulSTLen) || fIsMultipartEmbedded)
	{
		if (fIsMultipartEmbedded)
		{
			//		Call again the callback with the "Content-Type: message/rcf822" header
			// in order to make the "multipart/xxx" the "super" type
			if ((exc=ReReportContentType(lpszFetchRsp, pszMIMEMessageType, pszMIMERfc822SubType)) != EOK)
				return exc;
		}

		if ((exc=HandleEmbeddedMsgPart(lpszFetchRsp)) != EOK)
			return exc;
	}

	// optional: MD5, disposition and more extension data
	if ((exc=FillNonEmptyParseBuffer()) != EOK)
		return exc;

	if (*m_lpszCurPos != IMAP4_PARLIST_EDELIM)
	{
		if (!fIsExtensibleBody)
			return EPARSETABLES;

		if ((exc=HandleOptionalMsgPartHdr(lpszFetchRsp, pszStdContentMD5Hdr)) != EOK)
			return exc;
		if ((exc=HandleOptionalMsgPartDisposition(lpszFetchRsp)) != EOK)
			return exc;
		if ((exc=HandleOptionalMsgPartLanguage(lpszFetchRsp)) != EOK)
			return exc;

		// skip ending list separator
		if ((exc=FillNonEmptyParseBuffer()) != EOK)
			return exc;
		if (*m_lpszCurPos != IMAP4_PARLIST_EDELIM)
			return ESTATE;
	}

	// skip end of list separator
	m_lpszCurPos++;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::HandleMsgPartMultipart (LPCTSTR lpszFetchRsp)
{
	TCHAR		szBodyTag[MAX_RFC822_MIMETAG_LEN+2], *lpszBT=szBodyTag;
	UINT32	ulRemLen=MAX_RFC822_MIMETAG_LEN;
	EXC_TYPE	exc=strlinsstr(&lpszBT, pszMIMEMultipartType, &ulRemLen);
	if (exc != EOK)
		return exc;
	if ((exc=strlinsch(&lpszBT, RFC822_MIMETAG_SEP, &ulRemLen)) != EOK)
		return exc;

	UINT32	ulMPLen=0;
	if ((exc=CopyStringHdrVal(lpszBT, ulRemLen, ulMPLen, FALSE, FALSE)) != EOK)
		return exc;

	// asume "mixed" as default
	if (0 == ulMPLen)
	{
		if ((exc=strlinsstr(&lpszBT, pszMIMEMixedSubType, &ulRemLen)) != EOK)
			return exc;
	}

	if ((exc=m_cfnCaller.CallMsgPartHdr(lpszFetchRsp, pszStdContentTypeHdr, szBodyTag)) != EOK)
		return exc;

	// skip any further body extension data if non extensible
	BOOL	fIsExtensibleBody=(0 == _tcsicmp(lpszFetchRsp, IMAP4_BODYSTRUCT));
	if (!fIsExtensibleBody)
	{
		// make sure no further data for non-extensible BODY
		if ((exc=FillNonEmptyParseBuffer()) != EOK)
			return exc;

		if (*m_lpszCurPos != IMAP4_PARLIST_EDELIM)
			return EPARSETABLES;

		return EOK;
	}

	// order is: body-content-type-list, body-disposition-list, body-language
	if ((exc=HandleOptionalMsgPartParamsList(lpszFetchRsp, pszStdContentTypeHdr)) != EOK)
		return exc;
	if ((exc=HandleOptionalMsgPartDisposition(lpszFetchRsp)) != EOK)
		return exc;
	if ((exc=HandleOptionalMsgPartLanguage(lpszFetchRsp)) != EOK)
		return exc;

	// handle any more extension data
	for (exc=FillNonEmptyParseBuffer(); (*m_lpszCurPos != IMAP4_PARLIST_EDELIM) && (EOK == exc); exc=FillNonEmptyParseBuffer())
	{

		if (IMAP4_PARLIST_SDELIM == *m_lpszCurPos)
		{
			if ((exc=HandleMsgPartParamsList(lpszFetchRsp, _T("X-$$$:"))) != EOK)
				return exc;
			continue;
		}

		if ((exc=HandleMsgPartHdr(lpszFetchRsp, _T("X-$$$:"))) != EOK)
			return exc;
	}

	// NOTE !!! do not skip end of list - it is skipped by caller !!
	return exc;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::ParseMsgPart (LPCTSTR lpszFetchRsp, LPCTSTR lpszPartPrefix)
{
	EXC_TYPE	exc=FillNonEmptyParseBuffer();
	if (exc != EOK)
		return exc;

	// check if have sub-part
	BOOLEAN	fIsMultipart=FALSE;
	if (IMAP4_PARLIST_SDELIM == *m_lpszCurPos)
	{
		m_lpszCurPos++;
		fIsMultipart = TRUE;
	}

	m_cfnCaller.SetMsgPart(lpszPartPrefix);

	for (UINT32	ulSubPartNum=1; ; ulSubPartNum++)
	{
		// build msg part for this sub-part
		CIMAP4MsgPartHandler	mph;

		// add the part prefix (if any)
		if (!IsEmptyStr(lpszPartPrefix))
		{
			if ((exc=mph.SetPartID(lpszPartPrefix)) != EOK)
				return exc;
			if ((exc=mph.AddChar(IMAP4_BODYPART_DELIM)) != EOK)
				return exc;
		}

		if ((exc=mph.AddNum(ulSubPartNum)) != EOK)
			return exc;

		if ((exc=FillNonEmptyParseBuffer()) != EOK)
			return exc;

		LPCTSTR	lpszNewPartID=mph.GetPartID();
		m_cfnCaller.SetMsgPart(lpszNewPartID);

		if (IMAP4_PARLIST_SDELIM == *m_lpszCurPos)
		{
			if ((exc=ParseMsgPart(lpszFetchRsp, lpszNewPartID)) != EOK)
				return exc;
		}
		else
		{
			if ((exc=HandleMsgPartSimple(lpszFetchRsp)) != EOK)
				return exc;
		}

		if ((exc=FillNonEmptyParseBuffer()) != EOK)
			return exc;

		// restore original part ID
		m_cfnCaller.SetMsgPart(lpszPartPrefix);

		// assume 1st non-starting parantheses signals end of sub-parts
		if (*m_lpszCurPos != IMAP4_PARLIST_SDELIM)
			break;

		m_lpszCurPos++;	// prepare for next sub-part
	}

	// restore original part ID
	m_cfnCaller.SetMsgPart(lpszPartPrefix);

	if (fIsMultipart)
	{
		if ((exc=HandleMsgPartMultipart(lpszFetchRsp)) != EOK)
			return exc;

		// at end of multipart handling the current position MUST be the end of list (see code)
		if (*m_lpszCurPos != IMAP4_PARLIST_EDELIM)
			return ESTATE;
		m_lpszCurPos++;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::ParseBodyStructFetchRsp (LPCTSTR		lpszFetchRsp,
																		  const BOOL	fIsRecursive)
{
	EXC_TYPE	exc=FillNonEmptyParseBuffer();
	if (exc != EOK)
		return exc;

	if (IMAP4_PARLIST_SDELIM != *m_lpszCurPos)
		return CheckNILParseBuffer(FALSE);

	m_lpszCurPos++;
	if (fIsRecursive)
		return ParseMsgPart(lpszFetchRsp, m_cfnCaller.GetMsgPart());

	m_cfnCaller.SetMsgPart(_T(""));

	if ((exc=ParseMsgPart(lpszFetchRsp, _T(""))) != EOK)
		return exc;

	m_cfnCaller.ClearMsgPart();
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::ParseEnvelopeUnlimitedLiteralHdrVal (LPCTSTR lpszFetchRsp, LPCTSTR lpszSubHdr)
{
	UINT32	ulHdrLen=0;
	EXC_TYPE	exc=ExtractLiteralCount(ulHdrLen);
	if (exc != EOK)
		return exc;

	// ignore anything till end of line since literal follows on next line
	if ((exc=SkipToEndOfLine()) != EOK)
		return exc;

	for (UINT32	ulRemLen=ulHdrLen; ulRemLen > 0; )
	{
		const UINT32	ulMaxReadLen=min(ulRemLen, m_ulRspBufLen);
		const UINT32	oLen=(UINT32) m_SBSock.Fill(m_lpszRspBuf, (size_t) ulMaxReadLen, (SINT32) m_ulRspTimeout);
		if (oLen != ulMaxReadLen)
			return ENOTCONNECTION;

		m_lpszRspBuf[oLen] = _T('\0');
		m_lpszCurPos = &m_lpszRspBuf[oLen];

		// use first read to invoke callback
		if (ulRemLen == ulHdrLen)
		{
			if ((exc=m_cfnCaller.CallEnvelopeHdrVal(lpszFetchRsp, lpszSubHdr, m_lpszRspBuf)) != EOK)
				return exc;
		}

		ulRemLen -= oLen;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::ParseEnvelopeUnlimitedUnquotedHdrVal (LPCTSTR lpszFetchRsp, LPCTSTR lpszSubHdr)
{
	EXC_TYPE	exc=EOK;
	BOOLEAN	fCfnCalled=FALSE;
	LPCTSTR	lpszHdrVal=NULL;

	for ( ; ; )
	{
		BOOLEAN	fMoreHdrData=TRUE;

		lpszHdrVal = m_lpszCurPos;

		for ( ; (*m_lpszCurPos != _T('\0')); m_lpszCurPos++)
			if (_istspace(*m_lpszCurPos) || (IMAP4_PARLIST_EDELIM == *m_lpszCurPos))
			{
				fMoreHdrData = FALSE;
				break;
			}

		if (!fMoreHdrData)
			break;

		// if not started from start of buffer, then try again
		if (lpszHdrVal != m_lpszRspBuf)
		{
			if ((exc=RefillFetchParseBuffer(lpszHdrVal)) != EOK)
				return exc;

			continue;
		}

		// check if need to invoke callback
		if (!fCfnCalled)
		{
			if ((exc=m_cfnCaller.CallEnvelopeHdrVal(lpszFetchRsp, lpszSubHdr, lpszHdrVal)) != EOK)
				return exc;

			fCfnCalled = TRUE;
		}

		if ((exc=RefillFetchParseBuffer()) != EOK)
			return exc;
	}

	if (!fCfnCalled)
	{
		TCHAR	tch=*m_lpszCurPos;

		*((LPTSTR) m_lpszCurPos) = _T('\0');
		exc = m_cfnCaller.CallEnvelopeHdrVal(lpszFetchRsp, lpszSubHdr, lpszHdrVal);
		*((LPTSTR) m_lpszCurPos) = tch;

		return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::ParseEnvelopeUnlimitedQuotedHdrVal (LPCTSTR lpszFetchRsp, LPCTSTR lpszSubHdr)
{
	EXC_TYPE	exc=EOK;
	BOOLEAN	fCfnCalled=FALSE;
	LPCTSTR	lpszHdrVal=NULL;

	m_lpszCurPos++;	// skip initial quote

	for ( ; ; )
	{
		BOOLEAN	fMoreHdrData=TRUE;

		lpszHdrVal = m_lpszCurPos;

		for ( ; (*m_lpszCurPos != _T('\0')); m_lpszCurPos++)
			if (IsIMAP4QuoteDelimCurPos())
			{
				fMoreHdrData = FALSE;
				break;
			}

		if (!fMoreHdrData)
			break;

		// if not started from start of buffer, then try again
		if (lpszHdrVal != m_lpszRspBuf)
		{
			if ((exc=RefillFetchParseBuffer(lpszHdrVal)) != EOK)
				return exc;

			continue;
		}

		// check if need to invoke callback
		if (!fCfnCalled)
		{
			if ((exc=m_cfnCaller.CallEnvelopeHdrVal(lpszFetchRsp, lpszSubHdr, lpszHdrVal)) != EOK)
				return exc;

			fCfnCalled = TRUE;
		}

		if ((exc=RefillFetchParseBuffer()) != EOK)
			return exc;
	}

	// do not bother if empty header value
	if ((!fCfnCalled) && (m_lpszCurPos > lpszHdrVal))
	{
		TCHAR	tch=*m_lpszCurPos;

		*((LPTSTR) m_lpszCurPos) = _T('\0');
		exc = m_cfnCaller.CallEnvelopeHdrVal(lpszFetchRsp, lpszSubHdr, lpszHdrVal);
		*((LPTSTR) m_lpszCurPos) = tch;
	}

	m_lpszCurPos++;	// skip ending quote
	return exc;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::ParseEnvelopeUnlimitedStringHdrVal (LPCTSTR lpszFetchRsp, LPCTSTR lpszSubHdr)
{
	EXC_TYPE	exc=EOK;

	if ((exc=FillNonEmptyParseBuffer()) != EOK)
		return exc;

	// handle literal header value
	if (IMAP4_OCTCNT_SDELIM == *m_lpszCurPos)
		return ParseEnvelopeUnlimitedLiteralHdrVal(lpszFetchRsp, lpszSubHdr);

	// handle NIL or unquoted value
	if (IMAP4_QUOTE_DELIM != *m_lpszCurPos)
	{
		// don't bother the callback if empty header
		if (EOK == (exc=CheckNILParseBuffer(FALSE)))
			return EOK;

		// this is not a NIL atom - extract as non-quoted
		if (EWILDCARD != exc)
			return exc;

		return ParseEnvelopeUnlimitedUnquotedHdrVal(lpszFetchRsp, lpszSubHdr);
	}

	return ParseEnvelopeUnlimitedQuotedHdrVal(lpszFetchRsp, lpszSubHdr);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::ParseEnvelopeStringHdrVal (LPCTSTR			lpszFetchRsp,
																			 LPCTSTR			lpszSubHdr,
																			 const BOOLEAN	fAllowOverflow)
{
	LPCTSTR	lpszHdrVal=NULL;
	UINT32	ulHdrLen=0;
	EXC_TYPE	exc=ExtractStringHdrVal(lpszHdrVal, ulHdrLen, FALSE, fAllowOverflow);
	if (exc != EOK)
		return exc;

	// don't bother the callback if empty header
	if (0 == ulHdrLen)
		return EOK;

	LPTSTR	lpszHE=(LPTSTR) (lpszHdrVal + ulHdrLen);
	TCHAR		tch=*lpszHE;

	*lpszHE = _T('\0');
	exc = m_cfnCaller.CallEnvelopeHdrVal(lpszFetchRsp, lpszSubHdr, lpszHdrVal);
	*lpszHE = tch;

	return exc;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::AddEnvelopeAddrHdrVal (const UINT32	ulMaxHdrLen,
																		LPTSTR&			lpszAPos,
																		UINT32&			ulRemLen,
																		UINT32&			ulHdrLen)
{
	LPCTSTR	lpszHdrVal=NULL;
	EXC_TYPE	exc=ExtractStringHdrVal(lpszHdrVal, ulHdrLen, FALSE, FALSE);
	if (exc != EOK)
		return exc;

	// silently ignore address parts that are too long
	if (ulHdrLen >= ulMaxHdrLen)
		ulHdrLen = 0;

	if (ulHdrLen != 0)
	{
		if ((exc=strlinschars(&lpszAPos, lpszHdrVal, ulHdrLen, &ulRemLen)) != EOK)
			return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::ParseEnvelopeAddrHdrVal (LPCTSTR			lpszFetchRsp,
																		  LPCTSTR			lpszSubHdr,
																		  const BOOLEAN	fSingleInstance)
{
	EXC_TYPE	exc=EOK;

	if ((exc=FillNonEmptyParseBuffer()) != EOK)
		return exc;

	// allow NIL as hdr value
	if (IMAP4_PARLIST_SDELIM != *m_lpszCurPos)
	{
		if ((exc=CheckNILParseBuffer(FALSE)) != EOK)
			return exc;

		return EOK;
	}

	// non-NIL list
	m_lpszCurPos++;	// skip list starter

	if ((exc=FillNonEmptyParseBuffer()) != EOK)
		return exc;

	for (BOOLEAN fCfnCalled=FALSE; IMAP4_PARLIST_EDELIM != *m_lpszCurPos; )
	{
		if (IMAP4_PARLIST_SDELIM != *m_lpszCurPos)
			return EUNMATCHEDLISTS;

		m_lpszCurPos++;	// skip address structure starter

		//	personal-name
		TCHAR		szDisplayName[MAX_PERSONAL_EMAIL_NAMELEN+2]=_T(""), *lspDN=szDisplayName;
		UINT32	ulDNRemLen=MAX_PERSONAL_EMAIL_NAMELEN, ulDNLen=0;
		if ((exc=AddEnvelopeAddrHdrVal(MAX_PERSONAL_EMAIL_NAMELEN, lspDN, ulDNRemLen, ulDNLen)) != EOK)
			return exc;

		// just in case of non-standard address structure
		static const UINT32	MAX_ENVADDR_COMPLEN=max(MAX_DNS_DOMAIN_LEN,MAX_USER_PART_LEN);

		// return-path
		TCHAR		szEMDomain[MAX_ENVADDR_COMPLEN+2]=_T(""), *dsp=szEMDomain;
		UINT32	ulEMRemLen=MAX_ENVADDR_COMPLEN, ulEMLen=0;
		if ((exc=AddEnvelopeAddrHdrVal(MAX_ENVADDR_COMPLEN, dsp, ulEMRemLen, ulEMLen)) != EOK)
			return exc;

		// mailbox-name
		TCHAR		szMboxName[MAX_ENVADDR_COMPLEN+2]=_T(""), *lspMN=szMboxName;
		UINT32	ulMNRemLen=MAX_ENVADDR_COMPLEN, ulMNLen=0;
		if ((exc=AddEnvelopeAddrHdrVal(MAX_ENVADDR_COMPLEN, lspMN, ulMNRemLen, ulMNLen)) != EOK)
			return exc;

		// host domain
		TCHAR		szHostDomain[MAX_ENVADDR_COMPLEN+2]=_T(""), *lspHD=szHostDomain;
		UINT32	ulHDRemLen=MAX_ENVADDR_COMPLEN, ulHDLen=0;
		if ((exc=AddEnvelopeAddrHdrVal(MAX_ENVADDR_COMPLEN, lspHD, ulHDRemLen, ulHDLen)) != EOK)
			return exc;

		// at end of address structure there must be a list end delimiter
		//
		// Note: although RFC2060 states only 4 elements to an address structure, some servers
		//			(e.g., SW.COM KX-4.2) do not handle correctly names with quotes in them, and
		//			create a list with more elements. So, if not found end of list, we keep going
		//			until we find it, and assume that the last 3 components are according to RFC2060.
		if ((exc=FillNonEmptyParseBuffer()) != EOK)
			return exc;

		enum { ENVADDR_EMDOMAIN=0, ENVADDR_MBNAME, ENVADDR_HOSTDOMAIN, NUM_ENVADDR_COMPS };
		LPTSTR	lpszComps[NUM_ENVADDR_COMPS]={ szEMDomain, szMboxName, szHostDomain };
		UINT32	ulExtraCompsIndex=0;

		for (ulExtraCompsIndex=0; IMAP4_PARLIST_EDELIM != *m_lpszCurPos; ulExtraCompsIndex++)
		{
			LPCTSTR	lpszEMDomain=lpszComps[(ulExtraCompsIndex + ENVADDR_EMDOMAIN) % NUM_ENVADDR_COMPS];

			// obviously, the "EMDomain" is part of the personal name since more to come
			if ((ulEMLen=_tcslen(lpszEMDomain)) != 0)
			{
				if ((exc=strlinschars(&lspDN, lpszEMDomain, ulEMLen, &ulDNRemLen)) != EOK)
					return exc;
				ulDNLen += ulEMLen;
			}

			// we always read the "last" component
			LPTSTR	lpszLastComp=lpszComps[(ulExtraCompsIndex % NUM_ENVADDR_COMPS)];
			LPTSTR	lspLC=lpszLastComp;
			UINT32	ulLCRemLen=MAX_ENVADDR_COMPLEN, ulLCLen=0;

			if ((exc=AddEnvelopeAddrHdrVal(MAX_ENVADDR_COMPLEN, lspLC, ulLCRemLen, ulLCLen)) != EOK)
				return exc;

			if ((exc=FillNonEmptyParseBuffer()) != EOK)
				return exc;
		}

		m_lpszCurPos++;	// skip list end delimiter

		// compensate for possible extra parameters in list
		LPCTSTR	lpszMboxName=lpszComps[(ulExtraCompsIndex + ENVADDR_MBNAME) % NUM_ENVADDR_COMPS];
		BOOLEAN	fHaveMboxName=((ulMNLen=_tcslen(lpszMboxName)) != 0);

		// detect empty/missing host name
		//
		// Note: RFC2060 states that if unknown it must be NIL, but many servers use
		//			a ".MISSING-HOST-NAME" or a variation of it - usually with a '.' as 1st char
		static const TCHAR szIMAP4MissingKeyword[]=_T(".MISSING");
		LPCTSTR	lpszHostName=lpszComps[(ulExtraCompsIndex + ENVADDR_HOSTDOMAIN) % NUM_ENVADDR_COMPS];
		BOOLEAN	fHaveHost=(((ulHDLen=_tcslen(lpszHostName)) != 0) && (*lpszHostName != _T('.')));

		// if only display name available, then it will appear as the mailbox name (all others are empty/bad)
		if (0 == (ulDNLen=_tcslen(szDisplayName)))
		{
			if (fHaveMboxName && (!fHaveHost))
			{
				_tcscpy(szDisplayName, lpszMboxName);
				ulDNLen = ulMNLen;

				*((LPTSTR) lpszMboxName) = _T('\0');
				ulMNLen = 0;

				fHaveMboxName = FALSE;
			}
		}

		// if empty or single quoted display name, then mark as empty
		if ((IMAP4_QUOTE_DELIM == szDisplayName[0]) && (ulDNLen <= 2))
		{
			szDisplayName[0] = _T('\0');
			ulDNLen = 0;
		}

		// if have display name but bad/illegal e-mail address, then generate a non-existing address
		if (ulDNLen != 0)
		{
			if ((!fHaveMboxName) || ((exc=ValidateRFC822LocalMailPart(lpszMboxName, ulMNLen)) != EOK))
			{
				_tcscpy((LPTSTR) lpszMboxName, szNonMailUserLocalMailPart);
				ulMNLen = _tcslen(lpszMboxName);
				fHaveMboxName = TRUE;
			}

			if ((!fHaveHost) || ((exc=ValidateRFC822MailDomainPart(lpszHostName, ulHDLen)) != EOK))
			{
				_tcscpy((LPTSTR) lpszHostName, szNonMailUserMailDomainPart);
				ulHDLen = _tcslen(lpszHostName);
				fHaveHost = TRUE;
			}
		}

		if (fHaveHost && fHaveMboxName && ((!fSingleInstance) || (!fCfnCalled)))
		{
			// build "address pair"
			TCHAR	szAddrVal[MAX_ADDRPAIR_EMAIL_LEN+2]=_T("");
			CStrlBuilder	strb(szAddrVal, MAX_ADDRPAIR_EMAIL_LEN);

			if (ulDNLen != 0)
			{
				// if display name quoted, then use enclosing quotes
				if ((IMAP4_QUOTE_DELIM == szDisplayName[0]) && (IMAP4_QUOTE_DELIM == szDisplayName[ulDNLen-1]))
				{
					// make sure quote is not used in personal name - if so, then replace it with single quote
					for (LPTSTR	lpszQTP=_tcschr(&szDisplayName[1], IMAP4_QUOTE_DELIM); lpszQTP != NULL; lpszQTP=_tcschr(lpszQTP, IMAP4_QUOTE_DELIM))
					{
						if (lpszQTP < (&szDisplayName[ulDNLen-1]))
							*lpszQTP = _T('\'');
						else
							break;
					}

					if ((exc=strb.AddChars(szDisplayName, ulDNLen)) != EOK)
						return exc;
				}
				else	// quote display name (if unquoted originally)
				{
					// make sure quote is not used in personal name - if so, then replace it with single quote
					for (LPTSTR	lpszQTP=_tcschr(szDisplayName, IMAP4_QUOTE_DELIM); lpszQTP != NULL; lpszQTP=_tcschr(lpszQTP, IMAP4_QUOTE_DELIM))
						*lpszQTP = _T('\'');

					if ((exc=strb.AddChar(IMAP4_QUOTE_DELIM)) != EOK)
						return exc;
					if ((exc=strb.AddChars(szDisplayName, ulDNLen)) != EOK)
						return exc;
					if ((exc=strb.AddChar(IMAP4_QUOTE_DELIM)) != EOK)
						return exc;
				}

				if ((exc=strb.AddChar(_T(' '))) != EOK)
					return exc;
			}

			if ((exc=strb.AddChar(EMAIL_PATH_SDELIM)) != EOK)
				return exc;

			LPCTSTR	lpszEMVal=strb.GetCurPos();
			if ((exc=strb.AddChars(lpszMboxName, ulMNLen)) != EOK)
				return exc;
			if ((exc=strb.AddChar(INET_DOMAIN_SEP)) != EOK)
				return exc;
			if ((exc=strb.AddChars(lpszHostName, ulHDLen)) != EOK)
				return exc;

			// if invalid e-mail address formed, then ignore it
			if (EOK == (exc=ValidateRFC822Email(lpszEMVal)))
			{
				if ((exc=strb.AddChar(EMAIL_PATH_EDELIM)) != EOK)
					return exc;

				if ((exc=m_cfnCaller.CallEnvelopeHdrVal(lpszFetchRsp, lpszSubHdr, szAddrVal)) != EOK)
					return exc;

				fCfnCalled = TRUE;
			}
		}

		if ((exc=FillNonEmptyParseBuffer()) != EOK)
			return exc;
	}

	m_lpszCurPos++;	// skip address list structures delimiter
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::ParseEnvelopeFetchRsp (LPCTSTR	lpszFetchRsp)
{
	EXC_TYPE	exc=EOK;

	if ((exc=FillNonEmptyParseBuffer()) != EOK)
		return exc;

	// allow "NIL" as envelope
	if (IMAP4_PARLIST_SDELIM != *m_lpszCurPos)
	{
		if ((exc=CheckNILParseBuffer(FALSE)) != EOK)
			return exc;

		return EOK;
	}

	m_lpszCurPos++;

	if ((exc=ParseEnvelopeStringHdrVal(lpszFetchRsp, pszStdDateHdr, FALSE)) != EOK)
		return exc;
	if ((exc=ParseEnvelopeUnlimitedStringHdrVal(lpszFetchRsp, pszStdSubjectHdr)) != EOK)
		return exc;
	if ((exc=ParseEnvelopeAddrHdrVal(lpszFetchRsp, pszStdFromHdr, TRUE)) != EOK)
		return exc;
	if ((exc=ParseEnvelopeAddrHdrVal(lpszFetchRsp, pszStdSenderHdr, TRUE)) != EOK)
		return exc;
	if ((exc=ParseEnvelopeAddrHdrVal(lpszFetchRsp, pszStdReplyToHdr, FALSE)) != EOK)
		return exc;
	if ((exc=ParseEnvelopeAddrHdrVal(lpszFetchRsp, pszStdToHdr, FALSE)) != EOK)
		return exc;
	if ((exc=ParseEnvelopeAddrHdrVal(lpszFetchRsp, pszStdCcHdr, FALSE)) != EOK)
		return exc;
	if ((exc=ParseEnvelopeAddrHdrVal(lpszFetchRsp, pszStdBccHdr, FALSE)) != EOK)
		return exc;
	if ((exc=ParseEnvelopeStringHdrVal(lpszFetchRsp, pszStdInReplyToHdr, TRUE)) != EOK)
		return exc;
	if ((exc=ParseEnvelopeStringHdrVal(lpszFetchRsp, pszStdMessageIDHdr, FALSE)) != EOK)
		return exc;

	// at end of envelope parsing current position must be closing parantheses
	if ((exc=FillNonEmptyParseBuffer()) != EOK)
		return exc;
	if (IMAP4_PARLIST_EDELIM != *m_lpszCurPos)
		return EUDFFORMAT;

	m_lpszCurPos++;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::ParseInternalDateFetchRsp (LPCTSTR lpszFetchRsp)
{
	LPCTSTR	lpszIDate=NULL;

	for (UINT32 ulRdx=0; ; ulRdx++)
	{
		EXC_TYPE	exc=FillNonEmptyParseBuffer();
		if (exc != EOK)
			return exc;

		if (!IsIMAP4QuoteDelimCurPos())
			return ELOGNAMESYNTAX;

		for (lpszIDate=m_lpszCurPos, m_lpszCurPos++; *m_lpszCurPos != _T('\0'); m_lpszCurPos++)
			if (IsIMAP4QuoteDelimCurPos())
				break;

		// if reached end of string then refill and retry - date is terminated by a quote
		if (_T('\0') == *m_lpszCurPos)
		{
			if ((exc=RefillFetchParseBuffer(lpszIDate)) != EOK)
				return exc;

			continue;
		}

		break;
	}

	// create EOS terminate date
	*((LPTSTR) m_lpszCurPos) = _T('\0');

	// clean up any leading/trailing spaces
	for (lpszIDate++; _istspace(*lpszIDate) && (*lpszIDate != _T('\0')); lpszIDate++);

	for (LPCTSTR	lpszEDate=m_lpszCurPos; lpszEDate > lpszIDate; lpszEDate--)
		if ((*lpszEDate != _T('\0')) && (!_istspace(*lpszEDate)))
		{
			*((LPTSTR) (lpszEDate+1)) = _T('\0');
			break;
		}

	m_lpszCurPos++;

	return m_cfnCaller.CallInternalDateHdr(lpszFetchRsp, lpszIDate);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::ParseFetchEquivalentRsp (LPCTSTR	lpszFetchRsp, LPCTSTR lpszSection)
{
	const UINT32	MAX_EQUIV_RSP=((2 * MAX_IMAP4_OPCODE_LEN) + 2);
	TCHAR				szEquiv[MAX_EQUIV_RSP+2]=_T(""), *lsp=szEquiv;
	UINT32			ulRemLen=MAX_EQUIV_RSP;
	EXC_TYPE			exc=strlinsstr(&lsp, lpszFetchRsp, &ulRemLen);
	if (exc != EOK)
		return exc;

	if ((exc=strlinsch(&lsp, IMAP4_BRCKT_SDELIM, &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsstr(&lsp, lpszSection, &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsch(&lsp, IMAP4_BRCKT_EDELIM, &ulRemLen)) != EOK)
		return exc;

	return ParseBodyFetchRsp(szEquiv);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::PreProcessSectionFetchRsp (LPTSTR lpszSection, const UINT32 ulMaxLen)
{
	LPTSTR	lsp=lpszSection;
	UINT32	ulRemLen=ulMaxLen;
	EXC_TYPE	exc=strlinsch(&lsp, IMAP4_BRCKT_SDELIM, &ulRemLen);
	if (exc != EOK)
		return exc;

	// skip headers list response since it is of no interest (so far)
	if ((exc=FillNonEmptyParseBuffer()) != EOK)
		return exc;

	LPCTSTR	lpszSecStart=NULL;
	for (lpszSecStart=m_lpszCurPos; (*m_lpszCurPos != IMAP4_BRCKT_EDELIM) && (*m_lpszCurPos != _T(' ')) && (*m_lpszCurPos != IMAP4_PARLIST_SDELIM); m_lpszCurPos++)
	{
		if (_T('\0') == *m_lpszCurPos)
		{
			if ((exc=RefillFetchParseBuffer(lpszSecStart)) != EOK)
				return exc;

			lpszSecStart = m_lpszCurPos;
			continue;
		}
	}

	UINT32	ulSecLen=m_lpszCurPos - lpszSecStart;
	if (ulSecLen != 0)
	{
		if ((exc=strlinschars(&lsp, lpszSecStart, ulSecLen, &ulRemLen)) != EOK)
			return exc;
	}

	if (IMAP4_BRCKT_EDELIM != *m_lpszCurPos)
	{
		if ((exc=FillNonEmptyParseBuffer()) != EOK)
			return exc;

		if (*m_lpszCurPos != IMAP4_PARLIST_SDELIM)
			return EUNMATCHEDLISTS;

		m_lpszCurPos++;

		if ((exc=FillNonEmptyParseBuffer()) != EOK)
			return exc;

		// check (and skip) headers list
		while (*m_lpszCurPos != IMAP4_PARLIST_EDELIM)
		{
			LPCTSTR	lpszHdrName=NULL;

			for (lpszHdrName=m_lpszCurPos ; (*m_lpszCurPos != _T(' ')) && (IMAP4_PARLIST_EDELIM != *m_lpszCurPos) && (*m_lpszCurPos != _T('\0')); m_lpszCurPos++);

			if (_T('\0') == *m_lpszCurPos)
			{
				if ((exc=RefillFetchParseBuffer(lpszHdrName)) != EOK)
					return exc;

				continue;
			}

			UINT32	ulHdrLen=(m_lpszCurPos - lpszHdrName);
			TCHAR		chPos=(*m_lpszCurPos), chNext=*(m_lpszCurPos+1);

			// create EOS terminated hdr
			*((LPTSTR) m_lpszCurPos) = _T(':');
			*((LPTSTR) (m_lpszCurPos + 1)) = _T('\0');

			exc = m_cfnCaller.CallBodyHdrListVal(lpszHdrName);

			// restore original character(s)
			*((LPTSTR) m_lpszCurPos) = chPos;
			*((LPTSTR) (m_lpszCurPos + 1)) = chNext;

			if (exc != EOK)
				return exc;

			if ((exc=FillNonEmptyParseBuffer()) != EOK)
				return exc;
		}

		m_lpszCurPos++;

		if ((exc=FillNonEmptyParseBuffer()) != EOK)
			return exc;

		if (IMAP4_BRCKT_EDELIM != *m_lpszCurPos)
			return EUNMATCHEDLISTS;
	}

	m_lpszCurPos++;

	if ((exc=strlinsch(&lsp, IMAP4_BRCKT_EDELIM, &ulRemLen)) != EOK)
		return exc;

	if ((exc=FillNonEmptyParseBuffer()) != EOK)
		return exc;

	// check if have octet count
	if (IMAP4_OFFSET_SDELIM == *m_lpszCurPos)
	{
		m_lpszCurPos++;

		if ((exc=strlinsch(&lsp, IMAP4_OFFSET_SDELIM, &ulRemLen)) != EOK)
			return exc;

		if ((exc=FillNonEmptyParseBuffer()) != EOK)
			return exc;

		LPCTSTR	lpszOffset=NULL;
		while (IMAP4_OFFSET_EDELIM != *m_lpszCurPos)
		{
			for (lpszOffset=m_lpszCurPos; _istdigit(*m_lpszCurPos) && (*m_lpszCurPos != _T('\0')); m_lpszCurPos++);

			if (_T('\0') == *m_lpszCurPos)
			{
				if ((exc=RefillFetchParseBuffer(lpszOffset)) != EOK)
					return exc;
				continue;
			}
		}

		if (NULL == lpszOffset)
			return ETRANSID;

		UINT32	ulOffLen=(m_lpszCurPos - lpszOffset);
		if ((exc=strlinschars(&lsp, lpszOffset, ulOffLen, &ulRemLen)) != EOK)
			return exc;

		m_lpszCurPos++;

		if ((exc=strlinsch(&lsp, IMAP4_OFFSET_EDELIM, &ulRemLen)) != EOK)
			return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

typedef EXC_TYPE (*IMAP4_FETCHRSP_PARSER_CFN)(CIMAP4FetchRspParser&	frp, LPCTSTR lpszFetchRsp);

inline EXC_TYPE imap4ParseUIDFetchRsp (CIMAP4FetchRspParser&	frp, LPCTSTR	lpszFetchRsp)
{
	return frp.ParseUIDFetchRsp(lpszFetchRsp);
}

inline EXC_TYPE imap4ParseFlagsFetchRsp (CIMAP4FetchRspParser&	frp, LPCTSTR lpszFetchRsp)
{
	return frp.ParseFlagsFetchRsp(lpszFetchRsp);
}

inline EXC_TYPE imap4ParseBodyFetchRsp (CIMAP4FetchRspParser&	frp, LPCTSTR lpszFetchRsp)
{
	return frp.ParseBodyFetchRsp(lpszFetchRsp);
}

inline EXC_TYPE imap4ParseBodyStructFetchRsp (CIMAP4FetchRspParser&	frp, LPCTSTR lpszFetchRsp)
{
	return frp.ParseBodyStructFetchRsp(lpszFetchRsp);
}

inline EXC_TYPE imap4ParseEnvelopeFetchRsp (CIMAP4FetchRspParser&	frp, LPCTSTR lpszFetchRsp)
{
	return frp.ParseEnvelopeFetchRsp(lpszFetchRsp);
}

inline EXC_TYPE imap4ParseInternalDateFetchRsp (CIMAP4FetchRspParser&	frp, LPCTSTR lpszFetchRsp)
{
	return frp.ParseInternalDateFetchRsp(lpszFetchRsp);
}

inline EXC_TYPE imap4ParseRFC822FetchRsp (CIMAP4FetchRspParser&	frp, LPCTSTR	lpszFetchRsp)
{
	return frp.ParseRFC822FetchRsp(lpszFetchRsp);
}

inline EXC_TYPE imap4ParseRFC822HdrFetchRsp (CIMAP4FetchRspParser&	frp, LPCTSTR	lpszFetchRsp)
{
	return frp.ParseRFC822HdrFetchRsp(lpszFetchRsp);
}

inline EXC_TYPE imap4ParseRFC822TextFetchRsp (CIMAP4FetchRspParser&	frp, LPCTSTR lpszFetchRsp)
{
	return frp.ParseRFC822TextFetchRsp(lpszFetchRsp);
}

inline EXC_TYPE imap4ParseRFC822SizeFetchRsp (CIMAP4FetchRspParser&	frp, LPCTSTR	lpszFetchRsp)
{
	return frp.ParseRFC822SizeFetchRsp(lpszFetchRsp);
}

static const STR2PTRASSOC i4fra[]={
	{	IMAP4_FLAGS,			(LPVOID) imap4ParseFlagsFetchRsp				},
	{	IMAP4_UID,				(LPVOID)	imap4ParseUIDFetchRsp				},
	{	IMAP4_BODY,				(LPVOID) imap4ParseBodyFetchRsp				},
	{	IMAP4_BODYSTRUCT,		(LPVOID) imap4ParseBodyStructFetchRsp		},
	{	IMAP4_ENVELOPE,		(LPVOID) imap4ParseEnvelopeFetchRsp			},
	{	IMAP4_INTERNALDATE,	(LPVOID) imap4ParseInternalDateFetchRsp	},
	{	IMAP4_RFC822,			(LPVOID) imap4ParseRFC822FetchRsp			},
	{	IMAP4_RFC822HDR,		(LPVOID) imap4ParseRFC822HdrFetchRsp		},
	{	IMAP4_RFC822SIZE,		(LPVOID) imap4ParseRFC822SizeFetchRsp		},
	{	IMAP4_RFC822TEXT,		(LPVOID)	imap4ParseRFC822TextFetchRsp		},
	{	NULL,						NULL													}	// mark end
};

static const CStr2PtrMapper i4frMap(i4fra, 0, FALSE);

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::ParseFetchContents ()
{
	EXC_TYPE	exc=FillNonEmptyParseBuffer();
	if (exc != EOK)
		return exc;

	while (IMAP4_PARLIST_EDELIM != *m_lpszCurPos)
	{
		// extract FETCH modifier
		LPCTSTR	lpszFetchRsp=m_lpszCurPos;
		for (; *m_lpszCurPos != _T('\0'); m_lpszCurPos++)
			if (_istspace(*m_lpszCurPos) || (IMAP4_BRCKT_SDELIM == *m_lpszCurPos))
				break;

		// we must have a space or bracket delimited FETCH response value - otherwise assume continued on next buffer
		if (_T('\0') == *m_lpszCurPos)
		{
			if ((exc=RefillFetchParseBuffer(lpszFetchRsp)) != EOK)
				return exc;

			// restart from non-space
			for ( ; _istspace(*m_lpszCurPos) && (*m_lpszCurPos != _T('\0')); m_lpszCurPos++);

			continue;
		}

		// create an EOS terminated FETCH response string
		const UINT32	MAX_FETCHRSP_KWLEN=(MAX_FETCHRSP_MODLEN+2+MAX_DWORD_DISPLAY_LENGTH+2);
		TCHAR			szFetchRsp[MAX_FETCHRSP_KWLEN+2]=_T("");
		UINT32			ulFRLen=(m_lpszCurPos - lpszFetchRsp);
		if (ulFRLen > MAX_FETCHRSP_KWLEN)
			return EILLEGALOPCODE;

		_tcsncpy(szFetchRsp, lpszFetchRsp, ulFRLen);
		szFetchRsp[ulFRLen] = _T('\0');

		// handle special case of BODY with possible section
		if (IMAP4_BRCKT_SDELIM == *m_lpszCurPos)
		{
			m_lpszCurPos++;

			if ((exc=PreProcessSectionFetchRsp(&szFetchRsp[ulFRLen], (MAX_FETCHRSP_KWLEN-ulFRLen))) != EOK)
				return exc;
		}

		// check for BODY[section] and extract only the "pure" response
		LPTSTR	lpszSection=_tcschr(szFetchRsp, IMAP4_BRCKT_SDELIM);
		if (lpszSection != NULL)
			*lpszSection = _T('\0');

		LPVOID	pV=NULL;
		if ((exc=i4frMap.FindKey(szFetchRsp, pV)) != EOK)
			return exc;

		// restore section (if any)
		if (lpszSection != NULL)
			*lpszSection = IMAP4_BRCKT_SDELIM;

		IMAP4_FETCHRSP_PARSER_CFN	lpfnPcfn=(IMAP4_FETCHRSP_PARSER_CFN) pV;
		if (NULL == lpfnPcfn)
			return EBADADDR;

		if ((exc=m_cfnCaller.SetModifier(szFetchRsp)) != EOK)
			return exc;

		if ((exc=(*lpfnPcfn)(*this, szFetchRsp)) != EOK)
			return exc;

		m_cfnCaller.ClearModifier();

		if ((exc=FillNonEmptyParseBuffer()) != EOK)
			return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FetchRspParser::HandleUntaggedResponse (LPCTSTR lpszTag, LPCTSTR lpszOp)
{
	// ignore non-FETCH responses
	if (_tcsicmp(lpszOp, szIMAP4FetchCmd) != 0)
		return SkipToEndOfLine();

	// extract msg sequence number
	m_cfnCaller.Reset();

	EXC_TYPE	exc=m_cfnCaller.SetCurSeqNo(lpszTag);
	if (exc != EOK)
		return exc;

	// find start of FETCH response contents
	for ( ; (*m_lpszCurPos != IMAP4_PARLIST_SDELIM) && (*m_lpszCurPos != _T('\0')); m_lpszCurPos++);
	if (*m_lpszCurPos != IMAP4_PARLIST_SDELIM)
		return EUDFFORMAT;

	m_lpszCurPos++;	// skip returned parameters list

	if ((exc=ParseFetchContents()) != EOK)
		return exc;

	//parsing should stop at end of returned fetch list
	if (*m_lpszCurPos != IMAP4_PARLIST_EDELIM)
		return EUDFFORMAT;

	m_lpszCurPos++;
	m_cfnCaller.ClearCurSeqNo();
	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

EXC_TYPE imap4HandleFetchRspSync (ISockioInterface&	SBSock,
											 LPCTSTR					lpszTag,
											 IMAP4_FETCHRSP_CFN	lpfnHcfn,
											 LPVOID					pArg,
											 LPTSTR					lpszRspBuf,
											 const UINT32			ulMaxRspLen,
											 const UINT32			ulRspTimeout)
{
	if (IsEmptyStr(lpszTag))
		return EILLEGALOPCODE;

	if ((NULL == lpfnHcfn) || (NULL == lpszRspBuf) || (ulMaxRspLen < (MAX_IMAP4_CMD_LEN / 2)))
		return EBADADDR;

	CIMAP4FetchRspParser	frp(SBSock, lpszTag, lpfnHcfn, pArg, lpszRspBuf, ulMaxRspLen, ulRspTimeout);
	return frp.ParseResponse();
}

EXC_TYPE imap4HandleFetchRspSync (ISockioInterface&	SBSock,
											 LPCTSTR					lpszTag,
											 IMAP4_FETCHRSP_CFN	lpfnHcfn,
											 LPVOID					pArg,
											 const UINT32			ulRspTimeout)
{
	TCHAR		szRsp[MAX_IMAP4_DATA_LEN+2]=_T("");

	return imap4HandleFetchRspSync(SBSock, lpszTag, lpfnHcfn, pArg, szRsp, MAX_IMAP4_DATA_LEN, ulRspTimeout);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4FetchMsgRangeSync (ISockioInterface&		SBSock,
											LPCTSTR					lpszITag,			// NULL == auto-generate
											const BOOLEAN			fIsUIDFetch,
											LPCTSTR					lpszMsgRange,		// NULL == "1:*"
											LPCTSTR					lpszModifiers[],	// last member must be NULL
											IMAP4_FETCHRSP_CFN	lpfnHcfn,
											LPVOID					pArg,
											const UINT32			ulRspTimeout)
{
	if ((NULL == lpszModifiers) || (NULL == lpfnHcfn))
		return EPARAM;

	if (NULL == lpszModifiers[0])
		return EEMPTYENTRY;

	TCHAR				szCmd[MAX_IMAP4_DATA_LEN+MAX_IMAP4_TAG_LEN+2]=_T("");
	CStrlBuilder	strb(szCmd, MAX_IMAP4_DATA_LEN+MAX_IMAP4_TAG_LEN);
	LPCTSTR			lpszTag=lpszITag;
	TCHAR				szTag[MAX_IMAP4_TAG_LEN+2]=_T("");
	EXC_TYPE			exc=InitIMAP4Cmd(lpszTag, szIMAP4FetchCmd, fIsUIDFetch, szTag, MAX_IMAP4_TAG_LEN, strb);
	if (exc != EOK)
		return exc;
	if ((exc=strb.AddChar(_T(' '))) != EOK)
		return exc;

	LPCTSTR	lpszMRange=(IsEmptyStr(lpszMsgRange) ? _T("1:*") : lpszMsgRange);
	if ((exc=strb.AddStr(lpszMRange)) != EOK)
		return exc;
	if ((exc=strb.AddChar(_T(' '))) != EOK)
		return exc;

	// add modifiers
	//
	// Note: for a single modifier no enclosing parantheses is generated
	if (lpszModifiers[1] != NULL)
	{
		if ((exc=strb.AddChar(IMAP4_PARLIST_SDELIM)) != EOK)
			return exc;
	}

	for (UINT32	mdx=0; ; mdx++)
	{
		LPCTSTR	lpszFM=lpszModifiers[mdx];
		if (IsEmptyStr(lpszFM)) break;

		if (mdx > 0)
		{
			if ((exc=strb.AddChar(_T(' '))) != EOK)
				return exc;
		}

		if ((exc=strb.AddStr(lpszFM)) != EOK)
			return exc;
	}

	// check if need to close parantheses
	if (lpszModifiers[1] != NULL)
	{
		if ((exc=strb.AddChar(IMAP4_PARLIST_EDELIM)) != EOK)
			return exc;
	}

	if ((exc=strb.AddStr(_T("\r\n"))) != EOK)
		return exc;

	int	wLen=SBSock.Write(szCmd);
	if (wLen <= 0)
		return ENOTCONNECTION;

	return imap4HandleFetchRspSync(SBSock, lpszTag, lpfnHcfn, pArg, ulRspTimeout);
}

/*---------------------------------------------------------------------------*/
