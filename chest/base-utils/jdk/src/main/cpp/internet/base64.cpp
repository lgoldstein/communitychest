#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>

#include <sys/types.h>
#include <sys/stat.h>

#include <util/errors.h>
#include <util/string.h>
#include <util/tables.h>

#include <internet/base64.h>

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
extern
#endif
const char b64XferEncoding[]="base64";

/*---------------------------------------------------------------------------*/

UINT32 B64IOWrite (IOWRITECALLBACK lpfnWcfn, void *pFout, const char pBuf[], const UINT32 ulBufLen)
{
	UINT32		ulRemLen=ulBufLen;
	const char	*bp=pBuf;

	if (NULL == lpfnWcfn)
		return IOCFN_BAD_LEN;

	while (ulRemLen > 0)
	{
		UINT32	ulWLen=min(BASE64_MAX_LINE_LEN, ulRemLen);
		int		sCount=(*lpfnWcfn)(pFout, bp, ulWLen);

		if ((UINT32) sCount != ulWLen)
			return IOCFN_BAD_LEN;
		if ((sCount=(*lpfnWcfn)(pFout, "\r\n", 2)) != 2)
			return IOCFN_BAD_LEN;

		bp += ulWLen;
		ulRemLen -= ulWLen;
	}

	return ulBufLen;
}

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
inline
#else
static
#endif
EXC_TYPE b64BlockEncode (const UINT32 encVal, LPTSTR oBufP)
{
	oBufP[0] = base64_encode_tbl[(encVal >> 18) & BASE64_MASK_VALUE];
	oBufP[1] = base64_encode_tbl[(encVal >> 12) & BASE64_MASK_VALUE];
	oBufP[2] = base64_encode_tbl[(encVal >>  6) & BASE64_MASK_VALUE];
	oBufP[3] = base64_encode_tbl[ encVal        & BASE64_MASK_VALUE];

	return EOK;
}

/*---------------------------------------------------------------------------*/

/* Note: outMaxLen should be > (4/3) * inBufLen */
EXC_TYPE b64_encode_buf (const UINT8	inBuf[],
								 const UINT32	inBufLen,	/* bytes to encode */
								 UINT32			*inLen,		/* encoded bytes */
								 char				outBuf[],
								 const UINT32	outBufLen,	/* available space */
								 UINT32			*outLen)		/* used space */
{
	const UINT8 *iBufP=inBuf;
	char			*oBufP=outBuf;
	UINT32		oLen=0, iLen=0, iRemLen=inBufLen, oRemLen=outBufLen;

	if ((NULL == inBuf) || (NULL == inLen) ||
		 (NULL == outBuf) || (NULL == outLen))
		return EPARAM;

	/* we work as long we have enough available input and output space */
	for ( ;	((oRemLen >= BASE64_OUTPUT_BLOCK_LEN) &&
			 (iRemLen >= BASE64_INPUT_BLOCK_LEN) &&
			 (oLen < outBufLen) && (iLen < inBufLen));
		iBufP += BASE64_INPUT_BLOCK_LEN,
		iLen += BASE64_INPUT_BLOCK_LEN,
		iRemLen -= BASE64_INPUT_BLOCK_LEN,
		oBufP += BASE64_OUTPUT_BLOCK_LEN,
		oLen += BASE64_OUTPUT_BLOCK_LEN,
		oRemLen -= BASE64_OUTPUT_BLOCK_LEN)
	{
		UINT32	encVal=(((UINT32) iBufP[0]) << 16) |
							 (((UINT32) iBufP[1]) <<  8) |
							  ((UINT32) iBufP[2]);
		EXC_TYPE	exc=b64BlockEncode(encVal, oBufP);
		if (exc != EOK)
			return exc;
	}

	if ((*outLen=oLen) < outBufLen)
		outBuf[oLen] = '\0';

	*inLen = iLen;
	return EOK;
}

/*---------------------------------------------------------------------------*/

/* build the last block encoding */
EXC_TYPE b64_encode_finish (const UINT8	inRemBuf[],	/* bytes left un-encoded */
									 const UINT32	inRemLen,	/* number of bytes left */
									 char				outBuf[],
									 const UINT32	outRemLen,	/* should be at least 4 */
									 UINT32			*outLen)
{
	EXC_TYPE		exc=EOK;
	const UINT8 *iBufP=NULL;
	char			*oBufP=NULL;
	UINT32		iLen=0, iRemLen=0, encVal=0, i;

	if ((exc=b64_encode_buf(inRemBuf, inRemLen, &iLen, outBuf, outRemLen, outLen)) != EOK)
		return exc;

	/* encode remainder - there are 3 possibilities:
	 *
	 * (a) iRemLen == 0 - i.e. entire buffer has been encoded since the length
	 *			was an integral multiple of 3. In this case there is nothing to be
	 *			done (no padding required).
	 *
	 * (b) iRemLen == 1 - we add 2 encoded characters and 2 padding characters
	 *
	 * (c) iRemLen == 2 - we add 3 encoded characters and 1 padding character
	 */

	if ((iRemLen=(inRemLen - iLen)) >= BASE64_INPUT_BLOCK_LEN)
		return EOVERFLOW;

	if (0 == iRemLen)
		return EOK;

	oBufP = (outBuf + (*outLen));
	iBufP = (inRemBuf + iLen);

	/* create a 24-bit value by adding padded zero(s) on the right */
	switch(iRemLen)
	{
		case 1 :
			encVal = (((UINT32) iBufP[0]) << 16);
			break;

		case 2 :
			encVal |= (((UINT32) iBufP[0]) << 16);
			encVal |= (((UINT32) iBufP[1]) << 8);
			break;

		/*		Since the input is encoded in blocks of 3 octets, the remainder
		 * can only be between 0-2 (and we have handled 0).
		 */
		default :
			return ESTATE;
	}
	
	if ((exc=b64BlockEncode(encVal, oBufP)) != EOK)
		return exc;

	/* 
	 * pad the last output block as required by RFC2045 
	 *
	 *	Note: encoding of "iRemLen" bytes takes (iRemLen+1) characters
	 */
	for (i = (iRemLen+1); i < BASE64_OUTPUT_BLOCK_LEN; i++)
		oBufP[i] = (UINT8) BASE64_PAD_CHAR;

	/* we are adding an output block anyway */
	if ((*outLen += BASE64_OUTPUT_BLOCK_LEN) < outRemLen)
		outBuf[*outLen] = '\0';

	return EOK;
}

/*---------------------------------------------------------------------------*/

/*
 *		Outputs the buffer in lines of 76 characters. If the output buffer
 * len is not a multiple of 76 characters, then the remaining ones are NOT
 * flushed. The actual number of output characters is returned in "outLen".
 */

static EXC_TYPE b64_output_buf (void				*pFout,
										  IOWRITECALLBACK	lpfnWcfn,
										  const char		outBuf[],
										  const UINT32		outBufLen,
										  UINT32				*outLen)
{
	const char		*oBufP=outBuf;
	UINT32			oRemLen=outBufLen;

	if ((NULL == lpfnWcfn) || (NULL == outBuf) || (NULL == outLen))
		return EPARAM;

	for (*outLen = 0;
		  (*outLen < outBufLen) && (oRemLen >= BASE64_MAX_LINE_LEN);
		  *outLen += BASE64_MAX_LINE_LEN, oBufP += BASE64_MAX_LINE_LEN, oRemLen -= BASE64_MAX_LINE_LEN)
	{
		const char	*tsp=(oBufP + BASE64_MAX_LINE_LEN), tch=*tsp;
		UINT32		ulWriteLen=0;

		*((char *) tsp) = '\0';	/* replace original with EOS */
		ulWriteLen = B64IOWrite(lpfnWcfn, pFout, oBufP, BASE64_MAX_LINE_LEN);
		*((char *) tsp) = tch;	/* restore original char */

		if (ulWriteLen != BASE64_MAX_LINE_LEN)
			return EIOHARD;

	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

#define OREM_LEN (BASE64_MAX_LINE_LEN+BASE64_OUTPUT_BLOCK_LEN)

typedef struct {
		UINT32				iLen;
		UINT32				oLen;
		IOWRITECALLBACK	lpfnWcfn;
		void					*pFout;
		char					oRem[OREM_LEN+BASE64_OUTPUT_BLOCK_LEN];
		UINT8					iRem[BASE64_INPUT_BLOCK_LEN];
} B64ENCSESS;

/*---------------------------------------------------------------------------*/

typedef struct {
	IOWRITECALLBACK	lpfnWcfn;
	void					*pFout;
	UINT8					*pBuf;
	UINT32				iMaxLen;
	UINT32				iLen;
	UINT32				oLen;
	char					oRem[BASE64_OUTPUT_BLOCK_LEN+2];
} B64DECSESS;

typedef union {
	B64ENCSESS	encSess;
	B64DECSESS	decSess;
} B64SESS;

/*---------------------------------------------------------------------------*/

/*	Opens and initializes a base64 encode session handle (NULL if error) */
LPB64ESESSION b64sess_create (void)
{
	 B64SESS	*pSess=NULL;

#ifdef __cplusplus
	 pSess = (B64SESS *) new BYTE[sizeof(B64SESS)];
#else
	 pSess = (B64SESS *) malloc(sizeof(B64SESS));
#endif

	if (NULL == pSess)
		return (LPB64ESESSION) NULL;

	memset(pSess, 0, (sizeof *pSess));
	return (LPB64ESESSION) pSess;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE b64sess_delete (LPB64ESESSION lpSess)
{
	B64SESS	*pSess=(B64SESS *) lpSess;

	if (NULL == lpSess)
		return EPARAM;

#ifdef __cplusplus
	LPBYTE	pBuf=(LPBYTE) pSess;
	delete [] pBuf;
#else
	free((void *) pSess);
#endif

	return EOK;
}

/*---------------------------------------------------------------------------*/

/*	Initialize and prepare for a new encoding session */
EXC_TYPE b64sess_encode_start (LPB64ESESSION		lpSess,
										 IOWRITECALLBACK	lpfnWcfn,
										 void					*pFout)
{
	B64ENCSESS	*pSess=(B64ENCSESS *) lpSess;

	if ((NULL == lpSess) || (NULL == lpfnWcfn))
		return EPARAM;

	memset(pSess, 0, (sizeof *pSess));
	pSess->lpfnWcfn = lpfnWcfn;
	pSess->pFout = pFout;

	return EOK;
}

/*---------------------------------------------------------------------------*/

/*	Process a new buffer for this session */
EXC_TYPE b64sess_encode_process (LPB64ESESSION	lpSess,
											const UINT8		pBuf[],
											const UINT32	ulBufLen)
{
	EXC_TYPE		exc=EOK;
	B64ENCSESS	*pSess=(B64ENCSESS *) lpSess;
	UINT32		iLen=0;
	const UINT8	*iBufP=pBuf;
	char			*oBufP=NULL;

	if ((NULL == lpSess) || (NULL == pBuf))
		return EPARAM;
	if (NULL == pSess->lpfnWcfn)
		return ESTATE;
	if (0 == ulBufLen)
		return EOK;

	iBufP = pBuf;
	oBufP = &(pSess->oRem[pSess->oLen]);

	/*		If any "leftovers" from previous encoding, complete the input block
	 * and encode it.
	 */

	if (pSess->iLen > 0)
	{
		UINT32	rLen=0, wLen=0;

		for (iLen = 0;
			  (iLen < ulBufLen) && (pSess->iLen < BASE64_INPUT_BLOCK_LEN);
			  iBufP++, iLen++, (pSess->iLen++))
			pSess->iRem[pSess->iLen] = *iBufP;

		/* not enough to complete the block */
		if (pSess->iLen < BASE64_INPUT_BLOCK_LEN)
			return EOK;

		if ((exc=b64_encode_buf(pSess->iRem,pSess->iLen,&rLen,
										oBufP,(OREM_LEN - pSess->oLen),&wLen)) != EOK)
			return exc;

		/*		We encode exactly ONE input block, so the output should be extacly
		 * ONE output block.
		 */
		if ((wLen != BASE64_OUTPUT_BLOCK_LEN) ||
			 (rLen != BASE64_INPUT_BLOCK_LEN))
			return EUNKNOWNEXIT;

		pSess->iLen -= rLen;
		if (pSess->iLen != 0)
			return ESTATE;
		pSess->oLen += wLen;
		oBufP += wLen;
	}

	while (iLen < ulBufLen)
	{
		UINT32 eLen=0, oSize=0, idx=0;

		if ((exc=b64_encode_buf(iBufP, (ulBufLen - iLen), &eLen,
										oBufP, (OREM_LEN - pSess->oLen), &oSize))!=EOK)
			return exc;

		/* if nothing encoded then assume not enough data left */
		if (0 == eLen)
			break;

		/* At this stage the following conditions hold:
		 *
		 * -	the octets between "eLen" and "iLen" are not encoded and should be
		 *		transferred to next buffer start.
		 *
		 * - the output buffer contains "oSize" more characters.
		 */

		iLen += eLen;
		iBufP += eLen;

		oBufP += oSize;
		*oBufP = '\0';
		pSess->oLen += oSize;

		oSize = 0;
		if ((exc=b64_output_buf(pSess->pFout, pSess->lpfnWcfn,
										pSess->oRem, pSess->oLen, &oSize)) != EOK)
			return exc;

		/* move unwritten characters to next iteration */
		for (idx = 0, oBufP = pSess->oRem; oSize < pSess->oLen;
			  idx++, oSize++, oBufP++)
			*oBufP = pSess->oRem[oSize];
		*oBufP = '\0';
		pSess->oLen = idx;
	}

	/* at this stage, less than a full input block should remain */
	if ((ulBufLen - iLen) >= BASE64_INPUT_BLOCK_LEN)
		return EOVERFLOW;

	/* preserve "leftovers" (if any) */
	for (pSess->iLen = 0; iLen < ulBufLen; iLen++, (pSess->iLen)++, iBufP++)
		pSess->iRem[pSess->iLen] = *iBufP;

	return EOK;
}

/*---------------------------------------------------------------------------*/

/*		Finish whatever "leftovers" there are (Note: callback function for
 * writing may still be called.
 */
EXC_TYPE b64sess_encode_finish (LPB64ESESSION lpSess)
{
	EXC_TYPE	exc=EOK;
	B64ENCSESS	*pSess=(B64ENCSESS *) lpSess;
	UINT32	oSize=0;

	if (NULL == lpSess)
		return EPARAM;

	if (NULL == pSess->lpfnWcfn)
		return ESTATE;

	if (pSess->iLen > 0)
	{
		if ((exc=b64_encode_finish(pSess->iRem, pSess->iLen,
											&(pSess->oRem[pSess->oLen]),
											(OREM_LEN - pSess->oLen),
											&oSize)) != EOK)
			return exc;

		pSess->oLen += oSize;
		pSess->oRem[pSess->oLen] = '\0';
		pSess->iLen = 0;
	}

	if ((exc=b64_output_buf(pSess->pFout, pSess->lpfnWcfn,
									pSess->oRem, pSess->oLen, &oSize)) != EOK)
		return exc;

	/* any output leftovers ? */
	if (oSize < pSess->oLen)
	{
		const char *lp=&(pSess->oRem[oSize]);
		if (B64IOWrite(pSess->lpfnWcfn, pSess->pFout, lp, (pSess->oLen - oSize)) != (pSess->oLen - oSize))
			return EIOHARD;
	}

	memset(pSess, 0, (sizeof *pSess));
	return EOK;
}

/*---------------------------------------------------------------------------*/

/*		Close and delete the session ("b64sess_encode_finish" is called first !!)
 * This routine invalidates the session handle.
 */
EXC_TYPE b64sess_encode_end (LPB64ESESSION lpSess)
{
	EXC_TYPE	st=EOK;
	B64ENCSESS	*pSess=(B64ENCSESS *) lpSess;

	if (NULL == lpSess)
		return EPARAM;

	if ((pSess->iLen > 0) || (pSess->oLen > 0))
		st = b64sess_encode_finish(lpSess);

	return st;
}

/*---------------------------------------------------------------------------*/

/* encodes the contents of the "fin" stream in base64 and places the output in
 * the "fout" stream. The routine allocates 2 working buffers of "blkSize" (the
 * output buffer is actually ~1.33 * blkSize), which are freed once routine is
 * completed. The I/O is performed via the supplied  callback functions.
 */

EXC_TYPE b64_encode_stream (IOREADCALLBACK	lpfnRcfn,
									 void					*pFin,
									 IOWRITECALLBACK	lpfnWcfn,
									 void					*pFout,
									 const UINT32		iSugBlkSize)
{
	UINT8		*iBuf=NULL, *iBufP=NULL;
	char		*oBuf=NULL, *oBufP=NULL;
	UINT32	iBlkSize=((iSugBlkSize == 0) ? BASE64_MIN_IBLKSIZE : iSugBlkSize);
	UINT32	oBlkSize=BASE64_MAX_OUTBUF_LEN(iBlkSize);
	UINT32	iLen=0, oLen=0, oSize=0;
	EXC_TYPE	st=EFATALEXIT;
	BOOLEAN	fContinue=TRUE;

	if ((NULL == lpfnRcfn) || (NULL == lpfnWcfn))
		return EPARAM;

#ifdef __cplusplus
	iBuf = new UINT8[iBlkSize+sizeof(UINT32)];
	oBuf = new char[oBlkSize+sizeof(UINT32)];
#else
	iBuf = (UINT8 *) malloc(iBlkSize+sizeof(UINT32));
	oBuf = (char *) malloc(oBlkSize+sizeof(UINT32));
#endif

	if ((NULL == iBuf) || (NULL == oBuf))
		goto Quit;

	iBufP = iBuf;
	oBufP = oBuf;

	do
	{
		UINT32 eLen=0, idx, iSize=(*lpfnRcfn)(pFin, iBufP, (iBlkSize - iLen));

		/* the read size has to take into account possible leftovers */

		if (IOCFN_BAD_LEN == iSize)
		{
			st = EIOHARD;
			goto Quit;
		}

		if (iSize < (iBlkSize - iLen))
			fContinue = FALSE;

		iLen += iSize;

		if ((st=b64_encode_buf(iBuf, iLen, &eLen, oBufP, (oBlkSize - oLen), &oSize)) != EOK)
			goto Quit;

		/* At this stage the following conditions hold:
		 *
		 * -	the octets between "eLen" and "iLen" are not encoded and should be
		 *		transferred to next buffer start.
		 *
		 * - the output buffer contains "oSize" more characters.
		 */

		/* prepare for next input - move relevant bytes */

		iLen -= eLen;	/* number of bytes to copy */
		for (idx = 0, iBufP = (iBuf + eLen); idx < iLen; idx++, iBufP++)
			iBuf[idx] = (*iBufP);
		iBufP = (iBuf + iLen);

		oLen += oSize;
		oBuf[oLen] = '\0';
		if ((st=b64_output_buf(pFout, lpfnWcfn, oBuf, oLen, &oSize)) != EOK)
			goto Quit;

		/* move non-output characters (oLen - oSize) to beginning of buffer */
		oLen -= oSize;
		for (idx = 0, oBufP = (oBuf + oSize); idx < oLen ; idx++, oBufP++)
			oBuf[idx] = (*oBufP);
		oBufP = (oBuf + oLen);

		if (0 == iSize)
			break;
	} while (fContinue);

	if ((st=b64_encode_finish(iBuf, iLen, oBufP, (oBlkSize - oLen), &oSize)) != EOK)
		goto Quit;

	oLen += oSize;
	oBuf[oLen] = '\0';
	if ((st=b64_output_buf(pFout, lpfnWcfn, oBuf, oLen, &oSize)) != EOK)
		goto Quit;

	/* if "leftover" data still in buffer then flush it */
	if (oSize < oLen)
	{
		UINT32	oDiff=(oLen - oSize);
		char		*pRem=(oBuf + oSize);
		UINT32	wLen=B64IOWrite(lpfnWcfn, pFout, pRem, oDiff);

		if (wLen != oDiff)
		{
			st = EIOHARD;
			goto Quit;
		}
	}

	/* this point is reached if everything OK */
	st = EOK;

Quit:	/* clean up locally allocated resources */
	if (iBuf != NULL)
	{
#ifdef __cplusplus
		delete [] iBuf;
#else
		free(iBuf);
#endif
		iBuf = NULL;
	}

	if (oBuf != NULL)
	{
#ifdef __cplusplus
		delete [] oBuf;
#else
		free(oBuf);
#endif
		oBuf = NULL;
	}

	return st;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE b64_encode_named_file (const char		pszFPath[],
										  IOWRITECALLBACK	lpfnWcfn,
										  void				*pFout,
										  const UINT32		iSugBlkSize)
{
	EXC_TYPE	exc=EOK;
	FILE		*fin=NULL;

	if (IsEmptyStr(pszFPath) || (NULL == lpfnWcfn))
		return EPARAM;

	if (NULL == (fin=fopen(pszFPath, "rb")))
		return EFNEXIST;
	exc = b64_encode_file(fin, lpfnWcfn, pFout, iSugBlkSize);
	fclose(fin);
	return exc;
}

/*---------------------------------------------------------------------------*/

/* returns:
 *		EOK - data processed
 *		EEOF - no more data
 *		EPREPOSITION - some illegal values were found (and ignored)
 *		E??? - other internal errors
 */
EXC_TYPE b64_decode_buf (const char		inBuf[],
								 const UINT32	inBufLen,
								 UINT32			*inLen,	/* actual decode size */
								 UINT8			outBuf[],
								 const UINT32	outBufLen,
								 UINT32			*outLen)
{
	const char	*pIn=inBuf;
	UINT8			*pOut=outBuf;
	UINT32		iLen=0, oLen=0;
	BOOLEAN		fPadChar=FALSE, fBadVal=FALSE;

	if ((NULL == inBuf) || (NULL == inLen) ||
		 (NULL == outBuf) || (NULL == outLen))
		return EPARAM;

	/* make sure we have enough space both in input and output buffers */
	while ((iLen < inBufLen) && (oLen < outBufLen))
	{
		UINT32	uIdx=0, iSize=iLen, oSize=oLen;
		UINT16	uVal=0, uBitsNum=0;

		for (uIdx=0; (uIdx < BASE64_OUTPUT_BLOCK_LEN) && (iLen < inBufLen); pIn++,iLen++)
		{
			char		tch=*pIn, nch='\0';
			UINT16	dVal=0;

			if (isspace(tch))
				continue;

			if (BASE64_PAD_CHAR == (tch))
			{
				fPadChar = TRUE;
				dVal = 0;
			}
			else
			{
				dVal = base64_decode_tbl[tch];

				if (FIsBadBase64Value(dVal))
				{
					fBadVal = TRUE;
					continue;
				}
			}

			uIdx++;	/* one more valid char found */

			/* if padding and no more bits in buffer, do nothing */
			if (fPadChar && (0 == uBitsNum))
				continue;

			if ((uIdx < BASE64_OUTPUT_BLOCK_LEN) && (iLen < (inBufLen-1)))
				nch = pIn[1];

			/*
			 *		Look ahead - if next char is padding then need only to complete
			 * missing bits in current value
			 */
			if (BASE64_PAD_CHAR == nch)
			{
				UINT16	uRemBits=(UINT16) (CHAR_BIT - uBitsNum);
				UINT16	uRemMask=(UINT16) ((1 << uRemBits) - 1);
				UINT16	uValBits=(UINT16) (BASE64_BITS_PER_OCTET - uRemBits);

				uVal <<= uRemBits;
				dVal >>= uValBits;
				uVal |= (dVal & uRemMask);
				uBitsNum += uRemBits;
			}
			else	/* next char not padding */
			{
				/* make room for next 6 bits */
				uVal <<= BASE64_BITS_PER_OCTET;
				uVal |= (dVal & BASE64_MASK_VALUE);
				uBitsNum += BASE64_BITS_PER_OCTET;
			}

			/* check if we have enough bits for an octet */
			if (uBitsNum >= CHAR_BIT)
			{
				if (oLen >= outBufLen)
				{
					uIdx--;	/* the valid char was not used */
					break;
				}

				/* the octet is built left-to-right so high bits are needed */
				uBitsNum -= CHAR_BIT;
				*pOut = (UINT8) ((uVal >> uBitsNum) & 0xFF);
				pOut++;
				oLen++;
			}
		}

		/* if unable to build a full decode block, stop and wait for next buf */
		if (uIdx < BASE64_OUTPUT_BLOCK_LEN)
		{
			/*		Restore values before the trial to decode a block - provided
			 * we started anything.
			 */
			if (uIdx > 0)
			{
				iLen = iSize;
				oLen = oSize;
			}

			break;
		}

		/* the remaining bits num must be 0 since we decoded a full block */
		if (uBitsNum != 0)
			return ESTATE;

		if (fPadChar)
			break;
	}

	*inLen = iLen;
	*outLen = oLen;

	/* NOTE !!! if this is the last buffer and there are bad chars, EPREPOSITION is NOT returned !!! */
	if (fPadChar)
		return EEOF;

	if (fBadVal)
		return EPREPOSITION;

	return EOK;
}

/*---------------------------------------------------------------------------*/

/*	Initialize and prepare for a new decoding session */
EXC_TYPE b64sess_decode_start (LPB64ESESSION		lpSess,
										 UINT8				pBuf[],
										 const UINT32		iMaxLen,
										 IOWRITECALLBACK	lpfnWcfn,
										 void					*pFout)
{
	B64DECSESS	*pSess=(B64DECSESS *) lpSess;

	if ((NULL == lpSess) || (NULL == lpfnWcfn) ||
		 (iMaxLen <= BASE64_MAX_LINE_LEN) || (NULL == pBuf))
		return EPARAM;

	memset(pSess, 0, (sizeof *pSess));

	pSess->lpfnWcfn = lpfnWcfn;
	pSess->pFout = pFout;
	pSess->pBuf = pBuf;
	pSess->iMaxLen = iMaxLen;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE b64sess_decode_flush (LPB64ESESSION	lpSess)
{
	B64DECSESS	*pSess=(B64DECSESS *) lpSess;
	UINT32		wLen=0;

	if (NULL == pSess)
		return EPARAM;
	if (NULL == pSess->lpfnWcfn)
		return ESTATE;
	if (0 == pSess->iLen)
		return EOK;

	wLen = (*(pSess->lpfnWcfn))(pSess->pFout,(char *) pSess->pBuf,pSess->iLen);
	if (wLen != pSess->iLen)
		return EIOHARD;

	pSess->iLen = 0;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE b64sess_decode_process (LPB64ESESSION	lpSess,
											const char		lpszBuf[],
											const UINT32	ulBufLen)
{
	EXC_TYPE		exc=EOK;
	B64DECSESS	*pSess=(B64DECSESS *) lpSess;
	const char	*lp=lpszBuf, *tp=NULL;
	UINT8			*bp=NULL;
	UINT32		iRemLen=ulBufLen, oLen=0, iLen=0;
	BOOLEAN		fBadChars=FALSE;

	if ((NULL == pSess) || ((NULL == lpszBuf) && (ulBufLen != 0)))
		return EPARAM;

	/* skip trailing white spaces */
	if (iRemLen > 0)
	{
		for (tp=(lpszBuf + (iRemLen - 1UL)); iRemLen > 0; iRemLen--, tp--)
			if (!isspace(*tp))
				break;
	}

	/* check if any remains from previous decode */
	if (pSess->oLen != 0)
	{
		UINT8		iBlk[BASE64_INPUT_BLOCK_LEN];
		BOOLEAN	fIsEOF=FALSE;

		/* complete to full base64 output block - provided no spaces */
		for ( ; (iRemLen > 0) && (pSess->oLen < BASE64_OUTPUT_BLOCK_LEN); lp++, iRemLen--)
		{
			if (!isspace(*lp))
			{
				pSess->oRem[pSess->oLen] = *lp;
				pSess->oLen++;
			}
		}
		pSess->oRem[pSess->oLen] = '\0';	/* for debug... */

		/* check if buffer had enough data to complete an output block */
		if (pSess->oLen < BASE64_OUTPUT_BLOCK_LEN)
			return EOK;

		exc = b64_decode_buf(pSess->oRem, BASE64_OUTPUT_BLOCK_LEN, &oLen,
									iBlk, BASE64_INPUT_BLOCK_LEN, &iLen);
		if (exc != EOK)
		{
			if (EPREPOSITION == exc)
				fBadChars = TRUE;
			else if (EEOF == exc)
				fIsEOF = TRUE;
			else
				return exc;
		}

		/* make sure entire block has been decoded */
		if (oLen != BASE64_OUTPUT_BLOCK_LEN)
			return ESTATE;

		/* check how much room in working buffer */
		oLen = (pSess->iMaxLen - pSess->iLen);
		bp = (pSess->pBuf + pSess->iLen);
		if (oLen < iLen)
		{
			UINT32	ulDiff=(iLen - oLen);

			memcpy(bp, iBlk, oLen);
			pSess->iLen += oLen;
			if ((exc=b64sess_decode_flush(pSess)) != EOK)
				return exc;

			memcpy(pSess->pBuf, &iBlk[oLen], ulDiff);
			pSess->iLen += ulDiff;
		}
		else	/* have enough room to accomodate "leftover" buffer */
		{
			memcpy(bp, iBlk, iLen);
			pSess->iLen += iLen;
		}

		pSess->oLen = 0;
		pSess->oRem[0] = '\0';	/* for debug... */

		if (fIsEOF)
			return EEOF;
	}

	/* at this point we know that there are no remaining "leftovers" */
	while (iRemLen > 0)
	{
		UINT32	bLen=(pSess->iMaxLen - pSess->iLen);

		bp = (pSess->pBuf + pSess->iLen);
		if ((exc=b64_decode_buf(lp, iRemLen, &oLen, bp, bLen, &iLen)) != EOK)
		{
			if (EPREPOSITION == exc)
			{
				// if nothing processed then assume everything is wrong
				if (0 == iLen)
					return exc;

				fBadChars = TRUE;
			}
			else
			{
				if (EEOF == exc)
					pSess->iLen += iLen;
				return exc;
			}
		}

		pSess->iLen += iLen;
		iRemLen -= oLen;
		lp += oLen;

		if ((0 == iLen) || (pSess->iLen >= pSess->iMaxLen))
		{
			if ((exc=b64sess_decode_flush(pSess)) != EOK)
				return exc;

			/* check if enough left for one more decode block - excluding spaces */
			for (tp=lp, pSess->oLen = 0, iLen = iRemLen; iLen > 0; iLen--, tp++)
			{
				if (isspace(*tp))
					continue;

				pSess->oRem[pSess->oLen] = *tp;
				pSess->oLen++;

				if (pSess->oLen >= BASE64_OUTPUT_BLOCK_LEN)
					break;
			}

			/* not enough for one block */
			if (pSess->oLen < BASE64_OUTPUT_BLOCK_LEN)
			{
				pSess->oRem[pSess->oLen] = '\0'; /* for debug */
				break;
			}

			pSess->oLen = 0;
		}
	}

	if (fBadChars)
		return EPREPOSITION;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE b64sess_decode_end (LPB64ESESSION	lpSess)
{
	EXC_TYPE		exc=EOK;
	B64DECSESS	*pSess=(B64DECSESS *) lpSess;

	if (NULL == pSess)
		return EPARAM;

	if (pSess->oLen != 0)
	{
		char		szBuf[BASE64_OUTPUT_BLOCK_LEN+2];
		UINT32	iLen=0;

		for (iLen = 0 ; iLen < BASE64_OUTPUT_BLOCK_LEN; iLen++)
			szBuf[iLen] = BASE64_PAD_CHAR;
		szBuf[BASE64_OUTPUT_BLOCK_LEN] = '\0';

		iLen = (BASE64_OUTPUT_BLOCK_LEN - pSess->oLen);

		// we expect an EOF !!!
		if ((exc=b64sess_decode_process(pSess, szBuf, iLen)) != EEOF)
			return ESTATE;
	}

	if ((exc=b64sess_decode_flush(pSess)) != EOK)
		return exc;

	memset(pSess, 0, (sizeof *pSess));
	return EOK;
}

/*---------------------------------------------------------------------------*/

#define MIME_BOUNDARY_DELIM	'-'

/*		Decodes the incoming stream into the output stream. If a MIME boundary is
 * supplied, then decoding stops once boundary is encountered. In this case,
 * the function returns an indication if this is the last boundary. At the same
 * time, the input stream should be positioned at the first char of the base64
 * encoding.
 *
 * Note: if boundary used, then upon return the input stream is positioned on
 *			first char of first line AFTER the boundary.
 *
 * If no boundary supplied, then entire input stream is decoded.
 */
EXC_TYPE b64_decode_stream (IOREADCALLBACK	lpfnRcfn,
									 void					*pFin,
									 IOWRITECALLBACK	lpfnWcfn,
									 void					*pFout,
									 const char			MIMEBoundary[],
									 const UINT32		iSugBlkSize,
									 BOOLEAN				*pfIsLast)
{
	char		*iBuf=NULL, *pIn=NULL, *pBound=NULL;
	UINT8		*oBuf=NULL;
	UINT32	iBlkSize=((iSugBlkSize == 0) ? BASE64_MIN_IBLKSIZE : iSugBlkSize);
	UINT32	oBlkSize=BASE64_MAX_INBUF_LEN(iBlkSize), iLen=0;
	EXC_TYPE	st=EFATALEXIT;
	size_t	sBoundLen=0;
	BOOLEAN	fContinue=TRUE, fBadChars=FALSE;

	if ((NULL == lpfnRcfn) || (NULL == lpfnWcfn) || (NULL == pfIsLast))
		return EPARAM;

	if (MIMEBoundary != NULL)
		*pfIsLast = FALSE;
	else	/* if no boundary, obviously this is the last decode */
		*pfIsLast = TRUE;

#ifdef __cplusplus
	iBuf = new char[iBlkSize + BASE64_OUTPUT_BLOCK_LEN];
	oBuf = new UINT8[oBlkSize + BASE64_INPUT_BLOCK_LEN];
#else
	iBuf = (char *) malloc(iBlkSize + BASE64_OUTPUT_BLOCK_LEN);
	oBuf = (UINT8 *) malloc(oBlkSize + BASE64_INPUT_BLOCK_LEN);
#endif	/* of ifdef __cplusplus */

	if ((NULL == iBuf) || (NULL == oBuf))
		goto Quit;
	pIn = iBuf;

	while (fContinue)
	{
		UINT32	iSize=0, oSize=0, wLen=0, rLen=0;

		for (iBuf[iBlkSize]='\0'; (iLen < iBlkSize); iLen += rLen,pIn += rLen)
		{
			rLen = sIOReadLine(lpfnRcfn,pFin,(UINT8 *) pIn,(iBlkSize - iLen));
			if (0 == rLen)
				continue;

			if (IOCFN_BAD_LEN == rLen)
			{
				fContinue = FALSE;
				break;
			}

			pIn[rLen] = '\0';	/* create NULL terminated string */

			/*		If found boundary start delimiter, then adjust the data len
			 * and signal end of processing (even if this is not a TRUE boundary
			 * we should stop - if not TRUE boundary we'll find out and return an
			 * error...).
			 */
			if ((pBound=strchr(pIn, MIME_BOUNDARY_DELIM)) != NULL)
			{
				sBoundLen = strlen(pBound);
				rLen -= sBoundLen;	/* subtract the length of the boundary part */
				pIn[rLen] = '\0';	/* create NULL terminated string */
				fContinue = FALSE;
				break;
			}

			/* no boundary - remove terminating '\n' (if any) */
			if ('\n' == pIn[rLen-1])
			{
				rLen--;
				pIn[rLen] = '\0';	/* create NULL terminated string */
			}
			if (0 == rLen)
				continue;

			/* no boundary - remove terminating '\n' (if any) */
			if ('\r' == pIn[rLen-1])
			{
				rLen--;
				pIn[rLen] = '\0';	/* create NULL terminated string */
			}

			if (0 == rLen)
				continue;
		}	/* end of while iLen < iBlkSize */

		if ((st=b64_decode_buf(iBuf,iLen,&iSize,oBuf,oBlkSize,&oSize)) != EOK)
		{
			if (EPREPOSITION == st)
				fBadChars = TRUE;
			else if (EEOF != st)
				goto Quit;
			else
				st = EOK;
		}

		if ((wLen=(*lpfnWcfn)(pFout, (const char *) oBuf, oSize)) != oSize)
		{
			st = EIOHARD;
			goto Quit;
		}

		/*		At this stage "iSize" out of "iLen" characters have been decoded,
		 * so we have to move the rest to the start of the buffer for next read
		 * (if any).
		 */

		if (fContinue)
		{
			for (pIn=(iBuf + iSize), wLen=0; iSize < iLen; iSize++, pIn++, wLen++)
				iBuf[wLen] = *pIn;
			iLen -= iSize;
			pIn = (iBuf + iLen);
		}
	}	/* end of while fContinue */

	/* check if we encountered a valid boundary delimiter */
	if (((MIMEBoundary != NULL) && (NULL == pBound)) ||
		 ((NULL == MIMEBoundary) && (pBound != NULL)))
	{
		st = ESTATE;
		goto Quit;
	}

	if (pBound != NULL) 
	{
		/* restore it - it has been replaced by '\0' when first encountered */
		*pBound = MIME_BOUNDARY_DELIM;

		/* check if we read entire boundary line */
		if ('\n' != pBound[sBoundLen-1])
		{
			/* move partial boundary to start of input buf to make room for rest */
			for (pIn = iBuf, iLen = 0; iLen < sBoundLen; iLen++, pIn++, pBound++)
				*pIn = *pBound;
			*pIn = '\0';
			pBound = iBuf;

			iLen = sIOReadLine(lpfnRcfn,pFin,(UINT8 *) pIn,(iBlkSize - iLen));
			if (IOCFN_BAD_LEN == iLen)
			{
				st = EIOHARD;
				goto Quit;
			}
			if ('\n' == pIn[iLen-1])
				pIn[iLen-1] = '\0';
		}
		else	/* we have full boundary */
			pBound[sBoundLen-1] = '\0';	/* replace terminating '\n' with NULL */

		/* boundary must start with "--" */
		if ((pBound[0] != MIME_BOUNDARY_DELIM) || (pBound[1] != MIME_BOUNDARY_DELIM))
		{
			st = ESTATE;
			goto Quit;
		}
		pBound += 2;

		/* check end of boundary for "last" markings */
		if (NULL == (pIn=strchr(pBound, '\0')))
		{
			st = EPREPOSITION;
			goto Quit;
		}

		pIn -= 2;
		if ((MIME_BOUNDARY_DELIM == pIn[0]) && (MIME_BOUNDARY_DELIM == pIn[1]))
		{
			*pfIsLast = TRUE;
			*pIn = '\0';	/* terminate boundary string */
		}

		/* we have found a boundary and it MUST match the one we expect */
		if (strcmp(pBound, MIMEBoundary) != 0)
		{
			st = EDATACHAIN;
			goto Quit;
		}
	}

	st = EOK;	/* this point is reached if everything OK */

Quit:
#ifdef __cplusplus
	if (iBuf != NULL)
		delete [] iBuf;
	if (oBuf != NULL)
		delete [] oBuf;
#else
	if (iBuf != NULL)
		free((void *) iBuf);
	if (oBuf != NULL)
		free((void *) oBuf);
#endif
	iBuf = NULL;
	oBuf = NULL;

	if (fBadChars && (EOK == st))
		st = EPREPOSITION;

	return st;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE b64_decode_to_named_file (IOREADCALLBACK	lpfnRcfn,
											  void				*pFin,
											  const char		pszFilePath[],
											  const char		MIMEBoundary[],
											  const UINT32		iBlkSize,
											  BOOLEAN			*pfIsLast)
{
	EXC_TYPE	exc=EOK;
	FILE		*fout=NULL;

	if (IsEmptyStr(pszFilePath) || (NULL == lpfnRcfn))
		return EPARAM;

	if (NULL == (fout=fopen(pszFilePath, "wb")))
		return EFNEXIST;
	exc = b64_decode_to_file(lpfnRcfn, pFin, fout, MIMEBoundary, iBlkSize, pfIsLast);
	fclose(fout);

	return exc;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE b64_decode_named_to_named_file (const char	pszInputFilePath[],
													  const char	pszOutputFilePath[],
													  const char	MIMEBoundary[],
													  const UINT32	iBlkSize,
													  BOOLEAN		*pfIsLast)
{
	EXC_TYPE	exc=EOK;
	FILE		*fin=NULL;

	if (IsEmptyStr(pszInputFilePath))
		return EPARAM;

	if (NULL == (fin=fopen(pszInputFilePath, "r")))
		return EFNEXIST;

	exc = b64_decode_file_to_named_file(fin, pszOutputFilePath, MIMEBoundary, iBlkSize, pfIsLast);
	fclose(fin);

	return exc;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE b64_calc_encode_size (const UINT32	ulFSize,
										 const BOOLEAN	fAddLinesSep,
										 UINT32			*pulB64Size)
{
	UINT32	ulB64Size=0, ulB64Blocks=0;

	if (NULL == pulB64Size)
		return EPARAM;
	*pulB64Size = 0;
			
	/* calculate expected size if we encode "pure" MIME (w/o CRLF) */
	ulB64Blocks = (ulFSize / BASE64_INPUT_BLOCK_LEN);
	if ((ulFSize % BASE64_INPUT_BLOCK_LEN) != 0)
		ulB64Blocks++;
	ulB64Size = (ulB64Blocks * BASE64_OUTPUT_BLOCK_LEN);

	/* calculate expected output size */
	if (fAddLinesSep)
	{
		UINT32	ulB64Lines=(ulB64Size / BASE64_MAX_LINE_LEN);
		UINT32	ulB64LastLine=(ulB64Size % BASE64_MAX_LINE_LEN);
	
		/* check if last line complete */
		if (ulB64LastLine != 0)
			ulB64Lines++;

		/* take into account CRLF at end of each line */
		ulB64Size += (ulB64Lines * 2UL);
	}

	*pulB64Size = ulB64Size;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE b64_calc_decode_size (const UINT32	ulEncSize,
										 const UINT32	ulLineLen,
										 UINT32			*pulBinSize)
{
	UINT32	ulB64Lines=0, ulB64FLL=(ulLineLen+2);
	UINT32	ulB64LSize=0, ulB64RemLen=0, ulB64EBlks=0;

	if ((0 == ulLineLen) || (NULL == pulBinSize))
		return EPARAM;

	*pulBinSize = 0;
	if (0 == ulEncSize)
		return EOK;

	/* take into account CRLF when calculating number of full lines */
	ulB64Lines = (ulEncSize / ulB64FLL);

	/* number of bytes taken by full lines */
	ulB64LSize = (ulB64Lines * ulB64FLL);

	/* remainder - non-full line */
	ulB64RemLen = ulEncSize - ulB64LSize;

	/* calculate "pure" B64 size - w/o the CRLF(s) */
	ulB64LSize -= (ulB64Lines * 2);
	ulB64LSize += ulB64RemLen;

	/* number of decode blocks */
	ulB64EBlks = (ulB64LSize / BASE64_OUTPUT_BLOCK_LEN);

	/* each encode block yields one decoded block */
	*pulBinSize = ulB64EBlks * BASE64_INPUT_BLOCK_LEN;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE b64_get_file_encode_size (const char		lpszFilePath[],
											  const BOOLEAN	fAddLinesSep,
											  UINT32				*pulB64Size)
{
	struct _stat	fst;

	if (IsEmptyStr(lpszFilePath) || (NULL == pulB64Size))
		return EPARAM;
	*pulB64Size = 0;

	memset(&fst, 0, (sizeof fst));
	if (0 != _stat(lpszFilePath, &fst))
		return EFNEXIST;

	return b64_calc_encode_size((UINT32) fst.st_size, fAddLinesSep, pulB64Size);
}

/*---------------------------------------------------------------------------*/
