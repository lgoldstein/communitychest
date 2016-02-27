#include <_types.h>

#include <util/string.h>
#include <util/tables.h>
#include <util/errors.h>

#include <internet/rfc822msg.h>

//////////////////////////////////////////////////////////////////////////////

// resets state to start of message parsing
void CRFC822MsgParser::Reset ()
{
	m_hdrParser.Reset();
	m_hdrData.Reset();

	m_szMIMEBoundary[0] = _T('\0');
	m_ulMIMEBoundaryLen = 0;

	memset(&m_msgFlags, 0, (sizeof m_msgFlags));
	m_msgState = RFC822_BEFORE_HDRS_MSGSTATE;
}

/*---------------------------------------------------------------------------*/

// returns EFNEXIST if keyword not found
EXC_TYPE CRFC822MsgParser::ExtractAttachName (LPCTSTR			lpszHdrValue,
															 LPTSTR			lpszAttachName,
															 const UINT32	ulMaxLen)
{
	if (NULL == lpszHdrValue)
		return EPARAM;

	TCHAR		szLoclName[MAX_RFC822_ATTACH_NAME_LEN+4]=_T("");
	LPTSTR	lpszName=((NULL == lpszAttachName) ? szLoclName : lpszAttachName);
	UINT32	ulNameLen=((NULL == lpszAttachName) ? MAX_RFC822_ATTACH_NAME_LEN : ulMaxLen); 

	return RFC822ExtractAttachName(lpszHdrValue, lpszName, ulNameLen);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRFC822MsgParser::HandleContentType (LPCTSTR	lpszHdrValue)
{
	if (IsEmptyStr(lpszHdrValue))
		return EEMPTYENTRY;

	EXC_TYPE		exc=EOK;
	if (!HaveContentType())
	{
		LPCTSTR	lpszType=NULL, lpszSubType=NULL;
		UINT32	ulTypeLen=0, ulSubTypeLen=0;

		exc = rfc822ExtractContentTypePtrs(lpszHdrValue,
													  &lpszType, &ulTypeLen,
													  &lpszSubType, &ulSubTypeLen);
		if (exc != EOK)
			return exc;

		m_msgFlags.m_fHaveContentType = 1;
		m_msgFlags.m_fIsMultipartMIME =
			(IsRFC822MIMEMultipartType(lpszType,ulTypeLen) ? 1 : 0);
		m_msgFlags.m_fIsDirectAttach = 0;

		if (!IsMultipartMIME())
		{
			if (IsRFC822MIMETextType(lpszType,ulTypeLen))
			{
				exc = ExtractAttachName(lpszHdrValue);
				if (exc != EFNEXIST)
				{
					if (exc != EOK)
						return exc;
					m_msgFlags.m_fIsDirectAttach = 1;
				}
			}
			else	// not a text MIME type
				m_msgFlags.m_fIsDirectAttach = 1;

			return EOK;
		}
	}

	// "hunt" for MIME boundary as long as it is not known
	if (IsMIMEBoundaryKnown())
		return EOK;

	exc = RFC822ExtractMIMEBoundary(lpszHdrValue, m_szMIMEBoundary, MAX_RFC822_MIME_BOUNDARY_LEN);
	if (exc != EFNEXIST)
	{
		if (EOK == exc)
			m_ulMIMEBoundaryLen = _tcslen(m_szMIMEBoundary);

		return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

// we only do this to detect direct attachments
EXC_TYPE CRFC822MsgParser::HandleContentDisposition (LPCTSTR	lpszHdrValue)
{
	if (IsEmptyStr(lpszHdrValue))
		return EEMPTYENTRY;

	if (IsDirectAttach())
		return EOK;

	EXC_TYPE		exc=ExtractAttachName(lpszHdrValue);
	if (EFNEXIST == exc)	// OK if not found attachment name (maybe inline...)
		return EOK;

	if (exc != EOK)
		return exc;

	m_msgFlags.m_fIsDirectAttach = 1;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRFC822MsgParser::HandleAccumulatedHdr ()
{
	LPCTSTR	lpszHdrName=m_hdrData.GetHdrName();
	if (IsEmptyStr(lpszHdrName))	// OK not to have current hdr
		return EOK;

	LPCTSTR	lpszHdrValue=m_hdrData.GetHdrValue();
	EXC_TYPE	exc=EOK;
	if (_tcsicmp(lpszHdrName, pszStdContentTypeHdr) == 0)
		exc = HandleContentType(lpszHdrValue);
	else if (_tcsicmp(lpszHdrName, pszStdContentDisposition) == 0)
		exc = HandleContentDisposition(lpszHdrValue);

	m_hdrData.Reset();
	return exc;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRFC822MsgParser::HandleMsgHdr ()
{
	// "hunt" for MIME boundary only within msg hdrs
	if (IsMsgBody())
		return ESTATE;

	LPCTSTR	lpszAccHdrName=m_hdrData.GetHdrName();
	LPCTSTR	lpszHdrName=m_hdrParser.GetHdrName();
	EXC_TYPE	exc=EOK;
	if (_tcsicmp(lpszAccHdrName, lpszHdrName) != 0)
	{
		if ((exc=HandleAccumulatedHdr()) != EOK)
			return exc;
	}

	// acumulated headers of interest until we can analyze them in full
	if ((_tcsicmp(lpszHdrName, pszStdContentTypeHdr) == 0) ||
		 (_tcsicmp(lpszHdrName, pszStdContentDisposition) == 0))
	{
		LPCTSTR	lpszHdrValue=m_hdrParser.GetHdrValue();
		if ((exc=m_hdrData.AddData(lpszHdrName, lpszHdrValue)) != EOK)
			return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRFC822MsgParser::HandleMIMEBoundary (const BOOLEAN fIsLast)
{
	// ignore text up to 1st boundary delimiter from multipart MIME
	if ((RFC822_BEFORE_BODY_MSGSTATE != m_msgState) ||
		 (!IsMultipartMIME()) || (m_msgFlags.m_ulMIMEPart > 0))
		m_msgState = ((fIsLast) ? RFC822_BEFORE_MSG_END : RFC822_BEFORE_ATTHDRS_MSGSTATE);

	// make sure no further MIME boundary follows the last one
	if (IsLastMIMEPart())
		return ESTATE;

	// at boundary, attachment body has ended
	m_msgFlags.m_fIsLastMIMEPart = ((fIsLast) ? 1 : 0);
	m_msgFlags.m_ulMIMEPart++;
	m_msgFlags.m_fIsMIMESeparator = 1;

	// start of attachment headers expected, so prepare for it.
	m_hdrParser.Reset();
	m_hdrData.Reset();

	return EOK;
}

/*---------------------------------------------------------------------------*/

// sets current state to "newState" provided current state is "oldState".
// return TRUE if current state changed
BOOLEAN CRFC822MsgParser::SetCondState (const RFC822MSGSTATE	oldState,
													 const RFC822MSGSTATE	newState)
{
	if (oldState == m_msgState)
	{
		m_msgState = newState;
		return TRUE;
	}
	else
		return FALSE;
}

/*---------------------------------------------------------------------------*/

// determine if 1st part in multipart msg is body or attachment
EXC_TYPE CRFC822MsgParser::CheckMIMEFirstMsgPart (LPCTSTR lpszLine, const UINT32	ulLen)
{
	if ((RFC822_BEFORE_BODY_MSGSTATE != m_msgState) ||
		 (!IsMultipartMIME()) || IsMultiBodyPart() ||
		 (1U != (unsigned) m_msgFlags.m_ulMIMEPart))
		return EOK;

	if (IsEmptyStr(lpszLine) || (0 == ulLen))
		return EOK;

	LPTSTR	lpszLE=(LPTSTR) (lpszLine + ulLen);
	TCHAR		tch=*lpszLE;

	*lpszLE = _T('\0');
	EXC_TYPE	exc=m_hdrParser.ProcessHdr(lpszLine);
	*lpszLE = tch;	// restore char
	if (exc != EOK)
		return exc;

	LPCTSTR	lpszHdrName=m_hdrParser.GetHdrName();
	if (_tcsicmp(lpszHdrName, pszStdContentTypeHdr) != 0)
		return EOK;

	LPCTSTR	lpszHdrValue=m_hdrParser.GetHdrValue();
	LPCTSTR	lpszType=NULL, lpszSubType=NULL;
	UINT32	ulTLen=0, ulSLen=0;
	exc = rfc822ExtractContentTypePtrs(lpszHdrValue,
												  &lpszType, &ulTLen,
												  &lpszSubType, &ulSLen);
	if ((exc != EOK) || IsEmptyStr(lpszType) || (0 == ulTLen) ||
		 IsEmptyStr(lpszSubType) || (0 == ulSLen))
		return EOK;

	if (IsRFC822PlainText(lpszType,ulTLen,lpszSubType,ulSLen))
	{
		m_hdrParser.Reset();
		m_hdrData.Reset();
		m_msgFlags.m_fIsMultiBodyPart = 1;
	}
	else
	{
		m_msgState = RFC822_BEFORE_ATTHDRS_MSGSTATE;
		m_msgFlags.m_fIsDirectAttach = 1;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

// processes input line and changes state/data
//
// Note: some internal pointers may point to the original supplied
//			buffer. Therefore, it MUST NOT change while this object is
//			"in use" (i.e. until all calls to any of its methods have
//			been completed.
EXC_TYPE CRFC822MsgParser::ProcessLine (LPCTSTR			lpszOrgLine,
													 const UINT32	ulLineLen)
{
	EXC_TYPE	exc=EOK;
	UINT32	ulLen=ulLineLen;
	LPCTSTR	lpszLine=lpszOrgLine;
	BOOLEAN	fStateChanged=FALSE;

	if ((NULL == lpszLine) && (ulLen != 0))
		return EBADBUFF;

	// do not allow processing beyond end of message
	if (IsMsgEnd())
		return ESTATE;

	m_msgFlags.m_fIsMIMESeparator = 0;

	fStateChanged = SetCondState(RFC822_BEFORE_HDRS_MSGSTATE, RFC822_ENVELOPE_MSGSTATE);

	// Exchange 5.5 sometimes use a line full of "whitespace" to signal a blank line
	if ((RFC822_ENVELOPE_MSGSTATE == m_msgState) && (ulLen > 0))
	{
		if (_istspace(*lpszLine))
		{
			for (LPCTSTR	lpszChk=(lpszLine+1); ; lpszChk++)
			{
				// this means that line is full of "whitespace"
				if (_T('\0') == *lpszChk)
				{
					lpszLine = lpszChk;
					ulLen = 0;
					break;
				}

				if (!_istspace(*lpszChk))
					break;
			}
		}
	}

	// a blank line may signal either start of body or end of attachment headers
	// (or it may simply be a "simple" blank line)
	if (0 == ulLen)
	{
		// ignore any empty lines after last MIME boundary
		if (IsMultipartMIME() && IsLastMIMEPart())
			return EOK;

		// 1st blank line after direct attachment is right before the data
		if (IsDirectAttach())
		{
			if (IsMsgHdrs())
			{
				m_msgState = RFC822_BEFORE_ATTDATA_MSGSTATE;
				return EOK;
			}
		}

		// if moved from envelope headers to body, reset parser
		if (SetCondState(RFC822_ENVELOPE_MSGSTATE, RFC822_BEFORE_BODY_MSGSTATE))
		{
			m_hdrParser.Reset();
			if ((exc=HandleAccumulatedHdr()) != EOK)
				return exc;
			return EOK;
		}

			// any blank line after 1st one is data	(Note: do not change check order !!!)
		if (SetCondState(RFC822_BEFORE_ATTDATA_MSGSTATE, RFC822_ATTDATA_MSGSTATE) ||
			// 1st blank line after MIME boundary denotes start of data
			 SetCondState(RFC822_BEFORE_ATTHDRS_MSGSTATE, RFC822_BEFORE_ATTDATA_MSGSTATE) ||
			 // 1st blank line after attachment headers denotes start of data
			 SetCondState(RFC822_ATTHDRS_MSGSTATE, RFC822_BEFORE_ATTDATA_MSGSTATE))
		{
			m_hdrParser.Reset();
			if ((exc=HandleAccumulatedHdr()) != EOK)
				return exc;
			m_hdrData.Reset();
		}
		else
		{
			// skip any blank lines up to 1st MIME part
			if ((RFC822_BEFORE_BODY_MSGSTATE == m_msgState) && IsMultipartMIME() && (0 == m_msgFlags.m_ulMIMEPart))
				return EOK;

			// any blank line after 1st one is part of the body
			if (SetCondState(RFC822_BEFORE_BODY_MSGSTATE, RFC822_BODY_MSGSTATE))
			{
				m_hdrParser.Reset();
				m_hdrData.Reset();
			}
		}

		return EOK;
	}

	// at this point we know the line is not blank
	if (IsMsgHdrs() && (!IsMIMEBoundaryKnown()))
	{
		// check for immediate MIME
		if ((ulLen > RFC822_MIME_BOUNDARY_DELIMS_LEN) &&
			 (strnicmp(lpszLine, pszMIMEBoundaryDelims, RFC822_MIME_BOUNDARY_DELIMS_LEN) == 0))
		{
			UINT32	ulMIMELen=(ulLen - RFC822_MIME_BOUNDARY_DELIMS_LEN);
			LPCTSTR	lpszMIMEBoundary=(lpszLine + RFC822_MIME_BOUNDARY_DELIMS_LEN);

			// check if also last...
			BOOLEAN fIsLast=FALSE;
			if (ulMIMELen > RFC822_MIME_BOUNDARY_DELIMS_LEN)
			{
				LPCTSTR	lpszMIMEEnd=(lpszMIMEBoundary + (ulMIMELen - RFC822_MIME_BOUNDARY_DELIMS_LEN));
				if (_tcsicmp(lpszMIMEEnd, pszMIMEBoundaryDelims) == 0)
				{
					ulMIMELen -= RFC822_MIME_BOUNDARY_DELIMS_LEN;
					fIsLast = TRUE;
				}
			}

			if (ulMIMELen > MAX_RFC822_MIME_BOUNDARY_LEN)
				return EMEM;

			_tcsncpy(m_szMIMEBoundary, lpszMIMEBoundary, ulMIMELen);
			m_szMIMEBoundary[ulMIMELen] = _T('\0');
			m_ulMIMEBoundaryLen = ulMIMELen;

			m_msgFlags.m_fIsLastMIMEPart = ((fIsLast) ? 1 : 0);
			return HandleMIMEBoundary(fIsLast);
		}
	}

	// "hunt" for MIME boundary if msg still within headers
	if (IsMsgHdrs())
	{
		if ((exc=m_hdrParser.ProcessHdr(lpszLine)) != EOK)
			return exc;

		if ((exc=HandleMsgHdr()) != EOK)
			return exc;

		return EOK;
	}

	// detect message end
	if ((1 == ulLen) && (_T('.') == *lpszLine))
	{
		// for multipart MIME the message end indicator must come AFTER last boundary
		if (IsMultipartMIME() && (!IsLastMIMEPart()))
			return EOK;

		m_msgState = RFC822_MSGEND_MSGSTATE;
		return EOK;
	}

	// ignore any text lines after last MIME boundary
	if (IsMultipartMIME() && IsLastMIMEPart())
		return EOK;

	// ignore text up to 1st boundary delimiter from multipart MIME
	if ((RFC822_BEFORE_BODY_MSGSTATE != m_msgState) || (!IsMultipartMIME()))
	{
		// do not change order !!!
		fStateChanged = SetCondState(RFC822_BEFORE_BODY_MSGSTATE, RFC822_BODY_MSGSTATE);
		fStateChanged = SetCondState(RFC822_BEFORE_ATTDATA_MSGSTATE, RFC822_ATTDATA_MSGSTATE);
	}

	// "suspect" body lines which begin with MIME boundary.
	//
	// Note: for some (unknown) reason, server may start the body by immediate
	//			MIME encoding, without "declaring" this in the msg headers. This
	//			is NOT handled (!!!)
	BOOLEAN	fIsBoundary=FALSE, fIsLast=FALSE;
	if ((exc=IsRFC822MIMEBoundary(lpszLine, m_szMIMEBoundary, &fIsBoundary, &fIsLast)) != EOK)
		return exc;

	if (fIsBoundary)
		return HandleMIMEBoundary(fIsLast);

	// for multipart msg check if 1st part is body or attachment
	CheckMIMEFirstMsgPart(lpszLine, ulLen);

	// At this point we handle only attachment headers
	if (SetCondState(RFC822_BEFORE_ATTHDRS_MSGSTATE, RFC822_ATTHDRS_MSGSTATE))
	{
		m_hdrParser.Reset();
		m_hdrData.Reset();
	}

	if (!IsAttachHdrs())
		return EOK;

	LPTSTR	lpszLE=(LPTSTR) (lpszLine + ulLen);
	TCHAR		tch=*lpszLE;
	*lpszLE = _T('\0');
	exc = m_hdrParser.ProcessHdr(lpszLine);
	*lpszLE = tch;	// restore char

	return exc;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRFC822MsgParser::ProcessLine (LPCTSTR lpszLine)
{
	if (NULL == lpszLine)
		return EPARAM;
	else
		return ProcessLine(lpszLine,_tcslen(lpszLine));
}

//////////////////////////////////////////////////////////////////////////////

CRFC822EnvelopeData::CRFC822EnvelopeData (const UINT32 ulAvgRcipsNum)
	: m_lpszDate(NULL), m_lpszSubject(NULL), m_lpszFrom(NULL),
	  m_lpszSender(NULL), m_lpszMsgId(NULL),
	  m_lpszInReplyTo(NULL),
	  m_ReplyTo(ulAvgRcipsNum, ulAvgRcipsNum),
//	  m_Bcc(ulAvgRcipsNum, ulAvgRcipsNum), 
	  m_Cc(ulAvgRcipsNum, ulAvgRcipsNum),
	  m_To(ulAvgRcipsNum, ulAvgRcipsNum) 
{
}

/*---------------------------------------------------------------------------*/

void CRFC822EnvelopeData::Clear ()
{
	strreleasebuf(m_lpszDate);
	strreleasebuf(m_lpszSubject);
	strreleasebuf(m_lpszFrom);
	strreleasebuf(m_lpszSender);
	strreleasebuf(m_lpszInReplyTo);
	strreleasebuf(m_lpszMsgId);

	m_To.Reset();
	m_Cc.Reset();
//	m_Bcc.Reset();
	m_ReplyTo.Reset();
}

/*---------------------------------------------------------------------------*/

// copy constructor
CRFC822EnvelopeData::CRFC822EnvelopeData (const CRFC822EnvelopeData& ed)
	: m_lpszDate(NULL), m_lpszSubject(NULL), m_lpszFrom(NULL),
	  m_lpszSender(NULL), m_lpszMsgId(NULL),
	  m_lpszInReplyTo(NULL),
	  m_ReplyTo(DEFAULT_RFC822_RCIPS_NUM, DEFAULT_RFC822_RCIPS_NUM),
//	  m_Bcc(DEFAULT_RFC822_RCIPS_NUM, DEFAULT_RFC822_RCIPS_NUM), 
	  m_Cc(DEFAULT_RFC822_RCIPS_NUM, DEFAULT_RFC822_RCIPS_NUM),
	  m_To(DEFAULT_RFC822_RCIPS_NUM, DEFAULT_RFC822_RCIPS_NUM) 
{
	EXC_TYPE	exc=UpdateData(ed);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRFC822EnvelopeData::UpdateData (const CRFC822EnvelopeData& ed)
{
	EXC_TYPE	exc=EOK;

	Clear();

	if ((exc=strupdatebuf(ed.GetDate(), m_lpszDate)) != EOK)
		return exc;
	if ((exc=strupdatebuf(ed.GetSubject(), m_lpszSubject)) != EOK)
		return exc;
	if ((exc=strupdatebuf(ed.GetOriginator(), m_lpszFrom)) != EOK)
		return exc;
	if ((exc=strupdatebuf(ed.GetSender(), m_lpszSender)) != EOK)
		return exc;
	if ((exc=strupdatebuf(ed.GetInReplyTo(), m_lpszInReplyTo)) != EOK)
		return exc;
	if ((exc=strupdatebuf(ed.GetMessageId(), m_lpszMsgId)) != EOK)
		return exc;

	if ((exc=m_ReplyTo.Merge(ed.m_ReplyTo)) != EOK)
		return exc;
	if ((exc=m_To.Merge(ed.m_To)) != EOK)
		return exc;
	if ((exc=m_Cc.Merge(ed.m_Cc)) != EOK)
		return exc;
//	if ((exc=m_Bcc.Merge(ed.m_Bcc)) != EOK)
//		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE urlstCfn (LPCTSTR		lpszHdrValue,
								  LPCTSTR		lpszName,
								  const UINT32	ulNameLen,
								  LPCTSTR		lpszAddr,
								  const UINT32	ulAddrLen,
								  BOOLEAN		*pfContEnum,
								  LPVOID			pArg)
{
	if (NULL == pArg) 
		return ECONTEXT;

	if (IsEmptyStr(lpszAddr) || (0 == ulAddrLen))
		return EBADADDR;

	EXC_TYPE			exc=EOK;
	TCHAR				szAddrPair[MAX_ADDRPAIR_EMAIL_LEN+2]=_T("");
	CStrlBuilder	strb(szAddrPair, MAX_ADDRPAIR_EMAIL_LEN);
	if (ulNameLen != 0)
	{
		if ((exc=strb.AddChar(_T('\"'))) != EOK)
			return exc;
		if ((exc=strb.AddChars(lpszName, ulNameLen)) != EOK)
			return exc;
		if ((exc=strb.AddChar(_T('\"'))) != EOK)
			return exc;
		if ((exc=strb.AddChar(_T(' '))) != EOK)
			return exc;
	}

	if ((exc=strb.AddChar(EMAIL_PATH_SDELIM)) != EOK)
		return exc;
	if ((exc=strb.AddChars(lpszAddr, ulAddrLen)) != EOK)
		return exc;
	if ((exc=strb.AddChar(EMAIL_PATH_EDELIM)) != EOK)
		return exc;

	CVSDCollection&	rl=*((CVSDCollection *) pArg);
	if ((exc=rl.AddItem(szAddrPair, (1+_tcslen(szAddrPair)) * sizeof(TCHAR))) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

inline EXC_TYPE UpdateStrHdr (LPCTSTR lpszHdrName, LPTSTR& lpszDst, LPCTSTR lpszVal)
{
	return ((NULL == lpszDst) ? strupdatebuf(lpszVal, lpszDst) : EEXIST);
}

typedef EXC_TYPE (*RFC822_ENVHDR_CFN)(CRFC822EnvelopeData&	ed,
												  LPCTSTR					lpszSubHdr,
												  LPCTSTR					lpszVal);

inline EXC_TYPE UpdateRecipsList (CVSDCollection& rl, LPCTSTR lpszVal)
{
	return (IsEmptyStr(lpszVal) ? EEMPTYENTRY : EnumRFC822AddrPairList(lpszVal, urlstCfn, (LPVOID) &rl));
}

inline EXC_TYPE UpdateReplyToList (CRFC822EnvelopeData&	ed,
											  LPCTSTR					lpszSubHdr,
											  LPCTSTR					lpszVal)
{
	return UpdateRecipsList((CVSDCollection &) ed.GetReplyAddress(), lpszVal);
}

inline EXC_TYPE UpdateToList (CRFC822EnvelopeData&	ed,
										LPCTSTR					lpszSubHdr,
										LPCTSTR					lpszVal)
{
	return UpdateRecipsList((CVSDCollection &) ed.GetToRecips(), lpszVal);
}

inline EXC_TYPE UpdateCcList (CRFC822EnvelopeData&	ed,
										LPCTSTR					lpszSubHdr,
										LPCTSTR					lpszVal)
{
	return UpdateRecipsList((CVSDCollection &) ed.GetCcRecips(), lpszVal);
}

//inline EXC_TYPE UpdateBccList (CRFC822EnvelopeData&	ed,
//										 LPCTSTR					lpszSubHdr,
//										 LPCTSTR					lpszVal)
//{
//	return UpdateRecipsList((CVSDCollection &) ed.GetBccRecips(), lpszVal);
//}

static const STR2PTRASSOC edAssocs[]={
	{	pszStdReplyToHdr,	(LPVOID) UpdateReplyToList	},
	{	pszStdToHdr,		(LPVOID) UpdateToList		},
	{	pszStdCcHdr,		(LPVOID) UpdateCcList		},
//	{	pszStdBccHdr,		(LPVOID) UpdateBccList		},
	{	NULL,					NULL								}	// mark end
};

static const CStr2PtrMapper edaMap(edAssocs, 0, FALSE);

BOOLEAN CRFC822EnvelopeData::IsListEnvelopeHdr (LPCTSTR lpszHdrName) const
{
	LPVOID	pV=NULL;
	EXC_TYPE	exc=edaMap.FindKey(lpszHdrName, pV);
	return (EOK == exc);
}

/*---------------------------------------------------------------------------*/

static const STR2PTRASSOC smplHdrs[]={
	{	pszStdDateHdr,			(LPVOID) UpdateStrHdr	},
	{	pszStdSubjectHdr,		(LPVOID) UpdateStrHdr	},
	{	pszStdFromHdr,			(LPVOID) UpdateStrHdr	},
	{	pszStdSenderHdr,		(LPVOID) UpdateStrHdr	},
	{	pszStdMessageIDHdr,	(LPVOID) UpdateStrHdr	},
	{	pszStdInReplyToHdr,	(LPVOID) UpdateStrHdr	},
	{	NULL,						NULL							}	// mark end
};

static const CStr2PtrMapper smplHdrsMap(smplHdrs, 0, FALSE);

BOOLEAN CRFC822EnvelopeData::IsSimpleEnvelopeHdr (LPCTSTR lpszHdrName) const
{
	LPVOID	pV=NULL;
	EXC_TYPE	exc=smplHdrsMap.FindKey(lpszHdrName, pV);
	return (EOK == exc);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRFC822EnvelopeData::ProcessHdrData (LPCTSTR lpszHdrName, LPCTSTR lpszHdrVal)
{
	if (IsEmptyStr(lpszHdrName))
		return EBADHEADER;

	// check if "simple" headers
	if (_tcsicmp(lpszHdrName, pszStdDateHdr) == 0)
		return UpdateStrHdr(lpszHdrName, m_lpszDate, lpszHdrVal);
	else if (_tcsicmp(lpszHdrName, pszStdSubjectHdr) == 0)
		return UpdateStrHdr(lpszHdrName, m_lpszSubject, lpszHdrVal);
	else if (_tcsicmp(lpszHdrName, pszStdFromHdr) == 0)
		return UpdateStrHdr(lpszHdrName, m_lpszFrom, lpszHdrVal);
	else if (_tcsicmp(lpszHdrName, pszStdSenderHdr) == 0)
		return UpdateStrHdr(lpszHdrName, m_lpszSender, lpszHdrVal);
	else if (_tcsicmp(lpszHdrName, pszStdMessageIDHdr) == 0)
		return UpdateStrHdr(lpszHdrName, m_lpszMsgId, lpszHdrVal);
	else if (_tcsicmp(lpszHdrName, pszStdInReplyToHdr) == 0)
		return UpdateStrHdr(lpszHdrName, m_lpszInReplyTo, lpszHdrVal);
	else if (_tcsicmp(lpszHdrName, pszStdBccHdr) == 0)
		return EOK;

	LPVOID	pV=NULL;
	EXC_TYPE	exc=edaMap.FindKey(lpszHdrName, pV);
	if (exc != EOK)
		return exc;

	RFC822_ENVHDR_CFN	lpfnHcfn=(RFC822_ENVHDR_CFN) pV;
	return (*lpfnHcfn)(*this, lpszHdrName, lpszHdrVal);
}

/*---------------------------------------------------------------------------*/
