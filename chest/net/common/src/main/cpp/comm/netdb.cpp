#include <string.h>
#include <ctype.h>

/*--------------------------------------------------------------------------*/

#include <_types.h>
#include <util/errors.h>
#include <util/string.h>
#include <internet/general.h>
#include <comm/socket.h>
#include <comm/netdb.h>

/*--------------------------------------------------------------------------*/

#ifndef _REENTRANT

/*--------------------------------------------------------------------------*/

static struct hostent *insert_haddr (struct hostent *hp,
												 const int		 addr,
												 char				 **dp2p,
												 int				 *rlen_p)
{
	char *dp=*dp2p;
	int	 rlen=0;

	if (hp == NULL)
		return(NULL);
	if (dp == NULL)
		return(NULL);
	if (rlen_p == NULL)
		return(NULL);

	/* assume no aliases */
	hp->h_aliases = NULL;
	hp->h_addrtype = AF_INET;
	hp->h_length = (sizeof addr);

	rlen = *rlen_p;

	/* not enough space to hold the address, a pointer, and a NULL pointer */
	if (rlen <= ((sizeof addr) + 2 * sizeof(char *)))
		return(NULL);
	hp->h_addr_list = (char **) dp;

	dp += (2 * sizeof(char *));
	rlen -= (2 * sizeof(char *));
	hp->h_addr_list[0] = dp;
	hp->h_addr_list[1] = NULL;

	memcpy((void *) dp, (const void *) &addr, (sizeof addr));
	dp += (sizeof addr);
	rlen -= (sizeof addr);

	*dp2p = dp;
	*rlen_p = rlen;
	return(hp);
}

/*--------------------------------------------------------------------------*/

static EXC_TYPE add_string (char			**ppszField,
									 const char	pszValue[],
									 char			**ppszNext,
									 int			*prLen)
{
	char	*dp=NULL;
	int	rlen=0, clen=0;

	if ((NULL == ppszField) || (NULL == pszValue) || (NULL == ppszNext) || (NULL == prLen))
		return EPARAM;

	if (NULL == (dp=*ppszNext))
		return EINVAL;
	rlen = *prLen;

	if ((clen=strlen(pszValue)) >= rlen)
		return EOVERFLOW;
	strcpy(dp, pszValue);

	*ppszField = dp;
	*prLen = (rlen - clen - 1);
	*ppszNext = (dp + clen + 1);

	return OK;
}

/*--------------------------------------------------------------------------*/

struct hostent *gethostbyname_r (const char		*hostname,
											struct hostent *hp,
											char				*hostdata,
											int				hlen,
											int				*hperrno)
{
	int						rlen=hlen;
	const struct in_addr *iaP=NULL;
	char						*dp=hostdata;
	size_t					l=0;
	const struct hostent	*thp=hp;

	*hperrno = ERROR; /* assume nothing OK */

	/* no hostname supplied */
	if (hostname == NULL)
		return(NULL);

	/* no structure for return values supplied */
	if (hp == NULL)
		return(NULL);
	memset(hp, 0, (sizeof *hp));

	/* no buffer for additional information supplied */
	if (hostdata == NULL)
		return(NULL);
	memset(hostdata, 0, hlen);

	if (NULL == (thp=gethostbyname(hostname)))
		return NULL;

	iaP = (const struct in_addr *) thp->h_addr;
	/* make sure correct address is entered */
	if ((0 == iaP->s_addr) || (((unsigned long) 0xffffffff) == (unsigned long) iaP->s_addr))
		return NULL;

	if (insert_haddr(hp, (int) iaP->s_addr, &dp, &rlen) == NULL)
		return(NULL);

	hp->h_addrtype = thp->h_addrtype;
	hp->h_length = thp->h_length;

	if (thp->h_name != NULL)
	{
		if ((l=strlen(thp->h_name)) >= (size_t) rlen)
			return(NULL);

		hp->h_name = dp;
		strcpy(hp->h_name, thp->h_name);
	}
	else
	{
		if ((l=strlen(hostname)) >= (size_t) rlen)
			return(NULL);

		/* copy hostname to returned structure - assume this is official name */
		hp->h_name = dp;
		strcpy(hp->h_name, hostname);
	}

	dp += (l+1);
	rlen -= (l+1);

	*hperrno = OK;
	return(hp);
}

/*--------------------------------------------------------------------------*/

struct hostent *gethostbyaddr_r (const char		*haddr,
											int				len,
											int				type,
											struct hostent *hp,
											char				*buf,
											int				buf_len,
											int				*hperrno)
{
	int				  addr=ERROR, rlen=buf_len;
	char			  *dp=buf;
	struct hostent *thp=hp;

	*hperrno = ERROR; /* assume nothing OK */

	/* no address supplied */
	if (haddr == NULL)
		return(NULL);

	/* address length does not match supported type */
	if (len != (sizeof addr))
		return(NULL);

	/* only Internet addresses supported */
	if (type != AF_INET)
		return(NULL);

	/* no return structure supplied */
	if (hp == NULL)
		return(NULL);
	memset(hp, 0, (sizeof *hp));

	/* no buffer for addition values supplied */
	if (buf == NULL)
		return(NULL);

	memcpy((void *) &addr, (const void *) haddr, (sizeof addr));
	if ((thp=insert_haddr(hp, addr, &dp, &rlen)) == NULL)
		return(NULL);

	/* Unable to get host name from address */
	if (NULL == (thp=gethostbyaddr(haddr, len, type)))
		return(NULL);

	hp->h_addrtype = thp->h_addrtype;
	hp->h_length = thp->h_length;
	hp->h_name = dp;
	hp->h_name[0] = '\0';

	if (thp->h_name != NULL)
	{
		/* not enough space to hold host name */
		if ((size_t) rlen <= strlen(thp->h_name))
			return(NULL);
		strcpy(hp->h_name, thp->h_name);
	}

	*hperrno = OK;
	return(hp);
}

/*--------------------------------------------------------------------------*/

struct servent *getservbyname_r (const char		*name,
											const char		*proto,
											struct servent *srvrecp,
											char				*buf,
											int				buflen)
{
	struct servent	*pSrvc=NULL;
	int	rlen=buflen;
	char	*dp=buf;

	if ((name == NULL) ||	(buf == NULL) || (srvrecp == NULL))
		return(NULL);

	if (buflen <= 0)
		return(NULL);

	memset(srvrecp, 0, (sizeof *srvrecp));
	if (NULL == (pSrvc=getservbyname(name, proto)))
		return NULL;

	srvrecp->s_port = pSrvc->s_port;

	if (add_string(&(srvrecp->s_name), pSrvc->s_name, &dp, &rlen) != EOK)
		return NULL;
	if (add_string(&(srvrecp->s_proto), pSrvc->s_proto, &dp, &rlen) != EOK)
		return NULL;
  
	return srvrecp;
}

/*--------------------------------------------------------------------------*/

#endif	/* of ifndef _REENTRANT */

/*--------------------------------------------------------------------------*/

#define HOST_DATA_LEN	128

EXC_TYPE get_host_address (const char hostname[], struct in_addr *addr)
{
	int				hperrno=EOK;
	char				hostdata[HOST_DATA_LEN];
	struct hostent hostrec,	*hp=&hostrec;

	if (IsEmptyStr(hostname) || (NULL == addr))
		return EPARAM;

	/*
	 *	If name starts with a digit assume it is a dot notation
	 */

	if (isdigit(hostname[0]))
	{
		if ((long) (addr->s_addr=inet_dot_str2address(hostname)) == (-1))
			return EPATHNAMESYNTAX;
		else
			return EOK;
	}

	hp = gethostbyname_r(hostname, &hostrec, hostdata, HOST_DATA_LEN, &hperrno);

	if (hp == NULL)
		return EEXIST;

	if (NULL == hp->h_addr_list)
		return EEMPTYENTRY;

	memcpy((void *) &(addr->s_addr),(const void *) hp->h_addr_list[0],(sizeof addr->s_addr));
	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE get_host_address_string (const char		hostname[],
											 char				ha_string[],
											 const size_t	ha_len)
{
	struct in_addr IAddr;
	const char		*pszAddr=NULL;

	if (IsEmptyStr(hostname) || (NULL == ha_string) || (0 == ha_len))
		return EPARAM;

	if (isdigit(hostname[0]))
	{
		if ((long) (IAddr.s_addr=inet_dot_str2address(hostname)) == (-1))
			return EINVALIDNUMERIC;
	}
	else
	{
		EXC_TYPE exc=get_host_address(hostname, &IAddr);

		if (exc != EOK)
			return exc;
	}

	pszAddr = inet_ntoa(IAddr);
	if ((NULL == pszAddr) || (strlen(pszAddr) >= ha_len))
		return EMEM;

	strcpy(ha_string, pszAddr);
	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE get_host_name (char hostname[], const size_t hlen,
								const struct in_addr *iaddr)
{
	struct hostent hent, *hp=&hent;
	char				buf[HOST_DATA_LEN];
	int				herr=EOK;

	if ((NULL == hostname) || (NULL == iaddr) || (0 == hlen))
		return EPARAM;

	hostname[0] = '\0';
	hp = gethostbyaddr_r((const char *) &(iaddr->s_addr),
								(sizeof iaddr->s_addr),
								AF_INET,
								&hent,
								buf,
								(sizeof buf),
								&herr);
	if (hp == NULL)
		return EEXIST;

	if (hp->h_name == NULL)
		return EEMPTYENTRY;

	if (strlen(hp->h_name) >= hlen)
		return EMEM;

	strcpy(hostname, hp->h_name);
	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE inet_ntoa_r (const struct in_addr& a, LPTSTR lpszAddr, const UINT32 ulMaxLen)
{
	if ((NULL == lpszAddr) || (0 == ulMaxLen))
		return EPARAM;
	*lpszAddr = _T('\0');

	CStrlBuilder	strb(lpszAddr, ulMaxLen);
	EXC_TYPE			exc=strb.AddNum((UINT32) a.S_un.S_un_b.s_b1 & 0x00FF);
	if (exc != EOK)
		return exc;
	if ((exc=strb.AddChar(_T('.'))) != EOK)
		return exc;

	if ((exc=strb.AddNum((UINT32) a.S_un.S_un_b.s_b2 & 0x00FF)) != EOK)
		return exc;
	if ((exc=strb.AddChar(_T('.'))) != EOK)
		return exc;

	if ((exc=strb.AddNum((UINT32) a.S_un.S_un_b.s_b3 & 0x00FF)) != EOK)
		return exc;
	if ((exc=strb.AddChar(_T('.'))) != EOK)
		return exc;

	if ((exc=strb.AddNum((UINT32) a.S_un.S_un_b.s_b4 & 0x00FF)) != EOK)
		return exc;

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE GetPeerAddressDetails (const SOCKET sock, LPTSTR lpszPeerAddr /* may be NULL */, const UINT32 ulMaxLen, int *pPort /* may be NULL */)
{
	union {
		struct sockaddr		a;
		struct sockaddr_in	i;
		BYTE						dummyBuf[MAX_PATH];
	} dtUnn;
	int		nLen=(sizeof dtUnn);
	EXC_TYPE	exc=EOK;

	if ((lpszPeerAddr != NULL) && (ulMaxLen > 0))
		*lpszPeerAddr = _T('\0');
	if (pPort != NULL)
		*pPort = 0;
	if (BAD_SOCKET == sock)
		return EPARAM;

	memset(&dtUnn, 0, (sizeof dtUnn));

	if ((exc=getpeername(sock, &dtUnn.a, &nLen)) != EOK)
		return EUNKNOWNEXIT;

	if ((lpszPeerAddr != NULL) && (ulMaxLen > 0))
	{
		if ((exc=inet_ntoa_r(dtUnn.i.sin_addr, lpszPeerAddr, ulMaxLen)) != EOK)
			return exc;
	}

	if (pPort != NULL)
		*pPort = (int) ntohs(dtUnn.i.sin_port);

	return EOK;
}

EXC_TYPE GetPeerAddressPort (const SOCKET sock, int *pPort)
{
	if (NULL == pPort)
		return EPARAM;
	else
		return GetPeerAddressDetails(sock, NULL, 0, pPort);
}

EXC_TYPE GetPeerAddressStr (const SOCKET sock, LPTSTR lpszPeerAddr, const UINT32 ulMaxLen)
{

	if ((NULL == lpszPeerAddr) || (0 == ulMaxLen))
		return EPARAM;
	else
		return GetPeerAddressDetails(sock, lpszPeerAddr, ulMaxLen, NULL);
}

#ifdef __cplusplus

//////////////////////////////////////////////////////////////////////////////

EXC_TYPE CNetServersCache::Reset ()
{
	EXC_TYPE	exc=m_srvCache.Reset();
	m_ulCurSrvr = 0;
	return exc;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CNetServersCache::Clear ()
{
	EXC_TYPE	exc=m_srvCache.Clear();
	m_ulCurSrvr = 0;
	return exc;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CNetServersCache::AddServer (LPCTSTR lpszHostName /* ignored */, LPCTSTR lpszHostAddr)
{
	if (IsEmptyStr(lpszHostAddr))
		return EPARAM;

	if ((long) inet_dot_str2address(lpszHostAddr) == (-1))
		return EINVALIDNUMERIC;

	return m_srvCache.AddItem((LPVOID) lpszHostAddr, ((GetSafeStrlen(lpszHostAddr)+1) * sizeof(TCHAR)));
}

/*--------------------------------------------------------------------------*/

// list may contain either names (in which case they are resolved) or IP addresses
//
// Note: cache is not checked for duplicate entries - this enables "weighted" round robin
EXC_TYPE CNetServersCache::AddServers (LPCTSTR lpszSrvList, const BOOLEAN fResolveNames)
{
	LPCTSTR	lpszSrv=lpszSrvList;
	while (!IsEmptyStr(lpszSrv))
	{
		LPCTSTR	lpszNext=_tcschr(lpszSrv, NETSRVRCACHESEP);
		if (NULL == lpszNext)
			lpszNext = strlast(lpszSrv);

		UINT32	ulSNLen=(lpszNext - lpszSrv);
		TCHAR		tch=*lpszNext;
		if (tch != _T('\0'))
			*((LPTSTR) lpszNext) = _T('\0');

		EXC_TYPE	exc=EOK;
		if (fResolveNames)
		{
			TCHAR	szAddr[MAX_DNS_DOMAIN_LEN+2]=_T("");
			exc = get_host_address_string(lpszSrv, szAddr, MAX_DNS_DOMAIN_LEN);
			if (tch != _T('\0'))
				*((LPTSTR) lpszNext) = tch;
			if (exc != EOK)
				return exc;

			ulSNLen = _tcslen(szAddr);
			if ((exc=AddServer(lpszSrv, szAddr)) != EOK)
				return exc;
		}
		else	// no need to resolve name
		{
			exc = m_srvCache.AddItem((LPVOID) lpszSrv, ((ulSNLen+1) * sizeof(TCHAR)));
			if (tch != _T('\0'))
				*((LPTSTR) lpszNext) = tch;
			if (exc != EOK)
				return exc;
		}

		if (tch != _T('\0'))
			lpszNext++;
		lpszSrv = lpszNext;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

typedef struct {
	LPCNETSRVRSCACHE	pNSC;
	BOOLEAN				fResolveNames;
} ACSARGS, *LPACSARGS;

static EXC_TYPE adsCfn (LPVOID pItem, LPVOID pArg, BOOLEAN& fContEnum)
{
	if (NULL == pArg)
		return ECONTEXT;

	fContEnum = TRUE;

	ACSARGS&	acsa=*((LPACSARGS) pArg);
	LPCNETSRVRSCACHE	pNSC=acsa.pNSC;
	if (NULL == pNSC)
		return ESTATE;

	return pNSC->AddServers((LPCTSTR) pItem, acsa.fResolveNames);
}

EXC_TYPE CNetServersCache::AddServers (const CNetServersCache& nsc, const BOOLEAN fResolveNames)
{
	ACSARGS	acsa={ this, fResolveNames };
	return nsc.EnumServers(adsCfn, (LPVOID) &acsa);
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CNetServersCache::GetNextServer (LPCTSTR& lpszSrvr)
{
	lpszSrvr = NULL;

	UINT32	ulMaxNum=GetCacheSize();
	if (0 == ulMaxNum)
		return EEMPTYENTRY;
	ulMaxNum--;

	if (m_ulCurSrvr >= ulMaxNum)
		m_ulCurSrvr = 0;
	else
		m_ulCurSrvr++;

	LPVOID	pV=NULL;
	EXC_TYPE	exc=m_srvCache.GetData(m_ulCurSrvr, pV);
	if (EOK == exc)
	{
		lpszSrvr = (LPCTSTR) pV;
		if (IsEmptyStr(lpszSrvr))
			exc = ESTATE;
	}

	return exc;
}

/*--------------------------------------------------------------------------*/

typedef struct {
	LPCTSTR	lpszServer;
	BOOLEAN	fFound;
} FSARGS;

static EXC_TYPE fsCfn (LPVOID	pItem, LPVOID pArg, BOOLEAN& fContEnum)
{
	if (NULL == pArg)
		return ECONTEXT;
	FSARGS&	fsa=*((FSARGS *) pArg);

	if (IsEmptyStr(fsa.lpszServer) || fsa.fFound)
		return ECONTEXT;

	LPCTSTR	lpszServer=(LPCTSTR) pItem;
	if (IsEmptyStr(lpszServer))
		return ESTATE;

	fsa.fFound = (0 == _tcsicmp(fsa.lpszServer, lpszServer));
	fContEnum = (!fsa.fFound);

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CNetServersCache::FindServer (LPCTSTR lpszServer, const BOOLEAN fResolveNames) const
{
	EXC_TYPE	exc=EOK;

	FSARGS	fsa={ lpszServer, FALSE };
	TCHAR		szAddr[MAX_DNS_DOMAIN_LEN+2]=_T("");
	if (fResolveNames)
	{
		if ((exc=get_host_address_string(lpszServer, szAddr, MAX_DNS_DOMAIN_LEN)) != EOK)
			return exc;

		fsa.lpszServer = szAddr;
	}

	if ((exc=EnumServers(fsCfn, (LPVOID) &fsa)) != EOK)
		return exc;

	if (!fsa.fFound)
		return EFNEXIST;

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

// Note: cannot be re-initialized (e.g. if already set through constructor)
EXC_TYPE CDNSServersCache::InitCache (const UINT32 ulMaxSrvrs, const UINT32 ulGrowNum)
{
	EXC_TYPE	exc=CNetServersCache::InitCache(ulMaxSrvrs, ulGrowNum);
	if (exc != EOK)
		return exc;

	if ((exc=m_hostsMap.InitMap((ulMaxSrvrs + ulGrowNum), FALSE)) != EOK)
		return exc;

	return EOK;
}

/*--------------------------------------------------------------------------*/

// Note: returns error if duplicate entry
EXC_TYPE CDNSServersCache::AddServer (LPCTSTR lpszHostName, LPCTSTR lpszHostAddr)
{
	if (IsEmptyStr(lpszHostName) || IsEmptyStr(lpszHostAddr))
		return EPARAM;

	// make sure this is an IP address
	if ((long) inet_dot_str2address(lpszHostAddr) == (-1))
		return EINVALIDNUMERIC;

	LPVOID	pIndex=NULL;
	EXC_TYPE	exc=m_hostsMap.FindKey(lpszHostName, pIndex);
	if (EOK == exc)
		return EEXIST;

	pIndex = (LPVOID) m_srvCache.GetSize();
	if ((exc=m_srvCache.AddItem((LPVOID) lpszHostAddr, (GetSafeStrlen(lpszHostAddr) + 1) * sizeof(TCHAR))) != EOK)
		return exc;

	if ((exc=m_hostsMap.AddKey(lpszHostName, pIndex)) != EOK)
	{
		m_srvCache.RemoveItem((UINT32) pIndex);
		return exc;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

// Note: duplicate entries are skipped
EXC_TYPE CDNSServersCache::AddServers (LPCTSTR lpszSrvList, const BOOLEAN fResolveNames /* ignored */)
{
	return CNetServersCache::AddServers(lpszSrvList, TRUE);
}

/*--------------------------------------------------------------------------*/

// Note: duplicate entries are skipped
EXC_TYPE CDNSServersCache::AddServers (const CNetServersCache& nsc, const BOOLEAN fResolveNames /* ignored */)
{
	const CVSDCollection&	dsca=nsc.GetServers();
	for (UINT32	ulIndex=0, ulSize=dsca.GetSize(); ulIndex < ulSize; ulIndex++)
	{
		LPCTSTR	lpszItem=(LPCTSTR) dsca[ulIndex];
		EXC_TYPE	exc=AddServer(lpszItem, lpszItem);
		if (exc != EOK)
		{
			// skip existing entries
			if (EEXIST == exc)
				continue;
		}
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

// if not known, then added
EXC_TYPE CDNSServersCache::Resolve (LPCTSTR lpszHostName, LPCTSTR& lpszHostAddr, const BOOLEAN fAutoAdd)
{
	lpszHostAddr = NULL;

	if (IsEmptyStr(lpszHostName))
		return EPARAM;

	LPVOID	pIndex=NULL;
	EXC_TYPE	exc=m_hostsMap.FindKey(lpszHostName, pIndex);
	if (exc != EOK)
	{
		if (!fAutoAdd)
			return exc;

		TCHAR	szHostAddr[MAX_DNS_DOMAIN_LEN+2]=_T("");
		if ((exc=::get_host_address_string(lpszHostName, szHostAddr, MAX_DNS_DOMAIN_LEN)) != EOK)
			return exc;
		if ((exc=AddServer(lpszHostName, szHostAddr)) != EOK)
			return exc;
		// re-find it - it MUST be there
		if ((exc=m_hostsMap.FindKey(lpszHostName, pIndex)) != EOK)
			return exc;
	}

	lpszHostAddr = (LPCTSTR) m_srvCache[(UINT32) pIndex];
	if (IsEmptyStr(lpszHostAddr))
		return ESTATE;
	else
		return EOK;
}

/*--------------------------------------------------------------------------*/

// Note: duplicate entries are skipped
EXC_TYPE CDNSServersCache::Merge (const CDNSServersCache& dsc)
{
	CStr2PtrMapEnum			dsme(dsc.m_hostsMap);
	const CVSDCollection&	dsca=dsc.m_srvCache;
	LPCTSTR						lpszHostName=NULL;
	LPVOID						pIndex=NULL;

	for (EXC_TYPE	exc=dsme.GetFirst(lpszHostName, pIndex); EOK == exc; exc=dsme.GetNext(lpszHostName, pIndex))
	{
		LPVOID	pPrev=NULL;
		if (EOK == (exc=m_hostsMap.FindKey(lpszHostName, pPrev)))
			continue;	// skip duplicate entries

		if ((exc=AddServer(lpszHostName, (LPCTSTR) dsca[(UINT32) pIndex])) != EOK)
			return exc;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CDNSServersCache::Reset ()
{
	EXC_TYPE	exc=CNetServersCache::Reset();
	if (exc != EOK)
		return exc;

	m_hostsMap.Reset();
	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CDNSServersCache::Clear ()
{
	EXC_TYPE	exc=CNetServersCache::Clear();
	if (exc != EOK)
		return exc;

	m_hostsMap.Clear();
	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE CDNSServersCacheEnum::GetValue (LPCTSTR& lpszHostName, LPCTSTR& lpszHostAddr, const BOOLEAN fIsFirst)
{
	LPVOID	pIndex=NULL;
	EXC_TYPE	exc=(fIsFirst ? m_mpe.GetFirst(lpszHostName, pIndex) : m_mpe.GetNext(lpszHostName, pIndex));
	if (exc != EOK)
		return exc;

	lpszHostAddr = (LPCTSTR) m_adc[(UINT32) pIndex];
	if (IsEmptyStr(lpszHostAddr))
		return ESTATE;

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

#endif	/* of ifdef __cplusplus */

/*--------------------------------------------------------------------------*/

EXC_TYPE DecodeIPAddressSpecPairChars (LPCTSTR			lpszAddrPair,
													const UINT32	ulAPLen,
													UINT32			*pulBaseAddr,
													UINT32			*pulNetmask)
{
	LPCTSTR	lpszAPMask=NULL, lpszNetmask=NULL;
	TCHAR		chESave=_T('\0');

	if (IsEmptyStr(lpszAddrPair) || (0 == ulAPLen) ||
		 (NULL == pulBaseAddr) || (NULL == pulNetmask))
		return EPARAM;

	*pulBaseAddr = 0;
	*pulNetmask = 0;

	if (NULL != (lpszAPMask=_tcsnchr(lpszAddrPair, IPADDRPAIRNETMASKDELIM, ulAPLen)))
		lpszNetmask = (lpszAPMask + 1);
	else
		lpszAPMask = (lpszAddrPair + ulAPLen);

	chESave = *lpszAPMask;
	*((LPTSTR) lpszAPMask) = _T('\0');
	*pulBaseAddr=inet_addr(lpszAddrPair);
	*((LPTSTR) lpszAPMask) = chESave;

	if (INADDR_NONE == *pulBaseAddr)
		return EBADADDR;

	if (lpszNetmask != NULL)
	{
		UINT32	ulBALen=(lpszNetmask - lpszAddrPair), ulNMLen=(ulAPLen - ulBALen);
		EXC_TYPE	exc=EOK;
		UINT32	ulBitsNum=argument_to_dword(lpszNetmask, ulNMLen, EXC_ARG(exc));

		/* if non-EOK code returned, then this must be a full netmask */
		if (exc != EOK)
		{
			LPCTSTR	lpszNMEnd=(lpszNetmask + ulNMLen);

			chESave = *lpszNMEnd;
			*((LPTSTR) lpszNMEnd) = _T('\0');
			*pulNetmask = inet_addr(lpszNetmask);
			*((LPTSTR) lpszNMEnd) = chESave;

			if (INADDR_NONE == *pulNetmask)
				return ECONTEXT;
		}
		else
		{
			if (ulBitsNum >= INT32_BITS_NUM)
				return EOVERFLOW;

			if (ulBitsNum != 0)
				*pulNetmask = htonl(0xffffffff << (INT32_BITS_NUM - ulBitsNum));
		}
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

/* Note: pairs list may be empty/NULL, delimiter may NOT be EOS or IPADDRPAIRNETMASKDELIM */
EXC_TYPE EnumIPAddressSpecPairs (LPCTSTR					lpszPairsList,
											const TCHAR				chSepDelim,
											IPADDRPAIR_ENUM_CFN	lpfnEcfn,
											LPVOID					pArg)
{
	UINT32	ulPairNdx=0;
	LPCTSTR	lpszCurPos=lpszPairsList;
	BOOLEAN	fContEnum=TRUE;

	if ((NULL == lpfnEcfn) || (_T('\0') == chSepDelim) ||
		 (IPADDRPAIRNETMASKDELIM == chSepDelim) || (_T('.') == chSepDelim))
		return EPARAM;

	if (IsEmptyStr(lpszPairsList))
		return EOK;

	for (ulPairNdx=0; fContEnum; ulPairNdx++)
	{
		EXC_TYPE	exc=EOK;
		LPCTSTR	lpszAPStart=lpszCurPos;
		UINT32	ulAPLen=0, ulBaseAddr=0, ulNetmask=0;

		for ( ; _istspace(*lpszAPStart) && (*lpszAPStart != _T('\0')); lpszAPStart++);
		if (_T('\0') == *lpszAPStart)
			break;

		for (lpszCurPos=lpszAPStart; (*lpszCurPos != chSepDelim) && (*lpszCurPos != _T('\0')) && (!_istspace(*lpszCurPos)); lpszCurPos++);

		ulAPLen = (lpszCurPos - lpszAPStart);

		/* if not reached EOS or separator, then search for them */
		if (*lpszCurPos != chSepDelim)
			for ( ; (*lpszCurPos != chSepDelim) && (*lpszCurPos != _T('\0')); lpszCurPos++);

		if ((exc=DecodeIPAddressSpecPairChars(lpszAPStart, ulAPLen, &ulBaseAddr, &ulNetmask)) != EOK)
			return exc;

		if ((exc=(*lpfnEcfn)(ulPairNdx, ulBaseAddr, ulNetmask, pArg, &fContEnum)) != EOK)
			return exc;

		if (_T('\0') == *lpszCurPos)
			break;

		if (chSepDelim == *lpszCurPos)
			lpszCurPos++;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/
