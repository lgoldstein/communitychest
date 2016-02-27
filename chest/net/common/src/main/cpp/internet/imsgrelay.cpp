#include <internet/pop3Lib.h>
#include <futils/general.h>

//////////////////////////////////////////////////////////////////////////////

EXC_TYPE IMsgRelayStream::SignalMsgError (const UINT32 ulMsgNo, const EXC_TYPE err)
{
	TCHAR		szErr[MAX_DWORD_DISPLAY_LENGTH+2]=_T("");
	size_t	eLen=dword_to_argument((DWORD) err, szErr);

	return HandleProtoState(POP3_ERR, ulMsgNo, szErr);
}

/*--------------------------------------------------------------------------*/

int IMsgRelayStream::WriteV (LPCTSTR lpszFmt, va_list ap)
{
	if (IsEmptyStr(lpszFmt))
		return (-3);

	TCHAR	szLine[POP3_MAX_LINE_LENGTH+2]=_T("");
	int	wLen=_vsntprintf(szLine, POP3_MAX_LINE_LENGTH, lpszFmt, ap);
	if (wLen < 0)
		return wLen;
	szLine[wLen] = _T('\0');

	return Write(szLine, wLen);
}

/*--------------------------------------------------------------------------*/

int IMsgRelayStream::Writef (LPCTSTR lpszFmt, ...)
{
	va_list	ap;
	va_start(ap, lpszFmt);
	int	wLen=WriteV(lpszFmt, ap);
	va_end(ap);
	return wLen;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE IMsgRelayStream::LogVMsg (const UINT32		ulLevel,
											  const EXC_TYPE	rexc,
											  LPCTSTR			lpszFmt,
											  va_list			ap)
{
	TCHAR	szLogMsg[POP3_MAX_LINE_LENGTH+2]=_T("");
	int	wLen=_vsntprintf(szLogMsg, POP3_MAX_LINE_LENGTH, lpszFmt, ap);
	if (wLen <= 0)
		wLen = POP3_MAX_LINE_LENGTH;
	szLogMsg[wLen] = _T('\0');

	HandleProtoState(szPOP3HelpCmd, ulLevel, szLogMsg);
	return rexc;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE IMsgRelayStream::LogMsgf (const UINT32			ulLevel,
												const EXC_TYPE		rexc,
												LPCTSTR					lpszFmt,
												...)
{
	va_list	ap;
	va_start(ap, lpszFmt);
	EXC_TYPE	exc=LogVMsg(ulLevel, rexc, lpszFmt, ap);
	va_end(ap);
	return exc;
}

/////////////////////////////////////////////////////////////////////////////

CMsgFilesRelay::CMsgFilesRelay ()
	: IMsgRelayStream(), m_fp(NULL), m_lpszFilePath(NULL),
	  m_lpszFilesDir(NULL), m_lpszFilesPrefix(NULL), m_lpszFilesSuffix(NULL)
{
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CMsgFilesRelay::Reset (const BOOLEAN fRemoveFile)
{
	EXC_TYPE	exc=EOK;

	if (m_fp != NULL)
	{
		fclose(m_fp);
		m_fp = NULL;
	}

	if (fRemoveFile)
	{
		if (!IsEmptyStr(m_lpszFilePath))
			exc = _tremove(m_lpszFilePath);
	}

	strreleasebuf(m_lpszFilePath);
	return exc;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CMsgFilesRelay::Cleanup ()
{
	EXC_TYPE	exc=Reset(TRUE);

	strreleasebuf(m_lpszFilesDir);
	strreleasebuf(m_lpszFilesPrefix);
	strreleasebuf(m_lpszFilesSuffix);

	return exc;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CMsgFilesRelay::StartMsgRetrieval (const UINT32 /* ulMsgNo */)
{
	EXC_TYPE	exc=Reset(TRUE);	// delete any previously open file
	TCHAR		szFilePath[MAX_PATH+2]=_T("");
	UINT		uid=GetTempFileName((IsEmptyStr(m_lpszFilesDir) ? _T(".") : m_lpszFilesDir),
										  (IsEmptyStr(m_lpszFilesPrefix) ? _T("p3r") : m_lpszFilesPrefix),
										  0, szFilePath);
	if (0 == uid)
		return EPATHNAMESYNTAX;

	if (!IsEmptyStr(m_lpszFilesSuffix))
	{
		exc = _tremove(szFilePath);

		LPTSTR	lpszSuffix=_tcsrchr(szFilePath, _T('.'));
		if (NULL == lpszSuffix)
		{
			lpszSuffix = strlast(szFilePath);
			lpszSuffix = strladdch(lpszSuffix, _T('.'));
		}

		if (_T('.') == *m_lpszFilesSuffix)
			_tcscpy(lpszSuffix, m_lpszFilesSuffix);
		else
			_tcscpy((lpszSuffix+1), m_lpszFilesSuffix);
	}

	if ((exc=strupdatebuf(szFilePath, m_lpszFilePath)) != EOK)
		return exc;

	if (NULL == (m_fp=_tfopen(m_lpszFilePath, _T("wb"))))
		return EFNEXIST;

	return EOK;
}

/*--------------------------------------------------------------------------*/

// Only start/stop of message is handled
EXC_TYPE CMsgFilesRelay::HandleProtoState (LPCTSTR			lpszProtoCmd,
														 const UINT32	ulMsgNo,
														 LPCTSTR			/* lpszCmdVal */)
{
	if (IsEmptyStr(lpszProtoCmd))
		return EILLEGALOPCODE;

	if (_tcsicmp(lpszProtoCmd, szPOP3TopCmd) == 0)
		return StartMsgRetrieval(ulMsgNo);
	else if (_tcsicmp(lpszProtoCmd, szPOP3RetrCmd) == 0)
		return StopMsgRetrieval(ulMsgNo);

	return EOK;
}

/*--------------------------------------------------------------------------*/

int CMsgFilesRelay::Write (LPCTSTR lpszData, const size_t dLen)
{
	if (NULL == m_fp)
		return (-1);

	if (0 == dLen)
		return 0;

	if (NULL == lpszData)
		return (-3);

	return fwrite(lpszData, sizeof(TCHAR), dLen, m_fp);
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CMsgFilesRelay::SetInfo (LPCTSTR	lpszFilesDir,		// NULL/empty == CWD
											  LPCTSTR	lpszFilesPrefix,	// NULL/empty == auto-create
											  LPCTSTR	lpszFilesSuffix)	// NULL/empty == auto-create
{
	EXC_TYPE	exc=Cleanup();

	if (!IsEmptyStr(lpszFilesDir))
	{
		if ((exc=strupdatebuf(lpszFilesDir, m_lpszFilesDir)) != EOK)
			return exc;
	}

	if (!IsEmptyStr(lpszFilesPrefix))
	{
		if ((exc=strupdatebuf(lpszFilesPrefix, m_lpszFilesPrefix)) != EOK)
			return exc;
	}

	if (!IsEmptyStr(lpszFilesSuffix))
	{
		if ((exc=strupdatebuf(lpszFilesSuffix, m_lpszFilesSuffix)) != EOK)
			return exc;
	}

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

// Note: all the function preserves EXACTLY the input line length
UINT32 AdjustParsedLine (const RFC822MSGSTATE	msgState,
								 LPTSTR						lpszLine,
								 const UINT32				ulLineLen,
								 const UINT32				ulLineNdx,
								 BOOLEAN&					fAdjusted)
{
	fAdjusted = FALSE;

	if ((msgState > RFC822_ENVELOPE_MSGSTATE) ||		// adjust only envelope headers
		 IsEmptyStr(lpszLine) || (0 == ulLineLen) ||	// provided they are not empty
		 IsSafeSpace(*lpszLine))							// and not starting with a space character
		return ulLineLen;

	LPTSTR		lpszLineEnd=(lpszLine + ulLineLen);
	const TCHAR	chLastChar=*lpszLineEnd;
	if (chLastChar != _T('\0'))
		*lpszLineEnd = _T('\0');

	// check if header parsing succeeds
	CRFC822HdrParser	hdrParser;
	EXC_TYPE				exc=hdrParser.ProcessHdr(lpszLine);

	if (chLastChar != _T('\0'))
		*lpszLineEnd = chLastChar;

	if (EOK == exc)
		return ulLineLen;
	exc = EOK;

	// build an "X-" header prefix
	static const TCHAR	szXAdjustedPrefix[]=_T("X-Adjusted-");
	TCHAR	szXHdrName[MAX_RFC822_HDR_NAME_LEN+2]=_T("");
	CStrlBuilder	strb(szXHdrName, MAX_RFC822_HDR_NAME_LEN);
	if (EOK == exc)
		exc = strb.AddStr(szXAdjustedPrefix);
	if (EOK == exc)
		exc = strb.AddNum(ulLineNdx);
	if (EOK == exc)
		exc = strb.AddStr(_T(": "));

	// if cannot build the "X-" header or it exceeds available line length, then
	// do nothing (since we are commited to not changing the line length
	const UINT32	ulXLen=_tcslen(szXHdrName);
	if ((exc != EOK) || (ulXLen >= ulLineLen))
		return ulLineLen;

	// make room for "ulXLen" characters by "shifting" the original line
	for (UINT32	ulSrcPos=(ulLineLen - ulXLen), ulDstPos=ulLineLen; ulSrcPos > 0; ulSrcPos--, ulDstPos--)
		lpszLine[ulDstPos-1] = lpszLine[ulSrcPos-1];

	// add the "X-Header"
	const TCHAR	chXHdrEnd=lpszLine[ulXLen];
	_tcscpy(lpszLine, szXHdrName);
	lpszLine[ulXLen] = chXHdrEnd;

	fAdjusted = TRUE;
	return ulLineLen;
}

//////////////////////////////////////////////////////////////////////////////

// callback function used to process a relay event
typedef EXC_TYPE (*IMRDS_PROTORLYCMD_HNDLR_CFN)(IMsgRelayDetailedStream&	imrds,
																const UINT32					ulMsgNum,
																LPCTSTR							lpszVal);

static EXC_TYPE imrdsConnectHandler (IMsgRelayDetailedStream&	imrds,
												 const UINT32					ulConnId,
												 LPCTSTR							lpszVal)
{
	ISockioInterface	*pSIO=(ISockioInterface *) ulConnId;
	if (NULL == pSIO)
		return imrds.LogMsgf(IMsgRelayDetailedStream::IMSGRLY_PROTO_WARNING, ECONTEXT, _T("imrdsConnectHandler(%s) - no connection interface"), GetSafeStrPtr(lpszVal));

	return imrds.HandleRelayConnect(*pSIO, lpszVal);
}

static EXC_TYPE imrdsMsgErrorHandler (IMsgRelayDetailedStream&	imrds,
												  const UINT32					ulMsgNum,
												  LPCTSTR						lpszVal)
{
	EXC_TYPE	exc=EOK, msgErr=argument_to_dword(lpszVal, GetSafeStrlen(lpszVal), EXC_ARG(exc));
	if (exc != EOK)
		return imrds.LogMsgf(IMsgRelayDetailedStream::IMSGRLY_PROTO_WARNING, exc, _T("imrdsMsgErrorHandler(%s) - bad/illegal (0x%08x) value"), GetSafeStrPtr(lpszVal), exc);

	return imrds.HandleMsgError(msgErr);
}

static EXC_TYPE imrdsLogHandler (IMsgRelayDetailedStream&	imrds,
											const UINT32					ulMsgNum,
											LPCTSTR							lpszVal)
{
	return imrds.HandleLogMsg(ulMsgNum, lpszVal);
}

static EXC_TYPE imrdsRelayStartHandler (IMsgRelayDetailedStream&	imrds,
													 const UINT32					ulMsgNum,
													 LPCTSTR							lpszVal)
{
	return imrds.HandleRelayStart(lpszVal);
}

static EXC_TYPE imrdsMboxStatHandler (IMsgRelayDetailedStream&	imrds,
												  const UINT32					ulMsgNum,
												  LPCTSTR						lpszVal)
{
	extern EXC_TYPE ExtractPOP3StatResponse (LPCTSTR lpszStatRsp, UINT32	*pulMsgsNum, UINT32 *pulMboxSize);
	UINT32	ulMsgsNum=0, ulMboxSize=0;
	EXC_TYPE	exc=ExtractPOP3StatResponse(lpszVal, &ulMsgsNum, &ulMboxSize);
	if (exc != EOK)
		return imrds.LogMsgf(IMsgRelayDetailedStream::IMSGRLY_PROTO_WARNING, exc, _T("imrdsMboxStatHandler - cannot (0x%08x) extract info from value=%s"), exc, GetSafeStrPtr(lpszVal));

	return imrds.HandleMboxStatus(ulMsgsNum, ulMboxSize);
}

static EXC_TYPE imrdsMsgStartHandler (IMsgRelayDetailedStream&	imrds,
												  const UINT32					ulMsgNum,
												  LPCTSTR						lpszVal)
{
	return imrds.HandleMsgRelayStart(ulMsgNum);
}

static EXC_TYPE imrdsMsgUIDLHandler (IMsgRelayDetailedStream&	imrds,
												 const UINT32					ulMsgNum,
												 LPCTSTR							lpszVal)
{
	return imrds.HandleMsgUIDL(ulMsgNum, lpszVal);
}

static EXC_TYPE imrdsMsgEndHandler (IMsgRelayDetailedStream&	imrds,
												const UINT32					ulMsgNum,
												LPCTSTR							lpszVal)
{
	return imrds.HandleMsgRelayEnd(ulMsgNum);
}

static EXC_TYPE imrdsDelMsgHandler (IMsgRelayDetailedStream&	imrds,
												const UINT32					ulMsgNum,
												LPCTSTR							lpszVal)
{
	return imrds.QueryMsgDeletion(ulMsgNum);
}

static EXC_TYPE imrdsRelayEndHandler (IMsgRelayDetailedStream&	imrds,
												  const UINT32					relExc,
												  LPCTSTR						lpszUID)
{
	return imrds.HandleRelayEnd(lpszUID, relExc);
}

/*--------------------------------------------------------------------------*/

// function(s) to call for relay event
static const STR2PTRASSOC imrdsHcmdAssoc[]={
	{	POP3_OK,			(LPVOID) imrdsConnectHandler		},	// connected - arg=details
	{	POP3_ERR,		(LPVOID) imrdsMsgErrorHandler		}, // some parsing error - arg=errno
	{	szPOP3UserCmd,	(LPVOID)	imrdsRelayStartHandler	},	// starting relay - arg=POP3 host
	{	szPOP3StatCmd,	(LPVOID) imrdsMboxStatHandler		},	// expected remote retrieval size - arg=size
	{	szPOP3HelpCmd,	(LPVOID) imrdsLogHandler			},	// log - arg=log msg string
	{	szPOP3TopCmd,	(LPVOID) imrdsMsgStartHandler		},	// start relay of message - arg=msg ID
	{	szPOP3UidlCmd,	(LPVOID) imrdsMsgUIDLHandler		},	// check msg UIDL - arg=UIDL info
	{	szPOP3RetrCmd,	(LPVOID) imrdsMsgEndHandler		},	// end relay of message - arg=msg ID
	{	szPOP3DeleCmd,	(LPVOID) imrdsDelMsgHandler		},	// query msg delete AFTER retrieve
	{	szPOP3QuitCmd,	(LPVOID)	imrdsRelayEndHandler		},	// end relay - arg=error code

	{	NULL,				NULL										}	// mark end of list
};
static const CStr2PtrMapper imrdsHcmdMap(imrdsHcmdAssoc, 0, FALSE);

/*--------------------------------------------------------------------------*/

EXC_TYPE IMsgRelayDetailedStream::DetailProtoState (LPCTSTR			lpszProtoCmd,
																	 const UINT32	ulMsgNo,
																	 LPCTSTR			lpszCmdVal)
{
	LPVOID	pV=NULL;
	EXC_TYPE	exc=imrdsHcmdMap.FindKey(lpszProtoCmd, pV);
	if (exc != EOK)
		return LogMsgf(IMSGRLY_PROTO_WARNING, exc, _T("IMsgRelayDetailedStream::DetailProtoState() - cannot (0x%08x) find cmd=%s handler"), exc, GetSafeStrPtr(lpszProtoCmd));

	IMRDS_PROTORLYCMD_HNDLR_CFN	lpfnHcfn=(IMRDS_PROTORLYCMD_HNDLR_CFN) pV;
	if ((exc=(*lpfnHcfn)(*this, ulMsgNo, lpszCmdVal)) != EOK)
		return exc;

	return EOK;
}

/*--------------------------------------------------------------------------*/
