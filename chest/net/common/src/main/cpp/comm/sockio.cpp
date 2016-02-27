#include <stddef.h>
#include <stdlib.h>
#include <ctype.h>

#include <_types.h>
#include <util/string.h>
#include <util/errors.h>
#include <comm/socket.h>

/*---------------------------------------------------------------------------*/

/* writes the supplied buffer up to "buflen" characters.
 *
 * returns number of actual characters written or (-1) if write error occurred.
 */

int sockWrite (SOCKET sock, const char buf[], const size_t buflen)
{
	const char *bp=buf;
	size_t wLen=0;

	while (wLen < buflen)
	{
		int	sLen=(buflen - wLen);
		int	cLen=send(sock, bp, sLen, 0);
		if (cLen <= 0)
			return (-1);

		wLen += cLen;
		bp += cLen;
	}

	return wLen;
}

/*---------------------------------------------------------------------------*/

int sockWriteVCmdf (SOCKET sock, const char fmt[], va_list ap)
{
	char	line[MAX_SOCK_CMDF_LINE_LEN+1];
	int	wLen=_vsnprintf(line, MAX_SOCK_CMDF_LINE_LEN, fmt, ap);

	if (wLen < 0)
		return wLen;

	line[MAX_SOCK_CMDF_LINE_LEN] = '\0';
	return sockWrite(sock, line, wLen);
}

/*---------------------------------------------------------------------------*/

/*		This routine provides a general formatting interface for writing a SHORT
 * command/response to a socket.
 *
 * Note: inefficient (uses "sprintf") and limited to up to MAX_SOCK_CMDF
 * characters.
 *
 * Returns number of actual characters written or (-1) if write error.
 */

int sockWriteCmdf (SOCKET sock, const char fmt[], ...)
{
	va_list ap;
	int retval=(-1);

	va_start(ap, fmt);
	retval = sockWriteVCmdf(sock, fmt, ap);
	va_end(ap);

	return retval;
}

/*---------------------------------------------------------------------------*/

/*		Reads one command from socket. A command is defined as all characters
 * up to the first CRLF (which is not read as part of the line). If only <LF>
 * found, then it is considered an end-of-line as well.
 *
 * Function returns number of characters in the "buf" (up to "bufLen") or (-1)
 * if read error occurred.
 *
 * Note: an internal inactivity mechanism is implemented - if maxSecs != 0 then
 *			routine waits up to the specified number of seconds for input.
 */

int sockReadCmd (SOCKET sock, char buf[], const size_t bufLen, const SINT32 maxSecs)
{
	size_t		rLen=0;
	char			*bp=buf;
	ioctl_value	aLen=0;

	if ((BAD_SOCKET == sock) || (NULL == buf))
		return (-1);

	while (rLen < bufLen)
	{
		int cbRcv=(-1);

		/* if exhausted available data then wait for more */
		if ((0 == aLen) && (maxSecs != 0))
		{
			struct timeval timeoutVal={ maxSecs, 0 };
			fd_set readSet;

			FD_ZERO(&readSet);
			FD_SET(sock, &readSet);

			/* wait a limited amount of time for user input */
			if ((cbRcv=select(sock, &readSet, NULL, NULL, &timeoutVal)) == 0) /* time limit expired */
				return (-2);

			/* check if some error occured */
			if (SOCKET_ERROR == cbRcv)
				return (-1);

			/* check how much data we can read at once */
			if (ioctlsocket(sock, FIONREAD, &aLen) != 0)
				return (-3);

			/* we must have some data since "select" succeeded */
			if (0 == aLen)
				return (-8);
		}

		cbRcv = recv(sock, bp, sizeof(char), 0);
		if (aLen > 0)
			aLen--;

		if (cbRcv != sizeof(char))
		{
			if (0 == rLen)
				return (-1);
			break;
		}

		/* skip CR */
		if (((char) 0x0d) == (*bp))
			continue;

		if (((char) 0x0a) == (*bp))
			break;

		bp++;
		rLen++;
	}

	*bp = '\0';
	return rLen;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE sockClose (SOCKET sock)
{
	int nRes=(-1);

	if (BAD_SOCKET == sock)
		return EPARAM;

	nRes = shutdown(sock, 2);
	if ((nRes=closesocket(sock)) != 0)
		return EIOHARD;

	return EOK;
}

#ifdef __cplusplus
/*---------------------------------------------------------------------------*/

// Note: if already set then error returned
EXC_TYPE ISockioInterface::SetIOCfn (ISOCK_IO_CFN lpfnIcfn, LPVOID pIArg)
{
	if (NULL == lpfnIcfn)
		return EBADADDR;

	if (m_lpfnIcfn != NULL)
		return EFEXIST;

	m_lpfnIcfn = lpfnIcfn;
	m_pIArg = pIArg;
	return EOK;
}

/*-------------------------------------------------------------------------*/

int ISockioInterface::WriteCmdf (const char fmt[], ...)
{
	va_list ap;
	int retval=(-1);

	va_start(ap, fmt);
	retval = WriteVCmdf(fmt, ap);
	va_end(ap);

	return retval;
}

/*-------------------------------------------------------------------------*/
#endif	/* of ifdef __cplusplus */

#ifdef __cplusplus
EXC_TYPE CopyFileToSocket (FILE					*fin,
									ISockioInterface&	ISock,
									const UINT32		ulBufLen,
									BYTE					*pBuf)
{
	EXC_TYPE	exc=EOK;
	BYTE		*bp=pBuf;

	if ((NULL == fin) || (0 == ulBufLen))
		return EPARAM;

	if (NULL == pBuf)
	{
		if (NULL == (bp=new BYTE[ulBufLen+2]))
			return EMEM;
	}

	for (UINT32 ulBdx=0; ; ulBdx++)
	{
		int	rLen=fread((void *) bp, (sizeof *bp), ulBufLen, fin), wLen=(-1);
		if (0 == rLen)
			break;

		if ((wLen=ISock.Write((const char *) bp, rLen)) != rLen)
		{
			exc = EIOSOFT;
			break;
		}
	}

	if (NULL == pBuf)
	{
		if (bp != NULL)
		{
			delete [] bp;
			bp = NULL;
		}
	}

	return exc;
}
#endif	/* of ifdef __cplusplus */

/*-------------------------------------------------------------------------*/

#ifdef __cplusplus
EXC_TYPE CopyNamedFileToSocket (const char			pszFPath[],
										  ISockioInterface&	ISock,
										  const UINT32			ulBufLen,
										  BYTE					*pBuf)	// may be NULL
{
	FILE		*fin=NULL;
	EXC_TYPE	exc=EOK;

	if (IsEmptyStr(pszFPath))
		return EPATHNAMESYNTAX;

	if (NULL == (fin=fopen(pszFPath, "rb")))
		return EFNEXIST;

	exc = CopyFileToSocket(fin, ISock, ulBufLen, pBuf);
	fclose(fin);

	return exc;
}
#endif	/* of ifdef __cplusplus */

/*-------------------------------------------------------------------------*/
