#include <futils/general.h>

/*-----------------------------------------------------------------*/

/* callback to read from file pointer (FILE *) */
UINT32 fileIOReadCfn (void *pFin, UINT8 pBuf[], const UINT32 ulBufLen)
{
	FILE		*fin=(FILE *) pFin;
	UINT32	rLen=0;
	UINT8		*bp=pBuf;

	if ((NULL == pFin) || (NULL == pBuf))
		return IOCFN_BAD_LEN;

	while (rLen < ulBufLen)
	{
		size_t sCount=fread(bp, sizeof(UINT8), (ulBufLen - rLen), fin);

		rLen += sCount;
		bp += sCount;

		if (sCount < (ulBufLen - rLen))
		{
			if (ferror(fin))
				return IOCFN_BAD_LEN;
			if (feof(fin))
				return rLen;
		}
	}

	return rLen;
}

/*-----------------------------------------------------------------*/

/* callback to write to file (FILE *) */
UINT32 fileIOWriteCfn (void *pFout, const char pBuf[], const UINT32 ulBufLen)
{
	FILE			*fout=(FILE *) pFout;
	UINT32		wLen=0;
	const char	*bp=pBuf;

	if ((NULL == pFout) || (NULL == pBuf))
		return IOCFN_BAD_LEN;

	while (wLen < ulBufLen)
	{
		size_t sCount=fwrite(bp, sizeof(char), (ulBufLen - wLen), fout);

		if (sCount < (ulBufLen - wLen))
		{
			if (ferror(fout) || ((-1) == (int) sCount))
				return IOCFN_BAD_LEN;
		}

		wLen += sCount;
		bp += sCount;
	}

	return wLen;
}

/*---------------------------------------------------------------------------*/

/* callback to write to text file (FILE *) */
UINT32 textFileIOWriteCfn (void *pFout, const char pBuf[], const UINT32 ulBufLen)
{
	FILE			*fout=(FILE *) pFout;
	UINT32		wLen=0;
	const char	*bp=pBuf;

	if ((NULL == pFout) || (NULL == pBuf))
		return IOCFN_BAD_LEN;

	/* skip CR */
	if ('\r' == *bp)
	{
		bp++;
		wLen++;
	}

	while (wLen < ulBufLen)
	{
		size_t sCount=fwrite(bp, sizeof(char), (ulBufLen - wLen), fout);

		if (sCount < (ulBufLen - wLen))
		{
			if (ferror(fout) || ((-1) == (int) sCount))
				return IOCFN_BAD_LEN;
		}

		wLen += sCount;
		bp += sCount;
	}

	return wLen;
}

/*---------------------------------------------------------------------------*/

/* reads a "line" (including the '\n') from the input (up to max buf len) */
UINT32 sIOReadLine (IOREADCALLBACK	lpfnRcfn,
						  void				*pFin,
						  UINT8				pBuf[],
						  const UINT32		ulBufLen)
{
	UINT8		*bp=pBuf;
	UINT32	uLen=0;

	if ((NULL == lpfnRcfn) || (NULL == pBuf))
		return IOCFN_BAD_LEN;

	/* leave one place for the terminating '\0' */
	while (uLen < ulBufLen)
	{
		UINT32 rLen=(*lpfnRcfn)(pFin, bp, sizeof(char));

		if (rLen != sizeof(char))
		{
			if (uLen > 0)
				break;
			return IOCFN_BAD_LEN;
		}

		/* skip CR (use only LF as end of line) */
		if ('\r' == (char) (*bp))
			continue;
		if ('\n' == (char) (*bp))
			break;

		bp++;
		uLen++;
	}

	if (uLen < ulBufLen)
		*bp = (UINT8) '\0';
	return uLen;
}

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
int CFileFdGuard::Close ()
{
	if (m_fd >= 0)
	{
		int	nRet=::close(m_fd);
		m_fd = (-1);
		return nRet;
	}
	else
		return 0;
}
#endif	/* __cplusplus */

/*---------------------------------------------------------------------------*/

/*
 * Callback that can be used to seek a stream
 *
 *		nDir - <0 means BACKWARD-RELATIVE to current position
 *				 >0 means FORWARD-RELATIVE to current position
 *				 =0 means ABSOLUTE POSITION
 *
 *		ulCount - ABSOLUTE offset to be used for seeking
 */
EXC_TYPE fileIOSeekCfn (void *pFp, const int nDir, const UINT32 ulCount)
{
	if (NULL == pFp)
		return ENOTCONNECTION;

	if (nDir != 0)
		return fseek((FILE *) pFp, (long) SIGNOF(nDir) * (long) ulCount, SEEK_CUR);
	else
		return fseek((FILE *) pFp, ulCount, SEEK_SET);
}

/*---------------------------------------------------------------------------*/
