#include <_types.h>

#include <internet/rfc822.h>
#include <internet/base64.h>
#include <internet/qpenc.h>

/*---------------------------------------------------------------------------*/
/*
 *	Contains strings related to RFC822 (mainly e-mail headers)
 */
/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
#define SZXTRN extern
#else
#define SZXTRN
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

SZXTRN const TCHAR pszStdFromHdr[]=_T("From:");
SZXTRN const TCHAR pszStdSenderHdr[]=_T("Sender:");
SZXTRN const TCHAR pszStdToHdr[]=_T("To:");
SZXTRN const TCHAR pszStdCcHdr[]=_T("Cc:");
SZXTRN const TCHAR pszStdBccHdr[]=_T("Bcc:");
SZXTRN const TCHAR pszStdDateHdr[]=_T("Date:");
SZXTRN const TCHAR pszStdMIMEVersionHdr[]=_T("MIME-Version:");
SZXTRN const TCHAR pszStdReturnPathHdr[]=_T("Return-Path:");
SZXTRN const TCHAR pszStdReplyToHdr[]=_T("Reply-To:");
SZXTRN const TCHAR pszStdReplyCcHdr[]=_T("Reply-Cc:");
SZXTRN const TCHAR pszStdInReplyToHdr[]=_T("In-Reply-To:");
SZXTRN const TCHAR pszStdReceivedHdr[]=_T("Received:");
SZXTRN const TCHAR pszStdSubjectHdr[]=_T("Subject:");
SZXTRN const TCHAR pszStdContentTypeHdr[]=_T("Content-Type:");
SZXTRN const TCHAR pszStdContentLengthHdr[]=_T("Content-Length:");
SZXTRN const TCHAR pszStdContentLocationHdr[]=_T("Content-Location:");
SZXTRN const TCHAR pszStdContentBaseHdr[]=_T("Content-Base:");
SZXTRN const TCHAR pszStdContentIDHdr[]=_T("Content-ID:");
SZXTRN const TCHAR pszStdContentXferEncoding[]=_T("Content-Transfer-Encoding:");
SZXTRN const TCHAR pszStdContentDisposition[]=_T("Content-Disposition:");
SZXTRN const TCHAR pszStdContentDescription[]=_T("Content-Description:");
SZXTRN const TCHAR pszStdContentDuration[]=_T("Content-Duration:");
SZXTRN const TCHAR pszStdContentLanguage[]=_T("Content-Language:");
SZXTRN const TCHAR pszStdContentMD5Hdr[]=_T("Content-MD5:");
SZXTRN const TCHAR pszStdMessageIDHdr[]=_T("Message-ID:");
SZXTRN const TCHAR pszStdImportanceHdr[]=_T("Importance:");
SZXTRN const TCHAR pszStdSensitivityHdr[]=_T("Sensitivity:");
SZXTRN const TCHAR pszStdStatusHdr[]=_T("Status:");
SZXTRN const TCHAR pszStdReturnReceiptToHdr[]=_T("Return-Receipt-To:");
SZXTRN const TCHAR pszStdReturnReceiptCcHdr[]=_T("Return-Receipt-Cc:");
SZXTRN const TCHAR pszStdDispositionNotificationToHdr[]=_T("Disposition-Notification-To:");

SZXTRN const TCHAR pszStdResentFromHdr[]=_T("Resent-From:");
SZXTRN const TCHAR pszStdResentSenderHdr[]=_T("Resent-Sender:");
SZXTRN const TCHAR pszStdResentToHdr[]=_T("Resent-To:");
SZXTRN const TCHAR pszStdResentCcHdr[]=_T("Resent-Cc:");
SZXTRN const TCHAR pszStdResentDateHdr[]=_T("Resent-Date:");
SZXTRN const TCHAR pszStdResentMessageIDHdr[]=_T("Resent-Message-ID:");

SZXTRN const TCHAR pszStdApparentlyToHdr[]=_T("Apparently-To:");
SZXTRN const TCHAR pszStdApparentlyCcHdr[]=_T("Apparently-Cc:");
SZXTRN const TCHAR pszStdApparentlyBccHdr[]=_T("Apparently-Bcc:");

/*---------------------------------------------------------------------------*/

/* NULL terminated list of all standard e-mail headers */
SZXTRN LPCTSTR pszStdEMailHdrs[]={
	pszStdFromHdr,
	pszStdSenderHdr,
	pszStdToHdr,
	pszStdCcHdr,
	pszStdBccHdr,
	pszStdDateHdr,
	pszStdMessageIDHdr,
	pszStdMIMEVersionHdr,

	pszStdImportanceHdr,
	pszStdSensitivityHdr,
	pszStdStatusHdr,
	pszStdReturnReceiptToHdr,
	pszStdReturnReceiptCcHdr,
	pszStdReplyToHdr,
	pszStdInReplyToHdr,
	pszStdReturnPathHdr,

	pszStdReceivedHdr,
	pszStdContentTypeHdr,
	pszStdContentLengthHdr,
	pszStdContentIDHdr,
	pszStdContentLocationHdr,
	pszStdContentBaseHdr,
	pszStdContentXferEncoding,
	pszStdContentDisposition,
	pszStdContentDescription,
	pszStdContentDuration,
	pszStdContentLanguage,
	pszStdContentMD5Hdr,

	pszStdDispositionNotificationToHdr,

	pszStdResentFromHdr,
	pszStdResentSenderHdr,
	pszStdResentToHdr,
	pszStdResentCcHdr,
	pszStdResentDateHdr,
	pszStdResentMessageIDHdr,

	pszStdApparentlyToHdr,
	pszStdApparentlyCcHdr,
	pszStdApparentlyBccHdr,

	NULL	/* mark end of list */
};

/*---------------------------------------------------------------------------*/

/* some "well-known" X-hdrs */
SZXTRN const TCHAR pszXPriorityHdr[]=_T("X-Priority:");
SZXTRN const TCHAR pszXMailerHdr[]=_T("X-Mailer:");

/*---------------------------------------------------------------------------*/

/* special "headers" generated for information */
SZXTRN const TCHAR szXMsgSize[]=_T("X-Msg-Size:");
SZXTRN const TCHAR szXBody[]=_T("X-Body:");
SZXTRN const TCHAR szXSockVal[]=_T("X-SockVal:");
SZXTRN const TCHAR szXLogfError[]=_T("X-Logf-Error:");
SZXTRN const TCHAR szXContHdr[]=_T("X-Cont-Hdr");
SZXTRN const TCHAR szXConnAbort[]=_T("X-Conn-Abort:");
SZXTRN const TCHAR szXAuthAbort[]=_T("X-Auth-Abort:");
SZXTRN const TCHAR szXHdrParser[]=_T("X-Hdr-Parser:");
SZXTRN const TCHAR szXThreadStart[]=_T("X-Thread-Start:");
SZXTRN const TCHAR szXDynamicServer[]=_T("X-Dynamic-Server:");
SZXTRN const TCHAR szXThreadEnd[]=_T("X-Thread-End:");

/*---------------------------------------------------------------------------*/

SZXTRN const TCHAR pszMIMEBoundaryDelims[]=_T("--");

/* some well known values */

SZXTRN const TCHAR pszMIMEApplicationType[]=_T("application");
SZXTRN const TCHAR pszMIMEMultipartType[]=_T("multipart");
SZXTRN const TCHAR pszMIMEAudioType[]=_T("audio");
SZXTRN const TCHAR pszMIMEImageType[]=_T("image");
SZXTRN const TCHAR pszMIMETextType[]=_T("text");
SZXTRN const TCHAR pszMIMEMessageType[]=_T("message");

/* NULL terminated list of MIME types */
SZXTRN LPCTSTR pszMIMETypesList[]={
	pszMIMEMultipartType,
	pszMIMEApplicationType,
	pszMIMEAudioType,
	pszMIMEImageType,
	pszMIMETextType,
	pszMIMEMessageType,

	NULL	/* mark end */
};

/*---------------------------------------------------------------------------*/

SZXTRN const TCHAR pszMIMEMixedSubType[]=_T("mixed");
SZXTRN const TCHAR pszMIMEOctetStreamSubType[]=_T("octet-stream");
SZXTRN const TCHAR pszMIMETiffSubType[]=_T("tiff");
SZXTRN const TCHAR pszMIMEPlainSubType[]=_T("plain");
SZXTRN const TCHAR pszMIMEHtmlSubType[]=_T("html");
SZXTRN const TCHAR pszMIMEAlternativeSubType[]=_T("alternative");
SZXTRN const TCHAR pszMIMERfc822SubType[]=_T("rfc822");
SZXTRN const TCHAR pszMIMEVoiceMsgSubType[]=_T("voice-message");
SZXTRN const TCHAR pszMIMEFaxMsgSubType[]=_T("fax-message");
SZXTRN const TCHAR pszMIMEDirectorySubType[]=_T("directory");
SZXTRN const TCHAR pszMIME32KADPCMSubType[]=_T("32KADPCM");
SZXTRN const TCHAR pszMIMEReportSubType[]=_T("report");
SZXTRN const TCHAR pszMIMEDlvryStatusSubType[]=_T("delivery-status");
SZXTRN const TCHAR pszMIMERelatedSubType[]=_T("related");
SZXTRN const TCHAR pszMIMEParallelSubType[]=_T("parallel");
SZXTRN const TCHAR pszMIMEDigestSubType[]=_T("digest");
SZXTRN const TCHAR pszMIMEPngSubType[]=_T("png");
SZXTRN const TCHAR pszMIMEWaveSubType[]=_T("wav");
SZXTRN const TCHAR pszMIMEMSGSMSubType[]=_T("msgsm");
SZXTRN const TCHAR pszMIMEBasicSubType[]=_T("basic");
SZXTRN const TCHAR pszMIMEFormDataSubType[]=_T("form-data");

/*---------------------------------------------------------------------------*/

/* some non-standard multipart sub-types */
SZXTRN const TCHAR pszMIMEAppleDoubleSubType[]=_T("appledouble");
SZXTRN const TCHAR pszMIMEMSTNEFSubType[]=_T("ms-tnef");
SZXTRN const TCHAR pszMIMESMSMsgSubType[]=_T("sms-message");

/* NULL terminated list of MIME sub types */
SZXTRN LPCTSTR pszMIMESubTypesList[]={
	pszMIMEOctetStreamSubType,
	pszMIMEMixedSubType,
	pszMIMERelatedSubType,
	pszMIMEDigestSubType,
	pszMIMEParallelSubType,
	pszMIMETiffSubType,
	pszMIMEPlainSubType,
	pszMIMEHtmlSubType,
	pszMIMEAlternativeSubType,
	pszMIMERfc822SubType,
	pszMIMEVoiceMsgSubType,
	pszMIMEFaxMsgSubType,
	pszMIMEDirectorySubType,
	pszMIME32KADPCMSubType,
	pszMIMEReportSubType,
	pszMIMEDlvryStatusSubType,
	pszMIMEPngSubType,
	pszMIMEWaveSubType,
	pszMIMEMSGSMSubType,
	pszMIMEBasicSubType,
	pszMIMEFormDataSubType,

	/* some non-standard multipart sub-types */
	pszMIMEAppleDoubleSubType,
	pszMIMEMSTNEFSubType,
	pszMIMESMSMsgSubType,

	NULL	/* mark end */
};

/*---------------------------------------------------------------------------*/

SZXTRN const TCHAR pszMIMEBoundaryKeyword[]=_T("boundary");
SZXTRN const TCHAR pszMIMEFilenameKeyword[]=_T("filename");
SZXTRN const TCHAR pszMIMECharsetKeyword[]=_T("charset");
SZXTRN const TCHAR pszMIMENameKeyword[]=_T("name");
SZXTRN const TCHAR pszMIMEProfileKeyword[]=_T("profile");
SZXTRN const TCHAR pszMIMEVoiceKeyword[]=_T("voice");
SZXTRN const TCHAR pszMIMEVersionKeyword[]=_T("version");
SZXTRN const TCHAR pszMIMETypeKeyword[]=_T("type");
SZXTRN const TCHAR pszMIMEStartKeyword[]=_T("start");
SZXTRN const TCHAR pszMIMECodecKeyword[]=_T("codec");

/*---------------------------------------------------------------------------*/

/* NULL terminated list of keywords */
SZXTRN LPCTSTR lpszMIMEKeywordsList[]={
	pszMIMEBoundaryKeyword,
	pszMIMEFilenameKeyword,
	pszMIMECharsetKeyword,
	pszMIMENameKeyword,
	pszMIMEProfileKeyword,
	pszMIMEVoiceKeyword,
	pszMIMEVersionKeyword,
	pszMIMETypeKeyword,
	pszMIMEStartKeyword,
	pszMIMECodecKeyword,

	NULL	/* mark end */
};

/*---------------------------------------------------------------------------*/

SZXTRN const TCHAR pszMIMEAttachmentDisp[]=_T("attachment");
SZXTRN const TCHAR pszMIMEInlineDisp[]=_T("inline");

/* NULL terminated list of dispositions */
SZXTRN LPCTSTR lpszMIMEDispositions[]={
	pszMIMEAttachmentDisp,
	pszMIMEInlineDisp,

	NULL	/* mark end */
};

/*---------------------------------------------------------------------------*/

/* '\0' terminated list of characters not allowed in RFC822 atoms
 *
 * Note: includes only characters above (and including) space (ASCII 32). All
 *		characters below ASCII 32 or above ASCII 126 are automatically considered
 *		illegal in an atom. Use "IsRFC822AtomChar" wherever possible instead of
 *		doing your own checks.
 */
SZXTRN const TCHAR nonRFC822AtomChars[]={
	_T(' '),
	_T('<'), _T('>'),
	_T('['), _T(']'),
	_T('('), _T(')'),
	_T(':'), _T(';'), _T(','), _T('.'),
	_T('@'), _T('\\'),
	_T('\''), _T('"'),

	_T('\0')		/* always last */
};

/*---------------------------------------------------------------------------*/

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
BOOLEAN IsRFC822AtomChar (const TCHAR ch)
{
	if ((ch <= _T(' ')) || (ch >= (TCHAR) 0x7F))
		return FALSE;

	LPCTSTR	lpszPos=_tcschr(nonRFC822AtomChars, ch);
	if (lpszPos != NULL)
		return FALSE;

	return TRUE;
}

/*---------------------------------------------------------------------------*/

/*
 *		Returns EOK if the supplied data conforms to this defintion (Note:
 * an empty list is NOT considered an atom). See "IsRFC822AtomChar" for
 * specification what consists an atom
 */

EXC_TYPE CheckRFC822AtomText (LPCTSTR lpszAtom, const UINT32 ulALen)
{
	if (ulALen <= 0)
		return EEMPTYENTRY;
	if (NULL == lpszAtom)	/* should not happen */
		return ENUCBADBUF;

	LPCTSTR	lpszCurPos=lpszAtom;
	for (UINT32	aIndex=0; aIndex < ulALen; aIndex++, lpszCurPos++)
	{
		if (!IsRFC822AtomChar(*lpszCurPos))
			return ERESERVEPARAM;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

static LPCTSTR inetSkipWelcomeArgument (LPCTSTR lpszCurWPos)
{
	static const TCHAR szInetNonIgnoredWelcomePatternChars[]=_T("()[]{};");

	LPCTSTR	lpszWPos=lpszCurWPos;
	if (NULL == lpszWPos)
		return NULL;

	while ((!_istspace(*lpszWPos)) &&
		    (*lpszWPos != _T('\0')) &&
			 (NULL == _tcschr(szInetNonIgnoredWelcomePatternChars, *lpszWPos)))
		lpszWPos++;

	return lpszWPos;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE inetAnalyzeWelcomePattern (LPCTSTR			lpszWelcome,
												LPCTSTR			lpszWPattern,
												LPTSTR			lpszType,
												const UINT32	ulMaxTypeLen,
												LPTSTR			lpszVersion,
												const UINT32	ulMaxVerLen)
{
	EXC_TYPE	exc=EOK;
	LPCTSTR	lpszWPos=lpszWelcome, lpszPPos=lpszWPattern;

	if (IsEmptyStr(lpszWelcome) || IsEmptyStr(lpszWPattern) ||
		 (NULL == lpszType) || (0 == ulMaxTypeLen) ||
		 (NULL == lpszVersion) || (0 == ulMaxVerLen))
		return EPARAM;

	*lpszType = _T('\0');
	*lpszVersion = _T('\0');

	for ( ;(*lpszWPos != _T('\0')) && (*lpszPPos != _T('\0')); lpszPPos++)
	{
		/* ignore any spaces in the pattern or the welcome line */
		for ( ; _istspace(*lpszWPos) && (*lpszWPos != _T('\0')); lpszWPos++);
		for ( ; _istspace(*lpszPPos) && (*lpszPPos != _T('\0')); lpszPPos++);

		/* if either position is at EOS then no need to check any further */
		if ((_T('\0') == *lpszPPos) || (_T('\0') == *lpszWPos))
			break;

		if (INET_WPAT_MODIFIER != *lpszPPos)
		{
			if (*lpszPPos != *lpszWPos)
				return EIOUNCLASS;

			lpszWPos++;
			continue;
		}
		
		/* handle modifier */
		lpszPPos++;

		switch(*lpszPPos)
		{
			case INET_WPAT_SKIPTODELIM:
				*lpszPPos++;

				if ((_T('\0') == *lpszPPos) || (_T(' ') == *lpszPPos))
					return EUDFFORMAT;

				for ( ; (*lpszWPos != *lpszPPos) && (*lpszWPos != _T('\0')); lpszWPos++);

				/* fall through to normal comparison ... */

			case INET_WPAT_MODIFIER	: /* handles '%%' as well */
				if (*lpszPPos != *lpszWPos)
					return EIOUNCLASS;

				lpszWPos++;
				break;

			case INET_WPAT_RFC822DATE:
				{
					/* find end of RFC822 date/time by searching for the GMT offset */
					LPCTSTR	lpszDTStart=lpszWPos, lpszDTEnd=_tcschr(lpszDTStart, RFC822_POSITIVE_GMT);
					if (NULL == lpszDTEnd)
						lpszDTEnd = _tcschr(lpszDTStart, RFC822_NEGATIVE_GMT);
					if (NULL == lpszDTEnd)
						return EIOUNCLASS;

					/* skip GMT offset */
					LPCTSTR	lpszGMTOffset=lpszDTEnd;
					for (lpszDTEnd++; _istdigit(*lpszDTEnd) && (*lpszDTEnd != _T('\0')); lpszDTEnd++);
					LPCTSTR	lpszGMTOEnd=lpszDTEnd;

					/* skip to check if there is a timezone comment */
					for (lpszWPos=lpszDTEnd; _istspace(*lpszWPos) && (*lpszWPos != _T('\0')); lpszWPos++);

					/* if there is a timezone comment then skip it */
					if (RFC822_HDR_COMMENT_SDELIM == *lpszWPos)
					{
						for (lpszWPos++; (*lpszWPos != RFC822_HDR_COMMENT_EDELIM) && (*lpszWPos != _T('\0')); lpszWPos++);
						if (RFC822_HDR_COMMENT_EDELIM != *lpszWPos)
							return EIOUNCLASS;
						lpszWPos++;
					}

					const TCHAR	tch=(*lpszDTEnd);
					if (tch != _T('\0'))
						*((LPTSTR) lpszDTEnd) = _T('\0');	/* create EOS terminate string */

					struct tm dtv={ 0 };
					exc = DecodeRFC822DateTime(lpszDTStart, &dtv);
					if (tch != _T('\0'))	/* restore original */
						*((LPTSTR) lpszDTEnd) = tch;

					if (exc != EOK)
						return EIOUNCLASS;
				}
				break;

			case INET_WPAT_IGNORE		:
				/* check if ignore rest of welcome line */
				if (_T('\0') == *(lpszPPos+1))
				{
					if (*lpszType != _T('\0'))
						return EOK;
					else	/* if type yet undetermined, then try another pattern */
						return EIOUNCLASS;
				}

				/* ignore current alpha string */
				lpszWPos = inetSkipWelcomeArgument(lpszWPos);
				break;

			case INET_WPAT_TYPE			:
			case INET_WPAT_VERSION		:
				{
					LPTSTR	lpszArg=((INET_WPAT_TYPE == *lpszPPos) ? lpszType : lpszVersion), lpszArgEnd=strlast(lpszArg);
					UINT32	ulMaxLen=((INET_WPAT_TYPE == *lpszPPos) ? ulMaxTypeLen : ulMaxVerLen);
					UINT32	ulCurLen=_tcslen(lpszArg), ulArgLen=0;
					LPCTSTR	lpszArgStart=NULL;

					/* check if have type/version override */
					if (_T('=') == *(lpszPPos + 1))
					{
						lpszPPos += 2;

						if ((_T('\0') == *lpszPPos) || (_T(' ') == *lpszPPos))
							return EUDFFORMAT;

						for (lpszArgStart=lpszPPos, lpszPPos++; (!_istspace(*lpszPPos)) && (*lpszPPos != _T('\0')); lpszPPos++);
						ulArgLen = (lpszPPos - lpszArgStart);
						lpszPPos--;	/* compensate for automatic increment by loop */
					}
					else
					{
						lpszArgStart = lpszWPos;
						lpszWPos = inetSkipWelcomeArgument(lpszWPos);
						ulArgLen = (lpszWPos - lpszArgStart);
					}

					/* if expecting a modifier (type/version) and no data, then obviously no match */
					if (0 == ulArgLen)
						return EIOUNCLASS;

					if ((ulArgLen + ulCurLen + 1) >= ulMaxLen)
						return EOVERFLOW;

					/* concatenate to current value */
					if (ulCurLen > 0)
						lpszArgEnd = strladdch(lpszArgEnd, _T(' '));
					lpszArgEnd = strlncat(lpszArgEnd, lpszArgStart, ulArgLen);
					*lpszArgEnd = _T('\0');
				}
				break;

			default							:/* this point is reached for unknown modifier */
				return ELITERAL;
		}
	}	/* end of scanning pattern */

	/* check if reached end of pattern and end of welcome line simultaneously (and have a type) */
	if ((_T('\0') == *lpszPPos) && (_T('\0') == *lpszWPos) && (*lpszType != _T('\0')))
		return EOK;

	/* if this point is reached then no match was found */
	return EIOUNCLASS;
}

/*--------------------------------------------------------------------------*/

/* go over known patterns - returns EIOUNCLASS if no match found */
EXC_TYPE inetAnalyzeWelcome (LPCTSTR						lpszWelcome,
									  LPCTSTR						lpszPatterns[],
									  INETPROTO_WELCOME_ANCFN	lpfnAcfn,
									  LPTSTR							lpszType,
									  const UINT32					ulMaxTypeLen,
									  LPTSTR							lpszVersion,
									  const UINT32					ulMaxVerLen)
{
	EXC_TYPE	exc=EOK;
	UINT32	ulWdx=0, ulMatch=(UINT32) (-1);

	if ((NULL == lpszType) || (0 == ulMaxTypeLen) ||
		 (NULL == lpszVersion) || (0 == ulMaxVerLen))
		return EPARAM;

	*lpszType = _T('\0');
	*lpszVersion = _T('\0');

	if (NULL == lpfnAcfn)
		return EBADADDR;
	if (NULL == lpszPatterns)
		return EIOUNCLASS;

	for (ulWdx=0; ; ulWdx++)
	{
		LPCTSTR	lpszWPat=lpszPatterns[ulWdx];
		if (IsEmptyStr(lpszWPat))
			break;

		if (EIOUNCLASS != (exc=(*lpfnAcfn)(lpszWelcome, lpszWPat, lpszType, ulMaxTypeLen, lpszVersion, ulMaxVerLen)))
		{
			if (exc != EOK)
				break;

			/* if OK received, then prefer a pattern with a full identification */
			if ((*lpszType != _T('\0')) && (*lpszVersion != _T('\0')))
				return EOK;

			ulMatch = ulWdx;	/* remember it, in case no other match found */
		}

		*lpszType = _T('\0');
		*lpszVersion = _T('\0');
	}

	/* if this point is reached then no full match was found - check if partial match found */
	if (ulMatch != (UINT32) (-1))
		return (*lpfnAcfn)(lpszWelcome, lpszPatterns[ulMatch], lpszType, ulMaxTypeLen, lpszVersion, ulMaxVerLen);

	return exc;
}

/*--------------------------------------------------------------------------*/

#if defined(UNICODE) || defined(_UNICODE)
#	define GetWCharVal(tch) ((UINT16) tch)
#else
#	ifdef __cplusplus
inline UINT16 GetWCharVal (const TCHAR tch)
{
	return (((UINT16) tch) & 0x00FF);
}
#	else
#		define GetWCharVal(tch)	(((UINT16) (tch)) & 0x00FF)
#	endif	/* __cplusplus */
#endif	/* UNICODE */

/* Returns recommended encoding - (Q)uoted-Printable or (B)ase64
 *
 * Note: if no encoding required (i.e. RFC822_7BIT_ENC or RFC822_NONE_ENC) then returned
 *			encoding length is same as header length
 */
EXC_TYPE DetermineRFC822HdrValEncoding (LPCTSTR			lpszHdrValue,
													 const UINT32	ulHdrLen,
													 RFC822ENCCASE	*phdrEnc,
													 UINT32			*pulEncLen)
{
	EXC_TYPE	exc=EOK;
	UINT32	ulXChars=0, ulQPLen=0, ulB64Len=0;
	LPCTSTR	lpszHChar=lpszHdrValue;

	if ((NULL == phdrEnc) || (NULL == pulEncLen))
		return EPARAM;

	*phdrEnc = RFC822_NONE_ENC;
	*pulEncLen = 0;

	if (0 == ulHdrLen)
		return EOK;
	if (IsEmptyStr(lpszHdrValue))
		return EBADHEADER;

	/* Note: at end of loop "ulEncLen" holds total string length */
	for (; (*pulEncLen) < ulHdrLen; lpszHChar++, (*pulEncLen)++)
	{
		UINT16	wChar=GetWCharVal(*lpszHChar);

		/* UTF-8 is transparent to characters up to 0x9f, but in order not to take chances, we stop at 0x7F */
		if ((wChar < (UINT16) 0x0020) || (wChar > (UINT16) 0x007F))
			ulXChars++;
		else if (!fIsQPXferChar(*lpszHChar))
			ulQPLen += (1 + MAX_BYTE_HEX_DISPLAY_LENGTH);
	}

	/* if no "translatable" characters then do nothing */
	if (0 == ulXChars)
		return EOK;

	// check which encoding requires less space
	if ((exc=b64_calc_encode_size(ulHdrLen, FALSE, &ulB64Len)) != EOK)
		return exc;

	ulQPLen += (ulHdrLen - ulXChars) + (ulXChars * (1 + MAX_BYTE_HEX_DISPLAY_LENGTH));

	if (ulB64Len < ulQPLen)
	{
		*pulEncLen = ulB64Len;
		*phdrEnc = RFC822_B64_ENC;
	}
	else
	{
		*pulEncLen = ulQPLen;
		*phdrEnc = RFC822_QP_ENC;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

/* Note: encoding MUST be either QP or BASE64 (otherwise error returned) */
EXC_TYPE BuildRFC822CharsetHdrValue (LPCTSTR					lpszCharset,
												 const RFC822ENCCASE	eHdrEnc,
												 LPCTSTR					lpszHdrValue,
												 const UINT32			ulHdrLen,
												 LPTSTR					lpszOutput,
												 const UINT32			ulMaxLen)
{
	EXC_TYPE	exc=EOK;
	LPTSTR	lsp=lpszOutput;
	UINT32	ulRemLen=ulMaxLen, iLen=0, oLen=0;

	if (IsEmptyStr(lpszCharset) || IsEmptyStr(lpszHdrValue) || (0 == ulHdrLen) ||
		 ((RFC822_B64_ENC != eHdrEnc) && (RFC822_QP_ENC != eHdrEnc)) ||
		 (NULL == lpszOutput) || (ulMaxLen <= BASE64_OUTPUT_BLOCK_LEN))
		return EPARAM;
	*lpszOutput = _T('\0');

	/* build charset prefix */
	if ((exc=strlinsch(&lsp, ISOSTRDELIM, &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsch(&lsp, ISOENCDELIM, &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsstr(&lsp, lpszCharset, &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsch(&lsp, ISOENCDELIM, &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsch(&lsp, ((RFC822_B64_ENC == eHdrEnc) ? ISOB64ENCDELIM : ISOQPENCDELIM), &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsch(&lsp, ISOENCDELIM, &ulRemLen)) != EOK)
		return exc;

	if (RFC822_B64_ENC == eHdrEnc)
	{
		if ((exc=b64_encode_finish((const UINT8 *) lpszHdrValue, ulHdrLen, lsp, ulRemLen, &oLen)) != EOK)
			return exc;

		lsp += oLen;
		*lsp = _T('\0');
		ulRemLen -= oLen;
	}
	else	/* it must be QP encoding */
	{
		LPTSTR	lpszOBuf=lsp;

		if ((exc=qp_encode_buf(lpszHdrValue, ulHdrLen, &iLen, lsp, ulRemLen, &oLen)) != EOK)
			return exc;

		/* make sure entire buffer encoded */
		if (iLen != ulHdrLen)
			return ESTATE;

		lsp += oLen;
		*lsp = _T('\0');
		ulRemLen -= oLen;

		/* as per RFC1342 we need to encode '?' and '_' as their QP counterparts, and ' ' as '_' */
		for ( ; *lpszOBuf != _T('\0'); lpszOBuf++)
		{
			switch(*lpszOBuf)
			{
				case _T(' ')	:
					*lpszOBuf = _T('_');
					break;

				case _T('\t')	: /* although TAB should not appear in headers, it might */
				case _T('?')	:
				case _T('_')	:
					{
						LPTSTR	lpszBufEnd=lsp;
						TCHAR		chNextChar=*(lpszOBuf+1);

						if (ulRemLen <= MAX_BYTE_HEX_DISPLAY_LENGTH)
							return EOVERFLOW;

						/* move string to make room for HEX display */
						for ( ; lpszBufEnd > lpszOBuf; lpszBufEnd--)
							lpszBufEnd[MAX_BYTE_HEX_DISPLAY_LENGTH] = *lpszBufEnd;

						ulRemLen -= MAX_BYTE_HEX_DISPLAY_LENGTH;
						lsp += MAX_BYTE_HEX_DISPLAY_LENGTH;

						if ((exc=add_QP_encoding(*lpszOBuf, &lpszOBuf)) != EOK)
							return exc;

						/* we need to restore the original following char since it is overwritten by the '\0' */
						*lpszOBuf = chNextChar;

						/* counter the '++' effect of the loop */
						lpszOBuf--;
					}
					break;

				default			:	/* do nothing */;
			}
		}
	}

	if ((exc=strlinsch(&lsp, ISOENCDELIM, &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsch(&lsp, ISOSTRDELIM, &ulRemLen)) != EOK)
		return exc;

	return EOK;
}

/*--------------------------------------------------------------------------*/

/*		Determines best encoding with charset and performs it. If no translation required,
 * then returns immediately (and the recommened header encoding will NOT be QP or Base64)
 */
EXC_TYPE BuildCanonicalRFC822CharsetHdrValue (LPCTSTR			lpszCharset,
															 LPCTSTR			lpszHdrValue,
															 const UINT32	ulHdrLen,
															 RFC822ENCCASE	*phdrEnc,
															 LPTSTR			lpszOutput,
															 const UINT32	ulMaxLen)
{
	UINT32	ulEncLen=0;
	EXC_TYPE	exc=DetermineRFC822HdrValEncoding(lpszHdrValue, ulHdrLen, phdrEnc, &ulEncLen);
	if (exc != EOK)
		return exc;

	if ((NULL == lpszOutput) || (0 == ulMaxLen) || (NULL == phdrEnc))
		return EBADBUFF;
	*lpszOutput = _T('\0');

	if ((RFC822_B64_ENC != *phdrEnc) && (RFC822_QP_ENC != *phdrEnc))
		return EOK;

	if ((exc=BuildRFC822CharsetHdrValue(lpszCharset, (*phdrEnc), lpszHdrValue, ulHdrLen, lpszOutput, ulMaxLen)) != EOK)
		return exc;

	return EOK;
}

/*--------------------------------------------------------------------------*/

SZXTRN const TCHAR szNonMailUserLocalMailPart[]=_T("non-mail-user");
SZXTRN const TCHAR szNonMailUserMailDomainPart[]=_T("missing.domain.net");

BOOLEAN IsNonMailUserLocalMailPart (LPCTSTR lpszPart, const UINT32 ulPartLen)
{
	if (IsEmptyStr(lpszPart) || (ulPartLen != _tcslen(szNonMailUserLocalMailPart)))
		return FALSE;

	return (0 == _tcsnicmp(lpszPart, szNonMailUserLocalMailPart, ulPartLen));
}

BOOLEAN IsNonMailUserMailDomainPart (LPCTSTR lpszPart, const UINT32 ulPartLen)
{
	if (IsEmptyStr(lpszPart) || (ulPartLen != _tcslen(szNonMailUserMailDomainPart)))
		return FALSE;

	return (0 == _tcsnicmp(lpszPart, szNonMailUserMailDomainPart, ulPartLen));
}

/* Note: returns TRUE if either (!) the local part or the domain are non-mail-user */
BOOLEAN IsNonMailUserEmailAddress (LPCTSTR lpszAddr, const UINT32 ulAddrLen)
{
	/* address must be at least "a@b.c" */
	if (IsEmptyStr(lpszAddr) || (ulAddrLen <= 5))
		return FALSE;

	LPCTSTR	lpszDomain=_tcsnrchr(lpszAddr, INET_DOMAIN_SEP, ulAddrLen);
	if (IsEmptyStr(lpszDomain))
		return FALSE;

	const UINT32	ulALen=(lpszDomain - lpszAddr);
	if (!IsNonMailUserLocalMailPart(lpszAddr, ulALen))
		return FALSE;

	if (!IsNonMailUserMailDomainPart((lpszDomain+1), (ulAddrLen - (ulALen + 1))))
		return FALSE;

	return TRUE;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE ValidateRFC822MessageIDChars (LPCTSTR lpszMsgID, const UINT32 ulIDLen)
{
	const TCHAR	chSDelim=((NULL == lpszMsgID) ? _T('\0') : *lpszMsgID);
	LPCTSTR		lpszCurPos=lpszMsgID;
	UINT32		ulCurPos=0;

	if ((NULL == lpszMsgID) || (0 == ulIDLen))
		return EEMPTYENTRY;

	for (ulCurPos = 0; ulCurPos < ulIDLen; ulCurPos++, lpszCurPos++)
	{
		const TCHAR	chPos=*lpszCurPos;
		if ((chPos < _T(' ')) || (chPos > (TCHAR) 0x7E))
			return EPARSETABLES;
	}

	/* if have start delimiter then make sure that ending one exists as well */
	if (RFC822_MSGID_SDELIM == chSDelim)
	{
		if (*(lpszMsgID + (ulIDLen - 1)) != RFC822_MSGID_EDELIM)
			return EUNMATCHEDLISTS;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

// NOTE: returns EOK if zero ID length to begin with
EXC_TYPE StripRFC822MessageIDRemark (LPCTSTR lpszMsgID, const UINT32 ulIDLen, /* OUT */ LPCTSTR& lpszEffID, /* OUT */ UINT32& ulEffLen)
{
	UINT32	ulCurPos=0;
	LPCTSTR	lpszCurPos=lpszMsgID;

	lpszEffID = NULL;
	ulEffLen = 0;

	// if zero length, don't care what the pointer says
	if (ulIDLen <= 0)
		return EOK;

	if (NULL == lpszMsgID)
		return EPARAM;

	// skip starting whitespace
	for (	; _istspace(*lpszCurPos) && (ulCurPos < ulIDLen); lpszCurPos++, ulCurPos++);
	if (ulCurPos >= ulIDLen)	// OK if exhausted all white-space
		return EOK;

	lpszEffID = lpszCurPos;
	// check if delimited ID
	if (RFC822_MSGID_SDELIM == *lpszEffID)
	{
		for (lpszCurPos++, ulCurPos++; (ulCurPos < ulIDLen); lpszCurPos++, ulCurPos++)
		{
			if (RFC822_MSGID_EDELIM == *lpszCurPos)
				break;
		}

		// make sure stopped because found end delimiter 
		if (ulCurPos >= ulIDLen)
			return EUNMATCHEDLISTS;

		lpszCurPos++;	// skip delimiter
	}
	// if comment PRECEDING the ID, then skip it and retry recursively
	else if (RFC822_HDR_COMMENT_SDELIM == *lpszEffID)
	{
		for (lpszCurPos++, ulCurPos++; (ulCurPos < ulIDLen); lpszCurPos++, ulCurPos++)
		{
			if (RFC822_HDR_COMMENT_EDELIM == *lpszCurPos)
				break;
		}

		// make sure stopped because found end delimiter 
		if (ulCurPos >= ulIDLen)
			return EOVERLAY;

		lpszCurPos++;	// skip the comment end delimiter

		const UINT32	ulSkippedLen=(lpszCurPos - lpszMsgID);
		// if exhausted ID on remark, then assume empty ID
		if (ulSkippedLen >= ulIDLen)
		{
			lpszEffID = NULL;
			return EOK;
		}

		// try recursively now that we stripped the preceding remark
		return StripRFC822MessageIDRemark(lpszCurPos, (ulIDLen - ulSkippedLen), lpszEffID, ulEffLen);
	}
	else	// non-delimited ID - stop at first white-space or '('
	{
		for ( ; (ulCurPos < ulIDLen); lpszCurPos++, ulCurPos++)
		{
			if ((RFC822_HDR_COMMENT_SDELIM == *lpszCurPos) || _istspace(*lpszCurPos))
				break;
		}
	}

	// this point assumes that we are one character AFTER the end of the ID
	if (0 == (ulEffLen=(lpszCurPos - lpszEffID)))
		lpszEffID = NULL;

	return EOK;
}

/*--------------------------------------------------------------------------*/
