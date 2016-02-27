#include <stdio.h>
#include <string.h>

#include <util/errors.h>
#include <comm/socket.h>

/*-------------------------------------------------------------------------*/

EXC_TYPE CMultiSock::SetReadSocket (SOCKET			sock,
												const UINT32	ulMaxLen,
												const BOOLEAN	fAddToWriteSet)
{
	EXC_TYPE	exc=EOK;

	if ((exc=m_ReadSock.Detach()) != EOK)
		return exc;
	if ((exc=m_ReadSock.Attach(sock, ulMaxLen)) != EOK)
		return exc;

	if (fAddToWriteSet)
	{
		if ((exc=Attach(sock)) != EOK)
			return exc;
	}

	return EOK;
}

/*-------------------------------------------------------------------------*/

// Note: error is returned only if writing fails on read socket (if written to)
int CMultiSock::Write (const char buf[], const size_t bufLen)
{
	SOCKET rsock=m_ReadSock.GetSocket();

	if (0 == m_WriteSocks.GetSize())
		return (-4);

	if (0 == bufLen)
		return bufLen;

	if (NULL == buf)
		return (-1);

	CSockSetEnum	se(&m_WriteSocks);
	for (SOCKET wsock=se.GetFirstSock(); wsock != BAD_SOCKET; wsock=se.GetNextSock())
	{
		int	wLen=::sockWrite(wsock, buf, bufLen);

		if ((size_t) wLen != bufLen)
		{
			// return error only if such received from read socket
			if (rsock == wsock)
				return wLen;
		}
	}

	return bufLen;
}

/*-------------------------------------------------------------------------*/

// Note: error is returned only if writing fails on read socket (if written to)
int CMultiSock::Writeln (const char buf[], const size_t bufLen)
{
	int	wLen=Write(buf, bufLen);

	if (wLen < 0)
		return wLen;

	return Write("\r\n", 2);
}

/*-------------------------------------------------------------------------*/

int CMultiSock::WriteVCmdf (const char fmt[], va_list ap)
{
	if ((NULL == fmt) || ('\0' == *fmt))
		return (-3);

	char	line[MAX_SOCK_CMDF_LINE_LEN+1];
	int	wLen=_vsnprintf(line, MAX_SOCK_CMDF_LINE_LEN, fmt, ap);

	if (wLen < 0)
		return wLen;

	line[MAX_SOCK_CMDF_LINE_LEN] = '\0';
	return Write(line, wLen);
}

/*-------------------------------------------------------------------------*/

EXC_TYPE CMultiSock::CloseAll (const BOOL fCloseReadSock)
{
	EXC_TYPE	exc=EOK, err=EOK;
	SOCKET	rsock=m_ReadSock.GetSocket();

	if (fCloseReadSock)
	{
		if ((err=Close()) != EOK)
			exc = err;
	}

	CSockSetEnum	se(&m_WriteSocks);
	for (SOCKET wsock=se.GetFirstSock(); wsock != BAD_SOCKET; wsock=se.GetNextSock())
	{
		// skip since either already closed or not allowed to close it anyway
		if (wsock == rsock)
			continue;

		exc = sockClose(wsock);
	}

	m_WriteSocks.ClearSet();

	return exc;
}

/*-------------------------------------------------------------------------*/
