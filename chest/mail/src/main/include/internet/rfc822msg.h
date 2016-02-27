#ifndef _RFC822_MSG_H_
#define _RFC822_MSG_H_

/*---------------------------------------------------------------------------*/

#include <util/string.h>
#include <util/memory.h>

/*
 *	Contains RFC822 message parsing utilities
 */

#include <internet/rfc822.h>
#include <internet/base64.h>
#include <internet/qpenc.h>

/*---------------------------------------------------------------------------*/

#define RFC822MSG_EOM_SEQLEN		3	/* .+CR+LF */
#define RFC822MSG_EOM_PATLEN 		(RFC822MSG_EOM_SEQLEN+2) /* CR+LF+.+CR+LF */

#ifdef __cplusplus
// class for hunting for end-of-message (CRLF.CRLF)
class CRFC822MsgEOM {
	private:
		TCHAR	m_szEOMPattern[RFC822MSG_EOM_PATLEN+2];

	public:
		// default constructor
		CRFC822MsgEOM () { Reset(); }

		// copy constructor
		CRFC822MsgEOM (const CRFC822MsgEOM& me)
		{
			_tcscpy(m_szEOMPattern, me.m_szEOMPattern);
		}

		virtual CRFC822MsgEOM& operator= (const CRFC822MsgEOM& me)
		{
			_tcscpy(m_szEOMPattern, me.m_szEOMPattern);
			return *this;
		}

		virtual void Reset () { m_szEOMPattern[0] = _T('\0'); }

		virtual EXC_TYPE ProcessBuf (LPCTSTR lpszBuf, const UINT32 ulBufLen);

		// returns TRUE if current pattern is an end-of-message
		virtual BOOLEAN IsMsgEOM () const;

		virtual ~CRFC822MsgEOM () { }
};
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

typedef enum {
	RFC822_BEFORE_HDRS_MSGSTATE,		/* initial state */
	RFC822_ENVELOPE_MSGSTATE,			/* parsing envelope */
	RFC822_BEFORE_BODY_MSGSTATE,		/* after parsing 1st blank line */
	RFC822_BODY_MSGSTATE,				/* within msg body */
	RFC822_BEFORE_ATTHDRS_MSGSTATE,	/* after MIME separator */
	RFC822_ATTHDRS_MSGSTATE,			/* parsing attachment headers */
	RFC822_BEFORE_ATTDATA_MSGSTATE,	/* blank line after attachment headers */
	RFC822_ATTDATA_MSGSTATE,			/* attachment data */
	RFC822_BEFORE_MSG_END,				/* after last MIME boundary */
	RFC822_MSGEND_MSGSTATE,				/* after end of msg ('.' + CRLF) */
	RFC822_BAD_MSGSTATE
} RFC822MSGSTATE;

#define fIsBadRFC822MsgState(s) (((unsigned) (s)) >= RFC822_BAD_MSGSTATE)

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CRFC822MsgParser {
	private:
		CRFC822HdrParser	m_hdrParser;
		CRFC822HdrData		m_hdrData;
		TCHAR					m_szMIMEBoundary[MAX_RFC822_MIME_BOUNDARY_LEN+2];
		UINT32				m_ulMIMEBoundaryLen;
		RFC822MSGSTATE		m_msgState;

		struct {
			unsigned m_ulMIMEPart		: 16;
			unsigned m_fHaveContentType:  1;
			unsigned m_fIsMultipartMIME:  1;
			unsigned m_fIsDirectAttach :  1;
			unsigned m_fIsMIMESeparator:	1;
			unsigned m_fIsLastMIMEPart	:  1;
			unsigned m_fIsMultiBodyPart:	1;
		} m_msgFlags;

		BOOLEAN IsMultiBodyPart () const
		{
			return (m_msgFlags.m_fIsMultiBodyPart != 0);
		}

		// sets current state to "newState" provided current state is "oldState".
		// return TRUE if current state changed
		BOOLEAN SetCondState (const RFC822MSGSTATE	oldState,
									 const RFC822MSGSTATE	newState);

		// determine if 1st part in multipart msg is body or attachment
		EXC_TYPE CheckMIMEFirstMsgPart (LPCTSTR lpszLine, const UINT32	ulLen);

		// returns EFNEXIST if keyword not found
		EXC_TYPE ExtractAttachName (LPCTSTR			lpszHdrValue,
											 LPTSTR			lpszAttachName=NULL,
											 const UINT32	ulMaxLen=0);

		BOOLEAN HaveContentType () const
		{
			return (m_msgFlags.m_fHaveContentType != 0);
		}

		EXC_TYPE HandleContentType (LPCTSTR	lpszHdrValue);

		EXC_TYPE HandleContentDisposition (LPCTSTR	lpszHdrValue);

		EXC_TYPE HandleAccumulatedHdr ();

		EXC_TYPE HandleMsgHdr ();

		EXC_TYPE HandleMIMEBoundary (const BOOLEAN fIsLast);

	public:
		// resets state to start of message parsing
		virtual void Reset ();

		CRFC822MsgParser () { Reset(); }

		// processes input line and changes state/data
		//
		// Note: some internal pointers may point to the original supplied
		//			buffer. Therefore, it MUST NOT change while this object is
		//			"in use" (i.e. until all calls to any of its methods have
		//			been completed.
		virtual EXC_TYPE ProcessLine (LPCTSTR lpszOrgLine, const UINT32 ulLineLen);

		virtual EXC_TYPE ProcessLine (LPCTSTR lpszLine);

		virtual RFC822MSGSTATE GetMsgState () const
		{
			return m_msgState;
		}

		// Note: msg body flag is set as of 1st blank line and till end of msg
		//			(i.e. including withing attachment(s))
		virtual BOOLEAN IsMsgBody () const 
		{
			return (RFC822_BODY_MSGSTATE <= m_msgState);
		}

		virtual BOOLEAN IsMsgHdrs () const
		{
			return (RFC822_ENVELOPE_MSGSTATE == m_msgState);
		}

		virtual BOOLEAN IsMultipartMIME () const
		{
			return (m_msgFlags.m_fIsMultipartMIME != 0);
		}

		virtual BOOLEAN IsDirectAttach () const
		{
			return (m_msgFlags.m_fIsDirectAttach != 0);
		}

		// Note: after processing 1st blank line AFTER the MIME headers, but
		//			before the actual MIME data, the reported status is:
		//
		//				IsMsgBody() & (!IsAttachHdrs())
		virtual BOOLEAN IsAttachHdrs () const
		{
			return (RFC822_ATTHDRS_MSGSTATE == m_msgState);
		}

		virtual BOOLEAN IsRFC822Hdrs () const
		{
			return (IsMsgHdrs() || IsAttachHdrs());
		}

		// Note: after processing MIME boundary at end of attachment data, and
		//			before processing any further line(s), the reported status is:
		//
		//			IsMsgBody() & (!IsAttachBody())
		virtual BOOLEAN IsAttachBody () const
		{
			return (RFC822_ATTDATA_MSGSTATE == m_msgState);
		}

		virtual BOOLEAN IsMIMESeparator () const
		{
			return (m_msgFlags.m_fIsMIMESeparator != 0);
		}

		virtual BOOLEAN IsLastMIMEPart () const
		{
			return (m_msgFlags.m_fIsLastMIMEPart != 0);
		}
		
		virtual BOOLEAN IsMIMEBoundaryKnown () const
		{
			return (m_ulMIMEBoundaryLen != 0);
		}

		// Note: trying to process more lines after end of message is an error !!!
		virtual BOOLEAN IsMsgEnd () const 
		{
			return (RFC822_MSGEND_MSGSTATE == m_msgState);
		}

		virtual const char *GetMIMEBoundary () const 
		{
			return m_szMIMEBoundary;
		}

		// Note(s):
		//
		//	a. calling this method before msg body is detected returns
		//		undefined value.
		//
		// b. msg body is considered MIME part 0
		//
		// c. MIME part number is increased as soon as the MIME boundary
		//		is detected (i.e. BEFORE attachment headers are processed)
		virtual UINT32 GetMIMEPart () const
		{
			return (UINT32) m_msgFlags.m_ulMIMEPart;
		}

		virtual const CRFC822HdrParser& GetHdrsParser () const
		{
			return m_hdrParser;
		}

		// Note: returns NULL if not msg or attachment hdr
		virtual LPCTSTR GetHdrName () const
		{
			return (IsRFC822Hdrs() ? m_hdrParser.GetHdrName() : NULL);
		}

		// Note: returns NULL if not msg or attachment hdr
		virtual LPCTSTR GetHdrValue () const
		{
			return (IsRFC822Hdrs() ? m_hdrParser.GetHdrValue() : NULL);
		}

		// Note: returns undefined value if not msg or attachment hdr
		virtual BOOLEAN IsContHdr () const
		{
			return m_hdrParser.IsContHdr();
		}

		virtual ~CRFC822MsgParser () { }

}; 	// end of RFC822 msg parser

typedef CRFC822MsgParser	*LPRFC822MSGPARSER;
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

/* msg extraction events */
typedef enum {
	RFC822MSGEX_MSGHDR_CASE,
	RFC822MSGEX_MSGBODY_CASE,
	RFC822MSGEX_ATTHDR_CASE,
	RFC822MSGEX_ATTSTART_CASE,
	RFC822MSGEX_ATTDATA_CASE,
	RFC822MSGEX_ATTEND_CASE,
	RFC822MSGEX_BAD_CASE
} RFC822MSGEXCASE;

#define fIsBadRFC822MsgExCase(c) (((unsigned) (c)) >= RFC822MSGEX_BAD_CASE)

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CRFC822MsgExtractor;
typedef CRFC822MsgExtractor	*LPRFC822MSGEXTRACTOR;

/*		Callback for handling msg extraction events. If non-EOK result is
 * returned then parsing stops and returned value is propagated (!!!)
 */
typedef EXC_TYPE (*RFC822MSGEX_CFN_TYPE)(const RFC822MSGEXCASE	meCase,
													  CRFC822MsgExtractor&	msgEx,
													  void						*pArg);
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

#define DEFAULT_RFC822_DECODE_SIZE	1024

#ifdef __cplusplus
class CRFC822MsgExtractor : public CRFC822MsgParser {
	private:
		RFC822MSGSTATE			m_lastMsgState;
		CRFC822HdrData			m_hdrAcc;
		CB64Decoder				m_b64Decoder;
		CQPDecoder				m_qpDecoder;
		UINT32					m_ulAttachNameLen;
		char						*m_lpszAttachName;
		char						m_szAttachType[MAX_RFC822_CONTENT_TYPE_LEN+2];
		char						m_szAttachSubType[MAX_RFC822_CONTENT_SUBTYPE_LEN+2];
		char						m_szCharSet[MAX_RFC822_CHARSET_LEN+2];
		char						m_szAttachEncoding[MAX_RFC822_CONTENT_ENCODE_LEN+2];
		UINT32					m_ulAttachNum;
		RFC822MSGEX_CFN_TYPE	m_lpfnHcfn;
		UINT8						*m_pBuf;
		UINT32					m_ulBufLen;	// last decoded size
		UINT32					m_ulMaxLen;
		void						*m_pArg;
		RFC822ENCCASE			m_encAttach;
		BOOLEAN					m_fAutoAlloc;
		BOOLEAN					m_fDecoding;

		// disable copy constructor and assigment operator
		CRFC822MsgExtractor (const CRFC822MsgExtractor& );
		CRFC822MsgExtractor& operator= (const CRFC822MsgExtractor& );

		void RestartAttach ();

		EXC_TYPE StopDecoding ();

		EXC_TYPE HandleAccumulatedHdr ();

		EXC_TYPE AccumulateHdrData ();

		EXC_TYPE SignalAttachStart ();

		EXC_TYPE UpdateContentTypeInfo (const char lpszAccValue[]);

		EXC_TYPE UpdateContentXferInfo (const char lpszAccValue[]);

		EXC_TYPE HandleAttachHdr ();

		EXC_TYPE HandleEnvelopeHdr ();

		EXC_TYPE HandlePlainData (const char				*lpszLine,
										  const UINT32				ulLen,
										  const RFC822MSGEXCASE	mCase);

		EXC_TYPE DecodeMsgPart (const char					lpszLine[],
										const UINT32				ulLen,
										const RFC822MSGEXCASE	mCase);

		void Cleanup ();

		EXC_TYPE InvokeCfn (const RFC822MSGEXCASE		meCase);

		EXC_TYPE ReportAttachStart ();

		EXC_TYPE WriteData (const UINT8 pBuf[], const UINT32 ulBufLen);

		static UINT32 wcfn (void *pFout,const char pBuf[],const UINT32 ulBufLen);

	public:
		virtual void Reset ();

		CRFC822MsgExtractor ();

		virtual EXC_TYPE SetDecodeParams (RFC822MSGEX_CFN_TYPE	lpfnHcfn,
													 void							*pArg,
													 const UINT32				iMaxLen=DEFAULT_RFC822_DECODE_SIZE,
													 UINT8						*pBuf=NULL);	// NULL == auto allocate

		virtual const UINT8 *GetDataBuf () const
		{
			return m_pBuf;
		}

		virtual UINT32 GetDataLen () const
		{
			return m_ulBufLen;
		}

		// starting from 1 (0 == no attachment (yet))
		virtual UINT32 GetAttachNum () const
		{
			return m_ulAttachNum;
		}

		// returns empty string if unknown/invalid
		virtual const char *GetAttachName () const
		{
			return ((NULL == m_lpszAttachName) ? "" : m_lpszAttachName);
		}

		// returns empty string if unknown/invalid
		virtual const char *GetAttachType () const
		{
			return m_szAttachType;
		}

		// returns empty string if unknown/invalid
		virtual const char *GetAttachSubType () const
		{
			return m_szAttachSubType;
		}

		// returns empty string if unknown/invalid
		virtual const char *GetAttachEncodingString () const
		{
			return m_szAttachEncoding;
		}

		virtual const char *GetAttachCharSet () const
		{
			return m_szCharSet;
		}

		virtual RFC822ENCCASE GetAttachEncoding () const
		{
			return m_encAttach;
		}

		// processes input line and changes state/data
		//
		// Note: some internal pointers may point to the original supplied
		//			buffer. Therefore, it MUST NOT change while this object is
		//			"in use" (i.e. until all calls to any of its methods have
		//			been completed.
		virtual EXC_TYPE ProcessLine (const char lpszLine[], const UINT32 ulLen);

		virtual ~CRFC822MsgExtractor ()	{	Cleanup(); }
};	// end of RFC822 msg extractor
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// processes a generalized buffer - including (mandatory) CRLF
class CRFC822MsgBufferProcessor : public CRFC822MsgExtractor {
	private:
		LPTSTR	m_lpszAccLine;
		UINT32	m_ulCurAccLen;
		UINT32	m_ulMaxAccLen;
		BOOLEAN	m_fAutoAlloc;

		// disable copy constructor and assignment operator
		CRFC822MsgBufferProcessor (const CRFC822MsgBufferProcessor& );
		CRFC822MsgBufferProcessor operator= (const CRFC822MsgBufferProcessor& );

		// do not allow direct call to base class initializer
		CRFC822MsgExtractor::SetDecodeParams;

	public:
		CRFC822MsgBufferProcessor ()
			: CRFC822MsgExtractor(), m_lpszAccLine(NULL), m_ulMaxAccLen(0), 
			  m_fAutoAlloc(FALSE), m_ulCurAccLen(0)
		{
		}

		virtual void Reset ();

		virtual EXC_TYPE SetProcessingParams (RFC822MSGEX_CFN_TYPE	lpfnHcfn,
														  void						*pArg,
														  const UINT32				iMaxAcc=DEFAULT_RFC822_DECODE_SIZE,
														  LPTSTR						pAcc=NULL,		// NULL == auto allocate
														  const UINT32				iMaxLen=DEFAULT_RFC822_DECODE_SIZE,
														  UINT8						*pBuf=NULL);	// NULL == auto allocate

		// processes input line and changes state/data
		//
		// Note: some internal pointers may point to the original supplied
		//			buffer. Therefore, it MUST NOT change while this object is
		//			"in use" (i.e. until all calls to any of its methods have
		//			been completed.
		virtual EXC_TYPE ProcessLine (const char lpszLine[], const UINT32 ulLen);

		virtual ~CRFC822MsgBufferProcessor ()
		{
			if (m_fAutoAlloc && (NULL != m_lpszAccLine))
				delete [] m_lpszAccLine;
		}
};

typedef CRFC822MsgBufferProcessor *LPRFC822MBUFPROC;

extern EXC_TYPE ExtractRFC822MsgFromFile (const char				*lpszMsgFile,
														RFC822MSGEX_CFN_TYPE	lpfnXcfn,
														LPVOID					pArg);
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class IMsgRelayStream {
	private:
		// disable copy constructor and assignment operator
		IMsgRelayStream (const IMsgRelayStream& );
		IMsgRelayStream& operator= (const IMsgRelayStream& );

	public:
		IMsgRelayStream () { }

		// used to inform about "meta" information
		//
		//		Command	Msg.#		Value						Remark
		//		-------	-----		-----						------
		//		+OK &ISockioInterface	server name/address	connect success
		//		USER		  0		logged in user			authentication success (also relay start)
		//		STAT		  0		server response
		//		LIST		  #		size						reported msg size
		//		UIDL		  #		UIDL						reported msg UIDL (non-EOK == skip, EWARNINGEXIT=delete)
		//		TOP		  #		empty						called BEFORE message retrieval
		//		RETR		  #		empty						called AFTER successful (!) message retrieval
		//		DELE		  #		empty						queries if msg to be deleted AFTER retrieval (EOK == YES)
		//		QUIT		 errno	empty						signals end of relay
		//		-ERR		  #		errno						some error while parsing/retrieving the specified message
		//		HELP		 level	error string			level=0-error, 1-warning, 2-info (see enumeration)
		//
		// Note: EABORTEXIT means skip to next message (where message number is valid)

		virtual EXC_TYPE HandleProtoState (LPCTSTR		/* lpszProtoCmd */,
													  const UINT32	/* ulMsgNo */,
													  LPCTSTR		/* lpszCmdVal */)
		{
			return EOK;
		}

		virtual EXC_TYPE SignalMsgError (const UINT32 ulMsgNo, const EXC_TYPE err);

		// Note(s):
		//
		//		a. called BEFORE writing the data
		//		b. called only if msg parsing requested
		virtual EXC_TYPE HandleMsgContents (const UINT32				/* ulMsgNo */,
														const RFC822MSGEXCASE	/* mxCase */,
														CRFC822MsgExtractor&		/* msgEx */)
		{
			return EOK;
		}

		virtual int Write (LPCTSTR lpszData, const size_t dLen) = 0;

		virtual int Write (LPCTSTR lpszData)
		{
			return Write(lpszData, ((NULL == lpszData) ? 0 : _tcslen(lpszData)));
		}

		virtual int WriteV (LPCTSTR lpszFmt, va_list ap);

		virtual int Writef (LPCTSTR lpszFmt, ...);

		enum { IMSGRLY_PROTO_ERROR=0, IMSGRLY_PROTO_WARNING=1, IMSGRLY_PROTO_INFO=2 };

		virtual EXC_TYPE LogVMsg (const UINT32		ulLevel,
										  const EXC_TYPE	rexc,
										  LPCTSTR			lpszFmt,
										  va_list			ap);

		virtual EXC_TYPE LogMsgf (const UINT32			ulLevel,
										  const EXC_TYPE		rexc,
										  LPCTSTR				lpszFmt,
										  ...);

		virtual ~IMsgRelayStream () { }
};
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class ISockioInterface;	// defined in <comm/socket.h>

// this object interprets the protocol state reports and calls the various sub-methods
class IMsgRelayDetailedStream : public IMsgRelayStream {
	private:
		// disable copy constructor and assignment operator
		IMsgRelayDetailedStream (const IMsgRelayDetailedStream& );
		IMsgRelayDetailedStream& operator= (const IMsgRelayDetailedStream& );

	protected:
		// should be called from "HandleProtoState" in order to interpret the parameters
		virtual EXC_TYPE DetailProtoState (LPCTSTR		lpszProtoCmd,
													  const UINT32	ulMsgNo,
													  LPCTSTR		lpszCmdVal);

	public:
		IMsgRelayDetailedStream ()
			: IMsgRelayStream()
		{
		}

		virtual EXC_TYPE HandleRelayConnect (ISockioInterface& ifRelay, LPCTSTR lpszServer) = 0;

		virtual EXC_TYPE HandleMsgError (const EXC_TYPE msgErr) = 0;

		virtual EXC_TYPE HandleLogMsg (const UINT32	ulLevel, LPCTSTR lpszLogMsg) = 0;

		virtual EXC_TYPE HandleRelayStart (LPCTSTR lpszUID) = 0;

		virtual EXC_TYPE HandleMboxStatus (const UINT32 ulMsgsNum, const UINT32 ulMboxSize) = 0;

		virtual EXC_TYPE HandleMsgRelayStart (const UINT32 ulMsgID) = 0;

		virtual EXC_TYPE HandleMsgUIDL (const UINT32 ulMsgID, LPCTSTR lpszUIDL) = 0;

		virtual EXC_TYPE HandleMsgRelayEnd (const UINT32 ulMsgID) = 0;

		virtual EXC_TYPE QueryMsgDeletion (const UINT32 ulMsgID) = 0;

		virtual EXC_TYPE HandleRelayEnd (LPCTSTR lpszUID, const EXC_TYPE relExc) = 0;

		virtual ~IMsgRelayDetailedStream ()
		{
		}
};
#endif	/* of __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
// Note: all these functions preserve EXACTLY the input line length

extern UINT32 AdjustParsedLine (const RFC822MSGSTATE	msgState,
										  LPTSTR						lpszLine,
										  const UINT32				ulLineLen,
										  const UINT32				ulLineNdx,
										  BOOLEAN&					fAdjusted);

inline UINT32 AdjustParsedLine (const CRFC822MsgExtractor&	msgEx,
										  LPTSTR								lpszLine,
										  const UINT32						ulLineLen,
										  const UINT32						ulLineNdx,
										  BOOLEAN&							fAdjusted)
{
	return AdjustParsedLine(msgEx.GetMsgState(), lpszLine, ulLineLen, ulLineNdx, fAdjusted);
}
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
#include <stdio.h>

// relays messages into a series of files
class CMsgFilesRelay : public IMsgRelayStream {
	private:
		FILE		*m_fp;					// currently open file
		LPTSTR	m_lpszFilePath;
		LPTSTR	m_lpszFilesDir;		// NULL/empty == CWD
		LPTSTR	m_lpszFilesPrefix;	// NULL/empty == auto-create
		LPTSTR	m_lpszFilesSuffix;	// NULL/empty == auto-create

		EXC_TYPE Reset (const BOOLEAN fRemoveFile);

		EXC_TYPE Cleanup ();

		EXC_TYPE StartMsgRetrieval (const UINT32 ulMsgNo);

		EXC_TYPE StopMsgRetrieval (const UINT32 /* ulMsgNo */)
		{
			return Reset(FALSE);
		}

		// disable copy constructor and assignment operator
		CMsgFilesRelay (const CMsgFilesRelay& );
		CMsgFilesRelay& operator= (const CMsgFilesRelay& );

	public:
		CMsgFilesRelay ();

		virtual EXC_TYPE SetInfo (LPCTSTR	lpszFilesDir=NULL,		// NULL/empty == CWD
										  LPCTSTR	lpszFilesPrefix=NULL,	// NULL/empty == auto-create
										  LPCTSTR	lpszFilesSuffix=NULL);	// NULL/empty == auto-create

		// Only start/stop of message is handled
		virtual EXC_TYPE HandleProtoState (LPCTSTR		lpszProtoCmd,
													  const UINT32	ulMsgNo,
													  LPCTSTR		lpszCmdVal);

		virtual int Write (LPCTSTR lpszData, const size_t dLen);

		virtual ~CMsgFilesRelay () { Cleanup(); }
};
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// class to hold basic msg envelope data
class CRFC822EnvelopeData {
	protected:
		LPTSTR			m_lpszDate;
		LPTSTR			m_lpszSubject;
		LPTSTR			m_lpszFrom;
		LPTSTR			m_lpszSender;
		LPTSTR			m_lpszInReplyTo;
		LPTSTR			m_lpszMsgId;
		CVSDCollection	m_ReplyTo;
		CVSDCollection	m_To;
		CVSDCollection	m_Cc;
//		CVSDCollection	m_Bcc;

		virtual BOOLEAN IsSimpleEnvelopeHdr (LPCTSTR lpszHdrName) const;
		virtual BOOLEAN IsListEnvelopeHdr (LPCTSTR lpszHdrName) const;
		virtual BOOLEAN IsEnvelopeHdr (LPCTSTR lpszHdrName) const
		{
			return (IsSimpleEnvelopeHdr(lpszHdrName) || IsListEnvelopeHdr(lpszHdrName));
		}

	public:
		enum { DEFAULT_RFC822_RCIPS_NUM=4 };

		// Note: zero causes undefined behavior !!!
		CRFC822EnvelopeData (const UINT32 ulAvgRcipsNum=DEFAULT_RFC822_RCIPS_NUM);

		// copy constructor
		CRFC822EnvelopeData (const CRFC822EnvelopeData& ed);

		virtual EXC_TYPE UpdateData (const CRFC822EnvelopeData& ed);
		virtual EXC_TYPE ProcessHdrData (LPCTSTR lpszHdrName, LPCTSTR lpszHdrVal);

		virtual LPCTSTR GetDate () const	{ return m_lpszDate; }
		virtual EXC_TYPE GetDate (struct tm& tms) const	{ return DecodeRFC822DateTime(m_lpszDate, &tms); }

		virtual LPCTSTR GetOriginator () const	{ return m_lpszFrom; }
		virtual LPCTSTR GetSender () const		{ return m_lpszSender; }
		virtual LPCTSTR GetSubject () const		{ return m_lpszSubject; }
		virtual LPCTSTR GetMessageId () const	{ return m_lpszMsgId; }
		virtual LPCTSTR GetInReplyTo () const	{ return m_lpszInReplyTo; }

		virtual const CVSDCollection& GetReplyAddress () const	{ return m_ReplyTo; }
		virtual const CVSDCollection& GetToRecips () const			{ return m_To; }
		virtual const CVSDCollection& GetCcRecips () const			{ return m_Cc; }
//		virtual const CVSDCollection& GetBccRecips () const		{ return m_Bcc; }

		virtual void Clear ();

		virtual ~CRFC822EnvelopeData () { Clear(); }
};

typedef CRFC822EnvelopeData *LPRFC822ENVELOPE;
#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/

#endif