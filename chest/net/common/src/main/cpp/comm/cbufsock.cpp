#include <stdio.h>
#include <string.h>

#include <util/errors.h>
#include <util/string.h>
#include <comm/socket.h>

/*-------------------------------------------------------------------------*/

// also default constructor
CBuffSock::CBuffSock (ISOCK_IO_CFN	lpfnIcfn, LPVOID pIArg)
	: ISockioInterface(lpfnIcfn, pIArg),
	  m_sock(BAD_SOCKET), m_ulMaxLen(0), m_ulCurLen(0), m_ulCurIdx(0),
	  m_pBuf(NULL), m_fAutoAlloc(FALSE)
{
}

/*-------------------------------------------------------------------------*/

CBuffSock::CBuffSock (SOCKET			sock,
							 const UINT32	ulMaxLen,
							 LPBYTE			pBuf,
							 ISOCK_IO_CFN	lpfnIcfn,
							 LPVOID			pIArg)
	: ISockioInterface(lpfnIcfn, pIArg),
	  m_sock(BAD_SOCKET), m_ulMaxLen(0), m_ulCurLen(0), m_ulCurIdx(0),
	  m_pBuf(NULL), m_fAutoAlloc(FALSE)
{
	if (sock != BAD_SOCKET)
		Attach(sock, ulMaxLen, pBuf);
	else
		SetIOBuffer(ulMaxLen, pBuf);
}

/*-------------------------------------------------------------------------*/

// returns number of actually read data bytes (or negative if error)
// if maxSecs != 0 then awaits maximum specified seconds for data to
// become available on socket
int CBuffSock::FillBuf (const SINT32 maxSecs)
{
	if ((BAD_SOCKET == m_sock) || (NULL == m_pBuf) || (0 == m_ulMaxLen))
		return (-1);

	// if no more room then do nothing
	if (m_ulCurLen >= m_ulMaxLen)
		return 0;

	fd_set	readSet;
	FD_ZERO(&readSet);
	FD_SET(m_sock, &readSet);

	struct timeval timeoutVal={ maxSecs, 0 }, *ptm=&timeoutVal;
	if (0 == maxSecs)
		ptm = NULL;

	/* wait a limited amount of time for user input */
	int	cbRcv=select(m_sock, &readSet, NULL, NULL, ptm);
	if (0 == cbRcv) /* time limit expired */
		return (-2);

	/* check if some error occured */
	if (SOCKET_ERROR == cbRcv)
		return (-1);

	/* find out how much we can read without blocking */
	ioctl_value	ulAvailBytes=0;
	if (ioctlsocket(m_sock, FIONREAD, &ulAvailBytes) != 0)
		return (-3);

	/* some data MUST exist (e.g. when a socket is closed, a zero size is reported) */
	if (0 == ulAvailBytes)
		return (-8);

	UINT32	ulReadLen=min((m_ulMaxLen - m_ulCurLen), (UINT32) ulAvailBytes);
	BYTE	*bp=(m_pBuf + m_ulCurLen);
	cbRcv = recv(m_sock, (char *) bp, ulReadLen, 0);

	/* since we use a blocking sys-call, some data MUST exist upon return */
	if ((0 == cbRcv) || (SOCKET_ERROR == cbRcv))
		return (-4);

	// should not happen since we asked for only "ulReadLen" bytes to read
	if (((UINT32) cbRcv) > ulReadLen)
		return (-5);

	if (m_lpfnIcfn != NULL)
	{
		static const ISOCKIOFLAGS fFlags={ 1, 0, 0 };

		EXC_TYPE	exc=(*m_lpfnIcfn)(*this, fFlags, bp, cbRcv, m_pIArg);
		if (exc != EOK)
			return (-6);
	}

	m_ulCurLen += cbRcv;
	return cbRcv;
}

/*-------------------------------------------------------------------------*/

// throws away any current data and also exhausts all currently
// available data from socket.
EXC_TYPE CBuffSock::FlushReadBuffer ()
{
	if ((BAD_SOCKET == m_sock) || (NULL == m_pBuf) || (0 == m_ulMaxLen))
		return ECONNNOTOPEN;

	/* find out how much we can read without blocking */
	ioctl_value	ulAvailBytes=0;
	if (ioctlsocket(m_sock, FIONREAD, &ulAvailBytes) != 0)
		return EIOJOB;

	while (ulAvailBytes > 0)
	{
		UINT32	ulReadLen=min((UINT32) ulAvailBytes, m_ulMaxLen);

		// this call should not block since we know there is available data
		int		cbRcv=recv(m_sock, (char *) m_pBuf, ulReadLen, 0);

		/* since we know data exists, some data MUST exist upon return */
		if ((0 == cbRcv) || (SOCKET_ERROR == cbRcv))
			return ETRANSMISSION;

		// should not happen since we asked for only "ulReadLen" bytes to read
		if (((UINT32) cbRcv) > ulReadLen)
			return EFRAGMENTATION;

		ulAvailBytes -= ulReadLen;
	}

	m_ulCurLen = 0;
	m_ulCurIdx = 0;

	return EOK;
}

/*-------------------------------------------------------------------------*/

EXC_TYPE CBuffSock::SetIOBuffer (const UINT32 ulMaxLen, LPBYTE	pBuf)
{
	// check if already have address
	if (m_pBuf != NULL)
		return EOPENPARAM;

	// check if buffering required
	if (ulMaxLen > sizeof(UINT32))
	{
		if (NULL == pBuf)
		{
			// add padding bytes to avoid boundary conditions
			if (NULL == (m_pBuf=new BYTE[ulMaxLen+sizeof(UINT32)]))
				return EMEM;
		}
		else	// have external buffer
		{
			m_pBuf = pBuf;
		}

		memset(m_pBuf, 0, ulMaxLen);
		m_fAutoAlloc = (NULL == pBuf);
		m_ulMaxLen = ulMaxLen;
	}

	m_ulCurLen = 0;
	m_ulCurIdx = 0;

	return EOK;
}

/*-------------------------------------------------------------------------*/

EXC_TYPE CBuffSock::Attach (SOCKET	sock, const UINT32 ulMaxLen, LPBYTE pBuf)
{
	m_ulReadCount = 0;
	m_ulWriteCount = 0;

	EXC_TYPE	exc=SetIOBuffer(ulMaxLen, pBuf);
	if (exc != EOK)
	{
		// allow already existing buffer
		if (EOPENPARAM != exc)
			return exc;
	}

	if (BAD_SOCKET == sock)
		return EPARAM;

	// check if already attached
	if (m_sock != BAD_SOCKET)
		return EPORTIDUSED;

	m_sock = sock;
	return EOK;
}

/*-------------------------------------------------------------------------*/

// Note: does not close the socket !!!
EXC_TYPE CBuffSock::Detach (const BOOL fCloseSock)
{
	if (fCloseSock)
		return Close();

	if (m_pBuf != NULL)
	{
		if (m_fAutoAlloc)
			delete [] m_pBuf;
		m_pBuf = NULL;
	}

	m_fAutoAlloc = FALSE;
	m_sock = BAD_SOCKET;
	m_ulMaxLen = 0;
	m_ulCurLen = 0;
	m_ulCurIdx = 0;

	return EOK;
}

/*-------------------------------------------------------------------------*/

/*
 *		Reads up to specified buffer size - if data available in buffer then
 * returns data from it. Otherwise waits up to specified number of seconds
 * and returns whatever data received by then.
 *
 * Note: the fact that returned data len is less than requested does not mean
 *			in any way that the peer host finished sending a message !!!
 */
int CBuffSock::Read (char buf[], const size_t bufLen, const SINT32 maxSecs)
{
	if ((NULL == buf) || (BAD_SOCKET == m_sock))
		return (-1);
	*buf = '\0';

	/* if no buffering then read as much as available */
	if (0 == m_ulMaxLen)
	{
		struct timeval timeoutVal={ maxSecs, 0 };
		fd_set			readSet;
		FD_ZERO(&readSet);
		FD_SET(m_sock, &readSet);

		/* wait a limited amount of time for user input */
		int	cbRcv=select(m_sock, &readSet, NULL, NULL, &timeoutVal);
		if (0 == cbRcv) /* time limit expired */
			return (-2);

		/* check if some error occured */
		if (SOCKET_ERROR == cbRcv)
			return (-1);

		/* find out how much we can read without blocking */
		ioctl_value	ulAvailBytes=0;
		if (ioctlsocket(m_sock, FIONREAD, &ulAvailBytes) != 0)
			return (-3);

		/* some data MUST exist (e.g. when a socket is closed, a zero size is reported) */
		if (0 == ulAvailBytes)
			return (-8);

		ioctl_value	ulReadLen=min(bufLen, ulAvailBytes);
		cbRcv = recv(m_sock, buf, ulReadLen, 0);

		if (m_lpfnIcfn != NULL)
		{
			static const ISOCKIOFLAGS rFlags={ 1, 0, 0 };

			EXC_TYPE	exc=(*m_lpfnIcfn)(*this, rFlags, (const BYTE *) buf, cbRcv, m_pIArg);
			if (exc != EOK)
				return (-4);
		}

		if (cbRcv > 0)
			m_ulReadCount += cbRcv;

		return cbRcv;
	}

	/* check how much unread data is available */
	UINT32	ulDataLen=0;
	if (m_ulCurLen >= m_ulCurIdx)
		ulDataLen = (m_ulCurLen - m_ulCurIdx);

	if (0 == ulDataLen)
	{
		int		rLen=(-1);
		UINT32	ulRemLen=0;

		/* check how much more we can read into buffer */
		if (m_ulMaxLen >= m_ulCurLen)
			ulRemLen = (m_ulMaxLen - m_ulCurLen);
		if (0 == ulRemLen)
		{
			m_ulCurIdx = 0;
			m_ulCurLen = 0;
		}

		if ((rLen=FillBuf(maxSecs)) <= 0)
			return rLen;

		if (m_ulCurLen >= m_ulCurIdx)
			ulDataLen = m_ulCurLen - m_ulCurIdx;
	}

	const UINT32	cLen=min(ulDataLen, bufLen);
	const char		*bp=(char *) (m_pBuf + m_ulCurIdx);

	memcpy(buf, bp, cLen);
	m_ulCurIdx += cLen;
	m_ulReadCount += cLen;

	return cLen;
}

/*-------------------------------------------------------------------------*/

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

int CBuffSock::ReadCmd (char buf[], const size_t bufLen, const SINT32 maxSecs, BOOLEAN	*pfStrippedCRLF)
{
	char		*bp=buf;
	size_t	bLen=0;
	BOOLEAN	fReadCont=TRUE, fStripCRLF=FALSE;

	if ((NULL == buf) || (BAD_SOCKET == m_sock))
		return (-1);

	*bp = '\0';
	if (pfStrippedCRLF != NULL)
		*pfStrippedCRLF = FALSE;

	/* if no buffering then call low-level routine */
	if (0 == m_ulMaxLen)
		return Read(buf, bufLen, maxSecs);

	if (bufLen <= 0)
		return bufLen;

	while (fReadCont)
	{
		for (const char	*m_bp=(const char *) (m_pBuf + m_ulCurIdx) ;
			  (m_ulCurIdx < m_ulCurLen) && (fReadCont);
			  m_ulCurIdx++, m_bp++, m_ulReadCount++)
		{
			/* skip CR (if found) */
			if (((char) 0x0d) == (*m_bp))
				continue;

			/* return line */
			if (((char) 0x0a) == (*m_bp))
			{
				*bp = '\0';
				fReadCont = FALSE;
				fStripCRLF = TRUE;
				continue;	// skip LF
			}

			/* textual command lines cannot contain EOS */
			if (_T('\0') == *m_bp)
				*bp = _T(' ');
			else
				*bp = *m_bp;

			bp++;
			bLen++;

			if (bLen >= bufLen)
				fReadCont = FALSE;
		}

		/* if all data exhausted then "collapse" data buffer */
		if (m_ulCurIdx >= m_ulCurLen)
		{
			m_ulCurLen = 0;
			m_ulCurIdx = 0;
			memset(m_pBuf, 0, m_ulMaxLen);
		}

		if (fReadCont)
		{
			/*		This point is reached if data in buffer could not be used
			 * to build a line.
			 */
			int	rLen=(-1);

			if ((rLen=FillBuf(maxSecs)) < 0)
				return rLen;
		}
	}

	/* this point is reached if CR/LF found or buffer filled */
	if (pfStrippedCRLF != NULL)
		*pfStrippedCRLF = fStripCRLF;
	*bp = '\0';

	if (m_lpfnIcfn != NULL)
	{
		const ISOCKIOFLAGS cFlags={ 1, 1, (fStripCRLF ? 1 : 0) };

		EXC_TYPE	exc=(*m_lpfnIcfn)(*this, cFlags, (const BYTE *) buf, bLen, m_pIArg);
		if (exc != EOK)
			return (-5);
	}

	return bLen;
}

/*-------------------------------------------------------------------------*/

int CBuffSock::Write (const char buf[], const size_t bufLen)
{
	if (m_lpfnIcfn != NULL)
	{
		static const ISOCKIOFLAGS wFlags={ 0, 0, 0 };

		EXC_TYPE	exc=(*m_lpfnIcfn)(*this, wFlags, (const BYTE *) buf, bufLen, m_pIArg);
		if (exc != EOK)
			return (-4);
	}

	const int	wLen=::sockWrite(m_sock, buf, bufLen);
	if (wLen > 0)
		m_ulWriteCount += wLen;
	return wLen;
}

/*-------------------------------------------------------------------------*/

int CBuffSock::CfnWriteVCmdf (const char fmt[], va_list ap)
{
	char	line[MAX_SOCK_CMDF_LINE_LEN+1]="";
	int	wLen=_vsnprintf(line, MAX_SOCK_CMDF_LINE_LEN, fmt, ap);
	if (wLen < 0)
		return wLen;
	line[wLen] = '\0';

	return Write(line, wLen);
}

/*-------------------------------------------------------------------------*/

int CBuffSock::WriteVCmdf (const char fmt[], va_list ap)
{
	if (NULL == m_lpfnIcfn)
	{
		const int wLen=::sockWriteVCmdf(m_sock, fmt, ap);
		if (wLen > 0)
			m_ulWriteCount += wLen;
		return wLen;
	}
	else
		return CfnWriteVCmdf(fmt, ap);
}

/*-------------------------------------------------------------------------*/

#define CRLF_LEN	2
static const char szCrLf[]="\r\n";

int CBuffSock::Writeln (const char buf[], const size_t bufLen)
{
	int wLen=(-1);

	if ((size_t) (wLen=Write(buf, bufLen)) != bufLen)
		return wLen;

	if ((wLen=Write(szCrLf, CRLF_LEN)) != CRLF_LEN)
		return wLen;

	return bufLen;
}

/*-------------------------------------------------------------------------*/

EXC_TYPE CBuffSock::Connect (const char hostName[], const int iPort)
{
	EXC_TYPE	exc=EOK;
	SOCKET	sock=BAD_SOCKET;

	m_ulReadCount = 0;
	m_ulWriteCount = 0;

	if (IsEmptyStr(hostName) || (iPort <= 0))
		return EPARAM;

	if (BAD_SOCKET != m_sock)
		return ECONNOPEN;

	if ((exc=::sock_connect(&sock, hostName, iPort)) != EOK)
		return exc;

	if ((exc=Attach(sock)) != EOK)
	{
		Detach();
		::sockClose(sock);
		return exc;
	}

	return EOK;
}

/*-------------------------------------------------------------------------*/

// closes the socket and releases the associated resources
EXC_TYPE CBuffSock::Close ()
{
	EXC_TYPE exc=EOK;

	if (m_sock != BAD_SOCKET)
	{
		exc = ::sockClose(m_sock);
		m_sock = BAD_SOCKET;
	}

	Detach(FALSE);

	return exc;
}

//////////////////////////////////////////////////////////////////////////////

// returns index of first (!) socket with available data
EXC_TYPE WaitOnMultipleBuffSocks (const CBuffSock	*socks[],
											 const UINT32		ulNumSocks,
											 const UINT32		ulWaitTimeout,
											 UINT32&				ulSdx)
{
	ulSdx = ulNumSocks;

	if (0 == ulNumSocks)
		return EOK;
	if (ulNumSocks > FD_SETSIZE)
		return EOVERFLOW;

	if (NULL == socks)
		return EBADBUFF;

	// find out if have any available data to read already
	for (ulSdx=0; ulSdx < ulNumSocks; ulSdx++)
	{
		const CBuffSock	*pASock=socks[ulSdx];
		if (NULL == pASock)
			return EFTYPE;

		const UINT32	ulAvailBytes=pASock->GetAvailableData();
		if (ulAvailBytes != 0)
			return EOK;
	}

	struct timeval timeoutVal={ (SINT32) ulWaitTimeout, 0 };
	fd_set			readSet;
	SOCKET			maxSock=0;

	FD_ZERO(&readSet);
	for (ulSdx=0; ulSdx < ulNumSocks; ulSdx++)
	{
		const CBuffSock	*pRSock=socks[ulSdx];
		if (NULL == pRSock)
			return ETYPE;

		const SOCKET	rsock=pRSock->GetSocket();
		if (BAD_SOCKET == rsock)
			return ENOTCONFIGURED;

		FD_SET(rsock, &readSet);
		if (rsock > maxSock)
			maxSock = rsock;
	}

	int	nRdy=select(maxSock, &readSet, NULL, NULL, &timeoutVal);
	if (0 == nRdy) /* time limit expired */
		return ETIME;
	if (SOCKET_ERROR == nRdy)
		return ETRANSMISSION;

	for (ulSdx=0; ulSdx < ulNumSocks; ulSdx++)
	{
		const CBuffSock	*pDSock=socks[ulSdx];
		if (NULL == pDSock)
			return ECONTEXT;

		const SOCKET	dsock=pDSock->GetSocket();
		if (FD_ISSET(dsock, &readSet))
			return EOK;
	}

	// if reached this point something is wrong since some socket MUST have available data
	return ESTATE;
}

/*-------------------------------------------------------------------------*/

// assumes last one is NULL
EXC_TYPE WaitOnMultipleBuffSocks (const CBuffSock	*socks[],
											 const UINT32		ulWaitTimeout,
											 UINT32&				ulSdx)
{
	ulSdx = 0;

	if (NULL == socks)
		return EOK;

	for (UINT32	ulNumSocks=0; ; ulNumSocks++)
	{
		if (NULL == socks[ulNumSocks])
			return WaitOnMultipleBuffSocks(socks, ulNumSocks, ulWaitTimeout, ulSdx);
	}

	return EFATALEXIT;
}

/*-------------------------------------------------------------------------*/
