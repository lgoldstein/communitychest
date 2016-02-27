#include <stdio.h>
#include <stdarg.h>
#include <stdlib.h>
#include <stddef.h>
#include <string.h>
#include <ctype.h>
#include <time.h>
#include <limits.h>

#include <_types.h>
#include <comm/socket.h>

#include <internet/general.h>
#include <internet/pop3Lib.h>

/*
 *		This module implements a generic POP3 server that uses callback functions
 * to interact with the application. It can safely be spawned as a separate
 * thread, but the application must deal with synchronization within its
 * internal data structures once the callback functions are called.
 *
 *		The server implements a "snapshot" view - i.e. the mailbox contents are
 * "loaded" only ONCE during the session and used. This means that the
 * application must handle cases in which the mailbox content changes in the
 * background (e.g. new messages arrive, old messages deleted externally).
 */

/*---------------------------------------------------------------------------*/

/* structure holding application callback functions */
typedef struct tag_pop3_cfns {
	POP3_SESSION_CREATE_CFN		sessCreateCfn;
	void								*sessCreateArg;
	POP3_USER_VALIDATE_CFN		validateCfn;
	POP3_LOAD_MSGS_CFN			loadMsgsCfn;
	POP3_RELEASE_MSGS_CFN		releaseMsgsCfn;
	POP3_SEND_MSG_DATA_CFN		sendDataCfn;
	POP3_SESSION_RELEASE_CFN	sessReleaseCfn;
	POP3_LOG_MSG_CFN				logMsgCfn;
	void								*logMsgArg;
} POP3_CFNS;

/*-------------------------------------------------------------------------*/

/* skips the character 'c' in string 's' (up to end of string) */
static char *strskip (char s[], char c)
{
	char *sP;

	if (NULL == s) return s;
	for ( sP = s; ((*sP) == c) && ((*sP) != '\0'); sP++);

	return sP;
}

/*---------------------------------------------------------------------------*/

/*		reads the password for the specified user and invokes the application's
 * validation callback routine.
 */
static HRESULT validate_user (void							*sessionP,
										SOCKET						sock,
										const char					userName[],
										POP3_USER_VALIDATE_CFN	validateCfn,
										POP3_LOG_MSG_CFN			logMsgCfn,
										void							*logMsgArg)
{
	int iRead;
	char iLine[64], *spaceP=NULL, *passP=NULL;

	/* we read the password in any case BEFORE we check the validity of the user
	 * name in order not to divulge user names.
	 */

	if ((iRead=sockReadCmd(sock, iLine, (sizeof iLine), POP3_DEFAULT_AUTOLOGOUT_TIMEOUT)) <= 0)
	{
		if (logMsgCfn != NULL)
			(*logMsgCfn)(logMsgArg,"Failed to read 'PASS' command");

		if (iRead < 0)
			return ERROR_BAD_NET_RESP;

		if (sockWriteCmdf(sock, "%s illegal / bad input\r\n", POP3_ERR) <= 0)
			return ERROR_BAD_NET_RESP;

		return ERROR_BAD_COMMAND;
	}

	if ((NULL == userName) || ('\0' == userName[0]))
		goto deny_access;

	/* There must be a password argument !!! */
	if (NULL == (spaceP=strchr(iLine, POP3_DELIMITER)))
	{
		if (sockWriteCmdf(sock, "%s Illegal / Unknown input\r\n", POP3_ERR) <= 0)
			return ERROR_BAD_NET_RESP;
		return ERROR_BAD_LOGON_SESSION_STATE;
	}

	/* create a NULL terminated string out of the opcode */
	*spaceP = '\0';

	/* if QUIT-ing then abort the connection */
	if (stricmp(iLine, "QUIT") == 0)
		return ERROR_BAD_NET_RESP;		/* force connection closure */

	/* make sure this is the PASS command */
	if (stricmp(iLine, "PASS") != 0)
	{
		if (sockWriteCmdf(sock, "%s Protocol violation\r\n", POP3_ERR) <= 0)
			return ERROR_BAD_NET_RESP;
		return ERROR_BAD_LOGON_SESSION_STATE;
	}

	/* delimit with '\0' the password string */
	passP = strskip((spaceP+1), POP3_DELIMITER);
	if (NULL != (spaceP=strchr((passP+1), POP3_DELIMITER)))
	{
		*spaceP = '\0';

		/* make sure this is the only argument */
		for (spaceP++; '\0' != (*spaceP); spaceP++)
			if (!isspace(*spaceP))
			{
				if (sockWriteCmdf(sock, "%s Illegal/Unexpected input\r\n", POP3_ERR) <= 0)
					return ERROR_BAD_NET_RESP;

				return ERROR_BAD_COMMAND;
			}
	}

	if (S_OK == (*validateCfn)(sessionP, userName, passP))
		return S_OK;

deny_access:
	if (sockWriteCmdf(sock, "%s access denied\r\n", POP3_ERR) <= 0)
		return ERROR_BAD_NET_RESP;
	return ERROR_INVALID_ACCESS;
}

/*---------------------------------------------------------------------------*/

/* performs the authentication stage (user & password) */

static HRESULT do_pop3_authentication (void							*sessionP,
													SOCKET						sock,
													const char					peerAddress[],
													POP3_USER_VALIDATE_CFN	validateCfn,
													POP3_LOG_MSG_CFN			logMsgCfn,
													void							*logMsgArg)
{
#define POP3_AUTH_MAX_RETRIES	3
	char iLine[64], *spaceP=NULL;
	int iRead;
	UINT32 retriesNum;

	if (gethostname(iLine, (sizeof iLine)) != 0)
		strcpy(iLine, "generic");

	if (sockWriteCmdf(sock, "%s %s POP3 server ready (hello %s)\r\n", POP3_OK, iLine, ((peerAddress) ? peerAddress : "WRU ?")) <= 0)
	{
		if (logMsgCfn != NULL)
			(*logMsgCfn)(logMsgArg, "Failed to send initial hello to %s", peerAddress);
		return ERROR_BAD_NET_RESP;
	}

	/* try authenticating the user up to some max. retries if unsuccessful after
	 * this many retries, return error code (which eventually aborts the
	 * connection).
	 */

	for (retriesNum = 0; retriesNum < POP3_AUTH_MAX_RETRIES; retriesNum++)
	{
		if ((iRead=sockReadCmd(sock, iLine, (sizeof iLine), POP3_DEFAULT_AUTOLOGOUT_TIMEOUT)) <= 0)
		{
			if (logMsgCfn != NULL)
				(*logMsgCfn)(logMsgArg,"Failed to read 'USER' command");
			return ERROR_BAD_NET_RESP;
		}

		/* all allowed commands at this stage (except QUIT) have at least ONE argument */
		if (NULL == (spaceP=strchr(iLine, POP3_DELIMITER)))
		{
			if (stricmp(iLine, "QUIT") == 0)
			{
				sockWriteCmdf(sock, "%s end of session\r\n", POP3_OK);
				return ERROR_BAD_LOGON_SESSION_STATE;
			}

			/* if this is not QUIT then illegal input */
			if (sockWriteCmdf(sock, "%s illegal / unexpected input\r\n", POP3_ERR) <= 0)
				return ERROR_BAD_NET_RESP;
			continue;
		}
		*spaceP = '\0';

		if (stricmp(iLine, "USER") == 0)
		{
			const char *userP=strskip((spaceP+1), POP3_DELIMITER);
			DWORD rCode=S_OK;

			/* make sure user name is supplied */
			if ('\0' == (*userP))
			{
				if (sockWriteCmdf(sock, "%s illegal / unexpected input\r\n", POP3_ERR) <= 0)
					return ERROR_BAD_NET_RESP;
				continue;
			}

			/* create a NULL terminated user name */
			if (NULL != (spaceP=(char *) strchr((userP+1), POP3_DELIMITER)))
			{
				*spaceP = '\0';

				/* make sure no further argument is supplied */
				for (spaceP++; '\0' != *spaceP; spaceP++)
					if (!isspace(*spaceP)) break;

				if ('\0' != (*spaceP))
				{
					if (sockWriteCmdf(sock, "%s illegal / unexpected input\r\n", POP3_ERR) <= 0)
						return ERROR_BAD_NET_RESP;
					continue;
				}
			}

			if (sockWriteCmdf(sock, "%s enter password for %s\r\n", POP3_OK, userP) <= 0)
				return ERROR_BAD_NET_RESP;

			if (S_OK == (rCode=validate_user(sessionP, sock, userP, validateCfn, logMsgCfn, logMsgArg)))
				return S_OK;

			/* if bad network reponse no use in retrying the authentication */
			if (ERROR_BAD_NET_RESP == rCode)
				return rCode;
		}
		else if (stricmp(iLine, "APOP") == 0)
		{
			/* N/A yet */
			if (sockWriteCmdf(sock, "%s access denied\r\n", POP3_ERR) <= 0)
				return ERROR_BAD_NET_RESP;
			continue;
		}
		else if (stricmp(iLine, "QUIT") == 0)
		{
			sockWriteCmdf(sock, "%s end of session\r\n", POP3_OK);
			return ERROR_BAD_LOGON_SESSION_STATE;
		}
		else
		{
			if (sockWriteCmdf(sock, "%s protocol violation\r\n", POP3_ERR) <= 0)
				return ERROR_BAD_NET_RESP;
			continue;
		}
	}

	sockWriteCmdf(sock, "%s too many authentication errors\r\n", POP3_ERR);
	return ERROR_BAD_LOGON_SESSION_STATE;
}

/*---------------------------------------------------------------------------*/

static void undelete_msgs (MSG_DSC_TYPE *msgsP, const UINT32 msgsNum)
{
	MSG_DSC_TYPE *dscP=msgsP;
	UINT32 midx;

	if (NULL == msgsP)
		return;

	for (midx = 0; midx < msgsNum; midx++, dscP++)
		dscP->fIsDelMarked = FALSE;
}

/*---------------------------------------------------------------------------*/

static DWORD do_pop3_stat_command (SOCKET sock, const MSG_DSC_TYPE *msgsP, UINT32 msgsNum)
{
	UINT32 sz=0, mNum=0, midx;
	const MSG_DSC_TYPE *dscP=msgsP;

	for (midx = 0; midx < msgsNum; midx++, dscP++)
		if (!dscP->fIsDelMarked)
		{
			sz += dscP->msgSize;
			mNum++;
		}

	if (sockWriteCmdf(sock, "%s %lu %lu\r\n", POP3_OK, mNum, sz) <= 0)
		return ERROR_BAD_NET_RESP;

	return S_OK;
}

/*---------------------------------------------------------------------------*/

static DWORD do_pop3_list_command (SOCKET					sock,
											  const char			cmdArg[],
											  const MSG_DSC_TYPE *msgsP,
											  const UINT32			msgsNum)
{
	if ((BAD_SOCKET == sock) ||((msgsNum != 0) && (NULL == msgsP)))
		return ERROR_BAD_ARGUMENTS;

	/* if no arguments then list all */
	if ((NULL == cmdArg) || ('\0' == cmdArg[0]))
	{
		const MSG_DSC_TYPE *dscP=msgsP;
		UINT32 midx;

		if (sockWriteCmdf(sock, "%s listing follows\r\n", POP3_OK) <= 0)
			return ERROR_BAD_NET_RESP;

		for (midx = 1; midx <= msgsNum; midx++, dscP++)
			if (!dscP->fIsDelMarked)
				if (sockWriteCmdf(sock, " %lu %lu\r\n", midx, dscP->msgSize) <= 0)
					return ERROR_BAD_NET_RESP;

		/* mark end of multi-line response */
		if (sockWriteCmdf(sock, ".\r\n") <= 0)
			return ERROR_BAD_NET_RESP;
	}
	else	/* specific message requested */
	{
		UINT32 midx=atol(cmdArg);
		const MSG_DSC_TYPE *dscP=NULL;
		int wLen=(-1);

		if ((midx > 0) && (midx <= msgsNum))
		{
			dscP = (msgsP + (midx-1));
			if (dscP->fIsDelMarked)
				dscP = NULL;
		}

		if (NULL == dscP)
			wLen = sockWriteCmdf(sock, "%s no such message (%lu)\r\n", POP3_ERR, midx);
		else
			wLen = sockWriteCmdf(sock, "%s %lu %lu\r\n", POP3_OK, midx, dscP->msgSize);

		if (wLen <= 0)
			return ERROR_BAD_NET_RESP;
	}

	return S_OK;
}

/*---------------------------------------------------------------------------*/

static DWORD do_pop3_dele_command (SOCKET			sock,
											  const char	cmdArg[],
											  MSG_DSC_TYPE *msgsP,
											  const UINT32	msgsNum)
{
	UINT32 midx;
	MSG_DSC_TYPE *dscP=NULL;
	int wLen=(-1);

	if ((BAD_SOCKET == sock) || (NULL == msgsP))
		return ERROR_BAD_ARGUMENTS;

	/* if no arguments then reject command */
	if ((NULL == cmdArg) || ('\0' == cmdArg[0]))
	{
		if (sockWriteCmdf(sock, "%s no message specified\r\n", POP3_ERR) <= 0)
			return ERROR_BAD_NET_RESP;
		return S_OK;	/* we return OK since we want to accept further commands */
	}

	midx = atol(cmdArg);
	if ((midx > 0) && (midx <= msgsNum))
	{
		dscP = (msgsP + (midx-1));
		if (dscP->fIsDelMarked)	/* if already marked for deletion do nothing */
			dscP = NULL;
		else
			dscP->fIsDelMarked = TRUE;
	}

	if (NULL == dscP)	/* bad message or already deleted */
		wLen = sockWriteCmdf(sock, "%s no such valid message (%lu)\r\n", POP3_ERR, midx);
	else
		wLen = sockWriteCmdf(sock, "%s message %lu deleted\r\n", POP3_OK, midx);

	if (wLen <= 0)
		return ERROR_BAD_NET_RESP;
	return S_OK;
}

/*---------------------------------------------------------------------------*/

static DWORD send_pop3_msg_data (SOCKET						sock,
											void							*sessionP,
											MSG_DSC_TYPE				*msgsP,
											const UINT32					msgsNum,
											const char					msgArg[],
											const char					linesArg[],
											POP3_SEND_MSG_DATA_CFN	sendDataCfn)
{
	UINT32 midx=0;
	LONG lnum=(-1);
	MSG_DSC_TYPE *dscP=NULL;
	DWORD retCode=S_OK;

	if ((BAD_SOCKET == sock) || (NULL == msgsP) || (NULL == msgArg) || (NULL == sendDataCfn))
		return ERROR_BAD_ARGUMENTS;

	midx = atol(msgArg);
	if ((midx > 0) && (midx <= msgsNum))
	{
		dscP = (msgsP + (midx - 1));
		if (dscP->fIsDelMarked)
			dscP = NULL;
	}

	if (NULL == dscP)
	{
		if (sockWriteCmdf(sock, "%s no such valid message (%lu)\r\n", POP3_ERR, midx) <= 0)
			return ERROR_BAD_NET_RESP;
		return S_OK;	/* we are willing to accept more messages */
	}

	if (sockWriteCmdf(sock, "%s message follows\r\n", POP3_OK) <= 0)
		return ERROR_BAD_NET_RESP;

	lnum = ((linesArg != NULL) ? atol(linesArg) : (-1));
	if (S_OK == (retCode=(*sendDataCfn)(sessionP, dscP, midx, sock, lnum)))
		if (sockWriteCmdf(sock, ".\r\n") <= 0)
			retCode = ERROR_BAD_NET_RESP;

	/* mark message as read if successfully transmitted ALL of it */
	if (((-1) == lnum) && (S_OK == retCode))
		dscP->fIsReadMarked = TRUE;

	return retCode;
}

/*---------------------------------------------------------------------------*/

static DWORD do_pop3_session (void *sessionP, SOCKET sock, const POP3_CFNS *cfnsP)
{
	char iLine[64];
	int iRead=(-1);
	DWORD retCode=S_OK;
	MSG_DSC_TYPE *msgsP=NULL;
	UINT32 msgsNum=0;
	POP3_LOG_MSG_CFN logMsgCfn=NULL;
	void *logMsgArg=NULL;

	if (NULL == cfnsP)
		return ERROR_BAD_ARGUMENTS;
	logMsgCfn = cfnsP->logMsgCfn;
	logMsgArg = cfnsP->logMsgArg;

	if ((retCode=(*(cfnsP->loadMsgsCfn))(sessionP, &msgsNum, &msgsP)) != S_OK)
	{
		sockWriteCmdf(sock, "%s cannot load user's mailbox\r\n", POP3_ERR);
		return ERROR_BAD_LOGON_SESSION_STATE;
	}
	else
	{
		if (sockWriteCmdf(sock, "%s login accepted\r\n", POP3_OK) <= 0)
			return ERROR_BAD_NET_RESP;
	}

	while (S_OK == retCode)
	{
		char *cmdArg=NULL;

		if ((iRead=sockReadCmd(sock, iLine, (sizeof iLine), POP3_DEFAULT_AUTOLOGOUT_TIMEOUT)) < 0)
		{
			if (logMsgCfn != NULL)
				(*logMsgCfn)(logMsgArg, "Failed to read session command");
			retCode = ERROR_BAD_NET_RESP;
			goto session_exit;
		}

		if (NULL != (cmdArg=strchr(iLine, POP3_DELIMITER)))
		{
			*cmdArg = '\0';
			cmdArg++;
		}
		else
			cmdArg = strchr(iLine, '\0');

		if (stricmp(iLine, "STAT") == 0)
			retCode = do_pop3_stat_command(sock, msgsP, msgsNum);
		else if (stricmp(iLine, "LIST") == 0)
			retCode = do_pop3_list_command(sock, cmdArg, msgsP, msgsNum);
		else if (stricmp(iLine, "RETR") == 0)
			retCode = send_pop3_msg_data(sock, sessionP, msgsP, msgsNum, cmdArg, NULL, cfnsP->sendDataCfn);
		else if (stricmp(iLine, "DELE") == 0)
			retCode = do_pop3_dele_command(sock, cmdArg, msgsP, msgsNum);
		else if (stricmp(iLine, "NOOP") == 0)
		{
			/* respond to "no-operation" */
			if (sockWriteCmdf(sock, "%s\r\n", POP3_OK) <= 0)
				retCode = ERROR_BAD_NET_RESP;
		}
		else if (stricmp(iLine, "RSET") == 0)
		{
			undelete_msgs(msgsP, msgsNum);
			if (sockWriteCmdf(sock, "%s all messages unmarked\r\n", POP3_OK) <= 0)
				retCode = ERROR_BAD_NET_RESP;
		}
		else if (stricmp(iLine, "TOP") == 0)
		{
			char *lNumP=strchr(cmdArg, POP3_DELIMITER);

			if (NULL == lNumP)
			{
				if (sockWriteCmdf(sock, "%s missing lines num argument\r\n", POP3_ERR) <= 0)
					retCode = ERROR_BAD_NET_RESP;
				continue;
			}
			*lNumP = '\0';
			lNumP++;

			retCode = send_pop3_msg_data(sock, sessionP, msgsP, msgsNum, cmdArg, lNumP, cfnsP->sendDataCfn);
		}
		else if (stricmp(iLine, "UIDL") == 0)
		{
			if (sockWriteCmdf(sock, "%s command N/A\r\n", POP3_ERR) <= 0)
				retCode = ERROR_BAD_NET_RESP;
			continue;
		}
		else if (stricmp(iLine, "HELP") == 0)
		{
			if (sockWriteCmdf(sock, "%s command N/A\r\n", POP3_ERR) <= 0)
				retCode = ERROR_BAD_NET_RESP;
			continue;
		}
		else if (stricmp(iLine, "QUIT") == 0)
		{
			sockWriteCmdf(sock, "%s bye\r\n", POP3_OK);
			break;
		}
		else if ((stricmp(iLine, "USER") == 0) ||
					(stricmp(iLine, "PASS") == 0) ||
					(stricmp(iLine, "APOP") == 0))
		{
			/* authentication commands at this stage are illegal */
			if (sockWriteCmdf(sock, "%s protocol violation\r\n", POP3_ERR) <= 0)
				retCode = ERROR_BAD_NET_RESP;
			continue;
		}
		else	/* no such command... */
		{
			sockWriteCmdf(sock, "%s unknown command: %s\r\n", POP3_ERR, iLine);
		}
	}

session_exit:
	/* since connection is aborted, we undelete all messages */
	if (retCode != S_OK)
		undelete_msgs(msgsP, msgsNum);

	if (msgsP != NULL)
	{
		(*(cfnsP->releaseMsgsCfn))(sessionP, msgsNum, msgsP);
		msgsP = NULL;
	}

	return retCode;
}

/*---------------------------------------------------------------------------*/

/* structure used to pass parameters to a POP3 session handler */
typedef struct pop3_handler_params {
	SOCKET		sock;
	HANDLE		countSem;
	char			peerAddress[64];
	POP3_CFNS	cfns;
} POP3_HNDLR_PARAMS;

/*---------------------------------------------------------------------------*/

/* this is the entry point for the thread which handles ONE POP3 session (i.e. client) */
static DWORD WINAPI do_pop3_handler (LPVOID arg)
{
	const POP3_HNDLR_PARAMS *hParamsP=(const POP3_HNDLR_PARAMS *) arg;
	void *sessionP=NULL;	/* application session handle */
	DWORD retCode=S_OK;

	if (NULL == hParamsP)
		return ERROR_BAD_ARGUMENTS;

	/* let application create its session structure */
	if((retCode=(*(hParamsP->cfns.sessCreateCfn))(&sessionP, hParamsP->cfns.sessCreateArg, hParamsP->peerAddress)) != S_OK)
		goto handler_exit;

	/* create timer monitoring task to enforce inactivity auto-logout */

	/* authenticate user */
	if ((retCode=do_pop3_authentication(sessionP, hParamsP->sock, hParamsP->peerAddress, hParamsP->cfns.validateCfn, hParamsP->cfns.logMsgCfn, hParamsP->cfns.logMsgArg)) != S_OK)
		goto handler_exit;

	if ((retCode=do_pop3_session(sessionP, hParamsP->sock, &(hParamsP->cfns))) != S_OK)
		goto handler_exit;

handler_exit:	/* clean up */
	if (hParamsP->cfns.logMsgCfn != NULL)
		(*(hParamsP->cfns.logMsgCfn))(hParamsP->cfns.logMsgArg,"Finished session with %s", hParamsP->peerAddress);

	sockClose(hParamsP->sock);

	if (hParamsP->countSem != NULL)
		if (!ReleaseSemaphore(hParamsP->countSem, 1, NULL))
			if (hParamsP->cfns.logMsgCfn != NULL)
				(*(hParamsP->cfns.logMsgCfn))(hParamsP->cfns.logMsgArg,"ERR: Unable to signal session handler over: %d", GetLastError());

	(*(hParamsP->cfns.sessReleaseCfn))(sessionP);
	sessionP = NULL;

#ifdef __cplusplus
	free(arg);
#else
	delete hParamsP;
#endif
	hParamsP = NULL; /* just cleaning up */

	return retCode;
}

/*---------------------------------------------------------------------------*/

typedef struct tag_pop3_srvr_params {
	int			portNum;
	UINT32			cMaxClients;	/* if 1 then no need to spawn client threads */
	POP3_CFNS	cfns;
} POP3_SRVR_PARAMS;

/*---------------------------------------------------------------------------*/
/* this routine accepts incoming connections from clients and spawns (if necessary) a thread
 * to handle each client (up to the specified max number of clients.
 */

static DWORD WINAPI do_pop3_server (LPVOID arg)
{
	const POP3_SRVR_PARAMS *sParamsP=(const POP3_SRVR_PARAMS *) arg;
	POP3_LOG_MSG_CFN logMsgCfn=NULL;
	void *logMsgArg;
	SOCKET sock=BAD_SOCKET;
	DWORD retCode=S_OK;
	HANDLE countSem=NULL;

	if (NULL == sParamsP)
		return ERROR_BAD_ARGUMENTS;

	logMsgCfn = sParamsP->cfns.logMsgCfn;
	logMsgArg = sParamsP->cfns.logMsgArg;

	if (sock_server_setup(&sock, sParamsP->portNum) != EOK)
	{
		if (logMsgCfn != NULL)
			(*logMsgCfn)(logMsgArg,
							 "Cannot create listening socket on %u",
							 sParamsP->portNum);

		retCode = ERROR_BAD_DESCRIPTOR_FORMAT;
		goto server_exit;
	}

	/* if more than one client allowed, then create a counting semaphore used to limit
	 * the number of clients. This is done by creating the semaphore with "cMaxClients"
	 * units initially, and taking one unit for each client. Once the client session is
	 * over, the client's thread sends back the unit, thus enabling another client session
	 * to occur.
	 */

	if (sParamsP->cMaxClients > 1)
	{
		if (NULL == (countSem=CreateSemaphore(NULL, sParamsP->cMaxClients, sParamsP->cMaxClients, NULL)))
		{
			retCode = GetLastError();
			goto server_exit;
		}
	}

	/* forever loop */
	for ( ; ; )
	{
		struct sockaddr_in s;
		struct sockaddr *sP=(struct sockaddr *) &s;
		int sLen=(sizeof s);
		SOCKET nsock=BAD_SOCKET;
		POP3_HNDLR_PARAMS *hParamsP=NULL;

		/* check that max clients limit not exceeded */
		if (countSem != NULL)
			if (WAIT_FAILED == WaitForSingleObject(countSem, INFINITE))
			{
				if (logMsgCfn != NULL)
					(*logMsgCfn)(logMsgArg, "ERR: Failed to wait indefinitely for client unit: %d", GetLastError());
				goto server_exit;
			}

		/* await client connection request */
		if ((int) (nsock=accept(sock, sP, &sLen)) <= 0)
		{
			if (logMsgCfn != NULL)
				(*logMsgCfn)(logMsgArg, "ERR: Failed to accept new connection");
			retCode = ERROR_BAD_NET_RESP;
			goto server_exit;
		}

		if (logMsgCfn != NULL)
			(*logMsgCfn)(logMsgArg, "Accepted connection from %s", inet_ntoa(s.sin_addr));

		/* create & initialize structure used to pass parameters to handler thread */
#ifdef __cplusplus
		hParamsP = new POP3_HNDLR_PARAMS;
#else
		hParamsP = (POP3_HNDLR_PARAMS *) malloc(sizeof(POP3_HNDLR_PARAMS));
#endif
		if (NULL == hParamsP)
		{
			if (logMsgCfn != NULL)
				(*logMsgCfn)(logMsgArg, "ERR: Cannot allocate handler params for %s\n", inet_ntoa(s.sin_addr));

			sockClose(nsock);
			retCode = ERROR_NO_SYSTEM_RESOURCES;
			goto server_exit;
		}

		/* create a handler's parameters structure and pass it to the handler thread */
		memset(hParamsP, 0, (sizeof *hParamsP));
		hParamsP->sock = nsock;
		hParamsP->countSem = countSem;
		strcpy(hParamsP->peerAddress, inet_ntoa(s.sin_addr));
		hParamsP->cfns = sParamsP->cfns;

		/* if more than 1 client allowed, spawn a thread to handle this client, otherwise
		 * perform the POP3 protocol within this thread's context - no need to spawn a thread
		 * for just 1 client.
		 */

		if (sParamsP->cMaxClients > 1)
		{
			DWORD threadId=(DWORD) (-1);
			HANDLE threadHandle=CreateThread(NULL, 0, do_pop3_handler, (LPVOID) hParamsP, 0, &threadId);

			if (NULL == threadHandle)
			{
				if (logMsgCfn != NULL)
					(*logMsgCfn)(logMsgArg, "ERR: Cannot create POP3 handler thread: ERR=%d", GetLastError());

#ifdef __cplusplus
				delete hParamsP;
#else
				free(hParamsP);
#endif
				sockClose(nsock);
				if (!ReleaseSemaphore(hParamsP->countSem, 1, NULL))
					if (logMsgCfn != NULL)
						(*logMsgCfn)(logMsgArg,  "ERR: Unable to return client unit: %d", GetLastError());

				continue;
			}

			CloseHandle(threadHandle);
		}
		else
			do_pop3_handler(hParamsP);

	}

server_exit:
	if (sock != BAD_SOCKET)
	{
		sockClose(sock);
		sock = BAD_SOCKET;
	}

	if (countSem != NULL)
	{
		CloseHandle(countSem);
		countSem = NULL;
	}

	if (arg != NULL)
#ifdef __cplusplus
		delete ((POP3_SRVR_PARAMS *) sParamsP);
#else
		free(arg);
#endif

	sParamsP = NULL;
	return retCode;
}

/*---------------------------------------------------------------------------*/

/*		if "portName" NULL or empty then default port (110) is used.
 *		if "portName" numeric than this number is used.
 *		if "portName" is a string, then "getservbyname" is used (e.g.: "pop3").
 */

HRESULT POP3_server_create (const char						portName[],
									 POP3_SESSION_CREATE_CFN	sessCreateCfn,
									 void								*sessCreateArg,
									 POP3_USER_VALIDATE_CFN		validateCfn,
									 POP3_LOAD_MSGS_CFN			loadMsgsCfn,
									 POP3_RELEASE_MSGS_CFN		releaseMsgsCfn,
									 POP3_SEND_MSG_DATA_CFN		sendDataCfn,
									 POP3_SESSION_RELEASE_CFN	sessReleaseCfn,
									 POP3_LOG_MSG_CFN				logMsgCfn,
									 void								*logMsgArg,
									 UINT32							cMaxClients)
{
	int portNum=(-1);
	POP3_SRVR_PARAMS *sParamsP=NULL;

	if (0 == cMaxClients)
		return ERROR_BAD_ARGUMENTS;

	if (NULL == portName)
		portNum = IPPORT_POP3;
	else if ((portNum=port_string2value(portName)) <= 0)
	{
		if (logMsgCfn != NULL)
			(*logMsgCfn)(logMsgArg,
							 "Cannot resolve port %s",
							 ((portName) ? portName : ""));
		return ERROR_BAD_NET_NAME;
	}

#ifdef __cplusplus
	sParamsP = new POP3_SRVR_PARAMS;
#else
	sParamsP = (POP3_SRVR_PARAMS *) malloc(sizeof(POP3_SRVR_PARAMS));
#endif
	if (NULL == sParamsP)
	{
		if (logMsgCfn != NULL)
			(*logMsgCfn)(logMsgArg, "Cannot create POP3 server params record");
		return ERROR_NO_SYSTEM_RESOURCES;
	}

	memset(sParamsP, 0, (sizeof *sParamsP));
	sParamsP->portNum = portNum;
	sParamsP->cMaxClients = cMaxClients;
	if ((NULL == (sParamsP->cfns.sessCreateCfn=sessCreateCfn))	||
		 (NULL == (sParamsP->cfns.validateCfn=validateCfn))		||
		 (NULL == (sParamsP->cfns.loadMsgsCfn=loadMsgsCfn))		||
		 (NULL == (sParamsP->cfns.releaseMsgsCfn=releaseMsgsCfn))||
		 (NULL == (sParamsP->cfns.sendDataCfn=sendDataCfn))		||
		 (NULL == (sParamsP->cfns.sessReleaseCfn=sessReleaseCfn)))
		return ERROR_BAD_ARGUMENTS;

	sParamsP->cfns.sessCreateArg = sessCreateArg;
	sParamsP->cfns.logMsgCfn = logMsgCfn;
	sParamsP->cfns.logMsgArg = logMsgArg;

	if (cMaxClients > 1)
	{
		DWORD threadId=(DWORD) (-1);
		HANDLE threadHandle=CreateThread(NULL, 0, do_pop3_server, (LPVOID) sParamsP, 0, &threadId);

		if (NULL == threadHandle)
		{
			DWORD err=GetLastError();

			if (logMsgCfn != NULL)
				(*logMsgCfn)(logMsgArg, "Cannot create POP3 server thread");
#ifdef __cplusplus
			delete sParamsP;
#else
			free(sParamsP);
#endif
			return err;
		}

		CloseHandle(threadHandle);
	}
	else
		do_pop3_server(sParamsP);

	return S_OK;
}

/*---------------------------------------------------------------------------*/
