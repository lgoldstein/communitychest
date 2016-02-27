#ifndef _DNS_RESOLVE_H
#define _DNS_RESOLVE_H

/* requires linking with "ws2_32.lib" */
/* requires linking with "dnsapi.lib" */

#define MAX_DNS_NAME_RESULT_LEN	128

/*
 * MX_RESULT is a single result of an MX record query.
 * WORD	MxPref -- Preference of this host. LOWER values are preferred!!
 * char	DnsName[MAX_DNS_NAME_RESULT_LEN + 1] -- A host willing to act as a 
 *    mail exchange for the owner name.
 */
typedef struct	MX_RESULT
{
	WORD	MxPref;
	char	DnsName[MAX_DNS_NAME_RESULT_LEN + 1];
} MX_RESULT;

/*
 * ---------------------------------------------------------------------------
 * int	MxResolve(const char *pszDomain, MX_RESULT *pMxResults, 
 *                int NumStructs)
 *  Description: Returns the MX record for the given domain.
 *  Parameters:
 *  const char *pszDomain -- Name of domain to resolve
 *  MX_RESULT *pMxResults -- Pointer to an array of MX_RESULT structures
 *  int *pNumStructs -- On input -- number of input structures,
 *                      On output -- number of output structures.
 *  Returns   :  Zero upon success, or nonzero otherwise.
 *               See "Winsock2.h" for detailed error descriptions.
 *               Note the error WSASERVICE_NOT_FOUND (10108) returned
 *               in case of no MX records for the given domain.
 *  Remarks :    1. Lower values of preferences should be preferred.
 *               2. The function uses winsock2 functions. It is the
 *                  user's responsibility to initialize it using WSAStartup().
 * -----------------------------------------------------------------------------
 */
extern int WinMxResolve (const char *pszDomain, /* uses WinSock2 */
								 MX_RESULT *pMxResults, 
								 int *pNumStructs);

#ifdef __cplusplus
#include <windns.h>

class CWinDndRecsGuard {
	private:
		PDNS_RECORD&	m_pRecs;

		CWinDndRecsGuard (const CWinDndRecsGuard& );
		CWinDndRecsGuard& operator= (const CWinDndRecsGuard& );

	public:
		CWinDndRecsGuard (PDNS_RECORD& pRecs)
			: m_pRecs(pRecs)
		{
		}

		void Release ()
		{
			if (m_pRecs != NULL)
			{
				::DnsRecordListFree(m_pRecs, DnsFreeRecordListDeep);
				m_pRecs = NULL;
			}
		}

		~CWinDndRecsGuard ()
		{
			Release();
		}
};
#endif	/* __cplusplus */

extern int DnsMxResolve (const char *pszDomain,	/* uses WinDns */
								 MX_RESULT	*pMxResults, 
								 int			*pNumStructs);

/*
 * ---------------------------------------------------------------------------
 * int FindMinMxRecord (const MX_RESULT	*pMxResults,
 *							const int			NumResults,
 *							const int	*nMinMXResult)
 *  Description: finds MX record with lowest preference.
 *  Parameters:
 *	 const MX_RESULT	*pMxResults -- Pointer to returned MX results
 *	 const int			NumResults -- number of results
 *	 int					*nMinMXResult -- index of found record (if successful)
 *  Returns   :  Zero upon success, or nonzero otherwise.
 * -----------------------------------------------------------------------------
 */

extern int FindMinMxRecord (const MX_RESULT	*pMxResults,
									 const int			NumResults,
									 int					*nMinMXResult);

/*
 * -----  -----  ------  ----- N O T E  -----  -----  -----  -----
 * The rest of the file contains private implementation definitions
 * that should be called by user.
 * -----  ------  -----  ----- N O T E  -----  -----  -----  -----
 */

extern int GetDnsBlobResult (const char *pszDomain, 
									  int DnsRecordType, 
									  char *pBlob, 
									  DWORD BlobSize);

extern int ParseDnsResults (const char *pBlob,
									 const char *pszDomain,
									 MX_RESULT *pMxResults, 
									 int *pNumStructs);

extern int ParseDnsHdr(const char *pData, WORD *pAnswerCount);

extern int ParseDnsQuestion(const char *pData, WORD offset, char *pQname, 
					 WORD QnameSize, WORD *pQType);

extern int ParseDnsRRHdr(const char *pData, WORD offset, char *pRname, 
					 WORD RnameSize, WORD *pQType);

extern int ParseDnsMXResult(const char *pData, WORD init_offset, char *pMXname, 
					 WORD MXnameSize, WORD *pPref);

extern unsigned short GetDnsString(const char *pData, 
							WORD initOffset, 
							char *pDst,
							WORD	DstSize);

#define DNS_BLOB_SIZE	(512)
#define	MX_RECORD		(15)


#define BIT(x)			(1<<(x))
#define UPTOBIT(x)	((BIT(x) - 1))
#define BITMASK(a,b)	((UPTOBIT(a) ^ UPTOBIT(b)) | BIT(b))

/*
 * DNS frame definitions
 */
#define DNS_RCODE_MASK		(BITMASK(0,3))
#define DNS_TRUNCATION_BIT	(BIT(9))

#define DNS_IN_CLASS		(1)  // Internet

/*
 * Macros and definitions for DNS "compression"
 */
#define DNS_OFFSET_IS_PTR(x) ((x & (BITMASK(6,7))) == BITMASK(6,7))
#define DNS_PTR_MASK			  (BITMASK(14,15))

#endif /* DNS_RESOLVE_H */

