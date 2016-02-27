#include <_types.h>
#include <util/errors.h>
#include <util/string.h>

#include <internet/base64.h>
#include <internet/qpenc.h>

/*---------------------------------------------------------------------------*/

/* if no encoding found then NULL(s) are returned */
EXC_TYPE extract_iso_encoding (const char	*lpszOrgStr,
										 const char	**lppszCharSet,
										 UINT32		*pulCSLen,
										 const char	**lppszEncType,
										 const char	**lppszEncTxt,
										 UINT32		*pulTxtLen)
{
	const char	*tsp=NULL, *lpszCS=NULL, *lpszEC=NULL, *lpszET=NULL;
	UINT32		ulCSL=0, ulTL=0;

	if ((NULL == lpszOrgStr) || (NULL == lppszCharSet) ||
		 (NULL == lppszEncType) || (NULL == pulCSLen) ||
		 (NULL == lppszEncTxt) || (NULL == pulTxtLen))
		return EPARAM;

	*lppszCharSet = NULL;
	*pulCSLen = 0;
	*lppszEncType = NULL;
	*lppszEncTxt = NULL;
	*pulTxtLen = 0;

	/* find encoding start - marked by "=?" */
	for (tsp=strchr(lpszOrgStr, ISOENCDELIM) ; tsp != NULL; tsp=strchr(tsp, ISOENCDELIM))
	{
		if ((lpszOrgStr != tsp) && (ISOSTRDELIM == *(tsp-1)))
			break;

		tsp++;
	}

	/* maybe no encoding found */
	if (NULL == tsp)
		return EOK;

	/* at this point we have a "=?" substring - charset spec follows it */
	tsp++;	/* skip '?' delimiter */
	lpszCS = tsp;	/* remember start of charset */

	/* find end of charset */
	for (tsp++; (*tsp != ISOENCDELIM) && (*tsp != '\0'); tsp++);
	if (*tsp != ISOENCDELIM)
		return EOK;
	ulCSL = (tsp - lpszCS);

	tsp++; 	/* skip '?' delimiter */
	lpszEC = tsp;	/* remember encoding type char */
	tsp++;
	if (*tsp != ISOENCDELIM)
		return EOK;
	tsp++; 	/* skip '?' delimiter */
	lpszET = tsp;	/* remember start of encoded text */

	/* look for ending delimiter */
	if (NULL == (tsp=strchr(lpszET, ISOENCDELIM)))
		return EOK;
	ulTL = (tsp - lpszET);

	/* make sure this is the ending delimiter */
	if (ISOSTRDELIM != *(tsp+1))
		return EOK;

	/* make sure this is a valid encoding char */
	if ((toupper(*lpszEC) != ISOQPENCDELIM) &&
		(toupper(*lpszEC) != ISOB64ENCDELIM))
		return ETYPE;

	/* return results */
	*lppszCharSet = lpszCS;
	*pulCSLen = ulCSL;
	*lppszEncType = lpszEC;
	*lppszEncTxt = lpszET;
	*pulTxtLen = ulTL;

	return EOK;
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE find_iso_encoding (const char	*lpszOrgStr,
											  const char	**lppszSrc,
											  UINT32			*pulSkipLen,
											  const char	**lppszEncType,
											  const char	**lppszISOEnc,
											  UINT32			*pulEncLen)
{
	EXC_TYPE		exc=EOK;
	const char	*lpszSrc=lpszOrgStr;
	const char	*lpszCS=NULL;
	UINT32		ulCSL=0;

	if ((NULL == lpszOrgStr) || (NULL == lppszSrc) ||
		 (NULL == lppszEncType) || (NULL == pulSkipLen) ||
		 (NULL == lppszISOEnc) || (NULL == pulEncLen))
		return EPARAM;

	*lppszSrc = lpszOrgStr;
	*pulSkipLen = 0;

	exc = extract_iso_encoding(lpszOrgStr, &lpszCS, &ulCSL, lppszEncType, lppszISOEnc, pulEncLen);
	if (exc != EOK)
		return EOK;

	/* if no charset/encoding then skip entire string */
	if (NULL == lpszCS)
	{
		const char	*lpszE=strlast(lpszOrgStr);

		*lppszSrc = lpszOrgStr;
		*pulSkipLen = (lpszE - lpszSrc);
		return EOK;
	}

	*pulSkipLen = (lpszCS - lpszSrc - 2UL);
	*lppszSrc = ((*lppszISOEnc) + (*pulEncLen) + 2UL);

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE convert_ISOQP_string (const char lpszVal[], char lpszStr[], const UINT32 ulMaxSize)
{
	if ((NULL == lpszVal) || (NULL == lpszStr) || (0 == ulMaxSize))
		return EPARAM;

	char			*lpszDst=lpszStr;
	const char	*lpszSrc=lpszVal;
	UINT32		ulRemLen=ulMaxSize;

	// check subject for ISO quoted-printable characters so we can replace them
	for ( ; *lpszSrc != '\0'; )
	{
		const char	*lpszISOEnc=NULL, *lpszOrgSrc=lpszSrc, *lpszEncType=NULL;
		UINT32		ulSkipLen=0, ulEncLen=0, cLen=0;
		EXC_TYPE		hr=find_iso_encoding(lpszOrgSrc, &lpszSrc, &ulSkipLen, &lpszEncType, &lpszISOEnc, &ulEncLen);

		if ((EOK != hr) || (NULL == lpszISOEnc))
		{
			cLen = min(ulRemLen, strlen(lpszOrgSrc));
			lpszDst = strlncat(lpszDst, lpszOrgSrc, cLen);
			break;
		}

		if ((cLen=min(ulRemLen, ulSkipLen)) > 0)
		{
			lpszDst = strlncat(lpszDst, lpszOrgSrc, cLen);
			if (0 == (ulRemLen -= cLen))
				break;
		}

		switch(toupper(*lpszEncType))
		{
			case ISOQPENCDELIM	:
				cLen = quotedprintable2ascii(lpszISOEnc, ulEncLen, lpszDst, ulRemLen);
				break;

			case ISOB64ENCDELIM	:
				hr = b64_decode_buf(lpszISOEnc, ulEncLen, &ulSkipLen,
										  (UINT8 *) lpszDst, ulRemLen, &cLen);
				if ((hr != EOK) && (hr != EEOF))
					return hr;
				break;

			default					:
				return ETYPE;
		}
		if (cLen >= ulRemLen)
			break;

		lpszDst += cLen;
		if (0 == (ulRemLen -= cLen))
			break;
	}

	// make sure string is NULL terminated
	*lpszDst = '\0';
	*(lpszStr + (ulMaxSize - 1)) = '\0';
	return ERROR_SUCCESS;
}

/*---------------------------------------------------------------------------*/
