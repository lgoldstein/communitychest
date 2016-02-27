#include <internet/imap4Lib.h>

/*---------------------------------------------------------------------------*/

// build tag and add it to the command line (Note: updates tag pointer if auto-generated)
EXC_TYPE InitIMAP4Tag (LPCTSTR&			lpszRTag,	// in/out
							  LPTSTR				lpszAutoTag,
							  const UINT32		ulMaxTagLen,
							  IStrlBuilder&	strb)
{
	EXC_TYPE	exc=EOK;

	if (IsEmptyStr(lpszRTag))
	{
		if ((exc=GetIMAP4AutoTag(lpszAutoTag, ulMaxTagLen)) != EOK)
			return exc;
		lpszRTag = lpszAutoTag;
	}

	if ((exc=strb.AddStr(lpszRTag)) != EOK)
		return exc;
	if ((exc=strb.AddChar(_T(' '))) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

// build tag+command and add it to the command line (Note: updates tag pointer if auto-generated)
EXC_TYPE InitIMAP4Cmd (LPCTSTR&			lpszRTag,	// in/out
							  LPCTSTR			lpszCmd,
							  const BOOLEAN	fIsUID,
							  LPTSTR				lpszAutoTag,
							  const UINT32		ulMaxTagLen,
							  IStrlBuilder&	strb)
{
	EXC_TYPE	exc=InitIMAP4Tag(lpszRTag, lpszAutoTag, ulMaxTagLen, strb);
	if (exc != EOK)
		return exc;

	if (IsEmptyStr(lpszCmd))
		return EEMPTYENTRY;

	if (fIsUID)
	{
		if ((exc=strb.AddStr(IMAP4_UID)) != EOK)
			return exc;
		if ((exc=strb.AddChar(_T(' '))) != EOK)
			return exc;
	}

	if ((exc=strb.AddStr(lpszCmd)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE SendIMAP4Reference (ISockioInterface&	SBSock,
									  LPCTSTR				lpszCmdPrefix,
									  LPCTSTR				lpszRef,
									  const BOOLEAN		fAllowRefChars,
									  const UINT32			ulRspTimeout)
{
	// an empty reference is OK
	if (IsEmptyStr(lpszRef))
		return EOK;

	LPCTSTR	lpszFS=lpszRef, lpszFE=strlast(lpszFS);

	// if quote start, check if also quote end
	if (IMAP4_QUOTE_DELIM == *lpszFS)
	{
		if (lpszFS < (lpszFE-1))
		{
			//		assume quoted folder only if also ending in quote (otherwise some
			// "special" folder name that only starts with quote)
			if (IMAP4_QUOTE_DELIM == *(lpszFE-1))
			{
				lpszFS++;
				lpszFE--;
			}
		}
	}

	// check if folder contains special characters
	LPCTSTR	lpszSChar=NULL;
	for (LPCTSTR lpszFC=lpszFS; lpszFC < lpszFE; lpszFC++)
	{
		// characters which require sending the folder as literal
		static const TCHAR szIMAP4SpecialFolderChars[]={
			IMAP4_PARLIST_SDELIM,		/* ( */
			IMAP4_PARLIST_EDELIM,		/* ) */
			IMAP4_OCTCNT_SDELIM,			/* { */
			IMAP4_QUOTE_DELIM,
			IMAP4_MSGRANGE_WILDCARD,	/* * */
			IMAP4_LISTWILDCARD,			/* % */
			_T('\\'),
			_T('\0')	// mark end
		};

		if ((lpszSChar=_tcschr(szIMAP4SpecialFolderChars, *lpszFC)) != NULL)
		{
			if (!fAllowRefChars)
				break;

			// if reference chars allowed, then ignore them
			if ((*lpszSChar != IMAP4_LISTWILDCARD) && (*lpszSChar != IMAP4_MSGRANGE_WILDCARD))
				break;

			lpszSChar = NULL;
		}
	}

	// if no special character found then do nothing
	if (NULL == lpszSChar)
		return EOK;

	// write the command + literal count
	UINT32	ulOCount=(lpszFE - lpszFS);
	int		wLen=(-1);
	if (IsEmptyStr(lpszCmdPrefix))
		wLen = SBSock.WriteCmdf(_T(" %c%lu%c\r\n"), IMAP4_OCTCNT_SDELIM, ulOCount, IMAP4_OCTCNT_EDELIM);
	else
		wLen = SBSock.WriteCmdf(_T("%s %c%lu%c\r\n"), lpszCmdPrefix, IMAP4_OCTCNT_SDELIM, ulOCount, IMAP4_OCTCNT_EDELIM);
	if (wLen <= 0)
		return ENOTCONNECTION;

	EXC_TYPE	exc=WaitForIMAP4Continuation(SBSock, NULL, NULL, ulRspTimeout);
	if (exc != EOK)
		return exc;

	// create a EOS terminated folder name (if quoted)
	if ((UINT32) (wLen=SBSock.Write(lpszFS, ulOCount)) != ulOCount)
		return ENOTCONNECTION;

	return ELITERAL;
}

/*---------------------------------------------------------------------------*/

// add folder (quote it if necessary)
EXC_TYPE AddIMAP4Reference (IStrlBuilder& strb, LPCTSTR lpszRef)
{
	EXC_TYPE	exc=EOK;

	if (IMAP4_QUOTE_DELIM != *lpszRef)
	{
		if ((exc=strb.AddChar(IMAP4_QUOTE_DELIM)) != EOK)
			return exc;
		if ((exc=strb.AddStr(lpszRef)) != EOK)
			return exc;
		if ((exc=strb.AddChar(IMAP4_QUOTE_DELIM)) != EOK)
			return exc;
	}
	else	// assume quoted
	{
		if ((exc=strb.AddStr(lpszRef)) != EOK)
			return exc;
	}

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

CIMAP4RspParser::CIMAP4RspParser (ISockioInterface&	SBSock,
											 LPCTSTR					lpszTag,
											 const UINT32			ulRspTimeout,
											 const UINT32			ulRspBufLen)
	: m_SBSock(SBSock), m_lpszTag(lpszTag), m_fStripCRLF(FALSE), m_ulRspTimeout(ulRspTimeout),
	  m_lpszCurPos(NULL), m_lpszRspBuf(NULL), m_ulRspBufLen(0), m_fAutoAlloc(FALSE)
{
	if (ulRspBufLen != 0)
	{
		if ((m_lpszRspBuf=new TCHAR[ulRspBufLen+2]) != NULL)
		{
			m_lpszCurPos = m_lpszRspBuf;
			m_ulRspBufLen = ulRspBufLen;
			m_fAutoAlloc = TRUE;
		}
	}
}

CIMAP4RspParser::CIMAP4RspParser (ISockioInterface&	SBSock,
											 LPCTSTR					lpszTag,
											 LPTSTR					lpszRspBuf,
											 const UINT32			ulMaxRspLen,
											 const UINT32			ulRspTimeout)
	: m_SBSock(SBSock), m_lpszTag(lpszTag), m_fStripCRLF(FALSE), m_ulRspTimeout(ulRspTimeout),
	  m_lpszCurPos(NULL), m_lpszRspBuf(lpszRspBuf), m_ulRspBufLen(ulMaxRspLen), m_fAutoAlloc(FALSE)
{
}

//////////////////////////////////////////////////////////////////////////////

BOOLEAN CIMAP4RspParser::IsIMAP4QuoteDelimCurPos () const
{
	if (IsEmptyStr(m_lpszCurPos))
		return FALSE;

	if (IMAP4_QUOTE_DELIM != *m_lpszCurPos)
		return FALSE;

	if (m_lpszCurPos > m_lpszRspBuf)
	{
		// skip escaped quote
		if (_T('\\') == *(m_lpszCurPos-1))
			return FALSE;
	}

	return TRUE;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4RspParser::SkipLiteralData (const UINT32 ulDataLen)
{
	for (UINT32	ulRemLen=ulDataLen; ulRemLen > 0; )
	{
		UINT32	ulALen=min(ulRemLen, m_ulRspBufLen);
		int		rLen=m_SBSock.Fill(m_lpszRspBuf, (size_t) ulALen, (SINT32) m_ulRspTimeout);
		if ((UINT32) rLen != ulALen)
			return ((rLen <= 0) ? ENOTCONNECTION : EIOWRPROT);
		ulRemLen -= ulALen;
			
		m_lpszCurPos = &m_lpszRspBuf[rLen];
		*((LPTSTR) m_lpszCurPos) = _T('\0');
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

// assumes literal count is at current position
EXC_TYPE CIMAP4RspParser::SkipLiteralData ()
{
	UINT32	ulDataLen=0;
	EXC_TYPE	exc=ExtractLiteralCount(ulDataLen);
	if (EOK == exc)
		exc = SkipLiteralData(ulDataLen);
	return exc;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4RspParser::SkipToEndOfLine ()
{
	while (!m_fStripCRLF)
	{
		const int	rLen=m_SBSock.ReadCmd(m_lpszRspBuf, m_ulRspBufLen, (SINT32) m_ulRspTimeout, &m_fStripCRLF);
		if (rLen < 0)
			return ENOTCONNECTION;

		m_lpszRspBuf[rLen] = _T('\0');
	}

	// point beyond end of line
	m_lpszCurPos = strlast(m_lpszRspBuf);
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4RspParser::RefillFetchParseBuffer (LPCTSTR lpszBufStart)
{
	UINT32	ulLOLen=0;

	if (lpszBufStart != NULL)
	{
		// keep the partial response
		_tcscpy(m_lpszRspBuf, lpszBufStart);
		ulLOLen = (m_lpszCurPos - lpszBufStart);
	}

	UINT32 ulALen=(m_ulRspBufLen - ulLOLen);
	if (0 == ulALen)
		return EIONODATA;

	// read continuation
	int	rLen=m_SBSock.ReadCmd(&m_lpszRspBuf[ulLOLen], ulALen, (SINT32) m_ulRspTimeout, &m_fStripCRLF);
	if (rLen <= 0)
		return ENOTCONNECTION;

	// reset and retry
	m_lpszCurPos = m_lpszRspBuf;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4RspParser::FillNonEmptyParseBuffer ()
{
	// find start of flags value(s)
	for (UINT32 ulRdx=0; ; ulRdx++)
	{
		for ( ; _istspace(*m_lpszCurPos) && (*m_lpszCurPos != _T('\0')); m_lpszCurPos++);

		// if reached end of buffer while skipping whitespace, then refill and retry
		if (_T('\0') != *m_lpszCurPos)
			break;

		EXC_TYPE	exc=RefillFetchParseBuffer();
		if (exc != EOK)
			return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4RspParser::CheckNILParseBuffer (const BOOLEAN fAllowEOS)
{
	LPCTSTR	lpszNIL=NULL;

	for (UINT32	ulNdx=0; ; ulNdx++)
	{
		EXC_TYPE	exc=EOK;

		if ((exc=FillNonEmptyParseBuffer()) != EOK)
			return exc;
		
		// find end of atom
		for (lpszNIL=m_lpszCurPos, m_lpszCurPos++; (!_istspace(*m_lpszCurPos)) && (*m_lpszCurPos != IMAP4_PARLIST_EDELIM) && (*m_lpszCurPos != _T('\0')); m_lpszCurPos++);
		
		// if reached end of string then refill and retry - atom is terminated by either a space or a list end
		if ((_T('\0') == *m_lpszCurPos) && (!fAllowEOS))
		{
			if ((exc=RefillFetchParseBuffer(lpszNIL)) != EOK)
				return exc;
			
			continue;
		}

		break;
	}

	UINT32	ulNLen=(m_lpszCurPos - lpszNIL);
	if ((ulNLen != _tcslen(IMAP4_NIL)) || (_tcsnicmp(lpszNIL, IMAP4_NIL, ulNLen) != 0))
	{
		m_lpszCurPos = lpszNIL;
		return EWILDCARD;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

// assume immediate response is FLAGS (if non-NULL enumeration function is supplied then it is called for the raw data)
EXC_TYPE CIMAP4RspParser::ExtractMsgFlags (IMAP4_MSGFLAGS& msgFlags, IMAP4_FLAGS_ENUM_CFN lpfnEcfn, LPVOID pArg)
{
	EXC_TYPE	exc=EOK;
	UINT32	ulFlgIndex=0;
	BOOLEAN	fContEnum=(lpfnEcfn != NULL);

	memset(&msgFlags, 0, (sizeof msgFlags));

	// allow "NIL" as flags list
	if (IMAP4_PARLIST_SDELIM != *m_lpszCurPos)
	{
		if ((exc=CheckNILParseBuffer(FALSE)) != EOK)
			return exc;

		return EOK;
	}

	// non-NIL flags
	for (m_lpszCurPos++; ; )
	{
		// find next flag
		if ((exc=FillNonEmptyParseBuffer()) != EOK)
			return exc;
		
		if (IMAP4_PARLIST_EDELIM == *m_lpszCurPos)
		{
			m_lpszCurPos++;
			break;
		}
		
		LPCTSTR	lpszFlag=m_lpszCurPos;
		for ( ; (!_istspace(*m_lpszCurPos)) && (IMAP4_PARLIST_EDELIM != *m_lpszCurPos) && (*m_lpszCurPos != _T('\0')); m_lpszCurPos++);
		
		// if reached end of string then refill and retry - flag is terminated by either a space or a list end
		if (_T('\0') == *m_lpszCurPos)
		{
			if ((exc=RefillFetchParseBuffer(lpszFlag)) != EOK)
				return exc;
			
			continue;
		}
		
		const UINT32					ulFLen=(m_lpszCurPos - lpszFlag);
		const IMAP4_MSGFLAG_CASE	fCase=imap4XlateExtMsgFlag(lpszFlag, ulFLen);
		if (fIsBadIMAP4MsgFlagCase(fCase))
			return ETRANSID;
		
		if ((exc=imap4UpdateMsgFlags(&msgFlags, fCase, TRUE)) != EOK)
			return exc;

		if (fContEnum)
		{
			if ((exc=(*lpfnEcfn)(ulFlgIndex, lpszFlag, ulFLen, pArg, &fContEnum)) != EOK)
				return exc;
			else
				ulFlgIndex++;
		}
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4RspParser::ExtractStringHdrVal (LPCTSTR&			lpszHdrVal,
															  UINT32&			ulHdrLen,
															  const BOOLEAN	fAllowEOS,
															  const BOOLEAN	fAllowOverflow)
{
	EXC_TYPE	exc=EOK;

	if ((exc=FillNonEmptyParseBuffer()) != EOK)
		return exc;

	// handle literal header value
	if (IMAP4_OCTCNT_SDELIM == *m_lpszCurPos)
		return ExtractLiteralHdrVal(lpszHdrVal, ulHdrLen, fAllowOverflow);

	TCHAR	chDelim=*m_lpszCurPos;
	if (IMAP4_QUOTE_DELIM != chDelim)
	{
		if (EOK == (exc=CheckNILParseBuffer(fAllowEOS)))
		{
			lpszHdrVal = _T("");
			ulHdrLen = 0;
			return EOK;
		}

		// this is not a NIL atom - check if non-quoted
		if (EWILDCARD != exc)
			return exc;

		chDelim = _T('\0');
	}
	else	// quoted string - skip initial quote
	{
		m_lpszCurPos++;
	}

	BOOLEAN	fMoreHdrData=TRUE;
	for (lpszHdrVal=m_lpszCurPos; ; lpszHdrVal=m_lpszCurPos)
	{
		if (IMAP4_QUOTE_DELIM == chDelim)
		{
			for ( ; (*m_lpszCurPos != _T('\0')); m_lpszCurPos++)
				if (IsIMAP4QuoteDelimCurPos())
				{
					fMoreHdrData = FALSE;
					break;
				}
		}
		else	// non-quoted atom
		{
			for ( ; (*m_lpszCurPos != _T('\0')); m_lpszCurPos++)
				if (_istspace(*m_lpszCurPos) || (IMAP4_PARLIST_EDELIM == *m_lpszCurPos))
				{
					fMoreHdrData = FALSE;
					break;
				}

			if (fAllowEOS && (_T('\0') == *m_lpszCurPos))
				fMoreHdrData = FALSE;
		}

		// if reached end of string and not entire hdr found, then refill and retry
		if (fMoreHdrData)
		{
			if ((exc=RefillFetchParseBuffer(lpszHdrVal)) != EOK)
				return exc;

			continue;
		}

		break;
	}

	ulHdrLen = (m_lpszCurPos - lpszHdrVal);
	if (IMAP4_QUOTE_DELIM == chDelim)
		m_lpszCurPos++;	// skip quote (if any)

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4RspParser::CopyStringHdrVal (LPTSTR				lpszHdrVal,
														  const UINT32		ulMaxLen,
														  UINT32&			ulHdrLen,
														  const BOOLEAN	fAllowEOS,
														  const BOOLEAN	fAllowOverflow)
{
	LPCTSTR	lpszHV=NULL;
	EXC_TYPE	exc=ExtractStringHdrVal(lpszHV, ulHdrLen, fAllowEOS, fAllowOverflow);
	if (exc != EOK)
		return exc;

	if (ulHdrLen >= ulMaxLen)
		return EOVERFLOW;

	_tcsncpy(lpszHdrVal, lpszHV, ulHdrLen);
	lpszHdrVal[ulHdrLen] = _T('\0');
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4RspParser::ExtractLiteralCount (UINT32& ulOCount)
{
	for (ulOCount=0, m_lpszCurPos++ ; 0 == ulOCount; )
	{
		EXC_TYPE	exc=FillNonEmptyParseBuffer();
		if (exc != EOK)
			return exc;

		LPCTSTR lpszOCount=m_lpszCurPos;
		for (; _istdigit(*m_lpszCurPos) && (*m_lpszCurPos != _T('\0')); m_lpszCurPos++);
		
		// if reached end of string then refill and retry - octet count terminated by a '}'
		if (_T('\0') == *m_lpszCurPos)
		{
			if ((exc=RefillFetchParseBuffer(lpszOCount)) != EOK)
				return exc;
			
			continue;
		}
		
		UINT32	ulCLen=(m_lpszCurPos - lpszOCount);
		ulOCount = argument_to_dword(lpszOCount, ulCLen, EXC_ARG(exc));
		if (exc != EOK)
			return exc;

		// find end of octet count
		while (IMAP4_OCTCNT_EDELIM != *m_lpszCurPos)
		{
			if ((exc=FillNonEmptyParseBuffer()) != EOK)
				return exc;
		}
		
		break;
	}

	// ingoring anything after literal count
	return SkipToEndOfLine();
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4RspParser::ExtractLiteralHdrVal (LPCTSTR&			lpszHdrVal,
																UINT32&			ulHdrLen,
																const BOOLEAN	fAllowOverflow)
{
	EXC_TYPE	exc=ExtractLiteralCount(ulHdrLen);
	if (exc != EOK)
		return exc;

	// ignore anything till end of line since literal follows on next line
	if ((exc=SkipToEndOfLine()) != EOK)
		return exc;

	// the header literal count must fit into a response buffer
	if (ulHdrLen >= m_ulRspBufLen)
	{
		if (!fAllowOverflow)
			return EOVERFLOW;

		// use 1st half of response buffer to hold initial data
		UINT32	ulMaxReadLen=(m_ulRspBufLen / 2);
		BOOLEAN	fStripCRLF=FALSE;

		// read up to 1/2 buffer or CRLF
		int	oLen=m_SBSock.ReadCmd(m_lpszRspBuf, ulMaxReadLen, (SINT32) m_ulRspTimeout, &fStripCRLF);
		if (oLen < 0)
			return ENOTCONNECTION;

		// now use whatever is left of the response buffer to skip the overflow data
		LPTSTR	lpszReadBuf=(m_lpszRspBuf + oLen + 1);
		UINT32	ulRemLen=(ulHdrLen - oLen);
		if (fStripCRLF)
			ulRemLen -= 2;

		ulHdrLen = oLen;
		ulMaxReadLen = (m_ulRspBufLen - oLen - 1);

		while (ulRemLen > 0)
		{
			UINT32	ulSLen=min(ulRemLen, ulMaxReadLen);
			if ((UINT32) (oLen=m_SBSock.Fill(lpszReadBuf, (size_t) ulSLen, (SINT32) m_ulRspTimeout)) != ulSLen)
				return ENOTCONNECTION;
			ulRemLen -= (UINT32) oLen;
		}
	}
	else	// read the literal data
	{
		int	rLen=m_SBSock.Fill(m_lpszRspBuf, (size_t) ulHdrLen, (SINT32) m_ulRspTimeout);
		if ((UINT32) rLen != ulHdrLen)
			return ENOTCONNECTION;
	}

	m_lpszRspBuf[ulHdrLen] = _T('\0');
	if (0 == ulHdrLen)
		lpszHdrVal = _T("");
	else
		lpszHdrVal = m_lpszRspBuf;

	m_lpszCurPos = &m_lpszRspBuf[ulHdrLen];
	m_fStripCRLF = FALSE;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4RspParser::ExtractNumVal (LPCTSTR& lpszNumVal, UINT32& ulNumLen)
{
	lpszNumVal = NULL;
	ulNumLen = 0;

	EXC_TYPE	exc=FillNonEmptyParseBuffer();
	if (exc != EOK)
		return exc;

	while (0 == ulNumLen)
	{
		for (lpszNumVal=m_lpszCurPos; _istdigit(*m_lpszCurPos) && (*m_lpszCurPos != _T('\0')); m_lpszCurPos++);

		// if reached end of string then refill and retry - value is incomplete
		if (_T('\0') == *m_lpszCurPos)
		{
			if ((exc=RefillFetchParseBuffer(lpszNumVal)) != EOK)
				return exc;
			
			continue;
		}

		if (0 == (ulNumLen=(m_lpszCurPos - lpszNumVal)))
			return EINVALIDNUMERIC;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4RspParser::ExtractNumVal (UINT32& ulNumVal)
{
	ulNumVal = (UINT32) (-1);

	LPCTSTR	lpszNumVal=NULL;
	UINT32	ulNumLen=0;
	EXC_TYPE	exc=ExtractNumVal(lpszNumVal, ulNumLen);
	if (EOK == exc)
		ulNumVal = argument_to_dword(lpszNumVal, ulNumLen, EXC_ARG(exc));

	return exc;
}

/*---------------------------------------------------------------------------*/

/*		Handles a response of type "* OPCODE fldr (params list)" as
 * received thru "HandleUntaggedResponse". In this case, "lpszOpArg"
 * should be the "lpszOp" argument of the "HandleUntaggedResponse".
 *		Upon return, places "m_lpszCurPos" one place beyond the '('.
 */
EXC_TYPE CIMAP4RspParser::SkipFolderUpToParamsList (LPCTSTR lpszOpArg)
{
	EXC_TYPE	exc=EOK;

	// allow literal folder name
	if (IMAP4_OCTCNT_SDELIM == *lpszOpArg)
	{
		UINT32	ulDataLen=0;
		BOOLEAN	fIsLitPlus=FALSE;
		if ((exc=imap4ExtractLiteralCount(lpszOpArg, &ulDataLen, &fIsLitPlus)) != EOK)
			return exc;
		if (fIsLitPlus)
			return EMULTIHOP;
		if ((exc=SkipLiteralData(ulDataLen)) != EOK)
			return exc;
	}

	if ((exc=FillNonEmptyParseBuffer()) != EOK)
		return exc;

	// we skip the folder name since not interested in it
	while (*m_lpszCurPos != IMAP4_PARLIST_SDELIM)
	{
		if ((IMAP4_OCTCNT_SDELIM == *m_lpszCurPos) || (IMAP4_QUOTE_DELIM == *m_lpszCurPos))
		{
			LPCTSTR	lpszFolderName=NULL;
			UINT32	ulFNLen=0;

			if ((exc=ExtractStringHdrVal(lpszFolderName, ulFNLen, FALSE, FALSE)) != EOK)
				return exc;
		}
		else
			m_lpszCurPos++;

		if ((exc=FillNonEmptyParseBuffer()) != EOK)
			return exc;
	}

	m_lpszCurPos++;	// skip options list start delimiter
	return EOK;
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE imap4GetNonPairedRsp (LPCTSTR		lpszRsp,
												  TCHAR			szTag[],
												  const UINT32	ulMaxTagLen,
												  LPCTSTR&		lpszCont)
{
	szTag[0] = _T('\0');
	lpszCont = NULL;

	if (IMAP4_CONTINUE_RSP == *lpszRsp)
	{
		lpszCont = lpszRsp;
		return ECONTINUED;
	}

	if (IMAP4_UNTAGGED_RSP == *lpszRsp)
	{
		EXC_TYPE	exc=imap4GetArg((lpszRsp + 1), szTag, ulMaxTagLen, &lpszCont);
		if (exc != EOK)
			return exc;

		return EDATACHAIN;
	}

	return imap4GetArg(lpszRsp, szTag, ulMaxTagLen, &lpszCont);
}

/*---------------------------------------------------------------------------*/

inline EXC_TYPE imap4GetNonPairedTaggedRsp (LPCTSTR		lpszRsp,
														  TCHAR			szOp[],
														  const UINT32	ulMaxOpLen,
														  LPCTSTR&		lpszCont)
{
	return imap4GetArg(lpszRsp, szOp, ulMaxOpLen, &lpszCont);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4RspParser::ParseResponse (const BOOLEAN fRspPair)
{
	BOOLEAN	fHaveTagMatch=FALSE;

	for (UINT32	ulRdx=0; (!fHaveTagMatch); ulRdx++)
	{
		// read next response buffer
		int	rLen=m_SBSock.ReadCmd(m_lpszRspBuf, m_ulRspBufLen, (SINT32) m_ulRspTimeout, &m_fStripCRLF);
		if (rLen <= 0)
			return ENOTCONNECTION;

		for (UINT32	ulSdx=0; ; ulSdx++)
		{
			TCHAR		szTag[MAX_IMAP4_TAG_LEN+2]=_T(""), szOp[MAX_IMAP4_OPCODE_LEN+2]=_T("");
			EXC_TYPE	exc=EOK;

			if (fRspPair)
				exc = imap4ExtractRsp(m_lpszRspBuf, szTag, MAX_IMAP4_TAG_LEN, szOp, MAX_IMAP4_OPCODE_LEN, &m_lpszCurPos);
			else
				exc = imap4GetNonPairedRsp(m_lpszRspBuf, szTag, MAX_IMAP4_TAG_LEN, m_lpszCurPos);

			if (EOK == exc)
			{
				// if tag matches then translate operation code (OK/BAD/NO)
				if (_tcsicmp(m_lpszTag, szTag) != 0)
					return EILLOGICALRENAME;
				fHaveTagMatch = TRUE;

				if (!fRspPair)
				{
					if ((exc=imap4GetNonPairedTaggedRsp(m_lpszCurPos, szOp, MAX_IMAP4_OPCODE_LEN, m_lpszCurPos)) != EOK)
						return exc;
				}

				if ((exc=SkipToEndOfLine()) != EOK)
					return exc;
				
				if ((exc=imap4XlateRspCode(szOp)) != EOK)
					return exc;

				break;
			}
			
			if (EDATACHAIN != exc)
				return exc;	// do not allow any other response other than untagged
			
			if ((exc=HandleUntaggedResponse(szTag, szOp)) != EOK)
			{
				if (ENOTCONNECTION == exc)
					return exc;

				// try to re-synchronize
				if ((exc=ResyncResponse(m_lpszTag, exc)) != EOK)
					return exc;

				continue;
			}

			// check if read entire response for the specified message
			if ((exc=SkipToEndOfLine()) != EOK)
				return exc;

			break;
		}
	}

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////
