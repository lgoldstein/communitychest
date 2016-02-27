#ifndef _RFC822_H_
#define _RFC822_H_

/*---------------------------------------------------------------------------*/
/*
 *	Contains definitions related to RFC822 (mainly e-mail headers)
 */
/*---------------------------------------------------------------------------*/

#include <time.h>

#include <util/string.h>
#include <util/errors.h>

#include <internet/general.h>

/*---------------------------------------------------------------------------*/

#define MAX_RFC822_HDR_NAME_LEN	32

/*---------------------------------------------------------------------------*/

/* standard headers (including ':') */
extern const TCHAR pszStdFromHdr[];
extern const TCHAR pszStdSenderHdr[];
extern const TCHAR pszStdToHdr[];
extern const TCHAR pszStdCcHdr[];
extern const TCHAR pszStdBccHdr[];
extern const TCHAR pszStdDateHdr[];
extern const TCHAR pszStdMIMEVersionHdr[];
extern const TCHAR pszStdReturnPathHdr[];
extern const TCHAR pszStdReplyToHdr[];
extern const TCHAR pszStdReplyCcHdr[];
extern const TCHAR pszStdInReplyToHdr[];
extern const TCHAR pszStdReceivedHdr[];
extern const TCHAR pszStdSubjectHdr[];

extern const TCHAR pszStdContentTypeHdr[];
extern const TCHAR pszStdContentLengthHdr[];
extern const TCHAR pszStdContentLocationHdr[];
extern const TCHAR pszStdContentIDHdr[];
extern const TCHAR pszStdContentXferEncoding[];
extern const TCHAR pszStdContentDisposition[];
extern const TCHAR pszStdContentDescription[];
extern const TCHAR pszStdContentDuration[];
extern const TCHAR pszStdContentLanguage[];
extern const TCHAR pszStdContentMD5Hdr[];
extern const TCHAR pszStdContentBaseHdr[];

extern const TCHAR pszStdMessageIDHdr[];
extern const TCHAR pszStdImportanceHdr[];
extern const TCHAR pszStdSensitivityHdr[];
extern const TCHAR pszStdStatusHdr[];

extern const TCHAR pszStdReturnReceiptToHdr[];
extern const TCHAR pszStdReturnReceiptCcHdr[];
extern const TCHAR pszStdDispositionNotificationToHdr[];

extern const TCHAR pszStdResentFromHdr[];
extern const TCHAR pszStdResentSenderHdr[];
extern const TCHAR pszStdResentToHdr[];
extern const TCHAR pszStdResentCcHdr[];
extern const TCHAR pszStdResentDateHdr[];
extern const TCHAR pszStdResentMessageIDHdr[];

extern const TCHAR pszStdApparentlyToHdr[];
extern const TCHAR pszStdApparentlyCcHdr[];
extern const TCHAR pszStdApparentlyBccHdr[];

/*---------------------------------------------------------------------------*/

/* NULL terminated list of all standard e-mail headers */
extern LPCTSTR pszStdEMailHdrs[];

/*---------------------------------------------------------------------------*/

/* some "well-known" X-hdrs */
extern const TCHAR pszXPriorityHdr[];
extern const TCHAR pszXMailerHdr[];

/*---------------------------------------------------------------------------*/

/* some "well-known" values for :X-Priority:" header */
typedef enum {
	RFC822_HHXPRIORITY=1,	/* highest */
	RFC822_HIXPRIORITY=2,	/* high */
	RFC822_NRXPRIORITY=3,	/* normal */
	RFC822_LOXPRIORITY=4,	/* low */
	RFC822_LLXPRIORITY=5,	/* lowest */
	RFC822_BDXPRIORITY=7
} RFC822XPRIORITYCASE;

/*
 *	Returns a (virtual) numerical value which represents the importance/priority:
 *
 *		0 = normal
 *		> 0 high - the more positive the higher the importance/priority
 *		< 0 low - the more negative the lower the importance/priority
 *
 *	Note: NULL/empty means normal (!)
 */
extern EXC_TYPE XlateRFC822Importance (LPCTSTR lpszImportance, int *pImp);
extern EXC_TYPE XlateRFC822XPriority (LPCTSTR lpszXPriority, int *pImp);

/*---------------------------------------------------------------------------*/

extern EXC_TYPE ValidateRFC822LocalMailPart (LPCTSTR lpszPart, const UINT32 ulPartLen);
/* Note: domain should NOT include the preceding '@' */
extern EXC_TYPE ValidateRFC822MailDomainPart (LPCTSTR lpszDomain, const UINT32 ulDLen);
extern EXC_TYPE ValidateRFC822EmailAddr (LPCTSTR lpszAddr, const UINT32 ulAddrLen);

#ifdef __cplusplus
inline EXC_TYPE ValidateRFC822MailAlias (LPCTSTR lpszAlias)
{
	return ValidateRFC822LocalMailPart(lpszAlias, GetSafeStrlen(lpszAlias));
}

inline EXC_TYPE ValidateRFC822MailDomain (LPCTSTR lpszDomain)
{
	return ValidateRFC822MailDomainPart(lpszDomain, GetSafeStrlen(lpszDomain));
}

inline EXC_TYPE ValidateRFC822Email (LPCTSTR lpszAddr)
{
	return ValidateRFC822EmailAddr(lpszAddr, GetSafeStrlen(lpszAddr));
}
#else
#	define ValidateRFC822Email(lpszAddr)			ValidateRFC822EmailAddr((lpszAddr), GetSafeStrlen(lpszAddr))
#	define ValidateRFC822MailAlias(lpszAlias)		ValidateRFC822LocalMailPart((lpszAlias), GetSafeStrlen(lpszAlias))
#	define ValidateRFC822MailDomain(lpszDomain)	ValidateRFC822MailDomainPart((lpszDomain), GetSafeStrlen(lpszDomain))
#endif	/* __cplusplus */

/*---------------------------------------------------------------------------*/

extern const TCHAR szNonMailUserLocalMailPart[];
extern const TCHAR szNonMailUserMailDomainPart[];

extern BOOLEAN IsNonMailUserLocalMailPart (LPCTSTR lpszPart, const UINT32 ulPartLen);
extern BOOLEAN IsNonMailUserMailDomainPart (LPCTSTR lpszPart, const UINT32 ulPartLen);
extern BOOLEAN IsNonMailUserEmailAddress (LPCTSTR lpszAddr, const UINT32 ulAddrLen);

#ifdef __cplusplus
inline BOOLEAN IsNonMailUserMailAlias (LPCTSTR lpszAlias)
{
	return IsNonMailUserLocalMailPart(lpszAlias, GetSafeStrlen(lpszAlias));
}

inline BOOLEAN IsNonMailUserMailDomain (LPCTSTR lpszDomain)
{
	return IsNonMailUserMailDomainPart(lpszDomain, GetSafeStrlen(lpszDomain));
}

inline BOOLEAN IsNonMailUserEmail (LPCTSTR lpszAddr)
{
	return IsNonMailUserEmailAddress(lpszAddr, GetSafeStrlen(lpszAddr));
}
#else
#	define IsNonMailUserMailAlias(lpszAlias)		IsNonMailUserLocalMailPart((lpszAlias), GetSafeStrlen(lpszAlias))
#	define IsNonMailUserMailDomain(lpszDomain)	IsNonMailUserMailDomainPart((lpszDomain), GetSafeStrlen(lpszDomain))
#	define IsNonMailUserEmail(lpszAddr)				IsNonMailUserEmailAddress((lpszAddr), GetSafeStrlen(lpszAddr))
#endif	/* __cplusplus */

/*---------------------------------------------------------------------------*/

extern EXC_TYPE ValidateRFC822MessageIDChars (LPCTSTR lpszMsgID, const UINT32 ulIDLen);

#ifdef __cplusplus
inline EXC_TYPE ValidateRFC822MessageID (LPCTSTR lpszMsgID)
{
	return ValidateRFC822MessageIDChars(lpszMsgID, GetSafeStrlen(lpszMsgID));
}

// NOTE: returns EOK if zero ID length to begin with
extern EXC_TYPE StripRFC822MessageIDRemark (LPCTSTR lpszMsgID, const UINT32 ulIDLen, /* OUT */ LPCTSTR& lpszEffID, /* OUT */ UINT32& ulEffLen);

inline EXC_TYPE StripRFC822MessageIDRemark (LPCTSTR lpszMsgID, /* OUT */ LPCTSTR& lpszEffID, /* OUT */ UINT32& ulEffLen)
{
	return StripRFC822MessageIDRemark(lpszMsgID, GetSafeStrlen(lpszMsgID), lpszEffID, ulEffLen);
}
#else
#	define ValidateRFC822MessageID(id) ValidateRFC822MessageIDChars((id),GetSafeStrlen(id))
#endif	/* __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// useful class for hdr data accumulation - including continued headers, which
// are concatenated to a single value (greedy increase)
class CRFC822HdrData {
	private:
		TCHAR		m_szHdrName[MAX_RFC822_HDR_NAME_LEN+2];
		LPTSTR	m_lpszHdrValue;
		UINT32	m_ulVLen;
		UINT32	m_ulGLen;
		UINT32	m_ulCLen;

		void ReleaseValue ();
		EXC_TYPE CheckHeaderName (LPCTSTR lpszHdrName);

	public:
		CRFC822HdrData (LPCTSTR			lpszHdrName,
							 LPCTSTR			lpszHdrValue=NULL,
							 const UINT32	ulGrowLen=MAX_RFC822_HDR_NAME_LEN);

		// also default constructor
		CRFC822HdrData (const UINT32	ulGrowLen=MAX_RFC822_HDR_NAME_LEN);

		// copy constructor
		CRFC822HdrData (const CRFC822HdrData& hd);

		EXC_TYPE UpdateData (const CRFC822HdrData& hd)
		{
			Reset();
			return AddData(hd.m_szHdrName, hd.m_lpszHdrValue);
		}

		// assignment operator
		CRFC822HdrData& operator= (const CRFC822HdrData& hd)
		{
			UpdateData(hd);
			return *this;
		}

		LPCTSTR GetHdrName () const
		{
			return m_szHdrName;
		}

		LPCTSTR GetHdrValue () const
		{ 
			return ((NULL == m_lpszHdrValue) ? _T("") : m_lpszHdrValue);
		}

		UINT32 GetHdrLen () const
		{
			return m_ulCLen;
		}

		EXC_TYPE SetData (LPCTSTR lpszHdrName, LPCTSTR lpszHdrValue, const UINT32 ulVLen);
		EXC_TYPE SetData (LPCTSTR lpszHdrName, LPCTSTR lpszHdrValue)
		{
			return SetData(lpszHdrName, lpszHdrValue, ((NULL == lpszHdrValue) ? 0 : _tcslen(lpszHdrValue)));
		}

		// if header name mismatch, then resets data
		EXC_TYPE AddData (LPCTSTR lpszHdrName, LPCTSTR lpszHdrValue, const UINT32 ulVLen);
		EXC_TYPE AddData (LPCTSTR lpszHdrName, LPCTSTR lpszHdrValue)
		{
			return AddData(lpszHdrName, lpszHdrValue, ((NULL == lpszHdrValue) ? 0 : _tcslen(lpszHdrValue)));
		}

		EXC_TYPE AddData (LPCTSTR lpszHdrValue, const UINT32 ulVLen)
		{
			return AddData(m_szHdrName, lpszHdrValue, ulVLen);
		}

		EXC_TYPE AddData (LPCTSTR lpszHdrValue)
		{
			return AddData(m_szHdrName, lpszHdrValue);
		}

		// Note: "ReplaceData" method(s) will cause the passed pointer to be auto-deleted upon destruction
		EXC_TYPE ReplaceData (LPCTSTR lpszHdrName, LPTSTR lpszHdrValue);

		EXC_TYPE ReplaceData (LPTSTR lpszHdrValue)
		{
			return ReplaceData(m_szHdrName, lpszHdrValue);
		}

		void Reset ();

		virtual ~CRFC822HdrData ()
		{
			ReleaseValue();
		}
};	// end of rfc822 hdr data class

typedef CRFC822HdrData *LPCRFC822HDRDATA;
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

/* Do not change order or add new values w/o updating "rfc822.cpp" !!! */
typedef enum {
	RFC822_NONE_ENC,	/* no specific encoding */
	RFC822_B64_ENC,	/* base64 */
	RFC822_QP_ENC,		/*	quoted-printable */
	RFC822_7BIT_ENC,
	RFC822_8BIT_ENC,
	RFC822_BAD_ENC
} RFC822ENCCASE;

#define RFC822_ENCS_NUM	RFC822_BAD_ENC
#define fIsBadRFC822EncCase(c) (((unsigned) (c)) >= RFC822_BAD_ENC)

/* translates the content transfer encoding string to its enumeration case 
 * returns RFC822_BAD_ENC if unknown encoding
 */
extern RFC822ENCCASE RFC822EncodingStr2Case (const char lpszEncoding[]);

/* returns NULL if bad case */
extern const char *RFC822EncodingCase2Str (const RFC822ENCCASE encCase);

/*---------------------------------------------------------------------------*/

/* Returns recommended encoding - (Q)uoted-Printable or (B)ase64
 *
 * Note: if no encoding required (i.e. RFC822_7BIT_ENC or RFC822_NONE_ENC) then returned
 *			encoding length is same as header length
 */
extern EXC_TYPE DetermineRFC822HdrValEncoding (LPCTSTR			lpszHdrValue,
															  const UINT32		ulHdrLen,
															  RFC822ENCCASE	*phdrEnc,
															  UINT32				*pulEncLen);

#ifdef __cplusplus
inline EXC_TYPE DetermineRFC822HdrStrValEncoding (LPCTSTR			lpszHdrValue,
																  RFC822ENCCASE	*phdrEnc,
																  UINT32				*pulEncLen)
{
	return DetermineRFC822HdrValEncoding(lpszHdrValue, GetSafeStrlen(lpszHdrValue), phdrEnc, pulEncLen);
}
#else
#define DetermineRFC822HdrStrValEncoding(h,pe,pl)	\
	DetermineRFC822HdrValEncoding(h, GetSafeStrlen(h), pe, pl)
#endif	/* of __cplusplus */

/* Builds a "=?xxx?Q/B?.....?=" representation of the header
 *
 * Note: encoding MUST be either QP or BASE64 (otherwise error returned)
 */
extern EXC_TYPE BuildRFC822CharsetHdrValue (LPCTSTR					lpszCharset,
														  const RFC822ENCCASE	eHdrEnc,
														  LPCTSTR					lpszHdrValue,
														  const UINT32				ulHdrLen,
														  LPTSTR						lpszOutput,
														  const UINT32				ulMaxLen);

/*		Determines best encoding with charset and performs it. If no translation required,
 * then returns immediately (and the recommened header encoding will NOT be QP or Base64)
 */
extern EXC_TYPE BuildCanonicalRFC822CharsetHdrValue (LPCTSTR			lpszCharset,
																	  LPCTSTR			lpszHdrValue,
																	  const UINT32		ulHdrLen,
																	  RFC822ENCCASE	*phdrEnc,
																	  LPTSTR				lpszOutput,
																	  const UINT32		ulMaxLen);

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// stores <hdr,value> pairs and enables quick access according to hdr name
class CRFC822HdrsTbl {
	private:
		LPCRFC822HDRDATA	*m_pHdrs;
		CStr2PtrMapper		m_Mapper;	// mapping for quick access
		UINT32				m_ulHNum;	// max available
		UINT32				m_ulCNum;	// currently used

		EXC_TYPE AllocateHdrData (LPCRFC822HDRDATA& pHdrData);

		void Cleanup ();

	public:
		// also default constructor
		CRFC822HdrsTbl (const UINT32 ulMaxHdrs=0);

		// copy constructor
		CRFC822HdrsTbl (const CRFC822HdrsTbl& ht);

		EXC_TYPE SetSize (const UINT32 ulMaxHdrs);
		UINT32 GetSize () const { return m_ulCNum; }

		EXC_TYPE SetHdrData (const char pszHdrName[], const char pszHdrValue[], const UINT32 ulVLen);
		EXC_TYPE SetHdrData (const char pszHdrName[], const char pszHdrValue[])
		{
			return SetHdrData(pszHdrName, pszHdrValue, ((NULL == pszHdrValue) ? 0 : _tcslen(pszHdrValue)));
		}
		EXC_TYPE SetHdrData (const CRFC822HdrData& hd)
		{
			return SetHdrData(hd.GetHdrName(), hd.GetHdrValue());
		}

		EXC_TYPE AddHdrData (const char pszHdrName[], const char pszHdrValue[], const UINT32 ulVLen);
		EXC_TYPE AddHdrData (const char pszHdrName[], const char pszHdrValue[])
		{
			return AddHdrData(pszHdrName, pszHdrValue, ((NULL == pszHdrValue) ? 0 : _tcslen(pszHdrValue)));
		}

		EXC_TYPE AddHdrData (const CRFC822HdrData& hd)
		{
			return AddHdrData(hd.GetHdrName(), hd.GetHdrValue());
		}

		// Note: the header value is auto-released (deallocated...)
		EXC_TYPE ReplaceHdrData (const char pszHdrName[], char *pszHdrValue);

		CRFC822HdrsTbl& operator += (const CRFC822HdrData& hd)
		{
			AddHdrData(hd);
			return *this;
		}

		EXC_TYPE GetHdrData (const char pszHdrName[], LPCRFC822HDRDATA& pHdrData) const;
		LPCRFC822HDRDATA operator[] (const char pszHdrName[]) const;

		// can be used to enumerate all headers
		EXC_TYPE GetHdrData (const UINT32 ulIdx, LPCRFC822HDRDATA& pHdrData) const;
		LPCRFC822HDRDATA operator[] (const UINT32 ulIdx) const;

		EXC_TYPE GetHdrData (const char pszHdrName[], LPCTSTR& lpszHdrValue) const;

		BOOLEAN IsHdrInTbl (const char pszHdrName[]) const;

		// header data is set only if no previous instance found
		EXC_TYPE UpdateHdrData (const char pszHdrName[], const char pszHdrValue[], const UINT32 ulVLen)
		{
			if (IsHdrInTbl(pszHdrName))
				return EOK;
			else
				return SetHdrData(pszHdrName, pszHdrValue, ulVLen);
		}

		EXC_TYPE UpdateHdrData (const char pszHdrName[], const char pszHdrValue[])
		{
			return UpdateHdrData(pszHdrName, pszHdrValue, ((NULL == pszHdrValue) ? 0 : _tcslen(pszHdrValue)));
		}

		EXC_TYPE UpdateHdrData (const CRFC822HdrData& hd)
		{
			return UpdateHdrData(hd.GetHdrName(), hd.GetHdrValue());
		}

		// copies supplied table OVER this one
		EXC_TYPE UpdateHdrsTbl (const CRFC822HdrsTbl& ht);
		CRFC822HdrsTbl& operator= (const CRFC822HdrsTbl& ht)
		{
			UpdateHdrsTbl(ht);
			return *this;
		}

		EXC_TYPE Reset ();

		virtual ~CRFC822HdrsTbl () { Cleanup(); }
};	// end of rfc822 hdrs tbl class

typedef CRFC822HdrsTbl *LPCRFC822HDRSTBL;
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

#define MAX_RFC822_DATETIME_LEN	42
#define MAX_RFC822_TIMEZONE_LEN	 8	/* including enclosing brackets and space(s) */

#define RFC822_POSITIVE_GMT	_T('+')
#define RFC822_NEGATIVE_GMT	_T('-')

#ifdef __cplusplus
/*	tmZone is the difference (in seconds) between the GMT and LOCAL time */
extern EXC_TYPE rfc822AddGMTOffset (const int tmZone, IStrlBuilder& strb);

inline EXC_TYPE rfc822BuildGMTOffset (const int tmZone, LPTSTR lpszGMTOffset, const UINT32 ulMaxLen)
{
	return rfc822AddGMTOffset(tmZone, CStrlBuilder(lpszGMTOffset, ulMaxLen));
}
#else
/*	tmZone is the difference (in seconds) between the GMT and LOCAL time */
extern EXC_TYPE rfc822BuildGMTOffset (const int tmZone, LPTSTR lpszGMTOffset, const UINT32 ulMaxLen);
#endif	/* __cplusplus */

#define MAX_RFC822_DATEENCODE_LEN	(MAX_RFC822_DATETIME_LEN+MAX_RFC822_TIMEZONE_LEN)

/* Format is:
 *
 *	"%s, %u %s %u %02u:%02u:%02u +/-nnnn",
 *		  day_of_week[tms.tm_wday], tms.tm_mday,
 *		  month_of_year[tms.tm_mon], (tms.tm_year + 1900),
 *		  tms.tm_hour, tms.tm_min, tms.tm_sec, _timezone/3600);
 *
 * Note: if supplied GMT offset is NULL/empty then "_timezone" variable is used
 */

extern EXC_TYPE BuildRFC822DTSDateTime (const struct tm	*pDT,
													 const char			lpszGMTOffset[],
													 char					lpszDateTime[],
													 const UINT32		ulMaxLen);

extern EXC_TYPE BuildRFC822TVDateTime (const time_t	tVal,
													const char		lpszGMTOffset[],
													char				lpszDateTime[],
													const UINT32	ulMaxLen);

extern EXC_TYPE BuildRFC822TVZoneDateTime (const time_t	tVal,
														 const int		tmZone,
														 char				lpszDateTime[],
														 const UINT32	ulMaxLen);

extern EXC_TYPE DecodeRFC822DateTime (const char szDTS[], struct tm *pDT);

/*---------------------------------------------------------------------------*/

/* delimiters for header comment(s) */
#define RFC822_HDR_COMMENT_SDELIM	_T('(')
#define RFC822_HDR_COMMENT_EDELIM	_T(')')

/* delimiters for message ID */
#define RFC822_MSGID_SDELIM			_T('<')
#define RFC822_MSGID_EDELIM			_T('>')

#define MAX_RFC822_MSGID_LEN	256

/*---------------------------------------------------------------------------*/

/* NULL terminated list of some non-standard but widely used headers */
extern const char *pszXStdEmailHdrs[];

/*---------------------------------------------------------------------------*/

/* special internal X-Headers */
extern const TCHAR szXLogfError[];
extern const TCHAR szXMsgSize[];
extern const TCHAR szXBody[];
extern const TCHAR szXSockVal[];
extern const TCHAR szXContHdr[];
extern const TCHAR szXConnAbort[];
extern const TCHAR szXAuthAbort[];
extern const TCHAR szXHdrParser[];
extern const TCHAR szXThreadStart[];
extern const TCHAR szXDynamicServer[];
extern const TCHAR szXThreadEnd[];

/*---------------------------------------------------------------------------*/

/* delimiters used to denote a MIME boundary */
#define RFC822_MIME_BOUNDARY_DELIMS_LEN	 2
#define MAX_RFC822_MIME_BOUNDARY_LEN		70	/* as per RFC2046 */

extern const TCHAR pszMIMEBoundaryDelims[];

extern EXC_TYPE IsRFC822MIMEBoundary (const char	lpszLine[],
												  const char	lpszMIMEBoundary[],
												  BOOLEAN		*pfIsBoundary,
												  BOOLEAN		*pfIsLast);

/*---------------------------------------------------------------------------*/

/* some well known MIME type values */

#define MAX_RFC822_CONTENT_TYPE_LEN			32

extern const TCHAR pszMIMEMultipartType[];
extern const TCHAR pszMIMEApplicationType[];
extern const TCHAR pszMIMEAudioType[];
extern const TCHAR pszMIMEImageType[];
extern const TCHAR pszMIMETextType[];
extern const TCHAR pszMIMEMessageType[];

/* NULL terminated list of MIME types */
extern LPCTSTR pszMIMETypesList[];

/*---------------------------------------------------------------------------*/

/* some well known MIME sub type values */

#define MAX_RFC822_CONTENT_SUBTYPE_LEN		64

extern const TCHAR pszMIMEOctetStreamSubType[];
extern const TCHAR pszMIMEMixedSubType[];
extern const TCHAR pszMIMERelatedSubType[];
extern const TCHAR pszMIMEDigestSubType[];
extern const TCHAR pszMIMEParallelSubType[];
extern const TCHAR pszMIMETiffSubType[];
extern const TCHAR pszMIMEPlainSubType[];
extern const TCHAR pszMIMEHtmlSubType[];
extern const TCHAR pszMIMEAlternativeSubType[];
extern const TCHAR pszMIMERfc822SubType[];
extern const TCHAR pszMIMEVoiceMsgSubType[];
extern const TCHAR pszMIMEFaxMsgSubType[];
extern const TCHAR pszMIMEDirectorySubType[];
extern const TCHAR pszMIME32KADPCMSubType[];
extern const TCHAR pszMIMEReportSubType[];
extern const TCHAR pszMIMEDlvryStatusSubType[];
extern const TCHAR pszMIMEPngSubType[];
extern const TCHAR pszMIMEWaveSubType[];
extern const TCHAR pszMIMEMSGSMSubType[];
extern const TCHAR pszMIMEBasicSubType[];
extern const TCHAR pszMIMEFormDataSubType[];

/* some non-standard multipart sub-types */
extern const TCHAR pszMIMEAppleDoubleSubType[];
extern const TCHAR pszMIMEMSTNEFSubType[];
extern const TCHAR pszMIMESMSMsgSubType[];

/* NULL terminated list of MIME sub types */
extern LPCTSTR pszMIMESubTypesList[];

/*---------------------------------------------------------------------------*/

#define MAX_RFC822_MIMETAG_LEN	\
	((MAX_RFC822_CONTENT_TYPE_LEN)+(MAX_RFC822_CONTENT_SUBTYPE_LEN)+2)

#define RFC822_MIMETAG_SEP		_T('/')

#ifdef __cplusplus
extern EXC_TYPE AddRFC822MIMETag (LPCTSTR lpszType, LPCTSTR lpszSubType, IStrlBuilder& strb);

extern EXC_TYPE BuildRFC822MIMETag (LPCTSTR lpszType, LPCTSTR lpszSubType, LPTSTR lpszTag, const UINT32 ulMaxLen);
#endif	/* __cplusplus */

/*---------------------------------------------------------------------------*/

#define MAX_RFC822_KEYWORD_LEN		64
#define MAX_RFC822_KWVAL_LEN			max(MAX_RFC822_MIME_BOUNDARY_LEN,256)
#define MAX_RFC822_KWPAIR_LEN	(MAX_RFC822_KEYWORD_LEN+MAX_RFC822_KWVAL_LEN+2)

#define RFC822_KEYWORD_VALUE_DELIM	_T('=')
#define RFC822_LIST_DELIM				_T(';')

/* some well known keywords in MIME headers */

extern const TCHAR pszMIMEBoundaryKeyword[];
extern const TCHAR pszMIMEFilenameKeyword[];
extern const TCHAR pszMIMECharsetKeyword[];
extern const TCHAR pszMIMENameKeyword[];
extern const TCHAR pszMIMEProfileKeyword[];
extern const TCHAR pszMIMEVoiceKeyword[];
extern const TCHAR pszMIMEVersionKeyword[];
extern const TCHAR pszMIMETypeKeyword[];
extern const TCHAR pszMIMEStartKeyword[];
extern const TCHAR pszMIMECodecKeyword[];

/* NULL terminated list of keywords */
extern LPCTSTR lpszMIMEKeywordsList[];

/*---------------------------------------------------------------------------*/

/* some dispositions */
extern const TCHAR pszMIMEAttachmentDisp[];
extern const TCHAR pszMIMEInlineDisp[];

/* NULL terminated list of dispositions */
extern LPCTSTR lpszMIMEDispositions[];

/*---------------------------------------------------------------------------*/

/*		Extracts a RFC822 header name & value. Returns the following values:
 *
 *		EOK - everything OK
 *		EOVERFLOW - if cannot accommodate either name or value in supplied buf(s)
 *		ECONTINUED - if 1st char is space - then name is set to empty and value
 *				is set to rest of value (including the leading space)
 *		EUDFFORMAT - if 1st char not space and not found header name delimiter ':'
 *		Otherwise - non EOK
 */

extern EXC_TYPE rfc822ExtractHdrData (const char	pszHdr[],
												  char			pszHdrName[],
												  const UINT32	ulNameLen,
												  char			pszHdrValue[],
												  const UINT32	ulValueLen);

/*		Extracts pointers to RFC822 header name & value. Returns the following
 * values:
 *
 *		EOK - everything OK
 *		ECONTINUED - if 1st char is space - then name is set to NULL and value
 *				is set to rest of value (including the leading space)
 *		EUDFFORMAT - if 1st char not space and not found header name delimiter ':'
 *		Otherwise - non EOK
 */

extern EXC_TYPE rfc822ExtractHdrPtrs (const char	pszHdr[],
												  const char	**ppszHdrName,
												  UINT32			*pulNameLen,
												  const char	**ppszHdrValue);

/*---------------------------------------------------------------------------*/

/* translates one of the RFC822 defined GMT codes to its value */
extern EXC_TYPE rfc822XlateImplicitGMTOffset (const char	lpszGMTOffset[],
															 int			*pnGMTOffset);

/* assumed format : "+/-HHMM" */
extern EXC_TYPE rfc822XlateExplicitGMTOffset (const char	lpszGMTOffset[],	/* empty == local */
															 int			*pnGMTOffset);

/* translates either implicit or explicit GMT offset(s) to its value */
extern EXC_TYPE rfc822XlateGMTOffset (const char	lpszGMTOffset[],	/* empty == local */
												  int				*pnGMTOffset);		/* seconds */

/* Note: assumes standard RFC822 date value */
extern EXC_TYPE rfc822ExtractGMTOffset (const char		*lpszDate,
													 char				*lpszOffsetGMT,
													 const UINT32	ulMaxGMTLen);

/*---------------------------------------------------------------------------*/

extern EXC_TYPE rfc822ExtractContentTypePtrs (const char	lpszContentType[],
															 const char	**lppszType,
															 UINT32		*pulTypeLen,
															 const char	**lppszSubType,
															 UINT32		*pulSubTypeLen);

/*---------------------------------------------------------------------------*/

extern BOOLEAN CompareRFC822TypeComp (const char	lpszSrcComp[],
												  const UINT32	ulSrcLen,
												  const char	lpszDstComp[]);

/*---------------------------------------------------------------------------*/

#define IsRFC822ContentType(lpszCType,ulTypeLen,lpszCmpType)	\
	CompareRFC822TypeComp(lpszCType,ulTypeLen,lpszCmpType)
#define IsRFC822ContentSubType(lpszSType,ulSubTypeLen,lpszCmpType)	\
	CompareRFC822TypeComp(lpszSType,ulSubTypeLen,lpszCmpType)
#define IsRFC822MIMEType(lpszT,ulTLen,lpszMT,lpszST,ulSTLen,lpszMST)	\
	(IsRFC822ContentType(lpszT,ulTLen,lpszMT) && IsRFC822ContentSubType(lpszST,ulSTLen,lpszMST))

/*---------------------------------------------------------------------------*/

#define IsRFC822MIMEMultipartType(lpszT,ulTLen)	\
	IsRFC822ContentType(lpszT,ulTLen,pszMIMEMultipartType)
#define IsRFC822MIMEApplicationType(lpszT,ulTLen)	\
	IsRFC822ContentType(lpszT,ulTLen,pszMIMEApplicationType)
#define IsRFC822MIMEAudioType(lpszT,ulTLen)	\
	IsRFC822ContentType(lpszT,ulTLen,pszMIMEAudioType)
#define IsRFC822MIMEImageType(lpszT,ulTLen)	\
	IsRFC822ContentType(lpszT,ulTLen,pszMIMEImageType)
#define IsRFC822MIMETextType(lpszT,ulTLen)	\
	IsRFC822ContentType(lpszT,ulTLen,pszMIMETextType)

/*---------------------------------------------------------------------------*/

#define IsRFC822MixedSubType(lpszS,ulSLen)	\
	IsRFC822ContentSubType(lpszS,ulSLen,pszMIMEMixedSubType)
#define IsRFC822PlainSubType(lpszS,ulSLen)	\
	IsRFC822ContentSubType(lpszS,ulSLen,pszMIMEPlainSubType)
#define IsRFC822HtmlSubType(lpszS,ulSLen)	\
	IsRFC822ContentSubType(lpszS,ulSLen,pszMIMEHtmlSubType)
#define IsRFC822OctetStreamSubType(lpszS,ulSLen)	\
	IsRFC822ContentSubType(lpszS,ulSLen,pszMIMEOctetStreamSubType)

extern BOOLEAN IsRFC822MultipartSubType (LPCTSTR lpszSubType);
extern BOOLEAN IsRFC822MsgSubType (LPCTSTR lpszSubType);

/*---------------------------------------------------------------------------*/

#define IsRFC822MultipartMixed(lpszT,ulTLen,lpszST,ulSTLen)	\
	IsRFC822MIMEType(lpszT,ulTLen,pszMIMEMultipartType,lpszST,ulSTLen,pszMIMEMixedSubType)
#define IsRFC822AppOctetStream(lpszT,ulTLen,lpszST,ulSTLen)	\
	IsRFC822MIMEType(lpszT,ulTLen,pszMIMEApplicationType,lpszST,ulSTLen,pszMIMEOctetStreamSubType)
#define IsRFC822PlainText(lpszT,ulTLen,lpszST,ulSTLen)	\
	IsRFC822MIMEType(lpszT,ulTLen,pszMIMETextType,lpszST,ulSTLen,pszMIMEPlainSubType)
#define IsRFC822EmbeddedMsg(lpszT,ulTLen,lpszST,ulSTLen)	\
	IsRFC822MIMEType(lpszT,ulTLen,pszMIMEMessageType,lpszST,ulSTLen,pszMIMERfc822SubType)

/*---------------------------------------------------------------------------*/

#define MAX_RFC822_ATTACH_NAME_LEN		96
#define MAX_RFC822_CONTENT_ENCODE_LEN	24
#define MAX_RFC822_CHARSET_LEN			24

/*---------------------------------------------------------------------------*/

extern EXC_TYPE RFC822FindKeywordValue (const char	lpszValue[],
													 const char	lpszKeyword[],
													 const char	**lppszKeyVal,
													 UINT32		*pulValLen);

/* returns EFNEXIST if not found keyword */
extern EXC_TYPE RFC822GetKeywordValue (const char		lpszValue[],
													const char		lpszKeyword[],
													char				lpszKeyVal[],
													const UINT32	ulValLen);

/*---------------------------------------------------------------------------*/

#define RFC822FindMIMEBoundary(lpszV,lppszMB,pulMBLen)	\
	RFC822FindKeywordValue(lpszV,pszMIMEBoundaryKeyword,lppszMB,pulMBLen)

#define RFC822ExtractMIMEBoundary(lpszV,lpszMB,ulMBLen)	\
	RFC822GetKeywordValue(lpszV,pszMIMEBoundaryKeyword,lpszMB,ulMBLen)

#define RFC822FindCharset(lpszV,lppszCS,pulCSLen)	\
	RFC822FindKeywordValue(lpszV,pszMIMECharsetKeyword,lppszCS,pulCSLen)

#define RFC822ExtractCharset(lpszV,lpszCS,ulCSLen)	\
	RFC822GetKeywordValue(lpszV,pszMIMECharsetKeyword,lpszCS,ulCSLen)


/*---------------------------------------------------------------------------*/

/* returns EFNEXIST if not found */
extern EXC_TYPE RFC822FindAttachName (const char	lpszValue[],
												  const char	**lppszAttachName,
												  UINT32			*pulNameLen);

extern EXC_TYPE RFC822ExtractAttachName (const char	lpszValue[],
													  char			lpszAttachName[],
													  const UINT32	ulNameLen);

/*---------------------------------------------------------------------------*/

extern EXC_TYPE DecodeRFC822AddrPair (LPCTSTR	lpszHdrValue,
												  LPCTSTR	*lppszName,
												  UINT32		*pulNameLen,
												  LPCTSTR	*lppszAddr,
												  UINT32		*pulAddrLen);
#ifdef __cplusplus
inline EXC_TYPE DecodeRFC822AddrPair (LPCTSTR	lpszHdrValue,
												  LPCTSTR&	lpszName,
												  UINT32&	ulNameLen,
												  LPCTSTR&	lpszAddr,
												  UINT32&	ulAddrLen)
{
	return DecodeRFC822AddrPair(lpszHdrValue, &lpszName, &ulNameLen, &lpszAddr, &ulAddrLen);
}
#endif	/* of __cplusplus */

/* '\0' terminated list of characters not allowed in RFC822 atoms
 *
 * Note: includes only characters above (and including) space (ASCII 32). All
 *		characters below ASCII 32 or above ASCII 126 are automatically considered
 *		illegal in an atom. Use "IsRFC822AtomChar" wherever possible instead of
 *		doing your own checks.
 */
extern const TCHAR nonRFC822AtomChars[];

/*		Returns TRUE if character is allowed in an RFC822 atom. As per RFC2822
 * an atom text is defined as:
 *
 * atext = ALPHA / DIGIT / ; Any character except controls, SP, and specials.
 *         "!" / "#" /
 *         "$" / "%" /
 *         "&" / "'" /
 *         "*" / "+" /
 *         "-" / "/" /
 *         "=" / "?" /
 *         "^" / "_" /
 *         "`" / "{" /
 *         "|" / "}" /
 *         "~"
 */
extern BOOLEAN IsRFC822AtomChar (const TCHAR ch);

/*
 *		Returns EOK if the supplied data conforms to RFC822 atom defintion.
 * Note: an empty string is NOT considered an atom. See "IsRFC822AtomChar"
 * for specification what consists an atom
 */

extern EXC_TYPE CheckRFC822AtomText (LPCTSTR lpszAtom, const UINT32 ulALen);

#ifdef __cplusplus
inline EXC_TYPE CheckRFC822AtomText (LPCTSTR lpszAtom)
{
	return (NULL == lpszAtom) ? EEMPTYENTRY : CheckRFC822AtomText(lpszAtom, ::_tcslen(lpszAtom));
}

extern EXC_TYPE BuildRFC822AddrPair (LPCTSTR			lpszRecipName,	/* may be NULL */
												 LPCTSTR			lpszRecipAddr,
												 IStrlBuilder&	aPair);
#endif	/* of __cplusplus */

extern EXC_TYPE BuildRFC822AddrPair (LPCTSTR			lpszRecipName,	/* may be NULL */
												 LPCTSTR			lpszRecipAddr,
												 LPTSTR			lpszAddrPair,
												 const UINT32	ulMaxLen);
/*
 *		Callback used to enumerate members of an address pair list - e.g. "To:",
 * "Cc:". If non-EOK return code, then enumeration is aborted and returned
 * code is propagated. to caller.
 */
typedef EXC_TYPE (*RFC822_AP_ECFN)(LPCTSTR		lpszHdrValue,
											  LPCTSTR		lpszName,
											  const UINT32	ulNameLen,
											  LPCTSTR		lpszAddr,
											  const UINT32	ulAddrLen,
											  BOOLEAN		*pfContEnum,
											  LPVOID			pArg);

/* Enumerates members of an address pair list - e.g. "To:", "Cc:" */
extern EXC_TYPE EnumRFC822AddrPairList (LPCTSTR				lpszHdrValue,
													 RFC822_AP_ECFN	lpfnEcfn,
													 LPVOID				pArg);

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// class for handling headers which may span several lines
class CRFC822HdrParser {
	private:
		TCHAR		m_szHdrName[MAX_RFC822_HDR_NAME_LEN+2];
		LPCTSTR	m_lpszHdrValue;
		BOOLEAN	m_fIsContHdr;

	public:
		void Reset ()
		{
			m_szHdrName[0] = _T('\0');
			m_lpszHdrValue = _T("");
			m_fIsContHdr = FALSE;
		}

		CRFC822HdrParser () { Reset(); }
		virtual ~CRFC822HdrParser () { }

		LPCTSTR GetHdrName () const
		{ 
			return m_szHdrName;
		}

		LPCTSTR GetHdrValue () const
		{
			return ((NULL == m_lpszHdrValue) ? _T("") : m_lpszHdrValue);
		}

		BOOLEAN IsContHdr () const { return m_fIsContHdr; }

		// Note: some internal pointers may point to the original supplied
		//			buffer. Therefore, it MUST NOT change while this object is
		//			"in use" (i.e. until all calls to any of its methods have
		//			been completed.
		EXC_TYPE ProcessHdr (LPCTSTR lpszHdr);
};	// end of RFC822 header parser

typedef CRFC822HdrParser *LPCRFC822HDRPARSER;
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

typedef struct {
	LPCTSTR			m_lpszName;
	UINT32			m_ulSize;		/* (-1) means unknown */
	UINT32			m_ulDuration;	/* (-1) means unknown */
	LPCTSTR			m_lpszType;
	LPCTSTR			m_lpszSubType;
	LPCTSTR			m_lpszCharSet;
	LPCTSTR			m_lpszDesc;
	LPCTSTR			m_lpszID;
#ifdef __cplusplus
	const CStr2StrMapper	*m_pDispProps;
	const CStr2StrMapper	*m_pCTypeProps;
#else
	LPVOID					m_pDispProps;
	LPVOID					m_pCTypeProps;
#endif	/* of __cplusplus */
	RFC822ENCCASE	m_attEnc;
	BOOLEAN			m_fIsVirtualName;
} RFC822ATTACHINFO, *LPRFC822ATTACHINFO;

/*---------------------------------------------------------------------------*/

typedef EXC_TYPE (*RFC822PROPSLISTENUMCFN)(LPCTSTR			lpszPropName,
														 const UINT32	ulNameLen,
														 LPCTSTR			lpszPropVal,	/* may be NULL/empty */
														 const UINT32	ulValLen,	/* valid data in value string (may be 0) */
														 LPVOID			pArg,
														 BOOLEAN			*pfContEnum);

extern EXC_TYPE EnumHeaderDataPropsList (LPCTSTR						lpszPropsList,	/* may be empty/NULL */
													  const TCHAR					chListSep,
													  RFC822PROPSLISTENUMCFN	lpfnEcfn,
													  LPVOID							pArg);

#ifdef __cplusplus
inline EXC_TYPE EnumRFC822PropsList (LPCTSTR						lpszPropsList,	// may be empty/NULL
												 RFC822PROPSLISTENUMCFN	lpfnEcfn,
												 LPVOID						pArg)
{
	return EnumHeaderDataPropsList(lpszPropsList, RFC822_LIST_DELIM, lpfnEcfn, pArg);
}
#else
#	define EnumRFC822PropsList(pl,ef,a) EnumHeaderDataPropsList((pl), RFC822_LIST_DELIM, (ef), (a))
#endif

extern EXC_TYPE CountHeaderDataPropsList (LPCTSTR		lpszPropsList, /* may be empty/NULL */
													   const TCHAR	chListSep,
														UINT32		*pulCount);

#ifdef __cplusplus
inline EXC_TYPE CountRFC822PropsList (LPCTSTR lpszPropsList /* may be empty/NULL */, UINT32& ulCount)
{
	return CountHeaderDataPropsList(lpszPropsList, RFC822_LIST_DELIM, &ulCount);
}
#else
#	define CountRFC822PropsList(l,pc) CountHeaderDataPropsList((l),RFC822_LIST_DELIM,(pc))
#endif

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
extern EXC_TYPE ParseHeaderDataPropsList (LPCTSTR				lpszPropsList,	// may be empty/NULL
														const TCHAR			chListSep,
														CStr2StrMapper&	propSet);

														// ';' separated list of disposition properties
// Note: does not clear the properties set prior to parsing
inline EXC_TYPE ParseRFC822PropsList (LPCTSTR lpszPropsList, CStr2StrMapper& propSet)
{
	return ParseHeaderDataPropsList(lpszPropsList, RFC822_LIST_DELIM, propSet);
}

extern EXC_TYPE ParseRFC822ContentTypeInfo (LPCTSTR			lpszCTypeInfo,
														  LPCTSTR&			lpszType,
														  UINT32&			ulTypeLen,
														  LPCTSTR&			lpszSubType,
														  UINT32&			ulSubTypeLen,
														  CStr2StrMapper&	propSet);

extern EXC_TYPE AddHdrDataPropListPair (IStrlBuilder&	istr,
													 const UINT32	ulPropIndex,
													 const TCHAR	chListSep,
													 LPCTSTR			lpszPropName,
													 LPCTSTR			lpszPropVal); /* may be NULL/empty */

inline EXC_TYPE AddRFC822PropListPair (IStrlBuilder&	istr,
													const UINT32	ulPropIndex,
													LPCTSTR			lpszPropName,
													LPCTSTR			lpszPropVal) /* may be NULL/empty */
{
	return AddHdrDataPropListPair(istr, ulPropIndex, RFC822_LIST_DELIM, lpszPropName, lpszPropVal);
}

extern EXC_TYPE BuildHdrDataPropsList (const CStr2StrMapper& propSet, const TCHAR chListSep, IStrlBuilder& plst);

// ';' separated list of disposition properties
inline EXC_TYPE BuildRFC822PropsList (const CStr2StrMapper& propSet, IStrlBuilder& plst)
{
	return BuildHdrDataPropsList(propSet, RFC822_LIST_DELIM, plst);
}

// builds a "flat" representation of the "Content-Type:" header - i.e. "type/sub-type" followed by properties (if any)
extern EXC_TYPE BuildRFC822ContentTypeInfo (LPCTSTR					lpszType,
														  LPCTSTR					lpszSubType,
														  const CStr2StrMapper&	propSet,
														  IStrlBuilder&			isctype);

extern EXC_TYPE BuildHdrDataPropsList (const CStr2StrMapper&	propSet,
													const TCHAR					chListSep,
													LPTSTR						lpszPropsList,
													const UINT32				ulMaxLen);

// ';' separated list of disposition properties
inline EXC_TYPE BuildRFC822PropsList (const CStr2StrMapper& propSet,
												  LPTSTR						lpszPropsList,
												  const UINT32				ulMaxLen)
{
	return BuildHdrDataPropsList(propSet, RFC822_LIST_DELIM, lpszPropsList, ulMaxLen);
}

extern EXC_TYPE ReadjustRFC822PropsList (const CStr2PtrMapper&	propSet, IStrlBuilder& plst);

extern EXC_TYPE ReadjustRFC822PropsList (const CStr2PtrMapper&	propSet,
													  LPTSTR						lpszPropsList,
													  const UINT32				ulMaxLen);
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CAttachInfo {
	private:
		CStr2StrMapper	m_CDispProps;
		CStr2StrMapper	m_CTypeProps;
		UINT32			m_ulSize;
		UINT32			m_ulDuration;
		LPTSTR			m_lpszPath;
		LPTSTR			m_lpszType;
		LPTSTR			m_lpszSubType;
		LPTSTR			m_lpszSuperType;
		LPTSTR			m_lpszSuperSubType;
		LPTSTR			m_lpszDesc;
		LPTSTR			m_lpszID;
		RFC822ENCCASE	m_attEnc;
		BOOLEAN			m_fIsVirtualName;

		static EXC_TYPE SetNumProp (LPCTSTR lpszVal, UINT32& ulVal);

		static EXC_TYPE UpdateStr (LPTSTR& lpszDst, LPCTSTR lpszSrc);

		static EXC_TYPE ReInitStr (LPTSTR& lpszDst, LPCTSTR lpszSrc)
		{
			if (IsEmptyStr(lpszDst))
				return UpdateStr(lpszDst, lpszSrc);
			else
				return EOK;
		}

		static EXC_TYPE AddPropPair (CStr2StrMapper& aMap, LPCTSTR lpszPropPair);

		static LPCTSTR GetAttProp (const CStr2StrMapper& aProp, LPCTSTR lpszPropName);

		// adds the new property only if previous value empty or does not exist
		static EXC_TYPE UpdatePropVal (CStr2StrMapper& aProp, LPCTSTR lpszPropName, LPCTSTR lpszPropVal);

		EXC_TYPE InitContentType (LPCTSTR lpszMIMEType, const BOOLEAN fUpdate);

	public:
		// average number of properties for an attachment
		enum { AVG_ATTPROPS_NUM=8 };

		CAttachInfo (const UINT32 ulAvgAttProps=AVG_ATTPROPS_NUM);

		EXC_TYPE SetInfo (LPCTSTR					lpszName,
								const UINT32			ulSize,	// may be zero
								LPCTSTR					lpszPath, // may be NULL/empty
								LPCTSTR					lpszType,
								LPCTSTR					lpszSubType,
								LPCTSTR					lpszCharSet=NULL, // may be NULL/empty
								const RFC822ENCCASE	attEnc=RFC822_NONE_ENC,
								LPCTSTR					lpszDesc=NULL,
								LPCTSTR					lpszID=NULL);

		EXC_TYPE SetInfo (LPCTSTR			lpszName,
								const UINT32	ulSize,			// may be zero
								LPCTSTR			lpszPath,		// may be NULL/empty
								LPCTSTR			lpszType,
								LPCTSTR			lpszSubType,
								LPCTSTR			lpszCharSet,	// may be NULL/empty
								LPCTSTR			lpszEnc=NULL,	// NULL == no encoding	
								LPCTSTR			lpszDesc=NULL,	// may be NULL/empty
								LPCTSTR			lpszID=NULL)	// may be NULL/empty
		{
			return SetInfo(lpszName, ulSize, lpszPath,
								lpszType, lpszSubType, lpszCharSet,
								((!IsEmptyStr(lpszEnc)) ? RFC822EncodingStr2Case(lpszEnc) : RFC822_NONE_ENC),
								lpszDesc, lpszID);
		}

		EXC_TYPE UpdateInfo (const CAttachInfo& ai);

		EXC_TYPE SetName (LPCTSTR lpszName, const BOOLEAN fIsVirtual);

		EXC_TYPE UpdateName (LPCTSTR lpszName, const BOOLEAN fIsVirtual);

		void SetSize (const UINT32 ulSize)
		{
			m_ulSize = ulSize;
		}

		EXC_TYPE SetSize (LPCTSTR lpszSize)
		{
			return SetNumProp(lpszSize, m_ulSize);
		}

		void SetDuration (const UINT32 ulDuration)
		{
			m_ulDuration = ulDuration;
		}

		EXC_TYPE SetDuration (LPCTSTR lpszDuration)
		{
			return SetNumProp(lpszDuration, m_ulDuration);
		}

		EXC_TYPE SetPath (LPCTSTR lpszPath)	// may be NULL/empty
		{
			return UpdateStr(m_lpszPath, lpszPath);
		}

		EXC_TYPE UpdatePath (LPCTSTR lpszPath)	// may be NULL/empty
		{
			return ReInitStr(m_lpszPath, lpszPath);
		}

		EXC_TYPE SetDescription (LPCTSTR lpszDesc)	// may be NULL/empty
		{
			return UpdateStr(m_lpszDesc, lpszDesc);
		}

		EXC_TYPE UpdateDescription (LPCTSTR lpszDesc)	// may be NULL/empty
		{
			return ReInitStr(m_lpszDesc, lpszDesc);
		}

		EXC_TYPE SetCharSet (LPCTSTR lpszCharset);

		EXC_TYPE UpdateCharSet (LPCTSTR lpszCharset)	// may be NULL/empty
		{
			if (IsEmptyStr(lpszCharset))
				return EOK;
			else
				return UpdateCTypeProp(pszMIMECharsetKeyword, lpszCharset);
		}

		EXC_TYPE SetID (LPCTSTR lpszID)
		{
			return UpdateStr(m_lpszID, lpszID);
		}

		EXC_TYPE UpdateID (LPCTSTR lpszID)
		{
			return ReInitStr(m_lpszID, lpszID);
		}

		EXC_TYPE SetContentType (LPCTSTR lpszType, LPCTSTR lpszSubType);
		EXC_TYPE UpdateContentType (LPCTSTR lpszType, LPCTSTR lpszSubType);

		EXC_TYPE SetContentType (LPCTSTR lpszMIMEType)
		{
			return InitContentType(lpszMIMEType, FALSE);
		}

		EXC_TYPE UpdateContentType (LPCTSTR lpszMIMEType)
		{
			return InitContentType(lpszMIMEType, TRUE);
		}

		EXC_TYPE SetEncoding (const RFC822ENCCASE eEncCase);
		EXC_TYPE SetEncoding (LPCTSTR lpszEnc)
		{
			return SetEncoding((!IsEmptyStr(lpszEnc)) ? RFC822EncodingStr2Case(lpszEnc) : RFC822_NONE_ENC);
		}

		void SetVirtualNameState (const BOOLEAN fIsVirtual)
		{
			m_fIsVirtualName = fIsVirtual;
		}

		// copy constructor
		CAttachInfo (const CAttachInfo& ai);

		// assignment operator
		CAttachInfo& operator= (const CAttachInfo& ai)
		{
			UpdateInfo(ai);
			return *this;
		}

		LPCTSTR GetName () const;
		BOOLEAN IsVirtualName () const { return m_fIsVirtualName; }

		// may be (-1)
		UINT32 GetSize () const { return m_ulSize; }
		UINT32 GetDuration () const { return m_ulDuration; }

		// Note: may be NULL/empty
		LPCTSTR GetPath () const {	return m_lpszPath; }

		LPCTSTR GetMIMEType () const { return m_lpszType; }
		LPCTSTR GetMIMESubType () const { return m_lpszSubType; }

		EXC_TYPE UpdateSuperMIMEType (LPCTSTR lpszType)
		{
			return UpdateStr(m_lpszSuperType, lpszType);
		}

		EXC_TYPE UpdateSuperMIMESubType (LPCTSTR lpszSubType)
		{
			return UpdateStr(m_lpszSuperSubType, lpszSubType);
		}

		// saves previous values when "SetContentType" is used
		LPCTSTR GetMIMESuperType () const { return m_lpszSuperType; }
		LPCTSTR GetMIMESuperSubType () const { return m_lpszSuperSubType; }

		// Note: may be NULL/empty
		LPCTSTR GetCharSet () const
		{
			return GetAttProp(m_CTypeProps, pszMIMECharsetKeyword);
		}

		RFC822ENCCASE GetMIMEEncoding () const { return m_attEnc; }

		// Note: may be NULL/empty
		LPCTSTR GetDescription () const { return m_lpszDesc; }
		LPCTSTR GetID () const { return m_lpszID; }

		// Content-Disposition properties
		const CStr2StrMapper& GetDispositionProps () const { return m_CDispProps; }
		// Content-Type properties
		const CStr2StrMapper& GetContentTypeProps () const { return m_CTypeProps; }

		// Note: pointers stop being valid if object destroyed
		EXC_TYPE GetInfo (RFC822ATTACHINFO& ai) const;

		EXC_TYPE AddDispProps (const CStr2StrMapper& ap)
		{
			return m_CDispProps.Merge(ap);
		}

		EXC_TYPE AddDispProp (LPCTSTR lpszProp)
		{
			return m_CDispProps.AddKey(lpszProp);
		}

		EXC_TYPE AddDispProp (LPCTSTR lpszProp, LPCTSTR lpszPropVal)
		{
			return m_CDispProps.AddKey(lpszProp, lpszPropVal);
		}

		EXC_TYPE AddDispProp (LPCTSTR lpszProp, LPCTSTR lpszPropVal, const UINT32 ulVLen)
		{
			return m_CDispProps.AddKey(lpszProp, lpszPropVal, ulVLen);
		}

		// adds a "attr=value" or "attr" property
		EXC_TYPE AddDispPropPair (LPCTSTR lpszProp)
		{
			return AddPropPair(m_CDispProps, lpszProp);
		}

		// adds the new property only if previous value empty or does not exist
		EXC_TYPE UpdateDispProp (LPCTSTR lpszProp, LPCTSTR lpszPropVal)
		{
			return UpdatePropVal(m_CDispProps, lpszProp, lpszPropVal);
		}

		EXC_TYPE FindDispProp (LPCTSTR lpszProp, LPCTSTR& lpszPropVal) const
		{
			return m_CDispProps.FindKey(lpszProp, lpszPropVal);
		}

		// ';' separated list of disposition properties
		EXC_TYPE AddDispositionProps (LPCTSTR lpszDispProps) // may be NULL/empty
		{
			return ::ParseRFC822PropsList(lpszDispProps, m_CDispProps);
		}

		EXC_TYPE AddCTypeProps (const CStr2StrMapper& ap)
		{
			return m_CTypeProps.Merge(ap);
		}

		EXC_TYPE AddCTypeProp (LPCTSTR lpszProp)
		{
			return m_CTypeProps.AddKey(lpszProp);
		}

		EXC_TYPE AddCTypeProp (LPCTSTR lpszProp, LPCTSTR lpszPropVal)
		{
			return m_CTypeProps.AddKey(lpszProp, lpszPropVal);
		}

		EXC_TYPE AddCTypeProp (LPCTSTR lpszProp, LPCTSTR lpszPropVal, const UINT32 ulVLen)
		{
			return m_CTypeProps.AddKey(lpszProp, lpszPropVal, ulVLen);
		}

		// adds a "attr=value" or "attr" property
		EXC_TYPE AddCTypePropPair (LPCTSTR lpszProp)
		{
			return AddPropPair(m_CTypeProps, lpszProp);
		}

		// adds the new property only if previous value empty or does not exist
		EXC_TYPE UpdateCTypeProp (LPCTSTR lpszProp, LPCTSTR lpszPropVal)
		{
			return UpdatePropVal(m_CTypeProps, lpszProp, lpszPropVal);
		}

		EXC_TYPE FindCTypeProp (LPCTSTR lpszProp, LPCTSTR& lpszPropVal) const
		{
			return m_CTypeProps.FindKey(lpszProp, lpszPropVal);
		}

		// ';' separated list of disposition properties
		EXC_TYPE AddContentTypeProps (LPCTSTR lpszCTypeProps) // may be NULL/empty
		{
			return ::ParseRFC822PropsList(lpszCTypeProps, m_CTypeProps);
		}

		// looks first in Content-Type and then in Content-Disposition
		EXC_TYPE FindAttachProperty (LPCTSTR lpszPropName, LPCTSTR& lpszPropVal)
		{
			EXC_TYPE	exc=FindCTypeProp(lpszPropName, lpszPropVal);
			if (exc != EOK)
				exc = FindDispProp(lpszPropName, lpszPropVal);
			return exc;
		}

		void Cleanup ();

		virtual ~CAttachInfo () { Cleanup(); }
};

typedef CAttachInfo *LPATTINFO;
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

#define DEFAULT_ATT_SET_GROW	4UL

#ifdef __cplusplus
typedef EXC_TYPE (*ATS_ENUM_CFN)(const CAttachInfo&	attInfo,
										   const UINT32			ulAttIdx,
											const UINT32			ulTotNum,
											LPVOID					pArg,
											BOOLEAN&					fContEnum);

// class for holding a set of attachments
class CAttachSet {
	private:
		LPATTINFO	*m_pAtts;
		UINT32		m_ulMaxNum;		// maximum available room
		UINT32		m_ulCurNum;		// currently populated
		UINT32		m_ulGrowNum;	// grow if necessary

		// disable copy constructor
		CAttachSet (const CAttachSet& as);

		static EXC_TYPE ascCfn (const CAttachInfo&	attInfo,
										const UINT32			ulAttIdx,
										const UINT32			ulTotNum,
										LPVOID					pArg,
										BOOLEAN&					fContEnum);

	public:
		// also default constructor
		//
		// Note: growth factor may be zero - in which case, any attempt
		//			to add more than the initial size will be denied
		CAttachSet (const UINT32 ulInitialNum=0UL,
						const UINT32 ulGrowNum=DEFAULT_ATT_SET_GROW);

		EXC_TYPE AddSet (const CAttachSet& as)
		{
			return as.EnumAttachments(ascCfn, (LPVOID) this);
		}

		// Note: added set is not checked for duplicates !!!
		CAttachSet& operator+= (const CAttachSet& as)
		{
			AddSet(as);
			return *this;
		}

		EXC_TYPE UpdateSet (const CAttachSet& as)
		{
			Reset();
			return AddSet(as);
		}

		// assigment operator
		CAttachSet& operator= (const CAttachSet& as)
		{
			UpdateSet(as);
			return *this;
		}

		// Note(s):
		//
		//		a. info is duplicated internally
		//		b. set is not checked for duplicated entries
		EXC_TYPE AddAttInfo (const CAttachInfo& ai);
		CAttachSet& operator+= (const CAttachInfo& ai)
		{
			AddAttInfo(ai);
			return *this;
		}

		EXC_TYPE AddAttInfo (LPCTSTR					lpszName,
									const UINT32			ulSize,	// may be zero
									LPCTSTR					lpszPath, // may be NULL/empty
									LPCTSTR					lpszType,
									LPCTSTR					lpszSubType,
									LPCTSTR					lpszCharSet, // may be NULL/empty
									const RFC822ENCCASE	attEnc);

		EXC_TYPE AddAttInfo (LPCTSTR			lpszName,
									const UINT32	ulSize,	// may be zero
									LPCTSTR			lpszPath, // may be NULL/empty
									LPCTSTR			lpszType,
									LPCTSTR			lpszSubType,
									LPCTSTR			lpszCharSet, // may be NULL/empty
									LPCTSTR			lpszEnc) // may be NULL/empty
		{
			return AddAttInfo(lpszName, ulSize, lpszPath,
									lpszType, lpszSubType, lpszCharSet,
									((!IsEmptyStr(lpszEnc)) ? RFC822EncodingStr2Case(lpszEnc) : RFC822_NONE_ENC));
		}

		EXC_TYPE GetAttInfo (const UINT32 ulIdx, LPATTINFO& pInfo) const;

		// returns NULL if illegal index
		LPATTINFO operator[] (const UINT32 ulIdx) const;

		UINT32 GetSize () const { return m_ulCurNum; }

		EXC_TYPE EnumAttachments (ATS_ENUM_CFN lpfnEcfn, LPVOID pArg) const;

		void Reset () { m_ulCurNum = 0; }

		void Cleanup ();

		virtual ~CAttachSet () { Cleanup(); }
};

typedef CAttachSet *LPCATTSET;
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

#endif /* of ifdef _RFC822_H_ */
