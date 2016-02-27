#include <_types.h>

#include <util/string.h>
#include <util/tables.h>
#include <util/errors.h>

#include <internet/rfc822.h>
#include <internet/base64.h>
#include <internet/qpenc.h>

/*---------------------------------------------------------------------------*/

EXC_TYPE IsRFC822MIMEBoundary (const char	lpszLine[],
										 const char	lpszMIMEBoundary[],
										 BOOLEAN		*pfIsBoundary,
										 BOOLEAN		*pfIsLast)
{
	UINT32		iLen=0, mLen=0;
	const char	*lsp=lpszLine;

	if ((NULL == lpszLine) || (NULL == lpszMIMEBoundary) ||
		 (NULL == pfIsBoundary) || (NULL == pfIsLast))
		return EPARAM;

	*pfIsBoundary = FALSE;
	*pfIsLast = FALSE;

	/* if empty boundary then do nothing */
	if (0 == (mLen=strlen(lpszMIMEBoundary)))
		return EOK;

	/* make sure input can accomodate the delimiters and the boundary string */
	if ((iLen=strlen(lpszLine)) < (mLen + RFC822_MIME_BOUNDARY_DELIMS_LEN))
		return EOK;

	/* make sure prefix is the required delimiters */
	if (strnicmp(lsp, pszMIMEBoundaryDelims, RFC822_MIME_BOUNDARY_DELIMS_LEN) != 0)
		return EOK;

	lsp += RFC822_MIME_BOUNDARY_DELIMS_LEN;
	if (strnicmp(lsp, lpszMIMEBoundary, mLen) != 0)
		return EOK;

	*pfIsBoundary = TRUE;

	/* check if last boundary */
	lsp += mLen;
	*pfIsLast = (0 == strnicmp(lsp, pszMIMEBoundaryDelims, RFC822_MIME_BOUNDARY_DELIMS_LEN));
	return EOK;
}

/*---------------------------------------------------------------------------*/

static BOOLEAN IsInSubTypesMap (LPCTSTR lpszSubType, const CStr2PtrMapper& subsMap)
{
	if (IsEmptyStr(lpszSubType))
		return FALSE;

	LPVOID	pVal=NULL;
	EXC_TYPE	hr=subsMap.FindKey(lpszSubType, pVal);
	return (EOK == hr);
}

/*---------------------------------------------------------------------------*/

static const STR2PTRASSOC mpSubTypes[]={
	{	pszMIMEMixedSubType,			(LPVOID) pszMIMEMixedSubType			},
	{	pszMIMEVoiceMsgSubType,		(LPVOID) pszMIMEVoiceMsgSubType		},
	{	pszMIMEAlternativeSubType,	(LPVOID) pszMIMEAlternativeSubType	},
	{	pszMIMEDigestSubType,		(LPVOID) pszMIMEDigestSubType			},
	{	pszMIMERelatedSubType,		(LPVOID)	pszMIMERelatedSubType		},
	{	pszMIMEParallelSubType,		(LPVOID)	pszMIMEParallelSubType		},
	{	pszMIMEReportSubType,		(LPVOID) pszMIMEReportSubType			},

	/* some non-standard multipart sub-types */
	{	pszMIMEAppleDoubleSubType,	(LPVOID) pszMIMEAppleDoubleSubType	},

	{	NULL,								NULL											}	// mark end
};

static const CStr2PtrMapper mpSubTypesMap(mpSubTypes, 0, FALSE);

static const STR2PTRASSOC msgSubTypes[]={
	{	pszMIMERfc822SubType,		(LPVOID) pszMIMERfc822SubType			},
	{	pszMIMEDlvryStatusSubType,	(LPVOID) pszMIMEDlvryStatusSubType	},
	{	NULL,								NULL											}	// mark end
};

static const CStr2PtrMapper msgSubTypesMap(msgSubTypes, 0, FALSE);

/*---------------------------------------------------------------------------*/

BOOLEAN IsRFC822MultipartSubType (LPCTSTR lpszSubType)
{
	return IsInSubTypesMap(lpszSubType, mpSubTypesMap);
}

BOOLEAN IsRFC822MsgSubType (LPCTSTR lpszSubType)
{
	return IsInSubTypesMap(lpszSubType, msgSubTypesMap);
}

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

EXC_TYPE rfc822ExtractHdrData (const char		pszHdr[],
										 char				pszHdrName[],
										 const UINT32	ulNameLen,
										 char				pszHdrValue[],
										 const UINT32	ulValueLen)
{
	const char	*lpszHName=pszHdr;
	const char	*lpszHVal=NULL;
	UINT32		ulNLen=0, ulVLen=0;

	if (IsEmptyStr(pszHdr) || (NULL == pszHdrName) || (NULL == pszHdrValue) ||
		 (0 == ulNameLen) || (0 == ulValueLen))
		return EPARAM;

	*pszHdrName = _T('\0');
	*pszHdrValue = _T('\0');

	if (IsSafeSpace(*pszHdr))
	{
		lpszHName = _T("");
		lpszHVal = pszHdr;

		ulNLen = 0;
		ulVLen = strlen(pszHdr);
	}
	else	/* non-space */
	{
		/* find header name delimiter */
		for (lpszHVal=(lpszHName+1); (*lpszHVal != ':') && (*lpszHVal != '\0'); lpszHVal++);

		if (*lpszHVal != ':')
			return EUDFFORMAT;

		lpszHVal++;	/* skip name delimiter */
		ulNLen = (lpszHVal - pszHdr);

		/* skip any leading white-space in value */
		for ( ; IsSafeSpace(*lpszHVal) &&  (*lpszHVal != _T('\0')); lpszHVal++);

		ulVLen = strlen(lpszHVal);
	}

	if ((ulNLen >= ulNameLen) || (ulVLen >= ulValueLen))
		return EOVERFLOW;

	strncpy(pszHdrName, lpszHName, ulNLen);
	pszHdrName[ulNLen] = '\0';

	strncpy(pszHdrValue, lpszHVal, ulVLen);
	pszHdrValue[ulVLen] = '\0';

	if ('\0' == *pszHdrName)
		return ECONTINUED;

	return EOK;
}

/*---------------------------------------------------------------------------*/

/*		Extracts pointers to RFC822 header name & value. Returns the following
 * values:
 *
 *		EOK - everything OK
 *		ECONTINUED - if 1st char is space - then name is set to NULL and value
 *				is set to rest of value (including the leading space)
 *		EUDFFORMAT - if 1st char not space and not found header name delimiter ':'
 *		Otherwise - non EOK
 */

EXC_TYPE rfc822ExtractHdrPtrs (const char	pszHdr[],
										 const char	**ppszHdrName,
										 UINT32		*pulNameLen,
										 const char	**ppszHdrValue)
{
	const char	*tsp=pszHdr;

	if (IsEmptyStr(pszHdr) || (NULL == ppszHdrName) || (NULL == pulNameLen) || (NULL == ppszHdrValue))
		return EPARAM;

	*ppszHdrName = NULL;
	*pulNameLen = 0;
	*ppszHdrValue = NULL;

	/* check white-space leading char */
	if (isspace(*pszHdr))
	{
		*ppszHdrValue = pszHdr;
		return ECONTINUED;
	}

	/* find header name delimiter */
	for (tsp++; (*tsp != ':') && (*tsp != '\0'); tsp++);

	if (*tsp != ':')
	{
		/* detect immediate MIME */
		UINT32	ulLen=strlen(pszHdr);
		if ((ulLen > RFC822_MIME_BOUNDARY_DELIMS_LEN) &&
			 (strnicmp(pszHdr, pszMIMEBoundaryDelims, RFC822_MIME_BOUNDARY_DELIMS_LEN) == 0))
			return ESTREAMSPECIAL;

		return EUDFFORMAT;
	}
	tsp++;	/* skip delimiter */

/* next char must be either a space or EOS
	if ((!isspace(*tsp)) && (*tsp != '\0'))
		return EUIDNEXIST;
*/
	*pulNameLen = (tsp - pszHdr);

	/* skip any leading white-space in value */
	for ( ; isspace(*tsp) && (*tsp != '\0'); tsp++);

	*ppszHdrName = pszHdr;
	*ppszHdrValue = tsp;
	return EOK;
}

/*---------------------------------------------------------------------------*/

/*		Analyzes and extracts the "type/sub-type" format
 *
 * Note: returns EEXIST if no content type found (e.g. continued on next line)
 */
EXC_TYPE rfc822ExtractContentTypePtrs (const char	lpszContentType[],
													const char	**lppszType,
													UINT32		*pulTypeLen,
													const char	**lppszSubType,
													UINT32		*pulSubTypeLen)
{
	const char	*lpszCType=lpszContentType, *ksp=NULL, *lsp=NULL;

	if ((NULL == lpszCType) || (NULL == lppszType) || (NULL == pulTypeLen) ||
		 (NULL == lppszSubType) || (NULL == pulSubTypeLen))
		return EPARAM;

	*lppszType = NULL;
	*pulTypeLen = 0;
	*lppszSubType = NULL;
	*pulSubTypeLen = 0;

	for ( ; isspace(*lpszCType) && (*lpszCType != '\0'); lpszCType++);

	if ('\0' == *lpszCType)
		return EEXIST;

	if (NULL == (ksp=strchr(lpszCType, RFC822_MIMETAG_SEP)))
		return EUDFFORMAT;

	*lppszType = lpszCType;
	if (0 == (*pulTypeLen=(ksp - lpszCType)))
		return ENOPREFIX;

	/* find end of sub-type */
	for (ksp++, lsp=ksp; (!isspace(*lsp)) && (*lsp != RFC822_LIST_DELIM) && (*lsp != '\0'); lsp++);

	*lppszSubType = ksp;
	if (0 == (*pulSubTypeLen=(lsp - ksp)))
		return EEMPTYENTRY;

	return EOK;
}

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
EXC_TYPE AddRFC822MIMETag (LPCTSTR lpszType, LPCTSTR lpszSubType, IStrlBuilder& strb)
{
	if (IsEmptyStr(lpszType) || IsEmptyStr(lpszSubType))
		return EPARAM;

	EXC_TYPE	exc=strb.AddStr(lpszType);
	if (exc != EOK)
		return exc;
	if ((exc=strb.AddChar(RFC822_MIMETAG_SEP)) != S_OK)
		return exc;
	if ((exc=strb.AddStr(lpszSubType)) != S_OK)
		return exc;

	return S_OK;
}

EXC_TYPE BuildRFC822MIMETag (LPCTSTR lpszType, LPCTSTR lpszSubType, LPTSTR lpszTag, const UINT32 ulMaxLen)
{
	if ((NULL == lpszTag) || (0 == ulMaxLen))
		return EPARAM;
	else
		return AddRFC822MIMETag(lpszType, lpszSubType, CStrlBuilder(lpszTag, ulMaxLen));
}
#endif

/*--------------------------------------------------------------------------*/

BOOLEAN CompareRFC822TypeComp (const char		lpszSrcComp[],
										 const UINT32	ulSrcLen,
										 const char		lpszDstComp[])
{
	UINT32	ulDstLen=0;

	if ((NULL == lpszSrcComp) || (NULL == lpszDstComp) ||
		 ('\0' == *lpszSrcComp) || ('\0' == *lpszDstComp) ||
		 (0 == ulSrcLen))
		return FALSE;

	if ((ulDstLen=strlen(lpszDstComp)) != ulSrcLen)
		return FALSE;

	return (strnicmp(lpszSrcComp, lpszDstComp, ulSrcLen) == 0);
}

/*--------------------------------------------------------------------------*/

/* returns EFNEXIST if not found keyword */
EXC_TYPE RFC822FindKeywordValue (const char	lpszValue[],
										   const char	lpszKeyword[],
										   const char	**lppszKeyVal,
										   UINT32		*pulValLen)
{
	const char	*lpszB=NULL, *tsp=NULL, *lpszV=lpszValue;
	UINT32		ulKLen=0, ulSLen=0;

	if ((NULL == lpszValue) || IsEmptyStr(lpszKeyword) ||
		 (NULL == lppszKeyVal) || (NULL == pulValLen))
		return EPARAM;
	*lppszKeyVal = NULL;
	*pulValLen = 0;

	/* look for boundary keyword */
	ulSLen = strlen(lpszKeyword);
	for (lpszB=strchr(lpszV, '='); lpszB != NULL; lpszB = strchr(lpszV, '='))
	{
		/* go backwards from keyword delimiter */
		for (tsp=(lpszB-1); tsp > lpszV; tsp--)
		{
			if (isspace(*tsp) || (RFC822_LIST_DELIM == *tsp))
			{
				tsp++;
				break;
			}
		}

		/* check that this is the keyword */
		ulKLen = (lpszB - tsp);

		if ((ulKLen == ulSLen) && (strnicmp(tsp,lpszKeyword,ulKLen) == 0))
			break;

		lpszB++;	/* skip '=' sign */

		// skip value
		if ('\"' == *lpszB)
		{
			lpszB++;
			if (NULL == (lpszB=strchr(lpszB, '\"')))
				return EUDFFORMAT;
			lpszB++;
		}
		else	// non-delimited value
		{
			for ( ; (!isspace(*lpszB)) && (RFC822_LIST_DELIM != *lpszB) && (*lpszB != '\0'); lpszB++);
		}

		lpszV = lpszB;
	}

	/* at this point "lpszB" should point to '=' sign */
	if (NULL == lpszB)
		return EFNEXIST;

	lpszB++;
	if ('\"' == *lpszB)
	{
		lpszB++;

		if (NULL == (tsp=strchr(lpszB, '\"')))
			return EUDFFORMAT;
	}
	else	// non-delimited boundary
	{
		for (tsp=lpszB; (!isspace(*tsp)) && (RFC822_LIST_DELIM != *tsp) && (*tsp != '\0'); tsp++);
	}

	*lppszKeyVal = lpszB;
	*pulValLen = (tsp - lpszB);
	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE RFC822GetKeywordValue (const char	lpszValue[],
										  const char	lpszKeyword[],
										  char			lpszKeyVal[],
										  const UINT32	ulValLen)
{
	const char	*lpszV=NULL;
	UINT32		ulVLen=0;
	EXC_TYPE		exc=EOK;

	if ((NULL == lpszKeyVal) || (0 == ulValLen))
		return EPARAM;
	*lpszKeyVal = '\0';

	if ((exc=RFC822FindKeywordValue(lpszValue, lpszKeyword, &lpszV, &ulVLen)) != EOK)
		return exc;

	if (ulVLen >= ulValLen)
		return EOVERFLOW;

	strncpy(lpszKeyVal, lpszV, ulVLen);
	lpszKeyVal[ulVLen] = '\0';
	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE RFC822FindAttachName (const char	lpszValue[],
										 const char	**lppszAttachName,
										 UINT32		*pulNameLen)
{
	EXC_TYPE exc=RFC822FindKeywordValue(lpszValue, pszMIMEFilenameKeyword,
												   lppszAttachName, pulNameLen);
	if (EFNEXIST == exc)
		exc = RFC822FindKeywordValue(lpszValue, pszMIMENameKeyword,
											  lppszAttachName, pulNameLen);

	return exc;
}

/* returns EFNEXIST if not found */
EXC_TYPE RFC822ExtractAttachName (const char		lpszValue[],
											 char				lpszAttachName[],
											 const UINT32	ulNameLen)
{
	const char	*lpszAN=NULL;
	UINT32		ulALen=0;
	EXC_TYPE		exc=EOK;

	if ((NULL == lpszAttachName) || (0 == ulNameLen))
		return EPARAM;
	*lpszAttachName = '\0';

	if ((exc=RFC822FindAttachName(lpszValue, &lpszAN, &ulALen)) != EOK)
		return exc;

	if (ulALen >= ulNameLen)
		return EOVERFLOW;

	strncpy(lpszAttachName, lpszAN, ulALen);
	lpszAttachName[ulALen] = '\0';
	return EOK;
}

/*--------------------------------------------------------------------------*/

/* comparison is case insensitive */
static const char *rfc822EncStrings[]={
	"none",				/* no specific encoding */
	b64XferEncoding,	/* base64 */
	qpXferEncoding,	/*	quoted-printable */
	"7bit",
	"8bit",
	NULL		/* mark end of list */
};

/*--------------------------------------------------------------------------*/

RFC822ENCCASE RFC822EncodingStr2Case (const char lpszEncoding[])
{
	UINT32	idx=0;

	if (IsEmptyStr(lpszEncoding))
		return RFC822_BAD_ENC;

	for (idx=0; ; idx++)
	{
		const char	*lpszEncStr=rfc822EncStrings[idx];
		if (IsEmptyStr(lpszEncStr))
			break;

		if (stricmp(lpszEncStr, lpszEncoding) == 0)
			return ((RFC822ENCCASE) idx);
	}

	/* check for special case(s) */
	if (stricmp(lpszEncoding, "binary") == 0)
		return RFC822_8BIT_ENC;
	/* sometimes HTML clients use "gm" instead of "-" */
	else if (stricmp(lpszEncoding, "quotedgmprintable") == 0)
		return RFC822_QP_ENC;

	/* if unknown, then return 8bit */
	return RFC822_8BIT_ENC;
}

/*--------------------------------------------------------------------------*/

const char *RFC822EncodingCase2Str (const RFC822ENCCASE encCase)
{
	if (fIsBadRFC822EncCase(encCase))
		return NULL;

	return rfc822EncStrings[encCase];
}

/*--------------------------------------------------------------------------*/

EXC_TYPE DecodeRFC822AddrPair (LPCTSTR	lpszHdrValue,
										 LPCTSTR	*lppszName,
										 UINT32	*pulNameLen,
										 LPCTSTR	*lppszAddr,
										 UINT32	*pulAddrLen)
{
	LPCTSTR	lsp=lpszHdrValue, tsp=NULL;
	TCHAR		chSep=_T('\0');

	if ((NULL == lppszName) || (NULL == pulNameLen) ||
		 (NULL == lppszAddr) || (NULL == pulAddrLen))
		return EPARAM;

	*lppszName = NULL;
	*pulNameLen = 0;
	*lppszAddr = NULL;
	*pulAddrLen = 0;

	if (IsEmptyStr(lpszHdrValue))
		return EOK;

	/*
	 *		Expected format is: "...name..." <addr> where name might not be
	 * delimited or even missing
	 */
	for ( ; IsSafeSpace(*lsp) && (*lsp != _T('\0')); lsp++)
		if ((unsigned) *lsp >= (unsigned) 0x0080)
			break;

	/* allow the non-standard single-quote as well */
	if ((_T('\"') == *lsp) || (_T('\'') == *lsp))
	{
		chSep = *lsp;
		lsp++;

		if (NULL == (tsp=_tcschr(lsp, chSep)))
			return EBADADDR;

		*lppszName = lsp;
		*pulNameLen = (tsp - lsp);

		/* make sure nothing more between end of delimited name and start of address */
		if ((lsp=_tcschr((tsp+1), EMAIL_PATH_SDELIM)) != NULL)
		{
			LPCTSTR	ksp=((*lppszName) + (*pulNameLen));
			for (tsp=(lsp-1); tsp > ksp; tsp--)
				if (!IsSafeSpace(*tsp))
					break;

			/* somthing follows the delimiter  - so take everything as name */
			if (*tsp != chSep)
			{
				(*lppszName)--;
				*pulNameLen = (tsp - (*lppszName) + 1);
			}
		}
		else /* no address delimiter */
		{
			lsp = (tsp + 1);
		}
	}
	else if (EMAIL_PATH_SDELIM != *lsp) /* non-delimited name */
	{
		/* if no address delimiter, assume only address supplied (no name) */
		if (NULL == (tsp=_tcschr(lsp, EMAIL_PATH_SDELIM)))
		{
			UINT32	ulVLen=_tcslen(lsp);
			EXC_TYPE	exc=ValidateRFC822EmailAddr(lsp, ulVLen);

			/* if a valid address, then no name, otherwise assume this is the name */
			if (EOK == exc)
			{
				*lppszName = _T("");
				*pulNameLen = 0;

				*lppszAddr = lsp;
				*pulAddrLen = ulVLen;
			}
			else
			{
				*lppszName = lsp;
				*pulNameLen = ulVLen;

				*lppszAddr = _T("");
				*pulAddrLen = 0;
			}

			lsp += ulVLen;
		}
		else	/* have delimiter */
		{
			*lppszName = lsp;
			*pulNameLen = (tsp - lsp);

			lsp = tsp;
		}
	}

	if (NULL == *lppszAddr)
	{
		for ( ; IsSafeSpace(*lsp) && (*lsp != EMAIL_PATH_SDELIM) && (*lsp != _T('\0')); lsp++)
			if ((unsigned) (*lsp) >= (unsigned) 0x0080)
				break;

		if (EMAIL_PATH_SDELIM == *lsp)
		{
			chSep = EMAIL_PATH_EDELIM;
			lsp++;

			if (_T('[') == *lsp)
			{
				chSep = _T(']');
				lsp++;
			}

			if (NULL == (tsp=_tcschr(lsp, chSep)))
				return EBADADDR;

			/* skip preceding white space */
			for ( ; IsSafeSpace(*lsp) && (*lsp != _T('\0')); lsp++);

			*lppszAddr = lsp;
			*pulAddrLen = (tsp - lsp);

			lsp = (tsp + 1);
		}
		else	// empty address
		{
			*lppszAddr = _T("");
		}
	}

	/* remove trailing spaces from name & address */
	if (*pulNameLen > 0)
	{
		for (LPCTSTR lpszNE=((*lppszName) + (*pulNameLen) - 1); *pulNameLen > 0; lpszNE--)
			if (((unsigned) *lpszNE <= (unsigned) 0x007F) && IsSafeSpace(*lpszNE))
				(*pulNameLen)--;
			else
				break;
	}

	if (*pulAddrLen > 0)
	{
		for (LPCTSTR lpszAE=((*lppszAddr) + (*pulAddrLen) - 1); *pulAddrLen > 0; lpszAE--)
			if (((unsigned) *lpszAE <= (unsigned) 0x007F) && IsSafeSpace(*lpszAE))
				(*pulAddrLen)--;
			else
				break;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/


EXC_TYPE BuildRFC822AddrPair (LPCTSTR			lpszRecipName,	/* may be NULL */
										LPCTSTR			lpszRecipAddr,
										IStrlBuilder&	aPair)
{
	EXC_TYPE	exc=EOK;

	if (IsEmptyStr(lpszRecipAddr))
		return EEMPTYENTRY;

	if (!IsEmptyStr(lpszRecipName))
	{
		if ((exc=CheckRFC822AtomText(lpszRecipName)) != EOK)
		{
			if ((exc=aPair.AddChar(_T('\"'))) != EOK)
				return exc;

			// escape quote signs (if any)
			LPCTSTR	lpszCurPos=lpszRecipName;
			while (*lpszCurPos != _T('\0'))
			{
				LPCTSTR	lpszQPos=_tcschr(lpszCurPos, _T('\"'));
				if (IsEmptyStr(lpszQPos))
				{
					if ((exc=aPair.AddStr(lpszCurPos)) != EOK)
						return exc;

					break;
				}
				else	// escape the quote sign
				{
					if ((exc=aPair.AddChars(lpszCurPos, (lpszQPos - lpszCurPos))) != EOK)
						return exc;

					if ((exc=aPair.AddStr(_T("\\\""))) != EOK)
						return exc;

					lpszCurPos = (lpszQPos + 1);
				}
			}

			if ((exc=aPair.AddChar(_T('\"'))) != EOK)
				return exc;
		}
		else	// atom text - just add it
		{
			if ((exc=aPair.AddStr(lpszRecipName)) != EOK)
				return exc;
		}

		if ((exc=aPair.AddChar(_T(' '))) != EOK)
			return exc;
	}

	if (EMAIL_PATH_SDELIM == *lpszRecipAddr)
	{
		// make sure also ending in delimiter
		UINT32	ulRALen=_tcslen(lpszRecipAddr);
		LPCTSTR	lpszRAE=(lpszRecipAddr + ulRALen);
		if (*(lpszRAE-1) != EMAIL_PATH_EDELIM)
			return EUDFIO;

		// make sure delimiters do not appear inside the address
		if ((_tcsnchr((lpszRecipAddr+1), EMAIL_PATH_SDELIM, (ulRALen - 2)) != NULL) ||
			 (_tcsnchr((lpszRecipAddr+1), EMAIL_PATH_EDELIM, (ulRALen - 2)) != NULL))
			 return EBADADDR;

		// make sure this is a valid e-mail address
		if ((exc=ValidateRFC822EmailAddr((lpszRecipAddr + 1), (ulRALen - 2))) != EOK)
			return exc;

		if ((exc=aPair.AddStr(lpszRecipAddr)) != EOK)
			return exc;
	}
	else	// non-delimited address
	{
		// make sure this is a valid e-mail address
		if ((exc=ValidateRFC822Email(lpszRecipAddr)) != EOK)
			return exc;

		if ((exc=aPair.AddChar(EMAIL_PATH_SDELIM)) != EOK)
			return exc;
		if ((exc=aPair.AddStr(lpszRecipAddr)) != EOK)
			return exc;
		if ((exc=aPair.AddChar(EMAIL_PATH_EDELIM)) != EOK)
			return exc;
	}

	return EOK;
}

EXC_TYPE BuildRFC822AddrPair (LPCTSTR			lpszRecipName,	/* may be NULL */
										LPCTSTR			lpszRecipAddr,
										LPTSTR			lpszAddrPair,
										const UINT32	ulMaxLen)
{
	CStrlBuilder	aPair(lpszAddrPair, ulMaxLen);
	return BuildRFC822AddrPair(lpszRecipName, lpszRecipAddr, aPair);
}

/*---------------------------------------------------------------------------*/

/* Enumerates members of an address pair list - e.g. "To:", "Cc:" */
EXC_TYPE EnumRFC822AddrPairList (LPCTSTR			lpszHdrValue,
											RFC822_AP_ECFN	lpfnEcfn,
											LPVOID			pArg)
{
	if (NULL == lpfnEcfn)
		return EBADADDR;

	for (LPCTSTR	lpszR=lpszHdrValue; (!IsEmptyStr(lpszR));)
	{
		EXC_TYPE	hr=EOK;
		TCHAR		chDelim=_T('\n');

		/* find 1st non space */
		for ( ; IsSafeSpace(*lpszR) && (*lpszR != _T('\0')); lpszR++);
		if (_T('\0') == *lpszR)
			break;

		/* check for quoted display name */
		LPCTSTR	lpszTN=lpszR;
		if ((_T('\"') == *lpszTN) || (_T('\'') == *lpszTN))
		{
			/* find end of quote */
			if (NULL == (lpszTN=_tcschr((lpszTN+1), *lpszTN)))
				return ELITERAL;
			lpszTN++;	/* skip closing quote */
		}

		/* find end of current pair - allow either ';' or ',' as separators */
		LPCTSTR	lpszN=_tcschr(lpszTN, _T(','));
		if (NULL == lpszN)
			lpszN = _tcschr(lpszTN, _T(';'));
		if (NULL == lpszN)
			lpszN = strlast(lpszTN);
		else
			chDelim = *lpszN;

		UINT32	ulRLen=(lpszN - lpszR);
		TCHAR		tch=*lpszN;
		LPCTSTR	lpszName=NULL, lpszAddr=NULL;
		UINT32	ulNameLen=0UL, ulAddrLen=0UL;

		*((LPTSTR) lpszN) = _T('\0');
		hr = DecodeRFC822AddrPair(lpszR, lpszName, ulNameLen, lpszAddr, ulAddrLen);
		*((LPTSTR) lpszN) = tch;
		if (hr != EOK)
			return hr;

		BOOLEAN	fContEnum=TRUE;
		if ((hr=(*lpfnEcfn)(lpszHdrValue, lpszName, ulNameLen, lpszAddr, ulAddrLen, &fContEnum, pArg)) != EOK)
			return hr;
		if (!fContEnum)
			break;

		/* make sure parsing did not exceed current length */
		if ((ulNameLen+ulAddrLen) > ulRLen)
			return EOVERFLOW;
		if (chDelim == *(lpszR=lpszN))
			lpszR++;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

/*
 *	Returns a (virtual) numerical value which represents the importance/priority:
 *
 *		0 = normal
 *		> 0 high - the more positive the higher the importance/priority
 *		< 0 low - the more negative the lower the importance/priority
 */
EXC_TYPE XlateRFC822Importance (LPCTSTR lpszImportance, int *pImp)
{
	LPCTSTR	lsp=lpszImportance, tsp=NULL;

	if (NULL == pImp)
		return EPARAM;
	*pImp = 0;

	if (NULL == lpszImportance)
		return EOK;

	for ( ; IsSafeSpace(*lsp) && (*lsp != _T('\0')); lsp++);
	if (IsEmptyStr(lpszImportance))
		return EOK;

	for (tsp=lsp; (!IsSafeSpace(*lsp)) && (*lsp != _T('\0')); lsp++);
	switch(_totlower(*tsp))
	{
		case _T('l')	: *pImp = (-1); break;
		case _T('h')	: *pImp = 1; break;
		default			: /* do nothing */;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE XlateRFC822XPriority (LPCTSTR lpszXPriority, int *pImp)
{
	LPCTSTR	lsp=lpszXPriority, tsp=NULL;
	UINT32	ulXP=0;
	EXC_TYPE	exc=EOK;

	if (NULL == pImp)
		return EPARAM;
	*pImp = 0;

	if (NULL == lpszXPriority)
		return EOK;

	for ( ; IsSafeSpace(*lsp) && (*lsp != _T('\0')); lsp++);
	if (IsEmptyStr(lpszXPriority))
		return EOK;

	/* sometimes X-Priority header contains a remark, so we need to extract only the number in it */
	for (tsp=lsp; _istdigit(*lsp) && (*lsp != _T('\0')); lsp++);
	ulXP = argument_to_dword(tsp, (lsp - tsp), EXC_ARG(exc));
	*pImp = (int) (RFC822_NRXPRIORITY - ulXP);

	return exc;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE ValidateRFC822LocalMailPart (LPCTSTR lpszPart, const UINT32 ulPartLen)
{
	LPCTSTR	lsp=lpszPart;
	UINT32	ulAdx=0;

	if (IsEmptyStr(lpszPart) || (0 == ulPartLen))
		return ELOGNAMENEXIST;

	// check if valid separators in email
	for (ulAdx=0; ulAdx < ulPartLen; ulAdx++, lsp++)
	{
		TCHAR	ach=*lsp;

		if (_istdigit(ach) || _istalpha(ach))
			continue;

		/* forbidden separators in local part of email addresses (as per RFC821) */
		static const TCHAR szForbidEmailSeps[]={
			_T('<'), _T('>'),
			_T('('), _T(')'),
			_T('['), _T(']'),
			_T('\\'), /* _T('.'), */ _T(','), _T(';'), _T(':'), _T(' '),
			/* INET_DOMAIN_SEP, - see special handling further on */
			_T('\0')	// mark end
		};

		if (_tcschr(szForbidEmailSeps, ach) != NULL)
			return EUDFIO;

		/* do not allow consecutive '@' */
		if (INET_DOMAIN_SEP == ach)
		{
			if (INET_DOMAIN_SEP == *(lsp+1))
				return EFRAGMENTATION;
		}
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE ValidateRFC822MailDomainPart (LPCTSTR lpszDomain, const UINT32 ulDLen)
{
	LPCTSTR	lsp=lpszDomain;
	UINT32	ulDdx=0;

	if (IsEmptyStr(lpszDomain) || (0 == ulDLen))
		return ELOGNAMENEXIST;

	// make sure domain contains at least 2 components
	if (NULL == _tcsnchr(lpszDomain, _T('.'), ulDLen))
		return EUDFFORMAT;

	// make sure domain does not end in period
	if (_T('.') == lpszDomain[ulDLen-1])
		return ELOGNAMESYNTAX;

	for (ulDdx=0; ulDdx < ulDLen; ulDdx++, lsp++)
	{
		TCHAR	dch=*lsp;

		if (_istdigit(dch) || _istalpha(dch))
			continue;

		if ((dch != _T('.')) && (dch != _T('-')) && (dch != _T('_')))
			return EHOSTID;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE ValidateRFC822EmailAddr (LPCTSTR lpszAddr, const UINT32 ulAddrLen)
{
	if (IsEmptyStr(lpszAddr) || (0 == ulAddrLen))
		return EEMPTYENTRY;

	// allow "<>" as special case
	if (EMAIL_PATH_SDELIM == *lpszAddr)
	{
		if ((ulAddrLen != 2) || (lpszAddr[1] != EMAIL_PATH_EDELIM))
			return EPATHNAMESYNTAX;

		return EOK;
	}

	// make sure address contains domain name
	LPCTSTR lpszDomain=_tcsnrchr(lpszAddr, INET_DOMAIN_SEP, ulAddrLen);
	if (IsEmptyStr(lpszDomain))
		return ESEPARATOR;

	UINT32	ulALen=(lpszDomain - lpszAddr), ulDLen=(ulAddrLen - ulALen - 1UL);
	EXC_TYPE	exc=ValidateRFC822LocalMailPart(lpszAddr, ulALen);
	if (exc != EOK)
		return exc;

	if ((exc=ValidateRFC822MailDomainPart(lpszDomain+1, ulDLen)) != EOK)
		return exc;

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE EnumHeaderDataPropsList (LPCTSTR						lpszPropsList,	// may be empty/NULL
											 const TCHAR				chListSep,
											 RFC822PROPSLISTENUMCFN	lpfnEcfn,
											 LPVOID						pArg)
{
	if ((NULL == lpfnEcfn) || (_T('\0') == chListSep))
		return EBADADDR;

	if (IsEmptyStr(lpszPropsList))
		return EOK;

	for (LPCTSTR	lpszProp=lpszPropsList; *lpszProp != _T('\0'); )
	{
		// skip any preceding white spave
		for ( ; IsSafeSpace(*lpszProp) && (*lpszProp != _T('\0')); lpszProp++);
		if (_T('\0') == *lpszProp)
			break;

		// find end of property name
		LPCTSTR	lpszPropVal=lpszProp;
		for ( ; (!IsSafeSpace(*lpszPropVal)) &&
				  (*lpszPropVal != chListSep) &&
				  (*lpszPropVal != RFC822_KEYWORD_VALUE_DELIM) &&
				  (*lpszPropVal != _T('\0')); lpszPropVal++);

		UINT32	ulPropLen=(lpszPropVal - lpszProp);
		if (0 == ulPropLen)
			break;

		LPCTSTR			lpszPropEnd=lpszPropVal;
		const UINT32	ulNameLen=(lpszPropEnd - lpszProp);

		// make sure we stop because of a "correct" delimiter
		for ( ; (*lpszPropVal != chListSep) &&
				  (*lpszPropVal != RFC822_KEYWORD_VALUE_DELIM) &&
				  (*lpszPropVal != _T('\0')); lpszPropVal++);

		EXC_TYPE	exc=EOK;
		LPCTSTR	lpszValEnd=lpszPropVal;
		BOOLEAN	fContEnum=TRUE;

		if (RFC822_KEYWORD_VALUE_DELIM == *lpszPropVal)
		{
			lpszPropVal++;

			TCHAR		chVDelim=*lpszPropVal;
			UINT32	ulValLen=0;

			if (_T('\"') == chVDelim)
			{
				lpszPropVal++;

				// find terminating quote
				for (lpszValEnd=lpszPropVal; *lpszValEnd != chVDelim; lpszValEnd++)
					if (_T('\0') == *lpszValEnd)	// make sure not ending before end of quote
					{
						exc = EUDFFORMAT;
						break;
					}

				ulValLen = (lpszValEnd - lpszPropVal);
				lpszValEnd++;	// skip terminating quote
			}
			else	// non-quoted value
			{
				for ( ;
						(*lpszValEnd != chListSep) &&
						(!IsSafeSpace(*lpszValEnd)) &&
						(*lpszValEnd != _T('\0'));
					lpszValEnd++);

				ulValLen = (lpszValEnd - lpszPropVal);
			}

			if (EOK == exc)
			{
				exc = (*lpfnEcfn)(lpszProp, ulNameLen, lpszPropVal, ulValLen, pArg, &fContEnum);
			}
		}
		else	// singleton property
		{
			exc = (*lpfnEcfn)(lpszProp, ulNameLen, NULL, 0, pArg, &fContEnum);
		}

		if (exc != EOK)
			return exc;

		if (!fContEnum)
			break;

		for (lpszProp=lpszValEnd; *lpszProp != chListSep; lpszProp++)
			if (_T('\0') == *lpszProp)
				return EOK;

		lpszProp++;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

static EXC_TYPE cntCfn (LPCTSTR			lpszPropName,
								const UINT32	ulNameLen,
								LPCTSTR			lpszPropVal,	/* may be NULL/empty */
								const UINT32	ulValLen,	/* valid data in value string (may be 0) */
								LPVOID			pArg,
								BOOLEAN			*pfContEnum)
{
	if (NULL == pArg)
		return ECONTEXT;

	(*((UINT32 *) pArg))++;
	return EOK;
}

EXC_TYPE CountHeaderDataPropsList (LPCTSTR		lpszPropsList, /* may be empty/NULL */
											  const TCHAR	chListSep,
											  UINT32			*pulCount)
{
	if (NULL == pulCount)
		return EPARAM;
	*pulCount = 0;

	return EnumHeaderDataPropsList(lpszPropsList, chListSep, cntCfn, (LPVOID) pulCount);
}

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
static EXC_TYPE pplCfn (LPCTSTR			lpszPropName,
								const UINT32	ulNameLen,
								LPCTSTR			lpszPropVal,	/* may be NULL/empty */
								const UINT32	ulValLen,	/* valid data in value string (may be 0) */
								LPVOID			pArg,
								BOOLEAN			*pfContEnum)
{
	if (NULL == pArg)
		return ECONTEXT;

	CStr2StrMapper&	propSet=*((CStr2StrMapper *) pArg);

	if (0 == ulValLen)
		return propSet.AddKey(lpszPropName, ulNameLen);
	else
		return propSet.AddKey(lpszPropName, ulNameLen, lpszPropVal, ulValLen);
}

// ';' separated list of disposition properties
// Note: does not clear the properties set prior to parsing
EXC_TYPE ParseHeaderDataPropsList (LPCTSTR			lpszPropsList,	// may be empty/NULL
											  const TCHAR		chListSep,
											  CStr2StrMapper&	propSet)
{
	return EnumHeaderDataPropsList(lpszPropsList, chListSep, pplCfn, (LPVOID) &propSet);
}

EXC_TYPE ParseRFC822ContentTypeInfo (LPCTSTR				lpszCTypeInfo,
												 LPCTSTR&			lpszType,
												 UINT32&				ulTypeLen,
												 LPCTSTR&			lpszSubType,
												 UINT32&				ulSubTypeLen,
												 CStr2StrMapper&	propSet)
{
	EXC_TYPE	exc=rfc822ExtractContentTypePtrs(lpszCTypeInfo, &lpszType, &ulTypeLen, &lpszSubType, &ulSubTypeLen);
	if (exc != EOK)
		return exc;

	// find start of attributes (if any) by skipping spaces up to end of list or ';' sign
	LPCTSTR lpszCurPos=(lpszSubType + ulSubTypeLen);
	for (; _istspace(*lpszCurPos) && (*lpszCurPos != RFC822_LIST_DELIM) && (*lpszCurPos != _T('\0')); lpszCurPos++);
	// if no more data, then stop
	if (_T('\0') == *lpszCurPos)
		return EOK;

	// start one place after in order to skip the ';'
	if ((exc=ParseRFC822PropsList((lpszCurPos+1), propSet)) != EOK)
		return exc;

	return EOK;
}
#endif	/* of ifdef __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
EXC_TYPE AddHdrDataPropListPair (IStrlBuilder&	istr,
											const UINT32	ulPropIndex,
											const TCHAR		chListSep,
											LPCTSTR			lpszPropName,
											LPCTSTR			lpszPropVal)
{
	EXC_TYPE	exc=EOK;

	if (IsEmptyStr(lpszPropName))
		return EMULTIHOP;

	// if not 1st member then add list delimiter
	if (ulPropIndex != 0)
	{
		if ((exc=istr.AddChar(chListSep)) != EOK)
			return exc;
	}

	if ((exc=istr.AddStr(lpszPropName)) != EOK)
		return exc;

	// property value may be empty
	if (IsEmptyStr(lpszPropVal))
		return EOK;

	if ((exc=istr.AddChar(RFC822_KEYWORD_VALUE_DELIM)) != EOK)
		return exc;

	// quote the value
	if ((exc=istr.AddChar(_T('\"'))) != EOK)
		return exc;
	if ((exc=istr.AddStr(lpszPropVal)) != EOK)
		return exc;
	if ((exc=istr.AddChar(_T('\"'))) != EOK)
		return exc;

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE BuildHdrDataPropsList (const CStr2StrMapper& propSet, const TCHAR chListSep, IStrlBuilder& plst)
{
	CStr2StrMapEnum	pse(propSet);
	LPCTSTR				lpszPropName=NULL, lpszPropVal=NULL;
	EXC_TYPE				exc=pse.GetFirst(lpszPropName, lpszPropVal);
	for (UINT32 ulPropIndex=0; (EOK == exc); exc=pse.GetNext(lpszPropName, lpszPropVal), ulPropIndex++)
		if ((exc=AddHdrDataPropListPair(plst, ulPropIndex, chListSep, lpszPropName, lpszPropVal)) != EOK)
			return exc;

	return EOK;
}

EXC_TYPE BuildHdrDataPropsList (const CStr2StrMapper&	propSet,
										  const TCHAR				chListSep,
										  LPTSTR						lpszPropsList,
										  const UINT32				ulMaxLen)
{
	if ((NULL == lpszPropsList) || (0 == ulMaxLen))
		return EBADBUFF;
	*lpszPropsList = _T('\0');

	CStrlBuilder	strb(lpszPropsList, ulMaxLen);
	return BuildHdrDataPropsList(propSet, chListSep, strb);
}

/*--------------------------------------------------------------------------*/

// builds a "flat" representation of the "Content-Type:" header - i.e. "type/sub-type" followed by properties (if any)
EXC_TYPE BuildRFC822ContentTypeInfo (LPCTSTR						lpszType,
												 LPCTSTR						lpszSubType,
												 const CStr2StrMapper&	propSet,
												 IStrlBuilder&				isctype)
{
	EXC_TYPE	exc=EOK;

	if (IsEmptyStr(lpszType) || IsEmptyStr(lpszSubType))
		return EEMPTYENTRY;

	if ((exc=isctype.AddStr(lpszType)) != EOK)
		return exc;
	if ((exc=isctype.AddChar(RFC822_MIMETAG_SEP)) != EOK)
		return exc;
	if ((exc=isctype.AddStr(lpszSubType)) != EOK)
		return exc;

	// if no more properties, then do nothing
	if (0 == propSet.GetItemsCount())
		return EOK;

	// prepare for upcoming properties
	if ((exc=isctype.AddChar(RFC822_LIST_DELIM)) != EOK)
		return exc;
	if ((exc=BuildRFC822PropsList(propSet, isctype)) != EOK)
		return exc;

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE ReadjustRFC822PropsList (const CStr2PtrMapper&	propSet, IStrlBuilder& plst)
{
	CStr2PtrMapEnum	pse(propSet);
	LPCTSTR				lpszPropName=NULL;
	LPVOID				lpPropVal=NULL;
	EXC_TYPE				exc=pse.GetFirst(lpszPropName, lpPropVal);
	for (UINT32 ulPropIndex=0; (EOK == exc); exc=pse.GetNext(lpszPropName, lpPropVal), ulPropIndex++)
		if ((exc=AddRFC822PropListPair(plst, ulPropIndex, lpszPropName, (LPCTSTR) lpPropVal)) != EOK)
			return exc;

	return EOK;
}

EXC_TYPE ReadjustRFC822PropsList (const CStr2PtrMapper&	propSet,
											 LPTSTR						lpszPropsList,
											 const UINT32				ulMaxLen)
{
	if ((NULL == lpszPropsList) || (0 == ulMaxLen))
		return EBADBUFF;
	*lpszPropsList = _T('\0');

	CStrlBuilder	strb(lpszPropsList, ulMaxLen);
	return ReadjustRFC822PropsList(propSet, strb);
}
#endif	/* of ifdef __cplusplus */

/*--------------------------------------------------------------------------*/
