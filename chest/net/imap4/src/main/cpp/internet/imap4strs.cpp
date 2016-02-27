#include <ctype.h>

#include <util/string.h>
#include <util/errors.h>

#include <internet/imap4Lib.h>

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
#define SZXTRN extern
#else
#define SZXTRN
#endif

/*---------------------------------------------------------------------------*/

SZXTRN const char IMAP4_OK[]="OK";
SZXTRN const char IMAP4_NO[]="NO";
SZXTRN const char IMAP4_BAD[]="BAD";

typedef struct {
	const char	*lpszRsp;
	EXC_TYPE		eCode;
} XL_RSP;

static const XL_RSP IMAP4Responses[]={
	{	IMAP4_OK,	EOK			},
	{	IMAP4_BYE,	EOK			},	/* this is actually an OK response */
	{	IMAP4_NO,	EPERMISSION	},
	{	IMAP4_BAD,	ECONTEXT		},
	{	NULL,			EFATALEXIT	}	/* mark end of list */
};

/*---------------------------------------------------------------------------*/

/* Returns an equivalent numerical value for the response as follows:
 *
 *		EOK			<-	OK
 *		EPERMISSION	<-	NO
 *		ECONTEXT		<- BAD
 *
 *	Any other code signals some other error
 */
EXC_TYPE imap4XlateRspCode (const char lpszRspCode[])
{
	const XL_RSP *pRsp=&IMAP4Responses[0];

	if (IsEmptyStr(lpszRspCode))
		return EPARAM;

	while (pRsp->lpszRsp != NULL)
		if (_tcsicmp(lpszRspCode, pRsp->lpszRsp) == 0)
			return pRsp->eCode;
		else
			pRsp++;

	return EPREPOSITION;
}

/*---------------------------------------------------------------------------*/

SZXTRN const char szIMAP4LoginCmd[]="LOGIN";
SZXTRN const char szIMAP4LogoutCmd[]="LOGOUT";
SZXTRN const char szIMAP4FetchCmd[]="FETCH";
SZXTRN const char szIMAP4CapabilityCmd[]="CAPABILITY";
SZXTRN const char szIMAP4NoopCmd[]="NOOP";
SZXTRN const char szIMAP4SelectCmd[]="SELECT";
SZXTRN const char szIMAP4ListCmd[]="LIST";
SZXTRN const char szIMAP4LSUBCmd[]="LSUB";
SZXTRN const char szIMAP4AuthCmd[]="AUTHENTICATE";
SZXTRN const char szIMAP4ExamineCmd[]="EXAMINE";
SZXTRN const char szIMAP4CreateCmd[]="CREATE";
SZXTRN const char szIMAP4DeleteCmd[]="DELETE";
SZXTRN const char szIMAP4RenameCmd[]="RENAME";
SZXTRN const char szIMAP4SubsCmd[]="SUBSCRIBE";
SZXTRN const char szIMAP4UnSubsCmd[]="UNSUBSCRIBE";
SZXTRN const char szIMAP4StatusCmd[]="STATUS";
SZXTRN const char szIMAP4AppendCmd[]="APPEND";
SZXTRN const char szIMAP4CheckCmd[]="CHECK";
SZXTRN const char szIMAP4CloseCmd[]="CLOSE";
SZXTRN const char szIMAP4XpngCmd[]="EXPUNGE";
SZXTRN const char szIMAP4SearchCmd[]="SEARCH";
SZXTRN const char szIMAP4StoreCmd[]="STORE";
SZXTRN const char szIMAP4CopyCmd[]="COPY";
SZXTRN const char szIMAP4UIDCmd[]="UID";

/*---------------------------------------------------------------------------*/

/* NULL terminated list of IMAP4 commands */
SZXTRN const char *szIMAP4Cmds[]={
	szIMAP4LoginCmd,
	szIMAP4LogoutCmd,
	szIMAP4FetchCmd,
	szIMAP4CapabilityCmd,
	szIMAP4NoopCmd,
	szIMAP4SelectCmd,
	szIMAP4ListCmd,
	szIMAP4LSUBCmd,
	szIMAP4AuthCmd,
	szIMAP4ExamineCmd,
	szIMAP4CreateCmd,
	szIMAP4DeleteCmd,
	szIMAP4RenameCmd,
	szIMAP4SubsCmd,
	szIMAP4UnSubsCmd,
	szIMAP4StatusCmd,
	szIMAP4AppendCmd,
	szIMAP4CheckCmd,
	szIMAP4CloseCmd,
	szIMAP4XpngCmd,
	szIMAP4SearchCmd,
	szIMAP4StoreCmd,
	szIMAP4CopyCmd,
	szIMAP4UIDCmd,

	NULL		/* mark end of list */
};

/*---------------------------------------------------------------------------*/

BOOLEAN IsIMAP4Cmd (const char szCmd[])
{
	UINT32	i=0;

	if (IsEmptyStr(szCmd))
		return FALSE;

	for (i=0; szIMAP4Cmds[i] != NULL; i++)
		if (_tcsicmp(szCmd, szIMAP4Cmds[i]) == 0)
			return TRUE;

	return FALSE;
}

/*---------------------------------------------------------------------------*/

/* some well known atoms */
SZXTRN const char IMAP4_NIL[]="NIL";
SZXTRN const char IMAP4_REV1[]="IMAP4rev1";
SZXTRN const char IMAP4_BYE[]="BYE";

SZXTRN const char IMAP4_EXISTS[]="EXISTS";
SZXTRN const char IMAP4_RECENT[]="RECENT";
SZXTRN const char IMAP4_UNSEEN[]="UNSEEN";
SZXTRN const char IMAP4_PERMANENTFLAGS[]="PERMANENTFLAGS";
SZXTRN const char IMAP4_UIDVALIDITY[]="UIDVALIDITY";
SZXTRN const char IMAP4_SILENT[]=".SILENT";
SZXTRN const char IMAP4_MESSAGES[]="MESSAGES";
SZXTRN const char IMAP4_INBOX[]="INBOX";
SZXTRN const char IMAP4_UIDNEXT[]="UIDNEXT";

SZXTRN const char IMAP4_ALL[]="ALL";	/* equivalent to (FLAGS INTERNALDATE RFC822.SIZE ENVELOPE) */
SZXTRN const char IMAP4_BODY[]="BODY";
SZXTRN const char IMAP4_BODYPEEK[]="BODY.PEEK";
SZXTRN const char IMAP4_BODYSTRUCT[]="BODYSTRUCTURE";
SZXTRN const char IMAP4_ENVELOPE[]="ENVELOPE";
SZXTRN const char IMAP4_FAST[]="FAST";	/* equivalent to (FLAGS INTERNALDATE RFC822.SIZE) */
SZXTRN const char IMAP4_FLAGS[]="FLAGS";
SZXTRN const char IMAP4_FULL[]="FULL";	/* equivalent to (FLAGS INTERNALDATE RFC822.SIZE ENVELOPE BODY) */
SZXTRN const char IMAP4_INTERNALDATE[]="INTERNALDATE";
SZXTRN const char IMAP4_RFC822[]="RFC822";	/* euqivalent to BODY[] */
SZXTRN const char IMAP4_RFC822HDR[]="RFC822.HEADER"; /* equivalent to BODY.PEEK[HEADER] */
SZXTRN const char IMAP4_RFC822SIZE[]="RFC822.SIZE";
SZXTRN const char IMAP4_RFC822TEXT[]="RFC822.TEXT"; /* equivalent to BODY[TEXT] */
SZXTRN const char IMAP4_UID[]="UID";

/*---------------------------------------------------------------------------*/

SZXTRN const TCHAR IMAP4_SEENFLAG[]=_T("\\Seen");
SZXTRN const TCHAR IMAP4_ANSWEREDFLAG[]=_T("\\Answered");
SZXTRN const TCHAR IMAP4_FLAGGEDFLAG[]=_T("\\Flagged");
SZXTRN const TCHAR IMAP4_DELETEDFLAG[]=_T("\\Deleted");
SZXTRN const TCHAR IMAP4_DRAFTFLAG[]=_T("\\Draft");
SZXTRN const TCHAR IMAP4_RECENTFLAG[]=_T("\\Recent");
SZXTRN const TCHAR IMAP4_PRIVATEFLAGS[]=_T("\\*");

/* NULL terminated list of all flags - DO NOT CHANGE ORDER !!! */
SZXTRN LPCTSTR IMAP4MsgFlags[]={
	IMAP4_SEENFLAG,
	IMAP4_ANSWEREDFLAG,
	IMAP4_FLAGGEDFLAG,
	IMAP4_DELETEDFLAG,
	IMAP4_DRAFTFLAG,
	IMAP4_RECENTFLAG,
	IMAP4_PRIVATEFLAGS,

	NULL		/* mark end of list */
};

/*--------------------------------------------------------------------------*/

IMAP4_MSGFLAG_CASE imap4XlateExtMsgFlag (LPCTSTR lpszFlag, const UINT32 ulFLen)
{
	UINT32		ulIdx=0;

	if (IsEmptyStr(lpszFlag) || (0 == ulFLen))
		return IMAP4_BADFLAG_MSGCASE;

	for (ulIdx=0; (ulIdx < IMAP4_MSGFLAGS_NUM) && (IMAP4MsgFlags[ulIdx] != NULL); ulIdx++)
	{
		LPCTSTR	lpszKnownFlag=IMAP4MsgFlags[ulIdx];
		UINT32	ulKLen=_tcslen(lpszKnownFlag);

		if (ulFLen != ulKLen)
			continue;
		if (_tcsnicmp(lpszFlag, lpszKnownFlag, ulFLen) != 0)
			continue;

		return (IMAP4_MSGFLAG_CASE) ulIdx;
	}

	return IMAP4_EXTENSION_FLAGS;
}

/*--------------------------------------------------------------------------*/

IMAP4_MSGFLAG_CASE imap4XlateMsgFlag (LPCTSTR lpszFlag)
{
	if (NULL == lpszFlag)
		return IMAP4_BADFLAG_MSGCASE;
	return imap4XlateExtMsgFlag(lpszFlag, _tcslen(lpszFlag));
}

/*--------------------------------------------------------------------------*/

SZXTRN const TCHAR IMAP4_NOINFERIORS_FLAG[]=_T("\\Noinferiors");
SZXTRN const TCHAR IMAP4_NOSELECT_FLAG[]=_T("\\Noselect");
SZXTRN const TCHAR IMAP4_MARKED_FLAG[]=_T("\\Marked");
SZXTRN const TCHAR IMAP4_UNMARKED_FLAG[]=_T("\\Unmarked");

/* used only if CHILDREN capability reported */
SZXTRN const TCHAR IMAP4_HASCHILDREN_FLAG[]=_T("\\HasChildren");
SZXTRN const TCHAR IMAP4_HASNOCHILDREN_FLAG[]=_T("\\HasNoChildren");

/* NULL terminated list of all folder flags  - DO NOT CHANGE ORDER !! */
SZXTRN LPCTSTR IMAP4FldrFlags[]={
	IMAP4_NOINFERIORS_FLAG,
	IMAP4_NOSELECT_FLAG,
	IMAP4_MARKED_FLAG,
	IMAP4_UNMARKED_FLAG,

/* used only if CHILDREN capability reported */
	IMAP4_HASCHILDREN_FLAG,
	IMAP4_HASNOCHILDREN_FLAG,

	NULL		/* mark end of list */
};

/*--------------------------------------------------------------------------*/

IMAP4_FLDRFLAG_CASE imap4XlateExtFldrFlag (LPCTSTR lpszFlag, const UINT32 ulFLen)
{
	UINT32	ulIdx=0;

	if (IsEmptyStr(lpszFlag) || (0 == ulFLen))
		return IMAP4_BDFLG_FLDRCASE;
	if (*lpszFlag != IMAP4_SYSFLAG_SIGN)
		return IMAP4_BDFLG_FLDRCASE;

	for (ulIdx=0; ; ulIdx++)
	{
		LPCTSTR	lpszKnownFlag=IMAP4FldrFlags[ulIdx];
		UINT32	ulKLen=0;

		if (IsEmptyStr(lpszKnownFlag))
			break;

		if (ulFLen != (ulKLen=strlen(lpszKnownFlag)))
			continue;
		if (_tcsnicmp(lpszFlag, lpszKnownFlag, ulFLen) != 0)
			continue;

		return (IMAP4_FLDRFLAG_CASE) ulIdx;
	}

	/* if all flags exhausted, assume some extension */
	return IMAP4_EXTND_FLDRCASE;
}

/*--------------------------------------------------------------------------*/

IMAP4_FLDRFLAG_CASE imap4XlateFldrFlag (LPCTSTR lpszFlag)
{
	if (NULL == lpszFlag)
		return IMAP4_BDFLG_FLDRCASE;

	return imap4XlateExtFldrFlag(lpszFlag, _tcslen(lpszFlag));
}

/*--------------------------------------------------------------------------*/

typedef struct {
	const char *lpszOrgPart;
	const char *lpszEquivPart;
} XLBODYPARTEQUIV;

static const XLBODYPARTEQUIV xlBdyPartEquiv[]={
	{	IMAP4_RFC822,		"BODY[]"					},
	{	IMAP4_RFC822TEXT,	"BODY[TEXT]"			},
	{	IMAP4_RFC822HDR,	"BODY.PEEK[HEADER]"	},
	{	NULL,		NULL	}	/* mark end of list */
};

/*--------------------------------------------------------------------------*/

SZXTRN const char szIMAP4BodyHeaders[]="HEADER";
SZXTRN const char szIMAP4BodyHeaderFields[]="HEADER.FIELDS";
SZXTRN const char szIMAP4BodyNotHeaderFields[]="HEADER.FIELDS.NOT";
SZXTRN const char szIMAP4BodyMIME[]="MIME";
SZXTRN const char szIMAP4BodyText[]="TEXT";

/*--------------------------------------------------------------------------*/

typedef struct {
	LPCTSTR					lpszBodyPart;
	IMAP4_BODYPART_CASE	bCase;
} XLBDYPART;

static const XLBDYPART xlBdyParts[]={
	{	szIMAP4BodyHeaders,				IMAP4_HEADERS_BODYPART				},
	{	szIMAP4BodyHeaderFields,		IMAP4_HEADER_FIELDS_BODYPART		},
	{	szIMAP4BodyNotHeaderFields,	IMAP4_NOT_HEADER_FIELDS_BODYPART	},
	{	szIMAP4BodyMIME,					IMAP4_MIME_BODYPART					},
	{	szIMAP4BodyText,					IMAP4_TEXT_BODYPART					},
	{	NULL,									IMAP4_BAD_BODYPART					}
};

/*--------------------------------------------------------------------------*/

/* Note: empty/zero-length body part is translated as ALL */
IMAP4_BODYPART_CASE imap4XlateBodyPart (LPCTSTR lpszBodyPart, const UINT32 ulPartLen)
{
	LPCTSTR	lpszBP=lpszBodyPart;
	UINT32	ulIdx=0, ulPLen=ulPartLen, ulBPLen=ulPartLen;

	if (NULL == lpszBodyPart)
		return IMAP4_BAD_BODYPART;

	if ((_T('\0') == *lpszBodyPart) || (0 == ulPartLen))
		return IMAP4_ALL_BODYPART;

	/* skip white space */
	for (ulIdx=0, ulPLen=ulBPLen; _istspace(*lpszBP) && (ulIdx < ulPLen); ulIdx++, lpszBP++, ulBPLen--);

	/* find out if have any part numbers preceding the body part */
	for (ulIdx=0, ulPLen=ulBPLen; _istdigit(*lpszBP) && (ulIdx < ulPLen); ulIdx++, lpszBP++, ulBPLen--)
	{
		if (IMAP4_BODYPART_DELIM == *(lpszBP+1))
		{
			lpszBP++;
			ulIdx++;
			ulBPLen--;
		}
	}

	/* skip white space */
	for (ulIdx=0, ulPLen=ulBPLen; _istspace(*lpszBP) && (ulIdx < ulPLen); ulIdx++, lpszBP++, ulBPLen--);

	/* find out if have headers list */
	for (ulIdx=0, ulPLen=ulBPLen; ulIdx < ulPLen; ulIdx++)
	{
		TCHAR	tch=lpszBP[ulIdx];
		if (_istspace(tch) || (IMAP4_PARLIST_SDELIM == tch) || (IMAP4_BRCKT_EDELIM == tch))
		{
			ulBPLen = ulIdx;
			break;
		}
	}

	for (ulIdx=0; ; ulIdx++)
	{
		const XLBDYPART	*pxlPart=&xlBdyParts[ulIdx];
		if (NULL == pxlPart->lpszBodyPart)
			break;

		ulPLen = _tcslen(pxlPart->lpszBodyPart);
		if ((ulPLen == ulBPLen) &&
			 (_tcsnicmp(pxlPart->lpszBodyPart, lpszBP, ulPLen) == 0))
			return pxlPart->bCase;
	}

	return IMAP4_BAD_BODYPART;
}

/*--------------------------------------------------------------------------*/

/* returns NULL if not found requested part */
LPCTSTR imap4GetBodyPart (const IMAP4_BODYPART_CASE bCase)
{
	UINT32	ulIdx=0;

	if (fIsBadIMAP4BodyPart(bCase))
		return NULL;

	for (ulIdx=0; ; ulIdx++)
	{
		const XLBDYPART	*pxlPart=&xlBdyParts[ulIdx];

		if (NULL == pxlPart->lpszBodyPart)
			break;

		if (bCase == pxlPart->bCase)
			return pxlPart->lpszBodyPart;
	}

	return NULL;
}

/*--------------------------------------------------------------------------*/

/* returns EEXIST if no equivalence found */
EXC_TYPE imap4GetEquivBodyPart (const char	lpszPart[],
										  const UINT32	ulPartLen,
										  const char	**lppszEquivPart)
{
	UINT32	xlIdx=0;

	if ((NULL == lpszPart) || (NULL == lppszEquivPart))
		return EPARAM;
	*lppszEquivPart = NULL;

	if ((_T('\0') == *lpszPart) || (0 == ulPartLen))
		return EEXIST;

	for (xlIdx = 0; ; xlIdx++)
	{
		const XLBODYPARTEQUIV	*pXL=&xlBdyPartEquiv[xlIdx];
		UINT32						ulXLen=0;

		if (IsEmptyStr(pXL->lpszOrgPart) || IsEmptyStr(pXL->lpszEquivPart))
			break;

		if ((ulXLen=strlen(pXL->lpszOrgPart)) != ulPartLen)
			continue;

		if (strnicmp(pXL->lpszOrgPart, lpszPart, ulPartLen) != 0)
			continue;

		/* this point is reached if found part */
		*lppszEquivPart = pXL->lpszEquivPart;
		return EOK;
	}

	/* this point is reached if not found */
	return EEXIST;
}

/*--------------------------------------------------------------------------*/

/* SEARCH command keywords */

SZXTRN const char szIMAP4AllSearchKwd[]="ALL";
SZXTRN const char szIMAP4AnsweredSearchKwd[]="ANSWERED";
SZXTRN const char szIMAP4BccSearchKwd[]="BCC";
SZXTRN const char szIMAP4BeforeSearchKwd[]="BEFORE";
SZXTRN const char szIMAP4BodySearchKwd[]="BODY";
SZXTRN const char szIMAP4CcSearchKwd[]="CC";
SZXTRN const char szIMAP4DeletedSearchKwd[]="DELETED";
SZXTRN const char szIMAP4DraftSearchKwd[]="DRAFT";
SZXTRN const char szIMAP4FlaggedSearchKwd[]="FLAGGED";
SZXTRN const char szIMAP4FromSearchKwd[]="FROM";
SZXTRN const char szIMAP4HeaderSearchKwd[]="HEADER";
SZXTRN const char szIMAP4KeywordSearchKwd[]="KEYWORD";
SZXTRN const char szIMAP4LargerSearchKwd[]="LARGER";
SZXTRN const char szIMAP4NewSearchKwd[]="NEW";
SZXTRN const char szIMAP4NoteSearchKwd[]="NOT";
SZXTRN const char szIMAP4OldSearchKwd[]="OLD";
SZXTRN const char szIMAP4OnSearchKwd[]="ON";
SZXTRN const char szIMAP4OrSearchKwd[]="OR";
SZXTRN const char szIMAP4RecentSearchKwd[]="RECENT";
SZXTRN const char szIMAP4SeenSearchKwd[]="SEEN";
SZXTRN const char szIMAP4SentBeforeSearchKwd[]="SENTBEFORE";
SZXTRN const char szIMAP4SentOnSearchKwd[]="SENTON";
SZXTRN const char szIMAP4SentSinceSearchKwd[]="SENTSINCE";
SZXTRN const char szIMAP4SinceSearchKwd[]="SINCE";
SZXTRN const char szIMAP4SmallerSearchKwd[]="SMALLER";
SZXTRN const char szIMAP4SubjectSearchKwd[]="SUBJECT";
SZXTRN const char szIMAP4TextSearchKwd[]="TEXT";
SZXTRN const char szIMAP4ToSearchKwd[]="TO";
SZXTRN const char szIMAP4UIDSearchKwd[]="UID";
SZXTRN const char szIMAP4UnAnsweredSearchKwd[]="UNASWERED";
SZXTRN const char szIMAP4UnDeletedSearchKwd[]="UNDELETED";
SZXTRN const char szIMAP4UnDraftSearchKwd[]="UNDRAFT";
SZXTRN const char szIMAP4UnFlaggedSearchKwd[]="UNFLAGGED";
SZXTRN const char szIMAP4UnKeywordSearchKwd[]="UNKEYWORD";
SZXTRN const char szIMAP4UnSeenSearchKwd[]="UNSEEN";

/*--------------------------------------------------------------------------*/

/* must match IMAP4STKWCASE enumeration !!! */
static LPCTSTR i4stkwxl[]={
	IMAP4_MESSAGES,
	IMAP4_RECENT,
	IMAP4_UIDNEXT,
	IMAP4_UIDVALIDITY,
	IMAP4_UNSEEN,
	NULL					/* mark end */
};

/* returns NULL.empty if illegal value */
LPCTSTR imap4GetStatusKeywordString (const IMAP4STKWCASE eStCase)
{
	if (fIsBadIMAP4StatusKWCase(eStCase))
		return NULL;

	return i4stkwxl[eStCase];
}

/* returns IMAP4STBAD if unknown keyword */
IMAP4STKWCASE imap4GetStatusKeywordCase (LPCTSTR lpszKW, const UINT32 ulKWLen)
{
	UINT32	ulIdx=0;

	if (IsEmptyStr(lpszKW) || (0 == ulKWLen))
		return IMAP4STBAD;

	for (ulIdx=0; ; ulIdx++)
	{
		LPCTSTR	lpszStKw=i4stkwxl[ulIdx];
		UINT32	ulStKwLen=(IsEmptyStr(lpszStKw) ? 0 : _tcslen(lpszStKw));
		if (0 == ulStKwLen)
			break;

		if (ulStKwLen != ulKWLen)
			continue;
		if (_tcsnicmp(lpszStKw, lpszKW, ulKWLen) == 0)
			return (IMAP4STKWCASE) ulIdx;
	}

	return IMAP4STBAD;
}

/*---------------------------------------------------------------------------*/

/* some known server's names */
SZXTRN const TCHAR szIMAP4InterMailSrvrName[]=_T("InterMail");
SZXTRN const TCHAR szIMAP4MSExchangeSrvrName[]=_T("Exchange");
SZXTRN const TCHAR szIMAP4MirapointSrvrName[]=_T("Mirapoint");
SZXTRN const TCHAR szIMAP4LotusDominoSrvrName[]=_T("Domino");
SZXTRN const TCHAR szIMAP4CommtouchSrvrName[]=_T("NPLex");
SZXTRN const TCHAR szIMAP4NSMailSrvrName[]=_T("Netscape");
SZXTRN const TCHAR szIMAP4iPlanetSrvrName[]=_T("iPlanet");
SZXTRN const TCHAR szIMAP4CommuniGateProSrvrName[]=_T("CommuniGate Pro");
SZXTRN const TCHAR szIMAP4CriticalPathSrvrName[]=_T("CriticalPath");

/* NULL terminated list of known server type strings */
SZXTRN LPCTSTR IMAP4ServerTypes[]={
	szIMAP4InterMailSrvrName,
	szIMAP4MSExchangeSrvrName,
	szIMAP4MirapointSrvrName,
	szIMAP4LotusDominoSrvrName,
	szIMAP4CommtouchSrvrName,
	szIMAP4NSMailSrvrName,
	szIMAP4iPlanetSrvrName,
	szIMAP4CommuniGateProSrvrName,
	szIMAP4CriticalPathSrvrName,

	NULL		/* mark end of list */
};

/*
 *		Known templates for welcomes of different servers.
 */

/* SW.COM-KX4.3:	* OK IMAP4 server (InterMail vM.5.00.00.09 201-237-107-107) ready Tue, 30 Jan 2001 11:05:57 +0200 (IST) */
SZXTRN const TCHAR szIMAP4SWCOMKx4p3WelcomePattern[]=_T("IMAP4 server (%T %V %I) ready %D");

/* Exchange: * OK Microsoft Exchange IMAP4rev1 server version 5.5.2448.8 (newexch.cti2.com) ready */
SZXTRN const TCHAR szIMAP4MSExchangeWelcomePattern[]=_T("Microsoft %T IMAP4rev1 server version %V (%I) ready");

/* Exchange 2000: * OK Microsoft Exchange 2000 IMAP4rev1 server version 6.0.4417.0 (demoser2000.exch2000.cti2.com) ready. */
SZXTRN const TCHAR szIMAP4MSExch2000WelcomePattern[]=_T("Microsoft %T 2000 IMAP4rev1 server version %V (%I) ready.");

/* Exchange 20003: * OK Microsoft Exchange Server 2003 IMAP4rev1 server version 6.5.6944.0 (test-exch2003.newcti.com) ready. */
SZXTRN const TCHAR szIMAP4MSExch2003WelcomePattern[]=_T("Microsoft %T Server 2003 IMAP4rev1 server version %V (%I) ready.");

/* Mirapoint: * OK testdrive.mirapoint.com Mirapoint IMAP4 1.0 server ready */
SZXTRN const TCHAR szIMAP4MirapointWelcomePattern[]=_T("%I %T IMAP4 %V server ready");

/* Mirapoint IMAP4 proxy: * OK Mirapoint IMAP4PROXY 1.0 server ready */
SZXTRN const TCHAR szIMAP4MiraptProxyWelcomePattern[]=_T("%I %T IMAP4PROXY %V server ready");

/* Lotus: * OK Domino IMAP4 Server Release 5.0.4  ready Tue, 30 Jan 2001 13:59:48 +0200 */
SZXTRN const TCHAR szIMAP4LotusDominoWelcomePattern[]=_T("%T IMAP4 Server Release %V ready %D");

/* Commtouch: * OK IMAP4 server ready (NPlex 2.0.085) */
SZXTRN const TCHAR szIMAP4CommtouchWelcomePattern[]=_T("IMAP4 server ready (%T %V)");

/* Netscape Mail:	* OK ipnew_nts.cti2.com IMAP4 service (Netscape Messaging Server 4.15  (built Dec 14 1999)) */
SZXTRN const TCHAR szIMAP4NSMailWelcomePattern[]=_T("%I IMAP4 service (%T Messaging Server %V (built %*))");

/* iPlanet: * OK venus.telcotest.cti2.com IMAP4 service (iPlanet Messaging Server 5.1 (built May  7 2001)) */
SZXTRN const TCHAR szIMAP4iPlanetWelcomePattern[]=_T("%I IMAP4 service (%T Messaging Server %V (built %*))");

/* iPlanet+HotFix: *OK mail.cti.com IMAP4 service (iPlanet Messaging Server 5.2 HotFix 1.02 (built Sep 16 2002)) */
SZXTRN const TCHAR szIMAP4iPlanetHotFixWelcomePattern[]=_T("%I IMAP4 service (%T Messaging Server %V HotFix %I (built %*))");

/* CommuniGate Pro: * OK CommuniGate Pro IMAP Server 3.4.8 at cgatepro.cti2.com ready */
SZXTRN const TCHAR szIMAP4CommuniGateProWelcomePattern[]=_T("%T %T IMAP Server %V at %I ready");

/* NOTE !!! The type value must match value of "szIMAP4CriticalPathSrvrName"
/* CriticalPath: * OK IMAP4 server ready (6.0.021) */
SZXTRN const TCHAR szIMAP4CriticalPathWelcomePattern[]=_T("IMAP4 %T=CriticalPath server ready (%V)");

/* CriticalPath PROXY: * OK IMAP4 PROXY server ready (7.3.104) */
SZXTRN const TCHAR szIMAP4CriticalPathProxyWelcomePattern[]=_T("IMAP4 PROXY %T=CriticalPath server ready (%V)");

SZXTRN LPCTSTR IMAP4KnownWelcomePatterns[]={
	szIMAP4SWCOMKx4p3WelcomePattern,
	szIMAP4MSExchangeWelcomePattern,
	szIMAP4MirapointWelcomePattern,
	szIMAP4MiraptProxyWelcomePattern,
	szIMAP4LotusDominoWelcomePattern,
	szIMAP4CommtouchWelcomePattern,
	szIMAP4NSMailWelcomePattern,
	szIMAP4MSExch2000WelcomePattern,
	szIMAP4MSExch2003WelcomePattern,
	szIMAP4iPlanetWelcomePattern,
	szIMAP4iPlanetHotFixWelcomePattern,
	szIMAP4CommuniGateProWelcomePattern,
	szIMAP4CriticalPathWelcomePattern,
	szIMAP4CriticalPathProxyWelcomePattern,

	NULL	/* mark end */
};

/*--------------------------------------------------------------------------*/

/* returns EIOUNCLASS if no match found */
EXC_TYPE imap4AnalyzeWelcomePattern (LPCTSTR			lpszWelcome,
												 LPCTSTR			lpszWPattern,
												 LPTSTR			lpszType,
												 const UINT32	ulMaxTypeLen,
												 LPTSTR			lpszVersion,
												 const UINT32	ulMaxVerLen)
{
	EXC_TYPE	exc=EOK;
	TCHAR		szTArg[MAX_IMAP4_OPCODE_LEN+2];
	LPCTSTR	lpszWPos=lpszWelcome;

	if (IsEmptyStr(lpszWelcome) || IsEmptyStr(lpszWPattern) ||
		 (NULL == lpszType) || (0 == ulMaxTypeLen) ||
		 (NULL == lpszVersion) || (0 == ulMaxVerLen))
		return EPARAM;

	*lpszType = _T('\0');
	*lpszVersion = _T('\0');

	/* make sure this is an untagged response */
	for ( ; _istspace(*lpszWPos) && (*lpszWPos != _T('\0')); lpszWPos++);
	if (*lpszWPos != IMAP4_UNTAGGED_RSP)
		return EUDFFORMAT;

	/* make sure this is an OK response */
	if ((exc=imap4GetArg((lpszWPos+1), szTArg, MAX_IMAP4_OPCODE_LEN, &lpszWPos)) != EOK)
		return exc;
	if ((exc=imap4XlateRspCode(szTArg)) != EOK)
		return exc;

	if ((exc=inetAnalyzeWelcomePattern(lpszWPos, lpszWPattern, lpszType, ulMaxTypeLen, lpszVersion, ulMaxVerLen)) != EOK)
		return exc;

	return EOK;
}

/*--------------------------------------------------------------------------*/

SZXTRN const char szIMAP4ReferralBracketKwd[]="REFERRAL";
SZXTRN const char szIMAP4ReadOnlyBracketKwd[]="READ-ONLY";
SZXTRN const char szIMAP4ReadWriteBracketKwd[]="READ-WRITE";

/* NULL terminate list of keywords that appear in bracketed tagged responses */
SZXTRN const char *szIMAP4BracketKwds[]={
	szIMAP4ReferralBracketKwd,
	szIMAP4ReadOnlyBracketKwd,
	szIMAP4ReadWriteBracketKwd,

	NULL	/* mark end of list */
};

/*--------------------------------------------------------------------------*/
