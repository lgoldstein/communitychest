#ifndef _POP3LIB_H_
#define _POP3LIB_H_

/*---------------------------------------------------------------------------*/

#include <util/string.h>
#include <comm/socket.h>

#include <internet/rfc822msg.h>

/*---------------------------------------------------------------------------*/

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

/* default POP3 port */
#ifndef IPPORT_POP3
#define IPPORT_POP3	110
#endif

/*---------------------------------------------------------------------------*/

/* Maximum allowed length of message body (or headers) lines */
#define POP3_MAX_LINE_LENGTH 1022
#define POP3_MAX_RSP_LEN		512

/* standard delimiter for arguments */
#define POP3_DELIMITER _T(' ')

/* used to signal all lines in a message */
#define POP3_ALL_LINES	(-1)

/*---------------------------------------------------------------------------*/

/* standard POP3 response headers */
extern const TCHAR POP3_OK[], POP3_ERR[];

/*---------------------------------------------------------------------------*/

extern const TCHAR szPOP3UserCmd[];
extern const TCHAR szPOP3PassCmd[];
extern const TCHAR szPOP3TopCmd[];
extern const TCHAR szPOP3RetrCmd[];
extern const TCHAR szPOP3ListCmd[];
extern const TCHAR szPOP3LastCmd[];
extern const TCHAR szPOP3UidlCmd[];
extern const TCHAR szPOP3DeleCmd[];
extern const TCHAR szPOP3RsetCmd[];
extern const TCHAR szPOP3ApopCmd[];
extern const TCHAR szPOP3StatCmd[];
extern const TCHAR szPOP3NoopCmd[];
extern const TCHAR szPOP3HelpCmd[];
extern const TCHAR szPOP3QuitCmd[];

/* NULL terminated list of all standard POP3 command */
extern LPCTSTR szPOP3Cmds[];

/*---------------------------------------------------------------------------*/

/* default logout used to close inactive connections (see sockReadCmd) */
#define POP3_DEFAULT_AUTOLOGOUT_TIMEOUT 30	/* in seconds */

/*---------------------------------------------------------------------------*/

/*		Callback function used when initial connection is established with a
 * client (i.e., BEFORE the authentication stage). The application can create
 * and initialize an internal data structure which it can use to manage the
 * session, and return it into the "sessionP2P". The identity of the remote
 * client is supplied in the "peerAddress" parameter.
 *
 * If routine return code is other than S_OK the connection is closed (Note:
 * returning a S_OK code with a NULL session pointer is OK !!!)
 */
typedef HRESULT (*POP3_SESSION_CREATE_CFN)(void			**sessionP2P,
														 void			*sessionArg,
			 											 const char	peerAddress[]);

/*		Callback function used to validate a user on the POP3 server if returns
 * S_OK then the user & password are valid and the POP3 session is accepted.
 * The application may assume that in case of accepting the user, the session
 * will be about this user's specific mailbox.
 */
typedef HRESULT (*POP3_USER_VALIDATE_CFN)(void			*sessionP,
														const char	userName[],
														const char	passwd[]);

/* this structure is used to describe one message in the user's mailbox */
typedef struct tag_msg_dsc_type {
	UINT32	msgSize;		/* msg size in octets. NOTE: total mbox size is
							 * the SUM of all message sizes (see "STAT" command).
							 */

	void	*msgP;		/* msg contents - Note: this could be
							 *		some internal identifier which
							 *		points to the actual contents. This
							 *		way the application can implement
							 *		a "lazy" loader whereas the actual
							 *		msg data is actually read only
							 *		when actually necessary.
							 */
	BOOLEAN fIsDelMarked;	/* True if marked for deletion */
	BOOLEAN fIsReadMarked;	/* True if user already read it (must be
								 * set initially by the "load_msgs" callback).
								 * It is set to TRUE as response to RETR command.
								 * Remains as such even after RSET, QUIT or
								 * lost connection.
								 */
} MSG_DSC_TYPE;

/*		Callback function used once user is authenticated in order to "load" the
 * user's mailbox contents. The application must allocate the necessary space
 * to hold the message descriptors and their identifiers / contents. If
 * returned number of messages is zero, then the application may return a NULL
 * pointer to the descriptors array.
 *
 * If return code other than S_OK returned, then connection is aborted.
 */
typedef HRESULT (*POP3_LOAD_MSGS_CFN)(void			*sessionP,
												  UINT32			*msgsNumP,
												  MSG_DSC_TYPE	**dscsP2P);

/*		Callback function used when the mailbox contents are no longer required.
 * The application should check the descriptors (if any) and handle the actual
 * deletion of those marked so by the "fIsDelMarked" field.
 */

typedef HRESULT (*POP3_RELEASE_MSGS_CFN)(void			*sessionP,
													  const UINT32	msgsNum,
													  MSG_DSC_TYPE	*dscsP);

/*		Callback function used whenever data from a specific message is required.
 * The application should send up to the specified number of lines from the
 * message BODY (-1 = all). After sending the headers a blank line must be sent
 * to separate headers from body.
 *
 * The generic POP3 code takes care of the last "." line (the application must
 * take care of STUFFING if necessary, but should not end the body with
 * "\n.\n" !!!).
 *
 * If code other than S_OK returned, then connection is closed.
 */

typedef HRESULT (*POP3_SEND_MSG_DATA_CFN)(void						*sessionP,
														const MSG_DSC_TYPE	*msgP,
														const UINT32			msgIdx,
														SOCKET					outSock,
														const LONG				linesNum);
/*
 * Called when session with POP3 client is finished
 */
typedef HRESULT (*POP3_SESSION_RELEASE_CFN)(void *sessionP);

/*		Callback function used for logging messages from this code. The "arg"
 * argument is the same one passed as "logArg" in the "POP3_server_create"
 *  routine.
 *
 * Note: this is an ELLIPSIS (var args) function !!!
 */
typedef void (*POP3_LOG_MSG_CFN)(void *logArg, const char fmt[], ...);

/*---------------------------------------------------------------------------*/

/*		if "portName" NULL or empty then default port (110) is used.
 *		if "portName" numeric than this number is used.
 *		if "portName" is a string, then "getservbyname" is used (e.g.: "pop3").
 */

extern HRESULT POP3_server_create (const char					portName[],
											  POP3_SESSION_CREATE_CFN	sessCreateCfn,
											  void							*sessCreateArg,
											  POP3_USER_VALIDATE_CFN	validateCfn,
											  POP3_LOAD_MSGS_CFN			loadMsgsCfn,
											  POP3_RELEASE_MSGS_CFN		releaseMsgsCfn,
											  POP3_SEND_MSG_DATA_CFN	sendDataCfn,
											  POP3_SESSION_RELEASE_CFN	sessReleaseCfn,
											  POP3_LOG_MSG_CFN			logMsgCfn,
											  void							*logMsgArg,
											  UINT32							cMaxClients);

/*---------------------------------------------------------------------------*/

/*		The follogin module provides a POP3 client interface
 */

/* timeout (in seconds) that a client may wait for a server response */
#define POP3_CLNT_DEFAULT_TIMEOUT	30

/* used to signal all messages */
#define POP3_ALL_MSGS	0

/*---------------------------------------------------------------------------*/

/* returns EOK if "+OK", ESTATE if "-ERR" and E??? otherwise */
extern EXC_TYPE pop3_xlate_rsp (LPCTSTR lpszRsp);

#ifdef __cplusplus
/*		Sends the command (with arguments - if any) and reads the OK/ERR response
 * (using the timeout). Returns non-EOK if problem encountered (e.g. returns
 * ENOTCONNECTION if network error, ESTATE if "-ERR" received).
 */

extern EXC_TYPE pop3_clnt_cmd_sync (ISockioInterface&	CBSock,
												const char			cmd[],
												const char			arg1[],	/* may be NULL */
												const char			arg2[],	/* ditto */
												char					rsp[],
												const UINT32		uRspLen,
												const UINT32		uRspTimeout);
#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
/* performs the USER/PASS authentication */
extern EXC_TYPE pop3_clnt_auth (ISockioInterface&	CBSock,
										  LPCTSTR				lpszUserName,
										  LPCTSTR				lpszPasswd,
										  LPTSTR					lpszRspBuf,
										  const UINT32			ulBufLen,
										  const UINT32			uRspTimeout);

extern EXC_TYPE pop3_clnt_auth (ISockioInterface&	CBSock,
										  LPCTSTR				lpszUserName,
										  LPCTSTR				lpszPasswd,
										  const UINT32			uRspTimeout);
#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
/*
 *	Makes sure the provided account parameters are valid. Returns:
 *
 *		ENOTCONNECTION - server could not be connected
 *		ESTATE - server service denied ("-ERR" in welcome line)
 *		EPERMISSION - authentication failed
 */
extern EXC_TYPE pop3_clnt_validate (LPCTSTR			lpszHost,
											   const int		iPort,		/* if 0 then use default */
												LPCTSTR			lpszUserName,
												LPCTSTR			lpszPasswd,
												LPTSTR			lpszRspBuf,
												const UINT32	ulBufLen,
												const UINT32	uRspTimeout);

extern EXC_TYPE pop3_clnt_validate (LPCTSTR			lpszHost,
											   const int		iPort,		/* if 0 then use default */
												LPCTSTR			lpszUserName,
												LPCTSTR			lpszPasswd,
												const UINT32	uRspTimeout);
#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/
/*		Callback function called to handle lines received from the POP3 server.
 * If returns FALSE, then retrieval is aborted (i.e. the user's callback is not
 * called, though the message is discarded through reading).
 */
typedef BOOLEAN (*POP3_CHNDL_CFN)(const UINT32	msgNum,		/* requested ID */
											 const SINT32	linesNum,	/* requested num */
											 const char		iBuf[],		/* read buffer */
											 const UINT32	iLen,			/* valid datalen */
											 void				*pArg);		/* caller arg */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus

/* retrieves the specified message - calls the callback functionon input */
extern EXC_TYPE pop3_clnt_retr (ISockioInterface&	CBSock,
										  const UINT32			msgNum,
										  const SINT32			linesNum,
										  POP3_CHNDL_CFN		lpfnHcfn,
										  void					*pArg,
										  const UINT32			uRspTimeout);

inline EXC_TYPE pop3_clnt_ghdrs (ISockioInterface&	CBSock,
										  const UINT32			msgNum,
										  POP3_CHNDL_CFN		lpfnHcfn,
										  void					*pArg,
										  const UINT32			uRspTimeout)
{
	return pop3_clnt_retr(CBSock,msgNum,0,lpfnHcfn,pArg,uRspTimeout);
}

inline EXC_TYPE pop3_clnt_gmsg (ISockioInterface&	CBSock,
										  const UINT32			msgNum,
										  POP3_CHNDL_CFN		lpfnHcfn,
										  void					*pArg,
										  const UINT32			uRspTimeout)
{
	return pop3_clnt_retr(CBSock,msgNum,POP3_ALL_LINES,lpfnHcfn,pArg,uRspTimeout);
}

#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/

extern EXC_TYPE ExtractPOP3StatResponse (LPCTSTR	lpszStatRsp,
													  UINT32		*pulMsgsNum,
													  UINT32		*pulMboxSize);

#ifdef __cplusplus
/* returns the mailbox status */
extern EXC_TYPE pop3_clnt_stat (ISockioInterface&	CBSock,
										  UINT32					*puMsgsNum,
										  UINT32					*puMboxSize,
										  const UINT32			uRspTimeout);
#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus

extern EXC_TYPE pop3_clnt_msg_uidl (ISockioInterface&	CBSock,
												const UINT32		msgNum,
												LPTSTR				lpszUIDL,
												const UINT32		ulMaxLen,
												const UINT32		uRspTimeout);

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
extern EXC_TYPE pop3_clnt_uidl (ISockioInterface&	CBSock,
										  const UINT32			msgNum,
										  POP3_CHNDL_CFN		lpfnHcfn,
										  void					*pArg,
										  const UINT32			uRspTimeout);

#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/

#define MAX_POP3MSGLIST_RSPLEN	((2 * MAX_DWORD_DISPLAY_LENGTH)+NAME_LENGTH)

#ifdef __cplusplus

extern EXC_TYPE pop3_clnt_msg_list (ISockioInterface&	CBSock,
												const UINT32		msgNum,
												UINT32&				ulMsgSize,
												const UINT32		uRspTimeout);

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
extern EXC_TYPE pop3_clnt_list (ISockioInterface&	CBSock,
										  const UINT32			msgNum,
										  POP3_CHNDL_CFN		lpfnHcfn,
										  void					*pArg,
										  const UINT32			uRspTimeout);
#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/

/* Note: calls callback with "szXBody" header (see "internet/rfc822.h") for
 *			end of headers.
 */
typedef BOOLEAN (*POP3_HDENM_CFN)(const UINT32	msgNum,
											 const char		lpszHdrName[],
											 const char		lpszHdrValue[],
											 const BOOLEAN	fIsContHdr,
											 void				*pArg);

#ifdef __cplusplus

/*
 * Calls the callback for each header - msg number may be POP3_ALL_MSGS
 */
extern EXC_TYPE pop3_enum_hdrs (ISockioInterface&	CBSock,
										  const UINT32			ulMsgNum,
										  POP3_HDENM_CFN		lpfnEcfn,
										  void					*pArg,
										  const UINT32			uRspTimeout);
#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus

/* marks requested message for deletion */
extern EXC_TYPE pop3_clnt_dele (ISockioInterface&	CBSock,
										  const UINT32			msgNum,
										  const UINT32			uRspTimeout);

/*---------------------------------------------------------------------------*/

/* sends a command for which only OK/ERROR is expected (i.e. no information) */
extern EXC_TYPE pop3_clnt_simpl_cmd (ISockioInterface&	CBSock,
												 const char				cmd[],
												 const UINT32			uRspTimeout);

/*---------------------------------------------------------------------------*/

/* quits the session (Note: closes the socket !!!) */
extern EXC_TYPE pop3_clnt_quit (ISockioInterface&	CBSock,
  										  const UINT32			uRspTimeout);

/* send a "no-operation" - can be used to check that connections is alive */
extern EXC_TYPE pop3_clnt_noop (ISockioInterface& CBSock,
  										  const UINT32			uRspTimeout);
#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
extern EXC_TYPE pop3_clnt_connect (ISockioInterface&	CBSock,
											  LPCTSTR				lpszHost,
											  const int				iPort,		/* if 0 then use default */
											  LPTSTR					lpszRspBuf,
											  const UINT32			ulBufLen,
											  const UINT32			uRspTimeout);

/* connects to the POP3 server and reads the initial "welcome" response */
extern EXC_TYPE pop3_clnt_connect (ISockioInterface&	CBSock,
											  LPCTSTR				lpszHost,
											  const int				iPort,	/*  0 = use default */
											  const UINT32			uRspTimeout);

#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/

/* (standard) information which can be retrieved about a message */
typedef struct tag_pop3_msg_info {
		UINT32	msgNum;
		UINT32	msgSize;
} POP3_MSG_INFO;

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
/*		Returns the messages info (up to specified limit) - returns
 * actual number of message numbers/IDs.
 *
 * Note: there is no direct way to distinguish between an EXACT population of
 *			the IDs array, and an overflow, since the routine stops populating
 *			the list once the limit is reached. Application should first use the
 *			"stat" routine in order to find out how much space to allocate.
 */

extern EXC_TYPE pop3_clnt_get_msgs_info (ISockioInterface&	CBSock,
													  POP3_MSG_INFO		*pInfo,
													  const UINT32			nMsgs,
													  UINT32					*pnMsgs,
													  const UINT32			uRspTimeout);
#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/

/*		Returns the messages info (up to specified limit) - returns
 * actual number of message numbers/IDs. Msg size is retrieved only if required
 *
 * Note: there is no direct way to distinguish between an EXACT population of
 *			the IDs array, and an overflow, since the routine stops populating
 *			the list once the limit is reached. Application should first use the
 *			"stat" routine in order to find out how much space to allocate.
 */

#define MAX_POP3_UIDL_LEN	72
#define MAX_POP3MSGUIDL_RSPLEN	(MAX_POP3_UIDL_LEN+MAX_DWORD_DISPLAY_LENGTH+NAME_LENGTH)

typedef struct {
		UINT32	msgNum;
		UINT32	msgSize;
		char		msgUIDL[MAX_POP3_UIDL_LEN+2];
} POP3_EXTMSG_INFO;

#ifdef __cplusplus
extern EXC_TYPE pop3_clnt_get_msgs_full_info (ISockioInterface&	CBSock,
															 POP3_EXTMSG_INFO		*pInfo,
															 const UINT32			nMsgs,
															 const BOOLEAN			fGetUIDL,
															 const BOOLEAN			fGetSize,
															 UINT32					*pnMsgs,
															 const UINT32			uRspTimeout);
#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CPOP3EnvelopeData : public CRFC822EnvelopeData {
	public:
		// Note: zero causes undefined behavior !!!
		CPOP3EnvelopeData (const UINT32 ulAvgRcipsNum=DEFAULT_RFC822_RCIPS_NUM)
			: CRFC822EnvelopeData(ulAvgRcipsNum) { }

		CPOP3EnvelopeData (const CPOP3EnvelopeData& ed)
			: CRFC822EnvelopeData(ed) { }

		EXC_TYPE ProcessHdr (LPCTSTR			lpszHdrName,
									LPCTSTR			lpszHdrValue,
									const BOOLEAN	fIsContHdr);

		virtual ~CPOP3EnvelopeData () { }
};

typedef CPOP3EnvelopeData *LPPOP3ENVELOPEDATA;
#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
extern EXC_TYPE pop3_get_msg_envelope (ISockioInterface&		CBSock,
													const UINT32			ulMsgNum,
													CPOP3EnvelopeData&	ed,
													const UINT32			uRspTimeout);

extern EXC_TYPE pop3_msg_extract (ISockioInterface&		CBSock,
											 const UINT32				ulMsgNum,
											 RFC822MSGEX_CFN_TYPE	lpfnXcfn,
											 void							*pArg,
											 const UINT32				uRspTimeout);
#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CPOP3MsgInfo {
	private:
		char		*m_lpszUIDL;
		UINT32	m_ulUIDLLen;
		UINT32	m_ulMsgNum;
		UINT32	m_ulMsgSize;

		EXC_TYPE UpdateInfo (const CPOP3MsgInfo& p3mi);

	public:
		CPOP3MsgInfo (const UINT32 ulMsgNum=0,
						  const char	*lpszUIDL=NULL,
						  const UINT32	ulMsgSize=0);

		// copy constructor
		CPOP3MsgInfo (const CPOP3MsgInfo& p3mi);

		// assignment operator
		CPOP3MsgInfo& operator= (const CPOP3MsgInfo& p3mi)
		{
			UpdateInfo(p3mi);
			return *this;
		}

		// msg number must always be non-zero
		EXC_TYPE SetInfo (const UINT32	ulMsgNum,
								const char		*lpszUIDL,
								const UINT32	ulMsgSize);
		EXC_TYPE SetUIDL (const UINT32 ulMsgNum, const char *lpszUIDL);
		EXC_TYPE SetSize (const UINT32 ulMsgNum, const UINT32 ulMsgSize);

		void Clear ();

		const char *GetUIDL () const { return m_lpszUIDL; }
		UINT32 GetMsgNum () const { return m_ulMsgNum; }
		UINT32 GetMsgSize () const { return m_ulMsgSize; }

		virtual ~CPOP3MsgInfo ()
		{
			if (m_lpszUIDL != NULL)
				delete [] m_lpszUIDL;
		}
};

typedef CPOP3MsgInfo *LPCPOP3MSGINFO;
#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CPOP3InfoTbl {
	private:
		UINT32			m_ulMsgsNum;
		CPOP3MsgInfo	*m_pInfo;
		CStr2PtrMapper	m_Mapper;

		CPOP3MsgInfo *FindInfo (const UINT32 ulMsgNum) const;

		// disable copy constructor and assignment operator
		CPOP3InfoTbl (const CPOP3InfoTbl& cp3it);
		CPOP3InfoTbl& operator= (const CPOP3InfoTbl& cp3it);

	public:
		CPOP3InfoTbl (const UINT32 ulMsgsNum=0);

		// does not allow re-initialization
		EXC_TYPE SetSize (const UINT32 ulMsgsNum);

		// if total msgs num not set then queries server for it
		EXC_TYPE Populate (ISockioInterface&	CBSock,
								 const BOOLEAN			fGetMsgSize,
								 const UINT32			uRspTimeout);

		UINT32 GetSize () const { return m_ulMsgsNum; }

		// msg number must always be non-zero
		EXC_TYPE AddInfo (const UINT32	ulMsgNum,
								const char		*lpszUIDL,
								const UINT32	ulMsgSize);
		EXC_TYPE AddUIDL (const UINT32 ulMsgNum, const char *lpszUIDL);
		EXC_TYPE AddSize (const UINT32 ulMsgNum, const UINT32 ulMsgSize);

		// returns info of specified msg number
		const CPOP3MsgInfo *GetMsgInfo (const UINT32 ulMsgNum) const
		{
			return FindInfo(ulMsgNum);
		}

		// returns info at ORDINAL index
		const CPOP3MsgInfo *GetInfo (const UINT32 ulIdx) const;

		// returns info matching specified UIDL
		const CPOP3MsgInfo *GetInfo (const char *lpszUIDL) const;

		// returns according to sorted order (if sorted)
		// NULL if index out of range
		const CPOP3MsgInfo *operator[] (const UINT32 ulIdx) const
		{
			return GetInfo(ulIdx);
		}

		// finds the specified UIDL (or NULL if fails)
		const CPOP3MsgInfo *operator[] (const char *lpszUIDL) const
		{
			return GetInfo(lpszUIDL);
		}

		void Clear ();

		virtual ~CPOP3InfoTbl () { Clear(); }
};

typedef CPOP3InfoTbl *LPCPOP3INFOTBL;
#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/

/*		Callback used for headers (and other stuff) on a POP3 relay. If returns
 * non-EOK value, then connection is aborted.
 */
typedef EXC_TYPE (*POP3_HDRS_CFN)(const char	pszHdr[], void *lpVal, void *pArg);

/*
 *	Retrieval flags for the POP3 account and sending them thru SMTP. using
 */

/*
 *		Delete from server after retrieval - if not set the UIDL(s) are supplied
 * to caller and given a chance to decide whether to retrieve the message or not.
 *
 *	Flow:
 *
 *		- callback is called with the "UIDL" (szPOP3UidlCmd) string and the UIDL
 *		as the value argument.
 *
 *		- if callback returns
 *
 *			+ EOK - message is retrieved
 *			+ EABORTEXIT - message is skipped
 *			+ Otherwise - connection is aborted
 */
#define POP3RLY_DELFROMSERVER	0x00000001

/*		Ask caller if expected data size can be accommodated in the recipient`s
 * mailbox (as supplied by the "STAT" command).
 *
 *	Flow:
 *
 *		- callback is called with the "STAT" (szPOP3StatCmd) string and the STAT response
 *		as the value (string) argument (e.g. "+OK 2 1436").
 *
 *		- if callback returns
 *
 *			+ EOK - retrieval proceeds as usual
 *			+ EABORTEXIT - retrieval is skipped
 *			+ Otherwise - connection is aborted
 */
#define POP3RLY_CHECKMBOXSIZE	0x00000002

/* Do NOT open an SMTP relay socket, but keep calling callback as usual */
#define POP3RLY_VIRTUALSMTP	0x00000004

#define IsPOP3RelayFlagSet(mask,flag) ((flag) == ((mask) & (flag)))

#define IsPOP3RelayDelFromServer(mask)	\
	IsPOP3RelayFlagSet(mask, POP3RLY_DELFROMSERVER)
#define IsPOP3RelayCheckMboxSize(mask)	\
	IsPOP3RelayFlagSet(mask, POP3RLY_CHECKMBOXSIZE)
#define IsPOP3RelayVirtualSMTP(mask)	\
	IsPOP3RelayFlagSet(mask, POP3RLY_VIRTUALSMTP)

/*---------------------------------------------------------------------------*/

typedef struct {
	LPCTSTR	lpszPOP3Host;
	int		iPOP3Port;		// 0 == use default
	LPCTSTR	lpszPOP3UID;
	LPCTSTR	lpszPOP3Passwd;
	UINT32	ulRspTimeout;
} POP3ACCOUNTDEF, *LPPOP3ACCOUNTDEF;

#define MAX_POP3EXTACC_ENCLEN	(MAX_RCVR_NAME_LEN+MAX_DWORD_DISPLAY_LENGTH+2)

extern EXC_TYPE EncodePOP3ExtAcc (const POP3ACCOUNTDEF	*pAccDef,
											 LPTSTR						lpszEnc,
											 const UINT32				ulMaxLen);

/*---------------------------------------------------------------------------*/

typedef struct {
	unsigned m_fRetrReverse	:	1;	// if set, retrieves messages from last down
	unsigned m_fCheckUIDL	:	1;	// if set, queries msg UIDL before retrieval
	unsigned m_fCheckSize	:	1;	// if set, queries msg size before retrieval
	unsigned m_fParseMsg		:	1;	// if set, performs msg parsing during retrieval
	unsigned m_fQueryDel		:	1;	// if set, queries msg deletion AFTER retrieval
} POP3RELAYFLAGS, *LPPOP3RELAYFLAGS;

/*---------------------------------------------------------------------------*/

#include <internet/smtpLib.h>

typedef struct {
	/* retrieval parameters */
	POP3ACCOUNTDEF		accDef;

	/* recipient parameters */
	SMTPMSGRELAYDEF	rcpDef;

	/* general parameters */
	POP3_HDRS_CFN	lpfnHcfn;
	LPVOID			pArg;
	UINT32			ulFlags;
} POP3RLYPARAMS, *LPPOP3RLYPARAMS;

/*		Retrieves all contents of the POP3 account and sends them thru SMTP using
 * the specified SMTP server and recipient. After a successful send, the msg is
 * deleted from the POP3 account (according to the flags options).
 *
 *	Special errors:
 *
 *		ECONNNOTOPEN - cannot connect to POP3 server
 *		EPASSWORDMISMATCH - cannot authenticate POP3 user
 *		EDEVFD - cannot get all messages
 */
extern EXC_TYPE POP3_relay_account (const POP3RLYPARAMS *pRlyParams);

/*---------------------------------------------------------------------------*/

/* "poppass" protocol for changing password */
#ifndef IPPORT_POPPASS
#define IPPORT_POPPASS	106
#endif

/*
 * The server's responses should be like an FTP server's responses:
 *
 *		1xx for in progress
 *		2xx for success
 *		3xx for more information needed
 *		4xx for temporary failure
 *		5xx for permanent failure
 *
 *	Putting it all together, here's a sample conversation:
 *
 *		S: 200 hello ....
 *		C: USER your-login-name
 *		S: 300 please send your password now
 *		C: PASS your-current-password
 *		S: 200 My, that was tasty...
 *		C: NEWPASS your-new-password
 *		S: 200 Happy to oblige
 *		C: QUIT
 *		S: 200 Bye-bye
 *		S: <closes connection>
 *		C: <closes connection>
 */

#define szPOPPassUserCmd	szPOP3UserCmd
#define szPOPPassPassCmd	szPOP3PassCmd
extern const char szPOPPassNewpassCmd[];

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus

// Note: does not inform stream about open conn.
extern EXC_TYPE OpenPOP3RelayConn (const POP3ACCOUNTDEF&	accDef,
											  ISockioInterface&		CBSock,
											  IMsgRelayStream&		rlyStream);

extern EXC_TYPE StreamRelayPOP3Msg (const POP3ACCOUNTDEF&	accDef,
												ISockioInterface&			CBSock,
												const UINT32				ulMsgNum,
												const POP3RELAYFLAGS&	rlyFlags,
												IMsgRelayStream&			rlyStream);

extern EXC_TYPE StreamRelayPOP3Conn (const POP3ACCOUNTDEF&	accDef,
												 ISockioInterface&		CBSock,
												 const POP3RELAYFLAGS&	rlyFlags,
												 IMsgRelayStream&			rlyStream);

extern EXC_TYPE StreamRelayPOP3Account (const POP3ACCOUNTDEF&	accDef,
													 const POP3RELAYFLAGS&	rlyFlags,
													 IMsgRelayStream&			rlyStream);

#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

/* returns EIOUNCLASS if no match found */
extern EXC_TYPE pop3AnalyzeWelcomePattern (LPCTSTR			lpszWelcome,
														 LPCTSTR			lpszWPattern,
														 LPTSTR			lpszType,
														 const UINT32	ulMaxTypeLen,
														 LPTSTR			lpszVersion,
														 const UINT32	ulMaxVerLen);

#ifdef __cplusplus
inline EXC_TYPE pop3AnalyzeWelcome (LPCTSTR			lpszWelcome,
												LPCTSTR			lpszPatterns[],
												LPTSTR			lpszType,
												const UINT32	ulMaxTypeLen,
												LPTSTR			lpszVersion,
												const UINT32	ulMaxVerLen)
{
	return inetAnalyzeWelcome(lpszWelcome, lpszPatterns,
									  pop3AnalyzeWelcomePattern,
									  lpszType, ulMaxTypeLen,
									  lpszVersion, ulMaxVerLen);
}
#else
#define pop3AnalyzeWelcome(w,p,t,tl,v,vl)	\
	inetAnalyzeWelcome(w,p,pop3AnalyzeWelcomePattern,t,tl,v,vl)
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

extern const TCHAR szPOP3SWCOMKx4p3WelcomePattern[];
extern const TCHAR szPOP3MSExchangeWelcomePattern[];
extern const TCHAR szPOP3Xcg2000WelcomePattern[];
extern const TCHAR szPOP3Xcg2003WelcomePattern[];
extern const TCHAR szPOP3CriticalPathWelcomePattern[];

extern LPCTSTR POP3KnownWelcomePatterns[];

/*---------------------------------------------------------------------------*/

/* some known server's names */
extern const TCHAR szPOP3InterMailSrvrName[];
extern const TCHAR szPOP3MSExchangeSrvrName[];
extern const TCHAR szPOP3Xcg2000SrvrName[];
extern const TCHAR szPOP3CriticalPathSrvrName[];

/* NULL terminated list of known server type strings */
extern LPCTSTR POP3ServerTypes[];

/*---------------------------------------------------------------------------*/

#endif /* of _POP3GEN_H_ */
