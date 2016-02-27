/* requires linking with "ws2_32.lib" */
#include <winsock2.h>
/* requires linking with "dnsapi.lib" */
#include <svcguid.h>
#include <win32/dnsResolve.h>
#include <util/string.h>

#ifndef SVCID_DNS
#	define SVCID_DNS(_RecordType) SVCID_TCP_RR(53, _RecordType)
#endif

#ifndef SVCID_DNS_TYPE_MX
#	define SVCID_DNS_TYPE_MX SVCID_DNS( 0x000f )
#endif

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
int WinMxResolve (const char *pszDomain, MX_RESULT *pMxResults, int *pNumStructs)
{
	int	ret=NO_ERROR;
	char	*pBlob=NULL;

	if ((NULL == pszDomain) || (NULL == pNumStructs))
	{
		ret = WSA_INVALID_PARAMETER;
		goto cleanup;
	}

	if (('0' == pszDomain[0]) || (0 == *pNumStructs))
	{
		ret = WSA_INVALID_PARAMETER;
		goto cleanup;
	}

	/*
	 * Allocate a BLOB for the DNS query result
	 */
	pBlob = (char *)malloc(DNS_BLOB_SIZE);
	if (NULL == pBlob)
	{
		ret = WSA_NOT_ENOUGH_MEMORY;
		goto cleanup;
	}

	ret = GetDnsBlobResult(pszDomain, MX_RECORD, pBlob, DNS_BLOB_SIZE);
	if (ret != NO_ERROR)
	{
		goto cleanup;
	}

	/*
	 * Now parse the MX results
	 */
	ret = ParseDnsResults(pBlob, pszDomain, pMxResults, pNumStructs);
	if (ret != NO_ERROR)
	{
		goto cleanup;
	}

	ret = NO_ERROR;

cleanup:
	if (pBlob != NULL)
	{
		free(pBlob);
		pBlob = NULL;
	}

	return (ret);
}

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

int FindMinMxRecord (const MX_RESULT	*pMxResults,
							const int			NumResults,
							int					*nMinMXResult)
{
	int	nIdx=1;

	if ((NULL == pMxResults) || (0 == NumResults) || (NULL == nMinMXResult))
		return WSA_INVALID_PARAMETER;

	*nMinMXResult = 0;
	for ( ; nIdx < NumResults; nIdx++)
	{
		const MX_RESULT	*pMXR=(pMxResults + nIdx);
		const MX_RESULT	*pMin=(pMxResults + (*nMinMXResult));

		if (pMin->MxPref > pMXR->MxPref)
			*nMinMXResult = nIdx;
	}

	return NO_ERROR;
}

/*
 * ---------------------------------------------------------------------------
 * int ParseDnsResults(const char *pBlob, const char *pszDomain,
 *				    MX_RESULT *pMxResults, 
 *					int *pNumStructs)
 *  Description: Parses the MX record results into the given input
 *  Parameters:
 *  const char *pBlob -- Pointer to DNS data blob returned from 
 *                       GetDnsBlobResult()
 *  const char *pszDomain -- Domain for which the MX record is sought
 *  MX_RESULT *pMxResults -- Pointer to an array of MX_RESULT structures
 *  int *pNumStructs -- On input -- number of input structures,
 *                      On output -- number of output structures.
 *  Returns   :  Zero upon success, or nonzero otherwise.
 *  Remarks :    1. Lower values of preferences should be preferred.
 *               2. The function uses winsock2 functions. It is the
 *                  user's responsibility to initialize it using WSAStartup().
 * -----------------------------------------------------------------------------
 */
int ParseDnsResults (const char	*pBlob,
							const char	*pszDomain,
							MX_RESULT	*pMxResults, 
							int			*pNumStructs)
{
	const char	*pData=pBlob;
	int			offset=0, ret=NO_ERROR, i=0;
	WORD			NumAnswers = 0, QType = 0;
	char			Qname[MAX_DNS_NAME_RESULT_LEN + 1]="", Rname[MAX_DNS_NAME_RESULT_LEN + 1]="";

	if ((NULL == pBlob) || (NULL == pMxResults) || 
		(NULL == pszDomain) ||
		(NULL == pNumStructs) || (0 == *pNumStructs))
	{
		ret = WSA_INVALID_PARAMETER;
		goto cleanup;
	}

	offset = ParseDnsHdr(pData, &NumAnswers);
	if ((-1) == offset)
	{
		ret = WSAEINTR;
		goto cleanup;
	}

	/*
	 * Return it in any case
	 */
	if (NumAnswers > *pNumStructs)
	{
		*pNumStructs = NumAnswers;
		ret = WSA_NOT_ENOUGH_MEMORY;
		goto cleanup;
	}

	*pNumStructs = NumAnswers;

	/*
	 * Parse question section
	 */
	offset = ParseDnsQuestion(pData, offset, Qname, (sizeof Qname), &QType);
	if (((-1) == offset) || (QType != MX_RECORD))
	{
		ret = WSAEINTR;
		goto cleanup;
	}

	/*
	 * Loop on all answers
	 */
	for (i = 0; i < NumAnswers; i++)
	{
		MX_RESULT	*pMXR=(pMxResults+i);

		/*
		 * Get general resource record section
		 */
		offset = ParseDnsRRHdr(pData, offset, Rname, sizeof(Rname), &QType);
		if (((-1) == offset) || (QType != MX_RECORD))
		{
			ret = WSAEINTR;
			goto cleanup;
		}

		/*
		 * Verify answer == question
		 */
		if (_stricmp(Qname, Rname) != 0)
		{
			ret = WSAEINTR;
			goto cleanup;
		}
		
		/*
		 * Get specific MX record results
		 */
		offset = ParseDnsMXResult(pData, offset, 
										  pMXR->DnsName, MAX_DNS_NAME_RESULT_LEN,
										  &(pMXR->MxPref));
		
		if ((-1) == offset)
		{
			ret = WSAEINTR;
			goto cleanup;
		}
	}

	ret = NO_ERROR;

cleanup:
	return (ret);
}

/*
 * ---------------------------------------------------------------------------
 * int ParseDnsHdr(const char *pData, WORD *pAnswerCount)
 *  Description: Parses the DNS header
 *  Parameters:
 *  const char *pData -- Pointer to start of data (blob)
 *  WORD *pAnswerCount -- Pointer to number of answer resource records
 *  Returns   :  offset to next data to parse or (-1) on error
 * -----------------------------------------------------------------------------
 */
int ParseDnsHdr (const char *pData, WORD *pAnswerCount)
{
	int			offset=0;
	WORD			wData=0;

	if ((NULL == pData) || (NULL == pAnswerCount))
		return (-1);

	/*
	 * ---------------------------------------------------------------------------

	                                    1  1  1  1  1  1
      0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
0   |                      ID                       |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
2   |QR|   Opcode  |AA|TC|RD|RA|   Z    |   RCODE   |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
4   |                    QDCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
6   |                    ANCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
8   |                    NSCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
10  |                    ARCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+

	 * ---------------------------------------------------------------------------
	 */

	wData = ntohs(*((WORD *)&pData[offset])); // ID
	offset += sizeof(WORD);

	wData = ntohs(*((WORD *)&pData[offset])); // Flags
	offset += sizeof(WORD);

	if (((wData & DNS_RCODE_MASK) != 0) || ((wData & DNS_TRUNCATION_BIT) != 0))
		return (-1);

	wData = ntohs(*((WORD *)&pData[offset])); // QDCount
	offset += sizeof(WORD);
	if (wData != 1)
		return (-1);

	wData = ntohs(*((WORD *)&pData[offset])); // ANCount
	offset += sizeof(WORD);
	if (0 == wData)
		return (-1);

	*pAnswerCount = wData;

	/*
	 * Now come the name server and additional records count ...
	 */
	offset += (2 * sizeof(WORD));
	return offset;
}

/*
 * ---------------------------------------------------------------------------
 * int ParseDnsQuestion(const char *pData, 
 *                      WORD init_offset, char *pQname, 
 *					    WORD QnameSize, WORD *pQType)
 *  Description: Parses the DNS question section
 *  Parameters:
 *  const char *pData -- Pointer to start of data (blob)
 *  WORD init_offset -- Initial offset
 *  char *pQname -- Pointer to question name buffer
 *	WORD QnameSize -- Question name size
 *  WORD *pQType -- Pointer to where question type is returned
 *  Returns   :  offset to next data to parse or (-1) on error
 * -----------------------------------------------------------------------------
 */
int ParseDnsQuestion (const char *pData,
							 WORD			init_offset,
							 char			*pQname, 
							 WORD			QnameSize,
							 WORD			*pQType)
{
	int			offset=init_offset;
	WORD			wData=0;

	if ((NULL == pData) || (NULL == pQname) || (NULL == pQType))
		return (-1);

	/*
	 * Get the question name
	 */
	offset = GetDnsString(pData, offset, pQname, QnameSize);
	if ((-1) == offset)
		return (-1);

	wData = ntohs(*((WORD *)&pData[offset])); // QType
	offset += sizeof(WORD);
	*pQType = wData;

	wData = ntohs(*((WORD *)&pData[offset])); // QClass
	offset += sizeof(WORD);

	if (wData != DNS_IN_CLASS)
		return (-1);

	return offset;
}

/*
 * ---------------------------------------------------------------------------
 * int ParseDnsRRHdr(const char *pData, WORD init_offset, char *pRname, 
 *					 WORD RnameSize, WORD *pQType)
 *
 *  Description: Parses the DNS RR (Resource Record) section
 *  Parameters:
 *  const char *pData -- Pointer to start of data (blob)
 *  WORD init_offset -- Initial offset
 *  char *pRname --  Resource name buffer
 *	WORD RnameSize -- Resource name buffer size
 *  WORD *pQType -- Pointer to where question type is returned
 *  Returns   :  offset to next data to parse or (-1) on error
 * -----------------------------------------------------------------------------
 */
int ParseDnsRRHdr (const char *pData,
						 WORD			init_offset,
						 char			*pRname, 
						 WORD			RnameSize,
						 WORD			*pQType)
{
	int offset=init_offset;
	WORD wData=0;

	/*
	 * Get the resource record name
	 */
	offset = GetDnsString(pData, offset, pRname, RnameSize);
	if ((-1) == offset)
		return (-1);

	wData = ntohs(*((WORD *)&pData[offset])); // QType
	offset += sizeof(WORD);
	*pQType = wData;

	wData = ntohs(*((WORD *)&pData[offset])); // QClass
	offset += sizeof(WORD);
	if (wData != DNS_IN_CLASS)
		return (-1);

	offset += sizeof (DWORD) + sizeof(WORD); // TTL and RDATA length
	return offset;
}

/*
 * ---------------------------------------------------------------------------
 * int ParseDnsMXResult(const char *pData, WORD init_offset, char *pMXname, 
 *					 WORD MXnameSize, WORD *pPref)
 *
 *  Description: Parses a single DNS MX specific section
 *  Parameters:
 *  const char *pData -- Pointer to start of data (blob)
 *  WORD init_offset -- Initial offset
 *  char *pMXname -- Pointer to MX name result
 *  WORD MXnameSize -- MX name result size
 *  WORD *pPref -- MX result preference
 *  Returns   :  offset to next data to parse or (-1) on error
 * -----------------------------------------------------------------------------
 */
int ParseDnsMXResult (const char *pData,
							 WORD init_offset,
							 char *pMXname, 
							 WORD MXnameSize,
							 WORD *pPref)
{
	int offset=init_offset;
	WORD wData=0;

	if ((NULL == pData) || (NULL == pPref))
		return (-1);

	wData = ntohs(*((WORD *)&pData[offset])); // MX Preference
	offset += sizeof(WORD);
	*pPref = wData;

	/*
	 * Get the resource record name
	 */
	return GetDnsString(pData, offset, pMXname, MXnameSize);
}

/*
 * ---------------------------------------------------------------------------
 * unsigned short GetDnsString(const char *pData, 
 *							WORD initOffset, 
 *							char *pDst,
 *							DWORD	DstSize)
 *
 *  Description: Gets a DNS string
 *  Parameters:
 *  const char *pData -- Pointer to start of data (blob)
 *  WORD init_offset -- Initial offset
 *  char *pDst -- Pointer to destination buffer
 *	DWORD	DstSize -- Destination buffer size
 *  Returns   :  offset to next data to parse or (-1) on error
 * -----------------------------------------------------------------------------
 */
unsigned short GetDnsString (const char *pData, 
									  WORD initOffset, 
									  char *pDst,
									  WORD	DstSize)
{
	unsigned short ret_offset = initOffset;
   unsigned short curr_offset = initOffset;
   unsigned short dst_offset = 0;
	BOOL ptr_found = FALSE;

	if ((NULL == pData) || (NULL == pDst))
		return ((unsigned short) (-1));

	while (pData[curr_offset] != 0)
	{
		/*
		 * End not found yet. Check if first word is length or offset
		 */
		if(DNS_OFFSET_IS_PTR(pData[curr_offset]))
		{
			/*
			 * Offset
			 */
			if(!ptr_found)
			{
				ret_offset += sizeof(WORD);
				ptr_found = TRUE;
			}

			curr_offset=ntohs(*((WORD *)&pData[curr_offset]));
			curr_offset &= ~(DNS_PTR_MASK);
		}
		else
		{
			/*
			 * Copy string
			 */
			int str_len = pData[curr_offset];
			
			if ((dst_offset + str_len) >= DstSize)
				return (-1);

			memcpy(&pDst[dst_offset],  &pData[curr_offset+1], str_len);
			dst_offset += str_len;
			pDst[dst_offset++]='.';
			curr_offset += str_len+1;

			if (!ptr_found)
			{
				ret_offset = curr_offset;
			}
		}
	}
	
	/*
	 * Change last '.' to '\0'
	 */
	pDst[dst_offset - 1]='\0';
	if (!ptr_found)
	{
		ret_offset++;
	}

	return(ret_offset);
}

int GetDnsBlobResult(const char *pszDomain, 
							int DnsRecordType, 
							char *pBlob, 
							DWORD BlobSize)
{
	int			ret = NO_ERROR;
	GUID			*pServiceType = NULL;
	GUID			DnsMxServiceType = SVCID_DNS_TYPE_MX;
   WSAQUERYSET	wsaQuerySet, *pwsaQueryResult = NULL;
   HANDLE      hQuery = NULL;
	DWORD			dwLength=0;
	
	if ((NULL == pszDomain) || (NULL == pBlob) || (0 == BlobSize))
	{
		ret = WSA_INVALID_PARAMETER;
		goto cleanup;
	}

	/*
	 * Select the GUID
	 */
	switch (DnsRecordType)
	{
		case MX_RECORD:
			pServiceType = &DnsMxServiceType;
			break;
		default:
			ret = WSA_INVALID_PARAMETER;
			goto cleanup;
			break;
	}

	/*
	 * Initialize QuerySet and allocate result buffer
	 */
	ZeroMemory(&wsaQuerySet, sizeof(wsaQuerySet));

	dwLength = 1024; // Should be enough
	pwsaQueryResult = (WSAQUERYSET *)malloc(dwLength);
	if (NULL == pwsaQueryResult)
	{
		ret = WSA_NOT_ENOUGH_MEMORY;
		goto cleanup;
	}

	ZeroMemory(pwsaQueryResult, dwLength);

	wsaQuerySet.dwSize = sizeof(WSAQUERYSET); 
	wsaQuerySet.lpszServiceInstanceName = (char *)pszDomain; 
	wsaQuerySet.lpServiceClassId = pServiceType; 
	wsaQuerySet.lpVersion = 0;     
	wsaQuerySet.lpszComment = 0; 
	wsaQuerySet.dwNameSpace = NS_DNS; 
	wsaQuerySet.lpNSProviderId = 0;     
	wsaQuerySet.lpszContext = 0; 
	wsaQuerySet.dwNumberOfProtocols = 0; 
	wsaQuerySet.lpafpProtocols = 0;  
	wsaQuerySet.dwNumberOfCsAddrs = 0;  
	wsaQuerySet.lpcsaBuffer = 0;  
	wsaQuerySet.lpBlob = 0;  

	/*
	 * We are only interested in the BLOB
	 */
	ret = WSALookupServiceBegin(&wsaQuerySet, LUP_RETURN_BLOB, &hQuery);
	if (ret != NO_ERROR )
	{ 
		ret = WSAGetLastError();
		goto cleanup;
	}

	/*
     * The query was accepted, so execute it via the Next call.
	 */
	ret = WSALookupServiceNext(hQuery, 0, &dwLength, pwsaQueryResult);
	if (ret != NO_ERROR) 
	{         
		ret = WSAGetLastError();          
		if ((WSAENOMORE == ret) || (WSA_E_NO_MORE == ret))
		{             
			ret = NO_ERROR;         
		}
		else
		{
			goto cleanup;
		}
	}

	/*
	 * Copy the BLOB.
	 */
	if (pwsaQueryResult->lpBlob != NULL)
	{
		if (pwsaQueryResult->lpBlob->pBlobData != NULL)
		{
			if (pwsaQueryResult->lpBlob->cbSize <= BlobSize)
			{
				memcpy(pBlob, pwsaQueryResult->lpBlob->pBlobData, pwsaQueryResult->lpBlob->cbSize);
			}
			else
			{
				ret = WSA_NOT_ENOUGH_MEMORY;
			}
		}
		else
		{
			ret = WSAEINTR;
		}
	}
	else
	{
		ret = WSAEINTR;
	}

	if (ret != NO_ERROR)
	{
		goto cleanup;
	}

	/*
	 * Note -- We don't support multiple calls to 
	 * WSALookupServiceNext(). Verify there is no more data
	 */
	ret = WSALookupServiceNext(hQuery, 0,&dwLength, pwsaQueryResult);
	if (ret != NO_ERROR)
	{
		ret = WSAGetLastError();
		if ((WSAENOMORE == ret) || (WSA_E_NO_MORE == ret))
		{
			ret = NO_ERROR;
		}
	}

	if (ret != NO_ERROR)
	{
		goto cleanup;
	}

	ret = NO_ERROR;

cleanup:
	if (hQuery != NULL)
	{
		WSALookupServiceEnd(hQuery);
		hQuery = NULL;
	}

	if (pwsaQueryResult != NULL)
	{
		free(pwsaQueryResult);
		pwsaQueryResult = NULL;
	}

	return (ret);
}

/*--------------------------------------------------------------------------*/

int DnsMxResolve (const char *pszDomain,	/* uses WinDns */
						MX_RESULT	*pMxResults, 
						int			*pNumStructs)
{
	HRESULT				hr=S_OK;
	PDNS_RECORD			pRecs=NULL, pRes=NULL;
	int					nCurRec=0, nMaxRecs=((NULL == pNumStructs) ? 0 : (*pNumStructs));
#ifdef __cplusplus
	CWinDndRecsGuard	drg(pRecs);
#else
#	error "Need to guard the record ptr !!!"
#endif	/* __cplusplus */

	if (IsEmptyStr(pszDomain) || (NULL == pMxResults) || (0 == nMaxRecs))
		return WSA_INVALID_PARAMETER;

	if ((hr=DnsQuery((char *) pszDomain, DNS_TYPE_MX, DNS_QUERY_STANDARD, NULL, &pRecs, NULL)) != NO_ERROR)
		return hr;

	for (pRes=pRecs; (pRes != NULL) && (nCurRec < nMaxRecs); pRes=pRes->pNext)
	{
		const DNS_MX_DATA&	dmx=pRes->Data.MX;
		MX_RESULT&				mxRes=pMxResults[nCurRec];

		/* response may contain A-records - so skip them */
		if (pRes->wType != DNS_TYPE_MX)
			continue;
		/* skip empty entries */
		if (IsEmptyStr(dmx.pNameExchange))
			continue;
		if (_tcslen(dmx.pNameExchange) > MAX_DNS_NAME_RESULT_LEN)
			return WSA_NOT_ENOUGH_MEMORY;

		memset(&mxRes, 0, (sizeof mxRes));
		mxRes.MxPref = dmx.wPreference;
		_tcscpy(mxRes.DnsName, dmx.pNameExchange);

		nCurRec++;
	}

	if (0 == (*pNumStructs=nCurRec))
		return WSASERVICE_NOT_FOUND;

	return NO_ERROR;
}

/*--------------------------------------------------------------------------*/
#if FALSE
INT MyGetAddressByName(
	IN     PTSTR         szServiceName, 
    IN     LPGUID        lpServiceType);

void parse ( unsigned char *data );

INT MyGetAddressByName(
	IN     PTSTR         szServiceName, 
    IN     LPGUID        lpServiceType)
{
    ULONG            dwLength = 1024;
    WSAQUERYSET      wsaQuerySet, *pwsaQueryResult;
	ULONG            err; 
    HANDLE           hRnR;     

    ZeroMemory(&wsaQuerySet, sizeof(wsaQuerySet));
	pwsaQueryResult = (WSAQUERYSET *)malloc(dwLength);
	if (NULL == pwsaQueryResult)
	{
		return (-1);
	}
    ZeroMemory(pwsaQueryResult, dwLength);

    wsaQuerySet.dwSize = sizeof( WSAQUERYSET); 
    wsaQuerySet.lpszServiceInstanceName = szServiceName; 
    wsaQuerySet.lpServiceClassId = lpServiceType; 
    wsaQuerySet.lpVersion = 0;     
	wsaQuerySet.lpszComment = 0; 
    wsaQuerySet.dwNameSpace = NS_ALL; 
    wsaQuerySet.lpNSProviderId = 0;     
	wsaQuerySet.lpszContext = 0; 
    wsaQuerySet.dwNumberOfProtocols = 0; 
    wsaQuerySet.lpafpProtocols = 0;  
    wsaQuerySet.dwNumberOfCsAddrs = 0;  
    wsaQuerySet.lpcsaBuffer = 0;  
    wsaQuerySet.lpBlob = 0;  

    err = WSALookupServiceBegin( &wsaQuerySet, 
								 LUP_RETURN_ALL, /* LUP_RETURN_ADDR | LUP_RETURN_NAME,*/
                                 &hRnR );      
	if ( err != NO_ERROR )     { 
        err = WSAGetLastError();          // 
        // Unsuccessful.         //         return (DWORD) err;     
		return err;
	}      // 
    // The query was accepted, so execute it via the Next call.     // 
	err = WSALookupServiceNext( hRnR, 
                                0,
								&dwLength, 
                                pwsaQueryResult );      
	if ( err != NO_ERROR ) 
    {         
		err = WSAGetLastError();          
		if ( err == WSA_E_NO_MORE ) 
        {             
			err = 0;         
		}  

        if ( err == WSASERVICE_NOT_FOUND )         
		{ 
            err = WSAHOST_NOT_FOUND;         
		}  
        // Unsuccessful.         //         
		return (DWORD) err;      
	}


	parse(pwsaQueryResult->lpBlob->pBlobData);

	return 0;  
} // RnrGetHostFromName   


//-----------------------------------------------------------------------------

	/*++  Routine Description:  
    Calls Winsock 2.0 service lookup routines to find service addresses.  
Arguments:  
    szServiceName - a friendly name which identifies the service we want 
        to find the address of.  
    lpServiceType - a GUID that identifies the type of service we want 
        to find the address of.  
    dwNameSpace - The Winsock2 Name Space to get address from (i.e. NS_ALL)  
    dwNumberOfProtocols - Size of the protocol constraint array, may be zero.  
    lpAftProtocols -  (Optional) References an array of AFPROTOCOLS structure. 
        Only services that utilize these protocols will be returned.  
    lpCSAddrInfos - On successful return, this will point to an array of 
        CSADDR_INFO structures that contains the host address(es). Memory 
        is passed in by callee and the length of the buffer is provided by 
        lpdwBufferLength.  
    lpdwBufferLength - On input provides the length in bytes of the buffer 
        lpCSAddrInfos. On output returns the length of the buffer used or 
        what length the buffer needs to be to store the address.  Return Value: 
     The number of CSADDR_INFO structures returned in lpCSAddrInfos, or 
    (INVALID_SOCKET) with a WIN32 error in GetLastError.  --*/


unsigned char qname[80];
unsigned char aname[80];
unsigned char bname[80];
unsigned char cname[80];
unsigned char buf[1024];
char gstring[128];
WORD ra;
WORD rb;
WORD rc;
DWORD la;
WORD ancount;
WORD type;

#define reverseword(w)		ntohs(w)
#define reversedword(dw)	ntohl(dw)
unsigned char name[80];


unsigned short dnsstring (unsigned char *pData, WORD initOffset, unsigned char *pDst)
{
    unsigned short ret = 0;
    unsigned short off = 0;

    off=0;
    ret=0;
    while(1)
    {
        if(pData[initOffset]==0)
        {
            if(!ret) ret=initOffset+1;
            if(off) off--;
            pDst[off]=0;
            return(ret);
        }
        if((pData[initOffset]&0xC0)==0xC0)
        {
            if(!ret) ret=initOffset+2;
            initOffset=pData[initOffset+1];
            continue;
        }
        memcpy(&pDst[off],&pData[initOffset+1],pData[initOffset]);
        off+=pData[initOffset];
        pDst[off++]='.';
        initOffset+=pData[initOffset]+1;
    }
}

void parse ( unsigned char *data )
{
	WORD *pw = (WORD *)data;

    ra=reverseword(*(WORD *)&data[0]);
    if(data[2]&0x02) return;
    if(data[3]&0x0F) return;
    ra=reverseword(*(WORD *)&data[4]);
    if(ra!=1) return;
    ancount=reverseword(*(WORD *)&data[6]);
    if(ancount==0) return;
    rb=dnsstring(data,12,qname);
    for(ra=0;qname[ra];ra++) qname[ra]=toupper(qname[ra]);
    ra=reverseword(*(WORD *)&data[rb]); rb+=2;
    rc=reverseword(*(WORD *)&data[rb]); rb+=2;
    //if(ra!=1) return;
    if(ra!=15) return; /* MX */
    if(rc!=1) return;
    rb=dnsstring(data,rb,aname);
    for(ra=0;aname[ra];ra++) aname[ra]=toupper(aname[ra]);
    type=reverseword(*(WORD *)&data[rb]); rb+=2;
    switch(type)
    {
        case  1:
        case  5:
        case  15: /* MX */
            break;
        default: return;
    }
    ra=reverseword(*(WORD *)&data[rb]); rb+=2;
    if(ra!=1) return;
    la=reversedword(*(DWORD *)&data[rb]); rb+=4;
    ra=reverseword(*(WORD *)&data[rb]); rb+=2;
    if(strcmp((char*)qname,(char *)aname)!=0) return;
    if(type==15)
    {
		WORD pref;
		pref = reverseword(*(WORD *)&data[rb]); rb+=2;

        rb=dnsstring(data,rb,bname);
        for(ra=0;bname[ra];ra++) bname[ra]=toupper(bname[ra]);
        if(ancount==1) return;
        rb=dnsstring(data,rb,cname);
        for(ra=0;cname[ra];ra++) cname[ra]=toupper(cname[ra]);
        if(strcmp((char *)bname,(char *)cname)!=0) return;
        type=reverseword(*(WORD *)&data[rb]); rb+=2;
        if(type!=1) return;
        ra=reverseword(*(WORD *)&data[rb]); rb+=2;
        if(ra!=1) return;
        la=reversedword(*(DWORD *)&data[rb]); rb+=4;
        ra=reverseword(*(WORD *)&data[rb]); rb+=2;
        if(ra!=4) return;
        wsprintf(gstring,"%s %u.%u.%u.%u",qname,data[rb],data[rb+1],data[rb+2],data[rb+3]);
        printf("%s\n",gstring);
        //addstring(gstring);
    }
    if(type==1)
    {
        if(ra!=4) return;
        wsprintf(gstring,"%s %u.%u.%u.%u",qname,data[rb],data[rb+1],data[rb+2],data[rb+3]);
        printf("%s\n",gstring);
        //addstring(gstring);
        return;
    }
    if(type==5)
    {
        rb=dnsstring(data,rb,bname);
        for(ra=0;bname[ra];ra++) bname[ra]=toupper(bname[ra]);
        if(ancount==1) return;
        rb=dnsstring(data,rb,cname);
        for(ra=0;cname[ra];ra++) cname[ra]=toupper(cname[ra]);
        if(strcmp((char *)bname,(char *)cname)!=0) return;
        type=reverseword(*(WORD *)&data[rb]); rb+=2;
        if(type!=1) return;
        ra=reverseword(*(WORD *)&data[rb]); rb+=2;
        if(ra!=1) return;
        la=reversedword(*(DWORD *)&data[rb]); rb+=4;
        ra=reverseword(*(WORD *)&data[rb]); rb+=2;
        if(ra!=4) return;
        wsprintf(gstring,"%s %u.%u.%u.%u",qname,data[rb],data[rb+1],data[rb+2],data[rb+3]);
        printf("%s\n",gstring);
        //addstring(gstring);
    }
}
#endif /* 0 */