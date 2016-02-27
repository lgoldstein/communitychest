#ifndef _COMM_NETDB_H_
#define _COMM_NETDB_H_

#include <util/string.h>
#include <util/memory.h>

/*---------------------------------------------------------------------------*/

extern EXC_TYPE get_host_address (const char hostname[], struct in_addr *addr);
extern EXC_TYPE get_host_address_string (const char	hostname[],
													  char			ha_string[],
													  const size_t	ha_len);
extern EXC_TYPE get_host_name (char hostname[], const size_t hlen,
										 const struct in_addr *iaddr);

extern struct hostent *gethostbyname_r (const char		*hostname,
													struct hostent *hp,
													char				*hostdata,
							 						int				hlen,
													int				*hperrno);

extern struct hostent *gethostbyaddr_r (const char		*haddr,
													int				len,
													int				type,
													struct hostent *hp,
													char				*buf,
													int				buf_len,
													int				*hperrno);

extern struct servent *getservbyname_r (const char		*name,
													const char		*proto,
													struct servent *srvrecp,
													char				*buf,
													int				buflen);

#ifdef __cplusplus
extern EXC_TYPE inet_ntoa_r (const struct in_addr& a, LPTSTR lpszAddr, const UINT32 ulMaxLen);
#endif	/* __cplusplus */

extern EXC_TYPE GetPeerAddressDetails (const SOCKET sock, LPTSTR lpszPeerAddr /* may be NULL */, const UINT32 ulMaxLen, int *pPort /* may be NULL */);
extern EXC_TYPE GetPeerAddressStr (const SOCKET sock, LPTSTR lpszPeerAddr, const UINT32 ulMaxLen);
extern EXC_TYPE GetPeerAddressPort (const SOCKET sock, int *pPort);

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// separator used in servers list to be added to cache
#define NETSRVRCACHESEP	_T(',')

// class to implement a round-robin servers cache
class CNetServersCache {
	protected:
		CVSDCollection	m_srvCache;		// contains IP addresses (if resolved) or names (if unresolved)
		UINT32			m_ulCurSrvr;

		// disable copy constructor and assignment operator
		CNetServersCache (const CNetServersCache& );
		CNetServersCache& operator= (const CNetServersCache& );

	public:
		CNetServersCache (const UINT32 ulMaxSrvrs=0, const UINT32 ulGrowNum=0)
			: m_srvCache(ulMaxSrvrs, ulGrowNum), m_ulCurSrvr(0)
		{
		}

		virtual UINT32 GetCacheSize () const { return m_srvCache.GetSize(); }

		// Note: cannot be re-initialized (e.g. if already set through constructor)
		virtual EXC_TYPE InitCache (const UINT32 ulMaxSrvrs, const UINT32 ulGrowNum)
		{
			return m_srvCache.SetParams(ulMaxSrvrs, ulGrowNum);
		}

		virtual EXC_TYPE AddServer (LPCTSTR lpszHostName /* ignored */, LPCTSTR lpszHostAddr);

		// list may contain either names (in which case they are resolved) or IP addresses
		//
		// Note: cache is not checked for duplicate entries - this enables "weighted" round robin
		virtual EXC_TYPE AddServers (LPCTSTR lpszSrvList, const BOOLEAN fResolveNames=TRUE);
		virtual EXC_TYPE AddServers (const CNetServersCache& nsc, const BOOLEAN fResolveNames=TRUE);

		virtual EXC_TYPE GetNextServer (LPCTSTR& lpszSrvr);

		virtual EXC_TYPE EnumServers (CVSD_ENUM_CFN lpfnEcfn, LPVOID pArg) const
		{
			return m_srvCache.EnumItems(lpfnEcfn, pArg);
		}

		virtual EXC_TYPE FindServer (LPCTSTR lpszServer, const BOOLEAN fResolveNames=TRUE) const;

		// contains IP addresses (if resolved) or names (if unresolved) strings
		virtual const CVSDCollection& GetServers () const
		{
			return m_srvCache;
		}

		virtual EXC_TYPE Reset ();

		// requires re-initialization
		virtual EXC_TYPE Clear ();

		virtual ~CNetServersCache ()
		{
		}
};	// end of net server cache class

typedef CNetServersCache *LPCNETSRVRSCACHE;
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CDNSServersCache : public CNetServersCache {
	protected:
		CStr2PtrMapper	m_hostsMap;	// key=name, value=index of address in the net servers cache

	public:
		CDNSServersCache (const UINT32 ulMaxSrvrs=0, const UINT32 ulGrowNum=0)
			: CNetServersCache(ulMaxSrvrs, ulGrowNum), m_hostsMap((ulMaxSrvrs+ulGrowNum), FALSE)
		{
		}

		// Note: cannot be re-initialized (e.g. if already set through constructor)
		virtual EXC_TYPE InitCache (const UINT32 ulMaxSrvrs, const UINT32 ulGrowNum);

		// Note: returns error if duplicate entry
		virtual EXC_TYPE AddServer (LPCTSTR lpszHostName, LPCTSTR lpszHostAddr);

		// Note: duplicate entries are skipped
		virtual EXC_TYPE AddServers (LPCTSTR lpszSrvList, const BOOLEAN fResolveNames=TRUE /* ignored */);

		// Note: duplicate entries are skipped
		virtual EXC_TYPE AddServers (const CNetServersCache& nsc, const BOOLEAN fResolveNames=TRUE /* ignored */);

		// Note: duplicate entries are skipped
		virtual EXC_TYPE Merge (const CDNSServersCache& dsc);

		// if not known, then added (if auto-add=TRUE)
		virtual EXC_TYPE Resolve (LPCTSTR lpszHostName, LPCTSTR& lpszHostAddr, const BOOLEAN fAutoAdd=TRUE);

		// key=name, value=index of address in the net servers cache
		virtual const CStr2PtrMapper&	GetMap () const
		{
			return m_hostsMap;
		}

		virtual EXC_TYPE Reset ();

		// requires re-initialization
		virtual EXC_TYPE Clear ();

		virtual ~CDNSServersCache ()
		{
		}
};

class CDNSServersCacheEnum {
	protected:
		CStr2PtrMapEnum			m_mpe;
		const CVSDCollection&	m_adc;

		virtual EXC_TYPE GetValue (LPCTSTR& lpszHostName, LPCTSTR& lpszHostAddr, const BOOLEAN fIsFirst);

	public:
		CDNSServersCacheEnum (const CDNSServersCache&  dsc)
			: m_mpe(dsc.GetMap()), m_adc(dsc.GetServers())
		{
		}

		virtual EXC_TYPE GetFirst (LPCTSTR& lpszHostName, LPCTSTR& lpszHostAddr)
		{
			return GetValue(lpszHostName, lpszHostAddr, TRUE);
		}

		virtual EXC_TYPE GetNext (LPCTSTR& lpszHostName, LPCTSTR& lpszHostAddr)
		{
			return GetValue(lpszHostName, lpszHostAddr, FALSE);
		}

		virtual UINT32 GetSize () const
		{
			return m_adc.GetSize();
		}

		virtual ~CDNSServersCacheEnum ()
		{
		}
};
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

typedef struct {
	UINT32	ulBaseAddr;
	UINT32	ulNetmask;	/* 0 == no special mask */
} IPADDRESSPAIR, *LPIPADDRESSPAIR;

/* a host address pair may be "1.2.3.4/24" or "1.2.3.4/255.255.255.0" or "1.2.3.4" */
#define IPADDRPAIRNETMASKDELIM	_T('/')

extern EXC_TYPE DecodeIPAddressSpecPairChars (LPCTSTR			lpszAddrPair,
															 const UINT32	ulAPLen,
															 UINT32			*pulBaseAddr,
															 UINT32			*pulNetmask);

#ifdef __cplusplus
inline EXC_TYPE DecodeIPAddressSpecPair (LPCTSTR	lpszAddrPair,
													  UINT32		*pulBaseAddr,
													  UINT32		*pulNetmask)
{
	return DecodeIPAddressSpecPairChars(lpszAddrPair, GetSafeStrlen(lpszAddrPair), pulBaseAddr, pulNetmask);
}

inline EXC_TYPE DecodeIPAddressSpecPair (LPCTSTR	lpszAddrPair, IPADDRESSPAIR& hap)
{
	return DecodeIPAddressSpecPair(lpszAddrPair, &hap.ulBaseAddr, &hap.ulNetmask);
}
#else
#define DecodeIPAddressSpecPair(a,b,n)	\
		DecodeIPAddressSpecPairChars((a),GetSafeStrlen(a),b,n)
#endif	/* __cplusplus */

/* enumeration callback of address pairs */
typedef EXC_TYPE (*IPADDRPAIR_ENUM_CFN)(const UINT32	ulPairNdx,	/* starts at zero */
													 const UINT32	ulBaseAddr,
													 const UINT32	ulNetmask,
													 LPVOID			pArg,
													 BOOLEAN			*pfContEnum);

/* Note: pairs list may be empty/NULL, delimiter may NOT be EOS, dot or IPADDRPAIRNETMASKDELIM */
extern EXC_TYPE EnumIPAddressSpecPairs (LPCTSTR					lpszPairsList,
													 const TCHAR			chSepDelim,
													 IPADDRPAIR_ENUM_CFN	lpfnEcfn,
													 LPVOID					pArg);

/*---------------------------------------------------------------------------*/

#endif /* of ifdef _COMM_NETDB_H_ */
