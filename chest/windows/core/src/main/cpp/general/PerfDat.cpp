/*
 * ---------------------------------------------------------------
 * Performance data interface functions
 * ---------------------------------------------------------------
 */

#include <wtypes.h>
#include <tchar.h>
#include <winerror.h>

#include <util/string.h>

#include <win32/PerfDat.h>

/*--------------------------------------------------------------------------*/

/*
 * Formats a counter as long integer
 */
HRESULT PerfDatFormatCounterLong (HPERF_DAT_COUNTER hCounter, long *lpCounter)
{
	DWORD						notUsed=0;
	PDH_FMT_COUNTERVALUE valStruct={ 0 };
	PDH_STATUS				pdh_stat=ERROR_SUCCESS;

	if ((NULL == hCounter) || (NULL == lpCounter))
		return ERROR_BAD_ARGUMENTS;

	pdh_stat = PdhGetFormattedCounterValue(hCounter,
														PDH_FMT_LONG,
														&notUsed,
														&valStruct);

	if (ERROR_SUCCESS == pdh_stat)
		*lpCounter = valStruct.longValue;

	return pdh_stat;
}

/*--------------------------------------------------------------------------*/

/* updates the value of the specified counter */
HRESULT PerfDatUpdateLong (HQUERY hQuery, HCOUNTER hCounter, long *lpCounter)
{
	HRESULT	hr=ERROR_SUCCESS;

	/* Collect data */
	if ((hr=PerfDatCollect(hQuery)) != ERROR_SUCCESS)
		return hr;

	/* Format the result as long int */
	if ((hr=PerfDatFormatCounterLong(hCounter, lpCounter)) != ERROR_SUCCESS)
		return hr;

	return ERROR_SUCCESS;
}

/*--------------------------------------------------------------------------*/

/* retrieves value of specified counter (NOTE: slow operation !!!) */
HRESULT PerfDatGetLong (LPCTSTR pszPerfDatPath, long *lpCounter)
{
	HQUERY					hQuery=NULL;
	CPerfDatQueryGuard	hqg(hQuery);
	HCOUNTER					hCounter=NULL;
	CPerfDatCounterGuard	hcg(hCounter);
	HRESULT					hr=PerfDatOpenQuery(&hQuery);
	if (hr != ERROR_SUCCESS)
		return hr;

	/* Add counter(s) */
	if ((hr=PerfDatAddCounter(hQuery, pszPerfDatPath, &hCounter)) != ERROR_SUCCESS)
		return hr;

	/* Collect data */
	if ((hr=PerfDatUpdateLong(hQuery, hCounter, lpCounter)) != ERROR_SUCCESS)
		return hr;

	return S_OK;
}

/*--------------------------------------------------------------------------*/

HRESULT PerfDatGetProcessCounterPath (LPCTSTR		lpszMachineName,	/* NULL == localhost */
												  LPCTSTR		lpszProcessName,	/* NULL == calling process */
												  LPCTSTR		lpszCounterName,
												  LPTSTR			lpszCounterPath,
												  const DWORD	dwMaxLen)
{
	HRESULT							hr=ERROR_SUCCESS;
	PDH_COUNTER_PATH_ELEMENTS	pdhElements={ 0 };
	TCHAR								szLclName[MAX_PDH_PROCCNTR_NAME_LENGTH+2]=_T("");
	DWORD								dwLen=0;

	if (IsEmptyStr(lpszCounterName))
		return ERROR_BAD_ARGUMENTS;

	if (IsEmptyStr(lpszProcessName))
	{
		TCHAR	szModuleName[MAX_PATH+1]=_T("");
		dwLen = GetModuleFileName(NULL, szModuleName, MAX_PATH);
		if ((0 == dwLen) || (dwLen >= MAX_PATH))
			return ERROR_MORE_DATA;

		/* find ".exe" end */
		LPCTSTR	lpszProcName=strlast(szModuleName);
		for (; lpszProcName > szModuleName; lpszProcName--)
			if (_T('.') == *lpszProcName)
				break;
		if (lpszProcName == szModuleName)
			return ERROR_BAD_FORMAT;

		if (_T('.') == *lpszProcName) 
		{
			*((LPTSTR) lpszProcName) = _T('\0');
			for (lpszProcName--; lpszProcName > szModuleName; lpszProcName--)
				if ((_T('/') == *lpszProcName) || (_T('\\') == *lpszProcName))
					break;

			if ((_T('/') == *lpszProcName) || (_T('\\') == *lpszProcName))
				lpszProcName++;
		}

		_tcsncpy(szLclName, lpszProcName, MAX_PDH_PROCCNTR_NAME_LENGTH);
		szLclName[MAX_PDH_PROCCNTR_NAME_LENGTH] = _T('\0');
		dwLen = _tcslen(szLclName);
	}
	else
	{
		/* find out if have ".exe" */
		LPCTSTR	lpszSuffix=_tcsrchr(lpszProcessName, _T('.'));
		if (!IsEmptyStr(lpszSuffix))
		{
			if (0 == _tcsicmp(lpszSuffix, _T(".exe")))
			{
				const size_t	cLen=(lpszSuffix - lpszProcessName);
				if (0 == cLen)
					return ERROR_NO_DATA;

				_tcsncpy(szLclName, lpszProcessName, min(cLen,MAX_PDH_PROCCNTR_NAME_LENGTH));
			}
			else
				_tcsncpy(szLclName, lpszProcessName, MAX_PDH_PROCCNTR_NAME_LENGTH);

		}
		else	/* no ".exe" */
		{
			_tcsncpy(szLclName, lpszProcessName, MAX_PDH_PROCCNTR_NAME_LENGTH);
		}

		szLclName[MAX_PDH_PROCCNTR_NAME_LENGTH] = _T('\0');
		dwLen = _tcslen(szLclName);
	}

	/*
	 *		The (empirical) counter names rules say that if the process name
	 * when added ".exe" exceeds the MAX_PDH_PROCCNTR_NAME_LENGTH, then the
	 * ".exe" is added until EXACTLY the length of MAX_PDH_PROCCNTR_NAME_LENGTH
	 * is reached (ofcourse, if the original name is already 
	 */
	if (((dwLen + 4 /* _tcslen(".exe") */) > MAX_PDH_PROCCNTR_NAME_LENGTH) && (dwLen < MAX_PDH_PROCCNTR_NAME_LENGTH))
	{
		const size_t	rLen=(MAX_PDH_PROCCNTR_NAME_LENGTH - dwLen);
		_tcsncpy(&szLclName[dwLen], _T(".exe"), rLen);
		szLclName[MAX_PDH_PROCCNTR_NAME_LENGTH] = _T('\0');
	}

	memset(&pdhElements, 0, (sizeof pdhElements));
	pdhElements.szMachineName = (LPTSTR) (IsEmptyStr(lpszMachineName) ? NULL : lpszMachineName);
	pdhElements.szObjectName = PSZ_PROCESS_COUNTERS_GROUP;
	pdhElements.szInstanceName = szLclName;
	pdhElements.dwInstanceIndex = (DWORD) (-1);
	pdhElements.szCounterName = (LPTSTR) lpszCounterName;

	dwLen = dwMaxLen;
	if ((hr=PdhMakeCounterPath(&pdhElements, lpszCounterPath, &dwLen,  0)) != ERROR_SUCCESS)
		return hr;
	if (dwLen >= dwMaxLen)
		return ERROR_MORE_DATA;

	return ERROR_SUCCESS;
}

/*--------------------------------------------------------------------------*/

HRESULT PerfDatAddProcessCounter (HQUERY		hQuery,
											 LPCTSTR		lpszMachineName,	/* NULL == localhost */
											 LPCTSTR		lpszProcessName,	/* NULL == this */
											 LPCTSTR		lpszCounterName,
											 HCOUNTER	*phCounter)
{
	HRESULT					hr=ERROR_SUCCESS;
	HCOUNTER					hCounter=NULL;
	CPerfDatCounterGuard	hcg(hCounter);
	TCHAR						szCntrPath[MAX_PATH+1]=_T("");

	if ((NULL == hQuery) || (NULL == phCounter) || IsEmptyStr(lpszCounterName))
		return ERROR_BAD_ARGUMENTS;
	*phCounter = NULL;

	if ((hr=PerfDatGetProcessCounterPath(lpszMachineName, lpszProcessName, lpszCounterName, szCntrPath, MAX_PATH)) != ERROR_SUCCESS)
		return hr;

	/* Add counter(s) */
	if ((hr=PerfDatAddCounter(hQuery, szCntrPath, &hCounter)) != ERROR_SUCCESS)
		return hr;

	*phCounter = hCounter;
	hCounter = NULL;	// disable auto-release

	return ERROR_SUCCESS;
}

#ifdef __cplusplus
//////////////////////////////////////////////////////////////////////////////

CProcessPerfDatInfoQuery::CProcessPerfDatInfoQuery ()
	: m_hQuery(NULL)
{
	::memset(&m_datInfo, 0, (sizeof m_datInfo));
	::memset(&m_infoDesc, 0, (sizeof m_infoDesc));
}

/*--------------------------------------------------------------------------*/

HRESULT CProcessPerfDatInfoQuery::AddCounters (LPCTSTR lpszMachineName, LPCTSTR lpszProcName)
{
	HRESULT	hr=::PerfDatOpenQuery(&m_hQuery);
	if (hr != S_OK)
		return hr;

	ULONG	ulDdx=0;
	for (; ; ulDdx++)
	{
		PROCDATINFODESC&	pdd=m_infoDesc[ulDdx];
		const LPCTSTR		lpszCntrName=pdd.lpszCntrName;
		if (IsEmptyStr(lpszCntrName) || (NULL == pdd.lpValue))
			break;

		HCOUNTER&	hCntr=pdd.hCntr;
		if (hCntr != NULL)
			return ERROR_INVALID_DOMAIN_STATE;

		if ((hr=::PerfDatAddProcessCounter(m_hQuery, lpszMachineName, lpszProcName, lpszCntrName, &hCntr)) != S_OK)
			return hr;
	}

	// make sure some counters exist
	if (0 == ulDdx)
		return ERROR_NO_DATA;

	return S_OK;
}

/*--------------------------------------------------------------------------*/

// may not be re-initialized unless "Close" called first
HRESULT CProcessPerfDatInfoQuery::Init (LPCTSTR lpszMachineName, // NULL/empty == localhost
													 LPCTSTR lpszProcName)	// NULL/empty == calling process
{
	if (m_hQuery != NULL)
		return ERROR_ALREADY_INITIALIZED;

	const PROCDATINFODESC	tmpDesc[]={
		{	PSZ_PROCESS_ID,					NULL, &m_datInfo.lProcessID		},
		{	PSZ_PROCESS_CPUTIME,				NULL,	&m_datInfo.lCPUTime			},
		{	PSZ_PROCESS_THREADCOUNT,		NULL,	&m_datInfo.lThreadCount		},
		{	PSZ_PROCESS_WORKINGSET,			NULL,	&m_datInfo.lWorkSet			},
		{	PSZ_PROCESS_WORKINGSET_PEAK,	NULL,	&m_datInfo.lWorkSetPeak		},
		{	PSZ_PROCESS_HANDLECOUNT,		NULL,	&m_datInfo.lHandleCount		},
		{	PSZ_PROCESS_VIRTBYTES,			NULL,	&m_datInfo.lVirtBytes		},
		{	PSZ_PROCESS_VIRTBYTES_PEAK,	NULL,	&m_datInfo.lVirtBytesPeak	},
		{	PSZ_PROCESS_PRIVBYTES,			NULL,	&m_datInfo.lPrivBytes		},

		{	NULL,									NULL,	NULL								}	// mark end
	};

	// make sure auto-initializer is not larger than required information
	{
		const ULONG	ulTmpDescsNum=((sizeof tmpDesc) / (sizeof tmpDesc[0]));
		if (ulTmpDescsNum > (MAX_PROCESS_PERFDAT_CNTRS_NUM+1))
			return ERROR_INVALID_SERVER_STATE;
	}

	// initialize counters descriptors
	{
		::memset(&m_infoDesc, 0, (sizeof m_infoDesc));

		for (ULONG	ulDdx=0; ulDdx < MAX_PROCESS_PERFDAT_CNTRS_NUM; ulDdx++)
		{
			const PROCDATINFODESC&	pdd=tmpDesc[ulDdx];
			const LPCTSTR				lpszCntrName=pdd.lpszCntrName;
			if (IsEmptyStr(lpszCntrName) || (NULL == pdd.lpValue))
				break;

			PROCDATINFODESC&	ldd=m_infoDesc[ulDdx];
			ldd = pdd;
		}
	}

	HRESULT	hr=AddCounters(lpszMachineName, lpszProcName);
	if (hr != S_OK)
	{
		Close();
		return hr;
	}

	::memset(&m_datInfo, 0, (sizeof m_datInfo));
	return S_OK;
}

/*--------------------------------------------------------------------------*/

HRESULT CProcessPerfDatInfoQuery::QueryInfo (PROCESS_PERFDAT_INFO& datInfo)
{
	::memset(&datInfo, 0, (sizeof datInfo));

	if (NULL == m_hQuery)
		return ERROR_INVALID_HANDLE;

	HRESULT	hr=::PerfDatCollect(m_hQuery);
	if (hr != S_OK)
		return hr;

	ULONG	ulDdx=0;
	for (; ; ulDdx++)
	{
		PROCDATINFODESC&	pdd=m_infoDesc[ulDdx];
		const LPCTSTR		lpszCntrName=pdd.lpszCntrName;
		if (IsEmptyStr(lpszCntrName) || (NULL == pdd.lpValue))
			break;

		if ((hr=::PerfDatFormatCounterLong(pdd.hCntr, pdd.lpValue)) != S_OK)
			return hr;
	}

	// make sure some counters exist
	if (0 == ulDdx)
		return ERROR_NO_DATA;

	datInfo = m_datInfo;
	return S_OK;
}

/*--------------------------------------------------------------------------*/

HRESULT CProcessPerfDatInfoQuery::Close ()
{
	HRESULT	hr=S_OK, lhr=S_OK;

	for (ULONG	ulDdx=0; ; ulDdx++)
	{
		PROCDATINFODESC&	pdd=m_infoDesc[ulDdx];
		const LPCTSTR		lpszCntrName=pdd.lpszCntrName;
		if (IsEmptyStr(lpszCntrName) || (NULL == pdd.lpValue))
			break;

		HCOUNTER&	hCntr=pdd.hCntr;
		if (NULL == hCntr)
			continue;

		if ((lhr=::PerfDatRemoveCounter(hCntr)) != S_OK)
			hr = lhr;
		hCntr = NULL;
	}
	::memset(&m_infoDesc, 0, (sizeof m_infoDesc));

	if (m_hQuery != NULL)
	{
		if ((lhr=::PerfDatCloseQuery(m_hQuery)) != S_OK)
			hr = lhr;
		m_hQuery = NULL;
	}

	::memset(&m_datInfo, 0, (sizeof m_datInfo));

	return hr;
}

//////////////////////////////////////////////////////////////////////////////

CPerfDatCountersGroup::CPerfDatCountersGroup ()
	: m_hQuery(NULL), m_lpszMachineName(NULL), m_lpszGroupName(NULL), m_cntrsMap()
{
}

/*--------------------------------------------------------------------------*/

HRESULT CPerfDatCountersGroup::Close ()
{
	HRESULT	hr=S_OK, lhr=S_OK;

	if (m_cntrsMap.GetItemsCount() != 0)
	{
		CStr2PtrMapEnum	cme(m_cntrsMap);
		LPCTSTR				lpszCntrName=NULL;
		LPVOID				pVal=NULL;

		for (lhr=cme.GetFirst(lpszCntrName, pVal); S_OK == lhr; lhr=cme.GetNext(lpszCntrName, pVal))
		{
			if (NULL == pVal)
				continue;
			if ((lhr=::PerfDatRemoveCounter((HCOUNTER) pVal)) != S_OK)
				hr = lhr;
		}

		m_cntrsMap.Clear();
	}

	if (m_hQuery != NULL)
	{
		if ((lhr=::PerfDatCloseQuery(m_hQuery)) != S_OK)
			hr = lhr;
		m_hQuery = NULL;
	}

	::strreleasebuf(m_lpszMachineName);
	::strreleasebuf(m_lpszGroupName);

	return hr;
}

/*--------------------------------------------------------------------------*/

// may not be re-initialized unless "Close" called first
HRESULT CPerfDatCountersGroup::Init (LPCTSTR lpszMachineName, LPCTSTR lpszGroupName, const ULONG ulMaxCounters)
{
	HRESULT	hr=S_OK;

	if (IsEmptyStr(lpszGroupName) || (0 == ulMaxCounters))
		return ERROR_BAD_ARGUMENTS;

	if ((m_hQuery != NULL) ||
		 (!IsEmptyStr(m_lpszMachineName)) ||
		 (!IsEmptyStr(m_lpszGroupName)) ||
		 (m_cntrsMap.GetSize() != 0))
		return ERROR_ALREADY_INITIALIZED;

	if (!IsEmptyStr(lpszMachineName))
	{
		if ((hr=::strupdatebuf(lpszMachineName, m_lpszMachineName)) != S_OK)
			return hr;
	}

	if ((hr=::strupdatebuf(lpszGroupName, m_lpszGroupName)) != S_OK)
		return hr;

	if ((hr=m_cntrsMap.InitMap(ulMaxCounters, FALSE)) != S_OK)
		return hr;

	if ((hr=::PerfDatOpenQuery(&m_hQuery)) != S_OK)
		return hr;

	return S_OK;
}

/*--------------------------------------------------------------------------*/

// see "PdhMakeCounterPath" for specifications
HRESULT CPerfDatCountersGroup::AddCounter (LPCTSTR			lpszCntrName,
														 LPCTSTR			lpszParentInstance,
														 LPCTSTR			lpszInstanceName,
														 const DWORD	dwInstance)
{
	if (IsEmptyStr(lpszCntrName))
		return ERROR_BAD_ARGUMENTS;
	if ((NULL == m_hQuery) || IsEmptyStr(m_lpszGroupName) || (0 == m_cntrsMap.GetSize()))
		return ERROR_BAD_ENVIRONMENT;

	LPVOID	pVal=NULL;
	HRESULT	hr=m_cntrsMap.FindKey(lpszCntrName, pVal);
	if (S_OK == hr)
		return ERROR_ALREADY_EXISTS;

	PDH_COUNTER_PATH_ELEMENTS	pdhElements={ 0 };
	pdhElements.szMachineName = (LPTSTR) (IsEmptyStr(m_lpszMachineName) ? NULL : m_lpszMachineName);
	pdhElements.szObjectName = (LPTSTR) m_lpszGroupName;
	pdhElements.szParentInstance = (LPTSTR) (IsEmptyStr(lpszParentInstance) ? NULL : lpszParentInstance);
	pdhElements.szInstanceName = (LPTSTR) (IsEmptyStr(lpszInstanceName) ? NULL : lpszInstanceName);
	pdhElements.dwInstanceIndex = dwInstance;
	pdhElements.szCounterName = (LPTSTR) lpszCntrName;

	TCHAR	szCntrPath[MAX_PATH+2]=_T("");
	DWORD	dwLen=MAX_PATH;
	if ((hr=::PdhMakeCounterPath(&pdhElements, szCntrPath, &dwLen, 0)) != S_OK)
		return hr;
	if (dwLen > MAX_PATH)
		return ERROR_MORE_DATA;

	HCOUNTER					hCounter=NULL;
	CPerfDatCounterGuard	hcg(hCounter);
	if ((hr=::PerfDatAddCounter(m_hQuery, szCntrPath, &hCounter)) != S_OK)
		return hr;

	if ((hr=m_cntrsMap.AddKey(lpszCntrName, (LPVOID) hCounter)) != S_OK)
		return hr;

	hCounter = NULL;	// disable auto-release
	return S_OK;
}

/*--------------------------------------------------------------------------*/

// if (-1) specified as number of counters, then last entry must be empty/NULL
HRESULT CPerfDatCountersGroup::AddCounters (LPCTSTR lpszCntrs[], const ULONG ulCNum)
{
	if ((NULL == m_hQuery) || IsEmptyStr(m_lpszGroupName) || (0 == m_cntrsMap.GetSize()))
		return ERROR_BAD_ENVIRONMENT;

	if (0 == ulCNum)
		return S_OK;

	if (NULL == lpszCntrs)
		return ERROR_BAD_ARGUMENTS;

	for (ULONG	ulCdx=0; ulCdx < ulCNum; ulCdx++)
	{
		const LPCTSTR	lpszCntrName=lpszCntrs[ulCdx];
		if (IsEmptyStr(lpszCntrName))
		{
			if (ulCNum != (ULONG) (-1))
				return ERROR_BAD_NETPATH;
			break;
		}

		HRESULT	hr=AddCounter(lpszCntrName);
		if (hr != S_OK)
			return hr;
	}

	return S_OK;
}

/*--------------------------------------------------------------------------*/

HRESULT CPerfDatCountersGroup::UpdateData ()
{
	if (NULL == m_hQuery)
		return ERROR_INVALID_HANDLE;

	HRESULT	hr=::PerfDatCollect(m_hQuery);
	if (hr != S_OK)
		return hr;

	return S_OK;
}

/*--------------------------------------------------------------------------*/

// Note: "UpdateData" must be called in order for a value (of the same counter) to be updated
HRESULT CPerfDatCountersGroup::GetCounterValue (LPCTSTR lpszCntrName, long& lValue) const
{
	lValue = 0;

	if (NULL == m_hQuery)
		return ERROR_INVALID_SERVER_STATE;

	LPVOID	pVal=NULL;
	HRESULT	hr=m_cntrsMap.FindKey(lpszCntrName, pVal);
	if (hr != S_OK)
		return hr;

	if (NULL == pVal)
		return ERROR_INVALID_HANDLE;

	if ((hr=::PerfDatFormatCounterLong((HCOUNTER) pVal, &lValue)) != S_OK)
		return hr;

	return S_OK;
}

//////////////////////////////////////////////////////////////////////////////

/*
 *		These methods can be used to enumerate values
 *
 * Note: "UpdateData" must be called before starting the enumeration
 *			if you want to have the values up-to-date	
 */
HRESULT CPerfDatCountersGroupEnum::GetFirstCounterValue (LPCTSTR& lpszCntrName, long& lValue)
{
	LPVOID	pVal=NULL;
	HRESULT	hr=m_cme.GetFirst(lpszCntrName, pVal);
	if (hr != S_OK)
		return hr;

	if ((hr=m_dtc.GetCounterValue(lpszCntrName, lValue)) != S_OK)
		return hr;

	return S_OK;
}

/*--------------------------------------------------------------------------*/

HRESULT CPerfDatCountersGroupEnum::GetNextCounterValue (LPCTSTR& lpszCntrName, long& lValue)
{
	LPVOID	pVal=NULL;
	HRESULT	hr=m_cme.GetNext(lpszCntrName, pVal);
	if (hr != S_OK)
		return hr;

	if ((hr=m_dtc.GetCounterValue(lpszCntrName, lValue)) != S_OK)
		return hr;

	return S_OK;
}

//////////////////////////////////////////////////////////////////////////////
#endif	/* __cplusplus */
