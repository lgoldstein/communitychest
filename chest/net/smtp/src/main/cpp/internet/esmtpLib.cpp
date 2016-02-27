#include <util/string.h>
#include <util/errors.h>

#include <internet/esmtpLib.h>
#include <internet/base64.h>

#ifdef __cplusplus
#define SZXTRN extern
#else
#define SZXTRN /* nothing */
#endif

/*--------------------------------------------------------------------------*/

SZXTRN const TCHAR szESMTPEhloCmd[]=_T("EHLO");
SZXTRN const TCHAR szESMTPAuthCmd[]=_T("AUTH");
SZXTRN const TCHAR szESMTPEtrnCmd[]=_T("ETRN");

/*--------------------------------------------------------------------------*/

SZXTRN const TCHAR szESMTLoginAuth[]=_T("LOGIN");
SZXTRN const TCHAR szESMTCRAMMD5Auth[]=_T("CRAM-MD5");
SZXTRN const TCHAR szESMTDigestMD5Auth[]=_T("DIGEST-MD5");

typedef struct {
	LPCTSTR			lpszAuthCase;
	ESMTPAUTHCASE	eAuthCase;
} EXLAC, *LPEXLAC;

static const EXLAC xls2ea[]={
	{	szESMTLoginAuth,		ESMTP_LOGIN_AUTHCASE		},
	{	szESMTCRAMMD5Auth,	ESMTP_CRAMMD5_AUTHCASE	},
	{	szESMTDigestMD5Auth,	ESMTP_DGSTMD5_AUTHCASE	},
	{	NULL,						ESMTP_BAD_AUTHCASE		}	/* mark end */
};

/*--------------------------------------------------------------------------*/

ESMTPAUTHCASE esmtpChars2AuthCase (LPCTSTR lpszAuthCase, const UINT32 ulACLen)
{
	if (IsEmptyStr(lpszAuthCase))
		return ESMTP_BAD_AUTHCASE;

	for (UINT32 i=0; ; i++)
	{
		const EXLAC	*pXL=&xls2ea[i];
		LPCTSTR		lpszXLCase=pXL->lpszAuthCase;
		UINT32		ulXLLen=GetSafeStrlen(lpszXLCase);

		if (IsEmptyStr(lpszXLCase) || (0 == ulXLLen))
			break;

		if ((ulXLLen == ulACLen) && (_tcsnicmp(lpszXLCase, lpszAuthCase, ulACLen) == 0))
			return pXL->eAuthCase;
	}

	return ESMTP_BAD_AUTHCASE;
}

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
EXC_TYPE esmtpSendEHelo (ISockioInterface&	ISock,
								 LPCTSTR					lpszID,	// if NULL/empty then hostname is used
								 ESMTP_CAPSCFN			lpfnEcfn,	// may be NULL
								 LPVOID					pArg,
								 UINT32&					rcode,
								 LPTSTR					lpszRspBuf,
								 const UINT32			ulMaxBufLen,
								 const UINT32			ulRspTimeout)
{
	if ((NULL == lpszRspBuf) || (ulMaxBufLen < MAX_DNS_DOMAIN_LEN))
		return EBADBUFF;

	LPCTSTR	lpszHost=lpszID;
	TCHAR		szHost[MAX_DNS_DOMAIN_LEN+2]=_T("");
	if (IsEmptyStr(lpszHost))
	{
		if (gethostname(szHost, MAX_DNS_DOMAIN_LEN) != 0)
			return EIOSOFT;

		lpszHost = szHost;
	}

	int	wLen=ISock.WriteCmdf(_T("%s %s\r\n"), szESMTPEhloCmd, lpszHost);
	if (wLen <= 0)
		return ENOTCONNECTION;

	BOOLEAN	fContEnum=TRUE, fIsLast=FALSE;
	for (UINT32	ulRdx=0; (!fIsLast); ulRdx++)
	{
		if (!fContEnum)
			return smtpGetFinalResponse(ISock, lpszRspBuf, ulMaxBufLen, rcode, ulRspTimeout);

		BOOLEAN	fStripCRLF=FALSE;
		int		rLen=ISock.ReadCmd(lpszRspBuf, ulMaxBufLen, (SINT32) ulRspTimeout, &fStripCRLF);
		if (rLen <= 0)
			return ENOTCONNECTION;

		/* we must have entire response in line */
		if (!fStripCRLF)
			return EIOUNCLASS;

		EXC_TYPE	exc=EOK;
		LPCTSTR	lsp=lpszRspBuf;
		for ( ; _istdigit(*lsp) && (*lsp != _T('\0')); lsp++);
		if (_T(' ') == *lsp)
		{
			rcode = argument_to_dword(lpszRspBuf, (lsp - lpszRspBuf), EXC_ARG(exc));
			if (exc != EOK)
				return exc;
			fIsLast = TRUE;
		}

		if (lpfnEcfn != NULL)
		{
			// skip to next value
			for (lsp++; _istspace(*lsp) && (*lsp != _T('\0')); lsp++);

			if ((ulRdx != 0) && (*lsp != _T('\0')))	// skip 1st response
			{
				if ((exc=(*lpfnEcfn)(ISock, lsp, pArg, fContEnum)) != EOK)
					return exc;
			}
		}
	}

	return EOK;
}

EXC_TYPE esmtpSendEHelo (ISockioInterface&	ISock,
								 LPCTSTR					lpszID,	// if NULL/empty then hostname is used
								 ESMTP_CAPSCFN			lpfnEcfn,	// may be NULL
								 LPVOID					pArg,
								 UINT32&					rcode,
								 const UINT32			ulRspTimeout)
{
	TCHAR		szLine[MAX_SMTP_CMD_LINE_LEN+2]=_T("");
	return esmtpSendEHelo(ISock, lpszID, lpfnEcfn, pArg, rcode, szLine, MAX_SMTP_CMD_LINE_LEN, ulRspTimeout);
}
#endif	/* of __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
// callback for enumerating capabilities listed in EHLO response
static EXC_TYPE pcmCfn (ISockioInterface&	ISock,
								LPCTSTR				lpszCapability,
								LPVOID				pArg,
								BOOLEAN&				fContEnum)
{
	if (NULL == pArg)
		return ECONTEXT;
	else
		return ((CStr2PtrMapper *) pArg)->AddKey(lpszCapability, NULL);
}

EXC_TYPE esmtpSendEHelo (ISockioInterface&	ISock,
								 LPCTSTR					lpszID,	// if NULL/empty then hostname is used
								 CStr2PtrMapper&		capsMap,	// key=capability name, value=NULL
								 UINT32&					rcode,
								 LPTSTR					lpszRspBuf,
								 const UINT32			ulMaxBufLen,
								 const UINT32			ulRspTimeout)
{
	return esmtpSendEHelo(ISock, lpszID, pcmCfn, (LPVOID) &capsMap, rcode, lpszRspBuf, ulMaxBufLen, ulRspTimeout);
}

EXC_TYPE esmtpSendEHelo (ISockioInterface&	ISock,
								 LPCTSTR					lpszID,	// if NULL/empty then hostname is used
								 CStr2PtrMapper&		capsMap,	// key=capability name, value=NULL
								 UINT32&					rcode,
								 const UINT32			ulRspTimeout)
{
	TCHAR		szLine[MAX_SMTP_CMD_LINE_LEN+2]=_T("");
	return esmtpSendEHelo(ISock, lpszID, capsMap, rcode, szLine, MAX_SMTP_CMD_LINE_LEN, ulRspTimeout);
}
#endif	/* of __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
static EXC_TYPE esmtpSendAuthParam (ISockioInterface&	ISock,
												LPCTSTR				lpszParam,
												UINT32&				rcode,
												LPTSTR				lpszRspBuf,
												const UINT32		ulMaxBufLen,
												const UINT32		ulRspTimeout)
{
	rcode = (UINT32) (-1);

	if (IsEmptyStr(lpszParam))
		return EFTYPE;

	TCHAR		szB64Buf[BASE64_MAX_LINE_LEN+2]=_T("");
	UINT32	oLen=0, eLen=_tcslen(lpszParam);
	EXC_TYPE	exc=b64_encode_finish((UINT8 *) lpszParam, eLen, szB64Buf, BASE64_MAX_LINE_LEN, &oLen);
	if (exc != EOK)
		return exc;
	szB64Buf[oLen] = _T('\0');

	return smtpSendCmdSync(ISock, szB64Buf, NULL, rcode, lpszRspBuf, ulMaxBufLen, ulRspTimeout);
}
#endif	/* of __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
EXC_TYPE esmtpAuthLogin (ISockioInterface&	ISock,
								 LPCTSTR					lpszUID,
								 LPCTSTR					lpszPassword,
								 UINT32&					rcode,
								 LPTSTR					lpszRspBuf,
								 const UINT32			ulMaxBufLen,
								 const UINT32			ulRspTimeout)
{
	rcode = (UINT32) (-1);

	if (IsEmptyStr(lpszUID) || IsEmptyStr(lpszPassword))
		return EPARAM;

	EXC_TYPE exc=smtpSendCmdSync(ISock, szESMTPAuthCmd, szESMTLoginAuth, rcode, lpszRspBuf, ulMaxBufLen, ulRspTimeout);
	if (exc != EOK)
		return exc;
	if (rcode != ESMTP_E_AUTH_DATA)
		return EPROTOCOL;

	if ((exc=esmtpSendAuthParam(ISock, lpszUID, rcode, lpszRspBuf, ulMaxBufLen, ulRspTimeout)) != EOK)
		return exc;
	if (rcode != ESMTP_E_AUTH_DATA)
		return EPROTOCOL;

	if ((exc=esmtpSendAuthParam(ISock, lpszPassword, rcode, lpszRspBuf, ulMaxBufLen, ulRspTimeout)) != EOK)
		return exc;
	if (rcode != ESMTP_E_AUTH_SUCCEED)
		return EPASSWORDMISMATCH;

	return EOK;
}

EXC_TYPE esmtpAuthLogin (ISockioInterface&	ISock,
								 LPCTSTR					lpszUID,
								 LPCTSTR					lpszPassword,
								 UINT32&					rcode,
								 const UINT32			ulRspTimeout)
{
	TCHAR		szLine[MAX_SMTP_CMD_LINE_LEN+2]=_T("");
	return esmtpAuthLogin(ISock, lpszUID, lpszPassword, rcode, szLine, MAX_SMTP_CMD_LINE_LEN, ulRspTimeout);
}
#endif	/* of __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
/*
 *		Each option in mapper is an "attribute=value" pair where the key is
 * attribute and the mapped item is the value. If the mapped item is NULL/empty,
 * then the key is assumed to be a predicate rather than an attribute. If no
 * attributes are specified, then the "smtp" version is called.
 *
 *	Note: it is assumed that the order of the attributes is not important
 */
static EXC_TYPE esmtpSetEnvelopeData (ISockioInterface&		ISock,
												  LPCTSTR					lpszCmd,
												  LPCTSTR					lpszTarget,
												  const CStr2StrMapper&	opts,
												  UINT32&					rcode,
												  LPTSTR						lpszRspBuf,
												  const UINT32				ulMaxBufLen,
												  const UINT32				ulRspTimeout)
{
	rcode = (UINT32) (-1);

	if (IsEmptyStr(lpszCmd))
		return EPARAM;

	CIncStrlBuilder	strb(MAX_EMAIL_ADDR_LEN+ARGUMENT_LENGTH, ARGUMENT_LENGTH);
	EXC_TYPE				exc=strb.AddStr(lpszCmd);
	if (exc != EOK)
		return exc;
	if ((exc=strb.AddChar(_T(' '))) != EOK)
		return exc;

	// if target not delimited than delimit it
	const LPCTSTR	lpszCmdArg=GetSafeStrPtr(lpszTarget);
	if (*lpszCmdArg != EMAIL_PATH_SDELIM)
	{
		if ((exc=strb.AddChar(EMAIL_PATH_SDELIM)) != EOK)
			return exc;
	}

	if ((exc=strb.AddStr(lpszCmdArg)) != EOK)
		return exc;

	if (*lpszCmdArg != EMAIL_PATH_SDELIM)
	{
		if ((exc=strb.AddChar(EMAIL_PATH_EDELIM)) != EOK)
			return exc;
	}

	// now add the options (if any)
	{
		CStr2StrMapEnum	ope(opts);
		LPCTSTR				lpszAttr=NULL, lpszVal=NULL;
		for (exc=ope.GetFirst(lpszAttr, lpszVal); EOK == exc; exc=ope.GetNext(lpszAttr, lpszVal))
		{
			if ((exc=strb.AddChar(_T(' '))) != EOK)
				return exc;
			if ((exc=strb.AddStr(lpszAttr)) != EOK)
				return exc;

			// check if this is an attribute or a predicate
			if (IsEmptyStr(lpszVal))
				continue;

			if ((exc=strb.AddChar(_T('='))) != EOK)
				return exc;
			if ((exc=strb.AddStr(lpszVal)) != EOK)
				return exc;
		}
	}

	if ((exc=strb.AddCRLF()) != EOK)
		return exc;

	// write the command
	{
		const LPCTSTR	lpszCmdLine=strb;
		const int		wLen=ISock.Write(lpszCmdLine);
		if (wLen <= 0)
			return ENOTCONNECTION;
	}

	if ((exc=smtpGetFinalResponse(ISock, lpszRspBuf, ulMaxBufLen, rcode, ulRspTimeout)) != EOK)
		return exc;

	return EOK;
}

EXC_TYPE esmtpSetSender (ISockioInterface&		ISock,
								 LPCTSTR						lpszSndr,	// may be NULL/empty
								 const CStr2StrMapper&	opts,
								 UINT32&						rcode,
								 LPTSTR						lpszRspBuf,
								 const UINT32				ulMaxBufLen,
								 const UINT32				ulRspTimeout)
{
	if (0 == opts.GetItemsCount())
		return smtpSetSender(ISock, lpszSndr, rcode, lpszRspBuf, ulMaxBufLen, ulRspTimeout);
	else
		return esmtpSetEnvelopeData(ISock, szSMTPMailFromCmd, lpszSndr, opts, rcode, lpszRspBuf, ulMaxBufLen, ulRspTimeout);
}

EXC_TYPE esmtpSetSender (ISockioInterface&		ISock,
								 LPCTSTR						lpszSndr,	// may be NULL/empty
								 const CStr2StrMapper&	opts,
								 UINT32&						rcode,
								 const UINT32				ulRspTimeout)
{
	TCHAR	szLine[MAX_SMTP_CMD_LINE_LEN+2]=_T("");

	return esmtpSetSender(ISock, lpszSndr, opts, rcode, szLine, MAX_SMTP_CMD_LINE_LEN, ulRspTimeout);
}

EXC_TYPE esmtpAddRecipient (ISockioInterface&		ISock,
									 LPCTSTR						lpszRecip,
									 const CStr2StrMapper&	opts,
									 UINT32&						rcode,
									 LPTSTR						lpszRspBuf,
									 const UINT32				ulMaxBufLen,
									 const UINT32				ulRspTimeout)
{
	if (0 == opts.GetItemsCount())
		return smtpAddRecepient(ISock, lpszRecip, rcode, lpszRspBuf, ulMaxBufLen, ulRspTimeout);
	else
		return esmtpSetEnvelopeData(ISock, szSMTPRcptToCmd, lpszRecip, opts, rcode, lpszRspBuf, ulMaxBufLen, ulRspTimeout);
}

EXC_TYPE esmtpAddRecipient (ISockioInterface&		ISock,
									 LPCTSTR						lpszRecip,
									 const CStr2StrMapper&	opts,
									 UINT32&						rcode,
									 const UINT32				ulRspTimeout)
{
	TCHAR	szLine[MAX_SMTP_CMD_LINE_LEN+2]=_T("");

	return esmtpAddRecipient(ISock, lpszRecip, opts, rcode, szLine, MAX_SMTP_CMD_LINE_LEN, ulRspTimeout);
}
#endif	/* of __cplusplus */

/*--------------------------------------------------------------------------*/

SZXTRN const TCHAR szESMTPDSNOption[]=_T("DSN");

SZXTRN const TCHAR szESMTPDSNENVIDKwd[]=_T("ENVID");

SZXTRN const TCHAR szESMTPDSNNotifyKwd[]=_T("NOTIFY");
SZXTRN const TCHAR	szESMTPDSNNotifyNeverOpt[]=_T("NEVER");
SZXTRN const TCHAR	szESMTPDSNNotifySuccessOpt[]=_T("SUCCESS");
SZXTRN const TCHAR	szESMTPDSNNotifyFailureOpt[]=_T("FAILURE");
SZXTRN const TCHAR	szESMTPDSNNotifyDelayOpt[]=_T("DELAY");

SZXTRN const TCHAR szESMTPDSNORCPTKwd[]=_T("ORCPT");

SZXTRN const TCHAR szESMTPDSNRETKwd[]=_T("RET");
SZXTRN const TCHAR	szESMTPDSNRETHdrsOpt[]=_T("HDRS");
SZXTRN const TCHAR	szESMTPDSNRETFullOpt[]=_T("FULL");

/*--------------------------------------------------------------------------*/

SZXTRN const LPCTSTR szESMTPDSNOptions[]={
	_T(""),
	szESMTPDSNRETHdrsOpt,
	szESMTPDSNRETFullOpt,

	NULL
};

/* Note: empty/NULL <=> ESMTP_DSN_RETNONE */
const ESMTPDSNRETCASE esmtpXlateDSNReturnOption (LPCTSTR lpszOpt, const UINT32 ulOptLen)
{
	if (0 == ulOptLen)
		return ESMTP_DSN_RETNONE;
	if (NULL == lpszOpt)
		return ESMTP_DSN_RETBAD;

	for (UINT32	ulOpNdx=0; ; ulOpNdx++)
	{
		const LPCTSTR	lpszKOP=szESMTPDSNOptions[ulOpNdx];
		const UINT32	ulKLen=_tcslen(lpszKOP);
		if (ulKLen != ulOptLen)
			continue;
		if (_tcsnicmp(lpszOpt, lpszKOP, ulKLen) != 0)
			continue;

		return ((ESMTPDSNRETCASE) ulOpNdx);
	}

	return ESMTP_DSN_RETBAD;
}

/* Note: returns NULL if error, _T("") if ESMTP_DSN_RETNONE */
const LPCTSTR esmtpGetDSNReturnOption (const ESMTPDSNRETCASE eRetOp)
{
	if (fIsBadESMTPDSNRetOpt(eRetOp))
		return NULL;
	else
		return szESMTPDSNOptions[eRetOp];
}

EXC_TYPE esmtpGetDSNReturnOptionString (const ESMTPDSNRETCASE eRetOp, LPTSTR lpszOpt, const UINT32 ulOptLen)
{
	const LPCTSTR	lpszRET=esmtpGetDSNReturnOption(eRetOp);
	if ((NULL == lpszOpt) || (0 == ulOptLen))
		return EPARAM;
	*lpszOpt = _T('\0');

	if (NULL == lpszRET)
		return EFTYPE;

	if (_tcslen(lpszRET) >= ulOptLen)
		return EOVERFLOW;

	_tcscpy(lpszOpt, lpszRET);
	return EOK;
}

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
static EXC_TYPE addESMTPDSNOption (const BOOL fIsSet, LPCTSTR lpszOpt, /* In/Out */ UINT32& ulOptNdx, IStrlBuilder& strb)
{
	if (!fIsSet)
		return EOK;

	EXC_TYPE	exc=EOK;

	if (ulOptNdx != 0)
	{
		if ((exc=strb.AddChar(_T(','))) != EOK)
			return exc;
	}

	if ((exc=strb.AddStr(lpszOpt)) != EOK)
		return exc;

	ulOptNdx++;
	return EOK;
}

// Note: if no option set, then nothing is done (and EOK returned)
EXC_TYPE esmtpBuildDSNNotifyOptions (const ESMTPDSNNOTIFYOPTS& opts, IStrlBuilder& strb)
{
	EXC_TYPE	exc=EOK;
	UINT32	ulOptNdx=0;

	if ((exc=addESMTPDSNOption((opts.m_fDelay != 0), szESMTPDSNNotifyDelayOpt, ulOptNdx, strb)) != EOK)
		return exc;
	if ((exc=addESMTPDSNOption((opts.m_fFailure != 0), szESMTPDSNNotifyFailureOpt, ulOptNdx, strb)) != EOK)
		return exc;
	if ((exc=addESMTPDSNOption((opts.m_fNever != 0), szESMTPDSNNotifyNeverOpt, ulOptNdx, strb)) != EOK)
		return exc;
	if ((exc=addESMTPDSNOption((opts.m_fSuccess != 0), szESMTPDSNNotifySuccessOpt, ulOptNdx, strb)) != EOK)
		return exc;

	return EOK;
}
#endif	/* of __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
EXC_TYPE esmtpSetDSNSender (ISockioInterface&		ISock,
									 LPCTSTR						lpszSndr,	// may be NULL/empty
									 LPCTSTR						lpszENVID,	// may be NULL/empty
									 const ESMTPDSNRETCASE	eRetCase,
									 UINT32&						rcode,
									 LPTSTR						lpszRspBuf,
									 const UINT32				ulMaxBufLen,
									 const UINT32				ulRspTimeout)
{
	EXC_TYPE			exc=EOK;
	CStr2StrMapper	opts(4, FALSE);

	rcode = (UINT32) (-1);

	if (!IsEmptyStr(lpszENVID))
	{
		if ((exc=opts.AddKey(szESMTPDSNENVIDKwd, lpszENVID)) != EOK)
			return exc;
	}

	if (eRetCase != ESMTP_DSN_RETNONE)
	{
		const LPCTSTR	lpszRET=esmtpGetDSNReturnOption(eRetCase);
		if (IsEmptyStr(lpszRET))
			return EFTYPE;

		if ((exc=opts.AddKey(szESMTPDSNRETKwd, lpszRET)) != EOK)
			return exc;
	}

	if ((exc=esmtpSetSender(ISock, lpszSndr, opts, rcode, lpszRspBuf, ulMaxBufLen, ulRspTimeout)) != EOK)
		return exc;

	return EOK;
}

EXC_TYPE esmtpSetDSNSender (ISockioInterface&		ISock,
									 LPCTSTR						lpszSndr,	// may be NULL/empty
									 LPCTSTR						lpszENVID,	// may be NULL/empty
									 const ESMTPDSNRETCASE	eRetCase,
									 UINT32&						rcode,
									 const UINT32				ulRspTimeout)
{
	TCHAR	szLine[MAX_SMTP_CMD_LINE_LEN+2]=_T("");
	return esmtpSetDSNSender(ISock, lpszSndr, lpszENVID, eRetCase, rcode, szLine, MAX_SMTP_CMD_LINE_LEN, ulRspTimeout);
}

EXC_TYPE esmtpAddDSNRecipient (ISockioInterface&			ISock,
										 LPCTSTR							lpszRecip,	// may be NULL/empty
										 const ESMTPDSNNOTIFYOPTS&	ntfyOpts,
										 LPCTSTR							lpszORCPT,	// may be NULL/empty
										 UINT32&							rcode,
										 LPTSTR							lpszRspBuf,
										 const UINT32					ulMaxBufLen,
										 const UINT32					ulRspTimeout)
{
	EXC_TYPE			exc=EOK;
	CStr2StrMapper	opts(4, FALSE);

	rcode = (UINT32) (-1);

	{
		TCHAR	szNtfyOpts[ESMTP_DSN_MAX_NOTIFY_LEN+2]=_T("");

		if ((exc=esmtpBuildDSNNotifyOptions(ntfyOpts, szNtfyOpts, ESMTP_DSN_MAX_NOTIFY_LEN)) != EOK)
			return exc;

		if (szNtfyOpts[0] != _T('\0'))
		{
			if ((exc=opts.AddKey(szESMTPDSNNotifyKwd, szNtfyOpts)) != EOK)
				return exc;
		}
	}

	if (!IsEmptyStr(lpszORCPT))
	{
		if ((exc=opts.AddKey(szESMTPDSNORCPTKwd, lpszORCPT)) != EOK)
			return exc;
	}

	if ((exc=esmtpAddRecipient(ISock, lpszRecip, opts, rcode, lpszRspBuf, ulMaxBufLen, ulRspTimeout)) != EOK)
		return exc;

	return EOK;
}

EXC_TYPE esmtpAddDSNRecipient (ISockioInterface&			ISock,
										 LPCTSTR							lpszRecip,	// may be NULL/empty
										 const ESMTPDSNNOTIFYOPTS&	ntfyOpts,
										 LPCTSTR							lpszORCPT,	// may be NULL/empty
										 UINT32&							rcode,
										 const UINT32					ulRspTimeout)
{
	TCHAR	szLine[MAX_SMTP_CMD_LINE_LEN+2]=_T("");
	return esmtpAddDSNRecipient(ISock, lpszRecip, ntfyOpts, lpszORCPT, rcode, szLine, MAX_SMTP_CMD_LINE_LEN, ulRspTimeout);
}
#endif	/* of __cplusplus */

/*--------------------------------------------------------------------------*/
