#include <internet/esmtpLib.h>
#include <internet/pop3Lib.h>

/*---------------------------------------------------------------------------*/

CSMTPMsgRelay::CSMTPMsgRelay ()
	: IMsgRelayStream(), m_ulCurMsg(0), m_fSingleConn(TRUE), m_rme(),
	  m_ulDetectBufSize(0), m_ulCurDetect(0), m_lpszDetectBuf(NULL),
	  m_fAutoDetectSndr(FALSE)
{
	memset(&m_rcpDef, 0, (sizeof m_rcpDef));
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CSMTPMsgRelay::Reset ()
{
	EXC_TYPE	exc=EOK;
	UINT32	rcode=(UINT32) (-1);

	m_ulCurMsg = 0;
	m_ulCurDetect = 0;

	if (m_lpszDetectBuf != NULL)
		*m_lpszDetectBuf = _T('\0');

	if (m_fSingleConn)
	{
		if ((exc=smtpSendReset(m_CBSock, rcode, m_rcpDef.ulRspTimeout)) != EOK)
			return exc;

		if (rcode != SMTP_E_ACTION_OK)
			return EILLEGALOPCODE;
	}
	else	// don't care if QUIT succeeds or not
	{
		exc = smtpQuit(m_CBSock, rcode, m_rcpDef.ulRspTimeout);
	}

	m_rme.Reset();

	if (m_fAutoDetectSndr)
	{
		LPTSTR	lpszV=(LPTSTR) m_rcpDef.lpszSMTPSndr;
		if (lpszV != NULL)
		{
			strreleasebuf(lpszV);
			m_rcpDef.lpszSMTPSndr = NULL;
		}
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CSMTPMsgRelay::Cleanup ()
{
	EXC_TYPE	exc=Reset();
	LPTSTR	lpszV=NULL;
	UINT32	rcode=(UINT32) (-1);

	// just making sure...
	exc = smtpQuit(m_CBSock, rcode, m_rcpDef.ulRspTimeout);
	exc = m_CBSock.Close();

	if ((lpszV=(LPTSTR) m_rcpDef.lpszSMTPHost) != NULL)
		strreleasebuf(lpszV);
	if ((lpszV=(LPTSTR) m_rcpDef.lpszSMTPSndr) != NULL)
		strreleasebuf(lpszV);
	if ((lpszV=(LPTSTR) m_rcpDef.lpszSMTPRecip) != NULL)
		strreleasebuf(lpszV);
	if ((lpszV=(LPTSTR) m_rcpDef.lpszSMTPAuthUID) != NULL)
		strreleasebuf(lpszV);
	if ((lpszV=(LPTSTR) m_rcpDef.lpszSMTPAuthPass) != NULL)
		strreleasebuf(lpszV);

	if (m_lpszDetectBuf != NULL)
		strreleasebuf(m_lpszDetectBuf);
	m_ulDetectBufSize = 0;

	memset(&m_rcpDef, 0, (sizeof m_rcpDef));

	m_fSingleConn = TRUE;
	m_fAutoDetectSndr = FALSE;

	return EOK;
}

/*---------------------------------------------------------------------------*/

// Note: if auto-sender option used then relay must be performed with msg parsing option enabled !!!
EXC_TYPE CSMTPMsgRelay::SetInfo (const SMTPMSGRELAYDEF&	rcpDef,
											 const BOOLEAN					fSingleConn,
											 const UINT32					ulDetectBufSize)
{
	EXC_TYPE	exc=Cleanup();
	LPTSTR	lpszV=NULL;

	if ((exc=strupdatebuf(rcpDef.lpszSMTPHost, lpszV)) != EOK)
		return exc;
	m_rcpDef.lpszSMTPHost = lpszV;

	if ((exc=strupdatebuf(rcpDef.lpszSMTPRecip, lpszV)) != EOK)
		return exc;
	m_rcpDef.lpszSMTPRecip = lpszV;

	if (!IsEmptyStr(rcpDef.lpszSMTPSndr))
	{
		if ((exc=strupdatebuf(rcpDef.lpszSMTPSndr, lpszV)) != EOK)
			return exc;
		m_rcpDef.lpszSMTPSndr = lpszV;
		m_fAutoDetectSndr = FALSE;
	}
	else	// need auto-detect sender
	{
		if (ulDetectBufSize < MAX_RFC822_MSGID_LEN)
			return EOVERFLOW;

		if (NULL == (m_lpszDetectBuf=new TCHAR[ulDetectBufSize+2]))
			return EMEM;
		*m_lpszDetectBuf = _T('\0');

		m_ulDetectBufSize = ulDetectBufSize;
		m_fAutoDetectSndr = TRUE;
	}

	if (!IsEmptyStr(rcpDef.lpszSMTPAuthUID))
	{
		if ((exc=strupdatebuf(rcpDef.lpszSMTPAuthUID, lpszV)) != EOK)
			return exc;
		m_rcpDef.lpszSMTPAuthUID = lpszV;

		if ((exc=strupdatebuf(rcpDef.lpszSMTPAuthPass, lpszV)) != EOK)
			return exc;
		m_rcpDef.lpszSMTPAuthPass = lpszV;
	}

	m_rcpDef.iSMTPPort = rcpDef.iSMTPPort;
	m_rcpDef.ulRspTimeout = rcpDef.ulRspTimeout;

	m_fSingleConn = fSingleConn;

	return EOK;
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE ConnectSMTPServer (ISockioInterface& CBSock, const SMTPMSGRELAYDEF& rcpDef)
{
	UINT32	rcode=(UINT32) (-1);
	EXC_TYPE	exc=smtpOpenSock(CBSock, rcpDef.lpszSMTPHost, rcpDef.iSMTPPort, rcode, rcpDef.ulRspTimeout);
	if (exc != EOK)
		return exc;

	if (rcode != SMTP_E_DOMAIN_RDY)
		return ESTATE;

	// check if need authenticated SMTP
	if (IsEmptyStr(rcpDef.lpszSMTPAuthUID))
	{
		if ((exc=smtpSendHelo(CBSock, rcpDef.lpszSMTPHost, rcode, rcpDef.ulRspTimeout)) != EOK)
			return exc;
		if (rcode != SMTP_E_ACTION_OK)
			return EILLEGALOPCODE;
	}
	else
	{
		if ((exc=esmtpSendEHelo(CBSock, NULL, NULL, NULL, rcode, rcpDef.ulRspTimeout)) != EOK)
			return exc;
		if (rcode != SMTP_E_ACTION_OK)
			return EILLEGALOPCODE;

		exc = esmtpAuthLogin(CBSock, rcpDef.lpszSMTPAuthUID, rcpDef.lpszSMTPAuthPass, rcode, rcpDef.ulRspTimeout);
		if (exc != EOK)
			return exc;
		if (rcode != ESMTP_E_AUTH_SUCCEED)
			return EFACCESS;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CSMTPMsgRelay::RestartSMTPSession (LPCTSTR lpszSndr)
{
	if (IsEmptyStr(lpszSndr))
		return EBADADDR;

	if (0 == m_ulCurMsg)
		return ECONTEXT;

	UINT32	rcode=(UINT32) (-1);
	EXC_TYPE	exc=EOK;
	if (!m_fSingleConn)
	{
		if ((exc=ConnectSMTPServer(m_CBSock, m_rcpDef)) != EOK)
		{
			Reset();
			return exc;
		}
	}

	if ((exc=smtpSetSender(m_CBSock, lpszSndr, rcode, m_rcpDef.ulRspTimeout)) != EOK)
		return exc;
	if (rcode != SMTP_E_ACTION_OK)
		return EILLEGALOPCODE;

	if ((exc=smtpAddRecepient(m_CBSock, m_rcpDef.lpszSMTPRecip, rcode, m_rcpDef.ulRspTimeout)) != EOK)
		return exc;
	if (rcode != SMTP_E_ACTION_OK)
		return EILLEGALOPCODE;

	if ((exc=smtpStartData(m_CBSock, rcode, m_rcpDef.ulRspTimeout)) != EOK)
		return exc;
	if (rcode != SMTP_E_START_INP)
		return EILLEGALOPCODE;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CSMTPMsgRelay::StartMsgRetrieval (const UINT32 ulMsgNo)
{
	Reset();

	m_ulCurMsg = ulMsgNo;

	// check if auto-detect sender
	if (!IsEmptyStr(m_rcpDef.lpszSMTPSndr))
		return RestartSMTPSession(m_rcpDef.lpszSMTPSndr);

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CSMTPMsgRelay::StopMsgRetrieval (const UINT32 /* ulMsgNo */)
{
	if (!m_rme.IsMsgEOM())
	{
		int	wLen=Write(_T("\r\n.\r\n"), 5);
		if (wLen <= 0)
			return ENOTCONNECTION;
	}

	UINT32	rcode=(UINT32) (-1);
	TCHAR		szRsp[MAX_SMTP_CMD_LINE_LEN+2]=_T("");
	// allow longer time for final response after message end to compensate for possible server processing
	EXC_TYPE	exc=smtpGetFinalResponse(m_CBSock, szRsp, MAX_SMTP_CMD_LINE_LEN, rcode, MAX_ISRV_DPROC_TIME(m_rcpDef.ulRspTimeout));
	if (exc != EOK)
		return exc;

	if (rcode != SMTP_E_ACTION_OK)
		exc = EILLEGALOPCODE;

	EXC_TYPE	err=Reset();
	if (err != EOK)
		exc = err;

	return exc;
}

/*---------------------------------------------------------------------------*/

// Only start/stop of message is handled
EXC_TYPE CSMTPMsgRelay::HandleProtoState (LPCTSTR			lpszProtoCmd,
														const UINT32	ulMsgNo,
														LPCTSTR			/* lpszCmdVal */)
{
	if (IsEmptyStr(lpszProtoCmd))
		return EILLEGALOPCODE;

	if (m_fSingleConn)
	{
		// if single connection then use relay start event to create connection
		if (_tcsicmp(lpszProtoCmd, szPOP3UserCmd) == 0)
			return ConnectSMTPServer(m_CBSock, m_rcpDef);
	}

		// use relay end event to close connection
	if (_tcsicmp(lpszProtoCmd, szPOP3QuitCmd) == 0)
	{
		UINT32	rcode=(UINT32) (-1);
		// don't care if QUIT failed since message sent successfully (see "StopMsgRetrieval")
		smtpQuit(m_CBSock, rcode, m_rcpDef.ulRspTimeout);
	}
	else if (_tcsicmp(lpszProtoCmd, szPOP3TopCmd) == 0)
		return StartMsgRetrieval(ulMsgNo);
	else if (_tcsicmp(lpszProtoCmd, szPOP3RetrCmd) == 0)
		return StopMsgRetrieval(ulMsgNo);

	return EOK;
}

/*---------------------------------------------------------------------------*/

// Note(s):
//
//		a. called BEFORE writing the data
//		b. called only if msg parsing requested
EXC_TYPE CSMTPMsgRelay::HandleMsgContents (const UINT32				/* ulMsgNo */,
														 const RFC822MSGEXCASE	mxCase,
														 CRFC822MsgExtractor&	msgEx)
{
	EXC_TYPE	exc=EOK;
	LPCTSTR	lpszAddr=NULL;
	UINT32	ulAddrLen=0;

	// need parsing only if auto-detect sender
	if (!IsEmptyStr(m_rcpDef.lpszSMTPSndr))
		return EOK;

	// if by the end of message headers we have no sender then use a default
	if (RFC822MSGEX_MSGHDR_CASE != mxCase)
	{
		// if this is a direct attachment then allow temporarily "attachment" headers
		if (msgEx.IsDirectAttach() && (RFC822MSGEX_ATTHDR_CASE == mxCase))
			return EOK;

		exc = ECONTEXT;

		LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, exc, _T("CSMTPMsgRelay::HandleMsgContents() - no sender address before end of headers"));
	}
	else	// still hunting for sender headers
	{
		// look for "From:" or "Sender:"
		LPCTSTR	lpszHdrName=msgEx.GetHdrName();
		if ((_tcsicmp(lpszHdrName, pszStdFromHdr) != 0) && (_tcsicmp(lpszHdrName, pszStdSenderHdr) != 0))
			return EOK;

		LPCTSTR	lpszHdrValue=msgEx.GetHdrValue();
		LPTSTR	lpszName=NULL;
		UINT32	ulNameLen=0;

		if (EOK == (exc=DecodeRFC822AddrPair(lpszHdrValue, (LPCTSTR &) lpszName, (UINT32 &) ulNameLen, (LPCTSTR &) lpszAddr, (UINT32 &) ulAddrLen)))
		{
			if (0 == ulAddrLen)
				exc = EBADADDR;
		}
	}

	// if not found real sender use anonymous
	if (exc != EOK)
	{
		static TCHAR szNoAddress[]=_T("no-mail-user@nowhere.inter.net");
		lpszAddr = szNoAddress;
		ulAddrLen = _tcslen(lpszAddr);

		LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, exc,
				  _T("CSMTPMsgRelay::HandleMsgContents() - cannot (0x%08x) resolve sender address using %s"),
				  exc, lpszAddr);
	}

	LPTSTR	lpszAE=(LPTSTR) (lpszAddr+ulAddrLen);
	TCHAR		tch=*lpszAE;
	*lpszAE = _T('\0');
	if (EOK == (exc=RestartSMTPSession(lpszAddr)))
	{
		LPTSTR	lpszV=NULL;
		if (EOK == (exc=strupdatebuf(lpszAddr, lpszV)))
			m_rcpDef.lpszSMTPSndr = lpszV;
	}
	*lpszAE = tch;

	if (exc != EOK)
		return LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, exc,
							_T("CSMTPMsgRelay::HandleMsgContents() - cannot (0x%08x) copy sender address using %s"),
							exc, lpszAddr);

	// write everything accumulated till found sender header(s)
	//
	// Note: we can do this without writing the actual headers since this
	//			method is called BEFORE "Write" - which will take care of
	//			writing the sender header(s) to the socket (now that the sender
	//			is known).
	int	wLen=m_CBSock.Write(m_lpszDetectBuf, (size_t) m_ulCurDetect);
	if (wLen != (int) m_ulCurDetect)
		return ENOTCONNECTION;

	*m_lpszDetectBuf = _T('\0');
	m_ulCurDetect = 0;

	return EOK;
}

/*---------------------------------------------------------------------------*/

int CSMTPMsgRelay::Write (LPCTSTR lpszData, const size_t dLen)
{
	// cannot have data after EOM
	if (m_rme.IsMsgEOM())
		return (-3);

	EXC_TYPE	exc=m_rme.ProcessBuf(lpszData, (UINT32) dLen);
	if (exc != EOK)
		return (-4);

	// if have sender, then must also have active connection
	if (!IsEmptyStr(m_rcpDef.lpszSMTPSndr))
	{
		if (m_rme.IsMsgEOM())
			exc = EEOF;	// dummy - for debug
		return m_CBSock.Write(lpszData, dLen);
	}

	// we must have an active connection before the data ends
	if (m_rme.IsMsgEOM())
		return (-5);

	// until we have sender, we must accumulate data
	LPTSTR	lpszCurPos=(m_lpszDetectBuf + m_ulCurDetect);
	UINT32	ulRemLen=(m_ulDetectBufSize - m_ulCurDetect);
	if (ulRemLen < (UINT32) dLen)
		return (-3);

	_tcsncpy(lpszCurPos, lpszData, dLen);
	lpszCurPos[dLen] = _T('\0');
	m_ulCurDetect += dLen;
	return dLen;
}

/*---------------------------------------------------------------------------*/
