#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#include <_types.h>
#include <comm/socket.h>
#include <util/string.h>
#include <util/errors.h>
#include <futils/general.h>

#include <internet/rfc822msg.h>
#include <internet/smtpLib.h>

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
#	define SZXTRN extern
#else
#	define SZXTRN	/* nothing */
#endif

SZXTRN const TCHAR szSMTPHeloCmd[]=_T("HELO");
SZXTRN const TCHAR szSMTPMailFromCmd[]=_T("MAIL FROM:");
SZXTRN const TCHAR szSMTPRcptToCmd[]=_T("RCPT TO:");
SZXTRN const TCHAR szSMTPDataCmd[]=_T("DATA");
SZXTRN const TCHAR szSMTPNoopCmd[]=_T("NOOP");
SZXTRN const TCHAR szSMTPQuitCmd[]=_T("QUIT");
SZXTRN const TCHAR szSMTPRsetCmd[]=_T("RSET");
SZXTRN const TCHAR szSMTPHelpCmd[]=_T("HELP");

/*---------------------------------------------------------------------------*/

static EXC_TYPE smtpBuildEMailAddress (LPCTSTR lpszOrgAddr, LPTSTR lpszAddr, const UINT32 ulMaxLen)
{
	LPTSTR	lsp=lpszAddr;
	UINT32	ulOrgLen=GetSafeStrlen(lpszOrgAddr);

	if ((0 == ulOrgLen) || (NULL == lpszAddr) || (0 == ulMaxLen))
		return EBADBUFF;
	*lpszAddr = _T('\0');

	if (EMAIL_PATH_SDELIM == *lpszOrgAddr)
	{
		LPCTSTR	lpszOrgEnd=(lpszOrgAddr + (ulOrgLen - 1));

		if (*lpszOrgEnd != EMAIL_PATH_EDELIM)
			return EUDFFORMAT;

		if (ulOrgLen >= ulMaxLen)
			return EOVERFLOW;

		_tcscpy(lpszAddr, lpszOrgAddr);
	}
	else	/* non-bracketed address */
	{
		if ((ulOrgLen + 2) >= ulMaxLen)
			return EOVERFLOW;

		lsp = strladdch(lsp, EMAIL_PATH_SDELIM);
		lsp = strlcat(lsp, lpszOrgAddr);
		lsp = strladdch(lsp, EMAIL_PATH_EDELIM);
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

/*		Returns TRUE if OK to disconnect from either side
 *
 * Note: we allow disconnect after DATA state (although not standard)
 */
extern BOOLEAN IsSMTPDisconnectOk (const SMTP_PROTO_STATE s)
{
	if ((SMTP_CONNECTED_STATE == s) ||	/* before HELO */
		 (SMTP_DATA_STATE == s) ||
		 (SMTP_QUIT_STATE == s))
		return TRUE;
	else
		return FALSE;
}

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
EXC_TYPE smtpParseCmd (LPCTSTR	lpszLine,
							  LPCTSTR&	lpszOp,
							  UINT32&	ulOpLen,
							  LPCTSTR&	lpszArg,
							  UINT32&	ulArgLen,
							  LPCTSTR&	lpszCont)
{
	lpszOp = NULL;
	ulOpLen = 0UL;
	lpszArg = NULL;
	ulArgLen = 0UL;
	lpszCont = NULL;

	if (IsEmptyStr(lpszLine))
		return EEMPTYENTRY;

	/* skip preceding white space */
	for (lpszOp=lpszLine; _istspace(*lpszOp) && (*lpszOp != _T('\0')); lpszOp++);
	if (_T('\0') == *lpszOp)
		return EILLEGALOPCODE;
	lpszArg = _T("");
	lpszCont = _T("");

	/* find end of opcode */
	LPCTSTR	tsp=lpszOp;
	for ( ; (!_istspace(*tsp)) && (*tsp != _T('\0')); tsp++, ulOpLen++);
	if (_T('\0') == *tsp)
		return EOK;	/* if no argument then fine... */

	/* skip preceding white-space in argument */
	for (tsp++; _istspace(*tsp) && (*tsp != _T('\0')); tsp++);
	if (_T('\0') == *tsp)
		return EOK;	/* if no argument then fine... */

	/* find end of argument (or of 2 word opcode) */
	for (lpszArg=tsp; (!_istspace(*tsp)) && (*tsp != _T('\0')) && (*tsp != _T(':')); tsp++, ulArgLen++);

	if (_T(':') == *tsp)
	{
		/* this is a 2 word opcode (e.g. "RCPT TO:") */
		tsp++;
		ulOpLen = (tsp - lpszOp);
		lpszArg = _T("");
		ulArgLen = 0UL;

		/* skip preceding white-space in argument */
		for (; _istspace(*tsp) && (*tsp != _T('\0')); tsp++);
		if (_T('\0') == *tsp)
			return EOK;	/* if no argument then fine... */

		/* find end of argument */

		/* if this an address argument, then return it in its entirety */
		if (EMAIL_PATH_SDELIM == *tsp)
		{
			for (lpszArg = tsp, tsp++; (*tsp != EMAIL_PATH_EDELIM) && (*tsp != _T('\0')); tsp++);

			if (*tsp != EMAIL_PATH_EDELIM)
				return EBADADDR;

			tsp++;
		}
		else
		{
			for (lpszArg = tsp, tsp++; (!_istspace(*tsp)) && (*tsp != _T('\0')); tsp++);
		}

		ulArgLen = (tsp - lpszArg);
	}

	for (lpszCont=tsp; _istspace(*lpszCont) && (*lpszCont != _T('\0')); lpszCont++);

	return EOK;
}
#endif

/*---------------------------------------------------------------------------*/

#define bzero(p,s) memset(p, 0, s)

/*---------------------------------------------------------------------------*/

static EXC_TYPE smtpBuildCmd (const char		cmd[],
										const char		arg[],
										char				l[],
										const UINT32	aLen)
{
	char	*lp=l;

	if ((NULL == cmd) || (NULL == l) || (0 == aLen))
		return EPARAM;

	if (NULL == arg)
	{
		if ((strlen(cmd)+2) >= aLen)
			return EOVERFLOW;
	}
	else
	{
		if ((strlen(cmd)+1+strlen(arg)+2) >= aLen)
			return EOVERFLOW;
	}

	*lp = '\0';
	lp = strlcat(lp, cmd);
	if ((arg != NULL) && (*arg != '\0'))
	{
		lp = strladdch(lp, ' ');
		lp = strlcat(lp, arg);
	}
	lp = strlcat(lp, "\r\n");

	return EOK;
}

/*---------------------------------------------------------------------------*/

/* Note: return ECONTINUED if not final code */
EXC_TYPE smtpGetResponse (const char szRsp[], UINT32 *rcode)
{
	EXC_TYPE		exc=EOK;
	const char	*lp=szRsp;

	if ((NULL == szRsp) || (NULL == rcode) || ('\0' == *szRsp))
		return EPARAM;

	if (!isdigit(*lp))
		return EINVALIDNUMERIC;
	while (isdigit(*lp) && (*lp != '\0')) lp++;

	*rcode = argument_to_dword(szRsp, (lp - szRsp), EXC_ARG(exc));
	if (exc != EOK)
		return exc;

	if ('-' == *lp)
		return ECONTINUED;

	if ((*lp != ' ') && (*lp != '\0'))
		return ESTRINGBUFFER;

	return EOK;
}

/*---------------------------------------------------------------------------*/

/* read response - which might be multi-line */
EXC_TYPE smtpGetFinalResponse (SOCKET			sock,
										 char				szLine[],
										 const size_t	sMaxLen,
										 UINT32			*rcode)
{
	if ((BAD_SOCKET == sock) || (NULL == rcode) ||
		 (NULL == szLine) || (0 == sMaxLen))
		return EPARAM;

	for ( ; ; )
	{
		EXC_TYPE	exc=EOK;
		int		rLen=sockReadCmd(sock, szLine, sMaxLen, 0);
		if (rLen <= 0)
			return ENOTCONNECTION;

		/* if not continuation line then stop */
		if ((exc=smtpGetResponse(szLine, rcode)) != ECONTINUED)
			return exc;
	}

	/* this point should never be reached */
	return EFATALEXIT;
}

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
/* read response - which might be multi-line */
EXC_TYPE smtpGetFinalResponse (ISockioInterface&	ISock,
										 char						szLine[],
										 const size_t			sMaxLen,
										 UINT32&					rcode,
										 const UINT32			ulRspTimeout)
{
	rcode = (UINT32) (-1);
	if ((NULL == szLine) || (0 == sMaxLen))
		return EPARAM;

	for (UINT32 ulLdx=0; ; ulLdx++)
	{
		EXC_TYPE	exc=EOK;
		BOOLEAN	fStripCRLF=FALSE;
		int		rLen=ISock.ReadCmd(szLine, sMaxLen, (SINT32) ulRspTimeout, &fStripCRLF);
		if (rLen <= 0)
			return ENOTCONNECTION;

		/* we must have entire response in line */
		if (!fStripCRLF)
			return EIOUNCLASS;

		/* if not continuation line then stop */
		if ((exc=smtpGetResponse(szLine, &rcode)) != ECONTINUED)
			return exc;
	}

	/* this point should never be reached */
	return EFATALEXIT;
}
#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/

#define MAX_SMTP_CMD_LEN	MAX_SMTP_CMD_LINE_LEN	/* max command */
#define MAX_SMTP_RSP_LEN	MAX_SMTP_CMD_LINE_LEN
#define MAX_PROTO_LINE		max(MAX_SMTP_CMD_LEN,MAX_SMTP_RSP_LEN)

/*---------------------------------------------------------------------------*/

/*		Builds & sends a command using the supplied argument (if any) and reads
 * back the response code. Note: skips multi-line responses and returns code
 * only from last line (in accordance with RFC821).
 */
EXC_TYPE smtpSendCmdSync (SOCKET			sock,
								  const char	cmd[],
								  const char	arg[],	/* may be NULL */
								  UINT32			*rcode)
{
	EXC_TYPE	exc=EOK;
	char		szLine[MAX_PROTO_LINE+2];
	int		wLen=(-1), rLen=(-1);

	if ((BAD_SOCKET == sock) || (NULL == cmd) || (NULL == rcode))
		return EPARAM;
	*rcode = SMTP_E_BAD_PARAM;
	if ('\0' == *cmd)
		return EPARAM;

	if ((exc=smtpBuildCmd(cmd, arg, szLine, MAX_PROTO_LINE)) != EOK)
		return exc;

	rLen = strlen(szLine);
	if ((wLen=sockWrite(sock, szLine, rLen)) != rLen)
		return ENOTCONNECTION;

	return smtpGetFinalResponse(sock, szLine, MAX_PROTO_LINE, rcode);
}

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
extern EXC_TYPE smtpSendCmdSync (ISockioInterface&	ISock,
											const char			cmd[],
											const char			arg[],	/* may be NULL */
											UINT32&				rcode,
											char					szRspBuf[],
											const UINT32		ulMaxLen,
											const UINT32		ulRspTimeout)
{
	EXC_TYPE	exc=EOK;

	rcode = (UINT32) (-1);
	if (IsEmptyStr(cmd))
		return EPARAM;

	if ((exc=smtpBuildCmd(cmd, arg, szRspBuf, ulMaxLen)) != EOK)
		return exc;

	const int	rLen=strlen(szRspBuf);
	const int	wLen=ISock.Write(szRspBuf, rLen);
	if (wLen != rLen)
		return ENOTCONNECTION;

	return smtpGetFinalResponse(ISock, szRspBuf, ulMaxLen, rcode, ulRspTimeout);
}

EXC_TYPE smtpSendCmdSync (ISockioInterface&	ISock,
								  const char			cmd[],
								  const char			arg[],	/* may be NULL */
								  UINT32&				rcode,
								  const UINT32			ulRspTimeout)
{
	char	szLine[MAX_PROTO_LINE+2]="";
	return smtpSendCmdSync(ISock, cmd, arg, rcode, szLine, MAX_PROTO_LINE, ulRspTimeout);
}
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

/*		Opens an SMTP socket to the specified host using the specified port. If
 * port if unspecified (i.e. <= 0) then "getservbyname" is used. If this fails
 * then the default IPPORT_SMTP (25) is used.
 *
 *		If successful, then returns the code in the server`s greeting line.
 */
EXC_TYPE smtpOpenSock (const char	hname[],
							  const int		pnum,
							  SOCKET			*pSock,
							  UINT32			*rcode)
{
	SOCKET	s=BAD_SOCKET;
	EXC_TYPE	exc=EOK;
	int		iConnPort=pnum;
	char		szLine[MAX_SMTP_RSP_LEN+2];

	if ((NULL == pSock) || (NULL == rcode))
		return EPARAM;
	*rcode = SMTP_E_BAD_PARAM;
	*pSock = BAD_SOCKET;

	/* should be commented out if no support for "getservbyname" */
	if (iConnPort <= 0)
		iConnPort = port_string2value("smtp");
	if (iConnPort <= 0)
		iConnPort = IPPORT_SMTP;

	if ((exc=sock_connect(&s, hname, iConnPort)) != EOK)
		return exc;

	if ((exc=smtpGetFinalResponse(s, szLine, MAX_SMTP_RSP_LEN, rcode)) != EOK)
	{
		sockClose(s);
		return exc;
	}

	*pSock = s;
	return EOK;
}

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
EXC_TYPE smtpOpenSock (ISockioInterface&	ISock,
							  const char			hname[],
							  const int				pnum,
							  UINT32&				rcode,
							  LPTSTR					lpszRsp,
							  const UINT32			ulMaxLen,
							  const UINT32			ulRspTimeout)
{
	EXC_TYPE	exc=EOK;
	int		iConnPort=pnum;

	rcode = (UINT32) (-1);

	/* should be commented out if no support for "getservbyname" */
	if (iConnPort <= 0)
	{
		static const char smtp_srvc[]="smtp";
		struct servent *sp=getservbyname(smtp_srvc, NULL);
		if (sp != NULL)
			iConnPort = htons(sp->s_port);
	}

	if (iConnPort <= 0)
		iConnPort = IPPORT_SMTP;

	if ((exc=ISock.Connect(hname, iConnPort)) != EOK)
		return exc;

	if ((exc=smtpGetFinalResponse(ISock, lpszRsp, ulMaxLen, rcode, ulRspTimeout)) != EOK)
	{
		ISock.Close();
		return exc;
	}

	return EOK;
}

EXC_TYPE smtpOpenSock (ISockioInterface&	ISock,
							  const char			hname[],
							  const int				pnum,
							  UINT32&				rcode,
							  const UINT32			ulRspTimeout)
{
	TCHAR		szLine[MAX_SMTP_RSP_LEN+2]=_T("");
	return smtpOpenSock(ISock, hname, pnum, rcode, szLine, MAX_SMTP_RSP_LEN, ulRspTimeout);
}
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

EXC_TYPE smtpSendHelo (SOCKET s, const char id[], UINT32 *rcode)
{
	char			curName[MAX_DNS_DOMAIN_LEN+2];
	const char	*pszID=id;

	if (IsEmptyStr(pszID))
	{
		if (gethostname(curName, MAX_DNS_DOMAIN_LEN) != 0)
			return EUNKNOWNEXIT;
		pszID = curName;
	}

	return smtpSendCmdSync(s, szSMTPHeloCmd, pszID, rcode);
}

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
EXC_TYPE smtpSendHelo (ISockioInterface&	ISock, const char id[], UINT32& rcode, char szRspBuf[], const UINT32 ulMaxLen, const UINT32 ulRspTimeout)
{
	char			curName[MAX_DNS_DOMAIN_LEN+2]="";
	const char	*pszID=id;

	if (IsEmptyStr(pszID))
	{
		if (gethostname(curName, MAX_DNS_DOMAIN_LEN) != 0)
			return EUNKNOWNEXIT;
		pszID = curName;
	}

	return smtpSendCmdSync(ISock, szSMTPHeloCmd, pszID, rcode, szRspBuf, ulMaxLen, ulRspTimeout);
}

EXC_TYPE smtpSendHelo (ISockioInterface&	ISock, const char id[], UINT32& rcode, const UINT32 ulRspTimeout)
{
	char	szLine[MAX_PROTO_LINE+2]="";
	return smtpSendHelo(ISock, id, rcode, szLine, MAX_PROTO_LINE, ulRspTimeout);
}
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

EXC_TYPE smtpAddRecepient (SOCKET s, const char rcpt[], UINT32 *rcode)
{
	if (IsEmptyStr(rcpt))
		return EPARAM;

	/* some mail servers require enclosing the address in "<...>" */
	if (EMAIL_PATH_SDELIM != *rcpt)
	{
		TCHAR 	szRcpt[MAX_RCVR_NAME_LEN+4];
		EXC_TYPE	exc=smtpBuildEMailAddress(rcpt, szRcpt, (MAX_RCVR_NAME_LEN+2));
		if (EOK == exc)
			return smtpSendCmdSync(s, szSMTPRcptToCmd, szRcpt, rcode);
	}

	return smtpSendCmdSync(s, szSMTPRcptToCmd, rcpt, rcode);
}

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
EXC_TYPE smtpAddRecepient (ISockioInterface& ISock, const char rcpt[], UINT32& rcode, char szRspBuf[], const UINT32 ulMaxLen, const UINT32 ulRspTimeout)
{
	if (IsEmptyStr(rcpt))
		return EPARAM;

	// some mail servers require enclosing the address in "<...>"
	if (EMAIL_PATH_SDELIM != *rcpt)
	{
		TCHAR 	szRcpt[MAX_RCVR_NAME_LEN+4]=_T("");
		EXC_TYPE	exc=smtpBuildEMailAddress(rcpt, szRcpt, (MAX_RCVR_NAME_LEN+2));
		if (EOK == exc)
			return smtpSendCmdSync(ISock, szSMTPRcptToCmd, szRcpt, rcode, szRspBuf, ulMaxLen, ulRspTimeout);
	}

	return smtpSendCmdSync(ISock, szSMTPRcptToCmd, rcpt, rcode, szRspBuf, ulMaxLen, ulRspTimeout);
}

EXC_TYPE smtpAddRecepient (ISockioInterface& ISock, const char rcpt[], UINT32& rcode, const UINT32 ulRspTimeout)
{
	char	szLine[MAX_PROTO_LINE+2]="";
	return smtpAddRecepient(ISock, rcpt, rcode, szLine, MAX_PROTO_LINE, ulRspTimeout);
}
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

EXC_TYPE smtpSetSender (SOCKET s, const char sndr[], UINT32 *rcode)
{
	if (IsEmptyStr(sndr))
		return EPARAM;

	/* some mail servers require enclosing the address in "<...>" */
	if (EMAIL_PATH_SDELIM != *sndr)
	{
		TCHAR 	szSndr[MAX_SNDR_NAME_LEN+4];
		EXC_TYPE	exc=smtpBuildEMailAddress(sndr, szSndr, (MAX_SNDR_NAME_LEN+2));
		if (EOK == exc)
			exc = smtpSendCmdSync(s, szSMTPMailFromCmd, szSndr, rcode);

		return exc;
	}

	return smtpSendCmdSync(s, szSMTPMailFromCmd, sndr, rcode);
}

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
EXC_TYPE smtpSetSender (ISockioInterface& ISock, const char sndr[], UINT32& rcode, char szRspBuf[], const UINT32 ulMaxLen, const UINT32 ulRspTimeout)
{
	if (IsEmptyStr(sndr))
		return EPARAM;

	// some mail servers require enclosing the address in "<...>"
	if (EMAIL_PATH_SDELIM != *sndr)
	{
		TCHAR 	szSndr[MAX_SNDR_NAME_LEN+4]=_T("");
		EXC_TYPE	exc=smtpBuildEMailAddress(sndr, szSndr, (MAX_SNDR_NAME_LEN+2));
		if (EOK == exc)
			exc = smtpSendCmdSync(ISock, szSMTPMailFromCmd, szSndr, rcode, szRspBuf, ulMaxLen, ulRspTimeout);

		return exc;
	}

	return smtpSendCmdSync(ISock, szSMTPMailFromCmd, sndr, rcode, szRspBuf, ulMaxLen, ulRspTimeout);
}

EXC_TYPE smtpSetSender (ISockioInterface&	ISock, const char sndr[], UINT32& rcode, const UINT32 ulRspTimeout)
{
	char	szLine[MAX_PROTO_LINE+2]="";
	return smtpSetSender(ISock, sndr, rcode, szLine, MAX_PROTO_LINE, ulRspTimeout);
}
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

EXC_TYPE smtpStartData (SOCKET s, UINT32 *rcode)
{
	return smtpSendCmdSync(s, szSMTPDataCmd, NULL, rcode);
}

EXC_TYPE smtpSendNoop (SOCKET s, UINT32 *rcode)
{
	return smtpSendCmdSync(s, szSMTPNoopCmd, NULL, rcode);
}

EXC_TYPE smtpSendReset (SOCKET s, UINT32 *rcode)
{
	return smtpSendCmdSync(s, szSMTPRsetCmd, NULL, rcode);
}

/*---------------------------------------------------------------------------*/

/* performs protocol up to specifying recipients */
EXC_TYPE smtpBasicSockOpen (const char	host[],
									 const int	port,
									 const char	fromUsr[],
									 SOCKET		*pSock)
{
	EXC_TYPE	exc=EOK;
	SOCKET	s=BAD_SOCKET;
	UINT32	rcode=SMTP_E_PARAMS;

	if (IsEmptyStr(host) || IsEmptyStr(fromUsr) || (NULL == pSock))
		return EPARAM;

	*pSock = BAD_SOCKET;

	if ((exc=smtpOpenSock(host, port, &s, &rcode)) != EOK)
		return exc;

	/* check that server response is OK */
	if (rcode != SMTP_E_DOMAIN_RDY)
	{
		exc = EABORTEXIT;
		goto Quit;
	}

	if ((exc=smtpSendHelo(s, NULL, &rcode)) != EOK)
		goto Quit;

	if (SMTP_E_ACTION_OK != rcode)
	{
		exc = EABORTEXIT;
		goto Quit;
	}

	if ((exc=smtpSetSender(s, fromUsr, &rcode)) != EOK)
		goto Quit;

	if (SMTP_E_ACTION_OK != rcode)
	{
		exc = EABORTEXIT;
		goto Quit;
	}

	exc = EOK;

Quit:
	if (exc != EOK)
	{
		if (s != BAD_SOCKET)
			sockClose(s);
		s = BAD_SOCKET;
	}

	*pSock = s;
	return exc;
}

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
EXC_TYPE smtpBasicSockOpen (ISockioInterface&	ISock,
									 const char				host[],
									 const int				port,
									 const char				fromUsr[],
									 const UINT32			ulRspTimeout)
{
	EXC_TYPE	exc=EOK;
	UINT32	rcode=SMTP_E_PARAMS;

	if (IsEmptyStr(host) || IsEmptyStr(fromUsr))
		return EPARAM;

	if ((exc=smtpOpenSock(ISock, host, port, rcode, ulRspTimeout)) != EOK)
		return exc;

	/* check that server response is OK */
	if (rcode != SMTP_E_DOMAIN_RDY)
	{
		exc = EABORTEXIT;
		goto Quit;
	}

	if ((exc=smtpSendHelo(ISock, NULL, rcode, ulRspTimeout)) != EOK)
		goto Quit;

	if (SMTP_E_ACTION_OK != rcode)
	{
		exc = EABORTEXIT;
		goto Quit;
	}

	if ((exc=smtpSetSender(ISock, fromUsr, rcode, ulRspTimeout)) != EOK)
		goto Quit;

	if (SMTP_E_ACTION_OK != rcode)
	{
		exc = EABORTEXIT;
		goto Quit;
	}

	exc = EOK;

Quit:
	if (exc != EOK)
		ISock.Close();

	return exc;
}
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

EXC_TYPE smtpDataSockOpen (const char	host[],
									const int	port,
									const char	fromUsr[],
									const char	**tusrs,
									SOCKET		*pSock)
{
	EXC_TYPE	exc=EOK;
	UINT32	i=0, tnum=0, rcode=SMTP_E_BAD_PARAM;
	SOCKET	s=BAD_SOCKET;

	if (IsEmptyStr(host) || IsEmptyStr(fromUsr) ||
		 (NULL == tusrs) || (NULL == pSock))
		return EPARAM;
	*pSock = BAD_SOCKET;

	if ((exc=smtpBasicSockOpen(host, port, fromUsr, &s)) != EOK)
		goto Quit;

	for (i=0; ; i++)
	{
		const char	*pszRcvr=tusrs[i];
		if (IsEmptyStr(pszRcvr))
			break;

		if ((exc=smtpAddRecepient(s, pszRcvr, &rcode)) != EOK)
			goto Quit;

		/* this is a special failure code - it suggests someone else to try - its
		 * suggested path is enclosed in '<...>'
		 */

		if (rcode == SMTP_E_USR_TRY)
			continue;

		/* if we get a bad response we go to next recepient and don't count it */
		if ((rcode == SMTP_E_ACTION_OK) || (rcode == SMTP_E_USR_NLOCAL))
			tnum++;
	}

	/* make sure at least ONE recepient */
	if (0 == tnum)
	{
		exc = EEMPTYENTRY;
		goto Quit;
	}

	if ((exc=smtpStartData(s, &rcode)) != EOK)
		goto Quit;

	if (rcode != SMTP_E_START_INP)
	{
		exc = EFACCESS;
		goto Quit;
	}

	exc = EOK;

Quit:
	if (exc != EOK)
	{
		if (s != BAD_SOCKET)
			sockClose(s);
		s = BAD_SOCKET;
	}

	*pSock = s;
	return exc;
}

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
EXC_TYPE smtpDataSockOpen (ISockioInterface&	ISock,
									const char			host[],
									const int			port,
									const char			fromUsr[],
									const char			**tusrs,
									const UINT32		ulRspTimeout)
{
	EXC_TYPE	exc=EOK;
	UINT32	i=0, tnum=0, rcode=SMTP_E_BAD_PARAM;

	if (IsEmptyStr(host) || IsEmptyStr(fromUsr) || (NULL == tusrs))
		return EPARAM;

	if ((exc=smtpBasicSockOpen(ISock, host, port, fromUsr, ulRspTimeout)) != EOK)
		goto Quit;

	for (i=0; ; i++)
	{
		const char	*pszRcvr=tusrs[i];
		if (IsEmptyStr(pszRcvr))
			break;

		if ((exc=smtpAddRecepient(ISock, pszRcvr, rcode, ulRspTimeout)) != EOK)
			goto Quit;

		/* this is a special failure code - it suggests someone else to try - its
		 * suggested path is enclosed in '<...>'
		 */

		if (rcode == SMTP_E_USR_TRY)
			continue;

		/* if we get a bad response we go to next recepient and don't count it */
		if ((rcode == SMTP_E_ACTION_OK) || (rcode == SMTP_E_USR_NLOCAL))
			tnum++;
	}

	/* make sure at least ONE recepient */
	if (0 == tnum)
	{
		exc = EEMPTYENTRY;
		goto Quit;
	}

	if ((exc=smtpStartData(ISock, rcode, ulRspTimeout)) != EOK)
		goto Quit;

	if (rcode != SMTP_E_START_INP)
	{
		exc = EFACCESS;
		goto Quit;
	}

	exc = EOK;

Quit:
	if (exc != EOK)
		ISock.Close();

	return exc;
}
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

EXC_TYPE smtpQuit (SOCKET s, UINT32 *rcode)
{
	EXC_TYPE	exc=smtpSendCmdSync(s, szSMTPQuitCmd, NULL, rcode);
	char		szDummy[MAX_DWORD_DISPLAY_LENGTH+2];
	int		rLen=sockReadCmd(s, szDummy, MAX_DWORD_DISPLAY_LENGTH, 0);
	sockClose(s);

	// should not happen, since other side is supposed to close the connection
	if (rLen >= 0)
		sockReadCmd(s, szDummy, MAX_DWORD_DISPLAY_LENGTH, 0);

	return exc;
}

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
EXC_TYPE smtpQuit (ISockioInterface& ISock, UINT32& rcode, const UINT32 ulRspTimeout)
{
	EXC_TYPE	exc=smtpSendCmdSync(ISock, szSMTPQuitCmd, NULL, rcode, ulRspTimeout);
	char		szDummy[MAX_DWORD_DISPLAY_LENGTH+2]="";

	// do a dummy read to ensure closure only if got an OK response
	int		rLen=(-1);
	if (EOK == exc)
		rLen = ISock.ReadCmd(szDummy, MAX_DWORD_DISPLAY_LENGTH, ulRspTimeout);

	ISock.Close();
	// should not happen, since other side is supposed to close the connection
	if (rLen >= 0)
		rLen = ISock.ReadCmd(szDummy, MAX_DWORD_DISPLAY_LENGTH, ulRspTimeout);	// dummy read

	return exc;
}
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

/* Note: closes socket !!! */
EXC_TYPE smtpDataSockClose (SOCKET s, const BOOLEAN add_crlf)
{
	EXC_TYPE	exc=EOK;
	UINT32	rcode=SMTP_E_START_INP, qcode=(UINT32) (-1);
	int		rLen=(-1);
	char		szLine[MAX_SMTP_RSP_LEN+2];

	if (add_crlf)
		rLen = sockWrite(s, "\r\n.\r\n", 5);
	else
		rLen = sockWrite(s, ".\r\n", 3);
	if (rLen <= 0)
		return ENOTCONNECTION;

	exc = smtpGetFinalResponse(s, szLine, MAX_SMTP_RSP_LEN, &rcode);
	if (exc != EOK)
		return exc;

	if ((exc=smtpSendReset(s, &qcode)) != EOK)
		return exc;

	exc = smtpQuit(s, &qcode);
	sockClose(s);

	if (rcode != SMTP_E_ACTION_OK)
		return EBADADDR;

	return EOK;
}

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
EXC_TYPE smtpDataSockClose (ISockioInterface&	ISock,
									 const BOOLEAN			add_crlf,
									 UINT32&					rcode,
									 LPTSTR					lpszRspBuf,
									 const UINT32			ulMaxLen,
									 const UINT32			ulRspTimeout)
{
	rcode = (UINT32) (-1);

	int	wLen=(-1);
	if (add_crlf)
		wLen = ISock.Write("\r\n.\r\n");
	else
		wLen = ISock.Write(".\r\n");
	if (wLen <= 0)
		return ENOTCONNECTION;

	// allow longer time for final response after message end to compensate for possible server processing
	EXC_TYPE exc=smtpGetFinalResponse(ISock, lpszRspBuf, ulMaxLen, rcode, MAX_ISRV_DPROC_TIME(ulRspTimeout));
	if (exc != EOK)
		return exc;

	UINT32	qcode=(UINT32) (-1);
	if ((exc=smtpQuit(ISock, qcode, ulRspTimeout)) != EOK)
		exc = EOK;

	ISock.Close();	// just making sure
	return EOK;
}

EXC_TYPE smtpDataSockClose (ISockioInterface&	ISock,
									 const BOOLEAN			add_crlf,
									 UINT32&					rcode,
									 const UINT32			ulRspTimeout)
{
	TCHAR	szLine[MAX_SMTP_RSP_LEN+2]=_T("");

	return smtpDataSockClose(ISock, add_crlf, rcode, szLine, MAX_SMTP_RSP_LEN, ulRspTimeout);
}
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#define DEFAULT_ATTENC_SIZE	1024

EXC_TYPE smtpSendAttachment (SOCKET						sock,
									  const char				pszFName[], /* NULL == last comp */
									  const char				pszFPath[],
									  const char				pszMIMEBoundary[],	/* NULL == direct attchment */
									  const BOOLEAN			fIsFirst,
									  const BOOLEAN			fIsLast,
									  const char				pszMIMEType[], /* may be NULL */
									  const char				pszMIMESubType[], /* may be NULL */
									  const RFC822ENCCASE	eEncoding)
{
	EXC_TYPE	exc=EOK;
	int		wLen=(-1);
	LPCTSTR	lpszEnc=RFC822EncodingCase2Str(eEncoding);
	LPCTSTR	lpszFN=pszFName;
	LPCTSTR	lpszMT=pszMIMEType;
	LPCTSTR	lpszMST=pszMIMESubType;
	BOOLEAN	fHaveMIMEBoundary=(!IsEmptyStr(pszMIMEBoundary));

	if ((BAD_SOCKET == sock) || IsEmptyStr(pszFPath) || IsEmptyStr(lpszEnc))
		return EPARAM;

	if (IsEmptyStr(lpszFN))
	{
		// find LAST separator
		if ((lpszFN=strrchr(pszFPath, FULL_PATH_CHAR)) != NULL)
		{
			lpszFN++;

			if (IsEmptyStr(lpszFN))
				return EEMPTYENTRY;
		}
		else
			lpszFN = pszFPath;
	}

	if (IsEmptyStr(lpszMT))
		lpszMT = pszMIMEApplicationType;
	if (IsEmptyStr(lpszMST))
		lpszMST = pszMIMEOctetStreamSubType;

	if (fIsFirst && fHaveMIMEBoundary)
	{
		if ((wLen=sockWriteCmdf(sock, "%s%s\r\n", pszMIMEBoundaryDelims, pszMIMEBoundary)) <= 0)
			return ENOTCONNECTION;
	}

	if ((wLen=sockWriteCmdf(sock, "%s %s/%s;\r\n", pszStdContentTypeHdr, lpszMT, lpszMST)) <= 0)
		return ENOTCONNECTION;

	if ((wLen=sockWriteCmdf(sock, "\t%s%c\"%s\"\r\n", pszMIMENameKeyword, RFC822_KEYWORD_VALUE_DELIM, lpszFN)) <= 0)
		return ENOTCONNECTION;

	if ((wLen=sockWriteCmdf(sock, "%s %s\r\n", pszStdContentXferEncoding, lpszEnc)) <= 0)
		return ENOTCONNECTION;

	if ((wLen=sockWriteCmdf(sock, "%s attachment;\r\n", pszStdContentDisposition)) <= 0)
		return ENOTCONNECTION;

	if ((wLen=sockWriteCmdf(sock, "\t%s%c\"%s\"\r\n", pszMIMEFilenameKeyword, RFC822_KEYWORD_VALUE_DELIM, lpszFN)) <= 0)
		return ENOTCONNECTION;

	/* mark start of attachment data */
	if ((wLen=sockWrite(sock, "\r\n", 2)) != 2)
		return ENOTCONNECTION;

	switch(eEncoding)
	{
		case RFC822_B64_ENC	:
			exc = b64_encode_named_file(pszFPath, sockIOWriteCfn, (void *) sock, DEFAULT_ATTENC_SIZE);
			break;

		case RFC822_QP_ENC	:
			exc = qp_encode_named_file(pszFPath, sockIOWriteCfn, (void *) sock, DEFAULT_ATTENC_SIZE);
			break;

		default					:
			exc = ESUPPORT;
	}
	if (exc != EOK)
		return exc;

	/* mark end of attachment data */
	if ((wLen=sockWrite(sock, "\r\n", 2)) != 2)
		return ENOTCONNECTION;

	if (fHaveMIMEBoundary)
	{
		if (fIsLast)
			wLen = sockWriteCmdf(sock, "%s%s%s\r\n", pszMIMEBoundaryDelims, pszMIMEBoundary, pszMIMEBoundaryDelims);
		else
			wLen = sockWriteCmdf(sock, "%s%s\r\n", pszMIMEBoundaryDelims, pszMIMEBoundary);
		if (wLen <= 0)
			return ENOTCONNECTION;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
EXC_TYPE smtpSendAttachment (ISockioInterface&		ISock,
									  const char				pszFName[], /* NULL == last comp */
									  const char				pszFPath[],
									  const char				pszMIMEBoundary[],	/* NULL == direct attchment */
									  const BOOLEAN			fIsFirst,
									  const BOOLEAN			fIsLast,
									  const char				pszMIMEType[], /* may be NULL */
									  const char				pszMIMESubType[], /* may be NULL */
									  const RFC822ENCCASE	eEncoding)
{
	EXC_TYPE	exc=EOK;
	int		wLen=(-1);
	LPCTSTR	lpszEnc=RFC822EncodingCase2Str(eEncoding);
	LPCTSTR	lpszFN=pszFName;
	LPCTSTR	lpszMT=pszMIMEType;
	LPCTSTR	lpszMST=pszMIMESubType;
	BOOLEAN	fSendMIMEBoundary=TRUE;

	if (IsEmptyStr(pszFPath) || IsEmptyStr(lpszEnc))
		return EPARAM;

	/* allow no MIME boundary for message with exactly ONE attachment */
	if ((!fIsFirst) || (!fIsLast))
	{
		if (IsEmptyStr(pszMIMEBoundary))
			return EPARAM;
	}
	else	/* this is the only attachment (i.e. first & last) */
	{
		if (IsEmptyStr(pszMIMEBoundary))
			fSendMIMEBoundary = FALSE;
	}

	if (IsEmptyStr(lpszFN))
	{
		if (NULL == (lpszFN=strchr(pszFPath, FULL_PATH_CHAR)))
			lpszFN = pszFPath;
	}

	if (IsEmptyStr(lpszMT))
		lpszMT = pszMIMEApplicationType;
	if (IsEmptyStr(lpszMST))
		lpszMST = pszMIMEOctetStreamSubType;

	if (fIsFirst && fSendMIMEBoundary)
	{
		if ((wLen=ISock.WriteCmdf("%s%s\r\n", pszMIMEBoundaryDelims, pszMIMEBoundary)) <= 0)
			return ENOTCONNECTION;
	}

	if ((wLen=ISock.WriteCmdf("%s %s/%s;\r\n", pszStdContentTypeHdr, lpszMT, lpszMST)) <= 0)
		return ENOTCONNECTION;

	if ((wLen=ISock.WriteCmdf("\t%s%c\"%s\"\r\n", pszMIMENameKeyword, RFC822_KEYWORD_VALUE_DELIM, lpszFN)) <= 0)
		return ENOTCONNECTION;

	if ((wLen=ISock.WriteCmdf("%s %s\r\n", pszStdContentXferEncoding, lpszEnc)) <= 0)
		return ENOTCONNECTION;

	if ((wLen=ISock.WriteCmdf("%s attachment;\r\n", pszStdContentDisposition)) <= 0)
		return ENOTCONNECTION;

	if ((wLen=ISock.WriteCmdf("\t%s%c\"%s\"\r\n", pszMIMEFilenameKeyword, RFC822_KEYWORD_VALUE_DELIM, lpszFN)) <= 0)
		return ENOTCONNECTION;

	/* mark start of attachment data */
	if ((wLen=ISock.Writeln()) != 2)
		return ENOTCONNECTION;

	switch(eEncoding)
	{
		case RFC822_B64_ENC	:
			if ((exc=b64_encode_named_file(pszFPath, isioIOWriteCfn, (void *) &ISock, DEFAULT_ATTENC_SIZE)) != EOK)
				return exc;
			break;

		case RFC822_QP_ENC	:
			if ((exc=qp_encode_named_file(pszFPath, isioIOWriteCfn, (void *) &ISock, DEFAULT_ATTENC_SIZE)) != EOK)
				return exc;
			break;

		default					:
			return ESUPPORT;
	}

	/* mark end of attachment data */
	if ((wLen=ISock.Writeln()) != 2)
		return ENOTCONNECTION;

	if (fSendMIMEBoundary)
	{
		if (fIsLast)
			wLen = ISock.WriteCmdf("%s%s%s\r\n", pszMIMEBoundaryDelims, pszMIMEBoundary, pszMIMEBoundaryDelims);
		else
			wLen = ISock.WriteCmdf("%s%s\r\n", pszMIMEBoundaryDelims, pszMIMEBoundary);
		if (wLen <= 0)
			return ENOTCONNECTION;
	}

	return EOK;
}
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
EXC_TYPE smtpSendAttachment (ISockioInterface&		ISock,
									  const CAttachInfo&		attInfo,
									  const char				pszMIMEBoundary[],	/* NULL == direct attchment */
									  const BOOLEAN			fIsFirst,
									  const BOOLEAN			fIsLast)
{
	LPCTSTR	lpszAttName=attInfo.GetName();
	LPCTSTR	lpszAttPath=attInfo.GetPath();
	LPCTSTR	lpszType=attInfo.GetMIMEType();
	LPCTSTR	lpszSubType=attInfo.GetMIMESubType();
	RFC822ENCCASE	eEncoding=attInfo.GetMIMEEncoding();

	return smtpSendAttachment(ISock, lpszAttName, lpszAttPath,
									  pszMIMEBoundary, fIsFirst, fIsLast,
									  lpszType, lpszSubType, eEncoding);
}
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// Note: assumes handshake + DATA completed - returns whether EOM pattern found in file
EXC_TYPE smtpSendMsgFile (ISockioInterface&	ISock, FILE *fp, BOOLEAN& fHasEOM)
{
	fHasEOM = FALSE;
	if (NULL == fp)
		return ENOTFILECONN;

	CRFC822MsgEOM	eomHunter;
	UINT32			ulXferSize=0;
	for ( ; ; )
	{
		const UINT32	MAX_SMTPSNDF_BUFSIZE=(4 * 1024);
		BYTE		szBuf[MAX_SMTPSNDF_BUFSIZE+2]=_T("");
		size_t	rLen=fread(szBuf, sizeof(BYTE), MAX_SMTPSNDF_BUFSIZE, fp);
		int		wLen=ISock.Write((LPCTSTR) szBuf, rLen);
		if (wLen != (int) rLen)
			return ENOTCONNECTION;

		ulXferSize += (UINT32) rLen;

		EXC_TYPE	exc=eomHunter.ProcessBuf((LPCTSTR) szBuf, (UINT32) rLen);
		if (exc != EOK)
			return exc;

		// check if reached EOM
		fHasEOM = eomHunter.IsMsgEOM();
		if (fHasEOM)
			break;

		// check if read entire file
		if ((UINT32) rLen < MAX_SMTPSNDF_BUFSIZE)
		{
			if (feof(fp))
				break;

			if (ferror(fp))
				return EIOHARD;
		}
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE smtpSendMsgFile (ISockioInterface&	ISock, LPCTSTR lpszFilePath, BOOLEAN& fHasEOM)
{
	fHasEOM = FALSE;

	if (IsEmptyStr(lpszFilePath))
		return EPATHNAMESYNTAX;

	FILE	*fp=_tfopen(lpszFilePath, _T("rb"));
	if (NULL == fp)
		return EFNEXIST;

	EXC_TYPE	exc=smtpSendMsgFile(ISock, fp, fHasEOM);
	fclose(fp);

	return exc;
}
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

EXC_TYPE smtpInit (const BOOLEAN /* verbOn */)
{
	return EOK;
}

/*---------------------------------------------------------------------------*/

/* returns EIOUNCLASS if no match found */
EXC_TYPE smtpAnalyzeWelcomePattern (LPCTSTR			lpszWelcome,
												LPCTSTR			lpszWPattern,
												LPTSTR			lpszType,
												const UINT32	ulMaxTypeLen,
												LPTSTR			lpszVersion,
												const UINT32	ulMaxVerLen)
{
	EXC_TYPE	exc=EOK;
	LPCTSTR	lpszWPos=lpszWelcome, lpszRCode=NULL;
	UINT32	rcode=(UINT32) (-1);

	if (IsEmptyStr(lpszWelcome) || IsEmptyStr(lpszWPattern) ||
		 (NULL == lpszType) || (0 == ulMaxTypeLen) ||
		 (NULL == lpszVersion) || (0 == ulMaxVerLen))
		return EPARAM;

	*lpszType = _T('\0');
	*lpszVersion = _T('\0');

	/* make sure this is a "Domain Ready" response */
	for ( ; _istspace(*lpszWPos) && (*lpszWPos != _T('\0')); lpszWPos++);

	for (lpszRCode=lpszWPos; _istdigit(*lpszWPos) && (*lpszWPos != _T('\0')); rcode++, lpszWPos++);
	/* allow for multi-line welcome */
	if ((!_istspace(*lpszWPos)) && (*lpszWPos != _T('\0')) && (_T('-') != *lpszWPos))
		return EUDFFORMAT;

	rcode = argument_to_dword(lpszRCode, (lpszWPos - lpszRCode), EXC_ARG(exc));
	if (exc != EOK)
		return exc;

	if (rcode != SMTP_E_DOMAIN_RDY)
		return EFACCESS;

	/* allow for multi-line welcome */
	if (_T('-') == *lpszWPos)
		lpszWPos++;

	for ( ; _istspace(*lpszWPos) && (*lpszWPos != _T('\0')); lpszWPos++);

	if ((exc=inetAnalyzeWelcomePattern(lpszWPos, lpszWPattern, lpszType, ulMaxTypeLen, lpszVersion, ulMaxVerLen)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

/* some known server's names */
SZXTRN const TCHAR szSMTPInterMailSrvrName[]=_T("InterMail");
SZXTRN const TCHAR szSMTPMSExchangeSrvrName[]=_T("Exchange");
SZXTRN const TCHAR szSMTPMirapointSrvrName[]=_T("Mirapoint");
SZXTRN const TCHAR szSMTPLotusDominoSrvrName[]=_T("Domino");
SZXTRN const TCHAR szSMTPCommtouchSrvrName[]=_T("NPLex");
SZXTRN const TCHAR szSMTPSendmailSrvrName[]=_T("Sendmail");
SZXTRN const TCHAR szSMTPInterScanSrvrName[]=_T("InterScan");
SZXTRN const TCHAR szSMTPNSMailSrvrName[]=_T("Netscape");
SZXTRN const TCHAR szSMTPiPlanetSrvrName[]=_T("iPlanet");
SZXTRN const TCHAR szSMTPXcg2000SrvrName[]=_T("Exchange");
SZXTRN const TCHAR szSMTPCommuniGateProSrvrName[]=_T("CommuniGate Pro");
SZXTRN const TCHAR szSMTPCriticalPathSrvrName[]=_T("CriticalPath");

/* NULL terminated list of known server type strings */
SZXTRN LPCTSTR SMTPServerTypes[]={
	szSMTPInterMailSrvrName,
	szSMTPMSExchangeSrvrName,
	szSMTPMirapointSrvrName,
	szSMTPLotusDominoSrvrName,
	szSMTPCommtouchSrvrName,
	szSMTPSendmailSrvrName,
	szSMTPInterScanSrvrName,
	szSMTPNSMailSrvrName,
	szSMTPiPlanetSrvrName,
	szSMTPXcg2000SrvrName,
	szSMTPCommuniGateProSrvrName,
	szSMTPCriticalPathSrvrName,

	NULL		/* mark end of list */
};

/*---------------------------------------------------------------------------*/

/* SW.COM-KX4.3:	220 moon.telcotest.cti2.com ESMTP server (InterMail vK.4.03.00.00 201-232-121 license d3f1e9b0ca21d978198f8b681cb00234) ready Wed, 28 Feb 2001 11:22:59 +0200 */
SZXTRN const TCHAR szSMTPSWCOMKx4p3WelcomePattern[]=_T("%I ESMTP server (%T %V %*) ready %D");

/* Exchange: 220 newexch.cti2.com ESMTP Server (Microsoft Exchange Internet Mail Service 5.5.2448.0) ready */
SZXTRN const TCHAR szSMTPMSExchangeWelcomePattern[]=_T("%I ESMTP Server (Microsoft %T Internet Mail Service %V) ready");

/* Mirapoint: 220 testdrive.mirapoint.com ESMTP Mirapoint 1.1.0; Wed, 28 Feb 2001 04:23:27 -0500 (EST) */
SZXTRN const TCHAR szSMTPMirapointWelcomePattern[]=_T("%I ESMTP %T %V; %D");

/* Lotus: 220 ctilotus.cti2.com ESMTP Service (Lotus Domino Release 5.0.4) ready at Wed, 28 Feb 2001 12:02:37 +0200 */
SZXTRN const TCHAR szSMTPLotusDominoWelcomePattern[]=_T("%I ESMTP Service (Lotus %T Release %V) ready at %D");

/* Commtouch: 220 s0mailgw01.prontomail.co.il ESMTP Service (NPlex 2.0.119) ready */
SZXTRN const TCHAR szSMTPCommtouchWelcomePattern[]=_T("%I ESMTP Service (%T %V) ready");

/* Sendmail: 220 mail2.icomverse.com ESMTP Sendmail 8.9.2/8.9.2; Thu, 8 Mar 2001 10:53:10 +0200 (IST) */
SZXTRN const TCHAR szSMTPSendmailWelcomePattern[]=_T("%I ESMTP %T %V; %D");

/* InterScan: 220 mail1.microsoft.com InterScan VirusWall NT ESMTP 3.24 (build 01/19/2000) ready at Thu, 08 Mar 2001 01:24:14 -0800 (Pacific Standard Time) */
SZXTRN const TCHAR szSMTPInterScanWelcomePattern[]=_T("%I %T %I NT ESMTP %V (build %I) ready at %D");

/* Netscape mail: 220 ipnew_nts.cti2.com ESMTP service (Netscape Messaging Server 4.15  (built Dec 14 1999)) */
SZXTRN const TCHAR szSMTPNSMailWelcomePattern[]=_T("%I ESMTP service (%T Messaging Server %V (built %*))");

/* iPlanet: 220 venus -- Server ESMTP (iPlanet Messaging Server 5.1 (built May  7 2001)) */
SZXTRN const TCHAR szSMTPiPlanetWelcomePattern[]=_T("%I %I Server ESMTP (%T Messaging Server %V (built %*))");

/* iPlanet+HotFix: 220 simba1 -- Server ESMTP (iPlanet Messaging Server 5.2 HotFix 1.02 (built Sep 16 2002)) */
SZXTRN const TCHAR szSMTPiPlanetHotFixWelcomePattern[]=_T("%I %I Server ESMTP (%T Messaging Server %V HotFix %I (built %*))");

/* NOTE !!! the hardcoded name must match the value in "szSMTPXcg2000SrvrName" */
/* Exchange2000: 220 exchange.newcti.com Microsoft ESMTP MAIL Service, Version: 5.0.2195.2966 ready at  Mon, 24 Sep 2001 02:52:54 -0700 */
SZXTRN const TCHAR szSMTPXcg2000WelcomePattern[]=_T("%I Microsoft %T=Exchange ESMTP MAIL Service, Version: %V ready at %D");

/* CommuniGate Pro: 220 cgatepro.cti2.com ESMTP CommuniGate Pro 3.4.8 is glad to see you! */
SZXTRN const TCHAR szSMTPCommuniGateProWelcomePattern[]=_T("%I ESMTP %T %T %V is glad to see you!");

/* NOTE !!! the hardcoded name must match the value in "szSMTPCriticalPathSrvrName" */
/* CriticalPath: 220 cticell.cpload.cti2.com ESMTP Service (6.0.021) ready */
SZXTRN const TCHAR szSMTPCriticalPathWelcomePattern[]=_T("%I ESMTP %T=CriticalPath Service (%V) ready");

SZXTRN LPCTSTR SMTPKnownWelcomePatterns[]={
	szSMTPSWCOMKx4p3WelcomePattern,
	szSMTPMSExchangeWelcomePattern,
	szSMTPMirapointWelcomePattern,
	szSMTPLotusDominoWelcomePattern,
	szSMTPCommtouchWelcomePattern,
	szSMTPSendmailWelcomePattern,
	szSMTPInterScanWelcomePattern,
	szSMTPNSMailWelcomePattern,
	szSMTPiPlanetWelcomePattern,
	szSMTPiPlanetHotFixWelcomePattern,
	szSMTPXcg2000WelcomePattern,
	szSMTPCommuniGateProWelcomePattern,
	szSMTPCriticalPathWelcomePattern,

	NULL	/* mark end */
};

/*---------------------------------------------------------------------------*/
