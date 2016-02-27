#include <comm/socket.h>

/*-----------------------------------------------------------------*/

#ifdef __cplusplus
/* callback to read from a ISockioInterface object */
UINT32 isioIOReadCfn (void *pFin, UINT8 pBuf[], const UINT32 ulBufLen)
{
	static const SINT32	DEFAULT_READ_TIMEOUT=30;

	ISockioInterface	*pSock=(ISockioInterface *) pFin;
	UINT32				rLen=0;
	UINT8					*bp=pBuf;

	if ((NULL == pFin) || (NULL == pBuf))
		return IOCFN_BAD_LEN;

	while (rLen < ulBufLen)
	{
		int sCount=pSock->Read((char *) bp, (ulBufLen - rLen), DEFAULT_READ_TIMEOUT);

		// if read something from socket, then return it (delay EOF till next call)
		if (sCount < 0)
		{
			if (0 == rLen)
				return IOCFN_BAD_LEN;
			break;
		}

		rLen += sCount;
		bp += sCount;
	}

	return rLen;
}
#endif	/* of __cplusplus */

/*-----------------------------------------------------------------*/

UINT32 sockIOWriteCfn (void *pFout, const char pBuf[], const UINT32 ulBufLen)
{
	SOCKET	sock=(SOCKET) pFout;
	int		sCount=(-1);

	if ((BAD_SOCKET == sock) || (NULL == pBuf))
		return IOCFN_BAD_LEN;

	if ((UINT32) (sCount=sockWrite(sock, pBuf, ulBufLen)) != ulBufLen)
		return IOCFN_BAD_LEN;
	else
		return ulBufLen;
}

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
/* callback to write to a ISockioInterface object */
UINT32 isioIOWriteCfn (void *pFout, const char pBuf[], const UINT32 ulBufLen)
{
	ISockioInterface	*pSock=(ISockioInterface *) pFout;
	int					sCount=(-1);

	if ((NULL == pFout) || (NULL == pBuf))
		return IOCFN_BAD_LEN;

	if ((UINT32) (sCount=pSock->Write(pBuf, ulBufLen)) != ulBufLen)
		return IOCFN_BAD_LEN;
	else
		return ulBufLen;
}
#endif	/* of __cplusplus */

/*-----------------------------------------------------------------*/
