#include <internet/imap4Lib.h>
#include <internet/pop3Lib.h>

/*---------------------------------------------------------------------------*/

EXC_TYPE EncodeIMAP4ExtAcc (const IMAP4ACCOUNTDEF	*pAccDef,
									 LPTSTR						lpszEnc,
									 const UINT32				ulMaxLen)
{
	LPTSTR	lsp=lpszEnc;
	UINT32	ulRemLen=ulMaxLen;
	EXC_TYPE	exc=EOK;

	if ((NULL == pAccDef) || (NULL == lpszEnc) || (0 == ulMaxLen))
		return EPARAM;

	*lsp = _T('\0');

	if ((exc=strlinsstr(&lsp, pAccDef->lpszIMAP4UID, &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsch(&lsp, INET_DOMAIN_SEP, &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsstr(&lsp, pAccDef->lpszIMAP4Host, &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsch(&lsp, _T(':'), &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsnum(&lsp, (UINT32) pAccDef->iIMAP4Port,  &ulRemLen)) != EOK)
		return exc;

	return exc;
}

/*---------------------------------------------------------------------------*/

// Note: does not inform stream about open conn.
EXC_TYPE OpenIMAP4RelayConn (const IMAP4ACCOUNTDEF&	accDef,
									  ISockioInterface&			CBSock,
									  IMsgRelayStream&			rlyStream)
{
	TCHAR		szRsp[MAX_IMAP4_DATA_LEN+2]=_T("");
	EXC_TYPE	exc=imap4Connect(CBSock, accDef.lpszIMAP4Host, accDef.iIMAP4Port, szRsp, MAX_IMAP4_DATA_LEN, accDef.ulRspTimeout);
	if (exc != EOK)
		return rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, exc,
								 _T("OpenIMAP4RelayConn - cannot (0x%08x) connect to %s on %d: %s"),
								 exc, accDef.lpszIMAP4Host, accDef.iIMAP4Port, szRsp);

	if ((exc=rlyStream.HandleProtoState(POP3_OK, (UINT32) &CBSock, accDef.lpszIMAP4Host)) != EOK)
		return exc;

	if ((exc=imap4LoginUserSync(CBSock, NULL, accDef.lpszIMAP4UID, accDef.lpszIMAP4Passwd, szRsp, MAX_IMAP4_DATA_LEN, accDef.ulRspTimeout)) != EOK)
		return rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, exc,
									    _T("OpenIMAP4RelayConn - cannot (0x%08x) authenticate \"%s@%s\": %s"),
									    exc, accDef.lpszIMAP4UID, accDef.lpszIMAP4Passwd, szRsp);

	return EOK;
}

/*---------------------------------------------------------------------------*/

class STREAMRLYCFNARGS {
	public:
		UINT32						m_ulMsgSeqNo;
		UINT32						m_ulMsgUID;
		UINT32						m_ulCurLine;
		const IMAP4ACCOUNTDEF&	m_AccDef;
		const IMAP4RELAYFLAGS&	m_RlyFlags;
		CRFC822MsgExtractor&		m_MsgEx;
		IMsgRelayStream&			m_RlyStream;
		CRFC822HdrData				m_BodyTrail;

		STREAMRLYCFNARGS (const UINT32				ulMsgSeqNo,
								const UINT32				ulMsgUID,
								const IMAP4ACCOUNTDEF&	accDef,
								const IMAP4RELAYFLAGS&	rlyFlags,
								CRFC822MsgExtractor&		msgEx,
								IMsgRelayStream&			rlyStream)
			: m_ulMsgSeqNo(ulMsgSeqNo)
			, m_ulMsgUID(ulMsgUID)
			, m_AccDef(accDef)
			, m_ulCurLine(1)
			, m_RlyFlags(rlyFlags)
			, m_MsgEx(msgEx)
			, m_RlyStream(rlyStream)
			, m_BodyTrail(szXBody)
		{
		}

		~STREAMRLYCFNARGS () { }
};

typedef STREAMRLYCFNARGS *LPSTREAMRLYCFNARGS;

/*---------------------------------------------------------------------------*/

static EXC_TYPE rlyParseCfn (const RFC822MSGEXCASE	meCase,
									  CRFC822MsgExtractor&	msgEx,
									  void						*pArg)
{
	if (NULL == pArg)
		return ECONTEXT;

	STREAMRLYCFNARGS&	sra=*((LPSTREAMRLYCFNARGS) pArg);
	IMsgRelayStream&	rlyStream=sra.m_RlyStream;

	EXC_TYPE	exc=rlyStream.HandleMsgContents(sra.m_ulMsgSeqNo, meCase, msgEx);
	if (exc != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE ProcessBodyTrail (const UINT32				ulMsgUID,
											 const IMAP4ACCOUNTDEF&	accDef,
											 CRFC822HdrData&			bodyTrail,
											 const UINT32				ulCurLine,
											 IMsgRelayStream&			rlyStream,
											 CRFC822MsgExtractor&	msgEx)
{
	for (LPCTSTR	lpszTData=bodyTrail.GetHdrValue(); *lpszTData != _T('\0'); )
	{
		LPCTSTR	lpszTDE=lpszTData;

		for ( ; *lpszTDE != _T('\0'); lpszTDE++)
		{
			if (_T('\n') == *lpszTDE)
			{
				*((LPTSTR) lpszTDE) = _T('\0');
				lpszTDE++;
				break;
			}

			if (_T('\r') == *lpszTDE)
				*((LPTSTR) lpszTDE) = _T('\0');
		}

		EXC_TYPE			exc=EOK;
		const UINT32	ulTDLen=_tcslen(lpszTData);
		BOOLEAN			fAdjusted=FALSE;
		const UINT32	ulNewLen=AdjustParsedLine(msgEx, (LPTSTR) lpszTData, ulTDLen, ulCurLine, fAdjusted);
		if (fAdjusted)
		{
			rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, EBADHEADER,
									_T("ProcessBodyTrail - adjusted msg %lu from \"%s@%s\" at line %lu: \"%s\""),
									ulMsgUID, accDef.lpszIMAP4UID, accDef.lpszIMAP4Host, ulCurLine, lpszTData);

			if ((exc=rlyStream.SignalMsgError(ulMsgUID, EBADHEADER)) != EOK)
				return exc;
		}

		if ((exc=msgEx.ProcessLine(lpszTData, ulTDLen)) != EOK)
		{
			rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_INFO, exc,
									_T("ProcessBodyTrail - cannot (0x%08x) process msg %lu from \"%s@%s\": \"%s\""),
									exc, ulMsgUID, accDef.lpszIMAP4UID, accDef.lpszIMAP4Host, lpszTData);

			if ((exc=rlyStream.SignalMsgError(ulMsgUID, exc)) != EOK)
				return exc;
		}

		lpszTData = lpszTDE;
	}

	bodyTrail.Reset();
	return EOK;
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE rlyFetchCfn (const CIMAP4FetchCfnData&	ftchData,
									  LPCTSTR							lpszFetchRsp,
									  LPCTSTR							lpszSubHdr,		// valid only for complex structures
									  LPCTSTR							lpszKeyword,	// non-NULL for "keyword=value" pair(s)
									  LPVOID								lpModVal)		// actual type depends on modifier
{
	LPSTREAMRLYCFNARGS	pSRA=(LPSTREAMRLYCFNARGS) ftchData.GetCallbackArg();
	if (NULL == pSRA)
		return ESTATE;

	const UINT32				ulMsgUID=pSRA->m_ulMsgUID;
	IMsgRelayStream&			rlyStream=pSRA->m_RlyStream;
	const IMAP4ACCOUNTDEF&	accDef=pSRA->m_AccDef;
	UINT32&						ulCurLine=pSRA->m_ulCurLine;

	// since we are doing a "UID FETCH", the result contains the UID
	if (0 == _tcsicmp(IMAP4_UID, lpszFetchRsp))
	{
		UINT32	ulFetchedUID=(UINT32) lpModVal;
		if (ulFetchedUID != ulMsgUID)
			return rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, ETRANSID,
											 _T("rlyFetchCfn - %s %s (%lu) vs. expected (%lu) mismatch from \"%s@%s\""),
											 szIMAP4FetchCmd, IMAP4_UID, ulFetchedUID, ulMsgUID, accDef.lpszIMAP4UID, accDef.lpszIMAP4Host);

		return EOK;
	}

	const IMAP4RELAYFLAGS&	rlyFlags=pSRA->m_RlyFlags;
	if (0 != rlyFlags.m_fParseMsg)
	{
		LPCTSTR					lpszBodyBuf=(LPCTSTR) lpModVal;
		CRFC822MsgExtractor&	msgEx=pSRA->m_MsgEx;
		CRFC822HdrData&		bodyTrail=pSRA->m_BodyTrail;
		EXC_TYPE					exc=EOK;

		while (*lpszBodyBuf != _T('\0'))
		{
			// if CR missing assume it appears in next buf
			LPCTSTR lpszBLE=_tcschr(lpszBodyBuf, _T('\r'));
			if (NULL == lpszBLE)
				break;

			UINT32	bLen=(lpszBLE - lpszBodyBuf);
			
			// if LF missing assume it appears in next buf
			lpszBLE++;
			if (*lpszBLE != _T('\n'))
				break;
			lpszBLE++;

			LPCTSTR		lpszNextLine=(lpszBodyBuf + bLen);
			const TCHAR	chNextChar=*lpszNextLine;
			*((LPTSTR) lpszNextLine) = _T('\0');
			
			// check if have trailer from previous buffer
			if (bodyTrail.GetHdrLen() != 0)
			{
				if ((exc=bodyTrail.AddData(lpszBodyBuf)) != EOK)
					return rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, exc,
					_T("rlyFetchCfn - cannot (0x%08x) add msg %lu body trailer from \"%s@%s\""),
					exc, ulMsgUID, accDef.lpszIMAP4UID, accDef.lpszIMAP4Host);
				
				if ((exc=ProcessBodyTrail(ulMsgUID, accDef, bodyTrail, ulCurLine, rlyStream, msgEx)) != EOK)
					return exc;
			}
			else	// no previous trailer - process as-is
			{
				BOOLEAN			fAdjusted=FALSE;
				const UINT32	ulNewLen=AdjustParsedLine(msgEx, (LPTSTR) lpszBodyBuf, bLen, ulCurLine, fAdjusted);
				if (fAdjusted)
				{
					rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, EBADHEADER,
											_T("rlyFetchCfn - adjusted msg %lu body line %lu from \"%s@%s\": \"%s\""),
											ulMsgUID, ulCurLine, accDef.lpszIMAP4UID, accDef.lpszIMAP4Host, lpszBodyBuf);

					if ((exc=rlyStream.SignalMsgError(ulMsgUID, EBADHEADER)) != EOK)
						return exc;
				}

				if ((exc=msgEx.ProcessLine(lpszBodyBuf, bLen)) != EOK)
				{
					rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_INFO, exc,
											_T("rlyFetchCfn - cannot (0x%08x) process msg %lu body line from \"%s@%s\": \"%s\""),
											exc, ulMsgUID, accDef.lpszIMAP4UID, accDef.lpszIMAP4Host, lpszBodyBuf);

					if ((exc=rlyStream.SignalMsgError(ulMsgUID, exc)) != EOK)
						return exc;
				}
			}
			
			ulCurLine++;
			*((LPTSTR) lpszNextLine) = chNextChar;
			lpszBodyBuf = lpszBLE;
		}
		
		// check if need to preserve some trailer for next iteration
		if (*lpszBodyBuf != _T('\0'))
		{
			if ((exc=bodyTrail.SetData(szXBody, lpszBodyBuf)) != EOK)
				return rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, exc,
											 _T("rlyFetchCfn - cannot (0x%08x) save msg %lu body trail from \"%s@%s\": \"%s\""),
											 exc, ulMsgUID, accDef.lpszIMAP4UID, accDef.lpszIMAP4Host, lpszBodyBuf);
		}
	}

	LPCTSTR			lpszWriteBuf=(LPCTSTR) lpModVal;
	const size_t	sbLen=_tcslen(lpszWriteBuf);
	int				wLen=rlyStream.Write(lpszWriteBuf, sbLen);
	if ((size_t) wLen != sbLen)
		return rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, EOVERLAY,
										 _T("rlyFetchCfn - stream conn. lost (%d) while send msg (%lu) data from \"%s@%s\""),
										 wLen, ulMsgUID, accDef.lpszIMAP4UID, accDef.lpszIMAP4Host);

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE StreamRelayIMAP4Msg (const IMAP4ACCOUNTDEF&	accDef,
										ISockioInterface&			CBSock,
										const UINT32				ulMsgID,
										const BOOLEAN				fIsUID,
										const IMAP4RELAYFLAGS&	rlyFlags,
										IMsgRelayStream&			rlyStream)
{
	IMAP4FASTMSGINFO	fmInfo;
	EXC_TYPE				exc=imap4FetchMsgFastInfo(CBSock, NULL, fIsUID, ulMsgID, fmInfo, accDef.ulRspTimeout);
	if (exc != EOK)
		return exc;

	const UINT32	ulMsgUID=fmInfo.ulMsgUID;
	const UINT32	ulMsgSeqNo=fmInfo.ulMsgSeqNo;
	if (rlyFlags.m_fCheckUID != 0)
	{
		TCHAR	szUID[MAX_DWORD_DISPLAY_LENGTH+2]=_T("");
		dword_to_argument(ulMsgUID, szUID);

		if ((exc=rlyStream.HandleProtoState(szPOP3UidlCmd, ulMsgSeqNo, szUID)) != EOK)
		{
			if (EWARNINGEXIT == exc)
			{
				EXC_TYPE	err=imap4DeleteMsgSync(CBSock, NULL, ulMsgUID, TRUE, TRUE, NULL, NULL, accDef.ulRspTimeout);
				if (err != EOK)
					return rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, err,
												    _T("StreamRelayIMAP4Msg - cannot (0x%08x) mark %lu for deletion from \"%s@%s\""),
												    err, ulMsgUID, accDef.lpszIMAP4UID, accDef.lpszIMAP4Host);
			}

			return exc;
		}
	}

	if (rlyFlags.m_fCheckSize != 0)
	{
		TCHAR	szMsgSize[MAX_DWORD_DISPLAY_LENGTH+2]=_T("");
		dword_to_argument((DWORD) fmInfo.ulMsgSize, szMsgSize);

		if ((exc=rlyStream.HandleProtoState(szPOP3ListCmd, ulMsgSeqNo, szMsgSize)) != EOK)
			return exc;
	}

	if ((exc=rlyStream.HandleProtoState(szPOP3TopCmd, ulMsgSeqNo, _T(""))) != EOK)
		return exc;

	CRFC822MsgExtractor	msgEx;
	STREAMRLYCFNARGS		sra(ulMsgSeqNo, ulMsgUID, accDef, rlyFlags, msgEx, rlyStream);
	if (rlyFlags.m_fParseMsg != 0)
	{
		if ((exc=msgEx.SetDecodeParams(rlyParseCfn, (LPVOID) &sra)) != EOK)
			return rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, exc,
										    _T("StreamRelayIMAP4Msg - cannot (0x%08x) init msg %lu parser for \"%s@%s\""),
										    exc, ulMsgUID, accDef.lpszIMAP4UID, accDef.lpszIMAP4Host);

	}

	if ((exc=imap4FetchMsgBodySync(CBSock, NULL, TRUE, ulMsgUID, rlyFetchCfn, (LPVOID) &sra, accDef.ulRspTimeout)) != EOK)
		return rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, exc,
									    _T("StreamRelayIMAP4Msg - cannot (0x%08x) %s %lu %s from \"%s@%s\""),
									    exc, szIMAP4FetchCmd, ulMsgUID, IMAP4_BODY, accDef.lpszIMAP4UID, accDef.lpszIMAP4Host);

	if (rlyFlags.m_fParseMsg != 0)
	{
		// see if have any body "leftovers"
		CRFC822HdrData&	bodyTrail=sra.m_BodyTrail;

		if (bodyTrail.GetHdrLen() != 0)
		{
			if ((exc=ProcessBodyTrail(ulMsgUID, accDef, bodyTrail, sra.m_ulCurLine, rlyStream, msgEx)) != EOK)
				return exc;
		}
	}

	// generate fake end of message
	int	wLen=rlyStream.Write(_T(".\r\n"));
	if (wLen != 3)
		return rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, ENOTCONNECTION,
									    _T("StreamRelayIMAP4Msg - conn. lost (%d) while send msg %lu EOM from \"%s@%s\""),
									    wLen, ulMsgUID, accDef.lpszIMAP4UID, accDef.lpszIMAP4Host);
		
	if ((exc=rlyStream.HandleProtoState(szPOP3RetrCmd, ulMsgSeqNo, _T(""))) != EOK)
		return exc;

	if (rlyFlags.m_fQueryDel != 0)
	{
		if ((exc=rlyStream.HandleProtoState(szPOP3DeleCmd, ulMsgSeqNo, _T(""))) != EOK)
			return exc;

		if ((exc=imap4DeleteMsgSync(CBSock, NULL, ulMsgUID, TRUE, TRUE, NULL, NULL, accDef.ulRspTimeout)) != EOK)
			return rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, exc,
										    _T("StreamRelayIMAP4Msg - cannot (0x%08x) mark %lu for deletion from \"%s@%s\""),
										    exc, ulMsgUID, accDef.lpszIMAP4UID, accDef.lpszIMAP4Host);
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE StreamRelayIMAP4Conn (const IMAP4ACCOUNTDEF&	accDef,
										 ISockioInterface&		CBSock,
										 LPCTSTR						lpszStreamFolder,
										 const IMAP4RELAYFLAGS&	rlyFlags,
										 IMsgRelayStream&			rlyStream)
{
	if (IsEmptyStr(lpszStreamFolder))
		return rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, EPATH,
									    _T("StreamRelayIMAP4Conn - NULL/empty (0x%08x) relay source folder"),
										 (UINT32) lpszStreamFolder);

	IMAP4SELECTSTRUCT	selStruct;
	const UINT32&		ulMsgsNum=selStruct.ulExistNum;
	EXC_TYPE				exc=imap4SelectFolderSync(CBSock, NULL, lpszStreamFolder, selStruct, accDef.ulRspTimeout);
	if (exc != EOK)
		return rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, exc,
									    _T("StreamRelayIMAP4Conn - cannot (0x%08x) %s source folder \"%s\" for \"%s@%s\""),
										 exc, szIMAP4SelectCmd, lpszStreamFolder, accDef.lpszIMAP4UID, accDef.lpszIMAP4Passwd);

	TCHAR	szStatRsp[(2 * MAX_DWORD_DISPLAY_LENGTH)+NAME_LENGTH+2]=_T(""), *lsp=szStatRsp;
	lsp = strlcat(lsp, POP3_OK);
	lsp = strladdch(lsp, _T(' '));
	lsp += dword_to_argument((DWORD) ulMsgsNum, lsp);
	lsp = strladdch(lsp, _T(' '));
	lsp += dword_to_argument((DWORD) ulMsgsNum, lsp);

	if ((exc=rlyStream.HandleProtoState(szPOP3StatCmd, 0, szStatRsp)) != EOK)
		return exc;

	for (UINT32 ulRNum=1; ulRNum <= ulMsgsNum; ulRNum++)
	{
		UINT32	ulEffNum=((rlyFlags.m_fRetrReverse != 0) ? ((ulMsgsNum - ulRNum) + 1) : ulRNum);

		if ((exc=StreamRelayIMAP4Msg(accDef, CBSock, ulEffNum, FALSE, rlyFlags, rlyStream)) != EOK)
		{
			if (ENOTCONNECTION == exc)
				return exc;
		}
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

// Note: implicitly streams from INBOX
EXC_TYPE StreamRelayIMAP4Account (const IMAP4ACCOUNTDEF&	accDef,
											 const IMAP4RELAYFLAGS&	rlyFlags,
											 IMsgRelayStream&			rlyStream)
{
	CBuffSock	CBSock;
	EXC_TYPE		exc=OpenIMAP4RelayConn(accDef, CBSock, rlyStream);
	if (exc != EOK)
		return exc;

	TCHAR	szExtAcc[MAX_IMAP4EXTACC_ENCLEN+2]=_T("");
	if ((exc=EncodeIMAP4ExtAcc(&accDef, szExtAcc, MAX_IMAP4EXTACC_ENCLEN)) != EOK)
		return rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, exc,
									    _T("StreamRelayIMAP4Account - cannot (0x%08x) encode \"%s@%s\""),
									    exc, accDef.lpszIMAP4UID, accDef.lpszIMAP4Host);

	if ((exc=rlyStream.HandleProtoState(szPOP3UserCmd, (UINT32) exc, szExtAcc)) != EOK)
		return exc;

	if (EOK == (exc=StreamRelayIMAP4Conn(accDef, CBSock, IMAP4_INBOX, rlyFlags, rlyStream)))
	{
		EXC_TYPE	xpgErr=imap4ExpungeSync(CBSock, NULL, NULL, NULL, accDef.ulRspTimeout);
		if (xpgErr != EOK)
			rlyStream.LogMsgf(IMsgRelayStream::IMSGRLY_PROTO_WARNING, exc,
								   _T("StreamRelayIMAP4Account - cannot (0x%08x) %s \"%s@%s\""),
								   xpgErr, szIMAP4XpngCmd, accDef.lpszIMAP4UID, accDef.lpszIMAP4Host);
	}

	EXC_TYPE	err=imap4LogoutUserSync(CBSock, NULL, accDef.ulRspTimeout);

	err = CBSock.Close();	// just making sure...
	TCHAR	szDummy[MAX_DWORD_DISPLAY_LENGTH+2]=_T("");
	int	rLen=CBSock.ReadCmd(szDummy, MAX_DWORD_DISPLAY_LENGTH, accDef.ulRspTimeout);

	err = rlyStream.HandleProtoState(szPOP3QuitCmd, (UINT32) exc, szExtAcc);
	return exc;
}

/*---------------------------------------------------------------------------*/
