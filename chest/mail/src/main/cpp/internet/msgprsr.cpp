#include <stddef.h>
#include <stdio.h>

#include <_types.h>

#include <util/string.h>
#include <util/tables.h>
#include <util/errors.h>

#include <internet/rfc822msg.h>

/*---------------------------------------------------------------------------*/

CRFC822MsgExtractor::CRFC822MsgExtractor ()
	: CRFC822MsgParser(),
	  m_lpfnHcfn(NULL),
	  m_pBuf(NULL),
	  m_fAutoAlloc(FALSE),
	  m_b64Decoder(),
	  m_qpDecoder(),
	  m_ulBufLen(0),
	  m_ulMaxLen(0),
	  m_pArg(NULL),
	  m_ulAttachNameLen(0),
	  m_lpszAttachName(NULL),
	  m_hdrAcc()
{
	Reset();
}

/////////////////////////////////////////////////////////////////////////////

void CRFC822MsgExtractor::RestartAttach ()
{
	if (m_lpszAttachName != NULL)
		*m_lpszAttachName = '\0';

	m_szAttachType[0] = '\0';
	m_szAttachSubType[0] = '\0';
	m_szCharSet[0] = '\0';
	m_szAttachEncoding[0] = '\0';
	m_ulBufLen = 0;
	m_encAttach = RFC822_NONE_ENC;
	m_fDecoding = FALSE;
	m_b64Decoder.Restart((void *) this);
	m_qpDecoder.Restart((void *) this);
	m_hdrAcc.Reset();
}

/////////////////////////////////////////////////////////////////////////////

void CRFC822MsgExtractor::Reset ()
{
	m_ulAttachNum = 0;
	m_lastMsgState = RFC822_BEFORE_HDRS_MSGSTATE;
	CRFC822MsgParser::Reset();
	RestartAttach();
}

/////////////////////////////////////////////////////////////////////////////

void CRFC822MsgExtractor::Cleanup ()
{
	if (m_fAutoAlloc)
	{
		if (m_pBuf != NULL)
			delete [] m_pBuf;
	}

	if (m_lpszAttachName != NULL)
		delete [] m_lpszAttachName;

	m_fAutoAlloc = FALSE;
	m_pBuf = NULL;
	m_lpszAttachName = NULL;
	m_ulAttachNameLen = 0;
	m_ulBufLen = 0;
	m_ulMaxLen = 0;
}

/////////////////////////////////////////////////////////////////////////////

EXC_TYPE CRFC822MsgExtractor::InvokeCfn (const RFC822MSGEXCASE		meCase)
{
	if (fIsBadRFC822MsgExCase(meCase))
		return EPARAM;

	if (NULL == m_lpfnHcfn)
		return ENOPKG;

	return (*m_lpfnHcfn)(meCase, *this, m_pArg);
}

/////////////////////////////////////////////////////////////////////////////

EXC_TYPE CRFC822MsgExtractor::WriteData (const UINT8 pBuf[], const UINT32 ulBufLen)
{
	if ((NULL == pBuf) && (ulBufLen != 0))
		return EPARAM;

	if (0 == ulBufLen)
		return EOK;

	if (pBuf != m_pBuf)
		return EBUFFEREDCONN;

	m_ulBufLen = ulBufLen;

	const RFC822MSGEXCASE	meCase=((RFC822_BODY_MSGSTATE == m_lastMsgState) ?
		RFC822MSGEX_MSGBODY_CASE : RFC822MSGEX_ATTDATA_CASE);

	return InvokeCfn(meCase);
}

/////////////////////////////////////////////////////////////////////////////

UINT32 CRFC822MsgExtractor::wcfn (void *pFout, const char pBuf[], const UINT32 ulBufLen)
{
	LPRFC822MSGEXTRACTOR pMsgEx=(LPRFC822MSGEXTRACTOR) pFout;

	if ((NULL == pMsgEx) || ((NULL == pBuf) && (ulBufLen != 0)))
		return ((UINT32) (-1));

	if (ulBufLen != 0)
	{
		EXC_TYPE	exc=pMsgEx->WriteData((const UINT8 *) pBuf, ulBufLen);
		if (exc != EOK)
			return ((UINT32) (-1));
	}

	return ulBufLen;
}

/////////////////////////////////////////////////////////////////////////////

EXC_TYPE CRFC822MsgExtractor::SetDecodeParams (RFC822MSGEX_CFN_TYPE	lpfnHcfn,
															  void						*pArg,
															  const UINT32				iMaxLen,
															  UINT8						*pBuf)	// NULL == auto allocate
{
	EXC_TYPE	exc=EOK;

	Cleanup();

	if ((NULL == lpfnHcfn) || (0 == iMaxLen))
		return EPARAM;

	if (NULL == (m_pBuf=pBuf))
	{
		if (NULL == (m_pBuf=new UINT8[iMaxLen+4]))
			return EMEM;
		m_fAutoAlloc = TRUE;
	}
	else
		m_fAutoAlloc = FALSE;

	exc = m_b64Decoder.Start(m_pBuf, iMaxLen, wcfn, (void *) this);
	if (exc != EOK)
	{
		Cleanup();
		return exc;
	}

	exc = m_qpDecoder.Start((char *) m_pBuf, iMaxLen, wcfn, (void *) this);
	if (exc != EOK)
	{
		Cleanup();
		return exc;
	}

	m_ulMaxLen = iMaxLen;
	m_lpfnHcfn = lpfnHcfn;
	m_pArg = pArg;

	return EOK;
}

/////////////////////////////////////////////////////////////////////////////

static EXC_TYPE UpdateAttachName (LPCTSTR	lpszHdrValue,
											 LPTSTR&	lpszAttName,	// in/out
											 UINT32&	ulAttachNameLen)
{
	LPCTSTR		lpszAN=NULL;
	UINT32		ulANLen=0UL;
	EXC_TYPE		exc=RFC822FindAttachName(lpszHdrValue, &lpszAN, &ulANLen);
	if (EFNEXIST == exc)
		return EOK;	// if not found, do nothing
	if (exc != EOK)
		return exc;

	// skip preceding white space
	for ( ; isspace(*lpszAN) && (*lpszAN != _T('\0')); lpszAN++, ulANLen--);
	if ((0UL == ulANLen) || (_T('\0') == *lpszAN))
		return EOK;

	// skip trailing whitespace
	for (LPCTSTR	lpszAE=(lpszAN + (ulANLen-1)); lpszAE > lpszAN; lpszAE--, ulANLen--)
		if (!isspace(*lpszAE))
			break;

	// check if need to re-allocate
	if (ulANLen > ulAttachNameLen)
	{
		if (lpszAttName != NULL)
		{
			delete [] lpszAttName;
			lpszAttName = NULL;
		}

		ulAttachNameLen = 0UL;

		if (NULL == (lpszAttName=new char[ulANLen+2]))
			return EMEM;

		ulAttachNameLen = ulANLen;
	}

	strncpy(lpszAttName, lpszAN, ulANLen);
	lpszAttName[ulANLen] = '\0';

	return EOK;
}

/////////////////////////////////////////////////////////////////////////////

EXC_TYPE CRFC822MsgExtractor::UpdateContentTypeInfo (const char lpszAccValue[])
{
	const char	*lpszType=NULL, *lpszSubType=NULL;
	UINT32		ulTypeLen=0, ulSubTypeLen=0;
	EXC_TYPE		exc=rfc822ExtractContentTypePtrs(lpszAccValue,
																&lpszType, &ulTypeLen,
																&lpszSubType, &ulSubTypeLen);
	if (exc != EOK)
		return exc;

	if ((ulTypeLen > MAX_RFC822_CONTENT_TYPE_LEN) ||
		 (ulSubTypeLen > MAX_RFC822_CONTENT_SUBTYPE_LEN))
		return EMEM;

	if ((0 == ulTypeLen) || (0 == ulSubTypeLen))
		return EIOUNCLASS;

	strncpy(m_szAttachType, lpszType, ulTypeLen);
	m_szAttachType[ulTypeLen] = '\0';

	strncpy(m_szAttachSubType, lpszSubType, ulSubTypeLen);
	m_szAttachSubType[ulSubTypeLen] = '\0';

	const char	*lpszCharSet=NULL;
	UINT32		ulCSLen=0;

	if ((exc=RFC822FindCharset(lpszAccValue, &lpszCharSet, &ulCSLen)) != EOK)
	{
		// OK if not found charset specification
		if (exc != EFNEXIST)
			return exc;
		return EOK;
	}

	if (ulCSLen != 0)
	{
		if (ulCSLen > MAX_RFC822_CHARSET_LEN)
			return EOVERFLOW;

		strncpy(m_szCharSet, lpszCharSet, ulCSLen);
		m_szCharSet[ulCSLen] = '\0';
	}

	return EOK;
}

/////////////////////////////////////////////////////////////////////////////

EXC_TYPE CRFC822MsgExtractor::UpdateContentXferInfo (const char lpszAccValue[])
{
	if (IsEmptyStr(lpszAccValue))
		return EBADBUFF;

	// skip preceding white space
	const char	*lpszXS=lpszAccValue;
	for ( ; isspace(*lpszXS) && (*lpszXS != '\0'); lpszXS++);

	// skip trailing white space
	const char *lpszXE=strlast(lpszXS);
	if (lpszXE <= lpszXS)
		return EEMPTYENTRY;
	for (lpszXE-- ; isspace(*lpszXE) && (lpszXE > lpszXS); lpszXE--);
	if (lpszXE <= lpszXS)
		return EEMPTYENTRY;

	lpszXE++;
	UINT32	ulXLen=(lpszXE - lpszXS);
	if (0 == ulXLen)
		return EEMPTYENTRY;

	if (ulXLen > MAX_RFC822_CONTENT_ENCODE_LEN)
		return EOVERFLOW;

	strncpy(m_szAttachEncoding, lpszXS, ulXLen);
	m_szAttachEncoding[ulXLen] = '\0';
	m_encAttach = RFC822EncodingStr2Case(m_szAttachEncoding);

	return EOK;
}

/////////////////////////////////////////////////////////////////////////////

EXC_TYPE CRFC822MsgExtractor::HandleAccumulatedHdr ()
{
	const char	*lpszAccName=m_hdrAcc.GetHdrName();
	if (IsEmptyStr(lpszAccName))
		return EOK;

	const char	*lpszAccValue=m_hdrAcc.GetHdrValue();
	EXC_TYPE		exc=EOK;

	if (stricmp(pszStdContentXferEncoding, lpszAccName) == 0)
	{
		if ((exc=UpdateContentXferInfo(lpszAccValue)) != EOK)
			return exc;
	}
	else if (stricmp(pszStdContentDisposition, lpszAccName) == 0)
	{
		if ((exc=UpdateAttachName(lpszAccValue, m_lpszAttachName, m_ulAttachNameLen)) != EOK)
			return exc;
	}
	else if (stricmp(pszStdContentTypeHdr, lpszAccName) == 0)
	{
		if ((exc=UpdateContentTypeInfo(lpszAccValue)) != EOK)
			return exc;

		if ((exc=UpdateAttachName(lpszAccValue, m_lpszAttachName, m_ulAttachNameLen)) != EOK)
			return exc;
	}

	m_hdrAcc.Reset();
	return EOK;
}

/////////////////////////////////////////////////////////////////////////////

static const STR2PTRASSOC attHdrs[]={
	{	pszStdContentTypeHdr,		(LPVOID) pszStdContentTypeHdr			},
	{	pszStdContentXferEncoding,	(LPVOID) pszStdContentXferEncoding	},
	{	pszStdContentLocationHdr,	(LPVOID)	pszStdContentLocationHdr	},
	{	pszStdContentLengthHdr,		(LPVOID) pszStdContentLengthHdr		},
	{	pszStdContentDisposition,	(LPVOID)	pszStdContentDisposition	},
	{	pszStdContentDescription,	(LPVOID)	pszStdContentDescription	},
	{	NULL,								NULL	}	// mark end of list
};

static CStr2PtrMapper contentHdrsMap(attHdrs, 7UL, FALSE);

static BOOLEAN IsRFC822ContentHdr (const char lpszHdrName[])
{
	if (IsEmptyStr(lpszHdrName))
		return FALSE;

	LPVOID	pVal=NULL;
	EXC_TYPE	exc=contentHdrsMap.FindKey(lpszHdrName, pVal);
	if (EOK == exc)
		return TRUE;

	return (strstr(lpszHdrName, "Content-") == lpszHdrName);
}

/////////////////////////////////////////////////////////////////////////////

EXC_TYPE CRFC822MsgExtractor::AccumulateHdrData ()
{
	const char	*lpszHdrName=GetHdrName();
	const char	*lpszHdrValue=GetHdrValue();
	BOOLEAN		fIsContentHdr=IsRFC822ContentHdr(lpszHdrName);

	if (IsEmptyStr(lpszHdrName) || (NULL == lpszHdrValue))
		return EINVALIDFNODE;

	const char	*lpszAccName=m_hdrAcc.GetHdrName();
	EXC_TYPE		exc=EOK;
	if (IsEmptyStr(lpszAccName))
	{
		if (fIsContentHdr)
		{
			if ((exc=m_hdrAcc.SetData(lpszHdrName, lpszHdrValue)) != EOK)
				return exc;
		}
	}
	else	// have an accumulated header name
	{
		// detect when header is changed so we can handle accumulated data
		if (stricmp(lpszHdrName, lpszAccName) != 0)
		{
			if ((exc=HandleAccumulatedHdr()) != EOK)
				return exc;

			if (fIsContentHdr)
			{
				if ((exc=m_hdrAcc.SetData(lpszHdrName, lpszHdrValue)) != EOK)
					return exc;
			}
		}
		else if (fIsContentHdr)	// same header as before
		{
			if ((exc=m_hdrAcc.AddData(lpszHdrValue)) != EOK)
				return exc;
		}
	}

	return EOK;
}

/////////////////////////////////////////////////////////////////////////////

EXC_TYPE CRFC822MsgExtractor::HandleAttachHdr ()
{
	EXC_TYPE	exc=AccumulateHdrData();
	if (exc != EOK)
		return exc;

	//		This method is called for direct attachment(s) as well. In that case,
	// some "non-attachment" related headers may be encountered after the
	// related ones. Hence, the callback reason has to be adjusted according to
	// the actual header "relevance"
	LPCTSTR				lpszHdrName=GetHdrName();
	BOOLEAN				fIsAttachHdr=IsRFC822ContentHdr(lpszHdrName);
	RFC822MSGEXCASE	mCase=((fIsAttachHdr) ?  RFC822MSGEX_ATTHDR_CASE :
									 RFC822MSGEX_MSGHDR_CASE);

	return InvokeCfn(mCase);
}

/////////////////////////////////////////////////////////////////////////////

EXC_TYPE CRFC822MsgExtractor::HandleEnvelopeHdr ()
{
	EXC_TYPE	exc=AccumulateHdrData();
	if (exc != EOK)
		return exc;

	return InvokeCfn(RFC822MSGEX_MSGHDR_CASE);
}

/////////////////////////////////////////////////////////////////////////////

EXC_TYPE CRFC822MsgExtractor::HandlePlainData (const char				*lpszLine,
															  const UINT32				ulLen,
															  const RFC822MSGEXCASE	mCase)
{
	if (0 == ulLen)
	{
		m_ulBufLen = 0;
		return InvokeCfn(mCase);
	}

	if ((0 == m_ulMaxLen) || (NULL == m_pBuf))
		return EBADBUFF;

	UINT32		ulRemLen=ulLen;
	const char	*lp=lpszLine;

	while (ulRemLen > 0)
	{
		UINT32	ulBufLen=min(ulRemLen, (m_ulMaxLen-4));

		memcpy(m_pBuf, lp, ulBufLen);
		m_ulBufLen = ulBufLen;

		// add stripped CRLF
		char	*lsp=(char *) (m_pBuf + m_ulBufLen);
		*lsp = '\0';

		lsp = strladdch(lsp, '\r');
		lsp = strladdch(lsp, '\n');
		m_ulBufLen += 2UL;

		EXC_TYPE	exc=InvokeCfn(mCase);
		if (exc != EOK)
			return exc;

		ulRemLen -= ulBufLen;
	}

	return EOK;
}

/////////////////////////////////////////////////////////////////////////////

EXC_TYPE CRFC822MsgExtractor::DecodeMsgPart (const char					lpszLine[],
															const UINT32				ulLen,
															const RFC822MSGEXCASE	mCase)
{
	if ((NULL == lpszLine) && (ulLen != 0))
		return EPARAM;

	EXC_TYPE exc=EOK;
	switch(m_encAttach)
	{
		case RFC822_B64_ENC	:
			if (0 == ulLen)
				return EOK;

			m_fDecoding = TRUE;

			exc = m_b64Decoder.Process(lpszLine, ulLen);
			if (exc != EEOF)
				return exc;

			if ((exc=StopDecoding()) != EOK)
				return exc;
			break;

		case RFC822_QP_ENC	:	/*	quoted-printable */
			m_fDecoding = TRUE;

			exc = m_qpDecoder.Process(lpszLine, ulLen);
			if (exc != EOK)
				return exc;

			// need to add the stripped CRLF
			exc = m_qpDecoder.Process("\r\n", 2);
			if (exc != EEOF)
				return exc;

			if ((exc=StopDecoding()) != EOK)
				return exc;
			break;

		case RFC822_NONE_ENC	:	/* no specific encoding */
		case RFC822_7BIT_ENC	:
		case RFC822_8BIT_ENC	:
			if ((exc=HandlePlainData(lpszLine, ulLen, mCase)) != EOK)
				return exc;
			break;

		default			:
			return ETYPE;
	}

	return EOK;
}

/////////////////////////////////////////////////////////////////////////////

EXC_TYPE CRFC822MsgExtractor::StopDecoding ()
{
	if (!m_fDecoding)
		return EOK;

	EXC_TYPE	exc=EOK;
	switch(m_encAttach)
	{
		case RFC822_B64_ENC	:
			exc = m_b64Decoder.End();
			break;

		case RFC822_QP_ENC	:
			exc = m_qpDecoder.End();
			break;

		case RFC822_NONE_ENC	:
		case RFC822_7BIT_ENC	:
		case RFC822_8BIT_ENC	:
			break;

		default					:
			exc = EFTYPE;
	}
	m_fDecoding = FALSE;

	if (exc != EOK)
	{
		if (exc != EEOF)
			return exc;
	}

	return EOK;
}

/////////////////////////////////////////////////////////////////////////////

EXC_TYPE CRFC822MsgExtractor::SignalAttachStart ()
{
	EXC_TYPE	exc=HandleAccumulatedHdr();
	if (exc != EOK)
		return exc;

	m_ulAttachNum++;
	return InvokeCfn(RFC822MSGEX_ATTSTART_CASE);
}

/////////////////////////////////////////////////////////////////////////////
// processes input line and changes state/data
//
// Note: some internal pointers may point to the original supplied
//			buffer. Therefore, it MUST NOT change while this object is
//			"in use" (i.e. until all calls to any of its methods have
//			been completed.
EXC_TYPE CRFC822MsgExtractor::ProcessLine (const char		lpszLine[],
														 const UINT32	ulLen)
{
	EXC_TYPE	exc=CRFC822MsgParser::ProcessLine(lpszLine, ulLen);
	if (exc != EOK)
		return exc;
	RFC822MSGSTATE	curState=GetMsgState();

	// detect state change(s)
	if (curState != m_lastMsgState)
	{
		switch(m_lastMsgState)
		{
			case RFC822_BEFORE_HDRS_MSGSTATE	:	/* initial state */
				break;

			case RFC822_ENVELOPE_MSGSTATE		:	/* parsing envelope */
				// for direct attachments the transition is this one
				if (RFC822_BEFORE_ATTDATA_MSGSTATE == curState)
				{
					if ((exc=SignalAttachStart()) != EOK)
						return exc;
				}
				else if (RFC822_BEFORE_BODY_MSGSTATE == curState)
				{
					// flush any accumulated header
					if ((exc=HandleAccumulatedHdr()) != EOK)
						return exc;
				}
				else if (RFC822_BODY_MSGSTATE == curState)
				{
					// if body started, check if we have some content type & charset override
					if (('\0' == m_szAttachType[0]) && ('\0' == m_szAttachSubType[0]))
					{
						strcpy(m_szAttachType, pszMIMETextType);
						strcpy(m_szAttachSubType, pszMIMEPlainSubType);
					}

#if FALSE
					if ('\0' == m_szCharSet[0])
						strcpy(m_szCharSet, pszUSASCIICharSet);
#endif
				}
				break;

			case RFC822_BEFORE_BODY_MSGSTATE	:	/* after parsing 1st blank line */
				break;

			case RFC822_BODY_MSGSTATE			:		/* within msg body */
				// exiting from msg body
				if ((exc=StopDecoding()) != EOK)
					return exc;
				break;

			case RFC822_BEFORE_ATTHDRS_MSGSTATE:/* after MIME boundary */
				// fall through (can happen if there are no headers after MIME boundary)	

			case RFC822_ATTHDRS_MSGSTATE		:	/* parsing attachment headers */
				if (RFC822_BEFORE_ATTDATA_MSGSTATE == curState)
				{
					if ((exc=SignalAttachStart()) != EOK)
						return exc;
				}
				break;
	
			case RFC822_BEFORE_ATTDATA_MSGSTATE:/* blank line after attach hdrs */
				break;

			case RFC822_ATTDATA_MSGSTATE		:	/* attachment data */
				// any change from this state to another means end of attachment
				if ((exc=StopDecoding()) != EOK)
					return exc;

				if ((exc=InvokeCfn(RFC822MSGEX_ATTEND_CASE)) != EOK)
					return exc;

				RestartAttach();
				break;

			case RFC822_BEFORE_MSG_END			:	/* after last MIME boundary */
			case RFC822_MSGEND_MSGSTATE		:/* after end of msg ('.' + CRLF) */
				break;

			default									:
				return ESTATE;
		}

		m_lastMsgState = curState;
	}

	switch(curState)
	{
		case RFC822_BEFORE_HDRS_MSGSTATE	:		/* initial state */
			break;
	
		case RFC822_ENVELOPE_MSGSTATE		:		/* parsing envelope */
			if (IsDirectAttach())
				exc = HandleAttachHdr();
			else
				exc = HandleEnvelopeHdr();
			break;

		case RFC822_BEFORE_BODY_MSGSTATE	:		/* after parsing 1st blank line */
			break;

		case RFC822_BODY_MSGSTATE			:		/* within msg body */
			exc = DecodeMsgPart(lpszLine, ulLen, RFC822MSGEX_MSGBODY_CASE);
			break;

		case RFC822_BEFORE_ATTHDRS_MSGSTATE:	/* after MIME separator */
			RestartAttach();
			break;

		case RFC822_ATTHDRS_MSGSTATE		:		/* parsing attachment headers */
			exc = HandleAttachHdr();
			break;
			
		case RFC822_BEFORE_ATTDATA_MSGSTATE:	/* blank line after attach hdrs */
			if (RFC822_B64_ENC == m_encAttach)
				exc = m_b64Decoder.Restart((void *) this);
			else if (RFC822_QP_ENC == m_encAttach)
				exc = m_qpDecoder.Restart((void *) this);
			break;
			
		case RFC822_ATTDATA_MSGSTATE		:	/* attachment data */
			exc = DecodeMsgPart(lpszLine, ulLen, RFC822MSGEX_ATTDATA_CASE);
			break;

		case RFC822_BEFORE_MSG_END			:	/* after last MIME boundary */
		case RFC822_MSGEND_MSGSTATE		:	/* after end of msg ('.' + CRLF) */
			break;

		default									:
			exc = ESTATE;
	}
	if (exc != EOK)
		return exc;

	return EOK;
}

/////////////////////////////////////////////////////////////////////////////

EXC_TYPE ExtractRFC822MsgFromFile (const char				*lpszMsgFile,
											  RFC822MSGEX_CFN_TYPE	lpfnXcfn,
											  LPVOID						pArg)
{
	if (IsEmptyStr(lpszMsgFile) || (NULL == lpfnXcfn))
		return EPARAM;

	FILE	*fin=fopen(lpszMsgFile, _T("r"));
	if (NULL == fin)
		return EFNEXIST;

	CRFC822MsgExtractor	msgEx;
	EXC_TYPE					exc=msgEx.SetDecodeParams(lpfnXcfn, pArg);
	if (exc != EOK)
		return exc;

	for (UINT32 ulLdx=1; ; ulLdx++)
	{
#define MAX_LINE_LENGTH	1022UL
		char	szLine[MAX_LINE_LENGTH+2]="", *lp=fgets(szLine, (MAX_LINE_LENGTH+1), fin);
#undef MAX_LINE_LENGTH

		if (NULL == lp)
		{
			if (feof(fin))
			{
				// check if '.' already parsed - if not generate a dummy line
				RFC822MSGSTATE	eState=msgEx.GetMsgState();
				if (RFC822_MSGEND_MSGSTATE != eState)
					exc = msgEx.ProcessLine(".", 1);
			}
			else	// not EOF
			{
				if (ferror(fin))
					exc = EIOSOFT;
			}

			break;
		}

		UINT32	ulLL=strlen(szLine);

		// remove CRLF at end of line (if any)
		if ('\n' == szLine[ulLL-1])
		{
			szLine[ulLL-1] = '\0';
			ulLL--;
		}

		if ('\r' == szLine[ulLL-1])
		{
			szLine[ulLL-1] = '\0';
			ulLL--;
		}

		if ((exc=msgEx.ProcessLine(szLine, ulLL)) != EOK)
			break;
	}

	fclose(fin);
	fin = NULL;

	return exc;
}

///////////////////////////////////////////////////////////////////////////////

void CRFC822MsgBufferProcessor::Reset ()
{
	m_ulCurAccLen = 0;
	if (m_lpszAccLine != NULL)
		*m_lpszAccLine = _T('\0');

	CRFC822MsgExtractor::Reset();
}

/*----------------------------------------------------------------------------*/

EXC_TYPE CRFC822MsgBufferProcessor::SetProcessingParams (RFC822MSGEX_CFN_TYPE	lpfnHcfn,
																			void						*pArg,
																			const UINT32			iMaxAcc,
																			LPTSTR					pAcc,		// NULL == auto allocate
																			const UINT32			iMaxLen,
																			UINT8						*pBuf)	// NULL == auto allocate
{
	if (0 == iMaxAcc)
		return EPARAM;

	EXC_TYPE	exc=SetDecodeParams(lpfnHcfn, pArg, iMaxLen, pBuf);
	if (exc != EOK)
		return exc;

	if (NULL == (m_lpszAccLine=pAcc))
	{
		if (NULL == (m_lpszAccLine=new TCHAR[iMaxAcc+sizeof(int)]))
			return EMEM;
		memset(m_lpszAccLine, 0, iMaxAcc);
		m_fAutoAlloc = TRUE;
	}
	m_ulMaxAccLen = iMaxAcc;

	return EOK;
}

/*----------------------------------------------------------------------------*/

EXC_TYPE CRFC822MsgBufferProcessor::ProcessLine (const char lpszLine[], const UINT32 ulLen)
{
	if (0 == ulLen)
		return EOK;

	EXC_TYPE	exc=EOK;
	UINT32	ulRemLen=ulLen;
	LPCTSTR	lpszCurPos=lpszLine;

	// check if any leftovers from previous time
	if (m_ulCurAccLen != 0)
	{
		if (NULL == m_lpszAccLine)
			return ECONTEXT;

		LPTSTR	lpszAccPos=(m_lpszAccLine + m_ulCurAccLen);
		for ( ;(ulRemLen > 0) && (*lpszCurPos != _T('\n')) && (m_ulCurAccLen < m_ulMaxAccLen);
			   lpszCurPos++, ulRemLen--, lpszAccPos++, m_ulCurAccLen++)
			*lpszAccPos = *lpszCurPos;

		// check why stopped
		if (*lpszCurPos != _T('\n'))
		{
			// check if exceeded available accumulation buffer
			if (ulRemLen != 0)
				return EOVERFLOW;

			return EOK;	// could not complete to full line
		}

		lpszCurPos++;	// skip LF;
		ulRemLen--;

		// at this point we have a complete line which might have a '\r' at its end
		*lpszAccPos = _T('\0');
		if ((m_ulCurAccLen > 0) && (_T('\r') == m_lpszAccLine[m_ulCurAccLen-1]))
		{
			m_ulCurAccLen--;
			m_lpszAccLine[m_ulCurAccLen] = _T('\0');
		}

		if ((exc=CRFC822MsgExtractor::ProcessLine(m_lpszAccLine, m_ulCurAccLen)) != EOK)
			return exc;

		m_ulCurAccLen = 0;
		*m_lpszAccLine = _T('\0');
	}

	// at this point all leftovers have been processed
	for ( ; ; )
	{
		LPCTSTR	lpszLE=lpszCurPos;
		UINT32	ulALen=ulRemLen;

		for ( ; (ulALen > 0) && (*lpszLE != _T('\n')); lpszLE++, ulALen--);
		if (*lpszLE != _T('\n'))
			break;

		UINT32	ulLLen=(lpszLE - lpszCurPos), ulRLen=ulLLen;
		if ((ulLLen > 0) && (_T('\r') == lpszCurPos[ulLLen-1]))
		{
			ulLLen--;
			*((LPTSTR) (lpszCurPos + ulLLen)) = _T('\0');
		}

		lpszLE++;	// skip LF
		ulRemLen--;

		if ((exc=CRFC822MsgExtractor::ProcessLine(lpszCurPos, ulLLen)) != EOK)
			return exc;

		// restore CR if deleted
		if (ulRLen != ulLLen)
			*((LPTSTR) (lpszCurPos + ulLLen)) = _T('\r');

		lpszCurPos = lpszLE;
	}

	// save unprocessed data for later
	if (ulRemLen > 0)
	{
		if (ulRemLen > m_ulMaxAccLen)
			return EOVERFLOW;

		_tcsncpy(m_lpszAccLine, lpszCurPos, ulRemLen);
		m_lpszAccLine[ulRemLen] = _T('\0');
		m_ulCurAccLen = ulRemLen;
	}

	return EOK;
}

///////////////////////////////////////////////////////////////////////////////
