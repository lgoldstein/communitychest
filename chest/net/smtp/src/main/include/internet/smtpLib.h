#ifndef _SMTP_LIB_H_
#define _SMTP_LIB_H_

/*---------------------------------------------------------------------------*/
/* usefule definitions and utilities associated with the SMTP protocol */
/*---------------------------------------------------------------------------*/

#include <stdio.h>

#include <_types.h>
#include <comm/socket.h>
#include <internet/rfc822msg.h>

/*---------------------------------------------------------------------------*/

/* in seconds */
#define DEFAULT_SMTP_TIMEOUT		30UL

/* take into account CRLF at end + reserved space */
#define MAX_SMTP_OPCODE_LEN		16UL
#define MAX_SMTP_CMD_LINE_LEN		 (512UL + 6UL)
#define MAX_SMTP_DATA_LINE_LEN	(1024UL + 6UL)

/*---------------------------------------------------------------------------*/

extern const TCHAR szSMTPHeloCmd[];
extern const TCHAR szSMTPMailFromCmd[];
extern const TCHAR szSMTPRcptToCmd[];
extern const TCHAR szSMTPDataCmd[];
extern const TCHAR szSMTPQuitCmd[];
extern const TCHAR szSMTPNoopCmd[];
extern const TCHAR szSMTPRsetCmd[];
extern const TCHAR szSMTPHelpCmd[];

#ifdef __cplusplus
extern EXC_TYPE smtpParseCmd (LPCTSTR	lpszLine,
										LPCTSTR&	lpszOp,
										UINT32&	ulOpLen,
										LPCTSTR&	lpszArg,
										UINT32&	ulArgLen,
										LPCTSTR&	lpszCont);
#endif

/*---------------------------------------------------------------------------*/

/* error/response codes */

#define SMTP_E_SYS_STAT    211 /* System status, or system help reply */
#define SMTP_E_HELP_MSG		214 /* Help message */
#define SMTP_E_DOMAIN_RDY  220 /* <domain> Service ready */
#define SMTP_E_DOMAIN_CLS  221 /* <domain> Service closing transmit channel */
#define SMTP_E_ACTION_OK   250 /* Requested mail action okay, completed */
#define SMTP_E_USR_NLOCAL  251 /* User not local; will forward to <path> */

#define is_bad_smtp_rcode(c) (((c) < 200) || ((c) >= 300))

#define SMTP_E_START_INP   354 /* Start mail input; end with <CRLF>.<CRLF> */
          
#define SMTP_E_SRVC_NA     421 /* <domain> Service not available */
#define SMTP_E_ACTION_NA   450 /* Requested mail action not taken */
#define SMTP_E_ACTION_ABRT 451 /* Requested action aborted */
#define SMTP_E_ACTION_MEM  452 /* insufficient system storage */

#define SMTP_E_SYNTAX      500 /* Syntax error, command unrecognized */
#define SMTP_E_PARAMS      501 /* Syntax error in parameters or arguments */
#define SMTP_E_CMD_NA      502 /* Command not implemented */
#define SMTP_E_BAD_CMD_SEQ 503 /* Bad sequence of commands */
#define SMTP_E_BAD_PARAM   504 /* Command parameter not implemented */
#define SMTP_E_MBOX_NA     550 /* mailbox unavailable */
#define SMTP_E_USR_TRY     551 /* User not local; please try <forward-path> */
#define SMTP_E_MEM_OVRFLW  552 /* exceeded storage allocation */
#define SMTP_E_MBOX_NAME   553 /* mailbox name not allowed */
#define SMTP_E_TRAN_FAILED 554 /* Transaction failed */

/*---------------------------------------------------------------------------*/

/*		The possible states of the SMTP protocol - we switch to a state AFTER
 * the appropriate command has been seen.
 */

typedef enum e_smtp_proto_state {
	SMTP_CONNECTED_STATE,	/* before HELO */
	SMTP_HELO_STATE,			/* after HELO */
	SMTP_MAILFROM_STATE,		/* after MAIL FROM */
	SMTP_RCPTTO_STATE,		/* after RCPT TO */
	SMTP_DATA_STATE,			/* during DATA state */
	SMTP_QUIT_STATE,			/* after QUIT */
	SMTP_BAD_PROTO_STATE
} SMTP_PROTO_STATE;

#define fIsBadSmtpProtoState(s) \
	(((unsigned) (s)) >= ((unsigned) SMTP_BAD_PROTO_STATE))

/*		Returns TRUE if OK to disconnect from either side
 *
 * Note: we allow disconnect after DATA state (although not standard)
 */
extern BOOLEAN IsSMTPDisconnectOk (const SMTP_PROTO_STATE s);

/*---------------------------------------------------------------------------*/

/* required on some systems (e.g. VxWorks) to "draw" in the module */
extern EXC_TYPE smtpInit (const BOOLEAN verbOn);

/*---------------------------------------------------------------------------*/

/* Note: return ECONTINUED if not final code */
extern EXC_TYPE smtpGetResponse (const char szRsp[], UINT32 *rcode);

/* read response - which might be multi-line */
extern EXC_TYPE smtpGetFinalResponse (SOCKET			sock,
												  char			szLine[],
												  const size_t	sMaxLen,
												  UINT32			*rcode);

#ifdef __cplusplus
/* read response - which might be multi-line */
extern EXC_TYPE smtpGetFinalResponse (ISockioInterface&	ISock,
												  char					szLine[],
												  const size_t			sMaxLen,
												  UINT32&				rcode,
												  const UINT32			ulRspTimeout);
#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/

/*		Opens an SMTP socket to the specified host using the specified port. If
 * port if unspecified (i.e. <= 0) then "getservbyname" is used. If this fails
 * then the default IPPORT_SMTP (25) is used.
 *
 *		If successful, then returns the code in the server`s greeting line.
 */
extern EXC_TYPE smtpOpenSock (const char	hname[],
										const int	pnum,
										SOCKET		*pSock,
										UINT32		*rcode);

#ifdef __cplusplus
extern EXC_TYPE smtpOpenSock (ISockioInterface&	ISock,
										const char			hname[],
										const int			pnum,
										UINT32&				rcode,
										LPTSTR				lpszRsp,
										const UINT32		ulMaxLen,
										const UINT32		ulRspTimeout);

extern EXC_TYPE smtpOpenSock (ISockioInterface&	ISock,
										const char			hname[],
										const int			pnum,
										UINT32&				rcode,
										const UINT32		ulRspTimeout);
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

/*		Builds & sends a command using the supplied argument (if any) and reads
 * back the response code. Note: skips multi-line responses and returns code
 * only from last line (in accordance with RFC821).
 */
extern EXC_TYPE smtpSendCmdSync (SOCKET		sock,
											const char	cmd[],
											const char	arg[],	/* may be NULL */
											UINT32		*rcode);

#ifdef __cplusplus
extern EXC_TYPE smtpSendCmdSync (ISockioInterface&	ISock,
											const char			cmd[],
											const char			arg[],	/* may be NULL */
											UINT32&				rcode,
											char					szRspBuf[],
											const UINT32		ulMaxLen,
											const UINT32		ulRspTimeout);

extern EXC_TYPE smtpSendCmdSync (ISockioInterface&	ISock,
											const char			cmd[],
											const char			arg[],	/* may be NULL */
											UINT32&				rcode,
											const UINT32		ulRspTimeout);
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

extern EXC_TYPE smtpSendHelo (SOCKET s, const char id[], UINT32 *rcode);
extern EXC_TYPE smtpSendNoop (SOCKET s, UINT32 *rcode);
extern EXC_TYPE smtpSendReset (SOCKET s, UINT32 *rcode);
extern EXC_TYPE smtpAddRecepient (SOCKET s, const char rcpt[], UINT32 *rcode);
extern EXC_TYPE smtpSetSender (SOCKET s, const char sndr[], UINT32 *rcode);
extern EXC_TYPE smtpStartData (SOCKET s, UINT32 *rcode);
extern EXC_TYPE smtpQuit (SOCKET s, UINT32 *rcode);

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
extern EXC_TYPE smtpSendHelo (ISockioInterface&	ISock, const char id[], UINT32& rcode, char szRspBuf[], const UINT32 ulMaxLen, const UINT32 ulRspTimeout);
extern EXC_TYPE smtpSendHelo (ISockioInterface&	ISock, const char id[], UINT32& rcode, const UINT32 ulRspTimeout);

extern EXC_TYPE smtpSetSender (ISockioInterface& ISock, const char sndr[], UINT32& rcode, char szRspBuf[], const UINT32 ulMaxLen, const UINT32 ulRspTimeout);
extern EXC_TYPE smtpSetSender (ISockioInterface& ISock, const char sndr[], UINT32& rcode, const UINT32 ulRspTimeout);

extern EXC_TYPE smtpAddRecepient (ISockioInterface& ISock, const char rcpt[], UINT32& rcode, char szRspBuf[], const UINT32 ulMaxLen, const UINT32 ulRspTimeout);
extern EXC_TYPE smtpAddRecepient (ISockioInterface& ISock, const char rcpt[], UINT32& rcode, const UINT32 ulRspTimeout);

inline EXC_TYPE smtpStartData (ISockioInterface& ISock, UINT32& rcode, char szRspBuf[], const UINT32 ulMaxLen, const UINT32 ulRspTimeout)
{
	return smtpSendCmdSync(ISock, szSMTPDataCmd, NULL, rcode, szRspBuf, ulMaxLen, ulRspTimeout);
}

inline EXC_TYPE smtpStartData (ISockioInterface& ISock, UINT32& rcode, const UINT32 ulRspTimeout)
{
	return smtpSendCmdSync(ISock, szSMTPDataCmd, NULL, rcode, ulRspTimeout);
}

inline EXC_TYPE smtpSendNoop (ISockioInterface&	ISock, UINT32& rcode, char szRspBuf[], const UINT32 ulMaxLen, const UINT32 ulRspTimeout)
{
	return smtpSendCmdSync(ISock, szSMTPNoopCmd, NULL, rcode, szRspBuf, ulMaxLen, ulRspTimeout);
}

inline EXC_TYPE smtpSendNoop (ISockioInterface&	ISock, UINT32& rcode, const UINT32 ulRspTimeout)
{
	return smtpSendCmdSync(ISock, szSMTPNoopCmd, NULL, rcode, ulRspTimeout);
}

inline EXC_TYPE smtpSendReset (ISockioInterface&	ISock, UINT32& rcode, char szRspBuf[], const UINT32 ulMaxLen, const UINT32 ulRspTimeout)
{
	return smtpSendCmdSync(ISock, szSMTPRsetCmd, NULL, rcode, szRspBuf, ulMaxLen, ulRspTimeout);
}

inline EXC_TYPE smtpSendReset (ISockioInterface&	ISock, UINT32& rcode, const UINT32 ulRspTimeout)
{
	return smtpSendCmdSync(ISock, szSMTPRsetCmd, NULL, rcode, ulRspTimeout);
}

extern EXC_TYPE smtpQuit (ISockioInterface& ISock, UINT32& rcode, const UINT32 ulRspTimeout);
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

/* performs protocol up to specifying recipients */
extern EXC_TYPE smtpBasicSockOpen (const char	host[],
											  const int		port,
											  const char	fromUsr[],
											  SOCKET			*pSock);

#ifdef __cplusplus
extern EXC_TYPE smtpBasicSockOpen (ISockioInterface&	ISock,
											  const char			host[],
											  const int				port,
											  const char			fromUsr[],
											  const UINT32			ulRspTimeout);
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

/* opens SMTP socket and prepares it for sending a message */
extern EXC_TYPE smtpDataSockOpen (const char	host[],
											 const int	port,
											 const char	fromUsr[],
											 const char	**tusrs,
											 SOCKET		*pSock);

#ifdef __cplusplus
extern EXC_TYPE smtpDataSockOpen (ISockioInterface&	ISock,
											 const char				host[],
											 const int				port,
											 const char				fromUsr[],
											 const char				**tusrs,
											 const UINT32			ulRspTimeout);
#endif	/* of ifdef __cplusplus */

/* closes the SMTP session (by sending QUIT command) - if "add_crlf" is TRUE
 * the write '\n' prior to closing (see rfc821)
 */

extern EXC_TYPE smtpDataSockClose (SOCKET s, const BOOLEAN add_crlf);

#ifdef __cplusplus
extern EXC_TYPE smtpDataSockClose (ISockioInterface&	ISock,
											  const BOOLEAN		add_crlf,
											  UINT32&				rcode,
											  LPTSTR					lpszRspBuf,
											  const UINT32			ulMaxLen,
											  const UINT32			ulRspTimeout);

extern EXC_TYPE smtpDataSockClose (ISockioInterface&	ISock,
											  const BOOLEAN		add_crlf,
											  UINT32&				rcode,
											  const UINT32			ulRspTimeout);
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

extern EXC_TYPE smtpSendAttachment (SOCKET					sock,
												const char				pszFName[], /* NULL == last comp */
												const char				pszFPath[],
												const char				pszMIMEBoundary[],	/* NULL == direct attchment */
												const BOOLEAN			fIsFirst,
												const BOOLEAN			fIsLast,
												const char				pszMIMEType[], /* may be NULL */
												const char				pszMIMESubType[], /* may be NULL */
												const RFC822ENCCASE	eEncoding);

#ifdef __cplusplus
extern EXC_TYPE smtpSendAttachment (ISockioInterface&		ISock,
												const char				pszFName[], /* NULL == last comp */
												const char				pszFPath[],
												const char				pszMIMEBoundary[],	/* NULL == direct attchment */
												const BOOLEAN			fIsFirst,
												const BOOLEAN			fIsLast,
												const char				pszMIMEType[], /* may be NULL */
												const char				pszMIMESubType[], /* may be NULL */
												const RFC822ENCCASE	eEncoding);

extern EXC_TYPE smtpSendAttachment (ISockioInterface&		ISock,
												const CAttachInfo&	attInfo,
												const char				pszMIMEBoundary[],	/* NULL == direct attchment */
												const BOOLEAN			fIsFirst,
												const BOOLEAN			fIsLast);
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// Note: assumes handshake + DATA completed - returns whether EOM pattern found in file
extern EXC_TYPE smtpSendMsgFile (ISockioInterface&	ISock, FILE *fp, BOOLEAN& fHasEOM);
extern EXC_TYPE smtpSendMsgFile (ISockioInterface&	ISock, LPCTSTR lpszFilePath, BOOLEAN& fHasEOM);
#endif	/* of ifdef __cplusplus */

/*		Callback which is called for each intercepted line. The user may change
 * the contents of the input line (up to the specified "sMaxLineLen"), in
 * which case the value of "psLineLen" must be adjusted accordingly.
 *
 * If TRUE is returned, then (possibly changed) line is output to socket.
 */

typedef BOOL (*SMTP_MON_CFN)(
#ifdef __cplusplus
				ISockioInterface&	rsock,
				ISockioInterface&	wsock,
#else
				SOCKET				rsock,	/* socket for reading */
				SOCKET				wsock,	/* socket for writing */
#endif	/* of ifdef __cplusplus */
				const BOOL			fIsServerLine,
				char					pszLine[],
				int					*piLineLen,	/* in/out, (-1)=ERR */
				const size_t		sMaxLineLen,
				const BOOL			fIsBody,	/* message body */
				void					*pArg);	/* user supplied argument */

/*
 *		Callback used to pass information about a new RFC822 header. This
 * includes the "X-RCPT-TO:" and "X-MAIL-FROM:" SMTP commands. The header is
 * passed including the ':' - e.g. "Subject: this is a subject", then
 * header is "Subject:" and value is "this is a subject".
 *
 *		The SAME argument is passed both to the monitoring callback and the
 * headers one (if any). If returns non-EOK code, then connection is aborted.
 */
typedef EXC_TYPE (*SMTP_HDR_CFN)(const char	pszHdr[],
											const char	pszVal[],
											LPVOID		pArg);

/*---------------------------------------------------------------------------*/

extern EXC_TYPE smtpCreatePipe (const UINT32	ulMaxClients,
										  const int		iAwaitPortNum,
										  const int		iConnectPortNum,
										  const LONG	lSrvrReadTimeout,
										  const LONG	lClntReadTimeout,
										  const char	szServerAddress[],
										  SMTP_MON_CFN	lpfnMcfn,
										  SMTP_HDR_CFN	lpfnHcfn,
										  LPVOID			pArg);

/*---------------------------------------------------------------------------*/

typedef struct {
	LPCTSTR	lpszSMTPHost;
	int		iSMTPPort;		// 0 == use default
	LPCTSTR	lpszSMTPSndr;	// if NULL/empty then auto-detected from retrieved msg
	LPCTSTR	lpszSMTPRecip;

	LPCTSTR	lpszSMTPAuthUID;	// if not NULL/empty then ESMTP AUTH handshake performed
	LPCTSTR	lpszSMTPAuthPass;	// if not NULL/empty then ESMTP AUTH handshake performed

	UINT32	ulRspTimeout;
} SMTPMSGRELAYDEF, *LPSMTPMSGRELAYDEF;

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// relays message to SMTP server
class CSMTPMsgRelay : public IMsgRelayStream {
	private:
		CBuffSock			m_CBSock;
		CRFC822MsgEOM		m_rme;
		SMTPMSGRELAYDEF	m_rcpDef;
		UINT32				m_ulCurMsg;
		UINT32				m_ulDetectBufSize;
		UINT32				m_ulCurDetect;
		LPTSTR				m_lpszDetectBuf;
		BOOLEAN				m_fSingleConn;
		BOOLEAN				m_fAutoDetectSndr;

		EXC_TYPE Cleanup ();

		EXC_TYPE RestartSMTPSession (LPCTSTR lpszSndr);

		EXC_TYPE StartMsgRetrieval (const UINT32 ulMsgNo);

		EXC_TYPE StopMsgRetrieval (const UINT32 ulMsgNo);

		// disable copy constructor and assignment operator
		CSMTPMsgRelay (const CSMTPMsgRelay& );
		CSMTPMsgRelay& operator= (const CSMTPMsgRelay& );

	public:
		CSMTPMsgRelay ();

		EXC_TYPE Reset ();

		// Note: if auto-sender option used then relay must be performed with msg parsing option enabled !!!
		EXC_TYPE SetInfo (const SMTPMSGRELAYDEF& rcpDef,
								const BOOLEAN				fSingleConn=FALSE,
								const UINT32				ulDetectBufSize=(4 * 1024));	// used for auto-detect

		// Only start/stop of message is handled
		virtual EXC_TYPE HandleProtoState (LPCTSTR		lpszProtoCmd,
													  const UINT32	ulMsgNo,
													  LPCTSTR		lpszCmdVal);

		// Note(s):
		//
		//		a. called BEFORE writing the data
		//		b. called only if msg parsing requested
		virtual EXC_TYPE HandleMsgContents (const UINT32				ulMsgNo,
														const RFC822MSGEXCASE	mxCase,
														CRFC822MsgExtractor&		msgEx);

		virtual int Write (LPCTSTR lpszData, const size_t dLen);

		virtual ~CSMTPMsgRelay () { Cleanup(); }
};
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

/* returns EIOUNCLASS if no match found */
extern EXC_TYPE smtpAnalyzeWelcomePattern (LPCTSTR			lpszWelcome,
														 LPCTSTR			lpszWPattern,
														 LPTSTR			lpszType,
														 const UINT32	ulMaxTypeLen,
														 LPTSTR			lpszVersion,
														 const UINT32	ulMaxVerLen);

#ifdef __cplusplus
inline EXC_TYPE smtpAnalyzeWelcome (LPCTSTR			lpszWelcome,
												LPCTSTR			lpszPatterns[],
												LPTSTR			lpszType,
												const UINT32	ulMaxTypeLen,
												LPTSTR			lpszVersion,
												const UINT32	ulMaxVerLen)
{
	return inetAnalyzeWelcome(lpszWelcome, lpszPatterns,
									  smtpAnalyzeWelcomePattern,
									  lpszType, ulMaxTypeLen,
									  lpszVersion, ulMaxVerLen);
}
#else
#define smtpAnalyzeWelcome(w,p,t,tl,v,vl)	\
	inetAnalyzeWelcome(w,p,smtpAnalyzeWelcomePattern,t,tl,v,vl)
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

extern const TCHAR szSMTPSWCOMKx4p3WelcomePattern[];
extern const TCHAR szSMTPMSExchangeWelcomePattern[];
extern const TCHAR szSMTPMirapointWelcomePattern[];
extern const TCHAR szSMTPLotusDominoWelcomePattern[];
extern const TCHAR szSMTPCommtouchWelcomePattern[];
extern const TCHAR szSMTPSendmailWelcomePattern[];
extern const TCHAR szSMTPInterScanWelcomePattern[];
extern const TCHAR szSMTPNSMailWelcomePattern[];
extern const TCHAR szSMTPiPlanetWelcomePattern[];
extern const TCHAR szSMTPiPlanetHotFixWelcomePattern[];
extern const TCHAR szSMTPXcg2000WelcomePattern[];
extern const TCHAR szSMTPCommuniGateProWelcomePattern[];
extern const TCHAR szSMTPCriticalPathWelcomePattern[];

extern LPCTSTR SMTPKnownWelcomePatterns[];

/*---------------------------------------------------------------------------*/

/* some known server's names */
extern const TCHAR szSMTPInterMailSrvrName[];
extern const TCHAR szSMTPMSExchangeSrvrName[];
extern const TCHAR szSMTPMirapointSrvrName[];
extern const TCHAR szSMTPLotusDominoSrvrName[];
extern const TCHAR szSMTPCommtouchSrvrName[];
extern const TCHAR szSMTPSendmailSrvrName[];
extern const TCHAR szSMTPInterScanSrvrName[];
extern const TCHAR szSMTPNSMailSrvrName[];
extern const TCHAR szSMTPiPlanetSrvrName[];
extern const TCHAR szSMTPXcg2000SrvrName[];
extern const TCHAR szSMTPCommuniGateProSrvrName[];
extern const TCHAR szSMTPCriticalPathSrvrName[];

/* NULL terminated list of known server type strings */
extern LPCTSTR SMTPServerTypes[];

/*---------------------------------------------------------------------------*/

#endif	/* of ifndef _SMPT_LIB_H_ */
