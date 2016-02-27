#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#include <_types.h>

#include <util/errors.h>
#include <util/string.h>

#include <comm/socket.h>

#include <internet/general.h>
#include <internet/pop3Lib.h>

/*---------------------------------------------------------------------------*/

/* standard POP3 response headers */
#ifdef __cplusplus
#define SZXTRN extern
#else
#define SZXTRN
#endif

SZXTRN const TCHAR POP3_OK[]=_T("+OK"), POP3_ERR[]=_T("-ERR");

/*---------------------------------------------------------------------------*/

SZXTRN const TCHAR szPOP3UserCmd[]=_T("USER");
SZXTRN const TCHAR szPOP3PassCmd[]=_T("PASS");
SZXTRN const TCHAR szPOP3TopCmd[]=_T("TOP");
SZXTRN const TCHAR szPOP3RetrCmd[]=_T("RETR");
SZXTRN const TCHAR szPOP3ListCmd[]=_T("LIST");
SZXTRN const TCHAR szPOP3LastCmd[]=_T("LAST");
SZXTRN const TCHAR szPOP3UidlCmd[]=_T("UIDL");
SZXTRN const TCHAR szPOP3DeleCmd[]=_T("DELE");
SZXTRN const TCHAR szPOP3RsetCmd[]=_T("RSET");
SZXTRN const TCHAR szPOP3ApopCmd[]=_T("APOP");
SZXTRN const TCHAR szPOP3StatCmd[]=_T("STAT");
SZXTRN const TCHAR szPOP3NoopCmd[]=_T("NOOP");
SZXTRN const TCHAR szPOP3HelpCmd[]=_T("HELP");
SZXTRN const TCHAR szPOP3QuitCmd[]=_T("QUIT");

/*---------------------------------------------------------------------------*/

/* NULL terminated list of all standard POP3 command */
SZXTRN LPCTSTR szPOP3Cmds[]={
	szPOP3UserCmd,
	szPOP3PassCmd,
	szPOP3TopCmd,
	szPOP3RetrCmd,
	szPOP3ListCmd,
	szPOP3LastCmd,
	szPOP3UidlCmd,
	szPOP3DeleCmd,
	szPOP3RsetCmd,
	szPOP3ApopCmd,
	szPOP3StatCmd,
	szPOP3NoopCmd,
	szPOP3HelpCmd,
	szPOP3QuitCmd,

	NULL
};

/*---------------------------------------------------------------------------*/

/* returns EOK if "+OK", ESTATE if "-ERR" and E??? otherwise */
EXC_TYPE pop3_xlate_rsp (LPCTSTR lpszRsp)
{
	if (IsEmptyStr(lpszRsp))
		return EEMPTYENTRY;

	if (_tcsstr(lpszRsp, POP3_OK) == lpszRsp)
		return EOK;
	else if (_tcsstr(lpszRsp, POP3_ERR) == lpszRsp)
		return ESTATE;

	return ECONTEXT;
}

/*---------------------------------------------------------------------------*/

/*		Sends the command (with arguments - if any) and reads the OK/ERR response
 * (using the timeout). Returns non-EOK if problem encountered (e.g. returns
 * ENOTCONNECTION if network error).
 */
EXC_TYPE pop3_clnt_cmd_sync (ISockioInterface&	CBSock,
									  const char			cmd[],
									  const char			arg1[],	/* may be NULL */
									  const char			arg2[],	/* ditto */
									  char					rsp[],
									  const UINT32			uRspLen,
									  const UINT32			uRspTimeout)
{
	SINT32	sLen=(-1);
	BOOLEAN	fStrippedCRLF=FALSE;

	if ((NULL == cmd) || (NULL == rsp))
		return EBADBUFF;
	*rsp = _T('\0');

	if (NULL == arg1)
		sLen = CBSock.WriteCmdf("%s\r\n", cmd);
	else if (NULL == arg2)
		sLen = CBSock.WriteCmdf("%s %s\r\n", cmd, arg1);
	else
		sLen = CBSock.WriteCmdf("%s %s %s\r\n", cmd, arg1, arg2);
	if (sLen <= 0)
		return ENOTCONNECTION;

	if ((sLen=CBSock.ReadCmd(rsp, uRspLen, uRspTimeout, &fStrippedCRLF)) <= 0)
		return ENOTCONNECTION;

	if (!fStrippedCRLF)
		return EOVERFLOW;

	return pop3_xlate_rsp(rsp);
}

/*---------------------------------------------------------------------------*/

/* performs the USER/PASS authentication */
EXC_TYPE pop3_clnt_auth (ISockioInterface&	CBSock,
								 LPCTSTR					lpszUserName,
								 LPCTSTR					lpszPasswd,
								 LPTSTR					lpszRspBuf,
								 const UINT32			ulBufLen,
								 const UINT32			uRspTimeout)
{
	EXC_TYPE	exc=EOK;

	if (IsEmptyStr(lpszUserName) || IsEmptyStr(lpszPasswd))
		return EPARAM;

	exc = pop3_clnt_cmd_sync(CBSock,szPOP3UserCmd,lpszUserName,NULL,lpszRspBuf,ulBufLen,uRspTimeout);
	if (exc != EOK)
		return exc;

	exc = pop3_clnt_cmd_sync(CBSock,szPOP3PassCmd,lpszPasswd,NULL,lpszRspBuf,ulBufLen,uRspTimeout);
	if (exc != EOK)
		return exc;

	return EOK;
}

EXC_TYPE pop3_clnt_auth (ISockioInterface&	CBSock,
								 LPCTSTR					lpszUserName,
								 LPCTSTR					lpszPasswd,
								 const UINT32			uRspTimeout)
{
	TCHAR	cmdRsp[POP3_MAX_RSP_LEN+2]=_T("");
	return pop3_clnt_auth(CBSock, lpszUserName, lpszPasswd, cmdRsp, POP3_MAX_RSP_LEN, uRspTimeout);
}

/*---------------------------------------------------------------------------*/

/* retrieves the specified message - calls the callback functionon input */
EXC_TYPE pop3_clnt_retr (ISockioInterface&	CBSock,
								 const UINT32			msgNum,
								 const SINT32			linesNum,
								 POP3_CHNDL_CFN		lpfnHcfn,
								 void						*pArg,
								 const UINT32			uRspTimeout)
{
	TCHAR		iBuf[POP3_MAX_LINE_LENGTH+6]=_T(""), tArg[24]=_T("");
	EXC_TYPE	exc=EOK;
	BOOLEAN	fCallCfn=TRUE, fBodyPart=FALSE;

	if (NULL == lpfnHcfn)
		return EBADADDR;

	dword_to_argument(msgNum, tArg);
	if (linesNum != POP3_ALL_LINES)
	{
		TCHAR	lArg[MAX_DWORD_DISPLAY_LENGTH+2]=_T("");

		dword_to_argument((DWORD) linesNum, lArg);
		exc = pop3_clnt_cmd_sync(CBSock,szPOP3TopCmd,tArg,lArg,iBuf,POP3_MAX_LINE_LENGTH,uRspTimeout);
	}
	else
	{
		exc = pop3_clnt_cmd_sync(CBSock,szPOP3RetrCmd,tArg,NULL,iBuf,POP3_MAX_LINE_LENGTH,uRspTimeout);
	}
	if (exc != EOK)
		return exc;

	for (BOOLEAN fContLine=FALSE; ; )
	{
		BOOLEAN	fStrippedCRLF=FALSE;
		LPTSTR	lpszReadBuf=(fContLine ? &iBuf[1] : iBuf);
		int		rLen=CBSock.ReadCmd(lpszReadBuf, POP3_MAX_LINE_LENGTH, uRspTimeout, &fStrippedCRLF);
		if (rLen < 0)
			return ENOTCONNECTION;
		lpszReadBuf[POP3_MAX_LINE_LENGTH] = _T('\0');

		/* break a long line into several - using a preceding space in order to avoid special conditions */
		if (fContLine)
		{
			iBuf[0] = _T(' ');
			rLen++;
		}

		fContLine = (!fStrippedCRLF);

		if (_tcsicmp(iBuf, _T(".")) == 0)
		{
			/* if last line, call once again to let application know about it */
			if (fCallCfn)
				fCallCfn = (*lpfnHcfn)(msgNum, linesNum, iBuf, rLen, pArg);
			break;
		}

		if (fCallCfn)
		{
			/*		Stop calling the callback after 1st blank line - which signals start of
			 * body - if zero lines requested from body (i.e. "TOP n 0" command).
			 *
			 * Note: for some reason, some lines from body are transmitted even in this
			 *			case - e.g. Exchange server does this for "multipart/alternative" MIME
			 *			types sometimes...
			 */

			if ((fBodyPart && (linesNum != 0)) || (!fBodyPart))
				fCallCfn = (*lpfnHcfn)(msgNum, linesNum, iBuf, rLen, pArg);
		}

		if (0 == rLen)
			fBodyPart = TRUE;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE extract_number (LPCTSTR *lppszBuf, UINT32 *pulNum)
{
	LPCTSTR	tsp=NULL, bp=NULL;
	EXC_TYPE	exc=EOK;

	if ((NULL == lppszBuf) || (NULL == pulNum))
		return EPARAM;

	if (NULL == (tsp=(*lppszBuf)))
		return EPARAM;

	/* find number start */
	for ( ; (!_istdigit(*tsp)) && (*tsp != _T('\0')); tsp++);

	/* if no digit found, then invalid response */
	if (_T('\0') == *tsp)
		return ECONTEXT;

	for (bp = tsp; _istdigit(*tsp) && (*tsp != _T('\0')); tsp++);	/* find end of number */

	*pulNum = argument_to_dword(bp, (tsp - bp), EXC_ARG(exc));

	if (*tsp != _T('\0'))
		*lppszBuf = (tsp + 1); /* prepare to continue */
	else
		*lppszBuf = tsp;
	return exc;
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE extract_argument (LPCTSTR	*lppszBuf, LPCTSTR *lppszArg, UINT32 *pulArgLen)
{
	LPCTSTR	tsp=NULL;

	if ((NULL == lppszBuf) || (NULL == lppszArg) || (NULL == pulArgLen))
		return EPARAM;

	*lppszArg = NULL;
	*pulArgLen = 0;

	if (NULL == (tsp=(*lppszBuf)))
		return EPARAM;

	for ( ; _istspace(*tsp) && (*tsp != _T('\0')); tsp++);
	if (_T('\0') == *tsp)	/* all spaces -> no UIDL */
		return ECONTEXT;

	for (*lppszArg = tsp; (!_istspace(*tsp)) && (*tsp != _T('\0')); tsp++);
	*pulArgLen = (tsp - (*lppszArg));

	if (*tsp != _T('\0'))
		*lppszBuf = (tsp + 1); /* prepare to continue */
	else
		*lppszBuf = tsp;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE ExtractPOP3StatResponse (LPCTSTR	lpszStatRsp,
											 UINT32	*pulMsgsNum,
											 UINT32	*pulMboxSize)
{
	LPCTSTR	tsp=NULL;
	EXC_TYPE	exc=EOK;

	if (IsEmptyStr(lpszStatRsp) || (NULL == pulMsgsNum) || (NULL == pulMboxSize))
		return EPARAM;

	*pulMsgsNum = 0;
	*pulMboxSize = 0;

	/* make sure this is an "+OK" response */
	if ((tsp=_tcsstr(lpszStatRsp, POP3_OK)) != lpszStatRsp)
		return pop3_xlate_rsp(lpszStatRsp);

	/* skip "+OK" response */
	for ( ; (!_istspace(*tsp)) && (*tsp != _T('\0')); tsp++);

	if ((exc=extract_number(&tsp, pulMsgsNum)) != EOK)
		return exc;
	if ((exc=extract_number(&tsp, pulMboxSize)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

/* returns the mailbox status */
EXC_TYPE pop3_clnt_stat (ISockioInterface&	CBSock,
								 UINT32					*pulMsgsNum,
								 UINT32					*pulMboxSize,
								 const UINT32			uRspTimeout)
{
	TCHAR		cmdRsp[POP3_MAX_RSP_LEN+4]=_T("");
	EXC_TYPE	exc=pop3_clnt_cmd_sync(CBSock, szPOP3StatCmd, NULL, NULL, cmdRsp, POP3_MAX_RSP_LEN, uRspTimeout);
	if (exc != EOK)
		return exc;

	return ExtractPOP3StatResponse(cmdRsp, pulMsgsNum, pulMboxSize);
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE pop3_clnt_dual_multi (ISockioInterface&	CBSock,
												  const char			cmd[],
												  POP3_CHNDL_CFN		lpfnHcfn,
												  void					*pArg,
												  const UINT32			uRspTimeout)
{
	TCHAR		iBuf[POP3_MAX_RSP_LEN+4]=_T("");
	EXC_TYPE	exc=EOK;
	UINT32	ulMLx=0;
	BOOLEAN	fCallCfn=TRUE;

	if (NULL == lpfnHcfn)
		return EPARAM;

	exc = pop3_clnt_cmd_sync(CBSock, cmd, NULL, NULL, iBuf, POP3_MAX_RSP_LEN, uRspTimeout);
	if (exc != EOK)
		return exc;

	for (ulMLx=0 ; ; ulMLx++)
	{
		BOOLEAN	fStrippedCRLF=FALSE;
		int		rLen=CBSock.ReadCmd(iBuf, POP3_MAX_RSP_LEN, uRspTimeout, &fStrippedCRLF);
		if (rLen < 0)
			return ENOTCONNECTION;

		if (!fStrippedCRLF)
			return EOVERFLOW;

		if (_tcsicmp(iBuf, _T(".")) == 0)
			break;

		if (fCallCfn)
		{
			UINT32	msgNum=0, bLen=0;;
			LPCTSTR	tsp=iBuf, bp=NULL;
			EXC_TYPE	err=extract_number(&tsp, &msgNum);

			/*		At this stage, "tsp" points to first character AFTER the
			 * space which separates the msg number from the UIDL.
			 */
			if (EOK == err)
				err = extract_argument(&tsp, &bp, &bLen);

			if (EOK == err)
				fCallCfn = (*lpfnHcfn)(msgNum, POP3_ALL_LINES, bp, bLen, pArg);
			else
				exc = err;
		}
	}

	return exc;
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE pop3_clnt_dual_singl (ISockioInterface&	CBSock,
												  const UINT32			msgNum,
												  const char			cmd[],
												  POP3_CHNDL_CFN		lpfnHcfn,
												  void					*pArg,
												  const UINT32			uRspTimeout)
{
	TCHAR		cmdRsp[POP3_MAX_RSP_LEN+4]=_T(""), tArg[MAX_DWORD_DISPLAY_LENGTH+2]=_T("");
	LPCTSTR	tsp=cmdRsp, bp=NULL;
	UINT32	rspNum=POP3_ALL_MSGS, bLen=0;
	EXC_TYPE	exc=EOK;
	BOOLEAN	fCallCfn=TRUE;

	if ((POP3_ALL_MSGS == msgNum) || (NULL == lpfnHcfn))
		return EPARAM;

	dword_to_argument(msgNum, tArg);
	exc = pop3_clnt_cmd_sync(CBSock, cmd, tArg, NULL, cmdRsp, POP3_MAX_RSP_LEN, uRspTimeout);
	if (exc != EOK)
		return exc;

	if ((exc=extract_number(&tsp, &rspNum)) != EOK)
		return exc;

	/* check that response msg number matches requested one */
	if (rspNum != msgNum)
		return EINVAL;

	if ((exc=extract_argument(&tsp, &bp, &bLen)) != EOK)
		return exc;

	fCallCfn = (*lpfnHcfn)(msgNum, POP3_ALL_LINES, bp, bLen, pArg);
	return exc;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE pop3_clnt_msg_uidl (ISockioInterface&	CBSock,
									  const UINT32			msgNum,
									  LPTSTR					lpszUIDL,
									  const UINT32			ulMaxLen,
									  const UINT32			uRspTimeout)
{
	TCHAR		szRsp[MAX_POP3MSGUIDL_RSPLEN+2]=_T("");
	LPCTSTR	lsp=szRsp, tsp=NULL;
	TCHAR		tArg[MAX_DWORD_DISPLAY_LENGTH+2]=_T("");
	EXC_TYPE	exc=EOK;
	UINT32	ulVLen=0;

	if ((0 == msgNum) || (NULL == lpszUIDL) || (0 == ulMaxLen))
		return EPARAM;
	*lpszUIDL = _T('\0');

	dword_to_argument(msgNum, tArg);
	exc = pop3_clnt_cmd_sync(CBSock, szPOP3UidlCmd, tArg, NULL, szRsp, MAX_POP3MSGUIDL_RSPLEN, uRspTimeout);
	if (exc != EOK)
		return exc;

	// skip +OK response
	for (lsp=szRsp; (!_istspace(*lsp)) && (*lsp != _T('\0')); lsp++);

	if ((exc=extract_number(&lsp, &ulVLen)) != EOK)
		return exc;
	if (ulVLen != msgNum)
		return EILLEGALOPCODE;

	if ((exc=extract_argument(&lsp, &tsp, &ulVLen)) != EOK)
		return exc;
	if (0 == ulVLen)
		return EFRAGMENTATION;
	if (ulVLen >= ulMaxLen)
		return EOVERFLOW;

	_tcsncpy(lpszUIDL, tsp, ulVLen);
	lpszUIDL[ulVLen] = _T('\0');
	return EOK;
}

/*---------------------------------------------------------------------------*/

/* calls the callback for each UIDL response - in this case:
 *
 *		msgNum - is the msg number for which the UIDL is called.
 *		linesNum - set to POP3_ALL_LINES
 *		iBuf - contains the UIDL
 *
 *	If a message nummber other than POP3_ALL_MSGS is supplied, then the callback
 * is called only once (for this message).
 *
 * Note: the callback may be called several time successfully, and the routine
 *			might still return non-EOK responses.
 */
EXC_TYPE pop3_clnt_uidl (ISockioInterface&	CBSock,
								 const UINT32			msgNum,
								 POP3_CHNDL_CFN		lpfnHcfn,
								 void						*pArg,
								 const UINT32			uRspTimeout)
{
	if (POP3_ALL_MSGS == msgNum)
		return pop3_clnt_dual_multi(CBSock, szPOP3UidlCmd, lpfnHcfn, pArg, uRspTimeout);
	else
		return pop3_clnt_dual_singl(CBSock, msgNum, szPOP3UidlCmd, lpfnHcfn, pArg, uRspTimeout);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE pop3_clnt_msg_list (ISockioInterface&	CBSock,
									  const UINT32			msgNum,
									  UINT32&				ulMsgSize,
									  const UINT32			uRspTimeout)
{
	TCHAR		szRsp[MAX_POP3MSGLIST_RSPLEN+2]=_T("");
	LPCTSTR	lsp=szRsp;
	TCHAR		tArg[MAX_DWORD_DISPLAY_LENGTH+2]=_T("");
	EXC_TYPE	exc=EOK;
	UINT32	ulVLen=0;

	ulMsgSize = 0;

	if (0 == msgNum)
		return EPARAM;

	dword_to_argument(msgNum, tArg);
	exc = pop3_clnt_cmd_sync(CBSock, szPOP3ListCmd, tArg, NULL, szRsp, MAX_POP3MSGLIST_RSPLEN, uRspTimeout);
	if (exc != EOK)
		return exc;

	// skip +OK response
	for (lsp=szRsp; (!_istspace(*lsp)) && (*lsp != _T('\0')); lsp++);

	if ((exc=extract_number(&lsp, &ulVLen)) != EOK)
		return exc;
	if (ulVLen != msgNum)
		return EILLEGALOPCODE;

	if ((exc=extract_number(&lsp, &ulMsgSize)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

/*		Calls the callback for each LIST response (if suppl - in this case:
 *
 *		msgNum - is the msg number for which the LIST is called.
 *		linesNum - set to POP3_ALL_LINES
 *		iBuf - contains the msg size
 *
 *	If a message nummber other than POP3_ALL_MSGS is supplied, then the callback
 * is called only once (for this message).
 *
 * Note: the callback may be called several time successfully, and the routine
 *			might still return non-EOK responses.
 */
EXC_TYPE pop3_clnt_list (ISockioInterface&	CBSock,
								 const UINT32			msgNum,
								 POP3_CHNDL_CFN		lpfnHcfn,
								 void						*pArg,
								 const UINT32			uRspTimeout)
{
	if (POP3_ALL_MSGS == msgNum)
		return pop3_clnt_dual_multi(CBSock, szPOP3ListCmd, lpfnHcfn, pArg, uRspTimeout);
	else
		return pop3_clnt_dual_singl(CBSock, msgNum, szPOP3ListCmd, lpfnHcfn, pArg, uRspTimeout);
}

/*---------------------------------------------------------------------------*/

typedef struct {
	EXC_TYPE					m_exc;
	POP3_HDENM_CFN			m_lpfnEcfn;
	void						*m_pArg;
	LPCRFC822HDRPARSER	m_pHdrParser;
	BOOLEAN					m_fStopParsing;
} HDENMARGS, *LPHDENMARGS;

/*---------------------------------------------------------------------------*/

static BOOLEAN ehd_cfn (const UINT32	msgNum,				/* requested ID */
								const SINT32	/* linesNum */,	/* requested num */
								const char		iBuf[],				/* read buffer */
								const UINT32	iLen,					/* valid datalen */
								void				*pArg)				/* caller arg */
{
	EXC_TYPE					exc=EOK;
	LPHDENMARGS				peArgs=(LPHDENMARGS) pArg;
	POP3_HDENM_CFN			lpfnEcfn=NULL;
	LPCRFC822HDRPARSER	pHdrParser=NULL;
	LPCTSTR					lpszHdrName=NULL, lpszHdrValue=NULL;
	BOOLEAN					fRetVal=FALSE, fIsContHdr=FALSE;

	if ((0 == msgNum) || ((NULL == iBuf) && (iLen != 0)) || (NULL == pArg))
	{
		exc = ECONTEXT;
		goto Quit;
	}

	if ((NULL == (lpfnEcfn=peArgs->m_lpfnEcfn)) ||
		 (NULL == (pHdrParser=peArgs->m_pHdrParser)))
	{
		exc = ENOLOADERMEM;
		goto Quit;
	}

	if (0 == iLen)
		return TRUE;

	/* skip end of headers */
	if ((1 == iLen) && (_T('.') == iBuf[0]))
		return (*lpfnEcfn)(msgNum, szXBody, iBuf, FALSE, peArgs->m_pArg);

	if (peArgs->m_fStopParsing)
		return TRUE;

	if ((exc=pHdrParser->ProcessHdr(iBuf)) != EOK)
	{
		/* if immediate MIME boundary detected stop parsing headers */
		if (ESTREAMSPECIAL == exc)
		{
			peArgs->m_fStopParsing = TRUE;
			return TRUE;
		}

		goto Quit;
	}

	lpszHdrName = pHdrParser->GetHdrName();
	lpszHdrValue = pHdrParser->GetHdrValue();
	fIsContHdr = pHdrParser->IsContHdr();

	exc = EOK;
	fRetVal = (*lpfnEcfn)(msgNum, lpszHdrName, lpszHdrValue, fIsContHdr, peArgs->m_pArg);

Quit:
	if ((exc != EOK) && (peArgs != NULL))
		peArgs->m_exc = exc;

	return fRetVal;
}

/*---------------------------------------------------------------------------*/

/*
 * Calls the callback for each header - msg number may be POP3_ALL_MSGS
 */
EXC_TYPE pop3_enum_hdrs (ISockioInterface&	CBSock,
								 const UINT32			ulMsgNum,
								 POP3_HDENM_CFN		lpfnEcfn,
								 void						*pArg,
								 const UINT32			uRspTimeout)
{
	HDENMARGS			hdeArgs;
	CRFC822HdrParser	hdrParser;
	UINT32				ulNumMsgs=0, ulMboxSize=0, ulMsgID=0;
	EXC_TYPE				exc=EOK;

	if (POP3_ALL_MSGS == ulMsgNum)
	{
		if ((exc=pop3_clnt_stat(CBSock, &ulNumMsgs, &ulMboxSize, uRspTimeout)) != EOK)
			return exc;

		ulMsgID = 1;
	}
	else	/* specific msg requested */
	{
		ulMsgID = ulMsgNum;
		ulNumMsgs = ulMsgNum;
	}

	if (NULL == lpfnEcfn)
		return EPARAM;

	memset(&hdeArgs, 0, (sizeof hdeArgs));
	hdeArgs.m_lpfnEcfn = lpfnEcfn;
	hdeArgs.m_pArg = pArg;
	hdeArgs.m_pHdrParser = &hdrParser;

	for (; ulMsgID <= ulNumMsgs; ulMsgID++)
	{
		hdrParser.Reset();

		if ((exc=pop3_clnt_ghdrs(CBSock, ulMsgID, ehd_cfn, (void *) &hdeArgs, uRspTimeout)) != EOK)
			return exc;

		if ((exc=hdeArgs.m_exc) != EOK)
			return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

/* marks requested message for deletion */
EXC_TYPE pop3_clnt_dele (ISockioInterface&	CBSock,
								 const UINT32			msgNum,
								 const UINT32			uRspTimeout)
{
	TCHAR cmdRsp[POP3_MAX_RSP_LEN+4]=_T(""), tArg[MAX_DWORD_DISPLAY_LENGTH+2]=_T("");

	dword_to_argument(msgNum, tArg);
	return pop3_clnt_cmd_sync(CBSock,szPOP3DeleCmd,tArg,NULL,cmdRsp,POP3_MAX_RSP_LEN,uRspTimeout);
}

/*---------------------------------------------------------------------------*/

/* sends a command for which only OK/ERROR is expected (i.e. no information) */
EXC_TYPE pop3_clnt_simpl_cmd (ISockioInterface& CBSock,
										const char			cmd[],
										const UINT32		uRspTimeout)
{
	TCHAR	cmdRsp[POP3_MAX_RSP_LEN+4]=_T("");

	return pop3_clnt_cmd_sync(CBSock,cmd,NULL,NULL,cmdRsp,POP3_MAX_RSP_LEN,uRspTimeout);
}

/*---------------------------------------------------------------------------*/

/* quits the session (Note: closes the socket !!!) */
EXC_TYPE pop3_clnt_quit (ISockioInterface& CBSock, const UINT32 uRspTimeout)
{
	TCHAR		cmdRsp[POP3_MAX_RSP_LEN+4]=_T("");
	EXC_TYPE exc=pop3_clnt_simpl_cmd(CBSock, szPOP3QuitCmd, uRspTimeout);
	// dummy read to ensure proper closure (only if got OK response)
	int		rLen=(-1);
	if (EOK == exc)
		rLen = CBSock.ReadCmd(cmdRsp, POP3_MAX_RSP_LEN, uRspTimeout);
	CBSock.Close();

	// should not happen, since other side is supposed to close the connection
	if (rLen >= 0)
		// do another read just to ensure proper closure (again)
		rLen = CBSock.ReadCmd(cmdRsp, POP3_MAX_RSP_LEN, uRspTimeout);

	return exc;
}

/* send a "no-operation" - can be used to check that connections is alive */
EXC_TYPE pop3_clnt_noop (ISockioInterface& CBSock, const UINT32 uRspTimeout)
{
	return pop3_clnt_simpl_cmd(CBSock, szPOP3NoopCmd, uRspTimeout);
}

/*---------------------------------------------------------------------------*/

/* connects to the POP3 server and reads the initial "welcome" response */
EXC_TYPE pop3_clnt_connect (ISockioInterface&	CBSock,
									 LPCTSTR					lpszHost,
									 const int				iPort,		/* if 0 then use default */
									 LPTSTR					lpszRspBuf,
									 const UINT32			ulBufLen,
									 const UINT32			uRspTimeout)
{
	EXC_TYPE exc=EOK;
	int		rLen=(-1), iConnPort=iPort;
	BOOLEAN	fStrippedCRLF=FALSE;

	if (iConnPort <= 0)
		iConnPort = port_string2value(_T("pop3"));

	if ((exc=CBSock.Connect(lpszHost, ((iConnPort <= 0) ? IPPORT_POP3 : iConnPort))) != EOK)
		return exc;

	if ((rLen=CBSock.ReadCmd(lpszRspBuf, ulBufLen, uRspTimeout, &fStrippedCRLF)) <= 0)
	{
		CBSock.Close();
		return ENOTCONNECTION;
	}

	if (!fStrippedCRLF)
	{
		CBSock.Close();
		return EOVERFLOW;
	}

	if ((exc=pop3_xlate_rsp(lpszRspBuf)) != EOK)
	{
		CBSock.Close();
		return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

/* connects to the POP3 server and reads the initial "welcome" response */
EXC_TYPE pop3_clnt_connect (ISockioInterface&	CBSock,
									 LPCTSTR					lpszHost,
									 const int				iPort,		/* if 0 then use default */
									 const UINT32			uRspTimeout)
{
	TCHAR		welcomeRsp[POP3_MAX_RSP_LEN+4]=_T("");
	return pop3_clnt_connect(CBSock, lpszHost, iPort, welcomeRsp, (POP3_MAX_RSP_LEN+2), uRspTimeout);
}

/*---------------------------------------------------------------------------*/

typedef struct tag_list_popl_arg {
		POP3_MSG_INFO	*pInfo;
		UINT32			nMsgs;
		UINT32			cMsgs;
} POPL_ARG;

static BOOLEAN pop3_clnt_list_populate (const UINT32	msgNum,
													 const SINT32	/* linesNum */,
													 const char		iBuf[],
													 const UINT32	iLen,
													 void				*pArg)
{
	POPL_ARG			*pPopl=(POPL_ARG *) pArg;
	POP3_MSG_INFO	*pInfo=NULL;
	EXC_TYPE			exc=EOK;

	if ((NULL == iBuf) || (NULL == pArg))
		return FALSE;

	/* no more room for message info - skip listing */
	if (pPopl->cMsgs >= pPopl->nMsgs)
		return FALSE;
	if (NULL == (pInfo=pPopl->pInfo))
		return FALSE;

	pInfo->msgNum = msgNum;
	pInfo->msgSize = argument_to_dword(iBuf, iLen, EXC_ARG(exc));
	if (exc != EOK) return TRUE;

	(pPopl->cMsgs)++;
	(pPopl->pInfo)++;
	return TRUE;
}

/*---------------------------------------------------------------------------*/

/*		Returns the messages info (up to specified limit) - returns
 * actual number of message numbers/IDs.
 *
 * Note: there is no direct way to distinguish between an EXACT population of
 *			the IDs array, and an overflow, since the routine stops populating
 *			the list once the limit is reached. Application should first use the
 *			"stat" routine in order to find out how much space to allocate.
 */

EXC_TYPE pop3_clnt_get_msgs_info (ISockioInterface&	CBSock,
											 POP3_MSG_INFO			*pInfo,
											 const UINT32			nMsgs,
											 UINT32					*pnMsgs,
											 const UINT32			uRspTimeout)
{
	POPL_ARG popl;
	EXC_TYPE	exc=EOK;

	if ((NULL == pInfo) || (NULL == pnMsgs))
		return EPARAM;
	*pnMsgs = 0;

	if (0 == nMsgs) return EOK;
	memset(pInfo, 0, nMsgs * (sizeof *pInfo));

	memset(&popl, 0, (sizeof popl));
	popl.pInfo = pInfo;
	popl.nMsgs = nMsgs;
	popl.cMsgs = 0;

	exc = pop3_clnt_list(CBSock, POP3_ALL_MSGS,
								pop3_clnt_list_populate, (void *) &popl,
								uRspTimeout);

	*pnMsgs = popl.cMsgs;
	return exc;
}

/*---------------------------------------------------------------------------*/

static POP3_EXTMSG_INFO *find_uidl_info_by_num (POP3_EXTMSG_INFO	*pInfoArr,
																const UINT32		iNum,
																const UINT32		msgNum)
{
	POP3_EXTMSG_INFO	*pInfo=pInfoArr;
	UINT32				uIdx=0;

	if (NULL == pInfo) return NULL;

	for ( ; uIdx < iNum; uIdx++, pInfo++)
		if (pInfo->msgNum == msgNum) return pInfo;

	return NULL;
}

/*---------------------------------------------------------------------------*/

typedef struct tag_uidl_popl_arg {
		POP3_EXTMSG_INFO	*pInfo;
		UINT32				nMsgs;
		UINT32				cMsgs;
		BOOLEAN				fGetUIDL;
		BOOLEAN				fGetSize;
} UIDL_ARG;

static BOOLEAN pop3_clnt_uidl_populate (const UINT32	msgNum,
													 const SINT32	/* linesNum */,
													 const char		iBuf[],
													 const UINT32	iLen,
													 void				*pArg)
{
	UIDL_ARG				*pPopl=(UIDL_ARG *) pArg;
	POP3_EXTMSG_INFO	*pInfo=NULL;
	EXC_TYPE				exc=EOK;

	if ((NULL == iBuf) || (NULL == pArg))
		return FALSE;

	if (pPopl->fGetSize)
	{
		if (pPopl->fGetUIDL)
		{
			/* if msg not found skip it */
			if (NULL == (pInfo=find_uidl_info_by_num(pPopl->pInfo, pPopl->cMsgs, msgNum)))
				return TRUE;
		}
		else
		{
			/* no more room for message info - skip listing */
			if ((pPopl->cMsgs >= pPopl->nMsgs) || (NULL == (pInfo=pPopl->pInfo)))
				return FALSE;
		}

		pInfo->msgNum = msgNum;
		pInfo->msgSize = argument_to_dword(iBuf, iLen, EXC_ARG(exc));
		if (exc != EOK)
			pInfo->msgSize = 0;

		if (!(pPopl->fGetUIDL))
		{
			(pPopl->cMsgs)++;
			(pPopl->pInfo)++;
		}
	}
	else
	{
		/* no more room for message info - skip listing */
		if ((pPopl->cMsgs >= pPopl->nMsgs) || (NULL == (pInfo=pPopl->pInfo)))
			return FALSE;

		pInfo->msgNum = msgNum;

		/* skip if msg UIDL too long */
		if (iLen >= (sizeof pInfo->msgUIDL)) return TRUE;

		strcpy(pInfo->msgUIDL, iBuf);
		(pPopl->cMsgs)++;
		(pPopl->pInfo)++;
	}

	return TRUE;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE pop3_clnt_get_msgs_full_info (ISockioInterface&	CBSock,
													POP3_EXTMSG_INFO	*pInfo,
													const UINT32		nMsgs,
													const BOOLEAN		fGetUIDL,
													const BOOLEAN		fGetSize,
													UINT32				*pnMsgs,
													const UINT32		uRspTimeout)
{
	UIDL_ARG	uArg;
	EXC_TYPE	exc=EOK;

	if ((NULL == pInfo) || (NULL == pnMsgs))
		return EPARAM;
	*pnMsgs = 0;

	/* caller must request something... */
	if ((!fGetSize) && (!fGetUIDL))
		return ELOADERSUPPORT;

	if (0 == nMsgs)
		return EOK;
	memset(pInfo, 0, nMsgs * (sizeof *pInfo));

	memset(&uArg, 0, (sizeof uArg));

	uArg.nMsgs = nMsgs;
	uArg.cMsgs = 0;
	uArg.fGetUIDL = fGetUIDL;

	if (fGetUIDL)
	{
		uArg.pInfo = pInfo;
		uArg.fGetSize = FALSE;

		exc = pop3_clnt_uidl(CBSock, POP3_ALL_MSGS,
									pop3_clnt_uidl_populate, (void *) &uArg,
									uRspTimeout);
		if (exc != EOK)
			return exc;
	}

	if (fGetSize)
	{
		uArg.pInfo = pInfo;
		uArg.fGetSize = TRUE;

		exc = pop3_clnt_list(CBSock, POP3_ALL_MSGS,
									pop3_clnt_uidl_populate, (void *) &uArg,
									uRspTimeout);
	}

	*pnMsgs = uArg.cMsgs;
	return exc;
}

/*---------------------------------------------------------------------------*/

typedef struct {
	LPRFC822MSGEXTRACTOR	pMsgEx;
	EXC_TYPE					exc;
} MSGXARGS, *LPMSGXARGS;

/*---------------------------------------------------------------------------*/

static BOOLEAN msgxCfn (const UINT32	/* msgNum */,		/* requested ID */
								const SINT32	/* linesNum */,	/* requested num */
								const char		iBuf[],		/* read buffer */
								const UINT32	iLen,			/* valid datalen */
								void				*pArg)		/* caller arg */
{
	LPMSGXARGS	pXA=(LPMSGXARGS) pArg;
	if (NULL == pXA)
		return FALSE;

	LPRFC822MSGEXTRACTOR	pMsgEx=pXA->pMsgEx;
	if (NULL == pMsgEx)
	{
		pXA->exc = ECONTEXT;
		return FALSE;
	}

	pXA->exc = pMsgEx->ProcessLine(iBuf, iLen);
	return (EOK == pXA->exc);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE pop3_msg_extract (ISockioInterface&		CBSock,
									const UINT32			ulMsgNum,
									RFC822MSGEX_CFN_TYPE	lpfnXcfn,
									void						*pArg,
									const UINT32			uRspTimeout)
{
	CRFC822MsgExtractor	msgEx;
	EXC_TYPE					exc=msgEx.SetDecodeParams(lpfnXcfn, pArg);
	if (exc != EOK)
		return exc;

	MSGXARGS	mxArgs;
	memset(&mxArgs, 0, (sizeof mxArgs));
	mxArgs.pMsgEx = &msgEx;

	exc = pop3_clnt_retr(CBSock, ulMsgNum, POP3_ALL_LINES, msgxCfn, (void *) &mxArgs, uRspTimeout);
	if (exc != EOK)
		return exc;
	if ((exc=mxArgs.exc) != EOK)
		return exc;

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

EXC_TYPE CPOP3EnvelopeData::ProcessHdr (LPCTSTR			lpszHdrName,
													 LPCTSTR			lpszHdrValue,
													 const BOOLEAN	fIsContHdr)
{
	// ignore empty header values
	if (IsEmptyStr(lpszHdrValue))
		return EOK;

	if (IsEnvelopeHdr(lpszHdrName))
		return ProcessHdrData(lpszHdrName, lpszHdrValue);

	// ignore headers not used for the envelope data
	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

typedef struct {
	LPPOP3ENVELOPEDATA	pED;
	EXC_TYPE					exc;
} GMEARGS, *LPGMEARGS;

static BOOLEAN gmeCfn (const UINT32		msgNum,
							  const char		lpszHdrName[],
							  const char		lpszHdrValue[],
							  const BOOLEAN	fIsContHdr,
							  void				*pArg)
{
	if (NULL == pArg)
		return FALSE;

	GMEARGS&		gmea=*((LPGMEARGS) pArg);
	EXC_TYPE&	exc=gmea.exc;
	if (NULL == gmea.pED)
	{
		exc = ECONTEXT;
		return FALSE;
	}

	CPOP3EnvelopeData&	ed=*gmea.pED;
	if ((exc=ed.ProcessHdr(lpszHdrName, lpszHdrValue, fIsContHdr)) != EOK)
		return FALSE;

	return TRUE;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE pop3_get_msg_envelope (ISockioInterface&	CBSock,
										  const UINT32			ulMsgNum,
										  CPOP3EnvelopeData&	ed,
										  const UINT32			uRspTimeout)
{
	ed.Clear();

	GMEARGS	gmea={ &ed, EOK };
	EXC_TYPE	exc=pop3_enum_hdrs(CBSock, ulMsgNum, gmeCfn, (LPVOID) &gmea, uRspTimeout);
	if (exc != EOK)
		return exc;
	if ((exc=gmea.exc) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

/*
 *	Makes sure the provided account parameters are valid. Returns:
 *
 *		ENOTCONNECTION - server could not be connected
 *		ESTATE - server service denied ("-ERR" in welcome line)
 *		EPERMISSION - authentication failed
 */
EXC_TYPE pop3_clnt_validate (LPCTSTR		lpszHost,
									  const int		iPort,		/* if 0 then use default */
									  LPCTSTR		lpszUserName,
									  LPCTSTR		lpszPasswd,
									  LPTSTR			lpszRspBuf,
									  const UINT32	ulBufLen,
									  const UINT32	uRspTimeout)
{
	CBuffSock	CBSock;
	EXC_TYPE		exc=pop3_clnt_connect(CBSock, lpszHost, iPort, lpszRspBuf, ulBufLen, uRspTimeout);
	if (exc != EOK)
	{
		if (ESTATE == exc)
			return exc;
		else
			return ENOTCONNECTION;
	}

	if ((exc=pop3_clnt_auth(CBSock, lpszUserName, lpszPasswd, lpszRspBuf, ulBufLen, uRspTimeout)) != EOK)
	{
		if (exc != ENOTCONNECTION)
			return EPERMISSION;
		else
			return exc;
	}

	return EOK;
}

EXC_TYPE pop3_clnt_validate (LPCTSTR		lpszHost,
									  const int		iPort,		/* if 0 then use default */
									  LPCTSTR		lpszUserName,
									  LPCTSTR		lpszPasswd,
									  const UINT32	uRspTimeout)
{
	TCHAR		welcomeRsp[POP3_MAX_RSP_LEN+4]=_T("");
	return pop3_clnt_validate(lpszHost, iPort, lpszUserName, lpszPasswd, welcomeRsp, POP3_MAX_RSP_LEN, uRspTimeout);
}

/*---------------------------------------------------------------------------*/

/* returns EIOUNCLASS if no match found */
EXC_TYPE pop3AnalyzeWelcomePattern (LPCTSTR			lpszWelcome,
												LPCTSTR			lpszWPattern,
												LPTSTR			lpszType,
												const UINT32	ulMaxTypeLen,
												LPTSTR			lpszVersion,
												const UINT32	ulMaxVerLen)
{
	EXC_TYPE	exc=EOK;
	LPCTSTR	lpszWPos=lpszWelcome;

	if (IsEmptyStr(lpszWelcome) || IsEmptyStr(lpszWPattern) ||
		 (NULL == lpszType) || (0 == ulMaxTypeLen) ||
		 (NULL == lpszVersion) || (0 == ulMaxVerLen))
		return EPARAM;

	*lpszType = _T('\0');
	*lpszVersion = _T('\0');

	/* make sure this is a "+OK" response */
	for ( ; _istspace(*lpszWPos) && (*lpszWPos != _T('\0')); lpszWPos++);
	if ((exc=pop3_xlate_rsp(lpszWPos)) != EOK)
		return exc;

	/* skip +OK */
	for (lpszWPos++; (!_istspace(*lpszWPos)) && (*lpszWPos != _T('\0')); lpszWPos++);
	if (_T('\0') == *lpszWPos)
		return EIOUNCLASS;

	/* skip to beginning of banner (after +OK) */
	for (lpszWPos++ ; _istspace(*lpszWPos) && (*lpszWPos != _T('\0')); lpszWPos++);
	if ((exc=inetAnalyzeWelcomePattern(lpszWPos, lpszWPattern, lpszType, ulMaxTypeLen, lpszVersion, ulMaxVerLen)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

/* some known server's names */
SZXTRN const TCHAR szPOP3InterMailSrvrName[]=_T("InterMail");
SZXTRN const TCHAR szPOP3MSExchangeSrvrName[]=_T("Exchange");
SZXTRN const TCHAR szPOP3Xcg2000SrvrName[]=_T("Exchange");
SZXTRN const TCHAR szPOP3CriticalPathSrvrName[]=_T("CriticalPath");

/* NULL terminated list of known server type strings */
SZXTRN LPCTSTR POP3ServerTypes[]={
	szSMTPMSExchangeSrvrName,
	szSMTPXcg2000SrvrName,
	szPOP3InterMailSrvrName,
	szPOP3CriticalPathSrvrName,

	NULL		/* mark end of list */
};

/*---------------------------------------------------------------------------*/

/* NOTE !!! the hardcoded name must match the value in "szPOP3InterMailSrvrName" */
/* SW.COM-KX4.3: +OK InterMail POP3 server ready */
SZXTRN const TCHAR szPOP3SWCOMKx4p3WelcomePattern[]=_T("%T=InterMail %V=KX4.3 InterMail POP3 server ready.");

/* Exchange: +OK Microsoft Exchange POP3 server version 5.5.2653.23 ready */
SZXTRN const TCHAR szPOP3MSExchangeWelcomePattern[]=_T("Microsoft %T POP3 server version %V ready");

/* Exchange2000: +OK Microsoft Exchange 2000 POP3 server version 6.0.5770.28 (exchange.newcti.com) ready. */
SZXTRN const TCHAR szPOP3Xcg2000WelcomePattern[]=_T("Microsoft %T 2000 POP3 server version %V (%I) ready.");

/* Exchange 2003: +OK Microsoft Exchange Server 2003 POP3 server version 6.5.6944.0 (test-exch2003.newcti.com) ready. */
SZXTRN const TCHAR szPOP3Xcg2003WelcomePattern[]=_T("Microsoft %T Server 2003 POP3 server version %V (%I) ready.");

/* NOTE !!! hardcoded value must match "szPOP3CriticalPathSrvrName"
/* CriticalPath: +OK POP3 server ready (6.0.021) <B97C4FDB4C676A668B323E13D1E26B7673362ABC@cticell.cpload.cti2.com> */
SZXTRN const TCHAR szPOP3CriticalPathWelcomePattern[]=_T("POP3 server %T=CriticalPath ready (%V) <%*>");

SZXTRN LPCTSTR POP3KnownWelcomePatterns[]={
 	szPOP3SWCOMKx4p3WelcomePattern,
	szPOP3MSExchangeWelcomePattern,
	szPOP3Xcg2000WelcomePattern,
	szPOP3Xcg2003WelcomePattern,
	szPOP3CriticalPathWelcomePattern,

	NULL	/* mark end */
};

/*---------------------------------------------------------------------------*/
