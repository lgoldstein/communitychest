#include <ctype.h>

#include <util/string.h>
#include <util/errors.h>
#include <util/tables.h>

#include <internet/imap4Lib.h>
#include <internet/base64.h>
#include <internet/rfc822.h>

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
/*
 *		Reads a client command - if {} delimited literals are encountered then
 * they are handled by issuing the '+' continuation prompt. As a result, the
 * returned line contains no such literals.
 */
int imap4ReadCmd (ISockioInterface&	CBSock,
						char					szBuf[],
						const size_t		sMaxLen,
						const LONG			maxSecs,
						BOOLEAN				*pfStrippedCRLF)
{
	if (0 == sMaxLen)
		return 0;
	if (NULL == szBuf)
		return (-3);
	if (pfStrippedCRLF != NULL)
		*pfStrippedCRLF = FALSE;

	char		*lp=szBuf;
	size_t	rLen=0, uLen=sMaxLen;

	*lp = _T('\0');
	while ((rLen < sMaxLen) && (uLen > 0))
	{
		int	cLen=CBSock.ReadCmd(lp, uLen, maxSecs, pfStrippedCRLF);
		if (cLen < 0)
			return cLen;

		// handle case of octet count
		char *ocp=_tcschr(lp, IMAP4_OCTCNT_SDELIM);
		if (ocp != NULL)
		{
			*ocp = _T('\0');
			cLen = (ocp - lp);
			ocp++;
		}

		if ((size_t) cLen > uLen)
			return (-2);

		rLen += cLen;
		lp += cLen;
		uLen -= cLen;

		if (pfStrippedCRLF != NULL)
		{
			if (!(*pfStrippedCRLF))
				break;
		}

		if (NULL == ocp)
			break;

		// this point is reached if have octet count
		char *ecp=_tcschr(ocp, IMAP4_OCTCNT_EDELIM);
		if (NULL == ecp)
			return (-4);
		*ecp = _T('\0');

		// make sure we can accomodate next octets
		EXC_TYPE	exc=EOK;
		UINT32	ulOcNum=argument_to_dword(ocp, strlen(ocp), EXC_ARG(exc));
		if (exc != EOK)
			return (-5);
		if (ulOcNum > uLen)
			return (-6);

		int wLen=CBSock.WriteCmdf("%c enter next %s octets\r\n", IMAP4_CONTINUE_RSP, ocp);
		if (wLen <= 0)
			return ((wLen < 0) ? wLen : (-1));
	}

	return rLen;
}
#endif	/* of __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
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
EXC_TYPE imap4ReadRsp (ISockioInterface&	SBSock,
							  char					szBuf[],
							  const size_t			sMaxLen,
							  size_t&				rLen,
							  const LONG			maxSecs)
{
	if ((NULL == szBuf) || (0 == sMaxLen))
		return EPARAM;

	char		*lp=szBuf;
	SINT32	lPrevCnt=(-1);
	size_t	uLen=sMaxLen;

	for (rLen=0,*lp='\0'; (rLen < sMaxLen) && (uLen > 0); )
	{
		int	cLen=SBSock.ReadCmd(lp, uLen, maxSecs);
		if (cLen < 0)
			return ENOTCONNECTION;

		// handle case of (current) octet count
		SINT32	oCount=(-1);
		char		*ocp=_tcschr(lp, IMAP4_OCTCNT_SDELIM);
		if (ocp != NULL)
		{
			*ocp = _T('\0');
			cLen = (ocp - lp);

			// skip preceding whitespace
			for (ocp++; _istspace(*ocp) && (*ocp != _T('\0')); ocp++);

			char *ecp=_tcschr(ocp, IMAP4_OCTCNT_EDELIM);
			if (NULL == ecp)
				return EUDFFORMAT;

			// skip following whitespace
			for (ecp--; _istspace(*ecp) && (ecp > ocp); ecp--);
			ecp++;
			*ecp = _T('\0');

			EXC_TYPE	exc=EOK;
			oCount = (SINT32) argument_to_dword(ocp, strlen(ocp), EXC_ARG(exc));
			if (exc != EOK)
				return exc;
		}

		// handle case of (previous) octet count
		if (lPrevCnt > 0)
			cLen = min(cLen, lPrevCnt);
		lPrevCnt = oCount;

		lp += cLen;
		*lp = _T('\0');

		rLen += cLen;
		uLen -= cLen;

		if (oCount > 0)
		{
			char	*pp=(lp - 1);

			if (!_istspace(*pp))
			{
				lp = strladdch(lp, _T(' '));
				rLen++;
				uLen--;
			}
		}
		else	// no octet count to follow
			break;
	}

	if ((IMAP4_UNTAGGED_RSP == szBuf[0]) &&
		 _istspace(szBuf[1]) && (szBuf[1] != _T('\0')))
		return EDATACHAIN;

	return EOK;
}
#endif	/* of __cplusplus */

/*--------------------------------------------------------------------------*/

EXC_TYPE imap4GetArg (const char		lpszLine[],
							 char				lpszArg[],
							 const UINT32	ulMaxArgLen,
							 const char		**lppszNext)
{
	const char	*lpszS=lpszLine;
	char			*lpszD=lpszArg;
	UINT32		ulIdx=0;

	if (IsEmptyStr(lpszLine) || (NULL == lppszNext) ||
		 (NULL == lpszArg) || (0 == ulMaxArgLen))
		return EPARAM;

	*lpszArg = _T('\0');
	*lppszNext = NULL;

	/* skip intermediate white-space */
	for ( ; _istspace(*lpszS) && (_T('\0') != *lpszS); lpszS++);

	/* handle delimited arguments */
	if (('\"' == *lpszS) || ('\'' == *lpszS))
	{
		char			cDelim=(*lpszS);
		const char	*lpszE=_tcschr((lpszS+1), cDelim);
		UINT32		ulALen=0;

		if (NULL == lpszE)
			return EUDFFORMAT;

		lpszS++;
		ulALen = (lpszE - lpszS);
		if (ulALen >= ulMaxArgLen)
			return EOVERFLOW;

		strncpy(lpszD, lpszS, ulALen);
		lpszD += ulALen;
		*lpszD = _T('\0');

		lpszS = (lpszE + 1);
	}
	else	/* non-delimited argument */
	{
		for (ulIdx = 0; ulIdx < ulMaxArgLen; ulIdx++, lpszS++, lpszD++)
		{
			*lpszD = *lpszS;

			if (_istspace(*lpszS) || (_T('\0') == *lpszS))
			{
				*lpszD = _T('\0');	/* terminate arg */
				break;
			}
		}

		if (ulIdx >= ulMaxArgLen)
			return EOVERFLOW;
	}

	/* skip intermediate white-space */
	for ( ; _istspace(*lpszS) && (_T('\0') != *lpszS); lpszS++);

	*lppszNext = lpszS;
	return EOK;
}

/*---------------------------------------------------------------------------*/

/* Converts modified UTF-7/BASE64 folder name into local representation
 *
 * Note: input string may be changed (even if non-EOK code returned)
 */
EXC_TYPE imap4AdjustFolderName (LPTSTR			lpszFldrName,
										  const UINT32 ulMaxLen,
										  UINT32			*pulFNLen)
{
	LPTSTR	lp=lpszFldrName;

	if (IsEmptyStr(lpszFldrName) || (0 == ulMaxLen) || (NULL == pulFNLen))
		return EPARAM;
	*pulFNLen = 0;

	if (NULL == (lp=_tcschr(lpszFldrName, IMAP4_AMPERSAND_DELIM)))
		lp = strlast(lpszFldrName);

	while (*lp != _T('\0'))
	{
		EXC_TYPE	exc=EOK;
		LPTSTR	tsp=lp, esp=NULL;
		TCHAR		szPad[BASE64_OUTPUT_BLOCK_LEN+2];
		UINT32	ulEncLen=0, iLen=0, oLen=0, pLen=0, dLen=0;

		/* "&-" combination is used to represent a single '&' */
		if (_T('-') == *(lp+1))
		{
			lp++;
			_tcscpy(lp, (lp+1));

			tsp = lp;
			if (NULL == (lp=_tcschr(tsp, IMAP4_AMPERSAND_DELIM)))
			{
				lp = strlast(tsp);
				break;
			}

			continue;
		}

		/* this point is reached when start of modified BASE64 signalled */

		_tcscpy(lp, (lp+1));	/* remove '&' from string */

		/* find end of encoding */
		if (NULL == (tsp=_tcschr(lp, _T('-'))))
			return EUDFFORMAT;

		ulEncLen = (tsp - lp);

		/* replace ',' with '/' as required by modified BASE64 */
		for (iLen=0, esp=lp; iLen < ulEncLen; iLen++, esp++)
			if (_T(',') == *esp)
				*esp = _T('/');
			else if (_istspace(*esp))	/* spaces not allowed */
				return EPATH;

		/* Create a correctly padded last block if needed */
		pLen = (ulEncLen % BASE64_OUTPUT_BLOCK_LEN);

		if ((dLen=(ulEncLen - pLen)) < ulEncLen)
		{
			_tcsncpy(szPad, (lp + dLen), BASE64_OUTPUT_BLOCK_LEN);
			for (iLen=pLen; iLen < BASE64_OUTPUT_BLOCK_LEN; iLen++)
				szPad[iLen] = BASE64_PAD_CHAR;
			szPad[BASE64_OUTPUT_BLOCK_LEN] = _T('\0');
		}

		exc = b64_decode_buf(lp, dLen, &iLen, (UINT8 *) lp, (ulEncLen+1), &oLen);
		if ((exc != EOK) && (exc != EEOF))
			return exc;

		/* 
		 *		We decode a multiple of BASE64_OUTPUT_BLOCK_LEN since "dLen" has
		 * been adjusted as such - so the decode length MUST equal the original
		 */
		if (iLen != dLen)
			return ECHECKEXCEPTION;

		/* decoding BASE64 must yield less than initial buffer size */
		if (iLen < oLen)
			return EBUFFEREDCONN;

		lp += oLen;

		/* handle trailer (if not entire buffer decoded) */
		if (pLen != 0)
		{
			exc = b64_decode_buf(szPad, BASE64_OUTPUT_BLOCK_LEN, &iLen, (UINT8 *) lp, (BASE64_INPUT_BLOCK_LEN+1), &oLen);
			if ((exc != EOK) && (exc != EEOF))
				return exc;

			lp += oLen;
		}

		_tcscpy(lp, (tsp + 1));	/* remove '-' at end of encoding */

		tsp = lp;
		if (NULL == (lp=_tcschr(tsp, IMAP4_AMPERSAND_DELIM)))
		{
			lp = strlast(tsp);
			break;
		}
	}

	if ((*pulFNLen=(lp - lpszFldrName)) > ulMaxLen)
		return EOVERFLOW;

	if (ulMaxLen > *pulFNLen)
		memset(lp, 0, (ulMaxLen - *pulFNLen));

	return EOK;
}

/*---------------------------------------------------------------------------*/

/*		Converts modified UTF-7/BASE64 folder name into local representation. All
 * '\0' preceded values are "reduced"
 *
 * Note: input string may be changed (even if non-EOK code returned)
 */
EXC_TYPE imap4CanonizeFolderName (LPTSTR			lpszFldrName,
											 const UINT32	ulMaxLen,
											 UINT32			*pulFNLen)
{
	LPTSTR	lpszDst=lpszFldrName;
	LPCTSTR	lpszSrc=lpszFldrName;
	UINT32	ulFLx=0;
	EXC_TYPE	exc=imap4AdjustFolderName(lpszFldrName, ulMaxLen, pulFNLen);
	if (exc != EOK)
		return exc;

	for (ulFLx=0; ulFLx < *pulFNLen; ulFLx++, lpszSrc++)
	{
		// allow for "transparent" UNICODE - i.e. 16 bit where first byte is zero
		if (_T('\0') == *lpszSrc)
			continue;

		if (lpszSrc != lpszDst)
			*lpszDst = *lpszSrc;
		lpszDst++;
	}
	*lpszDst = _T('\0');

	*pulFNLen = _tcslen(lpszFldrName);
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4EncodeFolderComp (LPCTSTR		lpszComp,
										  const UINT32	ulCLen,
										  LPTSTR			lpszEnc,
										  const UINT32	ulMaxLen)
{
	EXC_TYPE	exc=EOK;
	UINT32	oLen=0, ulRemLen=ulMaxLen, ulTLen=0;
	LPTSTR	lsp=lpszEnc, tsp=NULL;
	if ((NULL == lpszEnc) || (0 == ulMaxLen))
		return EBADBUFF;

	*lpszEnc = _T('\0');

	if (0 == ulCLen)
		return EOK;

	if ((exc=strlinsch(&lsp, IMAP4_AMPERSAND_DELIM, &ulRemLen)) != EOK)
		return exc;

	/* build "transparent" ASCII */
	for (tsp=lsp, oLen=0, ulTLen=ulRemLen; oLen < ulCLen; oLen++)
	{
		if ((exc=strlinseos(&tsp, &ulTLen)) != EOK)
			return exc;
		if ((exc=strlinsch(&tsp, lpszComp[oLen], &ulTLen)) != EOK)
			return exc;
	}

	/* build encoding somewhere further on */
	if ((exc=b64_encode_finish((const UINT8 *) lsp, (2 * ulCLen), tsp, ulTLen, &oLen)) != EOK)
	{
		if (exc != EEOF)
			return exc;
	}
	tsp[oLen] = _T('\0');

	/* now shift "down" the encoding */
	_tcscpy(lsp, tsp);

	/* remove BASE64 padding */
	for (tsp=lsp; *tsp != _T('\0'); tsp++)
	{
		/* delete padding */
		if (BASE64_PAD_CHAR == *tsp)
		{
			*tsp = _T('\0');
			break;
		}

		/* adjust modified BASE64 encoding */
		if (_T('/') == *tsp)
			*tsp = _T(',');
	}

	oLen = (tsp - lsp);
	lsp += oLen;
	ulRemLen -= oLen;

	if ((exc=strlinsch(&lsp, _T('-'), &ulRemLen)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4BuildCanonicalFolderName (LPCTSTR			lpszSrcFldr,
													 LPTSTR			lpszDstFldr,
													 const UINT32	ulMaxLen)
{
	LPCTSTR	tsp=lpszSrcFldr;
	LPTSTR	lsp=lpszDstFldr;
	UINT32	ulRemLen=ulMaxLen;

	if (IsEmptyStr(lpszSrcFldr) || (NULL == lpszDstFldr) || (0 == ulMaxLen))
		return EPARAM;

	for ( ; *tsp != _T('\0'); tsp++)
	{
		EXC_TYPE	exc=EOK;
		LPCTSTR	esp=tsp;
		UINT32	ulELen=0;

		/* handle '&' special case */
		if (IMAP4_AMPERSAND_DELIM == *tsp)
		{
			if ((exc=strlinsch(&lsp, *tsp, &ulRemLen)) != EOK)
				return exc;
			if ((exc=strlinsch(&lsp, _T('-'), &ulRemLen)) != EOK)
				return exc;

			continue;
		}

		/* handle "transparent" characters */
		if ((*tsp >= 0x20) && (*tsp <= 0x7e))
		{
			if ((exc=strlinsch(&lsp, *tsp, &ulRemLen)) != EOK)
				return exc;
			continue;
		}

		/* find end of non-transparent characters */
		for (tsp++; ((*tsp < 0x20) || (*tsp >= 0x7f)) && (*tsp != _T('\0')); tsp++);
		if ((exc=imap4EncodeFolderComp(esp, (tsp - esp), lsp, ulRemLen)) != EOK)
			return exc;

		ulELen = _tcslen(lsp);
		lsp += ulELen;
		ulRemLen -= ulELen;

		tsp--;	/* compensate for automatic ++ */
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

/*		Extracts the basic parameters of a IMAP4 client command. If successful,
 * then also returns the pointer to the first argument (if any).
 */
EXC_TYPE imap4ExtractCmd (const char	lpszCmd[],
								  char			lpszTag[],
								  const UINT32	ulMaxTagLen,
								  char			lpszOp[],
								  const UINT32	ulMaxOpLen,
								  const char	**lppszArgs)
{
	EXC_TYPE		exc=EOK;
	const char	*tsp=lpszCmd;

	if (IsEmptyStr(lpszCmd) || (NULL == lppszArgs) ||
		 (NULL == lpszTag) || (0 == ulMaxTagLen) ||
		 (NULL == lpszOp) || (0 == ulMaxOpLen))
		return EPARAM;

	*lpszTag = _T('\0');
	*lpszOp = _T('\0');
	*lppszArgs = NULL;

	if ((exc=imap4ExtractTag(tsp, lpszTag, ulMaxTagLen, &tsp)) != EOK)
		return exc;
	if ((exc=imap4GetArg(tsp, lpszOp, ulMaxOpLen, &tsp)) != EOK)
		return exc;

	*lppszArgs = tsp;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4ExtractUntaggedRsp (const char		lpszRsp[],
											 char				lpszTag[],
											 const UINT32	ulMaxTagLen,
											 char				lpszCode[],
											 const UINT32	ulMaxCodeLen,
											 const char		**lppszRspArgs)
{
	EXC_TYPE		exc=EOK;
	const char	*tsp=lpszRsp;

	if (IsEmptyStr(lpszRsp) || (NULL == lppszRspArgs) ||
		 (NULL == lpszTag) || (0 == ulMaxTagLen) ||
		 (NULL == lpszCode) || (0 == ulMaxCodeLen))
		return EPARAM;

	*lpszTag = _T('\0');
	*lpszCode = _T('\0');
	*lppszRspArgs = NULL;

	/* make sure this is indeed a valid unatgged response */
	if (*tsp != IMAP4_UNTAGGED_RSP)
		return EUDFFORMAT;
	tsp++;

	if (*tsp != ' ')
		return EUDFFORMAT;
	tsp++;

	if ((exc=imap4GetArg(tsp, lpszTag, ulMaxTagLen, &tsp)) != EOK)
		return exc;

	/* these response do not have fixed length continuation arguments */
	if ((_tcsicmp(lpszTag, IMAP4_BYE) != 0) &&
		 (_tcsicmp(lpszTag, szIMAP4StatusCmd) != 0) &&
		 (_tcsicmp(lpszTag, szIMAP4SearchCmd) != 0))
	{
		if ((exc=imap4GetArg(tsp, lpszCode, ulMaxCodeLen, &tsp)) != EOK)
			return exc;
	}

	*lppszRspArgs = tsp;
	return EOK;
}

/*---------------------------------------------------------------------------*/

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
EXC_TYPE imap4ExtractRsp (const char	lpszRsp[],
								  char			lpszTag[],
								  const UINT32	ulMaxTagLen,
								  char			lpszCode[],
								  const UINT32	ulMaxCodeLen,
								  const char	**lppszArgs)
{
	EXC_TYPE		exc=EOK;
	const char	*tsp=lpszRsp;

	if (IsEmptyStr(lpszRsp) || (NULL == lpszTag) || (0 == ulMaxTagLen) ||
		 (NULL == lpszCode) || (0 == ulMaxCodeLen) || (NULL == lppszArgs))
		return EPARAM;

	*lpszTag = _T('\0');
	*lpszCode = _T('\0');
	*lppszArgs = NULL;

	if (IMAP4_CONTINUE_RSP == *lpszRsp)
	{
		*lppszArgs = tsp;
		return ECONTINUED;
	}

	if (IMAP4_UNTAGGED_RSP == *lpszRsp)
	{
		exc = imap4ExtractUntaggedRsp(lpszRsp, lpszTag, ulMaxTagLen, lpszCode, ulMaxCodeLen, lppszArgs);
		if (exc != EOK)
			return exc;
		return EDATACHAIN;
	}

	if ((exc=imap4ExtractTag(tsp, lpszTag, ulMaxTagLen, &tsp)) != EOK)
		return exc;
	if ((exc=imap4GetArg(tsp, lpszCode, ulMaxCodeLen, &tsp)) != EOK)
		return exc;

	/* check returned response code */
	*lppszArgs = tsp;
	return imap4XlateRspCode(lpszCode);
}

/*---------------------------------------------------------------------------*/

/*		Extracts the IMAP4 opcode for which the response was returned - including
 * any bracketed response in between (e.g. [ALERT], [READ-WRITE]).
 */
EXC_TYPE imap4ExtractOpRsp (const char		*lpszOpRsp,
									 char				szBrcktRsp[],
									 const UINT32	ulMaxBrcktLen,
									 char				szRspOp[],
									 const UINT32	ulMaxRspOp,
									 const char		**lppszRspArgs)
{
	const char	*tsp=lpszOpRsp;

	if ((NULL == lpszOpRsp) || (NULL == szBrcktRsp) || (0 == ulMaxBrcktLen) ||
		 (NULL == szRspOp) || (0 == ulMaxRspOp) || (NULL == lppszRspArgs))
		return EPARAM;

	szBrcktRsp[0] = _T('\0');
	szRspOp[0] = _T('\0');

	while (_T('\0') != *tsp)
	{
		const char	*lsp=NULL;
		UINT32		ulCLen=0;

		/* skip any preceding white space */
		while (_istspace(*tsp) && (*tsp != _T('\0'))) tsp++;

		/* if not a bracketed response then continue */
		if (IMAP4_BRCKT_SDELIM != *tsp) break;

		tsp++;
		if (NULL == (lsp=_tcschr(tsp, IMAP4_BRCKT_EDELIM)))
			return EUDFFORMAT;

		if ((ulCLen=(lsp - tsp)) >= ulMaxBrcktLen)
			return ELIMIT;

		strncpy(szBrcktRsp, tsp, ulCLen);
		szBrcktRsp[ulCLen] = _T('\0');

		/* skip any following white space */
		for (tsp=(lsp+1); _istspace(*tsp) && (*tsp != _T('\0')); tsp++);
	}

	/* at this point we expect to find the response opcode */
	if (_T('\0') == *tsp)
		return EEMPTYENTRY;

	return imap4GetArg(tsp, szRspOp, ulMaxRspOp, lppszRspArgs);
}

/*---------------------------------------------------------------------------*/

/*		Extracts the IMAP4 tag, opcode and bracketed response */
EXC_TYPE imap4ExtractTaggedRsp (const char	lpszRsp[],
										  char			lpszTag[],
										  const UINT32	ulMaxTagLen,
										  char			lpszCode[],
										  const UINT32	ulMaxCodeLen,
										  char			szBrcktRsp[],
										  const UINT32	ulMaxBrcktLen,
										  const char	**lppszRspArgs)
{
	EXC_TYPE exc=imap4ExtractRsp(lpszRsp,lpszTag,ulMaxTagLen,lpszCode,ulMaxCodeLen,lppszRspArgs);
	if (exc != EOK)
		return exc;

	exc = imap4ExtractOpRsp(*lppszRspArgs,szBrcktRsp,ulMaxBrcktLen,lpszCode,ulMaxCodeLen,lppszRspArgs);
	if (exc != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

/* Note: if no referral is found, then an EOK with an empty server is returned */
EXC_TYPE imap4ExtractReferralRsp (const char		lpszRsp[],
											 char				lpszSrvr[],
											 const UINT32	ulMaxSrvLen)
{
	if ((NULL == lpszSrvr) || (ulMaxSrvLen < sizeof(NATIVE_WORD)))
		return EPARAM;
	*lpszSrvr = _T('\0');

	/* format: tag OK/NO/BAD [REFERRAL imap://<user>;AUTH=*@<server>/] */
	TCHAR		szTag[MAX_IMAP4_TAG_LEN+2]=_T(""), szRspCode[MAX_IMAP4_OPCODE_LEN+2]=_T("");
	LPCTSTR	lpszCont=NULL;
	EXC_TYPE	exc=imap4ExtractRsp(lpszRsp, szTag, MAX_IMAP4_TAG_LEN, szRspCode, MAX_IMAP4_OPCODE_LEN, &lpszCont);
	if (exc != EOK)
	{
		// ignore if got "NO" or "BAD" and proceed to check if this is a login referral
		if ((EPERMISSION != exc) && (ECONTEXT != exc))
			return exc;
	}

	if (*lpszCont != IMAP4_BRCKT_SDELIM)
		return EOK;

	/* it is OK if no more arguments */
	TCHAR szKwd[MAX_IMAP4_OPCODE_LEN+2]=_T("");
	if ((exc=imap4GetArg((lpszCont+1), szKwd, MAX_IMAP4_OPCODE_LEN, &lpszCont)) != EOK)
		return EOK;
	if (_tcsicmp(szKwd, szIMAP4ReferralBracketKwd) != 0)
		return EOK;

	static const TCHAR szIMAP4AuthRefPattern[]=_T("AUTH=*@");
	LPCTSTR	lpszRealServer=_tcsistr(lpszCont, szIMAP4AuthRefPattern);
	if (IsEmptyStr(lpszRealServer))
		return ELOGNAMENEXIST;

	const size_t	sRefOffset=_tcslen(szIMAP4AuthRefPattern);
	lpszRealServer += sRefOffset;
	/* accept stopping because of ']' and not only '/' */
	for (lpszCont = (lpszRealServer+1); (*lpszCont != _T('/')) && (*lpszCont != IMAP4_BRCKT_EDELIM) && (*lpszCont != _T('\0')); lpszCont++);
	if (_T('\0') == *lpszCont)
		return EUDFFORMAT;

	const UINT32	ulRSLen=(lpszCont - lpszRealServer);
	if (ulRSLen >= ulMaxSrvLen)
		return EOVERFLOW;

	/* make sure it is indeed a bracketed response */
	for ( ; (*lpszCont != IMAP4_BRCKT_EDELIM) && (*lpszCont != _T('\0')); lpszCont++);
	if (*lpszCont != IMAP4_BRCKT_EDELIM)
		return EFNODELIMIT;

	_tcsncpy(lpszSrvr, lpszRealServer, ulRSLen);
	lpszSrvr[ulRSLen] = _T('\0');

	return EOK;
}

/*---------------------------------------------------------------------------*/

/* Extracts a literal octet count - which MUST exists in the response.
 *
 * Returns:
 *
 *		EEXIST - if no literal count found
 *		EUDFFORMAT - if literal count syntax is illegal
 *		E??? - other errors
 */
EXC_TYPE imap4ExtractLiteralCount (LPCTSTR	lpszRsp,
											  UINT32		*pulCount,
											  BOOLEAN	*pfIsLitPlus)
{
	EXC_TYPE	exc=EOK;
	LPCTSTR	tsp=NULL, lsp=NULL;

	if (IsEmptyStr(lpszRsp) || (NULL == pulCount) || (NULL == pfIsLitPlus))
		return EPARAM;

	*pulCount = 0;
	*pfIsLitPlus = FALSE;

	/* search from end backwards (avoid confusion with quoted chars) */
	if (NULL == (tsp=_tcsrchr(lpszRsp, IMAP4_OCTCNT_SDELIM)))
		return EEXIST;

	/* skip preceding white space (if any) */
	for (tsp++; _istspace(*tsp) && (*tsp != _T('\0')); tsp++);

	/* detect count digits */
	for (lsp = tsp; isdigit(*lsp) && (*lsp != _T('\0')); lsp++);
	*pulCount = argument_to_dword(tsp, (lsp - tsp), EXC_ARG(exc));
	if (exc != EOK)
		return exc;

	/* skip preceding white space (if any) */
	for ( ; (*lsp != IMAP4_OCTCNT_EDELIM) && (*lsp != IMAP4_LITPLUS_CHAR) && _istspace(*lsp) && (*lsp != _T('\0')); lsp++);

	/* check if LITERAL+ option used */
	if (IMAP4_LITPLUS_CHAR == *lsp)
	{
		*pfIsLitPlus = TRUE;
		for (lsp++ ; (*lsp != IMAP4_OCTCNT_EDELIM) && _istspace(*lsp) && (*lsp != _T('\0')); lsp++);
	}

	/* make sure we find the ending delimiter */
	if (*lsp != IMAP4_OCTCNT_EDELIM)
		return EUDFFORMAT;

	return EOK;
}

/*---------------------------------------------------------------------------*/

/* extracts a list value (including the delimiting "()") */
EXC_TYPE imap4GetListValue (const char lpszValue[],
									 const char	**lppszList,
									 UINT32		*pulListLen)
{
	const char	*lpszS=lpszValue, *lpszE=NULL;
	UINT32		ulBCount=1UL;

	if ((NULL == lpszValue) || (NULL == lppszList) || (NULL == pulListLen))
		return EPARAM;

	*lppszList = NULL;
	*pulListLen = 0;

	/* skip any whitespace preceding the value */
	for ( ; _istspace(*lpszS) && (*lpszS != _T('\0')); lpszS++);

	for (lpszE=(lpszS+1); (ulBCount > 0) && (*lpszE != _T('\0')); lpszE++)
	{
		if (IMAP4_PARLIST_SDELIM == *lpszE)
			ulBCount++;
		else if (IMAP4_PARLIST_EDELIM == *lpszE)
			ulBCount--;
		else if (IMAP4_QUOTE_DELIM == *lpszE)
		{
			if (NULL == (lpszE=_tcschr((lpszE+1), IMAP4_QUOTE_DELIM)))
				return ECONTINUED;
		}
	}

	/* make sure we stopped because matched paranthesis */
	if (ulBCount != 0)
		return EUNMATCHEDLISTS;

	*lppszList = lpszS;
	*pulListLen = (lpszE - lpszS);

	return EOK;
}

/*---------------------------------------------------------------------------*/

/* Extracts a message modifier value from a response (which may contain it).
 *
 *	Returns:
 *		EEXIST - if modifier not found in response.
 */
EXC_TYPE imap4FindModifierValue (const char	lpszRsp[],
											const char	lpszModifier[],
											const char	**lppszVal,
											UINT32		*pulVLen)
{
	UINT32		ulCLen=0, ulMLen=0;
	const char	*lpszS=lpszRsp, *lpszE=NULL;

	if (IsEmptyStr(lpszModifier) || (NULL == lppszVal) || (NULL == pulVLen))
		return EPARAM;
		 
	*lppszVal = NULL;
	*pulVLen = 0;

	if ((NULL == lpszRsp) || (_T('\0') == *lpszRsp))
		return EEXIST;

	for (ulMLen=strlen(lpszModifier); (*lpszS != _T('\0')); lpszS = lpszE)
	{
		/* skip any whitespace */
		for (; _istspace(*lpszS) && (*lpszS != _T('\0')); lpszS++);

		// skip list delimiter (if any)
		if (IMAP4_PARLIST_SDELIM == *lpszS)
			lpszS++;

		/* find end of keyword */
		for (lpszE=(lpszS+1); (!_istspace(*lpszE)) && (*lpszE != _T('\0')); lpszE++);

		/* make sure this is the modifier we are looking for */
		ulCLen = (lpszE - lpszS);
		if ((ulCLen == ulMLen) && (strnicmp(lpszS, lpszModifier, ulCLen) == 0))
			break;
	}

	if (*lpszS == _T('\0'))
		return EEXIST;

	/* skip any whitespace preceding the value */
	for (lpszS=lpszE; _istspace(*lpszS) && (*lpszS != _T('\0')); lpszS++);

	/* handle list value(s) */
	if (IMAP4_PARLIST_SDELIM == *lpszS)
		return imap4GetListValue(lpszS, lppszVal, pulVLen);

	/* find end of value */
	for (lpszE=lpszS, ulCLen=0; (!_istspace(*lpszE)) && (*lpszE != IMAP4_PARLIST_EDELIM) && (*lpszE != _T('\0')); lpszE++, ulCLen++);

	*lppszVal = lpszS;
	*pulVLen = ulCLen;
	return EOK;
}

/*---------------------------------------------------------------------------*/

/* Note: if modifiers start with '(' then they MUST end with ')' */
EXC_TYPE imap4EnumModifiers (LPCTSTR				lpszMods,	/* may be empty/NULL */
									  IMAP4_MODSENUM_CFN	lpfnEcfn,
									  LPVOID					pArg)
{
	EXC_TYPE	exc=EOK;
	UINT32	ulModNdx=0;
	LPCTSTR	lpszMPos=lpszMods;
	TCHAR		chEDelim=_T('\0');
	BOOLEAN	fHaveExclusiveMod=FALSE, fContEnum=TRUE;

	if (NULL == lpfnEcfn)
		return EBADADDR;

	if (IsEmptyStr(lpszMods))
		return EOK;

	for ( ; _istspace(*lpszMPos) && (*lpszMPos != _T('\0')); lpszMPos++);
	if (IMAP4_PARLIST_SDELIM == *lpszMPos)
	{
		lpszMPos++;
		chEDelim = IMAP4_PARLIST_EDELIM;
	}

	for (ulModNdx=0 ; fContEnum && (*lpszMPos != chEDelim) && (*lpszMPos != _T('\0')); lpszMPos++, ulModNdx++)
	{
		LPCTSTR	lpszCurMod=lpszMPos, lpszModEnd=NULL;
		LPCTSTR	lpszModArg=NULL, lpszModArgEnd=NULL;
		TCHAR		tch=_T('\0');

		/* check if already have a special modifier */
		if (fHaveExclusiveMod)
			return EMULTIHOP;

		for (lpszCurMod ; _istspace(*lpszCurMod) && (*lpszCurMod != _T('\0')); lpszCurMod++);

		/* find end of modifier (including any arguments it might have) */
		for (lpszMPos=lpszCurMod; (!_istspace(*lpszMPos)) && (*lpszMPos != chEDelim) && (*lpszMPos != _T('\0')); lpszMPos++)
			if (IMAP4_BRCKT_SDELIM == *lpszMPos)
			{
				lpszModEnd = lpszMPos;

				if (lpszMPos == lpszCurMod)
					return EDEADLOCK;

				/* find end of argument */
				for (lpszMPos++, lpszModArg=lpszMPos; (*lpszMPos != IMAP4_BRCKT_EDELIM) && (*lpszMPos != _T('\0')); lpszMPos++);

				/* make sure stopped because found end of brackets */
				if (*lpszMPos != IMAP4_BRCKT_EDELIM)
					return EBADRQC;

				lpszModArgEnd = lpszMPos;
			}

		if (NULL == lpszModEnd)
			lpszModEnd = lpszMPos;
		tch = *lpszModEnd;
		*((LPTSTR) lpszModEnd) = _T('\0');

		if ((lpszModArg != NULL) && (lpszModArgEnd != NULL))
		{
			/* make sure only BODY and BODY.PEEK modifiers have arguments */
			if ((_tcsicmp(lpszCurMod, IMAP4_BODY) != 0) && (_tcsicmp(lpszCurMod, IMAP4_BODYPEEK) != 0))
			{
				*((LPTSTR) lpszModEnd) = tch;
				return EXFULL;
			}

			*((LPTSTR) lpszModArgEnd) = _T('\0');
		}

		// if this is a BODY/BODY.PEEK[] call, then send "[]" as the argument
		LPCTSTR	lpszCfnModArg=((IsEmptyStr(lpszModArg) && (lpszModArgEnd != NULL)) ? _T("[]") : lpszModArg);
		if (EOK == (exc=(*lpfnEcfn)(ulModNdx, lpszCurMod, lpszCfnModArg, pArg, &fContEnum)))
		{
			/* check if special modifier must be alone */
			if ((_tcsicmp(lpszCurMod, IMAP4_ALL) == 0) ||
				 (_tcsicmp(lpszCurMod, IMAP4_FAST) == 0) ||
				 (_tcsicmp(lpszCurMod, IMAP4_FULL) == 0))
			{
				if ((chEDelim != _T('\0')) || (ulModNdx != 0))
					exc = ENOSR;
				else
					fHaveExclusiveMod = TRUE;
			}
		}

		/* restore original strings */
		*((LPTSTR) lpszModEnd) = tch;
		if ((lpszModArg != NULL) && (lpszModArgEnd != NULL))
			*((LPTSTR) lpszModArgEnd) = IMAP4_BRCKT_EDELIM;

		if (exc != EOK)
			return exc;

		if (*lpszMPos == chEDelim)
			lpszMPos--;
	}

	/* make sure exited because syntax correct */
	if (*lpszMPos != chEDelim)
		return EUNMATCHEDLISTS;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4ExtractMsgStrMod (const char	lpszRsp[],
										  const char	lpszModifier[],
										  char			lpszVal[],
										  const UINT32	ulMaxLen)
{
	const char	*lpszMVal=NULL;
	UINT32		ulVLen=0;
	EXC_TYPE		exc=EOK;

	if ((NULL == lpszVal) || (0 == ulMaxLen))
		return EPARAM;

	*lpszVal = _T('\0');

	exc = imap4FindModifierValue(lpszRsp, lpszModifier, &lpszMVal, &ulVLen);
	if (exc != EOK) return exc;

	if (ulVLen >= ulMaxLen)
		return ELIMIT;

	strncpy(lpszVal, lpszMVal, ulVLen);
	lpszVal[ulVLen] = _T('\0');

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE imap4ExtractMsgNumMod (const char	lpszRsp[],
										  const char	lpszModifier[],
										  UINT32			*pulNum)
{
	char		szVal[MAX_DWORD_DISPLAY_LENGTH+2];
	EXC_TYPE	exc=EOK;

	if (NULL == pulNum)
		return EPARAM;
	*pulNum = 0;

	exc = imap4ExtractMsgStrMod(lpszRsp, lpszModifier, szVal, MAX_DWORD_DISPLAY_LENGTH);
	if (exc != EOK) return exc;

	*pulNum = argument_to_dword(szVal, strlen(szVal), EXC_ARG(exc));
	return exc;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE imap4UpdateMsgFlags (IMAP4_MSGFLAGS					*pFlags,
										const IMAP4_MSGFLAG_CASE	fCase,
										const BOOLEAN					fAddIt)
{
	if ((NULL == pFlags) || fIsBadIMAP4MsgFlagCase(fCase))
		return EPARAM;

	switch(fCase)
	{
		case IMAP4_SEEN_MSGCASE			: pFlags->m_fSeen = ((fAddIt) ? 1 : 0);		break;
		case IMAP4_ANSWERED_MSGCASE	: pFlags->m_fAnswered = ((fAddIt) ? 1 : 0);	break;
		case IMAP4_FLAGGED_MSGCASE		: pFlags->m_fFlagged = ((fAddIt) ? 1 : 0);	break;
		case IMAP4_DELETED_MSGCASE		: pFlags->m_fDeleted = ((fAddIt) ? 1 : 0);	break;
		case IMAP4_DRAFT_MSGCASE		: pFlags->m_fDraft = ((fAddIt) ? 1 : 0);		break;
		case IMAP4_RECENT_MSGCASE		: pFlags->m_fRecent = ((fAddIt) ? 1 : 0);		break;
		case IMAP4_PRIVATE_FLAGS		: pFlags->m_fPrivate = ((fAddIt) ? 1 : 0);	break;
		case IMAP4_EXTENSION_FLAGS		: pFlags->m_fExtended = ((fAddIt) ? 1 : 0);	break;
		default								: return EUNKNOWNEXIT;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE imap4EnumMsgFlags (LPCTSTR lpszFlags, IMAP4_FLAGS_ENUM_CFN lpfnEcfn, LPVOID pArg)
{
	UINT32	ulFlgIndex=0;
	LPCTSTR	lpszVal=lpszFlags;
	TCHAR		chDelim=_T('\0');

	if (NULL == lpfnEcfn)
		return EBADADDR;
	if (IsEmptyStr(lpszFlags))
		return EOK;

	/* skip any preceding spaces */
	for ( ; _istspace(*lpszVal) && (*lpszVal != _T('\0')); lpszVal++);
	if (IsEmptyStr(lpszVal) || (_tcsnicmp(IMAP4_NIL, lpszVal, _tcslen(IMAP4_NIL)) == 0))
		return EOK;

	if (IMAP4_PARLIST_SDELIM == (chDelim=*lpszVal))
		lpszVal++;

	for (ulFlgIndex=0; (*lpszVal != IMAP4_PARLIST_EDELIM) && (*lpszVal != _T('\0')) ; ulFlgIndex++)
	{
		EXC_TYPE	exc=EOK;
		LPCTSTR	lpszE=NULL;
		UINT32	ulVLen=0;
		BOOLEAN	fContEnum=TRUE;

		/* skip any preceding spaces */
		for ( ; _istspace(*lpszVal) && (*lpszVal != _T('\0')) && (*lpszVal != IMAP4_PARLIST_EDELIM); lpszVal++);
		if ((_T('\0') == *lpszVal) || (IMAP4_PARLIST_EDELIM == *lpszVal))
			break;

		/* skip till end of flag */
		for (lpszE = (lpszVal+1); (!_istspace(*lpszE)) && (*lpszE != IMAP4_PARLIST_EDELIM) && (*lpszE != _T('\0')); lpszE++);
		ulVLen = (lpszE - lpszVal);

		if ((exc=(*lpfnEcfn)(ulFlgIndex, lpszVal, ulVLen, pArg, &fContEnum)) != EOK)
			return exc;

		if (!fContEnum)
			return EOK;

		lpszVal = lpszE;	/* prepare for next iteration */
	}

	/* make sure we stopped because of the right reason */
	if (((IMAP4_PARLIST_SDELIM == chDelim) && (IMAP4_PARLIST_EDELIM != *lpszVal)) ||
		 ((IMAP4_PARLIST_SDELIM != chDelim) && (IMAP4_PARLIST_EDELIM == *lpszVal)))
		return EUDFFORMAT;

	return S_OK;
}

/*--------------------------------------------------------------------------*/

/* standard callback function for parsing flags (called by "imap4ParseMsgFlags") */
EXC_TYPE imap4StdMsgFlagsParseCfn (const UINT32	ulFlgIndex,	/* starts at zero */
											  LPCTSTR		lpszFlag,	/* raw flag string - NOTE: not necessarily EOS terminated !!! */
											  const UINT32	ulFlgLen,	/* length of string data */
											  LPVOID			pArg,			/* assumes an IMAP4_MSGFLAGS pointer */
											  BOOLEAN		*pfContEnum)
{
	IMAP4_MSGFLAGS	*pFlags=(IMAP4_MSGFLAGS *) pArg;
	if (NULL == pFlags)
		return ECONTEXT;

	/* use first call to zero the memory */
	if (0 == ulFlgIndex)
		memset(pFlags, 0, (sizeof *pFlags));

	if (IMAP4_SYSFLAG_SIGN == *lpszFlag)
	{
		const IMAP4_MSGFLAG_CASE	fCase=imap4XlateExtMsgFlag(lpszFlag, ulFlgLen);
		EXC_TYPE							exc=imap4UpdateMsgFlags(pFlags, fCase, TRUE);
		if (exc != EOK)
			return exc;
	}
	else	/* assume any non-system flag is a private one */
		pFlags->m_fExtended = 1;

	return EOK;
}

/*--------------------------------------------------------------------------*/

/* Note: looks for the "FLAGS" modifier in response */
EXC_TYPE imap4ExtractMsgFlags (const char lpszRsp[], IMAP4_MSGFLAGS *pFlags)
{
	EXC_TYPE		exc=EOK;
	const char	*lpszVal=NULL;
	UINT32		ulVLen=0;

	exc = imap4FindModifierValue(lpszRsp, IMAP4_FLAGS, &lpszVal, &ulVLen);
	if (exc != EOK)
		return exc;

	/* check for NIL atom */
	if ((strnicmp(lpszVal, IMAP4_NIL, ulVLen) == 0) &&
		 (strlen(IMAP4_NIL) == ulVLen))
		return EOK;

	return imap4ParseMsgFlags(lpszVal, pFlags);
}

/*--------------------------------------------------------------------------*/

EXC_TYPE imap4EncodeMsgFlags (const IMAP4_MSGFLAGS	*pFlags,
										LPTSTR					lpszEnc,
										const UINT32			ulMaxLen)
{
	EXC_TYPE	exc=EOK;
	LPTSTR	lsp=lpszEnc;
	UINT32	ulRemLen=ulMaxLen, ulIdx=0;
	BOOLEAN	fIs1st=TRUE;

	if ((NULL == pFlags) || (NULL == lpszEnc) || (0 == ulMaxLen))
		return EPARAM;

	if ((exc=strlinsch(&lsp, IMAP4_PARLIST_SDELIM, &ulRemLen)) != EOK)
		return exc;

	for (ulIdx = 0; ulIdx < IMAP4_STDMSGFLAGS_NUM; ulIdx++)
	{
		BOOLEAN	fIsSet=FALSE;
		switch(ulIdx)
		{
			case IMAP4_SEEN_MSGCASE		: fIsSet = (pFlags->m_fSeen != 0); break;
			case IMAP4_ANSWERED_MSGCASE: fIsSet = (pFlags->m_fAnswered != 0); break;
			case IMAP4_FLAGGED_MSGCASE	: fIsSet = (pFlags->m_fFlagged != 0); break;
			case IMAP4_DELETED_MSGCASE	: fIsSet = (pFlags->m_fDeleted != 0); break;
			case IMAP4_DRAFT_MSGCASE	: fIsSet = (pFlags->m_fDraft != 0); break;
			case IMAP4_RECENT_MSGCASE	: fIsSet = (pFlags->m_fRecent != 0); break;

				/* do nothing for unknown flags */
			/* case IMAP4_EXTENSION_FLAGS	: */
			/* case IMAP4_PRIVATE_FLAGS	: */

			default							:
				fIsSet = FALSE;
				break;

		}

		if (fIsSet)
		{
			LPCTSTR	lpszFlagStr=IMAP4MsgFlags[ulIdx];

			if (!fIs1st)
			{
				if ((exc=strlinsch(&lsp, _T(' '), &ulRemLen)) != EOK)
					return exc;
			}

			if ((exc=strlinsstr(&lsp, lpszFlagStr, &ulRemLen)) != EOK)
					return exc;

			fIs1st = FALSE;
		}
	}

	if ((exc=strlinsch(&lsp, IMAP4_PARLIST_EDELIM, &ulRemLen)) != EOK)
		return exc;

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE imap4UpdateFldrFlags (IMAP4_FLDRFLAGS				*pFlags,
										 const IMAP4_FLDRFLAG_CASE	fCase,
										 const BOOLEAN					fAddIt)
{
	if ((NULL == pFlags) || fIsBadIMAP4FldrFlagCase(fCase))
		return EPARAM;

	switch(fCase)
	{
		case IMAP4_NOINF_FLDRCASE	: pFlags->m_fNoInferiors = ((fAddIt) ? 1 : 0);	break;
		case IMAP4_NOSEL_FLDRCASE	: pFlags->m_fNoSelect = ((fAddIt) ? 1 : 0);	break;
		case IMAP4_MRKED_FLDRCASE	: pFlags->m_fMarked = ((fAddIt) ? 1 : 0);	break;
		case IMAP4_UNMRK_FLDRCASE	: pFlags->m_fUnMarked = ((fAddIt) ? 1 : 0);	break;

			/* valid only if CHILDREN capability reported */
		case IMAP4_HASCHILDREN_FLDRCASE		: pFlags->m_fHasChildren = ((fAddIt) ? 1 : 0);	break;
		case IMAP4_HASNOCHILDREN_FLDRCASE	: pFlags->m_fHasNoChildren = ((fAddIt) ? 1 : 0); break;

			/* some kind of unknown extension flag */
		case IMAP4_EXTND_FLDRCASE	: pFlags->m_fExtension = ((fAddIt) ? 1 : 0);	break;
		default							: return EUNKNOWNEXIT;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

/* Note: assumes first non-space in response is either list delimiter
 *			or NIL (i.e. empty list)
 */
EXC_TYPE imap4ExtractFldrFlags (LPCTSTR			lpszRsp,
										  IMAP4_FLDRFLAGS	*pFlags,
										  LPCTSTR			*lppszPars)
{
	LPCTSTR	tsp=lpszRsp;

	if (IsEmptyStr(lpszRsp) || (NULL == pFlags) || (NULL == lppszPars))
		return EPARAM;

	memset(pFlags, 0, (sizeof *pFlags));
	*lppszPars = NULL;

	for ( ; _istspace(*tsp) && (*tsp != _T('\0')); tsp++);

	/* if not starting with list delimiter check if NIL atom */
	if (*tsp != IMAP4_PARLIST_SDELIM)
	{
		LPCTSTR	lpszVal=tsp;
		UINT32	ulVLen=0;

		for ( ; (!_istspace(*tsp)) && (*tsp != _T('\0')); tsp++, ulVLen++);

		if ((_tcsnicmp(lpszVal, IMAP4_NIL, ulVLen) != 0) || (_tcslen(IMAP4_NIL) != ulVLen))
			return EUDFFORMAT;
	}
	else	/* have list delimiter */
	{
		for (tsp++; (*tsp != _T('\0')); tsp++)
		{
			EXC_TYPE					exc=EOK;
			LPCTSTR					lpszFlag=NULL;
			UINT32					ulFLen=0;
			IMAP4_FLDRFLAG_CASE	fCase=IMAP4_BDFLG_FLDRCASE;

			/* skip white space */
			for ( ; _istspace(*tsp) && (*tsp != _T('\0')); tsp++);

			/* check if reached end */
			if (IMAP4_PARLIST_EDELIM == *tsp)
				break;

			/* make sure flag starts with correct sign */
			if (IMAP4_SYSFLAG_SIGN != *tsp)
				return EUDFFORMAT;

			for (lpszFlag=tsp;
			(*tsp != _T('\0')) && (*tsp != IMAP4_PARLIST_EDELIM) && (!_istspace(*tsp));
			tsp++, ulFLen++);

			fCase = imap4XlateExtFldrFlag(lpszFlag, ulFLen);
			if ((exc=imap4UpdateFldrFlags(pFlags, fCase, TRUE)) != EOK)
				return exc;

			/* check if reached end */
			if (IMAP4_PARLIST_EDELIM == *tsp)
				break;
		}

		/* make sure loop exited because seen all flags */
		if (*tsp != IMAP4_PARLIST_EDELIM)
			return EUDFFORMAT;
		tsp++;
	}

	/* skip white space */
	for ( ; _istspace(*tsp) && (*tsp != _T('\0')); tsp++);

	*lppszPars = tsp;
	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE imap4AnalyzeMBRefsRsp (LPCTSTR				lpszRsp,
										  LPCTSTR				lpszMBRefCmd,	/* LIST/LSUB */
										  LPIMAP4FLDRFLAGS	pFlags,
										  TCHAR					*pchDelim,
										  LPCTSTR				*lppszFldr,
										  UINT32					*pulFLen)
{
	EXC_TYPE	exc=EOK;
	LPCTSTR	lsp=lpszRsp;
	TCHAR		szOp[MAX_IMAP4_OPCODE_LEN+2]="";

	if (IsEmptyStr(lpszRsp) || IsEmptyStr(lpszMBRefCmd) ||
		 (NULL == pFlags) || (NULL == pchDelim) ||
		 (NULL == lppszFldr) || (NULL == pulFLen))
		return EPARAM;

	memset(pFlags, 0, (sizeof pFlags));
	*pchDelim = _T('\0');
	*lppszFldr = NULL;
	*pulFLen = 0;

	/* skip any preceding white space */
	for ( ; _istspace(*lsp) && (*lsp != _T('\0')); lsp++);

	/* make sure this is an untagged response */
	if (*lsp != IMAP4_UNTAGGED_RSP)
		return EUDFFORMAT;

	lsp++;
	if ((exc=imap4GetArg(lsp, szOp, MAX_IMAP4_OPCODE_LEN, &lsp)) != EOK)
		return exc;

	/* make sure this is the LIST response */
	if (_tcsicmp(szOp, lpszMBRefCmd) != 0)
		return ELOGNAMESYNTAX;

	if ((exc=imap4ExtractFldrFlags(lsp, pFlags, &lsp)) != EOK)
		return exc;

	/* skip any preceding white space */
	for ( ; _istspace(*lsp) && (*lsp != _T('\0')); lsp++);

	/* extract delimiter */
	if (IMAP4_QUOTE_DELIM == *lsp)
	{
		lsp++;	/* skip delimiter */
		if (_T('\0') == (*pchDelim=*lsp))
			return EPATHNAMESYNTAX;

		/* skip ESCAPE character */
		if (_T('\\') == *pchDelim)
		{
			lsp++;
			if (_T('\0') == (*pchDelim=*lsp))
				return EPREPOSITION;
		}

		lsp++;
		if (*lsp != IMAP4_QUOTE_DELIM)
			return EUDFFORMAT;

		lsp++;	/* skip delimiter */
	}
	else /* non-delimited separator */
	{
		UINT32	ulVLen=1;
		LPCTSTR	lpszHD=lsp;

		for (lsp++; (!_istspace(*lsp)) && (*lsp != _T('\0')); lsp++, ulVLen++);

		if (ulVLen != 1)
		{
			/* check if this is the NIL atom */
			if ((_tcslen(IMAP4_NIL) != ulVLen) || (_tcsnicmp(lpszHD, IMAP4_NIL, ulVLen) != 0))
				return EILLEGALOPCODE;
		}
		else	/* if exactly on char then assume this is the delimiter */
		{
			if (_T('\0') == (*pchDelim=*lpszHD))
				return EPATHNAMESYNTAX;
		}
	}

	/* first char after delimiter must be space */
	if ((!_istspace(*lsp)) || (_T('\0') == *lsp))
		return EUDFFORMAT;

	/* skip any preceding white space */
	for (lsp++; _istspace(*lsp) && (*lsp != _T('\0')); lsp++);

	/* make sure we have some folder name */
	if (_T('\0') == *lsp)
		return EEMPTYENTRY;

	if (IMAP4_QUOTE_DELIM == *lsp)
	{
		lsp++;	/* skip delimiter */
		*lppszFldr = lsp;

		/* find closing delimiter */
		if (NULL == (lsp=_tcschr(lsp, IMAP4_QUOTE_DELIM)))
			return EUDFFORMAT;

		*pulFLen = (lsp - (*lppszFldr));
		lsp++;	/* skip delimiter */
	}
	else	/* non-delimited folder name */
	{
		*lppszFldr = lsp;
		for (lsp++; (!_istspace(*lsp)) && (*lsp != _T('\0')); lsp++);
		*pulFLen = (lsp - (*lppszFldr));
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

static EXC_TYPE addMsgSetSequenceEnd (LPTSTR			*lppszCurPos,
												  const UINT32 ulLastMember,
												  UINT32			*pulRemLen,
												  UINT32			*pulReqLen)
{
	TCHAR		szDummy[MAX_DWORD_DISPLAY_LENGTH+4];
	UINT32	ulMLen=dword_to_argument(ulLastMember, szDummy);

	*pulReqLen += (ulMLen + 1);

	if ((ulMLen+1) < (*pulRemLen))
	{
		LPTSTR	lsp=(*lppszCurPos);
		lsp = strladdch(lsp, IMAP4_MSGRANGE_DELIM);
		lsp = strlcat(lsp, szDummy);

		*lppszCurPos = lsp;
		*pulRemLen -= (ulMLen + 1);
	}
	else
	{
		*pulRemLen = 0;
		return EOVERFLOW;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

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
EXC_TYPE imap4CreateMsgSet (const UINT32	ulSet[],
									 const UINT32	ulSetSize,
									 LPTSTR			lpszMsgSet,
									 const UINT32	ulEncLen,
									 UINT32			*pulEncLen)
{
	EXC_TYPE	exc=EOK;
	TCHAR		szDummy[MAX_DWORD_DISPLAY_LENGTH+4];
	LPTSTR	lsp=lpszMsgSet;
	UINT32	ulRemLen=ulEncLen, ulSIdx=0, ulMLen=0, ulLastMmbr=0, ulReqLen=0;
	BOOLEAN	fIsSequence=FALSE, fIsOverflow=FALSE;

	if ((NULL == ulSet) || (NULL == pulEncLen) ||
		 ((NULL == lpszMsgSet) && (ulEncLen != 0)))
		return EPARAM;

	*pulEncLen = 0;
	if (lpszMsgSet != NULL)
		*lpszMsgSet = _T('\0');

	if (0 == ulSetSize)
		return EEMPTYENTRY;

	ulMLen = dword_to_argument(ulSet[0], szDummy);
	if (ulRemLen >= ulMLen)
	{
		lsp = strlcat(lsp, szDummy);
		ulRemLen -= ulMLen;
	}
	else
	{
		ulRemLen = 0;
		fIsOverflow = TRUE;
	}

	ulReqLen += ulMLen;
	ulLastMmbr = ulSet[0];

	for (ulSIdx = 1; ulSIdx < ulSetSize; ulSIdx++)
	{
		UINT32	ulMmbr=ulSet[ulSIdx];
		if (0 == ulMmbr)
			return EINVALIDFNODE;

		/* check if ascending sequence or end of identifiers */
		if ((ulMmbr-1) != ulLastMmbr)
		{
			if (fIsSequence)
			{
				if ((exc=addMsgSetSequenceEnd(&lsp, ulLastMmbr, &ulRemLen, &ulReqLen)) != EOK)
				{
					if (exc != EOVERFLOW)
						return exc;
					fIsOverflow = TRUE;
				}
			}

			fIsSequence = FALSE;
		}
		else	/* ascending sequence  */
		{
			fIsSequence = TRUE;
		}

		/* if not a sequence then simply add the member */
		if (!fIsSequence)
		{
			ulMLen = dword_to_argument(ulMmbr, szDummy);

			if ((ulMLen + 1) < ulRemLen)
			{
				lsp = strladdch(lsp, IMAP4_MSGLIST_DELIM);
				lsp = strlcat(lsp, szDummy);
				ulRemLen -= (ulMLen + 1);
			}
			else
			{
				ulRemLen = 0;
				fIsOverflow = TRUE;
			}

			ulReqLen += (ulMLen + 1);
		}

		ulLastMmbr = ulMmbr;
	}

	/* if still within sequence then last member is sequence end */
	if (fIsSequence)
	{
		if ((exc=addMsgSetSequenceEnd(&lsp, ulSet[ulSetSize-1], &ulRemLen, &ulReqLen)) != EOK)
		{
			if (exc != EOVERFLOW)
				return exc;
			fIsOverflow = TRUE;
		}

		fIsSequence = FALSE;
	}

	*pulEncLen = ulReqLen;
	if (fIsOverflow)
	{
		if (lpszMsgSet != NULL)
			*lpszMsgSet = _T('\0');
		return EOVERFLOW;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

/* returns number of msgs encoded in the msg set ((-1) if error, 0 if NULL/empty) */
UINT32 GetIMAP4MsgSetCount (LPCTSTR lpszMsgSet)
{
	LPCTSTR	lpszMS=lpszMsgSet;
	UINT32	ulMSCount=0;

	if (IsEmptyStr(lpszMsgSet))
		return 0;

	while (*lpszMS != _T('\0'))
	{
		EXC_TYPE	exc=EOK;
		LPCTSTR	lpszNumVal=lpszMS;
		UINT32	ulNVLen=0, ulSNum=0;

		for ( ; _istdigit(*lpszMS) && (*lpszMS != _T('\0')); lpszMS++);

		ulNVLen = (lpszMS - lpszNumVal);
		ulSNum = argument_to_dword(lpszNumVal, ulNVLen, EXC_ARG(exc));
		if (exc != EOK)
			return ((UINT32) (-1));

		if (IMAP4_MSGLIST_DELIM == *lpszMS)
		{
			ulMSCount++;
			lpszMS++;
		}
		else if (IMAP4_MSGRANGE_DELIM == *lpszMS)
		{
			UINT32	ulENum=0;

			for (lpszMS++, lpszNumVal=lpszMS; _istdigit(*lpszMS) && (*lpszMS != _T('\0')); lpszMS++);
			ulNVLen = (lpszMS - lpszNumVal);
			ulENum = argument_to_dword(lpszNumVal, ulNVLen, EXC_ARG(exc));
			if ((exc != EOK) || (ulSNum > ulENum))
				return ((UINT32) (-1));

			ulMSCount += ((1 + ulENum) - ulSNum);
			if (*lpszMS != _T('\0'))
				lpszMS++;
		}
		else if (*lpszMS != _T('\0'))	/* allow only termination */
			return ((UINT32) (-1));
	}

	return ulMSCount;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4CreateMsgRange (const UINT32	ulStartID,
										const UINT32	ulEndID,
										LPTSTR			lpszMsgSet,
										const UINT32	ulEncLen,
										UINT32			*pulEncLen)
{
	LPTSTR	lsp=lpszMsgSet;
	UINT32	ulRemLen=ulEncLen;
	EXC_TYPE	exc=EOK;

	if ((0 == ulStartID) || (0 == ulEndID) || (NULL == lpszMsgSet) ||
		 (0 == ulEncLen) || (NULL == pulEncLen))
		return EPARAM;

	*lpszMsgSet = _T('\0');
	*pulEncLen = 0;

	if (ulStartID > ulEndID)
		return ERANGE;

	if ((exc=strlinsnum(&lsp, ulStartID, &ulRemLen)) != EOK)
		return exc;

	/* allow a "range" of one member */
	if (ulEndID > ulStartID)
	{
		if ((exc=strlinsch(&lsp, IMAP4_MSGRANGE_DELIM, &ulRemLen)) != EOK)
			return exc;
		if ((exc=strlinsnum(&lsp, ulEndID, &ulRemLen)) != EOK)
			return exc;
	}

	*pulEncLen = (ulEncLen - ulRemLen);
	return EOK;
}

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
EXC_TYPE CIMAP4MsgSetParser::Init (LPCTSTR lpszMsgSet)
{
	Clear();

	if (!IsEmptyStr(lpszMsgSet))
	{
		UINT32	ulSetLen=_tcslen(lpszMsgSet);
		if (NULL == (m_lpszMsgSet=new TCHAR[ulSetLen+4]))
			return EMEM;

		_tcscpy(m_lpszMsgSet, lpszMsgSet);
		m_lpszMsgSet[ulSetLen+1] = _T('\0');	// last entry has 2 terminating '\0'-s
	}
	else
	{
		m_lpszMsgSet = NULL;
	}

	Reset();
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4MsgSetParser::Init (const UINT32 ulSet[], const UINT32 ulSetSize)
{
	Init(NULL);

	if ((NULL == ulSet) && (0 == ulSetSize))
		return EOK;

	if ((NULL == ulSet) || (0 == ulSetSize))
		return EPARAM;

	UINT32	ulEncLen=0;
	EXC_TYPE exc=imap4CreateMsgSet(ulSet, ulSetSize, NULL, 0, &ulEncLen);
	if (EOK == exc)	// should not return OK since we query it...
		return EFATALEXIT;

	if (NULL == (m_lpszMsgSet=new TCHAR[ulEncLen+4]))
		return EMEM;
	if ((exc=imap4CreateMsgSet(ulSet, ulSetSize, m_lpszMsgSet, (ulEncLen+1), &ulEncLen)) != EOK)
	{
		Clear();
		return exc;
	}

	m_lpszMsgSet[ulEncLen+1] = _T('\0');	// last entry has 2 terminating '\0'-s
	Reset();
	return EOK;
}

/*---------------------------------------------------------------------------*/

// if start/end ID (-1) => '*' (but not both)
EXC_TYPE CIMAP4MsgSetParser::Init (const UINT32 ulStartID, const UINT32 ulEndID)
{
	if (((UINT32) (-1) == ulStartID) && ((UINT32) (-1) == ulEndID))
		return ETRANSLIMIT;

	TCHAR	szEnc[(2*MAX_DWORD_DISPLAY_LENGTH)+4]=_T(""), *lsp=szEnc;
	if ((UINT32) (-1) == ulStartID)
		lsp = strladdch(lsp, IMAP4_MSGRANGE_WILDCARD);
	else
		lsp += dword_to_argument(ulStartID, lsp);

	lsp = strladdch(lsp, IMAP4_MSGRANGE_DELIM);
	if ((UINT32) (-1) == ulEndID)
		lsp = strladdch(lsp, IMAP4_MSGRANGE_WILDCARD);
	else
		lsp += dword_to_argument(ulEndID, lsp);

	return Init(szEnc);
}
#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
LPCTSTR CIMAP4MsgSetParser::GetNext ()
{
	if (IsEmptyStr(m_lpszCurPos))
		return NULL;

	LPCTSTR lpszVal=m_lpszCurPos;

	// prepare for next request
	for (m_lpszCurPos++; (*m_lpszCurPos != _T('\0')); m_lpszCurPos++)
		if (IMAP4_MSGLIST_DELIM == *m_lpszCurPos) 
		{
			// if found list delimiter then replace it with terminating NULL
			*m_lpszCurPos = _T('\0');
			break;
		}

	// last entry has TWO terminating NULL(s)
	m_lpszCurPos++;

	return lpszVal;
}

void CIMAP4MsgSetParser::Clear ()
{
	if (m_lpszMsgSet != NULL)
	{
		delete [] m_lpszMsgSet;
		m_lpszMsgSet = NULL;
	}

	m_lpszCurPos = NULL;
}
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
void CIMAP4MsgSetEnum::Reset ()
{
	m_SetParser.Reset();

	m_ulStartIdx = 0;
	m_ulEndIdx = 0;
	m_ulCurIdx = 0;
}
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
EXC_TYPE CIMAP4MsgSetEnum::Init (LPCTSTR			lpszMsgSet,
											const UINT32	ulMaxIdx,
											const BOOLEAN	fIsUIDSet)
{
	EXC_TYPE	exc=m_SetParser.Init(lpszMsgSet);
	if (exc != EOK)
		return exc;

	m_ulMaxIdx = ulMaxIdx;
	m_fIsUIDSet = fIsUIDSet;

	Reset();
	return EOK;
}
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
EXC_TYPE CIMAP4MsgSetEnum::SetRange (LPCTSTR lpszRange, UINT32& ulIdx)
{
	ulIdx = 0;

	if (IsEmptyStr(lpszRange))
		return EEOF;

	EXC_TYPE	exc=EOK;
	LPCTSTR	tsp=_tcschr(lpszRange, IMAP4_MSGRANGE_DELIM);
	if (NULL == tsp)
	{
		m_ulStartIdx = argument_to_dword(lpszRange, _tcslen(lpszRange), EXC_ARG(exc));
		if (exc != EOK)
			return exc;

		m_ulEndIdx = m_ulStartIdx;
	}
	else	// this is an actual range
	{
		if (lpszRange == tsp)
			return EUDFFORMAT;

		// determine start index
		if (IMAP4_MSGRANGE_WILDCARD == *(tsp-1))
		{
			// cannot replace start index for UID set
			if (m_fIsUIDSet)
				return ESUPPORT;

			m_ulStartIdx = 1;
		}
		else	// "normal" start index
		{
			m_ulStartIdx = argument_to_dword(lpszRange, (tsp - lpszRange), EXC_ARG(exc));
			if (exc != EOK) 
				return exc;
		}

		// determine end index
		if (IMAP4_MSGRANGE_WILDCARD == *(tsp+1))
		{
			if (0 == m_ulMaxIdx)
				return ESTATE;

			m_ulEndIdx = m_ulMaxIdx;
		}
		else	// "normal" end index
		{
			LPCTSTR	esp=(tsp+1);

			m_ulEndIdx = argument_to_dword(esp, _tcslen(esp), EXC_ARG(exc));
			if (exc != EOK)
				return exc;
		}
	}

	m_ulCurIdx = m_ulStartIdx;
	ulIdx = m_ulCurIdx;
	m_ulCurIdx++;
	return EOK;
}
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
// returns EEOF at end (or non-EOK for other errors)
EXC_TYPE CIMAP4MsgSetEnum::GetFirst (UINT32& ulIdx)
{
	Reset();
	return SetRange(m_SetParser.GetFirst(), ulIdx);
}
#endif

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
// returns EEOF at end (or non-EOK for other errors)
EXC_TYPE CIMAP4MsgSetEnum::GetNext (UINT32& ulIdx)
{
	if (m_ulCurIdx <= m_ulEndIdx)
	{
		ulIdx = m_ulCurIdx;
		m_ulCurIdx++;
		return EOK;
	}

	return SetRange(m_SetParser.GetNext(), ulIdx);
}
#endif	/* of __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
// returns EEOF if brace count is zero
EXC_TYPE CIMAP4ParListChecker::ProcessResponse (const char lpszRsp[], UINT32& ulOCount)
{
	ulOCount = 0;

	if (NULL == lpszRsp)
		return EPARAM;

	// scan response and check for paranthesized list balancing
	for (const char	*tsp=lpszRsp; (*tsp != _T('\0')) && (0 == ulOCount); tsp++)
	{
		switch(*tsp)
		{
			case IMAP4_PARLIST_SDELIM	:
				if (!m_fIsDelimStr)
					m_ulBraceCount++;
				break;

			case IMAP4_PARLIST_EDELIM	:
				if (!m_fIsDelimStr)
				{
					if (0 == m_ulBraceCount)
						return EUDFFORMAT;

					m_ulBraceCount--;
				}
				break;

			case IMAP4_QUOTE_DELIM		:
				if (m_fIsDelimStr)
					m_fIsDelimStr = FALSE;
				else
					m_fIsDelimStr = TRUE;
				break;

			case IMAP4_OCTCNT_SDELIM	:	// extract octet count and stop
				if (!m_fIsDelimStr)
				{
					const char	*lsp=tsp;
						
					for (lsp++, tsp++; isdigit(*lsp) && (*lsp != _T('\0')) && (*lsp != IMAP4_OCTCNT_EDELIM); lsp++);

					EXC_TYPE	exc=EOK;
					ulOCount = argument_to_dword(tsp, (lsp - tsp), EXC_ARG(exc));
					if ((*lsp != IMAP4_OCTCNT_EDELIM) || (exc != EOK))
						return ((exc != EOK) ? exc : EUDFFORMAT);
				}
				break;

			default							: break;
		}	// end of SWITCH
	}	// end of loop on response

	if (0 == m_ulBraceCount)
		return EEOF;
	return EOK;
}
#endif	/* of __cplusplus */

/*--------------------------------------------------------------------------*/

/* Searches for a BODY[n]<offset> response (or the equivalents)
 *
 * Returns:
 *		EEXIST if not found response
 *		EOK with empty part if no specific body part found
 *
 * Note: end offset == (-1) if no end offset specified
 */
EXC_TYPE imap4ExtractBodyPart (const char		lpszRsp[],
										 char				lpszPart[],
										 const UINT32	ulMaxPartLen,
										 BOOLEAN			*pfIsPeek,
										 UINT32			*pulStartOffset,
										 UINT32			*pulEndOffset)
{
	EXC_TYPE		exc=EOK;
	const char	*lpszS=lpszRsp, *lpszE=NULL;
	UINT32		ulBLen=0, ulCLen=0, ulPLen=0, ulRLen=0;

	if ((NULL == lpszRsp) || (NULL == pulStartOffset) || (NULL == pulEndOffset) ||
		 (NULL == pfIsPeek) || (NULL == lpszPart) || (0 == ulMaxPartLen))
		return EPARAM;

	*lpszPart = _T('\0');
	*pfIsPeek = FALSE;
	*pulStartOffset = 0;
	*pulEndOffset = (UINT32) (-1);

	for (ulBLen=strlen(IMAP4_BODY), ulPLen=strlen(IMAP4_BODYPEEK); (*lpszS != _T('\0')); lpszS = lpszE)
	{
		/* skip any whitespace */
		for (; _istspace(*lpszS) && (*lpszS != _T('\0')); lpszS++);

		// skip list delimiter (if any)
		if (IMAP4_PARLIST_SDELIM == *lpszS)
			lpszS++;

		/* find end of keyword */
		for (lpszE=(lpszS+1);
			  (!_istspace(*lpszE)) && (*lpszE != _T('\0')) && 
			  (*lpszE != IMAP4_BRCKT_SDELIM) &&
			  (*lpszE != IMAP4_OFFSET_SDELIM);
			  lpszE++);

		/* make sure this is the modifier we are looking for */
		ulCLen = (lpszE - lpszS);

		if ((ulCLen == ulBLen) && (strnicmp(lpszS, IMAP4_BODY, ulCLen) == 0))
		{
			*pfIsPeek = FALSE;
			break;
		}

		if ((ulCLen == ulPLen) && (strnicmp(lpszS, IMAP4_BODYPEEK, ulCLen) == 0))
		{
			*pfIsPeek = TRUE;
			break;
		}

		/* check for equivalent keywords */
		if ((*lpszE != IMAP4_BRCKT_SDELIM) && (*lpszE != IMAP4_OFFSET_SDELIM))
		{
			const char	*lpszEquivPart=NULL;

			exc = imap4GetEquivBodyPart(lpszS, ulCLen, &lpszEquivPart);
			if (exc != EOK)
			{
				if (exc != EEXIST)
					return exc;
			}
			else	/* found equivalence */
				lpszE = lpszEquivPart;
		}
	}

	if (*lpszS == _T('\0'))
		return EEXIST;

	for (lpszS=lpszE; (*lpszS != _T('\0')) && (!_istspace(*lpszS)); lpszE++, lpszS = lpszE)
	{
		const char	*lpszT=NULL;

		switch(*lpszS)
		{
			case IMAP4_BRCKT_SDELIM		:	/* extract body part */
				for (lpszS++, lpszE=lpszS;
					  (*lpszE != _T('\0')) && (*lpszE != IMAP4_BRCKT_EDELIM);
					  lpszE++);

				/* make sure body part is delimited correctly */
				if (*lpszE != IMAP4_BRCKT_EDELIM)
					return EUDFFORMAT;

				/* make sure we can accomodate body part */
				if ((ulCLen=(lpszE-lpszS)) >= ulMaxPartLen)
					return ELIMIT;

				strncpy(lpszPart, lpszS, ulCLen);
				lpszPart[ulCLen] = _T('\0');
				break;

			case IMAP4_OFFSET_SDELIM	:
				for (lpszS++, lpszE=lpszS;
					  (*lpszE != _T('\0')) && (*lpszE != IMAP4_OFFSET_EDELIM);
					  lpszE++)
					if ('.' == *lpszE)
						lpszT = lpszE;

				/* make sure body offset is delimited correctly */
				if (*lpszE != IMAP4_OFFSET_EDELIM)
					return EUDFFORMAT;

				if (NULL == lpszT)
				{
					ulCLen = (lpszE - lpszS);
					*pulStartOffset = argument_to_dword(lpszS, ulCLen, EXC_ARG(exc));
					if (exc != EOK)
						return exc;

					*pulEndOffset = (UINT32) (-1);
				}
				else	/* have octet count */
				{
					ulCLen = (lpszT - lpszS);
					*pulStartOffset = argument_to_dword(lpszS, ulCLen, EXC_ARG(exc));
					if (exc != EOK)
						return exc;

					lpszT++;
					ulBLen = (lpszE - lpszT);
					ulRLen = argument_to_dword(lpszT, ulBLen, EXC_ARG(exc));
					if (exc != EOK)
						return exc;

					*pulEndOffset = (*pulStartOffset) + ulRLen;
				}
				break;

			default							:
				if (_istspace(*lpszS))
					return EOK;
				else
					return EPARAM;
		}
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

/* Note: returned part ID is empty if no specific ID specified (e.g. headers
 *			in general requested)
 */
EXC_TYPE imap4AnalyzeBodyPart (LPCTSTR				lpszBodyPart,
										 IMAP4_BODYPART_CASE	*pbCase,
										 LPTSTR					lpszPartID,
										 const UINT32			ulMaxIDLen)
{
	LPCTSTR	lpszS=lpszBodyPart, lpszE=NULL;
	UINT32	ulCLen=0;
	BOOLEAN	fSpecFound=FALSE;

	if ((NULL == lpszBodyPart) || (NULL == pbCase) ||
		 (NULL == lpszPartID) || (0 == ulMaxIDLen))
		return EPARAM;

	*pbCase = IMAP4_BAD_BODYPART;
	*lpszPartID = _T('\0');

	/* skip any preceding white space */
	for ( ; _istspace(*lpszS) && (*lpszS != _T('\0')); lpszS++);

	/* if no specific body part then assume entire body requested */
	if (_T('\0') == *lpszS)
	{
		*pbCase = IMAP4_ALL_BODYPART;
		return EOK;
	}

	/* skip ID part (if any) */
	for (lpszE=lpszS ; (*lpszE != _T('\0')); lpszE++)
	{
		/* skip part ID specifier(s) */
		if (_istdigit(*lpszE))
			continue;

		/* check if end of ID and start of body part specifier */
		if (IMAP4_BODYPART_DELIM == *lpszE)
		{
			LPCTSTR	lpszN=(lpszE+1);

			if (_istdigit(*lpszN))
				continue;

			/* make sure we can accomodate part ID */
			if ((ulCLen=(lpszE - lpszS)) >= ulMaxIDLen)
				return ELIMIT;

			strncpy(lpszPartID, lpszS, ulCLen);
			lpszPartID[ulCLen] = _T('\0');

			lpszE = lpszN;
		}

		/* find end of specifier */
		for (lpszS=lpszE; (*lpszE != _T('\0')) && (!_istspace(*lpszE)); lpszE++);

		fSpecFound = TRUE;
		break;
	}

	/* if no specifier then entire body part was requested */
	if (!fSpecFound)
	{
		if ((ulCLen=strlen(lpszS)) >= ulMaxIDLen)
			return ELIMIT;

		_tcscpy(lpszPartID, lpszS);
		*pbCase = IMAP4_ALL_BODYPART;
		return EOK;
	}

	/*
	 *		At this stage "lpszS" points to body part specifier (if any), and
	 * "lpszE" points to one place beyond it, and the part ID (if any) has
	 * been copied to the supplied buffer.
	 */
	ulCLen = (lpszE - lpszS);
	*pbCase = imap4XlateBodyPart(lpszS, ulCLen);
	if (fIsBadIMAP4BodyPart(*pbCase))
		return ETYPE;

	return EOK;
}

/*--------------------------------------------------------------------------*/

/* Xlate(s) the part ID into numbers.
 *
 * Returns EXIST if no part ID supplied
 */
EXC_TYPE imap4AnalyzePartID (LPCTSTR		lpszPartID,
									  UINT32			ulPartID[],
									  UINT32			*pulIDLen,	/* number of components */
									  const UINT32	ulMaxIDLen)
{
	LPCTSTR	lpszT=lpszPartID;

	if ((NULL == lpszPartID) || (NULL == ulPartID) ||
		 (NULL == pulIDLen) || (0 == ulMaxIDLen))
		return EPARAM;
	*pulIDLen = 0;

	for ( ; _istspace(*lpszT) && (*lpszT != _T('\0')); lpszT++);

	if (_T('\0') == *lpszT)
		return EEXIST;
	if (!isdigit(*lpszT))
		return EUDFFORMAT;

	for ( ; (!_istspace(*lpszT)) && (*lpszT != _T('\0')); lpszT++)
	{
		EXC_TYPE	exc=EOK;
		LPCTSTR	lpszV=lpszT;
		UINT32	ulVLen=0;

		for (lpszT++; isdigit(*lpszT) && (*lpszT != _T('\0')); lpszT++);

		if (*pulIDLen >= ulMaxIDLen)
			return ELIMIT;

		ulVLen = (lpszT - lpszV);
		ulPartID[*pulIDLen] = argument_to_dword(lpszV, ulVLen, EXC_ARG(exc));
		if (exc != EOK)
			return exc;

		(*pulIDLen)++;

		if (*lpszT != _T('\0'))
		{
			/* make sure we stopped due to correct delimiter */
			if (*lpszT != IMAP4_BODYPART_DELIM)
				return ENOPREFIX;
		}
		else	/* end of string */
			break;
	}

	/* add an ending zero ID (if have room for it) */
	if ((*pulIDLen) < ulMaxIDLen)
		ulPartID[*pulIDLen] = 0;

	return EOK;
}

/*--------------------------------------------------------------------------*/

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
EXC_TYPE imap4AnalyzeBodyRsp (LPCTSTR		lpszBodyRsp,
										LPCTSTR		*lppszSection,
										UINT32		*pulSectionLen,
										LPCTSTR		*lppszHdrsList,
										UINT32		*pulListLen,
										UINT32		*pulOriginOctet)	/* (-1) if none */
{
	UINT32	ulVLen=0;
	LPCTSTR	lsp=lpszBodyRsp, tsp=NULL;
	EXC_TYPE	exc=EOK;

	if (IsEmptyStr(lpszBodyRsp) || (NULL == lppszSection) || (NULL == pulSectionLen) ||
		 (NULL == lppszHdrsList) || (NULL == pulListLen) || (NULL == pulOriginOctet))
		return EPARAM;
	*lppszSection = _T("");
	*pulSectionLen = 0;
	*lppszHdrsList = _T("");
	*pulListLen = 0;
	*pulOriginOctet = (UINT32) (-1);

	/* allow no body section */
	if (NULL == (lsp=_tcschr(lpszBodyRsp, IMAP4_BRCKT_SDELIM)))
		return EOK;

	/* find start of section (if any) */
	for (lsp++; _istspace(*lsp) && (IMAP4_BRCKT_EDELIM != *lsp); lsp++)
		if (_T('\0') == *lsp)
			return EUDFFORMAT;

	if (IMAP4_BRCKT_EDELIM != *lsp)
	{
		*lppszSection = lsp;

		/* find end of section (if any) */
		for (lsp++; (IMAP4_BRCKT_EDELIM != *lsp) ; lsp++)
		{
			if (_T('\0') == *lsp)
				return EUDFFORMAT;

			if (_istspace(*lsp) || (IMAP4_PARLIST_SDELIM == *lsp))
				break;
		}

		/* at this point "lsp" points to 1st place AFTER section */
		*pulSectionLen = (lsp - (*lppszSection));
	}

	/* check what comes AFTER the section (if anything) */
	for ( ; _istspace(*lsp) && (IMAP4_BRCKT_EDELIM != *lsp) && (IMAP4_PARLIST_SDELIM != *lsp); lsp++)
		if (_T('\0') == *lsp)
			return EUDFFORMAT;

	if (IMAP4_PARLIST_SDELIM == *lsp)
	{
		lsp++;
		*lppszHdrsList = lsp;

		if (NULL == (lsp=_tcschr(lsp, IMAP4_PARLIST_EDELIM)))
			return EUNMATCHEDLISTS;

		*pulListLen = (lsp - (*lppszHdrsList));
		lsp++;
	}

	/* check if have octets offset */
	for ( ; _istspace(*lsp) && (IMAP4_BRCKT_EDELIM != *lsp); lsp++)
		if (_T('\0') == *lsp)
			return EUDFFORMAT;
	if (IMAP4_BRCKT_EDELIM != *lsp)
		return EUNMATCHEDLISTS;

	for (lsp++; (*lsp != IMAP4_OFFSET_SDELIM) && (*lsp != _T('\0')); lsp++);
	if (*lsp != IMAP4_OFFSET_SDELIM)
		return EOK;

	/* find start of octets offset */
	for (lsp++; _istspace(*lsp) && (*lsp != _T('\0')); lsp++);
	if (!_istdigit(*lsp))
		return EINVALIDNUMERIC;

	for (tsp=lsp; _istdigit(*lsp) && (*lsp != _T('\0')); lsp++);
	ulVLen = (lsp - tsp);
	*pulOriginOctet = argument_to_dword(tsp, ulVLen, EXC_ARG(exc));
	if (exc != EOK)
		return exc;

	for ( ; (*lsp != IMAP4_OFFSET_EDELIM) && (*lsp != _T('\0')); lsp++);
	if (*lsp != IMAP4_OFFSET_EDELIM)
		return EUNMATCHEDLISTS;

	return EOK;
}

/*---------------------------------------------------------------------------*/

/*
 *	Analyzes a BODYSTRUCTURE response and returns next part (delimited within
 * paranthesis list).
 *
 * Returns EEOF if no more body parts found
 *
 * Note(s):
 *
 *		a. assumes ENTIRE body structure response in in supplied buffer
 *
 *		b. assumes initial '(' skipped (but terminating ')' not !!!)
 */
EXC_TYPE imap4GetNextBodyStructPart (const char	**lppszCurPos,
												 const char	**lppszPart,
												 UINT32		*pulPartLen)
{
	EXC_TYPE		exc=EOK;
	const char	*lpszS=NULL, *lpszE=NULL;

	if ((NULL == lppszCurPos) || (NULL == lppszPart) || (NULL == pulPartLen))
		return EPARAM;

	*lppszPart = NULL;
	*pulPartLen = 0;

	if (NULL == (lpszS=(*lppszCurPos)))
		return ENOPREFIX;

	for ( ; _istspace(*lpszS) && (*lpszS != _T('\0')); lpszS++);
	if (_T('\0') == *lpszS)
		return ESTATE;
	if (IMAP4_PARLIST_EDELIM == *lpszS)
		return EEOF;
	if (IMAP4_PARLIST_SDELIM != *lpszS)
		return ESTATE;

	if ((exc=imap4GetListValue(lpszS, lppszPart, pulPartLen)) != EOK)
		return exc;

	lpszE = ((*lppszPart) + (*pulPartLen));
	*lppszCurPos = lpszE;

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE imap4AnalyzeBodyStruct (const char	**lppszCurPos,
											const char	**lppszPart,
											UINT32		*pulPartLen)
{
	const char	*lpszS=NULL, *lpszE=NULL;

	if ((NULL == lppszCurPos) || (NULL == lppszPart) || (NULL == pulPartLen))
		return EPARAM;

	*lppszPart = NULL;
	*pulPartLen = 0;

	if (NULL == (lpszS=(*lppszCurPos)))
		return ENOPREFIX;

	for ( ; _istspace(*lpszS) && (*lpszS != _T('\0')); lpszS++);
	if (_T('\0') == *lpszS)
		return ESTATE;
	if (IMAP4_PARLIST_EDELIM == *lpszS)
		return EEOF;

	if (IMAP4_PARLIST_SDELIM == *lpszS)
	{
		EXC_TYPE	exc=imap4GetListValue(lpszS, lppszPart, pulPartLen);
		if (exc != EOK)
			return exc;

		lpszE = ((*lppszPart) + (*pulPartLen));
	}
	else	/* not a paranthesized list */
	{
		for (lpszE=(lpszS+1); (!_istspace(*lpszE)) && (*lpszE != _T('\0')); lpszE++)
			if ((IMAP4_PARLIST_SDELIM == *lpszE) || (IMAP4_PARLIST_EDELIM == *lpszE))
				break;

		*lppszPart = lpszS;
		*pulPartLen = (lpszE - lpszS);
	}

	*lppszCurPos = lpszE;

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE imap4GetBodyStructArg (const char	**lppszCurPos,
										  const UINT32	ulPartIdx,
										  const char	**lppszPart,
										  UINT32			*pulPartLen)
{
	UINT32	ulIdx=0;

	for ( ; ulIdx <= ulPartIdx; ulIdx++)
	{
		EXC_TYPE	exc=imap4AnalyzeBodyStruct(lppszCurPos, lppszPart, pulPartLen);
		if (exc != EOK) return exc;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

/* Note: it can only go FORWARD !!! */
EXC_TYPE imap4GetRelativeBodyStructArg (const char		**lppszCurPos,
													 const UINT32	ulCurPartIdx,
													 const UINT32	ulReqPartIdx,
													 const char		**lppszPart,
													 UINT32			*pulPartLen)
{
	if (ulCurPartIdx >= ulReqPartIdx)
		return ESUPPORT;

	return imap4GetBodyStructArg(lppszCurPos, (ulReqPartIdx - ulCurPartIdx - 1),
										  lppszPart, pulPartLen);
}

/*--------------------------------------------------------------------------*/

/* enumerates returned search result(s) - if any */
EXC_TYPE imap4EnumSearchResults (const char		pszResults[],
											IMAP4_SRES_CFN	lpfnEcfn,
											void				*pArg)
{
	const char	*lsp=pszResults;

	if ((NULL == pszResults) || (NULL == lpfnEcfn))
		return EPARAM;

	BOOLEAN		fContEnum=TRUE;
	while (*lsp != _T('\0'))
	{
		EXC_TYPE		exc=EOK;
		const char	*tsp=lsp;
		UINT32		nLen=0, ulSeqNo=0;

		/* skip any preceding white space */
		while (_istspace(*tsp) && (*tsp != _T('\0'))) tsp++;

		if (_T('\0') == *tsp)
			break;

		/* 1st non-space char must be a digit */
		if (!isdigit(*tsp))
			return EINVALIDNUMERIC;

		/* find end of msg sequence number */
		for (lsp=tsp, nLen=0; isdigit(*lsp) && (*lsp != _T('\0')); lsp++, nLen++);

		ulSeqNo = argument_to_dword(tsp, nLen, EXC_ARG(exc));
		if (exc != EOK)
			return exc;

		if (fContEnum)
		{
			if ((exc=(*lpfnEcfn)(ulSeqNo, pArg, &fContEnum)) != EOK)
				return exc;
		}
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

/* parses the SEARCH response and enumerates the result(s) - if any */
EXC_TYPE imap4EnumSearchResponse (const char			pszRsp[],
											 IMAP4_SRES_CFN	lpfnEcfn,
											 void					*pArg)
{
	EXC_TYPE		exc=EOK;
	const char	*lsp=pszRsp;
	char			szOp[MAX_IMAP4_OPCODE_LEN+2];

	if (IsEmptyStr(pszRsp) || (NULL == lpfnEcfn))
		return EPARAM;

	/* skip any preceding white space */
	while (_istspace(*lsp) && (*lsp != _T('\0'))) lsp++;

	/* make sure this is an untagged response */
	if (*lsp != IMAP4_UNTAGGED_RSP)
		return EUDFFORMAT;

	lsp++;
	if ((exc=imap4GetArg(lsp, szOp, MAX_IMAP4_OPCODE_LEN, &lsp)) != EOK)
		return exc;

	/* make sure this is the SEARCH response */
	if (_tcsicmp(szOp, szIMAP4SearchCmd) != 0)
		return ELOGNAMESYNTAX;

	return imap4EnumSearchResults(lsp, lpfnEcfn, pArg);
}

/*--------------------------------------------------------------------------*/

static EXC_TYPE GetNumDTComp (LPCTSTR *lppszCurPos, const TCHAR chSep, int *pn)
{
	EXC_TYPE	exc=EOK;
	LPCTSTR	lsp=NULL, tsp=NULL;
	UINT32	cLen=0;

	if ((NULL == lppszCurPos) || (NULL == pn) || (NULL == (lsp=(*lppszCurPos))))
		return EPARAM;
	*pn = (-1);

	for ( ; _istspace(*lsp) && (*lsp != _T('\0')); lsp++);
	for (tsp=lsp; _istdigit(*lsp) && (*lsp != _T('\0')); lsp++);

	cLen = (lsp - tsp);
	*pn = (int) argument_to_dword(tsp, cLen, EXC_ARG(exc));
	if (exc != EOK)
		return exc;

	if (chSep != _T('\0'))
	{
		if (*lsp != chSep)
			return EPREPOSITION;
		lsp++;
	}

	*lppszCurPos = lsp;
	return EOK;
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE GetMOYDTComp (LPCTSTR *lppszCurPos, const TCHAR chSep, int *pmoy)
{
	LPCTSTR	lsp=NULL, tsp=NULL;
	UINT32	cLen=0;

	if ((NULL == lppszCurPos) || (NULL == pmoy) || (NULL == (lsp=(*lppszCurPos))))
		return EPARAM;
	*pmoy = (-1);

	for ( ; _istspace(*lsp) && (*lsp != _T('\0')); lsp++);

	for (tsp=lsp; *lsp != chSep; lsp++);
	cLen = (lsp - tsp);
	if ((*pmoy=str2moy(tsp, cLen)) >= 12)
		return ECONTEXT;

	if (chSep != _T('\0'))
	{
		if (*lsp != chSep)
			return EPREPOSITION;
		lsp++;
	}

	*lppszCurPos = lsp;
	return EOK;
}

/*---------------------------------------------------------------------------*/

/*
 * Format is: "dd-mmm-yyyy hh:mm:ss [+/-]GMT"
 */
EXC_TYPE DecodeIMAP4InternalDate (LPCTSTR lpszDate, struct tm *ptm, int *ptmZone)
{
	LPCTSTR	lsp=lpszDate;
	EXC_TYPE	exc=EOK;

	if (IsEmptyStr(lpszDate) || (NULL == ptm) || (NULL == ptmZone))
		return EPARAM;
	memset(ptm, 0, (sizeof *ptm));
	*ptmZone = 0;

	if ((exc=GetNumDTComp(&lsp, IMAP4_RCVDATE_DELIM, &(ptm->tm_mday))) != EOK)
		return exc;
	if ((exc=GetMOYDTComp(&lsp, IMAP4_RCVDATE_DELIM, &(ptm->tm_mon))) != EOK)
		return exc;
	if ((exc=GetNumDTComp(&lsp, _T(' '), &(ptm->tm_year))) != EOK)
		return exc;
	ptm->tm_year -= 1900;

	if ((exc=GetNumDTComp(&lsp, IMAP4_RCVTIME_DELIM, &(ptm->tm_hour))) != EOK)
		return exc;
	if ((exc=GetNumDTComp(&lsp, IMAP4_RCVTIME_DELIM, &(ptm->tm_min))) != EOK)
		return exc;
	if ((exc=GetNumDTComp(&lsp, _T(' '), &(ptm->tm_sec))) != EOK)
		return exc;

	/* deal with GMT timezone */
	for ( ; _istspace(*lsp) && (*lsp != _T('\0')); lsp++);
	if ((exc=rfc822XlateExplicitGMTOffset(lsp, ptmZone)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

/*
 * Format is: "dd-mmm-yyyy hh:mm:ss [+/-]GMT"
 */
EXC_TYPE EncodeIMAP4InternalDate (const struct tm	*ptm,
											 const int			tmZone,
											 LPTSTR				lpszDate,
											 const UINT32		ulMaxLen)
{
	EXC_TYPE	exc=EOK;
	LPTSTR	lsp=lpszDate;
	UINT32	absTMZ=(UINT32) ((tmZone < 0) ? (0 - tmZone) : tmZone);
	UINT32	tmzH=(absTMZ / 3600), tmzM=((absTMZ % 3600) / 60);
	UINT32	ulRemLen=ulMaxLen;

	if ((NULL == ptm) || (NULL == lpszDate) || (0 == ulMaxLen))
		return EPARAM;

	if (ptm->tm_mon >= 12)
		return EINVALIDDATE;

	if ((exc=strlinspadnum(&lsp, (UINT32) ptm->tm_mday, 2, _T('0'), TRUE, &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsch(&lsp, IMAP4_RCVDATE_DELIM, &ulRemLen)) != EOK)
		return exc;

	if ((exc=strlinsstr(&lsp, month_of_year[ptm->tm_mon], &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsch(&lsp, IMAP4_RCVDATE_DELIM, &ulRemLen)) != EOK)
		return exc;

	if ((exc=strlinsnum(&lsp, (UINT32) (ptm->tm_year+1900), &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsch(&lsp, _T(' '), &ulRemLen)) != EOK)
		return exc;


	if ((exc=strlinspadnum(&lsp, (UINT32) ptm->tm_hour, 2, _T('0'), TRUE, &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsch(&lsp, IMAP4_RCVTIME_DELIM, &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinspadnum(&lsp, (UINT32) ptm->tm_min, 2, _T('0'), TRUE, &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsch(&lsp, IMAP4_RCVTIME_DELIM, &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinspadnum(&lsp, (UINT32) ptm->tm_sec, 2, _T('0'), TRUE, &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinsch(&lsp, _T(' '), &ulRemLen)) != EOK)
		return exc;

	if ((exc=strlinsch(&lsp, ((tmZone <= 0) ? _T('+') : _T('-')), &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinspadnum(&lsp, (UINT32) tmzH, 2, _T('0'), TRUE, &ulRemLen)) != EOK)
		return exc;
	if ((exc=strlinspadnum(&lsp, (UINT32) tmzM, 2, _T('0'), TRUE, &ulRemLen)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE GetIMAP4AutoTag (LPTSTR lpszTag, const UINT32 ulMaxLen)
{
	LPTSTR	lsp=lpszTag;
	UINT32	ulRemLen=ulMaxLen;

	if ((NULL == lpszTag) || (0 == ulMaxLen))
		return EBADBUFF;

	*lsp = _T('\0');
	return strlinspadnum(&lsp, (UINT32) clock(), MAX_DWORD_DISPLAY_LENGTH, _T('0'), TRUE, &ulRemLen);
}

/*---------------------------------------------------------------------------*/

/* makes sure returned welcome is "* OK ....." */
EXC_TYPE CheckIMAP4Welcome (LPCTSTR lpszWelcome)
{
	LPCTSTR	lpszNext=NULL;
	TCHAR		szCode[MAX_IMAP4_OPCODE_LEN+2]=_T("");
	EXC_TYPE	exc=EOK;

	if (IsEmptyStr(lpszWelcome))
		return EEMPTYENTRY;

	if ((lpszWelcome[0] != IMAP4_UNTAGGED_RSP) || (lpszWelcome[1] != _T(' ')))
		return ECONTEXT;

	if ((exc=imap4GetArg((lpszWelcome+2), szCode, MAX_IMAP4_OPCODE_LEN, &lpszNext)) != EOK)
		return exc;

	if (_tcsicmp(szCode, IMAP4_OK) != 0)
		return EUDFFORMAT;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4UpdateStatusValue (const IMAP4STKWCASE	eStCase,
											const UINT32			ulVal,
											LPIMAP4STATUSINFO		pInfo)
{
	if (NULL == pInfo)
		return EBADBUFF;

	switch(eStCase)
	{
		case IMAP4STMSGS		: pInfo->ulNumOfMsgs = ulVal; break;
		case IMAP4STRECENT	: pInfo->ulNumRecent = ulVal; break;
		case IMAP4STUIDNEXT	: pInfo->ulUIDNext = ulVal; break;
		case IMAP4STUDIVALID	: pInfo->ulUIDValidity = ulVal; break;
		case IMAP4STUNSEEN	: pInfo->ulUnseen = ulVal; break;
		default					:
			return EPARAM;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

/* if request starts with '(' it must also end with ')' */
EXC_TYPE imap4ParseStatusRequest (LPCTSTR lpszReq, IMAP4_STRQ_ECFN lpfnEcfn, LPVOID pArg)
{
	if (NULL == lpfnEcfn)
		return EPARAM;

	if (IsEmptyStr(lpszReq))
		return EOK;

	LPCTSTR	lsp=lpszReq;
	for (; _istspace(*lsp) && (*lsp != _T('\0')); lsp++);
	const TCHAR	chStart=(*lsp);

	if (IMAP4_PARLIST_SDELIM == chStart)
		lsp++;

	for (BOOLEAN	fContEnum=TRUE; (*lsp != IMAP4_PARLIST_EDELIM) && (*lsp != _T('\0')); )
	{
		/* find status keyword start */
		for ( ; _istspace(*lsp) && (*lsp != _T('\0')) && (*lsp != IMAP4_PARLIST_EDELIM); lsp++);
		if ((IMAP4_PARLIST_EDELIM == *lsp) || (_T('\0') == *lsp))
			break;

		/* find status keyword end */
		LPCTSTR lpszKW=lsp;
		for (; (!_istspace(*lsp)) && (*lsp != _T('\0')) && (*lsp != IMAP4_PARLIST_EDELIM); lsp++);
		const UINT32			ulKWLen=(lsp - lpszKW);
		const IMAP4STKWCASE	eStCase=imap4GetStatusKeywordCase(lpszKW, ulKWLen);

		EXC_TYPE	exc=(*lpfnEcfn)(lpszKW, ulKWLen, eStCase, pArg, &fContEnum);
		if (exc != EOK)
			return exc;

		/* do not break since the enumeration is not over */
		if (!fContEnum)
			return EOK;
	}

	if (IMAP4_PARLIST_SDELIM == chStart)
	{
		if (*lsp != IMAP4_PARLIST_EDELIM)
			return EUNMATCHEDLISTS;

		lsp++;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4ParseStatusList (LPCTSTR lpszRsp, LPIMAP4STATUSINFO pInfo)
{
	LPCTSTR	lsp=lpszRsp;

	if (IsEmptyStr(lpszRsp) || (NULL == pInfo))
		return EPARAM;
	memset(pInfo, 0xff, (sizeof *pInfo));

	for ( ; _istspace(*lsp) && (*lsp != _T('\0')); lsp++);
	if (_tcsicmp(lsp, IMAP4_NIL) == 0)
		return EOK;

	if (*lsp != IMAP4_PARLIST_SDELIM)
		return EUDFFORMAT;
	lsp++;

	while ((*lsp != IMAP4_PARLIST_EDELIM) && (*lsp != _T('\0')))
	{
		// find status keyword
		for ( ; _istspace(*lsp) && (*lsp != _T('\0')); lsp++);
		LPCTSTR	lpszKW=lsp;
		for ( ; (!_istspace(*lsp)) && (*lsp != _T('\0')); lsp++);
		UINT32	ulKWLen=(lsp - lpszKW);
		IMAP4STKWCASE eStCase=imap4GetStatusKeywordCase(lpszKW, ulKWLen);

		for ( ; _istspace(*lsp) && (*lsp != _T('\0')); lsp++);
		LPCTSTR	lpszVal=lsp;
		for ( ; _istdigit(*lsp) && (*lsp != _T('\0')); lsp++);
		UINT32	ulVLen=(lsp - lpszVal);

		EXC_TYPE	exc=EOK;
		UINT32	ulVal=argument_to_dword(lpszVal, ulVLen, EXC_ARG(exc));
		if (exc != EOK)
			return exc;

		if ((exc=imap4UpdateStatusValue(eStCase, ulVal, pInfo)) != EOK)
			return exc;

		for ( ; _istspace(*lsp) && (*lsp != IMAP4_PARLIST_EDELIM) && (*lsp != _T('\0')); lsp++);
	}

	if (*lsp != IMAP4_PARLIST_EDELIM)
		return EUDFFORMAT;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4ParseStatusRsp (LPCTSTR				lpszRsp,
										LPCTSTR				*lppszFolder,
										UINT32				*pulFNLen,
										LPIMAP4STATUSINFO	pInfo)
{
	UINT32	ulVLen=0;
	LPCTSTR	lpszOp=NULL;
	LPCTSTR	lpszFolder=NULL;
	LPCTSTR	lsp=lpszRsp;

	if (IsEmptyStr(lpszRsp) || (NULL == lppszFolder) || (NULL == pulFNLen) || (NULL == pInfo))
		return EPARAM;

	*lppszFolder = NULL;
	*pulFNLen = 0;

	/* skip untagged sign if found */
	if (IMAP4_UNTAGGED_RSP == *lsp)
		lsp++;

	/* find opcode response */
	for (lsp++; _istspace(*lsp) && (*lsp != _T('\0')); lsp++);
	for (lpszOp=lsp; (!_istspace(*lsp)) && (*lsp != _T('\0')); lsp++);

	/* make sure this is the status response */
	if ((ulVLen=(lsp - lpszOp)) != _tcslen(szIMAP4StatusCmd))
		return EILLEGALOPCODE;
	if (_tcsnicmp(lpszOp, szIMAP4StatusCmd, ulVLen) != 0)
		return EILLEGALOPCODE;

	/* find folder name */
	for (lsp++; _istspace(*lsp) && (*lsp != _T('\0')); lsp++);
	if (IMAP4_QUOTE_DELIM == *(lpszFolder=lsp))
	{
		lpszFolder++;
		for (lsp++; (*lsp != IMAP4_QUOTE_DELIM) && (*lsp != _T('\0')); lsp++);
		if (*lsp != IMAP4_QUOTE_DELIM)
			return EPATHNAMESYNTAX;

		*pulFNLen = (lsp - lpszFolder);
		lsp++;	/* skip quote */
	}
	else	/* non-quoted folder string */
	{
		/* Allow non-quoted folder string... (e.g. Sun Internet Mail Server) */
		for (lsp++; (*lsp != IMAP4_PARLIST_SDELIM) && (*lsp != _T('\0')); lsp++);

		/* find end of non-quoted folder string */
		for (lsp--; _istspace(*lsp) && (lsp > lpszFolder); lsp--);

		lsp++; /* skip non-space */
		*pulFNLen = (lsp - lpszFolder);
	}
	*lppszFolder = lpszFolder;

	return imap4ParseStatusList(lsp, pInfo);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4ParseNumAndOpPair (LPCTSTR lpszRsp,
											UINT32	*pulMsgID,
											LPCTSTR	*lppszOpcode,
											UINT32	*pulOpLen)

{
	EXC_TYPE	exc=EOK;
	LPCTSTR	lsp=lpszRsp, lpszVal=NULL;
	UINT32	ulVLen=0;

	if (IsEmptyStr(lpszRsp) || (NULL == pulMsgID) || (NULL == lppszOpcode) || (NULL == pulOpLen))
		return EPARAM;
	*pulMsgID = 0;
	*lppszOpcode = NULL;
	*pulOpLen = 0;

	/* skip untagged sign if found */
	if (IMAP4_UNTAGGED_RSP == *lsp)
		lsp++;

	/* find msg ID */
	for (lsp++; _istspace(*lsp) && (*lsp != _T('\0')); lsp++);
	for (lpszVal=lsp; _istdigit(*lsp) && (*lsp != _T('\0')); lsp++);
	ulVLen = (lsp - lpszVal);

	*pulMsgID = argument_to_dword(lpszVal, ulVLen, EXC_ARG(exc));
	if (exc != EOK)
		return exc;

	/* find msg opcode */
	for (lsp++; _istspace(*lsp) && (*lsp != _T('\0')); lsp++);
	for (lpszVal=lsp; (!_istspace(*lsp)) && (*lsp != _T('\0')); lsp++);
	if (0 == (ulVLen=(lsp - lpszVal)))
		return EEMPTYENTRY;

	*lppszOpcode = lpszVal;
	*pulOpLen = ulVLen;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4ExtractNumAndOpPair (LPCTSTR		lpszRsp,
											  UINT32			*pulMsgID,
											  LPTSTR			lpszOpcode,
											  const UINT32	ulMaxOpLen)
{
	LPCTSTR	lpszOp=NULL;
	UINT32	ulOpLen=0;
	EXC_TYPE	exc=imap4ParseNumAndOpPair(lpszRsp, pulMsgID, &lpszOp, &ulOpLen);

	if (EOK == exc)
	{
		if ((NULL == lpszOpcode) || (0 == ulMaxOpLen))
			return EPARAM;
		if (ulOpLen >= ulMaxOpLen)
			return EOVERFLOW;

		_tcsncpy(lpszOpcode, lpszOp, ulOpLen);
		lpszOpcode[ulOpLen] = _T('\0');
	}

	return exc;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4ParseExpungeRsp (LPCTSTR lpszRsp,
										 UINT32	*pulMsgID,
										 LPCTSTR	*lppszOpcode,
										 UINT32	*pulOpLen)
{
	return imap4ParseNumAndOpPair(lpszRsp, pulMsgID, lppszOpcode, pulOpLen);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4ParseStoreRsp (LPCTSTR			lpszRsp,
									  UINT32				*pulMsgID,
									  LPIMAP4MSGFLAGS	pFlags)
{
	EXC_TYPE	exc=EOK;
	TCHAR		szOp[MAX_IMAP4_OPCODE_LEN+2];
	UINT32	ulVLen=0;
	LPCTSTR	lpszVal=NULL, lsp=NULL;

	if (IsEmptyStr(lpszRsp) || (NULL == pulMsgID) || (NULL == pFlags))
		return EPARAM;

	*pulMsgID = 0;
	memset(pFlags, 0, (sizeof *pFlags));

	if ((exc=imap4ParseNumAndOpPair(lpszRsp, pulMsgID, &lpszVal, &ulVLen)) != EOK)
		return exc;

	/* make sure correct response code(s) */
	if (_tcsnicmp(lpszVal, szIMAP4FetchCmd, ulVLen) != 0)
		return EILLEGALOPCODE;

	for (lsp = (lpszVal+ulVLen); _istspace(*lsp) && (*lsp != IMAP4_PARLIST_SDELIM) && (*lsp != _T('\0')); lsp++);
	if (*lsp != IMAP4_PARLIST_SDELIM)
		return EUDFFORMAT;
	lsp++;

	if ((exc=imap4GetArg(lsp, szOp, MAX_IMAP4_OPCODE_LEN, &lsp)) != EOK)
		return exc;
	if (_tcsicmp(szOp, IMAP4_FLAGS) != 0)
		return EILLEGALOPCODE;

	if ((exc=imap4ParseMsgFlags(lsp, pFlags)) != EOK)
		return exc;

	/* Note: we do not check for terminating ')' */
	return EOK;
}

/*---------------------------------------------------------------------------*/

/* Note: places EOS where adequate */
EXC_TYPE imap4AdjustArg (LPTSTR	lpszLine,
								 LPCTSTR	*lppszArg,
								 UINT32	*pulArgLen,
								 LPCTSTR	*lppszNext)
{
	LPTSTR	lpszCurPos=lpszLine;
	TCHAR		chDelim=_T('\0');

	if (IsEmptyStr(lpszLine) || (NULL == lppszArg) || (NULL == pulArgLen) || (NULL == lppszNext))
		return EPARAM;

	*lppszArg = NULL;
	*pulArgLen = 0;
	*lppszNext = NULL;

	for (; _istspace(*lpszCurPos) && (*lpszCurPos != _T('\0')); lpszCurPos++);
	if (_T('\0') == *lpszCurPos)
	{
		*lppszArg = lpszCurPos;
		*lppszNext = lpszCurPos;
		return EOK;
	}

	for (*lppszArg=lpszCurPos, chDelim=*lpszCurPos, lpszCurPos++; *lpszCurPos != _T('\0'); lpszCurPos++)
	{
		if (IMAP4_QUOTE_DELIM == chDelim)
		{
			if (IMAP4_QUOTE_DELIM == *lpszCurPos)
			{
				/* make sure not an escaped quote */
				if (*(lpszCurPos-1) != _T('\\'))
				{
					lpszCurPos++;
					break;
				}
			}
		}
		else /* originally not quoted */
		{
			if (_istspace(*lpszCurPos))
				break;
		}
	}

	*pulArgLen = (lpszCurPos - (*lppszArg));

	if (_T('\0') == *lpszCurPos)
	{
		*lppszNext = lpszCurPos;
		return EOK;
	}
	*lpszCurPos = _T('\0');

	for (lpszCurPos++; _istspace(*lpszCurPos) && (*lpszCurPos != _T('\0')); lpszCurPos++);

	*lppszNext = lpszCurPos;
	return EOK;
}

/* Note: changes command buffer by adding EOS(s) */
EXC_TYPE imap4AdjustCmd (LPTSTR	lpszCmd,
								 LPCTSTR	*lppszTag,
								 LPCTSTR	*lppszOp,
								 BOOLEAN	*pfIsUID,
								 LPCTSTR	*lppszArgs)
{
	EXC_TYPE	exc=EOK;
	UINT32	ulCLen=0;
	LPCTSTR	lpszCurPos=lpszCmd;

	if (IsEmptyStr(lpszCmd) || (NULL == lppszTag) || (NULL == lppszOp) ||
		 (NULL == pfIsUID) || (NULL == lppszArgs))
		return EPARAM;

	*lppszTag = NULL;
	*lppszOp = NULL;
	*pfIsUID = FALSE;
	*lppszArgs = NULL;

	if (_istspace(*lpszCurPos))
		return ENOPREFIX;

	/* find tag */
	if ((exc=imap4AdjustArg((LPTSTR) lpszCurPos, lppszTag, &ulCLen, &lpszCurPos)) != EOK)
		return exc;
	if (0 == ulCLen)
		return ENOPREFIX;
	if (_T('\0') == *lpszCurPos)
		return ECHRNG;

	/* find opcode */
	if ((exc=imap4AdjustArg((LPTSTR) lpszCurPos, lppszOp, &ulCLen, &lpszCurPos)) != EOK)
		return exc;
	/* we must have an opcode */
	if (0 == ulCLen)
		return EILLEGALOPCODE;

	if (0 == _tcsicmp(*lppszOp, IMAP4_UID))
	{
		*pfIsUID = TRUE;

		/* find opcode */
		if ((exc=imap4AdjustArg((LPTSTR) lpszCurPos, lppszOp, &ulCLen, &lpszCurPos)) != EOK)
			return exc;
		/* an opcode always follows the UID modifier */
		if (0 == ulCLen)
			return EL3HLT;
	}

	/* UID modifier requires at least one more argument to the command */
	if ((*pfIsUID) && (_T('\0') == *lpszCurPos))
		return EUNATCH;

	*lppszArgs = lpszCurPos;
	return EOK;
}

/*---------------------------------------------------------------------------*/

/* Note: adds EOS(s) where appropriate */
EXC_TYPE imap4AdjustRangeCmd (LPTSTR	lpszArgs,
										LPCTSTR	*lppszMsgsList,
										LPCTSTR	*lppszModifiers)
{
	UINT32	ulMLen=0;
	LPCTSTR	lpszMsgsList=NULL;
	EXC_TYPE	exc=imap4AdjustArg(lpszArgs, lppszMsgsList, &ulMLen, lppszModifiers);
	if (exc != EOK)
		return exc;

	/* a message range must exist */
	if (0 == ulMLen)
		return EIONODATA;

	/* after the messages range there must be at least one modifier */
	if (IsEmptyStr(*lppszModifiers))
		return EIONOSPARES;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4XlateStoreFlagsOperation (LPCTSTR						lpszStoreOp,
													 IMAP4STOREFLAGSOPCASE	*peOpCase,
													 BOOLEAN						*pfIsSilent)
{
	LPCTSTR	lpszFStart=lpszStoreOp, lpszSStart=NULL;

	if (IsEmptyStr(lpszStoreOp) || (NULL == peOpCase) || (NULL == pfIsSilent))
		return EPARAM;
	*pfIsSilent = FALSE;

	if (_T('+') == *lpszFStart)
	{
		lpszFStart++;
		*peOpCase = IMAP4_STOREADD_FLAGS;
	}
	else if (_T('-') == *lpszFStart)
	{
		lpszFStart++;
		*peOpCase = IMAP4_STOREDEL_FLAGS;
	}
	else
		*peOpCase = IMAP4_STORESET_FLAGS;

	if (NULL == (lpszSStart=_tcsrchr(lpszFStart, _T('.'))))
	{
		if (_tcsicmp(lpszFStart, IMAP4_FLAGS) != 0)
			return EILLEGALOPCODE;
	}
	else	/* have ".xxx" */
	{
		UINT32	ulFOpLen=(lpszSStart - lpszFStart);

		if (_tcsicmp(lpszSStart, IMAP4_SILENT) != 0)
			return EILLEGALOPCODE;

		if ((ulFOpLen != _tcslen(IMAP4_FLAGS)) || (_tcsnicmp(lpszFStart, IMAP4_FLAGS, ulFOpLen) != 0))
			return EILLEGALOPCODE;

		*pfIsSilent = TRUE;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4ExtractStoreCmdArgs (LPCTSTR					lpszStoreArgs,
											  IMAP4STOREFLAGSOPCASE	*peOpCase,
											  BOOLEAN					*pfIsSilent,
											  IMAP4_MSGFLAGS			*pFlags)
{
	TCHAR		szOp[MAX_IMAP4_OPCODE_LEN+2];
	LPCTSTR	lpszNext=NULL;
	EXC_TYPE	exc=imap4GetArg(lpszStoreArgs, szOp, MAX_IMAP4_OPCODE_LEN, &lpszNext);
	if (exc != EOK)
		return exc;

	if ((exc=imap4XlateStoreFlagsOperation(szOp, peOpCase, pfIsSilent)) != EOK)
		return exc;

	if ((exc=imap4ParseMsgFlags(lpszNext, pFlags)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/
