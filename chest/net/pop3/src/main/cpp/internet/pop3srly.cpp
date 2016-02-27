#include <internet/pop3Lib.h>

/*---------------------------------------------------------------------------*/

EXC_TYPE EncodePOP3ExtAcc (const POP3ACCOUNTDEF	*pAccDef,
									LPTSTR					lpszEnc,
									const UINT32			ulMaxLen)
{
	LPTSTR	lsp=lpszEnc;
	UINT32	ulRemLen=ulMaxLen;
	EXC_TYPE	exc=EOK;

	if ((NULL == pAccDef) || (NULL == lpszEnc) || (0 == ulMaxLen))
		return EPARAM;

	*lsp = _T('\0');

	if ((exc=strlinsstr(&lsp, pAccDef->lpszPOP3UID, &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsch(&lsp, INET_DOMAIN_SEP, &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsstr(&lsp, pAccDef->lpszPOP3Host, &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsch(&lsp, _T(':'), &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsnum(&lsp, (UINT32) pAccDef->iPOP3Port,  &ulRemLen)) != EOK)
		return exc;

	return exc;
}

/*---------------------------------------------------------------------------*/

// Note: does not inform stream about open conn.
EXC_TYPE OpenPOP3RelayConn (const POP3ACCOUNTDEF&	accDef,
									 ISockioInterface&		CBSock,
									 IMsgRelayStream&			rlyStream)
{
	TCHAR		szRsp[POP3_MAX_RSP_LEN+2]=_T("");
	EXC_TYPE	exc=pop3_clnt_connect(CBSock, accDef.lpszPOP3Host, accDef.iPOP3Port, szRsp, POP3_MAX_RSP_LEN, accDef.ulRspTimeout);
	if (exc != EOK)
		return rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, exc,
									    _T("OpenPOP3RelayConn - cannot (0x%08x) connect to %s on %d: %s"),
									    exc, accDef.lpszPOP3Host, accDef.iPOP3Port, szRsp);

	if ((exc=rlyStream.HandleProtoState(POP3_OK, (UINT32) &CBSock, accDef.lpszPOP3Host)) != EOK)
		return exc;

	if ((exc=pop3_clnt_auth(CBSock, accDef.lpszPOP3UID, accDef.lpszPOP3Passwd, szRsp, POP3_MAX_RSP_LEN, accDef.ulRspTimeout)) != EOK)
		return rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, exc,
									    _T("OpenPOP3RelayConn - cannot (0x%08x) authenticate \"%s@%s\": %s"),
									    exc, accDef.lpszPOP3UID, accDef.lpszPOP3Host, szRsp);

	return EOK;
}

/*---------------------------------------------------------------------------*/

typedef struct {
	UINT32					ulMsgNum;
	const POP3ACCOUNTDEF	*pAccDef;
	const POP3RELAYFLAGS	*pRlyFlags;
	IMsgRelayStream		*pRlyStream;
} STREAMRLYCFNARGS, *LPSTREAMRLYCFNARGS;

static EXC_TYPE rlyParseCfn (const RFC822MSGEXCASE	meCase,
									  CRFC822MsgExtractor&	msgEx,
									  void						*pArg)
{
	if (NULL == pArg)
		return ECONTEXT;

	STREAMRLYCFNARGS&	sra=*((LPSTREAMRLYCFNARGS) pArg);
	if (NULL == sra.pRlyStream)
		return EIOHARD;

	IMsgRelayStream&	rlyStream=*sra.pRlyStream;
	if ((0 == sra.ulMsgNum) || (NULL == sra.pAccDef) || (NULL == sra.pRlyFlags))
		return rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, ECONTEXT,
									    _T("rlyParseCfn - bad cfn params: %lu,0x%08x,0x%08x"),
									    sra.ulMsgNum, sra.pAccDef, sra.pRlyFlags);

	return rlyStream.HandleMsgContents(sra.ulMsgNum, meCase, msgEx);
}

/*---------------------------------------------------------------------------*/

typedef struct {
	LPSTREAMRLYCFNARGS	pRlyArgs;
	CRFC822MsgExtractor	*pMsgEx;	// if NULL then no parsing
	CRFC822MsgEOM			*pRME;
	EXC_TYPE					exc;
	UINT32					ulCurLine;
	BOOLEAN					fHaveEOM;
} STREAMRLYRETRARGS, *LPSTREAMRLYRETRARGS;

static BOOLEAN rlyRetrMsgCfn (const UINT32	msgNum,		/* requested ID */
										const SINT32	/* linesNum */,	/* requested num */
										const char		iBuf[],		/* read buffer */
										const UINT32	bufLen,			/* valid datalen */
										void				*pArg)		/* caller arg */
{
	if (NULL == pArg)
		return FALSE;

	STREAMRLYRETRARGS&	rtra=*((LPSTREAMRLYRETRARGS) pArg);
	EXC_TYPE&				exc=rtra.exc;
	if (NULL == rtra.pRlyArgs)
	{
		exc = ECONTEXT;
		return FALSE;
	}
	STREAMRLYCFNARGS&	sra=*rtra.pRlyArgs;
	if (NULL == sra.pRlyStream)
	{
		exc = EIOHARD;
		return FALSE;
	}
	IMsgRelayStream&	rlyStream=*sra.pRlyStream;

	if ((0 == sra.ulMsgNum) || (NULL == sra.pAccDef) || (NULL == sra.pRlyFlags))
	{
		exc = rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, ECONTEXT,
									   _T("rlyRetrMsgCfn - bad cfn params: %lu,0x%08x,0x%08x"),
									   sra.ulMsgNum, sra.pAccDef, sra.pRlyFlags);
		return FALSE;
	}
	const POP3ACCOUNTDEF&	accDef=*sra.pAccDef;
	const POP3RELAYFLAGS&	rlyFlags=*sra.pRlyFlags;

	// after EOM there can be no further data
	if (rtra.fHaveEOM)
	{
		exc = rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, EEOF,
										_T("rlyRetrMsgCfn - data past EOM of %lu for \"%s@%s\": \"%s\""),
										msgNum, accDef.lpszPOP3UID, accDef.lpszPOP3Host, iBuf);
		return FALSE;
	}

	if (NULL == rtra.pRME)
	{
		exc = rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, ECONTEXT,
										_T("rlyRetrMsgCfn - no EOM parser for %lu from \"%s@%s\""),
										msgNum, accDef.lpszPOP3UID, accDef.lpszPOP3Host);
		return FALSE;
	}
	CRFC822MsgEOM&	rme=*rtra.pRME;

	UINT32	iLen=bufLen;
	if ((rtra.pMsgEx != NULL) && (rlyFlags.m_fParseMsg != 0))
	{
		CRFC822MsgExtractor&	msgEx=*rtra.pMsgEx;
		BOOLEAN					fAdjusted=FALSE;
		const UINT32			ulNewLen=AdjustParsedLine(msgEx, (LPTSTR) iBuf, iLen, rtra.ulCurLine, fAdjusted);
		if (fAdjusted)
		{
			rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, EBADHEADER,
									_T("rlyRetrMsgCfn - adjusted msg %lu from \"%s@%s\" at line %lu: \"%s\""),
									msgNum, accDef.lpszPOP3UID, accDef.lpszPOP3Host, rtra.ulCurLine, iBuf);

			if ((exc=rlyStream.SignalMsgError(msgNum, EBADHEADER)) != EOK)
				return FALSE;
		}

		if ((exc=msgEx.ProcessLine(iBuf, iLen)) != EOK)
		{
			rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_INFO, exc,
									_T("rlyRetrMsgCfn - cannot (0x%08x) process msg %lu at \"%s@%s\": \"%s\""),
									exc, msgNum, accDef.lpszPOP3UID, accDef.lpszPOP3Host, iBuf);

			if ((exc=rlyStream.SignalMsgError(msgNum, exc)) != EOK)
				return FALSE;
		}

		// for headers we may need to readjust the length since empty headers are truncated
		const RFC822MSGSTATE	xState=msgEx.GetMsgState();
		if ((xState < RFC822_BODY_MSGSTATE) || (RFC822_ATTHDRS_MSGSTATE == xState))
			iLen = _tcslen(iBuf);
	}

	// add stripped CRLF
	LPTSTR	lpszBE=(LPTSTR) (iBuf + iLen);
	lpszBE[0] = _T('\r');
	lpszBE[1] = _T('\n');
	lpszBE[2] = _T('\0');

	int		xLen=(int) (iLen+2), wLen=rlyStream.Write(iBuf, (size_t) xLen);
	EXC_TYPE	texc=rme.ProcessBuf(iBuf, (UINT32) xLen);
	*lpszBE = _T('\0');	// restore original line

	if (wLen != xLen)
	{
		exc = rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, EIOJOB,
										_T("rlyRetrMsgCfn - cannot (%d <> %d) write msg %lu at \"%s@%s\": \"%s\""),
										wLen, xLen, msgNum, accDef.lpszPOP3UID, accDef.lpszPOP3Host, iBuf);
		return FALSE;
	}

	if (EOK == texc)
	{
		if (rme.IsMsgEOM())
			rtra.fHaveEOM = TRUE;
	}
	else
	{
		rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, ECONTEXT,
								_T("rlyRetrMsgCfn - cannot (0x%08x) hunt for EOM in %lu from \"%s@%s\": \"%s\""),
								texc, msgNum, accDef.lpszPOP3UID, accDef.lpszPOP3Host, iBuf);
	}

	rtra.ulCurLine++;
	return TRUE;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE StreamRelayPOP3Msg (const POP3ACCOUNTDEF&	accDef,
									  ISockioInterface&		CBSock,
									  const UINT32				ulMsgNum,
									  const POP3RELAYFLAGS&	rlyFlags,
									  IMsgRelayStream&		rlyStream)
{
	EXC_TYPE	exc=EOK;

	if (rlyFlags.m_fCheckUIDL != 0)
	{
		TCHAR	szUIDL[MAX_POP3_UIDL_LEN+2]=_T("");

		if ((exc=pop3_clnt_msg_uidl(CBSock, ulMsgNum, szUIDL, (MAX_POP3_UIDL_LEN+1), accDef.ulRspTimeout)) != EOK)
			return rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, exc,
										   _T("StreamRelayPOP3Msg - cannot (0x%08x) get %s of %lu at \"%s@%s\""),
										   exc, szPOP3UidlCmd, ulMsgNum, accDef.lpszPOP3UID, accDef.lpszPOP3Host);

		if ((exc=rlyStream.HandleProtoState(szPOP3UidlCmd, ulMsgNum, szUIDL)) != EOK)
		{
			if (EWARNINGEXIT == exc)
			{
				EXC_TYPE	err=pop3_clnt_dele(CBSock, ulMsgNum, accDef.ulRspTimeout);
				if (err != EOK)
					return rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, err,
												    _T("StreamRelayPOP3Msg - cannot (0x%08x) %s %lu from \"%s@%s\""),
												    err, szPOP3DeleCmd, ulMsgNum, accDef.lpszPOP3UID, accDef.lpszPOP3Host);
			}

			return exc;
		}
	}

	if (rlyFlags.m_fCheckSize != 0)
	{
		UINT32	ulMsgSize=0;

		if ((exc=pop3_clnt_msg_list(CBSock, ulMsgNum, ulMsgSize, accDef.ulRspTimeout)) != EOK)
			return rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, exc,
										   _T("StreamRelayPOP3Msg - cannot (0x%08x) %s %lu at \"%s@%s\""),
										   exc, szPOP3ListCmd, ulMsgNum, accDef.lpszPOP3UID, accDef.lpszPOP3Host);

		TCHAR	szMsgSize[MAX_DWORD_DISPLAY_LENGTH+2]=_T("");
		dword_to_argument((DWORD) ulMsgSize, szMsgSize);
		if ((exc=rlyStream.HandleProtoState(szPOP3ListCmd, ulMsgNum, szMsgSize)) != EOK)
			return exc;
	}

	if ((exc=rlyStream.HandleProtoState(szPOP3TopCmd, ulMsgNum, _T(""))) != EOK)
		return exc;

	CRFC822MsgExtractor	msgEx;
	STREAMRLYCFNARGS		sra={ ulMsgNum, &accDef, &rlyFlags, &rlyStream	};
	CRFC822MsgEOM			rme;
	STREAMRLYRETRARGS		rtra={ &sra, NULL, &rme, EOK, 1, FALSE };
	if (rlyFlags.m_fParseMsg != 0)
	{
		if ((exc=msgEx.SetDecodeParams(rlyParseCfn, (LPVOID) &sra)) != EOK)
			return rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, exc,
										    _T("StreamRelayPOP3Msg - cannot (0x%08x) init msg %lu parser for \"%s@%s\""),
										    exc, ulMsgNum, accDef.lpszPOP3UID, accDef.lpszPOP3Host);

		rtra.pMsgEx = &msgEx;
	}

	exc = pop3_clnt_retr(CBSock, ulMsgNum, POP3_ALL_LINES, rlyRetrMsgCfn, (LPVOID) &rtra, accDef.ulRspTimeout);
	if (exc != EOK)
		return rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, exc,
									    _T("StreamRelayPOP3Msg - cannot (0x%08x) %s %lu from \"%s@%s\""),
									    exc, szPOP3RetrCmd, ulMsgNum, accDef.lpszPOP3UID, accDef.lpszPOP3Host);
	if ((exc=rtra.exc) != EOK)
		return exc;

	if ((exc=rlyStream.HandleProtoState(szPOP3RetrCmd, ulMsgNum, _T(""))) != EOK)
		return exc;

	if (rlyFlags.m_fQueryDel != 0)
	{
		if ((exc=rlyStream.HandleProtoState(szPOP3DeleCmd, ulMsgNum, _T(""))) != EOK)
			return exc;

		if ((exc=pop3_clnt_dele(CBSock, ulMsgNum, accDef.ulRspTimeout)) != EOK)
			return rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, exc,
										    _T("StreamRelayPOP3Msg - cannot (0x%08x) %s %lu from \"%s@%s\""),
										    exc, szPOP3DeleCmd, ulMsgNum, accDef.lpszPOP3UID, accDef.lpszPOP3Host);
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE StreamRelayPOP3Conn (const POP3ACCOUNTDEF&	accDef,
										ISockioInterface&			CBSock,
										const POP3RELAYFLAGS&	rlyFlags,
										IMsgRelayStream&			rlyStream)
{
	UINT32	ulMsgsNum=0, ulMboxSize=0;
	EXC_TYPE	exc=pop3_clnt_stat(CBSock, &ulMsgsNum, &ulMboxSize, accDef.ulRspTimeout);
	if (exc != EOK)
		return rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, exc,
									    _T("StreamRelayPOP3Conn - cannot (0x%08x) get %s of \"%s@%s\""),
									    exc, szPOP3StatCmd, accDef.lpszPOP3UID, accDef.lpszPOP3Host);

	TCHAR	szStatRsp[(2 * MAX_DWORD_DISPLAY_LENGTH)+NAME_LENGTH+2]=_T(""), *lsp=szStatRsp;
	lsp = strlcat(lsp, POP3_OK);
	lsp = strladdch(lsp, _T(' '));
	lsp += dword_to_argument((DWORD) ulMsgsNum, lsp);
	lsp = strladdch(lsp, _T(' '));
	lsp += dword_to_argument((DWORD) ulMboxSize, lsp);

	if ((exc=rlyStream.HandleProtoState(szPOP3StatCmd, 0, szStatRsp)) != EOK)
		return exc;

	for (UINT32 ulRNum=1; ulRNum <= ulMsgsNum; ulRNum++)
	{
		UINT32	ulEffNum=((rlyFlags.m_fRetrReverse != 0) ? ((ulMsgsNum - ulRNum) + 1) : ulRNum);

		if ((exc=StreamRelayPOP3Msg(accDef, CBSock, ulEffNum, rlyFlags, rlyStream)) != EOK)
		{
			if (ENOTCONNECTION == exc)
				return exc;
		}
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE StreamRelayPOP3Account (const POP3ACCOUNTDEF&	accDef,
											const POP3RELAYFLAGS&	rlyFlags,
											IMsgRelayStream&			rlyStream)
{
	CBuffSock	POP3Sock;
	EXC_TYPE		exc=OpenPOP3RelayConn(accDef, POP3Sock, rlyStream);
	if (exc != EOK)
		return exc;

	TCHAR	szExtAcc[MAX_POP3EXTACC_ENCLEN+2]=_T("");
	if ((exc=EncodePOP3ExtAcc(&accDef, szExtAcc, MAX_POP3EXTACC_ENCLEN)) != EOK)
		return rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, exc,
									    _T("StreamRelayPOP3Account - cannot (0x%08x) encode \"%s@%s\""),
									    exc, accDef.lpszPOP3UID, accDef.lpszPOP3Host);

	if ((exc=rlyStream.HandleProtoState(szPOP3UserCmd, (UINT32) exc, szExtAcc)) != EOK)
		return exc;

	exc = StreamRelayPOP3Conn(accDef, POP3Sock, rlyFlags, rlyStream);

	EXC_TYPE	err=pop3_clnt_quit(POP3Sock, accDef.ulRspTimeout);
	err = POP3Sock.Close();	// just making sure...
	TCHAR	szDummy[MAX_DWORD_DISPLAY_LENGTH+2]=_T("");
	int	rLen=POP3Sock.ReadCmd(szDummy, MAX_DWORD_DISPLAY_LENGTH, accDef.ulRspTimeout);

	err = rlyStream.HandleProtoState(szPOP3QuitCmd, (UINT32) exc, szExtAcc);
	return exc;
}

/*---------------------------------------------------------------------------*/
