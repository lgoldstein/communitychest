#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

/*---------------------------------------------------------------------------*/

#include <_types.h>

#include <util/errors.h>
#include <util/string.h>
#include <comm/socket.h>

#include <internet/general.h>
#include <internet/pop3Lib.h>
#include <internet/smtpLib.h>
#include <internet/rfc822.h>

/*---------------------------------------------------------------------------*/

#define CRLF_LEN	2
static const char szCrLf[]="\r\n";

static EXC_TYPE logerr(POP3_HDRS_CFN	lpfnHcfn,
							  LPVOID				pArg,
							  const char		fmt[],
							  ...)
{
#define MAX_LOG_LINE_LEN 256
	char		szMsg[MAX_LOG_LINE_LEN+4]="";
	va_list	ap;
	EXC_TYPE	exc=EOK;

	if ((NULL == lpfnHcfn) || (NULL == fmt) || ('\0' == *fmt))
		return EPARAM;

	va_start(ap, fmt);
	_vsnprintf(szMsg, MAX_LOG_LINE_LEN, fmt, ap);
	va_end(ap);

	szMsg[MAX_LOG_LINE_LEN] = '\0';
	exc = (*lpfnHcfn)(szXLogfError, szMsg, pArg);

	return exc;
}

/*---------------------------------------------------------------------------*/

#define POP3_HDR_BUF_SIZE	(4 * 1022)

class POP3_RLYARGS {
	public:
		CRFC822HdrParser	chp;
		SOCKET				SMTPsock;
		char					pHdrBuf[POP3_HDR_BUF_SIZE+4];
		char					*bp;
		UINT32				uRemLen;
		UINT32				uHdrLen;
		EXC_TYPE				exc;
		const char			*pszSMTPHost;
		int					iSMTPPort;
		const char			*pszSMTPRecip;
		POP3_HDRS_CFN		lpfnHcfn;
		LPVOID				pArg;
		BOOLEAN				fIsBody;

		POP3_RLYARGS (const char		p_pszSMTPHost[],
						  const int			p_iSMTPPort,
						  const char		p_pszSMTPRecip[],
						  POP3_HDRS_CFN	p_lpfnHcfn,
						  LPVOID				p_pArg);

		~POP3_RLYARGS () { }
};

/*---------------------------------------------------------------------------*/

POP3_RLYARGS::POP3_RLYARGS (const char		p_pszSMTPHost[],
									 const int		p_iSMTPPort,
									 const char		p_pszSMTPRecip[],
									 POP3_HDRS_CFN	p_lpfnHcfn,
									 LPVOID			p_pArg)
{
	SMTPsock = BAD_SOCKET;
	uRemLen = POP3_HDR_BUF_SIZE;
	pszSMTPHost = p_pszSMTPHost;
	iSMTPPort = p_iSMTPPort;
	pszSMTPRecip = p_pszSMTPRecip;
	lpfnHcfn = p_lpfnHcfn;
	pArg = p_pArg;
	exc = EOK;
	uHdrLen = 0;
	fIsBody = FALSE;
	pHdrBuf[0] = '\0';
	bp = pHdrBuf;
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE handle_relay_data (const UINT32		msgNum,
											  const char		iBuf[],
											  const UINT32		iLen,
											  POP3_RLYARGS		*pRArgs,
											  POP3_HDRS_CFN	lpfnHcfn,
											  LPVOID				pArg)
{
	EXC_TYPE	exc=EOK;

	if ((NULL == iBuf) || (NULL == pRArgs) || (NULL == lpfnHcfn))
		return EPARAM;
	if (BAD_SOCKET == pRArgs->SMTPsock)
		return EIOHARD;

	/* detect when we start the body */
	if (!(pRArgs->fIsBody))
	{
		/* detect immediate MIME boundary */
		if ((iLen > RFC822_MIME_BOUNDARY_DELIMS_LEN) &&
			 strnicmp(iBuf, pszMIMEBoundaryDelims, RFC822_MIME_BOUNDARY_DELIMS_LEN) == 0)
		{
			if ((exc=logerr(lpfnHcfn, pArg, "WARNING: immediate MIME in msg %lu: \"%s\"", msgNum, iBuf)) != EOK)
				return exc;

			pRArgs->fIsBody = TRUE;
		}

		/* blank line denotes start of body */
		if (0 == iLen)
			pRArgs->fIsBody = TRUE;
	}

	if (pRArgs->fIsBody)
	{
		if ((exc=(*lpfnHcfn)(szXBody, (void *) iBuf, pArg)) != EOK)
			return exc;
	}
	else	/* not msg body - parse hdrs */
	{
		CRFC822HdrParser&	chp=pRArgs->chp;

		if (EOK == (exc=chp.ProcessHdr(iBuf)))
		{
			if ((exc=(*lpfnHcfn)(szXHdrParser, (void *) &chp, pArg)) != EOK)
				return exc;
		}
		else
			logerr(lpfnHcfn, pArg,
					 "WARNING: (handle-rly-data) cannot (0x%08x) process msg %lu hdr \"%s\"",
					 exc, msgNum, iBuf);
	}

	/* check if end of message */
	if ((1 == iLen) && (stricmp(iBuf, ".") == 0))
	{
		exc = smtpDataSockClose(pRArgs->SMTPsock, FALSE);
		pRArgs->SMTPsock = BAD_SOCKET;
		pRArgs->fIsBody = FALSE;
	}
	else
	{
		char	*tbp=(char *) iBuf;
		int	wLen=(-1), xLen=(iLen + 2);

		tbp[iLen] = '\r';
		tbp[iLen+1] = '\n';
		tbp[iLen+2] = '\0';

		if ((wLen=sockWrite(pRArgs->SMTPsock, tbp, xLen)) != (int) xLen)
			exc = ENOTCONNECTION;

		tbp[iLen] = '\0';

		if (exc != EOK)
		{
			logerr(lpfnHcfn, pArg,
					 "WARNING: Lost connection with %s host in mid msg %lu",
					 pRArgs->pszSMTPHost, msgNum);
			return exc;
		}
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE handle_backlog_headers (const UINT32			msgNum,
													 SOCKET					SMTPsock, 
													 CRFC822HdrParser&	chp,
													 const char				pszHdrsBuf[],
													 const UINT32			/* ulHdrsLen */,
													 POP3_HDRS_CFN			lpfnHcfn,
													 LPVOID					pArg)
{
	const char			*bp=pszHdrsBuf;
	EXC_TYPE				exc=EOK;

	if ((BAD_SOCKET == SMTPsock) || (NULL == pszHdrsBuf) || (NULL == lpfnHcfn))
		return EPARAM;
	chp.Reset();

	while (*bp != '\0')
	{
		const char	*tsp=bp;
		char			tch='\0';

		/* find value end */
		for (tsp++; (*tsp != '\r') && (*tsp != '\n') && (*tsp != '\0'); tsp++);

		// create EOS string
		tch = *tsp;
		*((char *) tsp) = '\0';

		if (EOK == (exc=chp.ProcessHdr(bp)))
		{
			if ((exc=(*lpfnHcfn)(szXHdrParser, (void *) &chp, pArg)) != EOK)
				return exc;
		}
		else
			logerr(lpfnHcfn, pArg,
					 "WARNING: (handle-blog-hdrs) cannot (0x%08x) process msg %lu hdr \"%s\"",
					 exc, msgNum, bp);
		*((char *) tsp) = tch;	// restore original char

		if ('\r' == *tsp) tsp++;	// skip CRLF
		if ('\n' == *tsp) tsp++;

		bp = tsp;
	}

	return exc;
}

/*---------------------------------------------------------------------------*/

static BOOLEAN get_sender_address (const char	iBuf[],
											  char			szSender[],
											  const DWORD	ulSize)
{
	const char	*pszFrom=NULL, *tsp=NULL;
	char			cDelim='\0';
	size_t		tlen=0;

	if ((NULL == iBuf) || (NULL == szSender) || (0 == ulSize))
		return FALSE;
	*szSender = '\0';

	/* check if delimited address found */
	if (NULL == (pszFrom=strchr(iBuf, EMAIL_PATH_SDELIM)))
	{
		for (pszFrom=iBuf; *pszFrom != '\0'; pszFrom++)
		{
			/* skip any preceding white-space */
			for (cDelim = '\0' ; '\0' != *pszFrom; pszFrom++)
				if (!isspace(*pszFrom)) break;

			/* check that we have not skipped entire line */
			if ('\0' == (cDelim=*pszFrom))
				return FALSE;

			/* skip any delimited display name */
			if (('\"' != cDelim) && ('\'' != cDelim)) break;

			for (pszFrom++; *pszFrom != '\0'; pszFrom++)
				if (*pszFrom == cDelim) break;

			/* check that we have not skipped entire line */
			if ((*pszFrom != cDelim) || ('\0' == *pszFrom))
				return FALSE;
		}
	}
	else	/* delimited address string */
	{
		cDelim = *pszFrom;
		pszFrom++;	/* skip delimiter */
	}

	/* check if delimited address */
	if (EMAIL_PATH_SDELIM == cDelim)
	{
		/* find end of delimiter */
		if (NULL == (tsp=strchr(pszFrom, EMAIL_PATH_EDELIM)))
			return FALSE;
	}
	else	/* non delimited address assumed */
	{
		/* find end of address at first space char */
		for (tsp = pszFrom; *tsp != '\0'; tsp++)
			if (isspace(*tsp) && ((*tsp) != '-') && ((*tsp) != '_') &&
				 ((*tsp) != '.') && ((*tsp) != INET_DOMAIN_SEP))
				break;
	}

	/*		At this stage "pszFrom" points to 1st address char and
	 * "tsp" points to one char after last address char.
	 */
	if (((tlen=(tsp - pszFrom)) >= ulSize) || (0 == tlen))
		return FALSE;

	strncpy(szSender, pszFrom, tlen);
	szSender[tlen] = '\0';

	return TRUE;
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE handle_sender_header (const UINT32		msgNum,
												  const char		iBuf[],
												  const UINT32		iLen,
												  POP3_RLYARGS		*pRArgs,
												  POP3_HDRS_CFN	lpfnHcfn,
												  LPVOID				pArg)
{
	static const char szDefaultSender[]="nobody@nowhere.net";

	if ((NULL == pRArgs) || (NULL == lpfnHcfn))
		return EPARAM;

	const CRFC822HdrParser&	chp=pRArgs->chp;
	const char					*lpszHdr=chp.GetHdrName();
	const char					*lpszVal=chp.GetHdrValue();
	char							szSender[MAX_SNDR_NAME_LEN+2]="";
	EXC_TYPE						exc=EOK;
	UINT32						rcode=0;
	int							wLen=(-1);
	const char					*pszFrom=szDefaultSender;

	if (stricmp(lpszHdr, pszStdFromHdr) == 0)
	{
		if (get_sender_address(lpszVal, szSender, MAX_SNDR_NAME_LEN))
			pszFrom = szSender;
		else
			pszFrom = szDefaultSender;
	}

	if (pszFrom == szDefaultSender)
		logerr(lpfnHcfn, pArg,
				 "WARNING: Cannot find '%s' value for msg %lu",
				 pszStdFromHdr, msgNum);

	/* announce start of new msg relay */
	if ((exc=(*lpfnHcfn)(szPOP3TopCmd, (void *) msgNum, pArg)) != EOK)
		return exc;

	/* announce sender of new msg */
	if ((exc=(*lpfnHcfn)(szSMTPMailFromCmd, (void *) pszFrom, pArg)) != EOK)
		return exc;

	exc = smtpBasicSockOpen(pRArgs->pszSMTPHost, pRArgs->iSMTPPort, _T("<>"), &(pRArgs->SMTPsock));
	if (exc != EOK)
	{
		logerr(lpfnHcfn, pArg,
				 "WARNING: Cannot open SMTP conn to %s on %d for msg %lu: 0x%08x",
				 pRArgs->pszSMTPHost, pRArgs->iSMTPPort, msgNum, exc);
		return exc;
	}

	/* announce SMTP socket open */
	if ((exc=(*lpfnHcfn)(szXSockVal, (void *) pRArgs->SMTPsock, pArg)) != EOK)
		return exc;

	exc = smtpAddRecepient(pRArgs->SMTPsock, pRArgs->pszSMTPRecip, &rcode);
	if (exc != EOK)
	{
		logerr(lpfnHcfn, pArg,
				 "WARNING: Cannot add SMTP recip %s to %s for msg %lu: 0x%08x",
				 pRArgs->pszSMTPHost, pRArgs->pszSMTPRecip, msgNum, exc);
		return exc;
	}

	if ((rcode != SMTP_E_ACTION_OK) && (rcode != SMTP_E_USR_NLOCAL))
	{
		logerr(lpfnHcfn, pArg,
				 "WARNING: Bad code on add SMTP recip %s to %s for msg %lu: %lu",
				 pRArgs->pszSMTPHost, pRArgs->pszSMTPRecip, msgNum, rcode);
		return EIOSOFT;
	}

	if ((exc=smtpStartData(pRArgs->SMTPsock, &rcode)) != EOK)
	{
		logerr(lpfnHcfn, pArg,
				 "WARNING: Cannot start SMTP DATA to %s for msg %lu: 0x%08x",
				 pRArgs->pszSMTPHost, msgNum, exc);
		return exc;
	}

	if (rcode != SMTP_E_START_INP)
	{
		logerr(lpfnHcfn, pArg,
				 "WARNING: Bad code on SMTP DATA to %s for msg %lu: %lu",
				 pRArgs->pszSMTPHost,  msgNum, rcode);
		return EIOHARD;
	}

	if ((wLen=sockWrite(pRArgs->SMTPsock, pRArgs->pHdrBuf, pRArgs->uHdrLen)) != (int) pRArgs->uHdrLen)
	{
		logerr(lpfnHcfn, pArg,
				 "WARNING: host %s conn failed while sending msg %lu header(s)",
				 pRArgs->pszSMTPHost, msgNum);
		return ENOTCONNECTION;
	}

	/* make sure previous headers are terminated by CRLF */
	if (pRArgs->uHdrLen >= CRLF_LEN)
	{
		const char *bp=(pRArgs->pHdrBuf + pRArgs->uHdrLen);

		if (*(bp - 1) != '\n')
		{
			if ((wLen=sockWrite(pRArgs->SMTPsock, szCrLf, CRLF_LEN)) != CRLF_LEN)
			{
				logerr(lpfnHcfn, pArg,
						 "WARNING: host %s conn failed while sep hdrs for msg %lu header(s)",
						pRArgs->pszSMTPHost, msgNum);
				return ENOTCONNECTION;
			}
		}
	}

	if ((exc=handle_backlog_headers(msgNum, pRArgs->SMTPsock, pRArgs->chp,
											  pRArgs->pHdrBuf, pRArgs->uHdrLen,
											  lpfnHcfn, pArg)) != EOK)
		return exc;

	exc = handle_relay_data(msgNum, iBuf, iLen, pRArgs, lpfnHcfn, pArg);
	if (exc != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE handle_relay_headers (const UINT32		msgNum,
												  const char		iBuf[],
												  const UINT32		iLen,
												  POP3_RLYARGS		*pRArgs,
												  POP3_HDRS_CFN	lpfnHcfn,
												  LPVOID				pArg)
{
	const char	*lpszHdr=NULL;
	BOOLEAN		fForceSendToSMTP=FALSE;

	if ((NULL == iBuf) || (NULL == pRArgs) || (NULL == lpfnHcfn))
		return EPARAM;

	if (pRArgs->SMTPsock != BAD_SOCKET)
	{
		logerr(lpfnHcfn, pArg,
				 "WARNING: Not in headers hunting mode for msg %lu",
				 msgNum);
		return EPARAM;
	}

	/* detect immediate MIME */
	if ((iLen > RFC822_MIME_BOUNDARY_DELIMS_LEN) &&
		 strnicmp(iBuf, pszMIMEBoundaryDelims, RFC822_MIME_BOUNDARY_DELIMS_LEN) == 0)
	{
		lpszHdr = szXBody;
		fForceSendToSMTP = TRUE;
	}

	/* check if we have enough room in the buffer to accumulate this header */
	if ((iLen + CRLF_LEN) >= pRArgs->uRemLen)
	{
		logerr(lpfnHcfn, pArg,
				 "WARNING: Headers accumulation buf exceeded for msg %lu",
				 msgNum);
		fForceSendToSMTP = TRUE;
	}

	if (NULL == lpszHdr)
	{
		CRFC822HdrParser&	chp=pRArgs->chp;
		EXC_TYPE				exc=chp.ProcessHdr(iBuf);
		if (exc != EOK)
			logerr(lpfnHcfn, pArg,
					 "WARNING: (handle-rly-hdrs) cannot (0x%08x) process msg %lu hdr \"%s\"",
					 exc, msgNum, iBuf);
		lpszHdr = chp.GetHdrName();
	}

	/* send something if "From:" found or detected end of headers */
	if ((stricmp(lpszHdr, pszStdFromHdr) == 0) || (0 == iLen) || (fForceSendToSMTP))
		return handle_sender_header(msgNum, iBuf, iLen, pRArgs, lpfnHcfn, pArg);

	strncpy(pRArgs->bp, iBuf, iLen);
	pRArgs->bp += iLen;
	pRArgs->uRemLen -= iLen;
	pRArgs->uHdrLen += iLen;

	strcpy(pRArgs->bp, szCrLf);
	pRArgs->bp += CRLF_LEN;
	pRArgs->uRemLen -= CRLF_LEN;
	pRArgs->uHdrLen += CRLF_LEN;

	return EOK;
}

/*---------------------------------------------------------------------------*/

static BOOLEAN relay_cfn (const UINT32	msgNum,		/* requested ID */
								  const SINT32	/* linesNum */,	/* requested num */
								  const char	iBuf[],		/* read buffer */
								  const UINT32	iLen,			/* valid datalen */
								  void			*pRArg)		/* caller arg */
{
	POP3_RLYARGS	*pRArgs=(POP3_RLYARGS *) pRArg;

	if ((NULL == iBuf) || (NULL == pRArg))
	{
		if (pRArgs != NULL)
			pRArgs->exc = EFATALEXIT;
		return FALSE;
	}

	if ((NULL == pRArgs->pHdrBuf) || (NULL == pRArgs->bp) ||
		 (NULL == pRArgs->lpfnHcfn) || (NULL == pRArgs->pszSMTPHost) ||
		 (NULL == pRArgs->pszSMTPRecip))
	{
		pRArgs->exc = EFATALEXIT;
		return FALSE;
	}

	/*		If we have an already open SMTP socket, then it means that we have
	 * successfully handled the headers, and we can simply relay the contents.
	 */
	if (pRArgs->SMTPsock != BAD_SOCKET)
	{
		pRArgs->exc = handle_relay_data(msgNum, iBuf, iLen, pRArgs,
												  pRArgs->lpfnHcfn, pRArgs->pArg);

		if (pRArgs->exc != EOK)
			return FALSE;
		else
			return TRUE;
	}

	/* this point is reached as long as we have not found the "From:" yet */
	pRArgs->exc = handle_relay_headers(msgNum, iBuf, iLen, pRArgs,
												  pRArgs->lpfnHcfn, pRArgs->pArg);
	if (EOK != pRArgs->exc)
		return FALSE;
	else
		return TRUE;
}

/*---------------------------------------------------------------------------*/

/*		Relays the specified message to the SMTP target - if successful, then
 * deletes the message from the POP3 server (according to "fDeleteMsg" flag).
 */
static EXC_TYPE POP3_relay_msg (ISockioInterface&			POP3sock,
										  const POP3_EXTMSG_INFO&	msgInfo,
										  const char					pszSMTPHost[],
										  const int						iSMTPPort,
										  const char					pszSMTPRecip[],
										  POP3_HDRS_CFN				lpfnHcfn,
										  LPVOID							pArg,
										  const BOOLEAN				fDeleteMsg,
										  const UINT32					ulRspTimeout)
{
	UINT32			msgNum=msgInfo.msgNum;
	const char		*msgUIDL=msgInfo.msgUIDL;
	EXC_TYPE			exc=EOK, err=EOK;
	POP3_RLYARGS	rArgs(pszSMTPHost, iSMTPPort, pszSMTPRecip, lpfnHcfn, pArg);

	if (IsEmptyStr(pszSMTPHost) || (NULL == lpfnHcfn) || IsEmptyStr(pszSMTPRecip))
		return EFATALEXIT;

	/*		If UIDL available and not requested to delete the message then
	 * query application if message need be retrieved.
	 */
	if ((exc=(*lpfnHcfn)(szPOP3UidlCmd, (void *) &msgInfo, pArg)) != EOK)
	{
		/* EABORTEXIT means "skip" this message */
		if (EABORTEXIT == exc)
			return EOK;
		return exc;
	}

	exc = pop3_clnt_retr(POP3sock, msgNum, POP3_ALL_LINES, relay_cfn, (void *) &rArgs, ulRspTimeout);
	if (EOK == exc)
		exc = rArgs.exc;

	if ((EOK == exc) && (fDeleteMsg))
	{
		exc = pop3_clnt_dele(POP3sock, msgNum, ulRspTimeout);
		if (exc != EOK)
			logerr(lpfnHcfn, pArg,
					 "WARNING: Cannot delete msg %lu from POP3 user (0x%08x)",
					 msgNum, exc);
	}

	if (EOK == exc)
	{
		if ((err=(*lpfnHcfn)(szXMsgSize, (void *) rArgs.uHdrLen, pArg)) != EOK)
			exc = err;
	}

	if (rArgs.SMTPsock != BAD_SOCKET)
	{
		if ((err=sockClose(rArgs.SMTPsock)) != EOK)
			exc = err;
	}

	if (EOK == exc)
		exc = (*lpfnHcfn)(szPOP3RetrCmd, (void *) msgNum, pArg);

	return exc;
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE POP3_relay_start (ISockioInterface&		RSock,
											 const POP3RLYPARAMS&	rlyParams)
{
	const POP3ACCOUNTDEF&	accDef=rlyParams.accDef;
	LPCTSTR						lpszPOP3Host=accDef.lpszPOP3Host;
	POP3_HDRS_CFN				lpfnHcfn=rlyParams.lpfnHcfn;
	if (IsEmptyStr(lpszPOP3Host) || (NULL == lpfnHcfn))
		return ECONTEXT;

	LPVOID		pArg=rlyParams.pArg;
	EXC_TYPE		exc=(*lpfnHcfn)(szPOP3UserCmd, (LPVOID) lpszPOP3Host, pArg);
	if (exc != EOK)
		return exc;

	UINT32	ulRspTimeout=accDef.ulRspTimeout;
	if ((exc=pop3_clnt_connect(RSock, lpszPOP3Host, accDef.iPOP3Port, ulRspTimeout)) != EOK)
	{
		logerr(lpfnHcfn, pArg,
				 "WARNING:Cannot connect to POP3 host %s (err=0x%08x)",
				 lpszPOP3Host, exc);

		exc = (*lpfnHcfn)(szXConnAbort, (LPVOID) lpszPOP3Host, pArg);
		return ECONNNOTOPEN;
	}
	logerr(lpfnHcfn, pArg, "Connected with %s POP3 server", lpszPOP3Host);

	LPCTSTR	lpszPOP3UID=accDef.lpszPOP3UID;
	if ((exc=pop3_clnt_auth(RSock, lpszPOP3UID, accDef.lpszPOP3Passwd, ulRspTimeout)) != EOK)
	{
		logerr(lpfnHcfn, pArg,
				 "WARNING: Cannot authenticate user %s on %s (0x%08x)",
				 lpszPOP3UID, lpszPOP3Host, exc);

		exc = (*lpfnHcfn)(szXAuthAbort, (LPVOID) lpszPOP3UID, pArg);
		return EPASSWORDMISMATCH;
	}
	logerr(lpfnHcfn, pArg, "%s POP3 user authenticated", lpszPOP3UID);

	return EOK;
}

/*---------------------------------------------------------------------------*/

/*		Retrieves all contents of the POP3 account and sends them thru SMTP using
 * the specified SMTP server and recipient. After a successful send, the msg is
 * deleted from the POP3 account (according to the flags options).
 */
EXC_TYPE POP3_relay_account (const POP3RLYPARAMS *pRlyParams)
{
	if (NULL == pRlyParams)
		return EPARAM;

	POP3_HDRS_CFN	lpfnHcfn=NULL;
	LPVOID			pArg=pRlyParams->pArg;
	if (NULL == (lpfnHcfn=pRlyParams->lpfnHcfn))
		return ECONTEXT;

	CBuffSock					POP3sock;
	EXC_TYPE						exc=EOK, err=EOK;
	POP3_EXTMSG_INFO			*pInfo=NULL;
	const POP3ACCOUNTDEF&	accDef=pRlyParams->accDef;
	const SMTPMSGRELAYDEF&	rcpDef=pRlyParams->rcpDef;
	LPCTSTR						lpszPOP3Host=accDef.lpszPOP3Host;
	LPCTSTR						lpszPOP3UID=accDef.lpszPOP3UID;
	UINT32						uMsgsNum=0, uMboxSize=0, uIdx=0, uRetrNum=0;
	UINT32						ulRspTimeout=accDef.ulRspTimeout;
	BOOLEAN						fDelFromServer=IsPOP3RelayDelFromServer(pRlyParams->ulFlags);
	BOOLEAN						fCheckMboxSize=IsPOP3RelayCheckMboxSize(pRlyParams->ulFlags);

	if ((exc=POP3_relay_start(POP3sock, *pRlyParams)) != EOK)
		goto Quit;

	exc = pop3_clnt_stat(POP3sock, &uMsgsNum, &uMboxSize, ulRspTimeout);
	if (exc != EOK)
	{
		logerr(lpfnHcfn, pArg,
				 "WARNING: cannot (0x%08x) get \"%s@%s\" mbox status",
				 exc, lpszPOP3UID, lpszPOP3Host);
		goto Quit;
	}

	if (0 == uMsgsNum)
		goto Exit;

	if (fCheckMboxSize)
	{
		TCHAR	szStatRsp[(2 * MAX_DWORD_DISPLAY_LENGTH)+NAME_LENGTH+2]=_T(""), *lsp=szStatRsp;
		lsp = strlcat(lsp, POP3_OK);
		lsp = strladdch(lsp, _T(' '));
		lsp += dword_to_argument((DWORD) uMsgsNum, lsp);
		lsp = strladdch(lsp, _T(' '));
		lsp += dword_to_argument((DWORD) uMboxSize, lsp);

		if ((exc=(*lpfnHcfn)(szPOP3StatCmd, (void *) szStatRsp, pArg)) != EOK)
		{
			if (EABORTEXIT == exc)
			{
				logerr(lpfnHcfn, pArg, "skip relay from \"%s@%s\"", lpszPOP3UID, lpszPOP3Host);
				exc = EOK;
				goto Exit;
			}

			goto Quit;
		}
	}

	pInfo = (POP3_EXTMSG_INFO *) malloc(uMsgsNum * (sizeof *pInfo));
	if (NULL == pInfo)
	{
		logerr(lpfnHcfn, pArg,
				 "WARNING: Cannot allocate POP3 %lu msg dscs for \"%s@%s\"",
				 uMsgsNum, lpszPOP3UID, lpszPOP3Host);
		exc = EMEM;
		goto Quit;
	}
	memset(pInfo, 0, uMsgsNum * (sizeof *pInfo));

	exc = pop3_clnt_get_msgs_full_info(POP3sock, pInfo, uMsgsNum, TRUE, TRUE, &uMsgsNum, ulRspTimeout);
	if (exc != EOK)
	{
		logerr(lpfnHcfn, pArg,
				 "WARNING: cannot (0x%08x) get \"%s@%s\" POP3 msgs list",
				 exc, lpszPOP3UID, lpszPOP3Host);
		goto Quit;
	}
	logerr(lpfnHcfn, pArg,
			 "Retrieving %lu messages from \"%s@%s\"",
			 uMsgsNum, lpszPOP3UID, lpszPOP3Host);

	for (uIdx = 0, pInfo; uIdx < uMsgsNum; uIdx++)
	{
		const POP3_EXTMSG_INFO&	msgInfo=pInfo[uIdx];
		exc = POP3_relay_msg(POP3sock, msgInfo,
									rcpDef.lpszSMTPHost, rcpDef.iSMTPPort, rcpDef.lpszSMTPRecip,
									lpfnHcfn, pArg,
									fDelFromServer, ulRspTimeout);
		if (exc != EOK)
		{
			logerr(lpfnHcfn, pArg,
					 "WARNING: cannot (0x%08x) relay msg #%lu from \"%s@%s\"",
					 exc, msgInfo.msgNum, lpszPOP3UID, lpszPOP3Host);

			/* abort only if a fatal exit encountered */
			if ((EFATALEXIT == exc) || (ENOTCONNECTION == exc))
				break;
		}
		else /* delete the message now that we successfully relayed it */
		{
			uRetrNum++;
		}
	}

	if (uRetrNum != uMsgsNum)
	{
		logerr(lpfnHcfn, pArg,
				 "WARNING: Retrieved %lu out of %lu message from \"%s@%s\"",
				 uRetrNum, uMsgsNum, lpszPOP3UID, lpszPOP3Host);

		exc = EDEVFD;
	}

Exit:
	err = pop3_clnt_quit(POP3sock, ulRspTimeout);
	logerr(lpfnHcfn, pArg, "Disconnecting from %s POP3 server", lpszPOP3Host);

Quit:
	POP3sock.Close();	/* just making sure */

	if (pInfo != NULL)
	{
		free((LPVOID) pInfo);
		pInfo = NULL;
	}

	/* pass (HEX) exit code to callback */
	(*lpfnHcfn)(szPOP3QuitCmd, (void *) exc, pArg);
	return exc;
}

/*---------------------------------------------------------------------------*/
