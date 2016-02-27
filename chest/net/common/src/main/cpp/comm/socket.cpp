#include <string.h>
#include <ctype.h>

#include <_types.h>
#include <util/string.h>
#include <util/errors.h>

#include <comm/socket.h>
#include <comm/netdb.h>

/*---------------------------------------------------------------------------*/

static EXC_TYPE setup_socket_options (SOCKET sock, const BOOLEAN fIsServer)
{
	int				on_op=1;
	struct linger	l;
	EXC_TYPE			exc=EOK;

	if (sock == BAD_SOCKET)
		return(EINVAL);

	if (!fIsServer)
	{
		if (setsockopt(sock, SOL_SOCKET,SO_REUSEADDR,(char *) &on_op,(sizeof on_op)) < 0)
			exc = (EXC_TYPE) ENOTCONFIGURED;
	}

	/*
	 *		Turn on the "keep alive" mechanism - periodic checks that connection
	 * still alive.
	 */

	if (setsockopt(sock,SOL_SOCKET,SO_KEEPALIVE,(char *) &on_op,(sizeof on_op)) < 0)
		exc = (EXC_TYPE) ENOTCONFIGURED;

	/*
	 * Do not linger if pending messages on "close()" - terminate quickly.
	 */

	l.l_onoff = 0;
	l.l_linger = 0;
	if (setsockopt(sock, SOL_SOCKET, SO_LINGER,( char *) &l, (sizeof l)) < 0)
		exc = (EXC_TYPE) ENOTCONFIGURED;

	return exc;
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE sockcreate (SOCKET			*sock,
									 const int		sock_domain,
									 const int		sock_type,
									 const int		sock_prot,
									 const BOOLEAN	fIsServer)
{
	EXC_TYPE exc=EOK;

	if (NULL == sock)
		return EPARAM;

	if ((long) (*sock=socket(sock_domain, sock_type, sock_prot)) <= 0)
	{
		*sock = BAD_SOCKET;
		return((EXC_TYPE) ETRANSMISSION);
	}

	exc = setup_socket_options(*sock, fIsServer);
	if (exc != EOK)
	{
		sockClose(*sock);
		*sock = BAD_SOCKET;
		return(exc);
	}

	return(EOK);
}

/*---------------------------------------------------------------------------*/

/* creates a socket ready for serving (i.e. ready to 'accept') */
EXC_TYPE sock_server_setup (SOCKET *sock, const int port_num)
{
	struct sockaddr		addr;
	struct sockaddr_in	*addr_p=(struct sockaddr_in *) &addr;
	EXC_TYPE					exc=EOK;
	SOCKET					asock=BAD_SOCKET;

	if ((port_num <= 0) || (NULL == sock))
		return(EINVAL);
	*sock = BAD_SOCKET;

	if ((exc=sockcreate(&asock, AF_INET, SOCK_STREAM, IPPROTO_TCP, TRUE)) != EOK)
		return(exc);

	addr_p->sin_family = AF_INET;
	addr_p->sin_port = htons((u_short) port_num);
	addr_p->sin_addr.s_addr = htonl(INADDR_ANY);

	if (bind(asock, &addr, (sizeof *addr_p)) < 0)
	{
		closesocket(asock);
		return((EXC_TYPE) ECONTEXT);
	}

	if (listen(asock, SOMAXCONN) < 0)
	{
		closesocket(asock);
		return((EXC_TYPE) EINTERRUPTSATURATION);
	}

	*sock = asock;
	return(EOK);
}

/*---------------------------------------------------------------------------*/

/*		Creates a (STREAM) socket connected to the specified host on the
 * specified port
 */
EXC_TYPE sock_connect (SOCKET			*sock,
							  const char	hostname[],
							  const int		port_num)
{
	struct sockaddr		addr;
	struct sockaddr_in	*addr_p=(struct sockaddr_in *) &addr;
	EXC_TYPE					exc=EOK;
	SOCKET					csock=BAD_SOCKET;

	if ((NULL == sock) || (NULL == hostname) || (port_num <= 0))
		return EPARAM;
	*sock = BAD_SOCKET;

	memset(&addr, 0, (sizeof addr));
	addr_p->sin_family = AF_INET;
	addr_p->sin_port = htons((u_short) port_num);

	if ((exc=get_host_address(hostname, &(addr_p->sin_addr))) != EOK)
		return exc;

	if ((exc=sockcreate(&csock, AF_INET, SOCK_STREAM, IPPROTO_TCP, FALSE)) != EOK)
		return exc;

	if ((exc=connect(csock, &addr, (sizeof *addr_p))) != EOK)
	{
		sockClose(csock);
		return ENOTCONNECTION;
	}

	*sock = csock;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE sockWaitMultiple (SOCKET			socks[],
									const UINT32	ulNumSocks,
									const UINT32	ulMaxSecs,
									UINT32			*pulSockNdx)
{
	UINT32	ulSdx=0;
	fd_set	readSet;
	SOCKET	maxSock=0;
	int		nRdy=0;

	struct timeval timeoutVal={ (SINT32) ulMaxSecs, 0 };
	const struct timeval *ptm=((0 == ulMaxSecs) ? NULL : &timeoutVal);

	if ((NULL == socks) || (0 == ulNumSocks) || (NULL == pulSockNdx))
		return EPARAM;
	*pulSockNdx = ulNumSocks;

	FD_ZERO(&readSet);
	for (ulSdx=0; ulSdx < ulNumSocks; ulSdx++)
	{
		SOCKET	rsock=socks[ulSdx];
		if (BAD_SOCKET == rsock)
			return ENOTCONFIGURED;

		FD_SET(rsock, &readSet);
		if (rsock > maxSock)
			maxSock = rsock;
	}

	/* check if time limit expired */
	if (0 == (nRdy=select(maxSock, &readSet, NULL, NULL, ptm)))
		return ETIME;
	if (SOCKET_ERROR == nRdy)
		return ETRANSMISSION;

	for (ulSdx=0; ulSdx < ulNumSocks; ulSdx++)
	{
		SOCKET	dsock=socks[ulSdx];
		if (BAD_SOCKET == dsock)
			return ECONTEXT;

		if (FD_ISSET(dsock, &readSet))
		{
			*pulSockNdx = ulSdx;
			return EOK;
		}
	}

	/* if reached this point something is wrong since some socket MUST have available data */
	return ESTATE;
}
	
/* if maxSecs == 0 then wait is infinite */
EXC_TYPE sockWait (SOCKET sock, const SINT32 ulMaxSecs)
{
	SOCKET	socks[2]={ sock, BAD_SOCKET };
	UINT32	ulSockNdx=(UINT32) (-1);

	return sockWaitMultiple(socks, 1, ulMaxSecs, &ulSockNdx);
}

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
int ISockioInterface::Fill (char buf[], const size_t bufLen, const SINT32 maxSecs)
{
	char		*bp=buf;
	size_t	remLen=bufLen;

	while (remLen > 0)
	{
		int	rLen=Read(bp, remLen, maxSecs);
		if (rLen < 0)
			return rLen;

		bp += rLen;
		remLen -= rLen;
	}

	*bp = '\0';
	return bufLen;
}

/*--------------------------------------------------------------------------*/

static UINT32 FindSocket (const SOCKET fsock, const SOCKET ssocks[], const UINT32 uLen)
{
	if ((BAD_SOCKET == fsock) || (NULL == ssocks))
		return uLen;

	for (UINT32 i=0; i < uLen; i++)
		if (fsock == ssocks[i])
			return i;

	return uLen;
}

/*---------------------------------------------------------------------------*/

// Note: only up to available size is added (otherwise EOVERFLOW)
EXC_TYPE CSockSet::AddSocket (SOCKET sock)
{
	if (BAD_SOCKET == sock)
		return EPARAM;

	if (FindSocket(sock, m_Socks, m_ulSockCount) < m_ulSockCount)
		return EEXIST;

	if (m_ulSockCount >= FD_SETSIZE)
		return EOVERFLOW;

	m_Socks[m_ulSockCount] = sock;
	m_ulSockCount++;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CSockSet::AddSet (const CSockSet& cs)
{
	for (UINT32 i=0; i < cs.m_ulSockCount; i++)
	{
		EXC_TYPE exc=AddSocket(cs.m_Socks[i]);
		if (exc != EOK)
			return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

// Note: returns EEXIST if not found
EXC_TYPE CSockSet::DelSocket (SOCKET sock)
{
	if (BAD_SOCKET == sock)
		return EPARAM;

	UINT32 ulSockIdx=FindSocket(sock, m_Socks, m_ulSockCount);
	if (ulSockIdx >= m_ulSockCount)
		return EEXIST;

	// shift "down" instead of deleted member
	for (UINT32 ulNIdx=(ulSockIdx+1) ; ulNIdx < m_ulSockCount; ulSockIdx++, ulNIdx++)
		m_Socks[ulNIdx] = m_Socks[ulSockIdx];

	m_Socks[m_ulSockCount] = BAD_SOCKET;
	m_ulSockCount--;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CSockSet::DelSet (const CSockSet& cs)
{
	for (UINT32 i=0; i < cs.m_ulSockCount; i++)
	{
		EXC_TYPE exc=DelSocket(cs.m_Socks[i]);
		if (exc != EOK)
			return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CSockSet::SetSocket (SOCKET sock)
{
	if (BAD_SOCKET == sock)
		return EPARAM;

	m_ulSockCount = 0;
	return AddSocket(sock);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CSockSet::SetSet (const CSockSet& cs)
{
	m_ulSockCount = cs.m_ulSockCount;

	for (UINT32 i=0; i < cs.m_ulSockCount; i++)
		m_Socks[i] = cs.m_Socks[i];

	return EOK;
}

/*---------------------------------------------------------------------------*/

#endif	/* of ifdef __cplusplus */