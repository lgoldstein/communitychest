#include <internet/imap4Lib.h>
#include <futils/general.h>

//////////////////////////////////////////////////////////////////////////////

EXC_TYPE WaitForIMAP4Continuation (ISockioInterface&	SBSock,
											  LPTSTR					lpszRspBuf,
											  const UINT32			ulMaxLen,
											  IMAP4_RSPHNDL_CFN	lpfnHcfn,	// may be NULL
											  LPVOID					pArg,
											  const UINT32			ulRspTimeout)
{
	if ((NULL == lpszRspBuf) || (ulMaxLen < (MAX_IMAP4_TAG_LEN+MAX_IMAP4_OPCODE_LEN+2)))
		return EBADBUFF;

	for (UINT32	ulRdx=0; ; ulRdx++)
	{
		BOOLEAN	fStripCRLF=FALSE;
		int		rLen=SBSock.ReadCmd(lpszRspBuf, ulMaxLen, (SINT32) ulRspTimeout, &fStripCRLF);
		if (rLen <= 0)
			return ENOTCONNECTION;
		if (!fStripCRLF)
			return EOVERFLOW;

		if (IMAP4_LITPLUS_CHAR == *lpszRspBuf)
			break;

		// skip any untagged responses
		if (IMAP4_UNTAGGED_RSP != *lpszRspBuf)
			return ETRANSID;

		if (lpfnHcfn != NULL)
		{
			EXC_TYPE	exc=(*lpfnHcfn)(SBSock, lpszRspBuf, (UINT32) rLen, ulMaxLen, pArg);
			if (exc != EOK)
				return exc;
		}
	}

	return EOK;
}

// waits for the '+' sign (skipping any untagged responses meanwhile via the callback)
EXC_TYPE WaitForIMAP4Continuation (ISockioInterface&	SBSock,
											  IMAP4_RSPHNDL_CFN	lpfnHcfn,	// may be NULL
											  LPVOID					pArg,
											  const UINT32			ulRspTimeout)
{
	TCHAR		szRsp[MAX_IMAP4_DATA_LEN+2]=_T("");

	return WaitForIMAP4Continuation(SBSock, szRsp, MAX_IMAP4_DATA_LEN, lpfnHcfn, pArg, ulRspTimeout);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE SendIMAP4FolderCmd (ISockioInterface&	SBSock,
									  LPCTSTR				lpszITag,
									  LPCTSTR				lpszCmd,
									  const BOOLEAN		fIsUID,
									  LPCTSTR				lpszCmdPrefix,	// may be NULL
									  LPCTSTR				lpszFolder,
									  IMAP4_RSPHNDL_CFN	lpfnHcfn,
									  LPVOID					pArg,
									  LPTSTR					lpszRspBuf,
									  const UINT32			ulMaxRspLen,
									  const UINT32			ulRspTimeout)
{
	if ((lpszRspBuf != NULL) && (ulMaxRspLen > 0))
		*lpszRspBuf = _T('\0');

	if (IsEmptyStr(lpszCmd))
		return EBADMSG;
	if (IsEmptyStr(lpszFolder))
		return EPATH;
	if (NULL == lpfnHcfn)
		return EBADADDR;

	TCHAR				szCmd[MAX_IMAP4_DATA_LEN+MAX_IMAP4_TAG_LEN+2]=_T("");
	CStrlBuilder	strb(szCmd, MAX_IMAP4_DATA_LEN+MAX_IMAP4_TAG_LEN);
	LPCTSTR			lpszTag=lpszITag;
	TCHAR				szTag[MAX_IMAP4_TAG_LEN+2]=_T("");
	EXC_TYPE			exc=InitIMAP4Cmd(lpszTag, lpszCmd, fIsUID, szTag, MAX_IMAP4_TAG_LEN, strb);
	if (exc != EOK)
		return exc;

	if (!IsEmptyStr(lpszCmdPrefix))
	{
		if ((exc=strb.AddChar(_T(' '))) != EOK)
			return exc;
		if ((exc=strb.AddStr(lpszCmdPrefix)) != EOK)
			return exc;
	}

	int	wLen=(-1);
	if ((exc=SendIMAP4Folder(SBSock, szCmd, lpszFolder, ulRspTimeout)) != EOK)
	{
		if (exc != ELITERAL)
			return exc;

		strb.Reset();

		if ((wLen=SBSock.Writeln()) != 2)
			return ENOTCONNECTION;
	}
	else	// no special characters
	{
		if ((exc=strb.AddChar(_T(' '))) != EOK)
			return exc;

		if ((exc=AddIMAP4Folder(strb, lpszFolder)) != EOK)
			return exc;

		if ((exc=strb.AddCRLF()) != EOK)
			return exc;

		if ((wLen=SBSock.Write(szCmd)) <= 0)
			return ENOTCONNECTION;
	}

	return imap4GetRspSync(SBSock, lpszTag, lpszRspBuf, ulMaxRspLen, ulRspTimeout, lpfnHcfn, pArg);
}

EXC_TYPE SendIMAP4FolderCmd (ISockioInterface&	SBSock,
									  LPCTSTR				lpszITag,
									  LPCTSTR				lpszCmd,
									  const BOOLEAN		fIsUID,
									  LPCTSTR				lpszCmdPrefix,	// may be NULL
									  LPCTSTR				lpszFolder,
									  IMAP4_RSPHNDL_CFN	lpfnHcfn,
									  LPVOID					pArg,
									  const UINT32			ulRspTimeout)
{
	TCHAR	szRsp[MAX_IMAP4_DATA_LEN+2]=_T("");
	return SendIMAP4FolderCmd(SBSock, lpszITag, lpszCmd, fIsUID, lpszCmdPrefix, lpszFolder, lpfnHcfn, pArg, szRsp, MAX_IMAP4_DATA_LEN, ulRspTimeout);
}

/*---------------------------------------------------------------------------*/

// reads and parses response until tagged response found
//
// Note: does not handle literals, responses extending beyond the buffer limit
//			or continuations ('+')
EXC_TYPE imap4GetRspSync (ISockioInterface&	SBSock,
								  LPCTSTR				lpszTag,
								  LPTSTR					lpszRspBuf,
								  const UINT32			ulMaxRspLen,
								  const UINT32			ulRspTimeout,
								  IMAP4_RSPHNDL_CFN	lpfnHcfn,	// may be NULL
								  LPVOID					pArg)
{
	if (IsEmptyStr(lpszTag) || (NULL == lpszRspBuf) || (0 == ulMaxRspLen) || (NULL == lpfnHcfn))
		return EPARAM;

	for (UINT32 ulLdx=1; ; ulLdx++)
	{
		BOOLEAN	fStripCRLF=FALSE;
		int		rLen=SBSock.ReadCmd(lpszRspBuf, ulMaxRspLen, ulRspTimeout, &fStripCRLF);
		if (rLen <= 0)
			return ENOTCONNECTION;

		// do not handle response unless contained entirely in response buffer
		if (!fStripCRLF)
			return EOVERFLOW;

		// make sure not a literal count response by looking backward from end of response
		// for first non-space character and make sure it is not an octet count end delimiter
		for (LPCTSTR lpszRE=strlast(lpszRspBuf); lpszRE > lpszRspBuf; lpszRE--)
			if ((_T('\0') != *lpszRE) && (!_istspace(*lpszRE)))
			{
				if (IMAP4_OCTCNT_EDELIM == *lpszRE)
					return EILSEQ;
				break;
			}

		TCHAR		szTag[MAX_IMAP4_TAG_LEN+2]=_T(""), szOp[MAX_IMAP4_OPCODE_LEN+2]=_T("");
		LPCTSTR	lpszArgs=NULL;
		EXC_TYPE	exc=imap4ExtractRsp(lpszRspBuf, szTag, MAX_IMAP4_TAG_LEN, szOp, MAX_IMAP4_OPCODE_LEN, &lpszArgs);
		if (EOK == exc)
		{
			// if tag matches then translate operation code (OK/BAD/NO)
			if (_tcsicmp(lpszTag, szTag) == 0)
				return imap4XlateRspCode(szOp);

			// tag does not match the expected one
			return EILLOGICALRENAME;
		}
		else if (EDATACHAIN != exc)
			return exc;

		if ((exc=(*lpfnHcfn)(SBSock, lpszRspBuf, (UINT32) rLen, ulMaxRspLen, pArg)) != EOK)
		{
			if (EABORTEXIT == exc)
				break;
			return exc;
		}
	}

	// this point should not be reached
	return EFATALEXIT;
}

//////////////////////////////////////////////////////////////////////////////

// Note: automatically adds tag
EXC_TYPE imap4ExecCmdVSync (ISockioInterface&	SBSock,
									 LPCTSTR					lpszITag,	// NULL == auto generate
									 LPTSTR					lpszRspBuf,
									 const UINT32			ulMaxRspLen,
									 const UINT32			ulRspTimeout,
									 IMAP4_RSPHNDL_CFN	lpfnHcfn,
									 LPVOID					pArg,
									 LPCTSTR					lpszOp,
									 const BOOLEAN			fIsUID,
									 LPCTSTR					lpszCmdFmt,
									 va_list					ap)
{
	if ((lpszRspBuf != NULL) && (ulMaxRspLen > 0))
		*lpszRspBuf = _T('\0');

	if (IsEmptyStr(lpszOp))
		return EUDFFORMAT;

	TCHAR				szCmd[MAX_IMAP4_CMD_LEN+4]=_T("");
	CStrlBuilder	strb(szCmd, MAX_IMAP4_CMD_LEN);
	LPCTSTR			lpszTag=lpszITag;
	TCHAR				szTag[MAX_IMAP4_TAG_LEN+2]=_T("");
	EXC_TYPE			exc=InitIMAP4Cmd(lpszTag, lpszOp, fIsUID, szTag, MAX_IMAP4_TAG_LEN, strb);
	if (exc != EOK)
		return exc;

	if (!IsEmptyStr(lpszCmdFmt))
	{
		if ((exc=strb.AddChar(_T(' '))) != EOK)
			return exc;

		if ((exc=strb.VAddf(lpszCmdFmt, ap)) != EOK)
			return exc;
	}

	if ((exc=strb.AddCRLF()) != EOK)
		return exc;

	int	wLen=SBSock.Write(szCmd);
	if (wLen <= 0)
		return ENOTCONNECTION;

	return imap4GetRspSync(SBSock, lpszTag, lpszRspBuf, ulMaxRspLen, ulRspTimeout, lpfnHcfn, pArg);
}

/*---------------------------------------------------------------------------*/

// Note: automatically adds tag
EXC_TYPE imap4ExecCmdfSync (ISockioInterface&	SBSock,
									 LPCTSTR					lpszITag,	// NULL == auto generate
									 LPTSTR					lpszRsp,
									 const UINT32			ulMaxRspLen,
									 const UINT32			ulRspTimeout,
									 IMAP4_RSPHNDL_CFN	lpfnHcfn,
									 LPVOID					pArg,
									 LPCTSTR					lpszOp,
									 const BOOLEAN			fIsUID,
									 LPCTSTR					lpszCmdFmt,
									 ...)
{
	va_list	ap;
	va_start(ap, lpszCmdFmt);
	EXC_TYPE	exc=imap4ExecCmdVSync(SBSock, lpszITag, lpszRsp, ulMaxRspLen, ulRspTimeout, lpfnHcfn, pArg, lpszOp, fIsUID, lpszCmdFmt, ap);
	va_end(ap);

	return exc;
}

//////////////////////////////////////////////////////////////////////////////

EXC_TYPE imap4LoginUserSync (ISockioInterface&	SBSock,
									  LPCTSTR				lpszITag,	// NULL == auto generate
									  LPCTSTR				lpszUID,
									  LPCTSTR				lpszPasswd,
									  LPTSTR					lpszRspBuf,
									  const UINT32			ulMaxRspLen,
									  const UINT32			ulRspTimeout)
{
	if ((lpszRspBuf != NULL) && (ulMaxRspLen > 0))
		*lpszRspBuf = _T('\0');

	if (IsEmptyStr(lpszUID) || IsEmptyStr(lpszPasswd))
		return EEMPTYENTRY;

	if (*lpszUID != IMAP4_QUOTE_DELIM)
		return imap4ExecCmdfSync(SBSock, lpszITag, lpszRspBuf, ulMaxRspLen, ulRspTimeout,
										 imap4OKHCfn, NULL,
										 szIMAP4LoginCmd, FALSE, _T("%c%s%c %c%s%c"),
										 IMAP4_QUOTE_DELIM, lpszUID, IMAP4_QUOTE_DELIM,
										 IMAP4_QUOTE_DELIM, lpszPasswd, IMAP4_QUOTE_DELIM);
	else	// assume both quoted
		return imap4ExecCmdfSync(SBSock, lpszITag, lpszRspBuf, ulMaxRspLen, ulRspTimeout,
										 imap4OKHCfn, NULL,
										 szIMAP4LoginCmd, FALSE, _T("%s %s"),
										 lpszUID, lpszPasswd);
}

EXC_TYPE imap4LoginUserSync (ISockioInterface&	SBSock,
									  LPCTSTR				lpszITag,	// NULL == auto generate
									  LPCTSTR				lpszUID,
									  LPCTSTR				lpszPasswd,
									  const UINT32			ulRspTimeout)
{
	TCHAR szRsp[MAX_IMAP4_DATA_LEN+2]=_T("");
	return imap4LoginUserSync(SBSock, lpszITag, lpszUID, lpszPasswd, szRsp, MAX_IMAP4_DATA_LEN, ulRspTimeout);
}

//////////////////////////////////////////////////////////////////////////////

/*
 *	Makes sure the provided account parameters are valid. Returns:
 *
 *		ENOTCONNECTION - server could not be connected
 *		ESTATE - server service denied ("* NO/BAD" in welcome line)
 *		EPERMISSION - authentication failed
 */
EXC_TYPE imap4ValidateClientSync (LPCTSTR			lpszHost,
											 const int		iPort,		/* if 0 then use default */
											 LPCTSTR			lpszUserName,
											 LPCTSTR			lpszPasswd,
											 LPTSTR			lpszRspBuf,
											 const UINT32	ulBufLen,
											 const UINT32	ulRspTimeout)
{
	CBuffSock	SBSock;
	HRESULT		hr=imap4Connect(SBSock, lpszHost, iPort, lpszRspBuf, ulBufLen, ulRspTimeout);
	if (hr != EOK)
		return ENOTCONNECTION;

	if ((hr=imap4LoginUserSync(SBSock, NULL, lpszUserName, lpszPasswd, lpszRspBuf, ulBufLen, ulRspTimeout)) != EOK)
		return EPERMISSION;

	if ((hr=imap4LogoutUserSync(SBSock, NULL, lpszRspBuf, ulBufLen, ulRspTimeout)) != EOK)
		hr = EOK;

	return EOK;
}

EXC_TYPE imap4ValidateClientSync (LPCTSTR			lpszHost,
											 const int		iPort,		/* if 0 then use default */
											 LPCTSTR			lpszUserName,
											 LPCTSTR			lpszPasswd,
											 const UINT32	ulRspTimeout)
{
	TCHAR szRsp[MAX_IMAP4_DATA_LEN+2]=_T("");
	return imap4ValidateClientSync(lpszHost, iPort, lpszUserName, lpszPasswd, szRsp, MAX_IMAP4_DATA_LEN, ulRspTimeout);
}

//////////////////////////////////////////////////////////////////////////////

EXC_TYPE imap4NoopSync (ISockioInterface&	SBSock,
								LPCTSTR				lpszITag,	// NULL == auto-generate
								IMAP4_RSPHNDL_CFN	lpfnIcfn,	// may be NULL
								LPVOID				pArg,
								const UINT32		ulRspTimeout)
{
	TCHAR szRsp[MAX_IMAP4_DATA_LEN+2]=_T("");
	return imap4NoopSync(SBSock, lpszITag, lpfnIcfn, pArg, szRsp, MAX_IMAP4_DATA_LEN, ulRspTimeout);
}

//////////////////////////////////////////////////////////////////////////////

EXC_TYPE imap4CloseSync (ISockioInterface&	SBSock,
								 LPCTSTR					lpszITag,	// NULL == auto-generate
								 const UINT32			ulRspTimeout)
{
	TCHAR		szRsp[MAX_IMAP4_DATA_LEN+2]=_T("");
	return imap4CloseSync(SBSock, lpszITag, szRsp, MAX_IMAP4_DATA_LEN, ulRspTimeout);
}

//////////////////////////////////////////////////////////////////////////////

EXC_TYPE imap4LogoutUserSync (ISockioInterface&	SBSock,
										LPCTSTR				lpszITag,	// NULL == auto-generate
										LPTSTR				lpszRspBuf,
										const UINT32		ulMaxRspLen,
										const UINT32		ulRspTimeout)
{
	EXC_TYPE	exc=imap4ExecCmdfSync(SBSock, lpszITag, lpszRspBuf, ulMaxRspLen, ulRspTimeout,
											 imap4OKHCfn, NULL,
											 szIMAP4LogoutCmd, FALSE, NULL);

	// dummy read to ensure closure (only if got OK response)
	TCHAR	szDummyBuf[MAX_IMAP4_DATA_LEN+2]=_T("");
	int	rLen=(-1);
	if (EOK == exc)
		rLen = SBSock.ReadCmd(szDummyBuf, MAX_IMAP4_DATA_LEN, ulRspTimeout);
	SBSock.Close();

	// should not happen, since other side is supposed to close the connection
	if (rLen >= 0)
		// do another read just to ensure proper closure (again)
		rLen = SBSock.ReadCmd(szDummyBuf, MAX_IMAP4_DATA_LEN, ulRspTimeout);

	return exc;
}

// Note: also closes the connection
EXC_TYPE imap4LogoutUserSync (ISockioInterface&	SBSock,
										LPCTSTR				lpszITag,	// NULL == auto-generate
										const UINT32		ulRspTimeout)
{
	TCHAR		szRsp[MAX_IMAP4_DATA_LEN+2]=_T("");
	return imap4LogoutUserSync(SBSock, lpszITag, szRsp, MAX_IMAP4_DATA_LEN, ulRspTimeout);
}

//////////////////////////////////////////////////////////////////////////////

EXC_TYPE imap4Connect (ISockioInterface&	SBSock,
							  LPCTSTR				lpszHost,
							  const int				iConnPort,	// 0 == use default
							  LPTSTR					lpszRsp,
							  const UINT32			ulMaxLen,
							  const UINT32			ulRspTimeout)
{
	if (IsEmptyStr(lpszHost) || (iConnPort < 0) || (NULL == lpszRsp) || (0 == ulMaxLen))
		return EPARAM;

	int		iPort=resolve_service_port("imap", iConnPort, IPPORT_IMAP4);
	EXC_TYPE	exc=SBSock.Connect(lpszHost, iPort);
	if (exc != EOK)
		return exc;

	// read welcome line
	BOOLEAN	fStripCRLF=FALSE;
	int		rLen=SBSock.ReadCmd(lpszRsp, ulMaxLen, ulRspTimeout, &fStripCRLF);
	if (rLen <= 0)
		return ENOTCONNECTION;
	if (!fStripCRLF)
		return EOVERFLOW;

	if ((exc=CheckIMAP4Welcome(lpszRsp)) != EOK)
	{
		SBSock.Close();

		// dummy read to ensure closure
		LPTSTR	lpszDummy=(lpszRsp + rLen + 1);
		*lpszDummy = _T('\0');

		const UINT32	ulRemLen=(ulMaxLen - (UINT32) rLen - 1);
		rLen = SBSock.ReadCmd(lpszDummy, ulRemLen, ulRspTimeout, &fStripCRLF);

		return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4Connect (ISockioInterface&	SBSock,
							  LPCTSTR				lpszHost,
							  const int				iConnPort,	// 0 == use default
							  const UINT32			ulRspTimeout)
{
	TCHAR		szRsp[MAX_IMAP4_DATA_LEN+2]=_T("");
	return imap4Connect(SBSock, lpszHost, iConnPort, szRsp, MAX_IMAP4_DATA_LEN, ulRspTimeout);
}

//////////////////////////////////////////////////////////////////////////////

EXC_TYPE imap4RenameFolderSync (ISockioInterface&	SBSock,
										  LPCTSTR				lpszITag,	// NULL == auto-generate
										  LPCTSTR				lpszOldName,
										  LPCTSTR				lpszNewName,
										  LPTSTR					lpszRspBuf,
										  const UINT32			ulMaxRspLen,
										  const UINT32			ulRspTimeout)
{
	if ((lpszRspBuf != NULL) && (ulMaxRspLen > 0))
		*lpszRspBuf = _T('\0');

	if (IsEmptyStr(lpszOldName) || IsEmptyStr(lpszNewName))
		return EPATH;

	TCHAR				szCmd[MAX_IMAP4_CMD_LEN+4]=_T("");
	CStrlBuilder	strb(szCmd, MAX_IMAP4_CMD_LEN);
	LPCTSTR			lpszTag=lpszITag;
	TCHAR				szTag[MAX_IMAP4_TAG_LEN+2]=_T("");
	EXC_TYPE			exc=InitIMAP4Cmd(lpszTag, szIMAP4RenameCmd, FALSE, szTag, MAX_IMAP4_TAG_LEN, strb);
	if (exc != EOK)
		return exc;

	if ((exc=SendIMAP4Folder(SBSock, szCmd, lpszOldName, ulRspTimeout)) != EOK)
	{
		if (exc != ELITERAL)
			return exc;

		strb.Reset();
	}
	else	// no special characters
	{
		if ((exc=strb.AddChar(_T(' '))) != EOK)
			return exc;

		if ((exc=AddIMAP4Folder(strb, lpszOldName)) != EOK)
			return exc;
	}

	int	wLen=(-1);
	if ((exc=SendIMAP4Folder(SBSock, szCmd, lpszNewName, ulRspTimeout)) != EOK)
	{
		if (exc != ELITERAL)
			return exc;

		strb.Reset();

		if ((wLen=SBSock.Writeln()) != 2)
			return ENOTCONNECTION;
	}
	else	// no special characters
	{
		if ((exc=strb.AddChar(_T(' '))) != EOK)
			return exc;

		if ((exc=AddIMAP4Folder(strb, lpszNewName)) != EOK)
			return exc;

		if ((exc=strb.AddCRLF()) != EOK)
			return exc;

		if ((wLen=SBSock.Write(szCmd)) <= 0)
			return ENOTCONNECTION;
	}

	return imap4GetRspSync(SBSock, lpszTag, lpszRspBuf, ulMaxRspLen, ulRspTimeout, imap4OKHCfn, NULL);
}

EXC_TYPE imap4RenameFolderSync (ISockioInterface&	SBSock,
										  LPCTSTR				lpszITag,	// NULL == auto-generate
										  LPCTSTR				lpszOldName,
										  LPCTSTR				lpszNewName,
										  const UINT32			ulRspTimeout)
{
	TCHAR	szRsp[MAX_IMAP4_DATA_LEN+2]=_T("");
	return imap4RenameFolderSync(SBSock, lpszITag, lpszOldName, lpszNewName, szRsp, MAX_IMAP4_DATA_LEN, ulRspTimeout);
}

//////////////////////////////////////////////////////////////////////////////

typedef EXC_TYPE (*IMAP4_SLCTHCFN)(LPCTSTR				lpszOpt,
											  LPCTSTR				lpszArgs,
											  IMAP4SELECTSTRUCT&	selStruct);

/*---------------------------------------------------------------------------*/

static EXC_TYPE setIMAP4SlctBracketField (UINT32& ulFld, LPCTSTR lpszVal)
{
	if (IsEmptyStr(lpszVal))
		return EUDFFORMAT;

	// find start of value
	LPCTSTR	lpszBV=lpszVal;
	for ( ; _istspace(*lpszBV) && (*lpszBV != _T('\0')); lpszBV++);

	// find end of value
	LPCTSTR	lpszVE=lpszBV;
	for ( ; _istdigit(*lpszVE) && (*lpszVE != _T('\0')); lpszVE++);

	UINT32	ulVLen=(lpszVE - lpszBV);
	EXC_TYPE	exc=EOK;
	ulFld = argument_to_dword(lpszBV, ulVLen, EXC_ARG(exc));
	if (exc != EOK)
		return exc;

	if ((*lpszVE != _T('\0')) && (*lpszVE != _T(' ')) && (*lpszVE != IMAP4_BRCKT_EDELIM))
		return EPATHNAMESYNTAX;

	return EOK;
}

static EXC_TYPE imap4SlctUnseen (LPCTSTR					/* lpszOpt */,
											LPCTSTR					lpszArgs,
											IMAP4SELECTSTRUCT&	selStruct)
{
	return setIMAP4SlctBracketField(selStruct.ulUnseenNum, lpszArgs);
}

static EXC_TYPE imap4SlctExists (LPCTSTR					/* lpszOpt */,
											LPCTSTR					lpszArgs,
											IMAP4SELECTSTRUCT&	selStruct)
{
	return setIMAP4SlctBracketField(selStruct.ulExistNum, lpszArgs);
}

static EXC_TYPE imap4SlctRecent (LPCTSTR					/* lpszOpt */,
											LPCTSTR					lpszArgs,
											IMAP4SELECTSTRUCT&	selStruct)
{
	return setIMAP4SlctBracketField(selStruct.ulRecentNum, lpszArgs);
}

static EXC_TYPE imap4SlctUIDValidity (LPCTSTR				/* lpszOpt */,
												  LPCTSTR				lpszArgs,
												  IMAP4SELECTSTRUCT&	selStruct)
{
	return setIMAP4SlctBracketField(selStruct.ulUIDValidity, lpszArgs);
}

static EXC_TYPE imap4SlctUIDNext (LPCTSTR					/* lpszOpt */,
											 LPCTSTR					lpszArgs,
											 IMAP4SELECTSTRUCT&	selStruct)
{
	return setIMAP4SlctBracketField(selStruct.ulUIDNext, lpszArgs);
}

//////////////////////////////////////////////////////////////////////////////

class CIMAP4FolderStatsRspParser : public CIMAP4RspParser {
	private:
		LPCTSTR					m_lpszCmd;
		IMAP4SELECTSTRUCT&	m_selStruct;
		IMAP4_FLAGS_ENUM_CFN	m_lpfnFlagsEnumCfn;
		LPVOID					m_pFlagsArg;
		IMAP4_FLAGS_ENUM_CFN	m_lpfnPermFlagsEnumCfn;
		LPVOID					m_pPermFlagsArg;

		EXC_TYPE UpdateNumSelectField (LPCTSTR lpszField, LPCTSTR lpszCount);

		// handles EXISTS, RECENT, UNSEEN, UIDVALIDITY and UIDNEXT reports
		EXC_TYPE HandleMsgsStatusCount (LPCTSTR lpszStatOp, LPCTSTR lpszCount)
		{
			return UpdateNumSelectField(lpszStatOp, lpszCount);
		}

		EXC_TYPE HandleSelectionFlags ();

		// handle PERMANENTFLAGS, UIDNEXT and UIDVALIDITY
		EXC_TYPE HandleBracketedResponse (LPCTSTR lpszOp);

		// disable copy constructore and assignment operator
		CIMAP4FolderStatsRspParser (const CIMAP4FolderStatsRspParser& );
		CIMAP4FolderStatsRspParser& operator= (const CIMAP4FolderStatsRspParser& );

	public:
		CIMAP4FolderStatsRspParser (ISockioInterface&		SBSock,
											 LPCTSTR						lpszTag,
											 LPCTSTR						lpszCmd,
											 IMAP4SELECTSTRUCT&		selStruct,
											 IMAP4_FLAGS_ENUM_CFN	lpfnFlagsEnumCfn,	// may be NULL
											 LPVOID						pFlagsArg,
											 IMAP4_FLAGS_ENUM_CFN	lpfnPermFlagsEnumCfn,	// may be NULL
											 LPVOID						pPermFlagsArg,
											 LPTSTR						lpszRspBuf,
											 const UINT32				ulMaxRspLen,
											 const UINT32				ulRspTimeout);

		virtual EXC_TYPE HandleUntaggedResponse (LPCTSTR lpszTag, LPCTSTR lpszOp);

		virtual ~CIMAP4FolderStatsRspParser ()
		{
		}
};

/*--------------------------------------------------------------------------*/

CIMAP4FolderStatsRspParser::CIMAP4FolderStatsRspParser (ISockioInterface&		SBSock,
																		  LPCTSTR					lpszTag,
																		  LPCTSTR					lpszCmd,
																		  IMAP4SELECTSTRUCT&		selStruct,
																		  IMAP4_FLAGS_ENUM_CFN	lpfnFlagsEnumCfn,	// may be NULL
																		  LPVOID						pFlagsArg,
																		  IMAP4_FLAGS_ENUM_CFN	lpfnPermFlagsEnumCfn,	// may be NULL
																		  LPVOID						pPermFlagsArg,
																		  LPTSTR						lpszRspBuf,
																		  const UINT32				ulMaxRspLen,
																		  const UINT32				ulRspTimeout)
	: CIMAP4RspParser(SBSock, lpszTag, lpszRspBuf, ulMaxRspLen, ulRspTimeout)
	, m_lpszCmd(lpszCmd)
	, m_selStruct(selStruct)
	, m_lpfnFlagsEnumCfn(lpfnFlagsEnumCfn)
	, m_pFlagsArg(pFlagsArg)
	, m_lpfnPermFlagsEnumCfn(lpfnPermFlagsEnumCfn)
	, m_pPermFlagsArg(pPermFlagsArg)
{
	::memset(&m_selStruct, 0, (sizeof m_selStruct));
	m_selStruct.ulUnseenNum = (UINT32) (-1);
}

/*--------------------------------------------------------------------------*/

// handles numerical value responses
EXC_TYPE CIMAP4FolderStatsRspParser::UpdateNumSelectField (LPCTSTR lpszField, LPCTSTR lpszCount)
{
	static const STR2PTRASSOC slctCmdHandlers[]={
		{	IMAP4_UNSEEN,				(LPVOID) imap4SlctUnseen		},
		{	IMAP4_UIDVALIDITY,		(LPVOID) imap4SlctUIDValidity	},
		{	IMAP4_UIDNEXT,				(LPVOID) imap4SlctUIDNext		},
		{	IMAP4_EXISTS,				(LPVOID) imap4SlctExists		},
		{	IMAP4_RECENT,				(LPVOID) imap4SlctRecent		},

		{	NULL,							NULL									}	// mark end
	};

	static const CStr2PtrMapper slctCmdhMap(slctCmdHandlers, 0, FALSE);

	IMAP4_SLCTHCFN	lpfnHcfn=NULL;
	EXC_TYPE			exc=slctCmdhMap.FindKey(lpszField, (LPVOID &) lpfnHcfn);
	// ignore unknwon reponses
	if ((exc != EOK) || (NULL == lpfnHcfn))
		return EOK;

	if ((exc=(*lpfnHcfn)(lpszField, lpszCount, m_selStruct)) != EOK)
		return exc;

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FolderStatsRspParser::HandleSelectionFlags ()
{
	EXC_TYPE	exc=EOK;

	// re-synchronize to beginning of flags list
	for ( ; (m_lpszCurPos > m_lpszRspBuf) && (*m_lpszCurPos != IMAP4_PARLIST_SDELIM); m_lpszCurPos--);

	// allow "NIL" as flags list
	if (IMAP4_PARLIST_SDELIM != *m_lpszCurPos)
	{
		if ((exc=CheckNILParseBuffer(FALSE)) != EOK)
			return exc;

		return EOK;
	}

	if ((exc=ExtractMsgFlags(m_selStruct.dynFlags, m_lpfnFlagsEnumCfn, m_pFlagsArg)) != EOK)
		return exc;

	return EOK;
}

/*--------------------------------------------------------------------------*/

// handle PERMANENTFLAGS, UIDNEXT and UIDVALIDITY
EXC_TYPE CIMAP4FolderStatsRspParser::HandleBracketedResponse (LPCTSTR lpszOp)
{
	EXC_TYPE	exc=EOK;

	if (0 == ::_tcsicmp(lpszOp, IMAP4_PERMANENTFLAGS))
	{
		if ((exc=ExtractMsgFlags(m_selStruct.permFlags, m_lpfnPermFlagsEnumCfn, m_pPermFlagsArg)) != EOK)
			return exc;
	}
	else
	{
		if ((exc=FillNonEmptyParseBuffer()) != EOK)
			return exc;

		if ((exc=UpdateNumSelectField(lpszOp, m_lpszCurPos)) != EOK)
			return exc;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FolderStatsRspParser::HandleUntaggedResponse (LPCTSTR lpszTag, LPCTSTR lpszOp)
{
	EXC_TYPE	exc=EOK;

	// ignore any response that does not have at least 2 arguments
	if (IsEmptyStr(lpszTag) || IsEmptyStr(lpszOp))
		return SkipToEndOfLine();

	// check if this is a numbered response
	if (::_istdigit(*lpszTag))
	{
		if ((exc=HandleMsgsStatusCount(lpszOp, lpszTag)) != EOK)
			return exc;
	}
	else if (0 == ::_tcsicmp(lpszTag, IMAP4_OK))
	{
		// we are interested only in bracketed responses (and ignore all others)
		if (IMAP4_BRCKT_SDELIM == *lpszOp)
		{
			if ((exc=HandleBracketedResponse(lpszOp+1)) != EOK)
				return exc;
		}
	}
	else if (0 == ::_tcsicmp(lpszTag, IMAP4_FLAGS))
	{
		if ((exc=HandleSelectionFlags()) != EOK)
			return exc;
	}

	// we ignore any reponse that does not interest us
	if ((exc=SkipToEndOfLine()) != EOK)
		return exc;

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

EXC_TYPE imap4FolderStatsSync (ISockioInterface&		SBSock,
										 LPCTSTR						lpszITag,	// NULL == auto-generate
										 LPCTSTR						lpszCmd,
										 LPCTSTR						lpszFolder,
										 IMAP4SELECTSTRUCT&		selStruct,
										 IMAP4_FLAGS_ENUM_CFN	lpfnFlagsEnumCfn,	// may be NULL
										 LPVOID						pFlagsArg,
										 IMAP4_FLAGS_ENUM_CFN	lpfnPermFlagsEnumCfn,	// may be NULL
										 LPVOID						pPermFlagsArg,
										 LPTSTR						lpszRspBuf,
										 const UINT32				ulMaxRspLen,
										 const UINT32				ulRspTimeout)
{
	if ((lpszRspBuf != NULL) && (ulMaxRspLen > 0))
		*lpszRspBuf = _T('\0');

	if (IsEmptyStr(lpszFolder))
		return EPATH;

	TCHAR				szCmd[MAX_IMAP4_CMD_LEN+4]=_T("");
	CStrlBuilder	strb(szCmd, MAX_IMAP4_CMD_LEN);
	LPCTSTR			lpszTag=lpszITag;
	TCHAR				szTag[MAX_IMAP4_TAG_LEN+2]=_T("");
	EXC_TYPE			exc=InitIMAP4Cmd(lpszTag, lpszCmd, FALSE, szTag, MAX_IMAP4_TAG_LEN, strb);
	if (exc != EOK)
		return exc;

	int	wLen=(-1);
	if ((exc=SendIMAP4Folder(SBSock, szCmd, lpszFolder, ulRspTimeout)) != EOK)
	{
		if (exc != ELITERAL)
			return exc;

		strb.Reset();

		if ((wLen=SBSock.Writeln()) != 2)
			return ENOTCONNECTION;
	}
	else	// no special characters
	{
		if ((exc=strb.AddChar(_T(' '))) != EOK)
			return exc;

		if ((exc=AddIMAP4Folder(strb, lpszFolder)) != EOK)
			return exc;

		if ((exc=strb.AddCRLF()) != EOK)
			return exc;

		if ((wLen=SBSock.Write(szCmd)) <= 0)
			return ENOTCONNECTION;
	}

	CIMAP4FolderStatsRspParser	fsrp(SBSock, lpszTag, lpszCmd, selStruct, lpfnFlagsEnumCfn, pFlagsArg, lpfnPermFlagsEnumCfn, pPermFlagsArg, lpszRspBuf, ulMaxRspLen, ulRspTimeout);
	if ((exc=fsrp.ParseResponse()) != EOK)
		return exc;

	return EOK;
}

EXC_TYPE imap4FolderStatsSync (ISockioInterface&	SBSock,
										 LPCTSTR					lpszITag,	// NULL == auto-generate
										 LPCTSTR					lpszCmd,
										 LPCTSTR					lpszFolder,
										 IMAP4SELECTSTRUCT&	selStruct,
										 const UINT32			ulRspTimeout)
{
	TCHAR		szRsp[MAX_IMAP4_DATA_LEN+2]=_T("");
	return imap4FolderStatsSync(SBSock, lpszITag, lpszCmd, lpszFolder, selStruct, NULL, NULL, NULL, NULL, szRsp, MAX_IMAP4_DATA_LEN, ulRspTimeout);
}

/*--------------------------------------------------------------------------*/

static EXC_TYPE rfaCfn (const UINT32	ulFlgIndex,	/* starts at zero */
								LPCTSTR			lpszFlag,	/* raw flag string - NOTE: not necessarily EOS terminated !!! */
								const UINT32	ulFlgLen,	/* length of string data */
								LPVOID			pArg,
								BOOLEAN			*pfContEnum)
{
	if (NULL == pArg)
		return ECONTEXT;
	if (ulFlgLen > MAX_IMAP4_FLAGS_ENCLEN)
		return EOVERFLOW;

	TCHAR	szFlagCopy[MAX_IMAP4_FLAGS_ENCLEN+2]=_T("");
	_tcsncpy(szFlagCopy, lpszFlag, ulFlgLen);
	szFlagCopy[ulFlgLen] = _T('\0');

	CVSDCollection&	rawFlags=*((CVSDCollection *) pArg);
	EXC_TYPE				exc=rawFlags.AddItem((LPVOID) szFlagCopy, (ulFlgLen + 1) * sizeof(TCHAR));
	if (exc != EOK)
		return exc;

	return EOK;
}

/* NOTE(s):
 *		a. initial size of raw flags collection(s) should be IMAP4_BADFLAG_MSGCASE
 *		b. flags are ADDED to the raw collection(s) regardless of whether they already exist in them
 */
EXC_TYPE imap4FullFolderStatsSync (ISockioInterface&	SBSock,
											  LPCTSTR				lpszITag,	// NULL == auto-generate
											  LPCTSTR				lpszCmd,
											  LPCTSTR				lpszFolder,
											  IMAP4SELECTSTRUCT&	selStruct,
											  CVSDCollection&		dynRawFlags,	// each member is a string
											  CVSDCollection&		prmRawFlags,	// each member is a string
											  LPTSTR					lpszRspBuf,
											  const UINT32			ulMaxRspLen,
											  const UINT32			ulRspTimeout)
{
	return imap4FolderStatsSync(SBSock, lpszITag, lpszCmd, lpszFolder, selStruct, rfaCfn, (LPVOID) &dynRawFlags, rfaCfn, (LPVOID) &prmRawFlags, lpszRspBuf, ulMaxRspLen, ulRspTimeout);
}

EXC_TYPE imap4FullFolderStatsSync (ISockioInterface&	SBSock,
											  LPCTSTR				lpszITag,	// NULL == auto-generate
											  LPCTSTR				lpszCmd,
											  LPCTSTR				lpszFolder,
											  IMAP4SELECTSTRUCT&	selStruct,
											  CVSDCollection&		dynRawFlags,	// each member is a string
											  CVSDCollection&		prmRawFlags,	// each member is a string
											  const UINT32			ulRspTimeout)
{
	TCHAR		szRsp[MAX_IMAP4_DATA_LEN+2]=_T("");
	return imap4FullFolderStatsSync(SBSock, lpszITag, lpszCmd, lpszFolder, selStruct, dynRawFlags, prmRawFlags, szRsp, MAX_IMAP4_DATA_LEN, ulRspTimeout);
}

//////////////////////////////////////////////////////////////////////////////

class CIMAP4MBRefRspParser : public CIMAP4RspParser {
	private:
		IMAP4_LISTECFN		m_lpfnEcfn;
		LPVOID				m_pArg;
		LPCTSTR				m_lpszCmd;
		BOOLEAN				m_fContEnum;

		EXC_TYPE ExtractHierarchySeparator (LPCTSTR& lpszHSep, UINT32& ulHSLen);

		EXC_TYPE ExtractFolderFlags (LPCTSTR lpszIBuf, IMAP4_FLDRFLAGS& flags);

		// disable copy constructore and assignment operator
		CIMAP4MBRefRspParser (const CIMAP4MBRefRspParser& );
		CIMAP4MBRefRspParser& operator= (const CIMAP4MBRefRspParser& );

	public:
		CIMAP4MBRefRspParser (ISockioInterface&	SBSock,
									 LPCTSTR					lpszTag,
									 LPCTSTR					lpszCmd,
									 IMAP4_LISTECFN		lpfnEcfn,
									 LPVOID					pArg,
									 LPTSTR					lpszRspBuf,
									 const UINT32			ulMaxRspLen,
									 const UINT32			ulRspTimeout)
			: CIMAP4RspParser(SBSock, lpszTag, lpszRspBuf, ulMaxRspLen, ulRspTimeout)
			, m_lpfnEcfn(lpfnEcfn)
			, m_pArg(pArg)
			, m_lpszCmd(lpszCmd)
			, m_fContEnum(TRUE)
		{
		}

		virtual EXC_TYPE HandleUntaggedResponse (LPCTSTR lpszTag, LPCTSTR lpszOp);

		virtual ~CIMAP4MBRefRspParser () { }
};

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4MBRefRspParser::ExtractHierarchySeparator (LPCTSTR& lpszHSep, UINT32& ulHSLen)
{
	lpszHSep = NULL;
	ulHSLen = 0;

	EXC_TYPE	exc=FillNonEmptyParseBuffer();
	if (exc != EOK)
		return exc;

	if (*m_lpszCurPos != IMAP4_QUOTE_DELIM)
	{
		if (IMAP4_OCTCNT_SDELIM != *m_lpszCurPos)
		{
			// check if NIL reported
			if (EOK == (exc=CheckNILParseBuffer(FALSE)))
			{
				lpszHSep = _T("");
				return EOK;
			}
			else
			{
				if (exc != EWILDCARD)
					return exc;
			}

			for (lpszHSep=m_lpszCurPos; (!_istspace(*m_lpszCurPos)); m_lpszCurPos++);

			ulHSLen = (m_lpszCurPos - lpszHSep);
		}
		else	// literal
		{
			if ((exc=ExtractLiteralHdrVal(lpszHSep, ulHSLen, FALSE)) != EOK)
				return exc;
		}
	}
	else	// quoted separator
	{
		m_lpszCurPos++;

		for (lpszHSep=m_lpszCurPos; (*m_lpszCurPos != IMAP4_QUOTE_DELIM) && (*m_lpszCurPos != _T('\0')); m_lpszCurPos++);
		if (*m_lpszCurPos != IMAP4_QUOTE_DELIM)
			return ESPACE;

		ulHSLen = (m_lpszCurPos - lpszHSep);
		m_lpszCurPos++;	// skip quote sign
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4MBRefRspParser::ExtractFolderFlags (LPCTSTR lpszIBuf, IMAP4_FLDRFLAGS& flags)
{
	memset(&flags, 0, (sizeof flags));

	// check if NIL atom
	if (0 == _tcsicmp(lpszIBuf, IMAP4_NIL))
		return EOK;

	if (*lpszIBuf != IMAP4_PARLIST_SDELIM)
		return EUDFFORMAT;

	// skip start delimiter
	LPCTSTR	lpszIFlag=lpszIBuf;
	for (lpszIFlag++; ::_istspace(*lpszIFlag) && (*lpszIFlag != _T('\0')); lpszIFlag++);

	IMAP4_FLDRFLAG_CASE	fCase=IMAP4_BDFLG_FLDRCASE;
	EXC_TYPE					exc=EOK;

	// check for empty or one-member list
	{
		LPCTSTR	lpszEFlag=lpszIFlag;
		for (; (*lpszEFlag != IMAP4_PARLIST_EDELIM) && (*lpszEFlag != _T('\0')); lpszEFlag++);

		const UINT32	ulIFLen=(lpszEFlag - lpszIFlag);
		if (ulIFLen != 0)
		{
			fCase = ::imap4XlateExtFldrFlag(lpszIFlag, ulIFLen);
			if ((exc=::imap4UpdateFldrFlags(&flags, fCase, TRUE)) != EOK)
				return exc;
		}

		if (IMAP4_PARLIST_EDELIM == *lpszEFlag)
			return EOK;
	}

	while (*m_lpszCurPos != IMAP4_PARLIST_EDELIM)
	{
		for (m_lpszCurPos; ::_istspace(*m_lpszCurPos) && (*m_lpszCurPos != _T('\0')); m_lpszCurPos++);

		if (_T('\0') == *m_lpszCurPos)
		{
			if ((exc=RefillFetchParseBuffer()) != EOK)
				return exc;
			continue;
		}

		if (IMAP4_PARLIST_EDELIM == *m_lpszCurPos)
			break;

		if (IMAP4_SYSFLAG_SIGN != *m_lpszCurPos)
			return EUDFFORMAT;

		for (lpszIFlag=m_lpszCurPos; (*m_lpszCurPos != _T('\0')) && (*m_lpszCurPos != IMAP4_PARLIST_EDELIM); m_lpszCurPos++)
			if (::_istspace(*m_lpszCurPos))
				break;

		if (_T('\0') == *m_lpszCurPos)
		{
			if ((exc=RefillFetchParseBuffer(lpszIFlag)) != EOK)
				return exc;
			continue;
		}

		const UINT32	ulFlgLen=(m_lpszCurPos - lpszIFlag);
		fCase = ::imap4XlateExtFldrFlag(lpszIFlag, ulFlgLen);
		if ((exc=::imap4UpdateFldrFlags(&flags, fCase, TRUE)) != EOK)
			return exc;
	}

	m_lpszCurPos++;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4MBRefRspParser::HandleUntaggedResponse (LPCTSTR lpszTag, LPCTSTR lpszOp)
{
	// order is LIST/LSUB, flags, hierarchy, path
	if (_tcsicmp(lpszTag, m_lpszCmd) != 0)
	{
		// allow unsolicited responses (e.g. "* 3 EXISTS")
		if (!_istdigit(*lpszTag))
			return EILLEGALOPCODE;
		else
			return EOK;
	}

	EXC_TYPE	exc=FillNonEmptyParseBuffer();
	if (exc != EOK)
		return exc;

	IMAP4_FLDRFLAGS	flags;
	if ((exc=ExtractFolderFlags(lpszOp, flags)) != EOK)
		return exc;

	LPCTSTR	lpszHSep=NULL;
	UINT32	ulHSLen=0;
	if ((exc=ExtractHierarchySeparator(lpszHSep, ulHSLen)) != EOK)
		return exc;

	TCHAR	chSep=((0 == ulHSLen) ? _T('\0') : *lpszHSep);
	if ((_T('\\') == chSep) && (ulHSLen > 1))
		chSep = lpszHSep[1];

	LPCTSTR	lpszFolder=NULL;
	UINT32	ulFLen=0;
	if ((exc=ExtractStringHdrVal(lpszFolder, ulFLen, TRUE, FALSE)) != EOK)
		return exc;

	if (m_fContEnum)
	{
		LPCTSTR	lpszFE=(lpszFolder+ulFLen);
		TCHAR		chFE=*lpszFE;

		*((LPTSTR) lpszFE) = _T('\0');
		exc = (*m_lpfnEcfn)(m_SBSock, lpszFolder, chSep, flags, m_pArg, m_fContEnum);
		*((LPTSTR) lpszFE) = chFE;

		if (exc != EOK)
			return exc;
	}

	if ((exc=SkipToEndOfLine()) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4MBRefSync (ISockioInterface&	SBSock,
								 LPCTSTR					lpszITag,	// NULL == auto generate
								 LPCTSTR					lpszCmd,		// LIST/LSUB
								 LPCTSTR					lpszRef,
								 LPCTSTR					lpszMbox,
								 IMAP4_LISTECFN		lpfnECfn,
								 LPVOID					pArg,
								 LPTSTR					lpszRspBuf,
								 const UINT32			ulMaxRspLen,
								 const UINT32			ulRspTimeout)
{
	if ((NULL == lpfnECfn) || (NULL == lpszRspBuf) || (ulMaxRspLen < (MAX_IMAP4_CMD_LEN / 2)))
		return EBADADDR;
	*lpszRspBuf = _T('\0');

	TCHAR				szCmd[MAX_IMAP4_CMD_LEN+4]=_T("");
	CStrlBuilder	strb(szCmd, MAX_IMAP4_CMD_LEN);
	LPCTSTR			lpszTag=lpszITag;
	TCHAR				szTag[MAX_IMAP4_TAG_LEN+2]=_T("");
	EXC_TYPE			exc=InitIMAP4Cmd(lpszTag, lpszCmd, FALSE, szTag, MAX_IMAP4_TAG_LEN, strb);
	if (exc != EOK)
		return exc;
	if ((exc=strb.AddChar(_T(' '))) != EOK)
		return exc;

	// add reference
	LPCTSTR	lpszR=((NULL == lpszRef) ? _T("") : lpszRef);
	if (*lpszR != IMAP4_QUOTE_DELIM)
	{
		if ((exc=strb.AddChar(IMAP4_QUOTE_DELIM)) != EOK)
			return exc;
		if ((exc=strb.AddStr(lpszR)) != EOK)
			return exc;
		if ((exc=strb.AddChar(IMAP4_QUOTE_DELIM)) != EOK)
			return exc;
	}
	else
	{
		if ((exc=strb.AddStr(lpszR)) != EOK)
			return exc;
	}

	// add mailbox
	LPCTSTR	lpszM=((NULL == lpszMbox) ? _T("") : lpszMbox);
	int		wLen=(-1);

	// check if need to send as literal
	if ((exc=SendIMAP4Reference(SBSock, szCmd, lpszM, TRUE, ulRspTimeout)) != EOK)
	{
		if (exc != ELITERAL)
			return exc;

		strb.Reset();

		if ((wLen=SBSock.Writeln()) != 2)
			return ENOTCONNECTION;
	}
	else	// send as-is
	{
		if ((exc=strb.AddChar(_T(' '))) != EOK)
			return exc;

		if ((exc=AddIMAP4Reference(strb, lpszM)) != EOK)
			return exc;

		if ((exc=strb.AddCRLF()) != EOK)
			return exc;

		if ((wLen=SBSock.Write(szCmd)) <= 0)
			return ENOTCONNECTION;
	}

	CIMAP4MBRefRspParser	mbrp(SBSock, lpszTag, lpszCmd, lpfnECfn, pArg, lpszRspBuf, ulMaxRspLen, ulRspTimeout);
	if ((exc=mbrp.ParseResponse()) != EOK)
		return exc;

	return EOK;
}

EXC_TYPE imap4MBRefSync (ISockioInterface&	SBSock,
								 LPCTSTR					lpszITag,	// NULL == auto generate
								 LPCTSTR					lpszCmd,	// LIST/LSUB
								 LPCTSTR					lpszRef,
								 LPCTSTR					lpszMbox,
								 IMAP4_LISTECFN		lpfnECfn,
								 LPVOID					pArg,
								 const UINT32			ulRspTimeout)
{
	TCHAR	szRsp[MAX_IMAP4_CMD_LEN+2]=_T("");
	return imap4MBRefSync(SBSock, lpszITag, lpszCmd, lpszRef, lpszMbox, lpfnECfn, pArg, szRsp, MAX_IMAP4_CMD_LEN, ulRspTimeout);
}

//////////////////////////////////////////////////////////////////////////////

typedef struct {
	IMAP4_CAPSENUM_CFN	lpfnEcfn;
	LPVOID					pArg;
	BOOLEAN					fContEnum;
} CPAARGS, *LPCPAARGS;

static EXC_TYPE imap4CapabilityHCfn (ISockioInterface&	SBSock,
												 LPTSTR					lpszRsp,
												 const UINT32			/* ulRspLen */,
												 const UINT32			/* ulMaxRspLen */,
												 LPVOID					pArg)
{
	if (NULL == pArg)
		return ECONTEXT;

	CPAARGS&					cpa=*((LPCPAARGS) pArg);
	BOOLEAN&					fContEnum=cpa.fContEnum;
	IMAP4_CAPSENUM_CFN	lpfnEcfn=cpa.lpfnEcfn;
	if (NULL == lpfnEcfn)
		return EBADADDR;

	TCHAR		szOp[MAX_IMAP4_OPCODE_LEN+2]=_T("");
	LPCTSTR	lpszNext=NULL;
	EXC_TYPE	exc=imap4GetArg((lpszRsp+2), szOp, MAX_IMAP4_OPCODE_LEN, &lpszNext);
	if (exc != EOK)
		return exc;

	if (_tcsicmp(szOp, szIMAP4CapabilityCmd) != 0)
		return EILLEGALOPCODE;

	while (fContEnum)
	{
		for ( ; _istspace(*lpszNext) && (*lpszNext != _T('\0')); lpszNext++);
		if (_T('\0') == *lpszNext)
			break;

		LPCTSTR	lpszCap=lpszNext;
		for ( ; (*lpszNext != _T(' ')) && (*lpszNext != _T('\0')); lpszNext++);

		TCHAR	tch=*lpszNext;
		*((LPTSTR) lpszNext) = _T('\0');

		if ((exc=(*lpfnEcfn)(SBSock, lpszCap, cpa.pArg, fContEnum)) != EOK)
			return exc;
		if (_T('\0') == tch)
			break;

		*((LPTSTR) lpszNext) = tch;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4CapabilitiesSync (ISockioInterface&	SBSock,
										  LPCTSTR				lpszITag,	// NULL == auto-generate
										  IMAP4_CAPSENUM_CFN	lpfnEcfn,
										  LPVOID					pArg,
										  LPTSTR					lpszRspBuf,
										  const UINT32			ulMaxRspLen,
										  const UINT32			ulRspTimeout)
{
	if ((NULL == lpfnEcfn) || (NULL == lpszRspBuf) || (ulMaxRspLen < MAX_IMAP4_CMD_LEN))
		return EBADADDR;

	CPAARGS	cpa={	lpfnEcfn, pArg, TRUE };
	return imap4ExecCmdfSync(SBSock, lpszITag, lpszRspBuf, ulMaxRspLen, ulRspTimeout,
									 imap4CapabilityHCfn, (LPVOID) &cpa,
									 szIMAP4CapabilityCmd, FALSE, NULL);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4CapabilitiesSync (ISockioInterface&	SBSock,
										  LPCTSTR				lpszITag,	// NULL == auto-generate
										  IMAP4_CAPSENUM_CFN	lpfnEcfn,
										  LPVOID					pArg,
										  const UINT32			ulRspTimeout)
{

	TCHAR		szRsp[MAX_IMAP4_DATA_LEN+2]=_T("");
	return imap4CapabilitiesSync(SBSock, lpszITag, lpfnEcfn, pArg, szRsp, MAX_IMAP4_DATA_LEN, ulRspTimeout);
}

//////////////////////////////////////////////////////////////////////////////

class CIMAP4FolderStatusRspParser : public CIMAP4RspParser {
	private:
		IMAP4STATUSINFO&	m_fInfo;

		// disable copy constructore and assignment operator
		CIMAP4FolderStatusRspParser (const CIMAP4FolderStatusRspParser& );
		CIMAP4FolderStatusRspParser& operator= (const CIMAP4FolderStatusRspParser& );

		EXC_TYPE HandleUnsolicitedResponse (LPCTSTR lpszTag, LPCTSTR lpszOp);

	public:
		CIMAP4FolderStatusRspParser (ISockioInterface&	SBSock,
											  LPCTSTR				lpszTag,
											  IMAP4STATUSINFO&	fInfo,
											  LPTSTR					lpszRspBuf,
											  const UINT32			ulMaxRspLen,
											  const UINT32			ulRspTimeout)
			: CIMAP4RspParser(SBSock, lpszTag, lpszRspBuf, ulMaxRspLen, ulRspTimeout)
			, m_fInfo(fInfo)
		{
		}

		virtual EXC_TYPE HandleUntaggedResponse (LPCTSTR lpszTag, LPCTSTR lpszOp);

		virtual ~CIMAP4FolderStatusRspParser () { }
};

/*---------------------------------------------------------------------------*/

typedef EXC_TYPE (*STINFO_UPDCFN_TYPE)(IMAP4STATUSINFO& fInfo, const UINT32 ulVal);

inline EXC_TYPE stSetField (UINT32& ulDstVal, const UINT32 ulSrcVal)
{
	ulDstVal = ulSrcVal;
	return EOK;
}

inline EXC_TYPE stNumOfMsgsUpdate (IMAP4STATUSINFO& fInfo, const UINT32 ulVal)
{
	return stSetField(fInfo.ulNumOfMsgs, ulVal);
}

inline EXC_TYPE stNumRecentUpdate (IMAP4STATUSINFO& fInfo, const UINT32 ulVal)
{
	return stSetField(fInfo.ulNumRecent, ulVal);
}

inline EXC_TYPE stNumUnseenUpdate (IMAP4STATUSINFO& fInfo, const UINT32 ulVal)
{
	return stSetField(fInfo.ulUnseen, ulVal);
}

inline EXC_TYPE stUIDValidityUpdate (IMAP4STATUSINFO& fInfo, const UINT32 ulVal)
{
	return stSetField(fInfo.ulUIDValidity, ulVal);
}

static const STR2PTRASSOC stUnSolRspAssocs[]={
	{	IMAP4_EXISTS,		stNumOfMsgsUpdate		},
	{	IMAP4_RECENT,		stNumRecentUpdate		},
	{	IMAP4_UNSEEN,		stNumUnseenUpdate		},
	{	IMAP4_UIDVALIDITY,stUIDValidityUpdate	},
	{	NULL,					NULL						}	// mark end
};

static const CStr2PtrMapper stUnSolRspMap(stUnSolRspAssocs, 0, FALSE);

/*---------------------------------------------------------------------------*/

// allow unsolicited responses (e.g. "* 3 EXISTS") and use them to "correct" the returned information
EXC_TYPE CIMAP4FolderStatusRspParser::HandleUnsolicitedResponse (LPCTSTR lpszTag, LPCTSTR lpszOp)
{
	// find out if this response interests us
	LPVOID	pV=NULL;
	EXC_TYPE	exc=stUnSolRspMap.FindKey(lpszOp, pV);
	if (exc != EOK)
		return EOK;

	STINFO_UPDCFN_TYPE	lpfnUcfn=(STINFO_UPDCFN_TYPE) pV;
	if (NULL == lpfnUcfn)
		return ENOTCONFIGURED;

	// we are interested only in response with a number as 1st argument
	UINT32	ulVal=argument_to_dword(lpszTag, _tcslen(lpszTag), EXC_ARG(exc));
	if (exc != EOK)
		return exc;

	return (*lpfnUcfn)(m_fInfo, ulVal);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FolderStatusRspParser::HandleUntaggedResponse (LPCTSTR lpszTag, LPCTSTR lpszOp)
{
	if (_tcsicmp(lpszTag, szIMAP4StatusCmd) != 0)
		return HandleUnsolicitedResponse(lpszTag, lpszOp);

	EXC_TYPE	exc=SkipFolderUpToParamsList(lpszOp);
	if (exc != EOK)
		return exc;

	while (*m_lpszCurPos != IMAP4_PARLIST_EDELIM)
	{
		// get option name
		LPCTSTR	lpszOption=NULL;
		UINT32	ulOptLen=0;

		if ((exc=ExtractStringHdrVal(lpszOption, ulOptLen, FALSE, FALSE)) != EOK)
			return exc;

		IMAP4STKWCASE	stCase=imap4GetStatusKeywordCase(lpszOption, ulOptLen);
		if (fIsBadIMAP4StatusKWCase(stCase))
			return ETYPE;

		// get option value
		UINT32	ulOptVal=0;
		if ((exc=ExtractNumVal(ulOptVal)) != EOK)
			return exc;

		switch(stCase)
		{
			case IMAP4STMSGS		: m_fInfo.ulNumOfMsgs = ulOptVal; break;
			case IMAP4STRECENT	: m_fInfo.ulNumRecent = ulOptVal; break;
			case IMAP4STUIDNEXT	: m_fInfo.ulUIDNext = ulOptVal; break;
			case IMAP4STUDIVALID	: m_fInfo.ulUIDValidity = ulOptVal; break;
			case IMAP4STUNSEEN	: m_fInfo.ulUnseen = ulOptVal; break;

			default					:
				return ECONTEXT;
		}

		if ((exc=FillNonEmptyParseBuffer()) != EOK)
			return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4FolderStatusSync (ISockioInterface&			SBSock,
										  LPCTSTR						lpszITag,	// NULL == auto generate
										  LPCTSTR						lpszFolder,
										  const IMAP4STATUSFLAGS&	stFlags,
										  IMAP4STATUSINFO&			fInfo,
										  LPTSTR							lpszRspBuf,
										  const UINT32					ulMaxRspLen,
										  const UINT32					ulRspTimeout)
{
	memset(&fInfo, 0, (sizeof fInfo));

	if ((lpszRspBuf != NULL) && (ulMaxRspLen > 0))
		*lpszRspBuf = _T('\0');

	if (IsEmptyStr(lpszFolder))
		return EPATHNAMESYNTAX;

	TCHAR				szCmd[MAX_IMAP4_CMD_LEN+4]=_T("");
	CStrlBuilder	strb(szCmd, MAX_IMAP4_CMD_LEN);
	LPCTSTR			lpszTag=lpszITag;
	TCHAR				szTag[MAX_IMAP4_TAG_LEN+2]=_T("");
	EXC_TYPE			exc=InitIMAP4Cmd(lpszTag, szIMAP4StatusCmd, FALSE, szTag, MAX_IMAP4_TAG_LEN, strb);
	if (exc != EOK)
		return exc;

	// check how to send IMAP4 folder
	if (EOK == (exc=SendIMAP4Folder(SBSock, szCmd, lpszFolder, ulRspTimeout)))
	{
		if ((exc=strb.AddChar(_T(' '))) != EOK)
			return exc;
		
		if ((exc=AddIMAP4Folder(strb, lpszFolder)) != EOK)
			return exc;
	}
	else	// have special characters in folder name
	{
		if (exc != ELITERAL)
			return exc;

		strb.Reset();
	}

	if ((exc=strb.AddChar(_T(' '))) != EOK)
		return exc;

	// build command arguments template according to requested flags
	if ((exc=strb.AddChar(IMAP4_PARLIST_SDELIM)) != EOK)
		return exc;

	UINT32	ulOptsNum=0;
	for (UINT32 ulOdx=0; ulOdx < IMAP4_STINFO_ARGS_NUM; ulOdx++)
	{
		IMAP4STKWCASE	eStCase=(IMAP4STKWCASE) ulOdx;
		BOOLEAN			fUseOption=FALSE;

		switch(eStCase)
		{
			case IMAP4STMSGS		: fUseOption = (stFlags.m_fNumOfMsgs != 0); break;
			case IMAP4STRECENT	: fUseOption = (stFlags.m_fNumRecent != 0); break;
			case IMAP4STUIDNEXT	: fUseOption = (stFlags.m_fUIDNext != 0); break;
			case IMAP4STUDIVALID	: fUseOption = (stFlags.m_fUIDValidity != 0); break;
			case IMAP4STUNSEEN	: fUseOption = (stFlags.m_fUnseen != 0); break;
			default					: return ECONTEXT;
		}

		if (fUseOption)
		{
			LPCTSTR	lpszOption=imap4GetStatusKeywordString(eStCase);
			if (IsEmptyStr(lpszOption))
				return EBADBUFF;

			if (ulOdx > 0)
			{
				if ((exc=strb.AddChar(_T(' '))) != EOK)
					return exc;
			}

			if ((exc=strb.AddStr(lpszOption)) != EOK)
				return exc;

			ulOptsNum++;
		}
	}

	if ((exc=strb.AddChar(IMAP4_PARLIST_EDELIM)) != EOK)
		return exc;

	if (0 == ulOptsNum)
		return EEMPTYENTRY;

	if ((exc=strb.AddCRLF()) != EOK)
		return exc;

	int	wLen=SBSock.Write(szCmd);
	if (wLen <= 0)
		return ENOTCONNECTION;

	CIMAP4FolderStatusRspParser	fsrp(SBSock, lpszTag, fInfo, lpszRspBuf, ulMaxRspLen, ulRspTimeout);
	if ((exc=fsrp.ParseResponse()) != EOK)
		return exc;

	return EOK;
}

EXC_TYPE imap4FolderStatusSync (ISockioInterface&			SBSock,
										  LPCTSTR						lpszITag,	// NULL == auto generate
										  LPCTSTR						lpszFolder,
										  const IMAP4STATUSFLAGS&	stFlags,
										  IMAP4STATUSINFO&			fInfo,
										  const UINT32					ulRspTimeout)
{
	TCHAR		szRsp[MAX_IMAP4_DATA_LEN+2]=_T("");
	return imap4FolderStatusSync(SBSock, lpszITag, lpszFolder, stFlags, fInfo, szRsp, MAX_IMAP4_DATA_LEN, ulRspTimeout);
}

//////////////////////////////////////////////////////////////////////////////

class CIMAP4SearchRspParser : public CIMAP4RspParser {
	private:
		IMAP4_SRES_CFN	m_lpfnScfn;
		LPVOID			m_pArg;
		BOOLEAN			m_fContEnum;

	public:
		CIMAP4SearchRspParser (ISockioInterface&	SBSock,
									  LPCTSTR				lpszTag,
									  IMAP4_SRES_CFN		lpfnScfn,
									  LPVOID					pArg,
									  LPTSTR					lpszRspBuf,
									  const UINT32			ulMaxRspLen,
									  const UINT32			ulRspTimeout)
			: CIMAP4RspParser(SBSock, lpszTag, lpszRspBuf, ulMaxRspLen, ulRspTimeout)
			, m_lpfnScfn(lpfnScfn)
			, m_pArg(pArg)
			, m_fContEnum(TRUE)
		{
		}

		virtual EXC_TYPE HandleUntaggedResponse (LPCTSTR lpszTag, LPCTSTR lpszOp);

		virtual ~CIMAP4SearchRspParser () { }
};

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4SearchRspParser::HandleUntaggedResponse (LPCTSTR lpszTag, LPCTSTR lpszOp)
{
	if (_tcsicmp(lpszTag, szIMAP4SearchCmd) != 0)
	{
		// allow unsolicited responses (e.g. "* 3 EXISTS")
		if (!_istdigit(*lpszTag))
			return EILLEGALOPCODE;
		else
			return EOK;
	}

	for (EXC_TYPE	exc=EOK; ; exc=EOK)
	{
		for ( ; (!_istdigit(*m_lpszCurPos)) && (*m_lpszCurPos != _T('\0')); m_lpszCurPos++);

		// check if have more data
		if (_T('\0') == *m_lpszCurPos)
		{
			// if not entire line read then refill and retry
			if (!m_fStripCRLF)
			{
				if ((exc=RefillFetchParseBuffer()) != EOK)
					return exc;
				continue;
			}

			// at this point we know that entire data has been read and exhaustively parsed
			break;
		}

		LPCTSTR	lpszNum=m_lpszCurPos;
		for ( ; _istdigit(*m_lpszCurPos) && (*m_lpszCurPos != _T('\0')); m_lpszCurPos++);

		// if end of data but not entire line read then refill buffer and retry
		if ((_T('\0') == *m_lpszCurPos) && (!m_fStripCRLF))
		{
			if ((exc=RefillFetchParseBuffer(lpszNum)) != EOK)
				return exc;
			continue;
		}

		UINT32	ulNLen=(m_lpszCurPos - lpszNum);
		UINT32	ulNumVal=argument_to_dword(lpszNum, ulNLen, EXC_ARG(exc));
		if (exc != EOK)
			return exc;

		if (m_fContEnum)
		{
			if ((exc=(*m_lpfnScfn)(ulNumVal, m_pArg, &m_fContEnum)) != EOK)
				return exc;
		}
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4SearchMsgsSync (ISockioInterface&	SBSock,
										LPCTSTR				lpszITag,	// NULL == auto generate
										const BOOLEAN		fIsUID,
										LPCTSTR				lpszCharset,	// may be NULL/empty
										LPCTSTR				lpszCriteria,
										IMAP4_SRES_CFN		lpfnScfn,
										LPVOID				pArg,
										LPTSTR				lpszRspBuf,
										const UINT32		ulMaxRspLen,
										const UINT32		ulRspTimeout)
{
	if ((NULL == lpszRspBuf) || (ulMaxRspLen < (MAX_IMAP4_CMD_LEN/2)))
		return EBADADDR;
	*lpszRspBuf = _T('\0');

	if (IsEmptyStr(lpszCriteria))
		return ELIBEXEC;
	if (!IsEmptyStr(lpszCharset))
		return EREMCHG;

	TCHAR				szCmd[MAX_IMAP4_CMD_LEN+4]=_T("");
	CStrlBuilder	strb(szCmd, MAX_IMAP4_CMD_LEN);
	LPCTSTR			lpszTag=lpszITag;
	TCHAR				szTag[MAX_IMAP4_TAG_LEN+2]=_T("");
	EXC_TYPE			exc=InitIMAP4Cmd(lpszTag, szIMAP4SearchCmd, fIsUID, szTag, MAX_IMAP4_TAG_LEN, strb);
	if (exc != EOK)
		return exc;
	if ((exc=strb.AddChar(_T(' '))) != EOK)
		return exc;

	// check if can send in one "write" command
	int	wLen=(-1);
	UINT32	ulRLen=_tcslen(szCmd) + _tcslen(lpszCriteria);
	if (ulRLen > MAX_IMAP4_CMD_LEN)
	{
		if ((wLen=SBSock.Write(szCmd)) <= 0)
			return ENOTCONNECTION;
		if ((wLen=SBSock.Write(lpszCriteria)) <= 0)
			return ENOTCONNECTION;
		if ((wLen=SBSock.Writeln()) <= 0)
			return ENOTCONNECTION;
	}
	else	// can accomodate in single string
	{
		if ((exc=strb.AddStr(lpszCriteria)) != EOK)
			return exc;
		if ((exc=strb.AddCRLF()) != EOK)
			return exc;

		if ((wLen=SBSock.Write(szCmd)) <= 0)
			return ENOTCONNECTION;
	}

	CIMAP4SearchRspParser	srp(SBSock, lpszTag, lpfnScfn, pArg, lpszRspBuf, ulMaxRspLen, ulRspTimeout);
	return srp.ParseResponse();
}

EXC_TYPE imap4SearchMsgsSync (ISockioInterface&	SBSock,
										LPCTSTR				lpszITag,	// NULL == auto generate
										const BOOLEAN		fIsUID,
										LPCTSTR				lpszCharset,	// may be NULL/empty
										LPCTSTR				lpszCriteria,
										IMAP4_SRES_CFN		lpfnScfn,
										LPVOID				pArg,
										const UINT32		ulRspTimeout)
{
	TCHAR	szRsp[MAX_IMAP4_CMD_LEN+2]=_T("");
	return imap4SearchMsgsSync(SBSock, lpszITag, fIsUID, lpszCharset, lpszCriteria, lpfnScfn, pArg, szRsp, MAX_IMAP4_CMD_LEN, ulRspTimeout);
}

//////////////////////////////////////////////////////////////////////////////

typedef struct {
	IMAP4_XPNG_HCFN	lpfnXcfn;
	LPVOID				pArg;
	BOOLEAN				fContEnum;
} XPGARGS, *LPXPGARGS;

static EXC_TYPE imap4ExpungeHCfn (ISockioInterface&	SBSock,
											 LPTSTR					lpszRsp,
											 const UINT32			/* ulRspLen */,
											 const UINT32			/* ulMaxRspLen */,
											 LPVOID					pArg)
{
	if (NULL == pArg)
		return ECONTEXT;

	XPGARGS&	xpga=*((LPXPGARGS) pArg);
	BOOLEAN&	fContEnum=xpga.fContEnum;
	UINT32	ulMsgID=0, ulOpLen=0;
	LPCTSTR	lpszOpcode=NULL;
	EXC_TYPE	exc=imap4ParseExpungeRsp(lpszRsp, &ulMsgID, &lpszOpcode, &ulOpLen);
	if (exc != EOK)
		return exc;

	if (fContEnum)
	{
		IMAP4_XPNG_HCFN	lpfnXcfn=xpga.lpfnXcfn;
		if (NULL == lpfnXcfn)
			return EBADADDR;

		TCHAR	tch=lpszOpcode[ulOpLen];
		*((LPTSTR) (lpszOpcode + ulOpLen)) = _T('\0');
		exc = (*lpfnXcfn)(SBSock, ulMsgID, lpszOpcode, xpga.pArg, fContEnum);
		*((LPTSTR) (lpszOpcode + ulOpLen)) = tch;

		if (exc != EOK)
			return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4ExpungeSync (ISockioInterface&	SBSock,
									LPCTSTR				lpszITag,	// NULL == auto-generate
									IMAP4_XPNG_HCFN	lpfnXcfn,	// may be NULL
									LPVOID				pArg,
									const UINT32		ulRspTimeout)
{
	XPGARGS	xpga={ lpfnXcfn, pArg, (lpfnXcfn != NULL) };
	TCHAR		szRsp[MAX_IMAP4_DATA_LEN+2]=_T("");

	return imap4ExecCmdfSync(SBSock, lpszITag, szRsp, MAX_IMAP4_DATA_LEN, ulRspTimeout,
									 imap4ExpungeHCfn, (LPVOID) &xpga,
									 szIMAP4XpngCmd, FALSE, NULL);
}

//////////////////////////////////////////////////////////////////////////////

typedef struct {
	IMAP4_STORE_HCFN	lpfnHcfn;	// may be NULL
	LPVOID				pArg;
	BOOLEAN				fContEnum;
} STARGS;

static EXC_TYPE storeCfn (const CIMAP4FetchCfnData&	ftchData,
								  LPCTSTR							lpszFetchRsp,
								  LPCTSTR							lpszSubHdr,		// valid only for complex structures
								  LPCTSTR							lpszKeyword,	// non-NULL for "keyword=value" pair(s)
								  LPVOID								lpModVal)		// actual type depends on modifier
{
	LPVOID	pStarg=ftchData.GetCallbackArg();
	if (NULL == pStarg)
		return ECONTEXT;

	STARGS&	sta=*((STARGS *) pStarg);
	if ((NULL == sta.lpfnHcfn) || (!sta.fContEnum))
		return EOK;

	// we need only the FLAGS response
	if (_tcsicmp(lpszFetchRsp, IMAP4_FLAGS) != 0)
		return EOK;

	LPIMAP4MSGFLAGS	pFlags=(LPIMAP4MSGFLAGS) lpModVal;
	if (NULL == pFlags)
		return ENOLOCALBUFFER;

	return (*sta.lpfnHcfn)(ftchData.GetServerConn(), ftchData.GetMsgSeqNo(), *pFlags, sta.pArg, sta.fContEnum);
}

/*--------------------------------------------------------------------------*/

EXC_TYPE imap4StoreMsgsFlagsSync (ISockioInterface&			SBSock,
											 LPCTSTR							lpszITag,	// NULL == auto-generate
											 LPCTSTR							lpszMsgSet,	// NULL/empty == ALL
											 const IMAP4STORECMDFLAGS&	cmdMode,
											 LPCTSTR							lpszStoreFlags,	// may be NULL/empty
											 IMAP4_STORE_HCFN				lpfnHcfn,	// may be NULL
											 LPVOID							pArg,
											 const UINT32					ulRspTimeout)
{
	TCHAR				szCmd[MAX_IMAP4_DATA_LEN+MAX_IMAP4_TAG_LEN+MAX_IMAP4_OPCODE_LEN+2]=_T("");
	CStrlBuilder	strb(szCmd, MAX_IMAP4_DATA_LEN+MAX_IMAP4_TAG_LEN+MAX_IMAP4_OPCODE_LEN);
	LPCTSTR			lpszTag=lpszITag;
	TCHAR				szTag[MAX_IMAP4_TAG_LEN+2]=_T("");
	EXC_TYPE			exc=InitIMAP4Cmd(lpszTag, szIMAP4StoreCmd, cmdMode.m_fIsUID, szTag, MAX_IMAP4_TAG_LEN, strb);
	if (exc != EOK)
		return exc;
	if ((exc=strb.AddChar(_T(' '))) != EOK)
		return exc;

	LPCTSTR	lpszMRange=(IsEmptyStr(lpszMsgSet) ? _T("1:*") : lpszMsgSet);
	if ((exc=strb.AddStr(lpszMRange)) != EOK)
		return exc;
	if ((exc=strb.AddChar(_T(' '))) != EOK)
		return exc;

	if (IMAP4STOREADDMODE == cmdMode.eModCase)
	{
		if ((exc=strb.AddChar(_T('+'))) != EOK)
			return exc;
	}
	else if (IMAP4STOREDELMODE == cmdMode.eModCase)
	{
		if ((exc=strb.AddChar(_T('-'))) != EOK)
			return exc;
	}
	if ((exc=strb.AddStr(IMAP4_FLAGS)) != EOK)
		return exc;

	if (cmdMode.m_fSilent)
	{
		if ((exc=strb.AddStr(IMAP4_SILENT)) != EOK)
			return exc;
	}
	if ((exc=strb.AddChar(_T(' '))) != EOK)
		return exc;

	if (IsEmptyStr(lpszStoreFlags))
	{
		if ((exc=strb.AddStr(_T("()"))) != EOK)
			return exc;
	}
	else
	{
		if ((exc=strb.AddStr(lpszStoreFlags)) != EOK)
			return exc;
	}
	if ((exc=strb.AddCRLF()) != EOK)
		return exc;

	const int	wLen=SBSock.Write(szCmd);
	if (wLen <= 0)
		return ENOTCONNECTION;

	STARGS	sta={ lpfnHcfn, pArg, (NULL != lpfnHcfn) };
	// STORE response is FETCH labeled...
	if ((exc=imap4HandleFetchRspSync(SBSock, lpszTag, storeCfn, (LPVOID) &sta, ulRspTimeout)) != EOK)
		return exc;

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE imap4StoreMsgFlagsSync (ISockioInterface&				SBSock,
											LPCTSTR							lpszITag,	// NULL == auto-generate
											const UINT32					ulMsgID,
											const IMAP4STORECMDFLAGS&	cmdMode,
											LPCTSTR							lpszStoreFlags,	// may be NULL/empty
											IMAP4_STORE_HCFN				lpfnHcfn,	// may be NULL
											LPVOID							pArg,
											const UINT32					ulRspTimeout)
{
	TCHAR	szMsgID[MAX_DWORD_DISPLAY_LENGTH+2]=_T("");
	dword_to_argument(ulMsgID, szMsgID);
	return imap4StoreMsgsFlagsSync(SBSock, lpszITag, szMsgID, cmdMode, lpszStoreFlags, lpfnHcfn, pArg, ulRspTimeout);
}

EXC_TYPE imap4StoreMsgsSync (ISockioInterface&				SBSock,
									  LPCTSTR							lpszITag,	// NULL == auto-generate
									  LPCTSTR							lpszMsgSet,	// NULL/empty == ALL
									  const IMAP4STORECMDFLAGS&	cmdMode,
									  const IMAP4_MSGFLAGS&			flags,
									  IMAP4_STORE_HCFN				lpfnHcfn,	// may be NULL
									  LPVOID								pArg,
									  const UINT32						ulRspTimeout)
{
	TCHAR		szStoreFlags[MAX_IMAP4_FLAGS_ENCLEN+2]=_T("");
	EXC_TYPE	exc=imap4EncodeMsgFlags(&flags, szStoreFlags, MAX_IMAP4_FLAGS_ENCLEN);
	if (exc != EOK)
		return exc;

	if ((exc=imap4StoreMsgsFlagsSync(SBSock, lpszITag, lpszMsgSet, cmdMode, szStoreFlags, lpfnHcfn, pArg, ulRspTimeout)) != EOK)
		return exc;

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

EXC_TYPE imap4CopyMsgsSync (ISockioInterface&	SBSock,
									 LPCTSTR					lpszITag,	// NULL == auto-generate
									 const BOOLEAN			fIsUID,
									 LPCTSTR					lpszMsgSet,
									 LPCTSTR					lpszDstFolder,
									 LPTSTR					lpszRspBuf,
									 const UINT32			ulMaxRspLen,
									 const UINT32			ulRspTimeout)
{
	if (IsEmptyStr(lpszMsgSet))
		return ERANGE;

	return SendIMAP4FolderCmd(SBSock, lpszITag, szIMAP4CopyCmd, fIsUID, lpszMsgSet, lpszDstFolder, imap4OKHCfn, NULL, lpszRspBuf, ulMaxRspLen, ulRspTimeout);
}

EXC_TYPE imap4CopyMsgsSync (ISockioInterface&	SBSock,
									 LPCTSTR					lpszITag,	// NULL == auto-generate
									 const BOOLEAN			fIsUID,
									 LPCTSTR					lpszMsgSet,
									 LPCTSTR					lpszDstFolder,
									 const UINT32			ulRspTimeout)
{
	TCHAR		szRsp[MAX_IMAP4_DATA_LEN+2]=_T("");
	return imap4CopyMsgsSync(SBSock, lpszITag, fIsUID, lpszMsgSet, lpszDstFolder, szRsp, MAX_IMAP4_DATA_LEN, ulRspTimeout);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4AppendSync (ISockioInterface&	SBSock,
								  LPCTSTR				lpszITag,
								  LPCTSTR				lpszFolder,
								  LPCTSTR				lpszFlags,	// may be NULL/empty
								  LPCTSTR				lpszIDate,	// may be NULL/empty
								  const UINT32			ulDataLen,
								  IMAP4_APPDATA_CFN	lpfnDcfn,// data fetch callback
								  LPVOID					pArg,
								  LPTSTR					lpszRspBuf,
								  const UINT32			ulRspBufLen,
								  const UINT32			ulRspTimeout)
{
	if ((NULL == lpszRspBuf) || (ulRspBufLen <= MAX_IMAP4_TAG_LEN))
		return EBADBUFF;
	*lpszRspBuf = _T('\0');

	if (IsEmptyStr(lpszFolder))
		return EPATH;
	if (NULL == lpfnDcfn)
		return EBADADDR;

	CStrlBuilder	strb(lpszRspBuf, ulRspBufLen);
	LPCTSTR			lpszTag=lpszITag;
	TCHAR				szTag[MAX_IMAP4_TAG_LEN+2]=_T("");
	EXC_TYPE			exc=InitIMAP4Cmd(lpszTag, szIMAP4AppendCmd, FALSE, szTag, MAX_IMAP4_TAG_LEN, strb);
	if (exc != EOK)
		return exc;
	if ((exc=strb.AddChar(_T(' '))) != EOK)
		return exc;

	// add folder
	if ((exc=SendIMAP4Folder(SBSock, lpszRspBuf, lpszFolder, ulRspTimeout)) != EOK)
	{
		if (exc != ELITERAL)
			return exc;

		strb.Reset();
	}
	else	// no special characters
	{
		if ((exc=AddIMAP4Folder(strb, lpszFolder)) != EOK)
			return exc;
	}
	if ((exc=strb.AddChar(_T(' '))) != EOK)
		return exc;

	// add (optional) flags
	if (!IsEmptyStr(lpszFlags))
	{
		if ((exc=strb.AddStr(lpszFlags)) != EOK)
			return exc;
		if ((exc=strb.AddChar(_T(' '))) != EOK)
			return exc;
	}

	// add (optional) date
	if (!IsEmptyStr(lpszIDate))
	{
		if ((exc=strb.AddChar(IMAP4_QUOTE_DELIM)) != EOK)
			return exc;
		if ((exc=strb.AddStr(lpszIDate)) != EOK)
			return exc;
		if ((exc=strb.AddChar(IMAP4_QUOTE_DELIM)) != EOK)
			return exc;
		if ((exc=strb.AddChar(_T(' '))) != EOK)
			return exc;
	}

	// add octet count
	if ((exc=strb.AddChar(IMAP4_OCTCNT_SDELIM)) != EOK)
		return exc;
	if ((exc=strb.AddNum(ulDataLen)) != EOK)
		return exc;
	if ((exc=strb.AddChar(IMAP4_OCTCNT_EDELIM)) != EOK)
		return exc;

	// send command
	if ((exc=strb.AddCRLF()) != EOK)
		return exc;
	int	wLen=SBSock.Write(lpszRspBuf);
	if (wLen <= 0)
		return ENOTCONNECTION;

	// make sure continuation requested by server
	if ((exc=WaitForIMAP4Continuation(SBSock, lpszRspBuf, ulRspBufLen, NULL, NULL, ulRspTimeout)) != EOK)
		return exc;

	UINT32	ulAccLen=0;
	for (UINT32	ulBdx=0; ; ulBdx++)
	{
		UINT32	ulReadLen=0;
		if ((exc=(*lpfnDcfn)(pArg, lpszRspBuf, ulRspBufLen, ulReadLen)) != EOK)
		{
			if (EEOF == exc)
				break;
			else
				return exc;
		}

		if ((wLen=SBSock.Write(lpszRspBuf, ulReadLen)) != (int) ulReadLen)
			return ENOTCONNECTION;

		ulAccLen += ulReadLen;
		if (ulReadLen < ulRspBufLen)
			break;
	}

	// send terminating CRLF
	if ((wLen=SBSock.Writeln()) <= 0)
		return ENOTCONNECTION;

	// check response
	for (UINT32 ulRdx=0; ; ulRdx++)
	{
		BOOLEAN	fStripCRLF=FALSE;
		int		rLen=SBSock.ReadCmd(lpszRspBuf, ulRspBufLen, (SINT32) ulRspTimeout, &fStripCRLF);
		if (rLen <= 0)
			return ENOTCONNECTION;
		if (!fStripCRLF)
			return EIOJOB;

		TCHAR		szRT[MAX_IMAP4_TAG_LEN+2]=_T(""), szOp[MAX_IMAP4_OPCODE_LEN+2]=_T("");
		LPCTSTR	lpszNext=NULL;
		exc = imap4ExtractRsp(lpszRspBuf, szRT, MAX_IMAP4_TAG_LEN, szOp, MAX_IMAP4_OPCODE_LEN, &lpszNext);

		if (exc != EOK)
		{
			if (EDATACHAIN == exc)
				continue;
			return exc;
		}

		if (_tcsicmp(szRT, lpszTag) != 0)
			return EILLOGICALRENAME;

		return imap4XlateRspCode(szOp);
	}

	// should not be reached
	return EUNKNOWNEXIT;
}

EXC_TYPE imap4AppendSync (ISockioInterface&	SBSock,
								  LPCTSTR				lpszITag,
								  LPCTSTR				lpszFolder,
								  LPCTSTR				lpszFlags,	// may be NULL/empty
								  const struct tm		*pTM,		// NULL == none
								  const int				tmZone,	// (-1) == use default (only if non NULL date)
								  const UINT32			ulDataLen,
								  IMAP4_APPDATA_CFN	lpfnDcfn,// data fetch callback
								  LPVOID					pArg,
								  LPTSTR					lpszRspBuf,
								  const UINT32			ulRspBufLen,
								  const UINT32			ulRspTimeout)
{
	TCHAR	szIDate[MAX_IMAP4_INTERNALDATE_LEN+2]=_T("");

	// add (optional) date
	if (pTM != NULL)
	{

		EXC_TYPE	exc=EncodeIMAP4InternalDate(pTM, (((-1) == tmZone) ? _timezone : tmZone), szIDate, MAX_IMAP4_INTERNALDATE_LEN);
		if (exc  != EOK)
			return exc;
	}

	return imap4AppendSync(SBSock, lpszITag, lpszFolder, lpszFlags, szIDate, ulDataLen, lpfnDcfn, pArg, lpszRspBuf, ulRspBufLen, ulRspTimeout);
}

EXC_TYPE imap4AppendSync (ISockioInterface&	SBSock,
								  LPCTSTR				lpszITag,
								  LPCTSTR				lpszFolder,
								  LPCTSTR				lpszFlags,	// may be NULL/empty
								  const struct tm		*pTM,		// NULL == none
								  const int				tmZone,	// (-1) == use default (only if non NULL date)
								  const UINT32			ulDataLen,
								  IMAP4_APPDATA_CFN	lpfnDcfn,// data fetch callback
								  LPVOID					pArg,
								  const UINT32			ulRspTimeout)
{
	TCHAR		szRsp[DEFAULT_BUFFSOCK_LEN+4]=_T("");
	return imap4AppendSync(SBSock, lpszITag, lpszFolder, lpszFlags, pTM, tmZone, ulDataLen, lpfnDcfn, pArg, szRsp, DEFAULT_BUFFSOCK_LEN, ulRspTimeout);
}

EXC_TYPE imap4AppendSync (ISockioInterface&	SBSock,
								  LPCTSTR				lpszITag,
								  LPCTSTR				lpszFolder,
								  LPCIMAP4MSGFLAGS	pFlags,	// NULL == empty
								  const struct tm		*pTM,		// NULL == none
								  const int				tmZone,	// (-1) == use default (only if non NULL date)
								  const UINT32			ulDataLen,
								  IMAP4_APPDATA_CFN	lpfnDcfn,// data fetch callback
								  LPVOID					pArg,
								  LPTSTR					lpszRspBuf,
								  const UINT32			ulRspBufLen,
								  const UINT32			ulRspTimeout)
{
	TCHAR		szFlags[MAX_IMAP4_FLAGS_ENCLEN+2]=_T("");

	// add (optional) flags
	if (pFlags != NULL)
	{
		IMAP4_MSGFLAGS	tmpFlags(*pFlags);
		tmpFlags.m_fRecent = 0;	// this flag cannot be appended

		EXC_TYPE	exc=imap4EncodeMsgFlags(&tmpFlags, szFlags, MAX_IMAP4_FLAGS_ENCLEN);
		if (exc != EOK)
			return exc;
	}

	return imap4AppendSync(SBSock, lpszITag, lpszFolder, szFlags, pTM, tmZone, ulDataLen, lpfnDcfn, pArg, lpszRspBuf, ulRspBufLen, ulRspTimeout);
}

EXC_TYPE imap4AppendSync (ISockioInterface&	SBSock,
								  LPCTSTR				lpszITag,
								  LPCTSTR				lpszFolder,
								  LPCIMAP4MSGFLAGS	pFlags,	// NULL == empty
								  const struct tm		*pTM,		// NULL == none
								  const int				tmZone,	// (-1) == use default (only if non NULL date)
								  const UINT32			ulDataLen,
								  IMAP4_APPDATA_CFN	lpfnDcfn,// data fetch callback
								  LPVOID					pArg,
								  const UINT32			ulRspTimeout)
{
	TCHAR		szRsp[DEFAULT_BUFFSOCK_LEN+4]=_T("");
	return imap4AppendSync(SBSock, lpszITag, lpszFolder, pFlags, pTM, tmZone, ulDataLen, lpfnDcfn, pArg, szRsp, DEFAULT_BUFFSOCK_LEN, ulRspTimeout);
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE fprCfn (LPVOID pArg, LPTSTR lpszBuf, const UINT32 ulBufLen, UINT32& ulReadLen)
{
	if ((NULL == pArg) || (NULL == lpszBuf))
		return ECONTEXT;

	ulReadLen = fread((void *) lpszBuf, 1, ulBufLen, (FILE *) pArg);
	return EOK;
}

EXC_TYPE imapAppendFPSync (ISockioInterface&	SBSock,
									LPCTSTR				lpszITag,
									FILE					*fp,
									const UINT32		ulFSize,	// as received from "stat"
									LPCTSTR				lpszFolder,
									LPCIMAP4MSGFLAGS	pFlags,	// NULL == empty
									const struct tm	*pTM,		// NULL == none
									const int			tmZone,	// (-1) == use default (only if non NULL date)
									LPTSTR				lpszRspBuf,
									const UINT32		ulRspBufLen,
									const UINT32		ulRspTimeout)
{
	EXC_TYPE	exc=EOK;

	if ((NULL == fp) || (RFC822MSG_EOM_PATLEN >= ulFSize) || IsEmptyStr(lpszFolder))
		return EPARAM;

	if ((exc=fseek(fp, 0L, SEEK_SET)) != EOK)
		return exc;

	// check if file ending in EOM pattern
	UINT32	ulEffSize=ulFSize;
	if ((exc=fseek(fp, (long) (ulFSize-RFC822MSG_EOM_PATLEN), SEEK_SET)) != EOK)
		return exc;

	TCHAR		szEOMPat[RFC822MSG_EOM_PATLEN+2]=_T("");
	int		rLen=fread(szEOMPat, sizeof(TCHAR), RFC822MSG_EOM_PATLEN, fp);
	if (rLen != RFC822MSG_EOM_PATLEN)
		return EIOSOFT;

	// return to file start
	if ((exc=fseek(fp, 0L, SEEK_SET)) != EOK)
		return exc;

	CRFC822MsgEOM	rme;
	if ((exc=rme.ProcessBuf(szEOMPat, (UINT32) rLen)) != EOK)
		return exc;

	if (rme.IsMsgEOM())
		ulEffSize -= RFC822MSG_EOM_PATLEN;

	return imap4AppendSync(SBSock, lpszITag, lpszFolder, pFlags, pTM, tmZone, ulEffSize, fprCfn, (LPVOID) fp, lpszRspBuf, ulRspBufLen, ulRspTimeout);
}

EXC_TYPE imapAppendFPSync (ISockioInterface&	SBSock,
									LPCTSTR				lpszITag,
									FILE					*fp,
									const UINT32		ulFSize,	// as received from "stat"
									LPCTSTR				lpszFolder,
									LPCIMAP4MSGFLAGS	pFlags,	// NULL == empty
									const struct tm	*pTM,		// NULL == none
									const int			tmZone,	// (-1) == use default (only if non NULL date)
									const UINT32		ulRspTimeout)
{
	TCHAR		szRsp[DEFAULT_BUFFSOCK_LEN+4]=_T("");
	return imapAppendFPSync(SBSock, lpszITag, fp, ulFSize, lpszFolder, pFlags, pTM, tmZone, szRsp, DEFAULT_BUFFSOCK_LEN, ulRspTimeout);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4AppendFileSync (ISockioInterface&	SBSock,
										LPCTSTR				lpszITag,
										LPCTSTR				lpszFolder,
										LPCTSTR				lpszMsgFile,
										LPCIMAP4MSGFLAGS	pFlags,	// NULL == empty
										const struct tm	*pTM,		// NULL == none
										const int			tmZone,	// (-1) == use default (only if non NULL date)
										LPTSTR				lpszRspBuf,
										const UINT32		ulRspBufLen,
										const UINT32		ulRspTimeout)
{
	EXC_TYPE	exc=EOK;

	if (IsEmptyStr(lpszFolder) || IsEmptyStr(lpszMsgFile))
		return EPATHNAMESYNTAX;

	struct _stat	fst;
	if ((exc=_tstat(lpszMsgFile, &fst)) != EOK)
		return exc;

	FILE				*fp=_tfopen(lpszMsgFile, _T("rb"));
	CFilePtrGuard	fpg(fp);
	if (NULL == fp)
		return EFNEXIST;

	return imapAppendFPSync(SBSock, lpszITag, fp, fst.st_size, lpszFolder, pFlags, pTM, tmZone, lpszRspBuf, ulRspBufLen, ulRspTimeout);
}

EXC_TYPE imap4AppendFileSync (ISockioInterface&	SBSock,
										LPCTSTR				lpszITag,
										LPCTSTR				lpszFolder,
										LPCTSTR				lpszMsgFile,
										LPCIMAP4MSGFLAGS	pFlags,	// NULL == empty
										const struct tm	*pTM,		// NULL == none
										const int			tmZone,	// (-1) == use default (only if non NULL date)
										const UINT32		ulRspTimeout)
{
	TCHAR		szRsp[DEFAULT_BUFFSOCK_LEN+4]=_T("");
	return imap4AppendFileSync(SBSock, lpszITag, lpszFolder, lpszMsgFile, pFlags, pTM, tmZone, szRsp, DEFAULT_BUFFSOCK_LEN, ulRspTimeout);
}

/*---------------------------------------------------------------------------*/
