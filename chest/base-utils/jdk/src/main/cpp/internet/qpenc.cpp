#include <util/errors.h>
#include <internet/qpenc.h>
#include <futils/general.h>

#ifdef __cplusplus
#	define SZXTRN extern
#else
#	define SZXTRN
#endif

/*---------------------------------------------------------------------------*/

SZXTRN const char qpXferEncoding[]="quoted-printable";

/* TRUE if character can be transfered as-is without any encoding */
BOOLEAN fIsQPXferChar (const TCHAR c)
{
	if ((c >= (TCHAR) 0x20) && (c <= (TCHAR) 0x7e))
		return (c != QPDELIM);

	if ((_T('\t') == c) || (_T('\r') == c) || (_T('\n') == c))
		return TRUE;

	return FALSE;
}

/*---------------------------------------------------------------------------*/

/* Note: caller must make sure that enough room is available (including '\0') */
EXC_TYPE add_QP_encoding (const char cVal, char **lppQP)
{
	EXC_TYPE	exc=EOK;
	char		*lpszQP=NULL, tch='\0';

	if ((NULL == lppQP) || (NULL == (lpszQP=(*lppQP))))
		return EPARAM;

	lpszQP = strladdch(lpszQP, QPDELIM);

	tch = byte_to_hex_digit((BYTE) ((cVal >> 4) & 0x0F), EXC_ARG(exc));
	if (exc != EOK)
		return exc;
	lpszQP = strladdch(lpszQP, tch);

	tch = byte_to_hex_digit((BYTE) (cVal & 0x0F), EXC_ARG(exc));
	if (exc != EOK)
		return exc;
	lpszQP = strladdch(lpszQP, tch);

	*lppQP = lpszQP;
	return EOK;
}

/*---------------------------------------------------------------------------*/

/* returns value > "qp_len" if error, converted len otherwise.
 *
 * if "fConvAll" is false then only non-printable characters are converted
 */
size_t ascii2quotedprintable (const char		ascii_txt[],
										const size_t	ascii_len,
										char				qp_txt[],
										const size_t	qp_len,
										const BOOLEAN	fConvAll)
{
	const char	*lpszAscii=ascii_txt;
	size_t		aLen=0;
	char			*lpszQP=qp_txt;
	size_t		qLen=0;

	if ((NULL == ascii_txt) || (NULL == qp_txt))
		return (qp_len+1);

	for (*lpszQP = '\0' ; aLen < ascii_len; aLen++, lpszAscii++)
	{
		EXC_TYPE	exc=EOK;
		size_t	rLen=(qp_len-qLen);
		char		tch=(*lpszAscii);

		/* make sure we do not run out of ASCII data prematurely */
		if ('\0' == tch)
			return (qp_len+1);

		/* check if non-printable or full conversion requested */
		if (fConvAll || (!isprint(tch)))
		{
			/* make sure we can accomodate conversion string */
			if (rLen <= QPCHAR_DISPLAY_LENGTH)
				return (qp_len+1);

			if ((exc=add_QP_encoding(tch, &lpszQP)) != EOK)
				return (qp_len+1);
			qLen += QPCHAR_DISPLAY_LENGTH;
		}
		else	/* printable AND not full conversion requested */
		{
			/* make sure we can accomodate ASCII char */
			if (rLen <= 1)
				return (qp_len+1);

			lpszQP = strladdch(lpszQP, tch);
			qLen++;
		}
	}

	return qLen;
}

/*---------------------------------------------------------------------------*/

/* Note: assumes enough data in QP encoding */
EXC_TYPE decode_QP (const char **lppQP, char *lpch)
{
	const char	*lpszQP=NULL;
	char			tch='\0';

	if ((NULL == lppQP) || (NULL == lpch) || (NULL == (lpszQP=(*lppQP))))
		return EPARAM;

	tch = (*lpszQP);
	lpszQP++;

	if (QPDELIM == tch)
	{
		EXC_TYPE	exc=EOK;

		tch = (char) hex_argument_to_byte(lpszQP, MAX_BYTE_HEX_DISPLAY_LENGTH, EXC_ARG(exc));
		if (exc != EOK)
			return exc;

		lpszQP += MAX_BYTE_HEX_DISPLAY_LENGTH;
	}

	if ('\0' == tch)
		return ENODATA;

	*lpch = tch;
	*lppQP = lpszQP;
	return EOK;
}

/*---------------------------------------------------------------------------*/

/* returns value > "ascii_len" if error, converted len otherwise */
size_t quotedprintable2ascii (const char		qp_txt[],
										const size_t	qp_len,
										char				ascii_txt[],
										const size_t	ascii_len)
{
	char			*lpszAscii=ascii_txt;
	size_t		aLen=0;
	const char	*lpszQP=qp_txt;
	size_t		qLen=0;

	if ((NULL == ascii_txt) || (NULL == qp_txt))
		return (ascii_len+1);

	for (*lpszAscii = '\0'; qLen < qp_len; aLen++)
	{
		EXC_TYPE	exc=EOK;
		char		tch=(*lpszQP);

		/* make sure data does not end prematurely */
		if ('\0' == tch)
			return (ascii_len+1);

		/* make sure we can accomodate ASCII char */
		if (aLen >= ascii_len)
			return (ascii_len+1);
		
		qLen++;
		if (QPDELIM == tch)
		{
			size_t	rLen=(qp_len - qLen);

			/* make sure we have a full HEX value */
			if (rLen < MAX_BYTE_HEX_DISPLAY_LENGTH)
				return (ascii_len+1);

			qLen += MAX_BYTE_HEX_DISPLAY_LENGTH;
		}
		else if (_T('_') == tch)
			tch = _T(' ');

		if ((exc=decode_QP(&lpszQP, &tch)) != EOK)
			return (ascii_len+1);

		lpszAscii = strladdch(lpszAscii, tch);
	}

	return aLen;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE qp_encode_buf (const char		szIBuf[],
								const UINT32	ulILen,	/* chars to encode */
								UINT32			*pulInLen,	/* encoded chars */
								char				szOBuf[],
								const UINT32	ulOLen,	/* available space */
								UINT32			*pulOutLen)	/* used space */
{
	LPCTSTR	ip=szIBuf;
	LPTSTR	op=szOBuf;
	UINT32	ulORemLen=ulOLen;

	if ((NULL == pulInLen) || (NULL == pulOutLen))
		return EPARAM;
	*pulOutLen = 0;
	*pulInLen = 0;

	if ((0 == ulILen) || (0 == ulOLen))
		return EOK;

	if ((NULL == ip) || (NULL == op))
		return EBADBUFF;

	for (; (*pulInLen < ulILen) && (ulORemLen > 0);
		  (*pulInLen)++, ip++, op++, (*pulOutLen)++, ulORemLen--)
	{
		TCHAR	ech=*ip;
		*op = _T('\0');

		/* if possible then transfer transparently */
		if (fIsQPXferChar(ech))
		{
			*op = ech;
			continue;
		}

		/* check if have enough room to encode */
		if (ulORemLen <= QPCHAR_DISPLAY_LENGTH)
			break;

		*op = QPDELIM;
		byte_to_hex_argument((BYTE) ech, (op+1));

		*pulOutLen += MAX_BYTE_HEX_DISPLAY_LENGTH;
		ulORemLen -= MAX_BYTE_HEX_DISPLAY_LENGTH;
		op += MAX_BYTE_HEX_DISPLAY_LENGTH;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE qp_decode_buf (const char		szIBuf[],
								const UINT32	ulILen,	/* chars to encode */
								UINT32			*pulInLen,	/* encoded chars */
								char				szOBuf[],
								const UINT32	ulOLen,	/* available space */
								UINT32			*pulOutLen)	/* used space */
{
	const char	*ip=szIBuf;
	char			*op=szOBuf;
	UINT32		ulIRemLen=ulILen;

	if ((NULL == pulInLen) || (NULL == pulOutLen))
		return EPARAM;
	*pulOutLen = 0;
	*pulInLen = 0;

	if ((0 == ulILen) || (0 == ulOLen))
		return EOK;

	if ((NULL == ip) || (NULL == op))
		return EBADBUFF;

	for ( ; (*pulOutLen < ulOLen) && (ulIRemLen > 0) ;
			ip++, (*pulInLen)++, ulIRemLen--)
	{
		EXC_TYPE	exc=EOK;
		char		ech=*ip;
		BOOLEAN	fQPDecodeIt=FALSE;

		/* check if need to decode */
		if (QPDELIM == ech)
		{
			if (ulIRemLen > 0)
			{
				char	nch=*(ip+1);

				if (isxdigit(nch))
				{
					/* 1st digit is HEX - check if 2nd is as well */
					if (ulIRemLen > 1)
						fQPDecodeIt = isxdigit(*(ip+2));
				}
				else	/* skip soft CRLF(s) */
				{
					if ((_T('\r') == nch) && (ulIRemLen > 1))
					{
						char	lch=*(ip+2);

						if (_T('\n') == lch)
						{
							ip += 2;
							(*pulInLen) += 2;
							ulIRemLen -= 2;	/* LF will be skipped by loop iteration step */
							continue;
						}
					}
					else if (_T('\n') == nch)
						continue;	/* allow LF only */
				}
			}

			/* check if have enough to decode */
			if (ulIRemLen < QPCHAR_DISPLAY_LENGTH)
				break;
		}

		if (fQPDecodeIt)
		{
			ech = (char) hex_argument_to_byte((ip+1), MAX_BYTE_HEX_DISPLAY_LENGTH, EXC_ARG(exc));
			if (exc != EOK)
				return exc;

			ulIRemLen -= MAX_BYTE_HEX_DISPLAY_LENGTH;
			*pulInLen += MAX_BYTE_HEX_DISPLAY_LENGTH;
			ip += MAX_BYTE_HEX_DISPLAY_LENGTH;
		}
		else	/* "plain" character */
		{
			/* not allowed to decode NULL char transparently */
			if ('\0' == ech)
				return EIONODATA;
		}

		*op = ech;
		op++;
		(*pulOutLen)++;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

typedef struct {
	char					*pszAllocBuf;
	char					*pszBuf;
	UINT32				ulMaxLen;
	UINT32				ulCurLen;
	IOWRITECALLBACK	lpfnWcfn;
	LPVOID				pFout;
	UINT32				oLen;
	char					oRem[QPCHAR_DISPLAY_LENGTH+1];	/* "leftovers" */
} QPSESS, *LPQPSESS;

/*---------------------------------------------------------------------------*/

/*	Opens and initializes a quoted-printable encode/decode session handle (NULL if error) */
LPQPESESSION qpsess_create (void)
{
	LPQPSESS	pSess=NULL;

#ifdef __cplusplus
	pSess = (LPQPSESS) new BYTE[sizeof(QPSESS)];
#else
	pSess = (LPQPSESS) malloc(sizeof(QPSESS));
#endif
	if (NULL == pSess)
		return NULL;

	memset(pSess, 0, (sizeof *pSess));
	return (LPQPESESSION) pSess;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE qpsess_delete (LPQPESESSION lpSess)
{
	LPQPSESS	pSess=(LPQPSESS) lpSess;

	if (NULL == pSess)
		return EPARAM;

	/* check if auto-allocated buffer */
	if (pSess->pszBuf != pSess->pszAllocBuf)
	{
		char	*pszA=pSess->pszAllocBuf;
		if (pszA != NULL)
		{
#ifdef __cplusplus
			delete [] pszA;
#else
			free((void *) pszA);
#endif
		}
	}

#ifdef __cplusplus
	LPBYTE	pBuf=(LPBYTE) pSess;
	delete [] pBuf;
#else
	free((void *) pSess);
#endif

	return EOK;
}

/*---------------------------------------------------------------------------*/

/* returns EIOALTASSIGNED if moved current position back */
static EXC_TYPE AdjustFlushLine (LPCTSTR	*lppszCurPos, UINT32 *pulFLen)
{
	UINT32	ulLdx=0;
	LPCTSTR	lsp=NULL;

	if ((NULL == lppszCurPos) || (NULL == pulFLen))
		return EPARAM;
	if (NULL == (lsp=*lppszCurPos))
		return EBADBUFF;
	if ((*pulFLen) <= QPCHAR_DISPLAY_LENGTH)
		return EIOWRPROT;

	/* check for QP encoding delimiter */
	for (lsp--; ulLdx < QPCHAR_DISPLAY_LENGTH; lsp--, ulLdx++)
		if (QPDELIM == *lsp)
		{
			*lppszCurPos = lsp;
			*pulFLen -= (ulLdx + 1);
			return EIOALTASSIGNED;
		}

	return EOK;
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE qpsess_flush (LPQPSESS			pSess,
										const BOOLEAN	fIsDecode,
										const BOOLEAN	fFlushIt)
{
	EXC_TYPE				exc=EOK;
	IOWRITECALLBACK	lpfnWcfn=NULL;
	UINT32				ulWLen=0;

	if (NULL == pSess)
		return EILLVOL;

	if ((NULL == (lpfnWcfn=pSess->lpfnWcfn)) || (NULL == pSess->pszAllocBuf))
		return EBADADDR;

	if (0 == pSess->ulCurLen)
		return EOK;

	if (fIsDecode)
	{
		if ((ulWLen=(*lpfnWcfn)(pSess->pFout, pSess->pszAllocBuf, pSess->ulCurLen)) != pSess->ulCurLen)
			return EIOSOFT;

		pSess->ulCurLen = 0;
	}
	else	/* format output for encoding */
	{
		LPCTSTR	lpszCurPos=pSess->pszAllocBuf, lpszLine=lpszCurPos;
		UINT32	ulCLen=0, ulLLen=0, ulFLen=0;

		for (ulCLen=0, ulLLen=0; ulCLen < pSess->ulCurLen; ulCLen++, ulLLen++, ulFLen++, lpszCurPos++)
		{
			static const TCHAR	szQPSoftBreak[]={	QPDELIM, _T('\r'), _T('\n'), _T('\0') };

			/* if reached hard break, then reset the line length count */
			if (_T('\n') == *lpszCurPos)
			{
				ulLLen = 0;
				continue;
			}

			/* check if current line exceeds QP max. */
			if (ulLLen < MAX_QPENC_LINE_LEN)
				continue;

			/* make sure we don't "cut" an encoded value */
			if ((exc=AdjustFlushLine(&lpszCurPos, &ulFLen)) != EOK)
			{
				if (EIOALTASSIGNED != exc)
					return exc;

				/* moved current position backward so update current index as well */
				ulCLen = (lpszCurPos - pSess->pszAllocBuf);
			}

			/* flush whatever data we have so far */
			if ((ulWLen=(*lpfnWcfn)(pSess->pFout, lpszLine, ulFLen)) != ulFLen)
				return EIOSOFT;

			/* create a soft break */
			ulFLen = _tcslen(szQPSoftBreak);
			if ((ulWLen=(*lpfnWcfn)(pSess->pFout, szQPSoftBreak, ulFLen)) != ulFLen)
				return EIOSOFT;

			/* restart line hunt */
			ulLLen = 0;
			ulFLen = 0;
			lpszLine = lpszCurPos;
		}

		/* at this point "lpszLine" points to unflushed data and "ulFLen" show how much unflushed data exists */
		if (fFlushIt)
		{
			if ((ulWLen=(*lpfnWcfn)(pSess->pFout, lpszLine, ulFLen)) != ulFLen)
				return EIOSOFT;

			pSess->ulCurLen = 0;
		}
		else	/* leftovers... */
		{
			/* find last hard LF and flush up to it */
			LPCTSTR	lpszLastChar=(lpszLine + ulFLen);
			for (lpszLastChar--; lpszLastChar > lpszLine; lpszLastChar--)
				if (_T('\n') == *lpszLastChar)
				{
					ulCLen = (lpszLastChar - lpszLine) + 1;

					if ((ulWLen=(*lpfnWcfn)(pSess->pFout, lpszLine, ulCLen)) != ulCLen)
						return EIOSOFT;

					lpszLine = (lpszLastChar + 1);
					ulFLen -= ulCLen;
				}

			/* at this point "lpszLine" points to "leftover" data and "ulFLen" shows how much we have to save */
			if ((lpszLine != pSess->pszAllocBuf) && (ulFLen != 0))
				_tcsncpy(pSess->pszAllocBuf, lpszLine, ulFLen);

			/* the "leftovers" cannot exceed an entire line */
			if ((pSess->ulCurLen=ulFLen) > MAX_QPENC_LINE_LEN)
				return EOVERFLOW;
			pSess->pszAllocBuf[pSess->ulCurLen] = _T('\0');
		}
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE qpsess_end (LPQPSESS		pSess,
									 const BOOLEAN	fIsDecode,
									 const BOOLEAN fFlushIt)
{
	EXC_TYPE	exc=EOK;

	if (fFlushIt)
	{
		if ((exc=qpsess_flush(pSess, fIsDecode, TRUE)) != EOK)
			return exc;
	}

	if (NULL == pSess)
		return ENOTCONNECTION;

	/* check if auto-allocated buffer */
	if (pSess->pszBuf != pSess->pszAllocBuf)
	{
		char	*pszA=pSess->pszAllocBuf;
		if (pszA != NULL)
		{
#ifdef __cplusplus
			delete [] pszA;
#else
			free((void *) pszA);
#endif
		}
	}

	memset(pSess, 0, (sizeof *pSess));

	return exc;
}

/*---------------------------------------------------------------------------*/

/*	Initialize and prepare for a new encoding session */
static EXC_TYPE qpsess_start (LPQPSESS				pSess,
										const BOOLEAN		fIsDecode,
										IOWRITECALLBACK	lpfnWcfn,
										void					*pFout,
				/* may be NULL */	char					pBuf[],
										const UINT32		iBufSize)
{
	EXC_TYPE	exc=qpsess_end(pSess, fIsDecode, FALSE);
	if (exc != EOK)
		return exc;

	/* request at least one line of buffering */
	if ((NULL == pSess) || (NULL == lpfnWcfn) || (iBufSize <= MAX_QPENC_LINE_LEN))
		return EPARAM;

	if (NULL == (pSess->pszBuf=pBuf))
	{
		char	*pszA=NULL;
#ifdef __cplusplus
		pszA = new char[iBufSize+2];
#else
		pszA = (char *) malloc(iBufSize+2);
#endif
		if (NULL == pszA)
			return EMEM;

		pSess->pszAllocBuf = pszA;
	}
	else	/* use user supplied buffer */
	{
		pSess->pszAllocBuf = pBuf;
	}

	pSess->ulMaxLen = iBufSize;
	*(pSess->pszAllocBuf) = '\0';
	pSess->lpfnWcfn = lpfnWcfn;
	pSess->pFout = pFout;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE qpsess_encode_start (LPQPESESSION		lpSess,
										IOWRITECALLBACK	lpfnWcfn,
										void					*pFout,
				/* may be NULL */	char					pBuf[],
										const UINT32		iBufSize)
{
	return qpsess_start((LPQPSESS) lpSess, FALSE, lpfnWcfn, pFout, pBuf, iBufSize);
}

/*---------------------------------------------------------------------------*/

/*	Process a new buffer for this session */
EXC_TYPE qpsess_encode_process (LPQPESESSION	lpSess,
										  const char	pBuf[],
										  const UINT32	ulBufLen)
{
	LPQPSESS		pSess=(LPQPSESS) lpSess;
	EXC_TYPE		exc=EOK;
	char			*sp=NULL;
	const char	*bp=pBuf;
	UINT32		ulAvailLen=0, ulRemLen=ulBufLen;
	UINT32		ulILen=0, ulOLen=0;

	if (NULL == pSess)
		return EPARAM;

	if (0 == ulBufLen)
		return EOK;

	if (NULL == pBuf)
		return EBADBUFF;

	if (NULL == pSess->pszAllocBuf)
		return EIOHARD;

	sp = (pSess->pszAllocBuf + pSess->ulCurLen);
	if ((ulAvailLen=(pSess->ulMaxLen - pSess->ulCurLen)) <= QPCHAR_DISPLAY_LENGTH)
	{
		if ((exc=qpsess_flush(pSess, FALSE, FALSE)) != EOK)
			return exc;

		sp = (pSess->pszAllocBuf + pSess->ulCurLen);
		ulAvailLen = (pSess->ulMaxLen - pSess->ulCurLen);
	}
	*sp = _T('\0');

	/* at this point we know there are no leftovers */
	while (ulRemLen > 0)
	{
		exc = qp_encode_buf(bp, ulRemLen, &ulILen, sp, ulAvailLen, &ulOLen);
		if (exc != EOK)
			return exc;

		ulAvailLen -= ulOLen;
		sp += ulOLen;
		*sp = _T('\0');
		pSess->ulCurLen += ulOLen;

		bp += ulILen;
		ulRemLen -= ulILen;

		if (ulAvailLen <= QPCHAR_DISPLAY_LENGTH)
		{
			if ((exc=qpsess_flush(pSess, FALSE, FALSE)) != EOK)
				return exc;

			sp = (pSess->pszAllocBuf + pSess->ulCurLen);
			*sp = _T('\0');
			ulAvailLen = (pSess->ulMaxLen - pSess->ulCurLen);
			continue;
		}

		/* encoding must always succeed - no leftovers */
		if (0 == ulILen)
			return EIOHARD;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

/*	Close the encoding session */
EXC_TYPE qpsess_encode_end (LPQPESESSION lpSess)
{
	return qpsess_end((LPQPSESS) lpSess, FALSE, TRUE);
}

/*---------------------------------------------------------------------------*/

/*	Initialize and prepare for a new decoding session */
EXC_TYPE qpsess_decode_start (LPQPESESSION		lpSess,
				/* may be NULL */	char					pBuf[],
										const UINT32		iMaxLen,
										IOWRITECALLBACK	lpfnWcfn,
										void					*pFout)
{
	return qpsess_start((LPQPSESS) lpSess, TRUE, lpfnWcfn, pFout, pBuf, iMaxLen);
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE qpsess_handle_decode_leftovers (LPQPSESS		pSess,
																const char	**lppszCurPos,
																UINT32		*pulRemLen)
{
	EXC_TYPE		exc=EOK;
	char			*sp=(pSess->pszAllocBuf + pSess->ulCurLen);
	char			*op=pSess->oRem, *ep=_tcschr(op, QPDELIM);
	UINT32		ulAvailLen=(pSess->ulMaxLen - pSess->ulCurLen);
	UINT32		ulILen=0, ulOLen=0;

	if (0 == pSess->oLen)
		return EOK;

	/*
	 *		Basically, we need to distinguish between having the beginning of an encoding
	 * and having none of it.
	 */

	if (NULL == ep)
	{
		exc = qp_decode_buf(op, pSess->oLen, &ulILen, sp, ulAvailLen, &ulOLen);
		if (exc != EOK)
			return exc;

		/* since no special decoding required, we expect full decode */
		if (ulILen != pSess->oLen)
			return ESTATE;

		pSess->ulCurLen += ulOLen;
	}
	else
	{
		/*		We have 3 possible leftovers which may include the encoding char
		 *
		 *	a. "=xx"	- the last one/two characters may be missing
		 * b. "x=x" - the last character may be missing
		 *	c.	"xx="
		 */

		/* 1. reach a state of "=xx" by decoding anything up to the '=' */
		UINT32	ulEdx=(ep - op);
		if (ulEdx > 0)
		{
			exc = qp_decode_buf(op, ulEdx, &ulILen, sp, ulAvailLen, &ulOLen);
			if (exc != EOK)
				return exc;

			/* since no special decoding required, we expect full decode */
			if (ulILen != ulEdx)
				return ESTATE;

			pSess->ulCurLen += ulOLen;
			sp += ulOLen;
			ulAvailLen -= ulOLen;

			/* "shift" everything (including EOS) so '=' sign is first */
			for (ulILen=0; ulEdx <= pSess->oLen; ulILen++, ulEdx++)
				op[ulILen] = op[ulEdx];
			pSess->oLen = _tcslen(pSess->oRem);
		}

		/* 2. try to complete up to full decode */
		for ( ; (pSess->oLen < QPCHAR_DISPLAY_LENGTH) && (*pulRemLen > 0); pSess->oLen++, (*pulRemLen)--, (*lppszCurPos)++)
			op[pSess->oLen] = *(*lppszCurPos);
		op[pSess->oLen] = '\0';

		/* 3. check if have enough information to decode */
		if (pSess->oLen < QPCHAR_DISPLAY_LENGTH)
			return EOK;

		exc = qp_decode_buf(op, QPCHAR_DISPLAY_LENGTH, &ulILen, sp, ulAvailLen, &ulOLen);
		if (exc != EOK)
			return exc;

		/* we expect entire buffer to be decoded */
		if (ulILen != QPCHAR_DISPLAY_LENGTH)
			return ESTATE;

		sp += ulOLen;
		ulAvailLen -= ulOLen;
		pSess->ulCurLen += ulOLen;
	}

	pSess->oLen = 0;
	pSess->oRem[0] = '\0';

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE qpsess_decode_process (LPQPESESSION	lpSess,
										  const char	lpszBuf[],
										  const UINT32	ulBufLen)
{
	LPQPSESS		pSess=(LPQPSESS) lpSess;
	EXC_TYPE		exc=EOK;
	UINT32		ulRemLen=ulBufLen, ulAvailLen=0;
	UINT32		ulILen=0, ulOLen=0;
	const char	*bp=lpszBuf;
	char			*sp=NULL;

	if (NULL == pSess)
		return ENOTCONNECTION;

	if (0 == ulBufLen)
		return EOK;

	if (NULL == lpszBuf)
		return EBADBUFF;

	if (NULL == pSess->pszAllocBuf)
		return EIOHARD;

	sp = (pSess->pszAllocBuf + pSess->ulCurLen);
	if ((ulAvailLen=(pSess->ulMaxLen - pSess->ulCurLen)) <= QPCHAR_DISPLAY_LENGTH)
	{
		if ((exc=qpsess_flush(pSess, TRUE, TRUE)) != EOK)
			return exc;

		sp = (pSess->pszAllocBuf + pSess->ulCurLen);
		ulAvailLen = (pSess->ulMaxLen - pSess->ulCurLen);
	}

	/* check if have any "leftovers" */
	if (pSess->oLen > 0)
	{
		if ((exc=qpsess_handle_decode_leftovers(pSess, &bp, &ulRemLen)) != EOK)
			return exc;

		sp = (pSess->pszAllocBuf + pSess->ulCurLen);
		ulAvailLen = (pSess->ulMaxLen - pSess->ulCurLen);
	}

	while (ulRemLen > 0)
	{
		if (ulAvailLen <= QPCHAR_DISPLAY_LENGTH)
		{
			if ((exc=qpsess_flush(pSess, TRUE, TRUE)) != EOK)
				return exc;

			sp = (pSess->pszAllocBuf + pSess->ulCurLen);
			ulAvailLen = (pSess->ulMaxLen - pSess->ulCurLen);
		}

		exc = qp_decode_buf(bp, ulRemLen, &ulILen, sp, ulAvailLen, &ulOLen);
		if (exc != EOK)
			return exc;

		sp += ulOLen;
		ulAvailLen -= ulOLen;
		pSess->ulCurLen += ulOLen;

		/* check why nothing encoded */
		if (0 == ulILen)
		{
			/* save for next time */
			if (ulRemLen < QPCHAR_DISPLAY_LENGTH)
			{
				strncpy(pSess->oRem, bp, ulRemLen);

				pSess->oRem[ulRemLen] = '\0';
				pSess->oLen = ulRemLen;
				break;
			}
		}
		else	/* something encoded */
		{
			ulRemLen -= ulILen;
			bp += ulILen;
		}
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE qpsess_decode_flush (LPQPESESSION lpSess)
{
	return qpsess_flush((LPQPSESS) lpSess, TRUE, TRUE);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE qpsess_decode_end (LPQPESESSION	lpSess)
{
	return qpsess_end((LPQPSESS) lpSess, TRUE, TRUE);
}

/*---------------------------------------------------------------------------*/

/* encodes the contents of the "fin" stream in quoted-printable and places the output in
 * the "fout" stream. The routine allocates 2 working buffers of "blkSize" (the
 * output buffer is actually ~1.33 * blkSize), which are freed once routine is
 * completed. The I/O is performed via the supplied  callback functions.
 */

EXC_TYPE qp_encode_stream (IOREADCALLBACK		lpfnRcfn,
									void					*pFin,
									IOWRITECALLBACK	lpfnWcfn,
									void					*pFout,
									const UINT32		iBlkSize)
{
	if ((NULL == lpfnRcfn) || (NULL == lpfnWcfn))
		return EBADADDR;
	if (iBlkSize < QPENC_MIN_IBLKSIZE)
		return EBADBUFF;

#ifdef __cplusplus
	LPTSTR			lpszInputBuf=NULL;
	CStrBufGuard	ibg(lpszInputBuf);
	if (NULL == (lpszInputBuf=new TCHAR[iBlkSize+sizeof(UINT32)]))
		return EMEM;

	CQPEncoder	qpe;
	EXC_TYPE		exc=qpe.Start(NULL, iBlkSize, lpfnWcfn, pFout);
	if (exc != EOK)
		return exc;

	for (UINT32	ulAccLen=0; ; )
	{
		UINT32	rLen=(*lpfnRcfn)(pFin, (UINT8 *) lpszInputBuf, iBlkSize);
		if (IOCFN_BAD_LEN == rLen)
			return EIOHARD;
		lpszInputBuf[rLen] = _T('\0');

		if ((exc=qpe.Process(lpszInputBuf, rLen)) != EOK)
			return exc;

		ulAccLen += rLen;
		if (rLen < iBlkSize)
			break;
	}

	if ((exc=qpe.End()) != EOK)
		return exc;
#else
#error	"C N/A"
	$$$;
#endif	/* of __cplusplus */

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE qp_encode_named_file (LPCTSTR				lpszFPath,
										 IOWRITECALLBACK	lpfnWcfn,
										 void					*pFout,
										 const UINT32		iSugBlkSize)
{
	FILE		*fin=NULL;
	EXC_TYPE	exc=EOK;

	if (IsEmptyStr(lpszFPath))
		return EPATH;

	if (NULL == lpfnWcfn)
		return EBADADDR;

	if (NULL == (fin=_tfopen(lpszFPath, _T("rb"))))
		return EFNEXIST;

	exc = qp_encode_file(fin, lpfnWcfn, pFout, iSugBlkSize);
	fclose(fin);

	return exc;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE qp_decode_stream (IOREADCALLBACK		lpfnRcfn,
									void					*pFin,
									IOWRITECALLBACK	lpfnWcfn,
									void					*pFout,
									const UINT32		iBlkSize)
{
	if ((NULL == lpfnRcfn) || (NULL == lpfnWcfn))
		return EBADADDR;
	if (iBlkSize < QPENC_MIN_IBLKSIZE)
		return EBADBUFF;

#ifdef __cplusplus
	LPTSTR			lpszInputBuf=NULL;
	CStrBufGuard	ibg(lpszInputBuf);
	if (NULL == (lpszInputBuf=new TCHAR[iBlkSize+sizeof(UINT32)]))
		return EMEM;

	CQPDecoder	qpd;
	EXC_TYPE		exc=qpd.Start(NULL, iBlkSize, lpfnWcfn, pFout);
	if (exc != EOK)
		return exc;

	for (UINT32	ulAccLen=0; ; )
	{
		UINT32	rLen=(*lpfnRcfn)(pFin, (UINT8 *) lpszInputBuf, iBlkSize);
		if (IOCFN_BAD_LEN == rLen)
			return EIOHARD;
		lpszInputBuf[rLen] = _T('\0');

		if ((exc=qpd.Process(lpszInputBuf, rLen)) != EOK)
			return exc;

		ulAccLen += rLen;
		if (rLen < iBlkSize)
			break;
	}

	if ((exc=qpd.End()) != EOK)
		return exc;
#else
#error	"C N/A"
	$$$;
#endif	/* of __cplusplus */

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE qp_decode_to_named_file (IOREADCALLBACK	lpfnRcfn,
											 LPVOID				pFin,
											 LPCTSTR				lpszOutput,
											 const UINT32		iBlkSize)
{
	FILE		*fout=NULL;
	EXC_TYPE	exc=EOK;

	if (IsEmptyStr(lpszOutput))
		return EPATH;

	if (NULL == lpfnRcfn)
		return EBADADDR;

	if (NULL == (fout=_tfopen(lpszOutput, _T("wb"))))
		return EFNEXIST;

	exc = qp_decode_to_file(lpfnRcfn, pFin, fout, iBlkSize);
	fclose(fout);

	return exc;
}

/*---------------------------------------------------------------------------*/
