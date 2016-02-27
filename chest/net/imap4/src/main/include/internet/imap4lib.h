#ifndef _IMAP4_LIB_H_
#define _IMAP4_LIB_H_

#include <_types.h>
#include <time.h>

#include <comm/socket.h>

#include <util/string.h>
#include <util/memory.h>

#include <internet/rfc822msg.h>

/*---------------------------------------------------------------------------*/
/*		This header file contains IMAP4 related definitions and an interface to
 * a generic IMAP4 server (through callback functions).
 *
 *	See RFC-2060 for complete IMAP4 (rev1) specifications.
 *
 */
/*---------------------------------------------------------------------------*/

/* default IMAP4 port number */
#ifndef IPPORT_IMAP4
#define IPPORT_IMAP4	143
#endif

/*---------------------------------------------------------------------------*/

/* server responses */
extern const char IMAP4_OK[];
extern const char IMAP4_NO[];
extern const char IMAP4_BAD[];

/* Returns an equivalent numerical value for the response as follows:
 *
 *		EOK			<-	OK
 *		EPERMISSION	<-	NO
 *		ECONTEXT		<- BAD
 *
 *	Any other code signals some other error
 */
extern EXC_TYPE imap4XlateRspCode (const char lpszRspCode[]);

/*---------------------------------------------------------------------------*/

/* well known atoms/modifiers */
extern const char IMAP4_NIL[];
extern const char IMAP4_REV1[];
extern const char IMAP4_BYE[];

extern const char IMAP4_EXISTS[];
extern const char IMAP4_UNSEEN[];
extern const char IMAP4_RECENT[];
extern const char IMAP4_PERMANENTFLAGS[];
extern const char IMAP4_UIDVALIDITY[];

extern const char IMAP4_MESSAGES[];
extern const char IMAP4_SILENT[];
extern const char IMAP4_INBOX[];
extern const char IMAP4_UIDNEXT[];

/* FETCH command modifiers */
extern const char IMAP4_ALL[];	/* equivalent to (FLAGS INTERNALDATE RFC822.SIZE ENVELOPE) */
extern const char IMAP4_BODY[];
extern const char IMAP4_BODYPEEK[];
extern const char IMAP4_BODYSTRUCT[];
extern const char IMAP4_ENVELOPE[];
extern const char IMAP4_FAST[];	/* equivalent to (FLAGS INTERNALDATE RFC822.SIZE) */
extern const char IMAP4_FLAGS[];
extern const char IMAP4_FULL[];	/* equivalent to (FLAGS INTERNALDATE RFC822.SIZE ENVELOPE BODY) */
extern const char IMAP4_INTERNALDATE[];
extern const char IMAP4_RFC822[];		/* euqivalent to BODY[] */
extern const char IMAP4_RFC822HDR[];	/* equivalent to BODY.PEEK[HEADER] */
extern const char IMAP4_RFC822SIZE[];
extern const char IMAP4_RFC822TEXT[];	/* equivalent to BODY[TEXT] */
extern const char IMAP4_UID[];

/*---------------------------------------------------------------------------*/

/* server response tags */
#define IMAP4_CONTINUE_RSP	_T('+')
#define IMAP4_UNTAGGED_RSP	_T('*')

/* octet count delimiter(s) */
#define IMAP4_OCTCNT_SDELIM	_T('{')
#define IMAP4_OCTCNT_EDELIM	_T('}')

/* LITERAL+ character */
#define IMAP4_LITPLUS_CHAR	_T('+')
#define IMAP4_LISTWILDCARD	_T('%')

/* parenthesized list delimiter(s) */
#define IMAP4_PARLIST_SDELIM	_T('(')
#define IMAP4_PARLIST_EDELIM	_T(')')

/* bracketed response delimiter(s) */
#define IMAP4_BRCKT_SDELIM		_T('[')
#define IMAP4_BRCKT_EDELIM		_T(']')

/* quoted string(s) delimiter */
#define IMAP4_QUOTE_DELIM		_T('\"')
#define IMAP4_AMPERSAND_DELIM	_T('&')

#define IMAP4_MSGRANGE_DELIM		_T(':')
#define IMAP4_MSGLIST_DELIM		_T(',')
#define IMAP4_MSGRANGE_WILDCARD	_T('*')

#define IMAP4_NAMESPACE_DELIM	_T('#')

#define IMAP4_OFFSET_SDELIM	_T('<')
#define IMAP4_OFFSET_EDELIM	_T('>')

#define IMAP4_BODYPART_DELIM	_T('.')

#define IMAP4_RCVDATE_DELIM	_T('-')
#define IMAP4_RCVTIME_DELIM	_T(':')

/*---------------------------------------------------------------------------*/

/*
 *		Known templates for welcomes of different servers.
 *
 *	Note: each welcome MUST start with "* OK "
 */

extern const TCHAR szIMAP4SWCOMKx4p3WelcomePattern[];
extern const TCHAR szIMAP4MSExchangeWelcomePattern[];
extern const TCHAR szIMAP4MSExch2000WelcomePattern[];
extern const TCHAR szIMAP4MSExch2003WelcomePattern[];
extern const TCHAR szIMAP4MirapointWelcomePattern[];
extern const TCHAR szIMAP4MiraptProxyWelcomePattern[];
extern const TCHAR szIMAP4LotusDominoWelcomePattern[];
extern const TCHAR szIMAP4CommtouchWelcomePattern[];
extern const TCHAR szIMAP4NSMailWelcomePattern[];
extern const TCHAR szIMAP4iPlanetWelcomePattern[];
extern const TCHAR szIMAP4iPlanetHotFixWelcomePattern[];
extern const TCHAR szIMAP4CommuniGateProWelcomePattern[];
extern const TCHAR szIMAP4CriticalPathWelcomePattern[];
extern const TCHAR szIMAP4CriticalPathProxyWelcomePattern[];

extern LPCTSTR IMAP4KnownWelcomePatterns[];

/* some known server's names/types */
extern const TCHAR szIMAP4InterMailSrvrName[];
extern const TCHAR szIMAP4MSExchangeSrvrName[];
extern const TCHAR szIMAP4MirapointSrvrName[];
extern const TCHAR szIMAP4LotusDominoSrvrName[];
extern const TCHAR szIMAP4CommtouchSrvrName[];
extern const TCHAR szIMAP4NSMailSrvrName[];
extern const TCHAR szIMAP4iPlanetSrvrName[];
extern const TCHAR szIMAP4CommuniGateProSrvrName[];
extern const TCHAR szIMAP4CriticalPathSrvrName[];

/* NULL terminated list of known server type strings */
extern LPCTSTR IMAP4ServerTypes[];

/* returns EIOUNCLASS if no match found */
extern EXC_TYPE imap4AnalyzeWelcomePattern (LPCTSTR		lpszWelcome,
														  LPCTSTR		lpszWPattern,
														  LPTSTR			lpszType,
														  const UINT32	ulMaxTypeLen,
														  LPTSTR			lpszVersion,
														  const UINT32	ulMaxVerLen);

#ifdef __cplusplus
inline EXC_TYPE imap4AnalyzeWelcome (LPCTSTR			lpszWelcome,
												 LPCTSTR			lpszPatterns[],
												 LPTSTR			lpszType,
												 const UINT32	ulMaxTypeLen,
												 LPTSTR			lpszVersion,
												 const UINT32	ulMaxVerLen)
{
	return inetAnalyzeWelcome(lpszWelcome, lpszPatterns,
									  imap4AnalyzeWelcomePattern,
									  lpszType, ulMaxTypeLen,
									  lpszVersion, ulMaxVerLen);
}
#else
#define imap4AnalyzeWelcome(w,p,t,tl,v,vl)	\
	inetAnalyzeWelcome(w,p,imap4AnalyzeWelcomePattern,t,tl,v,vl)
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

/* Inactivity timeout (as specified in the RFC) */
#define IMAP4_DEF_TIMEOUT	30	/* minutes (!) */

#ifdef __cplusplus
/*
 *		Reads a client command - if {} delimited literals are encountered then
 * they are handled by issuing the '+' continuation prompt. As a result, the
 * returned line contains no such literals.
 */
extern int imap4ReadCmd (ISockioInterface&	CBSock,
								 char						szBuf[],
								 const size_t			sMaxLen,
								 const LONG				maxSecs=0,
								 BOOLEAN					*pfStrippedCRLF=NULL);

/*
 *		Reads a server response - if {} delimited literals are encountered then
 * they are handled by reading the next line and appending the proper octet
 * count. As a result, the entire response is contained without any literals.
 *
 *	Returns EDATACHAIN if this is an untagged response
 *
 * CAVEAT: this routine is intended to read the initial (untagged) response only
 *			or the final (tagged) response. It will not handle correctly responses
 *			which may contain msg parts (e.g. FETCH where RFC822 headers and/or body
 *			are expected)
 */
extern EXC_TYPE imap4ReadRsp (ISockioInterface&	SBSock,
										char					szBuf[],
										const size_t		sMaxLen,
										size_t&				rLen,
										const LONG			maxSecs=0);
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

#define MAX_IMAP4_TAG_LEN			36
#define MAX_IMAP4_OPCODE_LEN		24
#define MAX_IMAP4_CMD_LEN		  516
#define MAX_IMAP4_DATA_LEN		 1028

/*---------------------------------------------------------------------------*/

/* maximum length of a message part indicator */
#define MAX_IMAP4_MSGPART_LEN		64

#ifdef __cplusplus
class CIMAP4MsgPartHandler : public IStrlBuilder {
	protected:
		TCHAR					m_szStaticPart[MAX_IMAP4_MSGPART_LEN+2];
		UINT32				m_ulCurLen;	// used to track which builder to use
		CStrlBuilder		m_staticPart;
		CIncStrlBuilder	m_dynamicPart;

		virtual BOOLEAN UseStaticPart () const
		{
			return (m_ulCurLen < MAX_IMAP4_MSGPART_LEN);
		}

	public:
		// also default constructor
		CIMAP4MsgPartHandler (LPCTSTR lpszMsgPart=NULL);

		// any previous part ID is overridden
		virtual EXC_TYPE SetPartID (LPCTSTR lpszMsgPart);

		CIMAP4MsgPartHandler (const CIMAP4MsgPartHandler& mph);

		virtual LPCTSTR GetPartID () const;

		virtual LPCTSTR GetBuffer () const
		{
			return GetPartID();
		}

		virtual CIMAP4MsgPartHandler& operator= (const CIMAP4MsgPartHandler& mph)
		{
			EXC_TYPE	exc=SetPartID(mph.GetPartID());
			return *this;
		}

		virtual operator LPCTSTR () const
		{
			return GetPartID();
		}

		virtual UINT32 GetCurLen () const
		{
			return m_ulCurLen;
		}

		virtual LPCTSTR GetCurPos () const;

		virtual void Reset ();

		virtual EXC_TYPE AddChars (LPCTSTR lpszChars, const UINT32 ulCLen);

		// not allowed to add EOS to part ID
		virtual EXC_TYPE AddEOS ()
		{
			return ESUPPORT;
		}

		virtual ~CIMAP4MsgPartHandler ()
		{
		}
};
#endif	/* __cplusplus */

/*---------------------------------------------------------------------------*/

/* maximum bracketed response length */
#define MAX_IMAP4_BRCKT_LEN	MAX_IMAP4_MSGPART_LEN

/* maximum length of FETCH response modifier */
#define MAX_FETCHRSP_MODLEN	\
	(MAX_IMAP4_OPCODE_LEN+MAX_IMAP4_MSGPART_LEN+2+MAX_DWORD_DISPLAY_LENGTH+2)

/*---------------------------------------------------------------------------*/

extern const char szIMAP4LoginCmd[];
extern const char szIMAP4LogoutCmd[];
extern const char szIMAP4FetchCmd[];
extern const char szIMAP4CapabilityCmd[];
extern const char szIMAP4NoopCmd[];
extern const char szIMAP4SelectCmd[];
extern const char szIMAP4ListCmd[];
extern const char szIMAP4LSUBCmd[];
extern const char szIMAP4AuthCmd[];
extern const char szIMAP4ExamineCmd[];
extern const char szIMAP4CreateCmd[];
extern const char szIMAP4DeleteCmd[];
extern const char szIMAP4RenameCmd[];
extern const char szIMAP4SubsCmd[];
extern const char szIMAP4UnSubsCmd[];
extern const char szIMAP4StatusCmd[];
extern const char szIMAP4AppendCmd[];
extern const char szIMAP4CheckCmd[];
extern const char szIMAP4CloseCmd[];
extern const char szIMAP4XpngCmd[];
extern const char szIMAP4SearchCmd[];
extern const char szIMAP4StoreCmd[];
extern const char szIMAP4CopyCmd[];
extern const char szIMAP4UIDCmd[];

/*---------------------------------------------------------------------------*/

/* NULL terminated list of IMAP4 commands */
extern const char *szIMAP4Cmds[];

extern BOOLEAN IsIMAP4Cmd (const char szCmd[]);

/*---------------------------------------------------------------------------*/

extern const char szIMAP4ReferralBracketKwd[];
extern const char szIMAP4ReadOnlyBracketKwd[];
extern const char szIMAP4ReadWriteBracketKwd[];

/* NULL terminate list of keywords that appear in bracketed tagged responses */
extern const char *szIMAP4BracketKwds[];

/*---------------------------------------------------------------------------*/

/* Note: places EOS where adequate */
extern EXC_TYPE imap4AdjustArg (LPTSTR		lpszLine,
										  LPCTSTR	*lppszArg,
										  UINT32		*pulArgLen,
										  LPCTSTR	*lppszNext);

extern EXC_TYPE imap4GetArg (const char	lpszLine[],
									  char			lpszArg[],
									  const UINT32	ulMaxArgLen,
									  const char	**lppszNext);

/*---------------------------------------------------------------------------*/

#define imap4ExtractTag(lpszLine,lpszArg,ulMaxArgLen,lppszNext)	\
	imap4GetArg(lpszLine,lpszArg,ulMaxArgLen,lppszNext)

/*		Extracts the basic parameters of a IMAP4 client command. If successful,
 * then also returns the pointer to the first argument (if any).
 */
extern EXC_TYPE imap4ExtractCmd (const char		lpszCmd[],
											char				lpszTag[],
											const UINT32	ulMaxTagLen,
											char				lpszOp[],
											const UINT32	ulMaxOpLen,
											const char		**lppszArgs);

/* Note: changes command buffer by adding EOS(s) */
extern EXC_TYPE imap4AdjustCmd (LPTSTR		lpszCmd,
										  LPCTSTR	*lppszTag,
										  LPCTSTR	*lppszOp,
										  BOOLEAN	*pfIsUID,
										  LPCTSTR	*lppszArgs);

/* Note: adds EOS(s) where appropriate */
extern EXC_TYPE imap4AdjustRangeCmd (LPTSTR	lpszArgs,
												 LPCTSTR	*lppszMsgsList,
												 LPCTSTR	*lppszModifiers);

/*		Extracts the basic parameters of a IMAP4 server response. If successful,
 * then also returns the pointer to the first argument (if any).
 *
 * Note: if response is not one of the standard responses then returns as follows:
 *
 *		ECONTINUED - if non-final response (signaled by '+' first char)
 *		EDATACHAIN - if untagged response (signaled by '*' first char)
 *		E??? - according to "imap4XlateRspCode"
 *
 *	In the "untagged" case it returns the 2 values following the '*' sign
 */
extern EXC_TYPE imap4ExtractRsp (const char		lpszRsp[],
											char				lpszTag[],
											const UINT32	ulMaxTagLen,
											char				lpszCode[],
											const UINT32	ulMaxCodeLen,
											const char		**lppszArgs);

/*		Extracts the IMAP4 opcode for which the response was returned - including
 * any bracketed response in between (e.g. [ALERT], [READ-WRITE]).
 */
extern EXC_TYPE imap4ExtractOpRsp (const char	*lpszOpRsp,
											  char			szBrcktRsp[],
											  const UINT32	ulMaxBrcktLen,
											  char			szRspOp[],
											  const UINT32	ulMaxRspOp,
											  const char	**lppszRspArgs);

/*---------------------------------------------------------------------------*/

/*		Extracts the IMAP4 tag, opcode and bracketed response */
extern EXC_TYPE imap4ExtractTaggedRsp (const char		lpszRsp[],
													char				lpszTag[],
													const UINT32	ulMaxTagLen,
													char				lpszCode[],
													const UINT32	ulMaxCodeLen,
													char				szBrcktRsp[],
													const UINT32	ulMaxBrcktLen,
													const char		**lppszRspArgs);

extern EXC_TYPE imap4ExtractUntaggedRsp (const char	lpszRsp[],
													  char			lpszTag[],
													  const UINT32	ulMaxTagLen,
													  char			lpszCode[],
													  const UINT32	ulMaxCodeLen,
													  const char	**lppszRspArgs);

/* Note: if no referral is found, then an EOK with an empty server is returned */
extern EXC_TYPE imap4ExtractReferralRsp (const char	lpszRsp[], 
													  char			lpszSrvr[],
													  const UINT32	ulMaxSrvLen);

/*---------------------------------------------------------------------------*/

/* Extracts a literal octet count - which MUST exists in the response.
 *
 * Returns:
 *
 *		EEXIST - if no literal count found
 *		EUDFFORMAT - if literal count syntax is illegal
 *		E??? - other errors
 */
extern EXC_TYPE imap4ExtractLiteralCount (LPCTSTR	lpszRsp,
														UINT32	*pulCount,
														BOOLEAN	*pfIsLitPlus);

/*---------------------------------------------------------------------------*/

/* Converts modified UTF-7/BASE64 folder name into local representation
 *
 * Note: input string may be changed (even if non-EOK code returned)
 */
extern EXC_TYPE imap4AdjustFolderName (LPTSTR			lpszFldrName,
													const UINT32	ulMaxLen,
													UINT32			*pulFNLen);

/*		Converts modified UTF-7/BASE64 folder name into local representation. All
 * '\0' preceded values are "reduced"
 *
 * Note: input string may be changed (even if non-EOK code returned)
 */
extern EXC_TYPE imap4CanonizeFolderName (LPTSTR			lpszFldrName,
													  const UINT32	ulMaxLen,
													  UINT32			*pulFNLen);

extern EXC_TYPE imap4EncodeFolderComp (LPCTSTR			lpszComp,
													const UINT32	ulCLen,
													LPTSTR			lpszEnc,
													const UINT32	ulMaxLen);

extern EXC_TYPE imap4BuildCanonicalFolderName (LPCTSTR		lpszSrcFldr,
															  LPTSTR			lpszDstFldr,
															  const UINT32	ulMaxLen);

/* extracts a list value (including the delimiting "()") */
extern EXC_TYPE imap4GetListValue (const char	lpszValue[],
											  const char	**lppszList,
											  UINT32			*pulListLen);

/* Extracts a message modifier value from a response (which may contain it).
 *
 *	Returns:
 *		EEXIST - if modifier not found in response.
 */

extern EXC_TYPE imap4FindModifierValue (const char	lpszRsp[],
													 const char	lpszModifier[],
													 const char	**lppszVal,
													 UINT32		*pulVLen);

/* callback used to enumerate encoded modifiers in a string */
typedef EXC_TYPE (*IMAP4_MODSENUM_CFN)(const UINT32	ulModNdx,
													LPCTSTR			lpszModifier,
													LPCTSTR			lpszModArg,	/* non-NULL (==xxx) for BODY(.PEEK)[xxx] */
													LPVOID			pArg,
													BOOLEAN			*pfContEnum);

/* Note: if modifiers start with '(' then they MUST end with ')' */
extern EXC_TYPE imap4EnumModifiers (LPCTSTR					lpszMods,	/* may be empty/NULL */
												IMAP4_MODSENUM_CFN	lpfnEcfn,
												LPVOID					pArg);

/*---------------------------------------------------------------------------*/

extern EXC_TYPE imap4ExtractMsgStrMod (const char		lpszRsp[],
													const char		lpszModifier[],
													char				lpszVal[],
													const UINT32	ulMaxLen);

extern EXC_TYPE imap4ExtractMsgNumMod (const char	lpszRsp[],
													const char	lpszModifier[],
													UINT32		*pulNum);

#define imap4ExtractMsgUID(lpszRsp,pulMsgUID) \
	imap4ExtractMsgNumMod(lpszRsp,IMAP4_UID,pulMsgUID)
#define imap4ExtractMsgsNum(lpszRsp,pulMsgNum) \
	imap4ExtractMsgNumMod(lpszRsp,IMAP4_MESSAGES,pulMsgNum)
#define imap4ExtractNextUID(lpszRsp,pulNextUID) \
	imap4ExtractMsgNumMod(lpszRsp,IMAP4_UIDNEXT,pulNextUID)

/*---------------------------------------------------------------------------*/

/* returns EEXIST if no equivalence found */
extern EXC_TYPE imap4GetEquivBodyPart (const char		lpszPart[],
													const UINT32	ulPartLen,
													const char		**lppszEquivPart);

/* Searches for a BODY[n]<offset> response (or the equivalents)
 *
 * Returns:
 *		EEXIST if not found response
 *		EOK with empty part if no specific body part found
 *
 * Note: end offset == (-1) if no end offset specified
 */
extern EXC_TYPE imap4ExtractBodyPart (const char	lpszRsp[],
												  char			lpszPart[],
												  const UINT32	ulMaxPartLen,
												  BOOLEAN		*pfIsPeek,
												  UINT32			*pulStartOffset,
												  UINT32			*pulEndOffset);

/*---------------------------------------------------------------------------*/

/*
 *	Analyzes a BODYSTRUCTURE response and returns next part (delimited within
 * paranthesis list).
 *
 * Returns EEOF if no more body parts found
 *
 * Note(s):
 *
 *		a. assume(s) ENTIRE body structure response in in supplied buffer
 *
 *		b. assume(s) initial '(' skipped (but terminating ')' not !!!)
 */
extern EXC_TYPE imap4GetNextBodyStructPart (const char	**lppszCurPos,
														  const char	**lppszPart,
														  UINT32		*pulPartLen);

extern EXC_TYPE imap4AnalyzeBodyStruct (const char	**lppszCurPos,
													 const char	**lppszPart,
													 UINT32		*pulPartLen);

/*
 *		Analyzes a "BODY[section <optional-headers-list>]<origin-octet>" response.
 *
 *	Note(s):
 *
 *		a. returned section may be empty (e.g. "BODY[]")
 *		b. returned origin octet is (-1) if no partial fetch response
 *		c. section may be empty and yet have an origin octet (e.g. "BODY[]<0>" is valid)
 *		d. header list is NULL/empty if no headers list found
 */
extern EXC_TYPE imap4AnalyzeBodyRsp (LPCTSTR		lpszBodyRsp,
												 LPCTSTR		*lppszSection,
												 UINT32		*pulSectionLen,
												 LPCTSTR		*lppszHdrsList,
												 UINT32		*pulListLen,
												 UINT32		*pulOriginOctet);	/* (-1) if none */

/*---------------------------------------------------------------------------*/

#define IMAP4_MIMETYPE_STRUCT_IDX		0
#define IMAP4_MIMESUBTYPE_STRUCT_IDX	1
#define IMAP4_CONTENTTYPE_STRUCT_IDX	2
#define IMAP4_UNKNOWN_1_STRUCT_IDX		3
#define IMAP4_UNKNOWN_2_STRUCT_IDX		4
#define IMAP4_XFER_ENCODE_STRUCT_IDX	5
#define IMAP4_SIZE_STRUCT_IDX				6

/*---------------------------------------------------------------------------*/

extern EXC_TYPE imap4GetBodyStructArg (const char		**lppszCurPos,
													const UINT32	ulPartIdx,
													const char		**lppszPart,
													UINT32			*pulPartLen);

#define imap4GetXferEncodeBodyStructArg(lppszCurPos,lppszPart,pulPartLen)	\
	imap4GetBodyStructArg(lppszCurPos, IMAP4_XFER_ENCODE_STRUCT_IDX, lppszPart, pulPartLen)
#define imap4GetSizeBodyStructArg(lppszCurPos,lppszPart,pulPartLen)	\
	imap4GetBodyStructArg(lppszCurPos, IMAP4_SIZE_STRUCT_IDX, lppszPart, pulPartLen)

/* Note: it can only go FORWARD !!! */
extern EXC_TYPE imap4GetRelativeBodyStructArg (const char	**lppszCurPos,
															  const UINT32	ulCurPartIdx,
															  const UINT32	ulReqPartIdx,
															  const char	**lppszPart,
															  UINT32			*pulPartLen);

/*---------------------------------------------------------------------------*/

/* available body parts specifiers */
typedef enum {
	IMAP4_ALL_BODYPART,
	IMAP4_HEADERS_BODYPART,
	IMAP4_HEADER_FIELDS_BODYPART,
	IMAP4_NOT_HEADER_FIELDS_BODYPART,
	IMAP4_MIME_BODYPART,
	IMAP4_TEXT_BODYPART,
	IMAP4_BAD_BODYPART
} IMAP4_BODYPART_CASE;

#define IMAP4_BODYPARTS_NUM	IMAP4_BAD_BODYPART
#define fIsBadIMAP4BodyPart(p) (((unsigned) (p)) >= IMAP4_BAD_BODYPART)

/*---------------------------------------------------------------------------*/

extern const char szIMAP4BodyHeaders[];
extern const char szIMAP4BodyHeaderFields[];
extern const char szIMAP4BodyNotHeaderFields[];
extern const char szIMAP4BodyMIME[];
extern const char szIMAP4BodyText[];

/*---------------------------------------------------------------------------*/

/* Note: empty/zero-length body part is translated as ALL */
extern IMAP4_BODYPART_CASE imap4XlateBodyPart (LPCTSTR lpszBodyPart, const UINT32 ulPartLen);

#ifdef __cplusplus
inline IMAP4_BODYPART_CASE imap4XlateBodyPart (LPCTSTR lpszBodyPart)
{
	return ((NULL == lpszBodyPart) ? IMAP4_BAD_BODYPART :
			  imap4XlateBodyPart(lpszBodyPart, _tcslen(lpszBodyPart)));
}
#endif	/* of __cplusplus */

/* returns NULL if not found requested part */
extern LPCTSTR imap4GetBodyPart (const IMAP4_BODYPART_CASE bCase);

/*---------------------------------------------------------------------------*/

/* Note: returned part ID is empty if no specific ID specified (e.g. headers
 *			in general requested)
 */
extern EXC_TYPE imap4AnalyzeBodyPart (LPCTSTR					lpszBodyPart,
												  IMAP4_BODYPART_CASE	*pbCase,
												  LPTSTR						lpszPartID,
												  const UINT32				ulMaxIDLen);

/* Xlate(s) the part ID into numbers.
 *
 * Returns EXIST if no part ID supplied
 */
extern EXC_TYPE imap4AnalyzePartID (LPCTSTR			lpszPartID,
												UINT32			ulPartID[],
			/* number of components */	UINT32			*pulIDLen,
												const UINT32	ulMaxIDLen);

/*---------------------------------------------------------------------------*/

/* flags delimiter */
#define IMAP4_SYSFLAG_SIGN	_T('\\')

/* message flags */
extern const TCHAR IMAP4_SEENFLAG[];
extern const TCHAR IMAP4_ANSWEREDFLAG[];
extern const TCHAR IMAP4_FLAGGEDFLAG[];
extern const TCHAR IMAP4_DELETEDFLAG[];
extern const TCHAR IMAP4_DRAFTFLAG[];
extern const TCHAR IMAP4_RECENTFLAG[];
extern const TCHAR IMAP4_PRIVATEFLAGS[];

/* NULL terminated list of all msg flags */
extern LPCTSTR IMAP4MsgFlags[];

/*---------------------------------------------------------------------------*/

/* do not change order !! */
typedef enum {
	IMAP4_SEEN_MSGCASE,
	IMAP4_ANSWERED_MSGCASE,
	IMAP4_FLAGGED_MSGCASE,
	IMAP4_DELETED_MSGCASE,
	IMAP4_DRAFT_MSGCASE,
	IMAP4_RECENT_MSGCASE,

	/* non-standard flags */
	IMAP4_PRIVATE_FLAGS,		/* '\*' */
	IMAP4_EXTENSION_FLAGS,

	IMAP4_BADFLAG_MSGCASE
} IMAP4_MSGFLAG_CASE;

#define IMAP4_STDMSGFLAGS_NUM	IMAP4_PRIVATE_FLAGS
#define fIsStdIMAP4FlagsCase(f)	(((unsigned) (f)) < IMAP4_STDMSGFLAGS_NUM)

#define IMAP4_MSGFLAGS_NUM	IMAP4_BADFLAG_MSGCASE

#define MAX_IMAP4_FLAG_STRLEN		14	/* including space and '\\' */
#define MAX_IMAP4_FLAGS_ENCLEN	((IMAP4_MSGFLAGS_NUM * MAX_IMAP4_FLAG_STRLEN) + 2)

#define fIsBadIMAP4MsgFlagCase(f) (((unsigned) (f)) >= IMAP4_MSGFLAGS_NUM)

extern IMAP4_MSGFLAG_CASE imap4XlateMsgFlag (LPCTSTR lpszFlag);
extern IMAP4_MSGFLAG_CASE imap4XlateExtMsgFlag (LPCTSTR lpszFlag, const UINT32 ulFLen);

/*---------------------------------------------------------------------------*/

typedef struct {
	unsigned m_fSeen		: 1;
	unsigned m_fAnswered	: 1;
	unsigned m_fFlagged	: 1;
	unsigned m_fDeleted	: 1;
	unsigned m_fDraft		: 1;
	unsigned m_fRecent	: 1;
	unsigned m_fPrivate	: 1;
	unsigned m_fExtended	: 1;
} IMAP4_MSGFLAGS, *LPIMAP4MSGFLAGS;

typedef const IMAP4_MSGFLAGS *LPCIMAP4MSGFLAGS;

/*---------------------------------------------------------------------------*/

typedef EXC_TYPE (*IMAP4_FLAGS_ENUM_CFN)(const UINT32	ulFlgIndex,	/* starts at zero */
													  LPCTSTR		lpszFlag,	/* raw flag string - NOTE: not necessarily EOS terminated !!! */
													  const UINT32	ulFlgLen,	/* length of string data */
													  LPVOID			pArg,
													  BOOLEAN		*pfContEnum);

/* standard callback function for parsing flags (called by "imap4ParseMsgFlags") */
extern EXC_TYPE imap4StdMsgFlagsParseCfn (const UINT32	ulFlgIndex,	/* starts at zero */
														LPCTSTR			lpszFlag,	/* raw flag string - NOTE: not necessarily EOS terminated !!! */
														const UINT32	ulFlgLen,	/* length of string data */
														LPVOID			pArg,			/* assumes an IMAP4_MSGFLAGS pointer */
														BOOLEAN			*pfContEnum);

/* Note: automatically detects if flags list is "()" delimited */
extern EXC_TYPE imap4EnumMsgFlags (LPCTSTR lpszFlags, IMAP4_FLAGS_ENUM_CFN lpfnEcfn, LPVOID pArg);

#ifdef __cplusplus
inline EXC_TYPE imap4ParseMsgFlags (LPCTSTR lpszFlags, IMAP4_MSGFLAGS *pFlags)
{
	return imap4EnumMsgFlags(lpszFlags, imap4StdMsgFlagsParseCfn, (LPVOID) pFlags);
}
#else
#	define imap4ParseMsgFlags(lpszFlags,pFlags)	\
		imap4EnumMsgFlags((lpszFlags), imap4StdMsgFlagsParseCfn, (LPVOID) (pFlags))
#endif	/* __cplusplus */

/* Note: looks for the "FLAGS" modifier in response */
extern EXC_TYPE imap4ExtractMsgFlags (const char lpszRsp[], IMAP4_MSGFLAGS *pFlags);

extern EXC_TYPE imap4UpdateMsgFlags (IMAP4_MSGFLAGS				*pFlags,
												 const IMAP4_MSGFLAG_CASE	fCase,
												 const BOOLEAN					fAddIt);

extern EXC_TYPE imap4EncodeMsgFlags (const IMAP4_MSGFLAGS	*pFlags,
												 LPTSTR						lpszEnc,
												 const UINT32				ulMaxLen);

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CIMAP4MsgFlags {
	private:
		IMAP4_MSGFLAGS	m_msgFlags;

	public:
		CIMAP4MsgFlags ()
		{
			memset(&m_msgFlags, 0, (sizeof m_msgFlags));
		}

		CIMAP4MsgFlags (const IMAP4_MSGFLAGS& mFlags)
		{
			m_msgFlags = mFlags;
		}

		// copy constructor
		CIMAP4MsgFlags (const CIMAP4MsgFlags& mFlags)
		{
			m_msgFlags = mFlags.m_msgFlags;
		}

		// assignment operator
		CIMAP4MsgFlags& operator= (const CIMAP4MsgFlags& mFlags)
		{
			m_msgFlags = mFlags.m_msgFlags;
			return *this;
		}

		CIMAP4MsgFlags& operator= (const IMAP4_MSGFLAGS& mFlags)
		{
			m_msgFlags = mFlags;
			return *this;
		}

		EXC_TYPE ExtractMsgFlags (const char lpszRsp[])
		{
			return imap4ExtractMsgFlags(lpszRsp, &m_msgFlags);
		}

		EXC_TYPE UpdateMsgFlags (const IMAP4_MSGFLAG_CASE	fCase,
										 const BOOLEAN					fAddIt)
		{
			return imap4UpdateMsgFlags(&m_msgFlags, fCase, fAddIt);
		}

		CIMAP4MsgFlags& operator+= (const IMAP4_MSGFLAG_CASE	fCase)
		{
			imap4UpdateMsgFlags(&m_msgFlags, fCase, TRUE);
			return *this;
		}

		CIMAP4MsgFlags& operator-= (const IMAP4_MSGFLAG_CASE	fCase)
		{
			imap4UpdateMsgFlags(&m_msgFlags, fCase, FALSE);
			return *this;
		}

		const IMAP4_MSGFLAGS *GetFlags () const { return &m_msgFlags; }

		virtual ~CIMAP4MsgFlags () { }

		friend BOOLEAN operator== (const CIMAP4MsgFlags& f1,
											const CIMAP4MsgFlags& f2)
		{
			return ((f1.m_msgFlags.m_fSeen == f2.m_msgFlags.m_fSeen) &&
					  (f1.m_msgFlags.m_fAnswered == f2.m_msgFlags.m_fAnswered) &&
					  (f1.m_msgFlags.m_fFlagged == f2.m_msgFlags.m_fFlagged) &&
					  (f1.m_msgFlags.m_fDeleted == f2.m_msgFlags.m_fDeleted) &&
					  (f1.m_msgFlags.m_fDraft == f2.m_msgFlags.m_fDraft) &&
					  (f1.m_msgFlags.m_fRecent == f2.m_msgFlags.m_fRecent));
		}

		friend BOOLEAN operator!= (const CIMAP4MsgFlags& f1,
											const CIMAP4MsgFlags& f2)
		{
			return ((f1.m_msgFlags.m_fSeen != f2.m_msgFlags.m_fSeen) ||
					  (f1.m_msgFlags.m_fAnswered != f2.m_msgFlags.m_fAnswered) ||
					  (f1.m_msgFlags.m_fFlagged != f2.m_msgFlags.m_fFlagged) ||
					  (f1.m_msgFlags.m_fDeleted != f2.m_msgFlags.m_fDeleted) ||
					  (f1.m_msgFlags.m_fDraft != f2.m_msgFlags.m_fDraft) ||
					  (f1.m_msgFlags.m_fRecent != f2.m_msgFlags.m_fRecent));
		}
};	// end of IMAP4 msg flags class

inline EXC_TYPE imap4ExtractMsgFlags (const char lpszRsp[], CIMAP4MsgFlags& msgFlags)
{
	return msgFlags.ExtractMsgFlags(lpszRsp);
}
#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/

typedef enum {
	IMAP4_STORESET_FLAGS,	/* FLAGS */
	IMAP4_STOREADD_FLAGS,	/* +FLAGS */
	IMAP4_STOREDEL_FLAGS,	/* -FLAGS */
	IMAP4_STOREBAD_FLAGS
} IMAP4STOREFLAGSOPCASE;

extern EXC_TYPE imap4XlateStoreFlagsOperation (LPCTSTR					lpszStoreOp,
															  IMAP4STOREFLAGSOPCASE	*peOpCase,
															  BOOLEAN					*pfIsSilent);

extern EXC_TYPE imap4ExtractStoreCmdArgs (LPCTSTR						lpszStoreArgs,
														IMAP4STOREFLAGSOPCASE	*peOpCase,
														BOOLEAN						*pfIsSilent,
														IMAP4_MSGFLAGS				*pFlags);

/*---------------------------------------------------------------------------*/

extern const TCHAR IMAP4_NOINFERIORS_FLAG[];
extern const TCHAR IMAP4_NOSELECT_FLAG[];
extern const TCHAR IMAP4_MARKED_FLAG[];
extern const TCHAR IMAP4_UNMARKED_FLAG[];

/* used only if CHILDREN capability reported */
extern const TCHAR IMAP4_HASCHILDREN_FLAG[];
extern const TCHAR IMAP4_HASNOCHILDREN_FLAG[];

/* NULL terminated list of all folder flags */
extern LPCTSTR IMAP4FldrFlags[];

/*---------------------------------------------------------------------------*/

typedef enum {
	IMAP4_NOINF_FLDRCASE,
	IMAP4_NOSEL_FLDRCASE,
	IMAP4_MRKED_FLDRCASE,
	IMAP4_UNMRK_FLDRCASE,

	/* valid only if CHILDREN capability reported */
	IMAP4_HASCHILDREN_FLDRCASE,	
	IMAP4_HASNOCHILDREN_FLDRCASE,

	/* some kind of (unknown) extension flag */
	IMAP4_EXTND_FLDRCASE,

	IMAP4_BDFLG_FLDRCASE
} IMAP4_FLDRFLAG_CASE;

#define IMAP4_FLDRFLAGS_NUM	IMAP4_BDFLG_FLDRCASE
#define fIsBadIMAP4FldrFlagCase(f)	(((unsigned) (f)) >= IMAP4_FLDRFLAGS_NUM)

extern IMAP4_FLDRFLAG_CASE imap4XlateFldrFlag (LPCTSTR lpszFlag);
extern IMAP4_FLDRFLAG_CASE imap4XlateExtFldrFlag (LPCTSTR lpszFlag, const UINT32 ulFLen);

/*---------------------------------------------------------------------------*/

typedef struct {
	unsigned m_fNoInferiors		:	1;
	unsigned m_fNoSelect			:	1;
	unsigned m_fMarked			:	1;
	unsigned m_fUnMarked			:	1;
	unsigned m_fExtension		:	1;	/* if set then some unknown flag found */
	unsigned m_fHasChildren		:	1;	/* valid only if CHILDREN capability reported */
	unsigned m_fHasNoChildren	:	1;	/* valid only if CHILDREN capability reported */
} IMAP4_FLDRFLAGS, *LPIMAP4FLDRFLAGS;

/*---------------------------------------------------------------------------*/

extern EXC_TYPE imap4UpdateFldrFlags (IMAP4_FLDRFLAGS					*pFlags,
												  const IMAP4_FLDRFLAG_CASE	fCase,
												  const BOOLEAN					fAddIt);

/* Returns also first (non-space) char AFTER flags
 *
 * Note: assumes first non-space in response is either list delimiter
 *			or NIL (i.e. empty list)
 */
extern EXC_TYPE imap4ExtractFldrFlags (LPCTSTR				lpszRsp,
													IMAP4_FLDRFLAGS	*pFlags,
													LPCTSTR				*lppszPars);

/*---------------------------------------------------------------------------*/

extern EXC_TYPE imap4AnalyzeMBRefsRsp (LPCTSTR				lpszRsp,
													LPCTSTR				lpszMBRefCmd,	/* LIST/LSUB */
													LPIMAP4FLDRFLAGS	pFlags,
													TCHAR					*pchDelim,
													LPCTSTR				*lppszFldr,
													UINT32				*pulFLen);

#ifdef __cplusplus
inline EXC_TYPE imap4AnalyzeListRsp (const char			lpszRsp[],
												 LPIMAP4FLDRFLAGS	pFlags,
												 char					*pchDelim,
												 const char			**lpszFldr,
												 UINT32				*pulFLen)
{
	return imap4AnalyzeMBRefsRsp(lpszRsp, szIMAP4ListCmd, pFlags, pchDelim, lpszFldr, pulFLen);
}

inline EXC_TYPE imap4AnalyzeLSubRsp (const char			lpszRsp[],
												 LPIMAP4FLDRFLAGS	pFlags,
												 char					*pchDelim,
												 const char			**lpszFldr,
												 UINT32				*pulFLen)
{
	return imap4AnalyzeMBRefsRsp(lpszRsp, szIMAP4LSUBCmd, pFlags, pchDelim, lpszFldr, pulFLen);
}
#else
#define imap4AnalyzeListRsp(rsp,flg,dlm,fld,fln)	\
	imap4AnalyzeMBRefsRsp((rsp), szIMAP4ListCmd, (flg), (dlm), (fld), (fln))
#define imap4AnalyzeLSubRsp(rsp,flg,dlm,fld,fln)	\
	imap4AnalyzeMBRefsRsp((rsp), szIMAP4LSUBCmd, (flg), (dlm), (fld), (fln))
#endif	/* of ifdef __cplusplus

/*---------------------------------------------------------------------------*/

extern const char szIMAP4AllSearchKwd[];
extern const char szIMAP4AnsweredSearchKwd[];
extern const char szIMAP4BccSearchKwd[];
extern const char szIMAP4BeforeSearchKwd[];
extern const char szIMAP4BodySearchKwd[];
extern const char szIMAP4CcSearchKwd[];
extern const char szIMAP4DeletedSearchKwd[];
extern const char szIMAP4DraftSearchKwd[];
extern const char szIMAP4FlaggedSearchKwd[];
extern const char szIMAP4FromSearchKwd[];
extern const char szIMAP4HeaderSearchKwd[];
extern const char szIMAP4KeywordSearchKwd[];
extern const char szIMAP4LargerSearchKwd[];
extern const char szIMAP4NewSearchKwd[];
extern const char szIMAP4NoteSearchKwd[];
extern const char szIMAP4OldSearchKwd[];
extern const char szIMAP4OnSearchKwd[];
extern const char szIMAP4OrSearchKwd[];
extern const char szIMAP4RecentSearchKwd[];
extern const char szIMAP4SeenSearchKwd[];
extern const char szIMAP4SentBeforeSearchKwd[];
extern const char szIMAP4SentOnSearchKwd[];
extern const char szIMAP4SentSinceSearchKwd[];
extern const char szIMAP4SinceSearchKwd[];
extern const char szIMAP4SmallerSearchKwd[];
extern const char szIMAP4SubjectSearchKwd[];
extern const char szIMAP4TextSearchKwd[];
extern const char szIMAP4ToSearchKwd[];
extern const char szIMAP4UIDSearchKwd[];
extern const char szIMAP4UnAnsweredSearchKwd[];
extern const char szIMAP4UnDeletedSearchKwd[];
extern const char szIMAP4UnDraftSearchKwd[];
extern const char szIMAP4UnFlaggedSearchKwd[];
extern const char szIMAP4UnKeywordSearchKwd[];
extern const char szIMAP4UnSeenSearchKwd[];

/*---------------------------------------------------------------------------*/

/*		Callback used to enumerate search results (if any). If return value
 * is FALSE then enumeration is aborted.
 */
typedef EXC_TYPE (*IMAP4_SRES_CFN)(const UINT32	ulMsgSeqNo, void *pArg, BOOLEAN *pfContEnum);

/* enumerates returned search result(s) - if any */
extern EXC_TYPE imap4EnumSearcEXC_TYPEs (const char			pszResults[],
													 IMAP4_SRES_CFN	lpfnEcfn,
													 void					*pArg);

/* parses the SEARCH response and enumerates the result(s) - if any */
extern EXC_TYPE imap4EnumSearchResponse (const char		pszRsp[],
													  IMAP4_SRES_CFN	lpfnEcfn,
													  void				*pArg);

/*---------------------------------------------------------------------------*/

/*
 * Format is: "dd-mmm-yyyy hh:mm:ss [+/-]GMT"
 */
extern EXC_TYPE DecodeIMAP4InternalDate (LPCTSTR lpszDate, struct tm *ptm, int *ptmZone);

#define MAX_IMAP4_INTERNALDATE_LEN	\
	(MAX_BYTE_DISPLAY_LENGTH + 2	\
	+ MAX_WORD_DISPLAY_LENGTH		\
	+ MAX_WORD_DISPLAY_LENGTH + 2	\
	+ MAX_BYTE_DISPLAY_LENGTH + 1 \
	+ MAX_BYTE_DISPLAY_LENGTH + 1 \
	+ MAX_BYTE_DISPLAY_LENGTH + 1 \
	+ MAX_DWORD_DISPLAY_LENGTH + 2)

extern EXC_TYPE EncodeIMAP4InternalDate (const struct tm *ptm,
													  const int			tmZone,
													  LPTSTR				lpszDate,
													  const UINT32		ulMaxLen);

/*---------------------------------------------------------------------------*/

/*		Creates the smallest possible msg set representation - provided, the
 * set is SORTED in ASCENDING order. Otherwise, a less than optimal encoding
 * may be created.
 *
 *		If required encoding buffer size exceeds maximum, then correct size is
 * returned in "pulEncLen" and EOVERFLOW as return code.
 *
 * Note: one can call the routine with "lpszMsgSet==NULL" in order to query
 *			how much is needed for encoding
 */
extern EXC_TYPE imap4CreateMsgSet (const UINT32	ulSet[],
											  const UINT32	ulSetSize,
											  LPTSTR			lpszMsgSet,
											  const UINT32	ulEncLen,
											  UINT32			*pulEncLen);

#define MAX_MSGSET_RANGE_ENCLEN	((2*MAX_DWORD_DISPLAY_LENGTH)+2)

extern EXC_TYPE imap4CreateMsgRange (const UINT32	ulStartID,
												 const UINT32	ulEndID,
												 LPTSTR			lpszMsgSet,
												 const UINT32	ulEncLen,
												 UINT32			*pulEncLen);

/* returns number of msgs encoded in the msg set ((-1) if error, 0 if NULL/empty) */
extern UINT32 GetIMAP4MsgSetCount (LPCTSTR lpszMsgSet);

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CIMAP4MsgSetParser {
	private:
		LPTSTR	m_lpszMsgSet;
		LPTSTR	m_lpszCurPos;

		void Clear ();

		// disable copy constructor and assignment operator
		CIMAP4MsgSetParser (const CIMAP4MsgSetParser& mp);
		CIMAP4MsgSetParser& operator= (const CIMAP4MsgSetParser& mp);

	public:
		// restarts parsing
		void Reset () { m_lpszCurPos = m_lpszMsgSet; }

		// if NULL/empty then creates an empty set
		EXC_TYPE Init (LPCTSTR lpszMsgSet);
		EXC_TYPE Init (const UINT32 ulSet[], const UINT32 ulSetSize);

		// if start/end ID (-1) => '*' (but not both)
		EXC_TYPE Init (const UINT32 ulStartID, const UINT32 ulEndID);

		// also default constructor
		CIMAP4MsgSetParser (LPCTSTR lpszMsgSet=NULL)
			: m_lpszMsgSet(NULL), m_lpszCurPos(NULL)
		{
			Init(lpszMsgSet);
		}

		// returns pointer to 1st/next component (or NULL)
		virtual LPCTSTR GetFirst ()
		{
			Reset();
			return GetNext();
		}

		virtual LPCTSTR GetNext ();

		// possibly multi-string value - i.e. last entry is "\0\0"
		virtual LPCTSTR GetMsgSetEncoding () const { return m_lpszMsgSet; }

		virtual ~CIMAP4MsgSetParser () { Clear(); }
};	// end of IMAP4 msg set parser
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
// enumerates sequence numbers/UID(s).
class CIMAP4MsgSetEnum {
	private:
		CIMAP4MsgSetParser	m_SetParser;
		UINT32					m_ulStartIdx;
		UINT32					m_ulEndIdx;
		UINT32					m_ulCurIdx;
		UINT32					m_ulMaxIdx;
		BOOLEAN					m_fIsUIDSet;

		// disable copy constructor and assignment operator
		CIMAP4MsgSetEnum (const CIMAP4MsgSetEnum& mse);
		CIMAP4MsgSetEnum& operator= (const CIMAP4MsgSetEnum& mse);

		void Reset ();

		EXC_TYPE SetRange (LPCTSTR lpszRange, UINT32& ulIdx);

	public:
		EXC_TYPE Init (LPCTSTR			lpszMsgSet,
							const UINT32	ulMaxIdx=0,	// required if "1:*" range is used
							const BOOLEAN	fIsUIDSet=FALSE);

		// also default constructor
		CIMAP4MsgSetEnum (LPCTSTR			lpszMsgSet=NULL,
							   const UINT32	ulMaxIdx=0,
								const BOOLEAN	fIsUIDSet=FALSE)
		{
			Init(lpszMsgSet, ulMaxIdx, fIsUIDSet);
		}

		BOOLEAN IsUIDSet () const { return m_fIsUIDSet; }

		// returns EEOF at end (or non-EOK for other errors)
		EXC_TYPE GetFirst (UINT32& ulIdx);
		EXC_TYPE GetNext (UINT32& ulIdx);

		virtual ~CIMAP4MsgSetEnum () { }
};	// end of IMAP4 msg set enumerator
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
// class for detecting paranthesized response
class CIMAP4ParListChecker {
	private:
		UINT32	m_ulBraceCount;
		BOOLEAN	m_fIsDelimStr;
	public:
		void Reset ()
		{
			m_ulBraceCount = 0;
			m_fIsDelimStr = FALSE;
		}

		UINT32 GetBraceCount () const { return m_ulBraceCount; }
		BOOLEAN IsDelimStr () const { return m_fIsDelimStr; }

		// returns EEOF if brace count is zero
		EXC_TYPE ProcessResponse (const char lpszRsp[], UINT32& ulOCount);

		CIMAP4ParListChecker () { Reset(); }
		virtual ~CIMAP4ParListChecker () { }
};
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

extern EXC_TYPE GetIMAP4AutoTag (LPTSTR lpszTag, const UINT32 ulMaxLen);

/* makes sure returned welcome is "* OK ....." */
extern EXC_TYPE CheckIMAP4Welcome (LPCTSTR lpszWelcome);

/*--------------------------------------------------------------------------*/

/* do not change order !!! */
typedef enum {
	IMAP4STMSGS,		/* number of messages in folder */
	IMAP4STRECENT,		/* number of messages with "\Recent" flag set */
	IMAP4STUIDNEXT,	/* next UID to be assigned to mew message in mailbox */
	IMAP4STUDIVALID,	/* UID validity value assigned to mailbox */
	IMAP4STUNSEEN,		/* number of messages that do NOT have "\Seen" set */
	IMAP4STBAD
} IMAP4STKWCASE;

#define IMAP4_STINFO_ARGS_NUM	IMAP4STBAD
#define fIsBadIMAP4StatusKWCase(k) (((unsigned) (k)) >= IMAP4STBAD)

/* maximum string length for a (standard) STATUS response */
#define MAX_IMAP4_STATUS_INFO_LEN	((IMAP4_STINFO_ARGS_NUM * (MAX_DWORD_DISPLAY_LENGTH + NAME_LENGTH + 4)) + 2)

/* returns NULL.empty if illegal value */
extern LPCTSTR imap4GetStatusKeywordString (const IMAP4STKWCASE eStCase);

/* returns IMAP4STBAD if unknown keyword */
extern IMAP4STKWCASE imap4GetStatusKeywordCase (LPCTSTR lpszKW, const UINT32 ulKWLen);

/*--------------------------------------------------------------------------*/

/* specifies which information to retrieve via the status command */
typedef struct {
	unsigned m_fNumOfMsgs	: 1;	/* number of messages in folder */
	unsigned m_fNumRecent	: 1;	/* number of messages with "\Recent" flag set */
	unsigned m_fUIDNext		: 1;	/* next UID to be assigned to mew message in mailbox */
	unsigned m_fUIDValidity	: 1;	/* UID validity value assigned to mailbox */
	unsigned m_fUnseen		: 1;	/* number of messages that do NOT have "\Seen" set */
} IMAP4STATUSFLAGS, *LPIMAP4STATUSFLAGS;

/* returned information from status command - non-valid fields are set to (-1) */
typedef struct {
	UINT32	ulNumOfMsgs;	/* number of messages in folder */
	UINT32	ulNumRecent;	/* number of messages with "\Recent" flag set */
	UINT32	ulUIDNext;		/* next UID to be assigned to mew message in mailbox */
	UINT32	ulUIDValidity;	/* UID validity value assigned to mailbox */
	UINT32	ulUnseen;		/* number of messages that do NOT have "\Seen" set */
} IMAP4STATUSINFO, *LPIMAP4STATUSINFO;

extern EXC_TYPE imap4UpdateStatusValue (const IMAP4STKWCASE	eStCase,
													 const UINT32			ulVal,
													 LPIMAP4STATUSINFO	pInfo);

extern EXC_TYPE imap4ParseStatusList (LPCTSTR lpszRsp, LPIMAP4STATUSINFO pInfo);

extern EXC_TYPE imap4ParseStatusRsp (LPCTSTR					lpszRsp,
												 LPCTSTR					*lppszFolder,
												 UINT32					*pulFNLen,
												 LPIMAP4STATUSINFO	pInfo);

typedef EXC_TYPE (*IMAP4_STRQ_ECFN)(LPCTSTR					lpszModifier,
												const UINT32			ulModLen,
												const IMAP4STKWCASE	eModCase,	/* NOTE: may be IMAP4STBAD */
												LPVOID					pArg,
												BOOLEAN					*fContEnum);

/* if request starts with '(' it must also end with ')' */
extern EXC_TYPE imap4ParseStatusRequest (LPCTSTR lpszReq, IMAP4_STRQ_ECFN lpfnEcfn, LPVOID pArg);

/*--------------------------------------------------------------------------*/

/* parses a "* Num Opcode" response (e.g. "* 9 EXISTS") - where the '*' is optional */
extern EXC_TYPE imap4ParseNumAndOpPair (LPCTSTR	lpszRsp,
													 UINT32	*pulMsgID,
													 LPCTSTR	*lppszOpcode,
													 UINT32	*pulOpLen);

extern EXC_TYPE imap4ExtractNumAndOpPair (LPCTSTR			lpszRsp,
														UINT32			*pulMsgID,
														LPTSTR			lpszOpcode,
														const UINT32	ulMaxOpLen);

/*--------------------------------------------------------------------------*/

extern EXC_TYPE imap4ParseExpungeRsp (LPCTSTR	lpszRsp,
												  UINT32		*pulMsgID,
												  LPCTSTR	*lppszOpcode,
												  UINT32		*pulOpLen);

/*--------------------------------------------------------------------------*/

extern EXC_TYPE imap4ParseStoreRsp (LPCTSTR				lpszRsp,
												UINT32				*pulMsgID,
												LPIMAP4MSGFLAGS	pFlags);

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
// helper class for parsing responses which may span several lines (e.g. octet counts)
class CIMAP4RspParser {
	private:
		BOOLEAN	m_fAutoAlloc;

		CIMAP4RspParser (const CIMAP4RspParser& );
		CIMAP4RspParser& operator= (const CIMAP4RspParser& );

	protected:
		ISockioInterface&		m_SBSock;
		LPCTSTR					m_lpszTag;
		LPCTSTR					m_lpszCurPos;
		LPTSTR					m_lpszRspBuf;
		UINT32					m_ulRspBufLen;
		BOOLEAN					m_fStripCRLF;
		UINT32					m_ulRspTimeout;

		CIMAP4RspParser (ISockioInterface&	SBSock,
							  LPCTSTR				lpszTag,
							  const UINT32			ulRspTimeout,
							  const UINT32			ulRspBufLen=MAX_IMAP4_DATA_LEN);

		CIMAP4RspParser (ISockioInterface&	SBSock,
							  LPCTSTR				lpszTag,
							  LPTSTR					lpszRspBuf,
							  const UINT32			ulMaxRspLen,
							  const UINT32			ulRspTimeout);

		virtual BOOLEAN IsIMAP4QuoteDelimCurPos () const;

		virtual EXC_TYPE RefillFetchParseBuffer (LPCTSTR lpszBufStart=NULL);
		virtual EXC_TYPE FillNonEmptyParseBuffer ();

		// returns EWILDCARD if atom is not NIL, and returns current position to initial position
		virtual EXC_TYPE CheckNILParseBuffer (const BOOLEAN fAllowEOS);

		// assume immediate response is FLAGS (if non-NULL enumeration function is supplied then it is called for the raw data)
		virtual EXC_TYPE ExtractMsgFlags (IMAP4_MSGFLAGS& msgFlags, IMAP4_FLAGS_ENUM_CFN lpfnEcfn=NULL, LPVOID pArg=NULL);

		virtual EXC_TYPE SkipLiteralData (const UINT32 ulDataLen);

		// assumes literal count is at current position
		virtual EXC_TYPE SkipLiteralData ();
		virtual EXC_TYPE SkipToEndOfLine ();

		virtual EXC_TYPE ExtractLiteralCount (UINT32& ulOCount);

		virtual EXC_TYPE ExtractLiteralHdrVal (LPCTSTR&			lpszHdrVal,
															UINT32&			ulHdrLen,
															const BOOLEAN	fAllowOverflow);

		virtual EXC_TYPE ExtractStringHdrVal (LPCTSTR&			lpszHdrVal,
														  UINT32&			ulHdrLen,
														  const BOOLEAN	fAllowEOS,
														  const BOOLEAN	fAllowOverflow);

		virtual EXC_TYPE CopyStringHdrVal (LPTSTR				lpszHdrVal,
													  const UINT32		ulMaxLen,
													  UINT32&			ulHdrLen,
													  const BOOLEAN	fAllowEOS,
													  const BOOLEAN	fAllowOverflow);

		virtual EXC_TYPE ExtractNumVal (LPCTSTR& lpszNumVal, UINT32& ulNumLen);
		virtual EXC_TYPE ExtractNumVal (UINT32& ulNumVal);

		/*		Handles a response of type "* OPCODE fldr (params list)" as
		 * received thru "HandleUntaggedResponse". In this case, "lpszOpArg"
		 * should be the "lpszOp" argument of the "HandleUntaggedResponse".
		 *		Upon return, places "m_lpszCurPos" one place beyond the '('.
		 */
		virtual EXC_TYPE SkipFolderUpToParamsList (LPCTSTR lpszOpArg);

		virtual EXC_TYPE ResyncResponse (LPCTSTR lpszTag, const EXC_TYPE orgErr)
		{
			return orgErr;
		}

		virtual EXC_TYPE HandleUntaggedResponse (LPCTSTR lpszTag, LPCTSTR lpszOp) = 0;

		virtual ~CIMAP4RspParser ()
		{
			if (m_fAutoAlloc)
				strreleasebuf(m_lpszRspBuf);
		}

	public:
		// if "fRspPair" is TRUE then expected untagged responses contain at least 2 parameters
		// (e.g. "* EXPUNGE" is NOT a response pair untagged response)
		virtual EXC_TYPE ParseResponse (const BOOLEAN fRspPair=TRUE);
};
#endif	/* of ifdef __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
// build tag and add it to the command line (Note: updates tag pointer if auto-generated)
extern EXC_TYPE InitIMAP4Tag (LPCTSTR&			lpszRTag,	// in/out
										LPTSTR			lpszAutoTag,
										const UINT32	ulMaxTagLen,
										IStrlBuilder&	strb);

// build tag+command and add it to the command line (Note: updates tag pointer if auto-generated)
extern EXC_TYPE InitIMAP4Cmd (LPCTSTR&			lpszRTag,	// in/out
										LPCTSTR			lpszCmd,
										const BOOLEAN	fIsUID,
										LPTSTR			lpszAutoTag,
										const UINT32	ulMaxTagLen,
										IStrlBuilder&	strb);
#endif	/* of ifdef __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
extern EXC_TYPE SendIMAP4Reference (ISockioInterface&	SBSock,
												LPCTSTR				lpszCmdPrefix,
												LPCTSTR				lpszRef,
												const BOOLEAN		fAllowRefChars,
												const UINT32		ulRspTimeout);

// return ELITERAL if folder sent as literal
inline EXC_TYPE SendIMAP4Folder (ISockioInterface&	SBSock,
											LPCTSTR				lpszCmdPrefix,
											LPCTSTR				lpszFolder,
											const UINT32		ulRspTimeout)
{
	if (IsEmptyStr(lpszFolder))
		return EEMPTYENTRY;
	else
		return SendIMAP4Reference(SBSock, lpszCmdPrefix, lpszFolder, FALSE, ulRspTimeout);
}

// add folder (quote it if necessary)
extern EXC_TYPE AddIMAP4Reference (IStrlBuilder& strb, LPCTSTR lpszRef);

inline EXC_TYPE AddIMAP4Folder (IStrlBuilder& strb, LPCTSTR lpszFolder)
{
	if (IsEmptyStr(lpszFolder))
		return EPATH;
	else
		return AddIMAP4Reference(strb, lpszFolder);
}
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// callback for handling untagged responses - if returns EABORTEXIT then no
// more responses are read from server
//
// Note: in this case, it is assumed that the protocol state is "synchronized"
typedef EXC_TYPE (*IMAP4_RSPHNDL_CFN)(ISockioInterface&	SBSock,
												  LPTSTR					lpszRsp,
												  const UINT32			ulRspLen,
												  const UINT32			ulMaxRspLen,
												  LPVOID					pArg);

inline EXC_TYPE imap4OKHCfn (ISockioInterface&	/* SBSock */,
									  LPTSTR					/* lpszRsp */,
									  const UINT32			/* ulRspLen */,
									  const UINT32			/* ulMaxRspLen */,
									  LPVOID					/* pArg */)
{
	return EOK;
}

extern EXC_TYPE imap4GetRspSync (ISockioInterface&	SBSock,
											LPCTSTR				lpszTag,
											LPTSTR				lpszRspBuf,
											const UINT32		ulMaxRspLen,
											const UINT32		ulRspTimeout,
											IMAP4_RSPHNDL_CFN	lpfnHcfn,
											LPVOID				pArg);

inline EXC_TYPE imap4GetFinalRspSync (ISockioInterface&	SBSock,
												  LPCTSTR				lpszTag,
												  LPTSTR					lpszRspBuf,
												  const UINT32			ulMaxRspLen,
												  const UINT32			ulRspTimeout)
{
	return imap4GetRspSync(SBSock, lpszTag, lpszRspBuf, ulMaxRspLen, ulRspTimeout, imap4OKHCfn, NULL);
}
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
// Note: automatically adds tag
extern EXC_TYPE imap4ExecCmdVSync (ISockioInterface&	SBSock,
											  LPCTSTR				lpszITag,	// NULL == auto generate
											  LPTSTR					lpszRspBuf,
											  const UINT32			ulMaxRspLen,
											  const UINT32			ulRspTimeout,
											  IMAP4_RSPHNDL_CFN	lpfnHcfn,
											  LPVOID					pArg,
											  LPCTSTR				lpszOp,
											  const BOOLEAN		fIsUID,
											  LPCTSTR				lpszCmdFmt,	// may be NULL/empty
											  va_list				ap);

// Note: automatically adds tag
extern EXC_TYPE imap4ExecCmdfSync (ISockioInterface&	SBSock,
											  LPCTSTR				lpszITag,	// NULL == auto generate
											  LPTSTR					lpszRsp,
											  const UINT32			ulMaxRspLen,
											  const UINT32			ulRspTimeout,
											  IMAP4_RSPHNDL_CFN	lpfnHcfn,
											  LPVOID					pArg,
											  LPCTSTR				lpszOp,
											  const BOOLEAN		fIsUID,
											  LPCTSTR				lpszCmdFmt,	// may be NULL/empty
											  ...);
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
/*		Opens an IMAP4 socket to the specified host using the specified port. If
 * port if unspecified (i.e. <= 0) then "getservbyname" is used. If this fails
 * then the default IPPORT_IMAP4 (143) is used.
 */
extern EXC_TYPE imap4Connect (ISockioInterface&	SBSock,
										LPCTSTR				lpszHost,
										const int			iConnPort,	// 0 == use default
										LPTSTR				lpszRsp,
										const UINT32		ulMaxLen,
										const UINT32		ulRspTimeout);

extern EXC_TYPE imap4Connect (ISockioInterface&	SBSock,
										LPCTSTR				lpszHost,
										const int			iConnPort,	// 0 == use default
										const UINT32		ulRspTimeout);

// callback used to list server capabilities
typedef EXC_TYPE (*IMAP4_CAPSENUM_CFN)(ISockioInterface&	SBSock,
													LPCTSTR				lpszCapability,
													LPVOID				pArg,
													BOOLEAN&				fContEnum);

extern EXC_TYPE imap4CapabilitiesSync (ISockioInterface&		SBSock,
													LPCTSTR					lpszITag,	// NULL == auto-generate
													IMAP4_CAPSENUM_CFN	lpfnEcfn,
													LPVOID					pArg,
													const UINT32			ulRspTimeout);

extern EXC_TYPE imap4CapabilitiesSync (ISockioInterface&		SBSock,
													LPCTSTR					lpszITag,	// NULL == auto-generate
													IMAP4_CAPSENUM_CFN	lpfnEcfn,
													LPVOID					pArg,
													LPTSTR					lpszRspBuf,
													const UINT32			ulMaxRspLen,
													const UINT32			ulRspTimeout);

extern EXC_TYPE imap4LoginUserSync (ISockioInterface&	SBSock,
												LPCTSTR				lpszITag,	// NULL == auto generate
												LPCTSTR				lpszUID,
												LPCTSTR				lpszPasswd,
												LPTSTR				lpszRspBuf,
												const UINT32		ulMaxRspLen,
												const UINT32		ulRspTimeout);

extern EXC_TYPE imap4LoginUserSync (ISockioInterface&	SBSock,
												LPCTSTR				lpszITag,	// NULL == auto-generate
												LPCTSTR				lpszUID,
												LPCTSTR				lpszPasswd,
												const UINT32		ulRspTimeout);

inline EXC_TYPE imap4NoopSync (ISockioInterface&	SBSock,
										 LPCTSTR					lpszITag,	// NULL == auto-generate
										 IMAP4_RSPHNDL_CFN	lpfnIcfn,	// may be NULL
										 LPVOID					pArg,
										 LPTSTR					lpszRspBuf,
										 const UINT32			ulMaxRspLen,
										 const UINT32			ulRspTimeout)
{
	return imap4ExecCmdfSync(SBSock, lpszITag, lpszRspBuf, ulMaxRspLen, ulRspTimeout,
									((NULL == lpfnIcfn) ? imap4OKHCfn : lpfnIcfn),
									((NULL == lpfnIcfn) ? NULL : pArg),
									 szIMAP4NoopCmd, FALSE, NULL);
}

extern EXC_TYPE imap4NoopSync (ISockioInterface&	SBSock,
										 LPCTSTR					lpszITag,	// NULL == auto-generate
										 IMAP4_RSPHNDL_CFN	lpfnIcfn,	// may be NULL
										 LPVOID					pArg,
										 const UINT32			ulRspTimeout);

inline EXC_TYPE imap4CloseSync (ISockioInterface&	SBSock,
										  LPCTSTR				lpszITag,	// NULL == auto-generate
										  LPTSTR					lpszRspBuf,
										  const UINT32			ulMaxRspLen,
										  const UINT32			ulRspTimeout)
{
	return imap4ExecCmdfSync(SBSock, lpszITag, lpszRspBuf, ulMaxRspLen, ulRspTimeout,
									 imap4OKHCfn, NULL,
									 szIMAP4CloseCmd, FALSE, NULL);
}

extern EXC_TYPE imap4CloseSync (ISockioInterface&	SBSock,
										  LPCTSTR				lpszITag,	// NULL == auto-generate
										  const UINT32			ulRspTimeout);

extern EXC_TYPE imap4LogoutUserSync (ISockioInterface&	SBSock,
												 LPCTSTR					lpszITag,	// NULL == auto-generate
												 LPTSTR					lpszRspBuf,
												 const UINT32			ulMaxRspLen,
												 const UINT32			ulRspTimeout);

// Note: also closes the connection
extern EXC_TYPE imap4LogoutUserSync (ISockioInterface&	SBSock,
												 LPCTSTR					lpszITag,	// NULL == auto-generate
												 const UINT32			ulRspTimeout);
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
/*
 *	Makes sure the provided account parameters are valid. Returns:
 *
 *		ENOTCONNECTION - server could not be connected
 *		ESTATE - server service denied ("* NO/BAD" in welcome line)
 *		EPERMISSION - authentication failed
 */
extern EXC_TYPE imap4ValidateClientSync (LPCTSTR		lpszHost,
													  const int		iPort,		/* if 0 then use default */
													  LPCTSTR		lpszUserName,
													  LPCTSTR		lpszPasswd,
													  LPTSTR			lpszRspBuf,
													  const UINT32	ulBufLen,
													  const UINT32	ulRspTimeout);

extern EXC_TYPE imap4ValidateClientSync (LPCTSTR		lpszHost,
													  const int		iPort,		/* if 0 then use default */
													  LPCTSTR		lpszUserName,
													  LPCTSTR		lpszPasswd,
													  const UINT32	ulRspTimeout);
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

typedef struct {
	UINT32			ulExistNum;		/* number of messages in selected folder */
	UINT32			ulRecentNum;	/* number of recent messages in selected folder */
	UINT32			ulUnseenNum;	/* first unseen message number (or (-1) if unknown */
	UINT32			ulPermissions;
	UINT32			ulUIDValidity;
	IMAP4_MSGFLAGS	dynFlags;	/* supported flags */
	IMAP4_MSGFLAGS	permFlags;	/* flags that can be changed permanently */
	UINT32			ulUIDNext;		/* next UID to be used for the next new message */
} IMAP4SELECTSTRUCT, *LPIMAP4SELECTSTRUCT;

#ifdef __cplusplus

// waits for the '+' sign (skipping any untagged responses meanwhile via the callback)
extern EXC_TYPE WaitForIMAP4Continuation (ISockioInterface&	SBSock,
														LPTSTR				lpszRspBuf,
														const UINT32		ulMaxLen,
														IMAP4_RSPHNDL_CFN	lpfnHcfn,	// may be NULL
														LPVOID				pArg,
														const UINT32		ulRspTimeout);

extern EXC_TYPE WaitForIMAP4Continuation (ISockioInterface&	SBSock,
														IMAP4_RSPHNDL_CFN	lpfnHcfn,	// may be NULL
														LPVOID				pArg,
														const UINT32		ulRspTimeout);

extern EXC_TYPE SendIMAP4FolderCmd (ISockioInterface&	SBSock,
												LPCTSTR				lpszITag,
												LPCTSTR				lpszCmd,
												const BOOLEAN		fIsUID,
												LPCTSTR				lpszCmdPrefix,	// may be NULL
												LPCTSTR				lpszFolder,
												IMAP4_RSPHNDL_CFN	lpfnHcfn,
												LPVOID				pArg,
												LPTSTR				lpszRspBuf,
												const UINT32		ulMaxRspLen,
												const UINT32		ulRspTimeout);

extern EXC_TYPE SendIMAP4FolderCmd (ISockioInterface&	SBSock,
												LPCTSTR				lpszITag,
												LPCTSTR				lpszCmd,
												const BOOLEAN		fIsUID,
												LPCTSTR				lpszCmdPrefix,	// may be NULL
												LPCTSTR				lpszFolder,
												IMAP4_RSPHNDL_CFN	lpfnHcfn,
												LPVOID				pArg,
												const UINT32		ulRspTimeout);

extern EXC_TYPE imap4FolderStatsSync (ISockioInterface&		SBSock,
												  LPCTSTR					lpszITag,	// NULL == auto-generate
												  LPCTSTR					lpszCmd,
												  LPCTSTR					lpszFolder,
												  IMAP4SELECTSTRUCT&		selStruct,
												  IMAP4_FLAGS_ENUM_CFN	lpfnFlagsEnumCfn,	// may be NULL
												  LPVOID						pFlagsArg,
												  IMAP4_FLAGS_ENUM_CFN	lpfnPermFlagsEnumCfn,	// may be NULL
												  LPVOID						pPermFlagsArg,
												  LPTSTR						lpszRspBuf,
												  const UINT32				ulMaxRspLen,
												  const UINT32				ulRspTimeout);

/* NOTE(s):
 *		a. initial size of raw flags collection(s) should be IMAP4_BADFLAG_MSGCASE
 *		b. flags are ADDED to the raw collection(s) regardless of whether they already exist in them
 */
extern EXC_TYPE imap4FullFolderStatsSync (ISockioInterface&		SBSock,
														LPCTSTR					lpszITag,	// NULL == auto-generate
														LPCTSTR					lpszCmd,
														LPCTSTR					lpszFolder,
														IMAP4SELECTSTRUCT&	selStruct,
														CVSDCollection&		dynRawFlags,	// each member is a string
														CVSDCollection&		prmRawFlags,	// each member is a string
														LPTSTR					lpszRspBuf,
														const UINT32			ulMaxRspLen,
														const UINT32			ulRspTimeout);

extern EXC_TYPE imap4FullFolderStatsSync (ISockioInterface&		SBSock,
														LPCTSTR					lpszITag,	// NULL == auto-generate
														LPCTSTR					lpszCmd,
														LPCTSTR					lpszFolder,
														IMAP4SELECTSTRUCT&	selStruct,
														CVSDCollection&		dynRawFlags,	// each member is a string
														CVSDCollection&		prmRawFlags,	// each member is a string
														const UINT32			ulRspTimeout);

inline EXC_TYPE imap4FolderStatsSync (ISockioInterface&	SBSock,
												  LPCTSTR				lpszITag,	// NULL == auto-generate
												  LPCTSTR				lpszCmd,
												  LPCTSTR				lpszFolder,
												  IMAP4SELECTSTRUCT&	selStruct,
												  LPTSTR					lpszRspBuf,
												  const UINT32			ulMaxRspLen,
												  const UINT32			ulRspTimeout)
{
	return imap4FolderStatsSync(SBSock, lpszITag, lpszCmd, lpszFolder, selStruct, NULL, NULL, NULL, NULL, lpszRspBuf, ulMaxRspLen, ulRspTimeout);
}

extern EXC_TYPE imap4FolderStatsSync (ISockioInterface&	SBSock,
												  LPCTSTR				lpszITag,	// NULL == auto-generate
												  LPCTSTR				lpszCmd,
												  LPCTSTR				lpszFolder,
												  IMAP4SELECTSTRUCT&	selStruct,
												  const UINT32			ulRspTimeout);

/* NOTE(s):
 *		a. initial size of raw flags collection(s) should be IMAP4_BADFLAG_MSGCASE
 *		b. flags are ADDED to the raw collection(s) regardless of whether they already exist in them
 */
inline EXC_TYPE imap4SelectFullFolderSync (ISockioInterface&	SBSock,
														 LPCTSTR					lpszITag,	// NULL == auto-generate
														 LPCTSTR					lpszFolder,
														 IMAP4SELECTSTRUCT&	selStruct,
														 CVSDCollection&		dynRawFlags,	// each member is a string
														 CVSDCollection&		prmRawFlags,	// each member is a string
														 const UINT32			ulRspTimeout)
{
	return imap4FullFolderStatsSync(SBSock, lpszITag, szIMAP4SelectCmd, lpszFolder, selStruct, dynRawFlags, prmRawFlags, ulRspTimeout);
}

inline EXC_TYPE imap4SelectFolderSync (ISockioInterface&		SBSock,
													LPCTSTR					lpszITag,	// NULL == auto-generate
													LPCTSTR					lpszFolder,
													IMAP4SELECTSTRUCT&	selStruct,
													const UINT32			ulRspTimeout)
{
	return imap4FolderStatsSync(SBSock, lpszITag, szIMAP4SelectCmd, lpszFolder, selStruct, ulRspTimeout);
}

/* NOTE(s):
 *		a. initial size of raw flags collection(s) should be IMAP4_BADFLAG_MSGCASE
 *		b. flags are ADDED to the raw collection(s) regardless of whether they already exist in them
 */
inline EXC_TYPE imap4SelectFullFolderSync (ISockioInterface&	SBSock,
														 LPCTSTR					lpszITag,	// NULL == auto-generate
														 LPCTSTR					lpszFolder,
														 IMAP4SELECTSTRUCT&	selStruct,
														 CVSDCollection&		dynRawFlags,	// each member is a string
														 CVSDCollection&		prmRawFlags,	// each member is a string
														 LPTSTR					lpszRspBuf,
														 const UINT32			ulMaxRspLen,
														 const UINT32			ulRspTimeout)
{
	return imap4FullFolderStatsSync(SBSock, lpszITag, szIMAP4SelectCmd, lpszFolder, selStruct, dynRawFlags, prmRawFlags, lpszRspBuf, ulMaxRspLen, ulRspTimeout);
}

inline EXC_TYPE imap4SelectFolderSync (ISockioInterface&		SBSock,
													LPCTSTR					lpszITag,	// NULL == auto-generate
													LPCTSTR					lpszFolder,
													IMAP4SELECTSTRUCT&	selStruct,
													LPTSTR					lpszRspBuf,
													const UINT32			ulMaxRspLen,
													const UINT32			ulRspTimeout)
{
	return imap4FolderStatsSync(SBSock, lpszITag, szIMAP4SelectCmd, lpszFolder, selStruct, lpszRspBuf, ulMaxRspLen, ulRspTimeout);
}

inline EXC_TYPE imap4ExamineFolderSync (ISockioInterface&	SBSock,
													 LPCTSTR					lpszITag,	// NULL == auto-generate
													 LPCTSTR					lpszFolder,
													 IMAP4SELECTSTRUCT&	selStruct,
													 const UINT32			ulRspTimeout)
{
	return imap4FolderStatsSync(SBSock, lpszITag, szIMAP4ExamineCmd, lpszFolder, selStruct, ulRspTimeout);
}

/* NOTE(s):
 *		a. initial size of raw flags collection(s) should be IMAP4_BADFLAG_MSGCASE
 *		b. flags are ADDED to the raw collection(s) regardless of whether they already exist in them
 */
inline EXC_TYPE imap4ExamineFullFolderSync (ISockioInterface&	SBSock,
														  LPCTSTR				lpszITag,	// NULL == auto-generate
														  LPCTSTR				lpszFolder,
														  IMAP4SELECTSTRUCT&	selStruct,
														  CVSDCollection&		dynRawFlags,	// each member is a string
														  CVSDCollection&		prmRawFlags,	// each member is a string
														  const UINT32			ulRspTimeout)
{
	return imap4FullFolderStatsSync(SBSock, lpszITag, szIMAP4ExamineCmd, lpszFolder, selStruct, dynRawFlags, prmRawFlags, ulRspTimeout);
}

inline EXC_TYPE imap4ExamineFolderSync (ISockioInterface&	SBSock,
													 LPCTSTR					lpszITag,	// NULL == auto-generate
													 LPCTSTR					lpszFolder,
													 IMAP4SELECTSTRUCT&	selStruct,
													 LPTSTR					lpszRspBuf,
													 const UINT32			ulMaxRspLen,
													 const UINT32			ulRspTimeout)
{
	return imap4FolderStatsSync(SBSock, lpszITag, szIMAP4ExamineCmd, lpszFolder, selStruct, lpszRspBuf, ulMaxRspLen, ulRspTimeout);
}

/* NOTE(s):
 *		a. initial size of raw flags collection(s) should be IMAP4_BADFLAG_MSGCASE
 *		b. flags are ADDED to the raw collection(s) regardless of whether they already exist in them
 */
inline EXC_TYPE imap4ExamineFullFolderSync (ISockioInterface&	SBSock,
														  LPCTSTR				lpszITag,	// NULL == auto-generate
														  LPCTSTR				lpszFolder,
														  IMAP4SELECTSTRUCT&	selStruct,
														  CVSDCollection&		dynRawFlags,	// each member is a string
														  CVSDCollection&		prmRawFlags,	// each member is a string
														  LPTSTR					lpszRspBuf,
														  const UINT32			ulMaxRspLen,
														  const UINT32			ulRspTimeout)
{
	return imap4FullFolderStatsSync(SBSock, lpszITag, szIMAP4ExamineCmd, lpszFolder, selStruct, dynRawFlags, prmRawFlags, lpszRspBuf, ulMaxRspLen, ulRspTimeout);
}

extern EXC_TYPE imap4RenameFolderSync (ISockioInterface&	SBSock,
													LPCTSTR				lpszITag,	// NULL == auto-generate
													LPCTSTR				lpszOldName,
													LPCTSTR				lpszNewName,
													LPTSTR				lpszRspBuf,
													const UINT32		ulMaxRspLen,
													const UINT32		ulRspTimeout);

extern EXC_TYPE imap4RenameFolderSync (ISockioInterface&	SBSock,
													LPCTSTR				lpszITag,	// NULL == auto-generate
													LPCTSTR				lpszOldName,
													LPCTSTR				lpszNewName,
													const UINT32		ulRspTimeout);

inline EXC_TYPE imap4ExecFolderCmdSync (ISockioInterface&	SBSock,
													 LPCTSTR					lpszITag,	// NULL == auto-generate
													 LPCTSTR					lpszCmd,
													 LPCTSTR					lpszFolder,
													 const UINT32			ulRspTimeout)
{
	return SendIMAP4FolderCmd(SBSock, lpszITag, lpszCmd, FALSE, NULL, lpszFolder, imap4OKHCfn, NULL, ulRspTimeout);
}

inline EXC_TYPE imap4ExecFolderCmdSync (ISockioInterface&	SBSock,
													 LPCTSTR					lpszITag,	// NULL == auto-generate
													 LPCTSTR					lpszCmd,
													 LPCTSTR					lpszFolder,
													 LPTSTR					lpszRspBuf,
													 const UINT32			ulMaxRspLen,
													 const UINT32			ulRspTimeout)
{
	return SendIMAP4FolderCmd(SBSock, lpszITag, lpszCmd, FALSE, NULL, lpszFolder, imap4OKHCfn, NULL, lpszRspBuf, ulMaxRspLen, ulRspTimeout);
}

inline EXC_TYPE imap4CreateFolderSync (ISockioInterface&	SBSock,
													LPCTSTR				lpszITag,	// NULL == auto-generate
													LPCTSTR				lpszFolder,
													const BOOLEAN		fHasInferiors,
													const UINT32		ulRspTimeout)
{
	return imap4ExecFolderCmdSync(SBSock, lpszITag, szIMAP4CreateCmd, lpszFolder, ulRspTimeout);
}

inline EXC_TYPE imap4CreateFolderSync (ISockioInterface&	SBSock,
													LPCTSTR				lpszITag,	// NULL == auto-generate
													LPCTSTR				lpszFolder,
													const BOOLEAN		fHasInferiors,
													LPTSTR				lpszRspBuf,
													const UINT32		ulMaxRspLen,
													const UINT32		ulRspTimeout)
{
	return imap4ExecFolderCmdSync(SBSock, lpszITag, szIMAP4CreateCmd, lpszFolder, lpszRspBuf, ulMaxRspLen, ulRspTimeout);
}

inline EXC_TYPE imap4DeleteFolderSync (ISockioInterface&	SBSock,
													LPCTSTR				lpszITag,	// NULL == auto-generate
													LPCTSTR				lpszFolder,
													const UINT32		ulRspTimeout)
{
	return imap4ExecFolderCmdSync(SBSock, lpszITag, szIMAP4DeleteCmd, lpszFolder, ulRspTimeout);
}

inline EXC_TYPE imap4DeleteFolderSync (ISockioInterface&	SBSock,
													LPCTSTR				lpszITag,	// NULL == auto-generate
													LPCTSTR				lpszFolder,
													LPTSTR				lpszRspBuf,
													const UINT32		ulMaxRspLen,
													const UINT32		ulRspTimeout)
{
	return imap4ExecFolderCmdSync(SBSock, lpszITag, szIMAP4DeleteCmd, lpszFolder, lpszRspBuf, ulMaxRspLen, ulRspTimeout);
}

inline EXC_TYPE imap4SubscribeFolderSync (ISockioInterface&	SBSock,
														LPCTSTR				lpszITag,	// NULL == auto-generate
														LPCTSTR				lpszFolder,
														const UINT32		ulRspTimeout)
{
	return imap4ExecFolderCmdSync(SBSock, lpszITag, szIMAP4SubsCmd, lpszFolder, ulRspTimeout);
}

inline EXC_TYPE imap4UnsubscribeFolderSync (ISockioInterface&	SBSock,
														  LPCTSTR				lpszITag,	// NULL == auto-generate
														  LPCTSTR				lpszFolder,
														  const UINT32			ulRspTimeout)
{
	return imap4ExecFolderCmdSync(SBSock, lpszITag, szIMAP4UnSubsCmd, lpszFolder, ulRspTimeout);
}
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
// callback used to enumerate "LIST" responses
typedef EXC_TYPE (*IMAP4_LISTECFN)(ISockioInterface&			SBSock,
											  LPCTSTR						lpszFolder,
											  const TCHAR					chSep,
											  const IMAP4_FLDRFLAGS&	flags,
											  LPVOID							pArg,
											  BOOLEAN&						fContEnum);

extern EXC_TYPE imap4MBRefSync (ISockioInterface&	SBSock,
										  LPCTSTR				lpszITag,	// NULL == auto generate
										  LPCTSTR				lpszCmd,		// LIST/LSUB
										  LPCTSTR				lpszRef,
										  LPCTSTR				lpszMbox,
										  IMAP4_LISTECFN		lpfnECfn,
										  LPVOID					pArg,
										  LPTSTR					lpszRspBuf,
										  const UINT32			ulMaxRspLen,
										  const UINT32			ulRspTimeout);

extern EXC_TYPE imap4MBRefSync (ISockioInterface&	SBSock,
										  LPCTSTR				lpszITag,	// NULL == auto generate
										  LPCTSTR				lpszCmd,		// LIST/LSUB
										  LPCTSTR				lpszRef,
										  LPCTSTR				lpszMbox,
										  IMAP4_LISTECFN		lpfnECfn,
										  LPVOID					pArg,
										  const UINT32			ulRspTimeout);

inline EXC_TYPE imap4ListSync (ISockioInterface&	SBSock,
										 LPCTSTR					lpszITag,	// NULL == auto generate
										 LPCTSTR					lpszRef,
										 LPCTSTR					lpszMbox,
										 IMAP4_LISTECFN		lpfnECfn,
										 LPVOID					pArg,
										 const UINT32			ulRspTimeout)
{
	return imap4MBRefSync(SBSock, lpszITag, szIMAP4ListCmd, lpszRef, lpszMbox, lpfnECfn, pArg, ulRspTimeout);
}

inline EXC_TYPE imap4ListSync (ISockioInterface&	SBSock,
										 LPCTSTR					lpszITag,	// NULL == auto generate
										 LPCTSTR					lpszRef,
										 LPCTSTR					lpszMbox,
										 IMAP4_LISTECFN		lpfnECfn,
										 LPVOID					pArg,
										 LPTSTR					lpszRspBuf,
										 const UINT32			ulMaxRspLen,
										 const UINT32			ulRspTimeout)
{
	return imap4MBRefSync(SBSock, lpszITag, szIMAP4ListCmd, lpszRef, lpszMbox, lpfnECfn, pArg, lpszRspBuf, ulMaxRspLen, ulRspTimeout);
}

inline EXC_TYPE imap4LSubSync (ISockioInterface&	SBSock,
										 LPCTSTR					lpszITag,	// NULL == auto generate
										 LPCTSTR					lpszRef,
										 LPCTSTR					lpszMbox,
										 IMAP4_LISTECFN		lpfnECfn,
										 LPVOID					pArg,
										 const UINT32			ulRspTimeout)
{
	return imap4MBRefSync(SBSock, lpszITag, szIMAP4LSUBCmd, lpszRef, lpszMbox, lpfnECfn, pArg, ulRspTimeout);
}
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
typedef struct {
	LPCTSTR				lpszFolder;
	TCHAR					chSep;
	IMAP4_FLDRFLAGS	flags;
} IMAP4_FOLDERPROPS, *LPIMAP4FOLDERPROPS;

typedef EXC_TYPE (*IMAP4_FPSET_ENUM_CFN)(const IMAP4_FOLDERPROPS& fprops,
													  LPVOID							pArg,
													  BOOLEAN&						fContEnum);

class CIMAP4FoldersPropsSet {
	private:
		CStr2PtrMapper	m_FoldersMap;
		CVSDCollection	m_FoldersProps;

		// disable copy constructor and assignment operator
		CIMAP4FoldersPropsSet (const CIMAP4FoldersPropsSet& );
		CIMAP4FoldersPropsSet& operator= (const CIMAP4FoldersPropsSet& );

	public:
		enum { DEF_IMAP4_FPSET_ISIZE=8, DEF_IMAP4_FPSET_GROWSIZSE=4 };

		// also default constructor
		CIMAP4FoldersPropsSet (const UINT32 ulInitialSize=DEF_IMAP4_FPSET_ISIZE, const UINT32 ulGrowSize=DEF_IMAP4_FPSET_GROWSIZSE);

		EXC_TYPE AddFolder (const IMAP4_FOLDERPROPS& fprops);

		EXC_TYPE AddFolder (LPCTSTR						lpszFolder,
								  const TCHAR					chSep,
								  const IMAP4_FLDRFLAGS&	flags);

		EXC_TYPE EnumFoldersProps (IMAP4_FPSET_ENUM_CFN lpfnEcfn, LPVOID pArg) const;

		EXC_TYPE FindFolderProps (LPCTSTR lpszFolder, LPIMAP4FOLDERPROPS& pProps) const;

		UINT32 GetSize () const
		{
			return m_FoldersProps.GetSize();
		}

		void Reset ()
		{
			m_FoldersMap.Reset();
			m_FoldersProps.Reset();
		}

		virtual ~CIMAP4FoldersPropsSet  ()
		{
		}
};

extern EXC_TYPE imap4GetFoldersProps (ISockioInterface&			SBSock,
												  LPCTSTR						lpszITag,	// NULL == auto generate
												  LPCTSTR						lpszRef,
												  LPCTSTR						lpszMbox,
												  CIMAP4FoldersPropsSet&	fpSet,
												  const UINT32					ulRspTimeout);

inline EXC_TYPE imap4GetAllFoldersProps (ISockioInterface&			SBSock,
													  LPCTSTR						lpszITag,	// NULL == auto generate
													  CIMAP4FoldersPropsSet&	fpSet,
													  const UINT32					ulRspTimeout)
{
	return imap4GetFoldersProps(SBSock, lpszITag, _T(""), _T("*"), fpSet, ulRspTimeout);
}
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
extern EXC_TYPE imap4FolderStatusSync (ISockioInterface&			SBSock,
													LPCTSTR						lpszITag,	// NULL == auto generate
													LPCTSTR						lpszFolder,
													const IMAP4STATUSFLAGS&	stFlags,
													IMAP4STATUSINFO&			fInfo,
													LPTSTR						lpszRspBuf,
													const UINT32				ulMaxRspLen,
													const UINT32				ulRspTimeout);

extern EXC_TYPE imap4FolderStatusSync (ISockioInterface&			SBSock,
													LPCTSTR						lpszITag,	// NULL == auto generate
													LPCTSTR						lpszFolder,
													const IMAP4STATUSFLAGS&	stFlags,
													IMAP4STATUSINFO&			fInfo,
													const UINT32				ulRspTimeout);
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
// EXPUNG response are "* Num Opcode" where Opcode can be EXPUNGE, EXISTS, RECENT, etc.
typedef EXC_TYPE (*IMAP4_XPNG_HCFN)(ISockioInterface&	SBSock,
												const UINT32		ulMsgID,
												LPCTSTR				lpszOpcode,
												LPVOID				pArg,
												BOOLEAN&				fContEnum);

extern EXC_TYPE imap4ExpungeSync (ISockioInterface&	SBSock,
											 LPCTSTR					lpszITag,	// NULL == auto-generate
											 IMAP4_XPNG_HCFN		lpfnXcfn,	// may be NULL
											 LPVOID					pArg,
											 const UINT32			ulRspTimeout);
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

typedef enum {
	IMAP4STOREADDMODE,	/* +FLAGS */
	IMAP4STORESETMODE,	/*  FLAGS */
	IMAP4STOREDELMODE,	/* -FLAGS */
	IMAP4STOREBADMODE
} IMAP4STOREMODIFIERCASE;

#define fIsBadIMAP4StoreMode(m)	(((unsigned) (m)) >= IMAP4STOREBADMODE)

typedef struct {
	IMAP4STOREMODIFIERCASE	eModCase;
	BOOLEAN						m_fIsUID;
	BOOLEAN						m_fSilent;
} IMAP4STORECMDFLAGS, *LPIMAP4STORECMDFLAGS;

#define MAX_IMAP4_STORE_MSGSET_LEN	\
	(MAX_SOCK_CMDF_LINE_LEN - MAX_IMAP4_TAG_LEN - (2 * MAX_IMAP4_OPCODE_LEN) - MAX_IMAP4_FLAGS_ENCLEN - 8)

#ifdef __cplusplus
typedef EXC_TYPE (*IMAP4_STORE_HCFN)(ISockioInterface&		SBSock,
												 const UINT32				ulMsgID,	// changed msg ID
												 const IMAP4_MSGFLAGS&	flags,	// new flags
												 LPVOID						pArg,
												 BOOLEAN&					fContEnum);

extern EXC_TYPE imap4StoreMsgsFlagsSync (ISockioInterface&				SBSock,
													  LPCTSTR							lpszITag,	// NULL == auto-generate
													  LPCTSTR							lpszMsgSet,	// NULL/empty == ALL
													  const IMAP4STORECMDFLAGS&	cmdMode,
													  LPCTSTR							lpszStoreFlags,	// may be NULL/empty
													  IMAP4_STORE_HCFN				lpfnHcfn,	// may be NULL
													  LPVOID								pArg,
													  const UINT32						ulRspTimeout);

extern EXC_TYPE imap4StoreMsgFlagsSync (ISockioInterface&			SBSock,
													 LPCTSTR							lpszITag,	// NULL == auto-generate
													 const UINT32					ulMsgID,
													 const IMAP4STORECMDFLAGS&	cmdMode,
													 LPCTSTR							lpszStoreFlags,	// may be NULL/empty
													 IMAP4_STORE_HCFN				lpfnHcfn,	// may be NULL
													 LPVOID							pArg,
													 const UINT32					ulRspTimeout);

extern EXC_TYPE imap4StoreMsgsSync (ISockioInterface&				SBSock,
												LPCTSTR							lpszITag,	// NULL == auto-generate
												LPCTSTR							lpszMsgSet,
												const IMAP4STORECMDFLAGS&	cmdMode,
												const IMAP4_MSGFLAGS&		flags,
												IMAP4_STORE_HCFN				lpfnHcfn,	// may be NULL
												LPVOID							pArg,
												const UINT32					ulRspTimeout);

inline EXC_TYPE imap4StoreSetSync (ISockioInterface&				SBSock,
											  LPCTSTR							lpszITag,	// NULL == auto-generate
											  const CIMAP4MsgSetParser&	msgSet,
											  const IMAP4STORECMDFLAGS&	cmdMode,
											  const IMAP4_MSGFLAGS&			flags,
											  IMAP4_STORE_HCFN				lpfnHcfn,	// may be NULL
											  LPVOID								pArg,
											  const UINT32						ulRspTimeout)
{
	return imap4StoreMsgsSync(SBSock, lpszITag, msgSet.GetMsgSetEncoding(), cmdMode, flags, lpfnHcfn, pArg, ulRspTimeout);
}

extern EXC_TYPE imap4StoreMsgSync (ISockioInterface&				SBSock,
											  LPCTSTR							lpszITag,	// NULL == auto-generate
											  const UINT32						ulMsgID,
											  const IMAP4STORECMDFLAGS&	cmdMode,
											  const IMAP4_MSGFLAGS&			flags,
											  IMAP4_STORE_HCFN				lpfnHcfn,	// may be NULL
											  LPVOID								pArg,
											  const UINT32						ulRspTimeout);

extern EXC_TYPE imap4StoreRangeSync (ISockioInterface&			SBSock,
												 LPCTSTR							lpszITag,	// NULL == auto-generate
												 const UINT32					ulStartID,
												 const UINT32					ulEndID,
												 const IMAP4STORECMDFLAGS&	cmdMode,
												 const IMAP4_MSGFLAGS&		flags,
												 IMAP4_STORE_HCFN				lpfnHcfn,	// may be NULL
												 LPVOID							pArg,
												 const UINT32					ulRspTimeout);

inline EXC_TYPE imap4StoreMsgFlagsSync (ISockioInterface&			SBSock,
													 LPCTSTR							lpszITag,	// NULL == auto-generate
													 const UINT32					ulMsgID,
													 const IMAP4STORECMDFLAGS&	cmdMode,
													 const IMAP4_MSGFLAGS&		flags,
													 IMAP4_STORE_HCFN				lpfnHcfn,	// may be NULL
													 LPVOID							pArg,
													 const UINT32					ulRspTimeout)
{
	return imap4StoreRangeSync(SBSock, lpszITag, ulMsgID, ulMsgID, cmdMode, flags, lpfnHcfn, pArg, ulRspTimeout);
}
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
extern EXC_TYPE imap4DeleteMsgsSync (ISockioInterface&	SBSock,
												 LPCTSTR					lpszITag,	// NULL == auto-generate
												 LPCTSTR					lpszMsgSet,
												 const BOOLEAN			fIsUID,
												 const BOOLEAN			fSilent,
												 IMAP4_STORE_HCFN		lpfnHcfn,	// may be NULL
												 LPVOID					pArg,
												 const UINT32			ulRspTimeout);

extern EXC_TYPE imap4DeleteRangeSync (ISockioInterface&	SBSock,
												  LPCTSTR				lpszITag,	// NULL == auto-generate
												  const UINT32			ulStartID,
												  const UINT32			ulEndID,
												  const BOOLEAN		fIsUID,
												  const BOOLEAN		fSilent,
												  IMAP4_STORE_HCFN	lpfnHcfn,	// may be NULL
												  LPVOID					pArg,
												  const UINT32			ulRspTimeout);

inline EXC_TYPE imap4DeleteMsgSync (ISockioInterface&	SBSock,
												LPCTSTR				lpszITag,	// NULL == auto-generate
												const UINT32		ulMsgID,
												const BOOLEAN		fIsUID,
												const BOOLEAN		fSilent,
												IMAP4_STORE_HCFN	lpfnHcfn,	// may be NULL
												LPVOID				pArg,
												const UINT32		ulRspTimeout)
{
	return imap4DeleteRangeSync(SBSock, lpszITag, ulMsgID, ulMsgID, fIsUID, fSilent, lpfnHcfn, pArg, ulRspTimeout);
}

inline EXC_TYPE imap4DeleteSetSync (ISockioInterface&				SBSock,
												LPCTSTR							lpszITag,	// NULL == auto-generate
												const CIMAP4MsgSetParser&	msgSet,
												const BOOLEAN					fIsUID,
												const BOOLEAN					fSilent,
												IMAP4_STORE_HCFN				lpfnHcfn,	// may be NULL
												LPVOID							pArg,
												const UINT32					ulRspTimeout)
{
	return imap4DeleteMsgsSync(SBSock, lpszITag, msgSet.GetMsgSetEncoding(), fIsUID, fSilent, lpfnHcfn, pArg, ulRspTimeout);
}
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
extern EXC_TYPE imap4CopyMsgsSync (ISockioInterface&	SBSock,
											  LPCTSTR				lpszITag,	// NULL == auto-generate
											  const BOOLEAN		fIsUID,
											  LPCTSTR				lpszMsgSet,
											  LPCTSTR				lpszDstFolder,
											  LPTSTR					lpszRspBuf,
											  const UINT32			ulMaxRspLen,
											  const UINT32			ulRspTimeout);

extern EXC_TYPE imap4CopyMsgsSync (ISockioInterface&	SBSock,
											  LPCTSTR				lpszITag,	// NULL == auto-generate
											  const BOOLEAN		fIsUID,
											  LPCTSTR				lpszMsgSet,
											  LPCTSTR				lpszDstFolder,
											  const UINT32			ulRspTimeout);

inline EXC_TYPE imap4CopySetSync (ISockioInterface&			SBSock,
											 LPCTSTR							lpszITag,	// NULL == auto-generate
											 const BOOLEAN					fIsUID,
											 const CIMAP4MsgSetParser&	msgSet,
											 LPCTSTR							lpszDstFolder,
											 const UINT32					ulRspTimeout)
{
	return imap4CopyMsgsSync(SBSock, lpszITag, fIsUID, msgSet.GetMsgSetEncoding(), lpszDstFolder, ulRspTimeout);
}

extern EXC_TYPE imap4CopyMsgSync (ISockioInterface&	SBSock,
											 LPCTSTR					lpszITag,	// NULL == auto-generate
											 const BOOLEAN			fIsUID,
											 const UINT32			ulMsgID,
											 LPCTSTR					lpszDstFolder,
											 const UINT32			ulRspTimeout);

extern EXC_TYPE imap4CopyRangeSync (ISockioInterface&	SBSock,
												LPCTSTR				lpszITag,	// NULL == auto-generate
												const BOOLEAN		fIsUID,
												const UINT32		ulStartID,
												const UINT32		ulEndID,
												LPCTSTR				lpszDstFolder,
												const UINT32		ulRspTimeout);

extern EXC_TYPE imap4SearchMsgsSync (ISockioInterface&	SBSock,
												 LPCTSTR					lpszITag,	// NULL == auto generate
												 const BOOLEAN			fIsUID,
												 LPCTSTR					lpszCharset,	// may be NULL/empty
												 LPCTSTR					lpszCriteria,
												 IMAP4_SRES_CFN		lpfnScfn,
												 LPVOID					pArg,
												 LPTSTR					lpszRspBuf,
												 const UINT32			ulMaxRspLen,
												 const UINT32			ulRspTimeout);

extern EXC_TYPE imap4SearchMsgsSync (ISockioInterface&	SBSock,
												 LPCTSTR					lpszITag,	// NULL == auto generate
												 const BOOLEAN			fIsUID,
												 LPCTSTR					lpszCharset,	// may be NULL/empty
												 LPCTSTR					lpszCriteria,
												 IMAP4_SRES_CFN		lpfnScfn,
												 LPVOID					pArg,
												 const UINT32			ulRspTimeout);
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
// ulReadLen should contain actual number of bytes.
// EOF if assumed if read len < buf len or if EEOF returned.
typedef EXC_TYPE (*IMAP4_APPDATA_CFN)(LPVOID pArg, LPTSTR lpszBuf, const UINT32 ulBufLen, UINT32& ulReadLen);

extern EXC_TYPE imap4AppendSync (ISockioInterface&	SBSock,
											LPCTSTR				lpszITag,
											LPCTSTR				lpszFolder,
											LPCTSTR				lpszFlags,	// may be NULL/empty
											LPCTSTR				lpszIDate,	// may be NULL/empty
											const UINT32		ulDataLen,
											IMAP4_APPDATA_CFN	lpfnDcfn,// data fetch callback
											LPVOID				pArg,
											LPTSTR				lpszRspBuf,
											const UINT32		ulRspBufLen,
											const UINT32		ulRspTimeout);

extern EXC_TYPE imap4AppendSync (ISockioInterface&	SBSock,
											LPCTSTR				lpszITag,
											LPCTSTR				lpszFolder,
											LPCTSTR				lpszFlags,	// may be NULL/empty
											const struct tm	*pTM,		// NULL == none
											const int			tmZone,	// (-1) == use default (only if non NULL date)
											const UINT32		ulDataLen,
											IMAP4_APPDATA_CFN	lpfnDcfn,// data fetch callback
											LPVOID				pArg,
											LPTSTR				lpszRspBuf,
											const UINT32		ulRspBufLen,
											const UINT32		ulRspTimeout);

extern EXC_TYPE imap4AppendSync (ISockioInterface&	SBSock,
											LPCTSTR				lpszITag,
											LPCTSTR				lpszFolder,
											LPCTSTR				lpszFlags,	// may be NULL/empty
											const struct tm	*pTM,		// NULL == none
											const int			tmZone,	// (-1) == use default (only if non NULL date)
											const UINT32		ulDataLen,
											IMAP4_APPDATA_CFN	lpfnDcfn,// data fetch callback
											LPVOID				pArg,
											const UINT32		ulRspTimeout);

extern EXC_TYPE imap4AppendSync (ISockioInterface&	SBSock,
											LPCTSTR				lpszITag,
											LPCTSTR				lpszFolder,
											LPCIMAP4MSGFLAGS	pFlags,	// NULL == empty
											const struct tm	*pTM,		// NULL == none
											const int			tmZone,	// (-1) == use default (only if non NULL date)
											const UINT32		ulDataLen,
											IMAP4_APPDATA_CFN	lpfnDcfn,// data fetch callback
											LPVOID				pArg,
											LPTSTR				lpszRspBuf,
											const UINT32		ulRspBufLen,
											const UINT32		ulRspTimeout);

extern EXC_TYPE imap4AppendSync (ISockioInterface&	SBSock,
											LPCTSTR				lpszITag,
											LPCTSTR				lpszFolder,
											LPCIMAP4MSGFLAGS	pFlags,	// NULL == empty
											const struct tm	*pTM,		// NULL == none
											const int			tmZone,	// (-1) == use default (only if non NULL date)
											const UINT32		ulDataLen,
											IMAP4_APPDATA_CFN	lpfnDcfn,// data fetch callback
											LPVOID				pArg,
											const UINT32		ulRspTimeout);

extern EXC_TYPE imapAppendFPSync (ISockioInterface&	SBSock,
											 LPCTSTR					lpszITag,
											 FILE						*fp,
											 const UINT32			ulFSize,	// as received from "stat"
											 LPCTSTR					lpszFolder,
											 LPCIMAP4MSGFLAGS		pFlags,	// NULL == empty
											 const struct tm		*pTM,		// NULL == none
											 const int				tmZone,	// (-1) == use default (only if non NULL date)
											 LPTSTR					lpszRspBuf,
											 const UINT32			ulRspBufLen,
											 const UINT32			ulRspTimeout);

extern EXC_TYPE imapAppendFPSync (ISockioInterface&	SBSock,
											 LPCTSTR					lpszITag,
											 FILE						*fp,
											 const UINT32			ulFSize,	// as received from "stat"
											 LPCTSTR					lpszFolder,
											 LPCIMAP4MSGFLAGS		pFlags,	// NULL == empty
											 const struct tm		*pTM,		// NULL == none
											 const int				tmZone,	// (-1) == use default (only if non NULL date)
											 const UINT32			ulRspTimeout);

extern EXC_TYPE imap4AppendFileSync (ISockioInterface&	SBSock,
												 LPCTSTR					lpszITag,
												 LPCTSTR					lpszFolder,
												 LPCTSTR					lpszMsgFile,
												 LPCIMAP4MSGFLAGS		pFlags,	// NULL == empty
												 const struct tm		*pTM,		// NULL == none
												 const int				tmZone,	// (-1) == use default (only if non NULL date)
												 LPTSTR					lpszRspBuf,
												 const UINT32			ulRspBufLen,
												 const UINT32			ulRspTimeout);

extern EXC_TYPE imap4AppendFileSync (ISockioInterface&	SBSock,
												 LPCTSTR					lpszITag,
												 LPCTSTR					lpszFolder,
												 LPCTSTR					lpszMsgFile,
												 LPCIMAP4MSGFLAGS		pFlags,	// NULL == empty
												 const struct tm		*pTM,		// NULL == none
												 const int				tmZone,	// (-1) == use default (only if non NULL date)
												 const UINT32			ulRspTimeout);
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
// class with static IMAP4 FETCH response data
class CIMAP4FetchCfnData {
	public:
		CIMAP4FetchCfnData () { }

		virtual ISockioInterface& GetServerConn () const = 0;

		virtual UINT32 GetMsgSeqNo () const = 0;

		// Note: may differ from callback modifier for embedded messages - e.g.
		//			callback modifier may be ENVELOPE, but original modifier will
		//			still be BODY/BODYSTRUCTURE for the embedded message headers part.
		virtual LPCTSTR GetModifier () const = 0;

		// Note: returns non-NULL only for BODY/BODYSTRUCTURE modifier(s)
		virtual LPCTSTR GetMsgPart () const = 0;

		virtual LPVOID GetCallbackArg () const = 0;

		virtual ~CIMAP4FetchCfnData () { }
};

/*
 *		FetchRsp				modifier	lpszSubHdr	lpModVal
 *		--------				--------	----------	--------
 *		ENVELOPE							To:/Cc:/...	LPCTSTR
 *		FLAGS								NULL			LPIMAP4MSGFLAGS
 *		FLAGS					SILENT	raw flag		UINT32=length of data in raw flag
 *		INTERNALDATE					NULL			LPCTSTR
 *		RFC822.SIZE						NULL			UINT32
 *		UID								NULL			UINT32
 *		BODY								section...	LPCTSTR	Note: if keyword="X-Body:" (see szXBody
 *																	definition in rfc822.h) then "lpModVal"
 *																	is data fetch
 *		BODYSTRUCTURE					rfc822 hdr	LPCTSTR
 */
typedef EXC_TYPE (*IMAP4_FETCHRSP_CFN)(const CIMAP4FetchCfnData&	ftchData,
													LPCTSTR							lpszFetchRsp,
													LPCTSTR							lpszSubHdr,		// valid only for complex structures
													LPCTSTR							lpszKeyword,	// non-NULL for "keyword=value" pair(s)
													LPVOID							lpModVal);		// actual type depends on modifier

extern EXC_TYPE imap4HandleFetchRspSync (ISockioInterface&	SBSock,
													  LPCTSTR				lpszTag,
													  IMAP4_FETCHRSP_CFN	lpfnHcfn,
													  LPVOID					pArg,
													  LPTSTR					lpszRspBuf,
													  const UINT32			ulMaxRspLen,
													  const UINT32			ulRspTimeout);

extern EXC_TYPE imap4HandleFetchRspSync (ISockioInterface&	SBSock,
													  LPCTSTR				lpszTag,
													  IMAP4_FETCHRSP_CFN	lpfnHcfn,
													  LPVOID					pArg,
													  const UINT32			ulRspTimeout);
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
// class to hold ENVELOPE response data
class CIMAP4EnvelopeData : public CRFC822EnvelopeData {
	public:
		// Note: zero causes undefined behavior !!!
		CIMAP4EnvelopeData (const UINT32 ulAvgRcipsNum=DEFAULT_RFC822_RCIPS_NUM)
			: CRFC822EnvelopeData(ulAvgRcipsNum) { }

		// copy constructor
		CIMAP4EnvelopeData (const CIMAP4EnvelopeData& ed)
			: CRFC822EnvelopeData(ed) { }

		// this method can be called from the FETCH processing callback
		EXC_TYPE ProcessFetchRsp (LPCTSTR lpszSubHdr, LPVOID lpModVal)
		{
			return ProcessHdrData(lpszSubHdr, (LPCTSTR) lpModVal);
		}

		virtual ~CIMAP4EnvelopeData () { }
};

typedef CIMAP4EnvelopeData *LPIMAP4ENVELOPE;
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
class CIMAP4BodyStruct;
typedef CIMAP4BodyStruct *LPIMAP4BODYSTRUCT;

//		callback for enumerating a body structure - if non-EOK code returned then
// enumeration is aborted (and returned code is propagated)
typedef EXC_TYPE (*IMAP4_BODYSTRUCTENUM_CFN)(const CIMAP4BodyStruct& bs,
															LPVOID						pArg,
															BOOLEAN&						fContEnum);

// class for holding the complete message structure description
class CIMAP4BodyStruct {
	private:
		UINT32			m_ulAvgCNum;
		CAttachInfo		m_StructInfo;	// name, part, MIME content type, charset, encoding
		CVSDCollection	m_Contents;		// empty for "pure" attachments

		// disable copy constructor and assignment operator
		CIMAP4BodyStruct (const CIMAP4BodyStruct& );
		CIMAP4BodyStruct& operator= (const CIMAP4BodyStruct& );

		EXC_TYPE GetCurrentStruct (LPCTSTR	lpszMsgPart, LPIMAP4BODYSTRUCT& pBS);

		EXC_TYPE UpdateMsgPart (LPCTSTR	lpszMsgPart,
										LPCTSTR	lpszSubHdr,
										LPCTSTR	lpszKeyword,
										LPCTSTR	lpszValue);

		EXC_TYPE EnumContents (IMAP4_BODYSTRUCTENUM_CFN lpfnEcfn, LPVOID pArg, BOOLEAN& fContEnum) const;

	public:
		enum { AVG_IMAP4_ATTS_PER_MSG=4 };

		// Note: zero causes undefined behavior !!!
		CIMAP4BodyStruct (const UINT32 ulAvgContentsNum=AVG_IMAP4_ATTS_PER_MSG);

		// Note: resets the object first
		EXC_TYPE UpdateBodyStruct (const CIMAP4BodyStruct& bs);

		const CAttachInfo& GetInfo () const { return m_StructInfo; }
		BOOLEAN IsMultipartMsg () const;
		BOOLEAN HasAttachments () const;
		UINT32 GetContentsNum () const { return m_Contents.GetSize(); }

		// Note: returns NULL if out of range
		EXC_TYPE GetContentInfo (const UINT32 ulIdx, LPIMAP4BODYSTRUCT& pInfo) const;
		LPIMAP4BODYSTRUCT operator[] (const UINT32 ulIdx) const;

		// Note: 1st call is with "this"
		EXC_TYPE EnumContents (IMAP4_BODYSTRUCTENUM_CFN lpfnEcfn, LPVOID pArg) const;

		EXC_TYPE ProcessMsgPart (LPCTSTR	lpszMsgPart,
										 LPCTSTR	lpszSubHdr,
										 LPCTSTR	lpszKeyword,
										 LPCTSTR	lpszValue);

		EXC_TYPE ProcessResponse (const CIMAP4FetchCfnData&	ftchData,
										  LPCTSTR							lpszSubHdr,
										  LPCTSTR							lpszKeyword,
										  LPCTSTR							lpszValue)
		{
			return ProcessMsgPart(ftchData.GetMsgPart(), lpszSubHdr, lpszKeyword, lpszValue);
		}

		void Reset ();

		virtual ~CIMAP4BodyStruct () { Reset(); }
};
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

typedef struct {
	UINT32			ulMsgSeqNo;
	UINT32			ulMsgUID;
	UINT32			ulMsgSize;
	IMAP4_MSGFLAGS	flags;
	struct tm		iDate;
	int				tmZone;
} IMAP4FASTMSGINFO, *LPIMAP4FASTMSGINFO;

#ifdef __cplusplus
extern EXC_TYPE imap4FetchMsgRangeFastInfo (ISockioInterface&	SBSock,
														  LPCTSTR				lpszITag,			// NULL == auto-generate
														  const BOOLEAN		fIsUIDFetch,
														  LPCTSTR				lpszMsgRange,		// NULL == "1:*"
														  IMAP4FASTMSGINFO	fmInfo[],
														  const UINT32			ulMaxInfos,
														  UINT32&				ulInfosNum,			// (OUT) actually fetched
														  const UINT32			ulRspTimeout);

extern EXC_TYPE imap4FetchMsgFastInfo (ISockioInterface&	SBSock,
													LPCTSTR				lpszITag,			// NULL == auto-generate
													const BOOLEAN		fIsUIDFetch,
													const UINT32		ulMsgId,
													IMAP4FASTMSGINFO&	fmInfo,
													const UINT32		ulRspTimeout);

extern EXC_TYPE imap4FetchMsgRangeSync (ISockioInterface&	SBSock,
													 LPCTSTR					lpszITag,			// NULL == auto-generate
													 const BOOLEAN			fIsUIDFetch,
													 LPCTSTR					lpszMsgRange,		// NULL == "1:*"
													 LPCTSTR					lpszModifiers[],	// last member must be NULL
													 IMAP4_FETCHRSP_CFN	lpfnHcfn,
													 LPVOID					pArg,
													 const UINT32			ulRspTimeout);

extern EXC_TYPE imap4FetchMsgSync (ISockioInterface&	SBSock,
											  LPCTSTR				lpszITag,			// NULL == auto-generate
											  const BOOLEAN		fIsUIDFetch,
											  const UINT32			ulMsgID,
											  LPCTSTR				lpszModifiers[],	// last member must be NULL
											  IMAP4_FETCHRSP_CFN	lpfnHcfn,
											  LPVOID					pArg,
											  const UINT32			ulRspTimeout);

extern EXC_TYPE imap4FetchMsgBodySync (ISockioInterface&		SBSock,
													LPCTSTR					lpszITag,			// NULL == auto-generate
													const BOOLEAN			fIsUIDFetch,
													const UINT32			ulMsgID,
													IMAP4_FETCHRSP_CFN	lpfnHcfn,
													LPVOID					pArg,
													const UINT32			ulRspTimeout);
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
class CIMAP4FullMsgInfo {
	private:
		IMAP4FASTMSGINFO		m_FastInfo;
		CIMAP4EnvelopeData	m_Envelope;
		CIMAP4BodyStruct		m_BodyStruct;
		CIncStrlBuilder		m_rawFlags;

		struct {
			unsigned m_fHaveSeqNo		:	1;
			unsigned m_fHaveUID			:	1;
			unsigned m_fHaveSize			:	1;
			unsigned m_fHaveFlags		:	1;
			unsigned m_fHaveIDate		:	1;
			unsigned m_fHaveEnvelope	:	1;
			unsigned m_fHaveBodyStruct	:	1;
		} m_auxFlags;

		// disable copy constructor and assignment operator
		CIMAP4FullMsgInfo (const CIMAP4FullMsgInfo& );
		CIMAP4FullMsgInfo& operator= (const CIMAP4FullMsgInfo& );

	public:
		CIMAP4FullMsgInfo (const UINT32 ulAvgRcipsNum=CRFC822EnvelopeData::DEFAULT_RFC822_RCIPS_NUM,
								 const UINT32 ulAvgContentsNum=CIMAP4BodyStruct::AVG_IMAP4_ATTS_PER_MSG);

		BOOLEAN HaveMsgSeqNo () const { return (m_auxFlags.m_fHaveSeqNo != 0); }
		BOOLEAN HaveMsgUID () const { return (m_auxFlags.m_fHaveUID != 0); }
		BOOLEAN HaveMsgSize () const { return (m_auxFlags.m_fHaveSize != 0); }
		BOOLEAN HaveMsgFlags () const { return (m_auxFlags.m_fHaveFlags != 0); }
		BOOLEAN HaveMsgRawFlags () const { return (!IsEmptyStr(m_rawFlags)); }
		BOOLEAN HaveMsgInternalDate () const { return (m_auxFlags.m_fHaveIDate != 0); }
		BOOLEAN HaveMsgEnvelope () const { return (m_auxFlags.m_fHaveEnvelope != 0); }
		BOOLEAN HaveMsgBodyStructure () const { return (m_auxFlags.m_fHaveBodyStruct != 0); }

		UINT32 GetMsgSeqNo () const { return m_FastInfo.ulMsgSeqNo; }
		UINT32 GetMsgUID () const { return m_FastInfo.ulMsgUID; }
		UINT32 GetMsgSize () const {	return m_FastInfo.ulMsgSize; }
		const IMAP4_MSGFLAGS& GetMsgFlags () const { return m_FastInfo.flags; }
		// NOTE: the list (if not empty) is NOT delimited by '()'
		const LPCTSTR GetMsgRawFlags () const { return m_rawFlags; }
		const struct tm& GetMsgInternalDate () const { return m_FastInfo.iDate; }
		int GetMsgTimeZone () const { return m_FastInfo.tmZone; }
		const IMAP4FASTMSGINFO& GetMsgFastInfo () const { return m_FastInfo; }
		const CIMAP4EnvelopeData& GetMsgEnvelope () const { return m_Envelope; }
		const CIMAP4BodyStruct& GetMsgBodyStructure () const { return m_BodyStruct; }

		void ResetSize ()
		{
			m_FastInfo.ulMsgSize = 0;
			m_auxFlags.m_fHaveSize = 0;
		}

		// NOTE: also resets the raw flags
		void ResetFlags ()
		{
			memset(&m_FastInfo.flags, 0, (sizeof m_FastInfo.flags));
			m_auxFlags.m_fHaveFlags = 0;
			m_rawFlags.Reset();
		}

		void ResetInternalDate ()
		{
			memset(&m_FastInfo.iDate, 0, (sizeof m_FastInfo.iDate));
			m_FastInfo.tmZone = 0;
			m_auxFlags.m_fHaveIDate = 0;
		}

		void ResetFastInfo ();

		void ResetEnvelope ()
		{
			m_Envelope.Clear();
			m_auxFlags.m_fHaveEnvelope = 0;
		}

		void ResetBodyStructure ()
		{
			m_BodyStruct.Reset();
			m_auxFlags.m_fHaveBodyStruct = 0;
		}

		void Reset ();

		// Note: resets object before updating
		EXC_TYPE UpdateMsgInfo (const CIMAP4FullMsgInfo& mi);

		EXC_TYPE HandleFetchResponse (const CIMAP4FetchCfnData&	ftchData,
												LPCTSTR							lpszModifier,
												LPCTSTR							lpszSubHdr,		// valid only for complex structures
												LPCTSTR							lpszKeyword,	// non-NULL for "keyword=value" pair(s)
												LPVOID							lpModVal);		// actual type depends on modifier

		virtual ~CIMAP4FullMsgInfo () { Reset(); }
};

typedef CIMAP4FullMsgInfo	*LPIMAP4FULLMSGINFO;

extern EXC_TYPE imap4FetchMsgFullInfoSync (ISockioInterface&	SBSock,
														 LPCTSTR					lpszITag,			// NULL == auto-generate
														 const BOOLEAN			fIsUIDFetch,
														 const UINT32			ulMsgID,
														 CIMAP4FullMsgInfo&	msgInfo,
														 const UINT32			ulRspTimeout);
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
extern EXC_TYPE imap4ClearMsgsInfo (CVSDCollection&	msgsInfo);	// each member is a LPIMAP4FULLMSGINFO

// class for auto clear of msgs info set
class CIMAP4MsgsInfoGuard {
	private:
		CVSDCollection&	m_MsgsInfo;

	public:
		CIMAP4MsgsInfoGuard (CVSDCollection&	msgsInfo) : m_MsgsInfo(msgsInfo) { }
		virtual ~CIMAP4MsgsInfoGuard () { imap4ClearMsgsInfo(m_MsgsInfo); }
};

// Note(s):
//
//	a. clears the data collection before fetching the items
// b. caller must delete allocated msgs info in data collection (see "imap4ClearMsgsInfo")
extern EXC_TYPE imap4FetchMsgRangeInfo (ISockioInterface&	SBSock,
													 LPCTSTR					lpszITag,			// NULL == auto-generate
													 const BOOLEAN			fIsUIDFetch,
													 LPCTSTR					lpszMsgRange,		// NULL == "1:*"
													 LPCTSTR					lpszModifiers[],	// (NULL == full info) last member must be NULL
													 CVSDCollection&		msgsInfo,			// each member is a LPIMAP4FULLMSGINFO
													 const UINT32			ulRspTimeout);
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
typedef EXC_TYPE (*IMAP4_MSGPART_FETCH_CFN)(const UINT32						ulMsgID,
														  const BOOLEAN					fIsUIDFetch,
														  const IMAP4_BODYPART_CASE	eBodyPart,	// BAD == pure data
														  LPCTSTR							lpszMsgPart,
														  LPCTSTR							lpszPartData,
														  const UINT32						ulDataLen,
														  LPVOID								pArg);

// fetch specified message part information for all specified parts
extern EXC_TYPE imap4FetchMsgPartsData (ISockioInterface&			SBSock,
													 LPCTSTR							lpszITag,			// NULL == auto-generate
													 const UINT32					ulMsgID,
													 const BOOLEAN					fIsUIDFetch,
													 const IMAP4_BODYPART_CASE	eBodyPart,
													 LPCTSTR							lpszMsgParts[],	// last == NULL
													 IMAP4_MSGPART_FETCH_CFN	lpfnFcfn,
													 LPVOID							pArg,
													 const UINT32					ulRspTimeout);
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

typedef struct {
	unsigned m_fRetrReverse	:	1;	// if set, retrieves messages from last down
	unsigned m_fCheckUID		:	1;	// if set, queries msg UID before retrieval
	unsigned m_fCheckSize	:	1;	// if set, queries msg size before retrieval
	unsigned m_fParseMsg		:	1;	// if set, performs msg parsing during retrieval
	unsigned m_fQueryDel		:	1;	// if set, queries msg deletion AFTER retrieval
} IMAP4RELAYFLAGS, *LPIMAP4RELAYFLAGS;

typedef struct {
	LPCTSTR	lpszIMAP4Host;
	int		iIMAP4Port;		// 0 == use default
	LPCTSTR	lpszIMAP4UID;
	LPCTSTR	lpszIMAP4Passwd;
	UINT32	ulRspTimeout;
} IMAP4ACCOUNTDEF, *LPIMAP4ACCOUNTDEF;

#define MAX_IMAP4EXTACC_ENCLEN	(MAX_RCVR_NAME_LEN+MAX_DWORD_DISPLAY_LENGTH+2)

extern EXC_TYPE EncodeIMAP4ExtAcc (const IMAP4ACCOUNTDEF	*pAccDef,
											  LPTSTR						lpszEnc,
											  const UINT32				ulMaxLen);

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
// Note: protocol state reports are still POP3 commands (see <internet/rfc822msg.h>)

// does not inform stream about open conn.
extern EXC_TYPE OpenIMAP4RelayConn (const IMAP4ACCOUNTDEF&	accDef,
												ISockioInterface&			CBSock,
												IMsgRelayStream&			rlyStream);

// Note: assumes proper folder has been SELECT(ed)
extern EXC_TYPE StreamRelayIMAP4Msg (const IMAP4ACCOUNTDEF&	accDef,
												 ISockioInterface&		CBSock,
												 const UINT32				ulMsgID,
												 const BOOLEAN				fIsUID,
												 const IMAP4RELAYFLAGS&	rlyFlags,
												 IMsgRelayStream&			rlyStream);

extern EXC_TYPE StreamRelayIMAP4Conn (const IMAP4ACCOUNTDEF&	accDef,
												  ISockioInterface&			CBSock,
												  LPCTSTR						lpszStreamFolder,
												  const IMAP4RELAYFLAGS&	rlyFlags,
												  IMsgRelayStream&			rlyStream);

// Note: implicitly streams from INBOX
extern EXC_TYPE StreamRelayIMAP4Account (const IMAP4ACCOUNTDEF&	accDef,
													  const IMAP4RELAYFLAGS&	rlyFlags,
													  IMsgRelayStream&			rlyStream);

#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

#endif /* _IMAP4_LIB_H_ */
